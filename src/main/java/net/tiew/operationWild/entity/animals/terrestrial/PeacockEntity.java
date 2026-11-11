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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.misc.WindGustEntity;
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
    public static final int MAX_SOMNOLENCE = 900;
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
    private static final int SEDATION_WAKE_TICKS = 45;
    private static final float RUN_BLEND_RATE = 0.2f;
    private static final float SIT_BLEND_RATE = 0.1f;
    private static final float REST_BLEND_RATE = 0.07f;
    private static final float LINK_BLEND_IN_RATE = 1f / OWAttacksConstants.Peacock.LINK_START_TICKS;
    private static final float LINK_BLEND_OUT_RATE = 1f / OWAttacksConstants.Peacock.LINK_END_TICKS;
    private static final int IDLE_GESTURE_MIN_DELAY = 300;
    private static final int IDLE_GESTURE_MAX_DELAY = 700;
    private static final DustParticleOptions LINK_DUST = new DustParticleOptions(new Vector3f(0.36f, 1f, 0.54f), 1.1f);
    private static final DustParticleOptions DONOR_DUST = new DustParticleOptions(new Vector3f(0.85f, 0.2f, 0.25f), 0.8f);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_PHASE = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_DONOR = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LINK_RECEIVER = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUST_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWEEP_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWEEP_LINK_TICK = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> SWEEP_LINKS = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.STRING);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState gustAnimationState = new AnimationState();
    public final AnimationState sweepAnimationState = new AnimationState();
    public final AnimationState idleGestureAnimationState = new AnimationState();
    public final AnimationState peckAnimationState = new AnimationState();
    public final AnimationState callAnimationState = new AnimationState();

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
    private float sweepDamagePool = 0f;
    private final Set<Integer> sweepHits = new HashSet<>();

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private float linkBlend, linkBlendO;

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
        this.goalSelector.addGoal(2, new PeacockMeleeAttackGoal());
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
        builder.define(GUST_TICK, 0);
        builder.define(SWEEP_TICK, 0);
        builder.define(SWEEP_LINK_TICK, 0);
        builder.define(SWEEP_LINKS, "");
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
        return !isLinking() && !isGusting() && !isSweeping();
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
        return 0.25f;
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
            setMadByRider(this.isCombo() || this.isSweeping() || this.isGusting());
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.level() instanceof ServerLevel serverLevel) {
            tickLink(serverLevel);
            tickGust(serverLevel);
            tickSweep(serverLevel);
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
        return isGusting() ? speed * 0.35f : speed;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) this.setSitting(false);
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide() && isLinking() && amount >= OWAttacksConstants.Peacock.LINK_BREAK_DAMAGE) {
            breakLink(true);
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
            this.entityData.set(GUST_TICK, 0);
            this.entityData.set(SWEEP_TICK, 0);
        }
        super.die(damageSource);
        if (!this.level().isClientSide() && this.isSaddled()) this.spawnAtLocation(acceptSaddle());
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isSweeping();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isSweeping();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isLinking() && !isSweeping() && !isGusting();
    }

    @Override
    public boolean canUseUltimate() {
        return super.canUseUltimate() && !isGusting();
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof PeacockEntity other && other.isBaby() && !other.isTame() && !this.isTame()) return true;
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

    public void breakLinkByRider() {
        if (getLinkPhase() == LINK_NONE) return;
        breakLink(true);
    }

    private void breakLink(boolean withCooldown) {
        if (this.level().isClientSide()) return;
        boolean wasActive = getLinkPhase() == LINK_ACTIVE;
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

        if (this.getControllingPassenger() == null || isKnockedOut() || isSleeping() || !this.isAlive()) {
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
        if (available <= 0.05f || missing <= 0.05f) return;

        float amplification = OWAttacksConstants.Peacock.LINK_AMPLIFICATION;
        float taken = Math.min(donor.getMaxHealth() * OWAttacksConstants.Peacock.LINK_TRANSFER_RATIO, available);
        float given = Math.min(taken * amplification, missing);
        taken = given / amplification;

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

    public boolean performWingGust() {
        if (this.level().isClientSide()) return false;
        if (isGusting() || isSweeping() || isLinking() || isCombo()) return false;

        float cost = OWAttacksConstants.Peacock.GUST_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        this.entityData.set(GUST_TICK, OWAttacksConstants.Peacock.GUST_ANIMATION_TICKS);
        playSoundAt(this, OWSounds.PEACOCK_FAN.get(), 0.8f, 1.3f);
        return true;
    }

    private void tickGust(ServerLevel level) {
        int tick = getGustTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Peacock.GUST_ANIMATION_TICKS - tick;
        this.entityData.set(GUST_TICK, tick - 1);
        if (elapsed != OWAttacksConstants.Peacock.GUST_RELEASE_TICK) return;

        float yaw = this.getYHeadRot();
        Vec3 forward = Vec3.directionFromRotation(0f, yaw);
        Vec3 origin = this.position().add(forward.scale(0.8 * this.getScale())).add(0, 0.2, 0);
        WindGustEntity.launch(level, this, origin, yaw);

        playSoundAt(this, OWSounds.PEACOCK_WING_GUST.get(), 1.4f, (float) OWUtils.generateRandomInterval(0.95, 1.1));
        level.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y + 0.5, origin.z, 18, 0.6, 0.4, 0.6, 0.12);
    }

    public boolean activateRoyalSweep() {
        if (this.level().isClientSide()) return false;
        if (isSweeping() || isGusting()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Peacock.SWEEP_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Peacock.SWEEP_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);
        breakLink(false);

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

        linkBlendO = linkBlend;
        linkBlend = approach(linkBlend, isLinking() ? 1f : 0f, isLinking() ? LINK_BLEND_IN_RATE : LINK_BLEND_OUT_RATE);
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
        return this.getBbHeight() * 0.5 * this.getScale();
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

        this.gustAnimationState.animateWhen(isGusting(), this.tickCount);
        this.sweepAnimationState.animateWhen(isSweeping(), this.tickCount);

        handleIdleGestures();
        setupComboAnimations();
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
                && !isGusting() && !isSweeping() && !isKnockedOut();
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

    public int getGustTick() { return this.entityData.get(GUST_TICK); }

    public boolean isGusting() { return getGustTick() > 0; }

    public int getSweepTick() { return this.entityData.get(SWEEP_TICK); }

    public boolean isSweeping() { return getSweepTick() > 0; }

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
}
