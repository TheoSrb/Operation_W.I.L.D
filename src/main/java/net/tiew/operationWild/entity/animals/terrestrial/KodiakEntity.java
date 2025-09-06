package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.particle.OWParticles;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.core.OWUtils;

import java.util.*;
import java.util.function.Predicate;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class KodiakEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 180.0;
    public static final int TERRITORY_RADIUS = 80;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> FOOD_CHOOSE_FROM_CHEST = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RUB = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.BOOLEAN);

    public AnimationState transitionIdleStandingUp = new AnimationState();
    public AnimationState transitionStandingUpIdle = new AnimationState();

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState sleepingAnimationState = new AnimationState();
    public final AnimationState standingUpIdleAnimationState = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int sleepingAnimationTimeout = 0;
    public int standingUpIdleAnimationTimeout = 0;

    private int eatFoodFromChestTimer = 0;
    private int dirtyTimer = 0;

    public boolean playerEnteringInTerritory = true;

    public KodiakEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 45.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.FOLLOW_RANGE, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 30f, 15, 3));
        this.goalSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 30f, 8, 3, true));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8f));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new OWNearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(8, new NonTameRandomTargetGoal<>(this, Pillager.class, false, (Predicate<LivingEntity>) null));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(FOOD_CHOOSE_FROM_CHEST, ItemStack.EMPTY);
        builder.define(IS_MAD, false);
        builder.define(IS_RUB, false);
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
        return RANDOM(2) ? RANDOM(2) ? OWSounds.KODIAK_IDLE_1.get() : RANDOM(2) ? OWSounds.KODIAK_IDLE_2.get() : OWSounds.KODIAK_IDLE_3.get() : null;
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
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!isRunning()) super.playStepSound(blockPos, blockState);
    }

    public void tick() {
        super.tick();
        createCombo((int) (20 / comboSpeedMultiplier), (int) (12 / comboSpeedMultiplier), random.nextInt(2) == 0 ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 2.25, false, 2);

        createTerritorySystem();
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMad(this.isCombo());
        }

        handleRunningEffects(29, SoundEvents.HORSE_STEP, 0.5f, new int[]{5, 19});
        handleNappingEffects();
        handleGoldVariantEffects();
    }

    private void handleNappingEffects() {
        if (this.isNapping()) {
            setTarget(null);
            if (this.tickCount % 20 == 0) {
                Vec3 lookDirection = this.getLookAngle();
                double entityX = this.getX();
                double entityY = this.getY() + 1.15;
                double entityZ = this.getZ();
                double fixedX = entityX + lookDirection.x * 1.25;
                double fixedY = entityY;
                double fixedZ = entityZ + lookDirection.z * 1.25;
                this.level().addParticle(OWParticles.NAP_PARTICLES.get(),
                        fixedX, fixedY, fixedZ,
                        0, 0, 0);
            }
        }
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == KodiakVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) {
            this.jumpFromGround();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }

        if (!getFoodChooseFromChest().isEmpty()) {
            this.spawnAtLocation(getFoodChooseFromChest());
        }

        if (this.isSaddled()) {
            this.spawnAtLocation(OWItems.KODIAK_SADDLE.get());
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

        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!getFoodChooseFromChest().isEmpty()) {
            this.spawnAtLocation(getFoodChooseFromChest());
            this.setFoodChooseFromChest(ItemStack.EMPTY);
        }
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        this.setFoodChooseFromChest(ItemStack.EMPTY);
        this.playSound(SoundEvents.GENERIC_EAT);
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

        if (handleFoodRetrievalInteraction(player, hand, itemStack)) {
            return InteractionResult.SUCCESS;
        }

        if (handleTamingInteraction(player, itemStack)) {
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private boolean handleFoodRetrievalInteraction(Player player, InteractionHand hand, ItemStack itemStack) {
        if (!isBaby() && !getFoodChooseFromChest().isEmpty() && itemStack.isEmpty()) {
            player.setItemInHand(hand, this.getFoodChooseFromChest());
            player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
            setFoodChooseFromChest(ItemStack.EMPTY);

            if (this.random.nextInt(2) == 0) {
                if (!player.isCreative()) {
                    this.setTarget(player);
                    this.setMad(true);
                }
            }
            return true;
        }
        return false;
    }

    private boolean handleTamingInteraction(Player player, ItemStack itemStack) {
        if (isFood(itemStack) && !this.isTame()) {
            this.foodGiven++;
            this.setNap(false, 0);
            itemStack.shrink(1);
            this.playSound(SoundEvents.CAMEL_EAT);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && this.foodGiven >= this.foodWanted) {
                    this.setTame(true, player);
                    this.setSleeping(false);
                    resetSleepBar();
                }
            }
            return true;
        }
        return false;
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
        this.foodWanted = (int) OWUtils.generateRandomInterval(10, 16);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) {
            setVariant(KodiakVariant.SKIN_GOLD);
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

    public void createTerritorySystem() {
        if (this.isTame() || this.isBaby()) return;

        List<Player> playersInTerritory = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(TERRITORY_RADIUS));
        boolean foundValidPlayer = false;

        for (Player player : playersInTerritory) {
            if (player.isCreative()) continue;

            foundValidPlayer = true;

            if (playerEnteringInTerritory && this.random.nextInt(2) == 0 && !player.hasEffect(OWEffects.OCCUPIED_TERRITORY)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    OWUtils.showMessage(serverPlayer, Component.translatable("tooltip.enteringKodiakTerritory"), 0xc27452, false);
                }
                playerEnteringInTerritory = false;
            }
            player.addEffect(new MobEffectInstance(OWEffects.OCCUPIED_TERRITORY, 300, 0));
            break;
        }

        if (!foundValidPlayer) {
            playerEnteringInTerritory = true;
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

    private boolean canNap() {
        return this.isAlive() && this.getTarget() == null && this.onGround() && !this.isBaby() && !this.isTame() && getFoodChooseFromChest().isEmpty();
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

        setupStandingUpIdleAnimation();
        setupComboAnimations();
        setupSleepingAnimation();
    }

    private void setupStandingUpIdleAnimation() {
        if (this.isRub()) {
            if (this.standingUpIdleAnimationTimeout <= 0) {
                this.standingUpIdleAnimationTimeout = 80;
                this.standingUpIdleAnimationState.start(this.tickCount);
            } else {
                --this.standingUpIdleAnimationTimeout;
            }
        } else {
            this.standingUpIdleAnimationTimeout = 0;
            this.standingUpIdleAnimationState.stop();
        }
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

    private void setupSleepingAnimation() {
        if (this.isNapping()) {
            if (this.sleepingAnimationTimeout <= 0) {
                this.sleepingAnimationTimeout = 96;
                this.sleepingAnimationState.start(this.tickCount);
            } else {
                --this.sleepingAnimationTimeout;
            }
        } else {
            this.sleepingAnimationTimeout = 0;
            this.sleepingAnimationState.stop();
        }
    }

    public void setFoodChooseFromChest(ItemStack item, boolean playItemPickupSound) {
        this.entityData.set(FOOD_CHOOSE_FROM_CHEST, item);

        if (playItemPickupSound) {
            this.playSound(SoundEvents.ITEM_PICKUP);
        }

        if (!item.isEmpty()) {
            eatFoodFromChestTimer = 1;
        }
    }

    public void setFoodChooseFromChest(ItemStack item) {
        this.entityData.set(FOOD_CHOOSE_FROM_CHEST, item);
        if (!item.isEmpty()) {
            eatFoodFromChestTimer = 1;
        }
    }

    public ItemStack getFoodChooseFromChest() {
        return this.entityData.get(FOOD_CHOOSE_FROM_CHEST);
    }

    public void setMad(boolean isMad) {
        if (isMad) {
            if (this.getCurrentMode() == Mode.Passive) return;
        }
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() {
        return this.entityData.get(IS_MAD);
    }

    public void setRub(boolean isRub) {
        this.entityData.set(IS_RUB, isRub);
    }

    public boolean isRub() {
        return this.entityData.get(IS_RUB);
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

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("dirtyTimer", this.dirtyTimer);

        if (getFoodChooseFromChest() != null) {
            if (!getFoodChooseFromChest().isEmpty() && level() != null) {
                CompoundTag itemTag = (CompoundTag) getFoodChooseFromChest().save(level().registryAccess());
                tag.put("getFoodChooseFromChest", itemTag);
            }
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.dirtyTimer = tag.getInt("dirtyTimer");

        if (tag.contains("getFoodChooseFromChest", Tag.TAG_COMPOUND)) {
            CompoundTag itemTag = tag.getCompound("getFoodChooseFromChest");
            Optional<ItemStack> optionalStack = ItemStack.parse(level().registryAccess(), itemTag);
            optionalStack.ifPresent(this::setFoodChooseFromChest);
        }
    }
}