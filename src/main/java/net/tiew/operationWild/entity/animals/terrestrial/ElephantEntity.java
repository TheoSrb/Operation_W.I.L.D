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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWPlacedBlocks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class ElephantEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 300.0;

    private static final int CALL_MIN_COOLDOWN = 700;
    private static final int CALL_MAX_COOLDOWN = 1400;
    private static final int CALL_DURATION = 90;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CALLING = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> SHOULDER_BASH_TIMER = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER_BASH_SIDE = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> EARTHQUAKE_TICK = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    private static final double SEAT_HEIGHT = 0.78;
    private static final double SEAT_FORWARD = 0.10;
    private static final double SECOND_SEAT_BACK = 0.70;

    private static final int NO_TRIBE_WOOL_0 = 0xF9FFFE;
    private static final int NO_TRIBE_WOOL_1 = 0x9D9D97;

    private static final double WALK_CYCLE_MS = 3411.5;
    public static final long RIGHT_FOOT_CONTACT_MS = 1200L;
    public static final long LEFT_FOOT_CONTACT_MS = 2900L;

    public static final float WALK_ANIM_SPEED = 6.0f * 1.2f;
    public static final float RUN_ANIM_SPEED = 7.8f * 0.85f;

    private static final Vec3 FRONT_LEFT_FOOT  = new Vec3( 6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 FRONT_RIGHT_FOOT = new Vec3(-6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 BACK_LEFT_FOOT   = new Vec3( 8 / 16.0, 0, -16 / 16.0);
    private static final Vec3 BACK_RIGHT_FOOT  = new Vec3(-8 / 16.0, 0, -16 / 16.0);

    private static final double FOOTFALL_RADIUS = 15.0;
    private static final double FOOTFALL_HOP = 0.8;

    private static final int FOOTFALL_PARTICLES = 22;
    private static final double FOOTFALL_SPREAD = 0.22;

    private static final int FOOTFALL_MIN_INTERVAL = 3;


    private static final double CHARGE_SPEED = 0.32;

    private static final int FOOTSTEP_SOUND_REPEATS = 5;

    private static final float FULL_CHARGE = 100f;

    private static final float ROTATION_SPEED_STILL = 0.05f;
    private static final float ROTATION_SPEED_CHARGED = 0.025f;

    private static final float CHARGE_HEAD_MAX_PITCH = 40f;
    private static final float CHARGE_HEAD_RESPONSE = 0.12f;

    private static final double COMBO_SWEEP_KNOCKBACK = 1.15;
    private static final double COMBO_SWEEP_LIFT = 0.32;
    private static final double COMBO_SLAM_LAUNCH = 0.85;

    private static final float RIDDEN_ACCEL_RESPONSE = 0.30f;
    private static final float RIDDEN_BRAKE_RESPONSE = 0.12f;
    private static final float RIDDEN_BRAKE_RESPONSE_CHARGED = 0.035f;
    private static final float CHARGE_MEMORY_DECAY = 0.94f;

    private static final float CHARGE_DAMAGE = 3.5f;
    private static final float CHARGE_KNOCKBACK = 1.4f;
    private static final int CHARGE_HIT_INTERVAL = 20;

    private static final float CHARGE_LOG_BONUS_CHANCE = 0.33f;

    private static final int SHOCKWAVE_HIT_INTERVAL = 8;

    private static final int MAX_ACTIVE_SHOCKWAVES = 8;

    private static final float EARTHQUAKE_SPREAD_CHANCE = 0.45f;
    private static final int EARTHQUAKE_MAX_SPREAD = 2;

    private static final Direction[] HORIZONTAL_SPREAD = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };


    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState earthquakeAnimationState = new AnimationState();

    public int callAnimationStartTime = 0;

    public volatile float bodyAnimY = 0f;

    private int shoulderBashCooldown = 0;
    private int callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

    private boolean earthquakeImpactDone = false;

    private Vec3 earthquakeEpicentre = Vec3.ZERO;

    private static final class Shockwave {
        private int tick = 1;
        private final Vec3 origin;
        private final Vec3 direction;
        private double y;
        private final Map<Integer, Integer> struck = new HashMap<>();

        private Shockwave(Vec3 origin, Vec3 direction, double y) {
            this.origin = origin;
            this.direction = direction;
            this.y = y;
        }
    }

    private final List<Shockwave> shockwaves = new ArrayList<>();

    private double walkAnimTimeMs = 0;
    private double walkAnimTimeMsPrev = 0;

    private double lastTickX = Double.NaN;
    private double lastTickZ = Double.NaN;
    private double travelledPerTick = 0;

    private float riddenSpeed = 0f;
    private float chargeMemory = 0f;

    private float chargeHeadPitch = 0f;
    private float chargeHeadPitchPrev = 0f;

    private int groundStrikeCooldown = 0;

    private final Set<Integer> ploughStruck = new HashSet<>();

    public ElephantEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.14D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ElephantMeleeAttackGoal());

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof ElephantEntity elephant && !elephant.isSleeping() && !elephant.isNapping()
                        && !elephant.isEarthquakeGesture()) {
                    super.tick();
                }
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_CALLING, false);
        builder.define(SHOULDER_BASH_TIMER, 0);
        builder.define(SHOULDER_BASH_SIDE, 1);
        builder.define(EARTHQUAKE_TICK, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
    }

    @Override
    public int getEntityColor() {
        return 0x776a5e;
    }

    @Override
    public float getTheoreticalScale() {
        return 17f;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.TANK;
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
        return 4.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 4f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return true;
    }

    @Override
    public float sprintAccelerationMultiplier() {
        return 0.65f;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return false;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.ELEPHANT_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.ELEPHANT_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 280 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.75f * (1 + ((float) this.getLevel() / 50));
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
        return true;
    }
    
    @Override
    protected boolean canLean() {
        return !this.isSitting() && !this.isSleeping() && !this.isNapping() && !this.isEarthquakeGesture();
    }

    @Override
    protected float bankMaxAngle() {
        return 30f;
    }

    @Override
    protected float pitchMaxAngle() {
        return 0f;
    }

    @Override
    public float getRotationSpeed() {
        return Mth.lerp(getChargeRamp(), ROTATION_SPEED_STILL, ROTATION_SPEED_CHARGED);
    }

    @Override
    public boolean keepsAccelerationDuringCombo() {
        if (isSlammingCombo()) return false;
        return getChargeRamp() >= 1f;
    }

    public float getChargeRamp() {
        if (!this.isVehicle()) return 0f;
        return Mth.clamp(this.getAcceleration() / FULL_CHARGE, 0f, 1f);
    }

    private void tickChargeHeadPitch() {
        chargeHeadPitchPrev = chargeHeadPitch;
        chargeHeadPitch += (getChargeRamp() * CHARGE_HEAD_MAX_PITCH - chargeHeadPitch) * CHARGE_HEAD_RESPONSE;
    }

    public float getChargeHeadPitch(float partialTick) {
        return Mth.lerp(partialTick, chargeHeadPitchPrev, chargeHeadPitch);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.ELEPHANT.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.ELEPHANT_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!isFooted()) return;
        if (this.isInWater()) return;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        this.level().playLocalSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                soundtype.getStepSound(),
                this.getSoundSource(),
                soundtype.getVolume() * 1.5F,
                soundtype.getPitch() * pitchMod,
                false
        );

        for (int i = 0; i < FOOTSTEP_SOUND_REPEATS; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    OWSounds.ELEPHANT_FOOTSTEP.get(),
                    this.getSoundSource(),
                    0.9F,
                    pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.55f);
        spawnFootDust(FRONT_LEFT_FOOT);
        spawnFootDust(BACK_RIGHT_FOOT);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(0.62f);
        spawnFootDust(FRONT_RIGHT_FOOT);
        spawnFootDust(BACK_LEFT_FOOT);
    }

    private void spawnFootDust(Vec3 localOffset) {
        if (!this.level().isClientSide()) return;

        Vec3 foot = this.position().add(
                localOffset.scale(this.getScale()).yRot((float) -Math.toRadians(this.yBodyRot)));

        BlockState ground = this.level().getBlockState(
                BlockPos.containing(foot.x, foot.y - 0.2, foot.z));
        if (ground.isAir()) return;

        boolean charging = isChargingForward();
        int count = charging ? FOOTFALL_PARTICLES * 2 : FOOTFALL_PARTICLES;
        double lift = charging ? 0.16 : 0.08;

        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, ground);

        for (int i = 0; i < count; i++) {
            this.level().addParticle(dirt,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    foot.y + 0.05,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    (this.random.nextDouble() - 0.5) * 0.08,
                    lift * (0.5 + this.random.nextDouble()),
                    (this.random.nextDouble() - 0.5) * 0.08);
        }

        if (!charging) return;

        for (int i = 0; i < count / 2; i++) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    foot.y + 0.1,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.04,
                    (this.random.nextDouble() - 0.5) * 0.03);
        }
    }

    public float walkAnimationSpeed() {
        return (this.isRunning() || this.getState() == 2) ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
    }

    public double movementAmount() {
        return Math.max(travelledPerTick, this.walkAnimation.speed() / 4.0);
    }

    private void tickTravelled() {
        double x = this.getX();
        double z = this.getZ();

        if (!Double.isNaN(lastTickX)) {
            double dx = x - lastTickX;
            double dz = z - lastTickZ;
            travelledPerTick = Math.min(Math.sqrt(dx * dx + dz * dz), 1.0);
        }

        lastTickX = x;
        lastTickZ = z;
    }

    private void tickWalkAnimTime() {
        walkAnimTimeMsPrev = walkAnimTimeMs;

        double stride = Math.min(movementAmount() * 4.0, 1.0);
        walkAnimTimeMs = (walkAnimTimeMs + stride * 50.0 * walkAnimationSpeed()) % WALK_CYCLE_MS;
    }

    public double getWalkAnimTimeMs(float partialTick) {
        double current = walkAnimTimeMs;
        if (current < walkAnimTimeMsPrev) current += WALK_CYCLE_MS;
        return (walkAnimTimeMsPrev + (current - walkAnimTimeMsPrev) * partialTick) % WALK_CYCLE_MS;
    }

    public static boolean walkCycleCrossed(double previousTimeMs, double timeMs, long triggerTimeMs) {
        if (previousTimeMs <= timeMs) return previousTimeMs < triggerTimeMs && timeMs >= triggerTimeMs;
        return triggerTimeMs <= timeMs || triggerTimeMs > previousTimeMs;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        if (RANDOM(4)) return OWSounds.ELEPHANT_IDLE.get();
        if (RANDOM(3)) return OWSounds.ELEPHANT_IDLE_2.get();
        if (RANDOM(2)) return OWSounds.ELEPHANT_IDLE_3.get();
        return OWSounds.ELEPHANT_IDLE_4.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.ELEPHANT_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.ELEPHANT_HURTING.get() : OWSounds.ELEPHANT_HURTING_2.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (shoulderBashCooldown > 0) shoulderBashCooldown--;

        createCombo(28, 20, OWSounds.ELEPHANT_HURTING.get(), 4.0, 4.0, 2.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 3 : 1);
        setTamingPercentage(this.foodGiven, this.foodWanted);
        tickTravelled();
        tickWalkAnimTime();

        if (this.level().isClientSide()) {
            setupAnimationState();
            tickChargeHeadPitch();
            tickFootstepEffects();
        }
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isShoulderBashing() || this.isEarthquakeGesture());
        }

        if (this.isEarthquakeGesture()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }

        if (!this.level().isClientSide()) {
            tickShoulderBash();
            tickEarthquake();
            tickShockwave();
            tickCharge();
            tickGroundStrikes();
            handleCall();
        }
    }

    private void tickFootstepEffects() {
        if (this.isBaby()) return;
        if (!isFooted() || this.isInWater()) return;
        if (this.isSleeping() || this.isNapping() || this.isSitting() || this.isEarthquakeGesture()) return;
        if (movementAmount() < 0.0125) return;

        if (walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, RIGHT_FOOT_CONTACT_MS)) onRightFootDown();
        if (walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, LEFT_FOOT_CONTACT_MS)) onLeftFootDown();
    }

    private void tickGroundStrikes() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (groundStrikeCooldown > 0) groundStrikeCooldown--;

        if (this.isBaby()) return;
        if (!isFooted() || this.isInWater()) return;
        if (this.isSleeping() || this.isNapping() || this.isSitting() || this.isEarthquakeGesture()) return;

        if (movementAmount() < 0.0125) return;

        if (groundStrikeCooldown > 0) return;

        if (walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, RIGHT_FOOT_CONTACT_MS)
                || walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, LEFT_FOOT_CONTACT_MS)) {
            groundStrikeCooldown = FOOTFALL_MIN_INTERVAL;
            strikeGround(serverLevel);
        }
    }

    private boolean isFooted() {
        if (this.onGround()) return true;

        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        return !this.level().getBlockState(below).getCollisionShape(this.level(), below).isEmpty();
    }

    public boolean isChargingForward() {
        if (!this.isVehicle() || !this.isTame() || this.isBaby()) return false;
        return this.isRunning() || measuredSpeed() >= CHARGE_SPEED;
    }

    public boolean isFullyCharged() {
        if (!this.isTame() || this.isBaby()) return false;
        return getChargeRamp() >= 1f;
    }

    public double measuredSpeed() {
        return travelledPerTick;
    }

    private void tickCharge() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!isFullyCharged()) return;

        plough(serverLevel);
    }

    private void plough(ServerLevel serverLevel) {
        if (this.tickCount % CHARGE_HIT_INTERVAL == 0) ploughStruck.clear();

        Vec3 ahead = this.position().add(
                Vec3.directionFromRotation(0f, this.yBodyRot).scale(this.getBbWidth() * 0.7));
        AABB front = new AABB(ahead, ahead).inflate(this.getBbWidth() * 0.6, this.getBbHeight() * 0.5, this.getBbWidth() * 0.6)
                .move(0, this.getBbHeight() * 0.5, 0);

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos.betweenClosedStream(front).forEach(pos -> {
                BlockState state = serverLevel.getBlockState(pos);
                if (!state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS) && !state.is(BlockTags.SAPLINGS)) return;
                if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return;

                boolean log = state.is(BlockTags.LOGS);

                serverLevel.destroyBlock(pos.immutable(), true, this);
                OWPlacedBlocks.get(serverLevel).forget(pos);

                if (log && this.random.nextFloat() < CHARGE_LOG_BONUS_CHANCE) {
                    Block.popResource(serverLevel, pos, new ItemStack(state.getBlock()));
                }
            });
        }

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, front)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (!ploughStruck.add(target.getId())) continue;

            target.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE);
            target.knockback(CHARGE_KNOCKBACK, this.getX() - target.getX(), this.getZ() - target.getZ());
            target.hurtMarked = true;
        }
    }


    private void strikeGround(ServerLevel serverLevel) {
        AABB box = this.getBoundingBox().inflate(FOOTFALL_RADIUS, FOOTFALL_RADIUS * 0.5, FOOTFALL_RADIUS);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canBeShakenByFootfall)) {
            double distance = target.distanceTo(this);
            if (distance > FOOTFALL_RADIUS) continue;

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            double nearness = 1.0 - distance / FOOTFALL_RADIUS;
            double hop = FOOTFALL_HOP * nearness * footing;
            if (hop < 0.01) continue;

            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x, Math.max(motion.y, hop), motion.z);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    private boolean canBeShakenByFootfall(LivingEntity target) {
        if (target == this || target.getRootVehicle() == this) return false;
        if (target instanceof ElephantEntity) return false;
        if (target instanceof OWWaterEntity || target instanceof WaterAnimal) return false;

        MobCategory category = target.getType().getCategory();
        if (category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE) return false;

        if (target.isInWater() || target.isInLava()) return false;

        return target.onGround() || target.verticalCollision;
    }

    private void tickShoulderBash() {
        int timer = this.entityData.get(SHOULDER_BASH_TIMER);
        if (timer <= 0) return;

        if (timer == OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS - 1) {
            applyShoulderBashDamage();
        }

        this.entityData.set(SHOULDER_BASH_TIMER, timer - 1);
    }

    private void tickEarthquake() {
        int tick = this.entityData.get(EARTHQUAKE_TICK);
        if (tick <= 0) return;

        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;

        if (tick == windup && !earthquakeImpactDone) {
            earthquakeImpactDone = true;
            executeEarthquakeImpact();
        }

        int elapsed = tick - windup;

        if (elapsed > 0 && elapsed % OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL == 0
                && this.level() instanceof ServerLevel serverLevel) {
            pulseEarthquake(serverLevel);
        }

        int total = Math.max(OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS,
                windup + OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS);

        if (tick >= total) {
            cancelEarthquake();
            return;
        }

        this.entityData.set(EARTHQUAKE_TICK, tick + 1);
    }

    private void handleCall() {
        if (this.isCalling() && this.tickCount - callAnimationStartTime > CALL_DURATION) {
            this.setCalling(false);
        }

        if (callCooldown > 0) {
            callCooldown--;
            return;
        }

        callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

        if (!canPlayIdleAnimation()) return;

        this.setCalling(true);
        this.callAnimationStartTime = this.tickCount;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL,
                2.5f, isBaby() ? 1.4f : (float) OWUtils.generateRandomInterval(0.9, 1.05));
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected boolean isImmobile() {
        return this.isEarthquakeGesture() || isSlammingCombo();
    }

    public boolean isSlammingCombo() {
        return this.isCombo() && this.getComboAttack() == 3;
    }

    @Override
    public boolean canStartCombo() {
        return !isEarthquakeGesture();
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        if (isCombo && isEarthquakeGesture()) return;
        super.setCombo(isCombo, numberOfAttacks);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        if (this.level().isClientSide() || entity == null) return;
        if (entity == this.getControllingPassenger()) return;
        if (entity instanceof Player player && player.isCreative()) return;

        if (comboAttack == 3) {
            entity.push(0, COMBO_SLAM_LAUNCH, 0);
            entity.hurtMarked = true;
            return;
        }

        Vec3 sweep = getBashDirection(comboAttack == 1 ? -1 : 1);
        entity.push(sweep.x * COMBO_SWEEP_KNOCKBACK, COMBO_SWEEP_LIFT, sweep.z * COMBO_SWEEP_KNOCKBACK);
        entity.hurtMarked = true;
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (this.isImmobile() || (this.isCombo() && player.zza == 0)) {
            riddenSpeed = 0f;
            chargeMemory = 0f;
            return 0f;
        }

        float target = super.getRiddenSpeedVehicle(player);

        if (this.isRunning()) chargeMemory = getChargeRamp();
        else chargeMemory *= CHARGE_MEMORY_DECAY;

        float response = Math.abs(target) >= Math.abs(riddenSpeed)
                ? RIDDEN_ACCEL_RESPONSE
                : Mth.lerp(chargeMemory, RIDDEN_BRAKE_RESPONSE, RIDDEN_BRAKE_RESPONSE_CHARGED);

        riddenSpeed += (target - riddenSpeed) * response;
        if (Math.abs(riddenSpeed) < 1.0e-4f) riddenSpeed = 0f;
        return riddenSpeed;
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * SEAT_HEIGHT * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        boolean backSeat = this.getPassengers().indexOf(passenger) > 0;
        double forward = (SEAT_FORWARD - (backSeat ? SECOND_SEAT_BACK : 0)) * this.getScale();

        Vec3 seatOffset = new Vec3(0, 0, forward)
                .yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);

        if (backSeat && passenger instanceof LivingEntity living) {
            float facing = Mth.wrapDegrees(this.yBodyRot + 180f);
            living.yBodyRot = facing;
            living.yBodyRotO = facing;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity controlling = this.getControllingPassenger();
        if (controlling == null) return super.isControlledByLocalInstance();
        return this.getPassengers().indexOf(controlling) == 0 && super.isControlledByLocalInstance();
    }

    @Override
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseElephantVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(12, 20);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private ElephantVariant chooseElephantVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 2) return ElephantVariant.PINK;
        if (roll < 30) return ElephantVariant.GREY;
        return ElephantVariant.DEFAULT;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (EARTHQUAKE_TICK.equals(accessor) && !isEarthquakeGesture() && this.level().isClientSide()) {
            earthquakeAnimationState.stop();
        }
    }

    public static int computeBashSide(float riderYaw, float bodyYaw) {
        return Mth.wrapDegrees(riderYaw - bodyYaw) >= 0 ? 1 : -1;
    }

    public Vec3 getBashDirection(int side) {
        return Vec3.directionFromRotation(0f, this.yBodyRot + 90f * side);
    }

    public void performShoulderBash() {
        if (this.level().isClientSide()) return;
        if (isShoulderBashing() || isEarthquakeGesture()) return;
        if (shoulderBashCooldown > 0) return;

        float cost = OWAttacksConstants.Elephant.SHOULDER_BASH_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            cancelShoulderBash();
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        LivingEntity rider = this.getControllingPassenger();
        int side = computeBashSide(rider != null ? rider.getYRot() : this.getYRot(), this.yBodyRot);

        this.entityData.set(SHOULDER_BASH_SIDE, side);
        this.entityData.set(SHOULDER_BASH_TIMER, OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS);
        shoulderBashCooldown = OWAttacksConstants.Elephant.SHOULDER_BASH_COOLDOWN_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL,
                2.0f, (float) OWUtils.generateRandomInterval(0.85, 1.0));
    }

    public void cancelShoulderBash() {
        if (this.level().isClientSide()) return;
        this.entityData.set(SHOULDER_BASH_TIMER, 0);
        this.entityData.set(SHOULDER_BASH_SIDE, 1);
    }

    private void applyShoulderBashDamage() {
        int side = getShoulderBashSide();
        Vec3 dir = getBashDirection(side);
        double radius = OWAttacksConstants.Elephant.SHOULDER_BASH_RADIUS;

        Vec3 center = this.position().add(dir.scale(radius * 0.5)).add(0, this.getBbHeight() * 0.5, 0);
        AABB box = new AABB(center, center).inflate(radius * 0.75, this.getBbHeight() * 0.6, radius * 0.75);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target == this.getControllingPassenger()) continue;
            if (this.isAlliedTo(target)) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.SHOULDER_BASH_DAMAGE);
            target.push(dir.x * OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK,
                    0.45,
                    dir.z * OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK);
            target.hurtMarked = true;
        }

        OWUtils.spawnServerParticles(this, ParticleTypes.CLOUD, dir.x * 2, 0.4, dir.z * 2, 14, 0.4);
    }

    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);

        if (this.level().isClientSide()) return;
        if (this.attackTimer != timeToHit) return;
        if (this.getComboAttack() != 3) return;

        startShockwave();
    }

    private void startShockwave() {
        Vec3 direction = Vec3.directionFromRotation(0f, this.yBodyRot);
        Vec3 origin = this.position().add(direction.scale(this.getBbWidth() * 0.55));

        if (shockwaves.size() >= MAX_ACTIVE_SHOCKWAVES) shockwaves.remove(0);
        shockwaves.add(new Shockwave(origin, direction, this.getY()));

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL,
                3.5f, (float) OWUtils.generateRandomInterval(0.5, 0.65));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL,
                3.0f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
    }

    private static double surfaceYNear(ServerLevel serverLevel, double x, double z, double fromY) {
        int base = Mth.floor(fromY);

        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(Mth.floor(x), base + dy, Mth.floor(z));
            if (serverLevel.getBlockState(pos).isAir()) continue;
            if (!serverLevel.isEmptyBlock(pos.above())) continue;
            return pos.getY() + 1.0;
        }
        return fromY;
    }

    private void tickShockwave() {
        if (shockwaves.isEmpty()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            shockwaves.clear();
            return;
        }

        double speed = OWAttacksConstants.Elephant.SHOCKWAVE_SPEED;

        Iterator<Shockwave> it = shockwaves.iterator();
        while (it.hasNext()) {
            Shockwave wave = it.next();
            double front = wave.tick * speed;

            if (front > OWAttacksConstants.Elephant.SHOCKWAVE_LENGTH) {
                it.remove();
                continue;
            }

            sweepShockwave(serverLevel, wave, front - speed, front);
            wave.tick++;
        }
    }

    private void sweepShockwave(ServerLevel serverLevel, Shockwave wave, double back, double front) {
        double half = OWAttacksConstants.Elephant.SHOCKWAVE_HALF_WIDTH;
        Vec3 side = new Vec3(-wave.direction.z, 0, wave.direction.x);

        Vec3 axis = wave.origin.add(wave.direction.scale(front));

        wave.y = surfaceYNear(serverLevel, axis.x, axis.z, wave.y);

        for (int lane = -1; lane <= 1; lane++) {
            Vec3 point = axis.add(side.scale(lane * half * 0.7));
            double laneY = surfaceYNear(serverLevel, point.x, point.z, wave.y);

            BlockState ground = serverLevel.getBlockState(
                    BlockPos.containing(point.x, laneY - 0.1, point.z));
            if (ground.isAir()) continue;

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    point.x, laneY + 0.3, point.z, 14, 0.3, 0.2, 0.3, 0.15);
        }

        Vec3 a = new Vec3(wave.origin.x, wave.y, wave.origin.z)
                .add(wave.direction.scale(back));
        Vec3 b = new Vec3(wave.origin.x, wave.y, wave.origin.z)
                .add(wave.direction.scale(front));
        AABB box = new AABB(a, b).inflate(half, 2.5, half);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;

            Vec3 relative = target.position().subtract(
                    new Vec3(wave.origin.x, wave.y, wave.origin.z));
            double along = relative.dot(wave.direction);
            if (along <= back || along > front) continue;
            if (Math.abs(relative.dot(side)) > half) continue;
            if (Math.abs(relative.y) > 3.0) continue;

            Integer lastHit = wave.struck.get(target.getId());
            if (lastHit != null && wave.tick - lastHit < SHOCKWAVE_HIT_INTERVAL) continue;
            wave.struck.put(target.getId(), wave.tick);

            target.invulnerableTime = 0;
            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.SHOCKWAVE_DAMAGE);

            target.push(wave.direction.x * OWAttacksConstants.Elephant.SHOCKWAVE_CARRY,
                    OWAttacksConstants.Elephant.SHOCKWAVE_LAUNCH,
                    wave.direction.z * OWAttacksConstants.Elephant.SHOCKWAVE_CARRY);
            target.hurtMarked = true;
        }
    }

    public void activateEarthquake() {
        if (this.level().isClientSide()) return;
        if (isEarthquaking()) return;
        if (getUltimateKillCount() < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) return;

        float cost = OWAttacksConstants.Elephant.EARTHQUAKE_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        cancelShoulderBash();
        resetCombo(0);
        actualAttackNumber = 0;

        earthquakeImpactDone = false;
        this.entityData.set(EARTHQUAKE_TICK, 1);
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL, 3.0f, 0.75f);
    }

    private void cancelEarthquake() {
        this.entityData.set(EARTHQUAKE_TICK, 0);
        earthquakeImpactDone = false;
    }

    private void executeEarthquakeImpact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        earthquakeEpicentre = this.position();

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 4.0f, 0.4f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 4.0f, 0.6f);
    }

    private void pulseEarthquake(ServerLevel serverLevel) {
        double radius = OWAttacksConstants.Elephant.EARTHQUAKE_RADIUS;

        serverLevel.playSound(null, earthquakeEpicentre.x, earthquakeEpicentre.y, earthquakeEpicentre.z,
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 2.5f,
                (float) OWUtils.generateRandomInterval(0.55, 0.75));

        collapseScatteredBlocks(serverLevel, radius);
        shakeEntitiesAround(serverLevel, radius);
    }

    private void collapseScatteredBlocks(ServerLevel serverLevel, double radius) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        BlockPos origin = BlockPos.containing(earthquakeEpicentre);

        for (int attempt = 0; attempt < OWAttacksConstants.Elephant.EARTHQUAKE_BLOCKS_PER_PULSE; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(this.random.nextDouble()) * radius;

            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);

            collapseSurfaceAt(serverLevel, x, z, origin.getY());
        }
    }

    private void collapseSurfaceAt(ServerLevel serverLevel, int x, int z, int aroundY) {
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(x, aroundY + dy, z);
            if (!serverLevel.getBlockState(pos).isAir() || dy == -3) {
                if (breakSurfaceBlock(serverLevel, pos)) spreadCollapse(serverLevel, pos);
                return;
            }
        }
    }

    private void spreadCollapse(ServerLevel serverLevel, BlockPos origin) {
        int spread = 0;

        int first = this.random.nextInt(HORIZONTAL_SPREAD.length);

        for (int step = 0; step < HORIZONTAL_SPREAD.length; step++) {
            Direction direction = HORIZONTAL_SPREAD[(first + step) % HORIZONTAL_SPREAD.length];

            if (spread >= EARTHQUAKE_MAX_SPREAD) return;
            if (this.random.nextFloat() > EARTHQUAKE_SPREAD_CHANCE) continue;

            BlockPos side = origin.relative(direction);
            if (breakSurfaceBlock(serverLevel, side)
                    || breakSurfaceBlock(serverLevel, side.above())
                    || breakSurfaceBlock(serverLevel, side.below())) {
                spread++;
            }
        }
    }

    private boolean breakSurfaceBlock(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.hasBlockEntity()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (!serverLevel.isEmptyBlock(pos.above())) return false;

        float hardness = state.getDestroySpeed(serverLevel, pos);
        if (hardness < 0 || hardness > 3.0f) return false;
        if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return false;

        serverLevel.destroyBlock(pos, false);
        OWPlacedBlocks.get(serverLevel).forget(pos);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                10, 0.3, 0.15, 0.3, 0.1);
        return true;
    }

    private void shakeEntitiesAround(ServerLevel serverLevel, double radius) {
        AABB box = new AABB(earthquakeEpicentre, earthquakeEpicentre).inflate(radius, radius * 0.5, radius);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (target.position().distanceTo(earthquakeEpicentre) > radius) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_DAMAGE);

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            target.push(0, OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_HOP * footing, 0);
            target.hurtMarked = true;
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(122, true);

        if (this.isCalling()) {
            callAnimationState.startIfStopped(this.tickCount);
        } else {
            callAnimationState.stop();
        }

        if (this.isEarthquakeGesture()) {
            earthquakeAnimationState.startIfStopped(this.tickCount);
        } else {
            earthquakeAnimationState.stop();
        }

        setupComboAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isEarthquaking();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (45 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (42 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (34 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof ElephantEntity elephant) {
            elephant.setVariant(ElephantVariant.byId(variant));
            elephant.setInitialVariant(ElephantVariant.byId(variant));
        }
    }

    public ElephantVariant getVariant() {
        return ElephantVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(ElephantVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(ElephantVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(ElephantVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(ElephantVariant.Cosmetics.DEMON.variant);
            case 3 -> this.setSkin(ElephantVariant.Cosmetics.ZOMBIE.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public ElephantVariant getInitialVariant() {
        return ElephantVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(ElephantVariant variant) {
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

    public void setCalling(boolean calling) { this.entityData.set(IS_CALLING, calling); }

    public boolean isCalling() { return this.entityData.get(IS_CALLING); }

    public int getShoulderBashSide() { return this.entityData.get(SHOULDER_BASH_SIDE); }

    public int getShoulderBashTimer() { return this.entityData.get(SHOULDER_BASH_TIMER); }

    public boolean isShoulderBashing() { return this.entityData.get(SHOULDER_BASH_TIMER) > 0; }

    public int getEarthquakeTick() { return this.entityData.get(EARTHQUAKE_TICK); }

    public boolean isEarthquaking() { return this.entityData.get(EARTHQUAKE_TICK) > 0; }

    public boolean isEarthquakeGesture() {
        int tick = getEarthquakeTick();
        return tick > 0 && tick <= OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS;
    }

    public float getEarthquakeShakeIntensity() {
        int tick = getEarthquakeTick();
        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;
        int elapsed = tick - windup;

        if (elapsed < 0 || elapsed > OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS) return 0f;

        int interval = OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL;
        float sincePulse = (float) (elapsed % interval) / interval;
        float floor = OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_FLOOR;

        return OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_INTENSITY
                * (floor + (1f - floor) * (1f - sincePulse));
    }

    public int getSaddleWoolColor(int layer) {
        if (this.currentTeam == null) return layer == 0 ? NO_TRIBE_WOOL_0 : NO_TRIBE_WOOL_1;
        return layer == 0 ? this.currentTeam.getTeamColor() : this.currentTeam.getTeamSecondaryColor();
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }

    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

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
        if (tag.contains("ultimateKillCount")) {
            setUltimateKillCount(tag.getInt("ultimateKillCount"));
        }
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return 7; }

    class ElephantMeleeAttackGoal extends MeleeAttackGoal {

        public ElephantMeleeAttackGoal() {
            super(ElephantEntity.this, 7, true);
        }

        @Override
        public void start() {
            super.start();
            ElephantEntity.this.setMad(true);
            ElephantEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            ElephantEntity.this.setMad(false);
            ElephantEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 4.5;
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
