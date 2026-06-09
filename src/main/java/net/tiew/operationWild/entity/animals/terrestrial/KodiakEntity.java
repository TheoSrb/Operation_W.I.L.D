package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.block.SoundType;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.particle.OWParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.tiew.operationWild.core.OWUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.behavior.KodiakBehaviorHandler;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.kodiak.*;
import net.tiew.operationWild.entity.taming.TamingKodiak;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class KodiakEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, NeutralMob {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 180.0;
    private static final int MAX_EATING_TIMER = 400;
    private static final int MAX_HONEY_TIMER = 750;
    public static final int MAX_DIRTY_TIMER = 1200;
    public final int MAX_SITTING_TIMER = 600;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SHADE_SKIN = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ROLLING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> FOOD_PICK = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> IS_DIRTY = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SNIFFING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REJECT_ITEM = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CATCHING_SALMON = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RUBS = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FOOD_BAR_VALUE = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PAW_SLAM_CHARGING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PAW_SLAM_STRIKING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ULTIMATE_NAP_KILL_COUNT = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ULTIMATE_NAPPING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);

    public KodiakBehaviorHandler kodiakBehaviorHandler;
    public TamingKodiak kodiakTaming;

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState transitionIdleStandingUp = new AnimationState();
    public final AnimationState transitionStandingUpIdle = new AnimationState();
    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState rollingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState rejectingAnimationState = new AnimationState();
    public final AnimationState rubsAnimationState = new AnimationState();
    public final AnimationState pawSlamChargeAnimState = new AnimationState();
    public final AnimationState pawSlamChargeFullAnimState = new AnimationState();
    public final AnimationState pawSlamStrikeAnimState = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int napAnimationTimeout = 0;
    public int sniffingAnimationTimeout = 0;
    public int rejectingAnimationTimeout = 0;
    public int rubsAnimationTimeout = 0;
    public int pawSlamChargeAnimTimer = 0;
    public int pawSlamChargeFullAnimTimer = 0;
    public int pawSlamStrikeAnimTimer = 0;
    private int pawSlamStrikeServerTimer = 0;
    private int pawSlamHitTimer = -1;
    private float pawSlamPendingFactor = 0f;
    private int ultimateNapDurationTimer = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    private float rubYaw = 0f;

    public volatile float bodyAnimY = 0f;
    public volatile float bodyZRotCamera = 0f;
    public volatile float bodyXRotCamera = 0f;
    public volatile float pawSlamRiderYExtra = 0f;
    public volatile float pawSlamRiderZExtra = 0f;
    public volatile float bodyAnimY_passenger = 0f;
    public volatile float bodyZRot_passenger = 0f;
    public volatile float bodyXRot_passenger = 0f;

    public int rollTimer = 0;
    public int itemRejectionTimer = 0;
    public int sitTimer = 0;
    public int salmonCatchedTimer = 0;
    private int rubTimer = 0;

    public ItemStack foodPick = ItemStack.EMPTY;
    public boolean startEatingTimer = false;
    public int eatingTimer = 0;
    public boolean startHoneyTimer = false;
    private int honeyTimer = 0;
    public Player lastPlayerWhoFeedHim = null;

    public int numberOfBonusSearching = 0;
    public int numberOfBonusSearchingMax = this.random.nextInt(7) + 5;
    public int cropCheckTimer = 0;
    public BlockPos targetCrop = null;
    public int cropRadiusSearch = 0;

    private int dirtyTimer = 0;

    private boolean isSettingTarget = false;

    public ChestBlockEntity chestBlockEntity = null;
    public boolean isSearchingInsideChest = false;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(29, 41);
    private int remainingPersistentAngerTime;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;

    // ==================================================
    //            INTÉLLIGENCE ARTIFICIELLE
    // ==================================================

    public KodiakEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initKodiakBehaviorAndTaming();
    }

    private void initKodiakBehaviorAndTaming() {
        this.kodiakBehaviorHandler = new KodiakBehaviorHandler(this);
        this.kodiakTaming = new TamingKodiak(this, kodiakBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 45.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.FOLLOW_RANGE, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initKodiakBehaviorAndTaming(); // Create the AI before the goals, otherwise, null error

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new KodiakCatchFishGoal(this, 1.0f, () -> kodiakBehaviorHandler.catchSalmon()));

        this.goalSelector.addGoal(1, new KodiakMeleeAttackGoal());
        this.goalSelector.addGoal(1, new KodiakRollGoal(this, 1.5f));
        this.goalSelector.addGoal(1, new KodiakAttractedToFoodItemGoal(this, 1.75f, 15, 7.5f, () -> kodiakBehaviorHandler.pickupItemInHisMouth(this.foodPick), this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(2, new KodiakSearchInsideChestGoal(this, 2.0f, 35, 1.75f, () -> kodiakBehaviorHandler.openChest(chestBlockEntity)));

        this.goalSelector.addGoal(3, new KodiakTryFindWaterGoal(this));
        this.goalSelector.addGoal(3, new KodiakAttractedToBeeNestGoal(this, 1.75f, 25, 2.0f, kodiakBehaviorHandler::lookForHoneyInTheBeeNest, true));

        this.goalSelector.addGoal(4, new KodiakAttractedToCampfireGoal(this, 1.0f, 60, 2.25f, () -> kodiakBehaviorHandler.pickupItemInHisMouth(this.foodPick), true));

        this.goalSelector.addGoal(5, new KodiakAttractedToCropsGoal(this, 1.15f, 80, 2.25f, () -> kodiakBehaviorHandler.goToNewCropBlock(20), true));

        this.goalSelector.addGoal(6, new KodiakTemptGoal(this, 2D, Ingredient.of(Tags.Items.FOODS), false));

        this.goalSelector.addGoal(7, new KodiakRubsAgainstTreeGoal(this, 1.0f, 20, 4.0f, () -> kodiakBehaviorHandler.startingRubsAgainstTree()));

        this.goalSelector.addGoal(9, new NapGoal(this, 1.15f, 700, true));

        this.goalSelector.addGoal(10, new KodiakSitGoal(this, 0.25f));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new KodiakRandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new KodiakNearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(4, new KodiakNearestAttackableTargetGoal(this, Animal.class, true));
        this.targetSelector.addGoal(5, new KodiakNearestAttackableTargetGoal(this, Monster.class, true));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof KodiakEntity kodiak && !kodiak.isSleeping() && !kodiak.isNapping()) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_SHADE_SKIN, false);
        builder.define(IS_ROLLING, false);
        builder.define(FOOD_PICK, ItemStack.EMPTY);
        builder.define(IS_DIRTY, false);
        builder.define(IS_SNIFFING, false);
        builder.define(REJECT_ITEM, false);
        builder.define(IS_CATCHING_SALMON, false);
        builder.define(IS_RUBS, false);
        builder.define(IS_MAD, false);
        builder.define(FOOD_BAR_VALUE, 10);
        builder.define(IS_PAW_SLAM_CHARGING, false);
        builder.define(IS_PAW_SLAM_STRIKING, false);
        builder.define(ULTIMATE_NAP_KILL_COUNT, 0);
        builder.define(IS_ULTIMATE_NAPPING, false);
    }

    // ==================================================
    //             MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return 8215109;
    }

    @Override
    public float getTheoreticalScale() {
        return 10;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.BERSERKER;
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
    public List<Class<?>> getFavoriteTargetsByBeingNonTame() {
        return List.of(Pig.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return isUltimateNapping() ? 0f : 4f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return isUltimateNapping() ? 0f : 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return isUltimateNapping() ? 0f : 3f;
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
        return OWItems.KODIAK_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.KODIAK_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 350 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.85f * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public boolean preferRawMeat() {
        return false;
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
        return isUltimateNapping() ? 0 : isPawSlamCharging() ? 1f : 0.115f;
    }

    @Override
    protected boolean isImmobile() {
        return this.isRubs() || this.isPawSlamCharging() || this.isPawSlamStriking();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isUltimateNapping()) return 0f;
        return this.isImmobile() ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.KODIAK.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.KODIAK_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        return  RANDOM(2) ? OWSounds.KODIAK_IDLE_1.get() : RANDOM(2) ? OWSounds.KODIAK_IDLE_2.get() : OWSounds.KODIAK_IDLE_3.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.KODIAK_MISC.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.KODIAK_HURT.get() : OWSounds.KODIAK_MISC.get();
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        // Intentionally empty — replaced by animation callbacks below
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
        playStepSoundFromAnimation(0.8f);
    }


    public void onRightFootDown() {
        playStepSoundFromAnimation(1.0f);
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    public void tick() {
        super.tick();
        kodiakTaming.tick();

        boolean hasSomethingInHisMouth = getFoodPick() != null && !getFoodPick().isEmpty();

        if (!isPawSlamCharging() && !isPawSlamStriking()) {
            createCombo((int) (20 / comboSpeedMultiplier), (int) (12 / comboSpeedMultiplier), random.nextInt(2) == 0 ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 2.25, false, 2);
        }

        if (isPawSlamStriking() && !this.level().isClientSide()) {
            pawSlamStrikeServerTimer++;
            if (pawSlamStrikeServerTimer >= 15) {
                pawSlamStrikeServerTimer = 0;
                setPawSlamStriking(false);
            }
        }

        if (pawSlamHitTimer > 0 && !this.level().isClientSide()) {
            pawSlamHitTimer--;
            if (pawSlamHitTimer == 0) {
                pawSlamHitTimer = -1;
                executePawSlamHit(pawSlamPendingFactor);
            }
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);

        handleFoodBarSystem();

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (isSearchingInsideChest) this.setNap(false);

        if (!this.level().isClientSide() && isUltimateNapping()) {
            this.setHealth(Math.min(this.getMaxHealth(), this.getHealth() + 3f / 20f));
            ultimateNapDurationTimer--;
            if (ultimateNapDurationTimer <= 0) {
                cancelUltimateNap();
            }

            if (this.tickCount % 20 == 0 && this.level() instanceof ServerLevel serverLevel) {
                net.minecraft.world.phys.Vec3 look = this.getLookAngle();
                double px = this.getX() + look.x * 1.25;
                double py = this.getY() + 1.15;
                double pz = this.getZ() + look.z * 1.25;
                serverLevel.sendParticles(OWParticles.NAP_PARTICLES.get(), px, py, pz, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMad(this.isCombo());

        if (hasSomethingInHisMouth) {
            if (getFoodPick() == Items.HONEYCOMB.getDefaultInstance()) {
                if (startHoneyTimer) {
                    if (honeyTimer < MAX_HONEY_TIMER) honeyTimer++;
                    else {
                        kodiakBehaviorHandler.eatFoodInHisMouth(getFoodPick());
                    }
                }
            } else {
                if (startEatingTimer) {
                    if (eatingTimer < MAX_EATING_TIMER) eatingTimer++;
                    else {
                        kodiakBehaviorHandler.eatFoodInHisMouth(getFoodPick());
                    }
                }
            }
        }

        if (isCatchingSalmon()) {
            this.setCatchingSalmon(true);

            Vec3 lookDirection = this.getLookAngle();
            double spawnX = this.getX() + lookDirection.x * 2.0;
            double spawnY = this.getY() + 0.8;
            double spawnZ = this.getZ() + lookDirection.z * 2.0;

            salmonCatchedTimer++;

            if (this.tickCount % 15 == 0) {
                this.playSound(SoundEvents.SALMON_FLOP);
            }

            if (salmonCatchedTimer >= 800) {
                salmonCatchedTimer = 0;
                kodiakBehaviorHandler.isCatchSalmon = false;
                this.setCatchingSalmon(false);

                OWUtils.spawnItemParticles(this, Items.SALMON.getDefaultInstance(), spawnX, spawnY, spawnZ);

                this.playSound(SoundEvents.GENERIC_EAT);

                int foodValue = 5;
                this.setFoodBarValue(this.getFoodBarValue() + foodValue);
            }
        }

        if (this.isRolling()) {
            this.rollTimer++;

            kodiakBehaviorHandler.trampleCrops(this.blockPosition());
            kodiakBehaviorHandler.trampleCrops(this.blockPosition().below());

            Vec3 lookDirection = this.getLookAngle();
            Vec3 leftDirection = new Vec3(lookDirection.z, 0, -lookDirection.x);

            double rollSpeed = 0.075;
            this.setDeltaMovement(leftDirection.scale(rollSpeed));
            this.setDeltaMovement(this.getDeltaMovement().x, -1, this.getDeltaMovement().z);

            if (this.tickCount % 15 == 0) {
                this.playStepSound(this.blockPosition(), this.getBlockStateOn());

                double particleX = this.getX();
                double particleY = this.getY();
                double particleZ = this.getZ();

                if (!this.level().isClientSide) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
                        serverLevel.sendParticles(dirtParticle,
                                particleX, particleY, particleZ,
                                8,
                                0.5, 0.1, 0.5,
                                0.2);
                    }
                } else {
                    if (this.level() instanceof ClientLevel clientLevel) {
                        BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());

                        for (int i = 0; i < 8; i++) {
                            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.0;
                            double offsetY = (this.getRandom().nextDouble() - 0.5) * 0.2;
                            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.0;

                            double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.4;
                            double velocityY = this.getRandom().nextDouble() * 0.2;
                            double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.4;

                            clientLevel.addParticle(dirtParticle,
                                    particleX + offsetX,
                                    particleY + offsetY,
                                    particleZ + offsetZ,
                                    velocityX, velocityY, velocityZ);
                        }
                    }
                }
            }

            if (this.rollTimer >= 80) {
                this.rollTimer = 0;
                this.setRolling(false);
            }
        }

        if (this.getTarget() != null) {
            rubTimer = 0;
            this.setRubs(false);

            if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
                kodiakBehaviorHandler.eatFoodInHisMouth(this.getFoodPick());
            }
        }

        if (this.isRubs()) {
            rubTimer++;


            if (!this.level().isClientSide()) {
                this.setYRot(getRubYaw() + 180);
                this.yRotO = getRubYaw() + 180;
                this.setYHeadRot(getRubYaw() + 180);
            }

            if (tickCount % 20 == 0) {
                if (this.random.nextFloat() <= 0.1f) {
                    this.playSound(SoundEvents.ITEM_PICKUP);
                    this.spawnAtLocation(OWItems.KODIAK_COAT.get());
                }

                if (this.random.nextInt(2) == 0) {
                    this.playAmbientSound();
                }
            }

            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
            }
            this.getNavigation().stop();

            this.setJumping(false);
            this.jumping = false;

            if (rubTimer >= 300) {
                rubTimer = 0;
                this.setRubs(false);
            }
        }

        if (isSearchingInsideChest) {
            this.setDeltaMovement(0,0,0);

            if (chestBlockEntity != null) {
                BlockPos chestPos = chestBlockEntity.getBlockPos();
                this.setLookAt(chestPos.getX(), chestPos.getY(), chestPos.getZ());
            }
        }

        if (!this.isTame()) {
            if (this.isSitting()) {
                this.sitTimer++;

                if (sitTimer >= MAX_SITTING_TIMER) {
                    sitTimer = 0;
                    this.setSitting(false);
                }
            }
        }

        if (cropCheckTimer > 0) {
            cropCheckTimer--;
            if (cropCheckTimer == 0 && targetCrop != null) {
                if (OWUtils.distanceRest(this, targetCrop) <= 3) {
                    numberOfBonusSearching++;

                    if (numberOfBonusSearching >= numberOfBonusSearchingMax) {
                        numberOfBonusSearching = 0;
                        this.getNavigation().stop();
                    } else {
                        kodiakBehaviorHandler.goToNewCropBlock(cropRadiusSearch);
                    }
                }
                targetCrop = null;
            }
        }

        if (!this.level().isClientSide() && this.isDirty()) {
            if (dirtyTimer <= MAX_DIRTY_TIMER) {
                dirtyTimer++;
            } else {
                dirtyTimer = 0;
                setDirty(false);
            }
        }

        if (this.isSniffing()) {
            if (this.tickCount % 15 == 0) {
                float pitch = (float) (OWUtils.generateExponentialExp(0.7, 0.9));
                this.playSound(RANDOM(2) ? OWSounds.KODIAK_SNIFF_1.get() : RANDOM(2) ? OWSounds.KODIAK_SNIFF_2.get() : OWSounds.KODIAK_SNIFF_3.get(), 0.5f, pitch);
            }
        }

        if (this.isRejectingItem()) {
            itemRejectionTimer++;

            if (!this.level().isClientSide && itemRejectionTimer % 5 == 0) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1, this.getZ(), 10, 0f, 0f, 0f, 0.02);
            }

            if (itemRejectionTimer >= 21) {
                itemRejectionTimer = 0;
                this.setRejectItem(false);
            }
        }

        handleGoldVariantEffects();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.onGround()) {
            kodiakBehaviorHandler.trampleCrops(this.blockPosition());
            kodiakBehaviorHandler.trampleCrops(this.blockPosition().below());
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle() && !isRubs()) this.jumpFromGround();
    }

    @Override
    public boolean isPushable() {
        if (isRubs() || (isSitting() && !isTame())) return false;
        return super.isPushable();
    }

    protected double getBaseRiderYOffset(int idx) {
        double factor = (idx == 0) ? 0.5 : 0.65;
        return this.getBbHeight() * factor * this.getScale();
    }

    @Override
    protected double getBaseRiderYOffset() {
        return getBaseRiderYOffset(0);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        int idx = this.getPassengers().indexOf(passenger);

        if (isUltimateNapping()) {
            float scale = Math.max(this.getScale(), 1f);
            float napXLocal = (idx == 0 ? 0.55f : 0.42f) * scale;
            Vec3 napSeatOffset = new Vec3(-napXLocal, 0, 0).yRot((float) Math.toRadians(-this.yBodyRot));
            double napRiderY = this.getY() + (idx == 0 ? 0.85 : 1.05) * scale + getRiderAnimYOffset();
            passenger.fallDistance = 0f;
            function.accept(passenger, this.getX() + napSeatOffset.x, napRiderY, this.getZ() + napSeatOffset.z);
            float fixedYaw = this.getYRot();
            passenger.setYRot(fixedYaw);
            if (passenger instanceof LivingEntity living) {
                living.yBodyRot = fixedYaw;
                living.yHeadRot = fixedYaw;
            }
            return;
        }

        if (idx == 0) {
            float seatZ = 0.35f + pawSlamRiderZExtra;
            float seatY = 0f;

            if (isPawSlamCharging()) {
                float chargeProgress = Math.min(pawSlamChargeAnimState.getAccumulatedTime() / 2500f, 1.0f);
                float eased = chargeProgress * chargeProgress * (3f - 2f * chargeProgress);
                seatZ -= 0.45f * eased;
                seatY = 0.6f * eased;
            }

            if (isPawSlamStriking()) {
                float elapsed = pawSlamStrikeAnimState.getAccumulatedTime() / 50f;
                float progress = Math.min(elapsed / 10f, 1.0f);

                if (progress < 0.4f) {
                    float rise = progress / 0.4f;
                    seatZ -= 0.45f;
                    seatY = 0.6f + rise * 0.3f;
                } else {
                    float fall = (progress - 0.4f) / 0.6f;
                    float fallEased = fall * fall * fall;
                    seatZ -= 0.45f * (1f - fallEased);
                    seatY = 0.9f * (1f - fallEased);
                }
            }

            Vec3 seatOffset = new Vec3(0f, 0, seatZ)
                    .yRot((float) Math.toRadians(-this.yBodyRot));

            double baseY = getBaseRiderYOffset(0);
            double riderY = this.getY() + baseY + getRiderAnimYOffset() + seatY;

            passenger.fallDistance = 0f;
            function.accept(passenger,
                    this.getX() + seatOffset.x,
                    riderY,
                    this.getZ() + seatOffset.z);

            if (passenger instanceof LivingEntity living) {
                living.yBodyRot = this.yBodyRot;
            }
        } else {
            float seatZ = -0.8f;
            Vec3 seatOffset = new Vec3(0f, 0, seatZ)
                    .yRot((float) Math.toRadians(-this.yBodyRot));

            double baseY = getBaseRiderYOffset(1);
            float animY = -bodyAnimY_passenger / 16.0f * this.getScale();
            double riderY = this.getY() + baseY + animY;

            passenger.fallDistance = 0f;
            function.accept(passenger,
                    this.getX() + seatOffset.x,
                    riderY,
                    this.getZ() + seatOffset.z);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity controlling = this.getControllingPassenger();
        if (controlling == null) {
            return super.isControlledByLocalInstance();
        }
        return this.getPassengers().indexOf(controlling) == 0 && super.isControlledByLocalInstance();
    }

    protected void handleFoodBarSystem() {
        if (isTame()) return;
        if (this.tickCount % 1200 == 0) {
            this.setFoodBarValue(this.getFoodBarValue() - 1);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource); // le drop générique de l'Âme est géré par OWEntity.die()

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }

        if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
            this.spawnAtLocation(this.getFoodPick());
        }
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @javax.annotation.Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (isSettingTarget) {
            return;
        }

        if (target != null && isNapping()) {
            return;
        }

        isSettingTarget = true;
        try {
            super.setTarget(target);

            if (target != null) {
                if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
                    kodiakBehaviorHandler.eatFoodInHisMouth(this.getFoodPick());

                    Vec3 lookDirection = this.getLookAngle();
                    double spawnX = this.getX() + lookDirection.x * 2.0;
                    double spawnY = this.getY() + 0.8;
                    double spawnZ = this.getZ() + lookDirection.z * 2.0;

                    salmonCatchedTimer = 0;
                    kodiakBehaviorHandler.isCatchSalmon = false;
                    this.setCatchingSalmon(false);

                    OWUtils.spawnItemParticles(this, Items.SALMON.getDefaultInstance(), spawnX, spawnY, spawnZ);

                    this.playSound(SoundEvents.GENERIC_EAT);

                    int foodValue = 5;
                    this.setFoodBarValue(this.getFoodBarValue() + foodValue);
                }
            }
        } finally {
            isSettingTarget = false;
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (damageSource.getDirectEntity() instanceof Bee) return false;
        if (isUltimateNapping()) {
            v = v * 0.2f;
        }
        if (!this.isTame()) {
            if (this.isSitting()) this.setSitting(false);
            if (this.isRubs()) this.setRubs(false);
        }
        boolean result = super.hurt(damageSource, v);
        if (isUltimateNapping() && !isNapping()) {
            setNap(true);
        }

        if (result && !this.isTame() && damageSource.getEntity() instanceof LivingEntity attacker) {
            this.startPersistentAngerTimer();
            this.setPersistentAngerTarget(attacker.getUUID());
            this.setTarget(attacker);
        }
        return result;
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getNapKillCount();
        if (kills < OWAttacksConstants.Kodiak.NAP_KILLS_REQUIRED) {
            setNapKillCount(kills + 1);
        }
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof KodiakEntity otherKodiak) {
            if (otherKodiak.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherKodiak.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherKodiak.getOwnerUUID());
            } else {
                return !otherKodiak.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item heldItem = itemStack.getItem();

        if (!this.isTame() && !this.isRolling() && !this.isSearchingInsideChest && !this.isSitting()
                && this.getTarget() == null && !this.isNapping() && (this.getFoodPick() == ItemStack.EMPTY ||
                this.getFoodPick() == null && !this.isRubs())) {
            if (itemStack.is(Tags.Items.FOODS) || itemStack.is(Items.HONEYCOMB)) {
                if (!this.level().isClientSide()) {
                    kodiakBehaviorHandler.pickupItemInHisMouth(heldItem.getDefaultInstance().copy());
                    itemStack.shrink(1);
                    lastPlayerWhoFeedHim = player;
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (!this.isTame() && this.getFoodPick() != null && !this.getFoodPick().isEmpty() && !this.isRubs()) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, this.getFoodPick().copy());
                this.setFoodPick(ItemStack.EMPTY);
                this.playSound(SoundEvents.ITEM_PICKUP);
                this.playSound((OWUtils.RANDOM(2) ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get()), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
                setNap(false);

                if (!player.isCreative() && !player.isSpectator()) {
                    if (!this.level().isClientSide()) {
                        this.setTarget(player);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        if (this.isCatchingSalmon()) {
            if (player.getMainHandItem().is(Items.WATER_BUCKET)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SALMON_BUCKET));
                this.setCatchingSalmon(false);
                this.playSound(SoundEvents.BUCKET_FILL_FISH);
                this.playSound((OWUtils.RANDOM(2) ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get()), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
                setNap(false);

                if (!player.isCreative() && !player.isSpectator()) {
                    if (!this.level().isClientSide()) {
                        this.setTarget(player);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseKodiakVariant());
            this.setInitialVariant(this.getVariant());

            this.setFoodBarValue(10);
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());
        this.setSkinShade(false);

        switch (skinIndex) {
            case 1 -> this.setVariant(KodiakVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setVariant(KodiakVariant.Cosmetics.SKELETON.variant);
            case 3 -> this.setSkinShade(true);
            // Réserver les indices 4-7 pour de futurs skins cosmétiques
            default -> this.setVariant(getInitialVariant());
        }

        if (playingEffects) playSkinChangeEffect();
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    public void createMiniShockwave() {
        Vec3 look = this.getLookAngle();
        double x = this.getX() + look.x * 2.0;
        double z = this.getZ() + look.z * 2.0;
        BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particleOption, x, this.getY(), z, 60, 1.5, 0.1, 1.5, 0.25);
        } else {
            AABB area = new AABB(x - 1.5, this.getY() - 0.1, z - 1.5, x + 1.5, this.getY() + 0.2, z + 1.5);
            for (int i = 0; i < 60; i++) {
                double px = area.minX + Math.random() * (area.maxX - area.minX);
                double py = area.minY + Math.random() * (area.maxY - area.minY);
                double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
                this.level().addParticle(particleOption, px, py, pz, 0, 0.1, 0);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_HIT, SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == KodiakVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private KodiakVariant chooseKodiakVariant() {
        KodiakVariant variant;
        if (chance >= 66.67) variant = KodiakVariant.BLACK;
        else if (chance >= 33.33) variant = KodiakVariant.GREY;
        else variant = KodiakVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(48, true);
        createSitAnimation(58, true);

        if (this.isNapping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 96;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        }

        if (!this.isNapping()) {
            this.napAnimationTimeout = 0;
            this.napAnimationState.stop();
        }

        if (this.isRolling()) {
            if (!this.rollingAnimationState.isStarted()) {
                this.rollTimer = 0;
                this.rollingAnimationState.start(this.tickCount);
            }
        } else {
            this.rollingAnimationState.stop();
        }

        if (this.isSniffing()) {
            if (this.sniffingAnimationTimeout <= 0) {
                this.sniffingAnimationTimeout = 15;
                this.sniffingAnimationState.start(this.tickCount);
            } else --this.sniffingAnimationTimeout;
        }

        if (!this.isSniffing()) {
            this.sniffingAnimationTimeout = 0;
            this.sniffingAnimationState.stop();
        }

        if (this.isRejectingItem()) {
            if (this.rejectingAnimationTimeout <= 0) {
                this.rejectingAnimationTimeout = 21;
                this.rejectingAnimationState.start(this.tickCount);
            } else --this.rejectingAnimationTimeout;
        }

        if (!this.isRejectingItem()) {
            this.rejectingAnimationTimeout = 0;
            this.rejectingAnimationState.stop();
        }

        if (this.isRubs()) {
            if (this.rubsAnimationTimeout <= 0) {
                this.rubsAnimationTimeout = 28;
                this.rubsAnimationState.start(this.tickCount);
            } else --this.rubsAnimationTimeout;
        }

        if (!this.isRubs()) {
            this.rubsAnimationTimeout = 0;
            this.rubsAnimationState.stop();
        }

        if (this.isPawSlamCharging()) {
            if (this.pawSlamChargeAnimTimer <= 0) {
                this.pawSlamChargeAnimTimer = Integer.MAX_VALUE;
                this.pawSlamChargeAnimState.start(this.tickCount);
            } else --this.pawSlamChargeAnimTimer;
        } else {
            this.pawSlamChargeAnimTimer = 0;
            this.pawSlamChargeAnimState.stop();
        }

        boolean isChargeAtMax = this.isPawSlamCharging() && this.pawSlamChargeAnimState.getAccumulatedTime() >= 3000L;
        if (isChargeAtMax) {
            if (this.pawSlamChargeFullAnimTimer <= 0) {
                this.pawSlamChargeFullAnimTimer = Integer.MAX_VALUE;
                this.pawSlamChargeFullAnimState.start(this.tickCount);
            } else --this.pawSlamChargeFullAnimTimer;
        } else {
            this.pawSlamChargeFullAnimTimer = 0;
            this.pawSlamChargeFullAnimState.stop();
        }

        if (this.isPawSlamStriking()) {
            if (this.pawSlamStrikeAnimTimer <= 0) {
                this.pawSlamStrikeAnimTimer = 35;
                this.pawSlamStrikeAnimState.start(this.tickCount);
            } else --this.pawSlamStrikeAnimTimer;
        } else {
            this.pawSlamStrikeAnimTimer = 0;
            this.pawSlamStrikeAnimState.stop();
        }

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (30 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        if (this.isCombo(comboNumber)) {
            if (timer <= 0) {
                timer = maxTimer;
                animationState.start(this.tickCount);
            } else {
                --timer;
            }
        } else {
            if (comboNumber != 3 && timer > 0) {
                --timer;
            } else {
                timer = 0;
                animationState.stop();
            }
        }

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public KodiakVariant getVariant() {
        return KodiakVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(KodiakVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public KodiakVariant getInitialVariant() {
        return KodiakVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(KodiakVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public float getBodyZRot_passenger() {
        return bodyZRot_passenger;
    }

    public float getBodyXRot_passenger() {
        return bodyXRot_passenger;
    }

    public void setSkinShade(boolean isShade) { this.entityData.set(IS_SHADE_SKIN, isShade);}

    public boolean isShade() { return this.entityData.get(IS_SHADE_SKIN);}

    public boolean isDirty() {
        return this.entityData.get(IS_DIRTY);
    }

    public void setDirty(boolean isDirty) {
        this.entityData.set(IS_DIRTY, isDirty);
        if (isDirty) this.playSound(SoundEvents.HONEY_BLOCK_PLACE);
    }

    public boolean isCatchingSalmon() {
        return this.entityData.get(IS_CATCHING_SALMON);
    }

    public void setCatchingSalmon(boolean catching) {
        this.entityData.set(IS_CATCHING_SALMON, catching);
    }

    public void setRolling(boolean isRolling) { this.entityData.set(IS_ROLLING, isRolling);}

    public boolean isRolling() { return this.entityData.get(IS_ROLLING);}

    public void setSniffing(boolean isSniffing) { this.entityData.set(IS_SNIFFING, isSniffing);}

    public boolean isSniffing() { return this.entityData.get(IS_SNIFFING);}

    public void setRejectItem(boolean isRejectingItem) { this.entityData.set(REJECT_ITEM, isRejectingItem);}

    public boolean isRejectingItem() { return this.entityData.get(REJECT_ITEM);}

    public void setRubs(boolean isRubs) { this.entityData.set(IS_RUBS, isRubs);}

    public boolean isRubs() { return this.entityData.get(IS_RUBS);}

    public void setRubYaw(float yaw) {
        this.rubYaw = yaw;
    }

    public float getRubYaw() {
        return this.rubYaw;
    }

    public void setFoodBarValue(int getFoodBarValue) {
        this.entityData.set(FOOD_BAR_VALUE, getFoodBarValue);
        if (getFoodBarValue() >= 10) this.entityData.set(FOOD_BAR_VALUE, 10);
        else if (getFoodBarValue() <= 0) this.entityData.set(FOOD_BAR_VALUE, 0);
    }

    public int getFoodBarValue() { return this.entityData.get(FOOD_BAR_VALUE);}

    public boolean isHungry() {
        return this.getFoodBarValue() <= 0;
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD);}

    public ItemStack getFoodPick() {
        return this.entityData.get(FOOD_PICK);
    }

    public void setFoodPick(ItemStack food) {
        this.entityData.set(FOOD_PICK, food);
        if (!food.isEmpty()) {
            startEatingTimer = true;
            startHoneyTimer = true;
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

        tag.putBoolean("isShade", this.isShade());

        tag.putInt("getFoodBarValue", this.getFoodBarValue());

        tag.putInt("napKillCount", getNapKillCount());

        if (lastPlayerWhoFeedHim != null) {
            tag.putUUID("LastFeederUUID", lastPlayerWhoFeedHim.getUUID());
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");

        this.entityData.set(IS_SHADE_SKIN, tag.getBoolean("isShade"));

        this.entityData.set(FOOD_BAR_VALUE, tag.getInt("getFoodBarValue"));

        this.entityData.set(ULTIMATE_NAP_KILL_COUNT, tag.getInt("napKillCount"));

        if (tag.hasUUID("LastFeederUUID")) {
            UUID feederUUID = tag.getUUID("LastFeederUUID");
            lastPlayerWhoFeedHim = this.level().getPlayerByUUID(feederUUID);
        }
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    // ── Bear Nap (ultime) ─────────────────────────────────────────────────────

    public void activateUltimateNap() {
        if (isUltimateNapping()) {
            cancelUltimateNap();
            return;
        }
        if (getNapKillCount() < OWAttacksConstants.Kodiak.NAP_KILLS_REQUIRED) return;
        float cost = OWAttacksConstants.Kodiak.NAP_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            setUltimateNapping(false);
            setNap(false);
            return;
        }
        setVitalEnergy(0);
        setNapKillCount(0);
        setUltimateNapping(true);
        setNap(true);
        ultimateNapDurationTimer = OWAttacksConstants.Kodiak.NAP_DURATION_TICKS;
        this.playSound(OWSounds.KODIAK_HURT.get(), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
    }

    private void cancelUltimateNap() {
        setUltimateNapping(false);
        setNap(false);
        ultimateNapDurationTimer = 0;
        this.playSound(OWSounds.KODIAK_HURT.get(), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
    }

    public int getNapKillCount() { return this.entityData.get(ULTIMATE_NAP_KILL_COUNT); }
    private void setNapKillCount(int count) { this.entityData.set(ULTIMATE_NAP_KILL_COUNT, count); }

    public boolean isUltimateNapping() { return this.entityData.get(IS_ULTIMATE_NAPPING); }
    private void setUltimateNapping(boolean napping) { this.entityData.set(IS_ULTIMATE_NAPPING, napping); }


    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel) this.level(), false);
        super.customServerAiStep();
    }

    // ── Paw Slam ──────────────────────────────────────────────────────────────

    public void startPawSlamCharge() {
        if (getVitalEnergy() > getMaxVitalEnergy() - OWAttacksConstants.Kodiak.PAW_SLAM_ENERGY) {
            canShowVitalEnergyLack = true;
            return;
        }
        setPawSlamCharging(true);
    }

    public void cancelPawSlamCharge() {
        setPawSlamCharging(false);
    }

    public void performPawSlam(float factor) {
        if (this.level().isClientSide()) return;

        float energyRequired = OWAttacksConstants.Kodiak.PAW_SLAM_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - energyRequired) {
            canShowVitalEnergyLack = true;
            return;
        }

        setVitalEnergy(getVitalEnergy() + energyRequired);

        setPawSlamCharging(false);
        setPawSlamStriking(true);
        pawSlamStrikeServerTimer = 0;
        this.playSound(OWSounds.KODIAK_HURT.get(), 1.5f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
        pawSlamPendingFactor = factor;
        pawSlamHitTimer = 7;
    }

    private void executePawSlamHit(float factor) {
        double yaw     = Math.toRadians(this.getYRot());
        double reach   = 2.0 + 1.5 * factor;   // distance du centre devant l'ours
        double width   = 1.5 + 1.0 * factor;   // demi-largeur de la boîte
        double centerX = this.getX() - Math.sin(yaw) * reach;
        double centerZ = this.getZ() + Math.cos(yaw) * reach;
        double centerY = this.getY() + 0.5;

        AABB area = new AABB(
                centerX - width, centerY - 1.0, centerZ - width,
                centerX + width, centerY + 2.0, centerZ + width);

        java.util.UUID myOwner = this.getOwnerUUID();
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (target == this) return false;
            if (this.getPassengers().contains(target)) return false;
            if (isAlliedTo(target)) return false;
            if (myOwner != null) {
                if (target.getUUID().equals(myOwner)) return false;
                if (target instanceof TamableAnimal ta && myOwner.equals(ta.getOwnerUUID())) return false;
            }
            return true;
        });

        // 1 cible → 50 %, 2 → 40 %, 3 → 30 %, 4 → 20 %, 5+ → 10 %
        float percent = Math.max(0.50f - (targets.size() - 1) * 0.10f, 0.10f);

        for (LivingEntity target : targets) {
            float damage = target.getHealth() * percent;
            // Cibles avec plus de 100 pv max : dégâts plafonnés entre 5 (factor=0) et 20 (factor=1)
            if (target.getMaxHealth() > 100f) {
                float cap = 5f + factor * 15f;
                damage = Math.min(damage, cap);
            }
            target.hurt(this.damageSources().mobAttack(this), damage);
            Vec3 diff = target.position().subtract(this.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(diff.x * 1.8, 0.6, diff.z * 1.8));
            target.hasImpulse = true;
        }

        createMiniShockwave();
    }

    public void setPawSlamCharging(boolean value) { this.entityData.set(IS_PAW_SLAM_CHARGING, value); }
    public boolean isPawSlamCharging() { return this.entityData.get(IS_PAW_SLAM_CHARGING); }
    public void setPawSlamStriking(boolean value) { this.entityData.set(IS_PAW_SLAM_STRIKING, value); }
    public boolean isPawSlamStriking() { return this.entityData.get(IS_PAW_SLAM_STRIKING); }

    @Override
    protected int getDefaultSkinIndex() { return 4; }

    class KodiakMeleeAttackGoal extends MeleeAttackGoal {

        public KodiakMeleeAttackGoal() {
            super(KodiakEntity.this, 5, true);
        }

        @Override
        public boolean canUse() {
            if (KodiakEntity.this.getRemainingPersistentAngerTime() <= 0) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (KodiakEntity.this.getRemainingPersistentAngerTime() <= 0) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            if (KodiakEntity.this.getRemainingPersistentAngerTime() <= 0) {
                KodiakEntity.this.startPersistentAngerTimer();
            }
            KodiakEntity.this.setMad(true);
            KodiakEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            KodiakEntity.this.setMad(false);
            KodiakEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 3.5;
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

    class KodiakNearestAttackableTargetGoal extends NearestAttackableTargetGoal {

        public KodiakNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee) {
            super(mob, targetType, mustSee);
        }

        @Override
        public boolean canUse() {
            if (!KodiakEntity.this.isHungry()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (!KodiakEntity.this.isHungry()) return false;
            return super.canContinueToUse();
        }
    }
}