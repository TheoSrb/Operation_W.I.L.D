package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
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
import net.tiew.operationWild.entity.IOWCarriedCompanion;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.config.OWTargetLists;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.TarantulaVariant;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TarantulaEntity extends OWEntity implements IOWEntity, IOWTamable, IOWCarriedCompanion {

    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 25.0;
    public static final int MAX_SOMNOLENCE = 70;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 2;
    public static final int ENTITY_COLOR = 0x4f3429;
    public static final int DEFAULT_SKIN_INDEX = 0;

    public static final int SLOT_RIGHT = 0;
    public static final int SLOT_LEFT = 1;
    public static final int SLOT_NONE = -1;

    public static final int WEB_IDLE = 0;
    public static final int WEB_EXTENDING = 1;
    public static final int WEB_ATTACHED = 2;
    public static final int WEB_RETRACTING = 3;

    public static final int CLIMB_TICKS = 10;
    public static final float SHOULDER_BLEND_STEP = 1f / 9f;

    private static final int SEDATION_WAKE_TICKS = 50;
    private static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 30;
    private static final int FOOD_WANTED_MIN = 3;
    private static final int FOOD_WANTED_MAX = 5;
    private static final float MAX_VITAL_ENERGY = 220f;
    private static final int RED_KNEE_CHANCE = 20;
    private static final int CHILEAN_ROSE_CHANCE = 20;

    private static final double WRIST_HEIGHT = 0.86;
    private static final double WRIST_HEIGHT_CROUCH = 0.62;
    private static final double WRIST_SIDE = 0.37;
    private static final double WRIST_FORWARD = 0.06;
    private static final double CLIMB_ARC = 0.45;
    private static final double HOP_ARC = 0.35;
    private static final double HOP_DISTANCE = 0.9;
    private static final int CARRY_TOGGLE_COOLDOWN = 10;
    private static final double HUNTER_RELEASE_RANGE = 24.0;
    private static final int NAP_COOLDOWN_AFTER_DISMOUNT = 200;
    private static final int CARRIER_RESTORE_TIMEOUT = 600;
    private static final double CARRIER_RESTORE_RANGE = 8.0;
    private static final int MISC_IDLE_DURATION = 70;
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

    private static final int OUTCOME_MISS = 0;
    private static final int OUTCOME_ENTITY = 1;
    private static final int OUTCOME_ITEM = 2;
    private static final int OUTCOME_BLOCK = 3;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARRY_SLOT = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLIMB_TIMER = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMB_MOUNTING = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WEB_STATE = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WEB_LENGTH = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> WEB_PHASE_TICK = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEB_PHASE_TICKS = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<org.joml.Vector3f> WEB_END = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> WEB_TARGET = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> WEB_ITEM = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> WEB_SHOT_TIMER = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEB_COOLDOWN = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WEB_LOADED = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EAT_TIMER = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> EAT_ITEM = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> REFUSE_TIMER = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WALL_FACE = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WALL_DESCENDING = SynchedEntityData.defineId(TarantulaEntity.class, EntityDataSerializers.BOOLEAN);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState miscIdleAnimationState = new AnimationState();
    public final AnimationState threatAnimationState = new AnimationState();
    public final AnimationState hairFlickAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState armIdleAnimationState = new AnimationState();
    public final AnimationState webShotAnimationState = new AnimationState();
    public final AnimationState webHoldAnimationState = new AnimationState();
    public final AnimationState climbOnAnimationState = new AnimationState();
    public final AnimationState jumpOffAnimationState = new AnimationState();
    public final AnimationState refuseAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState selectAnimationState = new AnimationState();

    public int miscIdleAnimationStartTime = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public float webHoldWeight = 0f;
    public float webHoldWeightO = 0f;
    public @Nullable Vec3 renderedSpinneret = null;
    public long renderedSpinneretFrame = -1L;

    private int lastWebShotTimer = 0;
    private int lastClimbTimer = 0;
    private int lastRefuseTimer = 0;
    private int lastEatTimer = 0;
    private int selectAnimationStart = -1;
    public long selectFlashStartMs = -1L;
    public float shoulderBlend = 0f;
    public float shoulderBlendO = 0f;
    private int lastClientWebState = WEB_IDLE;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);

    private int carryCooldown = 0;
    private int napCooldown = 0;
    private Vec3 climbStart = null;
    private Vec3 hopTarget = null;
    private float hopYaw = 0f;
    private int climbCarrierId = -1;
    private int climbSlot = SLOT_NONE;
    private UUID pendingCarrier = null;
    private int pendingCarrierSlot = SLOT_RIGHT;
    private int pendingCarrierTicks = 0;

    private int webOutcome = OUTCOME_MISS;
    private int webItemEntityId = -1;
    private BlockPos webAnchorBlock = null;
    private Direction webAnchorFace = null;
    private int anchorTicks = 0;
    private int shotWindup = -1;

    private int wallTrend = 0;
    public float wallPitch = 0f;
    public float wallPitchO = 0f;
    public int lastWallFace = -1;

    public TarantulaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true, true));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(CARRY_SLOT, SLOT_NONE);
        builder.define(CLIMB_TIMER, 0);
        builder.define(CLIMB_MOUNTING, true);
        builder.define(WEB_STATE, WEB_IDLE);
        builder.define(WEB_LENGTH, 0f);
        builder.define(WEB_PHASE_TICK, 0);
        builder.define(WEB_PHASE_TICKS, 1);
        builder.define(WEB_END, new org.joml.Vector3f());
        builder.define(WEB_TARGET, -1);
        builder.define(WEB_ITEM, ItemStack.EMPTY);
        builder.define(WEB_SHOT_TIMER, 0);
        builder.define(WEB_COOLDOWN, 0);
        builder.define(WEB_LOADED, false);
        builder.define(EAT_TIMER, 0);
        builder.define(EAT_ITEM, ItemStack.EMPTY);
        builder.define(REFUSE_TIMER, 0);
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
        return 1.5f;
    }

    @Override
    public double napParticleHeight() { return 0.1; }

    @Override
    public double napParticleForward() { return 0.35; }

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
        return 2.4f;
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
        return OWEntityConfig.Archetypes.MARAUDER;
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
    public List<Class<?>> getFavoriteTargetsByBeingNonTame() {
        return OWTargetLists.TARANTULA;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.TARANTULA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return MAX_VITAL_ENERGY * (1f + this.getLevel() / 100f);
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.25f * (1 + ((float) this.getLevel() / 50));
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
        return 0.3f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.TARANTULA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.TARANTULA_FOOD);
    }

    @Override
    public Item favoriteFoodTier2() {
        return Items.RABBIT;
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
        return OWSounds.TARANTULA_AMBIENT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.TARANTULA_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.TARANTULA_DEATH.get();
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround() || this.isInWater() || this.isPassenger()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 150L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        SoundType soundtype = blockState.getSoundType(this.level(), this.blockPosition(), this);
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundtype.getStepSound(),
                this.getSoundSource(), soundtype.getVolume() * 0.05F, soundtype.getPitch() * pitchMod, false);
    }

    public void onFirstTetrapodDown() {
        playStepSoundFromAnimation(1.8f);
    }

    public void onSecondTetrapodDown() {
        playStepSoundFromAnimation(2.0f);
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

        setTamingPercentage(this.foodGiven, this.foodWanted);
        tickWebHoldWeight();
        tickWallPitch();

        if (this.level().isClientSide()) {
            tickShoulderBlend();
            setupAnimationState();
            tickWebParticles();
        }
        if (this.isInResurrection()) this.setSleeping(true);

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            if (getWebShotTimer() > 0) this.entityData.set(WEB_SHOT_TIMER, getWebShotTimer() - 1);
            if (getWebCooldown() > 0) this.entityData.set(WEB_COOLDOWN, getWebCooldown() - 1);
            if (getRefuseTimer() > 0) this.entityData.set(REFUSE_TIMER, getRefuseTimer() - 1);

            handleCarrierClimb();
            handleCarrierRestore();
            handleReload();
            handleWindup();
            handleWeb();
            handleWallClimb();

            if (isCarried() && (!this.isTame() || this.isInResurrection())) dismountFromCarrier();
        }
    }

    private void tickWebHoldWeight() {
        this.webHoldWeightO = this.webHoldWeight;
        float target = getWebState() == WEB_ATTACHED ? 1f : 0f;
        if (target == 1f && getWebShotTimer() > 0) {
            this.webHoldWeight = 1f;
            this.webHoldWeightO = 1f;
            return;
        }
        this.webHoldWeight += (target - this.webHoldWeight) * 0.3f;
        if (target == 0f && this.webHoldWeight < 0.005f) this.webHoldWeight = 0f;
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
        return isWebOut() || shotWindup >= 0;
    }

    @Override
    protected double followOwnerSpeedFactor() {
        return 10;
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

        int slot = freeSlot(player);
        if (slot == SLOT_NONE) return InteractionResult.PASS;

        if (isNapping() || isSleeping()) {
            setNap(false);
            setSleeping(false);
        }

        beginClimb(player, slot);
        return InteractionResult.SUCCESS;
    }

    public static int freeSlot(Player carrier) {
        boolean rightTaken = false;
        boolean leftTaken = false;
        for (Entity passenger : carrier.getPassengers()) {
            if (!(passenger instanceof TarantulaEntity tarantula)) return SLOT_NONE;
            if (tarantula.carrySlot() == SLOT_RIGHT) rightTaken = true;
            else leftTaken = true;
        }
        if (!rightTaken) return SLOT_RIGHT;
        return leftTaken ? SLOT_NONE : SLOT_LEFT;
    }

    private void beginClimb(Player carrier, int slot) {
        this.setSitting(false);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
        this.climbStart = this.position();
        this.climbCarrierId = carrier.getId();
        this.climbSlot = slot;
        this.entityData.set(CLIMB_MOUNTING, true);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);
        carryCooldown = CARRY_TOGGLE_COOLDOWN + CLIMB_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.TARANTULA_HOP.get(), SoundSource.NEUTRAL, 0.7f, (float) OWUtils.generateRandomInterval(1.0, 1.15));
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
        Vec3 to = mounting ? wristPosition(carrier, climbSlot, 1f) : (hopTarget == null ? this.position() : hopTarget);

        double eased = progress * progress * (3 - 2 * progress);
        double arc = (mounting ? CLIMB_ARC : HOP_ARC) * 4.0 * progress * (1 - progress);
        this.setPos(from.lerp(to, eased).add(0, arc, 0));

        float yaw = mounting ? Mth.rotLerp((float) eased, this.getYRot(), carrier.yBodyRot) : hopYaw;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);

        if (remaining > 0) return;

        int slot = climbSlot;
        abortClimb();
        if (!mounting) {
            this.setDeltaMovement(0, -0.1, 0);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    OWSounds.TARANTULA_HOP.get(), SoundSource.NEUTRAL, 0.8f, 0.95f);
            return;
        }
        perchOn(carrier, slot);
    }

    private boolean perchOn(Player carrier, int slot) {
        if (slot == SLOT_NONE || freeSlot(carrier) == SLOT_NONE) return false;
        for (Entity passenger : carrier.getPassengers()) {
            if (passenger instanceof TarantulaEntity other && other.carrySlot() == slot) slot = 1 - slot;
        }
        this.entityData.set(CARRY_SLOT, slot);
        if (!this.startRiding(carrier, true)) {
            this.entityData.set(CARRY_SLOT, SLOT_NONE);
            return false;
        }
        syncPassengersToCarrier(carrier);
        shakeOffHunters();
        this.level().playSound(null, carrier.getX(), carrier.getY() + 1.0, carrier.getZ(),
                OWSounds.TARANTULA_SCUTTLE.get(), SoundSource.NEUTRAL, 0.8f, 1.0f);
        this.level().playSound(null, carrier.getX(), carrier.getY() + 1.0, carrier.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.5f, 1.7f);
        return true;
    }

    private void abortClimb() {
        this.entityData.set(CLIMB_TIMER, 0);
        this.noPhysics = false;
        this.climbStart = null;
        this.hopTarget = null;
        this.climbCarrierId = -1;
        this.climbSlot = SLOT_NONE;
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

        int slot = pendingCarrierSlot;
        pendingCarrier = null;
        pendingCarrierTicks = 0;
        perchOn(carrier, slot);
    }

    @Override
    public void dismountFromCarrier() {
        Player carrier = getCarrier();
        int slot = carrySlot();
        releaseWeb(false);
        shotWindup = -1;
        Vec3 start = carrier == null ? this.position() : wristPosition(carrier, slot, 1f);
        this.stopRiding();
        this.entityData.set(CARRY_SLOT, SLOT_NONE);
        carryCooldown = CARRY_TOGGLE_COOLDOWN + CLIMB_TICKS;
        napCooldown = NAP_COOLDOWN_AFTER_DISMOUNT;

        if (carrier == null) return;
        syncPassengersToCarrier(carrier);

        float yaw = carrier.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 side = sideAxis(carrier.yBodyRot).scale(slot == SLOT_LEFT ? -1 : 1);
        Vec3 landing = new Vec3(carrier.getX() - Mth.sin(yaw) * HOP_DISTANCE + side.x * 0.4, carrier.getY(),
                carrier.getZ() + Mth.cos(yaw) * HOP_DISTANCE + side.z * 0.4);
        boolean free = this.level().noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(landing));
        this.hopTarget = free ? landing : carrier.position();
        this.hopYaw = carrier.yBodyRot;
        this.climbStart = start;
        this.climbCarrierId = carrier.getId();
        this.setPos(start);
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
        this.entityData.set(CLIMB_MOUNTING, false);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.TARANTULA_HOP.get(), SoundSource.NEUTRAL, 0.7f, 1.1f);
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

    @Override
    public int carrySlot() {
        int slot = this.entityData.get(CARRY_SLOT);
        return slot == SLOT_LEFT ? SLOT_LEFT : SLOT_RIGHT;
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMB_TIMER) > 0;
    }

    public @Nullable Player getCarrier() {
        return this.getVehicle() instanceof Player player ? player : null;
    }

    public static @Nullable TarantulaEntity carriedOn(@Nullable Player player, int slot) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof TarantulaEntity tarantula && tarantula.carrySlot() == slot) return tarantula;
        }
        return null;
    }

    public static boolean carriesAny(@Nullable Player player) {
        if (player == null) return false;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof TarantulaEntity) return true;
        }
        return false;
    }

    @Override
    public boolean canStartNap() {
        return napCooldown <= 0 && !isClimbing() && !isCarried();
    }

    private static Vec3 sideAxis(float bodyYaw) {
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
    }

    public static Vec3 wristPosition(Player carrier, int slot, float partialTicks) {
        float bodyYaw = ChameleonEntity.renderedBodyYaw(carrier, partialTicks);
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        double scale = carrier.getScale();
        double height = (carrier.isCrouching() ? WRIST_HEIGHT_CROUCH : WRIST_HEIGHT) * scale;
        Vec3 side = sideAxis(bodyYaw).scale((slot == SLOT_LEFT ? -1 : 1) * WRIST_SIDE * scale);
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).scale(WRIST_FORWARD * scale);
        return carrier.getPosition(partialTicks).add(0, height, 0).add(side).add(forward);
    }

    public Vec3 spinneretPosition(float partialTicks) {
        Player carrier = getCarrier();
        if (carrier == null) {
            float yaw = Mth.rotLerp(partialTicks, this.yBodyRotO, this.yBodyRot) * Mth.DEG_TO_RAD;
            double back = 0.45 * this.getScale();
            return this.getPosition(partialTicks).add(Mth.sin(yaw) * back, 0.3 * this.getScale(), -Mth.cos(yaw) * back);
        }
        return wristPosition(carrier, carrySlot(), partialTicks);
    }

    public static Vec3 ropePoint(Player player) {
        return player.position().add(0, player.getBbHeight() * 0.55, 0);
    }

    @Override
    public void rideTick() {
        super.rideTick();

        Player carrier = getCarrier();
        if (carrier == null) return;

        this.getNavigation().stop();
        this.setPos(wristPosition(carrier, carrySlot(), 1f));
        this.setYRot(carrier.yBodyRot);
        this.yBodyRot = carrier.yBodyRot;
        this.setYHeadRot(carrier.yBodyRot);
        this.fallDistance = 0f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
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
        releaseWeb(false);
        if (isCarried()) this.stopRiding();
        super.die(damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    public double webRange() {
        return OWPistePassives.has(this, OWPistePassives.SILK_REACH)
                ? OWAttacksConstants.Tarantula.WEB_RANGE_SILK_REACH
                : OWAttacksConstants.Tarantula.WEB_RANGE;
    }

    public double reelSpeedFactor() {
        return OWPistePassives.has(this, OWPistePassives.FAST_SPOOL) ? OWAttacksConstants.Tarantula.REEL_FAST_SPOOL_FACTOR : 1.0;
    }

    public boolean canShootWeb() {
        return getWebState() == WEB_IDLE && shotWindup < 0 && getWebCooldown() <= 0 && isWebLoaded() && !isEating()
                && getVitalEnergy() <= getVitalEnergyCapacity() - OWAttacksConstants.Tarantula.WEB_ENERGY;
    }

    public void useWeb() {
        if (this.level().isClientSide()) return;

        Player carrier = getCarrier();
        if (carrier == null || carrier.getVehicle() != null) return;

        int state = getWebState();
        if (state == WEB_ATTACHED) {
            releaseWeb(true);
            return;
        }
        if (state != WEB_IDLE || shotWindup >= 0 || getWebCooldown() > 0 || getRefuseTimer() > 0 || isEating()) return;

        if (!isWebLoaded()) {
            refuseWeb();
            return;
        }

        float cost = OWAttacksConstants.Tarantula.WEB_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        this.entityData.set(WEB_LOADED, false);

        shotWindup = OWAttacksConstants.Tarantula.WEB_WINDUP_TICKS;
        this.entityData.set(WEB_SHOT_TIMER, OWAttacksConstants.Tarantula.WEB_SHOT_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.TARANTULA_SCUTTLE.get(), SoundSource.NEUTRAL, 0.6f, 1.25f);
    }

    private void refuseWeb() {
        this.entityData.set(REFUSE_TIMER, OWAttacksConstants.Tarantula.REFUSE_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TARANTULA_REFUSE.get(), SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(1.0, 1.15));
    }

    private void handleReload() {
        int eating = getEatTimer();
        boolean ready = isCarried() && getWebState() == WEB_IDLE && shotWindup < 0 && getWebCooldown() <= 0;
        if (eating > 0) {
            if (!ready) {
                this.entityData.set(EAT_TIMER, 0);
                this.entityData.set(EAT_ITEM, ItemStack.EMPTY);
                return;
            }
            this.entityData.set(EAT_TIMER, --eating);
            if (eating % OWAttacksConstants.Tarantula.EAT_SOUND_INTERVAL == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TARANTULA_EAT.get(), SoundSource.NEUTRAL, 0.6f,
                        (float) OWUtils.generateRandomInterval(1.6, 1.9));
            }
            if (eating > 0) return;
            ItemStack food = this.getInventory().getStackInSlot(FOOD_SLOT);
            if (!food.isEmpty()) {
                consumeSilkFood(food);
                this.entityData.set(WEB_LOADED, true);
            }
            this.entityData.set(EAT_ITEM, ItemStack.EMPTY);
            return;
        }
        if (!ready || isWebLoaded()) return;
        ItemStack food = this.getInventory().getStackInSlot(FOOD_SLOT);
        if (food.isEmpty()) return;
        this.entityData.set(EAT_ITEM, food.copyWithCount(1));
        this.entityData.set(EAT_TIMER, OWAttacksConstants.Tarantula.EAT_TICKS);
    }

    private void tickShoulderBlend() {
        this.shoulderBlendO = this.shoulderBlend;
        Player carrier = getCarrier();
        float target = carrier != null && handHoldsItem(carrier, carrySlot()) ? 1f : 0f;
        if (this.shoulderBlend < target) this.shoulderBlend = Math.min(target, this.shoulderBlend + SHOULDER_BLEND_STEP);
        else if (this.shoulderBlend > target) this.shoulderBlend = Math.max(target, this.shoulderBlend - SHOULDER_BLEND_STEP);
    }

    public float shoulderBlend(float partialTicks) {
        float blend = Mth.lerp(partialTicks, shoulderBlendO, shoulderBlend);
        return blend * blend * (3f - 2f * blend);
    }

    public static boolean handHoldsItem(Player carrier, int slot) {
        net.minecraft.world.entity.HumanoidArm arm = slot == SLOT_LEFT ? net.minecraft.world.entity.HumanoidArm.LEFT : net.minecraft.world.entity.HumanoidArm.RIGHT;
        InteractionHand hand = carrier.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return !carrier.getItemInHand(hand).isEmpty();
    }

    public void flashSelection() {
        this.selectFlashStartMs = System.currentTimeMillis();
        this.selectAnimationStart = this.tickCount;
        this.selectAnimationState.start(this.tickCount);
    }

    public float selectionGlow() {
        if (selectFlashStartMs < 0) return 0f;
        float progress = (System.currentTimeMillis() - selectFlashStartMs) / (float) OWAttacksConstants.Tarantula.SELECT_FLASH_MS;
        return progress >= 1f ? 0f : 1f - progress * progress;
    }

    private void consumeSilkFood(ItemStack food) {
        food.shrink(1);
        this.setFoodCount(food.getCount());
        if (food.isEmpty()) {
            this.setFed(false);
            this.setItemFood(ItemStack.EMPTY);
        }
    }

    private void tickWebParticles() {
        int state = getWebState();
        Vec3 origin = spinneretPosition(1f);
        Vec3 end = webEnd();
        if (state == WEB_EXTENDING && lastClientWebState != WEB_EXTENDING) {
            silkBurst(origin, 6, 0.06);
        }
        if (state == WEB_EXTENDING) {
            Vec3 direction = end.subtract(origin);
            double length = direction.length();
            if (length > 1.0e-3) {
                Vec3 tip = origin.add(direction.scale(Math.min(1.0, currentWebLength(1f) / length)));
                silkBurst(tip, 2, 0.02);
            }
        }
        if (state == WEB_ATTACHED && lastClientWebState != WEB_ATTACHED) {
            silkBurst(end, 10, 0.12);
        }
        ItemStack meal = getEatItem();
        if (isEating() && !meal.isEmpty() && this.tickCount % 3 == 0) {
            Vec3 mouth = origin.add(0, 0.12, 0);
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, meal), mouth.x, mouth.y, mouth.z,
                        (this.random.nextDouble() - 0.5) * 0.08, this.random.nextDouble() * 0.08, (this.random.nextDouble() - 0.5) * 0.08);
            }
        }
        if (state == WEB_ATTACHED && this.random.nextInt(3) == 0) {
            Vec3 along = origin.lerp(end, this.random.nextDouble());
            this.level().addParticle(ParticleTypes.WHITE_ASH, along.x, along.y, along.z, 0, -0.01, 0);
        }
        if (lastClientWebState == WEB_ATTACHED && state != WEB_ATTACHED) {
            for (int i = 0; i < 12; i++) {
                Vec3 along = origin.lerp(end, this.random.nextDouble());
                silkBurst(along, 1, 0.05);
            }
            silkBurst(end, 8, 0.1);
        }
        lastClientWebState = state;
    }

    private void silkBurst(Vec3 at, int count, double spread) {
        BlockParticleOption cobweb = new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState());
        for (int i = 0; i < count; i++) {
            double dx = (this.random.nextDouble() - 0.5) * spread * 2;
            double dy = (this.random.nextDouble() - 0.5) * spread * 2;
            double dz = (this.random.nextDouble() - 0.5) * spread * 2;
            this.level().addParticle(i % 2 == 0 ? cobweb : ParticleTypes.WHITE_ASH, at.x + dx, at.y + dy, at.z + dz, dx * 0.2, dy * 0.2 + 0.01, dz * 0.2);
        }
    }

    private void handleWindup() {
        if (shotWindup < 0) return;

        Player carrier = getCarrier();
        if (carrier == null || carrier.getVehicle() != null) {
            shotWindup = -1;
            return;
        }
        if (--shotWindup > 0) return;
        shotWindup = -1;
        aimWeb(carrier);
    }

    private void aimWeb(Player carrier) {
        double range = webRange();
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
            AABB box = candidate.getBoundingBox().inflate(OWAttacksConstants.Tarantula.WEB_ENTITY_PICK_RADIUS);
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
            AABB box = candidate.getBoundingBox().inflate(OWAttacksConstants.Tarantula.WEB_ITEM_PICK_RADIUS);
            var clip = box.clip(eye, far);
            if (clip.isEmpty()) continue;
            double distance = clip.get().distanceTo(eye);
            if (distance < itemDistance) {
                itemDistance = distance;
                itemHit = candidate;
            }
        }

        webItemEntityId = -1;
        webAnchorBlock = null;

        if (itemHit != null) {
            webOutcome = OUTCOME_ITEM;
            webItemEntityId = itemHit.getId();
            launchWeb(itemHit.position().add(0, ITEM_GRAB_LIFT, 0), -1);
        } else if (entityHit != null) {
            webOutcome = OUTCOME_ENTITY;
            launchWeb(entityHit.getBoundingBox().getCenter(), entityHit.getId());
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            webOutcome = OUTCOME_BLOCK;
            webAnchorBlock = blockHit.getBlockPos();
            webAnchorFace = blockHit.getDirection();
            launchWeb(blockHit.getLocation(), -1);
        } else {
            webOutcome = OUTCOME_MISS;
            launchWeb(far, -1);
        }
    }

    private static boolean isFriendOf(Player carrier, LivingEntity entity) {
        if (entity.isAlliedTo(carrier)) return true;
        if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(carrier)) return true;
        return entity.getVehicle() == carrier || carrier.getVehicle() == entity;
    }

    private static boolean isHostileOrNeutral(Mob mob) {
        if (mob instanceof OWEntity owEntity) return owEntity.getTemperament() != OWEntityConfig.Temperament.PASSIVE;
        return mob instanceof net.minecraft.world.entity.monster.Enemy || mob instanceof net.minecraft.world.entity.NeutralMob;
    }

    private void launchWeb(Vec3 end, int targetId) {
        Vec3 origin = spinneretPosition(1f);
        float reach = (float) end.distanceTo(origin);
        this.entityData.set(WEB_END, end.toVector3f());
        this.entityData.set(WEB_TARGET, targetId);
        this.entityData.set(WEB_ITEM, ItemStack.EMPTY);
        this.entityData.set(WEB_LENGTH, reach);
        this.entityData.set(WEB_PHASE_TICK, 0);
        this.entityData.set(WEB_PHASE_TICKS, phaseTicks(reach, OWAttacksConstants.Tarantula.WEB_EXTEND_SPEED,
                OWAttacksConstants.Tarantula.WEB_EXTEND_MAX_TICKS));
        this.entityData.set(WEB_STATE, WEB_EXTENDING);
        this.anchorTicks = 0;

        this.level().playSound(null, origin.x, origin.y, origin.z,
                OWSounds.TARANTULA_WEB_SHOT.get(), SoundSource.NEUTRAL, 0.7f, (float) OWUtils.generateRandomInterval(0.95, 1.1));
        burst(ParticleTypes.WHITE_ASH, origin, 6, 0.05, 0.02);
    }

    private void burst(ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
        }
    }

    private static int phaseTicks(float length, double speed, int maxTicks) {
        return Mth.clamp(Mth.ceil(length / speed), 2, maxTicks);
    }

    private void handleWeb() {
        switch (getWebState()) {
            case WEB_EXTENDING -> extendWeb();
            case WEB_ATTACHED -> holdAnchor();
            case WEB_RETRACTING -> retractWeb();
            default -> {
            }
        }
    }

    private void extendWeb() {
        Vec3 origin = spinneretPosition(1f);
        Entity target = getWebTarget() >= 0 ? this.level().getEntity(getWebTarget()) : null;
        if (target != null && target.isAlive()) {
            Vec3 center = target.getBoundingBox().getCenter();
            this.entityData.set(WEB_END, center.toVector3f());
            this.entityData.set(WEB_LENGTH, (float) center.distanceTo(origin));
        } else if (webOutcome == OUTCOME_ITEM && this.level().getEntity(webItemEntityId) instanceof ItemEntity item && item.isAlive()) {
            Vec3 held = item.position().add(0, ITEM_GRAB_LIFT, 0);
            this.entityData.set(WEB_END, held.toVector3f());
            this.entityData.set(WEB_LENGTH, (float) held.distanceTo(origin));
        }

        int phase = getWebPhaseTick() + 1;
        this.entityData.set(WEB_PHASE_TICK, phase);
        if (phase < getWebPhaseTicks()) return;

        switch (webOutcome) {
            case OUTCOME_ENTITY -> strikeWebTarget(target);
            case OUTCOME_ITEM -> catchItem();
            case OUTCOME_BLOCK -> attachWeb();
            default -> startRetract(1);
        }
    }

    private void strikeWebTarget(@Nullable Entity target) {
        if (target instanceof LivingEntity living && living.isAlive()) {
            living.hurt(this.damageSources().mobAttack(this), this.getCombatDamage() * OWAttacksConstants.Tarantula.WEB_DAMAGE_RATIO);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, OWAttacksConstants.Tarantula.WEB_SLOWNESS_TICKS,
                    OWAttacksConstants.Tarantula.WEB_SLOWNESS_AMPLIFIER), this);
            Player carrier = getCarrier();
            if (carrier != null && living instanceof Mob mob && mob.isAlive() && isHostileOrNeutral(mob) && mob.canAttack(carrier)) {
                mob.setTarget(carrier);
            }
            Vec3 hit = living.getBoundingBox().getCenter();
            this.level().playSound(null, hit.x, hit.y, hit.z, OWSounds.TARANTULA_WEB_STICK.get(), SoundSource.NEUTRAL, 1.0f, 1.2f);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState()), hit, 14, 0.25, 0.1);
            burst(ParticleTypes.CRIT, hit, 8, 0.25, 0.25);
        }
        startRetract(OWAttacksConstants.Tarantula.WEB_IMPACT_HOLD_TICKS);
    }

    private void catchItem() {
        if (this.level().getEntity(webItemEntityId) instanceof ItemEntity item && item.isAlive()) {
            this.entityData.set(WEB_ITEM, item.getItem().copy());
            burst(new ItemParticleOption(ParticleTypes.ITEM, item.getItem()), item.position(), 8, 0.1, 0.08);
            this.level().playSound(null, item.getX(), item.getY(), item.getZ(), OWSounds.TARANTULA_WEB_STICK.get(), SoundSource.NEUTRAL, 0.7f, 1.5f);
            item.discard();
        }
        startRetract(OWAttacksConstants.Tarantula.WEB_IMPACT_HOLD_TICKS);
    }

    private void attachWeb() {
        Player holder = getCarrier();
        boolean submerged = holder != null && (holder.isUnderWater() || holder.isInWater());
        if (webAnchorBlock == null || this.level().getBlockState(webAnchorBlock).isAir() || submerged || isAnchorUnderwater()) {
            startRetract(1);
            return;
        }
        this.entityData.set(WEB_STATE, WEB_ATTACHED);
        Vec3 anchor = webEnd();
        this.level().playSound(null, anchor.x, anchor.y, anchor.z, OWSounds.TARANTULA_WEB_STICK.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        burst(new BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(webAnchorBlock)), anchor, 10, 0.15, 0.15);
        burst(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState()), anchor, 10, 0.12, 0.05);
    }

    private boolean isAnchorUnderwater() {
        if (webAnchorBlock == null) return false;
        if (!this.level().getFluidState(webAnchorBlock).isEmpty()) return true;
        BlockPos front = webAnchorFace == null ? BlockPos.containing(webEnd()) : webAnchorBlock.relative(webAnchorFace);
        return !this.level().getFluidState(front).isEmpty();
    }

    private void holdAnchor() {
        Player carrier = getCarrier();
        if (carrier == null || webAnchorBlock == null) {
            releaseWeb(false);
            return;
        }

        carrier.fallDistance = 0f;
        setVitalEnergy(getVitalEnergy() + OWAttacksConstants.Tarantula.ANCHOR_ENERGY_PER_TICK);
        if (getVitalEnergy() >= getVitalEnergyCapacity()) {
            setVitalEnergy(getVitalEnergyCapacity());
            canShowVitalEnergyLack = true;
            releaseWeb(true);
            return;
        }
        boolean blockGone = this.level().getBlockState(webAnchorBlock).isAir();
        boolean tooFar = ropePoint(carrier).distanceTo(webEnd()) > webRange() + OWAttacksConstants.Tarantula.ANCHOR_RELEASE_SLACK;
        if (blockGone || tooFar || carrier.getVehicle() != null || ++anchorTicks > OWAttacksConstants.Tarantula.ANCHOR_MAX_TICKS) {
            releaseWeb(true);
        }
    }

    public void releaseWeb(boolean playSound) {
        int state = getWebState();
        if (state == WEB_IDLE) {
            shotWindup = -1;
            return;
        }

        Player carrier = getCarrier();
        if (carrier != null) carrier.fallDistance = 0f;

        if (state == WEB_ATTACHED || state == WEB_EXTENDING) {
            this.entityData.set(WEB_LENGTH, currentWebLength(0f));
            startRetract(0);
        }
        if (playSound && state == WEB_ATTACHED) {
            Vec3 end = webEnd();
            this.level().playSound(null, end.x, end.y, end.z, OWSounds.TARANTULA_WEB_UNSTICK.get(), SoundSource.NEUTRAL, 0.9f, 1.0f);
        }
        if (!this.isAlive() || carrier == null) finishWeb();
    }

    private void startRetract(int holdTicks) {
        this.entityData.set(WEB_TARGET, -1);
        this.entityData.set(WEB_PHASE_TICK, -holdTicks);
        this.entityData.set(WEB_PHASE_TICKS, phaseTicks(getWebLength(), OWAttacksConstants.Tarantula.WEB_RETRACT_SPEED,
                OWAttacksConstants.Tarantula.WEB_RETRACT_MAX_TICKS));
        this.entityData.set(WEB_STATE, WEB_RETRACTING);
        webAnchorBlock = null;
    }

    private void retractWeb() {
        int phase = getWebPhaseTick() + 1;
        this.entityData.set(WEB_PHASE_TICK, phase);
        if (phase >= getWebPhaseTicks()) finishWeb();
    }

    private void finishWeb() {
        ItemStack caught = getWebItem();
        Player carrier = getCarrier();
        if (!caught.isEmpty()) {
            if (carrier != null) {
                if (!carrier.getInventory().add(caught.copy())) carrier.drop(caught.copy(), false);
                this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.6f);
            } else {
                this.spawnAtLocation(caught.copy());
            }
        }

        this.entityData.set(WEB_ITEM, ItemStack.EMPTY);
        this.entityData.set(WEB_LENGTH, 0f);
        this.entityData.set(WEB_PHASE_TICK, 0);
        this.entityData.set(WEB_TARGET, -1);
        this.entityData.set(WEB_STATE, WEB_IDLE);
        this.entityData.set(WEB_COOLDOWN, OWAttacksConstants.Tarantula.WEB_COOLDOWN_TICKS);
        webAnchorBlock = null;
        webAnchorFace = null;
        webItemEntityId = -1;
        webOutcome = OUTCOME_MISS;
        anchorTicks = 0;
        shotWindup = -1;
    }

    public float currentWebLength(float partialTicks) {
        int state = getWebState();
        float reach = getWebLength();
        float t = Mth.clamp((getWebPhaseTick() + partialTicks) / Math.max(1, getWebPhaseTicks()), 0f, 1f);
        if (state == WEB_EXTENDING) return reach * (1f - (1f - t) * (1f - t));
        if (state == WEB_RETRACTING) return reach * (1f - t * t);
        return state == WEB_IDLE ? 0f : reach;
    }

    private boolean canWallClimb() {
        return this.isAlive() && !this.isCarried() && !this.isPassenger() && !this.isClimbing() && !this.isInWater()
                && !this.isNapping() && !this.isSleeping() && !this.isKnockedOut() && !this.isSitting() && !this.noPhysics;
    }

    private @Nullable Direction findWall() {
        AABB box = this.getBoundingBox().deflate(0, WALL_PROBE_DEFLATE, 0);
        Vec3 facing = Vec3.directionFromRotation(0, this.getYRot());
        int current = getWallFace();
        Direction best = null;
        double bestScore = -Double.MAX_VALUE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
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
        Direction wall = canWallClimb() ? findWall() : null;
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

    private boolean nearLedge(Direction wall) {
        AABB above = this.getBoundingBox().deflate(0, WALL_PROBE_DEFLATE, 0)
                .move(wall.getStepX() * WALL_PROBE, WALL_MANTLE_REACH, wall.getStepZ() * WALL_PROBE);
        return this.level().noCollision(this, above);
    }

    private boolean facesWall(Direction wall) {
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
        Direction wall = Direction.from2DDataValue(face);
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

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        boolean resting = isNapping() || isSleeping();
        boolean carried = isCarried();

        if (carried) this.armIdleAnimationState.startIfStopped(this.tickCount);
        else this.armIdleAnimationState.stop();

        if (getWebState() == WEB_ATTACHED) this.webHoldAnimationState.startIfStopped(this.tickCount);
        else if (this.webHoldWeight <= 0f) this.webHoldAnimationState.stop();

        int shotTimer = getWebShotTimer();
        if (shotTimer > lastWebShotTimer) this.webShotAnimationState.start(this.tickCount);
        else if (shotTimer <= 0 || getWebState() == WEB_ATTACHED) this.webShotAnimationState.stop();
        lastWebShotTimer = shotTimer;

        int eatTimer = getEatTimer();
        if (eatTimer > lastEatTimer) this.eatAnimationState.start(this.tickCount);
        else if (eatTimer <= 0) this.eatAnimationState.stop();
        lastEatTimer = eatTimer;

        if (selectAnimationStart >= 0 && this.tickCount - selectAnimationStart > OWAttacksConstants.Tarantula.SELECT_ANIMATION_TICKS) {
            this.selectAnimationState.stop();
            selectAnimationStart = -1;
        }

        int refuseTimer = getRefuseTimer();
        if (refuseTimer > lastRefuseTimer) this.refuseAnimationState.start(this.tickCount);
        else if (refuseTimer <= 0) this.refuseAnimationState.stop();
        lastRefuseTimer = refuseTimer;

        int climbTimer = this.entityData.get(CLIMB_TIMER);
        if (climbTimer > lastClimbTimer) {
            if (isClimbMounting()) this.climbOnAnimationState.start(this.tickCount);
            else this.jumpOffAnimationState.start(this.tickCount);
        }
        if (climbTimer <= 0) {
            this.climbOnAnimationState.stop();
            this.jumpOffAnimationState.stop();
        }
        lastClimbTimer = climbTimer;

        if (resting && !carried) this.napAnimationState.startIfStopped(this.tickCount);
        else this.napAnimationState.stop();

        if (isSitting() && !resting && !carried) this.sittingAnimationState.startIfStopped(this.tickCount);
        else this.sittingAnimationState.stop();

        this.idleAnimationState.startIfStopped(this.tickCount);
        handleMiscIdleAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isPassenger() && !this.isInWater() && !this.isSitting() && !this.isWallClimbing() && this.wallPitch == 0f;
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted() || this.threatAnimationState.isStarted() || this.hairFlickAnimationState.isStarted();
    }

    protected void handleMiscIdleAnimations() {
        if (this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_DURATION || !canPlayIdleAnimation()) {
            this.miscIdleAnimationState.stop();
            this.threatAnimationState.stop();
            this.hairFlickAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            int pick = this.random.nextInt(4);
            AnimationState chosen = pick == 0 ? this.threatAnimationState : pick == 1 ? this.hairFlickAnimationState : this.miscIdleAnimationState;
            chosen.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);
    }

    private TarantulaVariant chooseVariant() {
        int roll = this.random.nextInt(100);
        if (roll < RED_KNEE_CHANCE) return TarantulaVariant.RED_KNEE;
        if (roll < RED_KNEE_CHANCE + CHILEAN_ROSE_CHANCE) return TarantulaVariant.CHILEAN_ROSE;
        return TarantulaVariant.DEFAULT;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            TarantulaVariant variant = chooseVariant();
            this.setVariant(variant);
            this.setInitialVariant(variant);
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
        if (entity instanceof TarantulaEntity tarantula) {
            tarantula.setVariant(TarantulaVariant.byId(variant));
            tarantula.setInitialVariant(TarantulaVariant.byId(variant));
        }
    }

    public TarantulaVariant getVariant() { return TarantulaVariant.byId(this.getTypeVariant() & 255); }

    public void setVariant(TarantulaVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255); }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
    }

    @Override
    public void changeSkinSilent(int skinIndex) { changeSkin(skinIndex, false); }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public TarantulaVariant getInitialVariant() { return TarantulaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT)); }

    public void setInitialVariant(TarantulaVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId()); }

    public int getWebState() { return this.entityData.get(WEB_STATE); }

    public float getWebLength() { return this.entityData.get(WEB_LENGTH); }

    public int getWebPhaseTick() { return this.entityData.get(WEB_PHASE_TICK); }

    public int getWebPhaseTicks() { return this.entityData.get(WEB_PHASE_TICKS); }

    public Vec3 webEnd() { return new Vec3(this.entityData.get(WEB_END)); }

    public int getWebTarget() { return this.entityData.get(WEB_TARGET); }

    public ItemStack getWebItem() { return this.entityData.get(WEB_ITEM); }

    public int getWebShotTimer() { return this.entityData.get(WEB_SHOT_TIMER); }

    public int getWebCooldown() { return this.entityData.get(WEB_COOLDOWN); }

    public int getRefuseTimer() { return this.entityData.get(REFUSE_TIMER); }

    public boolean isWebLoaded() { return this.entityData.get(WEB_LOADED); }

    public int getEatTimer() { return this.entityData.get(EAT_TIMER); }

    public boolean isEating() { return getEatTimer() > 0; }

    public ItemStack getEatItem() { return this.entityData.get(EAT_ITEM); }

    public boolean isWebOut() { return getWebState() != WEB_IDLE; }

    public boolean isWebAttached() { return getWebState() == WEB_ATTACHED; }

    public int getWallFace() { return this.entityData.get(WALL_FACE); }

    public boolean isWallDescending() { return this.entityData.get(WALL_DESCENDING); }

    public boolean isWallClimbing() { return getWallFace() >= 0; }

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
        tag.putBoolean("webLoaded", this.isWebLoaded());

        Player carrier = getCarrier();
        if (carrier != null) {
            tag.putUUID("wristCarrier", carrier.getUUID());
            tag.putInt("wristSlot", carrySlot());
        } else if (pendingCarrier != null) {
            tag.putUUID("wristCarrier", pendingCarrier);
            tag.putInt("wristSlot", pendingCarrierSlot);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.entityData.set(WEB_LOADED, tag.getBoolean("webLoaded"));
        this.pendingCarrier = tag.hasUUID("wristCarrier") ? tag.getUUID("wristCarrier") : null;
        this.pendingCarrierSlot = tag.getInt("wristSlot") == SLOT_LEFT ? SLOT_LEFT : SLOT_RIGHT;
        this.pendingCarrierTicks = 0;
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(23, 0f, 0f, 0.7f)
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
