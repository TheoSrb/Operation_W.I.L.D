package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.tiew.operationWild.core.OWUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.orca.OWOrcaBeachingGoal;
import net.tiew.operationWild.entity.navigation.SwimmerJumpPathNavigator;
import net.tiew.operationWild.entity.variants.OrcaVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class OrcaEntity extends OWWaterEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 270.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float>   RIDER_CONTROL_PITCH  = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_DASHING           = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BEACHED           = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);

    private int dashTicksLeft = 0;
    private Vec3 dashDirection = Vec3.ZERO;

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;

    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimXRot = 0f;
    public volatile float bodyAnimX = 0f;
    public volatile float bodyAnimY_passenger = 0f;
    public volatile float bodyZRot_passenger  = 0f;
    public volatile float bodyXRot_passenger  = 0f;
    public volatile float bodyAnimX_passenger = 0f;


    private int orcaUltimateKillCount = 0;

    private Vec3 lastPitchCheckPos = null;

    public OrcaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.moveControl = new OWSwimMoveControl(this);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new FollowBoatGoal(this));
        this.goalSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 20f, 28, 4, false) {
            // Pour les orques sauvages : OWOrcaBeachingGoal gère les targets sur la côte.
            // Ce goal s'arrête dès que la target quitte l'eau pour libérer les flags MOVE+LOOK.
            private boolean isBlockedForWild() {
                if (OrcaEntity.this.isTame()) return false;
                LivingEntity t = OrcaEntity.this.getTarget();
                return t != null && !t.isInWater();
            }

            @Override
            public boolean canUse() {
                if (isBlockedForWild()) return false;
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                if (isBlockedForWild()) return false;
                return super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new OWOrcaBeachingGoal(this));
        this.goalSelector.addGoal(4, new OrcaWanderGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(RIDER_CONTROL_PITCH, 0.0f);
        builder.define(IS_DASHING, false);
        builder.define(IS_BEACHED, false);
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.entityData.get(IS_DASHING);
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(IS_DASHING, dashing);
    }

    public boolean isBeached() { return this.entityData.get(IS_BEACHED); }
    public void setBeached(boolean beached) { this.entityData.set(IS_BEACHED, beached); }

    protected PathNavigation createNavigation(Level worldIn) {
        return new SwimmerJumpPathNavigator(this, worldIn);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x28313e;
    }

    @Override
    public float getTheoreticalScale() {
        return 18;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.BERSERKER;
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
        return List.of(Boat.class, Player.class, Animal.class, Monster.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.5f;
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
        return OWItems.ORCA_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.ORCA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 315 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1f * (1 + ((float) this.getLevel() / 50));
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
        return 0.075f;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir + 4;
    }

    @Override
    public int getMaxDepth() {
        return 60;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 10;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.ORCA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CROCODILE_FOOD);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        return RANDOM(3) ? RANDOM(2) ? OWSounds.CROCODILE_IDLE_2.get() : OWSounds.CROCODILE_IDLE_4.get() : null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.CROCODILE_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.CROCODILE_HURT.get();
    }

    // ==================================================
    //             SONS DE PAS (animation-driven)
    // ==================================================

    private long lastStepSoundMs = 0L;

    /**
     * Suppresses the vanilla automatic step sound (fired by Entity.move() every block traversed).
     * Footstep sounds are handled exclusively by animation events in CrocodileModel
     * via onFrontLeftFootDown() / onFrontRightFootDown().
     */
    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        // Intentionally empty — replaced by animation callbacks below
    }

    /**
     * Called by CrocodileModel (render thread) when the left foot touches the ground.
     * Plays the step sound of the block under the crocodile with a slightly lower pitch.
     */
    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.85f);
    }

    /**
     * Called by CrocodileModel (render thread) when the right foot touches the ground.
     * Plays the step sound of the block under the crocodile with a slightly higher pitch.
     */
    public void onRightFootDown() {
        playStepSoundFromAnimation(1.05f);
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;
        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 150L) return; // croc walks slower than tiger
        lastStepSoundMs = now;

        var pos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.isAir()) blockState = this.level().getBlockState(pos.below());

        net.minecraft.world.level.block.SoundType sound = blockState.getSoundType();
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                sound.getStepSound(),
                this.getSoundSource(),
                sound.getVolume() * 0.65f,
                sound.getPitch() * pitchMod * (0.85f + this.random.nextFloat() * 0.3f)
        );
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.75 * this.getScale();
    }

    protected double getBaseRiderYOffset(int idx) {
        return this.getBbHeight() * 0.75 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
    }

    public void tick() {
        super.tick();

        createCombo((int) (28 / comboSpeedMultiplier), (int) (18 / comboSpeedMultiplier), OWSounds.CROCODILE_MOUTH_CRUSH.get(), 4.5, 4, 3, false, 0.5f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide() && false) {
            Vec3 currentPos = this.position();
            boolean isMovingHorizontally = lastPitchCheckPos != null
                    && (Math.pow(currentPos.x - lastPitchCheckPos.x, 2)
                    + Math.pow(currentPos.z - lastPitchCheckPos.z, 2)) > 1e-6;
            lastPitchCheckPos = currentPos;

            if (this.isTame() && this.isVehicle() && !this.isSitting() && !this.isBaby() && this.isInWater()) {
                LivingEntity rider = this.getControllingPassenger();
                if (rider != null && isMovingHorizontally) {
                    float target = Mth.clamp(rider.getXRot(), -45f, 45f);
                    float smoothed = Mth.clamp(Mth.lerp(0.15f, this.entityData.get(RIDER_CONTROL_PITCH), target), -45f, 45f);
                    this.entityData.set(RIDER_CONTROL_PITCH, smoothed);
                }
            } else {
                float current = this.entityData.get(RIDER_CONTROL_PITCH);
                if (Math.abs(current) > 0.05f) {
                    this.entityData.set(RIDER_CONTROL_PITCH, current * 0.95f);
                } else if (current != 0f) {
                    this.entityData.set(RIDER_CONTROL_PITCH, 0f);
                }
            }
        }


        if (!this.level().isClientSide()) {
            if (this.dashTicksLeft > 0) {
                float t = this.dashTicksLeft / 30f;
                float speed = 3.8f * (t * t * t);

                if (this.dashTicksLeft > 20) {
                    Vec3 look = this.getLookAngle();
                    Vec3 front = this.position().add(look.scale(2.5));
                    AABB hitBox = new AABB(
                            front.x - 2.2, front.y - 1.5, front.z - 2.2,
                            front.x + 2.2, front.y + 1.5, front.z + 2.2
                    );
                    this.level().getEntitiesOfClass(LivingEntity.class, hitBox).forEach(target -> {
                        if (target != this && target != this.getFirstPassenger()) {
                            target.hurt(this.damageSources().mobAttack(this), this.getDamage());
                            Vec3 knockback = look.scale(1.2);
                            target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.25, knockback.z));
                        }
                    });
                }

                Entity rider = this.getFirstPassenger();
                if (rider instanceof Player player && player.zza > 0 && this.dashTicksLeft <= 15) {
                    this.dashTicksLeft = 0;
                    this.entityData.set(IS_DASHING, false);
                    return;
                }

                Vec3 current = this.getDeltaMovement();
                if (speed > 0.08f) {
                    this.setDeltaMovement(
                            this.dashDirection.x * speed,
                            current.y,
                            this.dashDirection.z * speed
                    );
                }

                this.dashTicksLeft--;

                if (this.dashTicksLeft == 0) {
                    this.entityData.set(IS_DASHING, false);
                }
            }
        }

        handleGoldVariantEffects();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.isInWater()) {
            if (this.onGround()) {
                this.setDeltaMovement(
                        this.getDeltaMovement().x + (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f,
                        0.5,
                        this.getDeltaMovement().z + (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f
                );
                this.setYRot(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.hasImpulse = true;
            }

            int air = this.getAirSupply();
            this.setAirSupply(air - 1);
            if (this.getAirSupply() <= -20) {
                this.setAirSupply(0);
                this.hurt(this.damageSources().dryOut(), 2.0f);
            }

            this.setXRot(90.0f);
            this.xRotO = 90.0f;
        } else {
            if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(Math.min(this.getAirSupply() + 4, this.getMaxAirSupply()));
            }
            this.setXRot(0.0f);
            this.xRotO = 0.0f;
        }
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

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) {
            return;
        }

        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {

    }

    // ── TIDAL RUSH ─────────────────────────────────────────────────────────────

    public void performOrcaDash() {
        float cost = OWAttacksConstants.Orca.TIDAL_RUSH_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        // Sauvegarde la direction horizontale pour le tick handler
        Vec3 look = this.getLookAngle();
        this.dashDirection = new Vec3(look.x, 0, look.z).normalize();

        // Kick initial — vitesse max dès le premier frame
        this.setDeltaMovement(this.dashDirection.scale(3.8));

        this.entityData.set(IS_DASHING, true);
        this.dashTicksLeft = 30; // 1.5s au lieu de 1s

        // Dégâts
        Vec3 front = this.position().add(look.scale(2.5));
        AABB hitBox = new AABB(
                front.x - 2.2, front.y - 1.5, front.z - 2.2,
                front.x + 2.2, front.y + 1.5, front.z + 2.2
        );
        this.level().getEntitiesOfClass(LivingEntity.class, hitBox).forEach(target -> {
            if (target != this && target != this.getFirstPassenger()) {
                target.hurt(this.damageSources().mobAttack(this), this.getDamage());
                Vec3 knockback = look.scale(1.2);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.25, knockback.z));
            }
        });

        // Particules & sons (inchangés)
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    pos.x, pos.y + 0.5, pos.z, 35, 1.1, 0.4, 1.1, 0.25);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    pos.x, pos.y + 0.2, pos.z, 25, 0.9, 0.3, 0.9, 0.08);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.AMBIENT, 2.5f, 0.8f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_SPLASH, SoundSource.AMBIENT, 1.8f, 1.0f);
    }


    public void activateOrcaCall() {
        float damage = 15f + 10f * (this.getLevel() / 50f);
        AABB hitBox = this.getBoundingBox().inflate(12.0);
        this.level().getEntitiesOfClass(LivingEntity.class, hitBox).forEach(target -> {
            if (target != this && target != this.getFirstPassenger() && !this.isAlliedTo(target)) {
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        });

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.AMBIENT, 3.0f, 0.5f);
        orcaUltimateKillCount = 0;
    }

    public int getOrcaUltimateKillCount() {
        return orcaUltimateKillCount;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        orcaUltimateKillCount++;
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof OrcaEntity otherOrca) {
            if (otherOrca.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherOrca.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherOrca.getOwnerUUID());
            } else {
                return !otherOrca.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        int idx = this.getPassengers().indexOf(passenger);

        float seatZ, seatX;
        switch (idx) {
            case 1  -> { seatZ = -0.9f; seatX =  0.45f; }
            case 2  -> { seatZ = -0.9f; seatX = -0.45f; }
            default -> { seatZ =  0.65f; seatX = 0f;    }
        }

        float boneX = idx == 0
                ? -bodyAnimX / 16.0f * this.getScale()
                : -bodyAnimX_passenger / 16.0f * this.getScale();
        seatX += boneX;

        float pitch = this.bodyAnimXRot;
        float rotatedY = -seatZ * Mth.sin(pitch);
        float rotatedZ =  seatZ * Mth.cos(pitch);

        Vec3 seatOffset = new Vec3(seatX, rotatedY, rotatedZ)
                .yRot((float) Math.toRadians(-this.yBodyRot));

        double baseY = getBaseRiderYOffset(idx);
        float animY = idx == 0
                ? getRiderAnimYOffset()
                : -bodyAnimY_passenger / 16.0f * this.getScale();
        double riderY = this.getY() + baseY + animY + seatOffset.y;

        passenger.fallDistance = 0f;
        function.accept(passenger,
                this.getX() + seatOffset.x,
                riderY,
                this.getZ() + seatOffset.z);

        if (idx == 0 && passenger instanceof LivingEntity living) {
            living.yBodyRot = this.yBodyRot;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 3;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity controlling = this.getControllingPassenger();
        if (controlling == null) {
            return super.isControlledByLocalInstance();
        }
        return this.getPassengers().indexOf(controlling) == 0 && super.isControlledByLocalInstance();
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

            this.setVariant(chooseOrcaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);

        if (this.isBaby()) {
            maxHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();
            maxMaturation = (int) (2000 * maxHealth + 10000 * this.getDamage());
            this.setHealth(1);
            foodWanted = (int) this.getMaxHealth();
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    protected int getDefaultSkinIndex() {
        return 7;
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == OrcaVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private OrcaVariant chooseOrcaVariant() {
        OrcaVariant variant;
        if (chance >= 66) variant = OrcaVariant.BLACK;
        else if (chance >= 33) variant = OrcaVariant.AQUA;
        else variant = OrcaVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(60, true);
        createSitAnimation(96, true);

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, 28);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, 28);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 28);
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        if (this.isCombo(comboNumber)) {
            if (timer <= 0) {
                timer = maxTimer;
                animationState.start(this.tickCount);
            } else {
                --timer;
            }
        } else {
            timer = 0;
            animationState.stop();
        }

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public OrcaVariant getVariant() {
        return OrcaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(OrcaVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(OrcaVariant skin) {
        this.setVariant(skin);
    }

    public float getRiderControlPitch() { return this.entityData.get(RIDER_CONTROL_PITCH); }

    public float getBodyZRot_passenger() { return bodyZRot_passenger; }
    public float getBodyXRot_passenger() { return bodyXRot_passenger; }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(OrcaVariant.Cosmetics.GOLD.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    public OrcaVariant getInitialVariant() {
        return OrcaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(OrcaVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("orcaUltimateKillCount", this.orcaUltimateKillCount);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.orcaUltimateKillCount = tag.getInt("orcaUltimateKillCount");

        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    static class OrcaWanderGoal extends Goal {

        private final OrcaEntity orca;
        private double targetX, targetY, targetZ;

        private boolean isVerticalPhase = false;
        private int verticalTimer = 0;
        private boolean goingUp = false;

        OrcaWanderGoal(OrcaEntity orca) {
            this.orca = orca;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!orca.isInWater()) return false;
            if (orca.getTarget() != null) return false;
            if (orca.isVehicle()) return false;
            if (orca.isSitting()) return false;
            if (orca.isNapping()) return false;
            pickHorizontalTarget();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!orca.isInWater()) return false;
            if (orca.getTarget() != null) return false;
            if (orca.isVehicle()) return false;
            if (orca.isSitting()) return false;
            double dx = orca.getX() - targetX;
            double dz = orca.getZ() - targetZ;
            if (dx * dx + dz * dz <= 6.25) pickHorizontalTarget();
            return true;
        }

        @Override
        public void tick() {
            if (isVerticalPhase) {
                verticalTimer--;

                double seaLevel = orca.level().getSeaLevel();
                double currentY = orca.getY();
                double minY = seaLevel - orca.getMaxDepth() + 5;
                double maxY = seaLevel - 2;

                boolean hitCeiling = goingUp  && currentY >= maxY;
                boolean hitFloor   = !goingUp && currentY <= minY;

                if (verticalTimer <= 0 || hitCeiling || hitFloor) {
                    isVerticalPhase = false;
                    orca.setTargetPitch(0f);
                    pickHorizontalTarget();
                } else {
                    double force = goingUp ? 0.025 : -0.025;
                    orca.setDeltaMovement(orca.getDeltaMovement().add(0, force, 0));

                    float pitchTarget = goingUp ? -25f : 25f;
                    float smooth = orca.getTargetPitch() + (pitchTarget - orca.getTargetPitch()) * 0.1f;
                    orca.setTargetPitch(smooth);
                }

            } else {
                if (orca.getRandom().nextInt(100) == 0) {
                    startVerticalPhase();
                    return;
                }
                float p = orca.getTargetPitch();
                if (Math.abs(p) > 0.1f) orca.setTargetPitch(p * 0.9f);
                else orca.setTargetPitch(0f);
            }

            orca.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0);
        }

        private void startVerticalPhase() {
            isVerticalPhase = true;
            verticalTimer = 60 + orca.getRandom().nextInt(100);
            goingUp = orca.getRandom().nextBoolean();

            double angle = orca.getRandom().nextDouble() * Math.PI * 2;
            double dist  = 4 + orca.getRandom().nextDouble() * 6;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;
            targetY = orca.getY();
        }

        private void pickHorizontalTarget() {
            isVerticalPhase = false;

            double angle = orca.getRandom().nextDouble() * Math.PI * 2;
            double dist  = 8 + orca.getRandom().nextDouble() * 12;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;

            double seaLevel = orca.level().getSeaLevel();
            double minY = seaLevel - orca.getMaxDepth() + 5;
            double maxY = seaLevel - 2;
            targetY = Mth.clamp(orca.getY() + (orca.getRandom().nextDouble() - 0.5) * 4, minY, maxY);
        }
    }
}