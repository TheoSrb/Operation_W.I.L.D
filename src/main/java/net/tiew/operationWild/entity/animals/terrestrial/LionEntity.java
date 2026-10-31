package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWSaddleShield;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.LionVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;
import net.tiew.operationWild.entity.taming.OWSedationImpact;

public class LionEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 140.0;
    public static final int MAX_SOMNOLENCE = 1700;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 1;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 2;
    public static final int FOOD_WANTED_MIN = 8;
    public static final int FOOD_WANTED_MAX = 13;
    public static final int ENTITY_COLOR = 0xCFAA70;
    public static final int DEFAULT_SKIN_INDEX = 1;

    private static final float MAX_VITAL_ENERGY_MALE = 320f;
    private static final float MAX_VITAL_ENERGY_FEMALE = 280f;

    public static final double MALE_HEALTH = 32.0;
    public static final double MALE_DAMAGE = 7.0;
    public static final double MALE_SPEED = 0.20;
    public static final double FEMALE_HEALTH = 28.0;
    public static final double FEMALE_DAMAGE = 8.0;
    public static final double FEMALE_SPEED = 0.21;
    public static final int MALE_ONE_IN = 3;
    public static final int ALBINO_PERCENT = 3;
    public static final float FEMALE_SCALE = 0.95f;

    private static final ResourceLocation PACK_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_pack_health");
    private static final ResourceLocation PACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_pack_damage");
    private static final int TELEPORT_ATTEMPTS = 12;
    private static final double CALL_AIM_COSINE = 0.97;
    private static final int WAVE_PARTICLES = 40;
    private static final int CALLABLE_REFRESH_TICKS = 10;
    private static final float RUN_BLEND_RATE = 0.18f;
    private static final float SIT_BLEND_RATE = 0.1f;
    private static final float REST_BLEND_RATE = 0.07f;
    private static final float ROAR_BLEND_IN_RATE = 0.25f;
    private static final float ROAR_BLEND_OUT_RATE = 0.12f;
    private static final float EMPOWER_BLEND_RATE = 0.08f;
    private static final int EAR_FLICK_TICKS = 6;
    private static final int SEDATION_WAKE_TICKS = 48;

    public static final int LATCH_NONE = 0;
    public static final int LATCH_RUSH = 1;
    public static final int LATCH_LEAP = 2;
    public static final int LATCH_HOLD = 3;
    public static final int LATCH_RELEASE = 4;
    private static final int END_GRIP_LOST = 0;
    private static final int END_KNOCKED_OFF = 1;
    private static final int END_PREY_DOWN = 2;
    private static final int END_LET_GO = 3;
    private static final int END_PREY_LOST = 4;
    private static final int END_RIDER_GONE = 5;
    public static final int ZONE_THROAT = 0;
    public static final int ZONE_FLANK = 1;
    public static final int ZONE_RUMP = 2;
    private static final double CLING_PIVOT = 0.94;
    private static final double CLING_SURFACE_OFFSET = 0.6;
    private static final double CLING_HEIGHT_RATIO = 0.6;
    private static final double CLING_TILT = Math.toRadians(70.0);
    private static final double SEAT_BACK_OFFSET = 0.45;
    private static final double SEAT_DROP = 0.5;
    private static final float THROAT_ZONE_DEGREES = 50f;
    private static final float RUMP_ZONE_DEGREES = 125f;
    private static final int FALL_GRACE_TICKS = 40;
    private static final int SHAKE_SIGNAL_TICKS = 8;
    private static final int DISTRESS_REFRESH_TICKS = 10;
    private static final float LATCH_BLEND_RATE = 0.22f;
    private static final float LEAP_BLEND_RATE = 0.45f;
    private static final float RELEASE_BLEND_RATE = 0.3f;
    private static final float DISTRESS_BLEND_RATE = 0.2f;
    private static final float GUARD_BLEND_RATE = 0.1f;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROAR_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> CLAN_ID = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CLAN_COLOR = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLAN_SIZE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CALL_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARRIVAL_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RESPOND_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RALLY_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMPOWER_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCH_PHASE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCH_TARGET = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LATCH_ANGLE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LATCH_GRIP = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCH_SHAKE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BITE_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISTRESS_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GUARD_POSE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RELEASE_LEAP = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LEAP_FLIGHT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState roarAnimationState = new AnimationState();
    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState arrivalAnimationState = new AnimationState();
    public final AnimationState respondAnimationState = new AnimationState();
    public final AnimationState leapAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();
    public final AnimationState releaseAnimationState = new AnimationState();
    public final AnimationState distressAnimationState = new AnimationState();

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private int clanCheckCooldown = 0;
    private int packNearbyLionesses = 0;
    private int packLionessCount = -1;

    private record PendingArrival(int lionessId, int tick, int slot) {}

    private final List<PendingArrival> pendingArrivals = new ArrayList<>();
    private int callTargetId = -1;

    private int clientCallableClanmates = 0;
    private int lastRoarElapsed = -1;
    private int lastCallElapsed = -1;

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private float roarBlend, roarBlendO;
    private float empowerBlend, empowerBlendO;
    private int earFlickTicks = 0;
    private int earFlickCooldown = 120;
    private boolean earFlickLeft = false;

    private int latchPhaseSeen = LATCH_NONE;
    private int latchPhaseTicks = 0;
    private Vec3 leapStart = Vec3.ZERO;
    private float leapStartYaw = 0f;
    private boolean latchByRider = false;
    private double rushSpeed = 0.0;
    private int biteCooldown = 0;
    private int pendingBiteImpact = 0;
    private int shakeTimer = 0;
    private int fallGraceTicks = 0;
    private int clientLeapTargetId = -1;
    private int clientDistressResponders = 0;
    private int lastBiteSignal = 0;
    private int lastShakeSignal = 0;

    private int guardLeaderId = -1;
    private int guardTicks = 0;
    private int guardSlot = 0;
    private int guardSlotCount = 1;
    private float guardSlotOffset = 0f;
    private boolean guardWasSitting = false;
    private int guardRoarTicks = 0;
    private boolean guardRoar = false;
    private int guardedTicks = 0;
    private final Map<Integer, Integer> frightened = new HashMap<>();

    private float latchBlend, latchBlendO;
    private float leapBlend, leapBlendO;
    private float releaseBlend, releaseBlendO;
    private float distressBlend, distressBlendO;
    private float guardBlend, guardBlendO;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public LionEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.175D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new LionGuardGoal());
        this.goalSelector.addGoal(2, new LionMeleeAttackGoal());

        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true));

        this.goalSelector.addGoal(7, new LionessStayWithPrideGoal());

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.65D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(ROAR_TICK, 0);
        builder.define(CLAN_ID, Optional.empty());
        builder.define(CLAN_COLOR, -1);
        builder.define(CLAN_SIZE, 0);
        builder.define(CALL_TICK, 0);
        builder.define(ARRIVAL_TICK, 0);
        builder.define(RESPOND_TICK, 0);
        builder.define(RALLY_TICK, 0);
        builder.define(EMPOWER_TICK, 0);
        builder.define(LATCH_PHASE, LATCH_NONE);
        builder.define(LATCH_TARGET, -1);
        builder.define(LATCH_ANGLE, 0f);
        builder.define(LATCH_GRIP, 0);
        builder.define(LATCH_SHAKE, 0);
        builder.define(BITE_TICK, 0);
        builder.define(DISTRESS_TICK, 0);
        builder.define(GUARD_POSE, false);
        builder.define(RELEASE_LEAP, true);
        builder.define(LEAP_FLIGHT, OWAttacksConstants.Lion.LEAP_FLIGHT_MIN_TICKS);
    }

    // ==================================================
    //               MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    private static final double SADDLE_SHIELD_COVERAGE = 0.75;
    private static final List<OWSaddleShield> SADDLE_SHIELDS = List.of(
            OWSaddleShield.modelPanel("cube_r1", -5.75f, 7.5f, 3.5f, -1.5708f, -0.0835f, -1.5706f, -6.5f, -5.5f, -0.5f, 12f, 11f, 1f).withCoverage(SADDLE_SHIELD_COVERAGE),
            OWSaddleShield.modelPanel("cube_r2", 5.75f, 7.5f, 3.5f, -1.5708f, 0.0835f, 1.5706f, -5.5f, -5.5f, -0.5f, 12f, 11f, 1f).withCoverage(SADDLE_SHIELD_COVERAGE));

    @Override
    public List<OWSaddleShield> saddleShields() {
        return SADDLE_SHIELDS;
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
    public boolean hasSedationAnimations() {
        return true;
    }

    @Override
    public int sedationWakeTicks() {
        return SEDATION_WAKE_TICKS;
    }

    private static final java.util.Set<String> FEMALE_HIDDEN_HEAD_PARTS = java.util.Set.of("mane");

    @Override
    public java.util.Set<String> excludedHeadParts() {
        return this.isMale() ? java.util.Set.of() : FEMALE_HIDDEN_HEAD_PARTS;
    }

    @Override
    public boolean hasStandardHeadPose() {
        return !this.isRoaring() && !this.isCalling() && !this.isDistressing() && getLatchPhase() == LATCH_NONE;
    }

    @Override
    public boolean usesAggressiveTaming() {
        return true;
    }

    @Override
    public float sedatedFleeSpeed() {
        return this.isMale() ? 5.5f : 6.5f;
    }

    @Override
    public int blockBreakLevel() {
        return OWBreakLevel.WOOD;
    }

    @Override
    public int asleepSomnolenceLossInterval() {
        return SOMNOLENCE_LOSS_INTERVAL_ASLEEP;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return this.isMale() ? OWEntityConfig.Archetypes.MARAUDER : OWEntityConfig.Archetypes.ASSASSIN;
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
    public float vehicleRunSpeedMultiplier() {
        return this.isMale() ? 4.5f : 4.75f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return this.isMale() ? 2.2f : 2.4f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 3f;
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
        return OWAdvancements.LION_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return (this.isMale() ? MAX_VITAL_ENERGY_MALE : MAX_VITAL_ENERGY_FEMALE) * (1f + this.getLevel() / 100f);
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return (this.isMale() ? 0.95f : 1.2f) * (1 + ((float) this.getLevel() / 50));
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
        return false;
    }

    @Override
    public float getRotationSpeed() {
        if (isRoaring() || isDistressing() || getLatchPhase() != LATCH_NONE) return 0f;
        if (isCalling()) return 0.12f;
        return this.isMale() ? 0.14f : 0.18f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.LION.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.LION_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getSecondaryCooldownDuration() {
        return this.isMale() ? OWAttacksConstants.Lion.CLAN_CALL_COOLDOWN_TICKS : OWAttacksConstants.Lion.LEAP_COOLDOWN_TICKS;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return RANDOM(2) ? OWSounds.TIGER_IDLE.get() : RANDOM(2) ? OWSounds.TIGER_IDLE_2.get() : OWSounds.TIGER_IDLE_3.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.TIGER_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.TIGER_HURT.get();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isMale() ? 0.8f : 1.0f);
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
        if (now - lastStepSoundMs < 200L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 7; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.15F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(this.isMale() ? 0.85f : 0.95f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(this.isMale() ? 1.05f : 1.15f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(21, 12, OWSounds.TIGER_HURTING.get(), 3.0, 3.5, 1.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 2 : 0);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isRoaring() || this.isCalling() || getLatchPhase() != LATCH_NONE);
        }

        tickLatch();

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.level() instanceof ServerLevel serverLevel) {
            tickRoar(serverLevel);
            tickCall(serverLevel);
            tickDistress(serverLevel);
            tickGuard(serverLevel);
            tickFrightened(serverLevel);
            tickAutoPounce();
            tickGestureCounters();
            tickClan(serverLevel);
        } else {
            tickClientEffects();
        }
    }

    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);
        if (attackTimer != timeToHit || !(this.level() instanceof ServerLevel level)) return;

        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        double reach = 1.6 * this.getScale();
        double x = this.getX() + forward.x * reach;
        double y = this.getY() + this.getBbHeight() * 0.55;
        double z = this.getZ() + forward.z * reach;
        boolean pounce = this.getComboAttack() == 3;

        if (pounce) {
            level.sendParticles(ParticleTypes.CRIT, x, y, z, 16, 0.35, 0.3, 0.35, 0.35);
            level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0f, 0.7f);
        } else {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0, 0, 0, 0);
            level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.8f,
                    (float) OWUtils.generateRandomInterval(1.15, 1.35));
        }

        BlockState ground = level.getBlockState(BlockPos.containing(x, this.getY() - 0.2, z));
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                x, this.getY() + 0.05, z, pounce ? 18 : 8, 0.35, 0.02, 0.35, 0.08);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isSedatedFleeing() || isNapping())) return;
        if (target != null && isClanmate(target)) return;

        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }

        super.setTarget(target);
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        return isRoaring() || isCalling() || isDistressing() ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) {
            this.setSitting(false);
        }

        float received = amount;
        if (this.isMale() && packNearbyLionesses > 0) {
            received *= 1f - OWAttacksConstants.Lion.PACK_BONUS_PER_LIONESS * packNearbyLionesses;
        }

        boolean latched = isLatchedOn();
        if (latched && !this.level().isClientSide() && damageSource.getEntity() != null
                && damageSource.getEntity() == getLatchTarget() && !(damageSource.getEntity() instanceof OWEntity)
                && this.random.nextFloat() > OWAttacksConstants.Lion.ZONE_EXPOSURE[getLatchZone()]) {
            return false;
        }

        boolean hurt = super.hurt(damageSource, received);
        if (hurt && latched && !this.level().isClientSide()) onLatchedHurt(received);

        if (hurt && !this.level().isClientSide() && this.isMale() && damageSource.getEntity() instanceof LivingEntity attacker) {
            rallyClanAgainst(attacker);
        }
        return hurt;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        int required = Math.max(OWAttacksConstants.Lion.ROAR_KILLS_REQUIRED, OWAttacksConstants.Lion.DISTRESS_KILLS_REQUIRED);
        if (kills < required) setUltimateKillCount(kills + 1);

        if (target instanceof LionEntity rival) claimDefeatedClan(level, rival);
        return super.killedEntity(level, target);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.entityData.set(ROAR_TICK, 0);
            this.entityData.set(CALL_TICK, 0);
            this.entityData.set(DISTRESS_TICK, 0);
            this.pendingArrivals.clear();
            if (getLatchPhase() != LATCH_NONE) endLatch();
            if (isGuarding()) endGuard();
        }
        super.die(damageSource);

        if (this.level() instanceof ServerLevel serverLevel) {
            leaveClan(serverLevel);
            if (this.isSaddled()) this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && this.level() instanceof ServerLevel serverLevel) leaveClan(serverLevel);
        super.remove(reason);
    }

    @Override
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
            this.clanCheckCooldown = 0;
        }
    }

    @Override
    public void setTameForAbsentOwner(UUID ownerId) {
        super.setTameForAbsentOwner(ownerId);
        this.setMad(false);
        this.clanCheckCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isRoaring() || isCalling() || isDistressing();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isRoaring() || isCalling() || isDistressing();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isRoaring() && !isCalling() && getLatchPhase() == LATCH_NONE;
    }

    @Override
    public boolean canUseUltimate() {
        return super.canUseUltimate() && getLatchPhase() == LATCH_NONE;
    }

    @Override
    protected boolean isLeapingVehicle() {
        return getLatchPhase() != LATCH_NONE || super.isLeapingVehicle();
    }

    @Override
    public boolean isPushable() {
        return !isLatchedOn() && super.isPushable();
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (isLatchedOn()) return;
        super.knockback(strength, x, z);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource damageSource) {
        if (fallGraceTicks > 0 || getLatchPhase() != LATCH_NONE) return false;
        return super.causeFallDamage(distance, multiplier, damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LionEntity other) {
            if (other.isBaby() && !other.isTame() && !this.isTame()) return true;
            if (isClanmate(other)) return true;
        }
        return super.isAlliedTo(entity);
    }

    private void tickClan(ServerLevel level) {
        if (clanCheckCooldown-- > 0) return;
        clanCheckCooldown = OWAttacksConstants.Lion.CLAN_CHECK_INTERVAL;

        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(getClanId());

        if (clan != null && (!data.isMember(clan.id, this.getUUID()) || clan.tamed != this.isTame())) {
            data.leave(this.getUUID());
            clan = null;
        } else if (clan == null && getClanId() != null) {
            data.leave(this.getUUID());
        }

        if (clan == null && !this.isBaby()) {
            clan = this.isMale() ? data.found(this.getUUID(), this.isTame(), this.random) : findClanToJoin(level, data);
        }

        setClan(clan);
        if (this.isMale()) updatePackStrength(level, clan);
    }

    @Nullable
    private LionClanData.Clan findClanToJoin(ServerLevel level, LionClanData data) {
        double radius = OWAttacksConstants.Lion.CLAN_JOIN_RADIUS;
        List<LionEntity> leaders = level.getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(radius),
                lion -> lion != this && lion.isAlive() && lion.isMale() && !lion.isBaby()
                        && lion.isTame() == this.isTame() && lion.getClanId() != null
                        && (!this.isTame() || (this.getOwnerUUID() != null && this.getOwnerUUID().equals(lion.getOwnerUUID()))));
        leaders.sort(Comparator.comparingDouble(this::distanceToSqr));

        for (LionEntity leader : leaders) {
            LionClanData.Clan clan = data.getClan(leader.getClanId());
            if (clan == null || clan.tamed != this.isTame() || clan.isFull()) continue;
            if (data.join(clan.id, this.getUUID())) return clan;
        }
        return null;
    }

    private void leaveClan(ServerLevel level) {
        LionClanData.get(level.getServer()).leave(this.getUUID());
        setClan(null);
    }

    private void setClan(@Nullable LionClanData.Clan clan) {
        Optional<UUID> id = clan == null ? Optional.empty() : Optional.of(clan.id);
        if (!this.entityData.get(CLAN_ID).equals(id)) this.entityData.set(CLAN_ID, id);
        this.entityData.set(CLAN_COLOR, clan == null ? -1 : clan.color);
        this.entityData.set(CLAN_SIZE, clan == null ? 0 : clan.size());
    }

    private void claimDefeatedClan(ServerLevel level, LionEntity rival) {
        if (!this.isMale() || !rival.isMale() || this.isTame() || rival.isTame()) return;
        UUID mine = getClanId();
        UUID theirs = rival.getClanId();
        if (mine == null || theirs == null || mine.equals(theirs)) return;

        LionClanData data = LionClanData.get(level.getServer());
        if (data.absorb(mine, theirs) <= 0) return;

        LionClanData.Clan clan = data.getClan(mine);
        for (LionEntity lioness : level.getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(OWAttacksConstants.Lion.CLAN_DEFENSE_RADIUS),
                lion -> theirs.equals(lion.getClanId()))) {
            lioness.setClan(clan != null && data.isMember(clan.id, lioness.getUUID()) ? clan : null);
            if (lioness.getTarget() == this) lioness.setTarget(null);
        }
        setClan(clan);
    }

    private void rallyClanAgainst(LivingEntity attacker) {
        if (getClanId() == null || attacker == this || isClanmate(attacker)) return;
        if (this.isTame() && (attacker == this.getOwner() || this.isAlliedTo(attacker))) return;
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) return;

        for (LionEntity lioness : clanLionessesWithin(OWAttacksConstants.Lion.CLAN_DEFENSE_RADIUS)) {
            if (lioness.getTarget() != null || lioness.isKnockedOut() || lioness.isVehicle()) continue;
            if (lioness.isAlliedTo(attacker) || attacker == lioness.getOwner()) continue;
            lioness.setTarget(attacker);
        }
    }

    private void updatePackStrength(ServerLevel level, @Nullable LionClanData.Clan clan) {
        int lionesses = clan == null ? 0 : clan.lionesses.size();
        if (lionesses != packLionessCount) {
            packLionessCount = lionesses;
            applyModifier(Attributes.MAX_HEALTH, PACK_HEALTH_MODIFIER,
                    OWAttacksConstants.Lion.PACK_HEALTH_PER_LIONESS * lionesses, AttributeModifier.Operation.ADD_VALUE);
            if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
        }

        int nearby = Math.min(clanLionessesWithin(OWAttacksConstants.Lion.PACK_NEARBY_RADIUS).size(),
                OWAttacksConstants.Lion.PACK_BONUS_MAX_LIONESSES);
        if (nearby != packNearbyLionesses) {
            packNearbyLionesses = nearby;
            applyModifier(Attributes.ATTACK_DAMAGE, PACK_DAMAGE_MODIFIER,
                    OWAttacksConstants.Lion.PACK_BONUS_PER_LIONESS * nearby, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    }

    private void applyModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                               ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    public List<LionEntity> clanLionessesWithin(double radius) {
        UUID clanId = getClanId();
        if (clanId == null) return List.of();
        return this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(radius),
                lion -> lion != this && lion.isAlive() && lion.isFemale() && clanId.equals(lion.getClanId())
                        && lion.distanceToSqr(this) <= radius * radius);
    }

    public boolean isClanmate(Entity entity) {
        UUID clanId = getClanId();
        return clanId != null && entity != this && entity instanceof LionEntity lion && clanId.equals(lion.getClanId());
    }

    @Nullable
    private LionEntity findClanMale() {
        if (!(this.level() instanceof ServerLevel level) || getClanId() == null) return null;
        LionClanData.Clan clan = LionClanData.get(level.getServer()).getClan(getClanId());
        if (clan == null || clan.male.equals(this.getUUID())) return null;
        return level.getEntity(clan.male) instanceof LionEntity male && male.isAlive() ? male : null;
    }

    public boolean hasCallableClanmates() {
        return getCallableClanmateCount() > 0;
    }

    public int getCallableClanmateCount() {
        return this.level().isClientSide() ? clientCallableClanmates : callableClanmates().size();
    }

    private List<LionEntity> callableClanmates() {
        List<LionEntity> lionesses = clanLionessesWithin(OWAttacksConstants.Lion.CLAN_CALL_RADIUS).stream()
                .filter(lion -> lion.isTame() && !lion.isKnockedOut() && !lion.isVehicle() && !lion.isPassenger())
                .sorted(Comparator.comparingDouble(this::distanceToSqr))
                .toList();
        return lionesses.subList(0, Math.min(lionesses.size(), OWAttacksConstants.Lion.CLAN_CALL_MAX_LIONESSES));
    }

    public void performClanCall() {
        if (this.level().isClientSide()) return;

        List<LionEntity> called = callableClanmates();
        if (isSecondaryOnCooldown() || isCalling() || isRoaring() || called.isEmpty()) {
            rejectAttack(OWAttackIds.CLAN_CALL);
            return;
        }

        float cost = OWAttacksConstants.Lion.CLAN_CALL_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            rejectAttack(OWAttackIds.CLAN_CALL);
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        startSecondaryCooldown();

        LivingEntity target = resolveCallTarget();
        callTargetId = target == null ? -1 : target.getId();

        pendingArrivals.clear();
        for (int i = 0; i < called.size(); i++) {
            int tick = OWAttacksConstants.Lion.CLAN_CALL_FIRST_ARRIVAL_TICK + i * OWAttacksConstants.Lion.CLAN_CALL_ARRIVAL_SPACING;
            pendingArrivals.add(new PendingArrival(called.get(i).getId(), tick, i));
        }

        resetCombo(0);
        actualAttackNumber = 0;
        this.getNavigation().stop();
        this.entityData.set(CALL_TICK, OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_BREATHE,
                SoundSource.NEUTRAL, 1.4f, this.isMale() ? 0.55f : 0.7f);
    }

    private void tickCall(ServerLevel level) {
        int tick = getCallTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS - tick;
        this.entityData.set(CALL_TICK, tick - 1);

        if (elapsed == OWAttacksConstants.Lion.CLAN_CALL_SHOUT_TICK) callShout(level);
        if (elapsed == OWAttacksConstants.Lion.CLAN_CALL_STOMP_TICK) groundSlam(level, 1.6, 14);

        LivingEntity target = level.getEntity(callTargetId) instanceof LivingEntity living && living.isAlive() ? living : null;
        for (Iterator<PendingArrival> iterator = pendingArrivals.iterator(); iterator.hasNext(); ) {
            PendingArrival arrival = iterator.next();
            if (arrival.tick() != elapsed) continue;
            iterator.remove();
            if (level.getEntity(arrival.lionessId()) instanceof LionEntity lioness && lioness.isAlive() && isClanmate(lioness)) {
                summonLioness(level, lioness, arrival.slot(), target);
            }
        }

        if (tick - 1 <= 0) {
            pendingArrivals.clear();
            callTargetId = -1;
        }
    }

    private void callShout(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 2.5f, this.isMale() ? 0.85f : 1.05f);

        Vec3 mouth = mouthPosition();
        level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 6, 0.1, 0.1, 0.1, 0.05);
        emitWave(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS, ParticleTypes.CLOUD, 0.3);
        emitRing(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS * 0.45, clanDust(2.0f));
        emitRing(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS * 0.8, clanDust(1.6f));
    }

    private void summonLioness(ServerLevel level, LionEntity lioness, int index, @Nullable LivingEntity target) {
        level.sendParticles(ParticleTypes.POOF, lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.5, lioness.getZ(),
                16, 0.4, 0.4, 0.4, 0.03);
        level.sendParticles(clanDust(1.4f), lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.5, lioness.getZ(),
                12, 0.4, 0.5, 0.4, 0.0);
        level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.NEUTRAL, 0.9f, 0.6f);

        boolean moved = lioness.distanceToSqr(this) >= 16.0 && teleportBeside(lioness, index);
        if (moved) {
            BlockState ground = lioness.getBlockStateOn();
            if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    lioness.getX(), lioness.getY() + 0.1, lioness.getZ(), 26, 0.6, 0.05, 0.6, 0.2);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, lioness.getX(), lioness.getY() + 0.2, lioness.getZ(),
                    6, 0.5, 0.05, 0.5, 0.01);
            level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), OWSounds.TIGER_JUMP.get(),
                    SoundSource.NEUTRAL, 1.2f, (float) OWUtils.generateRandomInterval(1.15, 1.3));
            lioness.entityData.set(ARRIVAL_TICK, OWAttacksConstants.Lion.ARRIVAL_TICKS);
        }

        float yaw = target != null
                ? (float) (Mth.atan2(target.getZ() - lioness.getZ(), target.getX() - lioness.getX()) * Mth.RAD_TO_DEG) - 90f
                : this.yBodyRot;
        lioness.setYRot(yaw);
        lioness.yBodyRot = yaw;
        lioness.setYHeadRot(yaw);

        lioness.answerClanCall(target);
    }

    private void answerClanCall(@Nullable LivingEntity target) {
        int duration = OWAttacksConstants.Lion.CLAN_CALL_DURATION_TICKS;
        this.setSitting(false);
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0));
        this.entityData.set(RALLY_TICK, duration);
        if (target != null && target.isAlive() && !this.isAlliedTo(target) && target != this.getOwner()) {
            this.setTarget(target);
        }
    }

    private boolean teleportBeside(LionEntity lioness, int index) {
        double[][] slots = {{-2.6, 0.8}, {2.6, 0.8}, {0.0, -3.0}};
        double[] slot = slots[index % slots.length];
        Vec3 preferred = new Vec3(slot[0], 0, slot[1]).scale(this.getScale())
                .yRot((float) Math.toRadians(-this.yBodyRot));

        if (lioness.randomTeleport(this.getX() + preferred.x, this.getY() + 1.0, this.getZ() + preferred.z, false)) {
            lioness.getNavigation().stop();
            return true;
        }

        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = 2.5 + this.random.nextDouble() * 2.0;
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            if (lioness.randomTeleport(x, this.getY() + 1.0, z, false)) {
                lioness.getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    @Nullable
    private LivingEntity resolveCallTarget() {
        if (this.getControllingPassenger() instanceof Player rider) {
            Vec3 eye = rider.getEyePosition();
            Vec3 look = rider.getLookAngle();
            double range = OWAttacksConstants.Lion.CLAN_CALL_TARGET_RANGE;
            LivingEntity best = null;
            double bestCosine = CALL_AIM_COSINE;
            for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
                if (!isValidCallTarget(candidate, rider)) continue;
                Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye);
                double distance = toTarget.length();
                if (distance < 1.0E-3 || distance > range) continue;
                double cosine = toTarget.scale(1.0 / distance).dot(look);
                if (cosine > bestCosine && rider.hasLineOfSight(candidate)) {
                    bestCosine = cosine;
                    best = candidate;
                }
            }
            if (best != null) return best;
        }

        int window = OWAttacksConstants.Lion.CLAN_CALL_LAST_HIT_TICKS;
        LivingEntity lastHit = this.getLastHurtMob();
        if (lastHit != null && lastHit.isAlive() && this.tickCount - this.getLastHurtMobTimestamp() < window) return lastHit;
        LivingEntity lastAttacker = this.getLastHurtByMob();
        if (lastAttacker != null && lastAttacker.isAlive() && this.tickCount - this.getLastHurtByMobTimestamp() < window) return lastAttacker;
        return null;
    }

    private boolean isValidCallTarget(LivingEntity candidate, Player rider) {
        if (candidate == this || candidate == rider || !candidate.isAlive()) return false;
        if (candidate.getRootVehicle() == this || isClanmate(candidate)) return false;
        if (candidate == this.getOwner() || this.isAlliedTo(candidate)) return false;
        return !(candidate instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    public boolean activateDominationRoar() {
        if (this.level().isClientSide()) return false;
        if (isRoaring() || isCalling()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Lion.ROAR_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Lion.ROAR_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        resetCombo(0);
        actualAttackNumber = 0;
        this.entityData.set(ROAR_TICK, OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS);
        if (!(this.getControllingPassenger() instanceof Player)) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_BREATHE,
                SoundSource.NEUTRAL, 1.6f, this.isMale() ? 0.5f : 0.65f);
        return true;
    }

    private void tickRoar(ServerLevel level) {
        int tick = getRoarTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS - tick;
        this.entityData.set(ROAR_TICK, tick - 1);

        if (elapsed == OWAttacksConstants.Lion.ROAR_SHOUT_TICK) {
            if (guardRoar) guardRoarShout(level);
            else roarOfDomination(level);
        }
        if (tick - 1 <= 0) guardRoar = false;
        if (elapsed == OWAttacksConstants.Lion.ROAR_STOMP_TICK) groundSlam(level, 1.4, 20);

        int[] pulses = OWAttacksConstants.Lion.ROAR_PULSE_TICKS;
        for (int i = 0; i < pulses.length; i++) {
            if (pulses[i] != elapsed) continue;
            float strength = 1f - (float) i / pulses.length;
            double radius = OWAttacksConstants.Lion.ROAR_WAVE_RADIUS * (0.55 + 0.45 * strength);
            emitWave(level, radius, ParticleTypes.CLOUD, 0.35 + 0.25 * strength);
            emitRing(level, 1.5 + i * 1.6, roarDust(1.3f + strength));
        }
    }

    private void roarOfDomination(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 3.0f, this.isMale() ? 0.8f : 1.0f);
        OWUtils.spawnServerParticles(this, ParticleTypes.FLASH, 0, 0.5, 0, 1, 0);

        Vec3 mouth = mouthPosition();
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        for (int i = 0; i < 18; i++) {
            double spread = (this.random.nextDouble() - 0.5) * 0.6;
            Vec3 direction = forward.yRot((float) spread).add(0, 0.05 + this.random.nextDouble() * 0.15, 0);
            level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 0,
                    direction.x, direction.y, direction.z, 0.35 + this.random.nextDouble() * 0.25);
        }

        int duration = OWAttacksConstants.Lion.ROAR_BUFF_TICKS;
        List<LionEntity> pride = new ArrayList<>(clanLionessesWithin(OWAttacksConstants.Lion.ROAR_RADIUS));
        if (this.isFemale()) pride.add(this);

        for (LionEntity lioness : pride) {
            lioness.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, OWAttacksConstants.Lion.ROAR_SPEED_AMPLIFIER));
            lioness.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, OWAttacksConstants.Lion.ROAR_STRENGTH_AMPLIFIER));
            lioness.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, OWAttacksConstants.Lion.ROAR_REGENERATION_AMPLIFIER));
            lioness.entityData.set(EMPOWER_TICK, duration);
            if (lioness == this) continue;

            lioness.entityData.set(RESPOND_TICK, OWAttacksConstants.Lion.RESPOND_TICKS);
            level.sendParticles(ParticleTypes.WAX_ON, lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.6, lioness.getZ(),
                    14, 0.45, 0.4, 0.45, 0.6);
            level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), OWSounds.TIGER_ROAR.get(),
                    SoundSource.NEUTRAL, 1.1f, (float) OWUtils.generateRandomInterval(1.2, 1.4));
        }
    }

    private void groundSlam(ServerLevel level, double reach, int count) {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        double x = this.getX() + forward.x * reach * this.getScale();
        double z = this.getZ() + forward.z * reach * this.getScale();

        BlockState ground = level.getBlockState(BlockPos.containing(x, this.getY() - 0.2, z));
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, this.getY() + 0.05, z,
                count, 0.6, 0.05, 0.6, 0.18);
        SoundType soundType = ground.getSoundType(level, BlockPos.containing(x, this.getY() - 0.2, z), this);
        level.playSound(null, x, this.getY(), z, soundType.getStepSound(), SoundSource.NEUTRAL,
                soundType.getVolume() * 1.4f, soundType.getPitch() * 0.55f);
    }

    private void emitWave(ServerLevel level, double radius, ParticleOptions particle, double speed) {
        double y = this.getY() + 0.15;
        for (int i = 0; i < WAVE_PARTICLES; i++) {
            double angle = i * (Math.PI * 2.0 / WAVE_PARTICLES);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.sendParticles(particle, this.getX() + dx * 0.8, y, this.getZ() + dz * 0.8, 0,
                    dx, 0.02, dz, speed * radius / 5.0);
        }
    }

    private void emitRing(ServerLevel level, double radius, ParticleOptions particle) {
        double y = this.getY() + 0.2;
        int count = Math.max(16, (int) (radius * 10));
        for (int i = 0; i < count; i++) {
            double angle = i * (Math.PI * 2.0 / count);
            level.sendParticles(particle, this.getX() + Math.cos(angle) * radius, y, this.getZ() + Math.sin(angle) * radius,
                    1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private DustParticleOptions clanDust(float size) {
        int color = getClanColor() >= 0 ? getClanColor() : ENTITY_COLOR;
        return new DustParticleOptions(new Vector3f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f), size);
    }

    private static DustParticleOptions roarDust(float size) {
        return new DustParticleOptions(new Vector3f(0.95f, 0.72f, 0.28f), size);
    }

    private Vec3 mouthPosition() {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        double reach = 1.35 * this.getScale();
        return new Vec3(this.getX() + forward.x * reach, this.getY() + 1.0 * this.getScale(), this.getZ() + forward.z * reach);
    }

    private void tickGestureCounters() {
        decrement(ARRIVAL_TICK);
        decrement(RESPOND_TICK);
        decrement(RALLY_TICK);
        decrement(EMPOWER_TICK);
    }

    private void decrement(EntityDataAccessor<Integer> accessor) {
        int value = this.entityData.get(accessor);
        if (value > 0) this.entityData.set(accessor, value - 1);
    }

    private void tickClientEffects() {
        tickAnimationBlends();
        tickLatchClient();

        if (this.isVehicle() && this.tickCount % CALLABLE_REFRESH_TICKS == 0) {
            clientCallableClanmates = callableClanmates().size();
        }

        int roarElapsed = isRoaring() ? OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS - getRoarTick() : -1;
        if (crossed(lastRoarElapsed, roarElapsed, OWAttacksConstants.Lion.ROAR_SHOUT_TICK)
                || crossed(lastRoarElapsed, roarElapsed, OWAttacksConstants.Lion.ROAR_STOMP_TICK)) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        lastRoarElapsed = roarElapsed;

        int callElapsed = isCalling() ? OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS - getCallTick() : -1;
        if (crossed(lastCallElapsed, callElapsed, OWAttacksConstants.Lion.CLAN_CALL_STOMP_TICK)) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        lastCallElapsed = callElapsed;

        if (isEmpowered() && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.WAX_ON,
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * (0.3 + this.random.nextDouble() * 0.6),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0, 0.02, 0);
        }

        if (isRallied() && this.tickCount % 4 == 0) {
            this.level().addParticle(clanDust(0.9f),
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * (0.2 + this.random.nextDouble() * 0.7),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0, 0.01, 0);
        }

        if ((isEmpowered() || isRallied()) && this.isRunning() && this.onGround() && this.tickCount % 2 == 0) {
            BlockState ground = this.getBlockStateOn();
            if (!ground.isAir()) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                        this.getX(), this.getY() + 0.1, this.getZ(), 0, 0.05, 0);
            }
        }
    }

    private static boolean crossed(int previous, int current, int mark) {
        return previous < mark && current >= mark;
    }

    private void tickAnimationBlends() {
        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() || this.getState() == 2 ? 1f : 0f, RUN_BLEND_RATE);

        sitBlendO = sitBlend;
        sitBlend = approach(sitBlend, this.isSitting() ? 1f : 0f, SIT_BLEND_RATE);

        restBlendO = restBlend;
        restBlend = approach(restBlend, this.isNapping() ? 1f : 0f, REST_BLEND_RATE);

        roarBlendO = roarBlend;
        roarBlend = approach(roarBlend, isRoaring() ? 1f : 0f, isRoaring() ? ROAR_BLEND_IN_RATE : ROAR_BLEND_OUT_RATE);

        empowerBlendO = empowerBlend;
        empowerBlend = approach(empowerBlend, isEmpowered() || isRallied() ? 1f : 0f, EMPOWER_BLEND_RATE);

        int phase = getLatchPhase();
        latchBlendO = latchBlend;
        latchBlend = approach(latchBlend, phase == LATCH_HOLD ? 1f : 0f, LATCH_BLEND_RATE);
        leapBlendO = leapBlend;
        leapBlend = approach(leapBlend, phase == LATCH_LEAP ? 1f : 0f, LEAP_BLEND_RATE);
        releaseBlendO = releaseBlend;
        releaseBlend = approach(releaseBlend, phase == LATCH_RELEASE ? 1f : 0f, RELEASE_BLEND_RATE);
        distressBlendO = distressBlend;
        distressBlend = approach(distressBlend, isDistressing() ? 1f : 0f, DISTRESS_BLEND_RATE);
        guardBlendO = guardBlend;
        guardBlend = approach(guardBlend, isGuardPosed() ? 1f : 0f, GUARD_BLEND_RATE);

        if (earFlickTicks > 0) {
            earFlickTicks--;
        } else if (--earFlickCooldown <= 0) {
            earFlickTicks = EAR_FLICK_TICKS;
            earFlickLeft = this.random.nextBoolean();
            earFlickCooldown = 80 + this.random.nextInt(160);
        }
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    public float runBlend(float partialTick) { return Mth.lerp(partialTick, runBlendO, runBlend); }

    public float sitBlend(float partialTick) { return Mth.lerp(partialTick, sitBlendO, sitBlend); }

    public float restBlend(float partialTick) { return Mth.lerp(partialTick, restBlendO, restBlend); }

    public float roarBlend(float partialTick) { return Mth.lerp(partialTick, roarBlendO, roarBlend); }

    public float empowerBlend(float partialTick) { return Mth.lerp(partialTick, empowerBlendO, empowerBlend); }

    public float latchBlend(float partialTick) { return Mth.lerp(partialTick, latchBlendO, latchBlend); }

    public float leapBlend(float partialTick) { return Mth.lerp(partialTick, leapBlendO, leapBlend); }

    public float releaseBlend(float partialTick) { return Mth.lerp(partialTick, releaseBlendO, releaseBlend); }

    public float distressBlend(float partialTick) { return Mth.lerp(partialTick, distressBlendO, distressBlend); }

    public float guardBlend(float partialTick) { return Mth.lerp(partialTick, guardBlendO, guardBlend); }

    public float earFlick(float partialTick) {
        if (earFlickTicks <= 0) return 0f;
        float progress = (EAR_FLICK_TICKS - earFlickTicks + partialTick) / EAR_FLICK_TICKS;
        return Mth.sin(Mth.clamp(progress, 0f, 1f) * (float) Math.PI);
    }

    public boolean isEarFlickLeft() { return earFlickLeft; }

    public boolean canLatchOnto(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity target) || target == this || !target.isAlive()) return false;
        if (target instanceof ArmorStand || target.isPassenger() || target.isUnderWater()) return false;
        if (target.getRootVehicle() == this || target == this.getOwner()) return false;
        if (target.getBbWidth() < OWAttacksConstants.Lion.LEAP_MIN_TARGET_WIDTH
                || target.getBbHeight() < OWAttacksConstants.Lion.LEAP_MIN_TARGET_HEIGHT) return false;
        if (isClanmate(target) || this.isAlliedTo(target)) return false;
        if (target instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null
                && tamable.getOwnerUUID().equals(this.getOwnerUUID())) return false;
        return !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    @Nullable
    public LivingEntity findLeapTarget(Player rider) {
        Vec3 eye = rider.getEyePosition();
        Vec3 look = rider.getLookAngle();
        double range = OWAttacksConstants.Lion.LEAP_TARGET_RANGE;
        LivingEntity best = null;
        double bestCosine = OWAttacksConstants.Lion.LEAP_AIM_COSINE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range), this::canLatchOnto)) {
            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye);
            double distance = toTarget.length();
            if (distance < 1.0E-3 || distance > range + candidate.getBbWidth() * 0.5) continue;
            double tolerance = Math.atan2(candidate.getBbWidth() * 0.5, distance);
            double cosine = toTarget.scale(1.0 / distance).dot(look) + tolerance * 0.12;
            if (cosine > bestCosine && rider.hasLineOfSight(candidate)) {
                bestCosine = cosine;
                best = candidate;
            }
        }
        return best;
    }

    public boolean startHuntingLeap(int targetId) {
        if (this.level().isClientSide() || this.isMale() || this.isBaby()) return false;
        if (getLatchPhase() != LATCH_NONE || isDistressing() || isSecondaryOnCooldown()) return false;
        if (!(this.getControllingPassenger() instanceof Player rider)) return false;
        if (!(this.level().getEntity(targetId) instanceof LivingEntity target) || !canLatchOnto(target) || !hasLatchRoom(target)) return false;
        if (this.distanceTo(target) > OWAttacksConstants.Lion.LEAP_TARGET_RANGE + target.getBbWidth() + 2.0) return false;

        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(rider.getEyePosition());
        if (toTarget.lengthSqr() > 1.0E-4 && toTarget.normalize().dot(rider.getLookAngle()) < OWAttacksConstants.Lion.LEAP_SERVER_AIM_COSINE) return false;
        if (!rider.hasLineOfSight(target)) return false;

        float cost = OWAttacksConstants.Lion.LEAP_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        beginHunt(target, true);
        return true;
    }

    private void beginHunt(LivingEntity target, boolean byRider) {
        latchByRider = byRider;
        resetCombo(0);
        actualAttackNumber = 0;
        this.getNavigation().stop();
        this.entityData.set(LATCH_TARGET, target.getId());
        this.entityData.set(LATCH_PHASE, LATCH_RUSH);
        this.setRunning(true);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_HURTING.get(),
                SoundSource.NEUTRAL, 1.3f, (float) OWUtils.generateRandomInterval(1.2, 1.3));
    }

    private void tickAutoPounce() {
        if (this.isMale() || this.isBaby() || getLatchPhase() != LATCH_NONE || isSecondaryOnCooldown()) return;
        if ((this.tickCount + this.getId()) % OWAttacksConstants.Lion.AUTO_LEAP_CHECK_TICKS != 0) return;
        if (this.getControllingPassenger() != null || this.isVehicle() || this.isPassenger()) return;
        if (isDistressing() || isRoaring() || this.isSitting() || this.isNapping() || this.isSleeping()
                || this.isKnockedOut() || this.isSedatedFleeing() || this.isInWater()) return;

        LivingEntity target = this.getTarget();
        if (target == null || target instanceof LionEntity || !canLatchOnto(target) || !hasLatchRoom(target)) return;
        double distance = this.distanceTo(target);
        if (distance > OWAttacksConstants.Lion.AUTO_LEAP_MAX_DISTANCE || !this.getSensing().hasLineOfSight(target)) return;
        if (this.random.nextFloat() > OWAttacksConstants.Lion.AUTO_LEAP_CHANCE) return;

        beginHunt(target, false);
    }

    public int getLatchTargetId() {
        return this.entityData.get(LATCH_TARGET);
    }

    public static int latchCapacity(LivingEntity target) {
        return Mth.clamp((int) Math.floor(target.getBbWidth() * OWAttacksConstants.Lion.LATCH_SLOTS_PER_BLOCK),
                1, OWAttacksConstants.Lion.LATCH_MAX_SLOTS);
    }

    private List<LionEntity> otherLatchers(LivingEntity target) {
        return this.level().getEntitiesOfClass(LionEntity.class, target.getBoundingBox().inflate(OWAttacksConstants.Lion.LEAP_TARGET_RANGE + 8.0),
                lion -> lion != this && lion.isAlive() && lion.isLatchActive() && lion.getLatchTargetId() == target.getId());
    }

    public boolean hasLatchRoom(LivingEntity target) {
        return otherLatchers(target).size() < latchCapacity(target);
    }

    @Nullable
    private Float chooseLatchAngle(LivingEntity target) {
        float desired = localAngleAround(target);
        float separation = 360f / latchCapacity(target) * 0.9f;
        List<Float> taken = new ArrayList<>();
        for (LionEntity other : otherLatchers(target)) {
            if (other.isLatchedOn()) taken.add(other.getLatchAngle());
        }
        for (int step = 0; step <= 36; step++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                float candidate = Mth.wrapDegrees(desired + sign * step * 5f);
                boolean free = true;
                for (float angle : taken) {
                    if (Math.abs(Mth.wrapDegrees(candidate - angle)) < separation) {
                        free = false;
                        break;
                    }
                }
                if (free) return candidate;
                if (step == 0) break;
            }
        }
        return null;
    }

    public void releaseLatchByRider() {
        int phase = getLatchPhase();
        if (phase == LATCH_HOLD || phase == LATCH_LEAP) releaseLatch(END_LET_GO);
        else if (phase == LATCH_RUSH) abortLeap();
    }

    public void lionessBite() {
        if (this.level().isClientSide() || getLatchPhase() != LATCH_HOLD || biteCooldown > 0) return;
        biteCooldown = OWAttacksConstants.Lion.BITE_COOLDOWN_TICKS;
        pendingBiteImpact = OWAttacksConstants.Lion.BITE_IMPACT_TICK;
        this.entityData.set(BITE_TICK, OWAttacksConstants.Lion.BITE_ANIMATION_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_HURTING.get(),
                SoundSource.NEUTRAL, 1.0f, (float) OWUtils.generateRandomInterval(1.25, 1.4));
    }

    private void tickLatch() {
        int phase = getLatchPhase();
        if (phase != latchPhaseSeen) {
            latchPhaseSeen = phase;
            latchPhaseTicks = 0;
            onLatchPhaseEntered(phase);
        } else {
            latchPhaseTicks++;
        }

        if (fallGraceTicks > 0) {
            fallGraceTicks--;
            this.fallDistance = 0f;
        }
        this.noPhysics = phase == LATCH_LEAP || phase == LATCH_HOLD;
        if (this.isNoGravity() != this.noPhysics) this.setNoGravity(this.noPhysics);
        if (phase == LATCH_NONE) return;

        LivingEntity target = getLatchTarget();
        boolean drives = !this.level().isClientSide() || this.isControlledByLocalInstance();
        if (drives && target != null) {
            if (phase == LATCH_RUSH) driveRush(target);
            else if (phase == LATCH_LEAP || phase == LATCH_HOLD) followAnchor(target, phase);
        }
        if (phase == LATCH_RELEASE) this.fallDistance = 0f;

        if (this.level() instanceof ServerLevel level) tickLatchServer(level, phase, target);
    }

    private void onLatchPhaseEntered(int phase) {
        if (phase == LATCH_RUSH) {
            Vec3 motion = this.getDeltaMovement();
            rushSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        } else if (phase == LATCH_LEAP) {
            leapStart = this.position();
            leapStartYaw = this.getYRot();
        } else if (phase == LATCH_RELEASE) {
            this.noPhysics = false;
            fallGraceTicks = FALL_GRACE_TICKS;
            if (!this.level().isClientSide() || this.isControlledByLocalInstance()) applyReleasePush();
        } else if (phase == LATCH_NONE) {
            this.noPhysics = false;
        }
    }

    private void driveRush(LivingEntity target) {
        float yaw = Mth.approachDegrees(this.getYRot(), yawTowards(target), OWAttacksConstants.Lion.LEAP_RUSH_TURN_DEGREES);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.setRunning(true);

        rushSpeed = Math.min(OWAttacksConstants.Lion.LEAP_RUSH_SPEED, rushSpeed + OWAttacksConstants.Lion.LEAP_RUSH_ACCELERATION);
        Vec3 direction = Vec3.directionFromRotation(0f, yaw);
        double y = this.getDeltaMovement().y;
        if (this.horizontalCollision && this.onGround()) y = OWAttacksConstants.Lion.LEAP_RUSH_JUMP;
        this.setDeltaMovement(direction.x * rushSpeed, y, direction.z * rushSpeed);
        this.hasImpulse = true;
    }

    private float yawTowards(LivingEntity target) {
        return (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90f;
    }

    private void followAnchor(LivingEntity target, int phase) {
        Vec3 anchor = latchAnchor(target);
        float facing = latchFacingYaw(target);
        Vec3 pos = anchor;
        float yaw = facing;

        if (phase == LATCH_LEAP) {
            float t = leapProgress();
            double arc = OWAttacksConstants.Lion.LEAP_ARC_BASE + OWAttacksConstants.Lion.LEAP_ARC_PER_TICK * getLeapFlightTicks();
            double y = Mth.lerp(t * t, leapStart.y, anchor.y) + arc * 4.0 * t * (1.0 - t);
            pos = new Vec3(Mth.lerp(t, leapStart.x, anchor.x), y, Mth.lerp(t, leapStart.z, anchor.z));
            yaw = Mth.rotLerp(t * t, leapStartYaw, facing);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(pos.x, pos.y, pos.z);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.setXRot(0f);
        this.fallDistance = 0f;
    }

    private float leapProgress() {
        return Mth.clamp((latchPhaseTicks + 1f) / getLeapFlightTicks(), 0f, 1f);
    }

    public float leapAnimationProgress(float partialTick) {
        if (getLatchPhase() != LATCH_LEAP) return 1f;
        return Mth.clamp((latchPhaseTicks + partialTick) / getLeapFlightTicks(), 0f, 1f);
    }

    public int getLeapFlightTicks() {
        return Math.max(1, this.entityData.get(LEAP_FLIGHT));
    }

    private float clingAmount() {
        int phase = getLatchPhase();
        if (phase == LATCH_HOLD) return 1f;
        if (phase == LATCH_LEAP) return leapProgress();
        return 0f;
    }

    private Vec3 latchAnchor(LivingEntity target) {
        double rad = Math.toRadians(target.yBodyRot + getLatchAngle());
        double radius = target.getBbWidth() * 0.5 + CLING_SURFACE_OFFSET * this.getScale();
        double lift = Math.max(0.0, target.getBbHeight() * CLING_HEIGHT_RATIO - CLING_PIVOT * this.getScale());
        return new Vec3(target.getX() - Math.sin(rad) * radius, target.getY() + lift, target.getZ() + Math.cos(rad) * radius);
    }

    private float latchFacingYaw(LivingEntity target) {
        return Mth.wrapDegrees(target.yBodyRot + getLatchAngle() + 180f);
    }

    private float localAngleAround(LivingEntity target) {
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        return Mth.wrapDegrees(yaw - target.yBodyRot);
    }

    private static int zoneOf(float localAngle) {
        float angle = Math.abs(Mth.wrapDegrees(localAngle));
        if (angle <= THROAT_ZONE_DEGREES) return ZONE_THROAT;
        if (angle >= RUMP_ZONE_DEGREES) return ZONE_RUMP;
        return ZONE_FLANK;
    }

    private void tickLatchServer(ServerLevel level, int phase, @Nullable LivingEntity target) {
        if (biteCooldown > 0) biteCooldown--;
        decrement(BITE_TICK);
        decrement(LATCH_SHAKE);

        if (phase == LATCH_RELEASE) {
            if (latchPhaseTicks >= OWAttacksConstants.Lion.LEAP_RELEASE_TICKS) endLatch();
            return;
        }

        if (target == null || target.level() != level) {
            if (phase == LATCH_RUSH) abortLeap();
            else releaseLatch(END_PREY_LOST);
            return;
        }
        if (!target.isAlive()) {
            if (phase == LATCH_RUSH) abortLeap();
            else releaseLatch(END_PREY_DOWN);
            return;
        }
        boolean ridden = this.getControllingPassenger() instanceof Player;
        if (ridden) latchByRider = true;
        if (latchByRider && !ridden) {
            if (phase == LATCH_RUSH) abortLeap();
            else releaseLatch(END_RIDER_GONE);
            return;
        }
        if (!ridden) this.getNavigation().stop();

        if (phase == LATCH_RUSH) {
            if (latchPhaseTicks > OWAttacksConstants.Lion.LEAP_RUSH_MAX_TICKS) abortLeap();
            else if (isInLeapReach(target)) beginLeap(level, target);
            return;
        }

        if (phase == LATCH_LEAP) {
            if (latchPhaseTicks >= getLeapFlightTicks()) latchOn(level, target);
            return;
        }

        int grip = getLatchGrip() - 1;
        this.entityData.set(LATCH_GRIP, Math.max(0, grip));
        if (grip <= 0) {
            releaseLatch(END_GRIP_LOST);
            return;
        }

        if (this.tickCount % 20 == 0) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, OWAttacksConstants.Lion.LATCH_SLOW_AMPLIFIER, false, false));
        }

        if (!latchByRider && biteCooldown <= 0 && pendingBiteImpact <= 0
                && this.random.nextFloat() < OWAttacksConstants.Lion.AUTO_BITE_CHANCE) {
            lionessBite();
        }

        if (pendingBiteImpact > 0 && --pendingBiteImpact == 0) {
            biteImpact(level, target);
            if (!target.isAlive()) {
                releaseLatch(END_PREY_DOWN);
                return;
            }
        }

        if (--shakeTimer <= 0) shakeOff(level, target);
        if (getLatchGrip() <= 0) {
            releaseLatch(END_GRIP_LOST);
            return;
        }

        if (this.tickCount % 5 == 0) {
            Vec3 claw = latchContactPoint(target, 0.2);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    claw.x, claw.y, claw.z, 1, 0.15, 0.2, 0.15, 0.0);
        }
    }

    private boolean isInLeapReach(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double gap = Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
        double dy = target.getY() - this.getY();
        float facing = Math.abs(Mth.wrapDegrees(yawTowards(target) - this.getYRot()));
        return gap <= OWAttacksConstants.Lion.LEAP_TRIGGER_DISTANCE && facing <= OWAttacksConstants.Lion.LEAP_TRIGGER_FACING_DEGREES
                && dy > -2.5 && dy < target.getBbHeight() + 1.5;
    }

    private void beginLeap(ServerLevel level, LivingEntity target) {
        Float angle = chooseLatchAngle(target);
        if (angle == null) {
            abortLeap();
            return;
        }
        this.entityData.set(LATCH_ANGLE, angle);
        double distance = this.position().distanceTo(latchAnchor(target));
        int flight = (int) Math.round(distance * OWAttacksConstants.Lion.LEAP_FLIGHT_TICKS_PER_BLOCK);
        this.entityData.set(LEAP_FLIGHT, Mth.clamp(flight, OWAttacksConstants.Lion.LEAP_FLIGHT_MIN_TICKS, OWAttacksConstants.Lion.LEAP_FLIGHT_MAX_TICKS));
        this.entityData.set(LATCH_PHASE, LATCH_LEAP);
        this.setRunning(false);

        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_JUMP.get(),
                SoundSource.NEUTRAL, 1.3f, (float) OWUtils.generateRandomInterval(1.15, 1.3));
        BlockState ground = this.getBlockStateOn();
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                this.getX(), this.getY() + 0.1, this.getZ(), 18, 0.4, 0.05, 0.4, 0.15);
    }

    private void latchOn(ServerLevel level, LivingEntity target) {
        this.entityData.set(LATCH_PHASE, LATCH_HOLD);
        this.entityData.set(LATCH_GRIP, OWAttacksConstants.Lion.GRIP_MAX_TICKS);
        shakeTimer = nextShakeDelay();

        float damage = this.getCombatDamage() * OWAttacksConstants.Lion.LATCH_POUNCE_DAMAGE_FACTOR;
        target.invulnerableTime = 0;
        target.hurt(this.damageSources().mobAttack(this), damage);

        Vec3 contact = latchContactPoint(target, 0.55);
        spawnBlood(level, contact, 22);
        level.sendParticles(ParticleTypes.CRIT, contact.x, contact.y, contact.z, 14, 0.3, 0.3, 0.3, 0.3);
        level.playSound(null, contact.x, contact.y, contact.z, SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.NEUTRAL, 1.1f, 0.65f);
        level.playSound(null, contact.x, contact.y, contact.z, SoundEvents.FOX_BITE,
                SoundSource.NEUTRAL, 1.4f, 0.55f);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_HURTING.get(),
                SoundSource.NEUTRAL, 1.2f, 1.1f);
    }

    private void biteImpact(ServerLevel level, LivingEntity target) {
        float damage = this.getCombatDamage() / MAX_ATTACKS_IN_COMBO * OWAttacksConstants.Lion.BITE_COMBO_MULTIPLIER;
        target.invulnerableTime = 0;
        target.hurt(this.damageSources().mobAttack(this), damage);

        Vec3 mouth = latchContactPoint(target, 0.8);
        spawnBlood(level, mouth, 18);
        level.playSound(null, mouth.x, mouth.y, mouth.z, SoundEvents.FOX_BITE,
                SoundSource.NEUTRAL, 1.3f, (float) OWUtils.generateRandomInterval(0.55, 0.7));
        level.playSound(null, mouth.x, mouth.y, mouth.z, SoundEvents.PLAYER_ATTACK_STRONG,
                SoundSource.NEUTRAL, 0.9f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
    }

    private void shakeOff(ServerLevel level, LivingEntity target) {
        shakeTimer = nextShakeDelay();
        int grip = getLatchGrip() - OWAttacksConstants.Lion.GRIP_SHAKE_LOSS;
        this.entityData.set(LATCH_GRIP, Math.max(0, grip));
        this.entityData.set(LATCH_SHAKE, SHAKE_SIGNAL_TICKS);

        BlockState ground = target.getBlockStateOn();
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                target.getX(), target.getY() + 0.1, target.getZ(), 20, target.getBbWidth() * 0.4, 0.05, target.getBbWidth() * 0.4, 0.15);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                SoundSource.NEUTRAL, 1.2f, 0.6f);
        SoundType soundType = ground.getSoundType(level, target.blockPosition(), target);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), soundType.getStepSound(),
                SoundSource.NEUTRAL, soundType.getVolume() * 1.5f, soundType.getPitch() * 0.6f);
    }

    private int nextShakeDelay() {
        int zone = getLatchZone();
        return OWAttacksConstants.Lion.GRIP_SHAKE_INTERVAL[zone] + this.random.nextInt(OWAttacksConstants.Lion.GRIP_SHAKE_SPREAD[zone]);
    }

    private void onLatchedHurt(float amount) {
        int grip = getLatchGrip() - Math.round(amount * OWAttacksConstants.Lion.GRIP_LOSS_PER_DAMAGE);
        this.entityData.set(LATCH_GRIP, Math.max(0, grip));
        if (amount >= this.getMaxHealth() * OWAttacksConstants.Lion.GRIP_KNOCKOFF_HEALTH_RATIO) releaseLatch(END_KNOCKED_OFF);
        else if (grip <= 0) releaseLatch(END_GRIP_LOST);
    }

    private void releaseLatch(int reason) {
        if (this.level().isClientSide()) return;
        int phase = getLatchPhase();
        if (phase != LATCH_HOLD && phase != LATCH_LEAP) return;
        boolean victory = reason == END_PREY_DOWN;
        this.entityData.set(RELEASE_LEAP, victory || reason == END_LET_GO);

        this.entityData.set(LATCH_PHASE, LATCH_RELEASE);
        this.entityData.set(LATCH_GRIP, 0);
        pendingBiteImpact = 0;
        startSecondaryCooldown();

        if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_JUMP.get(),
                    SoundSource.NEUTRAL, 1.1f, (float) OWUtils.generateRandomInterval(1.25, 1.4));
            if (victory) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                        SoundSource.NEUTRAL, 1.6f, 1.25f);
            }
        }
    }

    private void abortLeap() {
        if (this.level().isClientSide()) return;
        this.entityData.set(LATCH_PHASE, LATCH_NONE);
        this.entityData.set(LATCH_TARGET, -1);
        this.setRunning(false);
        setSecondaryCooldown(OWAttacksConstants.Lion.LEAP_ABORT_COOLDOWN_TICKS);
    }

    private void endLatch() {
        this.entityData.set(LATCH_PHASE, LATCH_NONE);
        this.entityData.set(LATCH_TARGET, -1);
        this.entityData.set(LATCH_GRIP, 0);
        this.noPhysics = false;
        pendingBiteImpact = 0;
    }

    private void applyReleasePush() {
        LivingEntity target = getLatchTarget();
        Vec3 away = target != null
                ? new Vec3(this.getX() - target.getX(), 0, this.getZ() - target.getZ())
                : Vec3.directionFromRotation(0f, this.getYRot() + 180f);
        away = away.lengthSqr() < 1.0E-4 ? Vec3.directionFromRotation(0f, this.getYRot() + 180f) : away.normalize();
        boolean leap = this.entityData.get(RELEASE_LEAP);
        double push = OWAttacksConstants.Lion.LEAP_RELEASE_PUSH * (leap ? 1.0 : 0.45);
        double lift = leap ? OWAttacksConstants.Lion.LEAP_RELEASE_LIFT : 0.12;
        this.setDeltaMovement(away.x * push, lift, away.z * push);
        this.hasImpulse = true;
    }

    private Vec3 latchContactPoint(LivingEntity target, double heightFraction) {
        Vec3 anchor = latchAnchor(target);
        Vec3 inward = new Vec3(target.getX() - anchor.x, 0, target.getZ() - anchor.z);
        if (inward.lengthSqr() > 1.0E-4) inward = inward.normalize().scale(CLING_SURFACE_OFFSET * this.getScale());
        double y = anchor.y + CLING_PIVOT * this.getScale() + heightFraction * this.getScale();
        return new Vec3(anchor.x + inward.x, Math.min(y, target.getY() + target.getBbHeight()), anchor.z + inward.z);
    }

    private static void spawnBlood(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(new DustParticleOptions(new Vector3f(0.55f, 0.02f, 0.02f), 1.3f),
                at.x, at.y, at.z, count, 0.18, 0.18, 0.18, 0.02);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                at.x, at.y, at.z, count / 2, 0.12, 0.12, 0.12, 0.12);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                at.x, at.y, at.z, count / 3, 0.25, 0.1, 0.25, 0.0);
    }

    private void tickLatchClient() {
        if (getLatchPhase() == LATCH_NONE && !this.isMale() && this.isControlledByLocalInstance()
                && this.getControllingPassenger() instanceof Player rider) {
            LivingEntity target = isSecondaryOnCooldown() || !this.isRunning() ? null : findLeapTarget(rider);
            if (target != null && !hasLatchRoom(target)) target = null;
            clientLeapTargetId = target == null ? -1 : target.getId();
        } else {
            clientLeapTargetId = getLatchPhase() == LATCH_RUSH ? this.entityData.get(LATCH_TARGET) : -1;
        }

        if (!this.isMale() && this.isVehicle() && this.tickCount % DISTRESS_REFRESH_TICKS == 0) {
            clientDistressResponders = distressResponders().size();
        }

        int shake = this.entityData.get(LATCH_SHAKE);
        if (shake > lastShakeSignal) net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        lastShakeSignal = shake;
    }

    public int getClientLeapTargetId() {
        return clientLeapTargetId;
    }

    public int getLatchPhase() {
        return this.entityData.get(LATCH_PHASE);
    }

    public boolean isLatchActive() {
        return getLatchPhase() != LATCH_NONE && getLatchPhase() != LATCH_RELEASE;
    }

    public boolean isLatchedOn() {
        int phase = getLatchPhase();
        return phase == LATCH_HOLD || phase == LATCH_LEAP;
    }

    @Nullable
    public LivingEntity getLatchTarget() {
        int id = this.entityData.get(LATCH_TARGET);
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    public float getLatchAngle() {
        return this.entityData.get(LATCH_ANGLE);
    }

    public int getLatchZone() {
        return zoneOf(getLatchAngle());
    }

    public int getLatchGrip() {
        return this.entityData.get(LATCH_GRIP);
    }

    public float getLatchGripRatio() {
        return Mth.clamp((float) getLatchGrip() / OWAttacksConstants.Lion.GRIP_MAX_TICKS, 0f, 1f);
    }

    public int getLatchShakeTicks() {
        return this.entityData.get(LATCH_SHAKE);
    }

    public int getBiteTick() {
        return this.entityData.get(BITE_TICK);
    }

    public boolean activateDistressCall() {
        if (this.level().isClientSide() || this.isMale()) return false;
        if (isDistressing() || getLatchPhase() != LATCH_NONE) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Lion.DISTRESS_KILLS_REQUIRED) return false;
        if (!isWoundedEnoughForDistress() || distressResponders().isEmpty()) return false;

        float cost = OWAttacksConstants.Lion.DISTRESS_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        setUltimateKillCount(0);
        resetCombo(0);
        actualAttackNumber = 0;
        this.getNavigation().stop();
        if (!(this.getControllingPassenger() instanceof Player)) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        this.entityData.set(DISTRESS_TICK, OWAttacksConstants.Lion.DISTRESS_GESTURE_TICKS);
        return true;
    }

    public boolean isWoundedEnoughForDistress() {
        return this.getHealth() <= this.getMaxHealth() * OWAttacksConstants.Lion.DISTRESS_HEALTH_RATIO;
    }

    public boolean isDistressReady() {
        return isWoundedEnoughForDistress() && getDistressResponderCount() > 0;
    }

    public int getDistressResponderCount() {
        return this.level().isClientSide() ? clientDistressResponders : distressResponders().size();
    }

    public int getDistressTick() {
        return this.entityData.get(DISTRESS_TICK);
    }

    public boolean isDistressing() {
        return getDistressTick() > 0;
    }

    private List<LionEntity> distressResponders() {
        UUID clanId = getClanId();
        if (clanId == null || !this.isTame()) return List.of();
        double radius = OWAttacksConstants.Lion.DISTRESS_CALL_RADIUS;
        return this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(radius),
                lion -> lion != this && lion.isAlive() && lion.isTame() && !lion.isBaby() && clanId.equals(lion.getClanId())
                        && !lion.isVehicle() && !lion.isPassenger() && !lion.isKnockedOut() && !lion.isLeashed()
                        && lion.distanceToSqr(this) <= radius * radius);
    }

    private void tickDistress(ServerLevel level) {
        if (guardedTicks > 0) {
            guardedTicks--;
            if (guardedTicks % 20 == 0) {
                emitRing(level, OWAttacksConstants.Lion.GUARD_RING_RADIUS + this.getBbWidth() * 0.5, clanDust(1.1f));
            }
        }

        int tick = getDistressTick();
        if (tick <= 0) return;
        int elapsed = OWAttacksConstants.Lion.DISTRESS_GESTURE_TICKS - tick;
        this.entityData.set(DISTRESS_TICK, tick - 1);

        if (elapsed == OWAttacksConstants.Lion.DISTRESS_MOAN_TICK) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_HURT.get(),
                    SoundSource.NEUTRAL, 1.4f, 1.35f);
        }
        if (elapsed == OWAttacksConstants.Lion.DISTRESS_FIRST_CALL_TICK) {
            distressShout(level, 2.6f, 1.38f, 0.8);
            summonGuard(level);
        }
        if (elapsed == OWAttacksConstants.Lion.DISTRESS_SECOND_CALL_TICK) {
            distressShout(level, 3.2f, 1.28f, 1.0);
        }
    }

    private void distressShout(ServerLevel level, float volume, float pitch, double strength) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, volume, pitch);
        Vec3 mouth = mouthPosition().add(0, 0.35 * this.getScale(), 0);
        for (int i = 0; i < 10; i++) {
            double spread = (this.random.nextDouble() - 0.5) * 0.5;
            Vec3 direction = Vec3.directionFromRotation(-55f, this.yBodyRot).yRot((float) spread);
            level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 0,
                    direction.x, direction.y, direction.z, 0.25 + this.random.nextDouble() * 0.2);
        }
        emitRing(level, 2.5 * strength, clanDust(1.6f));
        emitRing(level, 5.0 * strength, clanDust(1.3f));
        emitWave(level, 6.0 * strength, ParticleTypes.CLOUD, 0.25);
    }

    private void summonGuard(ServerLevel level) {
        List<LionEntity> responders = new ArrayList<>(distressResponders());
        if (responders.isEmpty()) return;
        responders.sort(Comparator.comparingDouble(this::distanceToSqr));

        guardedTicks = OWAttacksConstants.Lion.GUARD_TICKS;
        float offset = this.random.nextFloat() * 360f;
        LionEntity roarer = responders.stream().filter(LionEntity::isMale).findFirst().orElse(null);

        for (int i = 0; i < responders.size(); i++) {
            LionEntity guard = responders.get(i);
            guard.beginGuard(this, i, responders.size(), offset);
            if (guard == roarer) guard.guardRoarTicks = OWAttacksConstants.Lion.GUARD_ROAR_DEADLINE_TICKS;
            level.sendParticles(clanDust(1.4f), guard.getX(), guard.getY() + guard.getBbHeight() * 0.6, guard.getZ(),
                    12, 0.4, 0.4, 0.4, 0.0);
        }
    }

    private void beginGuard(LionEntity leader, int slot, int slotCount, float offset) {
        guardLeaderId = leader.getId();
        guardTicks = OWAttacksConstants.Lion.GUARD_TICKS;
        guardSlot = slot;
        guardSlotCount = Math.max(1, slotCount);
        guardSlotOffset = offset;
        guardWasSitting = this.isSitting();
        if (guardWasSitting) this.setSitting(false);
        if (this.getTarget() != null && !isGuardThreat(this.getTarget(), leader)) this.setTarget(null);

        this.entityData.set(RALLY_TICK, OWAttacksConstants.Lion.GUARD_TICKS);
        this.entityData.set(RESPOND_TICK, OWAttacksConstants.Lion.RESPOND_TICKS);
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, OWAttacksConstants.Lion.GUARD_SPEED_TICKS, 1, false, false));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 0.9f, (float) OWUtils.generateRandomInterval(1.2, 1.4) * (this.isMale() ? 0.8f : 1f));
    }

    private void endGuard() {
        guardTicks = 0;
        guardLeaderId = -1;
        guardRoarTicks = 0;
        this.entityData.set(GUARD_POSE, false);
        this.entityData.set(RALLY_TICK, 0);
        if (guardWasSitting && this.isAlive()) {
            this.getNavigation().stop();
            this.setSitting(true);
        }
        guardWasSitting = false;
    }

    public boolean isGuarding() {
        return guardTicks > 0 && guardLeaderId >= 0;
    }

    public boolean isGuardPosed() {
        return this.entityData.get(GUARD_POSE);
    }

    @Nullable
    private LionEntity getGuardLeader() {
        if (guardLeaderId < 0) return null;
        return this.level().getEntity(guardLeaderId) instanceof LionEntity leader && leader.isAlive() ? leader : null;
    }

    private Vec3 guardSlotPosition(LionEntity leader) {
        double angle = Math.toRadians(guardSlotOffset + guardSlot * 360.0 / guardSlotCount);
        double radius = OWAttacksConstants.Lion.GUARD_RING_RADIUS + leader.getBbWidth() * 0.5 + this.getBbWidth() * 0.3;
        return new Vec3(leader.getX() + Math.cos(angle) * radius, leader.getY(), leader.getZ() + Math.sin(angle) * radius);
    }

    private void tickGuard(ServerLevel level) {
        if (!isGuarding()) return;
        LionEntity leader = getGuardLeader();
        if (--guardTicks <= 0 || leader == null || this.isVehicle() || this.isKnockedOut()) {
            endGuard();
            return;
        }

        if (guardRoarTicks > 0) {
            guardRoarTicks--;
            boolean close = this.distanceTo(leader) <= OWAttacksConstants.Lion.GUARD_ROAR_TRIGGER_DISTANCE;
            if ((close || guardRoarTicks == 0) && !isRoaring() && this.onGround()) {
                guardRoarTicks = 0;
                startGuardRoar();
            }
        }

        if (this.tickCount % OWAttacksConstants.Lion.GUARD_SCAN_INTERVAL != 0) return;

        LivingEntity target = this.getTarget();
        if (target != null && (!target.isAlive() || target.distanceTo(leader) > OWAttacksConstants.Lion.GUARD_LEASH_RADIUS)) {
            this.setTarget(null);
            target = null;
        }
        if (target == null && !isRoaring()) {
            LivingEntity threat = pickGuardThreat(leader);
            if (threat != null) this.setTarget(threat);
        }
    }

    @Nullable
    private LivingEntity pickGuardThreat(LionEntity leader) {
        double radius = OWAttacksConstants.Lion.GUARD_THREAT_RADIUS;
        List<LivingEntity> threats = this.level().getEntitiesOfClass(LivingEntity.class, leader.getBoundingBox().inflate(radius),
                entity -> entity.distanceTo(leader) <= radius && isGuardThreat(entity, leader));
        if (threats.isEmpty()) return null;

        List<LionEntity> guards = this.level().getEntitiesOfClass(LionEntity.class, leader.getBoundingBox().inflate(OWAttacksConstants.Lion.GUARD_LEASH_RADIUS),
                lion -> lion != this && lion.guardLeaderId == leader.getId() && lion.getTarget() != null);
        return threats.stream()
                .min(Comparator.comparingDouble(threat -> this.distanceToSqr(threat)
                        + 64.0 * guards.stream().filter(lion -> lion.getTarget() == threat).count()))
                .orElse(null);
    }

    private boolean isGuardThreat(LivingEntity entity, LionEntity leader) {
        if (entity == leader || entity == this || !entity.isAlive() || entity instanceof ArmorStand) return false;
        if (leader.isClanmate(entity) || entity == leader.getOwner() || leader.isAlliedTo(entity)) return false;
        if (entity instanceof LionEntity lion && lion.guardLeaderId == leader.getId()) return false;
        if (entity instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null
                && tamable.getOwnerUUID().equals(leader.getOwnerUUID())) return false;
        if (entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
        if (entity instanceof Enemy) return true;
        if (entity == leader.getLastHurtByMob() && leader.tickCount - leader.getLastHurtByMobTimestamp() < 200) return true;
        return entity instanceof Mob mob && mob.getTarget() != null && isProtected(mob.getTarget(), leader);
    }

    private static boolean isProtected(LivingEntity entity, LionEntity leader) {
        return entity == leader || entity == leader.getOwner() || leader.isClanmate(entity)
                || entity.getRootVehicle() == leader;
    }

    private void startGuardRoar() {
        guardRoar = true;
        this.getNavigation().stop();
        this.entityData.set(ROAR_TICK, OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_BREATHE,
                SoundSource.NEUTRAL, 1.6f, 0.5f);
    }

    private void guardRoarShout(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 3.0f, this.isMale() ? 0.8f : 1.0f);
        OWUtils.spawnServerParticles(this, ParticleTypes.FLASH, 0, 0.5, 0, 1, 0);
        Vec3 mouth = mouthPosition();
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        for (int i = 0; i < 18; i++) {
            double spread = (this.random.nextDouble() - 0.5) * 0.6;
            Vec3 direction = forward.yRot((float) spread).add(0, 0.05 + this.random.nextDouble() * 0.15, 0);
            level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 0,
                    direction.x, direction.y, direction.z, 0.35 + this.random.nextDouble() * 0.25);
        }

        LionEntity leader = getGuardLeader();
        LionEntity center = leader != null ? leader : this;
        double radius = OWAttacksConstants.Lion.FEAR_RADIUS;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(radius).minmax(center.getBoundingBox().inflate(radius)),
                mob -> mob.isAlive() && (mob.distanceTo(this) <= radius || mob.distanceTo(center) <= radius)
                        && isFearTarget(mob, center))) {
            frightened.put(mob.getId(), OWAttacksConstants.Lion.FEAR_TICKS);
            mob.setTarget(null);
            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, OWAttacksConstants.Lion.FEAR_TICKS, 0, false, true));
            fleeFrom(mob, center);
            level.sendParticles(ParticleTypes.SPLASH, mob.getX(), mob.getY() + mob.getBbHeight() + 0.2, mob.getZ(),
                    8, 0.25, 0.1, 0.25, 0.1);
        }
    }

    private boolean isFearTarget(Mob mob, LionEntity center) {
        if (mob == this || mob == center || center.isClanmate(mob) || center.isAlliedTo(mob)) return false;
        if (mob instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null
                && tamable.getOwnerUUID().equals(center.getOwnerUUID())) return false;
        return mob instanceof Enemy || (mob.getTarget() != null && isProtected(mob.getTarget(), center));
    }

    private void fleeFrom(Mob mob, LionEntity center) {
        Vec3 away = new Vec3(mob.getX() - center.getX(), 0, mob.getZ() - center.getZ());
        if (away.lengthSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, this.random.nextFloat() * 360f);
        Vec3 point = mob.position().add(away.normalize().scale(OWAttacksConstants.Lion.FEAR_FLEE_DISTANCE));
        mob.getNavigation().moveTo(point.x, point.y, point.z, 1.4);
    }

    private void tickFrightened(ServerLevel level) {
        if (frightened.isEmpty() || this.tickCount % 10 != 0) return;
        LionEntity leader = getGuardLeader();
        LionEntity center = leader != null ? leader : this;
        frightened.entrySet().removeIf(entry -> {
            int left = entry.getValue() - 10;
            if (left <= 0 || !(level.getEntity(entry.getKey()) instanceof Mob mob) || !mob.isAlive()) return true;
            entry.setValue(left);
            mob.setTarget(null);
            fleeFrom(mob, center);
            return false;
        });
    }

    private void rejectAttack(int attackId) {
        if (this.getControllingPassenger() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OWAttackRejectedPacket(this.getId(), attackId));
        }
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.52 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        float cling = clingAmount();
        if (cling > 0f) {
            double rad = Math.toRadians(this.yBodyRot);
            double scale = this.getScale();
            double tilt = CLING_TILT * cling;
            double back = SEAT_BACK_OFFSET * scale;
            double standing = getBaseRiderYOffset() + getRiderAnimYOffset();
            double clingY = CLING_PIVOT * scale + Math.cos(tilt) * back - SEAT_DROP;
            double outward = -Math.sin(tilt) * back;
            passenger.fallDistance = 0f;
            function.accept(passenger,
                    this.getX() - Math.sin(rad) * outward,
                    this.getY() + Mth.lerp(cling, standing, clingY),
                    this.getZ() + Math.cos(rad) * outward);
            return;
        }

        Vec3 seatOffset = new Vec3(0, 0, 0).yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setGender(this.random.nextInt(MALE_ONE_IN) == 0 ? 1 : 0);
            if (this.isFemale()) this.setScale(this.getScale() * FEMALE_SCALE);
            applySexAttributes();

            this.setVariant(this.random.nextInt(100) < ALBINO_PERCENT ? LionVariant.ALBINO : LionVariant.DEFAULT);
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return data;
    }

    private void applySexAttributes() {
        double health = this.isMale() ? MALE_HEALTH : FEMALE_HEALTH;
        double damage = this.isMale() ? MALE_DAMAGE : FEMALE_DAMAGE;
        double speed = this.isMale() ? MALE_SPEED : FEMALE_SPEED;

        this.setRandomAttributes(this, health, damage, speed);
        this.setBaseHealth((float) health * 1.3f);
        this.setBaseDamage((float) damage);
        this.setBaseSpeed((float) speed);
        this.setHealth(this.getMaxHealth());
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(true);
        createSitAnimation(true);

        this.roarAnimationState.animateWhen(isRoaring(), this.tickCount);
        this.callAnimationState.animateWhen(isCalling(), this.tickCount);
        this.arrivalAnimationState.animateWhen(getArrivalTick() > 0, this.tickCount);
        this.respondAnimationState.animateWhen(getRespondTick() > 0, this.tickCount);
        this.leapAnimationState.animateWhen(getLatchPhase() == LATCH_LEAP, this.tickCount);
        this.releaseAnimationState.animateWhen(getLatchPhase() == LATCH_RELEASE, this.tickCount);
        this.distressAnimationState.animateWhen(isDistressing(), this.tickCount);

        int bite = this.entityData.get(BITE_TICK);
        if (bite > lastBiteSignal) this.biteAnimationState.start(this.tickCount);
        lastBiteSignal = bite;

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (33 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (29 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (30 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof LionEntity lion) {
            lion.setVariant(LionVariant.byId(variant));
            lion.setInitialVariant(LionVariant.byId(variant));
        }
    }

    public LionVariant getVariant() {
        return LionVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(LionVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(LionVariant skin) {
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

    public LionVariant getInitialVariant() {
        return LionVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(LionVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public void setMadByRider(boolean isMad) {
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD) || this.isTamingFury(); }

    public int getRoarTick() { return this.entityData.get(ROAR_TICK); }

    public boolean isRoaring() { return getRoarTick() > 0; }

    public int getCallTick() { return this.entityData.get(CALL_TICK); }

    public boolean isCalling() { return getCallTick() > 0; }

    public int getArrivalTick() { return this.entityData.get(ARRIVAL_TICK); }

    public int getRespondTick() { return this.entityData.get(RESPOND_TICK); }

    public boolean isRallied() { return this.entityData.get(RALLY_TICK) > 0; }

    public boolean isEmpowered() { return this.entityData.get(EMPOWER_TICK) > 0; }

    @Nullable
    public UUID getClanId() { return this.entityData.get(CLAN_ID).orElse(null); }

    public boolean hasClan() { return getClanId() != null; }

    public int getClanColor() { return this.entityData.get(CLAN_COLOR); }

    public int getClanSize() { return this.entityData.get(CLAN_SIZE); }

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
        if (getClanId() != null) tag.putUUID("clanId", getClanId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) setUltimateKillCount(tag.getInt("ultimateKillCount"));
        this.entityData.set(CLAN_ID, tag.hasUUID("clanId") ? Optional.of(tag.getUUID("clanId")) : Optional.empty());
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    class LionMeleeAttackGoal extends MeleeAttackGoal {

        public LionMeleeAttackGoal() {
            super(LionEntity.this, 7, true);
        }

        @Override
        public boolean canUse() {
            if (LionEntity.this.isSedatedFleeing() || LionEntity.this.isRoaring()) return false;
            if (LionEntity.this.getLatchPhase() != LATCH_NONE || LionEntity.this.isDistressing()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (LionEntity.this.isSedatedFleeing() || LionEntity.this.isRoaring()) return false;
            if (LionEntity.this.getLatchPhase() != LATCH_NONE || LionEntity.this.isDistressing()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            LionEntity.this.setMad(true);
            LionEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            LionEntity.this.setMad(false);
            LionEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2.5;
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

    class LionessStayWithPrideGoal extends Goal {

        private LionEntity male;
        private int repathCooldown;

        public LionessStayWithPrideGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private double radius() {
            return LionEntity.this.isTame()
                    ? OWAttacksConstants.Lion.CLAN_FIDELITY_RADIUS_TAMED
                    : OWAttacksConstants.Lion.CLAN_FIDELITY_RADIUS_WILD;
        }

        private boolean isFree() {
            LionEntity lioness = LionEntity.this;
            return lioness.isFemale() && !lioness.isBaby() && lioness.hasClan()
                    && lioness.getTarget() == null && !lioness.isSitting() && !lioness.isVehicle()
                    && !lioness.isKnockedOut() && !lioness.isSleeping() && !lioness.isNapping()
                    && !lioness.isLeashed() && (!lioness.isTame() || !lioness.isFollowingOwner());
        }

        @Override
        public boolean canUse() {
            if ((LionEntity.this.tickCount + LionEntity.this.getId()) % 20 != 0) return false;
            if (!isFree()) return false;
            this.male = LionEntity.this.findClanMale();
            return this.male != null && LionEntity.this.distanceToSqr(this.male) > radius() * radius();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.male == null || !this.male.isAlive() || !isFree()) return false;
            double settle = radius() * 0.5;
            return LionEntity.this.distanceToSqr(this.male) > settle * settle;
        }

        @Override
        public void start() {
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            if (--this.repathCooldown > 0 || this.male == null) return;
            this.repathCooldown = this.adjustedTickDelay(10);
            LionEntity.this.getNavigation().moveTo(this.male, 1.0D);
        }

        @Override
        public void stop() {
            this.male = null;
            LionEntity.this.getNavigation().stop();
        }
    }

    class LionGuardGoal extends Goal {

        private int repathCooldown;

        public LionGuardGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean active() {
            LionEntity lion = LionEntity.this;
            return lion.isGuarding() && lion.getTarget() == null && !lion.isVehicle()
                    && !lion.isKnockedOut() && !lion.isRoaring();
        }

        @Override
        public boolean canUse() {
            return active();
        }

        @Override
        public boolean canContinueToUse() {
            return active();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            LionEntity lion = LionEntity.this;
            LionEntity leader = lion.getGuardLeader();
            if (leader == null) return;

            Vec3 slot = lion.guardSlotPosition(leader);
            double dx = slot.x - lion.getX();
            double dz = slot.z - lion.getZ();
            double distanceSq = dx * dx + dz * dz;

            if (distanceSq > 1.96) {
                lion.entityData.set(GUARD_POSE, false);
                boolean far = distanceSq > 36.0;
                lion.setRunning(far);
                if (--this.repathCooldown <= 0) {
                    this.repathCooldown = this.adjustedTickDelay(8);
                    lion.getNavigation().moveTo(slot.x, slot.y, slot.z, far ? 1.8 : 1.0);
                }
                return;
            }

            lion.getNavigation().stop();
            lion.setRunning(false);
            double ox = slot.x - leader.getX();
            double oz = slot.z - leader.getZ();
            lion.getLookControl().setLookAt(slot.x + ox * 2.0, lion.getEyeY(), slot.z + oz * 2.0);
            float yaw = (float) (Mth.atan2(oz, ox) * Mth.RAD_TO_DEG) - 90f;
            lion.setYRot(Mth.approachDegrees(lion.getYRot(), yaw, 12f));
            lion.yBodyRot = lion.getYRot();
            lion.entityData.set(GUARD_POSE, true);
        }

        @Override
        public void stop() {
            LionEntity.this.entityData.set(GUARD_POSE, false);
            LionEntity.this.setRunning(false);
            LionEntity.this.getNavigation().stop();
        }
    }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(18, 2.4f, 7.8f, 1.0f)
    );

    @Override
    public List<OWSedationImpact> sedationImpacts() {
        return SEDATION_IMPACTS;
    }
}
