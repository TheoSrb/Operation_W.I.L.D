package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.config.OWTargetLists;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.misc.ThrownRockEntity;
import net.tiew.operationWild.entity.variants.GorillaVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GorillaEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, NeutralMob {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 175.0;
    public static final int ENTITY_COLOR = 0x333C42;
    public static final int DEFAULT_SKIN_INDEX = 1;
    public static final float AI_STEP_JUMP_FACTOR = 0.45f;

    private static final int MISC_IDLE_2_DURATION = 140;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(29, 41);

    public static final int CLIMB_SURGE_TICKS = 11;
    private static final double CLIMB_SURGE_SPEED = 0.20;
    private static final double CLIMB_SURGE_FLOOR = 0.04;
    private static final double CLIMB_STRAFE_SPEED = 0.13;
    private static final float CLIMB_TURN_DEGREES = 12f;
    private static final double CLIMB_WALL_PRESS = 0.09;
    private static final float CLIMB_ENERGY_PER_SURGE = 6f;
    private static final int CLIMB_VAULT_DURATION_TICKS = 5;
    private static final double CLIMB_VAULT_FORWARD = 0.52;
    private static final double CLIMB_VAULT_LIFT = 0.46;
    private static final int CLIMB_FALL_IMMUNITY_TICKS = 60;
    private static final double CLIMB_WALL_MARGIN = 0.45;
    private static final int CLIMB_RIDERLESS_TICKS = 100;
    private static final double SEAT_FORWARD = -0.15;
    private static final double ULTIMATE_SEAT_FORWARD = 0.62;
    private static final double ULTIMATE_SEAT_LIFT = 0.55;
    private static final double SEAT_FORWARD_CLIMBING = -0.85;

    private static final double CLIMB_ORBIT_RADIUS = 1.30;
    private static final double CLIMB_FACE_HYSTERESIS = 0.15;
    private static final float CLIMB_LOOK_DEADZONE = 18f;
    private static final float CLIMB_LOOK_FULL = 62f;
    private static final float CLIMB_HEAD_FREEDOM = 65f;
    private static final double CLIMB_PULL_GAIN = 0.40;
    private static final double CLIMB_PULL_MAX = 0.12;

    private static final ResourceLocation CHEST_BEAT_DAMAGE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "gorilla_chest_beat_damage");

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ROCK_CHARGING = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ROCK_THROW_TICK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LAUNCH_CHARGING = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RIDER_LAUNCH_TICK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHEST_BEAT_TICK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIMB_TICK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIMB_VAULT_TICK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CLIMB_YAW = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CLIMB_HANG = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CLIMB_LOOK = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CLIMB_COLUMN_X = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIMB_COLUMN_Z = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(GorillaEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState miscIdleAnimationState = new AnimationState();

    private int miscIdleAnimationStartTime = 0;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(400, 900);

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private boolean rockChargePending = false;
    private boolean launchChargePending = false;

    private int chestBeatBuffTimer = 0;
    private final List<Integer> chestBeatAllies = new ArrayList<>();

    private int launchedRiderId = -1;
    private int launchedRiderTimer = 0;

    private int climbFallImmunity = 0;
    private int climbRiderlessTicks = 0;

    public int clientClimbElapsed = -1;

    public float clientHangBlend = 0f;

    public float clientClimbSteer = 0f;

    public int clientRockChargeTicks = 0;

    public final AnimationState chestBeatAnimationState = new AnimationState();

    private int remainingPersistentAngerTime;

    @Nullable
    private UUID persistentAngerTarget;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public GorillaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.JUMP_STRENGTH, 0.95);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GorillaMeleeAttackGoal());

        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true));

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_ROCK_CHARGING, false);
        builder.define(ROCK_THROW_TICK, 0);
        builder.define(IS_LAUNCH_CHARGING, false);
        builder.define(RIDER_LAUNCH_TICK, 0);
        builder.define(CHEST_BEAT_TICK, 0);
        builder.define(CLIMB_TICK, 0);
        builder.define(CLIMB_VAULT_TICK, 0);
        builder.define(CLIMB_YAW, 0f);
        builder.define(CLIMB_HANG, false);
        builder.define(CLIMB_LOOK, 0f);
        builder.define(CLIMB_COLUMN_X, 0);
        builder.define(CLIMB_COLUMN_Z, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
    }

    // ==================================================
    //               MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 9f;
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
        return OWEntityConfig.Diet.VEGETARIAN;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.NEUTRAL;
    }

    @Override
    public List<Class<?>> getFavoriteTargetsByBeingNonTame() {
        return OWTargetLists.GORILLA;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.6f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.7f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 3f;
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
        return Items.AIR;
    }

    @Override
    public boolean requiresSaddleToRide() {
        return false;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.GORILLA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 200f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.85f * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public boolean preferRawMeat() {
        return false;
    }

    @Override
    public boolean preferCookedMeat() {
        return false;
    }

    @Override
    public boolean preferVegetables() {
        return true;
    }

    @Override
    public boolean riderCameraFollowsBodyTilt() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        if (isChestBeating() || isClimbing()) return 0f;
        return isRockCharging() || isLaunchCharging() ? 1f : 0.15f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.GORILLA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.GORILLA_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected float getJumpPower() {
        if (this.getControllingPassenger() != null) return super.getJumpPower();
        return super.getJumpPower() * AI_STEP_JUMP_FACTOR;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 120L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 5; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.16F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.75f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(0.9f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
                this.removeEffect(OWEffects.FEAR_EFFECT.getDelegate());
            }

            if (isRockCharging() && !this.isVehicle()) cancelRockCharge();
            if (isLaunchCharging() && !this.isVehicle()) cancelRiderLaunchCharge();

            tickGestureTimer(ROCK_THROW_TICK);
            tickGestureTimer(RIDER_LAUNCH_TICK);

            handleChestBeat();
            handleLaunchedRider();
        }

        createCombo(18, 11, OWSounds.LEG_HURT.get(),
                3.0, 3.0, 1.6, actualAttackNumber == 3, actualAttackNumber == 3 ? 1 : 2);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMadByRider(this.isCombo());

        if (!this.level().isClientSide() && !this.isVehicle()) {
            if (this.getState() == 2) this.setRunning(true);
            else if (this.getTarget() == null) this.setRunning(false);
        }

        if (this.level().isClientSide()) {
            tickClientClimbElapsed();
            clientRockChargeTicks = isRockCharging() ? clientRockChargeTicks + 1 : 0;
        }
        tickClimb();
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);
    }

    private void tickGestureTimer(EntityDataAccessor<Integer> accessor) {
        int value = this.entityData.get(accessor);
        if (value > 0) this.entityData.set(accessor, value - 1);
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        return this.isImmobile() ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    public boolean onClimbable() {
        return isClimbing() || super.onClimbable();
    }

    public boolean isClimbing() { return this.entityData.get(CLIMB_TICK) > 0; }

    public boolean isHangingOnWall() { return isClimbing() && this.entityData.get(CLIMB_HANG); }

    public float getClimbLook() { return this.entityData.get(CLIMB_LOOK); }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isClimbing() && !isVaulting();
    }

    @Override
    public boolean canUseUltimate() {
        return super.canUseUltimate() && !isClimbing() && !isVaulting();
    }

    public int getClimbTick() { return this.entityData.get(CLIMB_TICK); }

    public boolean isVaulting() { return this.entityData.get(CLIMB_VAULT_TICK) > 0; }

    public int getVaultTick() { return this.entityData.get(CLIMB_VAULT_TICK); }

    public float getClimbYaw() { return this.entityData.get(CLIMB_YAW); }

    public int getClimbSurgeIndex() {
        int tick = getClimbTick();
        return tick <= 0 ? 0 : (tick - 1) / CLIMB_SURGE_TICKS;
    }

    private void tickClimb() {
        if (climbFallImmunity > 0) {
            climbFallImmunity--;
            this.fallDistance = 0f;
        }

        if (isVaulting()) {
            tickVault();
            return;
        }

        int tick = getClimbTick();

        if (tick <= 0) {
            if (this.level().isClientSide()) return;
            if (this.isNoGravity()) this.setNoGravity(false);
            if (shouldStartClimb()) startClimb();
            return;
        }

        double cx = this.entityData.get(CLIMB_COLUMN_X) + 0.5;
        double cz = this.entityData.get(CLIMB_COLUMN_Z) + 0.5;
        double dx = cx - this.getX();
        double dz = cz - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4) {
            dx = 0.0;
            dz = 1.0;
            dist = 1.0;
        }

        Direction current = Direction.fromYRot(getClimbYaw());
        boolean wasAlongZ = current.getAxis() == Direction.Axis.Z;
        boolean alongZ = wasAlongZ
                ? Math.abs(dz) >= Math.abs(dx) - CLIMB_FACE_HYSTERESIS
                : Math.abs(dz) > Math.abs(dx) + CLIMB_FACE_HYSTERESIS;

        Direction face = alongZ
                ? (dz > 0 ? Direction.SOUTH : Direction.NORTH)
                : (dx > 0 ? Direction.EAST : Direction.WEST);

        if (!this.level().isClientSide() && face != current) this.entityData.set(CLIMB_YAW, face.toYRot());

        double along = alongZ ? dz : dx;
        double sign = along >= 0 ? 1.0 : -1.0;

        float turned = Mth.approachDegrees(this.getYRot(), face.toYRot(), CLIMB_TURN_DEGREES);
        this.setYRot(turned);
        this.yBodyRot = turned;

        LivingEntity climber = this.getControllingPassenger();

        float lookDelta = climber != null ? Mth.wrapDegrees(climber.getYRot() - face.toYRot()) : 0f;
        this.yHeadRot = turned + Mth.clamp(lookDelta, -CLIMB_HEAD_FREEDOM, CLIMB_HEAD_FREEDOM);

        float steer = Mth.clamp((Math.abs(lookDelta) - CLIMB_LOOK_DEADZONE)
                / (CLIMB_LOOK_FULL - CLIMB_LOOK_DEADZONE), 0f, 1f) * Math.signum(lookDelta);
        if (climber != null) steer = Mth.clamp(steer - climber.xxa, -1f, 1f);
        if (!this.level().isClientSide()) this.entityData.set(CLIMB_LOOK, steer);

        boolean hanging = this.entityData.get(CLIMB_HANG);
        int phase = (tick - 1) % CLIMB_SURGE_TICKS;
        double bump = Math.sin(Math.PI * (double) phase / CLIMB_SURGE_TICKS);
        double up = hanging ? 0.0 : CLIMB_SURGE_FLOOR + CLIMB_SURGE_SPEED * bump * bump;

        double pull = Mth.clamp((Math.abs(along) - CLIMB_ORBIT_RADIUS) * CLIMB_PULL_GAIN,
                -CLIMB_PULL_MAX, CLIMB_PULL_MAX) + CLIMB_WALL_PRESS;
        double pullX = alongZ ? 0.0 : sign * pull;
        double pullZ = alongZ ? sign * pull : 0.0;

        double tangent = steer * CLIMB_STRAFE_SPEED;
        double tanX = alongZ ? -sign * tangent : 0.0;
        double tanZ = alongZ ? 0.0 : sign * tangent;

        this.setDeltaMovement(pullX + tanX, up, pullZ + tanZ);
        this.hasImpulse = true;
        this.fallDistance = 0f;
        this.setNoGravity(true);

        if (this.level().isClientSide()) {
            if (!hanging && phase == 0) spawnClimbGrip(face);
            return;
        }

        if (this.isInWater()) {
            stopClimb();
            return;
        }

        if (climber == null) {
            if (++climbRiderlessTicks > CLIMB_RIDERLESS_TICKS) {
                stopClimb();
                return;
            }
        } else {
            climbRiderlessTicks = 0;
            if (climber.zza < 0f) {
                stopClimb();
                return;
            }
        }

        int columnX = this.entityData.get(CLIMB_COLUMN_X);
        int columnZ = this.entityData.get(CLIMB_COLUMN_Z);

        if (!columnSolid(columnX, columnZ, 1.6)) {
            long moved = nearestGrip(columnX, columnZ, 1.6);
            if (moved != Long.MIN_VALUE) {
                this.entityData.set(CLIMB_COLUMN_X, (int) (moved >> 32));
                this.entityData.set(CLIMB_COLUMN_Z, (int) moved);
            } else if (columnSolid(columnX, columnZ, 0.4)) {
                startVault(face);
                return;
            } else {
                stopClimb();
                return;
            }
        }

        boolean wantsToClimb = this.isRunning() && climber != null;
        boolean hasEnergy = getVitalEnergy() <= getVitalEnergyCapacity() - CLIMB_ENERGY_PER_SURGE;
        if (wantsToClimb && !hasEnergy) canShowVitalEnergyLack = true;

        boolean nowHanging = !wantsToClimb || !hasEnergy;
        this.entityData.set(CLIMB_HANG, nowHanging);

        if (nowHanging) return;

        if (phase == 0) setVitalEnergy(getVitalEnergy() + CLIMB_ENERGY_PER_SURGE);
        this.entityData.set(CLIMB_TICK, tick + 1);
    }

    private void tickVault() {
        int tick = getVaultTick();
        Direction face = Direction.fromYRot(getClimbYaw());

        double lift = tick == CLIMB_VAULT_DURATION_TICKS ? CLIMB_VAULT_LIFT : this.getDeltaMovement().y;
        this.setDeltaMovement(face.getStepX() * CLIMB_VAULT_FORWARD, lift, face.getStepZ() * CLIMB_VAULT_FORWARD);
        this.hasImpulse = true;
        this.fallDistance = 0f;

        if (this.level().isClientSide()) return;

        this.entityData.set(CLIMB_VAULT_TICK, tick - 1);
        if (tick - 1 <= 0) climbFallImmunity = CLIMB_FALL_IMMUNITY_TICKS;
    }

    private boolean shouldStartClimb() {
        if (!this.isTame() || this.isBaby() || this.isInWater()) return false;
        if (isChestBeating() || isRockCharging() || isLaunchCharging()) return false;
        LivingEntity rider = this.getControllingPassenger();
        if (rider == null || rider.zza <= 0) return false;
        if (!this.isRunning()) return false;
        if (getVitalEnergy() > getVitalEnergyCapacity() - CLIMB_ENERGY_PER_SURGE) return false;

        Direction face = Direction.fromYRot(rider.getYRot());
        return hasWall(face, 0.4) && hasWall(face, 1.6);
    }

    private boolean columnSolid(int columnX, int columnZ, double yOffset) {
        BlockPos pos = BlockPos.containing(columnX + 0.5, this.getY() + yOffset, columnZ + 0.5);
        BlockState state = this.level().getBlockState(pos);
        return state.isFaceSturdy(this.level(), pos, Direction.UP)
                || state.isCollisionShapeFullBlock(this.level(), pos);
    }

    private long nearestGrip(int columnX, int columnZ, double yOffset) {
        long best = Long.MIN_VALUE;
        double bestDist = Double.MAX_VALUE;

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                if (ox == 0 && oz == 0) continue;
                int nx = columnX + ox;
                int nz = columnZ + oz;
                if (!columnSolid(nx, nz, yOffset)) continue;

                double ddx = nx + 0.5 - this.getX();
                double ddz = nz + 0.5 - this.getZ();
                double d = ddx * ddx + ddz * ddz;
                if (d < bestDist) {
                    bestDist = d;
                    best = ((long) nx << 32) | (nz & 0xFFFFFFFFL);
                }
            }
        }
        return best;
    }

    private void tickClientClimbElapsed() {
        if (!isClimbing()) {
            clientClimbElapsed = -1;
            clientHangBlend = 0f;
            clientClimbSteer *= 0.8f;
            return;
        }

        clientHangBlend = Mth.clamp(clientHangBlend + (isHangingOnWall() ? 0.12f : -0.18f), 0f, 1f);
        clientClimbSteer += (getClimbLook() - clientClimbSteer) * 0.18f;

        if (isHangingOnWall()) {
            clientClimbElapsed = getClimbTick();
            return;
        }

        int server = getClimbTick();
        if (clientClimbElapsed < 0) {
            clientClimbElapsed = server;
        } else {
            clientClimbElapsed++;
            if (Math.abs(clientClimbElapsed - server) > 2) clientClimbElapsed = server;
        }
    }

    private double climbWallReach() {
        return this.getBbWidth() * 0.5 + CLIMB_WALL_MARGIN;
    }

    private boolean hasWall(Direction face, double yOffset) {
        BlockPos pos = BlockPos.containing(
                this.getX() + face.getStepX() * climbWallReach(),
                this.getY() + yOffset,
                this.getZ() + face.getStepZ() * climbWallReach());
        BlockState state = this.level().getBlockState(pos);
        return state.isFaceSturdy(this.level(), pos, face.getOpposite());
    }

    private void startClimb() {
        LivingEntity rider = this.getControllingPassenger();
        float yaw = rider != null ? rider.getYRot() : this.yBodyRot;
        Direction face = Direction.fromYRot(yaw);

        BlockPos grip = BlockPos.containing(
                this.getX() + face.getStepX() * climbWallReach(),
                this.getY() + 1.6,
                this.getZ() + face.getStepZ() * climbWallReach());

        this.entityData.set(CLIMB_COLUMN_X, grip.getX());
        this.entityData.set(CLIMB_COLUMN_Z, grip.getZ());
        this.entityData.set(CLIMB_YAW, face.toYRot());
        this.entityData.set(CLIMB_HANG, false);
        this.entityData.set(CLIMB_TICK, 1);
        this.getNavigation().stop();
    }

    private void startVault(Direction face) {
        this.entityData.set(CLIMB_TICK, 0);
        this.entityData.set(CLIMB_HANG, false);
        this.entityData.set(CLIMB_VAULT_TICK, CLIMB_VAULT_DURATION_TICKS);
        this.setNoGravity(false);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.1, 1.3));
    }

    private void stopClimb() {
        this.entityData.set(CLIMB_TICK, 0);
        this.entityData.set(CLIMB_HANG, false);
        this.entityData.set(CLIMB_LOOK, 0f);
        climbRiderlessTicks = 0;
        this.setNoGravity(false);
        climbFallImmunity = CLIMB_FALL_IMMUNITY_TICKS;
    }

    private void spawnClimbGrip(Direction face) {
        BlockPos pos = BlockPos.containing(
                this.getX() + face.getStepX() * climbWallReach(),
                this.getY() + 1.2,
                this.getZ() + face.getStepZ() * climbWallReach());
        BlockState state = this.level().getBlockState(pos);
        if (state.isAir()) return;

        double side = getClimbSurgeIndex() % 2 == 0 ? 0.35 : -0.35;
        double handX = this.getX() + face.getStepX() * (climbWallReach() - 0.1) - face.getStepZ() * side;
        double handZ = this.getZ() + face.getStepZ() * (climbWallReach() - 0.1) + face.getStepX() * side;

        for (int i = 0; i < 6; i++) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    handX + (this.random.nextDouble() - 0.5) * 0.25,
                    this.getY() + 1.35 + (this.random.nextDouble() - 0.5) * 0.3,
                    handZ + (this.random.nextDouble() - 0.5) * 0.25,
                    0, 0, 0);
        }

        SoundType soundtype = state.getSoundType(this.level(), pos, this);
        this.level().playLocalSound(this.getX(), this.getY() + 1.2, this.getZ(),
                soundtype.getHitSound(), this.getSoundSource(),
                soundtype.getVolume() * 0.9F, soundtype.getPitch() * 0.75F, false);
    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel) this.level(), false);
        super.customServerAiStep();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(OWEffects.FEAR_EFFECT.getDelegate())) return false;
        return super.canBeAffected(effect);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame() && this.isSitting()) this.setSitting(false);

        boolean result = super.hurt(damageSource, v);

        if (result && !this.isTame() && damageSource.getEntity() instanceof LivingEntity attacker
                && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            this.startPersistentAngerTimer();
            this.setPersistentAngerTarget(attacker.getUUID());
            this.setTarget(attacker);
        }
        return result;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (chestBeatBuffTimer > 0) clearChestBeatBonuses();
        chestBeatBuffTimer = 0;
        super.die(damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof GorillaEntity otherGorilla) {
            if (otherGorilla.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherGorilla.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherGorilla.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherGorilla.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Gorilla.CHEST_BEAT_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected boolean isImmobile() {
        return isRockCharging() || isLaunchCharging() || isChestBeating() || isClimbing();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isChestBeating();
    }

    @Override
    public int getSecondaryAttackCount() {
        return 2;
    }

    @Override
    public int getSecondaryCooldownDuration() {
        return OWAttacksConstants.Gorilla.SECONDARY_COOLDOWN_TICKS;
    }

    @Override
    protected void onSecondaryAttackChanged() {
        cancelRockCharge();
        cancelRiderLaunchCharge();
    }

    public void startRockCharge() {
        if (isRockCharging()) return;
        rockChargePending = true;
        if (isSecondaryOnCooldown()) return;
        this.entityData.set(IS_ROCK_CHARGING, true);
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();
    }

    public void cancelRockCharge() {
        rockChargePending = false;
        this.isChargingAttack = false;
        this.entityData.set(IS_ROCK_CHARGING, false);
    }

    public void performRockThrow(float chargeFactor) {
        if (this.level().isClientSide()) return;
        if (!rockChargePending || isSecondaryOnCooldown() || isChestBeating()) {
            cancelRockCharge();
            return;
        }

        float cost = OWAttacksConstants.Gorilla.ROCK_THROW_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            cancelRockCharge();
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        cancelRockCharge();
        startSecondaryCooldown();
        this.entityData.set(ROCK_THROW_TICK, OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS);

        float factor = Mth.clamp(chargeFactor, 0f, 1f);
        double speed = Mth.lerp(factor,
                OWAttacksConstants.Gorilla.ROCK_THROW_MIN_SPEED,
                OWAttacksConstants.Gorilla.ROCK_THROW_MAX_SPEED);
        float damage = (float) (getDamage() * Mth.lerp(factor,
                OWAttacksConstants.Gorilla.ROCK_THROW_MIN_DAMAGE_RATIO,
                OWAttacksConstants.Gorilla.ROCK_THROW_MAX_DAMAGE_RATIO));

        LivingEntity aimer = this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
        Vec3 look = aimer.getLookAngle();

        ThrownRockEntity rock = new ThrownRockEntity(this.level(), this, damage);
        rock.setPos(this.getX() + look.x * 1.8,
                this.getEyeY() + 0.35 + look.y * 0.6,
                this.getZ() + look.z * 1.8);
        rock.shoot(look.x, look.y + 0.10, look.z, (float) speed, 0f);
        this.level().addFreshEntity(rock);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL,
                1.4f, (float) OWUtils.generateRandomInterval(0.85, 1.05));
    }

    public void startRiderLaunchCharge() {
        if (isLaunchCharging()) return;
        launchChargePending = true;
        if (isSecondaryOnCooldown()) return;
        this.entityData.set(IS_LAUNCH_CHARGING, true);
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();
    }

    public void cancelRiderLaunchCharge() {
        launchChargePending = false;
        this.isChargingAttack = false;
        this.entityData.set(IS_LAUNCH_CHARGING, false);
    }

    public void performRiderLaunch(float chargeFactor) {
        if (this.level().isClientSide()) return;
        if (!launchChargePending || isSecondaryOnCooldown() || isChestBeating()) {
            cancelRiderLaunchCharge();
            return;
        }

        LivingEntity rider = this.getControllingPassenger();
        if (rider == null) {
            cancelRiderLaunchCharge();
            return;
        }

        float cost = OWAttacksConstants.Gorilla.RIDER_LAUNCH_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            cancelRiderLaunchCharge();
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        cancelRiderLaunchCharge();
        startSecondaryCooldown();
        this.entityData.set(RIDER_LAUNCH_TICK, OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS);

        float factor = Mth.clamp(chargeFactor, 0f, 1f);
        double power = Mth.lerp(factor,
                OWAttacksConstants.Gorilla.RIDER_LAUNCH_MIN_POWER,
                OWAttacksConstants.Gorilla.RIDER_LAUNCH_MAX_POWER);

        Vec3 look = rider.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) flat = new Vec3(this.getLookAngle().x, 0, this.getLookAngle().z);
        flat = flat.normalize();

        double lift = power * OWAttacksConstants.Gorilla.RIDER_LAUNCH_LIFT_RATIO;

        rider.stopRiding();
        rider.setDeltaMovement(flat.x * power, lift, flat.z * power);
        rider.hurtMarked = true;
        rider.fallDistance = 0f;
        rider.hasImpulse = true;

        launchedRiderId = rider.getId();
        launchedRiderTimer = OWAttacksConstants.Gorilla.RIDER_LAUNCH_FALL_IMMUNITY_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.NEUTRAL,
                1.2f, (float) OWUtils.generateRandomInterval(0.8, 1.0));
    }

    private void handleLaunchedRider() {
        if (launchedRiderTimer <= 0) {
            launchedRiderId = -1;
            return;
        }

        launchedRiderTimer--;
        Entity rider = this.level().getEntity(launchedRiderId);
        if (rider == null) {
            launchedRiderTimer = 0;
            launchedRiderId = -1;
            return;
        }

        rider.fallDistance = 0f;
        if (rider.onGround() && launchedRiderTimer < OWAttacksConstants.Gorilla.RIDER_LAUNCH_FALL_IMMUNITY_TICKS - 5) {
            launchedRiderTimer = 0;
            launchedRiderId = -1;
        }
    }

    public boolean activateChestBeat() {
        if (this.level().isClientSide()) return false;
        if (isChestBeating()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Gorilla.CHEST_BEAT_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Gorilla.CHEST_BEAT_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        cancelRockCharge();
        cancelRiderLaunchCharge();
        resetCombo(0);
        actualAttackNumber = 0;

        this.entityData.set(CHEST_BEAT_TICK, OWAttacksConstants.Gorilla.CHEST_BEAT_WINDUP_TICKS
                + OWAttacksConstants.Gorilla.CHEST_BEAT_GESTURE_TICKS);
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL, 3.0f, 0.7f);
        return true;
    }

    private void handleChestBeat() {
        int tick = this.entityData.get(CHEST_BEAT_TICK);
        if (tick > 0) {
            this.entityData.set(CHEST_BEAT_TICK, tick - 1);
            if (tick - 1 == OWAttacksConstants.Gorilla.CHEST_BEAT_GESTURE_TICKS) {
                executeChestBeat();
            }
            playChestDrum(chestBeatTotalTicks() - tick);
        }

        if (chestBeatBuffTimer > 0) {
            chestBeatBuffTimer--;
            if (chestBeatBuffTimer == 0) clearChestBeatBonuses();
        }
    }

    private static int chestBeatTotalTicks() {
        return OWAttacksConstants.Gorilla.CHEST_BEAT_WINDUP_TICKS
                + OWAttacksConstants.Gorilla.CHEST_BEAT_GESTURE_TICKS;
    }

    private void playChestDrum(int elapsed) {
        if (elapsed < OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_FIRST_TICK) return;
        if (elapsed > OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_LAST_TICK) return;
        if ((elapsed - OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_FIRST_TICK)
                % OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_INTERVAL != 0) return;

        boolean rightHand = ((elapsed - OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_FIRST_TICK)
                / OWAttacksConstants.Gorilla.CHEST_BEAT_DRUM_INTERVAL) % 2 == 0;

        this.level().playSound(null, this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL,
                0.55f, rightHand ? 0.58f : 0.66f);
    }

    private void executeChestBeat() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double radius = OWAttacksConstants.Gorilla.CHEST_BEAT_RADIUS;
        AABB area = this.getBoundingBox().inflate(radius);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_STUNNED, SoundSource.NEUTRAL, 3.0f, 0.8f);

        for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living == this) continue;

            if (isChestBeatAlly(living)) {
                applyChestBeatBonus(living);
                continue;
            }

            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    OWEffects.FEAR_EFFECT.getDelegate(), OWAttacksConstants.Gorilla.CHEST_BEAT_FEAR_TICKS, 0));

            Vec3 push = living.position().subtract(this.position());
            if (push.lengthSqr() < 1.0E-4) push = this.getLookAngle();
            push = push.normalize().scale(OWAttacksConstants.Gorilla.CHEST_BEAT_PUSH_POWER);
            living.push(push.x, 0.25, push.z);
            living.hurtMarked = true;

            if (living instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == this) {
                mob.setTarget(null);
            }
        }

        applyChestBeatBonus(this);
        chestBeatBuffTimer = OWAttacksConstants.Gorilla.CHEST_BEAT_DURATION_TICKS;
    }

    private boolean isChestBeatAlly(LivingEntity living) {
        if (living instanceof Player player) return this.isOwnedBy(player);
        if (this.getOwnerUUID() == null) return living instanceof GorillaEntity gorilla && !gorilla.isTame();
        return living instanceof TamableAnimal tamable
                && this.getOwnerUUID().equals(tamable.getOwnerUUID());
    }

    private void applyChestBeatBonus(LivingEntity living) {
        AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) return;

        damage.removeModifier(CHEST_BEAT_DAMAGE_MODIFIER);
        damage.addTransientModifier(new AttributeModifier(
                CHEST_BEAT_DAMAGE_MODIFIER,
                OWAttacksConstants.Gorilla.CHEST_BEAT_ALLY_DAMAGE_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                OWAttacksConstants.Gorilla.CHEST_BEAT_DURATION_TICKS, 0, false, false, true));

        if (living != this && !chestBeatAllies.contains(living.getId())) chestBeatAllies.add(living.getId());
    }

    private void clearChestBeatBonuses() {
        removeChestBeatBonus(this);
        for (int id : chestBeatAllies) {
            if (this.level().getEntity(id) instanceof LivingEntity ally) removeChestBeatBonus(ally);
        }
        chestBeatAllies.clear();
    }

    private static void removeChestBeatBonus(LivingEntity living) {
        AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.removeModifier(CHEST_BEAT_DAMAGE_MODIFIER);
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.5 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        if (isChestBeating()) {
            Vec3 shoulders = new Vec3(0, 0, ULTIMATE_SEAT_FORWARD)
                    .yRot((float) Math.toRadians(-this.yBodyRot));

            passenger.fallDistance = 0f;
            function.accept(passenger,
                    this.getX() + shoulders.x,
                    this.getY() + getBaseRiderYOffset() + ULTIMATE_SEAT_LIFT + getRiderAnimYOffset(),
                    this.getZ() + shoulders.z);

            float fixedYaw = this.getYRot();
            passenger.setYRot(fixedYaw);
            if (passenger instanceof LivingEntity living) {
                living.yBodyRot = fixedYaw;
                living.yHeadRot = fixedYaw;
            }
            return;
        }

        Vec3 seatOffset = new Vec3(0, 0, isClimbing() || isVaulting() ? SEAT_FORWARD_CLIMBING : SEAT_FORWARD)
                .yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseGorillaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private GorillaVariant chooseGorillaVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 3) return GorillaVariant.ALBINOS;
        if (roll < 15) return GorillaVariant.SILVER;
        if (roll < 40) return GorillaVariant.DARK;
        return GorillaVariant.DEFAULT;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(120, true);

        if (isChestBeating()) this.chestBeatAnimationState.startIfStopped(this.tickCount);
        else this.chestBeatAnimationState.stop();

        handleMiscIdleAnimations();
        setupComboAnimations();
    }

    private void handleMiscIdleAnimations() {
        if (this.miscIdleAnimationState.isStarted()
                && this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_2_DURATION) {
            this.miscIdleAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (canPlayIdleAnimation() && !isAnyIdleAnimationPlaying()) {
            this.miscIdleAnimationState.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(500, 1000);
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (33 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (33 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (36 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !isChestBeating();
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof GorillaEntity gorilla) {
            gorilla.setVariant(GorillaVariant.byId(variant));
            gorilla.setInitialVariant(GorillaVariant.byId(variant));
        }
    }

    public GorillaVariant getVariant() {
        return GorillaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(GorillaVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(GorillaVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public GorillaVariant getInitialVariant() {
        return GorillaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(GorillaVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public void setMadByRider(boolean isMad) {
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD); }

    public boolean isRockCharging() { return this.entityData.get(IS_ROCK_CHARGING); }

    public int getRockThrowTick() { return this.entityData.get(ROCK_THROW_TICK); }

    public boolean isLaunchCharging() { return this.entityData.get(IS_LAUNCH_CHARGING); }

    public int getRiderLaunchTick() { return this.entityData.get(RIDER_LAUNCH_TICK); }

    public int getChestBeatTick() { return this.entityData.get(CHEST_BEAT_TICK); }

    public boolean isChestBeating() { return getChestBeatTick() > 0; }

    public boolean isChestBeatGesture() {
        return getChestBeatTick() > 0 && getChestBeatTick() <= OWAttacksConstants.Gorilla.CHEST_BEAT_GESTURE_TICKS;
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }

    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    // ==================================================
    //               DONNÉES SAUVEGARDÉES
    // ==================================================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) setUltimateKillCount(tag.getInt("ultimateKillCount"));
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    class GorillaMeleeAttackGoal extends MeleeAttackGoal {

        public GorillaMeleeAttackGoal() {
            super(GorillaEntity.this, 6, true);
        }

        @Override
        public void start() {
            super.start();
            if (GorillaEntity.this.getRemainingPersistentAngerTime() <= 0) {
                GorillaEntity.this.startPersistentAngerTimer();
            }
            GorillaEntity.this.setMad(true);
            GorillaEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            GorillaEntity.this.setMad(false);
            GorillaEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 3.2;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;

            if (this.mob instanceof OWEntity owEntity) {
                if (!owEntity.isCombo()) {
                    owEntity.setCombo(true, 1);
                } else if (owEntity.isPauseCombo()) {
                    owEntity.playerContinueCombo = true;
                }
            }
        }
    }
}
