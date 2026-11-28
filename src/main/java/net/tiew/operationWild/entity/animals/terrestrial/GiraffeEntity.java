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
import net.minecraft.world.effect.MobEffectCategory;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
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
import net.tiew.operationWild.entity.goals.giraffe.GiraffeAutoHealGoal;
import net.tiew.operationWild.entity.goals.giraffe.GiraffeHerdFleeGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.OWSedationImpact;
import net.tiew.operationWild.entity.variants.GiraffeVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.platform.OWPlatform;
import net.tiew.operationWild.entity.platform.OWPlatformProfile;
import java.util.Set;

public class GiraffeEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 270.0;
    public static final int MAX_SOMNOLENCE = 4200;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 3;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 8;
    public static final int FOOD_WANTED_MIN = 12;
    public static final int FOOD_WANTED_MAX = 18;
    public static final int ENTITY_COLOR = 0xE0AF7B;
    public static final int DEFAULT_SKIN_INDEX = 1;

    public static final double BASE_HEALTH = 60.0;
    public static final double BASE_DAMAGE = 8.0;
    public static final double BASE_SPEED = 0.16;

    private static final float MAX_VITAL_ENERGY = 330f;
    private static final double RIDER_HEIGHT_FACTOR = 0.82;
    private static final double MODEL_ORIGIN_Y = 1.501;
    private static final Vector3f RIDER_FEET_IN_BODY = new Vector3f(0f, -3.4f / 16f, 7.5f / 16f);
    private static final Vector3f REST_RIDER_FEET_MODEL = new Vector3f(0f, -20.4f / 16f, 7.5f / 16f);
    public static final OWPlatformProfile PLATFORM_PROFILE = new OWPlatformProfile(7, 5, 6, 2, 2, 3, 2,
            33.0 / 16.0, -3.0 / 16.0, 2.0 / 16.0, null, List.of(3), List.of(), null, 0.0);
    private static final double PLATFORM_DROP_LYING = 30.0 / 16.0;
    private static final double PLATFORM_DROP_SWIMMING = 0.93;
    private static final double PLATFORM_DROP_KNOCKED_OUT = 33.0 / 16.0 - 0.5;
    private static final double PLATFORM_SWIM_MIN_DEPTH = 0.5;
    private static final int SEDATION_WAKE_TICKS = 60;
    private static final int SIT_TRANSITION_TICKS = 27;
    public static final int NAP_TRANSITION_TICKS = 35;
    private static final float RUN_BLEND_RATE = 0.15f;
    private static final float BLESS_BLEND_IN_RATE = 0.25f;
    private static final float BLESS_BLEND_OUT_RATE = 0.125f;
    private static final int IDLE_GESTURE_MIN_DELAY = 400;
    private static final int IDLE_GESTURE_MAX_DELAY = 900;
    private static final int IDLE_GESTURE_TICKS = 90;
    private static final float WILD_ROTATION_SPEED = 0.12f;
    private static final float RIDDEN_ROTATION_SPEED = 0.08f;
    private static final float BANK_MAX_ANGLE = 24f;
    private static final DustParticleOptions BLESSING_DUST = new DustParticleOptions(new Vector3f(1f, 0.84f, 0.36f), 1.2f);
    private static final DustParticleOptions SENTINEL_DUST = new DustParticleOptions(new Vector3f(1f, 0.93f, 0.58f), 1.0f);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLESSING_TICK = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLESSING_TARGET = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLESSING_COOLDOWN = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SENTINEL_CHARGE = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SENTINEL_CAST = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SENTINEL_ACTIVE = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> SENTINEL_LINKS = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.STRING);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState sentinelAnimationState = new AnimationState();
    public final AnimationState idleGestureAnimationState = new AnimationState();
    public final AnimationState napInState = new AnimationState();
    public final AnimationState napOutState = new AnimationState();

    private int idleGestureCooldown = IDLE_GESTURE_MIN_DELAY;
    private int idleGestureTicks = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;
    public volatile org.joml.Matrix4f boneMatrix = null;

    private float runBlend, runBlendO;
    private float blessBlend, blessBlendO;
    private int blessClientTicks = 0;
    private int herdFleeTicks = 0;
    private @Nullable Vec3 herdThreat = null;
    private final Set<Integer> sentinelLinked = new LinkedHashSet<>();
    private int sentinelHealTimer = 0;
    private boolean blessingAuto = false;
    private int blessingPulseTimer = 0;
    private boolean blessingCredited = false;
    private int sentinelScanTimer = 0;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public GiraffeEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GiraffeHerdFleeGoal(this));
        this.goalSelector.addGoal(1, new GiraffeAutoHealGoal(this));
        this.goalSelector.addGoal(3, new GiraffeMeleeAttackGoal());
        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(BLESSING_TICK, 0);
        builder.define(BLESSING_TARGET, -1);
        builder.define(BLESSING_COOLDOWN, 0);
        builder.define(SENTINEL_CHARGE, 0);
        builder.define(SENTINEL_CAST, 0);
        builder.define(SENTINEL_ACTIVE, 0);
        builder.define(SENTINEL_LINKS, "");
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
        return 15f;
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
        return !isBlessing() && !isSentinelCasting();
    }

    @Override
    public boolean usesAggressiveTaming() {
        return true;
    }

    @Override
    public List<EntityType<?>> getValidTamingProjectiles() {
        return List.of(OWEntityRegistry.TRANQUILIZER_ARROW.get(), OWEntityRegistry.TRANQUILIZER_DART.get());
    }

    @Override
    public float sedatedFleeSpeed() {
        return 5.5f;
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
    public void onSedationKnockOut() {
        cancelBlessing();
        stopSentinel();
        herdFleeTicks = 0;
        herdThreat = null;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.HEALER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.VEGETARIAN;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.PASSIVE;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.4f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.6f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1.5f;
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
    public OWPlatformProfile platformProfile() {
        return PLATFORM_PROFILE;
    }

    private boolean isPlatformTippedOver() {
        return this.isKnockedOut() || (this.isSleeping() && this.hasSedationAnimations() && !this.isNapping());
    }

    @Override
    public double platformPoseDrop() {
        if (isPlatformTippedOver()) return PLATFORM_DROP_KNOCKED_OUT;
        if (this.isSitting() || this.isNapping() || this.isSleeping()) return PLATFORM_DROP_LYING;
        if (this.isInWater() && !this.onGround() && this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > PLATFORM_SWIM_MIN_DEPTH) {
            return PLATFORM_DROP_SWIMMING;
        }
        return 0.0;
    }

    @Override
    public boolean platformCollidable() {
        return !isPlatformTippedOver();
    }

    @Override
    public Item acceptPlatform() {
        return OWItems.GIRAFFE_PLATFORM.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.GIRAFFE_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return MAX_VITAL_ENERGY * (1f + this.getLevel() / 100f);
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.0f * (1 + ((float) this.getLevel() / 50));
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
        if (isSentinelCasting()) return 0f;
        return this.isVehicle() ? RIDDEN_ROTATION_SPEED : WILD_ROTATION_SPEED;
    }

    @Override
    protected boolean canLean() {
        return !this.isSitting() && !this.isSleeping() && !this.isNapping() && !isSentinelCasting() && !isKnockedOut();
    }

    @Override
    protected float bankMaxAngle() {
        return BANK_MAX_ANGLE;
    }

    @Override
    protected float pitchMaxAngle() {
        return 0f;
    }

    @Override
    protected int sitTransitionTicks() {
        return SIT_TRANSITION_TICKS;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.GIRAFFE.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.GIRAFFE_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return SoundEvents.CAMEL_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAMEL_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
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
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 120L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundtype.getStepSound(), this.getSoundSource(),
                soundtype.getVolume() * 0.45F, soundtype.getPitch() * pitchMod, false);
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.CAMEL_STEP, this.getSoundSource(),
                0.35F, 0.8F * pitchMod, false);
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.8f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(0.9f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(24, this.getComboAttack() == 3 ? 17 : 16, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 3.0, 3.0, 2.0,
                actualAttackNumber == 2, actualAttackNumber == 2 ? 2.0f : 0.6f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.level() instanceof ServerLevel serverLevel) {
            tickBlessing(serverLevel);
            tickSentinel(serverLevel);
            if (herdFleeTicks > 0 && --herdFleeTicks == 0) herdThreat = null;
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
        double z = this.getZ() + forward.z * reach;

        level.sendParticles(ParticleTypes.POOF, x, this.getY() + 0.2, z, this.getComboAttack() == 3 ? 12 : 6, 0.4, 0.05, 0.4, 0.04);
        level.sendParticles(ParticleTypes.CRIT, x, this.getY() + 0.8, z, this.getComboAttack() == 3 ? 12 : 5, 0.3, 0.3, 0.3, 0.25);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isSedatedFleeing() || isNapping() || isHerdFleeing())) return;
        super.setTarget(target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) this.setSitting(false);
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide() && !this.isTame() && !isKnockedOut()
                && damageSource.getEntity() instanceof LivingEntity attacker && attacker != this) {
            alarmHerd(attacker);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            cancelBlessing();
            stopSentinel();
        }
        super.die(damageSource);
        if (!this.level().isClientSide() && this.isSaddled()) {
            this.flushPlatform();
            this.spawnAtLocation(this.hasPlatform() ? this.equippedSaddleStack().copy() : new net.minecraft.world.item.ItemStack(acceptSaddle()));
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isSentinelCasting();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isSentinelCasting();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isBlessing() && !isSentinelCasting();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isSentinelCasting()) return 0;
        return super.getRiddenSpeedVehicle(player);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof GiraffeEntity other && !other.isTame() && !this.isTame()) return true;
        return super.isAlliedTo(entity);
    }

    public void alarmHerd(LivingEntity attacker) {
        Vec3 threat = attacker.position();
        startHerdFlee(threat);
        double radius = OWAttacksConstants.Giraffe.HERD_FLEE_RADIUS;
        for (GiraffeEntity other : this.level().getEntitiesOfClass(GiraffeEntity.class, this.getBoundingBox().inflate(radius, 8.0, radius))) {
            if (other == this || other.isTame() || other.distanceTo(this) > radius) continue;
            other.startHerdFlee(threat);
        }
    }

    public void startHerdFlee(Vec3 threat) {
        if (this.isTame() || isKnockedOut() || this.isVehicle()) return;
        if (this.isSitting()) this.setSitting(false);
        herdThreat = threat;
        herdFleeTicks = OWAttacksConstants.Giraffe.HERD_FLEE_TICKS;
        this.setTarget(null);
    }

    public boolean isHerdFleeing() {
        return herdFleeTicks > 0 && herdThreat != null && !this.isTame();
    }

    public @Nullable Vec3 getHerdThreat() {
        return herdThreat;
    }

    public boolean isBlessingCandidate(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living) || living == this || !living.isAlive()) return false;
        if (living instanceof Player player && player.isSpectator()) return false;
        if (this.isAlliedTo(living)) return true;
        java.util.UUID owner = this.getOwnerUUID();
        if (owner == null) return false;
        if (living instanceof Player player && owner.equals(player.getUUID())) return true;
        if (living instanceof OWEntity other && owner.equals(other.getOwnerUUID())) return true;
        if (!this.level().isClientSide()) return false;
        net.tiew.operationWild.team.OWTeam tribe = net.tiew.operationWild.client.OWClientTribeData.get();
        if (tribe == null || !tribe.isMember(owner)) return false;
        if (living instanceof Player player) return tribe.isMember(player.getUUID());
        return living instanceof OWEntity other && other.getOwnerUUID() != null && tribe.isMember(other.getOwnerUUID());
    }

    public @Nullable LivingEntity pickBlessingTarget(LivingEntity aimer) {
        double range = OWAttacksConstants.Giraffe.BLESSING_AIM_RANGE;
        double tolerance = OWAttacksConstants.Giraffe.BLESSING_AIM_TOLERANCE;
        double minDistance = OWAttacksConstants.Giraffe.BLESSING_MIN_AIM_DISTANCE;

        Vec3 eye = aimer.getEyePosition();
        Vec3 look = aimer.getLookAngle().normalize();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, aimer.getBoundingBox().inflate(range))) {
            if (candidate == aimer || this.hasPassenger(candidate) || !isBlessingCandidate(candidate)) continue;

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

    public boolean canBless() {
        return !isBlessing() && !isSentinelCasting() && getBlessingCooldown() <= 0 && !isKnockedOut() && !this.isSitting();
    }

    public void requestBlessing(int targetId) {
        if (this.level().isClientSide()) return;
        Entity picked = this.level().getEntity(targetId);
        Entity rider = this.getControllingPassenger();
        if (!(rider instanceof Player) || !isBlessingCandidate(picked) || this.hasPassenger(picked)
                || this.distanceTo(picked) > OWAttacksConstants.Giraffe.BLESSING_AIM_RANGE + 4.0 || !canBless()) {
            rejectAttack(OWAttackIds.GIRAFFE_BLESSING);
            return;
        }
        float cost = OWAttacksConstants.Giraffe.BLESSING_START_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            rejectAttack(OWAttackIds.GIRAFFE_BLESSING);
            return;
        }
        startBlessing((LivingEntity) picked, cost, false);
    }

    public void releaseBlessingByRider() {
        if (this.level().isClientSide() || !isBlessing() || blessingAuto) return;
        stopBlessing();
    }

    public boolean autoBless(LivingEntity target) {
        if (this.level().isClientSide() || this.getControllingPassenger() != null) return false;
        if (target != this && !isBlessingCandidate(target)) return false;
        if (!canBless() || this.isCombo()) return false;
        float cost = OWAttacksConstants.Giraffe.BLESSING_START_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) return false;
        startBlessing(target, cost, true);
        return true;
    }

    private void startBlessing(LivingEntity target, float cost, boolean auto) {
        if (this.isCombo()) {
            resetCombo(0);
            actualAttackNumber = 0;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        blessingAuto = auto;
        blessingPulseTimer = 0;
        blessingCredited = false;
        this.entityData.set(BLESSING_TARGET, target.getId());
        this.entityData.set(BLESSING_TICK, OWAttacksConstants.Giraffe.BLESSING_MAX_TICKS);
        playSoundAt(this, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 0.9f);
        playSoundAt(this, SoundEvents.BEACON_ACTIVATE, 0.6f, 1.5f);
    }

    private void stopBlessing() {
        if (this.level().isClientSide()) return;
        boolean wasActive = isBlessing();
        cancelBlessing();
        if (!wasActive) return;
        this.entityData.set(BLESSING_COOLDOWN, OWAttacksConstants.Giraffe.BLESSING_COOLDOWN_TICKS);
        playSoundAt(this, SoundEvents.AMETHYST_BLOCK_BREAK, 0.7f, 1.3f);
    }

    public void cancelBlessing() {
        if (this.level().isClientSide()) return;
        this.entityData.set(BLESSING_TICK, 0);
        this.entityData.set(BLESSING_TARGET, -1);
        blessingAuto = false;
        blessingPulseTimer = 0;
        blessingCredited = false;
    }

    private void tickBlessing(ServerLevel level) {
        int cooldown = getBlessingCooldown();
        if (cooldown > 0) this.entityData.set(BLESSING_COOLDOWN, cooldown - 1);

        int tick = getBlessingTick();
        if (tick <= 0) return;

        LivingEntity target = getBlessingTarget();
        boolean lostControl = blessingAuto
                ? this.getControllingPassenger() != null || !canAutoHealNow()
                : !(this.getControllingPassenger() instanceof Player);
        if (lostControl || target == null || !target.isAlive() || isKnockedOut() || isSentinelCasting()
                || target.distanceTo(this) > OWAttacksConstants.Giraffe.BLESSING_AIM_RANGE + 4.0
                || (blessingAuto && target.getHealth() >= target.getMaxHealth())) {
            stopBlessing();
            return;
        }

        this.entityData.set(BLESSING_TICK, tick - 1);
        if (this.getControllingPassenger() == null) this.getLookControl().setLookAt(target, 30f, 30f);

        if (++blessingPulseTimer >= OWAttacksConstants.Giraffe.BLESSING_PULSE_TICKS) {
            blessingPulseTimer = 0;
            float cost = OWAttacksConstants.Giraffe.BLESSING_ENERGY_PER_SECOND * OWAttacksConstants.Giraffe.BLESSING_PULSE_TICKS / 20f;
            if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
                canShowVitalEnergyLack = true;
                stopBlessing();
                return;
            }
            setVitalEnergy(getVitalEnergy() + cost);
            applyBlessingPulse(level, target, 1f);

            if (OWPistePassives.has(this, OWPistePassives.SHARED_BLESSING)) {
                double radius = OWPistePassives.SHARED_BLESSING_RADIUS;
                for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius))) {
                    if (near == target || near.distanceTo(target) > radius) continue;
                    if (near != this && !isBlessingCandidate(near)) continue;
                    applyBlessingPulse(level, near, OWPistePassives.SHARED_BLESSING_FACTOR);
                }
            }

            if (!blessingCredited) {
                blessingCredited = true;
                if (this.isTame() && getSentinelCharge() < OWAttacksConstants.Giraffe.SENTINEL_BLESSINGS_REQUIRED) {
                    this.entityData.set(SENTINEL_CHARGE, getSentinelCharge() + 1);
                }
            }
        }

        if (tick - 1 <= 0) stopBlessing();
    }

    private void applyBlessingPulse(ServerLevel level, LivingEntity target, float factor) {
        float share = OWAttacksConstants.Giraffe.BLESSING_PULSE_TICKS / 20f * factor;
        target.heal(OWAttacksConstants.Giraffe.BLESSING_HEAL_PER_SECOND * share);
        if (target instanceof OWEntity beast) {
            beast.setVitalEnergy(Math.max(0f, beast.getVitalEnergy() - OWAttacksConstants.Giraffe.BLESSING_VITAL_PER_SECOND * share));
        }
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, OWAttacksConstants.Giraffe.BLESSING_RESISTANCE_TICKS,
                OWAttacksConstants.Giraffe.BLESSING_RESISTANCE_AMPLIFIER, false, false, true), this);

        double x = target.getX();
        double z = target.getZ();
        double mid = target.getY() + target.getBbHeight() * 0.5;
        level.sendParticles(BLESSING_DUST, x, target.getY() + target.getBbHeight() + 0.3, z, 4, 0.25, 0.4, 0.25, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, x, mid, z, 3, 0.35, 0.5, 0.35, 0.03);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, mid, z, 2, 0.4, 0.4, 0.4, 0.0);
        if (this.tickCount % 20 < OWAttacksConstants.Giraffe.BLESSING_PULSE_TICKS) {
            level.sendParticles(ParticleTypes.HEART, x, target.getY() + target.getBbHeight() + 0.4, z, 1, 0.3, 0.2, 0.3, 0.0);
            playSoundAt(target, SoundEvents.AMETHYST_BLOCK_CHIME, 0.9f, 1.2f + this.random.nextFloat() * 0.3f);
        }
    }

    public boolean activateSentinel() {
        if (this.level().isClientSide()) return false;
        if (isSentinelCasting() || isSentinelActive() || isKnockedOut()) return false;
        if (getSentinelCharge() < OWAttacksConstants.Giraffe.SENTINEL_BLESSINGS_REQUIRED) return false;

        float cost = OWAttacksConstants.Giraffe.SENTINEL_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);
        cancelBlessing();
        resetCombo(0);
        actualAttackNumber = 0;

        this.entityData.set(SENTINEL_CHARGE, 0);
        this.entityData.set(SENTINEL_CAST, OWAttacksConstants.Giraffe.SENTINEL_CAST_TICKS);
        this.entityData.set(SENTINEL_ACTIVE, 0);
        this.entityData.set(SENTINEL_LINKS, "");
        sentinelLinked.clear();
        sentinelHealTimer = 0;
        sentinelScanTimer = 0;
        this.getNavigation().stop();

        playSoundAt(this, SoundEvents.CAMEL_DASH, 1.6f, 0.7f);
        playSoundAt(this, SoundEvents.BEACON_ACTIVATE, 1.2f, 0.8f);
        return true;
    }

    public void stopSentinel() {
        if (this.level().isClientSide()) return;
        boolean wasActive = isSentinelActive();
        this.entityData.set(SENTINEL_CAST, 0);
        this.entityData.set(SENTINEL_ACTIVE, 0);
        this.entityData.set(SENTINEL_LINKS, "");
        sentinelLinked.clear();
        sentinelHealTimer = 0;
        sentinelScanTimer = 0;
        if (wasActive) playSoundAt(this, SoundEvents.BEACON_DEACTIVATE, 1.0f, 1.2f);
    }

    public double sentinelRadius() {
        double radius = OWAttacksConstants.Giraffe.SENTINEL_RADIUS;
        return OWPistePassives.has(this, OWPistePassives.GRAND_GUARDIAN) ? radius * OWPistePassives.GRAND_GUARDIAN_RADIUS_FACTOR : radius;
    }

    private double sentinelBreakDistance() {
        double distance = OWAttacksConstants.Giraffe.SENTINEL_BREAK_DISTANCE;
        return OWPistePassives.has(this, OWPistePassives.GRAND_GUARDIAN) ? distance * OWPistePassives.GRAND_GUARDIAN_RADIUS_FACTOR : distance;
    }

    private void tickSentinel(ServerLevel level) {
        int cast = getSentinelCast();
        if (cast > 0) {
            int elapsed = OWAttacksConstants.Giraffe.SENTINEL_CAST_TICKS - cast;
            this.entityData.set(SENTINEL_CAST, cast - 1);
            if (elapsed == OWAttacksConstants.Giraffe.SENTINEL_IMPACT_TICK) beginSentinel(level);
        }

        int active = getSentinelActive();
        if (active <= 0) return;
        if (isKnockedOut() || !this.isAlive()) {
            stopSentinel();
            return;
        }
        this.entityData.set(SENTINEL_ACTIVE, active - 1);
        if (active - 1 <= 0) {
            stopSentinel();
            return;
        }

        if (++sentinelScanTimer >= OWAttacksConstants.Giraffe.SENTINEL_SCAN_INTERVAL) {
            sentinelScanTimer = 0;
            refreshSentinelLinks(level);
        }
        if (++sentinelHealTimer >= OWAttacksConstants.Giraffe.SENTINEL_HEAL_INTERVAL) {
            sentinelHealTimer = 0;
            float heal = OWAttacksConstants.Giraffe.SENTINEL_HEAL_PER_SECOND * OWAttacksConstants.Giraffe.SENTINEL_HEAL_INTERVAL / 20f;
            for (LivingEntity ally : getSentinelLinkedEntities()) {
                if (ally.getHealth() >= ally.getMaxHealth()) continue;
                ally.heal(heal);
                level.sendParticles(SENTINEL_DUST, ally.getX(), ally.getY() + ally.getBbHeight() * 0.6, ally.getZ(), 4, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    private void beginSentinel(ServerLevel level) {
        int duration = OWAttacksConstants.Giraffe.SENTINEL_DURATION_TICKS;
        if (OWPistePassives.has(this, OWPistePassives.GRAND_GUARDIAN)) duration += OWPistePassives.GRAND_GUARDIAN_EXTRA_TICKS;
        this.entityData.set(SENTINEL_ACTIVE, duration);
        refreshSentinelLinks(level);

        double radius = sentinelRadius();
        for (int i = 0; i < 48; i++) {
            float angle = i / 48f * Mth.TWO_PI;
            level.sendParticles(SENTINEL_DUST, this.getX() + Mth.cos(angle) * 2.5, this.getY() + 0.2, this.getZ() + Mth.sin(angle) * 2.5,
                    1, 0, 0.05, 0, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, this.getX() + Mth.cos(angle) * radius * 0.5, this.getY() + 0.3,
                    this.getZ() + Mth.sin(angle) * radius * 0.5, 1, 0, 0.1, 0, 0.02);
        }
        level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.1, this.getZ(), 30, 1.5, 0.1, 1.5, 0.08);
        playSoundAt(this, SoundEvents.GENERIC_EXPLODE.value(), 0.5f, 1.6f);
        playSoundAt(this, SoundEvents.BEACON_POWER_SELECT, 1.5f, 0.9f);
        playSoundAt(this, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.5f, 0.7f);
    }

    private void refreshSentinelLinks(ServerLevel level) {
        double breakDistance = sentinelBreakDistance();
        sentinelLinked.removeIf(id -> {
            Entity entity = level.getEntity(id);
            return !(entity instanceof LivingEntity living) || !living.isAlive() || living.distanceTo(this) > breakDistance;
        });

        double radius = sentinelRadius();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
            if (sentinelLinked.size() >= OWAttacksConstants.Giraffe.SENTINEL_MAX_LINKS) break;
            if (sentinelLinked.contains(candidate.getId()) || candidate.distanceTo(this) > radius) continue;
            if (!isBlessingCandidate(candidate)) continue;
            if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) continue;
            sentinelLinked.add(candidate.getId());
            purgeHarmfulEffects(candidate);
            level.sendParticles(ParticleTypes.END_ROD, candidate.getX(), candidate.getY() + candidate.getBbHeight() * 0.5, candidate.getZ(),
                    10, 0.3, 0.4, 0.3, 0.04);
            playSoundAt(candidate, SoundEvents.AMETHYST_CLUSTER_PLACE, 0.8f, 1.5f);
        }

        StringBuilder ids = new StringBuilder();
        for (int id : sentinelLinked) {
            if (!ids.isEmpty()) ids.append(',');
            ids.append(id);
        }
        this.entityData.set(SENTINEL_LINKS, ids.toString());
    }

    private static void purgeHarmfulEffects(LivingEntity entity) {
        List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> harmful = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) harmful.add(instance.getEffect());
        }
        for (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect : harmful) entity.removeEffect(effect);
    }

    public boolean isSentinelLinked(LivingEntity entity) {
        return isSentinelActive() && sentinelLinked.contains(entity.getId());
    }

    public static @Nullable GiraffeEntity findGuardian(LivingEntity ally) {
        if (ally.level().isClientSide()) return null;
        double reach = OWAttacksConstants.Giraffe.SENTINEL_BREAK_DISTANCE * OWPistePassives.GRAND_GUARDIAN_RADIUS_FACTOR;
        for (GiraffeEntity giraffe : ally.level().getEntitiesOfClass(GiraffeEntity.class, ally.getBoundingBox().inflate(reach))) {
            if (giraffe != ally && giraffe.isAlive() && !giraffe.isKnockedOut() && giraffe.isSentinelLinked(ally)) return giraffe;
        }
        return null;
    }

    public void absorbFatalBlow(LivingEntity ally, float amount) {
        if (!(this.level() instanceof ServerLevel level)) return;
        this.invulnerableTime = 0;
        this.hurt(this.damageSources().magic(), amount);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, ally.getX(), ally.getY() + ally.getBbHeight() * 0.5, ally.getZ(),
                30, 0.4, 0.6, 0.4, 0.3);
        level.sendParticles(SENTINEL_DUST, this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ(), 20, 0.6, 0.6, 0.6, 0.02);
        playSoundAt(ally, SoundEvents.TOTEM_USE, 0.6f, 1.4f);
    }

    private void tickClientEffects() {
        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() || this.getState() == 2 ? 1f : 0f, RUN_BLEND_RATE);

        blessBlendO = blessBlend;
        if (isBlessing()) {
            if (blessBlend <= 0f) blessClientTicks = 0;
            blessClientTicks++;
        }
        blessBlend = approach(blessBlend, isBlessing() ? 1f : 0f, isBlessing() ? BLESS_BLEND_IN_RATE : BLESS_BLEND_OUT_RATE);

        if (isSentinelActive() && this.tickCount % 4 == 0) {
            Vec3 origin = hornTop();
            for (LivingEntity ally : getSentinelLinkedEntities()) {
                Vec3 to = ally.position().add(0, ally.getBbHeight() * 0.6, 0);
                double t = this.random.nextDouble();
                Vec3 at = origin.add(to.subtract(origin).scale(t));
                this.level().addParticle(SENTINEL_DUST, at.x, at.y + Math.sin(t * Math.PI) * 0.8, at.z, 0, 0, 0);
            }
        }
        if (isBlessing() && this.tickCount % 2 == 0) {
            Vec3 horns = hornTop();
            this.level().addParticle(ParticleTypes.END_ROD, horns.x, horns.y, horns.z,
                    (this.random.nextDouble() - 0.5) * 0.05, 0.05, (this.random.nextDouble() - 0.5) * 0.05);
        }
    }

    public Vec3 hornTop() {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
        return this.position().add(forward.scale(1.55 * this.getScale())).add(0, 5.4 * this.getScale() - bodyAnimY / 16.0, 0);
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
    public org.joml.Matrix4f riderBoneMatrix() {
        return this.boneMatrix;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        OWPlatform deck = this.getPlatform();
        if (deck != null && deck.profile().pilotSeat() != null) {
            Vec3 seat = deck.current().toWorld(deck.profile().pilotSeat());
            passenger.fallDistance = 0f;
            function.accept(passenger, seat.x, seat.y - deck.profile().pilotSeatDrop(), seat.z);
            return;
        }

        org.joml.Matrix4f bones = this.boneMatrix;
        Vector3f seat = bones != null ? bones.transformPosition(new Vector3f(RIDER_FEET_IN_BODY)) : new Vector3f(REST_RIDER_FEET_MODEL);

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

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, BASE_HEALTH, BASE_DAMAGE, BASE_SPEED);
            this.setBaseHealth((float) BASE_HEALTH * 1.3f);
            this.setBaseDamage((float) BASE_DAMAGE);
            this.setBaseSpeed((float) BASE_SPEED);
            this.setHealth(this.getMaxHealth());

            this.setVariant(chooseGiraffeVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return data;
    }

    private GiraffeVariant chooseGiraffeVariant() {
        int roll = this.random.nextInt(100);
        if (roll < 5) return GiraffeVariant.GOLDEN;
        if (roll < 20) return GiraffeVariant.RETICULATED;
        return GiraffeVariant.DEFAULT;
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(true);
        createSitAnimation(true);

        boolean napPose = this.isNapping() || (this.isSleeping() && !this.hasSedationAnimations());
        createTransitionAnimation("giraffeNapIn", napInState, napPose, NAP_TRANSITION_TICKS);
        createTransitionAnimation("giraffeNapOut", napOutState, !napPose, NAP_TRANSITION_TICKS);

        this.sentinelAnimationState.animateWhen(isSentinelCasting(), this.tickCount);

        handleIdleGestures();
        setupComboAnimations();
    }

    private void handleIdleGestures() {
        if (idleGestureTicks > 0 && --idleGestureTicks <= 0) this.idleGestureAnimationState.stop();
        if (!canPlayIdleGesture()) {
            if (idleGestureTicks > 0) {
                idleGestureTicks = 0;
                this.idleGestureAnimationState.stop();
            }
            return;
        }
        if (idleGestureTicks > 0 || --idleGestureCooldown > 0) return;

        idleGestureCooldown = IDLE_GESTURE_MIN_DELAY + this.random.nextInt(IDLE_GESTURE_MAX_DELAY - IDLE_GESTURE_MIN_DELAY);
        this.idleGestureAnimationState.start(this.tickCount);
        idleGestureTicks = IDLE_GESTURE_TICKS;
    }

    public boolean canPlayIdleGesture() {
        return this.getTarget() == null && !this.isNapping() && !this.isSitting() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isCombo() && !isBlessing()
                && !isSentinelCasting() && !isKnockedOut();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (37 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (34 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (37 / comboSpeedMultiplier));
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

    public float blessBlend(float partialTick) { return Mth.lerp(partialTick, blessBlendO, blessBlend); }

    public float blessSeconds(float partialTick) { return (blessClientTicks + partialTick) / 20f; }


    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof GiraffeEntity giraffe) {
            giraffe.setVariant(GiraffeVariant.byId(variant));
            giraffe.setInitialVariant(GiraffeVariant.byId(variant));
        }
    }

    public GiraffeVariant getVariant() {
        return GiraffeVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(GiraffeVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
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

    public GiraffeVariant getInitialVariant() {
        return GiraffeVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(GiraffeVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public int getBlessingTick() { return this.entityData.get(BLESSING_TICK); }

    public boolean isBlessing() { return getBlessingTick() > 0; }

    public int getBlessingTargetId() { return this.entityData.get(BLESSING_TARGET); }

    public @Nullable LivingEntity getBlessingTarget() {
        int id = getBlessingTargetId();
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    public int getBlessingCooldown() { return this.entityData.get(BLESSING_COOLDOWN); }

    public int getSentinelCharge() { return this.entityData.get(SENTINEL_CHARGE); }

    public int getSentinelCast() { return this.entityData.get(SENTINEL_CAST); }

    public boolean isSentinelCasting() { return getSentinelCast() > 0; }

    public int getSentinelActive() { return this.entityData.get(SENTINEL_ACTIVE); }

    public boolean isSentinelActive() { return getSentinelActive() > 0; }

    public List<LivingEntity> getSentinelLinkedEntities() {
        String raw = this.entityData.get(SENTINEL_LINKS);
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
        tag.putInt("sentinelCharge", this.getSentinelCharge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.entityData.set(SENTINEL_CHARGE, Mth.clamp(tag.getInt("sentinelCharge"), 0, OWAttacksConstants.Giraffe.SENTINEL_BLESSINGS_REQUIRED));
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final List<OWSedationImpact> SEDATION_IMPACTS = List.of(
            new OWSedationImpact(33, -16f, 0f, 1.0f),
            new OWSedationImpact(36, -48f, -14f, 0.6f)
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

    class GiraffeMeleeAttackGoal extends MeleeAttackGoal {

        public GiraffeMeleeAttackGoal() {
            super(GiraffeEntity.this, 1.4, true);
        }

        @Override
        public boolean canUse() {
            if (GiraffeEntity.this.isSedatedFleeing() || GiraffeEntity.this.isHerdFleeing()
                    || GiraffeEntity.this.isSentinelCasting() || GiraffeEntity.this.isBlessing()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (GiraffeEntity.this.isSedatedFleeing() || GiraffeEntity.this.isHerdFleeing()
                    || GiraffeEntity.this.isSentinelCasting()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            GiraffeEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            GiraffeEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 3.2;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;

            if (!GiraffeEntity.this.isCombo()) {
                GiraffeEntity.this.setCombo(true, 1);
            } else if (GiraffeEntity.this.isPauseCombo()) {
                GiraffeEntity.this.playerContinueCombo = true;
            }
        }
    }
}
