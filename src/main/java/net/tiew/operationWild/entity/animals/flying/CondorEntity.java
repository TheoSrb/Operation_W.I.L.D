package net.tiew.operationWild.entity.animals.flying;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Condor;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.particle.OWParticles;
import net.tiew.operationWild.entity.bosses.OWBossEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.config.OWTargetLists;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.CondorVariant;
import net.tiew.operationWild.item.OWItems;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CondorEntity extends OWSemiFlyingEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 190.0;
    public static final int MAX_SOMNOLENCE = 1800;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 1;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 2;
    public static final int FOOD_WANTED_MIN = 8;
    public static final int FOOD_WANTED_MAX = 13;
    public static final int ENTITY_COLOR = 0x404557;
    public static final int DEFAULT_SKIN_INDEX = 0;

    public static final double BASE_HEALTH = 32.0;
    public static final double BASE_DAMAGE = 6.0;
    public static final double BASE_SPEED = 0.16;

    private static final float MAX_VITAL_ENERGY = 360f;
    private static final int SEDATION_WAKE_TICKS = 50;
    private static final int GREY_VARIANT_CHANCE = 25;
    private static final float RUN_BLEND_RATE = 0.2f;
    private static final float SIT_BLEND_RATE = 0.1f;
    private static final float REST_BLEND_RATE = 0.07f;
    private static final int IDLE_GESTURE_MIN_DELAY = 360;
    private static final int IDLE_GESTURE_MAX_DELAY = 820;
    private static final float VOICE_PITCH = 0.55f;
    private static final Vector3f RIDER_FEET_IN_BODY2 = new Vector3f(0f, -1f / 16f, -4f / 16f);
    private static final Vector3f REST_RIDER_FEET_MODEL = new Vector3f(0f, 8f / 16f, -5f / 16f);
    private static final double MODEL_ORIGIN_Y = 1.501;
    private static final float CARRY_BLEND_RATE = 0.2f;
    private static final double PREY_LIFT_STEP = 0.125;
    private static final int PREY_LIFT_MAX_STEPS = 40;
    private static final double RELEASE_MAX_SPEED = 1.5;
    private static final double WATER_EXIT_LIFT = 0.42;
    private static final double WATER_EXIT_CLEARANCE = 1.6;
    private static final double WATER_FLOAT_SPEED = 0.05;
    private static final float WATER_FLOAT_FRACTION = 0.45f;

    public static final int TALON_NONE = 0;
    public static final int TALON_GRAB = 1;
    public static final int TALON_CARRY = 2;
    public static final int TALON_DROP = 3;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TALON_PREY = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TALON_PHASE = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TALON_COOLDOWN = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState sunbatheAnimationState = new AnimationState();
    public final AnimationState preenAnimationState = new AnimationState();
    public final AnimationState shakeAnimationState = new AnimationState();
    public final AnimationState displayAnimationState = new AnimationState();

    private int idleGestureCooldown = IDLE_GESTURE_MIN_DELAY;
    private int idleGestureTicks = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile Matrix4f boneMatrix = null;
    public volatile Matrix4f talonFrame = null;

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private long lastStepSoundMs = 0L;

    private int talonTicks = 0;
    private int carryTicks = 0;
    private boolean preyAlly = false;
    private boolean gentleRelease = false;
    private int releasedPreyId = -1;
    private Vec3 talonPrevPos = null;
    private final Map<Entity, Integer> softLandings = new LinkedHashMap<>();
    private int clientTalonPhase = TALON_NONE;
    private int grabStartTick = -1000;
    private int dropStartTick = -1000;
    private float carryBlend, carryBlendO;
    private boolean tailwindUnlocked = false;
    private boolean ironTalonsUnlocked = false;
    private int tailwindTicks = 0;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public CondorEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new CondorMeleeAttackGoal());
        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 900, true));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new CondorGroundStrollGoal());
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(TALON_PREY, -1);
        builder.define(TALON_PHASE, TALON_NONE);
        builder.define(TALON_COOLDOWN, 0);
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
    public boolean hasSedationAnimations() {
        return true;
    }

    @Override
    public int sedationWakeTicks() {
        return SEDATION_WAKE_TICKS;
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
    public float sedatedFleeSpeed() {
        return 5.0f;
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
        return OWEntityConfig.Archetypes.TANK;
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
        return OWTargetLists.CONDOR;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.6f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.4f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 2.2f;
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
        return OWItems.PEACOCK_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.CONDOR_TAMED_ADVANCEMENT;
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
        return true;
    }

    @Override
    public boolean preferVegetables() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        return 0.12f;
    }

    @Override
    public org.joml.Matrix4f riderBoneMatrix() {
        return this.boneMatrix;
    }

    @Override
    public double vehicleFlySpeed() {
        return 0.2;
    }

    @Override
    public double vehicleFlyRunSpeed() {
        return 0.35;
    }

    @Override
    public boolean canGlideInFlight() {
        return true;
    }

    @Override
    public int vehicleTakeoffDuration() {
        return 24;
    }

    @Override
    public double vehicleDiveSpeed() {
        return 1.15;
    }

    @Override
    public float vehicleGlideEntryPitch() {
        return 25f;
    }

    @Override
    public double vehicleClimbSpeed() {
        return 0.18;
    }

    @Override
    public double vehicleGlideExitSpeed() {
        return 0.42;
    }

    @Override
    public double vehicleGlideDiveGain() {
        return 0.035;
    }

    @Override
    public double vehicleGlideDrag() {
        return 0.0045;
    }

    @Override
    public double vehicleFlyAcceleration() {
        return 0.1;
    }

    @Override
    public double vehicleFlyDeceleration() {
        return 0.08;
    }

    @Override
    public float vehicleFlyTurnSpeed() {
        return 4.6f;
    }

    @Override
    public float vehicleFlyRunTurnSpeed() {
        return 3.4f;
    }

    @Override
    public float vehicleGlideTurnSpeed() {
        return 3.8f;
    }

    @Override
    public float takeoffEnergyCost() {
        return 12f;
    }

    @Override
    public float flyEnergyCost() {
        return 0.1f;
    }

    @Override
    public float flyRunEnergyCost() {
        return 0.18f;
    }

    @Override
    public float hoverEnergyCost() {
        return 0.14f;
    }

    @Override
    public float climbEnergyCost() {
        return 0.2f;
    }

    @Override
    public float glideEnergyCost() {
        return 0.025f;
    }

    @Override
    public double wingSpan() {
        return 1.9 * this.getScale();
    }

    @Override
    public @Nullable SoundEvent wingFlapSound() {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.CONDOR.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CONDOR_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return VOICE_PITCH + (this.random.nextFloat() - 0.5f) * 0.1f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 320;
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround() || this.isInWater() || this.isAirborneFlight()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 150L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        SoundType soundtype = blockState.getSoundType(this.level(), this.blockPosition(), this);
        for (int i = 0; i < 2; i++) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundtype.getStepSound(), this.getSoundSource(),
                    soundtype.getVolume() * 0.18F, soundtype.getPitch() * pitchMod, false);
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.9f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.0f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(16, 10, SoundEvents.PLAYER_ATTACK_SWEEP, 3.0, 3.0, 1.6, actualAttackNumber == 3, actualAttackNumber == 3 ? 2.0f : 0.8f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo());
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.tickCount % 20 == 0 || this.tickCount < 2) {
            tailwindUnlocked = OWPistePassives.has(this, OWPistePassives.TAILWIND);
            ironTalonsUnlocked = OWPistePassives.has(this, OWPistePassives.IRON_TALONS);
        }
        tickTailwind();

        if (this.level().isClientSide()) {
            tickAnimationBlends();
            tickTalonVisuals();
        } else {
            tickTalons();
        }
    }

    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);
        if (attackTimer != timeToHit || !(this.level() instanceof ServerLevel level)) return;

        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        double reach = 1.2 * this.getScale();
        double x = this.getX() + forward.x * reach;
        double y = this.getY() + this.getBbHeight() * 0.5;
        double z = this.getZ() + forward.z * reach;

        boolean gust = this.getComboAttack() == 3;
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, gust ? 3 : 1, gust ? 0.6 : 0, 0.1, gust ? 0.6 : 0, 0);
        level.sendParticles(gust ? ParticleTypes.CLOUD : ParticleTypes.CRIT, x, y, z, gust ? 5 : 4, 0.5, 0.3, 0.5, gust ? 0.06 : 0.2);

        strikeHeldPrey(level);
    }

    private void strikeHeldPrey(ServerLevel level) {
        LivingEntity prey = getTalonPrey();
        if (prey == null || preyAlly || !this.hasPassenger(prey) || this.lastAttackHitEntities.contains(prey)) return;
        float damage = this.getCombatDamage() / MAX_ATTACKS_IN_COMBO;
        if (!prey.hurt(this.damageSources().mobAttack(this), damage)) return;
        this.onSuccessfulHit(prey);
        level.sendParticles(ParticleTypes.CRIT, prey.getX(), prey.getY() + prey.getBbHeight() * 0.6, prey.getZ(), 6, 0.3, 0.2, 0.3, 0.15);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isSedatedFleeing() || isNapping())) return;
        if (!isTame()) setMad(!isBaby() && target != null && !this.isSitting());
        super.setTarget(target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) this.setSitting(false);
        return super.hurt(damageSource, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide() && this.isSaddled()) this.spawnAtLocation(acceptSaddle());
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CondorEntity other && !other.isTame() && !this.isTame()) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;
        if (isTalonPassenger(passenger)) {
            positionPrey(passenger, function);
            return;
        }

        Matrix4f bones = this.boneMatrix;
        Vector3f seat = bones != null ? bones.transformPosition(new Vector3f(RIDER_FEET_IN_BODY2)) : new Vector3f(REST_RIDER_FEET_MODEL);

        float scale = this.getScale();
        double yaw = Math.toRadians(this.yBodyRot);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double worldX = (seat.x * cos + seat.z * sin) * scale;
        double worldZ = (seat.x * sin - seat.z * cos) * scale;
        double worldY = (MODEL_ORIGIN_Y - seat.y) * scale;

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + worldX, this.getY() + worldY, this.getZ() + worldZ);
    }

    private boolean isTalonPassenger(Entity passenger) {
        return passenger.getId() == getTalonPreyId() || this.getPassengers().indexOf(passenger) > 0;
    }

    private void positionPrey(Entity prey, MoveFunction function) {
        Vec3 grip = talonPoint();
        double y = this.getY() + Condor.TALON_HOLD_GRIP * this.getScale() - prey.getBbHeight();
        double ceiling = this.getY();
        AABB box = prey.getDimensions(prey.getPose()).makeBoundingBox(grip.x, y, grip.z);
        for (int step = 0; step < PREY_LIFT_MAX_STEPS && y < ceiling && !this.level().noBlockCollision(prey, box); step++) {
            double lift = Math.min(PREY_LIFT_STEP, ceiling - y);
            y += lift;
            box = box.move(0, lift, 0);
        }

        prey.fallDistance = 0f;
        function.accept(prey, grip.x, y, grip.z);
        if (prey instanceof LivingEntity living && !(prey instanceof Player)) {
            living.setYRot(this.yBodyRot);
            living.setYBodyRot(this.yBodyRot);
            living.setYHeadRot(this.yBodyRot);
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        LivingEntity controller = super.getControllingPassenger();
        if (controller != null && controller.getId() == getTalonPreyId()) return null;
        return controller;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide() && passenger.getId() == getTalonPreyId()) {
            releasedPreyId = passenger.getId();
            onPreyDetached(passenger);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity living) {
        if (living.getId() == releasedPreyId) {
            releasedPreyId = -1;
            return gentleRelease ? freeSpotFor(living) : living.position();
        }
        return super.getDismountLocationForPassenger(living);
    }

    private Vec3 freeSpotFor(Entity entity) {
        AABB box = entity.getBoundingBox();
        for (int step = 0; step <= PREY_LIFT_MAX_STEPS; step++) {
            double lift = step * PREY_LIFT_STEP;
            if (this.level().noBlockCollision(entity, box.move(0, lift, 0))) return entity.position().add(0, lift, 0);
        }
        return new Vec3(entity.getX(), this.getY(), entity.getZ());
    }

    @Override
    public double flightSpeedFactor() {
        double factor = super.flightSpeedFactor();
        if (getTalonPreyId() >= 0) factor *= Condor.CARRY_SPEED_FACTOR;
        if (isTailwindActive()) factor *= OWPistePassives.TAILWIND_SPEED_FACTOR;
        return factor;
    }

    @Override
    protected double riddenBuoyancy() {
        if (!this.isInWater()) return super.riddenBuoyancy();
        boolean pushing = this.getControllingPassenger() instanceof Player rider && rider.zza > 0;
        if (pushing && this.horizontalCollision && hasRoomToClimbOut()) return WATER_EXIT_LIFT;
        return this.getFluidHeight(FluidTags.WATER) > this.getBbHeight() * WATER_FLOAT_FRACTION ? WATER_FLOAT_SPEED : 0.0;
    }

    private boolean hasRoomToClimbOut() {
        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot()).scale(0.4);
        return this.level().noCollision(this, this.getBoundingBox().move(forward.x, WATER_EXIT_CLEARANCE, forward.z));
    }

    public boolean isTailwindActive() {
        return tailwindTicks >= OWPistePassives.TAILWIND_DELAY_TICKS;
    }

    private void tickTailwind() {
        boolean wasActive = isTailwindActive();
        boolean fast = tailwindUnlocked && this.getFlightState() == FLIGHT_FAST;
        boolean fueled = 1f - this.flightEnergyRatio() > OWPistePassives.TAILWIND_MIN_ENERGY_LEFT;
        if (!fast) tailwindTicks = 0;
        else if (tailwindTicks > 0 || fueled) tailwindTicks++;
        if (!this.level().isClientSide() || !isTailwindActive()) return;
        spawnTailwindParticles(wasActive ? 2 : 14, wasActive ? 0.04 : 0.12);
    }

    private void spawnTailwindParticles(int count, double spread) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double span = this.wingSpan();
        for (int i = 0; i < count; i++) {
            double side = (this.random.nextDouble() * 2.0 - 1.0) * span;
            double back = this.random.nextDouble() * 1.5;
            double x = this.getX() - fz * side - fx * back;
            double y = this.getY() + this.getBbHeight() * (0.3 + this.random.nextDouble() * 0.6);
            double z = this.getZ() + fx * side - fz * back;
            this.level().addParticle(OWParticles.WIND_TRAIL.get(), x, y, z,
                    -fx * spread + (this.random.nextDouble() - 0.5) * spread, (this.random.nextDouble() - 0.5) * spread * 0.5,
                    -fz * spread + (this.random.nextDouble() - 0.5) * spread);
        }
    }

    private double talonSizeFactor() {
        return ironTalonsUnlocked ? OWPistePassives.IRON_TALONS_SIZE_FACTOR : 1.0;
    }

    private double maxPreyVolume() {
        double factor = talonSizeFactor();
        return Condor.PREY_MAX_VOLUME * factor * factor * factor;
    }

    // ==================================================
    //                  SERRES (SECONDAIRE)
    // ==================================================

    public int getTalonPreyId() {
        return this.entityData.get(TALON_PREY);
    }

    public int getTalonPhase() {
        return this.entityData.get(TALON_PHASE);
    }

    public int getTalonCooldownTicks() {
        return this.entityData.get(TALON_COOLDOWN);
    }

    @Nullable
    public LivingEntity getTalonPrey() {
        int id = getTalonPreyId();
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    public boolean isCarryingPrey() {
        LivingEntity prey = getTalonPrey();
        return prey != null && this.hasPassenger(prey);
    }

    public boolean canUseTalons() {
        return getTalonPhase() == TALON_NONE && getTalonCooldownTicks() <= 0 && this.isAirborneFlight();
    }

    private Vec3 talonForward() {
        return Vec3.directionFromRotation(0f, this.yBodyRot);
    }

    private Vec3 talonPoint() {
        return this.position().add(talonForward().scale(Condor.TALON_HOLD_AHEAD * this.getScale()));
    }

    private AABB talonZone() {
        double scale = this.getScale();
        Vec3 center = this.position().add(talonForward().scale(Condor.TALON_REACH_AHEAD * scale));
        double radius = Condor.TALON_REACH_RADIUS * scale;
        return new AABB(center.x - radius, this.getY() - Condor.TALON_REACH_BELOW * scale, center.z - radius,
                center.x + radius, this.getY() + Condor.TALON_REACH_ABOVE * scale, center.z + radius);
    }

    @Nullable
    public LivingEntity findTalonPrey() {
        Vec3 grip = talonPoint();
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, talonZone())) {
            LivingEntity target = talonTargetOf(candidate);
            if (target == null || !isValidTalonPrey(target)) continue;
            double distance = candidate.getBoundingBox().distanceToSqr(grip);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = target;
            }
        }
        return best;
    }

    @Nullable
    private static LivingEntity talonTargetOf(LivingEntity candidate) {
        if (candidate instanceof BoaTailPart part) return boaOf(part);
        return candidate;
    }

    @Nullable
    private static LivingEntity boaOf(BoaTailPart part) {
        Entity parent = part.getParentForRender();
        if (parent == null && !part.level().isClientSide()) parent = part.getParent();
        return parent instanceof LivingEntity living ? living : null;
    }

    private boolean talonZoneReaches(LivingEntity prey, AABB zone) {
        if (zone.intersects(prey.getBoundingBox())) return true;
        for (BoaTailPart part : this.level().getEntitiesOfClass(BoaTailPart.class, zone)) {
            if (boaOf(part) == prey) return true;
        }
        return false;
    }

    public boolean isValidTalonPrey(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity.isRemoved() || entity.isSpectator()) return false;
        if (entity.isPassenger() || entity.isVehicle()) return false;
        if (entity instanceof ArmorStand || entity instanceof OWBossEntity || entity instanceof BoaTailPart) return false;
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();
        double factor = talonSizeFactor();
        if (width > Condor.PREY_MAX_WIDTH * factor || height > Condor.PREY_MAX_HEIGHT * factor) return false;
        if (width * width * height > maxPreyVolume()) return false;
        if (entity instanceof Player) return isTalonAlly(entity);
        return true;
    }

    public boolean isTalonAlly(Entity entity) {
        if (this.isAlliedTo(entity) || this.isTameGrabAlly(entity)) return true;
        LivingEntity rider = this.getControllingPassenger();
        return rider != null && rider != entity && rider.isAlliedTo(entity);
    }

    public void useTalons() {
        if (this.level().isClientSide()) return;
        int phase = getTalonPhase();
        if (phase == TALON_CARRY || (phase == TALON_GRAB && isCarryingPrey())) {
            releasePrey(false);
            return;
        }
        if (phase != TALON_NONE || getTalonCooldownTicks() > 0 || !this.isAirborneFlight()) return;
        if (!(this.getControllingPassenger() instanceof Player) || this.isCombo() || this.isAttackLocked()) return;
        if (this.getVitalEnergy() > this.getVitalEnergyCapacity() - Condor.TALON_ENERGY) {
            this.canShowVitalEnergyLack = true;
            return;
        }
        LivingEntity prey = findTalonPrey();

        this.setVitalEnergy(this.getVitalEnergy() + Condor.TALON_ENERGY);
        this.entityData.set(TALON_PREY, prey != null ? prey.getId() : -1);
        setTalonPhase(TALON_GRAB);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.NEUTRAL, 1.0f, 0.7f);
    }

    private void setTalonPhase(int phase) {
        this.entityData.set(TALON_PHASE, phase);
        talonTicks = 0;
    }

    private void tickTalons() {
        int cooldown = getTalonCooldownTicks();
        if (cooldown > 0) this.entityData.set(TALON_COOLDOWN, cooldown - 1);
        tickSoftLandings();
        ejectStrayPassengers();

        int phase = getTalonPhase();
        if (phase == TALON_DROP) {
            if (++talonTicks >= Condor.TALON_DROP_TICKS) setTalonPhase(TALON_NONE);
        } else if (phase == TALON_GRAB || phase == TALON_CARRY) {
            tickHeldPrey(phase);
        }
        talonPrevPos = this.position();
    }

    private void tickHeldPrey(int phase) {
        LivingEntity prey = getTalonPrey();
        boolean held = prey != null && this.hasPassenger(prey);
        talonTicks++;

        if (!canKeepTalonGrip() || (held && !prey.isAlive())) {
            if (held) releasePrey(!this.isAirborneFlight());
            else abortGrab();
            return;
        }

        if (!held) {
            if (phase != TALON_GRAB) {
                abortGrab();
                return;
            }
            if (talonTicks < Condor.TALON_CATCH_TICK) return;
            LivingEntity target = prey != null && prey.isAlive() && isValidTalonPrey(prey) ? prey : findTalonPrey();
            if (target == null || !catchPrey(target)) abortGrab();
            return;
        }

        if (phase == TALON_GRAB && talonTicks >= Condor.TALON_GRAB_TICKS) setTalonPhase(TALON_CARRY);
        carryTicks++;

        float load = (float) (0.5 + prey.getBbWidth() * prey.getBbWidth() * prey.getBbHeight() / maxPreyVolume());
        this.setVitalEnergy(Math.min(this.getVitalEnergyCapacity(), this.getVitalEnergy() + Condor.CARRY_ENERGY_PER_TICK * load));
        if (isTalonExhausted()) {
            this.canShowVitalEnergyLack = true;
            releasePrey(false);
            return;
        }
        if (!preyAlly && carryTicks >= Condor.ENEMY_MAX_CARRY_TICKS) {
            releasePrey(false);
            return;
        }
        if (!preyAlly && prey instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    private boolean canKeepTalonGrip() {
        return this.isAlive() && !this.isKnockedOut() && !this.isSleeping() && this.isAirborneFlight()
                && this.getControllingPassenger() instanceof Player && !isTalonExhausted();
    }

    private boolean isTalonExhausted() {
        return this.getVitalEnergy() >= this.getVitalEnergyCapacity() - Condor.EXHAUSTED_MARGIN;
    }

    private boolean catchPrey(LivingEntity prey) {
        if (!isValidTalonPrey(prey)) return false;
        if (!talonZoneReaches(prey, talonZone().inflate(Condor.TALON_CATCH_TOLERANCE * this.getScale()))) return false;
        boolean ally = isTalonAlly(prey);
        this.entityData.set(TALON_PREY, prey.getId());
        if (!prey.startRiding(this, true)) return false;

        preyAlly = ally;
        carryTicks = 0;
        if (prey instanceof Mob mob) {
            mob.getNavigation().stop();
            if (!ally) mob.setTarget(null);
        }
        this.level().playSound(null, prey.getX(), prey.getY(), prey.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 1.0f, 0.75f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_AMBIENT, this.getSoundSource(), 2.0f, this.getVoicePitch());
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, prey.getX(), prey.getY() + prey.getBbHeight(), prey.getZ(), 6, 0.4, 0.1, 0.4, 0.03);
        }
        return true;
    }

    private void abortGrab() {
        this.entityData.set(TALON_PREY, -1);
        setTalonPhase(TALON_NONE);
        this.entityData.set(TALON_COOLDOWN, Condor.TALON_MISS_COOLDOWN_TICKS);
    }

    private void releasePrey(boolean gentle) {
        LivingEntity prey = getTalonPrey();
        if (prey == null || !this.hasPassenger(prey)) {
            onPreyDetached(null);
            return;
        }
        Vec3 velocity = gentle ? Vec3.ZERO : carrierVelocity();
        gentleRelease = gentle;
        prey.stopRiding();
        gentleRelease = false;
        prey.setDeltaMovement(velocity);
        prey.hasImpulse = true;
        prey.hurtMarked = true;
        if (gentle) softLandings.put(prey, 0);
    }

    private void onPreyDetached(@Nullable Entity prey) {
        preyAlly = false;
        carryTicks = 0;
        this.entityData.set(TALON_PREY, -1);
        setTalonPhase(TALON_DROP);
        this.entityData.set(TALON_COOLDOWN, Condor.TALON_COOLDOWN_TICKS);
        if (prey != null) {
            this.level().playSound(null, prey.getX(), prey.getY(), prey.getZ(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.NEUTRAL, 1.0f, 0.8f + this.random.nextFloat() * 0.1f);
        }
        SoundEvent flap = wingFlapSound();
        if (flap != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), flap, SoundSource.NEUTRAL, 0.9f, 1.1f);
    }

    private Vec3 carrierVelocity() {
        if (talonPrevPos == null) return Vec3.ZERO;
        Vec3 velocity = this.position().subtract(talonPrevPos);
        double speed = velocity.length();
        return speed > RELEASE_MAX_SPEED ? velocity.scale(RELEASE_MAX_SPEED / speed) : velocity;
    }

    private void tickSoftLandings() {
        Iterator<Map.Entry<Entity, Integer>> it = softLandings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Integer> entry = it.next();
            Entity entity = entry.getKey();
            int age = entry.getValue() + 1;
            if (entity.isRemoved() || entity.isPassenger() || age > Condor.SOFT_LANDING_MAX_TICKS
                    || (age > 2 && (entity.onGround() || entity.isInWater()))) {
                it.remove();
                continue;
            }
            entity.fallDistance = 0f;
            entry.setValue(age);
        }
    }

    private void ejectStrayPassengers() {
        List<Entity> passengers = this.getPassengers();
        int preyId = getTalonPreyId();
        for (int i = passengers.size() - 1; i >= 0; i--) {
            Entity passenger = passengers.get(i);
            if (passenger.getId() == preyId) continue;
            if (i == 0 && passenger instanceof Player) continue;
            passenger.stopRiding();
        }
    }

    private void tickTalonVisuals() {
        int phase = getTalonPhase();
        if (phase != clientTalonPhase) {
            if (phase == TALON_GRAB) grabStartTick = this.tickCount;
            if (phase == TALON_DROP) dropStartTick = this.tickCount;
            clientTalonPhase = phase;
        }
        boolean carrying = phase == TALON_CARRY
                || (phase == TALON_GRAB && isCarryingPrey());
        carryBlendO = carryBlend;
        carryBlend = approach(carryBlend, carrying ? 1f : 0f, CARRY_BLEND_RATE);
    }

    public float talonGrabSeconds(float partialTick) {
        return (this.tickCount - grabStartTick + partialTick) / 20f;
    }

    public float talonDropSeconds(float partialTick) {
        return (this.tickCount - dropStartTick + partialTick) / 20f;
    }

    public float carryBlend(float partialTick) {
        return Mth.lerp(partialTick, carryBlendO, carryBlend);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, BASE_HEALTH, BASE_DAMAGE, BASE_SPEED);
            this.setBaseHealth((float) BASE_HEALTH * 1.3f);
            this.setBaseDamage((float) BASE_DAMAGE);
            this.setBaseSpeed((float) BASE_SPEED);
            this.setHealth(this.getMaxHealth());

            this.setVariant(chooseCondorVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return data;
    }

    public static boolean checkCondorSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (!ground.isFaceSturdy(level, below, Direction.UP) || !ground.getFluidState().isEmpty()) return false;
        return isBrightEnoughToSpawn(level, pos) && level.canSeeSky(pos);
    }

    private CondorVariant chooseCondorVariant() {
        return this.random.nextInt(100) < GREY_VARIANT_CHANCE ? CondorVariant.GREY : CondorVariant.DEFAULT;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(true);
        createSitAnimation(true);

        handleIdleGestures();
        setupComboAnimations();
    }

    private void handleIdleGestures() {
        if (idleGestureTicks > 0 && --idleGestureTicks <= 0) stopIdleGestures();
        if (!canPlayIdleGesture()) {
            if (idleGestureTicks > 0) {
                idleGestureTicks = 0;
                stopIdleGestures();
            }
            return;
        }
        if (idleGestureTicks > 0 || --idleGestureCooldown > 0) return;

        idleGestureCooldown = IDLE_GESTURE_MIN_DELAY + this.random.nextInt(IDLE_GESTURE_MAX_DELAY - IDLE_GESTURE_MIN_DELAY);
        switch (this.random.nextInt(4)) {
            case 0 -> {
                this.sunbatheAnimationState.start(this.tickCount);
                idleGestureTicks = 120;
            }
            case 1 -> {
                this.preenAnimationState.start(this.tickCount);
                idleGestureTicks = 80;
            }
            case 2 -> {
                this.shakeAnimationState.start(this.tickCount);
                idleGestureTicks = 40;
            }
            default -> {
                this.displayAnimationState.start(this.tickCount);
                idleGestureTicks = 80;
            }
        }
    }

    private void stopIdleGestures() {
        this.sunbatheAnimationState.stop();
        this.preenAnimationState.stop();
        this.shakeAnimationState.stop();
        this.displayAnimationState.stop();
    }

    public boolean canPlayIdleGesture() {
        return this.getTarget() == null && !this.isNapping() && !this.isSitting() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isCombo() && !isKnockedOut() && !isAirborneFlight();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (29 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (28 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (35 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    private void tickAnimationBlends() {
        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() || this.getState() == 2 ? 1f : 0f, RUN_BLEND_RATE);

        sitBlendO = sitBlend;
        sitBlend = approach(sitBlend, this.isSitting() && !this.isAirborneFlight() ? 1f : 0f, SIT_BLEND_RATE);

        restBlendO = restBlend;
        restBlend = approach(restBlend, this.isNapping() ? 1f : 0f, REST_BLEND_RATE);
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    public float runBlend(float partialTick) { return Mth.lerp(partialTick, runBlendO, runBlend); }

    public float sitBlend(float partialTick) { return Mth.lerp(partialTick, sitBlendO, sitBlend); }

    public float restBlend(float partialTick) { return Mth.lerp(partialTick, restBlendO, restBlend); }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof CondorEntity condor) {
            condor.setVariant(CondorVariant.byId(variant));
            condor.setInitialVariant(CondorVariant.byId(variant));
        }
    }

    public CondorVariant getVariant() {
        return CondorVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(CondorVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(CondorVariant skin) {
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

    public CondorVariant getInitialVariant() {
        return CondorVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(CondorVariant variant) {
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(22, 6.0f, 1.5f, 1.0f)
    );

    @Override
    public List<OWSedationImpact> sedationImpacts() {
        return SEDATION_IMPACTS;
    }

    @Override
    protected int experienceRewardMin() {
        return 3;
    }

    @Override
    protected int experienceRewardMax() {
        return 6;
    }

    class CondorMeleeAttackGoal extends MeleeAttackGoal {

        public CondorMeleeAttackGoal() {
            super(CondorEntity.this, 1.2, true);
        }

        @Override
        public boolean canUse() {
            if (CondorEntity.this.isSedatedFleeing()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (CondorEntity.this.isSedatedFleeing()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            CondorEntity.this.setMad(true);
            CondorEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            CondorEntity.this.setMad(false);
            CondorEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2.4 * CondorEntity.this.getScale();
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;

            if (!CondorEntity.this.isCombo()) {
                CondorEntity.this.setCombo(true, 1);
            } else if (CondorEntity.this.isPauseCombo()) {
                CondorEntity.this.playerContinueCombo = true;
            }
        }
    }

    class CondorGroundStrollGoal extends RandomStrollGoal {

        public CondorGroundStrollGoal() {
            super(CondorEntity.this, 0.8D);
        }

        @Override
        public boolean canUse() {
            return !CondorEntity.this.isAirborneFlight() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !CondorEntity.this.isAirborneFlight() && super.canContinueToUse();
        }
    }
}
