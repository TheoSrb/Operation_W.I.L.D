package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.MarkedMudBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.behavior.CrocodileBehaviorHandler;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileAttackGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileChargingMouthGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileGoToWaterWithFoodGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileNapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.TamingCrocodile;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class CrocodileEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity {

    public static final double TAMING_EXPERIENCE = 205.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GRABBING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_DEATH_ROLLING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DEATH_ROLLING_PROGRESS = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> SACRIFICES_UNITY = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> START_TAMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ENTITIES_KILLED_DURING_TAMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TAMING_TIMER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LUNGING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LUNGE_TARGET_ID = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RIDER_CONTROL_PITCH = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_MOUTH_SLAMMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_MOUTH = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CHARGING_MOUTH_TIMER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_PLAYER_MOUTH_CHARGING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);

    public CrocodileBehaviorHandler crocodileBehaviorHandler;
    public TamingCrocodile crocodileTaming;

    public CrocodileTailPart[] tailParts = new CrocodileTailPart[3];

    private static final float SEG_DIST = 1.25f;
    private static final float BODY_TAIL_OFFSET = 0.75f;

    public volatile float tail1AnimXRot = 0f, tail1AnimYRot = 0f, tail1AnimZRot = 0f;
    public volatile float tail2AnimXRot = 0f, tail2AnimYRot = 0f, tail2AnimZRot = 0f;
    public volatile float tail3AnimXRot = 0f, tail3AnimYRot = 0f, tail3AnimZRot = 0f;

    private final double[] seg1 = new double[3];
    private final double[] seg2 = new double[3];
    private final double[] seg3 = new double[3];
    private float seg1Yaw = 0f, seg2Yaw = 0f, seg3Yaw = 0f;
    private boolean tailPosInit = false;

    public final AnimationState idleWaterAnimationState = new AnimationState();
    public final AnimationState idleWaterMountedAnimState = new AnimationState();
    public final AnimationState grabHoldAnimState = new AnimationState();
    public final AnimationState grabThrashAnimState = new AnimationState();
    public final AnimationState growlsAnimationState = new AnimationState();
    public final AnimationState gruntAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState deathRollWindupAnimState = new AnimationState();
    public final AnimationState deathRollAnimationState = new AnimationState();
    public final AnimationState mouthSlamAnimState = new AnimationState();

    public int idleWaterAnimationTimeout = 0;
    private int growlsAnimationStartTime = 0;
    private int gruntAnimationStartTime = 0;
    private int napAnimationStartTime = 0;
    public int mouthSlamAnimTimer = 0;
    private int grabThrashAnimStartTime = 0;
    private int mouthSlamServerTimer = 0;
    private int mouthSlamHitTimer = -1;
    private float mouthSlamPendingDamage = 0f;
    private float mouthSlamPendingKnockback = 0f;
    private boolean mouthSlamPendingBleed = false;

    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimX = 0f;

    public volatile org.joml.Matrix4f boneMatrix = null;

    public volatile org.joml.Matrix4f mouthMatrix = null;

    public volatile float mouthXRotDeg = 0f, mouthYRotDeg = 0f, mouthZRotDeg = 0f;

    private static final float REST_X = 0.0617f, REST_Y = 12.5453f, REST_Z = 6.0f;

    private static final float MODEL_ORIGIN_Y = 1.501f;

    public boolean canGrabOnLand = false;

    private int primalDivePhase = 0;
    private int primalDiveTimer = 0;
    private int primalDiveLungeTimer = 0;

    private int grabCooldown = 0;

    private int grabHoldTimer = 0;

    private int deathRollCooldown = 0;

    private static final int MAX_GRAB_COOLDOWN = 600;

    public static final int GRAB_START_TIMEOUT = 300;

    public static final int DEATH_ROLL_WINDUP_TICKS = 3;
    public static final int DEATH_ROLL_SPIN_TICKS = 34;
    public static final int DEATH_ROLL_TOTAL_TICKS = DEATH_ROLL_WINDUP_TICKS + DEATH_ROLL_SPIN_TICKS;
    private static final int DEATH_ROLL_COOLDOWN_TICKS = 60;

    private static final int PASSIVE_GRAB_TICKS = 200;
    public static final int PRIMAL_DIVE_GRAB_TICKS = 200;

    private static final double MOUTH_HOLD_FORWARD = 1.62;
    private static final double MOUTH_HOLD_HEIGHT = 0.53;
    private static final float MOUTH_HOLD_LOCAL_Z = 0.12f;

    public static final int DEATH_ROLL_BITES = 5;
    public static final float DEATH_ROLL_BITE_RATIO = 0.22f;
    public static final float DEATH_ROLL_PLAYER_BITE_RATIO = 0.035f;
    public static final float DEATH_ROLL_WILD_MULTIPLIER = 0.65f;

    private static final double PRIMAL_DIVE_LUNGE_SPEED = 1.05;
    private static final double DEATH_ROLL_DRIFT = 0.06;
    private static final int DEATH_ROLL_DRY_GRACE = 12;
    private static final int DEATH_ROLL_INPUT_BUFFER_TICKS = 60;

    private int deathRollDryTicks = 0;
    private int deathRollQueued = 0;

    private boolean isPrimalDiveGrab = false;
    private Vec3 lastPitchCheckPos = null;

    private static final int GROWLS_DURATION = 75;
    private static final int GRUNT_DURATION = 55;

    private int growlsCooldown = (int) OWUtils.generateRandomInterval(400, 1200);
    private int gruntCooldown = (int) OWUtils.generateRandomInterval(300, 500);

    public CrocodileEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initCrocodileBehaviorAndTaming();
    }

    private void initCrocodileBehaviorAndTaming() {
        this.crocodileBehaviorHandler = new CrocodileBehaviorHandler(this);
        this.crocodileTaming = new TamingCrocodile(this, crocodileBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 37.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ARMOR, 0.2D);
    }

    @Override
    protected boolean allowsUnownedPiloting() {
        return !this.isTame();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initCrocodileBehaviorAndTaming();

        this.goalSelector.addGoal(0, new CrocodileGoToWaterWithFoodGoal(this));
        this.goalSelector.addGoal(0, new JumpOutOfTheWaterGoal(this));
        this.goalSelector.addGoal(0, new FollowBoatGoal(this));
        this.goalSelector.addGoal(1, new CrocodileMeleeAttackGoal());
        this.goalSelector.addGoal(2, new CrocodileChargingMouthGoal(this));
        this.goalSelector.addGoal(3, new CrocodileNapGoal(this, 1.25f, 500, true));
        this.goalSelector.addGoal(4, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new OWRandomLookAroundGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof CrocodileEntity crocodile && !crocodile.isSleeping() && !crocodile.isNapping()) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_GRABBING, false);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(IS_DEATH_ROLLING, false);
        builder.define(DEATH_ROLLING_PROGRESS, 0);
        builder.define(GRAB_TIMEOUT, 0);
        builder.define(SACRIFICES_UNITY, 0.0f);
        builder.define(START_TAMING, false);
        builder.define(ENTITIES_KILLED_DURING_TAMING, 0);
        builder.define(TAMING_TIMER, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(IS_LUNGING, false);
        builder.define(LUNGE_TARGET_ID, -1);
        builder.define(RIDER_CONTROL_PITCH, 0.0f);
        builder.define(IS_MOUTH_SLAMMING, false);
        builder.define(IS_CHARGING_MOUTH, false);
        builder.define(CHARGING_MOUTH_TIMER, 0.0f);
        builder.define(IS_PLAYER_MOUTH_CHARGING, false);
    }

    @Override
    public int getEntityColor() {
        return 0x727957;
    }

    @Override
    public float getTheoreticalScale() {
        return 9;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
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
        return net.tiew.operationWild.entity.config.OWTargetLists.CROCODILE;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.25f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.55f;
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
        return OWItems.CROCODILE_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.CROCODILE_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 290f;
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
    public boolean riderCameraFollowsBodyTilt() {
        return false;
    }

    @Override
    public org.joml.Matrix4f riderBoneMatrix() {
        return this.boneMatrix;
    }

    @Override
    public float getRotationSpeed() {
        return 0.1f;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getMaxAirSupply() {
        return 300 * 10;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir + 10;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 30 : 10;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
    }

    @Override
    protected double riddenBuoyancy() {
        return 0.0D;
    }

    @Override
    protected double riddenAscendSpeed() {
        return 0.10D;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.4F;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.CROCODILE.get().create(serverLevel);
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
        playStepSoundFromAnimation(0.85f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.05f);
    }

    @Override
    protected double getBaseRiderYOffset() {
        float height = this.getVariant() == CrocodileVariant.Cosmetics.VERMILION_GUARDIAN.variant ? 0.4f : 0.5f;
        return this.getBbHeight() * height * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public void aiStep() {
        if (this.entityData.get(IS_LUNGING)) tickPrimalDiveLunge();
        super.aiStep();
        if (this.isInWater() || this.onGround() && !this.isBaby()) {
            BlockPos currentPos = this.blockPosition();
            crocodileBehaviorHandler.trampleLilyPads(currentPos);
            crocodileBehaviorHandler.trampleLilyPads(currentPos.above());
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isChargingMouth()) {
            Vec3 movement = this.getDeltaMovement();
            float multiplier = this.isVehicle() ? 0.45f : 0.15f;
            this.setDeltaMovement(movement.x * multiplier, movement.y, movement.z * multiplier);
        }

        super.travel(vec3);

        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle())
            this.jumpFromGround();
    }

    public void tick() {
        super.tick();
        crocodileTaming.tick();

        if (!this.isChargingMouth()) {
            if (!this.isTame() && this.isVehicle()) {
                createComboSimple(32, 15, OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0, 2, 2.25, 0.15f);
            } else {
                createCombo(32, 15, OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0, 2, 2.25, false, 0.15f);
            }
        }

        if (!this.level().isClientSide() && isPlayerMouthCharging()) {
            if (this.getControllingPassenger() == null) {
                cancelMouthSlamCharge();
            } else {
                float current = getChargingMouthTimer();
                if (current < 60f) {
                    setChargingMouthTimer(Math.min(current + 1f, 60f));
                }
            }
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting() && !this.isBaby()) setMadByRider(this.isCombo());

        if (!this.level().isClientSide()) {
            if (this.mouthSlamServerTimer > 0) {
                this.mouthSlamServerTimer--;
                if (this.mouthSlamServerTimer == 0) setMouthSlamming(false);
            }

            if (this.mouthSlamHitTimer > 0) {
                this.mouthSlamHitTimer--;
                if (this.mouthSlamHitTimer == 0) {
                    mouthSlamHitTimer = -1;
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                this.getX(), this.getY() + 0.8, this.getZ(),
                                20, 2.0, 0.4, 2.0, 0.15);
                    }
                    this.crocodileBehaviorHandler.performMouthSlamAttack(
                            mouthSlamPendingDamage, mouthSlamPendingKnockback, mouthSlamPendingBleed);
                }
            }

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

        if (this.getTarget() != null && this.getTarget().hasEffect(OWEffects.FRACTURE.getDelegate())) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.25, 1.0, 1.25));
        }

        if (!this.level().isClientSide() && !this.isBaby()) {
            if (grabCooldown > 0) grabCooldown--;
            if (deathRollCooldown > 0) deathRollCooldown--;
        }

        if (this.isInWater() && !this.isBaby()) {
            if (!this.level().isClientSide() && isPlayerMouthCharging()) cancelMouthSlamCharge();
            this.setChargingMouth(false);
            this.setChargingMouthTimer(0);
        }

        if (!this.level().isClientSide()) tickDeathRoll();

        if (primalDivePhase == 1 && !this.level().isClientSide()) {
            if (primalDiveTimer > 0) primalDiveTimer--;
            else cancelPrimalDive();
        }

        if (primalDivePhase == 2 && !this.level().isClientSide()) {
            if (primalDiveLungeTimer > 0) primalDiveLungeTimer--;

            Entity lungeRaw = this.level().getEntity(this.entityData.get(LUNGE_TARGET_ID));
            if (!(lungeRaw instanceof LivingEntity lungeTarget) || !lungeTarget.isAlive()) {
                cancelPrimalDive();
            } else {
                double distSq = this.distanceToSqr(lungeTarget);

                if (primalDiveLungeTimer <= 0 && distSq > 9.0) {
                    cancelPrimalDive();
                } else if (distSq <= 6.0 || primalDiveLungeTimer <= 0) {
                    closePrimalDiveGrab(lungeTarget);
                }
            }
        }

        if (!this.level().isClientSide() && !this.isBaby()) tickGrab();

        markMudWithFootprints();
        handleGoldVariantEffects();

        if (!this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                if (tailParts[i] == null || tailParts[i].isRemoved()) {
                    spawnTailParts();
                    break;
                }
            }
            updateTailChain();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.isGrabbing()) releaseGrab();

        super.die(damageSource);

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    public void setTame(boolean tame, Player player) {
        if (tame && !this.level().isClientSide() && this.isGrabbing()) {
            releaseGrab();
            cancelPrimalDive();
            grabCooldown = 0;
        }
        super.setTame(tame, player);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            spawnTailParts();
        }
    }

    private void spawnTailParts() {
        for (int i = 0; i < 3; i++) {
            if (tailParts[i] != null && !tailParts[i].isRemoved()) {
                tailParts[i].discard();
            }
            CrocodileTailPart part = new CrocodileTailPart(
                    net.tiew.operationWild.entity.OWEntityRegistry.CROCODILE_TAIL_PART.get(),
                    this.level(), this, i);
            part.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(part);
            tailParts[i] = part;
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide() && this.isGrabbing()) releaseGrab(false);

        super.remove(reason);
        if (!this.level().isClientSide()) {
            for (CrocodileTailPart part : tailParts) {
                if (part != null && !part.isRemoved()) {
                    part.remove(reason);
                }
            }
        }
    }

    private void updateTailChain() {
        float scale = this.getScale();
        float segLen = SEG_DIST * scale;
        float bodyOff = BODY_TAIL_OFFSET * scale;

        float yawRad = (float) Math.toRadians(yBodyRot);
        double attachX = getX() + Math.sin(yawRad) * bodyOff;
        double attachY = getY();
        double attachZ = getZ() - Math.cos(yawRad) * bodyOff;

        if (!tailPosInit) {
            seg1Yaw = seg2Yaw = seg3Yaw = yBodyRot;
            if (tailParts[0] != null) tailParts[0].setPos(
                    attachX + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    attachZ - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            if (tailParts[1] != null && tailParts[0] != null) tailParts[1].setPos(
                    tailParts[0].getX() + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    tailParts[0].getZ() - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            if (tailParts[2] != null && tailParts[1] != null) tailParts[2].setPos(
                    tailParts[1].getX() + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    tailParts[1].getZ() - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            tailPosInit = true;
        }

        seg1Yaw += Mth.wrapDegrees(yBodyRot - seg1Yaw) * 0.85f;
        seg2Yaw += Mth.wrapDegrees(seg1Yaw - seg2Yaw) * 0.65f;
        seg3Yaw += Mth.wrapDegrees(seg2Yaw - seg3Yaw) * 0.50f;

        seg1[0] = attachX;
        seg1[1] = attachY;
        seg1[2] = attachZ;

        seg2[0] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getX() : seg1[0];
        seg2[1] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getY() : attachY;
        seg2[2] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getZ() : seg1[2];

        seg3[0] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getX() : seg2[0];
        seg3[1] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getY() : seg2[1];
        seg3[2] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getZ() : seg2[2];

        applyToTailPart(0, seg1, seg1Yaw);
        applyToTailPart(1, seg2, seg2Yaw);
        applyToTailPart(2, seg3, seg3Yaw);
    }

    private void applyToTailPart(int index, double[] pos, float yaw) {
        CrocodileTailPart part = tailParts[index];
        if (part == null || part.isRemoved()) {
            part = new CrocodileTailPart(
                    OWEntityRegistry.CROCODILE_TAIL_PART.get(),
                    this.level(), this, index);
            this.level().addFreshEntity(part);
            tailParts[index] = part;
        }
        part.setPos(pos[0], pos[1], pos[2]);
        part.refreshDimensions();
        part.yRotO = part.getYRot();
        part.setYRot(yaw);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) {
            return;
        }

        super.setTarget(target);

        if (!isTame()) {
            setMad(!isBaby() && this.getTarget() != null && getSleepBarPercent() < 75 && !this.isSitting());
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        this.crocodileTaming.hurtAfterCombo(entity, comboAttack);

        if (this.level().isClientSide() || entity == null) return;
        if (crocodileBehaviorHandler.isReadyForTaming()) return;
        if (!canStartGrab()) return;

        if (!this.isTame()) {
            if (canGrabOnLand) {
                this.grabEntity(entity);
                return;
            }
            if (crocodileBehaviorHandler.findNearestWaterSource(10) == null) return;

            if (this.isInWater() || comboAttack == 3) {
                this.grabEntity(entity);
            }
            return;
        }

        if (crocodileBehaviorHandler.findNearestWaterSource(10) != null
                && this.getRandom().nextInt(100) < 20) {
            grabEntityPassive(entity);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame()) {
            if (this.isSitting()) this.setSitting(false);

            if (isStartingTaming() && this.getEntitiesKilledDuringTaming() > 0) {
                this.setEntitiesKilledDuringTaming(this.getEntitiesKilledDuringTaming() - 1);
            }
        }

        if (!this.level().isClientSide() && this.isGrabbing() && this.getGrabTimeout() > 0) {
            Entity attacker = damageSource.getEntity();
            if (attacker != null && attacker != this.getGrabbedTarget()) {
                this.setGrabTimeout(Math.max(0, this.getGrabTimeout() - (int) Math.min(60f, v * 6f)));
            }
        }

        if (this.isInWater()) {
            Vec3 knockback = this.getDeltaMovement();
            boolean wasHurt = super.hurt(damageSource, v);

            if (wasHurt) {
                Vec3 newKnockback = this.getDeltaMovement();
                Vec3 appliedKnockback = newKnockback.subtract(knockback);

                this.setDeltaMovement(knockback.add(appliedKnockback.scale(0.05)));
            }

            return wasHurt;
        }

        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        this.crocodileTaming.onKilledEntity(entity);
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CrocodileEntity otherCrocodile) {
            if (otherCrocodile.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherCrocodile.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherCrocodile.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherCrocodile.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == this.getGrabbedTarget()) {
            final double s = this.getScale();
            final double hang = Math.min(0.9f, passenger.getBbHeight() * 0.5f) * s;
            final float localZ = -(MOUTH_HOLD_LOCAL_Z + passenger.getBbWidth() * 0.35f);

            double px, py, pz;
            float jawYaw = this.yBodyRot;
            org.joml.Matrix4f jaws = this.mouthMatrix;

            if (jaws != null) {
                org.joml.Vector3f hold = jaws.transformPosition(new org.joml.Vector3f(0f, 0f, localZ));

                double ax = -hold.x;
                double ay = MODEL_ORIGIN_Y - hold.y;
                double az = hold.z;

                double yawRad = Math.toRadians(this.yBodyRot);
                double sin = Math.sin(yawRad), cos = Math.cos(yawRad);

                px = this.getX() + (-ax * cos + az * sin) * s;
                pz = this.getZ() + (-ax * sin - az * cos) * s;
                py = this.getY() + ay * s - hang;
                jawYaw = this.yBodyRot + this.mouthYRotDeg;

            } else {
                double yawRad = Math.toRadians(this.yBodyRot);
                double forward = (MOUTH_HOLD_FORWARD - MOUTH_HOLD_LOCAL_Z - localZ) * s;
                px = this.getX() - Math.sin(yawRad) * forward;
                pz = this.getZ() + Math.cos(yawRad) * forward;
                py = this.getY() + MOUTH_HOLD_HEIGHT * s - hang;
            }

            passenger.fallDistance = 0f;
            if (passenger instanceof LivingEntity prey && !(passenger instanceof Player)) {
                prey.setYRot(jawYaw);
                prey.setYBodyRot(jawYaw);
                prey.yHeadRot = jawYaw;
                prey.setXRot(Mth.clamp(-this.mouthXRotDeg, -80f, 80f));
            }
            function.accept(passenger, px, py, pz);
            return;
        }
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        final float s = this.getScale();
        final double baseY = getBaseRiderYOffset();

        float mx = 0f;
        float my = (float) (MODEL_ORIGIN_Y - baseY / s);
        float mz = 0f;

        float lx = mx - REST_X / 16f;
        float ly = my - REST_Y / 16f;
        float lz = mz - REST_Z / 16f;

        double dx = 0, dy = 0, dz = 0;
        org.joml.Matrix4f bones = this.boneMatrix;
        if (bones != null) {
            org.joml.Vector3f now = bones.transformPosition(new org.joml.Vector3f(lx, ly, lz));
            dx = now.x - mx;
            dy = now.y - my;
            dz = now.z - mz;
        }

        double seatX = dx * s;
        double seatY = -dy * s;
        double seatZ = -dz * s;

        double yRad = Math.toRadians(-this.yBodyRot);
        double worldX = seatX * Math.cos(yRad) + seatZ * Math.sin(yRad);
        double worldZ = -seatX * Math.sin(yRad) + seatZ * Math.cos(yRad);

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + worldX, this.getY() + baseY + seatY, this.getZ() + worldZ);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        crocodileTaming.mobInteract(player, hand);
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseCrocodileVariant());
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

    public static boolean checkCrocodileSpawnRules(EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState blockBelow = level.getBlockState(pos.below());
        Block blockType = blockBelow.getBlock();

        boolean validBlock = blockType == Blocks.MUD || blockType == Blocks.GRASS_BLOCK || blockType == Blocks.WATER;

        if (!validBlock) {
            return false;
        }

        boolean waterNearby = false;
        int searchRadius = 16;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    if (state.getBlock() == Blocks.WATER) {
                        waterNearby = true;
                        break;
                    }
                }
                if (waterNearby) break;
            }
            if (waterNearby) break;
        }

        return waterNearby;
    }

    private void markMudWithFootprints() {
        if (this.getTarget() == null && this.tickCount % 40 == 0 && isMoving()) {
            BlockPos blockPos = this.blockPosition();
            BlockState blockState = this.level().getBlockState(blockPos);

            if (blockState.is(Blocks.MUD)) {
                Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(this.level().getRandom());
                BlockState mudState = OWBlocks.MARKED_MUD.get().defaultBlockState().setValue(MarkedMudBlock.FACING, facing);

                this.level().setBlockAndUpdate(blockPos, mudState);
            }
        }
    }

    @Override
    protected int getDefaultSkinIndex() {
        return 7;
    }

    public void releaseGrab() {
        releaseGrab(true);
    }

    public void releaseGrab(boolean notifyNeighbours) {
        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null) {
            grabbed.noPhysics = false;
            if (grabbed.getVehicle() == this) grabbed.stopRiding();
            if (isPrimalDiveGrab && grabbed instanceof Mob grabbedMob) {
                grabbedMob.setNoAi(false);
            }
        }
        isPrimalDiveGrab = false;
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        this.setDeathRolling(false);
        this.setDeathRollProgress(0);
        this.setTarget(null);
        grabHoldTimer = 0;
        deathRollQueued = 0;
        deathRollDryTicks = 0;
        if (notifyNeighbours) clearNearbyCrocodileTargets();
    }

    private void clearNearbyCrocodileTargets() {
        if (this.level().isClientSide()) return;
        this.level().getEntitiesOfClass(CrocodileEntity.class, this.getBoundingBox().inflate(30))
                .forEach(otherCroc -> {
                    if (otherCroc != this && otherCroc.getTarget() == this) otherCroc.setTarget(null);
                });
    }

    @Override
    public LivingEntity getControllingPassenger() {
        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null && this.getFirstPassenger() == grabbed) return null;
        return super.getControllingPassenger();
    }

    private void tickGrab() {
        if (!this.isGrabbing()) {
            if (grabHoldTimer != 0) grabHoldTimer = 0;
            return;
        }

        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed == null || !grabbed.isAlive() || grabbed.isRemoved()
                || grabbed.level() != this.level()
                || (grabbed instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            releaseGrab();
            return;
        }

        if (grabHoldTimer > 0) {
            grabHoldTimer--;
            if (grabHoldTimer <= 0) {
                playGrabReleaseFeedback();
                releaseGrab();
                return;
            }
        }

        grabbed.noPhysics = true;
        grabbed.fallDistance = 0f;

        if (!(grabbed instanceof Player) && grabbed instanceof Mob grabbedMob) {
            grabbedMob.setTarget(null);
        }

        if (this.isInWater()) {
            this.setLookAt(grabbed.getX(), grabbed.getY(), grabbed.getZ());
        }

        if (grabHoldTimer <= 0 && this.getGrabTimeout() <= 0) {
            playGrabReleaseFeedback();
            releaseGrab();
            return;
        }

        if (grabHoldTimer <= 0 && grabbed instanceof Player) {
            this.setGrabTimeout(this.getGrabTimeout() + 1);

            if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                this.setGrabTimeout(0);
                grabbed.invulnerableTime = 0;
                grabbed.hurt(this.damageSources().mobAttack(this), Float.MAX_VALUE);
                clearNearbyCrocodileTargets();
                return;
            }
        }

        if (!grabbed.isPassenger()) {
            grabbed.startRiding(this, true);
        }

        boolean canDrown = !grabbed.getType().is(net.minecraft.tags.EntityTypeTags.CAN_BREATHE_UNDER_WATER);

        if (this.isDeathRolling()) {
            if (canDrown && grabbed.getAirSupply() < 1) grabbed.setAirSupply(1);
        } else if (this.isInWater() && grabbed.isInWater() && canDrown) {
            grabbed.setAirSupply(Math.max(-19, grabbed.getAirSupply() - 4));
        }

        if (this.tickCount % 40 == 0) {
            this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_2.get(),
                    SoundSource.HOSTILE, 0.9f, (float) OWUtils.generateRandomInterval(0.7, 0.9));
        }
    }

    private void tickDeathRoll() {
        if (this.isBaby()) return;
        if (!this.isDeathRolling() && !this.isGrabbing()) return;

        LivingEntity grabbed = this.getGrabbedTarget();

        if (this.isDeathRolling()) {
            if (grabbed == null || !this.isGrabbing()) {
                stopDeathRoll();
                return;
            }
            if (this.isInWaterForDeathRoll()) {
                deathRollDryTicks = 0;
            } else if (++deathRollDryTicks > DEATH_ROLL_DRY_GRACE) {
                stopDeathRoll();
                return;
            }
        } else {
            deathRollDryTicks = 0;
        }

        if (deathRollQueued > 0 && !this.isDeathRolling()) {
            deathRollQueued--;
            if (deathRollCooldown <= 0) {
                deathRollQueued = 0;
                startDeathRoll();
                return;
            }
        }

        if (!this.isDeathRolling() && grabbed != null && !this.isTame()
                && this.isInWaterForDeathRoll() && deathRollCooldown <= 0) {
            startDeathRoll();
            return;
        }

        if (!this.isDeathRolling()) return;

        int progress = this.getDeathRollProgress() + 1;
        this.setDeathRollProgress(progress);

        Vec3 velocity = this.getDeltaMovement();
        double driftX = velocity.x * 0.9;
        double driftZ = velocity.z * 0.9;
        double horizontal = Math.sqrt(driftX * driftX + driftZ * driftZ);

        if (horizontal < DEATH_ROLL_DRIFT) {
            double yawRad = Math.toRadians(this.yBodyRot);
            driftX = -Math.sin(yawRad) * DEATH_ROLL_DRIFT;
            driftZ = Math.cos(yawRad) * DEATH_ROLL_DRIFT;
        }

        double rise = this.level().getSeaLevel() - this.getY() >= 2 ? 0.035 : velocity.y * 0.9;
        this.setDeltaMovement(driftX, rise, driftZ);

        int spinTick = progress - DEATH_ROLL_WINDUP_TICKS;

        if (spinTick == 7 || spinTick == 19 || spinTick == DEATH_ROLL_SPIN_TICKS - 4) {
            playBoneCrack();
        }

        if (spinTick > 0 && spinTick % 6 == 0 && spinTick <= 30) {
            grabbed.invulnerableTime = 0;
            float ratio = grabbed instanceof Player ? DEATH_ROLL_PLAYER_BITE_RATIO : DEATH_ROLL_BITE_RATIO;
            if (!this.isTame()) ratio *= DEATH_ROLL_WILD_MULTIPLIER;
            grabbed.hurt(this.damageSources().mobAttack(this), this.getDamage() * ratio);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        this.getX(), this.getY() + 0.5, this.getZ(), 24, 0.9, 0.4, 0.9, 0.12);
                serverLevel.sendParticles(ParticleTypes.BUBBLE,
                        this.getX(), this.getY() + 0.3, this.getZ(), 18, 0.8, 0.4, 0.8, 0.05);
            }
            this.level().playSound(null, getX(), getY(), getZ(),
                    RANDOM(2) ? OWSounds.CROCODILE_HIT_1.get() : OWSounds.CROCODILE_HIT_2.get(),
                    SoundSource.HOSTILE, 1.1f, (float) OWUtils.generateRandomInterval(0.85, 1.05));
        }

        if (progress >= DEATH_ROLL_TOTAL_TICKS) {
            stopDeathRoll();
        }
    }

    public boolean canPrimalDiveTarget(LivingEntity candidate) {
        if (candidate == null || candidate == this) return false;
        if (!candidate.isAlive() || candidate.isRemoved() || !candidate.isInWater()) return false;
        if (candidate == this.getControllingPassenger()) return false;
        if (candidate instanceof CrocodileEntity) return false;
        if (candidate instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return false;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (this.isAlliedTo(candidate) || this.isTameGrabAlly(candidate)) return false;
        if (!canBeGrabbedWhileMounted(candidate)) return false;
        return true;
    }

    public boolean isInWaterForDeathRoll() {
        if (this.isInWater()) return true;
        return this.level().getFluidState(this.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)
                || this.level().getFluidState(this.blockPosition().below()).is(net.minecraft.tags.FluidTags.WATER);
    }

    public boolean startDeathRoll() {
        if (this.level().isClientSide() || this.isBaby()) return false;
        if (this.isDeathRolling()) return false;
        if (!this.isGrabbing() || this.getGrabbedTarget() == null || !this.isInWaterForDeathRoll()) return false;

        if (deathRollCooldown > 0) {
            deathRollQueued = DEATH_ROLL_INPUT_BUFFER_TICKS;
            return false;
        }
        deathRollQueued = 0;

        this.setDeathRolling(true);
        this.setDeathRollProgress(0);
        deathRollCooldown = DEATH_ROLL_TOTAL_TICKS + DEATH_ROLL_COOLDOWN_TICKS;

        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(),
                SoundSource.HOSTILE, 1.6f, 0.65f);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                    this.getX(), this.getY() + 0.4, this.getZ(), 40, 1.0, 0.4, 1.0, 0.2);
        }
        return true;
    }

    private void stopDeathRoll() {
        this.setDeathRolling(false);
        this.setDeathRollProgress(0);
    }

    private void playBoneCrack() {
        this.level().playSound(null, getX(), getY(), getZ(),
                net.minecraft.sounds.SoundEvents.SKELETON_DEATH, SoundSource.HOSTILE,
                0.42f, (float) OWUtils.generateRandomInterval(0.72, 0.88));
    }

    private void playGrabReleaseFeedback() {
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_4.get(),
                SoundSource.HOSTILE, 1.2f, (float) OWUtils.generateRandomInterval(0.8, 1.0));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.7, 0.3, 0.7, 0.08);
        }
    }

    private LivingEntity resolveGrabTarget(LivingEntity entity) {
        if (entity == null || this.isBaby()) return null;

        if (entity instanceof BoaTailPart tailPart) {
            if (tailPart.getParent() instanceof BoaEntity boaHead) entity = boaHead;
            else return null;
        }

        if (entity == this) return null;
        if (entity instanceof CrocodileEntity || entity instanceof BoaEntity || entity instanceof BoaTailPart) return null;
        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return null;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return null;
        if (!entity.isAlive() || entity.isRemoved()) return null;
        if (this.isTameGrabAlly(entity)) return null;
        if (!canBeGrabbedWhileMounted(entity)) return null;

        return entity;
    }

    private boolean canBeGrabbedWhileMounted(LivingEntity entity) {
        if (entity.isPassenger()) return false;
        return entity.getPassengers().isEmpty();
    }

    private boolean canStartGrab() {
        return !this.level().isClientSide()
                && !this.isBaby()
                && !this.isGrabbing()
                && !this.isSleeping()
                && !this.isNapping()
                && !this.isSitting()
                && !this.isChargingMouth()
                && grabCooldown <= 0
                && this.getHealth() >= 10;
    }

    private void grabEntity(LivingEntity rawTarget) {
        LivingEntity entity = resolveGrabTarget(rawTarget);
        if (entity == null || !canStartGrab()) return;

        int[] slidingLevels = getSlidingLevels(entity);
        float[] slidingMultiplier = OWEnchantments.SLIDING_ARMOR_MULTIPLIERS;
        int chance = this.getRandom().nextInt(100);

        float chancesToAvoidingGrab = calculateChanceToAvoidingGrab(slidingLevels, slidingMultiplier);
        if (chance < chancesToAvoidingGrab) return;

        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() > 20) return;

        this.setGrabbing(true, entity);
        if (!this.isGrabbing()) return;

        this.setGrabTimeout(GRAB_START_TIMEOUT);
        grabHoldTimer = 0;
        grabCooldown = MAX_GRAB_COOLDOWN;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        playGrabFeedback();
    }

    private void grabEntityPassive(LivingEntity rawTarget) {
        LivingEntity entity = resolveGrabTarget(rawTarget);
        if (entity == null || !canStartGrab()) return;

        this.setGrabbing(true, entity);
        if (!this.isGrabbing()) return;

        this.setGrabTimeout(GRAB_START_TIMEOUT);
        grabHoldTimer = PASSIVE_GRAB_TICKS;
        grabCooldown = MAX_GRAB_COOLDOWN;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        playGrabFeedback();
    }

    private void playGrabFeedback() {
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(),
                SoundSource.HOSTILE, 1.8f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    this.getX(), this.getY() + 0.7, this.getZ(), 8, 0.6, 0.3, 0.6, 0.05);
        }
    }

    private float calculateChanceToAvoidingGrab(int[] slidingLevels, float[] slidingMultiplier) {
        return (slidingLevels[0] * slidingMultiplier[0]) + (slidingLevels[1] * slidingMultiplier[1]) + (slidingLevels[2] * slidingMultiplier[2]) + (slidingLevels[3] * slidingMultiplier[3]);
    }

    private int[] getSlidingLevels(LivingEntity entity) {
        int[] slidingLevels = new int[4];
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (int i = 0; i < slots.length; i++) {
            ItemStack armor = entity.getItemBySlot(slots[i]);
            if (!armor.isEmpty()) {
                slidingLevels[i] = armor.getEnchantmentLevel(this.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(OWEnchantments.SLIDING));
            }
        }

        return slidingLevels;
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == CrocodileVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private CrocodileVariant chooseCrocodileVariant() {
        CrocodileVariant variant;
        if (chance >= 66) variant = CrocodileVariant.GREEN;
        else if (chance >= 33) variant = CrocodileVariant.DARK;
        else variant = CrocodileVariant.DEFAULT;
        return variant;
    }

    protected void handleMiscIdleAnimations() {
        if (this.growlsAnimationState.isStarted()
                && this.tickCount - growlsAnimationStartTime > GROWLS_DURATION) {
            this.growlsAnimationState.stop();
        }
        if (this.gruntAnimationState.isStarted()
                && this.tickCount - gruntAnimationStartTime > GRUNT_DURATION) {
            this.gruntAnimationState.stop();
        }

        if (!this.level().isClientSide()) return;

        boolean canPlay = crocodileBehaviorHandler.canPlayIdleAnimation()
                && !crocodileBehaviorHandler.isAnyIdleAnimationPlaying();

        if (growlsCooldown > 0) {
            growlsCooldown--;
        } else {
            if (canPlay) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        OWSounds.CROCODILE_IDLE_3.get(), this.getSoundSource(),
                        1.0F, isBaby() ? 2.0F : 1.0F, false);
                if (crocodileBehaviorHandler.canGrowl()) {
                    this.growlsAnimationState.start(this.tickCount);
                    growlsAnimationStartTime = this.tickCount;
                }
            }
            growlsCooldown = (int) OWUtils.generateRandomInterval(400, 1200);
        }

        if (gruntCooldown > 0) {
            gruntCooldown--;
        } else {
            if (canPlay && !this.growlsAnimationState.isStarted()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        OWSounds.CROCODILE_IDLE_1.get(), this.getSoundSource(),
                        1.0F, isBaby() ? 2.0F : 1.0F, false);
                if (crocodileBehaviorHandler.canGrunt()) {
                    this.gruntAnimationState.start(this.tickCount);
                    gruntAnimationStartTime = this.tickCount;
                }
            }
            gruntCooldown = (int) OWUtils.generateRandomInterval(300, 500);
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(96, true);

        handleMiscIdleAnimations();

        if (this.isInWater()) {
            if (this.idleWaterAnimationTimeout <= 0) {
                this.idleWaterAnimationTimeout = 57;
                this.idleWaterAnimationState.start(this.tickCount);
            } else --this.idleWaterAnimationTimeout;
        }

        if (!this.isInWater()) {
            this.idleWaterAnimationTimeout = 0;
            this.idleWaterAnimationState.stop();
        }

        if (this.isNapping()) {
            if (this.napAnimationStartTime <= 0) {
                this.napAnimationStartTime = 200;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationStartTime;
        }

        if (!this.isNapping()) {
            this.napAnimationStartTime = 0;
            this.napAnimationState.stop();
        }

        setupGrabAnimationStates();

        if (this.isMouthSlamming()) {
            if (this.mouthSlamAnimTimer <= 0) {
                this.mouthSlamAnimTimer = Integer.MAX_VALUE;
                this.mouthSlamAnimState.start(this.tickCount);
            } else --this.mouthSlamAnimTimer;
        } else {
            this.mouthSlamAnimTimer = 0;
            this.mouthSlamAnimState.stop();
        }

        setupComboAnimations();
    }

    private void setupGrabAnimationStates() {
        boolean rolling = this.isDeathRolling();
        int progress = this.getDeathRollProgress();
        boolean winding = rolling && progress < DEATH_ROLL_WINDUP_TICKS;

        if (winding) {
            this.deathRollWindupAnimState.startIfStopped(this.tickCount);
            this.deathRollAnimationState.stop();
        } else if (rolling) {
            this.deathRollWindupAnimState.stop();
            this.deathRollAnimationState.startIfStopped(this.tickCount);
        } else {
            this.deathRollWindupAnimState.stop();
            this.deathRollAnimationState.stop();
        }

        if (hasGrabSomething() && !rolling) {
            this.grabHoldAnimState.startIfStopped(this.tickCount);
        } else {
            this.grabHoldAnimState.stop();
        }

        if (hasGrabSomething() && !rolling) {
            if (this.grabThrashAnimStartTime <= 0) {
                this.grabThrashAnimStartTime = this.tickCount;
                this.grabThrashAnimState.start(this.tickCount);
            } else if (this.tickCount - this.grabThrashAnimStartTime > 60) {
                this.grabThrashAnimStartTime = this.tickCount;
                this.grabThrashAnimState.start(this.tickCount);
            }
        } else {
            this.grabThrashAnimStartTime = 0;
            this.grabThrashAnimState.stop();
        }
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, 37);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, 37);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 37);
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1:
                attack1ComboTimer = timer;
                break;
            case 2:
                attack2ComboTimer = timer;
                break;
            case 3:
                attack3ComboTimer = timer;
                break;
        }
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof CrocodileEntity croc) {
            croc.setVariant(CrocodileVariant.byId(variant));
            croc.setInitialVariant(CrocodileVariant.byId(variant));
        }
    }

    public CrocodileVariant getVariant() {
        return CrocodileVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(CrocodileVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(CrocodileVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(CrocodileVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(CrocodileVariant.Cosmetics.VERMILION_GUARDIAN.variant);
            case 3 -> this.setSkin(CrocodileVariant.Cosmetics.TOY.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public CrocodileVariant getInitialVariant() {
        return CrocodileVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(CrocodileVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setPlayerMouthCharging(boolean value) { this.entityData.set(IS_PLAYER_MOUTH_CHARGING, value); }
    public boolean isPlayerMouthCharging() { return this.entityData.get(IS_PLAYER_MOUTH_CHARGING); }

    public void setChargingMouth(boolean isChargingMouth) {
        this.entityData.set(IS_CHARGING_MOUTH, isChargingMouth);
    }

    public boolean isChargingMouth() {
        return this.entityData.get(IS_CHARGING_MOUTH);
    }

    public void setMouthSlamming(boolean value) {
        this.entityData.set(IS_MOUTH_SLAMMING, value);
    }

    public boolean isMouthSlamming() {
        return this.entityData.get(IS_MOUTH_SLAMMING);
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public void setMadByRider(boolean isMad) {
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() {
        return this.entityData.get(IS_MAD);
    }

    public void setDeathRollProgress(int getDeathRollProgress) {
        this.entityData.set(DEATH_ROLLING_PROGRESS, getDeathRollProgress);
    }

    public int getDeathRollProgress() {
        return Math.min(this.entityData.get(DEATH_ROLLING_PROGRESS), DEATH_ROLL_TOTAL_TICKS);
    }

    public void setStartingTaming(boolean isStartingTaming) {
        this.entityData.set(START_TAMING, isStartingTaming);
    }

    public boolean isStartingTaming() {
        return this.entityData.get(START_TAMING);
    }

    public void setGrabTimeout(int getGrabMaxTimeout) {
        this.entityData.set(GRAB_TIMEOUT, getGrabMaxTimeout);
    }

    public int getGrabTimeout() {
        return this.entityData.get(GRAB_TIMEOUT);
    }

    public void setGrabbing(boolean isGrabbing, LivingEntity entity) {
        if (isGrabbing && this.isTameGrabAlly(entity)) return;
        this.entityData.set(IS_GRABBING, isGrabbing);
        this.setGrabbedTarget(entity);
    }

    public boolean isGrabbing() {
        return this.entityData.get(IS_GRABBING);
    }

    public float getRiderControlPitch() {
        return this.entityData.get(RIDER_CONTROL_PITCH);
    }

    public void setEntitiesKilledDuringTaming(int getEntitiesKilledDuringTaming) {
        this.entityData.set(ENTITIES_KILLED_DURING_TAMING, getEntitiesKilledDuringTaming);
    }

    public int getEntitiesKilledDuringTaming() {
        return this.entityData.get(ENTITIES_KILLED_DURING_TAMING);
    }

    public void setTamingTime(int getTamingTime) {
        this.entityData.set(TAMING_TIMER, getTamingTime);
    }

    public int getTamingTime() {
        return this.entityData.get(TAMING_TIMER);
    }

    public void setSacrificesUnity(float sacrificesUnity) {
        this.entityData.set(SACRIFICES_UNITY, sacrificesUnity);
    }

    public float getSacrificesUnity() {
        return this.entityData.get(SACRIFICES_UNITY);
    }

    public void setDeathRolling(boolean isDeathRolling) {
        this.entityData.set(IS_DEATH_ROLLING, isDeathRolling);
    }

    public boolean isDeathRolling() {
        return this.entityData.get(IS_DEATH_ROLLING);
    }

    public boolean hasGrabSomething() {
        return this.isGrabbing() && this.getGrabbedTarget() != null;
    }

    public LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    public void setGrabbedTarget(LivingEntity target) {
        this.entityData.set(GRABBED_TARGET_ID, target == null ? -1 : target.getId());
    }

    public void setChargingMouthTimer(float chargingMouthTimer) {
        this.entityData.set(CHARGING_MOUTH_TIMER, chargingMouthTimer);
    }

    public float getChargingMouthTimer() {
        return this.entityData.get(CHARGING_MOUTH_TIMER);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

        tag.putInt("getEntitiesKilledDuringTaming", this.getEntitiesKilledDuringTaming());
        tag.putInt("getTamingTime", this.getTamingTime());
        tag.putBoolean("isStartingTaming", this.isStartingTaming());
        tag.putFloat("getSacrificesUnity", this.getSacrificesUnity());

        tag.putInt("ultimateKillCount", getUltimateKillCount());
        tag.putInt("grabCooldown", this.grabCooldown);
        tag.putInt("deathRollCooldown", this.deathRollCooldown);
        crocodileTaming.addAdditionalSaveData(tag);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");

        this.entityData.set(ENTITIES_KILLED_DURING_TAMING, tag.getInt("getEntitiesKilledDuringTaming"));
        this.entityData.set(TAMING_TIMER, tag.getInt("getTamingTime"));
        this.entityData.set(START_TAMING, tag.getBoolean("isStartingTaming"));
        this.entityData.set(SACRIFICES_UNITY, tag.getFloat("getSacrificesUnity"));

        this.entityData.set(ULTIMATE_KILL_COUNT, tag.getInt("ultimateKillCount"));
        this.grabCooldown = tag.getInt("grabCooldown");
        this.deathRollCooldown = tag.getInt("deathRollCooldown");

        this.entityData.set(IS_GRABBING, false);
        this.entityData.set(GRABBED_TARGET_ID, -1);
        this.entityData.set(IS_DEATH_ROLLING, false);
        this.entityData.set(DEATH_ROLLING_PROGRESS, 0);
        this.grabHoldTimer = 0;
        this.isPrimalDiveGrab = false;

        crocodileTaming.readAdditionalSaveData(tag);
        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }

    @Override
    public int getGrabMaxTimeout() {
        return 600;
    }

    public void startMouthSlamCharge() {
        if (this.isInWater() || this.isGrabbing()) return;
        setPlayerMouthCharging(true);
        setChargingMouthTimer(0);
    }

    public void cancelMouthSlamCharge() {
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
    }

    public void activatePrimalDive() {
        if (getUltimateKillCount() < OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED) return;
        if (!this.isInWater()) return;
        if (this.isGrabbing() || primalDivePhase != 0) return;
        float cost = OWAttacksConstants.Crocodile.PRIMAL_DIVE_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        this.isChargingAttack = true;
        primalDivePhase = 1;
        primalDiveTimer = 200;

        OWUtils.spawnServerParticles(this, ParticleTypes.BUBBLE, 1.0, 0.5, 1.0, 30, 0.3);
        this.level().playSound(null, getX(), getY(), getZ(),
                OWSounds.CROCODILE_IDLE_1.get(), SoundSource.AMBIENT, 1.5f,
                (float) OWUtils.generateRandomInterval(0.7, 0.9));
    }

    public void executePrimalDive(int targetEntityId) {
        if (primalDivePhase != 1) return;
        Entity raw = this.level().getEntity(targetEntityId);
        if (!(raw instanceof LivingEntity target) || !canPrimalDiveTarget(target)
                || this.distanceToSqr(target) > 32.0 * 32.0) {
            cancelPrimalDive();
            return;
        }

        this.entityData.set(LUNGE_TARGET_ID, targetEntityId);
        this.entityData.set(IS_LUNGING, true);
        primalDiveLungeTimer = 60;
        primalDivePhase = 2;
        primalDiveTimer = 0;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY() + 0.5, getZ(), 60, 1.2, 0.4, 1.2, 0.4);
            sl.sendParticles(ParticleTypes.UNDERWATER, getX(), getY() + 0.5, getZ(), 40, 1.0, 0.5, 1.0, 0.05);
        }
        this.level().playSound(null, getX(), getY(), getZ(),
                OWSounds.CROCODILE_IDLE_1.get(), SoundSource.AMBIENT, 2.0f,
                (float) OWUtils.generateRandomInterval(0.7, 0.9));
    }

    private void tickPrimalDiveLunge() {
        Entity raw = this.level().getEntity(this.entityData.get(LUNGE_TARGET_ID));
        if (raw == null) return;

        Vec3 aim = raw.getBoundingBox().getCenter().subtract(this.getEyePosition());
        double distance = aim.length();
        if (distance < 1.0E-4) return;

        Vec3 direction = aim.scale(1.0 / distance);

        double brake = Mth.clamp(distance / 5.0, 0.30, 1.0);
        Vec3 desired = direction.scale(PRIMAL_DIVE_LUNGE_SPEED * brake);

        Vec3 current = this.getDeltaMovement();
        this.setDeltaMovement(current.add(desired.subtract(current).scale(0.30)));

        float targetYaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0 / Math.PI)) - 90.0f;
        float smoothedYaw = this.getYRot() + Mth.wrapDegrees(targetYaw - this.getYRot()) * 0.30f;
        this.setYRot(smoothedYaw);
        this.yBodyRot = smoothedYaw;
        this.yHeadRot = smoothedYaw;

        if (!this.level().isClientSide()) {
            float targetPitch = (float) (-Math.toDegrees(Math.asin(Mth.clamp(direction.y, -1.0, 1.0))));
            this.setTargetPitch(Mth.lerp(0.25f, this.getTargetPitch(), Mth.clamp(targetPitch, -45f, 45f)));
        }
    }

    private void closePrimalDiveGrab(LivingEntity target) {
        setGrabbing(true, target);

        if (!this.isGrabbing() || this.getGrabbedTarget() != target) {
            cancelPrimalDive();
            return;
        }

        isPrimalDiveGrab = true;
        if (target instanceof Mob grabbedMob) grabbedMob.setNoAi(true);
        setGrabTimeout(GRAB_START_TIMEOUT);
        grabCooldown = MAX_GRAB_COOLDOWN;
        grabHoldTimer = PRIMAL_DIVE_GRAB_TICKS;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        setUltimateKillCount(0);
        primalDivePhase = 0;
        primalDiveLungeTimer = 0;
        this.entityData.set(IS_LUNGING, false);
        this.entityData.set(LUNGE_TARGET_ID, -1);
        this.isChargingAttack = false;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY() + 0.5, getZ(), 70, 1.2, 0.4, 1.2, 0.25);
            sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 50, 0.8, 0.3, 0.8, 0.15);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 0.5, getZ(), 20, 0.9, 0.3, 0.9, 0.1);
            sl.sendParticles(ParticleTypes.UNDERWATER, getX(), getY() + 0.5, getZ(), 40, 1.0, 0.5, 1.0, 0.05);
        }
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.AMBIENT, 2.5f, 0.60f);
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_HURT.get(), SoundSource.AMBIENT, 2.0f, 0.70f);
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_4.get(), SoundSource.AMBIENT, 1.5f, 0.75f);
    }

    public void cancelPrimalDive() {
        primalDivePhase = 0;
        primalDiveTimer = 0;
        primalDiveLungeTimer = 0;
        this.entityData.set(IS_LUNGING, false);
        this.entityData.set(LUNGE_TARGET_ID, -1);
        this.isChargingAttack = false;
    }

    public boolean isInPrimalDivePhase() {
        return primalDivePhase == 1;
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.entityData.get(IS_LUNGING);
    }

    @Override
    public void tickRidden(Player player, Vec3 vec3) {
        if (this.isLeapingVehicle()) {
            player.resetFallDistance();
            return;
        }
        super.tickRidden(player, vec3);
    }

    @Override
    public boolean isPlayerControlledDeathRoll() {
        return this.isTame() && this.isGrabbing() && this.getGrabbedTarget() != null;
    }

    public boolean isDeathRollOnCooldown() {
        return deathRollCooldown > 0;
    }

    public int getUltimateKillCount() {
        return this.entityData.get(ULTIMATE_KILL_COUNT);
    }

    public void setUltimateKillCount(int count) {
        this.entityData.set(ULTIMATE_KILL_COUNT, count);
    }

    public void performMouthSlam(float factor) {
        boolean wasCharging = isPlayerMouthCharging();
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
        if (!wasCharging || this.isInWater() || this.isGrabbing()) return;
        float energyRequired = OWAttacksConstants.Crocodile.MOUTH_SLAM_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - energyRequired) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + energyRequired);

        float baseDamage = Mth.lerp(factor, 5.0f, this.getDamage());
        mouthSlamPendingDamage = factor >= 1.0f ? baseDamage * 2f : baseDamage;
        mouthSlamPendingKnockback = 1.5f + factor * 2.0f;
        mouthSlamPendingBleed = factor >= 1.0f;
        mouthSlamHitTimer = 8;

        setMouthSlamming(true);
        this.mouthSlamServerTimer = 50;
    }

    public void performWildMouthSlam(float chargeRatio) {
        mouthSlamPendingDamage = this.getDamage() * (0.5f + chargeRatio * 0.5f);
        mouthSlamPendingKnockback = 2.0f + chargeRatio * 1.5f;
        mouthSlamPendingBleed = chargeRatio >= 1.0f;
        mouthSlamHitTimer = 8;

        setMouthSlamming(true);
        this.mouthSlamServerTimer = 50;
    }

    public static class CrocodileNearestAttackableTargetGoal extends NearestAttackableTargetGoal {

        private final CrocodileEntity crocodile;
        private final float attacksMultiplier;

        public CrocodileNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee, float attacksMultiplier) {
            this(mob, targetType, mustSee, attacksMultiplier, null);
        }

        public CrocodileNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee, float attacksMultiplier, java.util.function.Predicate<LivingEntity> selector) {
            super(mob, targetType, 10, mustSee, false, selector);
            this.crocodile = (CrocodileEntity) mob;
            this.attacksMultiplier = attacksMultiplier;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }

            if (this.target == null || this.crocodile.isNapping()) return false;

            if (this.crocodile.distanceTo(this.target) <= 6) {
                return true;
            }

            if (this.crocodile.level().isDay() && !this.target.isInWater()) {
                return this.crocodile.getRandom().nextInt((int) (50 / this.attacksMultiplier)) == 0;
            }

            return true;
        }
    }

    class CrocodileMeleeAttackGoal extends MeleeAttackGoal {

        public CrocodileMeleeAttackGoal() {
            super(CrocodileEntity.this, 3.0, true);
        }

        private boolean isCrocodileBlocked() {
            return CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()
                    || CrocodileEntity.this.isBaby()
                    || CrocodileEntity.this.hasGrabSomething()
                    || CrocodileEntity.this.isChargingAttack;
        }

        @Override
        public boolean canContinueToUse() {
            if (isCrocodileBlocked() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            if (isCrocodileBlocked() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return;
            super.start();
            CrocodileEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            CrocodileEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            if (CrocodileEntity.this.isChargingMouth() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return false;
            double reach = 4;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;
            if (CrocodileEntity.this.isChargingMouth() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return;

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