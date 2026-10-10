package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.LionVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class LionEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 140.0;
    public static final int MAX_SOMNOLENCE = 2200;
    public static final int SOMNOLENCE_LOSS_INTERVAL = 1;
    public static final int SOMNOLENCE_LOSS_INTERVAL_ASLEEP = 2;
    public static final int FOOD_WANTED_MIN = 8;
    public static final int FOOD_WANTED_MAX = 13;
    public static final int ENTITY_COLOR = 0xCFAA70;
    public static final int DEFAULT_SKIN_INDEX = 1;

    public static final double MALE_HEALTH = 32.0;
    public static final double MALE_DAMAGE = 7.0;
    public static final double MALE_SPEED = 0.20;
    public static final double FEMALE_HEALTH = 28.0;
    public static final double FEMALE_DAMAGE = 8.0;
    public static final double FEMALE_SPEED = 0.21;
    public static final int MALE_ONE_IN = 3;
    public static final int ALBINO_PERCENT = 3;
    public static final float FEMALE_SCALE = 0.92f;

    private static final ResourceLocation PACK_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_pack_health");
    private static final ResourceLocation PACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_pack_damage");
    private static final int TELEPORT_ATTEMPTS = 12;
    private static final double CALL_AIM_COSINE = 0.97;
    private static final int WAVE_PARTICLES = 40;
    private static final int CALLABLE_REFRESH_TICKS = 10;
    private static final float RUN_BLEND_RATE = 0.18f;
    private static final float SIT_BLEND_RATE = 0.1f;
    private static final float REST_BLEND_RATE = 0.07f;
    private static final float ROAR_BLEND_IN_RATE = 0.25f;
    private static final float ROAR_BLEND_OUT_RATE = 0.12f;
    private static final float EMPOWER_BLEND_RATE = 0.08f;
    private static final int EAR_FLICK_TICKS = 6;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROAR_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> CLAN_ID = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CLAN_COLOR = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLAN_SIZE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CALL_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARRIVAL_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RESPOND_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RALLY_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMPOWER_TICK = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState roarAnimationState = new AnimationState();
    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState arrivalAnimationState = new AnimationState();
    public final AnimationState respondAnimationState = new AnimationState();

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private int clanCheckCooldown = 0;
    private int packNearbyLionesses = 0;
    private int packLionessCount = -1;

    private record PendingArrival(int lionessId, int tick, int slot) {}

    private final List<PendingArrival> pendingArrivals = new ArrayList<>();
    private int callTargetId = -1;

    private int clientCallableClanmates = 0;
    private int lastRoarElapsed = -1;
    private int lastCallElapsed = -1;

    private float runBlend, runBlendO;
    private float sitBlend, sitBlendO;
    private float restBlend, restBlendO;
    private float roarBlend, roarBlendO;
    private float empowerBlend, empowerBlendO;
    private int earFlickTicks = 0;
    private int earFlickCooldown = 120;
    private boolean earFlickLeft = false;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public LionEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.205D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.JUMP_STRENGTH, 0.9);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(2, new LionMeleeAttackGoal());

        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true));

        this.goalSelector.addGoal(7, new LionessStayWithPrideGoal());

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
        builder.define(ROAR_TICK, 0);
        builder.define(CLAN_ID, Optional.empty());
        builder.define(CLAN_COLOR, -1);
        builder.define(CLAN_SIZE, 0);
        builder.define(CALL_TICK, 0);
        builder.define(ARRIVAL_TICK, 0);
        builder.define(RESPOND_TICK, 0);
        builder.define(RALLY_TICK, 0);
        builder.define(EMPOWER_TICK, 0);
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
        return 7.5f;
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
        return this.isMale() ? 5.5f : 6.5f;
    }

    @Override
    public int asleepSomnolenceLossInterval() {
        return SOMNOLENCE_LOSS_INTERVAL_ASLEEP;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return this.isMale() ? OWEntityConfig.Archetypes.MARAUDER : OWEntityConfig.Archetypes.ASSASSIN;
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
    public float vehicleRunSpeedMultiplier() {
        return this.isMale() ? 4.75f : 5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return this.isMale() ? 2.2f : 2.3f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 3f;
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
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.LION_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return this.isMale() ? 320f : 280f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return (this.isMale() ? 0.95f : 1.2f) * (1 + ((float) this.getLevel() / 50));
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
    public float getRotationSpeed() {
        if (isRoaring()) return 0f;
        if (isCalling()) return 0.12f;
        return this.isMale() ? 0.3f : 0.33f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.LION.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.LION_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getSecondaryCooldownDuration() {
        return OWAttacksConstants.Lion.CLAN_CALL_COOLDOWN_TICKS;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return RANDOM(2) ? OWSounds.TIGER_IDLE.get() : RANDOM(2) ? OWSounds.TIGER_IDLE_2.get() : OWSounds.TIGER_IDLE_3.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.TIGER_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.TIGER_HURT.get();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isMale() ? 0.8f : 1.0f);
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
        playStepSoundFromAnimation(this.isMale() ? 0.85f : 0.95f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(this.isMale() ? 1.05f : 1.15f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        createCombo(16, 10, OWSounds.TIGER_HURTING.get(), 3.0, 3.5, 1.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 2 : 0);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isRoaring() || this.isCalling());
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (this.level() instanceof ServerLevel serverLevel) {
            tickRoar(serverLevel);
            tickCall(serverLevel);
            tickGestureCounters();
            tickClan(serverLevel);
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
        double y = this.getY() + this.getBbHeight() * 0.55;
        double z = this.getZ() + forward.z * reach;
        boolean pounce = this.getComboAttack() == 3;

        if (pounce) {
            level.sendParticles(ParticleTypes.CRIT, x, y, z, 16, 0.35, 0.3, 0.35, 0.35);
            level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0f, 0.7f);
        } else {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0, 0, 0, 0);
            level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.8f,
                    (float) OWUtils.generateRandomInterval(1.15, 1.35));
        }

        BlockState ground = level.getBlockState(BlockPos.containing(x, this.getY() - 0.2, z));
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                x, this.getY() + 0.05, z, pounce ? 18 : 8, 0.35, 0.02, 0.35, 0.08);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isSedatedFleeing() || isNapping())) return;
        if (target != null && isClanmate(target)) return;

        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }

        super.setTarget(target);
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        return isRoaring() || isCalling() ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.isTame() && this.isSitting()) {
            this.setSitting(false);
        }

        float received = amount;
        if (this.isMale() && packNearbyLionesses > 0) {
            received *= 1f - OWAttacksConstants.Lion.PACK_BONUS_PER_LIONESS * packNearbyLionesses;
        }

        boolean hurt = super.hurt(damageSource, received);

        if (hurt && !this.level().isClientSide() && this.isMale() && damageSource.getEntity() instanceof LivingEntity attacker) {
            rallyClanAgainst(attacker);
        }
        return hurt;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Lion.ROAR_KILLS_REQUIRED) setUltimateKillCount(kills + 1);

        if (target instanceof LionEntity rival) claimDefeatedClan(level, rival);
        return super.killedEntity(level, target);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.entityData.set(ROAR_TICK, 0);
            this.entityData.set(CALL_TICK, 0);
            this.pendingArrivals.clear();
        }
        super.die(damageSource);

        if (this.level() instanceof ServerLevel serverLevel) {
            leaveClan(serverLevel);
            if (this.isSaddled()) this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && this.level() instanceof ServerLevel serverLevel) leaveClan(serverLevel);
        super.remove(reason);
    }

    @Override
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
            this.clanCheckCooldown = 0;
        }
    }

    @Override
    public void setTameForAbsentOwner(UUID ownerId) {
        super.setTameForAbsentOwner(ownerId);
        this.setMad(false);
        this.clanCheckCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isRoaring() || isCalling();
    }

    @Override
    public boolean isAttackLocked() {
        return super.isAttackLocked() || isRoaring() || isCalling();
    }

    @Override
    public boolean canStartCombo() {
        return super.canStartCombo() && !isRoaring() && !isCalling();
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LionEntity other) {
            if (other.isBaby() && !other.isTame() && !this.isTame()) return true;
            if (isClanmate(other)) return true;
        }
        return super.isAlliedTo(entity);
    }

    private void tickClan(ServerLevel level) {
        if (clanCheckCooldown-- > 0) return;
        clanCheckCooldown = OWAttacksConstants.Lion.CLAN_CHECK_INTERVAL;

        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(getClanId());

        if (clan != null && (!data.isMember(clan.id, this.getUUID()) || clan.tamed != this.isTame())) {
            data.leave(this.getUUID());
            clan = null;
        } else if (clan == null && getClanId() != null) {
            data.leave(this.getUUID());
        }

        if (clan == null && !this.isBaby()) {
            clan = this.isMale() ? data.found(this.getUUID(), this.isTame(), this.random) : findClanToJoin(level, data);
        }

        setClan(clan);
        if (this.isMale()) updatePackStrength(level, clan);
    }

    @Nullable
    private LionClanData.Clan findClanToJoin(ServerLevel level, LionClanData data) {
        double radius = OWAttacksConstants.Lion.CLAN_JOIN_RADIUS;
        List<LionEntity> leaders = level.getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(radius),
                lion -> lion != this && lion.isAlive() && lion.isMale() && !lion.isBaby()
                        && lion.isTame() == this.isTame() && lion.getClanId() != null
                        && (!this.isTame() || (this.getOwnerUUID() != null && this.getOwnerUUID().equals(lion.getOwnerUUID()))));
        leaders.sort(Comparator.comparingDouble(this::distanceToSqr));

        for (LionEntity leader : leaders) {
            LionClanData.Clan clan = data.getClan(leader.getClanId());
            if (clan == null || clan.tamed != this.isTame() || clan.isFull()) continue;
            if (data.join(clan.id, this.getUUID())) return clan;
        }
        return null;
    }

    private void leaveClan(ServerLevel level) {
        LionClanData.get(level.getServer()).leave(this.getUUID());
        setClan(null);
    }

    private void setClan(@Nullable LionClanData.Clan clan) {
        Optional<UUID> id = clan == null ? Optional.empty() : Optional.of(clan.id);
        if (!this.entityData.get(CLAN_ID).equals(id)) this.entityData.set(CLAN_ID, id);
        this.entityData.set(CLAN_COLOR, clan == null ? -1 : clan.color);
        this.entityData.set(CLAN_SIZE, clan == null ? 0 : clan.size());
    }

    private void claimDefeatedClan(ServerLevel level, LionEntity rival) {
        if (!this.isMale() || !rival.isMale() || this.isTame() || rival.isTame()) return;
        UUID mine = getClanId();
        UUID theirs = rival.getClanId();
        if (mine == null || theirs == null || mine.equals(theirs)) return;

        LionClanData data = LionClanData.get(level.getServer());
        if (data.absorb(mine, theirs) <= 0) return;

        LionClanData.Clan clan = data.getClan(mine);
        for (LionEntity lioness : level.getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(OWAttacksConstants.Lion.CLAN_DEFENSE_RADIUS),
                lion -> theirs.equals(lion.getClanId()))) {
            lioness.setClan(clan != null && data.isMember(clan.id, lioness.getUUID()) ? clan : null);
            if (lioness.getTarget() == this) lioness.setTarget(null);
        }
        setClan(clan);
    }

    private void rallyClanAgainst(LivingEntity attacker) {
        if (getClanId() == null || attacker == this || isClanmate(attacker)) return;
        if (this.isTame() && (attacker == this.getOwner() || this.isAlliedTo(attacker))) return;
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) return;

        for (LionEntity lioness : clanLionessesWithin(OWAttacksConstants.Lion.CLAN_DEFENSE_RADIUS)) {
            if (lioness.getTarget() != null || lioness.isKnockedOut() || lioness.isVehicle()) continue;
            if (lioness.isAlliedTo(attacker) || attacker == lioness.getOwner()) continue;
            lioness.setTarget(attacker);
        }
    }

    private void updatePackStrength(ServerLevel level, @Nullable LionClanData.Clan clan) {
        int lionesses = clan == null ? 0 : clan.lionesses.size();
        if (lionesses != packLionessCount) {
            packLionessCount = lionesses;
            applyModifier(Attributes.MAX_HEALTH, PACK_HEALTH_MODIFIER,
                    OWAttacksConstants.Lion.PACK_HEALTH_PER_LIONESS * lionesses, AttributeModifier.Operation.ADD_VALUE);
            if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
        }

        int nearby = Math.min(clanLionessesWithin(OWAttacksConstants.Lion.PACK_NEARBY_RADIUS).size(),
                OWAttacksConstants.Lion.PACK_BONUS_MAX_LIONESSES);
        if (nearby != packNearbyLionesses) {
            packNearbyLionesses = nearby;
            applyModifier(Attributes.ATTACK_DAMAGE, PACK_DAMAGE_MODIFIER,
                    OWAttacksConstants.Lion.PACK_BONUS_PER_LIONESS * nearby, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    }

    private void applyModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                               ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    public List<LionEntity> clanLionessesWithin(double radius) {
        UUID clanId = getClanId();
        if (clanId == null) return List.of();
        return this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(radius),
                lion -> lion != this && lion.isAlive() && lion.isFemale() && clanId.equals(lion.getClanId())
                        && lion.distanceToSqr(this) <= radius * radius);
    }

    public boolean isClanmate(Entity entity) {
        UUID clanId = getClanId();
        return clanId != null && entity != this && entity instanceof LionEntity lion && clanId.equals(lion.getClanId());
    }

    @Nullable
    private LionEntity findClanMale() {
        if (!(this.level() instanceof ServerLevel level) || getClanId() == null) return null;
        LionClanData.Clan clan = LionClanData.get(level.getServer()).getClan(getClanId());
        if (clan == null || clan.male.equals(this.getUUID())) return null;
        return level.getEntity(clan.male) instanceof LionEntity male && male.isAlive() ? male : null;
    }

    public boolean hasCallableClanmates() {
        return getCallableClanmateCount() > 0;
    }

    public int getCallableClanmateCount() {
        return this.level().isClientSide() ? clientCallableClanmates : callableClanmates().size();
    }

    private List<LionEntity> callableClanmates() {
        List<LionEntity> lionesses = clanLionessesWithin(OWAttacksConstants.Lion.CLAN_CALL_RADIUS).stream()
                .filter(lion -> lion.isTame() && !lion.isKnockedOut() && !lion.isVehicle() && !lion.isPassenger())
                .sorted(Comparator.comparingDouble(this::distanceToSqr))
                .toList();
        return lionesses.subList(0, Math.min(lionesses.size(), OWAttacksConstants.Lion.CLAN_CALL_MAX_LIONESSES));
    }

    public void performClanCall() {
        if (this.level().isClientSide()) return;

        List<LionEntity> called = callableClanmates();
        if (isSecondaryOnCooldown() || isCalling() || isRoaring() || called.isEmpty()) {
            rejectAttack(OWAttackIds.CLAN_CALL);
            return;
        }

        float cost = OWAttacksConstants.Lion.CLAN_CALL_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            rejectAttack(OWAttackIds.CLAN_CALL);
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        startSecondaryCooldown();

        LivingEntity target = resolveCallTarget();
        callTargetId = target == null ? -1 : target.getId();

        pendingArrivals.clear();
        for (int i = 0; i < called.size(); i++) {
            int tick = OWAttacksConstants.Lion.CLAN_CALL_FIRST_ARRIVAL_TICK + i * OWAttacksConstants.Lion.CLAN_CALL_ARRIVAL_SPACING;
            pendingArrivals.add(new PendingArrival(called.get(i).getId(), tick, i));
        }

        resetCombo(0);
        actualAttackNumber = 0;
        this.getNavigation().stop();
        this.entityData.set(CALL_TICK, OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_BREATHE,
                SoundSource.NEUTRAL, 1.4f, this.isMale() ? 0.55f : 0.7f);
    }

    private void tickCall(ServerLevel level) {
        int tick = getCallTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS - tick;
        this.entityData.set(CALL_TICK, tick - 1);

        if (elapsed == OWAttacksConstants.Lion.CLAN_CALL_SHOUT_TICK) callShout(level);
        if (elapsed == OWAttacksConstants.Lion.CLAN_CALL_STOMP_TICK) groundSlam(level, 1.6, 14);

        LivingEntity target = level.getEntity(callTargetId) instanceof LivingEntity living && living.isAlive() ? living : null;
        for (Iterator<PendingArrival> iterator = pendingArrivals.iterator(); iterator.hasNext(); ) {
            PendingArrival arrival = iterator.next();
            if (arrival.tick() != elapsed) continue;
            iterator.remove();
            if (level.getEntity(arrival.lionessId()) instanceof LionEntity lioness && lioness.isAlive() && isClanmate(lioness)) {
                summonLioness(level, lioness, arrival.slot(), target);
            }
        }

        if (tick - 1 <= 0) {
            pendingArrivals.clear();
            callTargetId = -1;
        }
    }

    private void callShout(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 2.5f, this.isMale() ? 0.85f : 1.05f);

        Vec3 mouth = mouthPosition();
        level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 6, 0.1, 0.1, 0.1, 0.05);
        emitWave(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS, ParticleTypes.CLOUD, 0.3);
        emitRing(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS * 0.45, clanDust(2.0f));
        emitRing(level, OWAttacksConstants.Lion.CLAN_CALL_WAVE_RADIUS * 0.8, clanDust(1.6f));
    }

    private void summonLioness(ServerLevel level, LionEntity lioness, int index, @Nullable LivingEntity target) {
        level.sendParticles(ParticleTypes.POOF, lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.5, lioness.getZ(),
                16, 0.4, 0.4, 0.4, 0.03);
        level.sendParticles(clanDust(1.4f), lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.5, lioness.getZ(),
                12, 0.4, 0.5, 0.4, 0.0);
        level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.NEUTRAL, 0.9f, 0.6f);

        boolean moved = lioness.distanceToSqr(this) >= 16.0 && teleportBeside(lioness, index);
        if (moved) {
            BlockState ground = lioness.getBlockStateOn();
            if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    lioness.getX(), lioness.getY() + 0.1, lioness.getZ(), 26, 0.6, 0.05, 0.6, 0.2);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, lioness.getX(), lioness.getY() + 0.2, lioness.getZ(),
                    6, 0.5, 0.05, 0.5, 0.01);
            level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), OWSounds.TIGER_JUMP.get(),
                    SoundSource.NEUTRAL, 1.2f, (float) OWUtils.generateRandomInterval(1.15, 1.3));
            lioness.entityData.set(ARRIVAL_TICK, OWAttacksConstants.Lion.ARRIVAL_TICKS);
        }

        float yaw = target != null
                ? (float) (Mth.atan2(target.getZ() - lioness.getZ(), target.getX() - lioness.getX()) * Mth.RAD_TO_DEG) - 90f
                : this.yBodyRot;
        lioness.setYRot(yaw);
        lioness.yBodyRot = yaw;
        lioness.setYHeadRot(yaw);

        lioness.answerClanCall(target);
    }

    private void answerClanCall(@Nullable LivingEntity target) {
        int duration = OWAttacksConstants.Lion.CLAN_CALL_DURATION_TICKS;
        this.setSitting(false);
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0));
        this.entityData.set(RALLY_TICK, duration);
        if (target != null && target.isAlive() && !this.isAlliedTo(target) && target != this.getOwner()) {
            this.setTarget(target);
        }
    }

    private boolean teleportBeside(LionEntity lioness, int index) {
        double[][] slots = {{-2.6, 0.8}, {2.6, 0.8}, {0.0, -3.0}};
        double[] slot = slots[index % slots.length];
        Vec3 preferred = new Vec3(slot[0], 0, slot[1]).scale(this.getScale())
                .yRot((float) Math.toRadians(-this.yBodyRot));

        if (lioness.randomTeleport(this.getX() + preferred.x, this.getY() + 1.0, this.getZ() + preferred.z, false)) {
            lioness.getNavigation().stop();
            return true;
        }

        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = 2.5 + this.random.nextDouble() * 2.0;
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            if (lioness.randomTeleport(x, this.getY() + 1.0, z, false)) {
                lioness.getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    @Nullable
    private LivingEntity resolveCallTarget() {
        if (this.getControllingPassenger() instanceof Player rider) {
            Vec3 eye = rider.getEyePosition();
            Vec3 look = rider.getLookAngle();
            double range = OWAttacksConstants.Lion.CLAN_CALL_TARGET_RANGE;
            LivingEntity best = null;
            double bestCosine = CALL_AIM_COSINE;
            for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
                if (!isValidCallTarget(candidate, rider)) continue;
                Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye);
                double distance = toTarget.length();
                if (distance < 1.0E-3 || distance > range) continue;
                double cosine = toTarget.scale(1.0 / distance).dot(look);
                if (cosine > bestCosine && rider.hasLineOfSight(candidate)) {
                    bestCosine = cosine;
                    best = candidate;
                }
            }
            if (best != null) return best;
        }

        int window = OWAttacksConstants.Lion.CLAN_CALL_LAST_HIT_TICKS;
        LivingEntity lastHit = this.getLastHurtMob();
        if (lastHit != null && lastHit.isAlive() && this.tickCount - this.getLastHurtMobTimestamp() < window) return lastHit;
        LivingEntity lastAttacker = this.getLastHurtByMob();
        if (lastAttacker != null && lastAttacker.isAlive() && this.tickCount - this.getLastHurtByMobTimestamp() < window) return lastAttacker;
        return null;
    }

    private boolean isValidCallTarget(LivingEntity candidate, Player rider) {
        if (candidate == this || candidate == rider || !candidate.isAlive()) return false;
        if (candidate.getRootVehicle() == this || isClanmate(candidate)) return false;
        if (candidate == this.getOwner() || this.isAlliedTo(candidate)) return false;
        return !(candidate instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    public boolean activateDominationRoar() {
        if (this.level().isClientSide()) return false;
        if (isRoaring() || isCalling()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Lion.ROAR_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Lion.ROAR_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        resetCombo(0);
        actualAttackNumber = 0;
        this.entityData.set(ROAR_TICK, OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS);
        if (!(this.getControllingPassenger() instanceof Player)) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_BREATHE,
                SoundSource.NEUTRAL, 1.6f, this.isMale() ? 0.5f : 0.65f);
        return true;
    }

    private void tickRoar(ServerLevel level) {
        int tick = getRoarTick();
        if (tick <= 0) return;

        int elapsed = OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS - tick;
        this.entityData.set(ROAR_TICK, tick - 1);

        if (elapsed == OWAttacksConstants.Lion.ROAR_SHOUT_TICK) roarOfDomination(level);
        if (elapsed == OWAttacksConstants.Lion.ROAR_STOMP_TICK) groundSlam(level, 1.4, 20);

        int[] pulses = OWAttacksConstants.Lion.ROAR_PULSE_TICKS;
        for (int i = 0; i < pulses.length; i++) {
            if (pulses[i] != elapsed) continue;
            float strength = 1f - (float) i / pulses.length;
            double radius = OWAttacksConstants.Lion.ROAR_WAVE_RADIUS * (0.55 + 0.45 * strength);
            emitWave(level, radius, ParticleTypes.CLOUD, 0.35 + 0.25 * strength);
            emitRing(level, 1.5 + i * 1.6, roarDust(1.3f + strength));
        }
    }

    private void roarOfDomination(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, 3.0f, this.isMale() ? 0.8f : 1.0f);
        OWUtils.spawnServerParticles(this, ParticleTypes.FLASH, 0, 0.5, 0, 1, 0);

        Vec3 mouth = mouthPosition();
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        for (int i = 0; i < 18; i++) {
            double spread = (this.random.nextDouble() - 0.5) * 0.6;
            Vec3 direction = forward.yRot((float) spread).add(0, 0.05 + this.random.nextDouble() * 0.15, 0);
            level.sendParticles(ParticleTypes.CLOUD, mouth.x, mouth.y, mouth.z, 0,
                    direction.x, direction.y, direction.z, 0.35 + this.random.nextDouble() * 0.25);
        }

        int duration = OWAttacksConstants.Lion.ROAR_BUFF_TICKS;
        List<LionEntity> pride = new ArrayList<>(clanLionessesWithin(OWAttacksConstants.Lion.ROAR_RADIUS));
        if (this.isFemale()) pride.add(this);

        for (LionEntity lioness : pride) {
            lioness.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, OWAttacksConstants.Lion.ROAR_SPEED_AMPLIFIER));
            lioness.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, OWAttacksConstants.Lion.ROAR_STRENGTH_AMPLIFIER));
            lioness.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, OWAttacksConstants.Lion.ROAR_REGENERATION_AMPLIFIER));
            lioness.entityData.set(EMPOWER_TICK, duration);
            if (lioness == this) continue;

            lioness.entityData.set(RESPOND_TICK, OWAttacksConstants.Lion.RESPOND_TICKS);
            level.sendParticles(ParticleTypes.WAX_ON, lioness.getX(), lioness.getY() + lioness.getBbHeight() * 0.6, lioness.getZ(),
                    14, 0.45, 0.4, 0.45, 0.6);
            level.playSound(null, lioness.getX(), lioness.getY(), lioness.getZ(), OWSounds.TIGER_ROAR.get(),
                    SoundSource.NEUTRAL, 1.1f, (float) OWUtils.generateRandomInterval(1.2, 1.4));
        }
    }

    private void groundSlam(ServerLevel level, double reach, int count) {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        double x = this.getX() + forward.x * reach * this.getScale();
        double z = this.getZ() + forward.z * reach * this.getScale();

        BlockState ground = level.getBlockState(BlockPos.containing(x, this.getY() - 0.2, z));
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, this.getY() + 0.05, z,
                count, 0.6, 0.05, 0.6, 0.18);
        SoundType soundType = ground.getSoundType(level, BlockPos.containing(x, this.getY() - 0.2, z), this);
        level.playSound(null, x, this.getY(), z, soundType.getStepSound(), SoundSource.NEUTRAL,
                soundType.getVolume() * 1.4f, soundType.getPitch() * 0.55f);
    }

    private void emitWave(ServerLevel level, double radius, ParticleOptions particle, double speed) {
        double y = this.getY() + 0.15;
        for (int i = 0; i < WAVE_PARTICLES; i++) {
            double angle = i * (Math.PI * 2.0 / WAVE_PARTICLES);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.sendParticles(particle, this.getX() + dx * 0.8, y, this.getZ() + dz * 0.8, 0,
                    dx, 0.02, dz, speed * radius / 5.0);
        }
    }

    private void emitRing(ServerLevel level, double radius, ParticleOptions particle) {
        double y = this.getY() + 0.2;
        int count = Math.max(16, (int) (radius * 10));
        for (int i = 0; i < count; i++) {
            double angle = i * (Math.PI * 2.0 / count);
            level.sendParticles(particle, this.getX() + Math.cos(angle) * radius, y, this.getZ() + Math.sin(angle) * radius,
                    1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private DustParticleOptions clanDust(float size) {
        int color = getClanColor() >= 0 ? getClanColor() : ENTITY_COLOR;
        return new DustParticleOptions(new Vector3f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f), size);
    }

    private static DustParticleOptions roarDust(float size) {
        return new DustParticleOptions(new Vector3f(0.95f, 0.72f, 0.28f), size);
    }

    private Vec3 mouthPosition() {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        double reach = 1.35 * this.getScale();
        return new Vec3(this.getX() + forward.x * reach, this.getY() + 1.0 * this.getScale(), this.getZ() + forward.z * reach);
    }

    private void tickGestureCounters() {
        decrement(ARRIVAL_TICK);
        decrement(RESPOND_TICK);
        decrement(RALLY_TICK);
        decrement(EMPOWER_TICK);
    }

    private void decrement(EntityDataAccessor<Integer> accessor) {
        int value = this.entityData.get(accessor);
        if (value > 0) this.entityData.set(accessor, value - 1);
    }

    private void tickClientEffects() {
        tickAnimationBlends();

        if (this.isVehicle() && this.tickCount % CALLABLE_REFRESH_TICKS == 0) {
            clientCallableClanmates = callableClanmates().size();
        }

        int roarElapsed = isRoaring() ? OWAttacksConstants.Lion.ROAR_ANIMATION_TICKS - getRoarTick() : -1;
        if (crossed(lastRoarElapsed, roarElapsed, OWAttacksConstants.Lion.ROAR_SHOUT_TICK)
                || crossed(lastRoarElapsed, roarElapsed, OWAttacksConstants.Lion.ROAR_STOMP_TICK)) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        lastRoarElapsed = roarElapsed;

        int callElapsed = isCalling() ? OWAttacksConstants.Lion.CLAN_CALL_GESTURE_TICKS - getCallTick() : -1;
        if (crossed(lastCallElapsed, callElapsed, OWAttacksConstants.Lion.CLAN_CALL_STOMP_TICK)) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
        }
        lastCallElapsed = callElapsed;

        if (isEmpowered() && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.WAX_ON,
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * (0.3 + this.random.nextDouble() * 0.6),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0, 0.02, 0);
        }

        if (isRallied() && this.tickCount % 4 == 0) {
            this.level().addParticle(clanDust(0.9f),
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * (0.2 + this.random.nextDouble() * 0.7),
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0, 0.01, 0);
        }

        if ((isEmpowered() || isRallied()) && this.isRunning() && this.onGround() && this.tickCount % 2 == 0) {
            BlockState ground = this.getBlockStateOn();
            if (!ground.isAir()) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                        this.getX(), this.getY() + 0.1, this.getZ(), 0, 0.05, 0);
            }
        }
    }

    private static boolean crossed(int previous, int current, int mark) {
        return previous < mark && current >= mark;
    }

    private void tickAnimationBlends() {
        runBlendO = runBlend;
        runBlend = approach(runBlend, this.isRunning() || this.getState() == 2 ? 1f : 0f, RUN_BLEND_RATE);

        sitBlendO = sitBlend;
        sitBlend = approach(sitBlend, this.isSitting() ? 1f : 0f, SIT_BLEND_RATE);

        restBlendO = restBlend;
        restBlend = approach(restBlend, this.isNapping() || this.isSleeping() ? 1f : 0f,
                this.isKnockedOut() ? REST_BLEND_RATE * 2f : REST_BLEND_RATE);

        roarBlendO = roarBlend;
        roarBlend = approach(roarBlend, isRoaring() ? 1f : 0f, isRoaring() ? ROAR_BLEND_IN_RATE : ROAR_BLEND_OUT_RATE);

        empowerBlendO = empowerBlend;
        empowerBlend = approach(empowerBlend, isEmpowered() || isRallied() ? 1f : 0f, EMPOWER_BLEND_RATE);

        if (earFlickTicks > 0) {
            earFlickTicks--;
        } else if (--earFlickCooldown <= 0) {
            earFlickTicks = EAR_FLICK_TICKS;
            earFlickLeft = this.random.nextBoolean();
            earFlickCooldown = 80 + this.random.nextInt(160);
        }
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    public float runBlend(float partialTick) { return Mth.lerp(partialTick, runBlendO, runBlend); }

    public float sitBlend(float partialTick) { return Mth.lerp(partialTick, sitBlendO, sitBlend); }

    public float restBlend(float partialTick) { return Mth.lerp(partialTick, restBlendO, restBlend); }

    public float roarBlend(float partialTick) { return Mth.lerp(partialTick, roarBlendO, roarBlend); }

    public float empowerBlend(float partialTick) { return Mth.lerp(partialTick, empowerBlendO, empowerBlend); }

    public float earFlick(float partialTick) {
        if (earFlickTicks <= 0) return 0f;
        float progress = (EAR_FLICK_TICKS - earFlickTicks + partialTick) / EAR_FLICK_TICKS;
        return Mth.sin(Mth.clamp(progress, 0f, 1f) * (float) Math.PI);
    }

    public boolean isEarFlickLeft() { return earFlickLeft; }

    private void rejectAttack(int attackId) {
        if (this.getControllingPassenger() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new OWAttackRejectedPacket(this.getId(), attackId));
        }
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.52 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        Vec3 seatOffset = new Vec3(0, 0, 0).yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setGender(this.random.nextInt(MALE_ONE_IN) == 0 ? 1 : 0);
            if (this.isFemale()) this.setScale(this.getScale() * FEMALE_SCALE);
            applySexAttributes();

            this.setVariant(this.random.nextInt(100) < ALBINO_PERCENT ? LionVariant.ALBINO : LionVariant.DEFAULT);
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = FOOD_WANTED_MIN + this.random.nextInt(FOOD_WANTED_MAX - FOOD_WANTED_MIN + 1);
        return data;
    }

    private void applySexAttributes() {
        double health = this.isMale() ? MALE_HEALTH : FEMALE_HEALTH;
        double damage = this.isMale() ? MALE_DAMAGE : FEMALE_DAMAGE;
        double speed = this.isMale() ? MALE_SPEED : FEMALE_SPEED;

        this.setRandomAttributes(this, health, damage, speed);
        this.setBaseHealth((float) health * 1.3f);
        this.setBaseDamage((float) damage);
        this.setBaseSpeed((float) speed);
        this.setHealth(this.getMaxHealth());
    }

    // ==================================================
    //                    ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(54, true);
        createSitAnimation(84, true);

        this.roarAnimationState.animateWhen(isRoaring(), this.tickCount);
        this.callAnimationState.animateWhen(isCalling(), this.tickCount);
        this.arrivalAnimationState.animateWhen(getArrivalTick() > 0, this.tickCount);
        this.respondAnimationState.animateWhen(getRespondTick() > 0, this.tickCount);

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (33 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (29 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (30 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    // ==================================================
    //                    ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof LionEntity lion) {
            lion.setVariant(LionVariant.byId(variant));
            lion.setInitialVariant(LionVariant.byId(variant));
        }
    }

    public LionVariant getVariant() {
        return LionVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(LionVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(LionVariant skin) {
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

    public LionVariant getInitialVariant() {
        return LionVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(LionVariant variant) {
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

    public int getRoarTick() { return this.entityData.get(ROAR_TICK); }

    public boolean isRoaring() { return getRoarTick() > 0; }

    public int getCallTick() { return this.entityData.get(CALL_TICK); }

    public boolean isCalling() { return getCallTick() > 0; }

    public int getArrivalTick() { return this.entityData.get(ARRIVAL_TICK); }

    public int getRespondTick() { return this.entityData.get(RESPOND_TICK); }

    public boolean isRallied() { return this.entityData.get(RALLY_TICK) > 0; }

    public boolean isEmpowered() { return this.entityData.get(EMPOWER_TICK) > 0; }

    @Nullable
    public UUID getClanId() { return this.entityData.get(CLAN_ID).orElse(null); }

    public boolean hasClan() { return getClanId() != null; }

    public int getClanColor() { return this.entityData.get(CLAN_COLOR); }

    public int getClanSize() { return this.entityData.get(CLAN_SIZE); }

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
        if (getClanId() != null) tag.putUUID("clanId", getClanId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) setUltimateKillCount(tag.getInt("ultimateKillCount"));
        this.entityData.set(CLAN_ID, tag.hasUUID("clanId") ? Optional.of(tag.getUUID("clanId")) : Optional.empty());
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    class LionMeleeAttackGoal extends MeleeAttackGoal {

        public LionMeleeAttackGoal() {
            super(LionEntity.this, 7, true);
        }

        @Override
        public boolean canUse() {
            if (LionEntity.this.isSedatedFleeing() || LionEntity.this.isRoaring()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (LionEntity.this.isSedatedFleeing() || LionEntity.this.isRoaring()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            LionEntity.this.setMad(true);
            LionEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            LionEntity.this.setMad(false);
            LionEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2.5;
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

    class LionessStayWithPrideGoal extends Goal {

        private LionEntity male;
        private int repathCooldown;

        public LionessStayWithPrideGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private double radius() {
            return LionEntity.this.isTame()
                    ? OWAttacksConstants.Lion.CLAN_FIDELITY_RADIUS_TAMED
                    : OWAttacksConstants.Lion.CLAN_FIDELITY_RADIUS_WILD;
        }

        private boolean isFree() {
            LionEntity lioness = LionEntity.this;
            return lioness.isFemale() && !lioness.isBaby() && lioness.hasClan()
                    && lioness.getTarget() == null && !lioness.isSitting() && !lioness.isVehicle()
                    && !lioness.isKnockedOut() && !lioness.isSleeping() && !lioness.isNapping()
                    && !lioness.isLeashed() && (!lioness.isTame() || !lioness.isFollowingOwner());
        }

        @Override
        public boolean canUse() {
            if ((LionEntity.this.tickCount + LionEntity.this.getId()) % 20 != 0) return false;
            if (!isFree()) return false;
            this.male = LionEntity.this.findClanMale();
            return this.male != null && LionEntity.this.distanceToSqr(this.male) > radius() * radius();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.male == null || !this.male.isAlive() || !isFree()) return false;
            double settle = radius() * 0.5;
            return LionEntity.this.distanceToSqr(this.male) > settle * settle;
        }

        @Override
        public void start() {
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            if (--this.repathCooldown > 0 || this.male == null) return;
            this.repathCooldown = this.adjustedTickDelay(10);
            LionEntity.this.getNavigation().moveTo(this.male, 1.0D);
        }

        @Override
        public void stop() {
            this.male = null;
            LionEntity.this.getNavigation().stop();
        }
    }
}
