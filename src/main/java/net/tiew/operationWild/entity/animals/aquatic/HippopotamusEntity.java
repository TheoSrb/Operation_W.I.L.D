package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.HippopotamusVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.packets.to_client.HippopotamusCrushPacket;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HippopotamusEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 230.0;
    public static final int MAX_SOMNOLENCE = 3600;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 3;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 6;
    public static final int FOOD_WANTED_MIN = 10;
    public static final int FOOD_WANTED_MAX = 16;
    public static final int ENTITY_COLOR = 0x675250;
    public static final int DEFAULT_SKIN_INDEX = 1;

    public static final int STAGGER_NONE = 0;
    public static final int STAGGER_BOUNCE = 1;
    public static final int STAGGER_DIZZY = 2;

    private static final int MISC_IDLE_2_DURATION = 70;
    private static final int FURY_SNORT_INTERVAL = 9;
    private static final double DRIVER_SEAT_FORWARD = 0.0;
    private static final double PASSENGER_SEAT_FORWARD = -0.8;
    private static final double SEAT_PIVOT_BACK = 0.125;
    private static final float CAMERA_TILT_FOLLOW_LAND = 0.6f;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private static final float SWIM_DEPTH = 0.9f;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROLLING = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ROLL_STAGGER = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROLL_STAGGER_KIND = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FURY_TICK = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FURY_GRUDGE = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RIVER_SURGE = SynchedEntityData.defineId(HippopotamusEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState miscIdleAnimationState = new AnimationState();
    public final AnimationState furyRoarAnimationState = new AnimationState();

    private int miscIdleAnimationStartTime = 0;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(500, 1100);

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private float rollMomentum = 0f;
    private boolean rollMomentumActive = false;
    private double rollLastY = Double.NaN;
    private float rollSlope = 0f;

    private double lastTickX = Double.NaN;
    private double lastTickZ = Double.NaN;
    private double measuredSpeed = 0.0;
    private double measuredSpeedPrev = 0.0;
    private final Map<Integer, Integer> crushImmunity = new HashMap<>();
    private int rumbleCooldown = 0;
    private int waterTicks = 0;

    private double visX;
    private double visZ;
    private boolean visValid = false;
    private int visLastFuryElapsed = -1;
    private int visLastStagger = 0;

    private float rollBlend, rollBlendO;
    private float rollAngle, rollAngleO;
    private float unrollTarget = Float.NaN;
    private float furyBlend, furyBlendO;
    private float dizzyBlend, dizzyBlendO;
    private float swimBlend, swimBlendO;
    private float runBlend, runBlendO;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public HippopotamusEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.165D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(2, new HippopotamusMeleeAttackGoal());

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
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(ROLLING, false);
        builder.define(ROLL_STAGGER, 0);
        builder.define(ROLL_STAGGER_KIND, STAGGER_NONE);
        builder.define(FURY_TICK, 0);
        builder.define(FURY_GRUDGE, 0f);
        builder.define(RIVER_SURGE, 0);
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
        return 13f;
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
        return 3.5f;
    }

    @Override
    public int asleepSomnolenceLossInterval() {
        return SOMNOLENCE_LOSS_INTERVAL_ASLEEP;
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
        return 2.8f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.93f;
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
        return OWAdvancements.HIPPOPOTAMUS_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 400f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.9f * (1 + ((float) this.getLevel() / 50));
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
        if (isFuryWindup() || isRollStaggered()) return 0f;
        if (isRolling()) return rollTurnSpeed();
        return 0.14f;
    }

    private float rollTurnSpeed() {
        LivingEntity rider = this.getControllingPassenger();
        if (rider == null) return OWAttacksConstants.Hippopotamus.ROLL_TURN_SPEED;
        float delta = Math.abs(Mth.wrapDegrees(rider.getYRot() - this.getYRot()));
        if (delta < 1.0E-3f) return OWAttacksConstants.Hippopotamus.ROLL_TURN_SPEED;
        return Math.min(OWAttacksConstants.Hippopotamus.ROLL_TURN_SPEED,
                OWAttacksConstants.Hippopotamus.ROLL_TURN_MAX_DEGREES / delta);
    }

    @Override
    protected boolean canLean() {
        return !this.isSitting() && !this.isSleeping() && !this.isNapping() && !isFuryWindup();
    }

    @Override
    protected float bankMaxAngle() {
        if (this.isInWater()) return 45f;
        return isRolling() ? 24f : 18f;
    }

    @Override
    protected float bankReferenceYawRate(boolean ridden) {
        if (isRolling()) return 1.0f;
        if (this.isInWater()) return super.bankReferenceYawRate(ridden);
        return ridden ? 7.0f : 2.5f;
    }

    @Override
    protected float pitchMaxAngle() {
        return this.isInWater() ? super.pitchMaxAngle() : 0f;
    }

    @Override
    public Vec3 riderCameraOffset(Player player, float eyeHeight, float partialTick) {
        float follow = Mth.lerp(smoothStep(swimBlend(partialTick)), CAMERA_TILT_FOLLOW_LAND, 1f);
        return net.tiew.operationWild.event.ClientEvents.riderEyeOffset(player, this, eyeHeight, partialTick).scale(follow);
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
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.HIPPOPOTAMUS.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.HIPPOPOTAMUS_FOOD);
    }

    @Override
    public Item favoriteFoodTier3() {
        return Items.MELON;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public float getSpeed() {
        float speed = super.getSpeed();
        if (isRiverSurging()) speed *= 1f + OWAttacksConstants.Hippopotamus.RIVER_SURGE_SPEED_BONUS;
        if (isRiverFuryActive()) speed *= OWAttacksConstants.Hippopotamus.FURY_SPEED_FACTOR;
        return speed;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 18 : 6;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 3;
    }

    @Override
    public int getMaxAirSupply() {
        return 300 * 6;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir + 10;
    }

    @Override
    public int getSecondaryCooldownDuration() {
        return OWAttacksConstants.Hippopotamus.ROLL_COOLDOWN_TICKS;
    }

    @Override
    protected boolean forceRiderLookBodyRotation() {
        return isRolling();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.85f;
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (isRolling()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 160L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 6; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.18F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.7f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(0.8f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        createCombo(20, 12, actualAttackNumber == 1 ? SoundEvents.RAVAGER_ATTACK : SoundEvents.EVOKER_FANGS_ATTACK,
                3.2, 2.6, 2.3, actualAttackNumber == 2, actualAttackNumber == 2 ? 2.5f : 1.2f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isRolling() || this.isRiverFuryActive());
        }

        if (!this.level().isClientSide() && !this.isVehicle()) {
            if (this.getState() == 2) this.setRunning(true);
            else if (this.getTarget() == null) this.setRunning(false);
        }

        if (!this.level().isClientSide()) {
            tickRoll();
            tickStagger();
            tickRiverFury();
            tickRiverSurge();
        }

        if (this.level().isClientSide()) {
            tickClientVisuals();
            setupAnimationState();
        }

        if (this.isInResurrection()) this.setSleeping(true);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        if (this.level().isClientSide() || entity == null) return;
        if (entity == this.getControllingPassenger()) return;
        if (entity instanceof Player player && player.isCreative()) return;

        if (comboAttack == 3) {
            entity.push(0, 0.32, 0);
            entity.hurtMarked = true;
        }

        if (isRiverFuryActive()) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    OWAttacksConstants.Hippopotamus.FURY_STAGGER_TICKS, 2, false, true));
        }
    }

    @Override
    public void attackEntitiesInFront(float attackDamage, SoundEvent sound, double width, double height, double reach, float knockback) {
        float damage = isRiverFuryActive()
                ? attackDamage * (1f + OWAttacksConstants.Hippopotamus.FURY_DAMAGE_BONUS + getFuryGrudge())
                : attackDamage;
        super.attackEntitiesInFront(damage, sound, width, height, reach, knockback);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean furious = isRiverFuryActive() && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        float received = furious ? amount * (1f - OWAttacksConstants.Hippopotamus.FURY_DAMAGE_REDUCTION) : amount;
        boolean hurt = super.hurt(damageSource, received);

        if (hurt && furious && !this.level().isClientSide() && damageSource.getEntity() instanceof LivingEntity) {
            this.entityData.set(FURY_GRUDGE, Math.min(OWAttacksConstants.Hippopotamus.FURY_GRUDGE_MAX,
                    getFuryGrudge() + OWAttacksConstants.Hippopotamus.FURY_GRUDGE_PER_HIT));
        }
        return hurt;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (isRiverFuryActive() || isRolling()) return;
        super.knockback(strength, x, z);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Hippopotamus.FURY_KILLS_REQUIRED) setUltimateKillCount(kills + 1);
        return super.killedEntity(level, target);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.entityData.set(ROLLING, false);
            this.entityData.set(FURY_TICK, 0);
        }
        super.die(damageSource);

        if (!this.level().isClientSide() && this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isFuryWindup() || isRollStaggered();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isRolling() && !isRollStaggered() && !isFuryWindup();
    }

    @Override
    public boolean canUseUltimate() {
        return super.canUseUltimate() && !isRolling() && !isRollStaggered();
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        if (isCombo && !canStartCombo()) return;
        super.setCombo(isCombo, numberOfAttacks);
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        int stagger = getRollStagger();
        if (stagger > 0) {
            rollMomentum = 0f;
            rollMomentumActive = false;
            resetRiddenSpeed();
            if (getRollStaggerKind() == STAGGER_BOUNCE) {
                int elapsed = OWAttacksConstants.Hippopotamus.ROLL_BOUNCE_TICKS - stagger;
                int recoil = OWAttacksConstants.Hippopotamus.ROLL_BOUNCE_RECOIL_TICKS;
                if (elapsed < recoil) {
                    return -OWAttacksConstants.Hippopotamus.ROLL_BOUNCE_RECOIL_SPEED * (1f - (float) elapsed / recoil);
                }
            }
            return 0f;
        }

        if (isFuryWindup()) {
            rollMomentum = 0f;
            rollMomentumActive = false;
            resetRiddenSpeed();
            return 0f;
        }

        if (isRolling() || rollMomentumActive) return tickRollMomentum(player);

        return super.getRiddenSpeedVehicle(player);
    }

    private float rollTopSpeed() {
        return this.getSpeed() * (vehicleRunSpeedMultiplier() / 1.75f) * OWAttacksConstants.Hippopotamus.ROLL_TOP_SPEED_RATIO;
    }

    private float tickRollMomentum(Player player) {
        float top = rollTopSpeed();
        boolean rolling = isRolling();

        if (rolling && !rollMomentumActive) {
            rollMomentumActive = true;
            rollMomentum = Math.min(top, (float) this.getDeltaMovement().horizontalDistance());
            rollLastY = Double.NaN;
            rollSlope = 0f;
        }

        double y = this.getY();
        float slope = 0f;
        if (!Double.isNaN(rollLastY) && this.onGround()) slope = (float) (rollLastY - y);
        rollLastY = y;
        rollSlope += (slope - rollSlope) * 0.3f;

        if (!rolling) {
            float normal = super.getRiddenSpeedVehicle(player);
            rollMomentum *= OWAttacksConstants.Hippopotamus.ROLL_STOP_GLIDE;
            if (rollMomentum <= normal + 0.005f) {
                rollMomentum = 0f;
                rollMomentumActive = false;
                carryRiddenSpeed(normal);
                return normal;
            }
            return rollMomentum;
        }

        float target = player.zza < 0 ? top * OWAttacksConstants.Hippopotamus.ROLL_BRAKE_RATIO : top;
        float slopeBonus = Mth.clamp(rollSlope * OWAttacksConstants.Hippopotamus.ROLL_SLOPE_GAIN,
                -0.5f, OWAttacksConstants.Hippopotamus.ROLL_SLOPE_MAX_BONUS);
        target *= 1f + slopeBonus;

        float accel = top / OWAttacksConstants.Hippopotamus.ROLL_ACCEL_TICKS;
        if (rollMomentum < target) rollMomentum = Math.min(target, rollMomentum + accel);
        else rollMomentum = Math.max(target, rollMomentum - accel * 1.5f);
        return rollMomentum;
    }

    public void toggleRoll() {
        if (this.level().isClientSide()) return;
        if (isRolling()) {
            stopRoll(STAGGER_NONE);
            return;
        }
        if (!canStartRoll()) {
            rejectSecondary();
            return;
        }

        if (!isRiverFuryActive()) {
            float cost = OWAttacksConstants.Hippopotamus.ROLL_START_ENERGY;
            if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
                canShowVitalEnergyLack = true;
                rejectSecondary();
                return;
            }
            setVitalEnergy(getVitalEnergy() + cost);
        }

        if (this.isCombo()) {
            resetCombo(0);
            actualAttackNumber = 0;
        }
        endSlide();
        crushImmunity.clear();
        rumbleCooldown = 0;
        this.entityData.set(ROLLING, true);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.HOGLIN_ATTACK, SoundSource.NEUTRAL, 1.2f, 0.55f);
    }

    private boolean canStartRoll() {
        return this.getControllingPassenger() instanceof Player
                && !this.isInWater()
                && getSecondaryCooldown() <= 0
                && !isRollStaggered()
                && !isFuryWindup()
                && !this.isSitting()
                && !this.isKnockedOut();
    }

    public void stopRoll(int staggerKind) {
        if (!isRolling()) return;
        this.entityData.set(ROLLING, false);
        startSecondaryCooldown();

        if (staggerKind == STAGGER_BOUNCE) {
            this.entityData.set(ROLL_STAGGER_KIND, STAGGER_BOUNCE);
            this.entityData.set(ROLL_STAGGER, OWAttacksConstants.Hippopotamus.ROLL_BOUNCE_TICKS);
        } else if (staggerKind == STAGGER_DIZZY) {
            this.entityData.set(ROLL_STAGGER_KIND, STAGGER_DIZZY);
            this.entityData.set(ROLL_STAGGER, OWAttacksConstants.Hippopotamus.ROLL_DIZZY_TICKS);
        }
    }

    private void rejectSecondary() {
        if (this.getControllingPassenger() instanceof net.minecraft.server.level.ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OWAttackRejectedPacket(this.getId(), OWAttackIds.HIPPO_ROLL));
        }
    }

    private void tickRoll() {
        double x = this.getX();
        double z = this.getZ();
        double speed = 0.0;
        if (!Double.isNaN(lastTickX)) {
            double dx = x - lastTickX;
            double dz = z - lastTickZ;
            speed = Math.min(Math.sqrt(dx * dx + dz * dz), 2.0);
        }
        lastTickX = x;
        lastTickZ = z;
        measuredSpeedPrev = measuredSpeed;
        measuredSpeed = speed;

        crushImmunity.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);

        if (!isRolling()) return;

        if (!(this.getControllingPassenger() instanceof Player) || this.isKnockedOut() || this.isSleeping()) {
            stopRoll(STAGGER_NONE);
            return;
        }

        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > SWIM_DEPTH) {
            stopRoll(STAGGER_NONE);
            return;
        }

        if (!isRiverFuryActive()) {
            float applied = this.isRunning() ? 0.65f : -getVitalEnergyRecuperation();
            setVitalEnergy(getVitalEnergy() + OWAttacksConstants.Hippopotamus.ROLL_ENERGY_PER_TICK - applied);
            if (getVitalEnergy() >= getVitalEnergyCapacity()) {
                setVitalEnergy(getVitalEnergyCapacity());
                canShowVitalEnergyLack = true;
                stopRoll(STAGGER_DIZZY);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.RAVAGER_STUNNED, SoundSource.NEUTRAL, 1.0f, 1.1f);
                return;
            }
        }

        if (measuredSpeedPrev >= OWAttacksConstants.Hippopotamus.ROLL_WALL_BOUNCE_MIN_SPEED
                && measuredSpeed < measuredSpeedPrev * 0.3
                && this.horizontalCollision) {
            bounceOffObstacle();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        tickRollRumble(serverLevel);

        if (measuredSpeed >= OWAttacksConstants.Hippopotamus.ROLL_CRUSH_MIN_SPEED) crushAhead(serverLevel);
    }

    private void tickRollRumble(ServerLevel serverLevel) {
        if (rumbleCooldown > 0) {
            rumbleCooldown--;
            return;
        }
        rumbleCooldown = Mth.clamp((int) (7 - measuredSpeed * 12), 2, 7);
        if (!this.onGround() || measuredSpeed < 0.04) return;

        BlockState below = this.getBlockStateOn();
        if (below.isAir()) return;

        SoundType soundType = below.getSoundType(serverLevel, this.blockPosition(), this);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), soundType.getStepSound(),
                SoundSource.NEUTRAL, soundType.getVolume() * 0.9f, soundType.getPitch() * 0.55f);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below),
                this.getX(), this.getY() + 0.1, this.getZ(),
                10, this.getBbWidth() * 0.4, 0.05, this.getBbWidth() * 0.4, 0.15);
    }

    private void crushAhead(ServerLevel serverLevel) {
        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        AABB box = this.getBoundingBox().inflate(0.3, 0.0, 0.3).move(forward.scale(0.55));
        float topSpeed = Math.max(rollTopSpeed(), 0.05f);
        float speedFactor = Mth.clamp((float) measuredSpeed / topSpeed, 0.4f, 1.25f);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (!target.isAlive() || target.isSpectator()) continue;
            if (target instanceof Player player && player.isCreative()) continue;
            if (crushImmunity.containsKey(target.getId())) continue;

            crushImmunity.put(target.getId(), this.tickCount + OWAttacksConstants.Hippopotamus.ROLL_CRUSH_IMMUNITY_TICKS);

            if (this.isAlliedTo(target) || target == this.getOwner()) {
                nudgeAlly(target);
                continue;
            }

            if (isTooBigToCrush(target)) {
                target.hurt(this.damageSources().mobAttack(this), this.getDamage() * 0.4f * speedFactor);
                bounceOffObstacle();
                return;
            }

            float damage = this.getDamage() * OWAttacksConstants.Hippopotamus.ROLL_CRUSH_DAMAGE_RATIO * speedFactor;
            if (isRiverFuryActive()) damage *= OWAttacksConstants.Hippopotamus.FURY_ROLL_CRUSH_MULTIPLIER;

            if (!target.hurt(this.damageSources().mobAttack(this), damage)) continue;

            target.knockback(OWAttacksConstants.Hippopotamus.ROLL_CRUSH_KNOCKBACK * speedFactor,
                    this.getX() - target.getX(), this.getZ() - target.getZ());
            flatten(target);

            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.3f, 0.6f);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.0f, (float) OWUtils.generateRandomInterval(0.7, 0.9));
            serverLevel.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.2, target.getZ(),
                    8, target.getBbWidth() * 0.5, 0.05, target.getBbWidth() * 0.5, 0.04);
        }
    }

    private boolean isTooBigToCrush(LivingEntity target) {
        if (target instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= OWAttacksConstants.Hippopotamus.ROLL_CRUSH_MAX_SCALE) {
            return true;
        }
        return target.getBbWidth() * target.getBbHeight() > this.getBbWidth() * this.getBbHeight();
    }

    private void nudgeAlly(LivingEntity ally) {
        Vec3 away = ally.position().subtract(this.position());
        if (away.horizontalDistanceSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, this.getYRot() + 90f);
        away = new Vec3(away.x, 0, away.z).normalize().scale(OWAttacksConstants.Hippopotamus.ROLL_ALLY_NUDGE);
        ally.push(away.x, 0.12, away.z);
        ally.hurtMarked = true;
    }

    private void flatten(LivingEntity target) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
                new HippopotamusCrushPacket(target.getId(), OWAttacksConstants.Hippopotamus.ROLL_CRUSH_FLATTEN_TICKS));
    }

    private void bounceOffObstacle() {
        stopRoll(STAGGER_BOUNCE);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_STUNNED, SoundSource.NEUTRAL, 1.4f, 0.8f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0f, 0.5f);

        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                this.getX() + forward.x * this.getBbWidth() * 0.6,
                this.getY() + this.getBbHeight() * 0.5,
                this.getZ() + forward.z * this.getBbWidth() * 0.6,
                1, 0, 0, 0, 0);
    }

    private void tickStagger() {
        int stagger = getRollStagger();
        if (stagger <= 0) return;

        this.entityData.set(ROLL_STAGGER, stagger - 1);
        if (stagger - 1 <= 0) this.entityData.set(ROLL_STAGGER_KIND, STAGGER_NONE);

        if (getRollStaggerKind() == STAGGER_DIZZY && stagger % 6 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    this.getX(), this.getY() + this.getBbHeight() * 1.05, this.getZ(),
                    3, 0.35, 0.1, 0.35, 0.02);
        }
    }

    public boolean activateRiverFury() {
        if (this.level().isClientSide()) return false;
        if (getFuryTick() > 0) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Hippopotamus.FURY_KILLS_REQUIRED) return false;
        if (isRolling() || isRollStaggered()) return false;

        float cost = OWAttacksConstants.Hippopotamus.FURY_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        resetCombo(0);
        actualAttackNumber = 0;
        endSlide();

        this.entityData.set(FURY_GRUDGE, 0f);
        this.entityData.set(FURY_TICK, furyTotalTicks());
        if (!(this.getControllingPassenger() instanceof Player)) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL, 3.0f, 0.7f);
        return true;
    }

    private static int furyTotalTicks() {
        return OWAttacksConstants.Hippopotamus.FURY_WINDUP_TICKS
                + OWAttacksConstants.Hippopotamus.FURY_DURATION_TICKS
                + OWAttacksConstants.Hippopotamus.FURY_FADE_TICKS;
    }

    private void tickRiverFury() {
        int tick = getFuryTick();
        if (tick <= 0) return;

        int elapsed = furyTotalTicks() - tick;
        this.entityData.set(FURY_TICK, tick - 1);

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (elapsed == OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_TICK) furyShockwave(serverLevel);

        if (tick - 1 == OWAttacksConstants.Hippopotamus.FURY_FADE_TICKS) {
            this.entityData.set(FURY_GRUDGE, 0f);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.HORSE_BREATHE, SoundSource.NEUTRAL, 1.6f, 0.45f);
            snort(serverLevel, 14, 0.06);
        }

        if (isRiverFuryActive() && elapsed > OWAttacksConstants.Hippopotamus.FURY_WINDUP_TICKS
                && elapsed % FURY_SNORT_INTERVAL == 0) {
            snort(serverLevel, 3, 0.02);
            if (this.random.nextInt(4) == 0) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.HORSE_BREATHE, SoundSource.NEUTRAL, 0.8f, (float) OWUtils.generateRandomInterval(0.5, 0.65));
            }
        }
    }

    private void snort(ServerLevel serverLevel, int count, double speed) {
        Vec3 nose = nosePosition();
        serverLevel.sendParticles(ParticleTypes.CLOUD, nose.x, nose.y, nose.z, count, 0.08, 0.04, 0.08, speed);
    }

    private Vec3 nosePosition() {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        double reach = 1.95 * this.getScale();
        return new Vec3(this.getX() + forward.x * reach,
                this.getY() + 0.95 * this.getScale(),
                this.getZ() + forward.z * reach);
    }

    private void furyShockwave(ServerLevel serverLevel) {
        double radius = OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_RADIUS;
        AABB area = this.getBoundingBox().inflate(radius, 1.5, radius);

        for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living == this || living.getRootVehicle() == this) continue;
            if (this.isAlliedTo(living) || living == this.getOwner()) continue;
            if (living instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            double distance = living.distanceTo(this);
            if (distance > radius) continue;

            Vec3 push = living.position().subtract(this.position());
            if (push.horizontalDistanceSqr() < 1.0E-4) push = this.getLookAngle();
            double power = OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_PUSH * (1.0 - 0.5 * distance / radius);
            push = new Vec3(push.x, 0, push.z).normalize().scale(power);
            living.push(push.x, 0.35, push.z);
            living.hurtMarked = true;
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_SLOW_TICKS, 1));
        }

        BlockState below = this.getBlockStateOn();
        if (!below.isAir()) {
            for (int i = 0; i < 24; i++) {
                double angle = i * (Math.PI * 2.0 / 24.0);
                double ringX = this.getX() + Math.cos(angle) * radius * 0.6;
                double ringZ = this.getZ() + Math.sin(angle) * radius * 0.6;
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below),
                        ringX, this.getY() + 0.1, ringZ, 4, 0.3, 0.05, 0.3, 0.2);
            }
        }
        serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(),
                30, radius * 0.4, 0.1, radius * 0.4, 0.05);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.7f, 0.55f);
    }

    private void tickRiverSurge() {
        if (this.isInWater()) {
            waterTicks++;
            if (waterTicks >= OWAttacksConstants.Hippopotamus.RIVER_SURGE_MIN_WATER_TICKS) {
                this.entityData.set(RIVER_SURGE, OWAttacksConstants.Hippopotamus.RIVER_SURGE_TICKS);
            }
            return;
        }

        waterTicks = 0;
        int surge = getRiverSurge();
        if (surge > 0) this.entityData.set(RIVER_SURGE, surge - 1);
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.61 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        boolean driver = this.getPassengers().indexOf(passenger) == 0;
        float roll = this.level().isClientSide() ? smoothStep(rollBlend(seatPartialTick)) : (isRolling() ? 1f : 0f);
        double scale = this.getScale();

        double forward = (driver ? DRIVER_SEAT_FORWARD : PASSENGER_SEAT_FORWARD) * scale * (1f - roll);
        double side = OWAttacksConstants.Hippopotamus.ROLL_SEAT_SPREAD * scale * roll * (driver ? 1 : -1);
        double pitch = Math.toRadians(this.getBodyXRot()) * (1f - roll);
        double tilt = (forward + SEAT_PIVOT_BACK * scale) * Math.sin(pitch);

        double seatY = getBaseRiderYOffset() + getRiderAnimYOffset() + tilt
                + OWAttacksConstants.Hippopotamus.ROLL_SEAT_LIFT * scale * roll;
        Vec3 seatOffset = new Vec3(side, 0, forward).yRot((float) Math.toRadians(-this.yBodyRot));

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + seatY, this.getZ() + seatOffset.z);

        if (!driver && passenger instanceof LivingEntity living) living.yBodyRot = this.yBodyRot;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(HippopotamusVariant.DEFAULT);
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private void tickClientVisuals() {
        double dx = 0.0;
        double dz = 0.0;
        if (visValid) {
            dx = this.getX() - visX;
            dz = this.getZ() - visZ;
            if (dx * dx + dz * dz > 16.0) {
                dx = 0.0;
                dz = 0.0;
            }
        }
        visX = this.getX();
        visZ = this.getZ();
        visValid = true;

        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        float travelled = (float) (dx * forward.x + dz * forward.z);
        float radius = OWAttacksConstants.Hippopotamus.ROLL_RADIUS_BLOCKS * this.getScale();

        tickRollVisuals(travelled, radius);

        furyBlendO = furyBlend;
        float furyTarget = isRiverFuryActive() ? 1f : 0f;
        float furyRate = furyTarget > furyBlend ? 1f / 12f : 1f / OWAttacksConstants.Hippopotamus.FURY_FADE_TICKS;
        furyBlend = approach(furyBlend, furyTarget, furyRate);

        dizzyBlendO = dizzyBlend;
        boolean dizzy = getRollStaggerKind() == STAGGER_DIZZY && getRollStagger() > 0 && rollBlend < 0.35f;
        dizzyBlend = approach(dizzyBlend, dizzy ? 1f : 0f, dizzy ? 0.12f : 0.08f);

        swimBlendO = swimBlend;
        boolean swimming = this.isInWater() && this.getFluidHeight(FluidTags.WATER) > SWIM_DEPTH;
        swimBlend = approach(swimBlend, swimming ? 1f : 0f, 0.1f);

        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() ? 1f : 0f, 0.2f);

        int elapsed = furyElapsed();
        if (visLastFuryElapsed >= 0 && visLastFuryElapsed < OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_TICK
                && elapsed >= OWAttacksConstants.Hippopotamus.FURY_SHOCKWAVE_TICK) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        visLastFuryElapsed = elapsed;

        int stagger = getRollStagger();
        if (stagger > visLastStagger && getRollStaggerKind() == STAGGER_BOUNCE) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        visLastStagger = stagger;

        if (isRiverSurging() && this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.DRIPPING_WATER,
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * (0.3 + this.random.nextDouble() * 0.6),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0, 0, 0);
        }
    }

    private void tickRollVisuals(float travelled, float radius) {
        rollBlendO = rollBlend;
        rollAngleO = rollAngle;

        if (isRolling()) {
            unrollTarget = Float.NaN;
            rollBlend = approach(rollBlend, 1f, 1f / OWAttacksConstants.Hippopotamus.ROLL_CURL_TICKS);
            rollAngle += travelled / radius * smoothStep(rollBlend);
        } else if (rollBlend > 0f) {
            if (Float.isNaN(unrollTarget)) {
                boolean bounced = getRollStaggerKind() == STAGGER_BOUNCE;
                float turns = rollAngle / FULL_TURN;
                unrollTarget = (float) (bounced ? Math.round(turns) : Math.ceil(turns)) * FULL_TURN;
            }

            float remaining = unrollTarget - rollAngle;
            float step = Math.abs(remaining) * 0.2f + Math.abs(travelled) / radius;
            step = Math.min(Math.abs(remaining), Math.max(step, 0.04f));
            rollAngle += Math.signum(remaining) * step;

            if (Math.abs(unrollTarget - rollAngle) < 0.7f) {
                rollBlend = approach(rollBlend, 0f, 1f / OWAttacksConstants.Hippopotamus.ROLL_UNCURL_TICKS);
            }

            if (rollBlend <= 0f) {
                rollAngle = 0f;
                rollAngleO = 0f;
                unrollTarget = Float.NaN;
            }
        }

        if (Math.abs(rollAngle) > FULL_TURN * 8f && Float.isNaN(unrollTarget)) {
            float wrap = (float) Math.floor(rollAngle / FULL_TURN) * FULL_TURN;
            rollAngle -= wrap;
            rollAngleO -= wrap;
        }
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    public static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    public float rollBlend(float partialTick) { return Mth.lerp(partialTick, rollBlendO, rollBlend); }

    public float rollAngle(float partialTick) { return Mth.lerp(partialTick, rollAngleO, rollAngle); }

    public float furyBlend(float partialTick) { return Mth.lerp(partialTick, furyBlendO, furyBlend); }

    public float dizzyBlend(float partialTick) { return Mth.lerp(partialTick, dizzyBlendO, dizzyBlend); }

    public float swimBlend(float partialTick) { return Mth.lerp(partialTick, swimBlendO, swimBlend); }

    public float runBlend(float partialTick) { return Mth.lerp(partialTick, runBlendO, runBlend); }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(72, true);
        createSitAnimation(120, true);

        this.furyRoarAnimationState.animateWhen(isFuryWindup(), this.tickCount);

        handleMiscIdleAnimations();
        setupComboAnimations();
    }

    private void handleMiscIdleAnimations() {
        if (this.miscIdleAnimationState.isStarted()
                && (this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_2_DURATION || !canPlayIdleAnimation())) {
            this.miscIdleAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (canPlayIdleAnimation() && !isAnyIdleAnimationPlaying()) {
            this.miscIdleAnimationState.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.HOGLIN_AMBIENT, this.getSoundSource(), 0.7f, isBaby() ? 1.1f : 0.55f, false);
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(600, 1300);
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (26 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (24 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (28 / comboSpeedMultiplier));
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
                && !this.isVehicle() && !this.isInWater() && !this.isSitting()
                && getFuryTick() <= 0 && !isRolling() && !isRollStaggered();
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof HippopotamusEntity hippopotamus) {
            hippopotamus.setVariant(HippopotamusVariant.byId(variant));
            hippopotamus.setInitialVariant(HippopotamusVariant.byId(variant));
        }
    }

    public HippopotamusVariant getVariant() {
        return HippopotamusVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(HippopotamusVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(HippopotamusVariant skin) {
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

    public HippopotamusVariant getInitialVariant() {
        return HippopotamusVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(HippopotamusVariant variant) {
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

    public boolean isRolling() { return this.entityData.get(ROLLING); }

    public int getRollStagger() { return this.entityData.get(ROLL_STAGGER); }

    public int getRollStaggerKind() { return this.entityData.get(ROLL_STAGGER_KIND); }

    public boolean isRollStaggered() { return getRollStagger() > 0; }

    public int getFuryTick() { return this.entityData.get(FURY_TICK); }

    public int furyElapsed() { return getFuryTick() <= 0 ? -1 : furyTotalTicks() - getFuryTick(); }

    public boolean isFuryWindup() {
        int elapsed = furyElapsed();
        return elapsed >= 0 && elapsed < OWAttacksConstants.Hippopotamus.FURY_WINDUP_TICKS;
    }

    public boolean isRiverFuryActive() { return getFuryTick() > OWAttacksConstants.Hippopotamus.FURY_FADE_TICKS; }

    public float getFuryGrudge() { return this.entityData.get(FURY_GRUDGE); }

    public int getRiverSurge() { return this.entityData.get(RIVER_SURGE); }

    public boolean isRiverSurging() { return getRiverSurge() > 0 && !this.isInWater(); }

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

    class HippopotamusMeleeAttackGoal extends MeleeAttackGoal {

        public HippopotamusMeleeAttackGoal() {
            super(HippopotamusEntity.this, 6, true);
        }

        @Override
        public void start() {
            super.start();
            HippopotamusEntity.this.setMad(true);
            HippopotamusEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            HippopotamusEntity.this.setMad(false);
            HippopotamusEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 3.0;
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
