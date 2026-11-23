package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.OWGlideProfile;
import net.tiew.operationWild.entity.misc.PeacockFeatherEntity;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.PeacockVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PeacockEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 60.0;
    public static final int MAX_SOMNOLENCE = 500;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 2;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 4;
    public static final int FOOD_WANTED_MIN = 6;
    public static final int FOOD_WANTED_MAX = 10;
    public static final int ENTITY_COLOR = 0x286EC3;
    public static final int DEFAULT_SKIN_INDEX = 1;

    public static final int LINK_NONE = 0;
    public static final int LINK_SELECTING = 1;
    public static final int LINK_ACTIVE = 2;

    public static final double BASE_HEALTH = 18.0;
    public static final double BASE_DAMAGE = 4.0;
    public static final double BASE_SPEED = 0.19;

    private static final float MAX_VITAL_ENERGY = 320f;
    private static final double RIDER_HEIGHT_FACTOR = 0.4;
    private static final int SEDATION_WAKE_TICKS = 45;
    private static final float RUN_BLEND_RATE = 0.2f;
    private static final float SIT_BLEND_RATE = 0.1f;
    private static final float REST_BLEND_RATE = 0.07f;
    private static final float LINK_BLEND_IN_RATE = 1f / OWAttacksConstants.Peacock.LINK_START_TICKS;
    private static final float LINK_BLEND_OUT_RATE = 1f / OWAttacksConstants.Peacock.LINK_END_TICKS;
    private static final int IDLE_GESTURE_MIN_DELAY = 300;
    private static final int IDLE_GESTURE_MAX_DELAY = 700;
    private static final DustParticleOptions LINK_DUST = new DustParticleOptions(new Vector3f(0.36f, 1f, 0.54f), 1.1f);
    private static final DustParticleOptions FEATHER_PUFF = new DustParticleOptions(new Vector3f(0.12f, 0.62f, 0.66f), 0.7f);
    private static final DustParticleOptions DONOR_DUST = new DustParticleOptions(new Vector3f(0.85f, 0.2f, 0.25f), 0.8f);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_PHASE = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_DONOR = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_RECEIVER = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEATHER_STOCK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEATHER_AIM_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEATHER_SHOT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWEEP_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWEEP_LINK_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> SWEEP_LINKS = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FAN_PHASE = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STORM_CALL = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);

    public static final int FAN_NONE = 0;
    public static final int FAN_OPEN = 1;
    public static final int FAN_SHAKE = 2;
    public static final int FAN_DANCE = 3;
    public static final int FAN_SHOW = 4;
    public static final int FAN_CLOSE = 5;

    public static final int FAN_OPEN_TICKS = 45;
    public static final int FAN_SHAKE_TICKS = 40;
    public static final int FAN_DANCE_LOOP_TICKS = 70;
    public static final int FAN_DANCE_TICKS = FAN_DANCE_LOOP_TICKS * 2;
    public static final int FAN_DANCE_VOLTE_TICK = 40;
    public static final int FAN_CLOSE_TICKS = 30;
    public static final int FAN_FEAR_DELAY_TICKS = 4;
    public static final double FAN_FEAR_RADIUS = 10.0;
    private static final float COURT_COUNTER_CHANCE = 0.6f;
    private static final int STORM_CHECK_INTERVAL = 40;
    private static final int STORM_FORECAST_TICKS = 2400;
    private static final int STORM_CALL_MIN_DELAY = 160;
    private static final int STORM_CALL_MAX_DELAY = 420;
    private static final float STORM_CALL_VOLUME = 3.0f;
    public static final int FAN_FEAR_DURATION_TICKS = 100;
    public static final int FAN_SLOW_DURATION_TICKS = 60;
    private static final int FAN_AGGRESSOR_MEMORY_TICKS = 100;
    private static final double FEATHER_LEAD_FACTOR = 0.8;

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState featherShotRightState = new AnimationState();
    public final AnimationState featherShotLeftState = new AnimationState();
    public final AnimationState sweepAnimationState = new AnimationState();
    public final AnimationState idleGestureAnimationState = new AnimationState();
    public final AnimationState peckAnimationState = new AnimationState();
    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState fanPhaseState = new AnimationState();
    public int fanPrevPhase = FAN_NONE;
    public long fanPrevMs = 0L;
    public float fanPrevMove = 0f;
    public float fanShowMove = 0f;
    private int clientFanPhase = FAN_NONE;
    private int clientStormCall = 0;

    private int fanPhaseTicks = 0;
    private int fanPhaseElapsed = 0;
    private boolean fanCourting = false;
    private boolean fanStorm = false;
    private int fanShowTicks = 0;
    private int fanFocusId = -1;
    private boolean stormIncoming = false;
    private int stormCallTimer = 0;

    private int idleGestureCooldown = IDLE_GESTURE_MIN_DELAY;
    private int idleGestureTicks = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private int linkSelectionTimer = 0;
    private int linkTransferTimer = 0;
    private boolean linkCredited = false;
    private int linkDurationTimer = 0;
    private boolean linkAuto = false;
    private float sweepDamagePool = 0f;
    private final Set<Integer> sweepHits = new HashSet<>();

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private float linkBlend, linkBlendO;
    private float aimBlend, aimBlendO;

    private int featherRegenTimer = 0;
    private int featherCadenceTimer = 0;
    private int featherReleaseTimer = 0;
    private boolean featherReleaseRight = true;
    private boolean featherCharging = false;
    private int featherChargeTick = 0;
    private int featherLastShotTick = Integer.MIN_VALUE / 2;
    private int featherReleaseTargetId = -1;
    private int featherAutoShotTick = Integer.MIN_VALUE / 2;
    private boolean featherReleaseHostile = false;
    private int clientFeatherRegen = 0;
    private int featherBurstLeft = 0;
    private float featherBurstClock = 0f;
    private final java.util.ArrayDeque<int[]> featherPendingReleases = new java.util.ArrayDeque<>();
    private int clientLastStock = -1;
    private int clientSeenShot = 0;
    private int clientShotTicks = 0;
    private int clientRightShotTicks = 0;
    private int clientLeftShotTicks = 0;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public PeacockEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.peacock.PeacockAutoHealGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.peacock.PeacockFanDisplayGoal(this));
        this.goalSelector.addGoal(2, new net.tiew.operationWild.entity.goals.peacock.PeacockWildFeatherGoal(this));
        this.goalSelector.addGoal(3, new PeacockMeleeAttackGoal());
        this.goalSelector.addGoal(4, new net.tiew.operationWild.entity.goals.peacock.PeacockCourtshipGoal(this));
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
        builder.define(LINK_PHASE, LINK_NONE);
        builder.define(LINK_DONOR, -1);
        builder.define(LINK_RECEIVER, -1);
        builder.define(FEATHER_STOCK, OWAttacksConstants.Peacock.FEATHER_MAX_STOCK);
        builder.define(FEATHER_AIM_TICK, 0);
        builder.define(FEATHER_SHOT, 0);
        builder.define(SWEEP_TICK, 0);
        builder.define(SWEEP_LINK_TICK, 0);
        builder.define(SWEEP_LINKS, "");
        builder.define(FAN_PHASE, FAN_NONE);
        builder.define(STORM_CALL, 0);
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
        return 5f;
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
    public boolean hasStandardHeadPose() {
        return !isLinking() && !isFeatherAiming() && !isSweeping() && !isGliding() && !isFanDisplaying();
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
        return 4.5f;
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
    public void onSedationKnockOut() {
        breakLink(false);
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.HEALER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.OMNIVOROUS;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.NEUTRAL;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.6f;
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
        return OWAdvancements.PEACOCK_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return MAX_VITAL_ENERGY * (1f + this.getLevel() / 100f);
    }

    @Override
    protected boolean pausesEnergyRecovery() {
        return isFeatherAiming();
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.1f * (1 + ((float) this.getLevel() / 50));
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
        if (isSweeping()) return 0f;
        return isFeatherAiming() ? 0.5f : 0.25f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.PEACOCK.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.PEACOCK_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getSecondaryAttackCount() {
        return 2;
    }

    @Override
    public int getSecondaryCooldownDuration() {
        return OWAttacksConstants.Peacock.LINK_COOLDOWN_TICKS;
    }

    @Override
    protected void onSecondaryAttackChanged() {
        if (!this.level().isClientSide()) breakLink(true);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return OWSounds.PEACOCK_IDLE.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.PEACOCK_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.PEACOCK_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
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
        if (now - lastStepSoundMs < 150L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 3; i++) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundtype.getStepSound(), this.getSoundSource(),
                    soundtype.getVolume() * 0.12F, soundtype.getPitch() * pitchMod, false);
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(1.25f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.4f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(18, 12, SoundEvents.PLAYER_ATTACK_SWEEP, 2.5, 2.5, 1.2, actualAttackNumber == 2, actualAttackNumber == 2 ? 1.5f : 0);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isSweeping() || this.isFeatherAiming());
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.level() instanceof ServerLevel serverLevel) {
            tickLink(serverLevel);
            tickFeathers(serverLevel);
            tickSweep(serverLevel);
            tickFanDisplay(serverLevel);
            tickStormSense(serverLevel);
        } else {
            tickClientEffects();
        }
    }

    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);
        if (attackTimer != timeToHit || !(this.level() instanceof ServerLevel level)) return;

        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        double reach = 0.9 * this.getScale();
        double x = this.getX() + forward.x * reach;
        double y = this.getY() + this.getBbHeight() * 0.45;
        double z = this.getZ() + forward.z * reach;

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CRIT, x, y, z, this.getComboAttack() == 3 ? 10 : 4, 0.25, 0.2, 0.25, 0.25);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isSedatedFleeing() || isNapping())) return;
        if (!isTame()) setMad(!isBaby() && target != null && !this.isSitting());
        super.setTarget(target);
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isSweeping()) return 0;
        float speed = super.getRiddenSpeedVehicle(player);
        return isFeatherAiming() ? speed * OWAttacksConstants.Peacock.FEATHER_AIM_SPEED_FACTOR : speed;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) this.setSitting(false);
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide() && isLinking() && amount >= OWAttacksConstants.Peacock.LINK_BREAK_DAMAGE) {
            breakLink(true);
        }
        if (hurt && !this.level().isClientSide() && isCourtingPhase() && damageSource.getEntity() instanceof LivingEntity attacker) {
            if (this.random.nextFloat() < COURT_COUNTER_CHANCE) startFanDisplay(attacker);
            else closeFan();
        }
        return hurt;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        creditUltimateCharge();
        return super.killedEntity(level, target);
    }

    private void creditUltimateCharge() {
        int charge = getUltimateKillCount();
        if (charge < OWAttacksConstants.Peacock.SWEEP_KILLS_REQUIRED) setUltimateKillCount(charge + 1);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            breakLink(false);
            this.entityData.set(FEATHER_AIM_TICK, 0);
            featherReleaseTimer = 0;
            featherReleaseTargetId = -1;
            featherBurstLeft = 0;
            featherPendingReleases.clear();
            featherReleaseHostile = false;
            this.entityData.set(SWEEP_TICK, 0);
            setFanPhase(FAN_NONE, 0);
        }
        super.die(damageSource);
        if (!this.level().isClientSide() && this.isSaddled()) this.spawnAtLocation(acceptSaddle());
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isSweeping() || isFanLocked();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isSweeping() || isFanDisplaying();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isLinking() && !isSweeping() && !isFanDisplaying();
    }

    @Override
    public @Nullable OWGlideProfile glideProfile() {
        return OWGlideProfile.DEFAULT;
    }

    @Override
    public boolean canGlideNow() {
        return super.canGlideNow() && !isSweeping();
    }

    @Override
    public @Nullable SoundEvent glideFlapSound() {
        return OWSounds.PEACOCK_WING_GUST.get();
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof PeacockEntity other && !other.isTame() && !this.isTame()) return true;
        return super.isAlliedTo(entity);
    }

    public boolean isLinkCandidate(@Nullable Entity entity) {
        if (!(entity instanceof OWEntity candidate) || candidate == this || !candidate.isAlive() || !candidate.isTame()) return false;
        if (this.isAlliedTo(candidate)) return true;
        if (!this.level().isClientSide()) return false;
        net.tiew.operationWild.team.OWTeam tribe = net.tiew.operationWild.client.OWClientTribeData.get();
        return tribe != null && this.getOwnerUUID() != null && candidate.getOwnerUUID() != null
                && tribe.isMember(this.getOwnerUUID()) && tribe.isMember(candidate.getOwnerUUID());
    }

    public static boolean canDonate(@Nullable LivingEntity entity) {
        return entity != null && entity.getHealth() > entity.getMaxHealth() * OWAttacksConstants.Peacock.LINK_DONOR_FLOOR + 0.05f;
    }

    public @Nullable OWEntity pickLinkTarget(LivingEntity aimer) {
        double range = OWAttacksConstants.Peacock.LINK_AIM_RANGE;
        double tolerance = OWAttacksConstants.Peacock.LINK_AIM_TOLERANCE;
        double minDistance = OWAttacksConstants.Peacock.LINK_MIN_AIM_DISTANCE;

        Vec3 eye = aimer.getEyePosition();
        Vec3 look = aimer.getLookAngle().normalize();
        OWEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (OWEntity candidate : this.level().getEntitiesOfClass(OWEntity.class, aimer.getBoundingBox().inflate(range))) {
            if (!isLinkCandidate(candidate)) continue;

            Vec3 center = candidate.position().add(0, candidate.getBbHeight() * 0.5, 0);
            Vec3 delta = center.subtract(eye);
            double along = delta.dot(look);
            if (along < minDistance || along > range) continue;

            double offset = delta.subtract(look.scale(along)).length() - candidate.getBbWidth() * 0.5;
            if (offset > tolerance) continue;

            double score = offset * 4 + along * 0.05;
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    public void selectLinkTarget(int targetId) {
        if (this.level().isClientSide()) return;
        Entity picked = this.level().getEntity(targetId);
        Entity rider = this.getControllingPassenger();
        if (!isLinkCandidate(picked) || rider == null
                || rider.distanceTo(picked) > OWAttacksConstants.Peacock.LINK_AIM_RANGE + 4.0) {
            rejectAttack(OWAttackIds.LIFE_LINK);
            return;
        }
        OWEntity target = (OWEntity) picked;

        switch (getLinkPhase()) {
            case LINK_NONE -> {
                if (isSecondaryOnCooldown() || isSweeping() || isCombo() || !canDonate(target)) {
                    rejectAttack(OWAttackIds.LIFE_LINK);
                    return;
                }
                this.entityData.set(LINK_DONOR, target.getId());
                this.entityData.set(LINK_PHASE, LINK_SELECTING);
                linkSelectionTimer = OWAttacksConstants.Peacock.LINK_SELECTION_TIMEOUT_TICKS;
                playSoundAt(target, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.4f);
            }
            case LINK_SELECTING -> {
                if (target.getId() == getLinkDonorId()) {
                    breakLink(false);
                    return;
                }
                if (getLinkDonor() == null) {
                    rejectAttack(OWAttackIds.LIFE_LINK);
                    breakLink(false);
                    return;
                }
                float cost = OWAttacksConstants.Peacock.LINK_START_ENERGY;
                if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
                    canShowVitalEnergyLack = true;
                    rejectAttack(OWAttackIds.LIFE_LINK);
                    return;
                }
                setVitalEnergy(getVitalEnergy() + cost);
                this.entityData.set(LINK_RECEIVER, target.getId());
                this.entityData.set(LINK_PHASE, LINK_ACTIVE);
                linkTransferTimer = 0;
                linkCredited = false;
                linkDurationTimer = OWAttacksConstants.Peacock.LINK_DURATION_TICKS;
                this.getNavigation().stop();
                playSoundAt(this, SoundEvents.BEACON_ACTIVATE, 0.8f, 1.6f);
                playSoundAt(target, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
            }
            default -> { }
        }
    }

    public boolean startAutoLink(OWEntity donor, OWEntity receiver) {
        if (this.level().isClientSide()) return false;
        if (getLinkPhase() != LINK_NONE || donor == receiver) return false;
        if (!isLinkCandidate(donor) || !isLinkCandidate(receiver) || !canDonate(donor)) return false;
        if (isSecondaryOnCooldown() || isSweeping() || isCombo() || isFeatherAiming()) return false;
        float cost = OWAttacksConstants.Peacock.LINK_START_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) return false;

        setVitalEnergy(getVitalEnergy() + cost);
        this.entityData.set(LINK_DONOR, donor.getId());
        this.entityData.set(LINK_RECEIVER, receiver.getId());
        this.entityData.set(LINK_PHASE, LINK_ACTIVE);
        linkAuto = true;
        linkSelectionTimer = 0;
        linkTransferTimer = 0;
        linkCredited = false;
        linkDurationTimer = OWAttacksConstants.Peacock.LINK_DURATION_TICKS;
        playSoundAt(donor, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.4f);
        playSoundAt(this, SoundEvents.BEACON_ACTIVATE, 0.8f, 1.6f);
        playSoundAt(receiver, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
        return true;
    }

    public boolean isAutoLinking() {
        return linkAuto && getLinkPhase() == LINK_ACTIVE;
    }

    public void breakLinkByRider() {
        if (getLinkPhase() == LINK_NONE) return;
        breakLink(true);
    }

    private void breakLink(boolean withCooldown) {
        if (this.level().isClientSide()) return;
        boolean wasActive = getLinkPhase() == LINK_ACTIVE;
        linkAuto = false;
        this.entityData.set(LINK_PHASE, LINK_NONE);
        this.entityData.set(LINK_DONOR, -1);
        this.entityData.set(LINK_RECEIVER, -1);
        linkSelectionTimer = 0;
        linkTransferTimer = 0;
        if (wasActive) {
            if (withCooldown) startSecondaryCooldown();
            playSoundAt(this, SoundEvents.AMETHYST_BLOCK_BREAK, 0.8f, 1.3f);
        }
    }

    private void tickLink(ServerLevel level) {
        int phase = getLinkPhase();
        if (phase == LINK_NONE) return;

        boolean lostControl = linkAuto
                ? this.getControllingPassenger() != null || !canAutoHealNow()
                : this.getControllingPassenger() == null;
        if (lostControl || isKnockedOut() || isSleeping() || !this.isAlive()) {
            breakLink(phase == LINK_ACTIVE);
            return;
        }

        if (phase == LINK_SELECTING) {
            if (--linkSelectionTimer <= 0 || !isLinkCandidate(getLinkDonor())) breakLink(false);
            return;
        }

        LivingEntity donor = getLinkDonor();
        LivingEntity receiver = getLinkReceiver();
        double maxDistance = OWAttacksConstants.Peacock.LINK_MAX_DISTANCE;
        if (!isLinkCandidate(donor) || !isLinkCandidate(receiver)
                || this.distanceTo(donor) > maxDistance || this.distanceTo(receiver) > maxDistance) {
            breakLink(true);
            return;
        }

        if (--linkDurationTimer <= 0) {
            breakLink(true);
            return;
        }

        if (++linkTransferTimer < OWAttacksConstants.Peacock.LINK_TRANSFER_INTERVAL) return;
        linkTransferTimer = 0;

        float floor = donor.getMaxHealth() * OWAttacksConstants.Peacock.LINK_DONOR_FLOOR;
        float available = donor.getHealth() - floor;
        float missing = receiver.getMaxHealth() - receiver.getHealth();
        if (available <= 0.05f || missing <= 0.05f) {
            if (linkAuto) breakLink(true);
            return;
        }

        boolean gentle = OWPistePassives.has(this, OWPistePassives.GENTLE_BOND);
        float donorFactor = gentle ? OWPistePassives.GENTLE_BOND_DONOR_FACTOR : 1f;
        float amplification = OWAttacksConstants.Peacock.LINK_AMPLIFICATION
                * (gentle ? OWPistePassives.GENTLE_BOND_HEAL_FACTOR : 1f);
        float drawn = Math.min(donor.getMaxHealth() * OWAttacksConstants.Peacock.LINK_TRANSFER_RATIO, available / donorFactor);
        float given = Math.min(drawn * amplification, missing);
        float taken = given / amplification * donorFactor;

        donor.setHealth(donor.getHealth() - taken);
        receiver.heal(given);
        if (!linkCredited) {
            linkCredited = true;
            creditUltimateCharge();
        }

        Vec3 donorCenter = donor.position().add(0, donor.getBbHeight() * 0.6, 0);
        Vec3 receiverCenter = receiver.position().add(0, receiver.getBbHeight() * 0.6, 0);
        level.sendParticles(DONOR_DUST, donorCenter.x, donorCenter.y, donorCenter.z, 6, 0.3, 0.3, 0.3, 0.02);
        level.sendParticles(ParticleTypes.HEART, receiverCenter.x, receiverCenter.y + 0.4, receiverCenter.z, 1, 0.3, 0.2, 0.3, 0.0);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, receiverCenter.x, receiverCenter.y, receiverCenter.z, 5, 0.4, 0.4, 0.4, 0.0);
        if (this.tickCount % 20 < OWAttacksConstants.Peacock.LINK_TRANSFER_INTERVAL) {
            playSoundAt(receiver, SoundEvents.AMETHYST_CLUSTER_STEP, 0.7f, 1.6f + this.random.nextFloat() * 0.3f);
        }
    }

    public boolean isFeatherAlly(@Nullable LivingEntity target) {
        if (target == null || target == this) return false;
        if (this.isAlliedTo(target)) return true;
        if (!this.isTame() && target instanceof PeacockEntity other && !other.isTame()) return true;
        if (target instanceof OWEntity other && this.getOwnerUUID() != null && this.getOwnerUUID().equals(other.getOwnerUUID())) return true;
        return target instanceof Player player && this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID());
    }

    public boolean canShootFeather() {
        return getFeatherStock() > 0 && !isGliding() && !isSweeping() && !isAttackLocked()
                && !isKnockedOut() && !isSitting();
    }

    public boolean autoShootFeatherAt(LivingEntity target) {
        if (this.level().isClientSide() || target == null || !target.isAlive()) return false;
        if (!canShootFeather() || isLinking() || featherCadenceTimer > 0 || featherReleaseTimer > 0) return false;
        float cost = OWAttacksConstants.Peacock.FEATHER_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) return false;

        setVitalEnergy(getVitalEnergy() + cost);
        queueAutoFeather(target, false);
        return true;
    }

    public boolean wildShootFeatherAt(LivingEntity target) {
        if (this.level().isClientSide() || this.isTame() || target == null || !target.isAlive()) return false;
        if (this.getControllingPassenger() != null) return false;
        if (!canShootFeather() || featherCadenceTimer > 0 || featherReleaseTimer > 0) return false;
        interruptComboForFeathers();
        queueAutoFeather(target, true);
        return true;
    }

    public boolean isFeatherReady() {
        return featherCadenceTimer <= 0 && featherReleaseTimer <= 0;
    }

    private void queueAutoFeather(LivingEntity target, boolean hostile) {
        featherLastShotTick = this.tickCount;
        featherAutoShotTick = this.tickCount;

        int shot = getFeatherShot() + 1;
        this.entityData.set(FEATHER_SHOT, shot);
        this.entityData.set(FEATHER_STOCK, getFeatherStock() - 1);
        this.entityData.set(FEATHER_AIM_TICK, OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS);
        featherCadenceTimer = OWAttacksConstants.Peacock.FEATHER_CADENCE_TICKS;
        featherReleaseTimer = OWAttacksConstants.Peacock.FEATHER_RELEASE_TICKS;
        featherReleaseRight = isRightWingShot(shot);
        featherReleaseTargetId = target.getId();
        featherReleaseHostile = hostile;
        playSoundAt(this, SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 0.6f, 1.6f + this.random.nextFloat() * 0.2f);
    }

    private boolean isAutoVolleyActive() {
        return this.tickCount - featherAutoShotTick <= OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS;
    }

    private void interruptComboForFeathers() {
        if (!isCombo()) return;
        resetCombo(0);
        actualAttackNumber = 0;
    }

    public boolean isFeatherVolleyActive() {
        return this.tickCount - featherLastShotTick <= OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS;
    }

    public void startFeatherCharge() {
        if (this.level().isClientSide()) return;
        if (!canShootFeather() || !(this.getControllingPassenger() instanceof Player)) return;
        interruptComboForFeathers();
        featherCharging = true;
        featherChargeTick = this.tickCount;
        this.entityData.set(FEATHER_AIM_TICK, OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS);
        playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 0.5f, 1.4f);
    }

    public void cancelFeatherCharge() {
        if (this.level().isClientSide() || !featherCharging) return;
        featherCharging = false;
        if (!isFeatherVolleyActive()) this.entityData.set(FEATHER_AIM_TICK, 0);
    }

    public boolean shootFeather() {
        if (this.level().isClientSide()) return false;
        if (!canShootFeather() || !(this.getControllingPassenger() instanceof Player)) return false;
        if (featherCadenceTimer > OWAttacksConstants.Peacock.FEATHER_CADENCE_SLACK_TICKS) return false;
        if (featherBurstLeft > 0) return false;
        boolean charged = featherCharging && this.tickCount - featherChargeTick
                >= OWAttacksConstants.Peacock.FEATHER_MIN_CHARGE_TICKS - OWAttacksConstants.Peacock.FEATHER_CHARGE_SLACK_TICKS;
        if (!isFeatherVolleyActive() && !charged) return false;
        float cost = OWAttacksConstants.Peacock.FEATHER_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        interruptComboForFeathers();
        featherCharging = false;
        if (hasFeatherTempest()) {
            featherBurstLeft = getFeatherStock();
            featherBurstClock = 0f;
            fireBurstFeather();
            return true;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        featherLastShotTick = this.tickCount;

        int shot = getFeatherShot() + 1;
        this.entityData.set(FEATHER_SHOT, shot);
        this.entityData.set(FEATHER_STOCK, getFeatherStock() - 1);
        this.entityData.set(FEATHER_AIM_TICK, OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS);
        featherCadenceTimer = OWAttacksConstants.Peacock.FEATHER_CADENCE_TICKS;
        featherReleaseTimer = OWAttacksConstants.Peacock.FEATHER_RELEASE_TICKS;
        featherReleaseRight = isRightWingShot(shot);
        playSoundAt(this, SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 0.6f, 1.6f + this.random.nextFloat() * 0.2f);
        return true;
    }

    public static boolean isRightWingShot(int shot) {
        return (shot & 1) == 1;
    }

    public boolean hasFeatherTempest() {
        return OWPistePassives.has(this, OWPistePassives.FEATHER_TEMPEST);
    }

    public float getFeatherDamageDivisor() {
        return hasFeatherTempest() ? OWPistePassives.FEATHER_TEMPEST_DAMAGE_DIVISOR : OWAttacksConstants.Peacock.FEATHER_DAMAGE_DIVISOR;
    }

    public int getFeatherMaxStock() {
        int base = OWAttacksConstants.Peacock.FEATHER_MAX_STOCK;
        return hasFeatherTempest() ? Math.round(base * OWPistePassives.FEATHER_TEMPEST_STOCK_FACTOR) : base;
    }

    private boolean fireBurstFeather() {
        if (getFeatherStock() <= 0) return false;
        float cost = OWAttacksConstants.Peacock.FEATHER_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        featherBurstLeft--;
        featherLastShotTick = this.tickCount;

        int shot = getFeatherShot() + 1;
        this.entityData.set(FEATHER_SHOT, shot);
        this.entityData.set(FEATHER_STOCK, getFeatherStock() - 1);
        this.entityData.set(FEATHER_AIM_TICK, OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS);
        featherCadenceTimer = OWAttacksConstants.Peacock.FEATHER_CADENCE_TICKS;
        featherPendingReleases.add(new int[]{this.tickCount + OWAttacksConstants.Peacock.FEATHER_RELEASE_TICKS, isRightWingShot(shot) ? 1 : 0});
        playSoundAt(this, SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 0.4f, 1.6f + this.random.nextFloat() * 0.3f);
        return true;
    }

    private void tickFeatherBurst() {
        if (!(this.getControllingPassenger() instanceof Player) || !canShootFeather() || isLinking()) {
            featherBurstLeft = 0;
            return;
        }
        float spacing = OWPistePassives.FEATHER_TEMPEST_BURST_TICKS / getFeatherMaxStock();
        featherBurstClock += 1f;
        while (featherBurstLeft > 0 && featherBurstClock >= spacing) {
            featherBurstClock -= spacing;
            if (!fireBurstFeather()) {
                featherBurstLeft = 0;
                return;
            }
        }
    }

    private void tickFeathers(ServerLevel level) {
        if (featherCadenceTimer > 0) featherCadenceTimer--;

        int aim = getFeatherAimTick();
        boolean unmanned = this.getControllingPassenger() == null && !isAutoVolleyActive();
        if ((aim > 0 || featherCharging) && (isGliding() || isCombo() || isSweeping() || isKnockedOut() || unmanned)) {
            aim = 0;
            featherCharging = false;
            featherBurstLeft = 0;
            this.entityData.set(FEATHER_AIM_TICK, 0);
        }
        if (featherBurstLeft > 0) {
            tickFeatherBurst();
            aim = getFeatherAimTick();
        }
        while (!featherPendingReleases.isEmpty() && featherPendingReleases.peek()[0] <= this.tickCount) {
            releaseFeather(level, featherPendingReleases.poll()[1] == 1);
        }
        if (featherCharging) {
            aim = OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS;
            this.entityData.set(FEATHER_AIM_TICK, aim);
            if (this.tickCount - featherChargeTick > OWAttacksConstants.Peacock.FEATHER_AIM_HOLD_TICKS) featherCharging = false;
        }

        if (featherReleaseTimer > 0 && --featherReleaseTimer == 0) releaseFeather(level, featherReleaseRight);

        if (aim > 0) {
            this.entityData.set(FEATHER_AIM_TICK, aim - 1);
            return;
        }

        int stock = getFeatherStock();
        if (stock >= getFeatherMaxStock()) {
            featherRegenTimer = 0;
            return;
        }
        if (++featherRegenTimer >= OWAttacksConstants.Peacock.FEATHER_REGEN_TICKS) {
            featherRegenTimer = 0;
            this.entityData.set(FEATHER_STOCK, stock + 1);
        }
    }

    private void releaseFeather(ServerLevel level, boolean rightWing) {
        if (isKnockedOut() || !this.isAlive()) return;

        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
        Vec3 right = new Vec3(-Mth.cos(yawRad), 0, -Mth.sin(yawRad));
        double side = (rightWing ? 1.0 : -1.0) * 0.45 * this.getScale();
        Vec3 origin = this.position()
                .add(0, this.getBbHeight() * 0.62, 0)
                .add(right.scale(side))
                .add(forward.scale(0.2 * this.getScale()));

        Entity homing = featherReleaseTargetId >= 0 ? level.getEntity(featherReleaseTargetId) : null;
        boolean hostile = featherReleaseHostile;
        featherReleaseTargetId = -1;
        featherReleaseHostile = false;
        if (hostile && homing instanceof LivingEntity prey && prey.isAlive() && this.getControllingPassenger() == null) {
            PeacockFeatherEntity.launch(level, this, origin, leadDirection(origin, prey, forward));
        } else if (homing instanceof LivingEntity guided && guided.isAlive() && this.getControllingPassenger() == null) {
            Vec3 aim = guided.position().add(0, guided.getBbHeight() * 0.55, 0).subtract(origin);
            Vec3 direction = aim.lengthSqr() < 1.0E-4 ? forward : aim.normalize();
            PeacockFeatherEntity.launchHoming(level, this, origin, direction, guided);
        } else {
            Vec3 direction = aimDirection(origin, forward);
            PeacockFeatherEntity.launch(level, this, origin, direction);
        }

        playSoundAt(this, OWSounds.PEACOCK_WING_GUST.get(), 0.45f, 1.7f + this.random.nextFloat() * 0.2f);
        playSoundAt(this, SoundEvents.ARROW_SHOOT, 0.5f, 1.8f + this.random.nextFloat() * 0.2f);
        level.sendParticles(FEATHER_PUFF, origin.x, origin.y, origin.z, 4, 0.1, 0.1, 0.1, 0.02);
    }

    private Vec3 leadDirection(Vec3 origin, LivingEntity prey, Vec3 fallback) {
        Vec3 center = prey.position().add(0, prey.getBbHeight() * 0.55, 0);
        double travel = center.distanceTo(origin) / OWAttacksConstants.Peacock.FEATHER_SPEED;
        Vec3 motion = prey.getDeltaMovement();
        Vec3 predicted = center.add(motion.x * travel * FEATHER_LEAD_FACTOR, 0, motion.z * travel * FEATHER_LEAD_FACTOR);
        Vec3 aim = predicted.subtract(origin);
        return aim.lengthSqr() < 1.0E-4 ? fallback : aim.normalize();
    }

    private boolean canRaiseFan() {
        return !this.isTame() && !this.isBaby() && !isKnockedOut() && !isSweeping() && !isGliding()
                && !this.isInWater() && this.getControllingPassenger() == null;
    }

    private void clearFeatherActionsForFan() {
        interruptComboForFeathers();
        featherReleaseTimer = 0;
        featherReleaseTargetId = -1;
        featherReleaseHostile = false;
        this.entityData.set(FEATHER_AIM_TICK, 0);
    }

    public boolean startFanDisplay() {
        return startFanDisplay(null);
    }

    public boolean startFanDisplay(@Nullable LivingEntity focus) {
        if (this.level().isClientSide() || !canRaiseFan()) return false;
        int phase = getFanPhase();
        if (phase == FAN_SHAKE || phase == FAN_CLOSE) return false;
        clearFeatherActionsForFan();
        fanFocusId = focus != null ? focus.getId() : -1;
        this.getNavigation().stop();
        fanCourting = false;
        if (phase == FAN_DANCE || phase == FAN_SHOW) {
            int focusId = fanFocusId;
            setFanPhase(FAN_SHAKE, FAN_SHAKE_TICKS);
            fanFocusId = focusId;
            playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.6f, 1.25f);
            return true;
        }
        if (phase == FAN_OPEN) return true;
        fanStorm = false;
        int focusId = fanFocusId;
        setFanPhase(FAN_OPEN, FAN_OPEN_TICKS);
        fanFocusId = focusId;
        playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.3f, 0.9f);
        return true;
    }

    public boolean startCourtship(@Nullable LivingEntity female, int showTicks, boolean storm) {
        if (this.level().isClientSide() || isFanDisplaying() || !canRaiseFan() || this.getTarget() != null) return false;
        clearFeatherActionsForFan();
        fanCourting = true;
        fanStorm = storm;
        fanShowTicks = Math.max(20, showTicks);
        this.getNavigation().stop();
        setFanPhase(FAN_OPEN, FAN_OPEN_TICKS);
        fanFocusId = female != null ? female.getId() : -1;
        playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.2f, 1.0f);
        if (storm) playSoundAt(this, OWSounds.PEACOCK_CALL.get(), STORM_CALL_VOLUME, 0.9f);
        return true;
    }

    public void closeFan() {
        if (this.level().isClientSide()) return;
        int phase = getFanPhase();
        if (phase == FAN_NONE || phase == FAN_CLOSE) return;
        fanCourting = false;
        setFanPhase(FAN_CLOSE, FAN_CLOSE_TICKS);
    }

    public void stopFanDisplay() {
        if (this.level().isClientSide()) return;
        fanCourting = false;
        setFanPhase(FAN_NONE, 0);
    }

    private void setFanPhase(int phase, int ticks) {
        this.entityData.set(FAN_PHASE, phase);
        fanPhaseTicks = ticks;
        fanPhaseElapsed = 0;
        if (phase == FAN_NONE) fanFocusId = -1;
    }

    public boolean isCourtingPhase() {
        int phase = getFanPhase();
        return fanCourting && (phase == FAN_OPEN || phase == FAN_DANCE || phase == FAN_SHOW);
    }

    public boolean isStormDance() {
        return fanStorm && isCourtingPhase();
    }

    public @Nullable LivingEntity getFanFocus() {
        return fanFocusId >= 0 && this.level().getEntity(fanFocusId) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private void tickFanDisplay(ServerLevel level) {
        int phase = getFanPhase();
        if (phase == FAN_NONE) return;
        if (isKnockedOut() || this.isInWater() || this.getControllingPassenger() != null || !this.isAlive()) {
            stopFanDisplay();
            return;
        }
        if (phase != FAN_SHOW) this.getNavigation().stop();
        int elapsed = fanPhaseElapsed++;

        if (phase == FAN_SHAKE) {
            LivingEntity focus = getFanFocus();
            if (focus != null) faceTowards(focus, 20f);
            if (elapsed % 5 == 0) {
                playSoundAt(this, SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 0.7f, 1.8f + this.random.nextFloat() * 0.2f);
                fanDisplayParticles(level, 6);
            }
            if (elapsed == FAN_FEAR_DELAY_TICKS) unleashFanFear(level);
        } else if (phase == FAN_DANCE) {
            int loop = elapsed % FAN_DANCE_LOOP_TICKS;
            if (loop == FAN_DANCE_VOLTE_TICK) {
                playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.2f, 1.3f);
                fanDisplayParticles(level, 10);
            }
            if (loop > 44 && loop < 66 && loop % 3 == 0) {
                playSoundAt(this, SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 0.5f, 1.9f + this.random.nextFloat() * 0.1f);
            }
            if (fanStorm && loop == 54) playSoundAt(this, OWSounds.PEACOCK_CALL.get(), STORM_CALL_VOLUME, 0.95f);
        }

        if (--fanPhaseTicks > 0) return;
        switch (phase) {
            case FAN_OPEN -> {
                if (fanCourting) {
                    keepFocus(FAN_DANCE, FAN_DANCE_TICKS);
                } else {
                    keepFocus(FAN_SHAKE, FAN_SHAKE_TICKS);
                    playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.6f, 1.25f);
                }
            }
            case FAN_DANCE -> keepFocus(FAN_SHOW, fanShowTicks);
            case FAN_SHAKE, FAN_SHOW -> {
                fanCourting = false;
                setFanPhase(FAN_CLOSE, FAN_CLOSE_TICKS);
            }
            default -> stopFanDisplay();
        }
    }

    private void keepFocus(int phase, int ticks) {
        int focusId = fanFocusId;
        setFanPhase(phase, ticks);
        fanFocusId = focusId;
    }

    public void faceTowards(Entity entity, float maxTurn) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        if (dx * dx + dz * dz < 1.0E-4) return;
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float yaw = Mth.approachDegrees(this.getYRot(), wanted, maxTurn);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
    }

    public boolean isStormIncoming() {
        return stormIncoming;
    }

    public static boolean forecastStorm(ServerLevel level) {
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling()) return false;
        if (!level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_WEATHER_CYCLE)) return false;
        if (!(level.getLevelData() instanceof net.minecraft.world.level.storage.ServerLevelData data)) return false;
        if (level.isThundering() || data.getClearWeatherTime() > 0) return false;
        int thunder = data.getThunderTime();
        int rain = data.getRainTime();
        if (data.isThundering()) {
            return !data.isRaining() && rain > 0 && rain <= STORM_FORECAST_TICKS && rain < thunder;
        }
        if (thunder <= 0 || thunder > STORM_FORECAST_TICKS) return false;
        return data.isRaining() ? rain > thunder : rain > 0 && rain <= thunder;
    }

    private void tickStormSense(ServerLevel level) {
        if (this.tickCount % STORM_CHECK_INTERVAL == 0) {
            boolean incoming = forecastStorm(level);
            if (incoming && !stormIncoming) stormCallTimer = 20 + this.random.nextInt(STORM_CALL_MIN_DELAY);
            stormIncoming = incoming;
        }
        if (!stormIncoming || isKnockedOut() || isNapping() || this.isSleeping() || this.isInWater()) return;
        if (--stormCallTimer > 0) return;
        stormCallTimer = STORM_CALL_MIN_DELAY + this.random.nextInt(STORM_CALL_MAX_DELAY - STORM_CALL_MIN_DELAY + 1);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.PEACOCK_CALL.get(),
                this.getSoundSource(), STORM_CALL_VOLUME, (isBaby() ? 1.5f : 1.0f) + (this.random.nextFloat() - 0.5f) * 0.1f);
        this.entityData.set(STORM_CALL, this.entityData.get(STORM_CALL) + 1);
    }

    private void fanDisplayParticles(ServerLevel level, int count) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 back = new Vec3(Mth.sin(yawRad), 0, -Mth.cos(yawRad)).scale(0.5 * this.getScale());
        double x = this.getX() + back.x;
        double y = this.getY() + this.getBbHeight() * 1.1;
        double z = this.getZ() + back.z;
        level.sendParticles(FEATHER_PUFF, x, y, z, count, 0.9, 0.6, 0.9, 0.02);
    }

    private void unleashFanFear(ServerLevel level) {
        playSoundAt(this, OWSounds.PEACOCK_CALL.get(), 2.0f, 0.75f);
        fanDisplayParticles(level, 30);
        for (LivingEntity aggressor : fanAggressors()) {
            LivingEntity victim = aggressor.getRootVehicle() instanceof OWEntity mount && mount != this ? mount : aggressor;
            victim.addEffect(new MobEffectInstance(OWEffects.FEAR_EFFECT.getDelegate(), FAN_FEAR_DURATION_TICKS, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FAN_SLOW_DURATION_TICKS, 0));
            if (victim != aggressor) aggressor.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FAN_SLOW_DURATION_TICKS, 0));
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, victim.getX(), victim.getY() + victim.getBbHeight() + 0.3, victim.getZ(),
                    3, 0.3, 0.15, 0.3, 0.0);
        }
    }

    public List<LivingEntity> fanAggressors() {
        List<LivingEntity> aggressors = new ArrayList<>();
        LivingEntity lastAttacker = this.tickCount - this.getLastHurtByMobTimestamp() <= FAN_AGGRESSOR_MEMORY_TICKS ? this.getLastHurtByMob() : null;
        for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FAN_FEAR_RADIUS))) {
            if (living == this || !living.isAlive() || living.isSpectator()) continue;
            if (living instanceof PeacockEntity || this.isAlliedTo(living)) continue;
            if (living instanceof Player player && player.isCreative()) continue;
            if (this.distanceToSqr(living) > FAN_FEAR_RADIUS * FAN_FEAR_RADIUS) continue;
            boolean aggressive = living == this.getTarget() || living == lastAttacker
                    || living instanceof Mob mob && mob.getTarget() == this
                    || living.getFirstPassenger() instanceof LivingEntity rider && (rider == this.getTarget() || rider == lastAttacker);
            if (!aggressive || !this.hasLineOfSight(living)) continue;
            aggressors.add(living);
        }
        return aggressors;
    }

    private Vec3 aimDirection(Vec3 origin, Vec3 fallback) {
        if (!(this.getControllingPassenger() instanceof Player rider)) return fallback;

        double distance = OWAttacksConstants.Peacock.FEATHER_AIM_DISTANCE;
        Vec3 eye = rider.getEyePosition();
        Vec3 look = rider.getLookAngle();
        Vec3 end = eye.add(look.scale(distance));

        HitResult block = this.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rider));
        Vec3 target = block.getType() == HitResult.Type.MISS ? end : block.getLocation();

        AABB sweep = rider.getBoundingBox().expandTowards(look.scale(distance)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), rider, eye, target, sweep,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != this && !this.hasPassenger(e));
        if (entityHit != null) {
            Entity hit = entityHit.getEntity();
            target = hit.position().add(0, hit.getBbHeight() * 0.5, 0);
        }

        Vec3 direction = target.subtract(origin);
        if (direction.lengthSqr() < 4.0) return look;
        return direction.normalize();
    }

    public boolean activateRoyalSweep() {
        if (this.level().isClientSide()) return false;
        if (isSweeping() || isGliding()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Peacock.SWEEP_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Peacock.SWEEP_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);
        breakLink(false);
        this.entityData.set(FEATHER_AIM_TICK, 0);

        setUltimateKillCount(0);
        resetCombo(0);
        actualAttackNumber = 0;
        sweepHits.clear();
        sweepDamagePool = 0f;
        this.entityData.set(SWEEP_TICK, OWAttacksConstants.Peacock.SWEEP_ANIMATION_TICKS);
        this.getNavigation().stop();

        playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 1.4f, 0.8f);
        playSoundAt(this, OWSounds.PEACOCK_CALL.get(), 1.6f, 1.0f);
        return true;
    }

    private void tickSweep(ServerLevel level) {
        int linkTick = getSweepLinkTick();
        if (linkTick > 0) this.entityData.set(SWEEP_LINK_TICK, linkTick - 1);

        int tick = getSweepTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Peacock.SWEEP_ANIMATION_TICKS - tick;
        this.entityData.set(SWEEP_TICK, tick - 1);

        int spinStart = OWAttacksConstants.Peacock.SWEEP_SPIN_START_TICK;
        int spinEnd = OWAttacksConstants.Peacock.SWEEP_SPIN_END_TICK;
        if (elapsed == spinStart) {
            playSoundAt(this, SoundEvents.PLAYER_ATTACK_SWEEP, 1.6f, 0.6f);
        }
        if (elapsed >= spinStart && elapsed <= spinEnd) {
            float from = (float) (elapsed - spinStart) / (spinEnd - spinStart + 1);
            float to = (float) (elapsed - spinStart + 1) / (spinEnd - spinStart + 1);
            sweepArc(level, from, to);
        }
        if (elapsed == spinEnd + 1) shareSweepDamage(level);
    }

    private void sweepArc(ServerLevel level, float from, float to) {
        double radius = OWAttacksConstants.Peacock.SWEEP_RADIUS * this.getScale();
        float baseYaw = this.yBodyRot + 180f;
        float startAngle = baseYaw - from * 360f;
        float endAngle = baseYaw - to * 360f;

        for (int i = 0; i < 3; i++) {
            float angle = Mth.lerp(i / 2f, startAngle, endAngle);
            Vec3 dir = Vec3.directionFromRotation(0f, angle);
            for (double d = 1.2; d <= radius; d += 1.1) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * d, this.getY() + 1.0 + d * 0.2, this.getZ() + dir.z * d, 1, 0, 0, 0, 0);
            }
        }

        AABB box = this.getBoundingBox().inflate(radius, 2.0, radius);
        if (sweepHits.add(Integer.MIN_VALUE)) net.tiew.operationWild.entity.bosses.OWBossStrikes.strike(this, box, getCombatDamage());
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive())) {
            if (sweepHits.contains(target.getId()) || this.isAlliedTo(target) || this.hasPassenger(target)) continue;
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz > radius * radius) continue;

            float bearing = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
            float delta = Mth.wrapDegrees(startAngle - bearing);
            if (delta < 0) delta += 360f;
            if (to < 1f && delta > (startAngle - endAngle) + 6f) continue;

            sweepHits.add(target.getId());
            float before = target.getHealth();
            float damage = getCombatDamage();
            target.invulnerableTime = 0;
            if (target.hurt(this.damageSources().mobAttack(this), damage)) {
                sweepDamagePool += Math.min(damage, before) * OWAttacksConstants.Peacock.SWEEP_HEAL_MULTIPLIER;
            }

            Vec3 away = new Vec3(dx, 0, dz);
            away = away.lengthSqr() < 1.0E-4 ? Vec3.directionFromRotation(0f, bearing) : away.normalize();
            OWUtils.addVelocityResisted(this, target, away.x * OWAttacksConstants.Peacock.SWEEP_PUSH,
                    OWAttacksConstants.Peacock.SWEEP_LIFT, away.z * OWAttacksConstants.Peacock.SWEEP_PUSH);
            target.hurtMarked = true;
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 12, 0.3, 0.3, 0.3, 0.3);
        }
    }

    private void shareSweepDamage(ServerLevel level) {
        float pool = sweepDamagePool;
        sweepDamagePool = 0f;
        sweepHits.clear();
        if (pool <= 0.01f) return;

        double radius = OWAttacksConstants.Peacock.SWEEP_SHARE_RADIUS;
        List<LivingEntity> allies = new ArrayList<>();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
            if (!candidate.isAlive() || candidate.distanceTo(this) > radius) continue;
            if (candidate != this && !this.isAlliedTo(candidate)) continue;
            if (candidate.getMaxHealth() - candidate.getHealth() <= 0.01f) continue;
            allies.add(candidate);
        }

        Set<Integer> healed = new HashSet<>();
        for (int pass = 0; pass < 4 && pool > 0.01f && !allies.isEmpty(); pass++) {
            float totalMissing = 0f;
            for (LivingEntity ally : allies) totalMissing += ally.getMaxHealth() - ally.getHealth();
            if (totalMissing <= 0.01f) break;

            float distributed = 0f;
            List<LivingEntity> stillHurt = new ArrayList<>();
            for (LivingEntity ally : allies) {
                float missing = ally.getMaxHealth() - ally.getHealth();
                float share = Math.min(pool * missing / totalMissing, missing);
                ally.heal(share);
                distributed += share;
                if (share > 0.01f && ally != this) healed.add(ally.getId());
                if (ally.getMaxHealth() - ally.getHealth() > 0.01f) stillHurt.add(ally);

                level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY() + ally.getBbHeight() + 0.3, ally.getZ(),
                        1 + (int) Math.min(4, share / 2), 0.35, 0.2, 0.35, 0.0);
                level.sendParticles(LINK_DUST, ally.getX(), ally.getY() + ally.getBbHeight() * 0.5, ally.getZ(), 10, 0.4, 0.5, 0.4, 0.02);
            }
            pool -= distributed;
            allies = stillHurt;
        }

        if (!healed.isEmpty()) {
            StringBuilder ids = new StringBuilder();
            for (int id : healed) {
                if (!ids.isEmpty()) ids.append(',');
                ids.append(id);
            }
            this.entityData.set(SWEEP_LINKS, ids.toString());
            this.entityData.set(SWEEP_LINK_TICK, OWAttacksConstants.Peacock.SWEEP_LINK_TICKS);
        }

        playSoundAt(this, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4f, 1.0f);
        playSoundAt(this, SoundEvents.PLAYER_LEVELUP, 0.5f, 1.8f);
    }

    private void tickClientEffects() {
        tickAnimationBlends();
        tickClientFeathers();

        if (getLinkPhase() != LINK_ACTIVE) return;
        LivingEntity donor = getLinkDonor();
        LivingEntity receiver = getLinkReceiver();
        if (donor == null || receiver == null) return;

        Vec3 from = donor.position().add(0, donor.getBbHeight() * 0.6, 0);
        Vec3 to = receiver.position().add(0, receiver.getBbHeight() * 0.6, 0);
        Vec3 span = to.subtract(from);
        for (int i = 0; i < 2; i++) {
            double t = this.random.nextDouble();
            Vec3 at = from.add(span.scale(t));
            Vec3 velocity = span.normalize().scale(0.12);
            this.level().addParticle(LINK_DUST, at.x + (this.random.nextDouble() - 0.5) * 0.2,
                    at.y + Math.sin(t * Math.PI) * 0.6, at.z + (this.random.nextDouble() - 0.5) * 0.2,
                    velocity.x, velocity.y, velocity.z);
        }
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.GLOW, from.x, from.y, from.z, 0, 0.03, 0);
            this.level().addParticle(ParticleTypes.GLOW, to.x, to.y, to.z, 0, 0.03, 0);
        }
    }

    private void tickAnimationBlends() {
        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() || this.getState() == 2 ? 1f : 0f, RUN_BLEND_RATE);

        sitBlendO = sitBlend;
        sitBlend = approach(sitBlend, this.isSitting() ? 1f : 0f, SIT_BLEND_RATE);

        restBlendO = restBlend;
        restBlend = approach(restBlend, this.isNapping() ? 1f : 0f, REST_BLEND_RATE);

        aimBlendO = aimBlend;
        aimBlend = approach(aimBlend, isFeatherAiming() ? 1f : 0f, isFeatherAiming() ? 0.25f : 0.12f);

        linkBlendO = linkBlend;
        linkBlend = approach(linkBlend, isLinking() ? 1f : 0f, isLinking() ? LINK_BLEND_IN_RATE : LINK_BLEND_OUT_RATE);
    }

    private void tickClientFeathers() {
        int stock = getFeatherStock();
        if (clientLastStock >= 0 && stock > clientLastStock) clientFeatherRegen = 0;
        clientLastStock = stock;
        int maxStock = getFeatherMaxStock();
        if (stock < maxStock && !isFeatherAiming()) {
            clientFeatherRegen = Math.min(clientFeatherRegen + 1, OWAttacksConstants.Peacock.FEATHER_REGEN_TICKS);
        } else if (stock >= maxStock) {
            clientFeatherRegen = 0;
        }

        int shot = getFeatherShot();
        if (shot != clientSeenShot) {
            boolean first = clientSeenShot == 0 && this.tickCount < 5;
            clientSeenShot = shot;
            if (!first) {
                clientShotTicks = OWAttacksConstants.Peacock.FEATHER_SHOT_ANIMATION_TICKS;
                if (isRightWingShot(shot)) {
                    featherShotRightState.start(this.tickCount);
                    clientRightShotTicks = clientShotTicks;
                } else {
                    featherShotLeftState.start(this.tickCount);
                    clientLeftShotTicks = clientShotTicks;
                }
            }
        } else if (clientShotTicks > 0) {
            clientShotTicks--;
        }
        if (clientRightShotTicks > 0 && --clientRightShotTicks == 0) featherShotRightState.stop();
        if (clientLeftShotTicks > 0 && --clientLeftShotTicks == 0) featherShotLeftState.stop();
    }

    public float featherRegenProgress() {
        return (float) clientFeatherRegen / OWAttacksConstants.Peacock.FEATHER_REGEN_TICKS;
    }

    public int clientShotTicksLeft() {
        return clientShotTicks;
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    private void playSoundAt(Entity at, SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
    }

    private void rejectAttack(int attackId) {
        if (this.getControllingPassenger() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OWAttackRejectedPacket(this.getId(), attackId));
        }
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * RIDER_HEIGHT_FACTOR * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        Vec3 seatOffset = new Vec3(0, 0, -0.1 * this.getScale()).yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);
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

            this.setVariant(choosePeacockVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return data;
    }

    private PeacockVariant choosePeacockVariant() {
        int roll = this.random.nextInt(100);
        if (roll < 2) return PeacockVariant.ALBINO;
        if (roll < 7) return PeacockVariant.CAMEO;
        if (roll < 15) return PeacockVariant.PURPLE;
        if (roll < 30) return PeacockVariant.GREEN;
        return PeacockVariant.DEFAULT;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(true);
        createSitAnimation(true);

        this.sweepAnimationState.animateWhen(isSweeping(), this.tickCount);
        trackFanPhase();
        trackStormCall();

        handleIdleGestures();
        setupComboAnimations();
    }

    private void trackFanPhase() {
        int phase = getFanPhase();
        if (phase == clientFanPhase) return;
        fanPrevPhase = clientFanPhase;
        fanPrevMs = this.fanPhaseState.isStarted() ? this.fanPhaseState.getAccumulatedTime() : 0L;
        fanPrevMove = fanShowMove;
        clientFanPhase = phase;
        if (phase == FAN_NONE) this.fanPhaseState.stop();
        else this.fanPhaseState.start(this.tickCount);
    }

    private void trackStormCall() {
        int call = this.entityData.get(STORM_CALL);
        if (call == clientStormCall) return;
        boolean fresh = clientStormCall != 0 || this.tickCount > 5;
        clientStormCall = call;
        if (!fresh || this.isNapping() || this.isKnockedOut() || isFanLocked() || isSweeping() || isGliding()) return;
        this.idleGestureAnimationState.stop();
        this.peckAnimationState.stop();
        this.callAnimationState.start(this.tickCount);
        idleGestureTicks = 55;
    }

    private void handleIdleGestures() {
        if (idleGestureTicks > 0 && --idleGestureTicks <= 0) {
            this.idleGestureAnimationState.stop();
            this.peckAnimationState.stop();
            this.callAnimationState.stop();
        }
        if (!canPlayIdleGesture()) {
            if (idleGestureTicks > 0) {
                idleGestureTicks = 0;
                this.idleGestureAnimationState.stop();
                this.peckAnimationState.stop();
                this.callAnimationState.stop();
            }
            return;
        }
        if (idleGestureTicks > 0 || --idleGestureCooldown > 0) return;

        idleGestureCooldown = IDLE_GESTURE_MIN_DELAY + this.random.nextInt(IDLE_GESTURE_MAX_DELAY - IDLE_GESTURE_MIN_DELAY);
        int pick = this.random.nextInt(3);
        if (pick == 0) {
            this.idleGestureAnimationState.start(this.tickCount);
            idleGestureTicks = 80;
        } else if (pick == 1) {
            this.peckAnimationState.start(this.tickCount);
            idleGestureTicks = 50;
        } else {
            this.callAnimationState.start(this.tickCount);
            idleGestureTicks = 55;
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), OWSounds.PEACOCK_CALL.get(),
                    this.getSoundSource(), 1.0f, isBaby() ? 1.6f : 1.0f, false);
        }
    }

    public boolean canPlayIdleGesture() {
        return this.getTarget() == null && !this.isNapping() && !this.isSitting() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isCombo() && !isLinking()
                && !isFeatherAiming() && !isSweeping() && !isKnockedOut() && !isGliding() && !isFanDisplaying();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (28 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (25 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (22 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public float runBlend(float partialTick) { return Mth.lerp(partialTick, runBlendO, runBlend); }

    public float sitBlend(float partialTick) { return Mth.lerp(partialTick, sitBlendO, sitBlend); }

    public float restBlend(float partialTick) { return Mth.lerp(partialTick, restBlendO, restBlend); }

    public float linkBlend(float partialTick) { return Mth.lerp(partialTick, linkBlendO, linkBlend); }

    public float aimBlend(float partialTick) { return Mth.lerp(partialTick, aimBlendO, aimBlend); }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof PeacockEntity peacock) {
            peacock.setVariant(PeacockVariant.byId(variant));
            peacock.setInitialVariant(PeacockVariant.byId(variant));
        }
    }

    public PeacockVariant getVariant() {
        return PeacockVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(PeacockVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(PeacockVariant skin) {
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

    public PeacockVariant getInitialVariant() {
        return PeacockVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(PeacockVariant variant) {
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

    public int getLinkPhase() { return this.entityData.get(LINK_PHASE); }

    public boolean isLinking() { return getLinkPhase() == LINK_ACTIVE; }

    public boolean isSelectingLink() { return getLinkPhase() == LINK_SELECTING; }

    public int getLinkDonorId() { return this.entityData.get(LINK_DONOR); }

    public int getLinkReceiverId() { return this.entityData.get(LINK_RECEIVER); }

    public @Nullable LivingEntity getLinkDonor() {
        return this.level().getEntity(getLinkDonorId()) instanceof LivingEntity living ? living : null;
    }

    public @Nullable LivingEntity getLinkReceiver() {
        return this.level().getEntity(getLinkReceiverId()) instanceof LivingEntity living ? living : null;
    }

    public int getFeatherStock() { return this.entityData.get(FEATHER_STOCK); }

    public int getFeatherAimTick() { return this.entityData.get(FEATHER_AIM_TICK); }

    public boolean isFeatherAiming() { return getFeatherAimTick() > 0; }

    public int getFeatherShot() { return this.entityData.get(FEATHER_SHOT); }

    public int getSweepTick() { return this.entityData.get(SWEEP_TICK); }

    public boolean isSweeping() { return getSweepTick() > 0; }

    public int getFanPhase() { return this.entityData.get(FAN_PHASE); }

    public boolean isFanDisplaying() { return getFanPhase() != FAN_NONE; }

    public boolean isFanLocked() {
        int phase = getFanPhase();
        return phase != FAN_NONE && phase != FAN_SHOW;
    }

    public int getSweepLinkTick() { return this.entityData.get(SWEEP_LINK_TICK); }

    public List<LivingEntity> getSweepLinkedEntities() {
        String raw = this.entityData.get(SWEEP_LINKS);
        List<LivingEntity> entities = new ArrayList<>();
        if (raw.isEmpty()) return entities;
        for (String part : raw.split(",")) {
            try {
                if (this.level().getEntity(Integer.parseInt(part)) instanceof LivingEntity living && living.isAlive()) entities.add(living);
            } catch (NumberFormatException ignored) {
            }
        }
        return entities;
    }

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
        tag.putInt("featherStock", this.getFeatherStock());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) setUltimateKillCount(tag.getInt("ultimateKillCount"));
        if (tag.contains("featherStock")) this.entityData.set(FEATHER_STOCK,
                Mth.clamp(tag.getInt("featherStock"), 0, getFeatherMaxStock()));
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    class PeacockMeleeAttackGoal extends MeleeAttackGoal {

        public PeacockMeleeAttackGoal() {
            super(PeacockEntity.this, 6, true);
        }

        @Override
        public boolean canUse() {
            if (PeacockEntity.this.isSedatedFleeing() || PeacockEntity.this.isSweeping()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (PeacockEntity.this.isSedatedFleeing() || PeacockEntity.this.isSweeping()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            PeacockEntity.this.setMad(true);
            PeacockEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            PeacockEntity.this.setMad(false);
            PeacockEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2.0;
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

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(22, 1.5f, 3.0f, 0.6f)
    );

    @Override
    public List<OWSedationImpact> sedationImpacts() {
        return SEDATION_IMPACTS;
    }

    @Override
    protected int experienceRewardMin() {
        return 2;
    }

    @Override
    protected int experienceRewardMax() {
        return 4;
    }
}
