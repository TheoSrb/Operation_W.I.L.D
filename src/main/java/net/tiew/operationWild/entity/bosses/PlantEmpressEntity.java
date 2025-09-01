package net.tiew.operationWild.entity.bosses;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.misc.PrimitiveSpearProjectileEntity;
import net.tiew.operationWild.entity.misc.SlingshotProjectile;
import net.tiew.operationWild.entity.misc.VenomousArrow;
import net.tiew.operationWild.entity.variants.PlantEmpressVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;
import java.util.Map;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class PlantEmpressEntity extends OWEntity implements OWEntityUtils {

    public String[] quests = {};
    public boolean test = false;
    private int bodyPartHitCooldown = 0;

    public final AnimationState head2DeathAnimationState = new AnimationState();
    public final AnimationState head3DeathAnimationState = new AnimationState();
    public final AnimationState head2DeathLoopAnimationState = new AnimationState();
    public final AnimationState head3DeathLoopAnimationState = new AnimationState();
    public int head2DeathAnimationTimeout = 0;
    public int head3DeathAnimationTimeout = 0;

    public int head2DeathTimer;
    public int head3DeathTimer;


    private int musicTimer = 0;
    private boolean musicPlayed = false;

    private static final EntityDataAccessor<Integer> HEAD_1_HEALTH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAD_2_HEALTH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAD_3_HEALTH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HEAD_2_DEATH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAD_2_DEATH_LOOP = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAD_3_DEATH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAD_3_DEATH_LOOP = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);

    public PlantEmpressVariant getVariant() { return PlantEmpressVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(PlantEmpressVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public PlantEmpressVariant getInitialVariant() { return PlantEmpressVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(PlantEmpressVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public PlantEmpressEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 300.0D).add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.FOLLOW_RANGE, 30.0D).add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() { return 1f;}

    public int getHead1Life() { return this.entityData.get(HEAD_1_HEALTH);}
    public void setHead1Life(int health) { this.entityData.set(HEAD_1_HEALTH, health);}
    public int getHead2Life() { return this.entityData.get(HEAD_2_HEALTH);}
    public void setHead2Life(int health) { this.entityData.set(HEAD_2_HEALTH, health);}
    public int getHead3Life() { return this.entityData.get(HEAD_3_HEALTH);}
    public void setHead3Life(int health) { this.entityData.set(HEAD_3_HEALTH, health);}

    public boolean isHead2Dead() { return this.entityData.get(HEAD_2_DEATH);}
    public void setHead2Dead(boolean isHead2Dead) { this.entityData.set(HEAD_2_DEATH, isHead2Dead);}
    public boolean isHead2DeadLoop() { return this.entityData.get(HEAD_2_DEATH_LOOP);}
    public void setHead2DeadLoop(boolean isHead2Dead) { this.entityData.set(HEAD_2_DEATH_LOOP, isHead2Dead);}

    public boolean isHead3Dead() { return this.entityData.get(HEAD_3_DEATH);}
    public void setHead3Dead(boolean isHead3Dead) { this.entityData.set(HEAD_3_DEATH, isHead3Dead);}
    public boolean isHead3DeadLoop() { return this.entityData.get(HEAD_3_DEATH_LOOP);}
    public void setHead3DeadLoop(boolean isHead3Dead) { this.entityData.set(HEAD_3_DEATH_LOOP, isHead3Dead);}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        super.playStepSound(blockPos, blockState);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Minecraft.getInstance().getSoundManager().stop();
        musicPlayed = false;
        musicTimer = 0;
        this.playSound(OWSounds.PLANT_EMPRESS_DEFEATED_THEME.get(), 1.0f, 1.0f);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void tick() {
        super.tick();
        LivingEntity target = this.getTarget();
        if (this.level().isClientSide()) setupAnimationState();



        musicTimer++;
        if (musicTimer == 2880) {
            this.playSound(OWSounds.PLANT_EMPRESS_THEME.get(), 1.0F, 1.0F);
            musicTimer = 0;
        }



        if (getHead1Life() + getHead2Life() + getHead3Life() > 0 && this.isAlive()) {
            this.setHealth(getHead1Life() + getHead2Life() + getHead3Life());

            System.out.println(getHead1Life() + " " + getHead2Life() + " " + getHead3Life() + " " + getHealth());
        }
        else this.kill();

        if (isHead2Dead()) {
            head2DeathTimer++;

            if (head2DeathTimer >= 54) {
                head2DeathTimer = 0;
                setHead2Dead(false);
                setHead2DeadLoop(true);
            }
        }

        if (isHead3Dead()) {
            head3DeathTimer++;

            if (head3DeathTimer >= 54) {
                head3DeathTimer = 0;
                setHead3Dead(false);
                setHead3DeadLoop(true);
            }
        }

        if (getHead1Life() <= 0) setHead1Life(0);
        if (getHead2Life() <= 0) setHead2Life(0);
        if (getHead3Life() <= 0) setHead3Life(0);

        if (this.onGround()) this.setDeltaMovement(Vec3.ZERO);

        if (target != null) {
            this.setLookAt(target.getX(), target.getY(), target.getZ());
        }

        if (!this.level().isClientSide) {
            if (bodyPartHitCooldown > 0) {
                bodyPartHitCooldown--;
            }

            if (bodyPartHitCooldown <= 0) {
                AABB head1AABB = bodyParts.get("head_1");
                AABB head2AABB = bodyParts.get("head_2");
                AABB head3AABB = bodyParts.get("head_3");
                if (head1AABB != null) {
                    AABB worldAABB = head1AABB.move(this.position());

                    List<Entity> entitiesInZone = this.level().getEntities(this, worldAABB);
                    for (Entity entity : entitiesInZone) {

                        if (entity instanceof Projectile) {
                            Projectile projectile = (Projectile) entity;
                            float damage = 0.0f;

                            if (projectile instanceof AbstractArrow) {
                                AbstractArrow arrow = (AbstractArrow) projectile;
                                damage = (float) arrow.getBaseDamage();
                            }
                            else if (projectile instanceof SlingshotProjectile) damage = 2.0f;
                            else if (projectile instanceof PrimitiveSpearProjectileEntity spear) damage = (float) spear.getBaseDamage();
                            else if (projectile instanceof ThrownTrident trident) damage = (float) trident.getBaseDamage();

                            handleBodyPartHit("head_1", damage, entity);
                            bodyPartHitCooldown = 20;
                            break;
                        }
                    }
                }
                if (head2AABB != null && getHead2Life() > 0) {
                    AABB worldAABB = head2AABB.move(this.position());

                    List<Entity> entitiesInZone = this.level().getEntities(this, worldAABB);
                    for (Entity entity : entitiesInZone) {
                        if (entity instanceof Projectile) {
                            Projectile projectile = (Projectile) entity;
                            float damage = 0.0f;

                            if (projectile instanceof AbstractArrow) {
                                AbstractArrow arrow = (AbstractArrow) projectile;
                                damage = (float) arrow.getBaseDamage();
                            }
                            else if (projectile instanceof SlingshotProjectile) damage = 2.0f;
                            else if (projectile instanceof PrimitiveSpearProjectileEntity spear) damage = (float) spear.getBaseDamage();
                            else if (projectile instanceof ThrownTrident trident) damage = (float) trident.getBaseDamage();

                            handleBodyPartHit("head_2", damage, entity);
                            bodyPartHitCooldown = 20;
                            break;
                        }
                    }
                }
                if (head3AABB != null) {
                    AABB worldAABB = head3AABB.move(this.position());

                    List<Entity> entitiesInZone = this.level().getEntities(this, worldAABB);
                    for (Entity entity : entitiesInZone) {
                        if (entity instanceof Projectile) {
                            Projectile projectile = (Projectile) entity;
                            float damage = 0.0f;

                            if (projectile instanceof AbstractArrow) {
                                AbstractArrow arrow = (AbstractArrow) projectile;
                                damage = (float) arrow.getBaseDamage();
                            }
                            else if (projectile instanceof SlingshotProjectile) damage = 2.0f;
                            else if (projectile instanceof PrimitiveSpearProjectileEntity spear) damage = (float) spear.getBaseDamage();
                            else if (projectile instanceof ThrownTrident trident) damage = (float) trident.getBaseDamage();

                            handleBodyPartHit("head_3", damage, entity);
                            bodyPartHitCooldown = 20;
                            break;
                        }
                    }
                }
            }
        }

        //showZone("head_3");
    }

    private void showZone(String zone) {
        if (this.level().isClientSide) {
            AABB headAABB = bodyParts.get(zone);
            if (headAABB != null) {
                AABB worldAABB = headAABB.move(this.position());

                double step = 0.1;

                for (double x = worldAABB.minX; x <= worldAABB.maxX; x += step) {
                    for (double y = worldAABB.minY; y <= worldAABB.maxY; y += step) {
                        for (double z = worldAABB.minZ; z <= worldAABB.maxZ; z += step) {
                            boolean onEdge = (x == worldAABB.minX || x >= worldAABB.maxX - step) ||
                                    (y == worldAABB.minY || y >= worldAABB.maxY - step) ||
                                    (z == worldAABB.minZ || z >= worldAABB.maxZ - step);

                            if (onEdge) {
                                this.level().addParticle(ParticleTypes.FLAME,
                                        x, y, z,
                                        0, 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    private final Map<String, AABB> bodyParts = Map.of(
            "head_1", new AABB(1.5 * this.getScale(), 7.5 * this.getScale(), -0.625 * this.getScale(), -1.5625 * this.getScale(), 10.75 * this.getScale(), 2.375 * this.getScale()),
            "head_2", new AABB(-3.0 * this.getScale(), 4.5 * this.getScale(), 0 * this.getScale(), -5.0625 * this.getScale(), 6.75 * this.getScale(), 3.0 * this.getScale()),
            "head_3", new AABB(3.0 * this.getScale(), 4.5 * this.getScale(), 0 * this.getScale(), 5.0625 * this.getScale(), 6.75 * this.getScale(), 3.0 * this.getScale())
    );

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.is(DamageTypes.GENERIC_KILL) ||
                damageSource.is(DamageTypes.OUTSIDE_BORDER) ||
                damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(damageSource, amount);
        }
        return false;
    }

    private void handleBodyPartHit(String bodyPart, float damage, Entity source) {
        float actualDamage = damage * (this.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) ? 1.5f : 1);

        switch (bodyPart) {
            case "head_1": {
                if (getHead2Life() <= 0 && getHead3Life() <= 0) {
                    setHead1Life((int) (getHead1Life() - actualDamage));
                    source.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
                    super.hurt(damageSource, actualDamage);
                }
                else {
                    if (source instanceof Projectile projectile) {
                        if (projectile instanceof AbstractArrow arrow && !(projectile instanceof PrimitiveSpearProjectileEntity)) arrow.discard();
                        projectile.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
                        if (projectile instanceof PrimitiveSpearProjectileEntity) projectile.playSound(OWSounds.SPEAR_HIT.get(), 1.0F, 1.0F);
                        if (projectile instanceof VenomousArrow) this.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) OWUtils.generateRandomInterval(200, 300), 0));
                    }
                }
                break;
            }
            case "head_2": {
                if (getHead2Life() > 0) {
                    int newLife = (int) (getHead2Life() - actualDamage);
                    setHead2Life(newLife);

                    if (newLife <= 0 && !isHead2DeadLoop() && !isHead2Dead()) {
                        setHead2Dead(true);
                    }

                    if (source instanceof Projectile projectile) {
                        if (projectile instanceof AbstractArrow arrow && !(projectile instanceof PrimitiveSpearProjectileEntity)) arrow.discard();
                        projectile.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
                        if (projectile instanceof PrimitiveSpearProjectileEntity) projectile.playSound(OWSounds.SPEAR_HIT.get(), 1.0F, 1.0F);
                        if (projectile instanceof VenomousArrow) this.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) OWUtils.generateRandomInterval(200, 300), 0));
                    }

                    super.hurt(damageSource, actualDamage);
                }
                break;
            }
            case "head_3": {
                if (getHead3Life() > 0) {
                    int newLife = (int) (getHead3Life() - actualDamage);
                    setHead3Life(newLife);

                    if (newLife <= 0 && !isHead3DeadLoop() && !isHead3Dead()) {
                        setHead3Dead(true);
                    }

                    if (source instanceof Projectile projectile) {
                        if (projectile instanceof AbstractArrow arrow && !(projectile instanceof PrimitiveSpearProjectileEntity)) arrow.discard();
                        projectile.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
                        if (projectile instanceof PrimitiveSpearProjectileEntity) projectile.playSound(OWSounds.SPEAR_HIT.get(), 1.0F, 1.0F);
                        if (projectile instanceof VenomousArrow) this.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) OWUtils.generateRandomInterval(200, 300), 0));
                    }

                    super.hurt(damageSource, actualDamage);
                }
                break;
            }
        }
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof PlantEmpressEntity otherPlant_Empress) {
            if (this.isTame()) return otherPlant_Empress.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherPlant_Empress.getOwnerUUID());
            else return !otherPlant_Empress.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));


            this.setVariant(PlantEmpressVariant.PLANT_EMPRESS);
            this.setInitialVariant(this.getVariant());
        }

        this.setHead1Life(150);
        this.setHead2Life(75);
        this.setHead3Life(75);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.getHead1Life() + this.getHead2Life() + this.getHead3Life());

        this.playSound(OWSounds.PLANT_EMPRESS_THEME.get(), 1.0F, 1.0F);

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }


    private void setupAnimationState() {
        createIdleAnimation(180, true);

        if (this.isHead2Dead()) {
            if (this.head2DeathAnimationTimeout <= 0) {
                this.head2DeathAnimationTimeout = 54;
                this.head2DeathAnimationState.start(this.tickCount);
            } else --this.head2DeathAnimationTimeout;
        }
        if (this.isHead2DeadLoop()) {
            this.head2DeathLoopAnimationState.start(this.tickCount);
        }

        if (this.isHead3Dead()) {
            if (this.head3DeathAnimationTimeout <= 0) {
                this.head3DeathAnimationTimeout = 54;
                this.head3DeathAnimationState.start(this.tickCount);
            } else --this.head3DeathAnimationTimeout;
        }
        if (this.isHead3DeadLoop()) {
            this.head3DeathLoopAnimationState.start(this.tickCount);
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(HEAD_1_HEALTH, 0);
        builder.define(HEAD_2_HEALTH, 0);
        builder.define(HEAD_3_HEALTH, 0);
        builder.define(HEAD_2_DEATH, false);
        builder.define(HEAD_2_DEATH_LOOP, false);
        builder.define(HEAD_3_DEATH, false);
        builder.define(HEAD_3_DEATH_LOOP, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);

        tag.putInt("getHead1Life", this.getHead1Life());
        tag.putInt("getHead2Life", this.getHead2Life());
        tag.putInt("getHead3Life", this.getHead3Life());

        tag.putBoolean("isHead2Dead", this.isHead2Dead());
        tag.putBoolean("isHead2DeadLoop", this.isHead2DeadLoop());
        tag.putBoolean("isHead3Dead", this.isHead3Dead());
        tag.putBoolean("isHead3DeadLoop", this.isHead3DeadLoop());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");

        this.entityData.set(HEAD_1_HEALTH, tag.getInt("getHead1Life"));
        this.entityData.set(HEAD_2_HEALTH, tag.getInt("getHead2Life"));
        this.entityData.set(HEAD_3_HEALTH, tag.getInt("getHead3Life"));

        this.entityData.set(HEAD_2_DEATH, tag.getBoolean("isHead2Dead"));
        this.entityData.set(HEAD_2_DEATH_LOOP, tag.getBoolean("isHead2DeadLoop"));
        this.entityData.set(HEAD_3_DEATH, tag.getBoolean("isHead3Dead"));
        this.entityData.set(HEAD_3_DEATH_LOOP, tag.getBoolean("isHead3DeadLoop"));
    }

    @Override
    public int getEntityColor() {
        return 8101455;
    }

    @Override
    public float getEntityScale() {
        return 0;
    }


}

