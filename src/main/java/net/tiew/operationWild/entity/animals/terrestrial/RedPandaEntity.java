package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
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
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWPanicGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.misc.FeastPileEntity;
import net.tiew.operationWild.entity.misc.HealSnackEntity;
import net.tiew.operationWild.networking.packets.to_client.FeedingPacket;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.entity.variants.RedPandaVariant;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class RedPandaEntity extends OWEntity implements IOWEntity, IOWTamable {

    public static final double TAMING_EXPERIENCE = 40.0;
    public static final int ENTITY_COLOR = 0xec8925;

    private static final double SHOULDER_SIDE_OFFSET = 0.44;
    private static final double SHOULDER_HEIGHT_OFFSET = 1.02;
    private static final double SHOULDER_FORWARD_OFFSET = -0.05;
    private static final double CROUCH_DROP = 0.28;
    private static final double CROUCH_FORWARD = 0.09;
    public static final float CROUCH_PITCH_DEGREES = 9f;
    private static final float CROUCH_BLEND = 0.35f;

    private static final int SHOULDER_TOGGLE_COOLDOWN = 10;
    private static final int NAP_COOLDOWN_AFTER_DISMOUNT = 200;
    private static final int SHOULDER_RESTORE_TIMEOUT = 600;
    private static final double SHOULDER_RESTORE_RANGE = 8.0;

    private static final int MISC_IDLE_DURATION = 54;
    private static final double PAW_SIDE_OFFSET = 0.17;

    private static final ItemStack[] SNACKS = {
            new ItemStack(Items.BAMBOO),
            new ItemStack(net.tiew.operationWild.item.OWItems.SAVAGE_BERRIES.get()),
            new ItemStack(Items.SWEET_BERRIES),
            new ItemStack(Items.GLOW_BERRIES),
            new ItemStack(Items.HONEYCOMB),
            new ItemStack(Items.APPLE),
            new ItemStack(Items.CARROT),
            new ItemStack(Items.POTATO),
            new ItemStack(Items.MELON_SLICE),
    };

    public static ItemStack snackForIndex(int index) {
        return SNACKS[Math.floorMod(index, SNACKS.length)];
    }

    private static final List<Integer> SNACK_BAG = new ArrayList<>();

    private static ItemStack randomSnack() {
        if (SNACK_BAG.isEmpty()) {
            for (int i = 0; i < SNACKS.length; i++) SNACK_BAG.add(i);
            java.util.Collections.shuffle(SNACK_BAG);
        }
        return SNACKS[SNACK_BAG.remove(SNACK_BAG.size() - 1)];
    }

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> THROW_TIMER = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    public static final float HEAL_POWER_BASE = 1.0f;
    public static final float HEAL_POWER_ROLL_MIN = 0.9f;
    public static final float HEAL_POWER_ROLL_MAX = 1.1f;
    public static final int DEFAULT_SKIN_INDEX = 2;

    public static final float HEAL_POWER_MAX = 1.85f;
    public static final float HEAL_POWER_STEP = 0.02f;

    private static final EntityDataAccessor<Float> HEAL_POWER =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CLIMB_TIMER =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMB_MOUNTING =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int CLIMB_TICKS = 20;
    private static final float CLIMB_CROUCH_PHASE = 0.28f;
    private static final double CLIMB_CROUCH_DEPTH = 0.17;
    private static final double CLIMB_LEAP_APEX = 0.62;
    private static final float CLIMB_HESITATION = 0.16f;
    private static final double CLIMB_HOP = 0.20;
    private static final double CLIMB_SPIRAL = 0.28;
    private static final int WAKE_TRANSITION_TICKS = 20;

    private static final EntityDataAccessor<Integer> AURA_TIMER = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEAST_CHARGE = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEAST_CAST_TIMER = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);

    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState throwAnimationState = new AnimationState();
    public final AnimationState feastAnimationState = new AnimationState();
    public final AnimationState shoulderIdleAnimationState = new AnimationState();
    public final AnimationState miscIdleAnimationState = new AnimationState();

    public int miscIdleAnimationStartTime = 0;
    private int lastThrowTimer = 0;
    private int lastFeastCastTimer = 0;
    private int secondThrowDelay = 0;
    private int pendingFeastPortions = 0;

    private int healSnackCooldown = 0;
    private final List<int[]> pendingSnacks = new ArrayList<>();
    private final List<int[]> pendingBites = new ArrayList<>();
    private int shoulderCooldown = 0;
    private int napCooldown = 0;
    private int wakeThenClimbDelay = 0;
    private int wakeThenClimbCarrierId = -1;

    private Vec3 climbStart = null;
    private int climbCarrierId = -1;
    private static final EntityDataAccessor<Float> CLIMB_PITCH =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.FLOAT);

    public float tailGroundPitch = 0f;
    public float tailGroundDrop = 0f;
    public int tailGroundProbeTick = -1;
    public float tailGroundLastAge = Float.NaN;

    public float tailSwing = 0f;
    public float tailSwingVelocity = 0f;
    public float tailLastAge = Float.NaN;

    public float perchPitch = 0f;
    public float perchPitchVelocity = 0f;
    public float perchLandSquash = 0f;
    public float perchSquashVelocity = 0f;
    public float perchLastVerticalSpeed = 0f;

    public float perchLastCarrierYaw = Float.NaN;
    public float perchTurnRate = 0f;
    public float perchRoll = 0f;
    public float perchRunLift = 0f;
    public boolean perchWasAirborne = false;

    public float crouchAmount = 0f;
    public float crouchAmountO = 0f;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);

    private UUID pendingCarrier = null;
    private int pendingCarrierTicks = 0;

    private final List<HealOverTime> healOverTime = new ArrayList<>();

    public RedPandaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    protected double followOwnerSpeedFactor() {
        return 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new OWPanicGoal(this, 2.8f, 1.0, 0) {
            @Override
            protected boolean shouldPanic() {
                return !RedPandaEntity.this.isTame() && RedPandaEntity.this.getLastHurtByMob() != null;
            }
        });
        this.goalSelector.addGoal(5, new net.tiew.operationWild.entity.goals.NapGoal(this, 1f, 800, true, true));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(THROW_TIMER, 0);
        builder.define(AURA_TIMER, 0);
        builder.define(FEAST_CHARGE, 0);
        builder.define(FEAST_CAST_TIMER, 0);
        builder.define(HEAL_POWER, HEAL_POWER_BASE);
        builder.define(CLIMB_TIMER, 0);
        builder.define(CLIMB_MOUNTING, true);
        builder.define(CLIMB_PITCH, 0f);
    }

    public boolean isClimbing() { return this.entityData.get(CLIMB_TIMER) > 0; }

    public boolean isClimbingUp() { return this.entityData.get(CLIMB_MOUNTING); }

    public float climbPitch() {
        return this.entityData.get(CLIMB_PITCH);
    }

    public float climbProgress() {
        return 1f - this.entityData.get(CLIMB_TIMER) / (float) CLIMB_TICKS;
    }

    public float getHealPower() {
        return this.entityData.get(HEAL_POWER);
    }

    public void setHealPower(float power) {
        this.entityData.set(HEAL_POWER, Mth.clamp(power, HEAL_POWER_ROLL_MIN, HEAL_POWER_MAX));
    }

    public boolean upgradeHealPower() {
        float current = getHealPower();
        if (current >= HEAL_POWER_MAX) return false;
        setHealPower(current + HEAL_POWER_STEP);
        return true;
    }

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 4f;
    }

    @Override
    public double napParticleHeight() { return 0.15; }

    @Override
    public double napParticleForward() { return 0.75; }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
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
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.RED_PANDA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 250f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.225f * (1 + ((float) this.getLevel() / 50));
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
    public float getRotationSpeed() {
        return 0.3f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.RED_PANDA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.RED_PANDA_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return RANDOM(2) ? SoundEvents.FOX_AMBIENT : SoundEvents.FOX_SNIFF;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.isPassenger()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 200L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 3; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.08F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(1.35f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.55f);
    }

    @Override
    public void tick() {
        super.tick();

        if (healSnackCooldown > 0) healSnackCooldown--;
        if (shoulderCooldown > 0) shoulderCooldown--;
        if (napCooldown > 0) napCooldown--;

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide()) {
            if (getThrowTimer() > 0) setThrowTimer(getThrowTimer() - 1);

            handleWakeThenClimb();
            handleShoulderClimb();
            handleFeast();
            handleFeastCast();
            handleSecondThrow();
            handlePendingSnacks();
            handlePendingBites();
            handleSnackCharge();
            handleHealOverTime();
            handleShoulderRestore();

            if (isOnShoulder() && (!this.isTame() || this.isInResurrection())) dismountFromShoulder();
        }
    }

    private void handleShoulderRestore() {
        if (pendingCarrier == null) return;

        if (isOnShoulder() || ++pendingCarrierTicks > SHOULDER_RESTORE_TIMEOUT) {
            pendingCarrier = null;
            pendingCarrierTicks = 0;
            return;
        }

        Player carrier = this.level().getPlayerByUUID(pendingCarrier);
        if (carrier == null || carrier.isRemoved()) return;
        if (this.distanceTo(carrier) > SHOULDER_RESTORE_RANGE) return;
        if (!carrier.getPassengers().isEmpty()) return;

        pendingCarrier = null;
        pendingCarrierTicks = 0;
        climbOnShoulder(carrier);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return super.mobInteract(player, hand);
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        if (!this.isTame() || this.isBaby() || this.isInResurrection()) return super.mobInteract(player, hand);

        ItemStack stack = player.getItemInHand(hand);
        if (isFood(stack) && this.getHealth() < this.getMaxHealth()) return super.mobInteract(player, hand);

        if (isOnShoulder()) {
            if (!player.isSteppingCarefully()) return InteractionResult.PASS;
            if (this.getVehicle() != player) return InteractionResult.PASS;
            dismountFromShoulder();
            return InteractionResult.SUCCESS;
        }

        if (player.isSteppingCarefully()) return super.mobInteract(player, hand);
        if (shoulderCooldown > 0 || isClimbing() || wakeThenClimbDelay > 0) return InteractionResult.PASS;
        if (!this.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) {
            return InteractionResult.PASS;
        }
        if (!player.getPassengers().isEmpty()) return InteractionResult.PASS;

        if (isNapping() || isSleeping()) {
            setNap(false);
            setSleeping(false);
            wakeThenClimbDelay = WAKE_TRANSITION_TICKS;
            wakeThenClimbCarrierId = player.getId();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.6f,
                    (float) OWUtils.generateRandomInterval(0.8, 1.0));
            return InteractionResult.SUCCESS;
        }

        return beginClimb(player, true) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean climbOnShoulder(Player player) {
        this.setSitting(false);
        this.getNavigation().stop();
        if (!this.startRiding(player, true)) return false;

        syncPassengersToCarrier(player);

        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN;
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.2, 1.4));
        return true;
    }

    public void dismountFromShoulder() {
        Entity vehicle = this.getVehicle();
        this.stopRiding();
        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN;
        napCooldown = NAP_COOLDOWN_AFTER_DISMOUNT;

        if (vehicle instanceof Player player) {
            syncPassengersToCarrier(player);
            beginClimb(player, false);
            return;
        }

        if (vehicle != null) this.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
    }

    private boolean beginClimb(Player carrier, boolean mounting) {
        if (this.level().isClientSide()) return false;

        this.setSitting(false);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;

        this.climbStart = this.position();
        this.climbCarrierId = carrier.getId();
        this.entityData.set(CLIMB_MOUNTING, mounting);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);
        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN + CLIMB_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                mounting ? SoundEvents.FOX_AMBIENT : SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(1.2, 1.45));
        return true;
    }

    private void handleWakeThenClimb() {
        if (wakeThenClimbDelay <= 0) return;
        if (--wakeThenClimbDelay > 0) return;

        int carrierId = wakeThenClimbCarrierId;
        wakeThenClimbCarrierId = -1;

        if (!(this.level().getEntity(carrierId) instanceof Player carrier) || !carrier.isAlive()) return;
        if (!carrier.getPassengers().isEmpty()) return;
        if (this.distanceTo(carrier) > SHOULDER_RESTORE_RANGE) return;

        beginClimb(carrier, true);
    }

    private void handleShoulderClimb() {
        int remaining = this.entityData.get(CLIMB_TIMER);
        if (remaining <= 0) return;

        if (!(this.level().getEntity(climbCarrierId) instanceof Player carrier) || !carrier.isAlive()) {
            abortClimb();
            return;
        }

        this.entityData.set(CLIMB_TIMER, --remaining);
        float progress = 1f - remaining / (float) CLIMB_TICKS;

        boolean mounting = this.entityData.get(CLIMB_MOUNTING);
        Vec3 shoulder = carrier.position().add(
                shoulderOffset(carrier.yBodyRot, this.getScale(), this.crouchAmount));
        Vec3 ground = carrier.position().add(
                shoulderOffset(carrier.yBodyRot, this.getScale(), 0f).multiply(1.6, 0, 1.6));

        Vec3 from = mounting ? climbStart : shoulder;
        Vec3 to = mounting ? shoulder : ground;

        Vec3 previous = this.position();
        Vec3 next = climbCurve(from, to, progress, mounting);
        this.setPos(next);

        Vec3 step = next.subtract(previous);
        if (step.lengthSqr() > 1.0e-8) {
            float slope = (float) (Mth.atan2(step.y, Math.max(step.horizontalDistance(), 1.0e-4))
                    * Mth.RAD_TO_DEG);
            this.entityData.set(CLIMB_PITCH, Mth.lerp(0.35f, climbPitch(), Mth.clamp(slope, -80f, 80f)));
        }

        if (remaining == CLIMB_TICKS / 2) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WOOL_HIT, SoundSource.NEUTRAL, 0.35f,
                    (float) OWUtils.generateRandomInterval(1.5, 1.8));
        }

        double dx = carrier.getX() - this.getX(), dz = carrier.getZ() - this.getZ();
        if (dx * dx + dz * dz > 1.0e-4) {
            this.setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f);
            this.yBodyRot = this.getYRot();
            this.setYHeadRot(this.getYRot());
        }

        if (remaining > 0) return;

        this.noPhysics = false;
        this.climbStart = null;
        this.climbCarrierId = -1;
        this.entityData.set(CLIMB_PITCH, 0f);

        if (mounting && this.startRiding(carrier, true)) {
            syncPassengersToCarrier(carrier);
            this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                    SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f,
                    (float) OWUtils.generateRandomInterval(1.2, 1.4));
        }
    }

    private void abortClimb() {
        this.entityData.set(CLIMB_TIMER, 0);
        this.entityData.set(CLIMB_PITCH, 0f);
        this.noPhysics = false;
        this.climbStart = null;
        this.climbCarrierId = -1;
    }

    private static Vec3 climbCurve(Vec3 from, Vec3 to, float progress, boolean mounting) {
        float approach;
        float rise;
        float leap = 0f;

        if (mounting) {
            leap = Mth.clamp((progress - CLIMB_CROUCH_PHASE) / (1f - CLIMB_CROUCH_PHASE), 0f, 1f);
            approach = leap;
            rise = leap;
        } else {
            float fall = Mth.clamp((progress - CLIMB_HESITATION) / (1f - CLIMB_HESITATION), 0f, 1f);
            approach = easeInOutCubic(fall);
            rise = fall < 0.75f
                    ? (fall / 0.75f) * (fall / 0.75f) * 0.88f
                    : 0.88f + 0.12f * easeOutCubic((fall - 0.75f) / 0.25f);
        }

        double x = Mth.lerp(approach, from.x, to.x);
        double z = Mth.lerp(approach, from.z, to.z);
        double y = Mth.lerp(rise, from.y, to.y);

        if (mounting) {
            float crouch = progress < CLIMB_CROUCH_PHASE
                    ? Mth.sin(progress / CLIMB_CROUCH_PHASE * Mth.PI) : 0f;
            y -= CLIMB_CROUCH_DEPTH * crouch;
            y += CLIMB_LEAP_APEX * 4.0 * leap * (1f - leap);
        } else {
            y += CLIMB_HOP * 0.4f * Mth.sin(Mth.clamp(rise, 0f, 1f) * Mth.PI);
        }

        Vec3 flat = to.subtract(from).multiply(1, 0, 1);
        if (flat.lengthSqr() > 1.0e-6) {
            Vec3 side = new Vec3(-flat.z, 0, flat.x).normalize();
            double spiral = CLIMB_SPIRAL * Mth.sin(progress * Mth.PI);
            x += side.x * spiral;
            z += side.z * spiral;
        }

        return new Vec3(x, y, z);
    }

    private static float easeOutCubic(float t) {
        float inverse = 1f - t;
        return 1f - inverse * inverse * inverse;
    }

    private static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private static void syncPassengersToCarrier(Player carrier) {
        if (carrier instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(serverPlayer));
        }
    }

    @Override
    public boolean canStartNap() {
        return napCooldown <= 0 && !isClimbing() && !isOnShoulder();
    }

    public boolean isOnShoulder() {
        return this.getVehicle() instanceof Player;
    }

    public static @Nullable RedPandaEntity getShoulderPanda(@Nullable Player player) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof RedPandaEntity redPanda) return redPanda;
        }
        return null;
    }

    public static @Nullable OWEntity resolveControlledEntity(@Nullable Player player) {
        if (player == null) return null;
        if (player.getRootVehicle() instanceof OWEntity mount) return mount;
        return getShoulderPanda(player);
    }

    public @Nullable Player getCarrier() {
        return this.getVehicle() instanceof Player player ? player : null;
    }

    public static Vec3 shoulderOffset(float yawDegrees, double scale, float crouch) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        double rightX = -Mth.cos(yaw);
        double rightZ = -Mth.sin(yaw);
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);

        double side = SHOULDER_SIDE_OFFSET * scale;
        double forward = SHOULDER_FORWARD_OFFSET * scale + CROUCH_FORWARD * crouch;

        return new Vec3(
                rightX * side + forwardX * forward,
                SHOULDER_HEIGHT_OFFSET - CROUCH_DROP * crouch,
                rightZ * side + forwardZ * forward);
    }

    @Override
    public void rideTick() {
        super.rideTick();

        Player carrier = getCarrier();
        if (carrier == null) return;

        this.getNavigation().stop();

        this.crouchAmountO = this.crouchAmount;
        this.crouchAmount += ((carrier.isCrouching() ? 1f : 0f) - this.crouchAmount) * CROUCH_BLEND;

        Vec3 offset = shoulderOffset(carrier.yBodyRot, this.getScale(), this.crouchAmount);
        this.setPos(carrier.getX() + offset.x, carrier.getY() + offset.y, carrier.getZ() + offset.z);
        this.setYRot(carrier.yBodyRot);
        this.yBodyRot = carrier.yBodyRot;
        this.yHeadRot = carrier.yHeadRot;
        this.setYHeadRot(carrier.yHeadRot);
        this.fallDistance = 0f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (isOnShoulder() && isShieldedWhileCarried(damageSource)) return false;
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
        return !isOnShoulder() && super.isPushable();
    }

    @Override
    public boolean isPickable() {
        return !isOnShoulder() && super.isPickable();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.isAlive();
    }

    @Override
    public void die(DamageSource damageSource) {
        if (isOnShoulder()) this.stopRiding();
        super.die(damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    public void throwHealSnack() {
        if (this.level().isClientSide()) return;
        if (healSnackCooldown > 0) return;

        Player carrier = getCarrier();
        if (carrier != null && carrier.isSteppingCarefully()) return;
        if (carrier != null && carrier.getVehicle() != null) return;

        LivingEntity aimer = carrier != null ? carrier : this.getOwner();
        if (aimer == null) return;

        LivingEntity target = pickHealTarget(aimer);
        if (target == null) {
            playAimFailure(aimer);
            return;
        }

        float cost = OWAttacksConstants.RedPanda.HEAL_SNACK_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        healSnackCooldown = OWAttacksConstants.RedPanda.HEAL_SNACK_COOLDOWN_TICKS;

        boolean twin = OWPistePassives.has(this, OWPistePassives.DOUBLE_RATION);
        setThrowTimer(OWAttacksConstants.RedPanda.HEAL_SNACK_THROW_TICKS);

        queueSnack(target, true, 0, -1);

        if (twin) {
            secondThrowDelay = OWPistePassives.DOUBLE_RATION_DELAY_TICKS;
            queueSnack(target, false, OWPistePassives.DOUBLE_RATION_DELAY_TICKS, 1);
        }
    }

    private void handleSecondThrow() {
        if (secondThrowDelay <= 0) return;
        if (--secondThrowDelay > 0) return;

        setThrowTimer(OWAttacksConstants.RedPanda.HEAL_SNACK_THROW_TICKS);
    }

    private void queueSnack(LivingEntity target, boolean credits, int stagger, int side) {
        pendingSnacks.add(new int[]{
                OWAttacksConstants.RedPanda.HEAL_SNACK_RELEASE_TICKS + stagger,
                target.getId(), credits ? 1 : 0, side});
    }

    private void handlePendingBites() {
        if (pendingBites.isEmpty()) return;

        Iterator<int[]> iterator = pendingBites.iterator();
        while (iterator.hasNext()) {
            int[] pending = iterator.next();
            if (--pending[0] > 0) continue;

            iterator.remove();
            if (!(this.level().getEntity(pending[1]) instanceof LivingEntity target) || !target.isAlive()) continue;

            float max = target.getMaxHealth();
            target.heal(max * OWAttacksConstants.RedPanda.HEAL_SNACK_INSTANT_RATIO * getHealPower());

            int pulses = OWAttacksConstants.RedPanda.HEAL_SNACK_OVER_TIME_TICKS
                    / OWAttacksConstants.RedPanda.HEAL_SNACK_OVER_TIME_INTERVAL;
            float perPulse =
                    (max * OWAttacksConstants.RedPanda.HEAL_SNACK_OVER_TIME_RATIO * getHealPower()) / pulses;

            healOverTime.removeIf(entry -> entry.targetId == target.getId());
            healOverTime.add(new HealOverTime(target.getId(), pulses, perPulse));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        target.getX(), target.getY() + target.getBbHeight() * 0.95, target.getZ(),
                        4, 0.3, 0.2, 0.3, 0.02);
            }

            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.4f,
                    (float) OWUtils.generateRandomInterval(1.4, 1.7));
        }
    }

    private void handlePendingSnacks() {
        if (pendingSnacks.isEmpty()) return;

        if (!this.isAlive()) {
            pendingSnacks.clear();
            return;
        }

        Iterator<int[]> iterator = pendingSnacks.iterator();
        while (iterator.hasNext()) {
            int[] pending = iterator.next();
            if (--pending[0] > 0) continue;

            iterator.remove();
            if (this.level().getEntity(pending[1]) instanceof LivingEntity target && target.isAlive()) {
                launchHealSnack(target, pending[2] == 1, pending[3]);
            }
        }
    }

    private Vec3 pawHoldPoint() {
        Player carrier = getCarrier();
        float yaw = (carrier != null ? carrier.getYRot() : this.getYRot()) * Mth.DEG_TO_RAD;
        return new Vec3(this.getX() - Mth.sin(yaw) * 0.3, this.getEyeY() - 0.05, this.getZ() + Mth.cos(yaw) * 0.3);
    }

    private void handleSnackCharge() {
        int timer = getThrowTimer();
        if (timer <= OWAttacksConstants.RedPanda.HEAL_SNACK_THROW_TICKS
                - OWAttacksConstants.RedPanda.HEAL_SNACK_RELEASE_TICKS) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 paws = pawHoldPoint();
        serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                paws.x, paws.y, paws.z, 1, 0.12, 0.06, 0.12, 0.01);
    }

    private void launchHealSnack(LivingEntity target, boolean credits, int side) {
        HealSnackEntity orb = new HealSnackEntity(this.level(), this, target, randomSnack());
        orb.setCredits(credits);

        Player carrier = getCarrier();
        float yaw = (carrier != null ? carrier.getYRot() : this.getYRot()) * Mth.DEG_TO_RAD;
        Vec3 paws = pawHoldPoint()
                .add(Mth.cos(yaw) * side * PAW_SIDE_OFFSET, 0, Mth.sin(yaw) * side * PAW_SIDE_OFFSET);

        orb.setPos(paws.x, paws.y + 0.18, paws.z);
        orb.aimAt(target);
        this.level().addFreshEntity(orb);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.3, 1.5));
    }

    private void playAimFailure(LivingEntity aimer) {
        this.level().playSound(null, aimer.getX(), aimer.getY(), aimer.getZ(),
                SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.NEUTRAL, 0.5f, 0.6f);
    }

    public @Nullable LivingEntity pickHealTarget(LivingEntity aimer) {
        double range = OWAttacksConstants.RedPanda.HEAL_SNACK_RANGE;
        double tolerance = OWAttacksConstants.RedPanda.HEAL_SNACK_AIM_TOLERANCE;
        double minDistance = OWAttacksConstants.RedPanda.HEAL_SNACK_MIN_AIM_DISTANCE;

        Vec3 eye = aimer.getEyePosition();
        Vec3 look = aimer.getLookAngle().normalize();

        AABB box = aimer.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (candidate == this || candidate == aimer || !candidate.isAlive()) continue;
            if (!isHealAlly(candidate)) continue;

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

        if (best == null && isHealAlly(aimer) && aimer.getHealth() < aimer.getMaxHealth()) return aimer;

        return best;
    }

    public boolean isHealAlly(LivingEntity candidate) {
        if (candidate == this.getOwner()) return true;
        if (this.isTameGrabAlly(candidate)) return true;
        return candidate instanceof TamableAnimal tamed && tamed.isOwnedBy(this.getOwner());
    }


    public void serveSnack(LivingEntity target, ItemStack snack, boolean credits) {
        if (this.level().isClientSide() || target == null || !target.isAlive()) return;

        pendingBites.add(new int[]{OWAttacksConstants.RedPanda.HEAL_SNACK_FEED_TICKS, target.getId()});
        beginFeeding(target, OWAttacksConstants.RedPanda.HEAL_SNACK_FEED_TICKS, snack);
        if (credits) creditFeastCharge();

        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(1.2, 1.5));
    }

    public void beginFeeding(LivingEntity target, int ticks) {
        beginFeeding(target, ticks, randomSnack());
    }

    public void beginFeeding(LivingEntity target, int ticks, ItemStack snack) {
        if (this.level().isClientSide() || target == null) return;

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, FeedingPacket.of(target, ticks, snack));

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    6, target.getBbWidth() * 0.4, target.getBbHeight() * 0.3, target.getBbWidth() * 0.4, 0.02);
        }
    }

    private void handleHealOverTime() {
        if (healOverTime.isEmpty()) return;
        if (this.tickCount % OWAttacksConstants.RedPanda.HEAL_SNACK_OVER_TIME_INTERVAL != 0) return;

        Iterator<HealOverTime> iterator = healOverTime.iterator();
        while (iterator.hasNext()) {
            HealOverTime entry = iterator.next();
            Entity found = this.level().getEntity(entry.targetId);

            if (!(found instanceof LivingEntity target) || !target.isAlive()) {
                iterator.remove();
                continue;
            }

            target.heal(entry.amountPerPulse);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                        target.getX(), target.getY() + target.getBbHeight() * 0.4, target.getZ(),
                        3, target.getBbWidth() * 0.5, target.getBbHeight() * 0.4, target.getBbWidth() * 0.5, 0.01);
            }

            if (--entry.remainingPulses <= 0) iterator.remove();
        }
    }

    public boolean activateFeast() {
        if (this.level().isClientSide()) return false;
        if (isAuraActive()) return false;

        Player carrier = getCarrier();
        if (carrier != null && carrier.getVehicle() != null) return false;

        float cost = OWAttacksConstants.RedPanda.FEAST_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        int duration = feastDurationTicks();
        this.entityData.set(FEAST_CHARGE, 0);
        this.pendingFeastPortions = duration / OWAttacksConstants.RedPanda.FEAST_PULSE_INTERVAL;
        setAuraTimer(duration);
        this.entityData.set(FEAST_CAST_TIMER, OWAttacksConstants.RedPanda.FEAST_CAST_TICKS);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BUNDLE_INSERT, SoundSource.NEUTRAL, 1.0f, 0.7f);
        return true;
    }

    private void handleFeastCast() {
        int timer = getFeastCastTimer();
        if (timer <= 0) return;

        this.entityData.set(FEAST_CAST_TIMER, timer - 1);

        if (timer - 1 != OWAttacksConstants.RedPanda.FEAST_CAST_TICKS
                - OWAttacksConstants.RedPanda.FEAST_CAST_RELEASE_TICKS) return;

        dropFeastPile(getCarrier());

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 paws = pawHoldPoint();

            for (ItemStack snack : SNACKS) {
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM, snack),
                        paws.x, paws.y, paws.z, 9, 0.35, 0.25, 0.35, 0.14);
            }

            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.getX(), this.getY() + 0.4, this.getZ(), 40, 0.5, 0.3, 0.5, 0.18);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BAMBOO_WOOD_PLACE, SoundSource.NEUTRAL, 1.1f, 0.8f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.NEUTRAL, 1.2f, 0.9f);
    }

    private void dropFeastPile(@Nullable Player carrier) {
        LivingEntity thrower = carrier != null ? carrier : this;
        float yaw = thrower.getYRot() * Mth.DEG_TO_RAD;
        double distance = OWAttacksConstants.RedPanda.FEAST_TOSS_DISTANCE;

        FeastPileEntity pile = new FeastPileEntity(this.level(), this,
                Math.max(1, pendingFeastPortions), feastRadius());

        pile.setPos(thrower.getX() - Mth.sin(yaw) * distance,
                thrower.getY() + 0.35,
                thrower.getZ() + Mth.cos(yaw) * distance);
        pile.plantOnGround();

        this.level().addFreshEntity(pile);
    }

    public void cancelFeast() {
        setAuraTimer(0);
    }

    private void handleFeast() {
        if (!isAuraActive()) return;

        setAuraTimer(getAuraTimer() - 1);
        if (getAuraTimer() <= 0) cancelFeast();
    }

    private int feastDurationTicks() {
        int base = OWAttacksConstants.RedPanda.FEAST_DURATION_TICKS
                + getFeastOvercharge() * OWAttacksConstants.RedPanda.FEAST_OVERCHARGE_TICKS_EACH;
        return OWPistePassives.has(this, OWPistePassives.LONG_TABLE)
                ? Math.round(base * OWPistePassives.LONG_TABLE_DURATION_FACTOR)
                : base;
    }

    private double feastRadius() {
        double base = OWAttacksConstants.RedPanda.FEAST_RADIUS;
        return OWPistePassives.has(this, OWPistePassives.LONG_TABLE)
                ? base * OWPistePassives.LONG_TABLE_RADIUS_FACTOR
                : base;
    }





    private void setupAnimationState() {
        boolean resting = isNapping() || isSleeping();

        if (isOnShoulder()) this.shoulderIdleAnimationState.startIfStopped(this.tickCount);
        else this.shoulderIdleAnimationState.stop();

        if (resting) this.napAnimationState.startIfStopped(this.tickCount);
        else this.napAnimationState.stop();

        if (isSitting() && !resting && !isOnShoulder()) this.sittingAnimationState.startIfStopped(this.tickCount);
        else this.sittingAnimationState.stop();

        this.idleAnimationState.startIfStopped(this.tickCount);
        handleMiscIdleAnimations();

        int throwTimer = getThrowTimer();
        if (throwTimer > lastThrowTimer) this.throwAnimationState.start(this.tickCount);
        else if (throwTimer <= 0) this.throwAnimationState.stop();
        lastThrowTimer = throwTimer;

        int castTimer = getFeastCastTimer();
        if (castTimer > lastFeastCastTimer) this.feastAnimationState.start(this.tickCount);
        else if (castTimer <= 0) this.feastAnimationState.stop();
        lastFeastCastTimer = castTimer;
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isMoving()
                && !this.isPassenger() && !this.isInWater();
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted();
    }

    protected void handleMiscIdleAnimations() {
        if (this.miscIdleAnimationState.isStarted()
                && this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_DURATION) {
            this.miscIdleAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            this.miscIdleAnimationState.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setHealPower((float) OWUtils.generateRandomInterval(HEAL_POWER_ROLL_MIN, HEAL_POWER_ROLL_MAX));

            this.setVariant(chooseRedPandaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 10);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (OWEntity.NAPPING.equals(accessor) && !isNapping() && !isSleeping() && this.level().isClientSide()) {
            napAnimationState.stop();
        }
    }

    private RedPandaVariant chooseRedPandaVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 15) return RedPandaVariant.BROWN;
        if (roll < 40) return RedPandaVariant.RED;
        return RedPandaVariant.DEFAULT;
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof RedPandaEntity redPanda) {
            redPanda.setVariant(RedPandaVariant.byId(variant));
            redPanda.setInitialVariant(RedPandaVariant.byId(variant));
        }
    }

    public RedPandaVariant getVariant() {
        return RedPandaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(RedPandaVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(RedPandaVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(RedPandaVariant.Cosmetics.GOLD.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() {
        return this.getInitialVariant().getId();
    }

    public RedPandaVariant getInitialVariant() {
        return RedPandaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(RedPandaVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public int getThrowTimer() { return this.entityData.get(THROW_TIMER); }


    public void setThrowTimer(int timer) { this.entityData.set(THROW_TIMER, timer); }

    public int getFeastCharge() { return this.entityData.get(FEAST_CHARGE); }

    public int getFeastCastTimer() { return this.entityData.get(FEAST_CAST_TIMER); }

    public void creditFeastCharge() {
        int ceiling = OWAttacksConstants.RedPanda.FEAST_SNACKS_REQUIRED
                + OWAttacksConstants.RedPanda.FEAST_OVERCHARGE_MAX;
        if (getFeastCharge() >= ceiling) return;
        this.entityData.set(FEAST_CHARGE, Math.min(ceiling, getFeastCharge() + 1));
    }

    public int getFeastOvercharge() {
        return Mth.clamp(getFeastCharge() - OWAttacksConstants.RedPanda.FEAST_SNACKS_REQUIRED,
                0, OWAttacksConstants.RedPanda.FEAST_OVERCHARGE_MAX);
    }

    public int getAuraTimer() { return this.entityData.get(AURA_TIMER); }

    public void setAuraTimer(int timer) { this.entityData.set(AURA_TIMER, Math.max(0, timer)); }

    public boolean isAuraActive() { return getAuraTimer() > 0; }

    public int getHealOrbCooldownTicks() { return healSnackCooldown; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putFloat("healPower", this.getHealPower());
        tag.putInt("feastCharge", this.getFeastCharge());

        Player carrier = getCarrier();
        if (carrier != null) tag.putUUID("shoulderCarrier", carrier.getUUID());
        else if (pendingCarrier != null) tag.putUUID("shoulderCarrier", pendingCarrier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("healPower")) setHealPower(tag.getFloat("healPower"));
        this.entityData.set(FEAST_CHARGE, tag.getInt("feastCharge"));
        this.pendingCarrier = tag.hasUUID("shoulderCarrier") ? tag.getUUID("shoulderCarrier") : null;
        this.pendingCarrierTicks = 0;
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final class HealOverTime {
        private final int targetId;
        private int remainingPulses;
        private final float amountPerPulse;

        private HealOverTime(int targetId, int remainingPulses, float amountPerPulse) {
            this.targetId = targetId;
            this.remainingPulses = remainingPulses;
            this.amountPerPulse = amountPerPulse;
        }
    }
}
