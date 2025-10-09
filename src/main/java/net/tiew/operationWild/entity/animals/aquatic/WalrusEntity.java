package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.behavior.WalrusBehaviorHandler;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomStrollGoal;
import net.tiew.operationWild.entity.goals.walrus.WalrusFollowSeabugGoal;
import net.tiew.operationWild.entity.taming.TamingWalrus;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.variants.WalrusVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class WalrusEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 165.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(WalrusEntity.class, EntityDataSerializers.INT);

    public WalrusBehaviorHandler walrusBehaviorHandler;
    public TamingWalrus walrusTaming;

    public final AnimationState scratchAnimationState = new AnimationState();
    public final AnimationState stretchesAnimationState = new AnimationState();
    public final AnimationState laughAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState idleWaterAnimationState = new AnimationState();

    private long scratchAnimationStartTime = 0;
    private long stretchesAnimationStartTime = 0;
    private long laughAnimationStartTime = 0;
    private long napAnimationTimeout = 0;
    private long idleWaterAnimationTimeout = 0;

    private static final int SCRATCH_DURATION = 50;
    private static final int STRETCHES_DURATION = 150;
    private static final int LAUGH_DURATION = 120;

    private int scratchInterval = (int) OWUtils.generateRandomInterval(400, 800);
    private int stretchesInterval = (int) OWUtils.generateRandomInterval(300, 900);
    private int laughInterval = (int) OWUtils.generateRandomInterval(800, 1300);

    public WalrusEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initWalrusBehaviorAndTaming();
    }

    private void initWalrusBehaviorAndTaming() {
        this.walrusBehaviorHandler = new WalrusBehaviorHandler(this);
        this.walrusTaming = new TamingWalrus(this, walrusBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 27.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.165D)
                .add(Attributes.FOLLOW_RANGE, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ARMOR, 0.15D)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.7D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initWalrusBehaviorAndTaming();

        this.goalSelector.addGoal(1, new OWAttackGoal(this, this.getSpeed() * 15f, 8, 3, false));
        this.goalSelector.addGoal(2, new WalrusFollowSeabugGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 2D, Ingredient.of(OWTags.Items.WALRUS_FOOD), false));
        this.goalSelector.addGoal(4, new NapGoal(this, 1.15f, 150, true));
        this.goalSelector.addGoal(5, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new OWRandomStrollGoal(this, 1.0D, 40));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 8215109;
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
        return List.of(Drowned.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.6f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return false;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return true;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.KODIAK_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.WALRUS_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 275 * (1 + ((float) this.getLevel() / 50));
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
        return false;
    }

    @Override
    public boolean preferVegetables() {
        return false;
    }

    @Override
    public int getMaxAirSupply() {
        return 300 * 20;
    }

    @Override
    public int getMaxDepth() {
        return 10;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 10;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.WALRUS.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.WALRUS_FOOD);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() {
        return 1f;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        super.playStepSound(blockPos, blockState);
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.5F;
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) {
            this.jumpFromGround();
        }
    }

    @Override
    public void tick() {
        super.tick();
        walrusTaming.tick();

        createCombo(23, 15, OWSounds.KODIAK_HURTING_2.get(), 4.0, 2, 2.35, false, 1.5f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        handleGoldVariantEffects();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }
        if (this.isSaddled()) this.spawnAtLocation(acceptSaddle());
    }

    private ItemStack createSoulStack() {
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());
        Item item = soulStack.getItem();

        if (item instanceof AnimalSoulItem animalSoulItem) {
            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack,
                    new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));

            animalSoulItem.saveEntityType(fakeContext, Component.nullToEmpty(this.getClass().getSimpleName()));
            animalSoulItem.saveEntityOwner(fakeContext, Component.nullToEmpty(this.getOwner() != null ? this.getOwner().getName().getString() : ""));
            animalSoulItem.saveEntityGender(fakeContext, this.isMale());
            animalSoulItem.saveEntityMaxHealth(fakeContext, this.getMaxHealth());
            animalSoulItem.saveEntityDamages(fakeContext, this.getDamage());
            animalSoulItem.saveEntitySpeed(fakeContext, this.getSpeed());
            animalSoulItem.saveEntityScale(fakeContext, this.getScale());
            animalSoulItem.saveEntityLevel(fakeContext, this.getLevel());
            animalSoulItem.saveEntityVariant(fakeContext, this.getVariant().getId());
        }

        return soulStack;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.getDirectEntity() instanceof AbstractArrow) {
            return true;
        }

        if (source.getDirectEntity() instanceof ThrowableItemProjectile throwable) {
            String name = throwable.getClass().getSimpleName();
            boolean isExcluded = name.equals("Snowball") || name.equals("ThrownEgg") ||
                    name.equals("ThrownEnderpearl") || name.equals("ThrownPotion") ||
                    name.equals("ThrownExperienceBottle");

            return !isExcluded;
        }

        return super.isInvulnerableTo(source);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof WalrusEntity otherWalrus) {
            if (this.isTame()) {
                return otherWalrus.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherWalrus.getOwnerUUID());
            } else {
                return !otherWalrus.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY() - 0.8, entity.getZ());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (isFood(itemStack) && !this.isTame()) {
            foodGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            itemStack.shrink(1);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && foodGiven >= foodWanted) {
                    this.setTame(true, player);
                    this.setSleeping(false);
                    resetSleepBar();
                }
            }
            return InteractionResult.SUCCESS;
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

            this.setVariant(chooseWalrusVariant());
            this.setInitialVariant(this.getVariant());
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private void handleGoldVariantEffects() {
        /*if (this.getVariant() == WalrusVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }*/
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (!this.level().isClientSide()) {
            Level world = this.level();
            if (world instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) world;
                serverWorld.sendParticles(ParticleTypes.ITEM_SLIME,
                        this.getX(), this.getY() + 1, this.getZ(),
                        100,
                        0.5, 0.5, 0.5,
                        0.02
                );
            }
        }
    }

    private WalrusVariant chooseWalrusVariant() {
        WalrusVariant variant;
        if (chance >= 50) variant = WalrusVariant.RED;
        else variant = WalrusVariant.DEFAULT;
        return variant;
    }

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }

    protected void handleMiscIdleAnimations() {
        if (this.scratchAnimationState.isStarted() &&
                this.tickCount - scratchAnimationStartTime > SCRATCH_DURATION) {
            this.scratchAnimationState.stop();
        }
        if (this.stretchesAnimationState.isStarted() &&
                this.tickCount - stretchesAnimationStartTime > STRETCHES_DURATION) {
            this.stretchesAnimationState.stop();
        }
        if (this.laughAnimationState.isStarted() &&
                this.tickCount - laughAnimationStartTime > LAUGH_DURATION) {
            this.laughAnimationState.stop();
        }

        if (tickCount % scratchInterval == 0) {
            if (walrusBehaviorHandler.canScratch() && walrusBehaviorHandler.canPlayIdleAnimation() && !walrusBehaviorHandler.isAnyIdleAnimationPlaying()) {
                this.scratchAnimationState.start(this.tickCount);
                scratchAnimationStartTime = this.tickCount;
            }

            scratchInterval = (int) OWUtils.generateRandomInterval(400, 800);
        }

        if (tickCount % stretchesInterval == 0) {
            if (walrusBehaviorHandler.canStretches() && walrusBehaviorHandler.canPlayIdleAnimation() && !walrusBehaviorHandler.isAnyIdleAnimationPlaying()) {
                this.stretchesAnimationState.start(this.tickCount);
                stretchesAnimationStartTime = this.tickCount;
            }

            stretchesInterval = (int) OWUtils.generateRandomInterval(300, 900);
        }

        if (tickCount % laughInterval == 0) {
            if (walrusBehaviorHandler.canLaugh() && walrusBehaviorHandler.canPlayIdleAnimation() && !walrusBehaviorHandler.isAnyIdleAnimationPlaying()) {
                this.laughAnimationState.start(this.tickCount);
                laughAnimationStartTime = this.tickCount;
            }

            laughInterval = (int) OWUtils.generateRandomInterval(500, 1100);
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(57, true);
        createSitAnimation(80, true);

        handleMiscIdleAnimations();

        if (this.isInWater()) {
            if (this.idleWaterAnimationTimeout <= 0) {
                this.idleWaterAnimationTimeout = 51;
                this.idleWaterAnimationState.start(this.tickCount);
            } else --this.idleWaterAnimationTimeout;
        }

        if (!this.isInWater()) {
            this.idleWaterAnimationTimeout = 0;
            this.idleWaterAnimationState.stop();
        }

        if (this.isNapping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 80;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        }

        if (!this.isNapping()) {
            this.napAnimationTimeout = 0;
            this.napAnimationState.stop();
        }
    }

    public WalrusVariant getVariant() {
        return WalrusVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(WalrusVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public WalrusVariant getInitialVariant() {
        return WalrusVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(WalrusVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
    }
}