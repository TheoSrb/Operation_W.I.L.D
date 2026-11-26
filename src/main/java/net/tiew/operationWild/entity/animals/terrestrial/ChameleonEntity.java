package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.IOWCarriedCompanion;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWPanicGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.ChameleonVariant;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class ChameleonEntity extends OWEntity implements IOWEntity, IOWTamable, IOWCarriedCompanion {

    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 20.0;
    public static final int MAX_SOMNOLENCE = 80;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 2;
    public static final int ENTITY_COLOR = 0x92a846;
    public static final int DEFAULT_SKIN_INDEX = 0;

    public static final int TONGUE_IDLE = 0;
    public static final int TONGUE_EXTENDING = 1;
    public static final int TONGUE_ATTACHED = 2;
    public static final int TONGUE_RETRACTING = 3;

    public static final int PERCH_BACK = 0;
    public static final int PERCH_HEAD = 1;

    public static final float PERCH_BLEND_STEP = 1f / 6f;
    public static final float CROUCH_BLEND = 0.35f;

    private static final int SEDATION_WAKE_TICKS = 40;
    private static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 30;
    private static final int FOOD_WANTED_MIN = 3;
    private static final int FOOD_WANTED_MAX = 6;
    private static final float MAX_VITAL_ENERGY = 250f;

    private static final double SHOULDER_HEIGHT = 1.406;
    private static final double SHOULDER_HEIGHT_CROUCH = 1.094;
    private static final double NECK_HEIGHT_CROUCH = 1.035;
    private static final double BACK_DROP = 0.30;
    private static final double BACK_SURFACE = 0.133;
    private static final double BACK_GAP = -0.07;
    private static final double HEAD_TOP = 0.49;
    private static final double HEAD_FORWARD = 0.0;
    private static final float CROUCH_LEAN = 0.5f;
    private static final float HEAD_PITCH_MIN = -45f;
    private static final float HEAD_PITCH_MAX = 60f;
    private static final double PERCH_ARC_LIFT = 0.25;
    private static final double HEAD_PIVOT_UP = 5.0 / 16.0;
    private static final double HEAD_PIVOT_FORWARD = 5.0 / 16.0;
    private static final double MOUTH_UP = 0.28;
    private static final double MOUTH_FORWARD = 0.625;

    public static final int CLIMB_TICKS = 12;
    private static final double HOP_ARC = 0.45;
    private static final double HOP_DISTANCE = 1.1;
    private static final float AIM_RESPONSE = 0.45f;
    private static final float TONGUE_HOLD_RESPONSE = 0.3f;
    private static final float AIM_BACK_YAW_LIMIT = 1.4f;
    private static final float AIM_BODY_PITCH_LIMIT = 1.25f;
    private static final float AIM_HEAD_LIMIT = 1.2f;

    private static final int SHOT_NONE = 0;
    private static final int SHOT_AIMED = 1;
    private static final int SHOT_PASSIVE = 2;
    private static final double CLIMB_ARC = 0.55;
    private static final int CARRY_TOGGLE_COOLDOWN = 10;
    private static final double HUNTER_RELEASE_RANGE = 24.0;
    private static final int NAP_COOLDOWN_AFTER_DISMOUNT = 200;
    private static final int CARRIER_RESTORE_TIMEOUT = 600;
    private static final double CARRIER_RESTORE_RANGE = 8.0;
    private static final int MISC_IDLE_DURATION = 60;

    private static final double ITEM_GRAB_LIFT = 0.25;
    private static final double WALL_PROBE = 0.12;
    private static final double WALL_PROBE_DEFLATE = 0.05;
    private static final double WALL_STICK = 0.04;
    private static final double WALL_FACING_DOT = 0.6;
    private static final double WALL_DESCENT_INTENT = 0.05;
    private static final double WALL_MANTLE_REACH = 0.5;
    private static final double WALL_MANTLE_PUSH = 0.1;
    private static final double WALL_BEYOND_DOT = 0.3;
    private static final double WALL_SLIDE_DAMPING = 0.6;
    private static final double WALL_CLIMB_UP_SPEED = 0.07;
    private static final double WALL_CLIMB_DOWN_SPEED = 0.07;
    private static final double WALL_TREND_SPEED = 0.02;
    private static final int WALL_TREND_LIMIT = 4;
    private static final int WALL_TREND_FLIP = 3;
    private static final float WALL_TURN_STEP = 20f;
    private static final float WALL_PITCH_RESPONSE = 0.28f;
    private static final int WALL_MAX_FALL_DISTANCE = 12;
    private static final int WILD_CAMOUFLAGE_MIN_TICKS = 300;
    private static final int WILD_CAMOUFLAGE_MAX_TICKS = 700;
    private static final int WILD_CAMOUFLAGE_COOLDOWN_MIN = 400;
    private static final int WILD_CAMOUFLAGE_COOLDOWN_MAX = 1200;
    private static final int WILD_CAMOUFLAGE_CHANCE = 40;
    private static final float WILD_CAMOUFLAGE_VOLUME = 0.6f;
    private static final Vector3f[] CAMOUFLAGE_PARTICLE_COLORS = {
            new Vector3f(0.57f, 0.66f, 0.27f), new Vector3f(0.85f, 0.78f, 0.29f), new Vector3f(0.25f, 0.68f, 0.60f)};
    private static final float CAMOUFLAGE_PARTICLE_SIZE = 0.9f;
    private static final int CAMOUFLAGE_PARTICLES_PER_COLOR = 7;
    private static final int CAMOUFLAGE_SPARKLES = 6;
    private static final int OUTCOME_MISS = 0;
    private static final int OUTCOME_ENTITY = 1;
    private static final int OUTCOME_ITEM = 2;
    private static final int OUTCOME_BLOCK = 3;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PERCH = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIMB_TIMER = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMB_MOUNTING = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TONGUE_CATCH_TIMER = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TONGUE_STATE = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TONGUE_LENGTH = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TONGUE_PHASE_TICK = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TONGUE_PHASE_TICKS = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> TONGUE_END = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> TONGUE_TARGET = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> TONGUE_ITEM = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> TONGUE_SHOT_TIMER = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WATCH_TARGET = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MIMICRY_TIMER = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MIMICRY_CAST_TIMER = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WALL_FACE = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WALL_DESCENDING = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.BOOLEAN);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState miscIdleAnimationState = new AnimationState();
    public final AnimationState backIdleAnimationState = new AnimationState();
    public final AnimationState headIdleAnimationState = new AnimationState();
    public final AnimationState transitionBackHeadAnimationState = new AnimationState();
    public final AnimationState transitionHeadBackAnimationState = new AnimationState();
    public final AnimationState tongueShotAnimationState = new AnimationState();
    public final AnimationState tongueCatchAnimationState = new AnimationState();
    public final AnimationState climbOnAnimationState = new AnimationState();
    public final AnimationState jumpOffAnimationState = new AnimationState();
    public final AnimationState tongueHoldAnimationState = new AnimationState();
    public final AnimationState camouflageCastAnimationState = new AnimationState();

    public int miscIdleAnimationStartTime = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public float perchBlend = 0f;
    public float perchBlendO = 0f;
    public float crouchAmount = 0f;
    public float crouchAmountO = 0f;

    private int lastPerch = PERCH_BACK;
    private int lastTongueShotTimer = 0;
    private int lastTongueCatchTimer = 0;
    private int lastClimbTimer = 0;
    public float aimWeight = 0f;
    public float aimWeightO = 0f;
    public float tongueLock = 0f;
    public float tongueLockO = 0f;
    public float tongueHoldWeight = 0f;
    public float tongueHoldWeightO = 0f;
    private @Nullable Vec3 lastAimPoint = null;
    public @Nullable float[] aimOverride = null;
    private int lastMimicryCastTimer = 0;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);

    private int carryCooldown = 0;
    private int napCooldown = 0;
    private Vec3 climbStart = null;
    private Vec3 hopTarget = null;
    private float hopYaw = 0f;
    private int climbCarrierId = -1;
    private UUID pendingCarrier = null;
    private int pendingCarrierTicks = 0;

    private int tongueCooldown = 0;
    private int tongueOutcome = OUTCOME_MISS;
    private boolean tongueAimed = false;
    private boolean retractOnAttach = false;
    private int tongueItemEntityId = -1;
    private BlockPos tongueAnchorBlock = null;
    private net.minecraft.core.Direction tongueAnchorFace = null;
    private int vineTicks = 0;
    private int perchReturnDelay = 0;
    private int pendingShot = SHOT_NONE;
    private int pendingShotTarget = -1;
    private int shotWindup = -1;

    private int watchScanTimer = 0;
    private int wallTrend = 0;
    private boolean wildCamouflaging = false;
    private int wildCamouflageCooldown = (int) OWUtils.generateRandomInterval(WILD_CAMOUFLAGE_COOLDOWN_MIN / 2.0, WILD_CAMOUFLAGE_COOLDOWN_MAX / 2.0);
    public float wallPitch = 0f;
    public float wallPitchO = 0f;
    public int lastWallFace = -1;

    public ChameleonEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.13D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OWPanicGoal(this, 2.4f, 1.0, 0) {
            @Override
            protected boolean shouldPanic() {
                return !ChameleonEntity.this.isTame() && ChameleonEntity.this.getLastHurtByMob() != null;
            }
        });
        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true, true));
        this.goalSelector.addGoal(6, new WildCamouflageGoal());
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(PERCH, PERCH_BACK);
        builder.define(CLIMB_TIMER, 0);
        builder.define(CLIMB_MOUNTING, true);
        builder.define(TONGUE_CATCH_TIMER, 0);
        builder.define(TONGUE_STATE, TONGUE_IDLE);
        builder.define(TONGUE_LENGTH, 0f);
        builder.define(TONGUE_PHASE_TICK, 0);
        builder.define(TONGUE_PHASE_TICKS, 1);
        builder.define(TONGUE_END, new Vector3f());
        builder.define(TONGUE_TARGET, -1);
        builder.define(TONGUE_ITEM, ItemStack.EMPTY);
        builder.define(TONGUE_SHOT_TIMER, 0);
        builder.define(WATCH_TARGET, -1);
        builder.define(MIMICRY_TIMER, 0);
        builder.define(MIMICRY_CAST_TIMER, 0);
        builder.define(WALL_FACE, -1);
        builder.define(WALL_DESCENDING, false);
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
        return 2f;
    }

    @Override
    public double napParticleHeight() { return 0.1; }

    @Override
    public double napParticleForward() { return 0.45; }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public boolean hasStandardHeadPose() {
        return !this.isClimbing() && !this.isCarried();
    }

    @Override
    public boolean usesAggressiveTaming() {
        return true;
    }

    @Override
    public List<EntityType<?>> getValidTamingProjectiles() {
        return List.of(OWEntityRegistry.TRANQUILIZER_ARROW.get(), OWEntityRegistry.TRANQUILIZER_WOODEN_STINGER.get(),
                OWEntityRegistry.SLINGSHOT_PROJECTILE.get(), OWEntityRegistry.TRANQUILIZER_DART.get());
    }

    @Override
    public boolean hasSedationAnimations() {
        return true;
    }

    @Override
    public int sedationWakeTicks() {
        return SEDATION_WAKE_TICKS;
    }

    @Override
    public float sedatedFleeSpeed() {
        return 2.2f;
    }

    @Override
    public int blockBreakLevel() {
        return OWBreakLevel.NONE;
    }

    @Override
    public int asleepSomnolenceLossInterval() {
        return SOMNOLENCE_LOSS_INTERVAL_ASLEEP;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.SCOUT;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.CARNIVOROUS;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.PASSIVE;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.CHAMELEON_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return MAX_VITAL_ENERGY * (1f + this.getLevel() / 100f);
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.225f * (1 + ((float) this.getLevel() / 50));
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
        return 0.2f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.CHAMELEON.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CHAMELEON_FOOD);
    }

    @Override
    public Item favoriteFoodTier2() {
        return Items.SALMON;
    }

    @Override
    public Item favoriteFoodTier3() {
        return Items.SPIDER_EYE;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping() || isCarried()) return null;
        return net.tiew.operationWild.sound.OWSounds.CHAMELEON_AMBIENT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return net.tiew.operationWild.sound.OWSounds.CHAMELEON_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return net.tiew.operationWild.sound.OWSounds.CHAMELEON_DEATH.get();
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.isPassenger()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 200L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        SoundType soundtype = blockState.getSoundType(this.level(), this.blockPosition(), this);
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundtype.getStepSound(),
                this.getSoundSource(), soundtype.getVolume() * 0.06F, soundtype.getPitch() * pitchMod, false);
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(1.5f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.7f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        if (carryCooldown > 0) carryCooldown--;
        if (napCooldown > 0) napCooldown--;
        if (tongueCooldown > 0) tongueCooldown--;

        setTamingPercentage(this.foodGiven, this.foodWanted);

        tickPerchBlend();
        tickWallPitch();

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            if (getTongueShotTimer() > 0) this.entityData.set(TONGUE_SHOT_TIMER, getTongueShotTimer() - 1);
            if (getTongueCatchTimer() > 0) this.entityData.set(TONGUE_CATCH_TIMER, getTongueCatchTimer() - 1);

            handleCarrierClimb();
            handleCarrierRestore();
            handlePendingShot();
            handleTongue();
            handlePerchReturn();
            handleWatch();
            handleMimicry();
            handleWallClimb();
            if (wildCamouflageCooldown > 0) wildCamouflageCooldown--;

            if (isCarried() && (!this.isTame() || this.isInResurrection())) dismountFromCarrier();
        }
    }

    private void tickPerchBlend() {
        this.perchBlendO = this.perchBlend;
        float target = getPerch() == PERCH_HEAD ? 1f : 0f;
        if (this.perchBlend < target) this.perchBlend = Math.min(target, this.perchBlend + PERCH_BLEND_STEP);
        else if (this.perchBlend > target) this.perchBlend = Math.max(target, this.perchBlend - PERCH_BLEND_STEP);
        this.aimWeightO = this.aimWeight;
        float aimTarget = isTongueOut() || getTongueShotTimer() > 0 ? 1f : 0f;
        this.aimWeight += (aimTarget - this.aimWeight) * AIM_RESPONSE;
        this.tongueLockO = this.tongueLock;
        this.tongueLock += ((isTongueOut() ? 1f : 0f) - this.tongueLock) * AIM_RESPONSE;
        this.tongueHoldWeightO = this.tongueHoldWeight;
        float holdTarget = getTongueState() == TONGUE_ATTACHED ? 1f : 0f;
        this.tongueHoldWeight += (holdTarget - this.tongueHoldWeight) * TONGUE_HOLD_RESPONSE;
        if (this.tongueHoldWeight < 0.005f && holdTarget == 0f) this.tongueHoldWeight = 0f;
        if (this.aimWeight < 0.005f && aimTarget == 0f) this.aimWeight = 0f;
    }

    @Override
    public boolean requiresSaddleToRide() {
        return false;
    }

    @Override
    public Item acceptSaddle() {
        return Items.AIR;
    }

    @Override
    protected boolean pausesEnergyRecovery() {
        return isTongueOut() || pendingShot != SHOT_NONE;
    }

    @Override
    protected double followOwnerSpeedFactor() {
        return 12;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide() || this.isKnockedOut()) return super.mobInteract(player, hand);
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);

        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame() || this.isBaby() || this.isInResurrection()) return super.mobInteract(player, hand);
        if (isFood(stack) && this.getHealth() < this.getMaxHealth()) return super.mobInteract(player, hand);

        if (isCarried()) {
            if (!player.isSteppingCarefully() || this.getVehicle() != player) return InteractionResult.PASS;
            dismountFromCarrier();
            return InteractionResult.SUCCESS;
        }

        if (player.isSteppingCarefully()) return super.mobInteract(player, hand);
        if (carryCooldown > 0 || isClimbing()) return InteractionResult.PASS;
        if (!this.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) return InteractionResult.PASS;
        if (!player.getPassengers().isEmpty()) return InteractionResult.PASS;

        if (isNapping() || isSleeping()) {
            setNap(false);
            setSleeping(false);
        }

        return beginClimb(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean beginClimb(Player carrier) {
        this.setSitting(false);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
        this.climbStart = this.position();
        this.climbCarrierId = carrier.getId();
        this.entityData.set(CLIMB_MOUNTING, true);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);
        carryCooldown = CARRY_TOGGLE_COOLDOWN + CLIMB_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_HOP.get(), SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(0.95, 1.1));
        return true;
    }

    private void handleCarrierClimb() {
        int remaining = this.entityData.get(CLIMB_TIMER);
        if (remaining <= 0) return;

        boolean mounting = this.entityData.get(CLIMB_MOUNTING);
        Player carrier = this.level().getEntity(climbCarrierId) instanceof Player player && player.isAlive() ? player : null;
        if (mounting && carrier == null) {
            abortClimb();
            return;
        }

        this.entityData.set(CLIMB_TIMER, --remaining);
        float progress = 1f - remaining / (float) CLIMB_TICKS;
        Vec3 from = climbStart == null ? this.position() : climbStart;
        Vec3 to = mounting
                ? perchPose(carrier, 0f, carrier.isCrouching() ? 1f : 0f, 1f).position()
                : (hopTarget == null ? this.position() : hopTarget);

        double eased = progress * progress * (3 - 2 * progress);
        double arc = (mounting ? CLIMB_ARC : HOP_ARC) * 4.0 * progress * (1 - progress);
        this.setPos(from.lerp(to, eased).add(0, arc, 0));

        if (mounting) {
            double dx = carrier.getX() - this.getX(), dz = carrier.getZ() - this.getZ();
            float toward = dx * dx + dz * dz > 1.0e-4 ? (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f : this.getYRot();
            float settle = Mth.clamp((progress - 0.45f) / 0.55f, 0f, 1f);
            this.setYRot(Mth.rotLerp(settle, toward, carrier.yBodyRot));
        } else {
            this.setYRot(hopYaw + 180f * (float) eased);
        }
        this.yBodyRot = this.getYRot();
        this.setYHeadRot(this.getYRot());

        if (remaining > 0) return;

        abortClimb();
        if (!mounting) {
            this.setDeltaMovement(0, -0.1, 0);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.tiew.operationWild.sound.OWSounds.CHAMELEON_HOP.get(), SoundSource.NEUTRAL, 0.9f, 0.9f);
            return;
        }
        if (!carrier.getPassengers().isEmpty()) return;
        if (!this.startRiding(carrier, true)) return;

        this.entityData.set(PERCH, PERCH_BACK);
        this.perchBlend = 0f;
        this.perchBlendO = 0f;
        syncPassengersToCarrier(carrier);
        shakeOffHunters();
        this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.3, 1.5));
        this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_SCUTTLE.get(), SoundSource.NEUTRAL, 0.8f, 1.0f);
    }

    private void abortClimb() {
        this.entityData.set(CLIMB_TIMER, 0);
        this.noPhysics = false;
        this.climbStart = null;
        this.hopTarget = null;
        this.climbCarrierId = -1;
    }

    public float climbPitchDegrees(float partialTicks) {
        int timer = this.entityData.get(CLIMB_TIMER);
        if (timer <= 0) return 0f;
        float progress = Mth.clamp(1f - (timer - partialTicks) / CLIMB_TICKS, 0f, 1f);
        if (this.entityData.get(CLIMB_MOUNTING)) {
            float t = Mth.clamp((progress - 0.35f) / 0.65f, 0f, 1f);
            return -90f * t * t * (3f - 2f * t);
        }
        float t = Mth.clamp(progress / 0.6f, 0f, 1f);
        return -90f * (1f - t * t * (3f - 2f * t));
    }

    public boolean isClimbMounting() {
        return this.entityData.get(CLIMB_MOUNTING);
    }

    private void handleCarrierRestore() {
        if (pendingCarrier == null) return;

        if (isCarried() || ++pendingCarrierTicks > CARRIER_RESTORE_TIMEOUT) {
            pendingCarrier = null;
            pendingCarrierTicks = 0;
            return;
        }

        Player carrier = this.level().getPlayerByUUID(pendingCarrier);
        if (carrier == null || carrier.isRemoved()) return;
        if (this.distanceTo(carrier) > CARRIER_RESTORE_RANGE) return;
        if (!carrier.getPassengers().isEmpty()) return;

        pendingCarrier = null;
        pendingCarrierTicks = 0;
        if (this.startRiding(carrier, true)) {
            this.entityData.set(PERCH, PERCH_BACK);
            syncPassengersToCarrier(carrier);
            shakeOffHunters();
        }
    }

    @Override
    public void dismountFromCarrier() {
        Player carrier = getCarrier();
        releaseTongue(false);
        cancelMimicry();
        clearPendingShot();
        Vec3 start = carrier == null ? this.position()
                : perchPose(carrier, this.perchBlend, this.crouchAmount, 1f).position();
        this.stopRiding();
        this.entityData.set(PERCH, PERCH_BACK);
        carryCooldown = CARRY_TOGGLE_COOLDOWN + CLIMB_TICKS;
        napCooldown = NAP_COOLDOWN_AFTER_DISMOUNT;

        if (carrier == null) return;
        syncPassengersToCarrier(carrier);

        float yaw = carrier.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 behind = new Vec3(carrier.getX() + Mth.sin(yaw) * HOP_DISTANCE, carrier.getY(), carrier.getZ() - Mth.cos(yaw) * HOP_DISTANCE);
        boolean free = this.level().noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(behind));
        this.hopTarget = free ? behind : carrier.position();
        this.hopYaw = carrier.yBodyRot;
        this.climbStart = start;
        this.climbCarrierId = carrier.getId();
        this.setPos(start);
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
        this.entityData.set(CLIMB_MOUNTING, false);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_HOP.get(), SoundSource.NEUTRAL, 0.8f, 1.05f);
    }

    private static void syncPassengersToCarrier(Player carrier) {
        if (carrier instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(serverPlayer));
        }
    }

    @Override
    public boolean isCarried() {
        return this.getVehicle() instanceof Player;
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMB_TIMER) > 0;
    }

    public @Nullable Player getCarrier() {
        return this.getVehicle() instanceof Player player ? player : null;
    }

    @Override
    public boolean canStartNap() {
        return napCooldown <= 0 && !isClimbing() && !isCarried();
    }

    public record PerchPose(Vec3 position, org.joml.Quaternionf rotation, float yaw) {

        public Vec3 transform(double x, double y, double z) {
            org.joml.Vector3f v = rotation.transform(new org.joml.Vector3f((float) x, (float) y, (float) z));
            return position.add(v.x, v.y, v.z);
        }

        public Vec3 axis(double x, double y, double z) {
            org.joml.Vector3f v = rotation.transform(new org.joml.Vector3f((float) x, (float) y, (float) z));
            return new Vec3(v.x, v.y, v.z);
        }
    }

    public static float renderedBodyYaw(Player carrier, float partialTicks) {
        if (carrier.getVehicle() instanceof OWEntity mount) {
            return Mth.rotLerp(partialTicks, mount.yBodyRotO, mount.yBodyRot);
        }
        if (carrier.getVehicle() instanceof LivingEntity vehicle) {
            float head = Mth.rotLerp(partialTicks, carrier.yHeadRotO, carrier.yHeadRot);
            float offset = Mth.clamp(Mth.wrapDegrees(head - Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot)), -85f, 85f);
            float yaw = head - offset;
            if (offset * offset > 2500f) yaw += offset * 0.2f;
            return yaw;
        }
        return Mth.rotLerp(partialTicks, carrier.yBodyRotO, carrier.yBodyRot);
    }

    public static PerchPose perchPose(Player carrier, float blend, float crouch, float partialTicks) {
        float bodyYaw = renderedBodyYaw(carrier, partialTicks);
        float headYaw = Mth.rotLerp(partialTicks, carrier.yHeadRotO, carrier.yHeadRot);
        float lookPitch = Mth.lerp(partialTicks, carrier.xRotO, carrier.getXRot());
        float eased = blend * blend * (3f - 2f * blend);

        net.minecraft.core.Direction bed = carrier.isSleeping() ? carrier.getBedOrientation() : null;
        boolean spinning = bed == null && carrier.isAutoSpinAttack();
        float swim = carrier.getSwimAmount(partialTicks);
        float fly = 0f;
        if (carrier.isFallFlying()) {
            float flying = carrier.getFallFlyingTicks() + partialTicks;
            fly = Mth.clamp(flying * flying / 100f, 0f, 1f);
        }
        float bodyPitch = fly > 0f ? fly * (90f + lookPitch)
                : Mth.lerp(swim, 0f, carrier.isInWater() ? 90f + lookPitch : 90f);
        float lying = bed != null || spinning ? 1f : Math.max(swim, fly);
        if (bed != null) crouch = 0f;

        float lean = CROUCH_LEAN * crouch;
        double shoulder = Mth.lerp(crouch, SHOULDER_HEIGHT, SHOULDER_HEIGHT_CROUCH);
        Vec3 normal = new Vec3(0, Mth.sin(lean), -Mth.cos(lean));
        Vec3 backPosition = new Vec3(0, shoulder - BACK_DROP * Mth.cos(lean), -BACK_DROP * Mth.sin(lean))
                .add(normal.scale(BACK_SURFACE + BACK_GAP));
        org.joml.Quaternionf backRotation = com.mojang.math.Axis.XP.rotationDegrees(-90f + lean * Mth.RAD_TO_DEG);

        float relativeYaw = bed != null ? 0f : Mth.wrapDegrees(headYaw - bodyYaw);
        float headPitch = Mth.clamp(lookPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX) * (1f - lying);
        org.joml.Quaternionf headRotation = com.mojang.math.Axis.YP.rotationDegrees(-relativeYaw)
                .mul(com.mojang.math.Axis.XP.rotationDegrees(headPitch));
        double neck = Mth.lerp(crouch, SHOULDER_HEIGHT, NECK_HEIGHT_CROUCH);
        org.joml.Vector3f top = headRotation.transform(new org.joml.Vector3f(0f, (float) HEAD_TOP, (float) HEAD_FORWARD));
        Vec3 headPosition = new Vec3(top.x, neck + top.y, top.z);

        Vec3 local = backPosition.lerp(headPosition, eased).add(0, PERCH_ARC_LIFT * Mth.sin(eased * Mth.PI), 0);
        org.joml.Quaternionf localRotation = new org.joml.Quaternionf(backRotation).slerp(headRotation, eased);

        org.joml.Quaternionf frame;
        Vec3 offset = Vec3.ZERO;
        if (bed != null) {
            frame = com.mojang.math.Axis.YP.rotationDegrees(sleepRotation(bed))
                    .mul(com.mojang.math.Axis.ZP.rotationDegrees(90f))
                    .mul(com.mojang.math.Axis.YP.rotationDegrees(270f))
                    .mul(com.mojang.math.Axis.YP.rotationDegrees(180f));
            float eye = carrier.getEyeHeight(net.minecraft.world.entity.Pose.STANDING) - 0.1f;
            offset = new Vec3(-bed.getStepX() * eye, 0, -bed.getStepZ() * eye);
        } else {
            frame = com.mojang.math.Axis.YP.rotationDegrees(-bodyYaw);
            if (spinning) {
                frame.mul(com.mojang.math.Axis.XP.rotationDegrees(90f + carrier.getXRot()))
                        .mul(com.mojang.math.Axis.YP.rotationDegrees((carrier.tickCount + partialTicks) * -75f));
            } else {
                frame.mul(com.mojang.math.Axis.XP.rotationDegrees(bodyPitch));
                if (fly > 0f) frame.mul(com.mojang.math.Axis.YP.rotation(glideRoll(carrier, partialTicks)));
                if (carrier.isVisuallySwimming()) local = local.add(0, -1.0, -0.3);
            }
        }

        float scale = carrier.getScale();
        org.joml.Vector3f world = new org.joml.Quaternionf(frame)
                .transform(new org.joml.Vector3f((float) local.x * scale, (float) local.y * scale, (float) local.z * scale));
        org.joml.Quaternionf rotation = new org.joml.Quaternionf(frame).mul(localRotation);
        return new PerchPose(carrier.getPosition(partialTicks).add(offset).add(world.x, world.y, world.z), rotation,
                bodyYaw + relativeYaw * eased);
    }

    private static float sleepRotation(net.minecraft.core.Direction bed) {
        return switch (bed) {
            case SOUTH -> 90f;
            case NORTH -> 270f;
            case EAST -> 180f;
            default -> 0f;
        };
    }

    private static float glideRoll(Player carrier, float partialTicks) {
        Vec3 view = carrier.getViewVector(partialTicks);
        Vec3 velocity = new Vec3(carrier.getX() - carrier.xo, carrier.getY() - carrier.yo, carrier.getZ() - carrier.zo);
        double moving = velocity.horizontalDistanceSqr();
        double looking = view.horizontalDistanceSqr();
        if (moving <= 0.0 || looking <= 0.0) return 0f;
        double cosine = Mth.clamp((velocity.x * view.x + velocity.z * view.z) / Math.sqrt(moving * looking), -1.0, 1.0);
        double side = velocity.x * view.z - velocity.z * view.x;
        return (float) (Math.signum(side) * Math.acos(cosine));
    }

    public PerchPose currentPerchPose(Player carrier, float partialTicks) {
        return perchPose(carrier, Mth.lerp(partialTicks, perchBlendO, perchBlend),
                Mth.lerp(partialTicks, crouchAmountO, crouchAmount), partialTicks);
    }

    public Vec3 mouthPosition(float partialTicks) {
        Player carrier = getCarrier();
        double up = MOUTH_UP * this.getScale();
        double forward = MOUTH_FORWARD * this.getScale();
        if (carrier == null) {
            float yaw = Mth.rotLerp(partialTicks, this.yBodyRotO, this.yBodyRot) * Mth.DEG_TO_RAD;
            return this.getPosition(partialTicks).add(-Mth.sin(yaw) * forward, up, Mth.cos(yaw) * forward);
        }
        return currentPerchPose(carrier, partialTicks).transform(0, up, forward);
    }

    public static Vec3 ropePoint(Player player) {
        return player.position().add(0, player.getBbHeight() + 0.1, 0);
    }

    @Override
    public void rideTick() {
        super.rideTick();

        Player carrier = getCarrier();
        if (carrier == null) return;

        this.getNavigation().stop();

        this.crouchAmountO = this.crouchAmount;
        this.crouchAmount += ((carrier.isCrouching() ? 1f : 0f) - this.crouchAmount) * CROUCH_BLEND;

        PerchPose pose = perchPose(carrier, this.perchBlend, this.crouchAmount, 1f);
        this.setPos(pose.position());
        float yaw = pose.yaw();
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.fallDistance = 0f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (wildCamouflaging && !this.level().isClientSide() && isMimicryActive()) breakMimicry();
        if (isCarried() && isShieldedWhileCarried(damageSource)) return false;
        return super.hurt(damageSource, amount);
    }

    private boolean isShieldedWhileCarried(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) return false;
        return source.getDirectEntity() instanceof LivingEntity;
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        if (vehicle instanceof Player && !force) return false;
        return super.startRiding(vehicle, force);
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return vehicle instanceof Player || super.canRide(vehicle);
    }

    @Override
    public boolean isPushable() {
        return !isCarried() && super.isPushable();
    }

    @Override
    public boolean isPickable() {
        return !isCarried() && super.isPickable();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.isAlive() && !isCarried();
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !isCarried() && super.canBeSeenAsEnemy();
    }

    private void shakeOffHunters() {
        for (Mob hunter : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(HUNTER_RELEASE_RANGE))) {
            if (hunter.getTarget() == this) hunter.setTarget(null);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        releaseTongue(false);
        cancelMimicry();
        if (isCarried()) this.stopRiding();
        super.die(damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    public double tongueRange() {
        return OWPistePassives.has(this, OWPistePassives.LONG_TONGUE)
                ? OWAttacksConstants.Chameleon.TONGUE_RANGE_LONG_TONGUE
                : OWAttacksConstants.Chameleon.TONGUE_RANGE;
    }

    public double watchRadius() {
        return OWPistePassives.has(this, OWPistePassives.LONG_TONGUE)
                ? OWAttacksConstants.Chameleon.WATCH_RADIUS_LONG_TONGUE
                : OWAttacksConstants.Chameleon.WATCH_RADIUS;
    }

    public void useTongue() {
        if (this.level().isClientSide()) return;

        Player carrier = getCarrier();
        if (carrier == null || carrier.getVehicle() != null) return;

        int state = getTongueState();
        if (state == TONGUE_ATTACHED) {
            releaseTongue(true);
            return;
        }
        if (state != TONGUE_IDLE || tongueCooldown > 0 || pendingShot != SHOT_NONE || isMimicryActive()) return;

        float cost = OWAttacksConstants.Chameleon.TONGUE_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        if (getPerch() != PERCH_HEAD) scuttle(carrier);
        this.entityData.set(PERCH, PERCH_HEAD);
        perchReturnDelay = 0;
        pendingShot = SHOT_AIMED;
        pendingShotTarget = -1;
        shotWindup = -1;
        retractOnAttach = false;
    }

    private void scuttle(Player carrier) {
        this.level().playSound(null, carrier.getX(), carrier.getY() + 1.5, carrier.getZ(),
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_SCUTTLE.get(), SoundSource.NEUTRAL, 0.7f, 1.1f);
        this.level().playSound(null, carrier.getX(), carrier.getY() + 1.5, carrier.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.35f, 1.8f);
    }

    private void handlePendingShot() {
        if (pendingShot == SHOT_NONE) return;

        Player carrier = getCarrier();
        if (carrier == null || carrier.getVehicle() != null) {
            clearPendingShot();
            return;
        }
        if (pendingShot == SHOT_AIMED && perchBlend < OWAttacksConstants.Chameleon.SHOT_PERCH_THRESHOLD) return;

        if (shotWindup < 0) {
            shotWindup = OWAttacksConstants.Chameleon.SHOT_WINDUP_TICKS;
            this.entityData.set(TONGUE_SHOT_TIMER, OWAttacksConstants.Chameleon.TONGUE_SHOT_TICKS);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_WINDUP.get(), SoundSource.NEUTRAL, 0.7f, 1.0f);
            return;
        }
        if (--shotWindup > 0) return;

        int kind = pendingShot;
        int targetId = pendingShotTarget;
        clearPendingShot();

        if (kind == SHOT_AIMED) {
            aimTongue(carrier);
            return;
        }
        if (this.level().getEntity(targetId) instanceof LivingEntity target && target.isAlive()
                && target.distanceTo(carrier) <= watchRadius() + 2.0) {
            tongueAimed = false;
            tongueOutcome = OUTCOME_ENTITY;
            launchTongue(target.getBoundingBox().getCenter(), target.getId());
        }
    }

    private void clearPendingShot() {
        pendingShot = SHOT_NONE;
        pendingShotTarget = -1;
        shotWindup = -1;
    }

    private void aimTongue(Player carrier) {
        double range = tongueRange();
        Vec3 eye = carrier.getEyePosition();
        Vec3 look = carrier.getLookAngle();
        Vec3 far = eye.add(look.scale(range));

        BlockHitResult blockHit = this.level().clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, carrier));
        double limit = blockHit.getType() == HitResult.Type.MISS ? range : blockHit.getLocation().distanceTo(eye);

        LivingEntity entityHit = null;
        double entityDistance = limit;
        AABB sweep = carrier.getBoundingBox().expandTowards(look.scale(limit)).inflate(1.0);
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, sweep,
                e -> e != carrier && e != this && e.isAlive() && !e.isSpectator() && !isFriendOf(carrier, e))) {
            AABB box = candidate.getBoundingBox().inflate(OWAttacksConstants.Chameleon.TONGUE_ENTITY_PICK_RADIUS);
            var clip = box.clip(eye, far);
            if (clip.isEmpty()) continue;
            double distance = clip.get().distanceTo(eye);
            if (distance < entityDistance) {
                entityDistance = distance;
                entityHit = candidate;
            }
        }

        ItemEntity itemHit = null;
        double itemDistance = Math.min(limit, entityDistance);
        for (ItemEntity candidate : this.level().getEntitiesOfClass(ItemEntity.class, sweep, ItemEntity::isAlive)) {
            AABB box = candidate.getBoundingBox().inflate(OWAttacksConstants.Chameleon.TONGUE_ITEM_PICK_RADIUS);
            var clip = box.clip(eye, far);
            if (clip.isEmpty()) continue;
            double distance = clip.get().distanceTo(eye);
            if (distance < itemDistance) {
                itemDistance = distance;
                itemHit = candidate;
            }
        }

        tongueAimed = true;
        tongueItemEntityId = -1;
        tongueAnchorBlock = null;

        if (itemHit != null) {
            tongueOutcome = OUTCOME_ITEM;
            tongueItemEntityId = itemHit.getId();
            launchTongue(itemHit.position().add(0, ITEM_GRAB_LIFT, 0), -1);
        } else if (entityHit != null) {
            tongueOutcome = OUTCOME_ENTITY;
            launchTongue(entityHit.getBoundingBox().getCenter(), entityHit.getId());
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            tongueOutcome = OUTCOME_BLOCK;
            tongueAnchorBlock = blockHit.getBlockPos();
            tongueAnchorFace = blockHit.getDirection();
            launchTongue(blockHit.getLocation(), -1);
        } else {
            tongueOutcome = OUTCOME_MISS;
            launchTongue(far, -1);
        }
    }

    private static boolean isHostileOrNeutral(Mob mob) {
        if (mob instanceof OWEntity owEntity) return owEntity.getTemperament() != OWEntityConfig.Temperament.PASSIVE;
        return mob instanceof net.minecraft.world.entity.monster.Enemy || mob instanceof net.minecraft.world.entity.NeutralMob;
    }

    private static boolean isFriendOf(Player carrier, LivingEntity entity) {
        if (entity.isAlliedTo(carrier)) return true;
        if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(carrier)) return true;
        return entity.getVehicle() == carrier || carrier.getVehicle() == entity;
    }

    private void launchTongue(Vec3 end, int targetId) {
        this.entityData.set(TONGUE_END, end.toVector3f());
        this.entityData.set(TONGUE_TARGET, targetId);
        this.entityData.set(TONGUE_ITEM, ItemStack.EMPTY);
        float reach = (float) end.distanceTo(mouthPosition(1f));
        this.entityData.set(TONGUE_LENGTH, reach);
        this.entityData.set(TONGUE_PHASE_TICK, 0);
        this.entityData.set(TONGUE_PHASE_TICKS, phaseTicks(reach, OWAttacksConstants.Chameleon.TONGUE_EXTEND_SPEED,
                OWAttacksConstants.Chameleon.TONGUE_EXTEND_MAX_TICKS));
        this.entityData.set(TONGUE_STATE, TONGUE_EXTENDING);
        this.vineTicks = 0;

        Vec3 mouth = mouthPosition(1f);
        this.level().playSound(null, mouth.x, mouth.y, mouth.z,
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_SHOT.get(), SoundSource.NEUTRAL, 0.6f,
                (float) OWUtils.generateRandomInterval(0.95, 1.1));
        burst(ParticleTypes.ITEM_SLIME, mouth, 6, 0.05, 0.08);
    }

    private void burst(net.minecraft.core.particles.ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
        }
    }

    private void handleTongue() {
        switch (getTongueState()) {
            case TONGUE_EXTENDING -> extendTongue();
            case TONGUE_ATTACHED -> holdVine();
            case TONGUE_RETRACTING -> retractTongue();
            default -> {
            }
        }
    }

    private static int phaseTicks(float length, double speed, int maxTicks) {
        return Mth.clamp(Mth.ceil(length / speed), 2, maxTicks);
    }

    public static float extensionCurve(float t) {
        float c1 = 1.6f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    private void extendTongue() {
        Vec3 mouth = mouthPosition(1f);
        Entity target = getTongueTarget() >= 0 ? this.level().getEntity(getTongueTarget()) : null;
        if (target != null && target.isAlive()) {
            Vec3 center = target.getBoundingBox().getCenter();
            this.entityData.set(TONGUE_END, center.toVector3f());
            this.entityData.set(TONGUE_LENGTH, (float) center.distanceTo(mouth));
        } else if (tongueOutcome == OUTCOME_ITEM && this.level().getEntity(tongueItemEntityId) instanceof ItemEntity item && item.isAlive()) {
            Vec3 held = item.position().add(0, ITEM_GRAB_LIFT, 0);
            this.entityData.set(TONGUE_END, held.toVector3f());
            this.entityData.set(TONGUE_LENGTH, (float) held.distanceTo(mouth));
        }

        int phase = getTonguePhaseTick() + 1;
        this.entityData.set(TONGUE_PHASE_TICK, phase);
        if (phase < getTonguePhaseTicks()) return;

        switch (tongueOutcome) {
            case OUTCOME_ENTITY -> strikeTongueTarget(target);
            case OUTCOME_ITEM -> catchItem();
            case OUTCOME_BLOCK -> attachVine();
            default -> {
                Vec3 end = tongueEnd();
                this.level().playSound(null, end.x, end.y, end.z, net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_MISS.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                startRetract(1);
            }
        }
    }

    private void strikeTongueTarget(@Nullable Entity target) {
        if (target instanceof LivingEntity living && living.isAlive()) {
            living.hurt(this.damageSources().mobAttack(this), this.getCombatDamage() * OWAttacksConstants.Chameleon.TONGUE_DAMAGE_RATIO);
            Player carrier = getCarrier();
            if (carrier != null && living instanceof Mob mob && mob.isAlive() && isHostileOrNeutral(mob) && mob.canAttack(carrier)) {
                mob.setTarget(carrier);
            }
            Vec3 hit = living.getBoundingBox().getCenter();
            this.level().playSound(null, hit.x, hit.y, hit.z,
                    net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_IMPACT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            this.level().playSound(null, hit.x, hit.y, hit.z,
                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 0.3f, 1.7f);
            burst(ParticleTypes.CRIT, hit, 14, 0.25, 0.35);
            burst(ParticleTypes.ITEM_SLIME, hit, 8, 0.15, 0.12);
            burst(ParticleTypes.SWEEP_ATTACK, hit, 1, 0.0, 0.0);
        }
        startRetract(OWAttacksConstants.Chameleon.TONGUE_IMPACT_HOLD_TICKS);
    }

    private void catchItem() {
        if (this.level().getEntity(tongueItemEntityId) instanceof ItemEntity item && item.isAlive()) {
            this.entityData.set(TONGUE_ITEM, item.getItem().copy());
            burst(new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM, item.getItem()), item.position(), 8, 0.1, 0.08);
            this.level().playSound(null, item.getX(), item.getY(), item.getZ(),
                    SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 1.0f, 1.5f);
            item.discard();
        }
        startRetract(OWAttacksConstants.Chameleon.TONGUE_IMPACT_HOLD_TICKS);
    }

    private void attachVine() {
        Player holder = getCarrier();
        boolean submerged = holder != null && (holder.isUnderWater() || holder.isInWater());
        if (tongueAnchorBlock == null || this.level().getBlockState(tongueAnchorBlock).isAir() || submerged || isAnchorUnderwater()) {
            Vec3 end = tongueEnd();
            this.level().playSound(null, end.x, end.y, end.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.7f, 0.8f);
            startRetract(1);
            return;
        }
        this.entityData.set(TONGUE_STATE, TONGUE_ATTACHED);
        Vec3 anchor = tongueEnd();
        this.level().playSound(null, anchor.x, anchor.y, anchor.z,
                net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_STICK.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        this.level().playSound(null, anchor.x, anchor.y, anchor.z,
                SoundEvents.HONEY_BLOCK_PLACE, SoundSource.NEUTRAL, 0.9f, 1.1f);
        burst(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(tongueAnchorBlock)), anchor, 14, 0.15, 0.15);
        burst(ParticleTypes.ITEM_SLIME, anchor, 6, 0.1, 0.05);
        if (retractOnAttach) {
            retractOnAttach = false;
            startRetract(OWAttacksConstants.Chameleon.TONGUE_IMPACT_HOLD_TICKS);
        }
    }

    private boolean isAnchorUnderwater() {
        if (tongueAnchorBlock == null) return false;
        if (!this.level().getFluidState(tongueAnchorBlock).isEmpty()) return true;
        BlockPos front = tongueAnchorFace == null ? BlockPos.containing(tongueEnd()) : tongueAnchorBlock.relative(tongueAnchorFace);
        return !this.level().getFluidState(front).isEmpty();
    }

    private void holdVine() {
        Player carrier = getCarrier();
        if (carrier == null || tongueAnchorBlock == null) {
            releaseTongue(false);
            return;
        }

        carrier.fallDistance = 0f;
        setVitalEnergy(getVitalEnergy() + OWAttacksConstants.Chameleon.VINE_ENERGY_PER_TICK);
        if (getVitalEnergy() >= getVitalEnergyCapacity()) {
            setVitalEnergy(getVitalEnergyCapacity());
            canShowVitalEnergyLack = true;
            releaseTongue(true);
            return;
        }
        boolean blockGone = this.level().getBlockState(tongueAnchorBlock).isAir();
        boolean tooFar = ropePoint(carrier).distanceTo(tongueEnd()) > tongueRange() + OWAttacksConstants.Chameleon.VINE_RELEASE_SLACK;
        if (blockGone || tooFar || carrier.getVehicle() != null || ++vineTicks > OWAttacksConstants.Chameleon.VINE_MAX_TICKS) {
            releaseTongue(true);
        }
    }

    public void releaseTongue(boolean playSound) {
        int state = getTongueState();
        if (state == TONGUE_IDLE) return;

        Player carrier = getCarrier();
        if (carrier != null) carrier.fallDistance = 0f;

        if (state == TONGUE_ATTACHED) {
            this.entityData.set(TONGUE_LENGTH, (float) tongueEnd().distanceTo(mouthPosition(1f)));
            startRetract(0);
        } else if (state == TONGUE_EXTENDING) {
            this.entityData.set(TONGUE_LENGTH, currentTongueLength(0f));
            startRetract(0);
        }
        if (playSound) {
            Vec3 end = tongueEnd();
            this.level().playSound(null, end.x, end.y, end.z,
                    net.tiew.operationWild.sound.OWSounds.CHAMELEON_TONGUE_UNSTICK.get(), SoundSource.NEUTRAL, 0.9f, 1.0f);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.8f, 1.2f);
            burst(ParticleTypes.ITEM_SLIME, end, 5, 0.1, 0.05);
        }
        if (!this.isAlive() || carrier == null) finishTongue();
    }

    private void startRetract(int holdTicks) {
        this.entityData.set(TONGUE_TARGET, -1);
        this.entityData.set(TONGUE_PHASE_TICK, -holdTicks);
        this.entityData.set(TONGUE_PHASE_TICKS, phaseTicks(getTongueLength(), OWAttacksConstants.Chameleon.TONGUE_RETRACT_SPEED,
                OWAttacksConstants.Chameleon.TONGUE_RETRACT_MAX_TICKS));
        this.entityData.set(TONGUE_STATE, TONGUE_RETRACTING);
        tongueAnchorBlock = null;
    }

    private void retractTongue() {
        int phase = getTonguePhaseTick() + 1;
        this.entityData.set(TONGUE_PHASE_TICK, phase);
        if (phase >= getTonguePhaseTicks()) finishTongue();
    }

    public float currentTongueLength(float partialTicks) {
        int state = getTongueState();
        float reach = getTongueLength();
        float t = Mth.clamp((getTonguePhaseTick() + partialTicks) / Math.max(1, getTonguePhaseTicks()), 0f, 1f);
        if (state == TONGUE_EXTENDING) return reach * extensionCurve(t);
        if (state == TONGUE_RETRACTING) return reach * (1f - t * t);
        return state == TONGUE_IDLE ? 0f : reach;
    }

    public float tongueWhip(float partialTicks) {
        int state = getTongueState();
        float t = Mth.clamp((getTonguePhaseTick() + partialTicks) / Math.max(1, getTonguePhaseTicks()), 0f, 1f);
        if (state == TONGUE_EXTENDING) return (1f - t) * (1f - t);
        if (state == TONGUE_RETRACTING) return 2f * t * (1f - t);
        return 0f;
    }

    public void releaseHeldTongue() {
        if (this.level().isClientSide()) return;
        int state = getTongueState();
        if (state == TONGUE_ATTACHED || state == TONGUE_EXTENDING && tongueOutcome == OUTCOME_BLOCK) {
            releaseTongue(true);
        } else if (pendingShot == SHOT_AIMED || state == TONGUE_EXTENDING) {
            retractOnAttach = true;
        }
    }

    private void finishTongue() {
        ItemStack caught = getTongueItem();
        Player carrier = getCarrier();
        if (!caught.isEmpty()) {
            if (carrier != null) {
                if (!carrier.getInventory().add(caught.copy())) carrier.drop(caught.copy(), false);
                this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.6f);
            } else {
                this.spawnAtLocation(caught.copy());
            }
        }

        this.entityData.set(TONGUE_ITEM, ItemStack.EMPTY);
        this.entityData.set(TONGUE_LENGTH, 0f);
        this.entityData.set(TONGUE_PHASE_TICK, 0);
        this.entityData.set(TONGUE_TARGET, -1);
        this.entityData.set(TONGUE_STATE, TONGUE_IDLE);
        retractOnAttach = false;
        if (this.isAlive()) {
            this.entityData.set(TONGUE_CATCH_TIMER, OWAttacksConstants.Chameleon.TONGUE_CATCH_TICKS);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.tiew.operationWild.sound.OWSounds.CHAMELEON_EAT.get(), SoundSource.NEUTRAL, 0.8f, (float) OWUtils.generateRandomInterval(0.95, 1.1));
        }
        tongueAnchorBlock = null;
        tongueItemEntityId = -1;
        tongueOutcome = OUTCOME_MISS;
        vineTicks = 0;

        tongueCooldown = OWAttacksConstants.Chameleon.TONGUE_COOLDOWN_TICKS;
        if (tongueAimed) perchReturnDelay = OWAttacksConstants.Chameleon.TONGUE_PERCH_RETURN_DELAY;
        tongueAimed = false;
    }

    private void handlePerchReturn() {
        if (perchReturnDelay <= 0) return;
        if (--perchReturnDelay > 0) return;
        if (getTongueState() != TONGUE_IDLE || pendingShot != SHOT_NONE) return;
        Player carrier = getCarrier();
        if (carrier != null && getPerch() != PERCH_BACK) scuttle(carrier);
        this.entityData.set(PERCH, PERCH_BACK);
    }

    public boolean canUseTongue() {
        return getTongueState() == TONGUE_ATTACHED
                || (getTongueState() == TONGUE_IDLE && tongueCooldown <= 0 && !isMimicryActive()
                && getVitalEnergy() <= getVitalEnergyCapacity() - OWAttacksConstants.Chameleon.TONGUE_ENERGY);
    }

    private void handleWatch() {
        Player carrier = getCarrier();
        if (carrier == null || !this.isTame()) {
            if (getWatchTarget() != -1) this.entityData.set(WATCH_TARGET, -1);
            return;
        }

        if (--watchScanTimer > 0) return;
        watchScanTimer = OWAttacksConstants.Chameleon.WATCH_SCAN_INTERVAL_TICKS;

        Mob threat = findHiddenThreat(carrier);
        int threatId = threat == null ? -1 : threat.getId();
        if (threatId != getWatchTarget()) {
            this.entityData.set(WATCH_TARGET, threatId);
            if (threat != null && carrier instanceof ServerPlayer serverPlayer) {
                serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.NEUTRAL, 0.6f, 1.8f);
            }
        }

        if (threat == null || tongueCooldown > 0 || isMimicryActive()) return;
        if (getTongueState() != TONGUE_IDLE || pendingShot != SHOT_NONE || getPerch() != PERCH_BACK || perchBlend > 0f) return;

        float cost = OWAttacksConstants.Chameleon.TONGUE_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) return;
        setVitalEnergy(getVitalEnergy() + cost);

        pendingShot = SHOT_PASSIVE;
        pendingShotTarget = threat.getId();
        shotWindup = -1;
    }

    private @Nullable Mob findHiddenThreat(Player carrier) {
        double radius = watchRadius();
        Vec3 eye = carrier.getEyePosition();
        Vec3 look = carrier.getLookAngle();
        Mob closest = null;
        double best = Double.MAX_VALUE;

        for (Mob mob : this.level().getEntitiesOfClass(Mob.class, carrier.getBoundingBox().inflate(radius),
                m -> m.isAlive() && m.getTarget() == carrier && isHostileOrNeutral(m) && !isFriendOf(carrier, m))) {
            Vec3 toMob = mob.getBoundingBox().getCenter().subtract(eye);
            double distance = toMob.length();
            if (distance > radius || distance < 1.0e-3) continue;
            if (look.dot(toMob.scale(1.0 / distance)) >= OWAttacksConstants.Chameleon.WATCH_BLIND_DOT) continue;
            if (!carrier.hasLineOfSight(mob)) continue;
            if (distance < best) {
                best = distance;
                closest = mob;
            }
        }
        return closest;
    }

    public boolean activateMimicry() {
        if (this.level().isClientSide()) return false;
        if (isMimicryActive()) {
            breakMimicry();
            return true;
        }

        Player carrier = getCarrier();
        if (carrier == null || carrier.getVehicle() != null) return false;

        float cost = OWAttacksConstants.Chameleon.MIMICRY_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        releaseTongue(true);
        clearPendingShot();
        int duration = mimicryDurationTicks();
        this.entityData.set(MIMICRY_TIMER, duration);
        this.entityData.set(MIMICRY_CAST_TIMER, OWAttacksConstants.Chameleon.MIMICRY_CAST_TICKS);
        carrier.addEffect(new MobEffectInstance(OWEffects.CAMOUFLAGE_EFFECT.getDelegate(), duration, 0, false, false, true));

        camouflageBurst(carrier, true, 1.0f);
        return true;
    }

    private void camouflageBurst(Entity anchor, boolean hiding, float volume) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double x = anchor.getX();
        double y = anchor.getY() + anchor.getBbHeight() * 0.5;
        double z = anchor.getZ();
        double spreadXZ = anchor.getBbWidth() * 0.45;
        double spreadY = anchor.getBbHeight() * 0.35;
        for (Vector3f color : CAMOUFLAGE_PARTICLE_COLORS) {
            serverLevel.sendParticles(new net.minecraft.core.particles.DustParticleOptions(color, CAMOUFLAGE_PARTICLE_SIZE),
                    x, y, z, CAMOUFLAGE_PARTICLES_PER_COLOR, spreadXZ, spreadY, spreadXZ, 0.0);
        }
        serverLevel.sendParticles(hiding ? ParticleTypes.WAX_OFF : ParticleTypes.WAX_ON,
                x, y, z, CAMOUFLAGE_SPARKLES, spreadXZ, spreadY, spreadXZ, 0.0);
        serverLevel.playSound(null, x, y, z,
                hiding ? net.tiew.operationWild.sound.OWSounds.CHAMELEON_CAMOUFLAGE.get() : net.tiew.operationWild.sound.OWSounds.CHAMELEON_CAMOUFLAGE_BREAK.get(),
                SoundSource.PLAYERS, volume * (hiding ? 1.0f : 0.6f), (float) OWUtils.generateRandomInterval(0.95, 1.05));
    }

    private int mimicryDurationTicks() {
        int base = OWAttacksConstants.Chameleon.MIMICRY_DURATION_TICKS;
        return OWPistePassives.has(this, OWPistePassives.SLOW_FADE)
                ? base + OWAttacksConstants.Chameleon.MIMICRY_SLOW_FADE_BONUS_TICKS
                : base;
    }

    private void handleMimicry() {
        int cast = getMimicryCastTimer();
        if (cast > 0) this.entityData.set(MIMICRY_CAST_TIMER, cast - 1);

        int timer = getMimicryTimer();
        if (timer <= 0) return;
        Player carrier = getCarrier();
        if (carrier == null && !wildCamouflaging) {
            cancelMimicry();
            return;
        }
        Entity anchor = carrier != null ? carrier : this;
        this.entityData.set(MIMICRY_TIMER, timer - 1);
        if (timer - 1 == OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS) {
            camouflageBurst(anchor, false, carrier != null ? 1.0f : WILD_CAMOUFLAGE_VOLUME);
        }
        if (timer - 1 <= 0) cancelMimicry();
    }

    public void breakMimicry() {
        Player carrier = getCarrier();
        int fade = OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS;
        Entity anchor = carrier != null ? carrier : wildCamouflaging ? this : null;
        if (anchor == null || getMimicryTimer() <= fade && getMimicryCastTimer() <= 0) {
            if (anchor == null) cancelMimicry();
            return;
        }
        if (carrier != null) {
            MobEffectInstance effect = carrier.getEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate());
            if (effect != null && !effect.isInfiniteDuration()) carrier.removeEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate());
        }
        this.entityData.set(MIMICRY_TIMER, fade);
        this.entityData.set(MIMICRY_CAST_TIMER, 0);
        camouflageBurst(anchor, false, carrier != null ? 1.0f : WILD_CAMOUFLAGE_VOLUME);
    }

    private void startWildCamouflage(int duration) {
        wildCamouflaging = true;
        this.entityData.set(MIMICRY_TIMER, duration);
        this.entityData.set(MIMICRY_CAST_TIMER, OWAttacksConstants.Chameleon.MIMICRY_CAST_TICKS);
        camouflageBurst(this, true, WILD_CAMOUFLAGE_VOLUME);
    }

    private boolean canCamouflageInWild() {
        return !this.isTame() && !this.isCarried() && !this.isClimbing() && !this.isNapping() && !this.isSleeping()
                && !this.isKnockedOut() && !this.isInWater() && this.onGround() && !this.isWallClimbing()
                && !this.isMimicryActive() && this.getLastHurtByMob() == null;
    }

    private final class WildCamouflageGoal extends net.minecraft.world.entity.ai.goal.Goal {

        private WildCamouflageGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (wildCamouflageCooldown > 0) return false;
            return canCamouflageInWild() && ChameleonEntity.this.getRandom().nextInt(WILD_CAMOUFLAGE_CHANCE) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return wildCamouflaging && !ChameleonEntity.this.isTame()
                    && getMimicryTimer() > OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS;
        }

        @Override
        public void start() {
            ChameleonEntity.this.getNavigation().stop();
            startWildCamouflage((int) OWUtils.generateRandomInterval(WILD_CAMOUFLAGE_MIN_TICKS, WILD_CAMOUFLAGE_MAX_TICKS));
        }

        @Override
        public void tick() {
            ChameleonEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            if (wildCamouflaging && getMimicryTimer() > OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS) breakMimicry();
            wildCamouflageCooldown = (int) OWUtils.generateRandomInterval(WILD_CAMOUFLAGE_COOLDOWN_MIN, WILD_CAMOUFLAGE_COOLDOWN_MAX);
        }
    }

    private boolean canWallClimb() {
        return this.isAlive() && !this.isCarried() && !this.isPassenger() && !this.isClimbing() && !this.isInWater()
                && !this.isNapping() && !this.isSleeping() && !this.isKnockedOut() && !this.isSitting() && !this.noPhysics;
    }

    private @Nullable net.minecraft.core.Direction findWall() {
        AABB box = this.getBoundingBox().deflate(0, WALL_PROBE_DEFLATE, 0);
        Vec3 facing = Vec3.directionFromRotation(0, this.getYRot());
        int current = getWallFace();
        net.minecraft.core.Direction best = null;
        double bestScore = -Double.MAX_VALUE;
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            AABB probe = box.move(direction.getStepX() * WALL_PROBE, 0, direction.getStepZ() * WALL_PROBE);
            if (this.level().noCollision(this, probe)) continue;
            double score = facing.x * direction.getStepX() + facing.z * direction.getStepZ()
                    + (current == direction.get2DDataValue() ? 2.0 : 0.0);
            if (score > bestScore) {
                bestScore = score;
                best = direction;
            }
        }
        return best;
    }

    private void handleWallClimb() {
        net.minecraft.core.Direction wall = canWallClimb() ? findWall() : null;
        boolean clinging = wall != null && (!this.onGround()
                || this.horizontalCollision && (isWallClimbing() || facesWall(wall)));
        if (!clinging) {
            if (getWallFace() != -1 && !this.getNavigation().isDone()) this.getNavigation().recomputePath();
            if (getWallFace() != -1) this.entityData.set(WALL_FACE, -1);
            if (isWallDescending()) this.entityData.set(WALL_DESCENDING, false);
            wallTrend = 0;
            return;
        }
        this.entityData.set(WALL_FACE, wall.get2DDataValue());

        Vec3 velocity = this.getDeltaMovement();
        if (velocity.y < -WALL_TREND_SPEED) wallTrend = Math.max(-WALL_TREND_LIMIT, wallTrend - 1);
        else if (velocity.y > WALL_TREND_SPEED) wallTrend = Math.min(WALL_TREND_LIMIT, wallTrend + 1);
        if (wallTrend <= -WALL_TREND_FLIP) this.entityData.set(WALL_DESCENDING, true);
        else if (wallTrend >= WALL_TREND_FLIP) this.entityData.set(WALL_DESCENDING, false);

        if (!this.horizontalCollision) {
            this.setDeltaMovement(velocity.x * WALL_SLIDE_DAMPING + wall.getStepX() * WALL_STICK, velocity.y,
                    velocity.z * WALL_SLIDE_DAMPING + wall.getStepZ() * WALL_STICK);
        }

        if (!isWallDescending() && !wantsToDescend() && nearLedge(wall)) {
            Vec3 current = this.getDeltaMovement();
            this.setDeltaMovement(wall.getStepX() * WALL_MANTLE_PUSH, Math.max(current.y, WALL_CLIMB_UP_SPEED),
                    wall.getStepZ() * WALL_MANTLE_PUSH);
        }

        float target = isWallDescending() ? wall.getOpposite().toYRot() : wall.toYRot();
        float yaw = Mth.approachDegrees(this.getYRot(), target, WALL_TURN_STEP);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
    }

    private boolean nearLedge(net.minecraft.core.Direction wall) {
        AABB above = this.getBoundingBox().deflate(0, WALL_PROBE_DEFLATE, 0)
                .move(wall.getStepX() * WALL_PROBE, WALL_MANTLE_REACH, wall.getStepZ() * WALL_PROBE);
        return this.level().noCollision(this, above);
    }

    private boolean facesWall(net.minecraft.core.Direction wall) {
        Vec3 facing = Vec3.directionFromRotation(0, this.getYRot());
        return facing.x * wall.getStepX() + facing.z * wall.getStepZ() > WALL_FACING_DOT;
    }

    private void tickWallPitch() {
        int face = getWallFace();
        if (face >= 0) lastWallFace = face;
        float target = face < 0 ? 0f : isWallDescending() ? 90f : -90f;
        this.wallPitchO = this.wallPitch;
        this.wallPitch += (target - this.wallPitch) * WALL_PITCH_RESPONSE;
        if (target == 0f && Math.abs(this.wallPitch) < 0.05f) this.wallPitch = 0f;
        if ((face >= 0 || this.wallPitch != 0f) && !this.isCarried()) {
            this.yBodyRot = this.getYRot();
            this.setYHeadRot(this.getYRot());
        }
    }

    public float wallLookFreedom(float partialTicks) {
        return 1f - Mth.clamp(Math.abs(wallPitchDegrees(partialTicks)) / 90f, 0f, 1f);
    }

    public float wallPitchDegrees(float partialTicks) {
        return Mth.lerp(partialTicks, wallPitchO, wallPitch);
    }

    public int getWallFace() { return this.entityData.get(WALL_FACE); }

    public boolean isWallDescending() { return this.entityData.get(WALL_DESCENDING); }

    public boolean isWallClimbing() { return getWallFace() >= 0; }

    @Override
    public boolean onClimbable() {
        return isWallClimbing() || super.onClimbable();
    }

    @Override
    public void travel(Vec3 input) {
        boolean lowering = isWallClimbing() && !this.onGround() && wantsToDescend();
        if (isWallClimbing()) {
            Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.x, Mth.clamp(velocity.y, -WALL_CLIMB_DOWN_SPEED, WALL_CLIMB_UP_SPEED), velocity.z);
        }
        super.travel(lowering ? Vec3.ZERO : input);
        if (lowering && this.getDeltaMovement().y > 0) {
            Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.x, -WALL_CLIMB_DOWN_SPEED, velocity.z);
        }
    }

    private boolean wantsToDescend() {
        net.minecraft.world.entity.ai.navigation.PathNavigation navigation = this.getNavigation();
        BlockPos target = !navigation.isDone() ? navigation.getTargetPos()
                : navigation instanceof net.tiew.operationWild.entity.navigation.ChameleonClimbNavigation climb ? climb.directTarget() : null;
        if (target == null || target.getY() >= this.getY() - WALL_DESCENT_INTENT) return false;
        int face = getWallFace();
        if (face < 0) return true;
        net.minecraft.core.Direction wall = net.minecraft.core.Direction.from2DDataValue(face);
        double dx = target.getX() + 0.5 - this.getX();
        double dz = target.getZ() + 0.5 - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        return distance < 1.0e-3 || (dx * wall.getStepX() + dz * wall.getStepZ()) / distance <= WALL_BEYOND_DOT;
    }

    @Override
    public void jumpFromGround() {
        if (canWallClimb()) return;
        super.jumpFromGround();
    }

    @Override
    public int getMaxFallDistance() {
        return canWallClimb() ? WALL_MAX_FALL_DISTANCE : super.getMaxFallDistance();
    }

    @Override
    public void calculateEntityAnimation(boolean includeHeight) {
        super.calculateEntityAnimation(includeHeight || isWallClimbing());
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        return new net.tiew.operationWild.entity.navigation.ChameleonClimbNavigation(this, level);
    }

    public void cancelMimicry() {
        wildCamouflaging = false;
        if (getMimicryTimer() <= 0 && getMimicryCastTimer() <= 0) return;
        Player carrier = getCarrier();
        if (carrier != null) {
            MobEffectInstance effect = carrier.getEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate());
            if (effect != null && !effect.isInfiniteDuration()) carrier.removeEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate());
        }
        this.entityData.set(MIMICRY_TIMER, 0);
        this.entityData.set(MIMICRY_CAST_TIMER, 0);
    }

    public static void breakCarrierMimicry(Player attacker) {
        if (attacker.level().isClientSide()) return;
        for (Entity passenger : attacker.getPassengers()) {
            if (passenger instanceof ChameleonEntity chameleon && chameleon.isMimicryActive()) chameleon.breakMimicry();
        }
    }

    public static @Nullable ChameleonEntity getMimicking(@Nullable Player player) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof ChameleonEntity chameleon && chameleon.isMimicryActive()) return chameleon;
        }
        return null;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        boolean resting = isNapping() || isSleeping();
        boolean carried = isCarried();
        int perch = getPerch();

        if (carried) {
            this.backIdleAnimationState.startIfStopped(this.tickCount);
            this.headIdleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.backIdleAnimationState.stop();
            this.headIdleAnimationState.stop();
        }

        if (carried && perch != lastPerch) {
            if (perch == PERCH_HEAD) {
                this.transitionHeadBackAnimationState.stop();
                this.transitionBackHeadAnimationState.start(this.tickCount);
            } else {
                this.transitionBackHeadAnimationState.stop();
                this.transitionHeadBackAnimationState.start(this.tickCount);
            }
        }
        if (!carried || (perchBlend <= 0f && perch == PERCH_BACK)) this.transitionHeadBackAnimationState.stop();
        if (!carried || (perchBlend >= 1f && perch == PERCH_HEAD)) this.transitionBackHeadAnimationState.stop();
        lastPerch = perch;

        if (getTongueState() == TONGUE_ATTACHED) this.tongueHoldAnimationState.startIfStopped(this.tickCount);
        else if (this.tongueHoldWeight <= 0f) this.tongueHoldAnimationState.stop();

        int climbTimer = this.entityData.get(CLIMB_TIMER);
        if (climbTimer > lastClimbTimer) {
            if (isClimbMounting()) this.climbOnAnimationState.start(this.tickCount);
            else this.jumpOffAnimationState.start(this.tickCount);
        }
        if (climbTimer <= 0 && !isCarried()) this.climbOnAnimationState.stop();
        if (climbTimer <= 0) this.jumpOffAnimationState.stop();
        if (isCarried()) this.jumpOffAnimationState.stop();
        lastClimbTimer = climbTimer;

        int catchTimer = getTongueCatchTimer();
        if (catchTimer > lastTongueCatchTimer) this.tongueCatchAnimationState.start(this.tickCount);
        else if (catchTimer <= 0) this.tongueCatchAnimationState.stop();
        lastTongueCatchTimer = catchTimer;

        int shotTimer = getTongueShotTimer();
        if (shotTimer > lastTongueShotTimer) this.tongueShotAnimationState.start(this.tickCount);
        else if (shotTimer <= 0) this.tongueShotAnimationState.stop();
        lastTongueShotTimer = shotTimer;

        int castTimer = getMimicryCastTimer();
        if (castTimer > lastMimicryCastTimer) this.camouflageCastAnimationState.start(this.tickCount);
        else if (castTimer <= 0) this.camouflageCastAnimationState.stop();
        lastMimicryCastTimer = castTimer;

        if (resting && !carried) this.napAnimationState.startIfStopped(this.tickCount);
        else this.napAnimationState.stop();

        if (isSitting() && !resting && !carried) this.sittingAnimationState.startIfStopped(this.tickCount);
        else this.sittingAnimationState.stop();

        this.idleAnimationState.startIfStopped(this.tickCount);
        handleMiscIdleAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isPassenger() && !this.isInWater() && !this.isWallClimbing() && this.wallPitch == 0f;
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted();
    }

    protected void handleMiscIdleAnimations() {
        if (this.miscIdleAnimationState.isStarted()
                && this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_DURATION) {
            this.miscIdleAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            this.miscIdleAnimationState.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(ChameleonVariant.DEFAULT);
            this.setInitialVariant(ChameleonVariant.DEFAULT);
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (OWEntity.NAPPING.equals(accessor) && !isNapping() && !isSleeping() && this.level().isClientSide()) {
            napAnimationState.stop();
        }
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof ChameleonEntity chameleon) {
            chameleon.setVariant(ChameleonVariant.byId(variant));
            chameleon.setInitialVariant(ChameleonVariant.byId(variant));
        }
    }

    public ChameleonVariant getVariant() { return ChameleonVariant.byId(this.getTypeVariant() & 255); }

    public void setVariant(ChameleonVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255); }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
    }

    @Override
    public void changeSkinSilent(int skinIndex) { changeSkin(skinIndex, false); }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public ChameleonVariant getInitialVariant() { return ChameleonVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT)); }

    public void setInitialVariant(ChameleonVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId()); }

    public int getPerch() { return this.entityData.get(PERCH); }

    public int getTongueState() { return this.entityData.get(TONGUE_STATE); }

    public float getTongueLength() { return this.entityData.get(TONGUE_LENGTH); }

    public int getTonguePhaseTick() { return this.entityData.get(TONGUE_PHASE_TICK); }

    public int getTonguePhaseTicks() { return this.entityData.get(TONGUE_PHASE_TICKS); }

    public Vec3 tongueEnd() { return new Vec3(this.entityData.get(TONGUE_END)); }

    public int getTongueTarget() { return this.entityData.get(TONGUE_TARGET); }

    public ItemStack getTongueItem() { return this.entityData.get(TONGUE_ITEM); }

    public int getTongueShotTimer() { return this.entityData.get(TONGUE_SHOT_TIMER); }

    public int getWatchTarget() { return this.entityData.get(WATCH_TARGET); }

    public int getMimicryTimer() { return this.entityData.get(MIMICRY_TIMER); }

    public int getMimicryCastTimer() { return this.entityData.get(MIMICRY_CAST_TIMER); }

    public int getTongueCatchTimer() { return this.entityData.get(TONGUE_CATCH_TIMER); }

    public @Nullable Vec3 aimPoint(float partialTicks) {
        if (isTongueOut()) {
            int targetId = getTongueTarget();
            if (targetId >= 0 && this.level().getEntity(targetId) instanceof Entity target) {
                return target.getPosition(partialTicks).add(0, target.getBbHeight() * 0.5, 0);
            }
            return tongueEnd();
        }
        if (getTongueShotTimer() <= 0) return null;

        Player carrier = getCarrier();
        if (carrier != null && getPerch() == PERCH_HEAD) {
            return carrier.getEyePosition(partialTicks).add(carrier.getViewVector(partialTicks).scale(12));
        }
        int watched = getWatchTarget();
        if (watched >= 0 && this.level().getEntity(watched) instanceof Entity threat) {
            return threat.getPosition(partialTicks).add(0, threat.getBbHeight() * 0.5, 0);
        }
        return null;
    }

    public Vec3[] bodyFrame(float partialTicks) {
        Player carrier = getCarrier();
        Vec3 base;
        Vec3 forward;
        Vec3 up;
        if (carrier != null) {
            PerchPose pose = currentPerchPose(carrier, partialTicks);
            base = pose.position();
            forward = pose.axis(0, 0, 1);
            up = pose.axis(0, 1, 0);
        } else {
            base = this.getPosition(partialTicks);
            float yawRad = Mth.rotLerp(partialTicks, this.yBodyRotO, this.yBodyRot) * Mth.DEG_TO_RAD;
            float pitchRad = climbPitchDegrees(partialTicks) * Mth.DEG_TO_RAD;
            Vec3 horizontal = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
            forward = horizontal.scale(Mth.cos(pitchRad)).add(0, -Mth.sin(pitchRad), 0);
            up = new Vec3(0, Mth.cos(pitchRad), 0).add(horizontal.scale(Mth.sin(pitchRad)));
        }
        return new Vec3[]{base, forward, up, forward.cross(up)};
    }

    private static float[] anglesIn(Vec3 direction, Vec3 forward, Vec3 up, Vec3 right) {
        double f = direction.dot(forward);
        double u = direction.dot(up);
        double r = direction.dot(right);
        return new float[]{(float) Mth.atan2(r, f), (float) Mth.atan2(-u, Math.sqrt(r * r + f * f))};
    }

    private static Vec3 rotateAbout(Vec3 vector, Vec3 axis, double angle) {
        Vec3 k = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return vector.scale(cos).add(k.cross(vector).scale(sin)).add(k.scale(k.dot(vector) * (1 - cos)));
    }

    public @Nullable Vec3 renderedMouth = null;

    public @Nullable Vec3 aimTarget(float partialTicks) {
        Vec3 point = aimPoint(partialTicks);
        if (point != null) lastAimPoint = point;
        else if (Mth.lerp(partialTicks, aimWeightO, aimWeight) <= 0f) lastAimPoint = null;
        return lastAimPoint;
    }

    public @Nullable float[] aimShare(float partialTicks) {
        if (aimOverride != null) return aimOverride;
        float weight = Mth.lerp(partialTicks, aimWeightO, aimWeight);
        Vec3 point = aimTarget(partialTicks);
        if (weight <= 0.001f || point == null) return null;

        Vec3[] frame = bodyFrame(partialTicks);
        double scale = this.getScale();
        Vec3 pivotGuess = frame[0].add(frame[2].scale(HEAD_PIVOT_UP * scale));
        Vec3 toTarget = point.subtract(pivotGuess);
        if (toTarget.lengthSqr() < 1.0e-6) return null;
        float[] whole = anglesIn(toTarget.normalize(), frame[1], frame[2], frame[3]);

        float headShare = isCarried() ? Mth.lerp(partialTicks, perchBlendO, perchBlend) : 0f;
        float yawLimit = Mth.lerp(headShare * headShare * (3f - 2f * headShare), AIM_BACK_YAW_LIMIT, Mth.PI);
        float bodyYaw = Mth.clamp(whole[0], -yawLimit, yawLimit) * weight;
        float bodyPitch = Mth.clamp(whole[1], -AIM_BODY_PITCH_LIMIT, AIM_BODY_PITCH_LIMIT) * weight;

        Vec3 forward = rotateAbout(frame[1], frame[2], -bodyYaw);
        Vec3 right = rotateAbout(frame[3], frame[2], -bodyYaw);
        Vec3 up = frame[2];
        forward = rotateAbout(forward, right, -bodyPitch);
        up = rotateAbout(up, right, -bodyPitch);

        Vec3 pivot = frame[0].add(up.scale(HEAD_PIVOT_UP * scale)).add(forward.scale(HEAD_PIVOT_FORWARD * scale));
        Vec3 fromPivot = point.subtract(pivot);
        if (fromPivot.lengthSqr() < 1.0e-6) return new float[]{bodyYaw, bodyPitch, 0f, 0f};
        float[] head = anglesIn(fromPivot.normalize(), forward, up, right);
        return new float[]{bodyYaw, bodyPitch,
                Mth.clamp(head[0], -AIM_HEAD_LIMIT, AIM_HEAD_LIMIT) * weight,
                Mth.clamp(head[1], -AIM_HEAD_LIMIT, AIM_HEAD_LIMIT) * weight};
    }

    public Vec3 tongueOrigin(float partialTicks, Vec3 end) {
        Vec3[] frame = bodyFrame(partialTicks);
        Vec3 origin = frame[0].add(frame[2].scale(MOUTH_UP * this.getScale()));
        Vec3 direction = end.subtract(origin);
        if (direction.lengthSqr() < 1.0e-6) return origin;
        return origin.add(direction.normalize().scale(MOUTH_FORWARD * this.getScale()));
    }

    public boolean isMimicryActive() { return getMimicryTimer() > 0; }

    public boolean isTongueOut() { return getTongueState() != TONGUE_IDLE; }

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

        Player carrier = getCarrier();
        if (carrier != null) tag.putUUID("backCarrier", carrier.getUUID());
        else if (pendingCarrier != null) tag.putUUID("backCarrier", pendingCarrier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.pendingCarrier = tag.hasUUID("backCarrier") ? tag.getUUID("backCarrier") : null;
        this.pendingCarrierTicks = 0;
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(16, -2.0f, 3.0f, 0.8f)
    );

    @Override
    public List<OWSedationImpact> sedationImpacts() {
        return SEDATION_IMPACTS;
    }

    @Override
    protected int experienceRewardMin() {
        return 1;
    }

    @Override
    protected int experienceRewardMax() {
        return 3;
    }
}
