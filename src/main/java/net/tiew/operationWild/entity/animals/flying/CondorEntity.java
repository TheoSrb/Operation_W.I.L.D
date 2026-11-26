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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;
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

import java.util.List;

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
    private static final double RIDER_SEAT_DROP = 0.75;
    private static final Vector3f SEAT_IN_BODY = new Vector3f(0f, -12f / 16f, -6f / 16f);
    private static final Vector3f REST_SEAT_MODEL = new Vector3f(0f, -4f / 16f, -5f / 16f);
    private static final double MODEL_ORIGIN_Y = 1.501;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(CondorEntity.class, EntityDataSerializers.BOOLEAN);

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

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private long lastStepSoundMs = 0L;

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
    public double vehicleDiveSpeed() {
        return 1.15;
    }

    @Override
    public double vehicleGlideEntrySpeed() {
        return 0.8;
    }

    @Override
    public double vehicleGlideExitSpeed() {
        return 0.5;
    }

    @Override
    public double vehicleGlideDiveGain() {
        return 0.035;
    }

    @Override
    public double vehicleGlideDrag() {
        return 0.006;
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
        return 3.2f;
    }

    @Override
    public float takeoffEnergyCost() {
        return 12f;
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

        if (this.level().isClientSide()) tickAnimationBlends();
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

        Matrix4f bones = this.boneMatrix;
        Vector3f seat = bones != null ? bones.transformPosition(new Vector3f(SEAT_IN_BODY)) : new Vector3f(REST_SEAT_MODEL);

        float scale = this.getScale();
        double yaw = Math.toRadians(this.yBodyRot);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double worldX = (seat.x * cos + seat.z * sin) * scale;
        double worldZ = (seat.x * sin - seat.z * cos) * scale;
        double worldY = (MODEL_ORIGIN_Y - seat.y) * scale - RIDER_SEAT_DROP;

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + worldX, this.getY() + worldY, this.getZ() + worldZ);
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
