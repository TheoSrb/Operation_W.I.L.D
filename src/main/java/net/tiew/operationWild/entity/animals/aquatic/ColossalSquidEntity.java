package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.config.OWTargetLists;
import net.tiew.operationWild.entity.goals.colossal_squid.ColossalSquidWanderGoal;
import net.tiew.operationWild.entity.goals.colossal_squid.ColossalSquidWildGrabGoal;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.ColossalSquidVariant;
import net.tiew.operationWild.item.OWItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class ColossalSquidEntity extends OWWaterEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 500.0;
    public static final int MAX_SOMNOLENCE = 7000;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 3;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 3;
    public static final int FOOD_WANTED_MIN = 8;
    public static final int FOOD_WANTED_MAX = 12;
    public static final int ENTITY_COLOR = 0xCD6D5D;

    private static final float MAX_VITAL_ENERGY = 360f;

    public static final int GRAB_NONE = 0;
    public static final int GRAB_PREPARE = 1;
    public static final int GRAB_LUNGE = 2;
    public static final int GRAB_PULL = 3;
    public static final int GRAB_HOLD = 4;
    public static final int GRAB_RELEASE = 5;
    public static final int GRAB_MISS = 6;

    private static final int SEDATION_WAKE_TICKS = 40;
    private static final float VOICE_PITCH = 0.5f;
    private static final float SEAT_LOCAL_Y = -9f;
    private static final float SEAT_LOCAL_Z = 21f;
    private static final float SEAT_SINK = 0.2f;
    private static final float MODEL_ORIGIN_Y = 1.501f;
    private static final double REST_SEAT_HEIGHT = 1.125;
    private static final double REST_SEAT_FORWARD = 0.25;
    private static final int AIR_LOSS_OUT_OF_WATER = 2;
    private static final int AIR_GAIN_IN_WATER = 8;
    private static final int DRY_OUT_DAMAGE_INTERVAL = 20;
    private static final float DRY_OUT_DAMAGE = 2.0f;
    private static final double COMBO_WIDTH = 4.5;
    private static final double COMBO_HEIGHT = 4.0;
    private static final double COMBO_REACH = 7.0;
    private static final double WILD_COMBAT_RANGE = 10.0;
    private static final float COMBAT_SLOW_TURNING = 1.0f;
    private static final float COMBAT_SLOW_FACING = 0.75f;
    private static final float COMBAT_SLOW_RESPONSE = 0.12f;
    private static final float COMBAT_SLOW_SPEED_LOSS = 0.35f;
    private static final float COMBAT_SLOW_ROTATION_LOSS = 0.7f;
    private static final float COMBAT_PITCH_RATE = 0.03f;
    private static final float BASE_ROTATION_SPEED = 0.0325f;
    private static final float FREE_YAW_CAP = 15f;
    private static final float WILD_MIN_TURN_STEP = OWEntity.MIN_TURN_STEP * 0.5f;
    private static final float COMBAT_YAW_CAP_FACING = 1.4f;
    private static final float COMBAT_YAW_CAP_TURNING = 0.6f;
    private static final float FREE_PITCH_CAP = 90f;
    private static final float COMBAT_PITCH_CAP = 0.9f;
    private static final float COMBAT_ROTATION_RESPONSE = 0.08f;

    private static final double SWIM_PERIOD_TICKS = 40.0;
    private static final float PULSE_START = 13f / 36f;
    private static final float PULSE_RISE = 6f / 36f;
    private static final float PULSE_DECAY = 12f / 36f;
    private static final float PULSE_TAIL = (float) Math.exp(-(1f - PULSE_RISE) / PULSE_DECAY);
    private static final float PULSE_GLIDE = 0.45f;
    private static final float PULSE_AMPLITUDE = 1.6075f;
    private static final float COMBAT_YAW_CAP_AIMING = 2.0f;
    private static final float JET_PHASE = 14f / 36f;
    private static final float WHIP_PHASE = 19f / 36f;
    private static final float STROKE_MIN_SWIM = 0.12f;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> COMBAT_FACING = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TURN_ID = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIP_ID = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_PHASE = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_FOCUS_ID = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_PULL_TICKS = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INK_ID = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INK_SPEED = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(ColossalSquidEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState eyesAnimationState = new AnimationState();
    public final AnimationState combatIdleAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState beachedAnimationState = new AnimationState();
    public final AnimationState turnToCombatAnimationState = new AnimationState();
    public final AnimationState turnToSwimAnimationState = new AnimationState();
    public final AnimationState grabPrepareAnimationState = new AnimationState();
    public final AnimationState grabLungeAnimationState = new AnimationState();
    public final AnimationState grabPullAnimationState = new AnimationState();
    public final AnimationState grabHoldAnimationState = new AnimationState();
    public final AnimationState grabReleaseAnimationState = new AnimationState();
    public final AnimationState grabMissAnimationState = new AnimationState();
    public final AnimationState inkAnimationState = new AnimationState();

    public int turnStartTick = -1000;
    public int inkStartTick = -1000;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimYRot = 0f;
    public volatile org.joml.Matrix4f seatMatrix = null;
    public volatile org.joml.Matrix4f riderMatrix = null;

    private int turnTicks = 0;
    private float combatSlow = 0f;
    private float riddenPitch = 0f;
    private float yawVelocity = 0f;
    private float pitchVelocity = 0f;
    private float riddenRotationSpeed = BASE_ROTATION_SPEED;
    private float lastSwimPhase = 0f;
    private int combatIdleTicks = 0;
    private boolean pendingCombo = false;
    private boolean pendingGrab = false;
    private boolean pendingInk = false;
    private int grabPhaseTicks = 0;
    private boolean grabAiming = false;
    private int grabAimTicks = 0;
    @Nullable
    private LivingEntity grabAimTarget = null;
    private int grabCooldown = 0;
    private int grabHoldTicks = 0;
    private Vec3 grabPullStart = null;
    private int inkTicks = 0;
    private int inkSpeedTicks = 0;
    private int dryOutTicks = 0;
    private int lastTurnId = 0;
    private int lastFlipId = -1;
    public int grabPhaseStartTick = -1000;
    private boolean visualCombatFacing = false;
    private float glideSpeed = 0f;
    private int glideTick = -1;
    private static final float GLIDE_BRAKE = 0.035f;
    private int beachedTicks = 0;
    private boolean beachedVisual = false;
    private float beachedBlend = 0f;
    private float beachedBlendO = 0f;
    private float beachedVelocity = 0f;
    private static final int BEACHED_SETTLE_TICKS = 6;
    private static final float BEACHED_SPRING = 0.05f;
    private static final float BEACHED_DAMPING = 0.44f;
    private static final float FLIP_LOOK_ANGLE = 75f;
    private int lastGrabPhase = GRAB_NONE;
    private int lastInkId = 0;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public ColossalSquidEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.moveControl = new OWSwimMoveControl(this);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new ColossalSquidWildGrabGoal(this));
        this.goalSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 11f, 20, 5, false));
        this.goalSelector.addGoal(4, new ColossalSquidWanderGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(COMBAT_FACING, false);
        builder.define(TURN_ID, 0);
        builder.define(FLIP_ID, 0);
        builder.define(GRAB_PHASE, GRAB_NONE);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(GRAB_FOCUS_ID, -1);
        builder.define(GRAB_PULL_TICKS, OWAttacksConstants.ColossalSquid.GRAB_PULL_TICKS);
        builder.define(GRAB_TIMEOUT, 0);
        builder.define(INK_ID, 0);
        builder.define(INK_SPEED, false);
        builder.define(ULTIMATE_KILL_COUNT, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // ==================================================
    //             MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 20;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public boolean usesAggressiveTaming() {
        return true;
    }

    @Override
    public float sedatedFleeSpeed() {
        return this.getSwimSpeed();
    }

    @Override
    public int blockBreakLevel() {
        return OWBreakLevel.STONE;
    }

    @Override
    public int asleepSomnolenceLossInterval() {
        return SOMNOLENCE_LOSS_INTERVAL_ASLEEP;
    }

    @Override
    public void onSedationKnockOut() {
        if (this.getGrabPhase() != GRAB_NONE) releaseGrab();
        cancelPendingActions();
        this.entityData.set(COMBAT_FACING, false);
        this.turnTicks = 0;
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
        return OWEntityConfig.Temperament.NEUTRAL;
    }

    @Override
    public List<Class<?>> getFavoriteTargets() {
        return OWTargetLists.COLOSSAL_SQUID;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.2f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 3f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.5f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return true;
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
        return OWAdvancements.COLOSSAL_SQUID_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return MAX_VITAL_ENERGY * (1f + this.getLevel() / 100f);
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.9f * (1 + ((float) this.getLevel() / 50));
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
    public boolean riderCameraFollowsBodyTilt() {
        return true;
    }

    @Override
    public void turnTowards(Vec3 direction, float factor) {
        if (direction.horizontalDistanceSqr() < 1.0E-6) return;
        float wanted = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
        float delta = Mth.wrapDegrees(wanted - this.getYRot());
        float step = delta * Mth.clamp(factor, 0f, 1f);
        float floor = Math.min(WILD_MIN_TURN_STEP, Math.abs(delta));
        if (Math.abs(step) < floor) step = Math.signum(delta) * floor;
        float yaw = this.getYRot() + step;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    public float getRotationSpeed() {
        if (this.getControllingPassenger() != null) return this.riddenRotationSpeed;
        return BASE_ROTATION_SPEED * (1f - COMBAT_SLOW_ROTATION_LOSS * this.combatSlow);
    }

    @Override
    protected float pitchMaxAngle() {
        return 89.0f;
    }

    @Override
    protected float bankMaxAngle() {
        return 40.0f;
    }

    @Override
    protected float bankReferenceYawRate(boolean ridden) {
        return ridden ? 9.0f : 4.5f;
    }

    @Override
    public org.joml.Matrix4f riderBoneMatrix() {
        return this.riderMatrix;
    }

    @Override
    public Vec2 getRiddenRotation(LivingEntity livingEntity) {
        return new Vec2(this.riddenPitch, livingEntity.getYRot());
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
        return currentAir;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 90 : 40;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 11 * (this.isInkSpeedActive() ? (float) (1 + OWAttacksConstants.ColossalSquid.INK_SPEED_BONUS) : 1f);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.COLOSSAL_SQUID.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.COLOSSAL_SQUID_FOOD);
    }

    @Override
    public Item favoriteFoodTier2() {
        return Items.COD;
    }

    @Override
    public Item favoriteFoodTier3() {
        return Items.SALMON;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        if (!RANDOM(3)) return null;
        return SoundEvents.SQUID_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.SQUID_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SQUID_HURT;
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    public void onPulseStroke() {
        if (!this.level().isClientSide() || !this.isInWater()) return;
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SQUID_SQUIRT,
                this.getSoundSource(), 0.35f, 0.55f + this.random.nextFloat() * 0.15f, false);
        spawnJetBubbles();
    }

    @Override
    protected double getBaseRiderYOffset() {
        return REST_SEAT_HEIGHT * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        tickCombatSlow();
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(16, 10, SoundEvents.PLAYER_ATTACK_SWEEP, COMBO_WIDTH, COMBO_HEIGHT, COMBO_REACH, false, 0.6f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) {
            setupAnimationState();
            tickSwimStrokes();
        }
        if (this.isInResurrection()) this.setSleeping(true);

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            tickEndlessCombo();
            tickFacing();
            tickGrab();
            tickInk();
        }
    }

    @Override
    public void aiStep() {
        mirrorRiddenDeltaMovement();
        super.aiStep();

        if (!this.isInWater()) {
            int air = this.getAirSupply() - AIR_LOSS_OUT_OF_WATER;
            if (air > 0) {
                this.setAirSupply(air);
                this.dryOutTicks = 0;
            } else {
                this.setAirSupply(0);
                if (++this.dryOutTicks >= DRY_OUT_DAMAGE_INTERVAL) {
                    this.dryOutTicks = 0;
                    this.hurt(this.damageSources().dryOut(), DRY_OUT_DAMAGE);
                }
            }
        } else {
            this.dryOutTicks = 0;
            if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(Math.min(this.getAirSupply() + AIR_GAIN_IN_WATER, this.getMaxAirSupply()));
            }
        }
    }

    private void mirrorRiddenDeltaMovement() {
        if (this.level().isClientSide()) return;
        if (!(this.getControllingPassenger() instanceof Player)) return;
        this.setDeltaMovement(this.getX() - this.xOld, this.getY() - this.yOld, this.getZ() - this.zOld);
    }

    public boolean isBeached() {
        return !this.isInWater();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (this.isBeached() || this.getGrabPhase() == GRAB_LUNGE) {
            this.resetRiddenSpeed();
            return 0f;
        }
        float speed = super.getRiddenSpeedVehicle(player) * combatSpeedFactor() * swimPulseFactor();
        if (this.isCombo() && player.zza <= 0f) speed = 0f;
        if (this.isInkSpeedActive()) speed *= (float) (1 + OWAttacksConstants.ColossalSquid.INK_SPEED_BONUS);
        return glide(player, speed);
    }

    /** Inertie : avance relâchée, la bête continue sur sa lancée au lieu de s'arrêter net. */
    private float glide(Player player, float speed) {
        if (this.glideTick == this.tickCount) return this.glideSpeed;
        this.glideTick = this.tickCount;
        boolean coasting = player.zza == 0f && speed >= 0f && speed < this.glideSpeed;
        this.glideSpeed = coasting ? this.glideSpeed + (speed - this.glideSpeed) * GLIDE_BRAKE : speed;
        if (Math.abs(this.glideSpeed) < 1.0e-4f) this.glideSpeed = 0f;
        return this.glideSpeed;
    }

    @Override
    public void travel(Vec3 travelVector) {
        super.travel(travelVector.scale(combatSpeedFactor() * swimPulseFactor()));
    }

    public float getSwimPhase(float partialTick) {
        double time = (this.level().getGameTime() % 4000L) + this.getId() * 7L + partialTick;
        return (float) ((time % SWIM_PERIOD_TICKS) / SWIM_PERIOD_TICKS);
    }

    private float swimPulseFactor() {
        if (!this.isInWater() || this.isCombatFacing() || isPivoting()) return 1f;
        float sinceStroke = this.getSwimPhase(0f) - PULSE_START;
        if (sinceStroke < 0f) sinceStroke += 1f;
        float rise = Mth.clamp(sinceStroke / PULSE_RISE, 0f, 1f);
        rise = rise * rise * (3f - 2f * rise);
        float decay = ((float) Math.exp(-Math.max(sinceStroke - PULSE_RISE, 0f) / PULSE_DECAY) - PULSE_TAIL) / (1f - PULSE_TAIL);
        return PULSE_GLIDE + PULSE_AMPLITUDE * rise * Math.max(decay, 0f);
    }

    private void tickSwimStrokes() {
        float phase = this.getSwimPhase(0f);
        float prev = this.lastSwimPhase;
        this.lastSwimPhase = phase;
        if (!this.isInWater() || this.isCombatFacing() || this.isTurning() || this.isSitting() || this.isNapping()) return;
        if (this.walkAnimation.speed() < STROKE_MIN_SWIM) return;
        if (crossed(prev, phase, JET_PHASE)) onPulseStroke();
        if (crossed(prev, phase, WHIP_PHASE)) spawnWhipBubbles();
    }

    private static boolean crossed(float prev, float cur, float mark) {
        if (prev <= cur) return prev < mark && cur >= mark;
        return mark <= cur || mark > prev;
    }

    private Vec3 bodyPoint(double x, double y, double z) {
        float pitch = this.getRidePitch(1f) * Mth.DEG_TO_RAD;
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return new Vec3(x, y, z).scale(this.getScale()).xRot(-pitch).yRot(-yaw);
    }

    private void spawnJetBubbles() {
        Vec3 back = bodyPoint(0, 0, -1).normalize();
        Vec3 siphon = this.position().add(bodyPoint(0, 0.45, -1.6));
        double spread = 0.35 * this.getScale();
        for (int i = 0; i < 22; i++) {
            double force = 2.5 + this.random.nextDouble() * 3.0;
            this.level().addParticle(ParticleTypes.BUBBLE,
                    siphon.x + (this.random.nextDouble() - 0.5) * spread,
                    siphon.y + (this.random.nextDouble() - 0.5) * spread,
                    siphon.z + (this.random.nextDouble() - 0.5) * spread,
                    back.x * force + (this.random.nextDouble() - 0.5) * 0.8,
                    back.y * force + (this.random.nextDouble() - 0.5) * 0.8,
                    back.z * force + (this.random.nextDouble() - 0.5) * 0.8);
        }
        for (int i = 0; i < 4; i++) {
            this.level().addParticle(ParticleTypes.BUBBLE_POP, siphon.x, siphon.y, siphon.z,
                    back.x * 0.15, back.y * 0.15, back.z * 0.15);
        }
    }

    private void spawnWhipBubbles() {
        Vec3 back = bodyPoint(0, 0, -1).normalize();
        for (int i = 0; i < 14; i++) {
            Vec3 tip = this.position().add(bodyPoint((this.random.nextDouble() - 0.5) * 2.6,
                    -0.4 + (this.random.nextDouble() - 0.5) * 1.2,
                    -(3.6 + this.random.nextDouble() * 1.6)));
            Vec3 side = bodyPoint((this.random.nextDouble() - 0.5) * 2.0, (this.random.nextDouble() - 0.5) * 2.0, 0);
            this.level().addParticle(ParticleTypes.BUBBLE, tip.x, tip.y, tip.z,
                    back.x * 1.2 + side.x, back.y * 1.2 + side.y, back.z * 1.2 + side.z);
        }
    }

    private float combatSpeedFactor() {
        LivingEntity rider = this.getControllingPassenger();
        if (isPivoting() && rider != null && rider.isSprinting()) return 1f;
        return 1f - COMBAT_SLOW_SPEED_LOSS * this.combatSlow;
    }

    private boolean isPivoting() {
        return this.level().isClientSide() ? this.isTurning() : this.turnTicks > 0;
    }

    private void tickCombatSlow() {
        boolean aiming = this.getGrabPhase() == GRAB_PREPARE;
        float target = isPivoting() || aiming ? COMBAT_SLOW_TURNING : this.isCombatFacing() ? COMBAT_SLOW_FACING : 0f;
        this.combatSlow += (target - this.combatSlow) * COMBAT_SLOW_RESPONSE;

        LivingEntity rider = this.getControllingPassenger();
        if (rider == null) {
            this.yawVelocity = 0f;
            this.pitchVelocity = 0f;
            return;
        }
        float response = Mth.lerp(this.combatSlow, 1f, COMBAT_ROTATION_RESPONSE);

        float rate = BASE_ROTATION_SPEED * (1f - COMBAT_SLOW_ROTATION_LOSS * this.combatSlow);
        float yawCap = this.combatSlow <= COMBAT_SLOW_FACING
                ? Mth.lerp(this.combatSlow / COMBAT_SLOW_FACING, FREE_YAW_CAP, COMBAT_YAW_CAP_FACING)
                : Mth.lerp((this.combatSlow - COMBAT_SLOW_FACING) / (COMBAT_SLOW_TURNING - COMBAT_SLOW_FACING), COMBAT_YAW_CAP_FACING, COMBAT_YAW_CAP_TURNING);
        if (aiming) yawCap = COMBAT_YAW_CAP_AIMING;
        float yawDelta = Mth.wrapDegrees(rider.getYRot() - this.getYRot());
        float yawWanted = Mth.clamp(yawDelta * rate, -yawCap, yawCap);
        this.yawVelocity += (yawWanted - this.yawVelocity) * response;
        this.riddenRotationSpeed = Math.abs(yawDelta) > 0.01f ? Mth.clamp(this.yawVelocity / yawDelta, 0f, 1f) : rate;

        float pitchRate = Mth.lerp(this.combatSlow, 1f, COMBAT_PITCH_RATE);
        float pitchCap = Mth.lerp(this.combatSlow, FREE_PITCH_CAP, COMBAT_PITCH_CAP);
        float pitchWanted = Mth.clamp((rider.getXRot() - this.riddenPitch) * pitchRate, -pitchCap, pitchCap);
        this.pitchVelocity += (pitchWanted - this.pitchVelocity) * response;
        this.riddenPitch += this.pitchVelocity;
    }

    @Override
    protected float comboTrackingDegreesPerTick() {
        return this.isVehicle() ? 360f : 3f;
    }

    @Override
    public boolean canStartCombo() {
        int phase = this.getGrabPhase();
        boolean grabBusy = phase != GRAB_NONE && phase != GRAB_HOLD;
        return super.canStartCombo() && !grabBusy && this.inkTicks <= 0 && !this.isBeached();
    }

    @Override
    public boolean canUseUltimate() {
        return super.canUseUltimate() && this.getGrabPhase() == GRAB_NONE && this.inkTicks <= 0;
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        if (isCombo && !this.level().isClientSide()) {
            if (!canStartCombo()) return;
            if (this.turnTicks > 0 && !this.isCombatFacing()) {
                this.pendingCombo = true;
                return;
            }
            if (!this.isCombatFacing()) requestFacing(true);
            float cost = OWAttacksConstants.ColossalSquid.COMBO_STRIKE_ENERGY;
            if (this.getVitalEnergy() > this.getMaxVitalEnergy() - cost) {
                this.canShowVitalEnergyLack = true;
                return;
            }
            this.setVitalEnergy(this.getVitalEnergy() + cost);
            this.combatIdleTicks = 0;
        }
        super.setCombo(isCombo, numberOfAttacks);
    }

    private void tickEndlessCombo() {
        if (!this.isPauseCombo() || !this.playerContinueCombo) return;
        if (this.actualAttackNumber < MAX_ATTACKS_IN_COMBO - 1) return;
        this.actualAttackNumber = 0;
        this.resetCombo(0);
        this.setCombo(true, 1);
    }

    private void requestFacing(boolean combat) {
        if (this.isCombatFacing() == combat || this.turnTicks > 0) return;
        if (combat && riderFacesTentacles()) {
            flipToCombat();
            return;
        }
        this.entityData.set(COMBAT_FACING, combat);
        this.entityData.set(TURN_ID, this.entityData.get(TURN_ID) + 1);
        this.turnTicks = combat ? OWAttacksConstants.ColossalSquid.TURN_TICKS : OWAttacksConstants.ColossalSquid.TURN_TO_SWIM_TICKS;
        this.combatIdleTicks = 0;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SQUID_SQUIRT,
                SoundSource.NEUTRAL, 0.8f, 0.5f);
    }

    private void tickFacing() {
        if (this.turnTicks > 0) {
            if (--this.turnTicks == 0) resolvePendingActions();
            return;
        }

        if (this.isSitting() || this.isNapping() || this.isSleeping() || this.isBeached()) {
            cancelPendingActions();
            if (this.isCombatFacing() && this.getGrabPhase() == GRAB_NONE) requestFacing(false);
            return;
        }

        if (!this.isCombatFacing()) return;

        boolean busy = this.isCombo() || this.getGrabPhase() != GRAB_NONE || this.inkTicks > 0;
        if (busy) {
            this.combatIdleTicks = 0;
            return;
        }

        LivingEntity rider = this.getControllingPassenger();
        boolean moving = rider != null && (rider.isSprinting() || rider.zza > 0f);
        LivingEntity target = this.getTarget();
        boolean targetClose = rider == null && target != null && target.isAlive() && this.distanceTo(target) <= WILD_COMBAT_RANGE;

        this.combatIdleTicks++;
        if ((moving && this.combatIdleTicks >= OWAttacksConstants.ColossalSquid.TURN_BACK_MOVE_GRACE_TICKS)
                || (!targetClose && this.combatIdleTicks >= OWAttacksConstants.ColossalSquid.TURN_BACK_IDLE_TICKS)) {
            requestFacing(false);
        }
    }

    private void resolvePendingActions() {
        if (!this.isCombatFacing() && this.grabAiming) {
            requestFacing(true);
            return;
        }
        if (!this.isCombatFacing()) {
            boolean combo = this.pendingCombo;
            cancelPendingActions();
            if (combo) this.setCombo(true, 1);
            return;
        }
        if (this.pendingInk) {
            this.pendingInk = false;
            startInkAnimation();
        } else if (this.pendingGrab) {
            this.pendingGrab = false;
            enterGrabPhase(GRAB_PREPARE);
        } else if (this.pendingCombo) {
            this.pendingCombo = false;
            this.setCombo(true, 1);
        }
        this.pendingCombo = false;
        this.pendingGrab = false;
    }

    private void cancelPendingActions() {
        this.pendingCombo = false;
        this.pendingGrab = false;
        this.pendingInk = false;
    }

    public boolean startAbyssalGrab() {
        if (this.level().isClientSide()) return false;
        int phase = this.getGrabPhase();
        if (phase == GRAB_HOLD) {
            releaseGrab();
            return true;
        }
        if (phase != GRAB_NONE || this.grabCooldown > 0 || this.inkTicks > 0 || this.isBeached()) return false;

        float cost = OWAttacksConstants.ColossalSquid.GRAB_ENERGY;
        if (this.getVitalEnergy() > this.getMaxVitalEnergy() - cost) {
            this.canShowVitalEnergyLack = true;
            return false;
        }
        this.setVitalEnergy(this.getVitalEnergy() + cost);
        this.grabCooldown = OWAttacksConstants.ColossalSquid.GRAB_COOLDOWN_TICKS;

        if (this.isCombo()) {
            this.resetCombo(0);
            this.actualAttackNumber = 0;
        }

        if (!this.isCombatFacing() || this.turnTicks > 0) {
            this.pendingGrab = true;
            requestFacing(true);
            return true;
        }
        enterGrabPhase(GRAB_PREPARE);
        return true;
    }

    public boolean beginGrabAim() {
        if (this.level().isClientSide()) return false;
        int phase = this.getGrabPhase();
        if (phase == GRAB_HOLD) {
            releaseGrab();
            return true;
        }
        if (phase != GRAB_NONE || this.grabCooldown > 0 || this.inkTicks > 0 || this.isBeached()) return false;
        if (this.getVitalEnergy() > this.getMaxVitalEnergy() - OWAttacksConstants.ColossalSquid.GRAB_ENERGY) {
            this.canShowVitalEnergyLack = true;
            return false;
        }
        if (this.isCombo()) {
            this.resetCombo(0);
            this.actualAttackNumber = 0;
        }
        this.pendingCombo = false;
        this.grabAiming = true;
        this.grabAimTicks = 0;
        this.grabAimTarget = null;
        if (!this.isCombatFacing() && this.turnTicks == 0) requestFacing(true);
        enterGrabPhase(GRAB_PREPARE);
        return true;
    }

    public void releaseGrabAim(int targetId) {
        if (this.level().isClientSide() || !this.grabAiming || this.getGrabPhase() != GRAB_PREPARE) return;
        this.grabAiming = false;

        float cost = OWAttacksConstants.ColossalSquid.GRAB_ENERGY;
        Entity aimed = targetId >= 0 ? this.level().getEntity(targetId) : null;
        double range = OWAttacksConstants.ColossalSquid.GRAB_RANGE * this.getScale() * OWAttacksConstants.ColossalSquid.GRAB_AIM_RANGE_SLACK;
        if (aimed instanceof LivingEntity prey && canGrab(prey) && this.distanceTo(prey) <= range
                && this.getVitalEnergy() <= this.getMaxVitalEnergy() - cost) {
            this.setVitalEnergy(this.getVitalEnergy() + cost);
            this.grabCooldown = OWAttacksConstants.ColossalSquid.GRAB_COOLDOWN_TICKS;
            this.grabAimTarget = prey;
        } else {
            this.grabAimTarget = null;
            this.grabCooldown = OWAttacksConstants.ColossalSquid.GRAB_WHIFF_COOLDOWN_TICKS;
        }
        enterGrabPhase(GRAB_LUNGE);
    }

    public boolean isGrabAiming() {
        return this.grabAiming;
    }

    private void enterGrabPhase(int phase) {
        this.entityData.set(GRAB_PHASE, phase);
        if (phase == GRAB_LUNGE) {
            LivingEntity focus = this.getControllingPassenger() != null ? this.grabAimTarget : this.getTarget();
            this.entityData.set(GRAB_FOCUS_ID, focus == null ? -1 : focus.getId());
        } else if (phase == GRAB_NONE || phase == GRAB_MISS || phase == GRAB_PREPARE) {
            this.entityData.set(GRAB_FOCUS_ID, -1);
        }
        this.grabPhaseTicks = 0;
        this.combatIdleTicks = 0;
        if (phase == GRAB_PREPARE) {
            this.getNavigation().stop();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SQUID_AMBIENT,
                    SoundSource.HOSTILE, 1.4f, 0.4f);
        }
        if (phase == GRAB_LUNGE) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.HOSTILE, 1.2f, 0.6f);
        }
    }

    private void tickGrab() {
        if (this.grabCooldown > 0) this.grabCooldown--;

        int phase = this.getGrabPhase();
        if (phase == GRAB_NONE) return;
        this.grabPhaseTicks++;

        switch (phase) {
            case GRAB_PREPARE -> {
                if (this.grabAiming) {
                    if (this.getControllingPassenger() == null || ++this.grabAimTicks > OWAttacksConstants.ColossalSquid.GRAB_AIM_MAX_TICKS) {
                        releaseGrabAim(-1);
                    }
                } else if (this.grabPhaseTicks >= OWAttacksConstants.ColossalSquid.GRAB_PREPARE_TICKS) {
                    enterGrabPhase(GRAB_LUNGE);
                }
            }
            case GRAB_LUNGE -> {
                if (this.grabPhaseTicks == OWAttacksConstants.ColossalSquid.GRAB_CATCH_TICK) tryCatchPrey();
                if (this.grabPhaseTicks >= OWAttacksConstants.ColossalSquid.GRAB_LUNGE_TICKS) {
                    enterGrabPhase(this.getGrabbedTarget() != null ? GRAB_PULL : GRAB_MISS);
                }
            }
            case GRAB_PULL -> {
                LivingEntity prey = this.getGrabbedTarget();
                if (!isPreyValid(prey)) {
                    releaseGrab();
                    return;
                }
                if (this.grabPhaseTicks >= this.getGrabPullTicks()) {
                    if (!prey.isPassenger()) prey.startRiding(this, true);
                    this.grabHoldTicks = 0;
                    if (prey instanceof Player) this.setGrabTimeout(OWAttacksConstants.ColossalSquid.GRAB_START_TIMEOUT);
                    enterGrabPhase(GRAB_HOLD);
                } else {
                    pullPrey(prey);
                }
            }
            case GRAB_HOLD -> tickHold();
            case GRAB_RELEASE -> {
                if (this.grabPhaseTicks >= OWAttacksConstants.ColossalSquid.GRAB_RELEASE_TICKS) enterGrabPhase(GRAB_NONE);
            }
            case GRAB_MISS -> {
                if (this.grabPhaseTicks >= OWAttacksConstants.ColossalSquid.GRAB_MISS_TICKS) enterGrabPhase(GRAB_NONE);
            }
            default -> enterGrabPhase(GRAB_NONE);
        }
    }

    private void tryCatchPrey() {
        double range = OWAttacksConstants.ColossalSquid.GRAB_RANGE * this.getScale();
        if (this.getControllingPassenger() != null) {
            LivingEntity aimed = this.grabAimTarget;
            this.grabAimTarget = null;
            if (aimed != null && isPreyValid(aimed) && canGrab(aimed)
                    && this.distanceTo(aimed) <= range * OWAttacksConstants.ColossalSquid.GRAB_AIM_RANGE_SLACK) {
                catchPrey(aimed);
            }
            return;
        }
        Vec3 look = this.grabDirection();
        Vec3 origin = this.position().add(0, this.getBbHeight() * 0.5, 0);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
            if (!canGrab(candidate)) continue;
            Vec3 toCandidate = candidate.position().add(0, candidate.getBbHeight() * 0.5, 0).subtract(origin);
            double distance = toCandidate.length();
            if (distance > range || distance < 1.0E-4) continue;
            if (toCandidate.scale(1 / distance).dot(look) < OWAttacksConstants.ColossalSquid.GRAB_CONE_DOT) continue;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        if (best != null) catchPrey(best);
    }

    private void catchPrey(LivingEntity prey) {
        this.setGrabbedTarget(prey);
        this.grabPullStart = prey.position();
        this.entityData.set(GRAB_FOCUS_ID, prey.getId());
        int pull = (int) Math.round(prey.distanceTo(this) * OWAttacksConstants.ColossalSquid.GRAB_PULL_TICKS_PER_BLOCK);
        this.entityData.set(GRAB_PULL_TICKS, Mth.clamp(pull, OWAttacksConstants.ColossalSquid.GRAB_PULL_TICKS,
                OWAttacksConstants.ColossalSquid.GRAB_PULL_MAX_TICKS));
        prey.hurt(this.damageSources().mobAttack(this), (float) this.getCombatDamage() * 0.25f);
        this.level().playSound(null, prey.getX(), prey.getY(), prey.getZ(), SoundEvents.SLIME_SQUISH,
                SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    public boolean canGrab(LivingEntity candidate) {
        if (candidate == this || !candidate.isAlive() || candidate.isSpectator()) return false;
        if (this.hasPassenger(candidate) || candidate.isPassenger()) return false;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (candidate instanceof IOWTamable tamable && tamable.getTheoreticalScale() >= this.getTheoreticalScale()) return false;
        if (this.isAlliedTo(candidate) || OWEntity.shareTribe(this, candidate)) return false;
        LivingEntity owner = this.getOwner();
        return owner == null || (candidate != owner && !OWEntity.shareTribe(owner, candidate));
    }

    private boolean isPreyValid(@Nullable LivingEntity prey) {
        return prey != null && prey.isAlive() && !prey.isRemoved() && prey.level() == this.level()
                && !(prey instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private void pullPrey(LivingEntity prey) {
        Vec3 start = this.grabPullStart != null ? this.grabPullStart : prey.position();
        float t = Mth.clamp((float) this.grabPhaseTicks / this.getGrabPullTicks(), 0f, 1f);
        float eased = t * t * (3f - 2f * t);
        Vec3 hold = this.holdPosition(prey);
        Vec3 pos = start.lerp(hold, eased);
        if (prey instanceof Player) {
            // Un joueur se déplace de son côté : une téléportation par tick le ferait saccader.
            // La vitesse est imposée et renvoyée au client, qui glisse jusqu'au point voulu.
            prey.setDeltaMovement(pos.subtract(prey.position()));
            prey.hurtMarked = true;
        } else {
            prey.teleportTo(pos.x, pos.y, pos.z);
            prey.setDeltaMovement(Vec3.ZERO);
        }
        prey.fallDistance = 0f;
        if (prey instanceof Mob mob) mob.setTarget(null);
    }

    private void tickHold() {
        LivingEntity prey = this.getGrabbedTarget();
        if (!isPreyValid(prey)) {
            releaseGrab();
            return;
        }
        if (!prey.isPassenger()) prey.startRiding(this, true);
        prey.fallDistance = 0f;
        if (prey instanceof Mob mob) mob.setTarget(null);

        this.grabHoldTicks++;
        if (prey instanceof Player) {
            if (this.getGrabTimeout() <= 0) {
                releaseGrab();
                return;
            }
            this.setGrabTimeout(this.getGrabTimeout() + 1);
            if (this.getGrabTimeout() >= this.getGrabMaxTimeout()) {
                this.setGrabTimeout(0);
                prey.invulnerableTime = 0;
                prey.hurt(this.damageSources().mobAttack(this), Float.MAX_VALUE);
                releaseGrab();
            }
        } else if (this.grabHoldTicks >= OWAttacksConstants.ColossalSquid.GRAB_ENTITY_RELEASE_TICKS) {
            releaseGrab();
        }
    }

    public void releaseGrab() {
        LivingEntity prey = this.getGrabbedTarget();
        if (prey != null && prey.getVehicle() == this) prey.stopRiding();
        this.setGrabbedTarget(null);
        this.setGrabTimeout(0);
        this.grabHoldTicks = 0;
        this.grabPullStart = null;
        int phase = this.getGrabPhase();
        if (phase == GRAB_HOLD || phase == GRAB_PULL) enterGrabPhase(GRAB_RELEASE);
        else if (phase != GRAB_NONE && phase != GRAB_RELEASE && phase != GRAB_MISS) enterGrabPhase(GRAB_NONE);
    }

    public void breakFreeFromGrab() {
        LivingEntity prey = this.getGrabbedTarget();
        releaseGrab();
        if (prey == null) return;
        Vec3 push = prey.position().subtract(this.position());
        push = push.lengthSqr() > 1.0E-4 ? push.normalize() : this.getLookAngle();
        prey.setDeltaMovement(push.scale(0.8));
        prey.hurtMarked = true;
        this.level().playSound(null, prey.getX(), prey.getY(), prey.getZ(), SoundEvents.SQUID_SQUIRT,
                SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    public Vec3 grabDirection() {
        LivingEntity rider = this.getControllingPassenger();
        float pitch = rider != null ? rider.getXRot() : this.getXRot();
        float yaw = rider != null ? rider.getYRot() : this.getYRot();
        return Vec3.directionFromRotation(pitch, yaw).normalize();
    }

    public Vec3 holdPosition(Entity prey) {
        double forward = OWAttacksConstants.ColossalSquid.GRAB_HOLD_FORWARD * this.getScale();
        Vec3 dir = Vec3.directionFromRotation(this.getXRot(), this.yBodyRot).normalize();
        double centerY = this.getY() + (OWAttacksConstants.ColossalSquid.GRAB_HOLD_HEIGHT + 0.55) * this.getScale();
        return new Vec3(this.getX() + dir.x * forward, centerY + dir.y * forward - prey.getBbHeight() * 0.5, this.getZ() + dir.z * forward);
    }

    @Override
    public void attackEntitiesInFront(float attackDamage, SoundEvent sound, double width, double height, double reach, float knockbackMultiplier) {
        if (this.getGrabPhase() != GRAB_HOLD) {
            super.attackEntitiesInFront(attackDamage, sound, width, height, reach, knockbackMultiplier);
            return;
        }
        LivingEntity prey = this.getGrabbedTarget();
        if (!isPreyValid(prey)) return;

        float heal = Math.min(prey.getHealth() * OWAttacksConstants.ColossalSquid.GRAB_HEAL_TARGET_RATIO,
                this.getMaxHealth() * OWAttacksConstants.ColossalSquid.GRAB_HEAL_SELF_CAP_RATIO);
        prey.invulnerableTime = 0;
        if (prey.hurt(this.damageSources().mobAttack(this), attackDamage)) {
            this.heal(heal);
            this.hurtAfterCombo(prey, this.getComboAttack());
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, prey.getX(), prey.getY(0.6), prey.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(1.0), this.getZ(), 1, 0.6, 0.3, 0.6, 0.0);
            }
        }
        this.level().playSound(null, prey.getX(), prey.getY(), prey.getZ(), SoundEvents.SLIME_SQUISH,
                SoundSource.HOSTILE, 1.2f, 0.45f + this.random.nextFloat() * 0.2f);
    }

    public boolean activateInkFrenzy() {
        if (this.level().isClientSide()) return false;
        if (this.getUltimateKillCount() < OWAttacksConstants.ColossalSquid.INK_KILLS_REQUIRED) return false;
        if (!canUseUltimate()) return false;

        float cost = OWAttacksConstants.ColossalSquid.INK_ENERGY;
        if (this.getVitalEnergy() > this.getMaxVitalEnergy() - cost) {
            this.canShowVitalEnergyLack = true;
            return false;
        }
        this.setVitalEnergy(0);
        this.setUltimateKillCount(0);

        if (!this.isCombatFacing() || this.turnTicks > 0) {
            this.pendingInk = true;
            requestFacing(true);
            return true;
        }
        startInkAnimation();
        return true;
    }

    private void startInkAnimation() {
        this.inkTicks = OWAttacksConstants.ColossalSquid.INK_ANIMATION_TICKS;
        this.entityData.set(INK_ID, this.entityData.get(INK_ID) + 1);
        this.getNavigation().stop();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.HOSTILE, 1.2f, 0.45f);
    }

    private void tickInk() {
        if (this.inkSpeedTicks > 0 && --this.inkSpeedTicks == 0) this.entityData.set(INK_SPEED, false);
        if (this.inkTicks <= 0) return;

        int elapsed = OWAttacksConstants.ColossalSquid.INK_ANIMATION_TICKS - this.inkTicks;
        if (elapsed == OWAttacksConstants.ColossalSquid.INK_BURST_TICK) releaseInkCloud();

        if (--this.inkTicks == 0) {
            this.entityData.set(COMBAT_FACING, false);
            this.turnTicks = 0;
            this.inkSpeedTicks = OWAttacksConstants.ColossalSquid.INK_SPEED_TICKS;
            this.entityData.set(INK_SPEED, true);
        }
    }

    private void releaseInkCloud() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double radius = OWAttacksConstants.ColossalSquid.INK_RADIUS;
        Vec3 center = this.position().add(0, this.getBbHeight() * 0.5, 0);

        serverLevel.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 450, radius * 0.3, radius * 0.2, radius * 0.3, 0.15);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 120, radius * 0.25, radius * 0.15, radius * 0.25, 0.02);
        this.level().playSound(null, center.x, center.y, center.z, SoundEvents.SQUID_SQUIRT, SoundSource.HOSTILE, 2.5f, 0.35f);

        LivingEntity owner = this.getOwner();
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
            if (victim == this || this.hasPassenger(victim) || victim.distanceToSqr(center) > radius * radius) continue;
            if (this.isAlliedTo(victim) || OWEntity.shareTribe(this, victim)) continue;
            if (owner != null && (victim == owner || OWEntity.shareTribe(owner, victim))) continue;
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, OWAttacksConstants.ColossalSquid.INK_BLIND_TICKS, 0), this);
            victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, OWAttacksConstants.ColossalSquid.INK_BLIND_TICKS, 0), this);
            if (victim instanceof Mob mob && mob.getTarget() == this) mob.setTarget(null);
        }
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.ColossalSquid.INK_KILLS_REQUIRED) setUltimateKillCount(kills + 1);
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.getGrabPhase() != GRAB_NONE) releaseGrab();
        super.die(damageSource);
        if (this.isSaddled()) this.spawnAtLocation(acceptSaddle());
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) return;
        super.setTarget(target);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.AQUATIC.bit();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        LivingEntity prey = this.getGrabbedTarget();
        if (prey != null && this.getFirstPassenger() == prey) return null;
        return super.getControllingPassenger();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (passenger == this.getGrabbedTarget()) return true;
        return this.getPassengers().stream().noneMatch(p -> p != this.getGrabbedTarget());
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity controlling = this.getControllingPassenger();
        if (controlling == null) return super.isControlledByLocalInstance();
        return this.getPassengers().indexOf(controlling) == 0 && super.isControlledByLocalInstance();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        if (passenger == this.getGrabbedTarget()) {
            Vec3 hold = this.holdPosition(passenger);
            passenger.fallDistance = 0f;
            function.accept(passenger, hold.x, hold.y, hold.z);
            return;
        }

        float s = this.getScale();
        Vec3 local;
        org.joml.Matrix4f matrix = this.seatMatrix;
        if (matrix != null) {
            org.joml.Vector3f seat = matrix.transformPosition(new org.joml.Vector3f(0f, SEAT_LOCAL_Y / 16f, SEAT_LOCAL_Z / 16f));
            local = new Vec3(-seat.x, MODEL_ORIGIN_Y - seat.y - SEAT_SINK, seat.z).scale(s);
        } else {
            local = new Vec3(0, REST_SEAT_HEIGHT - SEAT_SINK, -REST_SEAT_FORWARD).scale(s);
        }
        Vec3 offset = local.yRot((float) Math.toRadians(180f - this.yBodyRot));

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z);
        if (passenger instanceof LivingEntity living) living.yBodyRot = this.yBodyRot;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(ColossalSquidVariant.DEFAULT);
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);

        if (this.isBaby()) {
            maxHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();
            maxMaturation = (int) (2000 * maxHealth + 10000 * this.getDamage());
            this.setHealth(1);
            foodWanted = (int) this.getMaxHealth();
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public boolean hasSedationAnimations() {
        return true;
    }

    @Override
    public int sedationWakeTicks() {
        return SEDATION_WAKE_TICKS;
    }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(4, 0f, -20f, 0.4f),
            new OWSedationImpact(19, 3.5f, -10f, 1.0f)
    );

    @Override
    public List<OWSedationImpact> sedationImpacts() {
        return SEDATION_IMPACTS;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        this.eyesAnimationState.startIfStopped(this.tickCount);
        createIdleAnimation(true);
        createSitAnimation(true);
        this.combatIdleAnimationState.startIfStopped(this.tickCount);

        if (this.isNapping()) this.napAnimationState.startIfStopped(this.tickCount);
        else this.napAnimationState.stop();

        tickBeachedBlend();
        if (this.beachedVisual || this.beachedBlend > 0f) this.beachedAnimationState.startIfStopped(this.tickCount);
        else this.beachedAnimationState.stop();

        int flipId = this.entityData.get(FLIP_ID);
        if (this.lastFlipId == -1) this.lastFlipId = flipId;
        if (flipId != this.lastFlipId) {
            this.lastFlipId = flipId;
            applyYawFlip();
        }

        startTurnIfNeeded();
        if (!this.isTurning() && this.visualCombatFacing != this.isCombatFacing()) {
            this.visualCombatFacing = this.isCombatFacing();
        }
        if (this.tickCount - this.turnStartTick > getTurnDuration(this.isCombatFacing()) + 1) {
            this.turnToCombatAnimationState.stop();
            this.turnToSwimAnimationState.stop();
        }

        int phase = this.getGrabPhase();
        if (phase != this.lastGrabPhase) {
            this.lastGrabPhase = phase;
            this.grabPhaseStartTick = this.tickCount;
            this.grabPrepareAnimationState.stop();
            this.grabLungeAnimationState.stop();
            this.grabPullAnimationState.stop();
            this.grabReleaseAnimationState.stop();
            this.grabMissAnimationState.stop();
            if (phase != GRAB_HOLD) this.grabHoldAnimationState.stop();
            switch (phase) {
                case GRAB_PREPARE -> this.grabPrepareAnimationState.start(this.tickCount);
                case GRAB_LUNGE -> this.grabLungeAnimationState.start(this.tickCount);
                case GRAB_PULL -> this.grabPullAnimationState.start(this.tickCount);
                case GRAB_HOLD -> this.grabHoldAnimationState.startIfStopped(this.tickCount);
                case GRAB_RELEASE -> this.grabReleaseAnimationState.start(this.tickCount);
                case GRAB_MISS -> this.grabMissAnimationState.start(this.tickCount);
                default -> { }
            }
        }

        int inkId = this.entityData.get(INK_ID);
        if (inkId != this.lastInkId) {
            this.lastInkId = inkId;
            if (this.tickCount > 5) {
                this.inkStartTick = this.tickCount;
                this.inkAnimationState.start(this.tickCount);
            }
        }
        if (this.tickCount - this.inkStartTick > OWAttacksConstants.ColossalSquid.INK_ANIMATION_TICKS + 1) {
            this.inkAnimationState.stop();
        }

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        boolean holding = this.getGrabPhase() == GRAB_HOLD;
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) ((holding ? 20 : 27) / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) ((holding ? 20 : 27) / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) ((holding ? 20 : 29) / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1 -> attack1ComboTimer = timer;
            case 2 -> attack2ComboTimer = timer;
            case 3 -> attack3ComboTimer = timer;
        }
    }

    /** Caméra du cavalier déjà tournée vers les tentacules : inutile de pivoter pour frapper. */
    private boolean riderFacesTentacles() {
        LivingEntity rider = this.getControllingPassenger();
        if (rider == null) return false;
        return Math.abs(Mth.wrapDegrees(rider.getYRot() - (this.yBodyRot + 180f))) < FLIP_LOOK_ANGLE;
    }

    /**
     * Passage en face combat sans pivot : le lacet fait un demi-tour en même temps que les clés
     * d'animation, si bien que les tentacules restent où elles sont à l'écran. Le corps est décalé
     * pour que la selle ne bouge pas.
     */
    private void flipToCombat() {
        this.entityData.set(COMBAT_FACING, true);
        this.entityData.set(FLIP_ID, this.entityData.get(FLIP_ID) + 1);
        this.combatIdleTicks = 0;
        applyYawFlip();
    }

    private void applyYawFlip() {
        float s = this.getScale();
        Vec3 seat = new Vec3(0, 0, -REST_SEAT_FORWARD).scale(s).yRot((float) Math.toRadians(180f - this.yBodyRot));
        this.setYRot(this.getYRot() + 180f);
        this.yRotO += 180f;
        this.yBodyRot += 180f;
        this.yBodyRotO += 180f;
        this.yHeadRot += 180f;
        this.yHeadRotO += 180f;
        this.yawVelocity = 0f;
        if (!this.level().isClientSide() || this.isControlledByLocalInstance()) {
            this.setPos(this.getX() + seat.x * 2, this.getY(), this.getZ() + seat.z * 2);
        }
    }

    private void tickBeachedBlend() {
        this.beachedTicks = Mth.clamp(this.beachedTicks + (this.isBeached() ? 1 : -1), 0, BEACHED_SETTLE_TICKS);
        if (this.beachedTicks >= BEACHED_SETTLE_TICKS) this.beachedVisual = true;
        else if (this.beachedTicks == 0) this.beachedVisual = false;
        this.beachedBlendO = this.beachedBlend;
        this.beachedVelocity += ((this.beachedVisual ? 1f : 0f) - this.beachedBlend) * BEACHED_SPRING - this.beachedVelocity * BEACHED_DAMPING;
        this.beachedBlend += this.beachedVelocity;
        if (this.beachedBlend <= 0f || this.beachedBlend >= 1f) {
            this.beachedBlend = Mth.clamp(this.beachedBlend, 0f, 1f);
            this.beachedVelocity = 0f;
        }
    }

    public float getBeachedBlend(float partialTick) {
        float t = Mth.lerp(partialTick, this.beachedBlendO, this.beachedBlend);
        return t * t * (3f - 2f * t);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (TURN_ID.equals(key) && this.level().isClientSide()) startTurnIfNeeded();
    }

    /**
     * Lancé dès la réception du paquet, et non au tick suivant : sinon la face synchronisée basculait
     * avant que l'animation de pivot ne démarre, et la bête apparaissait déjà retournée une fraction
     * de seconde.
     */
    private void startTurnIfNeeded() {
        int turnId = this.entityData.get(TURN_ID);
        if (turnId == this.lastTurnId) return;
        this.lastTurnId = turnId;
        if (this.tickCount > 5 && this.tickCount - this.inkStartTick > OWAttacksConstants.ColossalSquid.INK_ANIMATION_TICKS + 2) {
            this.turnStartTick = this.tickCount;
            if (this.isCombatFacing()) {
                this.turnToSwimAnimationState.stop();
                this.turnToCombatAnimationState.start(this.tickCount);
            } else {
                this.turnToCombatAnimationState.stop();
                this.turnToSwimAnimationState.start(this.tickCount);
            }
        }
        this.visualCombatFacing = this.isCombatFacing();
    }

    /** Face affichée : ne bascule côté client qu'avec le départ du pivot (ou une bascule silencieuse). */
    public boolean isCombatFacingVisual() {
        return this.level().isClientSide() ? this.visualCombatFacing : this.isCombatFacing();
    }

    public int getGrabPullTicks() {
        return Math.max(1, this.entityData.get(GRAB_PULL_TICKS));
    }

    /** Créature que les tentacules visent : la proie désignée pendant la détente, puis la proie prise. */
    @Nullable
    public LivingEntity getGrabFocus() {
        int id = this.entityData.get(GRAB_FOCUS_ID);
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    /** Poids du guidage des tentacules vers la proie, selon la phase de saisie en cours. */
    public float getGrabGuideWeight(float ageInTicks) {
        float elapsed = ageInTicks - this.grabPhaseStartTick;
        return switch (this.getGrabPhase()) {
            case GRAB_LUNGE -> Mth.clamp(elapsed / 3f, 0f, 1f);
            case GRAB_PULL, GRAB_HOLD -> 1f;
            case GRAB_RELEASE, GRAB_MISS -> 1f - Mth.clamp(elapsed / 5f, 0f, 1f);
            default -> 0f;
        };
    }

    public static int getTurnDuration(boolean toCombat) {
        return toCombat ? OWAttacksConstants.ColossalSquid.TURN_TICKS : OWAttacksConstants.ColossalSquid.TURN_TO_SWIM_TICKS;
    }

    @Override
    protected boolean forceRiderLookBodyRotation() {
        return this.getGrabPhase() == GRAB_PREPARE;
    }

    public boolean isTurning() {
        return this.turnToCombatAnimationState.isStarted() || this.turnToSwimAnimationState.isStarted();
    }

    public boolean isInkAnimating() {
        return this.inkAnimationState.isStarted();
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof ColossalSquidEntity squid) {
            squid.setVariant(ColossalSquidVariant.byId(variant));
            squid.setInitialVariant(ColossalSquidVariant.byId(variant));
        }
    }

    public ColossalSquidVariant getVariant() { return ColossalSquidVariant.byId(this.getTypeVariant() & 255); }
    public void setVariant(ColossalSquidVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255); }
    public void setSkin(ColossalSquidVariant skin) { this.setVariant(skin); }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
    }

    @Override
    public void changeSkinSilent(int skinIndex) { changeSkin(skinIndex, false); }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public ColossalSquidVariant getInitialVariant() { return ColossalSquidVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT)); }
    public void setInitialVariant(ColossalSquidVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId()); }

    public boolean isCombatFacing() { return this.entityData.get(COMBAT_FACING); }
    public int getGrabPhase() { return this.entityData.get(GRAB_PHASE); }
    public boolean isGrabbing() { return this.getGrabPhase() == GRAB_HOLD && this.getGrabbedTarget() != null; }
    public int getGrabTimeout() { return this.entityData.get(GRAB_TIMEOUT); }
    public void setGrabTimeout(int timeout) { this.entityData.set(GRAB_TIMEOUT, timeout); }
    public int getGrabMaxTimeout() { return OWAttacksConstants.ColossalSquid.GRAB_MAX_TIMEOUT; }
    public boolean isInkSpeedActive() { return this.entityData.get(INK_SPEED); }
    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }
    public void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, count); }

    public @Nullable LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    public void setGrabbedTarget(@Nullable LivingEntity target) {
        this.entityData.set(GRABBED_TARGET_ID, target == null ? -1 : target.getId());
    }

    // ==================================================
    //               DONNÉES SAUVEGARDÉES
    // ==================================================

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) this.setUltimateKillCount(tag.getInt("ultimateKillCount"));

        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }

    @Override
    protected int getDefaultSkinIndex() {
        return 0;
    }
}
