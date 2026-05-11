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
    private static final EntityDataAccessor<Boolean> IS_WILD_STALKING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
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
    public final AnimationState idleDeathRollAnimState = new AnimationState();
    public final AnimationState growlsAnimationState = new AnimationState();
    public final AnimationState gruntAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState deathRollAnimationState = new AnimationState();
    public final AnimationState wildStalkAnimState = new AnimationState();
    public final AnimationState mouthSlamAnimState = new AnimationState();

    public int idleWaterAnimationTimeout = 0;
    private int growlsAnimationStartTime = 0;
    private int gruntAnimationStartTime = 0;
    private int napAnimationStartTime = 0;
    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int deathRollAnimationTimeout = 0;
    public int wildStalkAnimTimer = 0;
    public int mouthSlamAnimTimer = 0;
    private int mouthSlamServerTimer = 0;
    private int mouthSlamHitTimer = -1;
    private float mouthSlamPendingDamage = 0f;
    private float mouthSlamPendingKnockback = 0f;
    private boolean mouthSlamPendingBleed = false;

    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimX = 0f;

    private int attackingGrabTimer = 0;
    public int attackingGrabCooldown = 0;
    private int grabUnderwaterCooldown = 0;
    private int wildMouthSlamCooldown = 0;

    public boolean canGrabOnLand = false;

    private int primalDivePhase = 0;
    private int primalDiveTimer = 0;
    private int primalDiveLungeTimer = 0;

    private static final int MAX_GRAB_COOLDOWN = 600;
    private static long lastGrabTime = 0;

    private int passiveGrabTimer = 0;
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
    protected void registerGoals() {
        super.registerGoals();
        initCrocodileBehaviorAndTaming(); // Create the AI before the goals, otherwise, null error

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
                if (this.mob instanceof CrocodileEntity crocodile && !crocodile.isSleeping() && !crocodile.isNapping() && crocodile.getGrabbedTarget() == null) {
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
        builder.define(IS_WILD_STALKING, false);
        builder.define(IS_MOUTH_SLAMMING, false);
        builder.define(IS_CHARGING_MOUTH, false);
        builder.define(CHARGING_MOUTH_TIMER, 0.0f);
        builder.define(IS_PLAYER_MOUTH_CHARGING, false);
    }

    // Entity Methods
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
        return List.of(Boat.class, Player.class, Animal.class, Monster.class);
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
        return 290 * (1 + ((float) this.getLevel() / 50));
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
        return this.isTame() ? 30 : 5;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
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
        if (this.entityData.get(IS_LUNGING)) {
            int tid = this.entityData.get(LUNGE_TARGET_ID);
            Entity lungeRaw = this.level().getEntity(tid);
            if (lungeRaw != null) {
                Vec3 dir = lungeRaw.position().subtract(this.position()).normalize();
                this.setDeltaMovement(dir.x * 0.9, this.getDeltaMovement().y, dir.z * 0.9);
            }
        }
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
            float current = getChargingMouthTimer();
            if (current < 60f) {
                setChargingMouthTimer(Math.min(current + 1f, 60f));
            }
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting() && !this.isBaby()) setMad(this.isCombo());

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

        if (attackingGrabCooldown > 0 && !this.isBaby()) {
            attackingGrabCooldown--;
        }

        if (this.isInWater() && !this.isBaby()) {
            this.setChargingMouth(false);
            this.setChargingMouthTimer(0);

            if (this.getTarget() != null && !this.isTame()) {
                if (grabUnderwaterCooldown > 0) {
                    grabUnderwaterCooldown--;
                }
            }

            if (this.getGrabbedTarget() != null && !this.isTame()) {
                if (this.tickCount % 70 == 0) {
                    this.setDeathRolling(true);
                    this.setDeathRollProgress(0);
                }
            }
        } else {
            grabUnderwaterCooldown = 0;
        }

        if (this.isDeathRolling() && !this.isBaby()) {
            this.setDeathRollProgress(this.getDeathRollProgress() + 1);
            if (this.isInWater()) {
                if (this.level().getSeaLevel() - this.getY() >= 2) {
                    this.setDeltaMovement(0, 0.02, 0);
                }
            }

            try {
                this.getGrabbedTarget().invulnerableTime = 0;

                if (this.getDeathRollProgress() % 5 == 0) {
                    float damage = this.getGrabbedTarget() instanceof Player ? 0.5f : 1.5f;
                    this.getGrabbedTarget().hurt(this.damageSource, damage);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SPLASH,
                                this.getX(), this.getY() + 0.5, this.getZ(),
                                20,
                                0.5, 0.4, 0.5,
                                0.1);
                    }
                }

            } catch (NullPointerException e) {
            }

            if (this.getDeathRollProgress() >= 40) {
                this.setDeathRollProgress(0);
                this.setDeathRolling(false);
            }
        }

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

                if (distSq <= 6.0 || primalDiveLungeTimer <= 0) {
                    setGrabbing(true, lungeTarget);
                    isPrimalDiveGrab = true;
                    if (lungeTarget instanceof Mob grabbedMob) grabbedMob.setNoAi(true);
                    setGrabTimeout(300);
                    lastGrabTime = this.level().getGameTime();
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
            }
        }

        if (hasGrabSomething() && !this.isBaby()) {
            LivingEntity grabbed = this.getGrabbedTarget();
            boolean passiveReleased = false;

            // Passif apprivoisé : libération automatique après 10 s (200 ticks), joueur ou entité
            if (!this.level().isClientSide() && this.isTame() && passiveGrabTimer > 0) {
                passiveGrabTimer--;
                if (passiveGrabTimer <= 0) {
                    releaseGrab();
                    this.level().getEntitiesOfClass(CrocodileEntity.class, this.getBoundingBox().inflate(30))
                            .forEach(otherCroc -> {
                                if (otherCroc != this && otherCroc.getTarget() == this) otherCroc.setTarget(null);
                            });
                    passiveReleased = true;
                }
            }

            if (!passiveReleased) {
                if (grabbed instanceof Player) {
                    this.setGrabTimeout(this.getGrabTimeout() + 1);
                }

                try {
                    this.getGrabbedTarget().noPhysics = true;

                    if (!this.level().isClientSide() && !(grabbed instanceof Player)
                            && grabbed instanceof net.minecraft.world.entity.Mob grabbedMob) {
                        grabbedMob.setTarget(null);
                    }

                    if (this.isInWater()) {
                        this.setLookAt(this.getGrabbedTarget().getX(), this.getGrabbedTarget().getY(), this.getGrabbedTarget().getZ());
                    }

                    if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                        this.setGrabTimeout(0);
                        this.getGrabbedTarget().kill();

                        if (!this.level().isClientSide()) {
                            this.level().getEntitiesOfClass(CrocodileEntity.class, this.getBoundingBox().inflate(30))
                                    .forEach(otherCroc -> {
                                        if (otherCroc != this && otherCroc.getTarget() == this) {
                                            otherCroc.setTarget(null);
                                        }
                                    });
                        }
                    }

                } catch (NullPointerException e) {
                }

                if (!this.getGrabbedTarget().isAlive() || (this.getGrabbedTarget() instanceof Player player && player.isCreative())) {
                    releaseGrab();

                    if (!this.level().isClientSide()) {
                        this.level().getEntitiesOfClass(CrocodileEntity.class, this.getBoundingBox().inflate(30))
                                .forEach(otherCroc -> {
                                    if (otherCroc != this && otherCroc.getTarget() == this) {
                                        otherCroc.setTarget(null);
                                    }
                                });
                    }
                } else {
                    if (!grabbed.isPassenger()) {
                        grabbed.startRiding(this, true);
                    }
                }
            }
        }

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
            // Initialise les segments à la bonne position de départ
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

        // Ancre seg0 = point d'attache du body (Y réel du body)
        seg1[0] = attachX;
        seg1[1] = attachY;
        seg1[2] = attachZ;

        // Ancre seg1 = position réelle de seg0 après physique
        seg2[0] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getX() : seg1[0];
        seg2[1] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getY() : attachY;
        seg2[2] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getZ() : seg1[2];

        // Ancre seg2 = position réelle de seg1 après physique
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
        part.refreshDimensions(); // Met à jour la bbox à la nouvelle position
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
        boolean targetIsNearOfWater = crocodileBehaviorHandler.findNearestWaterSource(10) != null;
        boolean isAlreadyGrabbed = entity.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getOwner() != entity;
        boolean canGrab = targetIsNearOfWater && !this.level().isClientSide() &&
                !this.isTame() && !this.isSleeping() && !this.isNapping() && !this.isChargingMouth() && !isAlreadyGrabbed && this.getHealth() >= 10 && !(entity instanceof CrocodileEntity);

        this.crocodileTaming.hurtAfterCombo(entity, comboAttack);

        if (crocodileBehaviorHandler.isReadyForTaming()) return;

        if (canGrabOnLand) {
            if (!isAlreadyGrabbed && this.getHealth() >= 10 && !(entity instanceof CrocodileEntity)) {
                this.grabEntity(entity);
                return;
            }
        }

        if (canGrab) {
            if (this.onGround()) {
                if (comboAttack == 3) {
                    this.grabEntity(entity);
                    return;
                }
            } else if (this.isInWater()) {
                this.grabEntity(entity);
                return;
            }
        }

        if (this.isTame() && !this.level().isClientSide() && !this.isGrabbing()) {
            boolean nearWaterTamed = crocodileBehaviorHandler.findNearestWaterSource(10) != null;
            boolean alreadyGrabbed = entity.getVehicle() instanceof CrocodileEntity croc && croc.getOwner() != entity;
            if (nearWaterTamed && !alreadyGrabbed && !this.isSleeping() && !this.isNapping()
                    && this.getHealth() >= 10 && !(entity instanceof CrocodileEntity)
                    && this.getRandom().nextInt(100) < 20) {
                grabEntityPassive(entity);
            }
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
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CrocodileEntity otherCrocodile) {
            if (otherCrocodile.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherCrocodile.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherCrocodile.getOwnerUUID());
            } else {
                return !otherCrocodile.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == this.getGrabbedTarget()) {
            Vec3 look = this.getLookAngle();
            if (passenger instanceof Player) {
                function.accept(passenger, this.getX() + look.x * 2.65f, this.getY() - 1.0, this.getZ() + look.z * 2.65f);
            } else {
                function.accept(passenger, this.getX() + look.x * 1.75f, this.getY() - 0.2, this.getZ() + look.z * 1.75f);
            }
            return;
        }
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        double seatX = bodyAnimX / 16.0f * this.getScale();
        double seatY = getBaseRiderYOffset() + getRiderAnimYOffset();
        double seatZ = 0.0;

        double yRad = Math.toRadians(-this.yBodyRot);
        double worldX = seatX * Math.cos(yRad) + seatZ * Math.sin(yRad);
        double worldZ = -seatX * Math.sin(yRad) + seatZ * Math.cos(yRad);

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + worldX, this.getY() + seatY, this.getZ() + worldZ);
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
        return 7; // index 1 = GOLD, … 7 réservés, 8 = reset (no skin)
    }

    // ==================================================
    //             GRAB & MOBILITÉ
    // ==================================================

    /**
     * Libère proprement une cible attrapée : réinitialise la physique, stoppe le death roll
     * et efface la cible. Les crocs voisins qui ciblaient ce croco sont gérés côté appelant.
     */
    public void releaseGrab() {
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
        passiveGrabTimer = 0;
    }

    @Override
    protected boolean isImmobile() {
        return this.isGrabbing() && this.getGrabbedTarget() instanceof net.minecraft.world.entity.player.Player;
    }

    private void grabEntity(LivingEntity entity) {
        if (isBaby()) return;
        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return;

        if (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.getControllingPassenger() != null) {
            entity = tamableAnimal.getControllingPassenger();
        }

        long currentTime = this.level().getGameTime();
        if (currentTime - lastGrabTime < MAX_GRAB_COOLDOWN) return;

        int[] slidingLevels = getSlidingLevels(entity);
        float[] slidingMultiplier = OWEnchantments.SLIDING_ARMOR_MULTIPLIERS;
        int chance = this.getRandom().nextInt(100);

        float chancesToAvoidingGrab = calculateChanceToAvoidingGrab(slidingLevels, slidingMultiplier);

        if (chance >= chancesToAvoidingGrab) {
            if (entity instanceof OWEntity owEntity) {
                if (owEntity.getTheoreticalScale() <= 20) {
                    this.setGrabbing(true, entity);
                }
            } else {
                this.setGrabbing(true, entity);
            }

            this.setGrabTimeout(300);
            lastGrabTime = currentTime;
        }
    }

    private void grabEntityPassive(LivingEntity entity) {
        if (isBaby()) return;
        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return;

        if (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.getControllingPassenger() instanceof LivingEntity rider) {
            entity = rider;
        }

        long currentTime = this.level().getGameTime();
        if (currentTime - lastGrabTime < MAX_GRAB_COOLDOWN) return;

        this.setGrabbing(true, entity);
        passiveGrabTimer = 200;
        lastGrabTime = currentTime;
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

        if (this.isDeathRolling()) {
            if (this.getDeathRollProgress() == 1) {
                this.deathRollAnimationState.start(this.tickCount);
            }
        } else {
            this.deathRollAnimationState.stop();
        }

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

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, 37);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, 37);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 37);
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

    public boolean isMad() {
        return this.entityData.get(IS_MAD);
    }

    public void setDeathRollProgress(int getDeathRollProgress) {
        this.entityData.set(DEATH_ROLLING_PROGRESS, getDeathRollProgress);
    }

    public int getDeathRollProgress() {
        return Math.min(this.entityData.get(DEATH_ROLLING_PROGRESS), 40);
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
        if (this.level().isClientSide()) return false;
        return this.getGrabbedTarget() != null && this.isGrabbing();
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

    // ==================================================
    //           MOUTH SLAM (attaque chargée RMB)
    // ==================================================

    public void startMouthSlamCharge() {
        setPlayerMouthCharging(true);
        setChargingMouthTimer(0);
    }

    public void cancelMouthSlamCharge() {
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
    }

    // ==================================================
    //           PRIMAL DIVE (ultime)
    // ==================================================

    public void activatePrimalDive() {
        if (getUltimateKillCount() < OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED) return;
        if (!this.isInWater()) return;
        float cost = OWAttacksConstants.Crocodile.PRIMAL_DIVE_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
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
        if (!(raw instanceof LivingEntity target) || !target.isAlive() || !target.isInWater()) {
            cancelPrimalDive();
            return;
        }

        this.entityData.set(LUNGE_TARGET_ID, targetEntityId);
        this.entityData.set(IS_LUNGING, true);
        primalDiveLungeTimer = 40;
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
    public boolean isPlayerControlledDeathRoll() {
        return this.isTame() && this.isGrabbing();
    }

    public int getUltimateKillCount() {
        return this.entityData.get(ULTIMATE_KILL_COUNT);
    }

    public void setUltimateKillCount(int count) {
        this.entityData.set(ULTIMATE_KILL_COUNT, count);
    }

    /**
     * Exécute le Mouth Slam après une charge valide (≥ 1 s).
     *
     * @param factor 0.0 = 1 s de charge, 1.0 = 3 s de charge
     */
    public void performMouthSlam(float factor) {
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
        float energyRequired = OWAttacksConstants.Crocodile.MOUTH_SLAM_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - energyRequired) {
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
            super(mob, targetType, mustSee);
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