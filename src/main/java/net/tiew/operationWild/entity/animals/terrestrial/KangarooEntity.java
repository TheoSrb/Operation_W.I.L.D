package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWIndication;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWGrabberEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.neoforge.common.CommonHooks;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooAlertedFleeGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooAngerTargetGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooAngryAttackGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooDrownPursuerGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooFleeToWaterGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooGrazeGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooSeekShadeGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooShadeNapGoal;
import net.tiew.operationWild.entity.goals.kangaroo.KangarooThumpAlertGoal;
import net.tiew.operationWild.entity.navigation.KangarooMoveControl;
import net.tiew.operationWild.entity.variants.KangarooVariant;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class KangarooEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity, PlayerRideableJumping, NeutralMob {

    public static final double TAMING_EXPERIENCE = 65.0;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(25, 40);
    public static final double HERD_ANGER_RADIUS = 20.0;

    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GRAZING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_THUMPING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PIVOTING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DROWN_TARGET_ID = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public static final int DROWN_MAX_TIMEOUT = 160;
    public static final int DROWN_START_TIMEOUT = 70;
    public static final int DROWN_COOLDOWN_TICKS = 220;
    public static final int DROWN_STRUGGLE_REDUCTION = 14;
    public static final float DROWN_DAMAGE = 1.0f;
    public static final int DROWN_DAMAGE_INTERVAL = 25;
    public static final double DROWN_HOLD_FORWARD = 1.0;
    public static final double DROWN_HOLD_RISE = 1.05;
    public static final double DROWN_SUBMERGE_MARGIN = 0.28;
    public static final float DROWN_MIN_HEALTH_RATIO = 0.12f;

    public static final int DROWN_WINDUP_TICKS = 22;
    public static final double DROWN_WINDUP_MAX_RANGE = 4.0;

    private static final EntityDataAccessor<Integer> DROWN_TIMEOUT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DROWN_WINDUP = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public final AnimationState drownWindupAnimationState = new AnimationState();

    private int drownCooldown = 0;
    private LivingEntity pendingDrownVictim;

    public static final float PIVOT_DAMAGE_MULTIPLIER = 1.25f;
    public static final float AI_STEP_JUMP_FACTOR = 0.45f;

    private static final EntityDataAccessor<Boolean> IS_HOPPING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HOP_ID = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public static final double HOP_POWER_RIDDEN = 0.30;
    public static final int HOP_GROUND_DELAY = 8;
    public static final int HOP_GROUND_DELAY_FAST = 2;
    public static final double HOP_HURRY_SPEED = 0.30;

    public static final double HOP_WALK_HEIGHT = 3.0;
    public static final double HOP_WALK_DISTANCE = 2.0;
    public static final double HOP_RUN_HEIGHT = 1.0;
    public static final double HOP_RUN_DISTANCE = 6.0;
    public static final double HOP_REFERENCE_SPEED = 0.21;
    public static final double HOP_DISTANCE_FACTOR_MIN = 0.6;
    public static final double HOP_DISTANCE_FACTOR_MAX = 3.0;
    public static final double HOP_HEIGHT_CALIBRATION = 0.94;
    public static final double HOP_TICK_GRAVITY = 0.08;
    public static final double HOP_TARGET_MARGIN = 1.2;
    public static final double HOP_TARGET_MIN_REACH = 1.5;
    public static final float HOP_CHASE_TURN = 0.35f;
    public static final double HOP_AIR_FRICTION = 0.91;
    public static final double HOP_GROUND_BRAKE = 0.55;
    public static final float HOP_STEER = 0.10f;
    public static final float HOP_STEER_CHASE = 0.45f;
    public static final float HOP_STEER_RIDDEN = 0.65f;
    public static final double AI_SPEED_TO_BLOCKS = 1.0;
    public static final double HOP_MIN_SPEED = 0.012;

    private Vec3 hopDir = Vec3.ZERO;
    private double hopSpeed = 0.0;
    private int hopGroundTicks = 0;
    private boolean hopWasAirborne = false;

    public int hopAnimTicks = 0;
    public int hopAnimPeriod = 14;
    private int hopAnimLastId = -1;
    private boolean hopAnimLocalTrigger = false;
    private static final EntityDataAccessor<Integer> ALERT_TICKS = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public static final int ALERT_DURATION_TICKS = 600;

    public final AnimationState thumpAnimationState = new AnimationState();
    private int thumpAnimationTimeout = 0;

    public final AnimationState napAnimationState = new AnimationState();
    private int napAnimationTimeout = 0;

    public final AnimationState drownAnimationState = new AnimationState();

    public static final float HOT_BIOME_TEMPERATURE = 0.95f;
    public static final long HOT_HOURS_START = 4000L;
    public static final long HOT_HOURS_END = 9000L;
    public static final int OVERHEAT_THRESHOLD_TICKS = 300;
    public static final int OVERHEAT_MAX_TICKS = 900;

    private int overheatTicks = 0;

    private LivingEntity alertSource;
    private int herdThumpCooldown = 0;

    private static final EntityDataAccessor<Boolean> IS_SPINNING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_WEARING_BOXING_GLOVES = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WHIRLWIND_COOLDOWN = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TELLURIC_STOMP_PHASE = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public static final int STOMP_PHASE_NONE   = 0;
    public static final int STOMP_PHASE_WINDUP = 1;
    public static final int STOMP_PHASE_LEAP   = 2;
    public static final int STOMP_PHASE_HOVER  = 3;
    public static final int STOMP_PHASE_DIVE   = 4;

    private int telluricStompWindupTimer = 0;
    private int telluricStompLeapTimer = 0;
    private int telluricStompLeapElapsed = 0;
    private int telluricStompHoverTimer = 0;
    private int telluricStompDiveTimer = 0;
    private int telluricStompDiveElapsed = 0;
    private boolean telluricLeapImpulseApplied = false;

    public int telluricStompPhaseTicks = 0;
    private int telluricStompLastPhase = STOMP_PHASE_NONE;
    public int telluricStompOutroTicks = 0;
    public float telluricSpinAngle = 0f;
    public float telluricSpinSpeed = 0f;

    private int spinTicks = 0;
    private int sinceLastDamage = 0;
    private int whirlwindSoundTimer = 0;

    private float spinStopSpeed = 0f;
    private boolean wasSpinningForSpeed = false;

    public static final int WHIRLWIND_OUTRO_TICKS = 10;

    public int clientSpinTicks = 0;
    public float clientAnimTimeMs = 0f;
    public float clientSpinSpeed = 1f;
    public int clientOutroTicks = 0;
    private boolean clientWasSpinning = false;



    public boolean fourthHitFired = false;

    public volatile float bodyAnimY = 0f;

    protected float playerJumpPendingScale = 0f;
    private boolean isRidingJump = false;
    private int ridingJumpTimer = 0;

    public KangarooEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.moveControl = new KangarooMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.JUMP_STRENGTH, 1.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new KangarooDrownPursuerGoal(this));
        this.goalSelector.addGoal(2, new KangarooFleeToWaterGoal(this, 4.7D));
        this.goalSelector.addGoal(3, new KangarooAngryAttackGoal(this, 4.5D, 20, 3.0D));
        this.goalSelector.addGoal(4, new KangarooThumpAlertGoal(this));
        this.goalSelector.addGoal(5, new KangarooAlertedFleeGoal(this, 5.1D));
        this.goalSelector.addGoal(5, new KangarooShadeNapGoal(this, 1.4f, 700));
        this.goalSelector.addGoal(7, new KangarooSeekShadeGoal(this, 1.5D));
        this.goalSelector.addGoal(8, new KangarooGrazeGoal(this, 1.25D, 300));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.15D));
        this.goalSelector.addGoal(10, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new KangarooAngerTargetGoal(this));

        this.lookControl = new net.minecraft.world.entity.ai.control.LookControl(this) {
            @Override
            public void tick() {
                LivingEntity target = KangarooEntity.this.getTarget();
                if (target != null && KangarooEntity.this.getControllingPassenger() == null) {
                    KangarooEntity.this.getLookControl().setLookAt(target, 45.0f, 45.0f);
                }
                super.tick();
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_HOPPING, false);
        builder.define(HOP_ID, 0);
        builder.define(IS_GRAZING, false);
        builder.define(IS_THUMPING, false);
        builder.define(IS_PIVOTING, false);
        builder.define(DROWN_TARGET_ID, -1);
        builder.define(DROWN_TIMEOUT, 0);
        builder.define(DROWN_WINDUP, 0);
        builder.define(ALERT_TICKS, 0);
        builder.define(IS_SPINNING, false);
        builder.define(WHIRLWIND_COOLDOWN, 0);
        builder.define(IS_WEARING_BOXING_GLOVES, false);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(TELLURIC_STOMP_PHASE, 0);
    }

    @Override
    public int getEntityColor() {
        return 0xd7b17d;
    }

    @Override
    public float getTheoreticalScale() {
        return 7.5f;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.SCOUT;
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
    public float vehicleRunSpeedMultiplier() {
        return 3.75f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return -1f;
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
        return OWItems.TIGER_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.TIGER_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 150f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.0f * (1 + ((float) this.getLevel() / 50));
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
    protected int sitTransitionTicks() {
        return 25;
    }

    @Override
    public boolean riderCameraFollowsBodyTilt() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        return 0.225f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.KANGAROO.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.TIGER_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.625 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public int getComboPauseDelay() {
        return getComboAttack() == 3 ? 22 : 2;
    }

    @Override
    protected boolean isImmobile() {
        return isSpinning() || isTelluricStomping() || super.isImmobile();
    }

    @Override
    protected boolean isLeapingVehicle() {
        return isTelluricStomping() || super.isLeapingVehicle();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isSpinning()) {
            wasSpinningForSpeed = true;
            spinStopSpeed *= 0.65f;
            if (spinStopSpeed < 0.005f) spinStopSpeed = 0f;
            return spinStopSpeed;
        }
        if (wasSpinningForSpeed) {
            wasSpinningForSpeed = false;
            resetRiddenSpeed();
        }
        float speed = super.getRiddenSpeedVehicle(player);
        spinStopSpeed = speed;
        return speed;
    }

    @Override
    protected boolean forceRiderLookBodyRotation() {
        return isSpinning() || isTelluricStomping();
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return 0;
    }

    @Override
    protected float getJumpPower() {
        if (this.getControllingPassenger() != null) return super.getJumpPower();
        return super.getJumpPower() * AI_STEP_JUMP_FACTOR;
    }

    public boolean isHopping() { return this.entityData.get(IS_HOPPING); }
    private void setHopping(boolean value) {
        if (this.level().isClientSide()) return;
        if (this.entityData.get(IS_HOPPING) != value) this.entityData.set(IS_HOPPING, value);
    }

    private boolean useHopLocomotion() {
        return this.getControllingPassenger() == null
                && !this.isInWater()
                && !this.isPassenger()
                && !this.isSpinning()
                && !this.isTelluricStomping()
                && !this.isDrowningSomeone()
                && !this.isDrownWindingUp()
                && !this.isNapping()
                && !this.isSleeping()
                && !this.isSitting()
                && !this.isGrazing()
                && !this.isThumping()
                && !this.isImmobile();
    }

    private void faceChasedTarget() {
        LivingEntity chased = this.getTarget();
        if (chased == null || !chased.isAlive()) return;
        if (this.getControllingPassenger() != null) return;
        if (this.isSitting() || this.isNapping() || this.isSleeping()) return;

        double dx = chased.getX() - this.getX();
        double dz = chased.getZ() - this.getZ();
        if (dx * dx + dz * dz < 1.0e-4) return;

        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(Mth.rotLerp(HOP_CHASE_TURN, this.getYRot(), wanted));
        this.yBodyRot = this.getYRot();
        this.setYHeadRot(Mth.rotLerp(HOP_CHASE_TURN, this.yHeadRot, wanted));
        this.getLookControl().setLookAt(chased, 60.0f, 60.0f);
    }

    private double clampHopToTarget(double speed, double power) {
        LivingEntity chased = this.getTarget();
        if (chased == null || !chased.isAlive()) return speed;

        double distance = Math.sqrt(this.distanceToSqr(chased));
        double reach = Math.max(HOP_TARGET_MIN_REACH, distance - HOP_TARGET_MARGIN);

        return Math.max(HOP_MIN_SPEED * 2.0, Math.min(speed, reach / hopAirTicks(power)));
    }

    private double hopPower(boolean running) {
        double height = running ? HOP_RUN_HEIGHT : HOP_WALK_HEIGHT;
        return Math.sqrt(2.0 * HOP_TICK_GRAVITY * height) * HOP_HEIGHT_CALIBRATION;
    }

    private double hopAirTicks(double power) {
        return Math.max(1.0, 2.0 * power / HOP_TICK_GRAVITY);
    }

    private double hopSpeedReference() {
        if (this.getControllingPassenger() != null) {
            Vec3 motion = this.getDeltaMovement();
            return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        }
        return this.getSpeed();
    }

    private double hopForwardSpeed(boolean running, double power) {
        double air = hopAirTicks(power);

        if (this.getControllingPassenger() != null) {
            return hopSpeedReference() * (air + HOP_GROUND_DELAY_FAST) / air;
        }

        double base = running ? HOP_RUN_DISTANCE : HOP_WALK_DISTANCE;
        double speedFactor = Mth.clamp(hopSpeedReference() / HOP_REFERENCE_SPEED,
                HOP_DISTANCE_FACTOR_MIN, HOP_DISTANCE_FACTOR_MAX);
        return (base * speedFactor) / air;
    }

    private Vec3 hopIntent() {
        if (this.getControllingPassenger() != null) {
            Vec3 motion = this.getDeltaMovement();
            return new Vec3(motion.x, 0.0, motion.z);
        }

        float forward = this.zza;
        float strafe = this.xxa;
        if (Math.abs(forward) < 1.0e-3f && Math.abs(strafe) < 1.0e-3f) return Vec3.ZERO;

        LivingEntity chased = this.getTarget();
        if (chased != null && chased.isAlive() && this.getSensing().hasLineOfSight(chased)) {
            Vec3 straight = chased.position().subtract(this.position()).multiply(1, 0, 1);
            if (straight.lengthSqr() > 1.0e-4) {
                return straight.normalize().scale(this.getSpeed() * AI_SPEED_TO_BLOCKS);
            }
        }

        Vec3 ahead = Vec3.directionFromRotation(0f, this.getYRot());
        Vec3 side = new Vec3(-ahead.z, 0.0, ahead.x);
        Vec3 wanted = ahead.scale(forward).add(side.scale(strafe));
        if (wanted.lengthSqr() < 1.0e-6) return Vec3.ZERO;

        return wanted.normalize().scale(this.getSpeed() * AI_SPEED_TO_BLOCKS);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!this.isControlledByLocalInstance() || !useHopLocomotion()) {
            setHopping(false);
            super.travel(travelVector);
            return;
        }

        Vec3 intent = hopIntent();
        boolean wantsMove = intent.lengthSqr() > HOP_MIN_SPEED * HOP_MIN_SPEED;

        boolean ridden = this.getControllingPassenger() != null;

        if (this.onGround()) {
            hopGroundTicks++;

            boolean hurry = intent.length() >= HOP_HURRY_SPEED;
            int delay = (ridden || hurry) ? HOP_GROUND_DELAY_FAST : HOP_GROUND_DELAY;

            if (wantsMove && hopGroundTicks >= delay) {
                hopDir = intent.normalize();

                boolean running = this.isRunning() || (!ridden && hurry);
                double power = hopPower(running);
                hopSpeed = hopForwardSpeed(running, power);
                hopSpeed = clampHopToTarget(hopSpeed, power);

                this.setDeltaMovement(hopDir.x * hopSpeed / HOP_AIR_FRICTION, power, hopDir.z * hopSpeed / HOP_AIR_FRICTION);
                this.hasImpulse = true;
                hopGroundTicks = 0;
                setHopping(true);
                if (this.level().isClientSide()) {
                    hopAnimLocalTrigger = true;
                } else {
                    this.entityData.set(HOP_ID, (this.entityData.get(HOP_ID) + 1) & 0xFFFF);
                }
                CommonHooks.onLivingJump(this);
            } else {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x * HOP_GROUND_BRAKE, motion.y, motion.z * HOP_GROUND_BRAKE);
                if (!wantsMove) {
                    setHopping(false);
                    hopSpeed = 0.0;
                }
            }
        } else {
            hopGroundTicks = 0;

            if (hopSpeed > HOP_MIN_SPEED) {
                if (wantsMove) {
                    float steer = ridden ? HOP_STEER_RIDDEN
                            : (this.getTarget() != null ? HOP_STEER_CHASE : HOP_STEER);
                    Vec3 target = intent.normalize();
                    hopDir = hopDir.lerp(target, steer);
                    if (hopDir.lengthSqr() > 1.0e-6) hopDir = hopDir.normalize();
                }

                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(hopDir.x * hopSpeed / HOP_AIR_FRICTION,
                        motion.y,
                        hopDir.z * hopSpeed / HOP_AIR_FRICTION);
                setHopping(true);
            }
        }

        super.travel(travelVector);
    }

    private void tickHopLanding() {
        if (this.level().isClientSide()) return;

        if (this.getControllingPassenger() != null || !useHopLocomotion()) {
            setHopping(false);
            hopWasAirborne = false;
            return;
        }

        boolean airborne = !this.onGround();
        if (hopWasAirborne && !airborne && isHopping() && !isTelluricStomping()) {
            createLandingShockwave();
        }
        hopWasAirborne = airborne;
    }

    private void createLandingShockwave() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        BlockState ground = this.level().getBlockState(this.blockPosition().below());
        BlockParticleOption particleOption = ground.isAir()
                ? new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState())
                : new BlockParticleOption(ParticleTypes.BLOCK, ground);

        serverLevel.sendParticles(particleOption,
                this.getX(), this.getY() + 0.05, this.getZ(),
                26, 0.45, 0.05, 0.45, 0.14);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ROOTED_DIRT_HIT, SoundSource.AMBIENT, 0.85f, 1.0f);
    }

    private void tickHopAnimation() {
        boolean localAuthority = this.isControlledByLocalInstance() && this.getControllingPassenger() != null;
        boolean newHop;

        if (localAuthority) {
            newHop = hopAnimLocalTrigger;
            hopAnimLocalTrigger = false;
            hopAnimLastId = this.entityData.get(HOP_ID);
        } else {
            int id = this.entityData.get(HOP_ID);
            newHop = id != hopAnimLastId;
            hopAnimLastId = id;
        }

        if (newHop) {
            if (hopAnimTicks > 3 && hopAnimTicks < 40) {
                hopAnimPeriod = Mth.lerpInt(0.5f, hopAnimPeriod, hopAnimTicks);
            }
            hopAnimTicks = 0;
        } else {
            hopAnimTicks++;
        }
    }

    @Override
    public void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        if (isRidingJump) {
            ridingJumpTimer++;
            if (ridingJumpTimer > 5 && this.onGround()) {
                isRidingJump = false;
                ridingJumpTimer = 0;
            }
        } else {
            ridingJumpTimer = 0;
        }

        if (this.isControlledByLocalInstance() && this.onGround() && !isRidingJump) {
            if (playerJumpPendingScale > 0f && !isSpinning()) {
                executeRidersJump(playerJumpPendingScale);
            }
            playerJumpPendingScale = 0f;
        }

        if (this.isControlledByLocalInstance() && getTelluricStompPhase() == STOMP_PHASE_LEAP) {
            float progress = Mth.clamp(
                    (float) telluricStompLeapElapsed / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS, 0f, 1f);
            float ease = (1f - progress) * (1f - progress);
            Vec3 flat = Vec3.directionFromRotation(0f, player.getYRot());
            double fwd = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_FORWARD;
            this.setDeltaMovement(
                    flat.x * fwd,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_POWER * ease,
                    flat.z * fwd);
            this.hasImpulse = true;
            if (!telluricLeapImpulseApplied) {
                CommonHooks.onLivingJump(this);
                telluricLeapImpulseApplied = true;
            }
        }
    }

    private void executeRidersJump(float scale) {
        double verticalPower = this.getAttributeValue(Attributes.JUMP_STRENGTH) * scale
                * (double) this.getBlockJumpFactor()
                + (double) (this.getJumpBoostPower() * 3);

        Vec3 lookFlat = this.getLookAngle().multiply(1, 0, 1);
        if (lookFlat.lengthSqr() > 1.0E-7D) lookFlat = lookFlat.normalize();

        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(
                movement.x + lookFlat.x * 0.4 * scale,
                verticalPower,
                movement.z + lookFlat.z * 0.4 * scale
        );
        this.hasImpulse = true;
        CommonHooks.onLivingJump(this);

        this.level().playLocalSound(getX(), getY(), getZ(),
                SoundEvents.HORSE_JUMP, SoundSource.NEUTRAL, 0.4f, 1.0f, false);

        isRidingJump = true;
        ridingJumpTimer = 0;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        if (!this.isSaddled()) return;
        if (jumpPower < 0) jumpPower = 0;
        this.playerJumpPendingScale = jumpPower >= 90 ? 1.0f : 0.4f + 0.4f * jumpPower / 90.0f;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled() && !isSpinning();
    }

    @Override
    public void handleStartJump(int jumpPower) { }

    @Override
    public void handleStopJump() { }

    @Override
    public void tick() {
        super.tick();

        int timeToHit = getComboAttack() == 3 ? 6 : 10;
        int timeMax = getComboAttack() == 3 ? 35 : 20;
        createCombo(timeMax, timeToHit, SoundEvents.PLAYER_ATTACK_STRONG, 3.0, 3.0, 1.5, false, 1.5f);

        if (isPauseCombo() && getComboAttack() == 3) {
            resetCombo(0);
            actualAttackNumber = 0;
        }

        tickWhirlwind();
        tickTelluricStomp();
        tickAlert();
        tickOverheat();
        tickDrownWindup();
        tickDrowning();
        tickHopLanding();
        if (!this.level().isClientSide()) faceChasedTarget();

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) {
            tickHopAnimation();
            setupAnimationState();
        }
        if (this.isInResurrection()) this.setSleeping(true);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD); }
    public void setMad(boolean value) { this.entityData.set(IS_MAD, value); }

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
    protected void customServerAiStep() {
        if (this.level() instanceof ServerLevel serverLevel) this.updatePersistentAnger(serverLevel, false);
        super.customServerAiStep();
    }

    public void angerAt(LivingEntity attacker) {
        if (this.level().isClientSide() || attacker == null) return;
        if (this.isTame() || this.isBaby()) return;
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        if (this.isAlliedTo(attacker)) return;

        this.startPersistentAngerTimer();
        this.setPersistentAngerTarget(attacker.getUUID());

        this.setSitting(false);
        this.setNap(false);
        this.setGrazing(false);
        this.setTarget(attacker);
    }

    private void propagateAngerToHerd(LivingEntity attacker) {
        if (this.level().isClientSide() || attacker == null) return;

        List<KangarooEntity> herd = this.level().getEntitiesOfClass(KangarooEntity.class,
                this.getBoundingBox().inflate(HERD_ANGER_RADIUS),
                other -> other != this && other.isAlive() && !other.isTame() && !other.isBaby() && !other.isVehicle());

        for (KangarooEntity other : herd) {
            other.angerAt(attacker);
        }
    }

    public boolean isGrazing() { return this.entityData.get(IS_GRAZING); }
    public void setGrazing(boolean value) { this.entityData.set(IS_GRAZING, value); }

    public boolean isThumping() { return this.entityData.get(IS_THUMPING); }
    public void setThumping(boolean value) { this.entityData.set(IS_THUMPING, value); }

    public boolean isPivoting() { return this.entityData.get(IS_PIVOTING); }
    public void setPivoting(boolean value) {
        if (this.entityData.get(IS_PIVOTING) != value) this.entityData.set(IS_PIVOTING, value);
    }

    public int getHerdThumpCooldown() { return this.herdThumpCooldown; }
    public void setHerdThumpCooldown(int ticks) { this.herdThumpCooldown = Math.max(this.herdThumpCooldown, ticks); }

    public int getAlertTicks() { return this.entityData.get(ALERT_TICKS); }
    private void setAlertTicks(int value) { this.entityData.set(ALERT_TICKS, Math.max(0, value)); }
    public boolean isAlerted() { return getAlertTicks() > 0; }

    public LivingEntity getAlertSource() { return this.alertSource; }

    public void raiseAlert(LivingEntity source) {
        if (this.level().isClientSide()) return;
        this.alertSource = source;
        setAlertTicks(ALERT_DURATION_TICKS);
    }

    public boolean isDrowningSomeone() { return this.entityData.get(DROWN_TARGET_ID) != -1; }

    public LivingEntity getDrownVictim() {
        int id = this.entityData.get(DROWN_TARGET_ID);
        if (id == -1) return null;
        return this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    public boolean canStartDrowning() {
        return !this.level().isClientSide()
                && !this.isTame()
                && !this.isBaby()
                && !this.isVehicle()
                && !this.isDrowningSomeone()
                && !isDrownWindingUp()
                && drownCooldown <= 0
                && this.isInWater()
                && isOverDeepWater();
    }

    public int getDrownWindupTicks() { return this.entityData.get(DROWN_WINDUP); }
    public boolean isDrownWindingUp() { return getDrownWindupTicks() > 0; }

    public void startDrownWindup(LivingEntity victim) {
        if (this.level().isClientSide() || victim == null) return;

        this.pendingDrownVictim = victim;
        this.entityData.set(DROWN_WINDUP, DROWN_WINDUP_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 0.9f, 1.45f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 0.8f, 1.7f);
    }

    public void cancelDrownWindup() {
        if (this.level().isClientSide()) return;
        this.pendingDrownVictim = null;
        this.entityData.set(DROWN_WINDUP, 0);
    }

    private void tickDrownWindup() {
        if (this.level().isClientSide()) return;
        if (!isDrownWindingUp()) return;

        if (isDrowningSomeone()) {
            cancelDrownWindup();
            return;
        }

        LivingEntity victim = pendingDrownVictim;
        boolean stillValid = victim != null
                && victim.isAlive()
                && !victim.isRemoved()
                && victim.level() == this.level()
                && this.isInWater()
                && !(victim instanceof Player player && (player.isCreative() || player.isSpectator()));

        if (!stillValid) {
            cancelDrownWindup();
            drownCooldown = DROWN_COOLDOWN_TICKS / 3;
            return;
        }

        this.getLookControl().setLookAt(victim, 40.0f, 40.0f);

        double gap = this.distanceToSqr(victim);
        if (gap > 2.0 * 2.0) {
            Vec3 toVictim = victim.position().subtract(this.position()).multiply(1, 0, 1);
            if (toVictim.lengthSqr() > 1.0e-4) {
                Vec3 pull = toVictim.normalize().scale(0.09);
                this.setDeltaMovement(this.getDeltaMovement().add(pull.x, 0.0, pull.z));
            }
        } else {
            this.getNavigation().stop();
        }

        int left = getDrownWindupTicks();
        int elapsed = DROWN_WINDUP_TICKS - left;

        if (elapsed % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    8, 0.6, 0.05, 0.6, 0.12);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                    4, 0.3, 0.3, 0.3, 0.02);
        }
        if (elapsed % 6 == 3) {
            float rise = (float) elapsed / DROWN_WINDUP_TICKS;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SWIM, SoundSource.NEUTRAL, 0.8f, 0.9f + rise * 0.6f);
        }

        this.entityData.set(DROWN_WINDUP, left - 1);

        if (left - 1 > 0) return;

        boolean inReach = victim.isInWater()
                && this.distanceToSqr(victim) <= DROWN_WINDUP_MAX_RANGE * DROWN_WINDUP_MAX_RANGE;

        this.pendingDrownVictim = null;

        if (inReach) {
            startDrowning(victim);
        } else {
            drownCooldown = DROWN_COOLDOWN_TICKS / 2;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 1.0f, 1.3f);
        }
    }

    private double drownSurfaceY() {
        BlockPos pos = this.blockPosition();
        for (int i = 0; i <= 5; i++) {
            BlockPos probe = pos.above(i);
            if (!this.level().getFluidState(probe).is(FluidTags.WATER)) return probe.getY();
        }
        return pos.getY() + 6;
    }

    private boolean isOverDeepWater() {
        BlockPos pos = this.blockPosition();
        return this.level().getFluidState(pos).is(FluidTags.WATER)
                && this.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    public int getGrabTimeout() { return this.entityData.get(DROWN_TIMEOUT); }
    public void setGrabTimeout(int value) { this.entityData.set(DROWN_TIMEOUT, Mth.clamp(value, 0, DROWN_MAX_TIMEOUT)); }
    public int getGrabMaxTimeout() { return DROWN_MAX_TIMEOUT; }

    public void startDrowning(LivingEntity victim) {
        if (this.level().isClientSide() || victim == null) return;
        if (!victim.startRiding(this, true)) return;

        this.entityData.set(DROWN_TARGET_ID, victim.getId());
        setGrabTimeout(victim instanceof Player ? DROWN_START_TIMEOUT : 0);
        victim.noPhysics = true;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.2f, 0.75f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.0f, 1.35f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 0.9f, 0.7f);
    }

    public void releaseDrownVictim() {
        if (this.level().isClientSide()) return;

        LivingEntity victim = getDrownVictim();
        if (victim != null) {
            victim.noPhysics = false;
            if (victim.getVehicle() == this) victim.stopRiding();

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.NEUTRAL, 1.0f, 1.0f);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 1.0f, 0.9f);
        }

        this.entityData.set(DROWN_TARGET_ID, -1);
        setGrabTimeout(0);
        drownCooldown = DROWN_COOLDOWN_TICKS;
    }

    public void breakFreeFromDrown() {
        if (this.level().isClientSide()) return;

        LivingEntity victim = getDrownVictim();
        releaseDrownVictim();
        if (victim == null) return;

        Vec3 push = victim.position().subtract(this.position());
        push = push.lengthSqr() > 1.0e-4 ? push.multiply(1, 0, 1).normalize() : this.getLookAngle().multiply(1, 0, 1);
        victim.setDeltaMovement(push.x * 0.35, 0.55, push.z * 0.35);
        victim.hasImpulse = true;
        victim.hurtMarked = true;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.3f, 1.1f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.0f, 1.6f);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                    40, 0.5, 0.4, 0.5, 0.25);
        }
    }

    private void tickDrowning() {
        if (this.level().isClientSide()) return;

        if (drownCooldown > 0) drownCooldown--;
        if (!isDrowningSomeone()) return;

        LivingEntity victim = getDrownVictim();
        if (victim == null || !victim.isAlive() || victim.isRemoved()
                || victim.level() != this.level()
                || victim.getVehicle() != this
                || (victim instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            releaseDrownVictim();
            return;
        }

        if (!this.isInWater() || this.getHealth() <= this.getMaxHealth() * DROWN_MIN_HEALTH_RATIO) {
            releaseDrownVictim();
            return;
        }

        victim.noPhysics = true;
        victim.fallDistance = 0f;
        victim.setDeltaMovement(Vec3.ZERO);
        if (victim instanceof Mob mob) mob.setTarget(null);

        int timeout = getGrabTimeout();

        if (timeout <= 0 && victim instanceof Player) {
            breakFreeFromDrown();
            return;
        }

        if (victim.isInWater()) {
            victim.setAirSupply(Math.max(-19, victim.getAirSupply() - 4));
        }

        if (timeout > 0 && timeout % DROWN_DAMAGE_INTERVAL == 0) {
            victim.invulnerableTime = 0;
            victim.hurt(this.damageSources().mobAttack(this), DROWN_DAMAGE);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_HURT_DROWN, SoundSource.NEUTRAL, 0.55f, 1.1f);
        }

        if (this.level() instanceof ServerLevel serverLevel && timeout % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.7, victim.getZ(),
                    6, 0.3, 0.35, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    4, 0.5, 0.05, 0.5, 0.06);
        }

        float pressure = (float) timeout / DROWN_MAX_TIMEOUT;

        if (timeout % 18 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 0.85f, 1.0f + pressure * 0.4f);
        }
        if (timeout % 11 == 5) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SWIM, SoundSource.NEUTRAL, 0.7f, 0.7f + pressure * 0.5f);
        }
        if (timeout % 30 == 12) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.NEUTRAL, 0.9f, 0.8f + pressure * 0.5f);
        }
        if (timeout % 40 == 20) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 0.8f, 1.25f + pressure * 0.2f);
        }

        setGrabTimeout(timeout + 1);
        if (getGrabTimeout() >= DROWN_MAX_TIMEOUT) releaseDrownVictim();
    }

    public boolean isHotHours() {
        long time = this.level().getDayTime() % 24000L;
        return time >= HOT_HOURS_START && time < HOT_HOURS_END;
    }

    public boolean isInHotBiome() {
        return this.level().getBiome(this.blockPosition()).value().getBaseTemperature() >= HOT_BIOME_TEMPERATURE;
    }

    public boolean isInShade() {
        return !this.level().canSeeSky(this.blockPosition());
    }

    public int getOverheatTicks() { return this.overheatTicks; }

    private void tickOverheat() {
        if (this.level().isClientSide()) return;

        boolean exposed = !this.isTame() && isHotHours() && isInHotBiome() && !isInShade()
                && !this.isInWater() && !this.isNapping() && !this.isSleeping();

        if (!exposed) {
            if (overheatTicks > 0) overheatTicks -= 2;
            if (overheatTicks < 0) overheatTicks = 0;
            return;
        }

        if (overheatTicks < OVERHEAT_MAX_TICKS) overheatTicks++;

        if (overheatTicks < OVERHEAT_THRESHOLD_TICKS) return;

        if (this.tickCount % 40 == 0) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
            this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
        }

        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 15 == 0) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + this.getBbHeight() * 0.85, this.getZ(),
                    2, 0.25, 0.15, 0.25, 0.01);
        }
    }

    private void tickAlert() {
        if (this.level().isClientSide()) return;

        if (herdThumpCooldown > 0) herdThumpCooldown--;

        if (getAlertTicks() <= 0) {
            alertSource = null;
            return;
        }

        setAlertTicks(getAlertTicks() - 1);

        if (alertSource != null && (!alertSource.isAlive() || alertSource.level() != this.level())) {
            alertSource = null;
            setAlertTicks(0);
        }
    }

    public boolean isSpinning() { return this.entityData.get(IS_SPINNING); }
    private void setSpinning(boolean value) { this.entityData.set(IS_SPINNING, value); }

    public int getWhirlwindCooldownTicks() { return this.entityData.get(WHIRLWIND_COOLDOWN); }
    private void setWhirlwindCooldownTicks(int value) { this.entityData.set(WHIRLWIND_COOLDOWN, Math.max(0, value)); }

    public void startWhirlwind() {
        if (this.level().isClientSide()) return;
        if (isSpinning()) return;
        if (this.isInWater() || !this.onGround()) return;
        if (getWhirlwindCooldownTicks() > 0) return;
        if (getVitalEnergy() > getVitalEnergyCapacity() - OWAttacksConstants.Kangaroo.WHIRLWIND_ENERGY) {
            canShowVitalEnergyLack = true;
            return;
        }
        setSpinning(true);
        spinTicks = 0;
        sinceLastDamage = Integer.MAX_VALUE / 2;
        whirlwindSoundTimer = 0;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1.1f, 0.6f);
    }

    public void stopWhirlwind() {
        if (this.level().isClientSide()) return;
        if (!isSpinning()) return;
        boolean cooldownEarned = spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_THRESHOLD_TICKS;
        setSpinning(false);
        spinTicks = 0;
        if (cooldownEarned) {
            setWhirlwindCooldownTicks(OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_TICKS);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.NEUTRAL, 0.9f, 0.7f);
    }

    private void tickWhirlwind() {
        if (this.level().isClientSide()) {
            if (isSpinning()) {
                clientSpinTicks++;
                clientSpinSpeed = computeAnimSpeedMultiplier(clientSpinTicks);
                clientAnimTimeMs += 50f * clientSpinSpeed;
                clientWasSpinning = true;
            } else {
                if (clientWasSpinning) {
                    clientWasSpinning = false;
                    clientOutroTicks = WHIRLWIND_OUTRO_TICKS;
                }
                if (clientOutroTicks > 0) clientOutroTicks--;
                clientSpinTicks = 0;
                clientSpinSpeed = 1f;
                clientAnimTimeMs = 0f;
            }
            return;
        }

        if (isSpinning()) {
            Player rider = (getFirstPassenger() instanceof Player p) ? p : null;
            if (rider == null || (getOwnerUUID() != null && !getOwnerUUID().equals(rider.getUUID()))) {
                stopWhirlwind();
                return;
            }

            if (this.isInWater() || !this.onGround()) {
                stopWhirlwind();
                return;
            }

            spinTicks++;

            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_START_TICKS) {
                sinceLastDamage++;
                if (sinceLastDamage >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_INTERVAL_TICKS) {
                    sinceLastDamage = 0;
                    dealWhirlwindDamage(computeWhirlwindDamage(spinTicks));
                }
            }

            float speedFactor = Mth.clamp((float) spinTicks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
            int soundInterval = Math.max(2, (int) (8 - 6 * speedFactor));
            if (++whirlwindSoundTimer >= soundInterval) {
                whirlwindSoundTimer = 0;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL,
                        0.55f, 0.7f + 0.9f * speedFactor);
            }

            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS) {
                stopWhirlwind();
            }
        } else if (getWhirlwindCooldownTicks() > 0) {
            setWhirlwindCooldownTicks(getWhirlwindCooldownTicks() - 1);
        }
    }

    private float computeWhirlwindDamage(int ticks) {
        int start = OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_START_TICKS;
        int peak = OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS;
        float f = Mth.clamp((float) (ticks - start) / (peak - start), 0f, 1f);
        return OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MIN
                + (OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MAX - OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MIN) * f;
    }

    private void dealWhirlwindDamage(float amount) {
        double r = OWAttacksConstants.Kangaroo.WHIRLWIND_RADIUS;
        AABB area = this.getBoundingBox().inflate(r, 1.0, r);
        UUID owner = this.getOwnerUUID();

        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (target == this) return false;
            if (this.getPassengers().contains(target)) return false;
            if (isAlliedTo(target)) return false;
            if (owner != null) {
                if (target.getUUID().equals(owner)) return false;
                if (target instanceof TamableAnimal ta && owner.equals(ta.getOwnerUUID())) return false;
            }
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double distSq = dx * dx + dz * dz;
            double reach = r + target.getBbWidth() / 2.0;
            if (distSq > reach * reach) return false;
            if (distSq > 1.0e-4) {
                double dot = (dx * fx + dz * fz) / Math.sqrt(distSq);
                if (dot < OWAttacksConstants.Kangaroo.WHIRLWIND_FRONT_DOT) return false;
            }
            return true;
        });

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().mobAttack(this), amount);
        }
    }

    private float computeAnimSpeedMultiplier(int ticks) {
        float f = Mth.clamp((float) ticks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
        f = f * f;
        return 1f + 4f * f;
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }
    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    public int getTelluricStompPhase() { return this.entityData.get(TELLURIC_STOMP_PHASE); }
    private void setTelluricStompPhase(int phase) { this.entityData.set(TELLURIC_STOMP_PHASE, phase); }
    public boolean isTelluricStomping() { return getTelluricStompPhase() != STOMP_PHASE_NONE; }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(serverLevel, entity);
    }

    private boolean isGroundedForStomp() {
        if (this.onGround()) return true;
        AABB box = this.getBoundingBox();
        AABB probe = new AABB(box.minX, box.minY - 0.5, box.minZ, box.maxX, box.minY, box.maxZ);
        return !this.level().noCollision(this, probe);
    }

    public boolean activateTelluricStomp() {
        if (this.level().isClientSide()) return false;
        if (isTelluricStomping()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED) return false;
        if (getVitalEnergy() > getVitalEnergyCapacity() - OWAttacksConstants.Kangaroo.TELLURIC_STOMP_ENERGY) {
            canShowVitalEnergyLack = true;
            return false;
        }

        setVitalEnergy(getVitalEnergy() + OWAttacksConstants.Kangaroo.TELLURIC_STOMP_ENERGY);
        setUltimateKillCount(0);

        telluricLeapImpulseApplied = false;
        this.fallDistance = 0f;
        this.hasImpulse = true;

        if (isGroundedForStomp()) {
            setTelluricStompPhase(STOMP_PHASE_WINDUP);
            telluricStompWindupTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_WINDUP_TICKS;
        } else {
            setTelluricStompPhase(STOMP_PHASE_HOVER);
            telluricStompHoverTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_TICKS;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.2f, 0.7f);
        return true;
    }

    private void cancelTelluricStomp() {
        setTelluricStompPhase(STOMP_PHASE_NONE);
        telluricStompWindupTimer = 0;
        telluricStompLeapTimer = 0;
        telluricStompLeapElapsed = 0;
        telluricStompHoverTimer = 0;
        telluricStompDiveTimer = 0;
        telluricStompDiveElapsed = 0;
        telluricLeapImpulseApplied = false;
        this.setNoGravity(false);
    }

    private void tickTelluricStomp() {
        if (this.level().isClientSide()) tickTelluricStompVisuals();

        if (!isTelluricStomping()) {
            telluricLeapImpulseApplied = false;
            telluricStompLeapElapsed = 0;
            telluricStompDiveElapsed = 0;
            return;
        }

        this.fallDistance = 0f;
        int phase = getTelluricStompPhase();

        if (phase == STOMP_PHASE_WINDUP) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.55, 1.0, 0.55));

            if (this.level().isClientSide()) return;

            if (telluricStompWindupTimer > 0) {
                telluricStompWindupTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_LEAP);
                telluricStompLeapTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS;
                telluricLeapImpulseApplied = false;
                spawnTelluricLaunchBurst();
            }
            return;
        }

        if (phase == STOMP_PHASE_LEAP) {
            this.setNoGravity(true);
            telluricStompLeapElapsed++;

            if (this.level().isClientSide()) return;

            if (telluricStompLeapTimer > 0) {
                telluricStompLeapTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_HOVER);
                telluricStompHoverTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_TICKS;
            }
            return;
        }

        if (phase == STOMP_PHASE_HOVER) {
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_DAMPING));
            this.hasImpulse = true;

            if (this.level().isClientSide()) return;

            if (telluricStompHoverTimer > 0) {
                telluricStompHoverTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_DIVE);
                telluricStompDiveTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_MAX_DIVE_TICKS;
            }
            return;
        }

        this.setNoGravity(true);
        telluricStompDiveElapsed++;
        LivingEntity rider = this.getControllingPassenger();
        float lookPitch = rider != null ? rider.getXRot() : this.getXRot();
        float lookYaw = rider != null ? rider.getYRot() : this.getYRot();
        float divePitch = Mth.clamp(lookPitch, OWAttacksConstants.Kangaroo.TELLURIC_STOMP_MIN_DIVE_PITCH, 90f);
        Vec3 dir = Vec3.directionFromRotation(divePitch, lookYaw);
        float ramp = Mth.clamp((float) telluricStompDiveElapsed / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DIVE_RAMP_TICKS, 0f, 1f);
        double speed = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DIVE_SPEED * (0.3 + 0.7 * (ramp * ramp));
        this.setDeltaMovement(dir.scale(speed));
        this.hasImpulse = true;

        if (this.level().isClientSide()) return;

        if (telluricStompDiveElapsed % 3 == 1) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL,
                    0.75f, 0.55f + 0.09f * Math.min(telluricStompDiveElapsed, 12));
        }

        if (telluricStompDiveTimer > 0) telluricStompDiveTimer--;

        if (this.onGround() || this.verticalCollision || this.horizontalCollision || telluricStompDiveTimer <= 0) {
            executeTelluricStompImpact();
            cancelTelluricStomp();
        }
    }

    private void tickTelluricStompVisuals() {
        int phase = getTelluricStompPhase();

        if (telluricStompOutroTicks > 0) telluricStompOutroTicks--;

        if (phase != telluricStompLastPhase) {
            if (phase == STOMP_PHASE_NONE && telluricStompLastPhase == STOMP_PHASE_DIVE) {
                telluricStompOutroTicks = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_OUTRO_TICKS;
            }
            telluricStompLastPhase = phase;
            telluricStompPhaseTicks = 0;
        } else if (phase != STOMP_PHASE_NONE) {
            telluricStompPhaseTicks++;
        }

        tickTelluricSpin(phase);
        spawnTelluricStompParticles(phase);
    }

    private void tickTelluricSpin(int phase) {
        switch (phase) {
            case STOMP_PHASE_LEAP  -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 2.0f, 14f);
            case STOMP_PHASE_HOVER -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 5.0f, 34f);
            case STOMP_PHASE_DIVE  -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 6.0f, 52f);
            case STOMP_PHASE_WINDUP -> telluricSpinSpeed = 0f;
            default -> telluricSpinSpeed *= 0.7f;
        }

        telluricSpinAngle += telluricSpinSpeed;

        if (phase == STOMP_PHASE_NONE) {
            if (telluricSpinSpeed < 0.5f) telluricSpinSpeed = 0f;
            float snap = Math.round(telluricSpinAngle / 360f) * 360f;
            telluricSpinAngle = Mth.lerp(0.3f, telluricSpinAngle, snap);
            if (telluricSpinSpeed == 0f && Math.abs(telluricSpinAngle - snap) < 0.5f) telluricSpinAngle = 0f;
        }
    }

    private void spawnTelluricStompParticles(int phase) {
        if (phase == STOMP_PHASE_WINDUP) {
            BlockState ground = this.level().getBlockState(this.blockPosition().below());
            if (ground.isAir()) return;
            BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
            for (int i = 0; i < 4; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 0.9 + this.random.nextDouble() * 0.8;
                this.level().addParticle(dust,
                        this.getX() + Math.cos(a) * r, this.getY() + 0.1, this.getZ() + Math.sin(a) * r,
                        Math.cos(a) * 0.12, 0.14, Math.sin(a) * 0.12);
            }
            return;
        }

        if (phase != STOMP_PHASE_HOVER && phase != STOMP_PHASE_DIVE) return;

        boolean diving = phase == STOMP_PHASE_DIVE;
        int arms = diving ? 4 : 3;
        double height = this.getBbHeight();
        for (int i = 0; i < arms; i++) {
            double a = Math.toRadians(telluricSpinAngle) + i * (Math.PI * 2.0 / arms);
            double r = (diving ? 1.1 : 1.5) + this.random.nextDouble() * 0.5;
            double px = this.getX() + Math.cos(a) * r;
            double pz = this.getZ() + Math.sin(a) * r;
            double py = this.getY() + this.random.nextDouble() * height;
            double vx = -Math.sin(a) * (diving ? 0.55 : 0.35);
            double vz = Math.cos(a) * (diving ? 0.55 : 0.35);
            this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, vx, diving ? 0.18 : 0.04, vz);
            if (diving && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIT, px, py, pz, vx * 0.5, 0.1, vz * 0.5);
            }
        }

        if (diving) {
            spawnTelluricSoleTrail();
            spawnTelluricImpactTelegraph();
        }
    }

    private void spawnTelluricSoleTrail() {
        double spread = this.getBbWidth() * 0.35;
        for (int i = 0; i < 5; i++) {
            double px = this.getX() + (this.random.nextDouble() - 0.5) * spread;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5) * spread;
            double py = this.getY() - 0.15 - this.random.nextDouble() * 0.5;
            this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0, -0.55, 0.0);
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIT, px, py, pz,
                        (this.random.nextDouble() - 0.5) * 0.2, -0.35, (this.random.nextDouble() - 0.5) * 0.2);
            }
        }
    }

    private void spawnTelluricImpactTelegraph() {
        Vec3 from = this.position();
        net.minecraft.world.phys.BlockHitResult hit = this.level().clip(new net.minecraft.world.level.ClipContext(
                from, from.add(0.0, -32.0, 0.0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

        double groundY = hit.getLocation().y + 0.08;
        double fall = Math.max(0.0, this.getY() - groundY);
        float closeness = (float) Mth.clamp(1.0 - fall / 12.0, 0.15, 1.0);
        double radius = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_RADIUS;

        int points = 6 + (int) (10 * closeness);
        double baseAngle = Math.toRadians(telluricSpinAngle * 0.5);
        for (int i = 0; i < points; i++) {
            double a = baseAngle + (Math.PI * 2.0 * i) / points;
            double px = this.getX() + Math.cos(a) * radius;
            double pz = this.getZ() + Math.sin(a) * radius;
            this.level().addParticle(ParticleTypes.CRIT, px, groundY, pz,
                    -Math.sin(a) * 0.18, 0.04 + 0.1 * closeness, Math.cos(a) * 0.18);
        }

        BlockState ground = this.level().getBlockState(net.minecraft.core.BlockPos.containing(
                hit.getLocation().x, hit.getLocation().y - 0.1, hit.getLocation().z));
        if (ground.isAir()) return;
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        int inner = 1 + (int) (3 * closeness);
        for (int i = 0; i < inner; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = radius * (0.15 + this.random.nextDouble() * 0.5);
            this.level().addParticle(dust,
                    this.getX() + Math.cos(a) * r, groundY, this.getZ() + Math.sin(a) * r,
                    0.0, 0.08 + 0.22 * closeness, 0.0);
        }
    }

    private void spawnTelluricLaunchBurst() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        BlockState ground = this.level().getBlockState(this.blockPosition().below());
        if (!ground.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    this.getX(), this.getY() + 0.1, this.getZ(), 120, 1.2, 0.1, 1.2, 0.45);
        }
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.1, this.getZ(), 40, 1.4, 0.05, 1.4, 0.2);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                this.getX(), this.getY() + 0.4, this.getZ(), 8, 1.2, 0.1, 1.2, 0.0);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.7f, 1.5f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ROOTED_DIRT_BREAK, SoundSource.NEUTRAL, 1.3f, 0.6f);
    }

    private void executeTelluricStompImpact() {
        double radius = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_RADIUS;
        AABB area = this.getBoundingBox().inflate(radius, 2.0, radius);
        UUID owner = this.getOwnerUUID();

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (target == this) return false;
            if (this.getPassengers().contains(target)) return false;
            if (isAlliedTo(target)) return false;
            if (owner != null) {
                if (target.getUUID().equals(owner)) return false;
                if (target instanceof TamableAnimal ta && owner.equals(ta.getOwnerUUID())) return false;
            }
            return this.distanceToSqr(target) <= radius * radius;
        });

        float baseDamage = this.getDamage();
        for (LivingEntity target : targets) {
            double dist = Math.sqrt(this.distanceToSqr(target));
            float t = (float) Mth.clamp(dist / radius, 0.0, 1.0);
            float mult = Mth.lerp(t,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_CENTER_MULT,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_EDGE_MULT);

            target.hurt(this.damageSources().mobAttack(this), baseDamage * mult);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_SLOWNESS_TICKS, 0));

            Vec3 outward = target.position().subtract(this.position());
            outward = outward.lengthSqr() > 1.0e-4 ? outward.multiply(1, 0, 1).normalize() : Vec3.ZERO;
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x * 0.3 + outward.x * 0.3, 0.6, motion.z * 0.3 + outward.z * 0.3);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }

        createMiniShockwave();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.2, this.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3, this.getZ(),
                    24, radius * 0.6, 0.15, radius * 0.6, 0.0);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 0.5, this.getZ(),
                    30, radius * 0.6, 0.2, radius * 0.6, 0.1);
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1, this.getZ(),
                    60, radius * 0.65, 0.05, radius * 0.65, 0.15);
            BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
            serverLevel.sendParticles(dirtParticle, this.getX(), this.getY() + 0.1, this.getZ(),
                    240, radius * 0.6, 0.3, radius * 0.6, 0.5);

            int ringPoints = 48;
            for (int i = 0; i < ringPoints; i++) {
                double angle = (Math.PI * 2.0 * i) / ringPoints;
                double rx = this.getX() + Math.cos(angle) * radius;
                double rz = this.getZ() + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.CLOUD, rx, this.getY() + 0.25, rz,
                        2, 0.1, 0.1, 0.1, 0.03);
                serverLevel.sendParticles(dirtParticle, rx, this.getY() + 0.1, rz,
                        3, 0.15, 0.1, 0.15, 0.12);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1.6f, 0.7f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.4f, 0.6f);
    }

    public void createMiniShockwave() {
        Vec3 look = this.getLookAngle();
        double x = this.getX() + look.x * 2.0;
        double z = this.getZ() + look.z * 2.0;
        BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particleOption, x, this.getY(), z, 60, 1.0, 0.1, 1.0, 0.25);
        } else {
            AABB area = new AABB(x - 1.0, this.getY() - 0.1, z - 1.0, x + 1.0, this.getY() + 0.2, z + 1.0);
            for (int i = 0; i < 60; i++) {
                double px = area.minX + Math.random() * (area.maxX - area.minX);
                double py = area.minY + Math.random() * (area.maxY - area.minY);
                double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
                this.level().addParticle(particleOption, px, py, pz, 0, 0.1, 0);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_HIT, SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    @Override
    protected void onSuccessfulHit(LivingEntity entity) {
        super.onSuccessfulHit(entity);

        if (this.isWearingBoxingGloves()) {
            int exp = (int) OWUtils.generateRandomInterval(1, 2);

            if (!entity.level().isClientSide()) {
                ExperienceOrb.award((ServerLevel) entity.level(), entity.position(), exp);
            }
        }
    }

    @Override
    protected int getDefaultSkinIndex() { return 2; }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof KangarooEntity otherKangaroo) {
            if (otherKangaroo.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherKangaroo.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherKangaroo.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherKangaroo.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {

    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseKangarooVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private KangarooVariant chooseKangarooVariant() {
        int roll = this.random.nextInt(100);
        if (roll < 33) return KangarooVariant.ORANGE;
        if (roll < 66) return KangarooVariant.BROWN;
        return KangarooVariant.DEFAULT;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        float dealt = (!this.level().isClientSide() && !this.isTame() && isPivoting())
                ? amount * PIVOT_DAMAGE_MULTIPLIER
                : amount;

        boolean hurt = super.hurt(damageSource, dealt);

        if (hurt && !this.level().isClientSide() && !this.isTame()
                && damageSource.getEntity() instanceof LivingEntity attacker) {

            if (isDrowningSomeone() && attacker == getDrownVictim()) {
                int next = getGrabTimeout() - DROWN_STRUGGLE_REDUCTION;
                if (next <= 0 && attacker instanceof Player) breakFreeFromDrown();
                else setGrabTimeout(next);
            }

            angerAt(attacker);
            propagateAngerToHerd(attacker);
        }
        return hurt;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == getDrownVictim()) {
            if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

            double yawRad = Math.toRadians(this.yBodyRot);
            double forward = DROWN_HOLD_FORWARD * this.getScale();
            double px = this.getX() - Math.sin(yawRad) * forward;
            double pz = this.getZ() + Math.cos(yawRad) * forward;
            double eyeY = Math.min(this.getY() + DROWN_HOLD_RISE, drownSurfaceY() - DROWN_SUBMERGE_MARGIN);
            double py = eyeY - passenger.getEyeHeight();

            passenger.fallDistance = 0f;
            function.accept(passenger, px, py, pz);
            return;
        }
        super.positionRider(passenger, function);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        LivingEntity victim = getDrownVictim();
        if (victim != null && this.getFirstPassenger() == victim) return null;
        return super.getControllingPassenger();
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            cancelDrownWindup();
            if (isDrowningSomeone()) releaseDrownVictim();
        }
        super.die(damageSource);
    }

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(80, true);
        setupThumpAnimation();
        setupNapAnimation();
        setupDrownAnimation();

        setupComboAnimations();
    }

    private void setupDrownAnimation() {
        if (isDrowningSomeone()) {
            if (!drownAnimationState.isStarted()) drownAnimationState.start(this.tickCount);
        } else {
            drownAnimationState.stop();
        }

        if (isDrownWindingUp()) {
            if (!drownWindupAnimationState.isStarted()) drownWindupAnimationState.start(this.tickCount);
        } else {
            drownWindupAnimationState.stop();
        }
    }

    private void setupNapAnimation() {
        if (this.isNapping() || this.isSleeping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 64;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        } else {
            this.napAnimationTimeout = 0;
            this.napAnimationState.stop();
        }
    }

    private void setupThumpAnimation() {
        if (isThumping()) {
            if (thumpAnimationTimeout <= 0) {
                thumpAnimationTimeout = KangarooThumpAlertGoal.THUMP_DURATION;
                thumpAnimationState.start(this.tickCount);
            } else thumpAnimationTimeout--;
        } else {
            thumpAnimationTimeout = 0;
            thumpAnimationState.stop();
        }
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (35 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (32 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (50 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        boolean shouldPlay = this.isCombo(comboNumber) && !(comboNumber == 3 && fourthHitFired);
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, shouldPlay);

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public KangarooVariant getVariant() {
        return KangarooVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(KangarooVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(KangarooVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(KangarooVariant.Cosmetics.GOLD.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof KangarooEntity kangaroo) {
            kangaroo.setVariant(KangarooVariant.byId(variant));
            kangaroo.setInitialVariant(KangarooVariant.byId(variant));
        }
    }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public KangarooVariant getInitialVariant() {
        return KangarooVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(KangarooVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public boolean isWearingBoxingGloves() {
        return this.entityData.get(IS_WEARING_BOXING_GLOVES);
    }

    public void setWearingBoxingGloves(boolean gloves) {
        this.entityData.set(IS_WEARING_BOXING_GLOVES, gloves);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putBoolean("isWearingBoxingGloves", this.isWearingBoxingGloves());
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_WEARING_BOXING_GLOVES, tag.getBoolean("isWearingBoxingGloves"));
        this.entityData.set(ULTIMATE_KILL_COUNT, tag.getInt("ultimateKillCount"));
        this.readPersistentAngerSaveData(this.level(), tag);
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");

        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }
}
