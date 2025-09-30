package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.tiew.operationWild.core.OWUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.tiew.operationWild.entity.AI.AIKodiak;
import net.tiew.operationWild.entity.AI.AIKodiakManagement;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.OWAttackGoal;
import net.tiew.operationWild.entity.goals.OWBreedGoal;
import net.tiew.operationWild.entity.goals.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakAttractedToGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakRollGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakSearchInsideChestGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakTemptGoal;
import net.tiew.operationWild.entity.taming.TamingKodiak;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class KodiakEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {

    public AIKodiak kodiakAI;
    public AIKodiakManagement kodiakManagement;
    public TamingKodiak kodiakTaming;

    public static final double TAMING_EXPERIENCE = 180.0;

    public AnimationState transitionIdleStandingUp = new AnimationState();
    public AnimationState transitionStandingUpIdle = new AnimationState();

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState rollingAnimationState = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int napAnimationTimeout = 0;
    public int rollingAnimationTimeout = 0;

    public int rollTimer = 0;

    protected boolean canAttack = false;
    public ItemStack foodPick = ItemStack.EMPTY;

    public boolean startEatingTimer = false;
    private boolean startHoneyTimer = false;
    private static final int MAX_EATING_TIMER = 400;
    private static final int MAX_HONEY_TIMER = 750;
    public int eatingTimer = 0;
    private int honeyTimer = 0;

    public Player lastPlayerWhoFeedHim = null;
    public int numberOfBonusSearching = 0;
    private int numberOfBonusSearchingMax = this.random.nextInt(7) + 5;
    public int cropCheckTimer = 0;
    public BlockPos targetCrop = null;
    public int cropRadiusSearch = 0;

    private final int MAX_DIRTY_TIMER = 1200;
    private int dirtyTimer = 0;

    public ChestBlockEntity chestBlockEntity = null;
    public boolean isSearchingInsideChest = false;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SHADE_SKIN = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_ROLLING = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<ItemStack> FOOD_PICK = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> IS_DIRTY = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);

    public KodiakEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        createKodiakAI();
    }

    private void createKodiakAI() {
        this.kodiakAI = new AIKodiak(this);
        this.kodiakManagement = new AIKodiakManagement(this, kodiakAI);
        this.kodiakTaming = new TamingKodiak(this, kodiakManagement);
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
        this.registerBasicsGoals();

        createKodiakAI(); // Create the AI before the goals, otherwise null error

        this.kodiakAI = new AIKodiak(this);
        this.kodiakManagement = new AIKodiakManagement(this, kodiakAI);
        this.kodiakTaming = new TamingKodiak(this, kodiakManagement);

        this.goalSelector.addGoal(1, new KodiakAttractedToGoal<>(this, ItemEntity.class,
                1.75f, 15, 5.0f, () -> kodiakManagement.pickupItemInHisMouth(foodPick), this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(2, new KodiakSearchInsideChestGoal(this, 2.0f, 35,
                1.5f, () -> kodiakManagement.openChest(chestBlockEntity)));

        this.goalSelector.addGoal(3, new NapGoal(this, 0.5f, 700, true));

        this.goalSelector.addGoal(5, new KodiakAttractedToGoal<>(this, Blocks.BEE_NEST,
                1.75f, 25, 2.0f, kodiakManagement::lookForHoneyInTheBeeNest, this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(7, new KodiakRollGoal(this, 0.9f));

        this.goalSelector.addGoal(11, new KodiakAttractedToGoal<>(this, Blocks.CAMPFIRE,
                1.0f, 60, 1.0f, () -> kodiakManagement.pickupItemInHisMouth(foodPick), this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(13, new KodiakAttractedToGoal<>(this, BlockTags.CROPS,
                1.15f, 80, 0.5f, () -> kodiakManagement.goToNewCropBlock(20), this.getFoodPick().isEmpty()));
    }

    private void registerBasicsGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new KodiakTemptGoal(this, 2D, Ingredient.of(Tags.Items.FOODS), false));
        this.goalSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 30f, 8, 3, canAttack()));
        this.goalSelector.addGoal(6, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));

        /*this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Monster.class, true));*/
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_SHADE_SKIN, false);
        builder.define(IS_ROLLING, false);
        builder.define(FOOD_PICK, ItemStack.EMPTY);
        builder.define(IS_DIRTY, false);
    }

    // Entity Methods
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
    public float vehicleRunSpeedMultiplier() {
        return 4f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2;
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
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.KODIAK.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.KODIAK_FOOD);
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

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!isRunning()) super.playStepSound(blockPos, blockState);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.onGround()) {
            kodiakManagement.trampleCrops(this.blockPosition());
            kodiakManagement.trampleCrops(this.blockPosition().below());
        }
    }

    public void tick() {
        super.tick();
        kodiakAI.tick();
        kodiakTaming.tick();

        boolean hasSomethingInHisMouth = getFoodPick() != null && !getFoodPick().isEmpty();

        createCombo((int) (20 / comboSpeedMultiplier), (int) (12 / comboSpeedMultiplier), random.nextInt(2) == 0 ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 2.25, false, 2);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (isSearchingInsideChest) this.setNap(false);

        if (hasSomethingInHisMouth) {
            if (startEatingTimer) {
                if (eatingTimer < MAX_EATING_TIMER) eatingTimer++;
                else {
                    kodiakManagement.eatFoodInHisMouth(getFoodPick());
                }
            }

            if (getFoodPick() == Items.HONEYCOMB.getDefaultInstance()) {
                if (startHoneyTimer) {
                    if (honeyTimer < MAX_HONEY_TIMER) honeyTimer++;
                    else {
                        kodiakManagement.eatFoodInHisMouth(getFoodPick());
                    }
                }
            }
        }

        if (this.isRolling()) {
            this.rollTimer++;

            kodiakManagement.trampleCrops(this.blockPosition());
            kodiakManagement.trampleCrops(this.blockPosition().below());

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

        if (isSearchingInsideChest) {
            this.setDeltaMovement(0,0,0);

            if (chestBlockEntity != null) {
                BlockPos chestPos = chestBlockEntity.getBlockPos();
                this.setLookAt(chestPos.getX(), chestPos.getY(), chestPos.getZ());
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
                        kodiakManagement.goToNewCropBlock(cropRadiusSearch);
                    }
                }
                targetCrop = null;
            }
        }

        if (this.isDirty()) {
            if (dirtyTimer <= MAX_DIRTY_TIMER) {
                dirtyTimer++;
            } else {
                dirtyTimer = 0;
                setDirty(false);
            }
        }

        handleRunningEffects(29, SoundEvents.HORSE_STEP, 0.5f, new int[]{5, 19});
        handleGoldVariantEffects();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
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
    public void setTarget(@Nullable LivingEntity target) {
        if (isNapping()) return;
        super.setTarget(target);

        if (target != null) {
            if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
                kodiakManagement.eatFoodInHisMouth(this.getFoodPick());
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (damageSource.getDirectEntity() instanceof Bee) return false;
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
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
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        Vec3 movement = this.getDeltaMovement();
        Vec3 look = this.getLookAngle();
        double dot = movement.normalize().dot(look.normalize());
        int passengerIndex = this.getPassengers().indexOf(entity);

        if (passengerIndex == 0) {
            positionFirstPassenger(entity, moveFunction, look, dot);
        } else if (passengerIndex == 1) {
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY(), this.getZ() - (look.z / 1.5));
        } else if (this.isRunning() && dot >= 0.1) {
            float yOffset = calculateAnimatedYOffset(1.44F, 1.0f, 17.0F, 0.0F, 0.6F);
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY() + yOffset, this.getZ() - (look.z / 1.5));
        } else {
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY(), this.getZ() - (look.z / 1.5));
        }
    }

    private void positionFirstPassenger(Entity entity, MoveFunction moveFunction, Vec3 look, double dot) {
        if (entity instanceof Player player) {
            float comboOffset = getComboAttack() == 3 ? 0.35f : 0;

            if (player.zza == 0) {
                moveFunction.accept(entity,
                        this.getX() + (look.x / 2.5),
                        entity.getY() + (-0.2f) + comboOffset,
                        this.getZ() + (look.z / 2.5));
            } else if (this.isRunning() && dot >= 0.1) {
                float yOffset = calculateAnimatedYOffset(1.44F, 1.0f, 17.0F, 0.0F, 0.6F);
                moveFunction.accept(entity,
                        this.getX(),
                        entity.getY() + (-0.2f) + yOffset + comboOffset,
                        this.getZ());
            } else {
                moveFunction.accept(entity,
                        this.getX() + (look.x / 2.5),
                        entity.getY() + (-0.2f) + comboOffset,
                        this.getZ() + (look.z / 2.5));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item heldItem = itemStack.getItem();
        if (this.getTarget() == null && !this.isNapping() && (this.getFoodPick() == ItemStack.EMPTY || this.getFoodPick() == null)) {
            if (itemStack.is(Tags.Items.FOODS) || itemStack.is(Items.HONEYCOMB)) {
                kodiakManagement.pickupItemInHisMouth(heldItem.getDefaultInstance().copy());
                itemStack.shrink(1);
                lastPlayerWhoFeedHim = player;

                return InteractionResult.SUCCESS;
            }
        }

        if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, this.getFoodPick().copy());
                this.setFoodPick(ItemStack.EMPTY);
                this.playSound(SoundEvents.ITEM_PICKUP);
                this.playSound((OWUtils.RANDOM(2) ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get()), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
                setNap(false);
                this.setTarget(player);

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
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());
        this.setSkinShade(false);

        if (skinIndex == 1) {
            setVariant(KodiakVariant.SKIN_GOLD);
        } else if (skinIndex == 2) {
            setVariant(KodiakVariant.SKIN_SKELETON);
        } else if (skinIndex == 3) {
            setSkinShade(true);
        } else if (skinIndex == 7) {
            setVariant(getInitialVariant());
        }

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

    public void createMiniShockwave() {
        Vec3 look = this.getLookAngle();
        double x = this.getX() + look.x * 2.0;
        double z = this.getZ() + look.z * 2.0;
        AABB area = new AABB(x - 1, this.getY() - 1, z - 1, x + 1, this.getY() + 1, z + 1);

        for (int i = 0; i < 50; i++) {
            double px = area.minX + Math.random() * (area.maxX - area.minX);
            double py = area.minY + Math.random() * (area.maxY - area.minY);
            double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
            BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
            this.level().addParticle(particleOption, px, py, pz, 0, 0, 0);
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
            if (this.rollingAnimationTimeout <= 0) {
                this.rollingAnimationTimeout = 80;
                this.rollingAnimationState.start(this.tickCount);
            } else --this.rollingAnimationTimeout;
        }

        if (!this.isRolling()) {
            this.rollingAnimationTimeout = 0;
            this.rollingAnimationState.stop();
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
            timer = 0;
            animationState.stop();
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

    public void setSkinShade(boolean isShade) { this.entityData.set(IS_SHADE_SKIN, isShade);}

    public boolean isShade() { return this.entityData.get(IS_SHADE_SKIN);}

    protected boolean canAttack() {
        return canAttack;
    }

    protected void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    public boolean isDirty() {
        return this.entityData.get(IS_DIRTY);
    }

    public void setDirty(boolean isDirty) {
        this.entityData.set(IS_DIRTY, isDirty);
        this.playSound(SoundEvents.HONEY_BLOCK_PLACE);
    }

    public void setRolling(boolean isRolling) { this.entityData.set(IS_ROLLING, isRolling);}

    public boolean isRolling() { return this.entityData.get(IS_ROLLING);}

    public ItemStack getFoodPick() {
        return this.entityData.get(FOOD_PICK);
    }

    public void setFoodPick(ItemStack food) {
        this.entityData.set(FOOD_PICK, food);
        if (!food.isEmpty()) {
            startEatingTimer = true;
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

        tag.putBoolean("isShade", this.isShade());

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

        if (tag.hasUUID("LastFeederUUID")) {
            UUID feederUUID = tag.getUUID("LastFeederUUID");
            lastPlayerWhoFeedHim = this.level().getPlayerByUUID(feederUUID);
        }
    }
}