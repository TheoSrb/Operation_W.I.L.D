package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWPlacedBlocks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.core.OWWoolColors;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ElephantFootstepPacket;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.team.OWArenaManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class ElephantEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 300.0;

    private static final int CALL_MIN_COOLDOWN = 700;
    private static final int CALL_MAX_COOLDOWN = 1400;
    private static final int CALL_DURATION = 90;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CALLING = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> SHOULDER_BASH_TIMER = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER_BASH_SIDE = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    public static final int TRUNK_IDLE = 0;
    public static final int TRUNK_FILLING = 1;
    public static final int TRUNK_SPRAYING = 2;

    private static final EntityDataAccessor<Integer> TRUNK_WATER = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRUNK_MODE = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TRUNK_WATER_NEARBY = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> EARTHQUAKE_TICK = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SADDLE_WOOL_0 = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SADDLE_WOOL_1 = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    private static final double SEAT_HEIGHT = 0.78;
    private static final double SEAT_FORWARD = 0.10;
    private static final double SECOND_SEAT_BACK = 0.70;

    private static final int NO_TRIBE_WOOL_0 = 0xE9ECEC;
    private static final int NO_TRIBE_WOOL_1 = 0x8E8E86;

    private static final double WALK_CYCLE_MS = 3411.5;
    public static final long RIGHT_FOOT_CONTACT_MS = 1200L;
    public static final long LEFT_FOOT_CONTACT_MS = 2900L;

    public static final float WALK_ANIM_SPEED = 6.0f * 0.75f;
    public static final float RUN_ANIM_SPEED = 7.8f * 0.85f;

    private static final Vec3 FRONT_LEFT_FOOT  = new Vec3( 6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 FRONT_RIGHT_FOOT = new Vec3(-6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 BACK_LEFT_FOOT   = new Vec3( 8 / 16.0, 0, -16 / 16.0);
    private static final Vec3 BACK_RIGHT_FOOT  = new Vec3(-8 / 16.0, 0, -16 / 16.0);

    private static final double FOOTFALL_RADIUS = 15.0;
    private static final double FOOTFALL_HOP = 0.8;
    private static final double FOOTFALL_NEAR_PLATEAU = 6.0;

    private static final int FOOTFALL_PARTICLES = 22;
    private static final double FOOTFALL_SPREAD = 0.22;

    private static final int FOOTFALL_MIN_INTERVAL = 3;


    private static final float FALL_SAFE_DISTANCE = 2.0f;
    private static final float FALL_DAMAGE_MULTIPLIER = 2.0f;

    private static final double CHARGE_SPEED = 0.32;

    private static final int FOOTSTEP_SOUND_REPEATS = 5;

    private static final double FOOTSTEP_BROADCAST_RANGE = 48.0;

    private static final double FOOTFALL_MIN_SPEED = 0.03;
    private static final int TRAVEL_WINDOW = 5;
    private static final double WALK_PHASE_CORRECTION_RATE = 0.25;
    private static final double WALK_PHASE_CORRECTION_LIMIT = 0.4;

    private static final float FULL_CHARGE = 100f;

    private static final float ROTATION_SPEED_STILL = 0.05f;
    private static final float ROTATION_SPEED_CHARGED = 0.025f;

    private static final float CHARGE_HEAD_MAX_PITCH = 40f;
    private static final float CHARGE_HEAD_RESPONSE = 0.12f;

    private static final double COMBO_BREAK_REACH = 2.0;
    private static final double COMBO_BREAK_SIDE_OFFSET = 1.1;
    private static final double COMBO_BREAK_SWEEP_HALF = 1.2;
    private static final double COMBO_BREAK_SWEEP_HEIGHT = 0.7;
    private static final double COMBO_BREAK_COLUMN_HALF = 1.0;
    private static final double COMBO_BREAK_COLUMN_HEIGHT = 2.2;
    private static final double COMBO_BREAK_COLUMN_LIFT = 1.0;
    private static final float COMBO_BREAK_HARDNESS_PER_DAMAGE = 0.3f;
    private static final float COMBO_BREAK_MAX_HARDNESS = 25f;
    private static final ItemStack COMBO_BREAK_TOOL = new ItemStack(Items.NETHERITE_PICKAXE);

    private static final double COMBO_SWEEP_KNOCKBACK = 1.15;
    private static final double COMBO_SWEEP_LIFT = 0.32;
    private static final double COMBO_SLAM_LAUNCH = 0.85;

    private static final float RIDDEN_ACCEL_RESPONSE = 0.30f;
    private static final float RIDDEN_BRAKE_RESPONSE = 0.12f;
    private static final float RIDDEN_BRAKE_RESPONSE_CHARGED = 0.035f;
    private static final float CHARGE_MEMORY_DECAY = 0.94f;

    private static final float CHARGE_DAMAGE = 3.5f;
    private static final float CHARGE_KNOCKBACK = 1.4f;
    private static final int CHARGE_HIT_INTERVAL = 20;
    private static final double PLOUGH_EXTRA_HEIGHT = 1.0;
    private static final double STALL_RATIO = 0.35;
    private static final double STALL_MIN_PEAK = 0.10;
    private static final double STALL_PEAK_DECAY = 0.98;
    private static final float STALL_CHARGE_LOSS = 34.0f;
    private static final int STALL_GRACE_TICKS = 2;
    private static final double CHARGE_HIT_FORWARD = 0.5;
    private static final double CHARGE_HIT_HALF_WIDTH = 0.4;

    private static final float CHARGE_LOG_BONUS_CHANCE = 0.5f;
    private static final int TREE_FELL_MAX_LOGS = 40;
    private static final int TREE_FELL_RADIUS = 8;
    private static final int TREE_FELL_HEIGHT = 30;

    private static final net.minecraft.core.particles.DustParticleOptions GOLD_DUST =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0f, 0.80f, 0.15f), 1.4f);

    private static final int SHOCKWAVE_HIT_INTERVAL = 8;

    private static final int MAX_ACTIVE_SHOCKWAVES = 8;
    private static final int EARTHQUAKE_RING_POINTS = 26;

    private static final float EARTHQUAKE_SPREAD_CHANCE = 0.45f;
    private static final int EARTHQUAKE_MAX_SPREAD = 2;

    private static final Direction[] HORIZONTAL_SPREAD = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    
    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState earthquakeAnimationState = new AnimationState();

    public int callAnimationStartTime = 0;

    public volatile float bodyAnimY = 0f;

    private int shoulderBashCooldown = 0;

    private BlockPos lavaTargetPos = null;
    private int lavaSoakTicks = 0;

    /**
     * Avancement local du coup d'épaule, compté par le client pour son propre compte.
     *
     * <p>Le minuteur synchronisé ne décroît que côté serveur, et sa copie n'arrive au client que
     * lorsque le réseau la lui apporte — pas forcément à chaque tick. L'animation avançait donc par
     * à-coups, restant figée deux images puis sautant deux crans. Ce compteur-ci progresse d'un
     * tick client par tick client ; le minuteur réseau ne sert plus qu'à savoir quand le geste
     * commence et quand il finit.</p>
     */
    public int clientBashElapsed = -1;

    /** Visée lissée de la trompe (client), en degrés relatifs au corps. */
    public float trunkAimYaw, trunkAimYawO;
    public float trunkAimPitch, trunkAimPitchO;
    /** Gonflement de la trompe pendant l'aspiration (client) : 0 au repos, 1 pleine. */
    public float trunkSwell, trunkSwellO;
    /** Part de la pose de trompe pilotée à la visée plutôt que par les animations clés. */
    public float trunkAimWeight, trunkAimWeightO;
    /** Abaissement de l'encolure pendant l'aspiration, en degrés. */
    public float trunkHeadDip, trunkHeadDipO;
    /** Dernier tick où le panache a été émis : borne l'émission à 20 salves/s, pas à une par image. */
    public int lastSprayParticleTick = -1;
    private int callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

    private boolean earthquakeImpactDone = false;

    private Vec3 earthquakeEpicentre = Vec3.ZERO;

    private static final class Shockwave {
        private int tick = 1;
        private final Vec3 origin;
        private final Vec3 direction;
        private double y;
        private final Map<Integer, Integer> struck = new HashMap<>();

        private Shockwave(Vec3 origin, Vec3 direction, double y) {
            this.origin = origin;
            this.direction = direction;
            this.y = y;
        }
    }

    private final List<Shockwave> shockwaves = new ArrayList<>();

    private double walkAnimTimeMs = 0;
    private double walkAnimTimeMsPrev = 0;

    private double walkPhaseCorrectionMs = 0;

    private final double[] travelRingX = new double[TRAVEL_WINDOW];
    private final double[] travelRingZ = new double[TRAVEL_WINDOW];
    private int travelRingIndex = 0;
    private int travelRingTicks = 0;
    private double netSpeed = 0;
    private double rawSpeed = 0;
    private double lastTickX = Double.NaN;
    private double lastTickZ = Double.NaN;
    private int stallTicks = 0;
    private double runPeakSpeed = 0;

    private float riddenSpeed = 0f;
    private float chargeMemory = 0f;
    private float chargeDisplay = 0f;

    private float chargeHeadPitch = 0f;
    private float chargeHeadPitchPrev = 0f;

    private int groundStrikeCooldown = 0;

    private final Set<Integer> ploughStruck = new HashSet<>();

    public ElephantEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.14D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ElephantMeleeAttackGoal());

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof ElephantEntity elephant && !elephant.isSleeping() && !elephant.isNapping()
                        && !elephant.isEarthquakeGesture()) {
                    super.tick();
                }
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_CALLING, false);
        builder.define(SHOULDER_BASH_TIMER, 0);
        builder.define(SHOULDER_BASH_SIDE, 1);
        builder.define(TRUNK_WATER, 0);
        builder.define(TRUNK_MODE, TRUNK_IDLE);
        builder.define(TRUNK_WATER_NEARBY, false);
        builder.define(EARTHQUAKE_TICK, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(SADDLE_WOOL_0, NO_TRIBE_WOOL_0);
        builder.define(SADDLE_WOOL_1, NO_TRIBE_WOOL_1);
    }

    public static final int ENTITY_COLOR = 0x776a5e;

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 17f;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
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
        return 4.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 4f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return true;
    }

    @Override
    public float sprintAccelerationMultiplier() {
        return 0.65f;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return false;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.ELEPHANT_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.ELEPHANT_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 280f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.75f * (1 + ((float) this.getLevel() / 50));
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
        return true;
    }
    
    @Override
    protected boolean canLean() {
        return !this.isSitting() && !this.isSleeping() && !this.isNapping() && !this.isEarthquakeGesture();
    }

    @Override
    protected float bankMaxAngle() {
        return 30f;
    }

    @Override
    protected float pitchMaxAngle() {
        return 0f;
    }

    @Override
    public float getRotationSpeed() {
        return Mth.lerp(getChargeRamp(), ROTATION_SPEED_STILL, ROTATION_SPEED_CHARGED);
    }

    @Override
    public boolean keepsAccelerationDuringCombo() {
        if (isSlammingCombo()) return false;
        return getChargeRamp() >= 1f;
    }

    public float getChargeRamp() {
        if (!this.isVehicle()) return 0f;
        return Mth.clamp(this.getAcceleration() / FULL_CHARGE, 0f, 1f);
    }

    /**
     * L'éléphant peut-il charger tête baissée ?
     *
     * <p>Seulement le Coup d'Épaule en main. Avec le Jet de Trompe, la trompe est en position de
     * tir et la tête reste haute : la bête ne baisse plus l'encolure en sprintant, et ne laboure
     * donc plus rien au passage — ni arbres, ni feuillages. C'est le prix de l'autre carte.</p>
     */
    public boolean canChargeHeadDown() {
        return getSecondaryAttackIndex() == 0;
    }

    private void tickChargeHeadPitch() {
        chargeHeadPitchPrev = chargeHeadPitch;
        float target = canChargeHeadDown() ? getChargeRamp() * CHARGE_HEAD_MAX_PITCH : 0f;
        chargeHeadPitch += (target - chargeHeadPitch) * CHARGE_HEAD_RESPONSE;
    }

    public float getChargeHeadPitch(float partialTick) {
        return Mth.lerp(partialTick, chargeHeadPitchPrev, chargeHeadPitch);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.ELEPHANT.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.ELEPHANT_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!isFooted()) return;
        if (this.isInWater()) return;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        this.level().playLocalSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                soundtype.getStepSound(),
                this.getSoundSource(),
                soundtype.getVolume() * 1.5F,
                soundtype.getPitch() * pitchMod,
                false
        );

        for (int i = 0; i < FOOTSTEP_SOUND_REPEATS; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    OWSounds.ELEPHANT_FOOTSTEP.get(),
                    this.getSoundSource(),
                    0.9F,
                    pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.55f);
        spawnFootDust(FRONT_LEFT_FOOT);
        spawnFootDust(BACK_RIGHT_FOOT);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(0.62f);
        spawnFootDust(FRONT_RIGHT_FOOT);
        spawnFootDust(BACK_LEFT_FOOT);
    }

    private void spawnFootDust(Vec3 localOffset) {
        if (!this.level().isClientSide()) return;

        Vec3 foot = this.position().add(
                localOffset.scale(this.getScale()).yRot((float) -Math.toRadians(this.yBodyRot)));

        BlockState ground = this.level().getBlockState(
                BlockPos.containing(foot.x, foot.y - 0.2, foot.z));
        if (ground.isAir()) return;

        boolean charging = isChargingForward();
        int count = charging ? FOOTFALL_PARTICLES * 2 : FOOTFALL_PARTICLES;
        double lift = charging ? 0.16 : 0.08;

        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, ground);

        for (int i = 0; i < count; i++) {
            this.level().addParticle(dirt,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    foot.y + 0.05,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    (this.random.nextDouble() - 0.5) * 0.08,
                    lift * (0.5 + this.random.nextDouble()),
                    (this.random.nextDouble() - 0.5) * 0.08);
        }

        if (!charging) return;

        for (int i = 0; i < count / 2; i++) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    foot.y + 0.1,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.04,
                    (this.random.nextDouble() - 0.5) * 0.03);
        }
    }

    public float walkAnimationSpeed() {
        return (this.isRunning() || this.getState() == 2) ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
    }

    public double movementAmount() {
        return netSpeed;
    }

    private void tickTravelled() {
        double x = this.getX();
        double z = this.getZ();

        if (!Double.isNaN(lastTickX)) {
            double sx = x - lastTickX;
            double sz = z - lastTickZ;
            rawSpeed = Math.min(Math.sqrt(sx * sx + sz * sz), 1.0);
        }
        lastTickX = x;
        lastTickZ = z;

        double oldX = travelRingX[travelRingIndex];
        double oldZ = travelRingZ[travelRingIndex];
        travelRingX[travelRingIndex] = x;
        travelRingZ[travelRingIndex] = z;
        travelRingIndex = (travelRingIndex + 1) % TRAVEL_WINDOW;

        if (travelRingTicks < TRAVEL_WINDOW) {
            travelRingTicks++;
            return;
        }

        double ndx = x - oldX;
        double ndz = z - oldZ;
        netSpeed = Math.min(Math.sqrt(ndx * ndx + ndz * ndz) / TRAVEL_WINDOW, 1.0);
    }

    private void tickWalkAnimTime() {
        walkAnimTimeMsPrev = walkAnimTimeMs;

        double stride = Math.min(movementAmount() * 4.0, 1.0);
        double advance = stride * 50.0 * walkAnimationSpeed();

        double limit = advance * WALK_PHASE_CORRECTION_LIMIT;
        double correction = Mth.clamp(walkPhaseCorrectionMs * WALK_PHASE_CORRECTION_RATE, -limit, limit);
        walkPhaseCorrectionMs -= correction;

        walkAnimTimeMs = (walkAnimTimeMs + advance + correction + WALK_CYCLE_MS) % WALK_CYCLE_MS;
    }

    public double getWalkAnimTimeMs(float partialTick) {
        double current = walkAnimTimeMs;
        if (current < walkAnimTimeMsPrev) current += WALK_CYCLE_MS;
        return (walkAnimTimeMsPrev + (current - walkAnimTimeMsPrev) * partialTick) % WALK_CYCLE_MS;
    }

    public static boolean walkCycleCrossed(double previousTimeMs, double timeMs, long triggerTimeMs) {
        if (previousTimeMs <= timeMs) return previousTimeMs < triggerTimeMs && timeMs >= triggerTimeMs;
        return triggerTimeMs <= timeMs || triggerTimeMs > previousTimeMs;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        if (RANDOM(4)) return OWSounds.ELEPHANT_IDLE.get();
        if (RANDOM(3)) return OWSounds.ELEPHANT_IDLE_2.get();
        if (RANDOM(2)) return OWSounds.ELEPHANT_IDLE_3.get();
        return OWSounds.ELEPHANT_IDLE_4.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.ELEPHANT_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.ELEPHANT_HURTING.get() : OWSounds.ELEPHANT_HURTING_2.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (shoulderBashCooldown > 0) shoulderBashCooldown--;

        createCombo(28, 20, OWSounds.ELEPHANT_HURTING.get(), 4.0, 4.0, 2.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 3 : 1);
        setTamingPercentage(this.foodGiven, this.foodWanted);
        tickTravelled();
        tickStall();
        tickWalkAnimTime();

        if (this.level().isClientSide()) {
            setupAnimationState();
            tickChargeHeadPitch();
            tickTrunkAim();
            tickClientBashElapsed();
        }
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isShoulderBashing() || this.isEarthquakeGesture());
        }

        if (this.isEarthquakeGesture()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }

        tickShoulderBash();

        if (!this.level().isClientSide()) {
            tickTrunkWater();
            tickEarthquake();
            tickShockwave();
            tickChargeStall();
            tickCharge();
            tickGroundStrikes();
            handleCall();
        }
    }

    private void broadcastFootstep(ServerLevel serverLevel, boolean right) {
        ElephantFootstepPacket packet = new ElephantFootstepPacket(this.getId(), right);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(this) > FOOTSTEP_BROADCAST_RANGE * FOOTSTEP_BROADCAST_RANGE) continue;
            OWNetworkHandler.sendToClient(packet, player);
        }
    }

    public void onFootstepFromServer(boolean right) {
        if (!this.level().isClientSide()) return;

        double contact = right ? RIGHT_FOOT_CONTACT_MS : LEFT_FOOT_CONTACT_MS;
        double delta = (((contact - walkAnimTimeMs) % WALK_CYCLE_MS) + WALK_CYCLE_MS) % WALK_CYCLE_MS;
        if (delta > WALK_CYCLE_MS * 0.5) delta -= WALK_CYCLE_MS;
        walkPhaseCorrectionMs = delta;

        if (right) onRightFootDown(); else onLeftFootDown();

        net.tiew.operationWild.event.ClientEvents.addGroundShake(this, true);
    }

    private void tickGroundStrikes() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (groundStrikeCooldown > 0) groundStrikeCooldown--;

        if (this.isBaby()) return;
        if (!isFooted() || this.isInWater()) return;
        if (this.isSleeping() || this.isNapping() || this.isSitting() || this.isEarthquakeGesture()) return;

        if (netSpeed < FOOTFALL_MIN_SPEED) return;

        if (groundStrikeCooldown > 0) return;

        boolean right = walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, RIGHT_FOOT_CONTACT_MS);
        boolean left = walkCycleCrossed(walkAnimTimeMsPrev, walkAnimTimeMs, LEFT_FOOT_CONTACT_MS);
        if (!right && !left) return;

        groundStrikeCooldown = FOOTFALL_MIN_INTERVAL;
        strikeGround(serverLevel);
        broadcastFootstep(serverLevel, right);
    }

    private boolean isFooted() {
        if (this.onGround()) return true;

        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        return !this.level().getBlockState(below).getCollisionShape(this.level(), below).isEmpty();
    }

    public boolean isChargingForward() {
        if (!this.isVehicle() || !this.isTame() || this.isBaby()) return false;
        return this.isRunning() || measuredSpeed() >= CHARGE_SPEED;
    }

    public boolean isFullyCharged() {
        if (!this.isTame() || this.isBaby()) return false;
        return getChargeRamp() >= 1f;
    }

    public double measuredSpeed() {
        return netSpeed;
    }

    public boolean isStalled() {
        return stallTicks >= STALL_GRACE_TICKS;
    }

    private void tickStall() {
        if (!this.isVehicle() || !this.isRunning()) {
            stallTicks = 0;
            runPeakSpeed = 0;
            return;
        }

        runPeakSpeed = Math.max(runPeakSpeed * STALL_PEAK_DECAY, rawSpeed);

        if (runPeakSpeed < STALL_MIN_PEAK || rawSpeed >= runPeakSpeed * STALL_RATIO) {
            stallTicks = 0;
            return;
        }

        stallTicks++;
    }

    private void tickChargeStall() {
        if (!isStalled()) return;
        if (this.getAcceleration() <= 0f) return;

        setAcceleration(Math.max(0f, this.getAcceleration() - STALL_CHARGE_LOSS));
    }

    private void tickCharge() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!isFullyCharged()) return;
        // Tête haute, rien ne se laboure : le geste et son effet vont de pair, sans quoi les arbres
        // tomberaient sous une charge que plus rien ne montre à l'écran.
        if (!canChargeHeadDown()) return;

        plough(serverLevel);
    }

    public net.minecraft.core.particles.ParticleOptions skinParticle() {
        ElephantVariant variant = this.getVariant();
        if (variant == ElephantVariant.Cosmetics.GOLD.variant) return GOLD_DUST;
        if (variant == ElephantVariant.Cosmetics.DEMON.variant) return ParticleTypes.FLAME;
        if (variant == ElephantVariant.Cosmetics.ZOMBIE.variant) return ParticleTypes.SOUL;
        return null;
    }

    private void spawnSkinParticles(ServerLevel serverLevel, double x, double y, double z,
                                    int count, double spread, double speed) {
        net.minecraft.core.particles.ParticleOptions particle = skinParticle();
        if (particle == null) return;

        serverLevel.sendParticles(particle, x, y, z, count, spread, spread * 0.6, spread, speed);
    }

    private void fellTree(ServerLevel serverLevel, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);

        int felled = 0;
        while (!queue.isEmpty() && felled < TREE_FELL_MAX_LOGS) {
            BlockPos pos = queue.poll();
            BlockState state = serverLevel.getBlockState(pos);

            if (!state.is(BlockTags.LOGS)) continue;
            if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) continue;

            serverLevel.destroyBlock(pos, true, this);
            OWPlacedBlocks.get(serverLevel).forget(pos);
            felled++;

            if (this.random.nextFloat() < CHARGE_LOG_BONUS_CHANCE) {
                Block.popResource(serverLevel, pos, new ItemStack(state.getBlock()));
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = pos.offset(dx, dy, dz);
                        if (Math.abs(next.getX() - origin.getX()) > TREE_FELL_RADIUS) continue;
                        if (Math.abs(next.getZ() - origin.getZ()) > TREE_FELL_RADIUS) continue;
                        if (next.getY() - origin.getY() > TREE_FELL_HEIGHT || next.getY() < origin.getY() - 1) continue;
                        if (!visited.add(next.immutable())) continue;

                        queue.add(next.immutable());
                    }
                }
            }
        }
    }

    private void plough(ServerLevel serverLevel) {
        if (this.tickCount % CHARGE_HIT_INTERVAL == 0) ploughStruck.clear();

        Vec3 ahead = this.position().add(
                Vec3.directionFromRotation(0f, this.yBodyRot).scale(this.getBbWidth() * 0.7));
        AABB front = new AABB(ahead, ahead).inflate(this.getBbWidth() * 0.6, this.getBbHeight() * 0.5, this.getBbWidth() * 0.6)
                .move(0, this.getBbHeight() * 0.5, 0)
                .expandTowards(0, PLOUGH_EXTRA_HEIGHT, 0);

        if (canBreakTerrain(serverLevel) && canChargeHeadDown()) {
            BlockPos.betweenClosedStream(front).forEach(pos -> {
                BlockState state = serverLevel.getBlockState(pos);

                if (state.is(BlockTags.LOGS)) {
                    fellTree(serverLevel, pos.immutable());
                    return;
                }

                if (!state.is(BlockTags.LEAVES) && !state.is(BlockTags.SAPLINGS)) return;
                if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return;

                serverLevel.destroyBlock(pos.immutable(), true, this);
                OWPlacedBlocks.get(serverLevel).forget(pos);
            });
        }

        Vec3 contact = this.position().add(
                Vec3.directionFromRotation(0f, this.yBodyRot).scale(this.getBbWidth() * CHARGE_HIT_FORWARD));
        AABB hitBox = new AABB(contact, contact)
                .inflate(this.getBbWidth() * CHARGE_HIT_HALF_WIDTH,
                        this.getBbHeight() * 0.5,
                        this.getBbWidth() * CHARGE_HIT_HALF_WIDTH)
                .move(0, this.getBbHeight() * 0.5, 0);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (!ploughStruck.add(target.getId())) continue;

            target.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE);
            target.knockback(CHARGE_KNOCKBACK, this.getX() - target.getX(), this.getZ() - target.getZ());
            target.hurtMarked = true;
        }
    }


    private void strikeGround(ServerLevel serverLevel) {
        AABB box = this.getBoundingBox().inflate(FOOTFALL_RADIUS, FOOTFALL_RADIUS * 0.5, FOOTFALL_RADIUS);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canBeShakenByFootfall)) {
            double distance = target.distanceTo(this);
            if (distance > FOOTFALL_RADIUS) continue;

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            double nearness = 1.0 - Math.max(distance, FOOTFALL_NEAR_PLATEAU) / FOOTFALL_RADIUS;
            double hop = FOOTFALL_HOP * nearness * footing;
            if (hop < 0.01) continue;

            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x, Math.max(motion.y, hop), motion.z);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    private boolean canBeShakenByFootfall(LivingEntity target) {
        if (target == this || target.getRootVehicle() == this) return false;
        if (target instanceof ElephantEntity) return false;
        if (target instanceof OWWaterEntity || target instanceof WaterAnimal) return false;

        MobCategory category = target.getType().getCategory();
        if (category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE) return false;

        if (target.isInWater() || target.isInLava()) return false;

        return target.onGround() || target.verticalCollision;
    }

    private void tickShoulderBash() {
        int timer = this.entityData.get(SHOULDER_BASH_TIMER);
        if (timer <= 0) return;

        int elapsed = OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS - timer;
        int windup = OWAttacksConstants.Elephant.SHOULDER_BASH_WINDUP_TICKS;
        int dashEnd = windup + OWAttacksConstants.Elephant.SHOULDER_BASH_DASH_TICKS;

        // La ruée se pilote des DEUX côtés, comme le plongeon du kangourou : sur une monture, c'est
        // le client du cavalier qui fait autorité sur la position, et une vitesse posée par le seul
        // serveur serait écrasée au paquet suivant. Seul le minuteur, lui, avance côté serveur.
        if (elapsed >= windup && elapsed < dashEnd) {
            Vec3 dir = getBashDirection(getShoulderBashSide());
            double speed = OWAttacksConstants.Elephant.SHOULDER_BASH_DASH_SPEED * shoulderBashDashFalloff(elapsed - windup);
            double lift = elapsed == windup ? OWAttacksConstants.Elephant.SHOULDER_BASH_LIFT : this.getDeltaMovement().y;
            this.setDeltaMovement(dir.x * speed, lift, dir.z * speed);
            this.hasImpulse = true;
        } else if (elapsed >= dashEnd) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }

        if (this.level().isClientSide()) return;

        if (elapsed == windup) applyShoulderBashDamage();

        this.entityData.set(SHOULDER_BASH_TIMER, timer - 1);
    }

    /**
     * Profil de la ruée : plein régime sur les deux tiers, puis extinction douce. Une coupure nette
     * en fin de course arrêtait la masse comme un mur et ruinait tout le poids du geste.
     */
    private static double shoulderBashDashFalloff(int dashTick) {
        int dash = OWAttacksConstants.Elephant.SHOULDER_BASH_DASH_TICKS;
        float t = (float) dashTick / dash;
        if (t < 0.66f) return 1.0;
        return Mth.clamp(1.0 - (t - 0.66f) / 0.34f, 0.0, 1.0);
    }

    private void tickEarthquake() {
        int tick = this.entityData.get(EARTHQUAKE_TICK);
        if (tick <= 0) return;

        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;

        if (tick == windup && !earthquakeImpactDone) {
            earthquakeImpactDone = true;
            executeEarthquakeImpact();
        }

        int elapsed = tick - windup;

        if (elapsed > 0 && elapsed % OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL == 0
                && this.level() instanceof ServerLevel serverLevel) {
            pulseEarthquake(serverLevel);
        }

        int total = Math.max(OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS,
                windup + OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS);

        if (tick >= total) {
            cancelEarthquake();
            return;
        }

        this.entityData.set(EARTHQUAKE_TICK, tick + 1);
    }

    private void handleCall() {
        if (this.isCalling() && this.tickCount - callAnimationStartTime > CALL_DURATION) {
            this.setCalling(false);
        }

        if (callCooldown > 0) {
            callCooldown--;
            return;
        }

        callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

        if (!canPlayIdleAnimation()) return;

        this.setCalling(true);
        this.callAnimationStartTime = this.tickCount;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL,
                2.5f, isBaby() ? 1.4f : (float) OWUtils.generateRandomInterval(0.9, 1.05));
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return Mth.ceil((fallDistance - FALL_SAFE_DISTANCE) * multiplier * FALL_DAMAGE_MULTIPLIER);
    }

    @Override
    protected boolean isImmobile() {
        return this.isEarthquakeGesture() || isSlammingCombo();
    }

    public boolean isSlammingCombo() {
        return this.isCombo() && this.getComboAttack() == 3;
    }

    /**
     * Ni enchaînement pendant le séisme, ni pendant que la trompe travaille.
     *
     * <p>Le jet occupe la trompe et la tête, qui portent aussi bien le combo que le séisme : les
     * laisser partir ensemble mêlait deux gestes sur les mêmes os, et permettait surtout d'arroser
     * en frappant sans jamais rien y perdre.</p>
     */
    @Override
    public boolean canStartCombo() {
        return !isEarthquakeGesture() && !isSprayingWater() && !isFillingTrunk();
    }

    @Override
    public boolean canUseUltimate() {
        return !isSprayingWater() && !isFillingTrunk() && super.canUseUltimate();
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        // Dernier verrou côté données : même si un chemin oubliait d'interroger canStartCombo,
        // l'enchaînement ne peut pas s'armer pendant que la bête est occupée.
        if (isCombo && !canStartCombo()) return;
        super.setCombo(isCombo, numberOfAttacks);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        if (this.level().isClientSide() || entity == null) return;
        if (entity == this.getControllingPassenger()) return;
        if (entity instanceof Player player && player.isCreative()) return;

        if (comboAttack == 3) {
            entity.push(0, COMBO_SLAM_LAUNCH, 0);
            entity.hurtMarked = true;
            return;
        }

        Vec3 sweep = getBashDirection(comboAttack == 1 ? -1 : 1);
        entity.push(sweep.x * COMBO_SWEEP_KNOCKBACK, COMBO_SWEEP_LIFT, sweep.z * COMBO_SWEEP_KNOCKBACK);
        entity.hurtMarked = true;
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (this.isImmobile()) {
            riddenSpeed = 0f;
            chargeMemory = 0f;
            chargeDisplay = 0f;
            return 0f;
        }

        float target = (this.isCombo() && player.zza == 0) ? 0f : super.getRiddenSpeedVehicle(player);

        boolean stalled = isStalled();

        if (stalled) chargeMemory = 0f;
        else if (this.isRunning()) chargeMemory = getChargeRamp();
        else chargeMemory *= CHARGE_MEMORY_DECAY;

        float response = Math.abs(target) >= Math.abs(riddenSpeed)
                ? RIDDEN_ACCEL_RESPONSE
                : Mth.lerp(chargeMemory, RIDDEN_BRAKE_RESPONSE, RIDDEN_BRAKE_RESPONSE_CHARGED);

        riddenSpeed += (target - riddenSpeed) * response;
        if (Math.abs(riddenSpeed) < 1.0e-4f) riddenSpeed = 0f;

        float charge = Math.min(FULL_CHARGE, this.getAcceleration());
        chargeDisplay = (stalled || charge >= chargeDisplay)
                ? charge
                : chargeDisplay + (charge - chargeDisplay) * response;

        return riddenSpeed;
    }

    @Override
    public float getChargeDisplay() {
        return chargeDisplay;
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * SEAT_HEIGHT * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        boolean backSeat = this.getPassengers().indexOf(passenger) > 0;
        double forward = (SEAT_FORWARD - (backSeat ? SECOND_SEAT_BACK : 0)) * this.getScale();

        Vec3 seatOffset = new Vec3(0, 0, forward)
                .yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);

        if (passenger instanceof LivingEntity living) {
            float facing = Mth.wrapDegrees(this.yBodyRot + (backSeat ? 180f : 0f));
            living.yBodyRot = facing;
            living.yBodyRotO = facing;
        }
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
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseElephantVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(12, 20);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private ElephantVariant chooseElephantVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 20) return ElephantVariant.PINK;
        if (roll < 50) return ElephantVariant.GREY;
        return ElephantVariant.DEFAULT;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (EARTHQUAKE_TICK.equals(accessor) && !isEarthquakeGesture() && this.level().isClientSide()) {
            earthquakeAnimationState.stop();
        }
    }

    public static int computeBashSide(float riderYaw, float bodyYaw) {
        return Mth.wrapDegrees(riderYaw - bodyYaw) >= 0 ? 1 : -1;
    }

    public Vec3 getBashDirection(int side) {
        return Vec3.directionFromRotation(0f, this.yBodyRot + 90f * side);
    }

    public void performShoulderBash() {
        if (this.level().isClientSide()) return;
        if (isShoulderBashing() || isEarthquakeGesture()) return;
        if (shoulderBashCooldown > 0) return;

        float cost = OWAttacksConstants.Elephant.SHOULDER_BASH_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            cancelShoulderBash();
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        LivingEntity rider = this.getControllingPassenger();
        int side = computeBashSide(rider != null ? rider.getYRot() : this.getYRot(), this.yBodyRot);

        this.entityData.set(SHOULDER_BASH_SIDE, side);
        this.entityData.set(SHOULDER_BASH_TIMER, OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS);
        shoulderBashCooldown = OWAttacksConstants.Elephant.SHOULDER_BASH_COOLDOWN_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL,
                2.0f, (float) OWUtils.generateRandomInterval(0.85, 1.0));
    }

    private void tickClientBashElapsed() {
        if (!isShoulderBashing()) {
            clientBashElapsed = -1;
            return;
        }
        // Le geste vient de commencer : on se cale sur ce que le serveur annonce, puis on avance
        // seul. Un simple recalage doux corrige ensuite les dérives éventuelles sans à-coup.
        int serverElapsed = OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS - getShoulderBashTimer();
        if (clientBashElapsed < 0) {
            clientBashElapsed = serverElapsed;
        } else {
            clientBashElapsed++;
            if (Math.abs(clientBashElapsed - serverElapsed) > 2) clientBashElapsed = serverElapsed;
        }
    }

    /**
     * Rend la main sur la vélocité pendant le coup d'épaule.
     *
     * <p>Sans cet aiguillage, {@code OWEntity#travelRidden} réécrit chaque tick la vitesse de la
     * monture en {@code direction du regard × vitesse de marche} : le déport latéral était posé puis
     * effacé dans le même tick, et l'éléphant ne bougeait pas d'un pouce. C'est la même échappatoire
     * que celle du Pilon Tellurique du kangourou.</p>
     */
    @Override
    protected boolean isLeapingVehicle() {
        return isShoulderBashing() || super.isLeapingVehicle();
    }

    public void cancelShoulderBash() {
        if (this.level().isClientSide()) return;
        this.entityData.set(SHOULDER_BASH_TIMER, 0);
        this.entityData.set(SHOULDER_BASH_SIDE, 1);
    }

    // ── Jet de Trompe ────────────────────────────────────────────────────────

    /**
     * La trompe suit-elle le curseur en ce moment ?
     *
     * <p>Hors de ces cas, on laisse les animations clés en disposer entièrement : un éléphant
     * sauvage ou monté sur le coup d'épaule ne doit pas voir sa trompe changer de comportement.</p>
     */
    private boolean isTrunkAimActive() {
        // Uniquement pendant l'action, jamais sur la simple sélection de la carte. Le passage à la
        // pose de visée demande une seconde : équiper le Jet de Trompe faisait donc lentement
        // retomber la trompe hors de son animation de repos, sans que rien ne se passe par
        // ailleurs. On ne quitte les animations clés qu'au moment où l'on s'en sert.
        return isSprayingWater() || isFillingTrunk();
    }

    /**
     * Visée de la trompe, côté client uniquement.
     *
     * <p>Elle ne colle pas au curseur, elle le <b>rejoint</b> : une poursuite amortie, sur laquelle
     * se superposent deux sinusoïdes de périodes incommensurables. Rien ne se répète donc jamais à
     * l'identique, et la trompe garde son air vivant tout en restant du côté où l'on regarde.</p>
     */
    /** Profondeur de la nappe sous la base de la trompe, mise à jour par intermittence. */
    private double fillDrop = 0.0;
    /** Pointe de la trompe réellement dessinée, relevée par le rendu à chaque image. */
    public Vec3 clientTrunkTip = null;
    /** Consignes du régulateur d'aspiration : plongée de la trompe et abaissement de l'encolure. */
    private float fillPitch = 45f;
    private float fillHeadDip = 0f;
    private boolean wasFillingTrunk = false;

    /** Gain du régulateur : degrés de correction par bloc d'écart, borné par tick. */
    private static final float FILL_GAIN = 22f;
    private static final float FILL_MAX_STEP = 9f;
    /** On vise légèrement SOUS la surface : une pointe posée pile dessus semble l'effleurer. */
    private static final double FILL_TARGET_DEPTH = 0.25;

    /**
     * Ajuste la plongée jusqu'à ce que la pointe touche vraiment l'eau.
     *
     * <p>La trigonométrie seule ne pouvait pas y arriver : elle partait d'une base de trompe
     * estimée, ignorait l'échelle du modèle et la courbure que lui donne la répartition sur deux
     * segments. On mesure donc l'écart réel entre la pointe dessinée et la surface, et on le
     * referme — le rendu renseigne la position, le tick corrige la consigne, et la boucle converge
     * en quelques dixièmes de seconde quelle que soit la géométrie.</p>
     *
     * <p>Le tangage travaille en premier ; l'encolure ne reprend que ce qu'il ne peut plus absorber
     * une fois à bout de course. C'est bien la tête qui va chercher les derniers centimètres.</p>
     */
    private void tickFillReach() {
        if (this.tickCount % 5 == 0 || !wasFillingTrunk) {
            Double surface = findFillSurfaceY();
            fillDrop = surface == null ? Double.NaN : surface;
        }
        if (Double.isNaN(fillDrop)) return;

        double tipY;
        if (clientTrunkTip != null) {
            tipY = clientTrunkTip.y;
        } else {
            // Première image : on part de l'estimation géométrique, la mesure prendra la suite.
            tipY = getTrunkBase().y - TRUNK_LENGTH * Math.sin(Math.toRadians(fillPitch));
        }

        double error = tipY - (fillDrop - FILL_TARGET_DEPTH);
        float step = (float) Mth.clamp(error * FILL_GAIN, -FILL_MAX_STEP, FILL_MAX_STEP);

        float before = fillPitch;
        fillPitch = Mth.clamp(fillPitch + step, -30f, 90f);
        float leftover = step - (fillPitch - before);

        fillHeadDip = Mth.clamp(fillHeadDip + leftover * 0.7f, -18f, 28f);
    }

    /**
     * Angle de plongée nécessaire pour que le bout de la trompe touche l'eau.
     *
     * <p>Trigonométrie directe : une trompe de deux blocs inclinée de {@code a} descend de
     * {@code 2 × sin(a)}. On inverse. Au-delà de deux blocs de dénivelé la trompe seule ne suffit
     * plus, et c'est la tête qui va chercher le reste (cf. {@link #getTrunkHeadDip}).</p>
     *
     * <p>La nappe n'est ré-échantillonnée que toutes les cinq images : une bête qui boit ne se
     * déplace guère, et la recherche balaie une cinquantaine de colonnes.</p>
     */

    /**
     * Abaissement supplémentaire de la tête pendant l'aspiration, en degrés.
     *
     * <p>Il n'entre en jeu que lorsque la trompe touche à sa limite d'allonge : la bête baisse
     * alors l'encolure pour aller chercher les derniers centimètres, comme elle le ferait pour
     * boire dans un trou. Négatif si l'eau est plus haute qu'elle — elle relève la tête.</p>
     */
    public float getTrunkHeadDip(float partial) {
        return Mth.lerp(partial, trunkHeadDipO, trunkHeadDip);
    }

    private void tickTrunkAim() {
        trunkAimYawO = trunkAimYaw;
        trunkAimPitchO = trunkAimPitch;
        trunkSwellO = trunkSwell;
        trunkAimWeightO = trunkAimWeight;
        trunkHeadDipO = trunkHeadDip;

        // Le régulateur d'aspiration tient les deux consignes ; on ne fait ici que les rejoindre
        // en douceur, et les relâcher dès que la bête cesse de puiser.
        if (isFillingTrunk()) {
            if (!wasFillingTrunk) {
                fillPitch = 45f;
                fillHeadDip = 0f;
            }
            tickFillReach();
            wasFillingTrunk = true;
        } else {
            wasFillingTrunk = false;
            fillHeadDip = 0f;
        }
        trunkHeadDip += (fillHeadDip - trunkHeadDip) * 0.25f;

        float targetWeight = isTrunkAimActive() ? 1f : 0f;
        trunkAimWeight += (targetWeight - trunkAimWeight) * 0.12f;

        LivingEntity rider = this.getControllingPassenger();
        float targetYaw;
        float targetPitch;

        if (isFillingTrunk()) {
            // Elle plonge vers l'eau, quoi que regarde le cavalier — et exactement d'autant qu'il
            // faut pour l'atteindre, au lieu d'un angle figé qui tombait à côté dès que la nappe
            // n'était pas à la profondeur prévue.
            targetYaw = 0f;
            targetPitch = fillPitch;
        } else {
            float aimYaw = rider != null ? rider.getYRot() : this.getYRot();
            float aimPitch = rider != null ? rider.getXRot() : this.getXRot();
            // Débattement large : la trompe doit pouvoir désigner franchement les côtés et le sol,
            // pas se contenter d'incliner poliment la pointe. Le relevé la tient au-dessus de la
            // ligne de visée — c'est le même que celui du jet, sans quoi les deux divergeraient.
            targetYaw = Mth.clamp(Mth.wrapDegrees(aimYaw - this.yBodyRot), -80f, 80f);
            targetPitch = Mth.clamp(aimPitch - OWAttacksConstants.Elephant.WATER_SPRAY_LIFT_DEGREES, -70f, 80f);
        }

        // Deux régimes bien distincts.
        //
        // Au repos, la trompe FLÂNE autour du curseur : elle en garde le voisinage sans jamais s'y
        // poser, avec une poursuite très molle et une errance ample et lente. C'est ce qui lui donne
        // son air d'appendice vivant plutôt que de viseur.
        //
        // Pendant le jet elle se verrouille : les gouttes partent selon son axe réel, donc le
        // moindre écart se lirait comme un jet qui rate sa cible.
        float t = this.tickCount;
        float wander;
        // Le lacet a sa propre amplitude : pendant le jet, la trompe balaie franchement de gauche
        // à droite alors qu'elle reste sage en hauteur. C'est ce balayage horizontal qui donne
        // l'arrosage un peu approximatif d'une lance tenue à bout de trompe ; un vagabondage
        // vertical de même ampleur, lui, ferait piquer et cabrer le jet sans arrêt.
        float wanderYaw;
        float follow;
        if (isSprayingWater()) {
            wander = 0.15f;
            wanderYaw = 0.55f;
            follow = 0.34f;
        } else if (isFillingTrunk()) {
            wander = 0.50f;
            wanderYaw = 0.50f;
            follow = 0.18f;
        } else {
            // Franchement vague : la trompe ne vise pas, elle occupe le voisinage du curseur. Une
            // poursuite très lente laisse l'errance dominer, et c'est elle qu'on doit voir.
            wander = 2.7f;
            wanderYaw = 2.7f;
            follow = 0.032f;
        }

        targetYaw += wanderYaw * (7.0f * Mth.sin(t * 0.037f)
                + 3.5f * Mth.sin(t * 0.091f)
                + 5.0f * Mth.sin(t * 0.017f)
                + 4.0f * Mth.sin(t * 0.0071f));
        targetPitch += wander * (5.0f * Mth.sin(t * 0.053f)
                + 2.5f * Mth.sin(t * 0.113f)
                + 3.5f * Mth.sin(t * 0.023f)
                + 3.0f * Mth.sin(t * 0.0089f));
        trunkAimYaw += (targetYaw - trunkAimYaw) * follow;
        trunkAimPitch += (targetPitch - trunkAimPitch) * follow;

        // Le volume de la trompe suit la réserve, en permanence : une bête qui vient de faire le
        // plein garde sa trompe gonflée en marchant, et elle ne dégonfle qu'au fil du jet. Seule
        // l'aspiration y ajoute un battement, le temps qu'elle pompe.
        float targetSwell = getTrunkWaterRatio();
        if (isFillingTrunk()) targetSwell += 0.22f * Mth.sin(t * 0.45f);
        trunkSwell += (targetSwell - trunkSwell) * 0.2f;
    }

    public float getTrunkAimYaw(float partial) { return Mth.lerp(partial, trunkAimYawO, trunkAimYaw); }

    public float getTrunkAimPitch(float partial) { return Mth.lerp(partial, trunkAimPitchO, trunkAimPitch); }

    public float getTrunkSwell(float partial) { return Mth.lerp(partial, trunkSwellO, trunkSwell); }

    public float getTrunkAimWeight(float partial) { return Mth.lerp(partial, trunkAimWeightO, trunkAimWeight); }

    /** L'éléphant porte deux cartes secondaires : le coup d'épaule et le jet de trompe. */
    @Override
    public int getSecondaryAttackCount() {
        return 2;
    }

    @Override
    protected void onSecondaryAttackChanged() {
        stopTrunkAction();
    }

    public int getTrunkWater() { return this.entityData.get(TRUNK_WATER); }

    public int getTrunkMode() { return this.entityData.get(TRUNK_MODE); }

    public boolean isSprayingWater() { return getTrunkMode() == TRUNK_SPRAYING; }

    public boolean isFillingTrunk() { return getTrunkMode() == TRUNK_FILLING; }

    public float getTrunkWaterRatio() {
        return (float) getTrunkWater() / OWAttacksConstants.Elephant.WATER_SPRAY_CAPACITY;
    }

    /**
     * De l'eau est-elle à portée de trompe ?
     *
     * <p>Synchronisé plutôt que recalculé par le client : la carte du HUD s'en sert pour se griser,
     * et elle est redessinée à chaque image — sonder cent cinquante blocs soixante fois par seconde
     * pour une information qui bouge à peine n'aurait aucun sens. Le serveur la rafraîchit dix fois
     * moins souvent, ce qui suffit largement pour une bête qui marche.</p>
     */
    public boolean isWaterNearby() { return this.entityData.get(TRUNK_WATER_NEARBY); }

    /** La carte du jet est-elle utilisable : de l'eau en réserve, ou de quoi en puiser ? */
    public boolean canUseTrunkWater() { return getTrunkWater() > 0 || isWaterNearby(); }

    /**
     * Base de la trompe, en coordonnées du monde.
     *
     * <p>Volontairement approximative : c'est l'origine du jet côté <b>règles</b>, celle qui décide
     * de ce qui est touché. Le panache de gouttes, lui, part de la pointe réellement animée, calculée
     * image par image à partir des transformations d'os (cf. {@code ElephantTrunkSprayLayer}) — les
     * deux ne peuvent pas coïncider puisque la trompe ondule autour de la ligne de visée.</p>
     */
    public Vec3 getTrunkBase() {
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        return this.position().add(forward.scale(2.0)).add(0, this.getBbHeight() * 0.62, 0);
    }

    /**
     * Direction du jet : le regard du cavalier fait foi, relevé du même angle que la trompe.
     *
     * <p>Le relevé doit être appliqué ici aussi, et pas seulement à la pose : les gouttes suivent
     * l'axe dessiné de la trompe, donc les deux doivent partager le même angle sous peine de voir
     * l'eau tomber ailleurs qu'où elle est dessinée.</p>
     */
    public Vec3 getSprayDirection() {
        LivingEntity rider = this.getControllingPassenger();
        float pitch = rider != null ? rider.getXRot() : this.getXRot();
        float yaw = rider != null ? rider.getYRot() : this.yBodyRot;
        return Vec3.directionFromRotation(pitch - OWAttacksConstants.Elephant.WATER_SPRAY_LIFT_DEGREES, yaw);
    }

    /**
     * Démarre l'action de trompe au clic droit maintenu : elle aspire si de l'eau est à portée et
     * qu'il reste de la place, elle pulvérise sinon. Une seule commande pour les deux gestes.
     */
    public void startTrunkAction() {
        if (this.level().isClientSide()) return;
        if (isEarthquakeGesture() || isShoulderBashing() || this.isCombo()) return;

        boolean canFill = getTrunkWater() < OWAttacksConstants.Elephant.WATER_SPRAY_CAPACITY
                && isWaterInTrunkReach();

        if (canFill) {
            this.entityData.set(TRUNK_MODE, TRUNK_FILLING);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.NEUTRAL, 0.7f, 1.4f);
        } else if (getTrunkWater() > 0) {
            // Aucun cri au déclenchement : le jet s'annonce par son propre bruit d'eau, entretenu
            // tant qu'il dure (cf. tickTrunkWater). Un barrissement par pression sur le clic droit
            // rendait la moindre giclée tonitruante.
            this.entityData.set(TRUNK_MODE, TRUNK_SPRAYING);
        }
    }

    public void stopTrunkAction() {
        if (this.level().isClientSide()) return;
        this.entityData.set(TRUNK_MODE, TRUNK_IDLE);
        lavaTargetPos = null;
        lavaSoakTicks = 0;
    }

    /** Longueur de la trompe déployée, en blocs : deux segments de seize unités de modèle. */
    private static final double TRUNK_LENGTH = 2.0;

    /**
     * Hauteur de la surface d'eau la plus haute à portée de trompe, ou {@code null} s'il n'y en a
     * aucune.
     *
     * <p>On retient la <b>surface</b> et non le bloc : une eau à mi-hauteur ne se puise pas à la
     * même profondeur qu'une source pleine, et c'est précisément l'écart que la trompe doit
     * rattraper. Chaque colonne est parcourue de haut en bas et abandonnée dès sa première eau —
     * ce qui donne bien le dessus de la nappe et non son fond.</p>
     */
    private Double findFillSurfaceY() {
        Vec3 base = getTrunkBase();
        int reach = (int) Math.ceil(OWAttacksConstants.Elephant.WATER_SPRAY_FILL_REACH);
        BlockPos centre = BlockPos.containing(base);
        double best = Double.NEGATIVE_INFINITY;

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (int dy = 1; dy >= -reach * 2; dy--) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    var fluid = this.level().getFluidState(pos);
                    if (!fluid.is(FluidTags.WATER)) continue;
                    best = Math.max(best, pos.getY() + fluid.getHeight(this.level(), pos));
                    break;
                }
            }
        }
        return best == Double.NEGATIVE_INFINITY ? null : best;
    }

    private boolean isWaterInTrunkReach() {
        if (this.isInWater()) return true;

        Vec3 base = getTrunkBase();
        double reach = OWAttacksConstants.Elephant.WATER_SPRAY_FILL_REACH;
        // La trompe plonge : on sonde surtout vers le bas, pas une sphère centrée sur la tête.
        BlockPos min = BlockPos.containing(base.x - reach, base.y - reach * 2.0, base.z - reach);
        BlockPos max = BlockPos.containing(base.x + reach, base.y + 0.5, base.z + reach);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (this.level().getFluidState(pos).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    private void tickTrunkWater() {
        int mode = getTrunkMode();
        int water = getTrunkWater();
        int capacity = OWAttacksConstants.Elephant.WATER_SPRAY_CAPACITY;

        // Rafraîchi dix fois par seconde au plus, et seulement sous un cavalier : c'est la seule
        // situation où l'information sert (grisage de la carte).
        if (this.tickCount % 10 == 0) {
            boolean nearby = this.getControllingPassenger() != null && isWaterInTrunkReach();
            if (nearby != isWaterNearby()) this.entityData.set(TRUNK_WATER_NEARBY, nearby);
        }

        // Le cavalier a lâché la bête sans lâcher le clic : personne ne fermera le robinet.
        if (mode != TRUNK_IDLE && this.getControllingPassenger() == null) {
            stopTrunkAction();
            return;
        }

        // Un geste lourd prend le pas sur la trompe : on ne pulvérise pas en plein séisme.
        if (isEarthquakeGesture() || isShoulderBashing()) {
            if (mode != TRUNK_IDLE) stopTrunkAction();
            mode = TRUNK_IDLE;
        }

        if (mode == TRUNK_FILLING) {
            if (water >= capacity || !isWaterInTrunkReach()) {
                stopTrunkAction();
            } else {
                this.entityData.set(TRUNK_WATER,
                        Math.min(capacity, water + OWAttacksConstants.Elephant.WATER_SPRAY_FILL_PER_TICK));
            }
        } else if (mode == TRUNK_SPRAYING) {
            if (water <= 0) {
                stopTrunkAction();
            } else {
                if (this.tickCount % OWAttacksConstants.Elephant.WATER_SPRAY_COST_INTERVAL == 0) {
                    this.entityData.set(TRUNK_WATER, Math.max(0, water - 1));
                }
                if (this.level() instanceof ServerLevel serverLevel) performTrunkSpray(serverLevel);

                // Bruit d'eau entretenu : la brasse sur place, relancée assez serré pour qu'on
                // n'entende plus les coutures — un « pschhh » continu plutôt qu'un clapotis
                // périodique. Le pas de 3 ticks se marie mal avec la cadence de consommation
                // (2 ticks), et c'est voulu : les deux ne se synchronisent pas, ce qui évite un
                // battement régulier à l'oreille. La hauteur varie légèrement à chaque relance.
                if (this.tickCount % 3 == 0) {
                    float pitch = 1.45f + this.getRandom().nextFloat() * 0.35f;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.PLAYER_SWIM, SoundSource.NEUTRAL, 0.60f, pitch);
                }
                if (this.tickCount % 9 == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.WATER_AMBIENT, SoundSource.NEUTRAL, 0.45f, 1.3f);
                }
            }
        } else {
            lavaTargetPos = null;
            lavaSoakTicks = 0;
            if (water > 0 && this.tickCount % OWAttacksConstants.Elephant.WATER_SPRAY_LEAK_INTERVAL == 0) {
                this.entityData.set(TRUNK_WATER, water - 1);
            }
        }
    }

    /**
     * Point de l'arc du jet à {@code s} blocs de course depuis la trompe.
     *
     * <p>Statique et partagée : le serveur s'en sert pour décider de ce qui est touché, le rendu
     * pour semer ses gouttes. Les deux dessinent ainsi rigoureusement la même parabole — c'est la
     * seule façon que l'eau tombe là où on la voit tomber.</p>
     */
    public static Vec3 sprayArcPoint(Vec3 origin, Vec3 dir, double s) {
        double drop = OWAttacksConstants.Elephant.WATER_SPRAY_DROOP * s * s;
        return origin.add(dir.scale(s)).subtract(0, drop, 0);
    }

    /** Tangente à l'arc : direction réelle des gouttes à cette distance, de plus en plus piquée. */
    public static Vec3 sprayArcTangent(Vec3 dir, double s) {
        return dir.subtract(0, 2.0 * OWAttacksConstants.Elephant.WATER_SPRAY_DROOP * s, 0).normalize();
    }

    /**
     * Longueur réellement parcourue par le jet avant qu'il ne rencontre un obstacle.
     *
     * <p>Partagée elle aussi : sans elle, le rendu sèmerait ses gouttes sur toute la trajectoire
     * théorique, y compris sous le sol, là où le serveur a déjà arrêté le jet.</p>
     */
    public static double sprayReach(Level level, Vec3 origin, Vec3 dir) {
        double range = OWAttacksConstants.Elephant.WATER_SPRAY_RANGE;
        int steps = OWAttacksConstants.Elephant.WATER_SPRAY_BLOCK_STEPS;
        for (int i = 1; i <= steps; i++) {
            double s = range * i / steps;
            BlockPos pos = BlockPos.containing(sprayArcPoint(origin, dir, s));
            // La lave se laisse arroser : c'est tout l'intérêt, elle n'arrête pas le jet.
            if (level.getBlockState(pos).blocksMotion() && !level.getFluidState(pos).is(FluidTags.LAVA)) {
                return s;
            }
        }
        return range;
    }

    /** Demi-largeur du faisceau à cette distance : fin à la trompe, plus large au bout. */
    public static double sprayRadiusAt(double s) {
        double t = s / OWAttacksConstants.Elephant.WATER_SPRAY_RANGE;
        return OWAttacksConstants.Elephant.WATER_SPRAY_RADIUS * (0.35 + 0.65 * t);
    }

    private void performTrunkSpray(ServerLevel level) {
        Vec3 origin = getTrunkBase();
        Vec3 dir = getSprayDirection();
        double range = OWAttacksConstants.Elephant.WATER_SPRAY_RANGE;
        int steps = OWAttacksConstants.Elephant.WATER_SPRAY_BLOCK_STEPS;

        List<Vec3> arc = new ArrayList<>(steps);
        BlockPos firstLava = null;
        Set<BlockPos> doused = new HashSet<>();

        for (int i = 1; i <= steps; i++) {
            double s = range * i / steps;
            Vec3 point = sprayArcPoint(origin, dir, s);
            BlockPos pos = BlockPos.containing(point);

            // Un point sur trois suffit : les échantillons sont espacés de trois quarts de bloc et
            // le rayon d'arrosage en fait deux et demi, donc la couverture reste continue pour un
            // tiers du coût.
            if (i % 3 == 1) douseAround(level, point, doused);
            if (firstLava == null && level.getFluidState(pos).is(FluidTags.LAVA)) firstLava = pos.immutable();

            arc.add(point);

            // L'eau ne traverse pas les murs : le jet s'arrête au premier obstacle plein. La lave,
            // elle, se laisse arroser — c'est tout l'intérêt.
            if (level.getBlockState(pos).blocksMotion() && !level.getFluidState(pos).is(FluidTags.LAVA)) break;
        }

        tickLavaSoak(level, firstLava);
        pushEntitiesAlongArc(level, origin, dir, arc, range);
    }

    /**
     * Noie tout ce qui brûle autour d'un point de l'arc.
     *
     * <p>Le jet n'éteignait que les blocs qu'il traversait exactement, ce qui obligeait à viser au
     * bloc près : sur un arc en cloche, autant dire au jugé. Il arrose maintenant tout son
     * voisinage. Les positions déjà traitées sont mémorisées, sinon les voisinages de quarante
     * points d'échantillonnage se recouvriraient dix fois.</p>
     */
    private void douseAround(ServerLevel level, Vec3 point, Set<BlockPos> alreadyDone) {
        int r = (int) Math.ceil(OWAttacksConstants.Elephant.WATER_SPRAY_DOUSE_RADIUS);
        double rSqr = OWAttacksConstants.Elephant.WATER_SPRAY_DOUSE_RADIUS
                * OWAttacksConstants.Elephant.WATER_SPRAY_DOUSE_RADIUS;
        BlockPos center = BlockPos.containing(point);

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            if (pos.getCenter().distanceToSqr(point) > rSqr) continue;
            BlockPos immutable = pos.immutable();
            if (!alreadyDone.add(immutable)) continue;
            extinguishAt(level, immutable);
        }
    }

    /**
     * Dosage de la poussée du jet selon la corpulence de la cible, et sa résistance au recul.
     *
     * <p>Le volume de la boîte englobante sert de masse : c'est grossier, mais c'est la seule
     * grandeur que porte n'importe quelle créature, moddée comprise. On en prend la racine carrée
     * — le rapport brut va de un à plusieurs centaines entre une poule et un pachyderme — puis on
     * borne, pour qu'aucune bête ne devienne ni un boulet de canon ni un rocher.</p>
     */
    private static double massPushFactor(LivingEntity target, double minFactor, double maxFactor) {
        double volume = target.getBbWidth() * target.getBbWidth() * target.getBbHeight();
        double factor = Math.sqrt(OWAttacksConstants.Elephant.WATER_SPRAY_PUSH_REFERENCE_VOLUME
                / Math.max(0.05, volume));
        factor = Mth.clamp(factor, minFactor, maxFactor);

        // La résistance au recul est respectée comme pour n'importe quel coup : une créature bardée
        // d'armure ou un boss censé ne pas bouger ne se laisse pas davantage déplacer.
        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        return factor * Math.max(0.0, 1.0 - resistance);
    }

    private static double sprayPushFactor(LivingEntity target) {
        return massPushFactor(target,
                OWAttacksConstants.Elephant.WATER_SPRAY_PUSH_MIN_FACTOR,
                OWAttacksConstants.Elephant.WATER_SPRAY_PUSH_MAX_FACTOR);
    }

    private void pushEntitiesAlongArc(ServerLevel level, Vec3 origin, Vec3 dir, List<Vec3> arc, double range) {
        if (arc.isEmpty()) return;

        AABB box = new AABB(origin, origin);
        for (Vec3 point : arc) box = box.minmax(new AABB(point, point));
        box = box.inflate(OWAttacksConstants.Elephant.WATER_SPRAY_RADIUS);

        double reached = range * arc.size() / OWAttacksConstants.Elephant.WATER_SPRAY_BLOCK_STEPS;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target == this.getControllingPassenger()) continue;

            Vec3 center = target.getBoundingBox().getCenter();
            int hitIndex = -1;
            for (int i = 0; i < arc.size(); i++) {
                double s = range * (i + 1) / OWAttacksConstants.Elephant.WATER_SPRAY_BLOCK_STEPS;
                double allowed = sprayRadiusAt(s) + target.getBbWidth() * 0.5;
                if (arc.get(i).distanceToSqr(center) <= allowed * allowed) {
                    hitIndex = i;
                    break;
                }
            }
            if (hitIndex < 0) continue;

            // Éteindre vaut pour tout le monde, y compris les alliés : c'est un secours, pas une
            // attaque. Seule la bourrade épargne la tribu.
            target.clearFire();

            if (this.isAlliedTo(target)) continue;

            // Ce que l'eau brûle : blaze, enderman, golem de neige, strider. On s'appuie sur le
            // drapeau vanilla plutôt que sur une liste en dur, pour que toute créature du même
            // genre — moddée comprise — y réagisse aussi.
            if (target instanceof Mob mob && mob.isSensitiveToWater()
                    && this.tickCount % OWAttacksConstants.Elephant.WATER_SPRAY_DAMAGE_INTERVAL == 0) {
                target.hurt(this.damageSources().drown(),
                        OWAttacksConstants.Elephant.WATER_SPRAY_SENSITIVE_DAMAGE);
            }

            // Poussée dans le sens réel du jet à cet endroit-là : de face près de la trompe, de
            // plus en plus plongeante à mesure que l'eau retombe. Dosée selon le gabarit de la
            // cible, pour qu'un éléphant ne glisse pas comme une poule.
            double s = range * (hitIndex + 1) / OWAttacksConstants.Elephant.WATER_SPRAY_BLOCK_STEPS;
            double strength = OWAttacksConstants.Elephant.WATER_SPRAY_PUSH * sprayPushFactor(target);
            Vec3 push = sprayArcTangent(dir, s).scale(strength);
            target.setDeltaMovement(target.getDeltaMovement()
                    .add(push.x, Math.max(push.y, 0) + strength * 0.20, push.z));
            target.hurtMarked = true;
        }
    }

    private void extinguishAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(BlockTags.FIRE)) {
            level.removeBlock(pos, false);
            level.levelEvent(null, 1009, pos, 0);
        } else if (state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, false));
            level.levelEvent(null, 1009, pos, 0);
        }
    }

    /**
     * Figeage de la lave : il faut tenir le jet sur le <b>même</b> bloc sans interruption. Dès que
     * la visée change de bloc, le décompte repart de zéro — d'où le fait qu'assécher une coulée
     * demande de s'y appliquer, et vide la trompe bien avant d'en venir à bout.
     */
    private void tickLavaSoak(ServerLevel level, BlockPos lava) {
        if (lava == null) {
            lavaTargetPos = null;
            lavaSoakTicks = 0;
            return;
        }

        if (!lava.equals(lavaTargetPos)) {
            lavaTargetPos = lava;
            lavaSoakTicks = 0;
        }

        lavaSoakTicks++;

        if (lavaSoakTicks % 5 == 0) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    lava.getX() + 0.5, lava.getY() + 1.0, lava.getZ() + 0.5, 6, 0.35, 0.1, 0.35, 0.02);
            level.playSound(null, lava, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
        }

        if (lavaSoakTicks < OWAttacksConstants.Elephant.WATER_SPRAY_LAVA_TICKS) return;

        level.setBlockAndUpdate(lavaTargetPos, Blocks.COBBLESTONE.defaultBlockState());
        level.levelEvent(null, 1501, lavaTargetPos, 0);
        lavaTargetPos = null;
        lavaSoakTicks = 0;
    }

    private void applyShoulderBashDamage() {
        int side = getShoulderBashSide();
        Vec3 dir = getBashDirection(side);
        double radius = OWAttacksConstants.Elephant.SHOULDER_BASH_RADIUS;

        // Le choc s'entend : un impact sourd doublé du barrissement de l'effort.
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.55f, 1.7f);

        Vec3 center = this.position().add(dir.scale(radius * 0.5)).add(0, this.getBbHeight() * 0.5, 0);
        AABB box = new AABB(center, center).inflate(radius * 0.75, this.getBbHeight() * 0.6, radius * 0.75);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target == this.getControllingPassenger()) continue;
            if (this.isAlliedTo(target)) continue;

            target.hurt(this.damageSources().mobAttack(this),
                    (float) (this.getDamage() * OWAttacksConstants.Elephant.SHOULDER_BASH_DAMAGE_RATIO));
            // setDeltaMovement plutôt que push : la poussée s'ajoute à l'élan existant, si bien
            // qu'une cible qui venait vers l'éléphant partait deux fois moins loin que celle qui
            // fuyait. On impose la trajectoire, elle ne dépend plus de ce que faisait la victime —
            // seulement de son gabarit.
            double bashFactor = massPushFactor(target,
                    OWAttacksConstants.Elephant.SHOULDER_BASH_PUSH_MIN_FACTOR,
                    OWAttacksConstants.Elephant.SHOULDER_BASH_PUSH_MAX_FACTOR);
            double bashPush = OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK * bashFactor;

            target.setDeltaMovement(
                    dir.x * bashPush,
                    OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK_LIFT * bashFactor,
                    dir.z * bashPush);
            target.hurtMarked = true;
        }

        OWUtils.spawnServerParticles(this, ParticleTypes.CLOUD, dir.x * 2, 0.4, dir.z * 2, 14, 0.4);

        if (this.level() instanceof ServerLevel serverLevel) {
            spawnSkinParticles(serverLevel,
                    this.getX() + dir.x * 2, this.getY() + this.getBbHeight() * 0.5, this.getZ() + dir.z * 2,
                    18, 0.8, 0.08);
        }
    }

    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);

        if (this.level().isClientSide()) return;
        if (this.attackTimer != timeToHit) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        breakBlocksWithCombo(serverLevel, this.getComboAttack());

        if (this.getComboAttack() != 3) return;

        startShockwave();
    }

    private boolean canBreakTerrain(ServerLevel serverLevel) {
        if (OWArenaManager.isArena(serverLevel)) return false;
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public float maxBreakableHardness() {
        return Math.min(COMBO_BREAK_MAX_HARDNESS, this.getDamage() * COMBO_BREAK_HARDNESS_PER_DAMAGE);
    }

    private void breakBlocksWithCombo(ServerLevel serverLevel, int comboAttack) {
        if (!this.isTame()) return;
        if (!canBreakTerrain(serverLevel)) return;

        double scale = this.getScale();
        Vec3 forward = Vec3.directionFromRotation(0f, this.yBodyRot);
        Vec3 centre = this.position()
                .add(forward.scale(this.getBbWidth() * 0.5 + COMBO_BREAK_REACH * scale));

        AABB zone;
        if (comboAttack == 3) {
            zone = new AABB(centre, centre)
                    .inflate(COMBO_BREAK_COLUMN_HALF * scale,
                            COMBO_BREAK_COLUMN_HEIGHT * scale,
                            COMBO_BREAK_COLUMN_HALF * scale)
                    .move(0, COMBO_BREAK_COLUMN_HEIGHT * scale * 0.55 + COMBO_BREAK_COLUMN_LIFT, 0);
        } else {
            Vec3 side = getBashDirection(comboAttack == 1 ? -1 : 1);
            zone = new AABB(centre, centre)
                    .inflate(COMBO_BREAK_SWEEP_HALF * scale)
                    .move(side.x * COMBO_BREAK_SIDE_OFFSET * scale,
                            this.getBbHeight() * COMBO_BREAK_SWEEP_HEIGHT,
                            side.z * COMBO_BREAK_SIDE_OFFSET * scale);
        }

        float maxHardness = maxBreakableHardness();
        BlockPos.betweenClosedStream(zone).forEach(pos -> breakComboBlock(serverLevel, pos, maxHardness));
    }

    /** Le bois n'est abattu que par la bête qui charge tête baissée, quel qu'en soit le geste. */
    private boolean isWoodwork(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS);
    }

    private void breakComboBlock(ServerLevel serverLevel, BlockPos pos, float maxHardness) {
        BlockState state = serverLevel.getBlockState(pos);

        if (state.isAir()) return;
        // Le combo continue d'ouvrir la terre et la pierre, mais plus le bois : c'est le labour
        // tête baissée qui fait le bûcheron, et il est indisponible avec le Jet de Trompe en main.
        // Sans cette garde, l'éléphant abattait encore des arbres en tapant devant lui.
        if (!canChargeHeadDown() && isWoodwork(state)) return;
        if (state.hasBlockEntity()) return;
        if (!state.getFluidState().isEmpty()) return;

        float hardness = state.getDestroySpeed(serverLevel, pos);
        if (hardness < 0 || hardness > maxHardness) return;
        if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return;

        BlockPos immutable = pos.immutable();
        serverLevel.destroyBlock(immutable, false, this);
        Block.dropResources(state, serverLevel, immutable, null, this, COMBO_BREAK_TOOL);
        OWPlacedBlocks.get(serverLevel).forget(immutable);
    }

    private void startShockwave() {
        Vec3 direction = Vec3.directionFromRotation(0f, this.yBodyRot);
        Vec3 origin = this.position().add(direction.scale(this.getBbWidth() * 0.55));

        if (shockwaves.size() >= MAX_ACTIVE_SHOCKWAVES) shockwaves.remove(0);
        shockwaves.add(new Shockwave(origin, direction, this.getY()));

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL,
                3.5f, (float) OWUtils.generateRandomInterval(0.5, 0.65));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL,
                3.0f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
    }

    private static double surfaceYNear(ServerLevel serverLevel, double x, double z, double fromY) {
        int base = Mth.floor(fromY);

        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(Mth.floor(x), base + dy, Mth.floor(z));
            if (serverLevel.getBlockState(pos).isAir()) continue;
            if (!serverLevel.isEmptyBlock(pos.above())) continue;
            return pos.getY() + 1.0;
        }
        return fromY;
    }

    private void tickShockwave() {
        if (shockwaves.isEmpty()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            shockwaves.clear();
            return;
        }

        double speed = OWAttacksConstants.Elephant.SHOCKWAVE_SPEED;

        Iterator<Shockwave> it = shockwaves.iterator();
        while (it.hasNext()) {
            Shockwave wave = it.next();
            double front = wave.tick * speed;

            if (front > OWAttacksConstants.Elephant.SHOCKWAVE_LENGTH) {
                it.remove();
                continue;
            }

            sweepShockwave(serverLevel, wave, front - speed, front);
            wave.tick++;
        }
    }

    private void sweepShockwave(ServerLevel serverLevel, Shockwave wave, double back, double front) {
        double half = OWAttacksConstants.Elephant.SHOCKWAVE_HALF_WIDTH;
        Vec3 side = new Vec3(-wave.direction.z, 0, wave.direction.x);

        Vec3 axis = wave.origin.add(wave.direction.scale(front));

        wave.y = surfaceYNear(serverLevel, axis.x, axis.z, wave.y);

        for (int lane = -1; lane <= 1; lane++) {
            Vec3 point = axis.add(side.scale(lane * half * 0.7));
            double laneY = surfaceYNear(serverLevel, point.x, point.z, wave.y);

            BlockState ground = serverLevel.getBlockState(
                    BlockPos.containing(point.x, laneY - 0.1, point.z));
            if (ground.isAir()) continue;

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    point.x, laneY + 0.3, point.z, 14, 0.3, 0.2, 0.3, 0.15);
            spawnSkinParticles(serverLevel, point.x, laneY + 0.6, point.z, 6, 0.35, 0.03);
        }

        Vec3 a = new Vec3(wave.origin.x, wave.y, wave.origin.z)
                .add(wave.direction.scale(back));
        Vec3 b = new Vec3(wave.origin.x, wave.y, wave.origin.z)
                .add(wave.direction.scale(front));
        AABB box = new AABB(a, b).inflate(half, 2.5, half);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;

            Vec3 relative = target.position().subtract(
                    new Vec3(wave.origin.x, wave.y, wave.origin.z));
            double along = relative.dot(wave.direction);
            if (along <= back || along > front) continue;
            if (Math.abs(relative.dot(side)) > half) continue;
            if (Math.abs(relative.y) > 3.0) continue;

            Integer lastHit = wave.struck.get(target.getId());
            if (lastHit != null && wave.tick - lastHit < SHOCKWAVE_HIT_INTERVAL) continue;
            wave.struck.put(target.getId(), wave.tick);

            target.invulnerableTime = 0;
            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.SHOCKWAVE_DAMAGE);

            target.push(wave.direction.x * OWAttacksConstants.Elephant.SHOCKWAVE_CARRY,
                    OWAttacksConstants.Elephant.SHOCKWAVE_LAUNCH,
                    wave.direction.z * OWAttacksConstants.Elephant.SHOCKWAVE_CARRY);
            target.hurtMarked = true;
        }
    }

    public boolean activateEarthquake() {
        if (this.level().isClientSide()) return false;
        if (isEarthquaking()) return false;
        if (getUltimateKillCount() < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) return false;

        float cost = OWAttacksConstants.Elephant.EARTHQUAKE_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        cancelShoulderBash();
        resetCombo(0);
        actualAttackNumber = 0;

        earthquakeImpactDone = false;
        this.entityData.set(EARTHQUAKE_TICK, 1);
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL, 3.0f, 0.75f);
        return true;
    }

    private void cancelEarthquake() {
        this.entityData.set(EARTHQUAKE_TICK, 0);
        earthquakeImpactDone = false;
    }

    private void executeEarthquakeImpact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        earthquakeEpicentre = this.position();

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 4.0f, 0.4f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 4.0f, 1.0f);

        spawnSkinParticles(serverLevel, this.getX(), this.getY() + 0.4, this.getZ(), 90, 2.2, 0.28);
    }

    private void pulseEarthquake(ServerLevel serverLevel) {
        double radius = OWAttacksConstants.Elephant.EARTHQUAKE_RADIUS;

        serverLevel.playSound(null, earthquakeEpicentre.x, earthquakeEpicentre.y, earthquakeEpicentre.z,
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 2.5f,
                (float) OWUtils.generateRandomInterval(0.55, 0.75));

        spawnEarthquakeRing(serverLevel, radius);
        collapseScatteredBlocks(serverLevel, radius);
        shakeEntitiesAround(serverLevel, radius);
    }

    private void spawnEarthquakeRing(ServerLevel serverLevel, double radius) {
        if (skinParticle() == null) return;

        for (int i = 0; i < EARTHQUAKE_RING_POINTS; i++) {
            double angle = (Math.PI * 2 * i) / EARTHQUAKE_RING_POINTS;
            double distance = Math.sqrt(this.random.nextDouble()) * radius;

            double x = earthquakeEpicentre.x + Math.cos(angle) * distance;
            double z = earthquakeEpicentre.z + Math.sin(angle) * distance;
            double y = surfaceYNear(serverLevel, x, z, earthquakeEpicentre.y);

            spawnSkinParticles(serverLevel, x, y + 0.3, z, 2, 0.25, 0.05);
        }
    }

    private void collapseScatteredBlocks(ServerLevel serverLevel, double radius) {
        if (!canBreakTerrain(serverLevel)) return;

        BlockPos origin = BlockPos.containing(earthquakeEpicentre);

        for (int attempt = 0; attempt < OWAttacksConstants.Elephant.EARTHQUAKE_BLOCKS_PER_PULSE; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(this.random.nextDouble()) * radius;

            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);

            collapseSurfaceAt(serverLevel, x, z, origin.getY());
        }
    }

    private void collapseSurfaceAt(ServerLevel serverLevel, int x, int z, int aroundY) {
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(x, aroundY + dy, z);
            if (!serverLevel.getBlockState(pos).isAir() || dy == -3) {
                if (breakSurfaceBlock(serverLevel, pos)) spreadCollapse(serverLevel, pos);
                return;
            }
        }
    }

    private void spreadCollapse(ServerLevel serverLevel, BlockPos origin) {
        int spread = 0;

        int first = this.random.nextInt(HORIZONTAL_SPREAD.length);

        for (int step = 0; step < HORIZONTAL_SPREAD.length; step++) {
            Direction direction = HORIZONTAL_SPREAD[(first + step) % HORIZONTAL_SPREAD.length];

            if (spread >= EARTHQUAKE_MAX_SPREAD) return;
            if (this.random.nextFloat() > EARTHQUAKE_SPREAD_CHANCE) continue;

            BlockPos side = origin.relative(direction);
            if (breakSurfaceBlock(serverLevel, side)
                    || breakSurfaceBlock(serverLevel, side.above())
                    || breakSurfaceBlock(serverLevel, side.below())) {
                spread++;
            }
        }
    }

    private boolean breakSurfaceBlock(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.hasBlockEntity()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (!serverLevel.isEmptyBlock(pos.above())) return false;

        float hardness = state.getDestroySpeed(serverLevel, pos);
        if (hardness < 0 || hardness > 3.0f) return false;
        if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return false;

        serverLevel.destroyBlock(pos, false);
        OWPlacedBlocks.get(serverLevel).forget(pos);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                10, 0.3, 0.15, 0.3, 0.1);
        return true;
    }

    private void shakeEntitiesAround(ServerLevel serverLevel, double radius) {
        AABB box = new AABB(earthquakeEpicentre, earthquakeEpicentre).inflate(radius, radius * 0.5, radius);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (target.position().distanceTo(earthquakeEpicentre) > radius) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_DAMAGE);

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            target.push(0, OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_HOP * footing, 0);
            target.hurtMarked = true;
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(122, true);

        if (this.isCalling()) {
            callAnimationState.startIfStopped(this.tickCount);
        } else {
            callAnimationState.stop();
        }

        if (this.isEarthquakeGesture()) {
            earthquakeAnimationState.startIfStopped(this.tickCount);
        } else {
            earthquakeAnimationState.stop();
        }

        setupComboAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isEarthquaking();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (45 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (42 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (34 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof ElephantEntity elephant) {
            elephant.setVariant(ElephantVariant.byId(variant));
            elephant.setInitialVariant(ElephantVariant.byId(variant));
        }
    }

    public ElephantVariant getVariant() {
        return ElephantVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(ElephantVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(ElephantVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(ElephantVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(ElephantVariant.Cosmetics.DEMON.variant);
            case 3 -> this.setSkin(ElephantVariant.Cosmetics.ZOMBIE.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public ElephantVariant getInitialVariant() {
        return ElephantVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(ElephantVariant variant) {
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

    public void setCalling(boolean calling) { this.entityData.set(IS_CALLING, calling); }

    public boolean isCalling() { return this.entityData.get(IS_CALLING); }

    public int getShoulderBashSide() { return this.entityData.get(SHOULDER_BASH_SIDE); }

    public int getShoulderBashTimer() { return this.entityData.get(SHOULDER_BASH_TIMER); }

    public boolean isShoulderBashing() { return this.entityData.get(SHOULDER_BASH_TIMER) > 0; }

    public int getEarthquakeTick() { return this.entityData.get(EARTHQUAKE_TICK); }

    public boolean isEarthquaking() { return this.entityData.get(EARTHQUAKE_TICK) > 0; }

    public boolean isEarthquakeGesture() {
        int tick = getEarthquakeTick();
        return tick > 0 && tick <= OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS;
    }

    public float getEarthquakeShakeIntensity() {
        int tick = getEarthquakeTick();
        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;
        int elapsed = tick - windup;

        if (elapsed < 0 || elapsed > OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS) return 0f;

        int interval = OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL;
        float sincePulse = (float) (elapsed % interval) / interval;
        float floor = OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_FLOOR;

        return OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_INTENSITY
                * (floor + (1f - floor) * (1f - sincePulse));
    }

    public int getSaddleWoolColor(int layer) {
        return this.entityData.get(layer == 0 ? SADDLE_WOOL_0 : SADDLE_WOOL_1);
    }

    @Override
    public void onSaddleEquipped(ItemStack saddle) {
        if (this.level().isClientSide()) return;

        List<Item> wools = saddle.isEmpty() ? null : saddle.get(OWDataComponentTypes.SADDLE_WOOLS.get());
        int primary = NO_TRIBE_WOOL_0;
        int secondary = NO_TRIBE_WOOL_1;

        if (wools != null && wools.size() >= 2) {
            DyeColor first = OWWoolColors.colorOf(wools.get(0));
            DyeColor second = OWWoolColors.colorOf(wools.get(1));
            if (first != null) primary = OWWoolColors.rgb(first);
            if (second != null) secondary = OWWoolColors.rgb(second);
        }

        this.entityData.set(SADDLE_WOOL_0, primary);
        this.entityData.set(SADDLE_WOOL_1, secondary);
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }

    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
        tag.putInt("trunkWater", this.getTrunkWater());
        tag.putInt("saddleWool0", this.getSaddleWoolColor(0));
        tag.putInt("saddleWool1", this.getSaddleWoolColor(1));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) {
            setUltimateKillCount(tag.getInt("ultimateKillCount"));
        }
        this.entityData.set(TRUNK_WATER, Mth.clamp(tag.getInt("trunkWater"), 0,
                OWAttacksConstants.Elephant.WATER_SPRAY_CAPACITY));
        if (tag.contains("saddleWool0")) this.entityData.set(SADDLE_WOOL_0, tag.getInt("saddleWool0"));
        if (tag.contains("saddleWool1")) this.entityData.set(SADDLE_WOOL_1, tag.getInt("saddleWool1"));
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return 7; }

    class ElephantMeleeAttackGoal extends MeleeAttackGoal {

        public ElephantMeleeAttackGoal() {
            super(ElephantEntity.this, 7, true);
        }

        @Override
        public void start() {
            super.start();
            ElephantEntity.this.setMad(true);
            ElephantEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            ElephantEntity.this.setMad(false);
            ElephantEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 4.5;
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
