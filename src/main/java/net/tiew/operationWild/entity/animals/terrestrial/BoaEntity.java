package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.BoaVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class BoaEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 80.0;

    private BoaTailPart[] parts;

    private double prevChainX = Double.NaN;
    private double prevChainZ = Double.NaN;

    // Tick de jeu (serveur) auquel la dernière proie a été tuée → lance l'onde de digestion
    // qui parcourt la queue. -1 = aucune digestion en cours. Copié vers chaque BoaTailPart.
    private long digestionStartTick = -1L;
    // PV max de la dernière proie tuée → règle la vitesse (plus lent) et l'amplitude (plus gros)
    // de l'onde de digestion. Copié vers chaque BoaTailPart.
    private float digestionPower = 1.0f;

    private float smoothedYRot = Float.NaN;
    public final float[] ringBuffer = new float[64];
    public int ringBufferIndex = -1;

    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> CHILD_UUID =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CHILD_ID =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    // ── Attaque secondaire : Crochets Venimeux (toggle) ──────────────────────
    // VENOM_ARMED        : toggle armé (clic droit) ; consommé au prochain coup de combo touchant.
    // VENOM_COOLDOWN_TICKS : ticks de cooldown restants après une injection (1 min 30). 0 = prêt.
    private static final EntityDataAccessor<Boolean> VENOM_ARMED =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VENOM_COOLDOWN_TICKS =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    // ── Passif : Embuscade Silencieuse ───────────────────────────────────────
    // Vrai quand au moins une menace est détectée à proximité : le Boa devient muet.
    private static final EntityDataAccessor<Boolean> SILENT_AMBUSH =
            SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);

    public BoaEntity(EntityType<? extends BoaEntity> type, Level world,
                     float averageScale, int maxSleepBar, int foodWanted) {
        super(type, world, averageScale, maxSleepBar, foodWanted);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 25.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(2, new BoaEntity.BoaMeleeAttackGoal());

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, this.getSpeed() * 6.5f));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Horse.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, TigerEntity.class, true));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof BoaEntity boa && !boa.isSleeping() && !boa.isNapping()) {
                    super.tick();
                }
            }
        };

        registerBehaviorGoals(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(CHILD_UUID, java.util.Optional.empty());
        builder.define(CHILD_ID, -1);
        builder.define(VENOM_ARMED, false);
        builder.define(VENOM_COOLDOWN_TICKS, 0);
        builder.define(SILENT_AMBUSH, false);
    }

    // ── Crochets Venimeux : état & API ───────────────────────────────────────

    public boolean isVenomArmed() {
        return this.entityData.get(VENOM_ARMED);
    }

    public void setVenomArmed(boolean armed) {
        this.entityData.set(VENOM_ARMED, armed);
    }

    public int getVenomCooldownTicks() {
        return this.entityData.get(VENOM_COOLDOWN_TICKS);
    }

    public void setVenomCooldownTicks(int ticks) {
        this.entityData.set(VENOM_COOLDOWN_TICKS, ticks);
    }

    /**
     * Toggle de l'attaque secondaire (appelé côté serveur via OWAttackPacket).
     * Désarme si déjà armé ; sinon l'arme, sauf si un cooldown est en cours.
     */
    public void toggleVenomFangs() {
        if (this.level().isClientSide()) return;
        if (isVenomArmed()) {
            setVenomArmed(false);
        } else if (getVenomCooldownTicks() <= 0) {
            setVenomArmed(true);
        }
    }

    /**
     * Injecte VENOM I (durée aléatoire 30–60 s) à la cible, désarme le toggle
     * et démarre le cooldown de 1 min 30. Serveur uniquement.
     */
    private void applyVenomFangs(LivingEntity target) {
        int duration = (int) OWUtils.generateRandomInterval(
                OWAttacksConstants.Boa.VENOM_FANGS_MIN_DURATION_TICKS,
                OWAttacksConstants.Boa.VENOM_FANGS_MAX_DURATION_TICKS);
        target.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), duration, 0));
        setVenomArmed(false);
        setVenomCooldownTicks(OWAttacksConstants.Boa.VENOM_FANGS_COOLDOWN_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.BOA_HITTING.get(), SoundSource.HOSTILE, 1.0f, 1.35f);
    }

    // ── Passif : Embuscade Silencieuse ───────────────────────────────────────

    public boolean isInSilentAmbush() {
        return this.entityData.get(SILENT_AMBUSH);
    }

    private void setSilentAmbush(boolean active) {
        if (this.entityData.get(SILENT_AMBUSH) != active) this.entityData.set(SILENT_AMBUSH, active);
    }

    /**
     * Scanne périodiquement les alentours (serveur). Si au moins une menace est détectée,
     * le Boa apprivoisé passe en mode embuscade (muet). Réservé au Boa apprivoisé.
     */
    private void updateSilentAmbush() {
        if (!this.isTame()) {
            setSilentAmbush(false);
            return;
        }
        if (this.tickCount % OWAttacksConstants.Boa.SILENT_AMBUSH_CHECK_INTERVAL != 0) return;

        AABB box = this.getBoundingBox().inflate(OWAttacksConstants.Boa.SILENT_AMBUSH_RADIUS);
        boolean threat = !this.level().getEntitiesOfClass(LivingEntity.class, box, this::isAmbushThreat).isEmpty();
        setSilentAmbush(threat);
    }

    /**
     * Une entité est une "menace" pour l'embuscade si :
     *   • c'est un joueur hors de la tribu (et non le propriétaire) ;
     *   • c'est une créature apprivoisée appartenant à une autre tribu ;
     *   • c'est un mob non apprivoisé hostile ou neutre (les passifs comme les vaches sont ignorés).
     */
    private boolean isAmbushThreat(LivingEntity le) {
        if (le == this || le instanceof BoaTailPart) return false;
        if (!le.isAlive()) return false;
        if (this.isAlliedTo(le)) return false;

        if (le instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) return false;
            if (player.getUUID().equals(this.getOwnerUUID())) return false;
            return !isInMyTribe(player.getUUID());
        }

        if (le instanceof TamableAnimal tamable && tamable.isTame()) {
            java.util.UUID owner = tamable.getOwnerUUID();
            if (owner == null) return true;
            if (owner.equals(this.getOwnerUUID())) return false;
            return !isInMyTribe(owner);
        }

        // Mob non apprivoisé : uniquement hostile (Enemy) ou neutre (NeutralMob).
        return le instanceof Enemy || le instanceof NeutralMob;
    }

    /** Vrai si le joueur fait partie de la tribu du Boa (propriétaire de tribu ou membre). */
    private boolean isInMyTribe(java.util.UUID playerUuid) {
        if (playerUuid == null || this.currentTeam == null) return false;
        if (playerUuid.equals(this.currentTeam.getTeamOwnerUUID())) return true;
        java.util.UUID[] members = this.currentTeam.getTeamPlayersMembers();
        if (members != null) {
            for (java.util.UUID u : members) {
                if (playerUuid.equals(u)) return true;
            }
        }
        return false;
    }

    @Override
    public int getEntityColor() {
        return 0x566022;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.MARAUDER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.CARNIVOROUS;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.AGGRESSIVE;
    }

    @Override
    public List<Class<?>> getFavoriteTargets() {
        return List.of(Animal.class, Player.class, TigerEntity.class, Horse.class);
    }

    @Override
    public boolean preferRawMeat() {
        return true;
    }

    @Override
    public boolean preferCookedMeat() {
        return false;
    }

    @Override
    public boolean preferVegetables() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        return 0.05f;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.BOA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getTheoreticalScale() {
        return 8;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.2f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1.0f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 1.0f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return false;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return false;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.BOA_SADDLE.get();
    }

    @Override
    public float getMaxVitalEnergy() {
        return 200.0f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.5f;
    }

    @Override
    public int getMaxAirSupply() {
        return 300 * 10;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 20 : 10;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
    }

    @Override
    public void tick() {
        super.tick();

        createCombo(16, 10, OWSounds.BOA_HITTING.get(), 2.0, 2.5, 1.25, false, 0.25f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (Float.isNaN(this.smoothedYRot)) {
            this.smoothedYRot = this.getYRot();
        }
        if (this.isVehicle() && this.getControllingPassenger() != null) {
            this.smoothedYRot = this.getYRot();
            this.yBodyRot = this.getYRot();
        } else {
            float groundSpeed = (this.getTarget() != null) ? 0.5f : this.getRotationSpeed();
            float visualDelta = Mth.wrapDegrees(this.getYRot() - this.smoothedYRot);
            this.smoothedYRot = Mth.wrapDegrees(this.smoothedYRot + visualDelta * groundSpeed);
            this.yBodyRot = this.smoothedYRot;
        }
        this.yHeadRot = Mth.clamp(this.yHeadRot, this.yBodyRot - 70, this.yBodyRot + 70);

        if (this.ringBufferIndex < 0) {
            for (int i = 0; i < this.ringBuffer.length; ++i) {
                this.ringBuffer[i] = this.smoothedYRot;
            }
        }
        this.ringBufferIndex++;
        if (this.ringBufferIndex == this.ringBuffer.length) {
            this.ringBufferIndex = 0;
        }
        this.ringBuffer[this.ringBufferIndex] = this.smoothedYRot;

        double realVelX = 0, realVelZ = 0;
        if (!Double.isNaN(prevChainX)) {
            realVelX = this.getX() - prevChainX;
            realVelZ = this.getZ() - prevChainZ;
        }
        prevChainX = this.getX();
        prevChainZ = this.getZ();

        if (!this.level().isClientSide()) {
            // Cooldown des Crochets Venimeux
            int venomCd = getVenomCooldownTicks();
            if (venomCd > 0) setVenomCooldownTicks(venomCd - 1);

            // Passif : Embuscade Silencieuse
            updateSilentAmbush();

            final int segments = 7;
            final Entity child = getChild();
            if (child == null) {
                LivingEntity partParent = this;
                parts = new BoaTailPart[segments];
                BoaPartIndex partIndex = BoaPartIndex.HEAD;
                Vec3 prevPos = this.position();
                for (int i = 0; i < segments; i++) {
                    final float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                    final float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                    BoaTailPart part = new BoaTailPart(OWEntityRegistry.BOA_TAIL_PART.get(), this);
                    part.setParent(partParent);
                    part.copyDataFrom(this);
                    part.setBodyIndex(i);
                    part.setPartType(BoaPartIndex.sizeAt(1 + i));
                    if (partParent == this) {
                        this.setChildId(part.getUUID());
                        this.entityData.set(CHILD_ID, part.getId());
                    }
                    if (partParent instanceof BoaTailPart) {
                        ((BoaTailPart) partParent).setChildId(part.getUUID());
                    }
                    part.setPos(part.tickMultipartPosition(this.getId(), partIndex, prevPos, this.getXRot(), prevReqRot, reqRot, false));
                    partParent = part;
                    level().addFreshEntity(part);
                    parts[i] = part;
                    partIndex = part.getPartType();
                    prevPos = part.position();
                }
            }
            if (shouldReplaceParts() && this.getChild() instanceof BoaTailPart) {
                parts = new BoaTailPart[segments];
                parts[0] = (BoaTailPart) this.getChild();
                this.entityData.set(CHILD_ID, parts[0].getId());
                int i = 1;
                while (i < parts.length && parts[i - 1].getChild() instanceof BoaTailPart) {
                    parts[i] = (BoaTailPart) parts[i - 1].getChild();
                    i++;
                }
            }
            if (parts != null) {
                BoaPartIndex partIndex = BoaPartIndex.HEAD;
                float offset = this.isVehicle() ? 3 : 1;
                Vec3 prev = this.position().add(realVelX * offset, 0, realVelZ * offset);
                float xRot = this.getXRot();

                // Vague verticale autorisee des que le boa est dans l'eau, qu'il soit
                // monte (vehicule) ou non : l'ondulation horizontale existe deja dans les
                // deux cas, on aligne la verticale dessus.
                boolean swimming = this.isInWater();
                float swimFreq = 3.0f;
                float swimStep = 0.85f;
                float swimAmpDeg = 18f; // amplitude de la vague en degrés de XRot

                for (int i = 0; i < segments; i++) {
                    if (this.parts[i] != null) {
                        final float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                        final float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                        parts[i].copyDataFrom(this);
                        prev = parts[i].tickMultipartPosition(this.getId(), partIndex, prev, xRot, prevReqRot, reqRot, true);
                        partIndex = parts[i].getPartType();
                        xRot = parts[i].getXRot();
                    }
                }

                // Vague verticale : on ajoute uniquement au XRot, sans toucher aux positions
                // En eau le XRot naturel de tickMultipartPosition est ~0 (terrain = 0 dans les fluides)
                if (swimming) {
                    for (int i = 0; i < segments; i++) {
                        if (parts[i] != null) {
                            float wave = swimAmpDeg * (float) Math.sin(this.walkDist * swimFreq - i * swimStep);
                            parts[i].setXRot(Mth.clamp(parts[i].getXRot() + wave, -25f, 25f));
                        }
                    }
                }

                // Supprime silencieusement les nénuphars sur le trajet de toute la chaîne
                clearLilyPad(this.blockPosition());
                clearLilyPad(this.blockPosition().above());
                for (int i = 0; i < segments; i++) {
                    if (parts[i] != null) {
                        clearLilyPad(parts[i].blockPosition());
                        clearLilyPad(parts[i].blockPosition().above());
                    }
                }
            }
        }
    }

    private void clearLilyPad(BlockPos pos) {
        if (this.level().isClientSide()) return;
        var state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof WaterlilyBlock) {
            // Remplace par le fluide sous-jacent (eau) — aucun son, aucune particule
            this.level().setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && isNapping()) {
            return;
        }

        super.setTarget(target);
    }

    private boolean shouldReplaceParts() {
        if (parts == null || parts[0] == null) return true;
        for (int i = 0; i < 7; i++) {
            if (parts[i] == null) return true;
        }
        return false;
    }

    @Nullable
    public java.util.UUID getChildId() {
        return this.entityData.get(CHILD_UUID).orElse(null);
    }

    public void setChildId(@Nullable java.util.UUID uniqueId) {
        this.entityData.set(CHILD_UUID, java.util.Optional.ofNullable(uniqueId));
    }

    public Entity getChild() {
        java.util.UUID id = getChildId();
        if (id != null && !level().isClientSide) {
            return ((net.minecraft.server.level.ServerLevel) level()).getEntity(id);
        }
        return null;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    private ItemStack createSoulStack() {
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());
        Item item = soulStack.getItem();

        if (item instanceof AnimalSoulItem animalSoulItem) {
            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack,
                    new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));

            animalSoulItem.saveEntityType(fakeContext, Component.nullToEmpty(this.getClass().getSimpleName()));
            animalSoulItem.saveEntityOwner(fakeContext, Component.nullToEmpty(this.getOwner() != null ? this.getOwner().getName().getString() : ""));
            animalSoulItem.saveEntityGender(fakeContext, this.isMale());
            animalSoulItem.saveEntityMaxHealth(fakeContext, this.getMaxHealth());
            animalSoulItem.saveEntityDamages(fakeContext, this.getDamage());
            animalSoulItem.saveEntitySpeed(fakeContext, this.getSpeed());
            animalSoulItem.saveEntityScale(fakeContext, this.getScale());
            animalSoulItem.saveEntityLevel(fakeContext, this.getLevel());
            animalSoulItem.saveEntityVariant(fakeContext, this.getVariant().getId());
        }

        return soulStack;
    }

    private float getYawForPart(int i) {
        return this.getRingBuffer(4 + i * 2, 1.0F);
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    public float calcPartRotation(int i) {
        return (float) (40 * -Math.sin(this.walkDist * 3 - i));
    }

    public float getRingBuffer(int bufferOffset, float partialTicks) {
        if (this.isDeadOrDying()) {
            partialTicks = 0.0F;
        }
        partialTicks = 1.0F - partialTicks;
        final int i = this.ringBufferIndex - bufferOffset & 63;
        final int j = this.ringBufferIndex - bufferOffset - 1 & 63;
        final float d0 = this.ringBuffer[i];
        final float d1 = this.ringBuffer[j] - d0;
        return Mth.wrapDegrees(d0 + d1 * partialTicks);
    }

    public BoaVariant getVariant() {
        return BoaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(BoaVariant v) {
        this.entityData.set(VARIANT, v.getId() & 255);
    }

    public BoaVariant getInitialVariant() {
        return BoaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(BoaVariant v) {
        this.entityData.set(DATA_INITIAL_VARIANT, v.getId());
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
        if (skinIndex == 1) this.setVariant(BoaVariant.Cosmetics.GOLD.variant);
        if (skinIndex == 4) this.setVariant(BoaVariant.Cosmetics.LEVIATHAN.variant);
        if (skinIndex == 5) this.setVariant(BoaVariant.Cosmetics.PLUSH.variant);
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !(entity instanceof BoaTailPart) && super.canCollideWith(entity);
    }

    @Override
    protected void pushEntities() {
        if (this.level().isClientSide()) {
            super.pushEntities();
            return;
        }
        this.level().getEntities(this, this.getBoundingBox(),
                entity -> !(entity instanceof BoaTailPart) && EntitySelector.pushableBy(this).test(entity))
                .forEach(this::doPush);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof BoaTailPart) {
            return true;
        }
        if (entity instanceof BoaEntity otherBoa) {
            if (otherBoa.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherBoa.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherBoa.getOwnerUUID());
            } else {
                return !otherBoa.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    // ── Digestion : onde qui parcourt la queue après une mise à mort ─────────

    /** Tick de jeu du début de la digestion (-1 = aucune). Lu par les BoaTailPart pour l'anim. */
    public long getDigestionStartTick() {
        return this.digestionStartTick;
    }

    /** Puissance de la proie digérée (= ses PV max). Règle vitesse et amplitude de l'onde. */
    public float getDigestionPower() {
        return this.digestionPower;
    }

    /** Relance l'onde de digestion depuis la tête, calibrée sur la puissance de la proie. */
    private void triggerDigestion(float preyMaxHealth) {
        this.digestionStartTick = this.level().getGameTime();
        this.digestionPower = preyMaxHealth;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        triggerDigestion(target.getMaxHealth());
        return super.killedEntity(level, target);
    }

    @Override
    protected void onSuccessfulHit(LivingEntity entity) {
        // Boa apprivoisé monté : si les Crochets Venimeux sont armés, ce coup de combo injecte le venin.
        if (this.isTame() && this.isVehicle() && this.isVenomArmed()) {
            applyVenomFangs(entity);
            return;
        }
        if (RANDOM(10)) {
            if (!entity.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) && !this.isVehicle()) {
                entity.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) OWUtils.generateRandomInterval(6500, 9000), 0));
            }
        } else if (RANDOM(5)) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, (int) OWUtils.generateRandomInterval(180, 350), 1));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor,
                                        DifficultyInstance difficultyInstance,
                                        MobSpawnType mobSpawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this,
                    this.getAttributeBaseValue(Attributes.MAX_HEALTH),
                    this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE),
                    this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setVariant(chooseBoaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private BoaVariant chooseBoaVariant() {
        int roll = RANDOM.nextInt(10);
        if (roll < 2.5) return BoaVariant.DEFAULT;
        if (roll < 5) return BoaVariant.YELLOW;
        if (roll < 7.5) return BoaVariant.BROWN;
        return BoaVariant.DARK;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("initialVariant"));
        this.setVariant(BoaVariant.byId(tag.getInt("variant")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("initialVariant", this.getInitialVariant().getId());
        tag.putInt("variant", this.getVariant().getId());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return OWSounds.BOA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OWSounds.BOA_HURT.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // Embuscade Silencieuse : plus aucun son d'idle quand une menace est détectée.
        if (isInSilentAmbush()) return null;
        return OWSounds.BOA_IDLE_1.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        // Embuscade Silencieuse : plus aucun bruit de déplacement quand une menace est détectée.
        if (isInSilentAmbush()) return;
        super.playStepSound(blockPos, blockState);
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        float yOffset = (float) (this.getBbHeight() * 0.75);
        double yaw = Math.toRadians(this.getYRot());
        callback.accept(passenger,
                this.getX() - Math.sin(yaw) * 0.0,
                this.getY() + yOffset,
                this.getZ() + Math.cos(yaw) * 0.0);
    }

    private void setupAnimationState() {
        createSitAnimation(83, true);
    }

    class BoaMeleeAttackGoal extends MeleeAttackGoal {
        public BoaMeleeAttackGoal() {
            super(BoaEntity.this, 2.25f, true);
        }

        @Override
        public boolean canUse() {
            return !BoaEntity.this.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !BoaEntity.this.isTame() && super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            BoaEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            BoaEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;
            if (this.mob instanceof OWEntity owEntity) {
                if (!owEntity.isCombo()) owEntity.setCombo(true, 1);
                else if (owEntity.isPauseCombo()) owEntity.playerContinueCombo = true;
            }
        }
    }
}