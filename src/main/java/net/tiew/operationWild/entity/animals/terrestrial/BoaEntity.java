package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWGrabberEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.boa.BoaConstrictGoal;
import net.tiew.operationWild.entity.goals.boa.BoaEatItemGoal;
import net.tiew.operationWild.entity.goals.boa.BoaHypnotizeGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.BoaVariant;
import net.tiew.operationWild.entity.variants.TigerVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class BoaEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity {

    public static final double TAMING_EXPERIENCE = 80.0;

    private BoaTailPart[] parts;

    private double prevChainX = Double.NaN;
    private double prevChainZ = Double.NaN;

    private double smoothedVelX = 0.0;
    private double smoothedVelZ = 0.0;
    private static final double VEL_SMOOTH = 0.25;

    private long digestionStartTick = -1L;
    private float digestionPower = 1.0f;

    private boolean constricting = false;
    private LivingEntity constrictTarget = null;
    private int constrictTimer = 0;
    private int constrictCooldown = 0;
    private Vec3 constrictAnchor = null;
    private Vec3 constrictTargetAnchor = null;
    private float constrictFrozenYaw = Float.NaN;
    private boolean constrictLocked = false;
    private float strangleProgress = 0f;
    private float constrictBreath = 0f;

    private float sitProgress = 0f;
    private static final float SIT_CURL_PER_SEGMENT = 35f;
    private static final float SIT_TRANSITION_STEP = 1f / 20f;

    private static final float RIDDEN_YAW_LAG = 0.30f;
    private static final float RIDDEN_RUN_YAW_LAG = 0.16f;
    private static final float WILD_YAW_LAG = 0.30f;

    private static final float WAVE_FREQ = 2.5f;
    private static final float RIDDEN_RUN_WAVE_FREQ = 1.5f;

    private float smoothedYRot = Float.NaN;
    public final float[] ringBuffer = new float[64];
    public int ringBufferIndex = -1;

    private float wavePhase = 0f;
    private float prevWalkDist = 0f;

    private double clientSmoothedY = Double.NaN;
    private static final double HEAD_Y_SMOOTH = 0.35;

    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> CHILD_UUID = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CHILD_ID = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHILD_ID_2 = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> VENOM_ARMED = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VENOM_COOLDOWN_TICKS = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_GRABBING = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private boolean constrictUltArmed = false;
    private int constrictUltArmedTimer = 0;
    private static final EntityDataAccessor<Float> GRAB_HOLD_X = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GRAB_HOLD_Y = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GRAB_HOLD_Z = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> HYPNOTIZING = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HYPNOSIS_TARGET_ID = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private int hypnosisCooldown = 0;

    private static final int TONG_DURATION = 14;

    public final AnimationState tongAnimationState = new AnimationState();

    public int tongAnimationStartTime = 0;

    private int tongCooldown = (int) OWUtils.generateRandomInterval(100, 200);

    public BoaEntity(EntityType<? extends BoaEntity> type, Level world, float averageScale, int maxSleepBar, int foodWanted) {
        super(type, world, averageScale, maxSleepBar, foodWanted);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.MOVEMENT_SPEED, 0.17).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FOLLOW_RANGE, 25.0).add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new BoaConstrictGoal(this));
        this.goalSelector.addGoal(2, new BoaEntity.BoaMeleeAttackGoal());

        this.goalSelector.addGoal(5, new BoaHypnotizeGoal(this, 18.0, 120, 600, 0.15f, 0.65f));

        this.goalSelector.addGoal(8, new BoaEatItemGoal(this));

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, this.getSpeed() * 6.5f));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, t -> !this.isDigesting()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false, t -> !this.isDigesting()));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Horse.class, 10, true, false, t -> !this.isDigesting()));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, TigerEntity.class, 10, true, false, t -> !this.isDigesting()));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof BoaEntity boa && !boa.isSleeping() && !boa.isNapping()) {
                    super.tick();
                }
            }
        };

        registerBehaviorGoals(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(CHILD_UUID, java.util.Optional.empty());
        builder.define(CHILD_ID, -1);
        builder.define(CHILD_ID_2, -1);
        builder.define(VENOM_ARMED, false);
        builder.define(VENOM_COOLDOWN_TICKS, 0);
        builder.define(IS_GRABBING, false);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(GRAB_TIMEOUT, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(HYPNOTIZING, false);
        builder.define(HYPNOSIS_TARGET_ID, -1);
        builder.define(GRAB_HOLD_X, 0f);
        builder.define(GRAB_HOLD_Y, 0f);
        builder.define(GRAB_HOLD_Z, 0f);
    }

    @Override
    public int getEntityColor() {
        return 0x566022;
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
        return List.of(Animal.class, Player.class, TigerEntity.class, Horse.class);
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

    /**
     * Assise figée sur le tick : {@code positionRider} la prend au milieu des deux positions du
     * segment de queue porteur, puis la lisse lui-même via {@code smoothSeatClient}. Le rattrapage
     * générique n'a donc rien à corriger ici — il ne ferait qu'annuler l'interpolation du moteur.
     */
    @Override
    public boolean riderSeatIsFrameAccurate() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        return this.isVehicle() ? 0.1f : 0.05f;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.BOA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getTheoreticalScale() {
        return 8;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        float multplier = this.isInFight() ? 1.15f : 1.0f;
        return 2.2f * multplier;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        float multplier = this.isInFight() ? 1.15f : 1.0f;
        return 1.5f * multplier;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1.0f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 1.0f;
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
        return OWItems.BOA_SADDLE.get();
    }

    @Override
    public float getMaxVitalEnergy() {
        return 250.0f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.5f;
    }

    /**
     * Souffle du boa : trente secondes d'apnée.
     *
     * <p>Il tenait deux minutes et demie, ce qui rendait sa jauge d'oxygène muette : elle ne perdait
     * une bulle qu'au bout de vingt secondes, si bien qu'un plongeon ordinaire ne la faisait pas
     * bouger d'un pixel — pendant que la pression des profondeurs, elle, entamait la bête au bout de
     * quelques secondes. Le joueur voyait des dégâts sans cause lisible. Une réserve à l'échelle du
     * serpent rend la jauge parlante : une bulle toutes les quatre secondes.</p>
     */
    @Override
    public int getMaxAirSupply() {
        return 600;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 20 : 10;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
    }

    @Override
    public void tick() {
        super.tick();

        // Serpents nés avant la réduction de la réserve : leur souffle sauvegardé dépasse le
        // nouveau plafond, et la jauge resterait pleine deux minutes durant avant de daigner bouger.
        if (!this.level().isClientSide() && this.getAirSupply() > this.getMaxAirSupply()) {
            this.setAirSupply(this.getMaxAirSupply());
        }

        if (!constricting) createCombo(16, 10, OWSounds.BOA_HITTING.get(), 2.0, 2.5, 1.25, false, 0.25f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) {
            setupAnimationState();
            double actualY = this.getY();
            if (Double.isNaN(clientSmoothedY) || actualY <= clientSmoothedY) {
                clientSmoothedY = actualY;
            } else {
                clientSmoothedY += (actualY - clientSmoothedY) * HEAD_Y_SMOOTH;
            }
        }
        if (this.isInResurrection()) this.setSleeping(true);

        if (Float.isNaN(this.smoothedYRot)) {
            this.smoothedYRot = this.getYRot();
        }
        {
            final float lag;
            if (this.isVehicle() && this.getControllingPassenger() != null) {
                lag = this.isRunning() ? RIDDEN_RUN_YAW_LAG : RIDDEN_YAW_LAG;
            } else if (this.getTarget() != null) {
                lag = 0.5f;
            } else {
                lag = WILD_YAW_LAG;
            }
            float visualDelta = Mth.wrapDegrees(this.getYRot() - this.smoothedYRot);
            this.smoothedYRot = Mth.wrapDegrees(this.smoothedYRot + visualDelta * lag);
            this.yBodyRot = this.smoothedYRot;
        }
        this.yHeadRot = Mth.clamp(this.yHeadRot, this.yBodyRot - 70, this.yBodyRot + 70);

        if (this.ringBufferIndex < 0) {
            for (int i = 0; i < this.ringBuffer.length; ++i) {
                this.ringBuffer[i] = this.smoothedYRot;
            }
        }
        this.ringBufferIndex++;
        if (this.ringBufferIndex == this.ringBuffer.length) {
            this.ringBufferIndex = 0;
        }
        this.ringBuffer[this.ringBufferIndex] = this.smoothedYRot;

        double realVelX = 0, realVelZ = 0;
        if (!Double.isNaN(prevChainX)) {
            realVelX = this.getX() - prevChainX;
            realVelZ = this.getZ() - prevChainZ;
        }
        prevChainX = this.getX();
        prevChainZ = this.getZ();
        smoothedVelX += (realVelX - smoothedVelX) * VEL_SMOOTH;
        smoothedVelZ += (realVelZ - smoothedVelZ) * VEL_SMOOTH;

        if (!this.level().isClientSide()) {
            float currentWaveFreq = WAVE_FREQ;
            if (this.isVehicle() && this.isRunning() && this.getControllingPassenger() != null) {
                currentWaveFreq = RIDDEN_RUN_WAVE_FREQ;
            }
            this.wavePhase += (this.walkDist - this.prevWalkDist) * currentWaveFreq;
            this.prevWalkDist = this.walkDist;

            int venomCd = getVenomCooldownTicks();
            if (venomCd > 0) setVenomCooldownTicks(venomCd - 1);

            if (constrictUltArmed && --constrictUltArmedTimer <= 0) cancelConstrictUltimate();

            if (hypnosisCooldown > 0) hypnosisCooldown--;

            if (constrictCooldown > 0) constrictCooldown--;
            if (constricting) {
                if (strangleProgress < 5f) strangleProgress++;
                tickConstriction();
            } else {
                if (strangleProgress > 0f) strangleProgress--;
            }

            boolean wantSit = this.isTame() && this.isSitting();
            sitProgress = Mth.clamp(sitProgress + (wantSit ? SIT_TRANSITION_STEP : -SIT_TRANSITION_STEP), 0f, 1f);

            final int segments = 7;
            final Entity child = getChild();
            if (child == null) {
                LivingEntity partParent = this;
                parts = new BoaTailPart[segments];
                BoaPartIndex partIndex = BoaPartIndex.HEAD;
                Vec3 prevPos = this.position();
                for (int i = 0; i < segments; i++) {
                    final float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                    final float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                    BoaTailPart part = new BoaTailPart(OWEntityRegistry.BOA_TAIL_PART.get(), this);
                    part.setParent(partParent);
                    part.copyDataFrom(this);
                    part.setBodyIndex(i);
                    part.setPartType(BoaPartIndex.sizeAt(1 + i));
                    if (partParent == this) {
                        this.setChildId(part.getUUID());
                        this.entityData.set(CHILD_ID, part.getId());
                    }
                    if (i == 1) {
                        this.entityData.set(CHILD_ID_2, part.getId());
                    }
                    if (partParent instanceof BoaTailPart) {
                        ((BoaTailPart) partParent).setChildId(part.getUUID());
                    }
                    part.setPos(part.tickMultipartPosition(this.getId(), partIndex, prevPos, this.getXRot(), prevReqRot, reqRot, false));
                    partParent = part;
                    level().addFreshEntity(part);
                    parts[i] = part;
                    partIndex = part.getPartType();
                    prevPos = part.position();
                }
            }
            if (shouldReplaceParts() && this.getChild() instanceof BoaTailPart) {
                parts = new BoaTailPart[segments];
                parts[0] = (BoaTailPart) this.getChild();
                this.entityData.set(CHILD_ID, parts[0].getId());
                int i = 1;
                while (i < parts.length && parts[i - 1].getChild() instanceof BoaTailPart) {
                    parts[i] = (BoaTailPart) parts[i - 1].getChild();
                    i++;
                }
                this.entityData.set(CHILD_ID_2, parts[1] != null ? parts[1].getId() : -1);
            }
            if (parts != null) {
                BoaPartIndex partIndex = BoaPartIndex.HEAD;
                float offset = this.isVehicle() ? 3 : 1;
                Vec3 prev = this.position().add(smoothedVelX * offset, 0, smoothedVelZ * offset);
                float xRot = this.getXRot();

                boolean swimming = this.isInWater();
                float swimFreq = 3.0f;
                float swimStep = 0.85f;
                float swimAmpDeg = 18f;

                for (int i = 0; i < segments; i++) {
                    if (this.parts[i] != null) {
                        final float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                        final float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                        parts[i].copyDataFrom(this);
                        prev = parts[i].tickMultipartPosition(this.getId(), partIndex, prev, xRot, prevReqRot, reqRot, true);
                        partIndex = parts[i].getPartType();
                        xRot = parts[i].getXRot();
                    }
                }

                if (swimming) {
                    for (int i = 0; i < segments; i++) {
                        if (parts[i] != null) {
                            float wave = swimAmpDeg * (float) Math.sin(this.walkDist * swimFreq - i * swimStep);
                            parts[i].setXRot(Mth.clamp(parts[i].getXRot() + wave, -25f, 25f));
                        }
                    }
                }

                clearLilyPad(this.blockPosition());
                clearLilyPad(this.blockPosition().above());
                for (int i = 0; i < segments; i++) {
                    if (parts[i] != null) {
                        clearLilyPad(parts[i].blockPosition());
                        clearLilyPad(parts[i].blockPosition().above());
                    }
                }
            }
        }
    }

    private void clearLilyPad(BlockPos pos) {
        if (this.level().isClientSide()) return;
        var state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof WaterlilyBlock) {
            this.level().setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && isNapping()) {
            return;
        }

        super.setTarget(target);
    }

    private boolean shouldReplaceParts() {
        if (parts == null || parts[0] == null) return true;
        for (int i = 0; i < 7; i++) {
            if (parts[i] == null) return true;
        }
        return false;
    }

    @Nullable
    public java.util.UUID getChildId() {
        return this.entityData.get(CHILD_UUID).orElse(null);
    }

    public void setChildId(@Nullable java.util.UUID uniqueId) {
        this.entityData.set(CHILD_UUID, java.util.Optional.ofNullable(uniqueId));
    }

    public Entity getChild() {
        java.util.UUID id = getChildId();
        if (id != null && !level().isClientSide) {
            return ((net.minecraft.server.level.ServerLevel) level()).getEntity(id);
        }
        return null;
    }

    public float getHeadRenderYLag() {
        if (Double.isNaN(clientSmoothedY)) return 0f;
        return (float) (clientSmoothedY - this.getY());
    }

    public Entity getFirstTailPart() {
        int id = this.entityData.get(CHILD_ID);
        return id == -1 ? null : this.level().getEntity(id);
    }

    public Entity getSecondTailPart() {
        int id = this.entityData.get(CHILD_ID_2);
        Entity second = id == -1 ? null : this.level().getEntity(id);
        return second != null ? second : getFirstTailPart();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    private float getYawForPart(int i) {
        return this.getRingBuffer(4 + i * 2, 1.0F);
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    public float calcPartRotation(int i) {
        final float f = 1f - (this.strangleProgress * 0.2f);
        final float ramp = Mth.clamp(this.constrictTimer / 15f, 0f, 1f);
        final float curlPerSegment = 60f;
        final float sitCalm = 1f - this.sitProgress * 0.7f;
        return (float) (45 * -Math.sin(this.wavePhase - i)) * f * sitCalm + this.strangleProgress * 0.2f * i * curlPerSegment * ramp + this.sitProgress * i * SIT_CURL_PER_SEGMENT + this.constrictBreath * i * 10f * ramp;
    }

    public float getRingBuffer(int bufferOffset, float partialTicks) {
        if (this.isDeadOrDying()) {
            partialTicks = 0.0F;
        }
        partialTicks = 1.0F - partialTicks;
        final int i = this.ringBufferIndex - bufferOffset & 63;
        final int j = this.ringBufferIndex - bufferOffset - 1 & 63;
        final float d0 = this.ringBuffer[i];
        final float d1 = this.ringBuffer[j] - d0;
        return Mth.wrapDegrees(d0 + d1 * partialTicks);
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof BoaEntity boa) {
            boa.setVariant(BoaVariant.byId(variant));
            boa.setInitialVariant(BoaVariant.byId(variant));
        }
    }

    public BoaVariant getVariant() {
        return BoaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(BoaVariant v) {
        this.entityData.set(VARIANT, v.getId() & 255);
    }

    /** Variante naturelle exposée sous forme générique (cf. {@code OWEntity}). */
    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public BoaVariant getInitialVariant() {
        return BoaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(BoaVariant v) {
        this.entityData.set(DATA_INITIAL_VARIANT, v.getId());
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
        if (skinIndex == 1) this.setVariant(BoaVariant.Cosmetics.GOLD.variant);
        if (skinIndex == 4) this.setVariant(BoaVariant.Cosmetics.LEVIATHAN.variant);
        if (skinIndex == 5) this.setVariant(BoaVariant.Cosmetics.PLUSH.variant);
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !(entity instanceof BoaTailPart) && super.canCollideWith(entity);
    }

    @Override
    protected void pushEntities() {
        if (this.level().isClientSide()) {
            super.pushEntities();
            return;
        }
        this.level().getEntities(this, this.getBoundingBox(), entity -> !(entity instanceof BoaTailPart) && EntitySelector.pushableBy(this).test(entity)).forEach(this::doPush);
    }

    @Override
    protected int getDefaultSkinIndex() { return 7; }   // « Boa Par Défaut »

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof BoaTailPart) {
            return true;
        }
        if (entity instanceof BoaEntity otherBoa) {
            if (otherBoa.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherBoa.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherBoa.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherBoa.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    public long getDigestionStartTick() {
        return this.digestionStartTick;
    }

    public float getDigestionPower() {
        return this.digestionPower;
    }

    public boolean isDigesting() {
        if (this.digestionStartTick < 0) return false;
        float t = Math.max(0f, Math.min(1f, (this.digestionPower - 1f) / 49f));
        double durationTicks = (20.0 + 20.0 * t) * 20.0;
        return this.level().getGameTime() - this.digestionStartTick < durationTicks;
    }

    private void triggerDigestion(float preyMaxHealth) {
        this.digestionStartTick = this.level().getGameTime();
        this.digestionPower = preyMaxHealth;
    }

    public void triggerItemDigestion(float power) {
        triggerDigestion(power);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        triggerDigestion(target.getMaxHealth());
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Boa.CONSTRICT_ULT_KILLS_REQUIRED) setUltimateKillCount(kills + 1);
        return super.killedEntity(level, target);
    }

    public int getUltimateKillCount() {
        return this.entityData.get(ULTIMATE_KILL_COUNT);
    }

    public void setUltimateKillCount(int count) {
        this.entityData.set(ULTIMATE_KILL_COUNT, count);
    }

    public void activateConstrictUltimate() {
        if (this.level().isClientSide()) return;
        if (getUltimateKillCount() < OWAttacksConstants.Boa.CONSTRICT_ULT_KILLS_REQUIRED) return;
        if (this.isConstricting()) return;
        float cost = OWAttacksConstants.Boa.CONSTRICT_ULT_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        this.constrictUltArmed = true;
        this.constrictUltArmedTimer = (int) (OWAttacksConstants.Boa.CONSTRICT_ULT_TARGETING_MS / 50L) + 20;
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.BOA_HITTING.get(), SoundSource.HOSTILE, 1.2f, 0.7f);
    }

    public void executeConstrictUltimate(int targetEntityId) {
        if (this.level().isClientSide() || !this.constrictUltArmed) return;
        Entity raw = this.level().getEntity(targetEntityId);
        if (!(raw instanceof LivingEntity target) || !canConstrict(target) || this.distanceToSqr(target) > OWAttacksConstants.Boa.CONSTRICT_ULT_RANGE * OWAttacksConstants.Boa.CONSTRICT_ULT_RANGE) {
            cancelConstrictUltimate();
            return;
        }
        float cost = OWAttacksConstants.Boa.CONSTRICT_ULT_ENERGY;
        setVitalEnergy(getVitalEnergy() + cost);
        setUltimateKillCount(0);
        this.constrictUltArmed = false;
        this.constrictUltArmedTimer = 0;

        if (this.getFirstPassenger() instanceof Player rider) rider.stopRiding();
        this.setTarget(target);
        this.startConstrict(target);
    }

    public boolean canConstrict(LivingEntity t) {
        if (t == null || !t.isAlive() || t == this) return false;
        // Apprivoisé : ne pas constrict un allié de la tribu (joueur membre ou entité de la tribu).
        if (this.isTameGrabAlly(t)) return false;
        if (t instanceof IOWGrabberEntity) {
            if (t.isVehicle()) return false;
            if (t instanceof BoaEntity ob && ob.isConstricting()) return false;
            if (t instanceof TigerEntity ot && ot.isGrabbing()) return false;
            if (t instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity oc && oc.isGrabbing())
                return false;
        }
        if (t instanceof BoaTailPart) return false;
        if (this.isAlliedTo(t)) return false;
        if (t.isInWater()) return false;
        if (t instanceof Player p) {
            if (p.isCreative() || p.isSpectator()) return false;
            if (this.getOwnerUUID() != null && p.getUUID().equals(this.getOwnerUUID())) return false;
        }
        if (t.getMaxHealth() > 32f) return false;
        if (t.getBbWidth() > OWAttacksConstants.Boa.CONSTRICT_MAX_TARGET_WIDTH) return false;
        if (t.getBbHeight() > OWAttacksConstants.Boa.CONSTRICT_MAX_TARGET_HEIGHT) return false;
        boolean alreadyConstricted = this.level().getEntitiesOfClass(BoaEntity.class, t.getBoundingBox().inflate(16.0)).stream().anyMatch(o -> o != this && o.isConstricting() && o.getConstrictTarget() == t);
        if (alreadyConstricted) return false;
        return true;
    }

    public void startConstrict(LivingEntity target) {
        if (this.level().isClientSide() || target == null) return;
        this.constricting = true;
        this.constrictTarget = target;
        this.constrictTimer = 0;
        this.constrictAnchor = null;
        this.constrictTargetAnchor = null;
        this.constrictFrozenYaw = Float.NaN;
        this.constrictLocked = false;
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        this.getNavigation().stop();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.BOA_HITTING.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    public void stopConstrict() {
        LivingEntity grabbed = this.constrictTarget != null ? this.constrictTarget : this.getGrabbedTarget();
        if (grabbed != null && grabbed.getVehicle() == this) grabbed.stopRiding();
        if (grabbed instanceof Mob m) m.setNoAi(false);
        this.constricting = false;
        this.constrictTarget = null;
        this.constrictTimer = 0;
        this.constrictCooldown = OWAttacksConstants.Boa.CONSTRICT_COOLDOWN_TICKS;
        this.constrictAnchor = null;
        this.constrictTargetAnchor = null;
        this.constrictFrozenYaw = Float.NaN;
        this.constrictLocked = false;
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        this.setNoGravity(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.constrictBreath = 0f;
    }

    private void tickConstriction() {
        LivingEntity t = this.constrictTarget;
        boolean riddenByOther = this.isVehicle() && this.getFirstPassenger() != t;
        // Une prise devenue amicale — le serpent a été apprivoisé en plein étouffement, ou sa proie
        // a rejoint sa tribu — doit se relâcher immédiatement : sans gravité et immobilisé par
        // l'étreinte, le couple partait sinon à la dérive en l'air.
        if (t == null || !t.isAlive() || riddenByOther || this.isTameGrabAlly(t)) {
            stopConstrict();
            return;
        }
        this.constrictTimer++;
        this.constrictBreath = (float) (0.5 + 0.5 * Math.sin(this.constrictTimer * Mth.TWO_PI / 11.0));
        final boolean isPlayer = t instanceof Player;
        final boolean approaching = this.constrictTimer <= OWAttacksConstants.Boa.CONSTRICT_APPROACH_TICKS;

        this.setXRot(0);
        this.setNoGravity(true);
        this.fallDistance = 0;

        if (approaching) {
            float radius = t.getBbWidth() * -0.5F;
            float angle = Mth.DEG_TO_RAD * (t.yBodyRot - 45F);
            double extraX = radius * Mth.sin(Mth.PI + angle);
            double extraZ = radius * Mth.cos(angle);
            Vec3 targetVec = new Vec3(extraX + t.getX(), t.getY(1.0F), extraZ + t.getZ());
            Vec3 move = targetVec.subtract(this.position());
            double speed = OWAttacksConstants.Boa.CONSTRICT_MOVE_SPEED;
            if (move.lengthSqr() > speed * speed) move = move.normalize().scale(speed);
            this.setDeltaMovement(move);

            double dxA = t.getX() - this.getX();
            double dzA = t.getZ() - this.getZ();
            if (dxA * dxA + dzA * dzA > 1.0E-4) {
                float yawA = (float) (Mth.atan2(dzA, dxA) * Mth.RAD_TO_DEG) - 90f;
                this.setYRot(yawA);
                this.yBodyRot = yawA;
                this.yHeadRot = yawA;
                this.smoothedYRot = yawA;
            }
            return;
        }

        if (!this.constrictLocked) {
            this.constrictLocked = true;
            if (isPlayer) {
                this.setGrabbing(true, t);
                this.setGrabTimeout(OWAttacksConstants.Boa.CONSTRICT_GRAB_START_TIMEOUT);
                this.setGrabHold(t.position());
                t.startRiding(this, true);
            } else if (t instanceof Mob m) {
                m.setNoAi(true);
            }
        }

        if (isPlayer) {
            if (this.getGrabTimeout() <= 0) {
                Vec3 away = t.position().subtract(this.position());
                away = (away.lengthSqr() > 1.0E-4 ? away.normalize() : new Vec3(0, 0, 1)).scale(0.45).add(0, 0.3, 0);
                stopConstrict();
                this.setTarget(null);
                t.setDeltaMovement(away);
                t.hurtMarked = true;
                return;
            }
            this.setGrabTimeout(this.getGrabTimeout() + 1);
            if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                t.hurt(this.damageSources().mobAttack(this), Float.MAX_VALUE);
                stopConstrict();
                return;
            }
        } else if (this.constrictTimer > OWAttacksConstants.Boa.CONSTRICT_DURATION_TICKS) {
            stopConstrict();
            return;
        }

        {
            Vec3 anchor = (this.constrictAnchor != null) ? this.constrictAnchor : this.position();
            this.constrictAnchor = anchor;
            this.setDeltaMovement(Vec3.ZERO);
            double headY = t.getY() + t.getBbHeight() * OWAttacksConstants.Boa.CONSTRICT_HEAD_RAISE_RATIO;
            this.setPos(anchor.x, headY, anchor.z);
        }

        if (approaching || Float.isNaN(this.constrictFrozenYaw)) {
            double dx = t.getX() - this.getX();
            double dz = t.getZ() - this.getZ();
            if (dx * dx + dz * dz > 1.0E-4) {
                float yawToTarget = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
                this.setYRot(yawToTarget);
                this.yBodyRot = yawToTarget;
                this.yHeadRot = yawToTarget;
                this.smoothedYRot = yawToTarget;
                if (!approaching) this.constrictFrozenYaw = yawToTarget;
            }
        } else {
            this.setYRot(this.constrictFrozenYaw);
            this.yBodyRot = this.constrictFrozenYaw;
            this.yHeadRot = this.constrictFrozenYaw;
            this.smoothedYRot = this.constrictFrozenYaw;
        }

        Vec3 hold = (this.constrictTargetAnchor != null) ? this.constrictTargetAnchor : t.position();
        if (!approaching && this.parts != null) {
            double cx = 0, cz = 0;
            int n = 0;
            for (BoaTailPart p : this.parts) {
                if (p != null) {
                    cx += p.getX();
                    cz += p.getZ();
                    n++;
                }
            }
            if (n > 0) hold = new Vec3(cx / n, hold.y, cz / n);
        }
        this.constrictTargetAnchor = hold;
        if (isPlayer) this.setGrabHold(hold);
        if (!isPlayer) {
            Vec3 dm = t.getDeltaMovement();
            double vy = t.onGround() ? 0.0 : -0.08;
            t.setDeltaMovement(dm.x * 0.25, vy, dm.z * 0.25);
            final double TARGET_PULL = 0.6;
            t.setPos(Mth.lerp(TARGET_PULL, t.getX(), hold.x), t.getY(), Mth.lerp(TARGET_PULL, t.getZ(), hold.z));
            t.hurtMarked = true;
            if (t instanceof Mob m) m.getNavigation().stop();
        }
        t.setAirSupply(Math.max(-20, t.getAirSupply() - 5));

        boolean playerOutOfAir = isPlayer && t.getAirSupply() <= 0;
        if ((!isPlayer || playerOutOfAir) && this.constrictTimer >= 30 && this.constrictTimer % OWAttacksConstants.Boa.CONSTRICT_DAMAGE_INTERVAL == 0) {
            float squeeze = Math.min(1f, (float) this.constrictTimer / OWAttacksConstants.Boa.CONSTRICT_DURATION_TICKS);
            float dmg = Mth.lerp(squeeze, OWAttacksConstants.Boa.CONSTRICT_BASE_DAMAGE, OWAttacksConstants.Boa.CONSTRICT_MAX_DAMAGE);
            t.invulnerableTime = 0;
            t.hurt(this.damageSources().mobAttack(this), dmg);
        }
    }

    @Override
    protected void onSuccessfulHit(LivingEntity entity) {
        if (this.isTame() && this.isVehicle() && this.isVenomArmed()) {
            applyVenomFangs(entity);
            return;
        }
        if (RANDOM(10)) {
            if (!entity.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) && !this.isVehicle()) {
                entity.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) OWUtils.generateRandomInterval(6500, 9000), 0));
            }
        } else if (RANDOM(5)) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, (int) OWUtils.generateRandomInterval(180, 350), 1));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setVariant(chooseBoaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private BoaVariant chooseBoaVariant() {
        int roll = RANDOM.nextInt(10);
        if (roll < 2.5) return BoaVariant.DEFAULT;
        if (roll < 5) return BoaVariant.YELLOW;
        if (roll < 7.5) return BoaVariant.BROWN;
        return BoaVariant.DARK;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("initialVariant"));
        this.setVariant(BoaVariant.byId(tag.getInt("variant")));
        this.setUltimateKillCount(tag.getInt("ultimateKillCount"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("initialVariant", this.getInitialVariant().getId());
        tag.putInt("variant", this.getVariant().getId());
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return OWSounds.BOA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OWSounds.BOA_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {

    }

    @Override
    public void positionRider(Entity passenger, MoveFunction callback) {
        if (this.isGrabbing() && passenger == this.getGrabbedTarget()) {
            Vec3 a = this.getGrabHold();
            callback.accept(passenger, a.x, a.y, a.z);
            return;
        }
        if (!this.hasPassenger(passenger)) return;

        float yOffset = (float) (this.getBbHeight() * 0.1) - 0.18f;
        final double BACK = 0.65;
        Entity seg = getSecondTailPart();

        if (seg != null) {
            double sx = (seg.xOld + seg.getX()) * 0.5;
            double sy = (seg.yOld + seg.getY()) * 0.5;
            double sz = (seg.zOld + seg.getZ()) * 0.5;
            double yawR = Math.toRadians(seg.getYRot());
            double bx = sx + Math.sin(yawR) * BACK;
            double by = sy + yOffset;
            double bz = sz - Math.cos(yawR) * BACK;

            if (seg instanceof BoaTailPart part) {
                double s = this.getScale() / 16.0;
                double ax = -part.bodyAnimX * s;
                double ay = -part.bodyAnimY * s;
                double az = part.bodyAnimZ * s;
                double th = Math.toRadians(180.0 - seg.getYRot());
                double cos = Math.cos(th), sin = Math.sin(th);
                bx += ax * cos + az * sin;
                by += ay;
                bz += -ax * sin + az * cos;

                by += part.bodyBulge * 4.0 * s;
            }

            if (this.level().isClientSide()) {
                Vec3 smoothed = smoothSeatClient(new Vec3(bx, by, bz));
                bx = smoothed.x;
                by = smoothed.y;
                bz = smoothed.z;
            }

            callback.accept(passenger, bx, by, bz);
        } else {
            double yawR = Math.toRadians(this.getYRot());
            callback.accept(passenger, this.getX() + Math.sin(yawR) * BACK, this.getY() + yOffset, this.getZ() - Math.cos(yawR) * BACK);
        }
    }

    // --- Lissage client du siege ---
    // Le siege est accroche au 2e segment de queue, dont la position est calculee UNIQUEMENT cote
    // serveur (cf. tick()) puis repliquee : cote client il avance par paliers d'interpolation
    // reseau, avec deux a quatre ticks de retard. La tete, elle, est predite localement des que le
    // joueur la pilote — donc parfaitement fluide. Assis sur le segment, le cavalier se faisait
    // donc tirer par une position saccadee pendant que sa monture avancait en continu.
    //
    // On ne corrige que l'affichage : au lieu de suivre le segment en absolu, on suit la TETE
    // (fluide) en lui ajoutant le vecteur tete -> siege, lisse. Ce vecteur ne decrit que la forme
    // du corps, qui ne varie qu'a la vitesse ou le boa tourne : le filtrer supprime le tremblement
    // reseau sans introduire de retard sur le deplacement lui-meme. Le serveur, lui, garde le
    // calcul exact.
    /** Fraction de rattrapage par tick du vecteur tete -> siege. */
    private static final double SEAT_FOLLOW = 0.4D;
    /** Au-dela de cet ecart (en blocs au carre), on recale sec : teleportation, changement de monde. */
    private static final double SEAT_SNAP_DIST_SQR = 9.0D;

    private Vec3 seatOffset = null;

    private Vec3 smoothSeatClient(Vec3 rawSeat) {
        Vec3 offset = rawSeat.subtract(this.position());
        if (this.seatOffset == null || offset.distanceToSqr(this.seatOffset) > SEAT_SNAP_DIST_SQR) {
            this.seatOffset = offset;
        } else {
            this.seatOffset = this.seatOffset.add(offset.subtract(this.seatOffset).scale(SEAT_FOLLOW));
        }
        return this.position().add(this.seatOffset);
    }

    private void setGrabHold(Vec3 p) {
        this.entityData.set(GRAB_HOLD_X, (float) p.x);
        this.entityData.set(GRAB_HOLD_Y, (float) p.y);
        this.entityData.set(GRAB_HOLD_Z, (float) p.z);
    }

    private Vec3 getGrabHold() {
        return new Vec3(this.entityData.get(GRAB_HOLD_X), this.entityData.get(GRAB_HOLD_Y), this.entityData.get(GRAB_HOLD_Z));
    }

    @Override
    public int getGrabMaxTimeout() {
        return OWAttacksConstants.Boa.CONSTRICT_GRAB_MAX_TIMEOUT;
    }

    public boolean isGrabbing() {
        return this.entityData.get(IS_GRABBING);
    }

    public void setGrabbing(boolean grabbing, LivingEntity entity) {
        // Apprivoisé : ne jamais saisir un allié de la tribu (joueur membre ou entité de la tribu).
        if (grabbing && this.isTameGrabAlly(entity)) return;
        this.entityData.set(IS_GRABBING, grabbing);
        this.entityData.set(GRABBED_TARGET_ID, entity == null ? -1 : entity.getId());
    }

    public int getGrabbedTargetId() {
        return this.entityData.get(GRABBED_TARGET_ID);
    }

    public LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        return this.level().getEntity(id) instanceof LivingEntity le ? le : null;
    }

    public int getGrabTimeout() {
        return this.entityData.get(GRAB_TIMEOUT);
    }

    public void setGrabTimeout(int timeout) {
        this.entityData.set(GRAB_TIMEOUT, timeout);
    }

    public boolean isVenomArmed() {
        return this.entityData.get(VENOM_ARMED);
    }

    public void setVenomArmed(boolean armed) {
        this.entityData.set(VENOM_ARMED, armed);
    }

    public int getVenomCooldownTicks() {
        return this.entityData.get(VENOM_COOLDOWN_TICKS);
    }

    public void setVenomCooldownTicks(int ticks) {
        this.entityData.set(VENOM_COOLDOWN_TICKS, ticks);
    }

    public void toggleVenomFangs() {
        if (this.level().isClientSide()) return;
        if (isVenomArmed()) {
            setVenomArmed(false);
        } else if (getVenomCooldownTicks() <= 0) {
            setVenomArmed(true);
        }
    }

    public void cancelConstrictUltimate() {
        this.constrictUltArmed = false;
        this.constrictUltArmedTimer = 0;
    }

    /**
     * L'apprivoisement met fin à l'étouffement en cours.
     *
     * <p>Un serpent apprivoisé au beau milieu d'une prise gardait sa proie sanglée, sans gravité et
     * à la position gelée par l'étreinte : le duo montait en l'air et n'en redescendait plus, la
     * cible restant montée sur le Boa sans que rien ne vienne la libérer. On lâche donc tout —
     * étreinte, prise, ultime amorcée — avant que l'entité ne change de camp.</p>
     */
    @Override
    public void setTame(boolean tame, Player player) {
        if (tame && !this.level().isClientSide()) {
            cancelConstrictUltimate();
            if (this.constricting) stopConstrict();
            if (this.isGrabbing()) {
                LivingEntity grabbed = this.getGrabbedTarget();
                if (grabbed != null && grabbed.getVehicle() == this) grabbed.stopRiding();
                this.setGrabbing(false, null);
                this.setGrabTimeout(0);
            }
            this.setNoGravity(false);
        }
        super.setTame(tame, player);
    }

    public boolean isConstricting() {
        return this.constricting;
    }

    public LivingEntity getConstrictTarget() {
        return this.constrictTarget;
    }

    public int getConstrictCooldown() {
        return this.constrictCooldown;
    }

    public boolean isHypnotizing() {
        return this.entityData.get(HYPNOTIZING);
    }

    public void setHypnotizing(boolean value) {
        this.entityData.set(HYPNOTIZING, value);
    }

    public int getHypnosisTargetId() {
        return this.entityData.get(HYPNOSIS_TARGET_ID);
    }

    public void setHypnosisTargetId(int id) {
        this.entityData.set(HYPNOSIS_TARGET_ID, id);
    }

    public int getHypnosisCooldown() {
        return this.hypnosisCooldown;
    }

    public void setHypnosisCooldown(int ticks) {
        this.hypnosisCooldown = ticks;
    }

    private void applyVenomFangs(LivingEntity target) {
        int duration = (int) OWUtils.generateRandomInterval(OWAttacksConstants.Boa.VENOM_FANGS_MIN_DURATION_TICKS, OWAttacksConstants.Boa.VENOM_FANGS_MAX_DURATION_TICKS);
        target.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), duration, 0));
        setVenomArmed(false);
        setVenomCooldownTicks(OWAttacksConstants.Boa.VENOM_FANGS_COOLDOWN_TICKS);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.BOA_HITTING.get(), SoundSource.HOSTILE, 1.0f, 1.35f);
    }

    public boolean isThermalHeartTarget(LivingEntity le) {
        if (le == null || le == this || le instanceof BoaEntity || le instanceof BoaTailPart) return false;
        if (!le.isAlive() || le.isSpectator()) return false;
        if (this.isAlliedTo(le)) return false;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (le.getMaxHealth() > 32f) return false;
        double r = OWAttacksConstants.Boa.THERMAL_ENGAGE_RANGE;
        return this.distanceToSqr(le) <= r * r;
    }

    public static Vec3 thermalHeartCenter(LivingEntity le) {
        return le.getBoundingBox().getCenter();
    }

    public static float thermalSizeScale(LivingEntity le) {
        float size = (le.getBbWidth() + le.getBbHeight()) * 0.5f;
        float scale = size / OWAttacksConstants.Boa.THERMAL_REF_SIZE;
        return Mth.clamp(scale, OWAttacksConstants.Boa.THERMAL_SCALE_MIN, OWAttacksConstants.Boa.THERMAL_SCALE_MAX);
    }

    public boolean isLookingAtHeart(Player rider, LivingEntity target) {
        if (rider == null || target == null) return false;
        Vec3 eye = rider.getEyePosition();
        Vec3 look = rider.getLookAngle();
        Vec3 heart = thermalHeartCenter(target);

        Vec3 toHeart = heart.subtract(eye);
        double along = toHeart.dot(look);
        if (along <= 0) return false;
        Vec3 closest = eye.add(look.scale(along));
        double radius = OWAttacksConstants.Boa.THERMAL_AIM_RADIUS * thermalSizeScale(target);
        if (closest.distanceTo(heart) > radius) return false;
        BlockHitResult clip = this.level().clip(new net.minecraft.world.level.ClipContext(eye, heart, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, rider));
        return clip.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    public void spawnHeartBleed(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) return;
        Vec3 h = thermalHeartCenter(target);
        var blood = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.60f, 0.0f, 0.0f), 1.2f);
        server.sendParticles(blood, h.x, h.y, h.z, 18, 0.18, 0.18, 0.18, 0.02);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, h.x, h.y, h.z, 6, 0.1, 0.1, 0.1, 0.05);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isGrabbing() && this.getFirstPassenger() == this.getGrabbedTarget()) return null;
        return super.getControllingPassenger();
    }

    private void setupAnimationState() {
        handleMiscIdleAnimations();
        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (30 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

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

    public boolean isAnyIdleAnimationPlaying() {
        return this.tongAnimationState.isStarted();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isNapping() && !this.isMoving() && !this.isVehicle() && !this.isInWater();
    }

    public boolean canTong() {
        return canPlayIdleAnimation();
    }

    protected void handleMiscIdleAnimations() {
        if (this.tongAnimationState.isStarted() && this.tickCount - tongAnimationStartTime > TONG_DURATION) {
            this.tongAnimationState.stop();
        }

        if (tongCooldown > 0) {
            tongCooldown--;
            return;
        }

        if (this.level().isClientSide) {

            SoundEvent[] sounds = new SoundEvent[]{OWSounds.BOA_IDLE_1.get(), OWSounds.BOA_IDLE_2.get(), OWSounds.BOA_IDLE_3.get(), OWSounds.BOA_IDLE_4.get()};

            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sounds[this.getRandom().nextInt(sounds.length)], this.getSoundSource(), 1.0F, isBaby() ? 2.0F : 1.0F, false);
        }

        if (this.canTong() && this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            this.tongAnimationState.start(this.tickCount);
            tongAnimationStartTime = this.tickCount;
        }

        tongCooldown = (int) OWUtils.generateRandomInterval(400, 800);
    }

    class BoaMeleeAttackGoal extends MeleeAttackGoal {
        public BoaMeleeAttackGoal() {
            super(BoaEntity.this, 2.75f, true);
        }

        @Override
        public boolean canUse() {
            return !BoaEntity.this.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !BoaEntity.this.isTame() && super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            BoaEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            BoaEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2;
            return this.isTimeToAttack() && this.mob.distanceToSqr(entity) <= reach * reach && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;
            if (this.mob instanceof OWEntity owEntity) {
                if (!owEntity.isCombo()) owEntity.setCombo(true, 1);
                else if (owEntity.isPauseCombo()) owEntity.playerContinueCombo = true;
            }
        }
    }
}