package net.tiew.operationWild.entity.animals.terrestrial;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.tiew.operationWild.block.OWBlocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * COPIE STRICTE d'EntityAnacondaPart (Alex's Mobs), adaptee a Operation W.I.L.D.
 *
 * Differences volontaires, minimales :
 *   - 7 modeles distincts au lieu de 4 types : on stocke bodyIndex (0..6) et le
 *     renderer choisit models[bodyIndex]. Le partType (BoaPartIndex) ne sert
 *     qu'au backOffset, comme chez Alex.
 *   - hurt() redirige vers le parent BoaEntity (au lieu d'EntityAnaconda).
 *   - pas de strangle, pas de swell, pas de shedding (specifiques a l'anaconda).
 *
 * Tout le reste (tickMultipartPosition, getLowPartHeight/getHighPartHeight,
 * isOpaqueBlockAt, methodes LivingEntity obligatoires) est copie a l'identique.
 */
public class BoaTailPart extends LivingEntity implements IHurtableMultipart {

    private static final EntityDataAccessor<Integer> BODYINDEX =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BODY_TYPE =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEALTH_RATIO =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<BlockPos> HEAD_POS =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Long> DIGEST_START =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> DIGEST_POWER =
            SynchedEntityData.defineId(BoaTailPart.class, EntityDataSerializers.FLOAT);

    private double prevHeight = 0;
    private int headEntityId = -1;

    public BoaTailPart(EntityType<? extends LivingEntity> t, Level world) {
        super(t, world);
    }

    public BoaTailPart(EntityType<? extends LivingEntity> t, LivingEntity parent) {
        super(t, parent.level());
        this.setParent(parent);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15F);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return this.getParent() == null ? super.interact(player, hand) : this.getParent().interact(player, hand);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || super.isInvulnerableTo(source);
    }

    /** Empeche toute detection de suffocation (les segments traversent les blocs). */
    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.tickCount > 1) {
            final Entity parent = getParent();
            refreshDimensions();
            if (!this.level().isClientSide) {
                if (parent == null) {
                    this.remove(RemovalReason.DISCARDED);
                }
                if (parent != null) {
                    if (parent instanceof final LivingEntity livingEntityParent) {
                        if (livingEntityParent.hurtTime > 0 || livingEntityParent.deathTime > 0) {
                            // Synchro de l'effet de degats vers tous les clients.
                            net.tiew.operationWild.networking.OWNetworkHandler.sendToAllClients(
                                    new net.tiew.operationWild.networking.packets.to_client.MessageHurtMultipart(
                                            this.getId(), parent.getId()), (ServerLevel) this.level());
                            this.hurtTime = livingEntityParent.hurtTime;
                            this.deathTime = livingEntityParent.deathTime;
                        }
                    }
                    if (parent.isRemoved()) {
                        this.remove(RemovalReason.DISCARDED);
                    }
                } else if (tickCount > 20) {
                    remove(RemovalReason.DISCARDED);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    //  POSITIONNEMENT DE LA CHAINE — COPIE STRICTE d'EntityAnacondaPart
    // -------------------------------------------------------------------------

    public Vec3 tickMultipartPosition(int headId, BoaPartIndex parentIndex, Vec3 parentPosition,
                                      float parentXRot, float parentYRot, float ourYRot, boolean doHeight) {
        final Vec3 parentButt = parentPosition.add(
                calcOffsetVec(-parentIndex.getBackOffset() * this.getBoaScale(), parentXRot, parentYRot));
        final Vec3 ourButt = parentButt.add(
                calcOffsetVec((-this.getPartType().getBackOffset() - 0.5F * this.getBbWidth()) * this.getBoaScale(),
                        this.getXRot(), ourYRot));
        // APPROCHE 2 (anti-decollement vertical) : on rapproche le Y du bout de notre
        // segment de celui de son point d'attache au parent. calcOffsetVec incline le
        // segment selon SON pitch, ce qui peut ecarter verticalement la jonction du
        // point d'attache parent sur les pentes/marches. On corrige UNIQUEMENT le Y
        // (X/Z intacts => ondulation laterale preservee), en melangeant vers
        // parentButt.y. 0.5 = compromis ; monte vers 1 pour coller plus fort
        // (segments plus horizontaux), baisse vers 0 pour le comportement Alex d'origine.
        final double JOINT_VERTICAL_GLUE = 0.5D;
        final Vec3 ourButtGlued = new Vec3(
                ourButt.x,
                ourButt.y + (parentButt.y - ourButt.y) * JOINT_VERTICAL_GLUE,
                ourButt.z);
        final Vec3 avg = new Vec3((parentButt.x + ourButtGlued.x) / 2F, (parentButt.y + ourButtGlued.y) / 2F, (parentButt.z + ourButtGlued.z) / 2F);
        final double d0 = parentButt.x - ourButt.x;
        final double d2 = parentButt.z - ourButt.z;
        final double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        final double hgt = doHeight ? (getLowPartHeight(parentButt.x, parentButt.y, parentButt.z) + getHighPartHeight(ourButt.x, ourButt.y, ourButt.z)) : 0;
        // PISTE 5 : lissage progressif de la hauteur au lieu d'un saut au seuil 0.2.
        // prevHeight glisse vers hgt d'une fraction par tick, ce qui rend les montees
        // et descentes de blocs beaucoup plus organiques (plus de palier brutal).
        // 0.35 = vitesse de rattrapage ; baisse pour plus doux, monte pour plus reactif.
        prevHeight += (hgt - prevHeight) * 0.35D;
        final double partYDest = Mth.clamp(this.getBoaScale() * prevHeight, -0.6F, 0.6F);
        final float f = (float) (Mth.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
        final float rawAngle = Mth.wrapDegrees((float) (-(Mth.atan2(partYDest, d3) * Mth.RAD_TO_DEG)));
        // PISTE 2 : pas de correction du pitch adouci (7 au lieu de 10 deg/tick) pour
        // des transitions verticales moins saccadees sur les marches.
        final float f2 = this.limitAngle(this.getXRot(), rawAngle, 7F);
        this.setXRot(f2);
        this.setYRot(f);
        this.yHeadRot = f;
        // PISTE 4 : au tout premier positionnement (doHeight=false a la creation),
        // on aligne les valeurs "old" de rotation sur les valeurs courantes. Sans ca,
        // l'interpolation client part d'un yaw=0 et fait balayer le segment d'un grand
        // arc au spawn ou au rechargement. On evite ce balayage parasite.
        if (!doHeight) {
            this.yRotO = f;
            this.xRotO = f2;
            this.yHeadRotO = f;
        }
        this.moveTo(avg.x, avg.y, avg.z, f, f2);
        headEntityId = headId;
        return avg;
    }

    public double getLowPartHeight(double x, double yIn, double z) {
        if (isFluidAt(x, yIn, z))
            return 0.0D;
        double checkAt = 0D;
        while (checkAt > -3D && !isOpaqueBlockAt(x, yIn + checkAt, z)) {
            checkAt -= 0.2D;
        }
        return checkAt;
    }

    public double getHighPartHeight(double x, double yIn, double z) {
        if (isFluidAt(x, yIn, z))
            return 0.0D;
        double checkAt = 0D;
        while (checkAt <= 3D) {
            if (isOpaqueBlockAt(x, yIn + checkAt, z)) {
                checkAt += 0.2D;
            } else {
                break;
            }
        }
        return checkAt;
    }

    public boolean isOpaqueBlockAt(double x, double y, double z) {
        if (this.noPhysics) {
            return false;
        } else {
            final double d = 1D;
            final Vec3 vec3 = new Vec3(x, y, z);
            final AABB axisAlignedBB = AABB.ofSize(vec3, d, 1.0E-6D, d);
            return this.level().getBlockStates(axisAlignedBB).filter(Predicate.not(BlockBehaviour.BlockStateBase::isAir)).anyMatch((state) -> {
                BlockPos blockpos = BlockPos.containing(vec3.x, vec3.y, vec3.z);
                return state.isSuffocating(this.level(), blockpos) && Shapes.joinIsNotEmpty(state.getCollisionShape(this.level(), blockpos).move(vec3.x, vec3.y, vec3.z), Shapes.create(axisAlignedBB), BooleanOp.AND);
            });
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean isFluidAt(double x, double y, double z) {
        if (this.noPhysics) {
            return false;
        } else {
            return !level().getFluidState(BlockPos.containing(x, y, z)).isEmpty();
        }
    }

    public boolean hurtHeadId(DamageSource source, float f) {
        if (headEntityId != -1) {
            Entity e = level().getEntity(headEntityId);
            if (e instanceof BoaEntity) {
                return e.hurt(source, f);
            }
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return hurtHeadId(source, damage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHILD_UUID, Optional.empty());
        builder.define(PARENT_UUID, Optional.empty());
        builder.define(BODYINDEX, 0);
        builder.define(BODY_TYPE, BoaPartIndex.SEG0.ordinal());
        builder.define(VARIANT, 0);
        builder.define(SCALE, 1.0F);
        builder.define(HEALTH_RATIO, 1.0F);
        builder.define(HEAD_POS, BlockPos.ZERO);
        builder.define(DIGEST_START, -1L);
        builder.define(DIGEST_POWER, 1.0F);
    }

    /** Tick de jeu du début de la digestion (copié du Boa parent). -1 = aucune. */
    public long getDigestionStartTick() {
        return this.entityData.get(DIGEST_START);
    }

    /** Puissance de la proie digérée (= ses PV max, copié du Boa parent). */
    public float getDigestionPower() {
        return this.entityData.get(DIGEST_POWER);
    }

    @Override
    public void pushEntities() {
        final List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(0.2D, 0.0D, 0.2D));
        final Entity parent = this.getParent();
        if (parent != null) {
            entities.stream().filter(entity -> !entity.is(parent) && !(entity instanceof BoaTailPart || entity instanceof BoaEntity) && entity.isPushable()).forEach(entity -> entity.push(parent));
        }
    }

    // --- Methodes LivingEntity obligatoires (copie d'Alex) ---

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return ImmutableList.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void onAttackedFromServer(LivingEntity parent, float damage, DamageSource damageSource) {
        if (parent.deathTime > 0)
            this.deathTime = parent.deathTime;
        if (parent.hurtTime > 0)
            this.hurtTime = parent.hurtTime;
    }

    // --- Liens parent / enfant (UUID), copie d'Alex ---

    @Nullable
    public Entity getParent() {
        if (!this.level().isClientSide) {
            final UUID id = getParentId();
            if (id != null) {
                return ((ServerLevel) level()).getEntity(id);
            }
        }
        return null;
    }

    /**
     * Copie les donnees de rendu du parent vers la part (cote serveur, synchronise
     * ensuite vers le client). Approche d'Alex (copyDataFrom) : la part porte son
     * propre variant, donc le renderer n'a PAS besoin de resoudre le parent client.
     */
    public void copyDataFrom(BoaEntity boa) {
        this.entityData.set(VARIANT, boa.getTypeVariant());
        this.entityData.set(SCALE, boa.getScale());
        float maxH = boa.getMaxHealth();
        this.entityData.set(HEALTH_RATIO, maxH > 0 ? boa.getHealth() / maxH : 1.0F);
        BlockPos headPos = boa.blockPosition();
        BlockState stateAtHead = boa.level().getBlockState(headPos);
        boolean inMud = stateAtHead.is(Blocks.MUD) || stateAtHead.is(OWBlocks.MARKED_MUD.get());
        boolean inFluid = !stateAtHead.getFluidState().isEmpty();
        if (inMud || inFluid) {
            headPos = headPos.above();
        }
        this.entityData.set(HEAD_POS, headPos);
        this.entityData.set(DIGEST_START, boa.getDigestionStartTick());
        this.entityData.set(DIGEST_POWER, boa.getDigestionPower());
    }

    /**
     * Position du Boa parent (la tete), synchronisee. Le renderer echantillonne la
     * lumiere a cette position au lieu de celle du segment : ainsi un segment enfoui
     * dans un bloc ne devient PAS noir, il garde l'eclairage de la tete. Et si la
     * tete est dans le noir, toute la queue s'assombrit de la meme facon.
     */
    public BlockPos getHeadPos() {
        return this.entityData.get(HEAD_POS);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    /**
     * Ratio de PV du Boa parent (0..1), copie via copyDataFrom. Le layer de sang
     * l'utilise au lieu des PV du segment, qui restent toujours au max (le hurt
     * du segment est redirige vers la tete).
     */
    public float getParentHealthRatio() {
        return this.entityData.get(HEALTH_RATIO);
    }

    /**
     * Echelle du segment = echelle du Boa parent (copiee via copyDataFrom).
     * On NE touche PAS au getScale() vanilla de LivingEntity (ton OWEntity ne le
     * fait pas non plus) : l'echelle est appliquee a la main dans le renderer,
     * comme OWEntityRenderer le fait pour la tete.
     */
    public float getBoaScale() {
        return this.entityData.get(SCALE);
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUUID());
    }

    @Nullable
    public UUID getParentId() {
        return this.entityData.get(PARENT_UUID).orElse(null);
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.entityData.set(PARENT_UUID, Optional.ofNullable(uniqueId));
    }

    public Entity getChild() {
        if (!this.level().isClientSide) {
            final UUID id = getChildId();
            if (id != null) {
                return ((ServerLevel) level()).getEntity(id);
            }
        }
        return null;
    }

    @Nullable
    public UUID getChildId() {
        return this.entityData.get(CHILD_UUID).orElse(null);
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getParentId() != null) compound.putUUID("ParentUUID", this.getParentId());
        if (this.getChildId() != null) compound.putUUID("ChildUUID", this.getChildId());
        compound.putInt("BodyModel", getPartType().ordinal());
        compound.putInt("BodyIndex", getBodyIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("ParentUUID")) this.setParentId(compound.getUUID("ParentUUID"));
        if (compound.hasUUID("ChildUUID")) this.setChildId(compound.getUUID("ChildUUID"));
        this.setPartType(BoaPartIndex.fromOrdinal(compound.getInt("BodyModel")));
        this.setBodyIndex(compound.getInt("BodyIndex"));
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        Entity parent = this.getParent();
        return parent != null ? parent.getPickResult() : ItemStack.EMPTY;
    }

    public int getBodyIndex() {
        return this.entityData.get(BODYINDEX);
    }

    public void setBodyIndex(int index) {
        this.entityData.set(BODYINDEX, index);
    }

    public BoaPartIndex getPartType() {
        return BoaPartIndex.fromOrdinal(this.entityData.get(BODY_TYPE));
    }

    public void setPartType(BoaPartIndex index) {
        this.entityData.set(BODY_TYPE, index.ordinal());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}