package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.OWTameImplementation;
import net.tiew.operationWild.entity.config.IOWFoodsPreference;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.OWEgg;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.PeacockVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.PeacockFoodsSendToClient;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class PeacockEntity extends OWEntity implements IOWFoodsPreference, OWEntityUtils, OWTameImplementation {

    public static final double TAMING_EXPERIENCE = 25.0;

    public String[] quests = {};
    public final AnimationState fearAnimationState = new AnimationState();
    public final AnimationState deployingAnimationState = new AnimationState();
    public final AnimationState stayingDeployingAnimationState = new AnimationState();
    public final AnimationState stoppingDeployingAnimationState = new AnimationState();
    public int fearAnimationTimeout = 0;
    public int deployingAnimationTimeout = 0;
    public int stayingDeployingAnimationTimeout = 0;
    public int stoppingDeployingAnimationTimeout = 0;
    public boolean canFearEntities = true;
    public LivingEntity lastTarget = null;
    public int fearEntitiesTimer = 0;
    public int startDeployingTimer = 0;
    public int stayingDeployingTimer = 0;
    public int stoppingDeployingTimer = 0;
    public boolean femaleAccept = false;
    public int foodGiven = 0;
    public int foodWanted;
    public boolean peacockIsAggressive = false;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FEAR_ENTITIES = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_FLEE = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> START_DEPLOYING = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAY_DEPLOYING = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STOP_DEPLOYING = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_MAGE_SKIN = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);

    public PeacockVariant getVariant() { return PeacockVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(PeacockVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public PeacockVariant getInitialVariant() { return PeacockVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(PeacockVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public PeacockEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x464bc1;
    }

    @Override
    public float getEntityScale() {
        return 3.25f;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.PEACOCK_SADDLE.get();
    }

    @Override
    public List<Class<?>> getEntityType() {
        return MARAUDER_ENTITIES;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.PEACOCK_TAMED_ADVANCEMENT;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 175 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1f * (1 + ((float) this.getLevel() / 50));
    }


    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(1, new OWPanicGoal(this, this.getSpeed() * 16f, 3, 75));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.goalSelector.addGoal(1, new PeacockBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(OWItems.SAVAGE_BERRIES.get()), false));

        this.targetSelector.addGoal(1, new OWAttackGoal(this, this.getSpeed() * 15f,15, 1, true));

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 12D).add(Attributes.MOVEMENT_SPEED, 0.19D).add(Attributes.FOLLOW_RANGE, 20D).add(Attributes.ATTACK_DAMAGE, 2D).add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) { return OWEntityRegistry.PEACOCK.get().create(serverLevel);}

    @Override
    public boolean isFood(ItemStack itemStack) { return itemStack.is(OWItems.SAVAGE_BERRIES.get());}

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? OWSounds.PEACOCK_IDLE.get() : null;
    }

    protected float getSoundVolume() { return 4f;}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        super.playStepSound(blockPos, blockState);
    }

    public void setFearEntities(boolean isFearEntities) { this.entityData.set(FEAR_ENTITIES, isFearEntities);}

    public boolean isFearEntities() { return this.entityData.get(FEAR_ENTITIES);}

    public void setCanFlee(boolean isCanFlee) { this.entityData.set(CAN_FLEE, isCanFlee);}

    public boolean isCanFlee() { return this.entityData.get(CAN_FLEE);}

    public void setStartDeploying(boolean isDeploying) { this.entityData.set(START_DEPLOYING, isDeploying);}

    public boolean isDeploying() { return this.entityData.get(START_DEPLOYING);}

    public void setStayDeploying(boolean isStayingDeploying) { this.entityData.set(STAY_DEPLOYING, isStayingDeploying);}

    public boolean isStayingDeploying() { return this.entityData.get(STAY_DEPLOYING);}

    public void setStopDeploying(boolean isStoppingDeploying) { this.entityData.set(STOP_DEPLOYING, isStoppingDeploying);}

    public boolean isStoppingDeploying() { return this.entityData.get(STOP_DEPLOYING);}

    public void setMageSkin(boolean isMage) { this.entityData.set(IS_MAGE_SKIN, isMage);}
    public boolean isMage() { return this.entityData.get(IS_MAGE_SKIN);}

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());

        Item item = soulStack.getItem();
        if (item instanceof AnimalSoulItem animalSoulItem) {
            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack, new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));

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

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }
        if (this.isSaddled()) this.spawnAtLocation(this.acceptSaddle());
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }


    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());
        this.setMageSkin(false);

        if (skinIndex == 1) setVariant(PeacockVariant.SKIN_GOLD);
        if (skinIndex == 2) setMageSkin(true);
        else if (skinIndex == 7) setVariant(getInitialVariant());

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

    public void tick() {
        super.tick();
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new PeacockFoodsSendToClient(this.getId(), this.foodGiven, this.foodWanted), player);
                }
            }
        }

        if (!this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) createTameAttackSystem(10, 6, SoundEvents.ANVIL_LAND, 3.0, 3.5, 1.5, true);
        if (this.level().isClientSide()) setupAnimationState();

        if (isFearEntities() && !isBaby()) {
            if (this.isAlive()) {
                if (lastTarget != null) {
                    this.setLookAt(lastTarget.getX(), lastTarget.getY(), lastTarget.getZ());
                    fearEntitiesTimer++;
                    if (this.onGround()) this.setDeltaMovement(0, 0, 0);
                    this.hasImpulse = false;
                    this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));

                    if (fearEntitiesTimer >= 12 && fearEntitiesTimer < 32 && this.isEntityLookingAtThis(lastTarget, this.isTame() ? 0.25 : 0.5)) {
                        lastTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
                        lastTarget.addEffect(new MobEffectInstance(OWEffects.FEAR_EFFECT.getDelegate(), 412, 0));
                    }
                    if (fearEntitiesTimer >= 43) {
                        resetFearEntities();
                    }
                }
            } else {
                resetFearEntities();
            }

        }

        if (this.isFemale()) {
            List<PeacockEntity> peacocks = this.level().getEntitiesOfClass(PeacockEntity.class, getBoundingBox().inflate(20));

            for (PeacockEntity peacock : peacocks) {
                if (peacock.isMale() && peacock.isStayingDeploying() && femaleAccept) {
                    if (this.distanceTo(peacock) < 2) {
                        createEgg(peacock);
                    } else this.getNavigation().moveTo(peacock.getX(), peacock.getY(), peacock.getZ(), 1.0f);
                    break;
                }
            }
        }

        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.isInFight() && !isBaby() && !this.isTame() && this.isMale() && tickCount % 1248 == 0 && !this.isSleeping() && !this.isNapping() && !this.isSitting() && !this.isStayingDeploying()) {
            List<PeacockEntity> peacocks = this.level().getEntitiesOfClass(PeacockEntity.class, getBoundingBox().inflate(20));
            for (PeacockEntity females : peacocks) {
                if (females.isAlive() && !females.isTame()) {
                    this.setStartDeploying(true);
                    break;
                }
            }
        }

        if (isDeploying() && !isBaby()) {
            startDeployingTimer++;
            if (this.onGround()) this.setDeltaMovement(0, 0, 0);
            this.hasImpulse = false;
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
            if (startDeployingTimer >= 40) {
                this.setStartDeploying(false);
                this.setStayDeploying(true);
                this.startDeployingTimer = 0;
            }
        }

        if (isStayingDeploying() && !isBaby()) {
            stayingDeployingTimer++;
            List<PeacockEntity> peacocks = this.level().getEntitiesOfClass(PeacockEntity.class, getBoundingBox().inflate(20));
            for (PeacockEntity females : peacocks) {
                if (females.isAlive() && females.isFemale()) {
                    if (this.distanceTo(females) >= 3) {
                        this.getNavigation().moveTo(females.getX(), females.getY(), females.getZ(), 1.0f);
                    }
                }
            }

            if (stayingDeployingTimer >= 560 || isInFight()) {
                this.setStayDeploying(false);
                this.setStopDeploying(true);
                stayingDeployingTimer = 0;
            }
        }

        if (isStoppingDeploying() && !isBaby()) {
            stoppingDeployingTimer++;

            if (stoppingDeployingTimer >= 30) {
                this.setStopDeploying(false);
                stoppingDeployingTimer = 0;
            }
        }

        if (isCanFlee() && !isBaby()) {
            this.setRunning(true);
            if (!this.getNavigation().isInProgress()) {
                Vec3 randomPos = DefaultRandomPos.getPos(this, 80, 70);
                if (randomPos != null) {
                    this.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 6.0D);
                }
            }
        }

        if (this.getVariant() == PeacockVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    public void createEgg(PeacockEntity male) {
        if (male.isStayingDeploying() && male.isMale()) {
            male.setStayDeploying(false);
            male.setStopDeploying(true);
            male.stayingDeployingTimer = 0;
            femaleAccept = false;
            this.setAge(24000);
            male.setAge(24000);
            this.resetLove();
            male.resetLove();
            if (!this.level().isClientSide() && this.isFemale()) {;
                OWEgg egg = (OWEgg) OWBlocks.PEACOCK_EGG.get();
                BlockPos pos = this.blockPosition();
                this.level().setBlock(pos, egg.defaultBlockState(), 3);

                egg.setMaxHealthForPosition(pos, RANDOM(2) ? this.getMaxHealth() : male.getMaxHealth());
                egg.setMaxDamageForPosition(pos, RANDOM(2) ? this.getDamage() : male.getDamage());
                egg.setMaxSpeedForPosition(pos, RANDOM(2) ? this.getSpeed() : male.getSpeed());
                egg.setVariantForPosition(pos, RANDOM(2) ? this.getVariant().getId() : male.getVariant().getId());
                egg.setScaleForPosition(pos, (this.getScale() + male.getScale()) / 2);
            }
        }
    }

    private void resetFearEntities() {
        Timer fearCooldown = new Timer();
        this.setFearEntities(false);
        this.fearAnimationTimeout = 0;
        this.fearEntitiesTimer = 0;
        this.lastTarget = null;
        canFearEntities = false;
        this.setTarget(null);
        if (!this.isTame()) {
            setCanFlee(true);
        }

        fearCooldown.schedule(new TimerTask() {
            @Override
            public void run() {
                canFearEntities = true;
                setCanFlee(false);
            }
        }, 20000);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isFearEntities() && canFearEntities && damageSource.getEntity() != this.getOwner() && !isBaby()) {
            if (damageSource.getEntity() instanceof Player player && player.isCreative()) return super.hurt(damageSource, v);
            lastTarget = (LivingEntity) damageSource.getEntity();
            if (lastTarget != null && lastTarget.isAlive() && lastTarget instanceof LivingEntity livingEntity) {
                if (livingEntity.distanceTo(this) < 10) {
                    this.setStayDeploying(false);
                    this.setStartDeploying(false);
                    this.setFearEntities(true);
                }
            }
        }
        return super.hurt(damageSource, v);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY() - 0.5, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof PeacockEntity otherPeacock) {
            if (otherPeacock.isBaby()) return true;
            if (this.isTame()) return otherPeacock.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherPeacock.getOwnerUUID());
            else return !otherPeacock.isTame();
        }
        return super.isAlliedTo(entity);
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        createFoodHealingSystem(player, itemStack, preferRawMeat(), preferCookedMeat(), preferVegetables(), 1.5f);

        if (!isTame() && isFood(itemStack) && !isBaby()) {
            if (this.isMale()) {
                if (this.isStayingDeploying()) {
                    return super.mobInteract(player, hand);
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                return super.mobInteract(player, hand);
            }
        }

        if (itemStack.is(OWItems.SAVAGE_BERRIES.get()) && !this.isTame() && this.isBaby()) {
            foodGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            itemStack.shrink(1);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && foodGiven >= foodWanted) {
                    addTamingExperience(TAMING_EXPERIENCE / 2, player);
                    this.setTame(true, player);
                    this.setSleeping(false);
                    resetSleepBar();
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (itemStack.is(OWItems.SAVAGE_BERRIES.get())) return InteractionResult.PASS;
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));


            this.setVariant(choosePeacockVariant());
            this.setInitialVariant(this.getVariant());
        }

        if (this.isBaby()) {
            maxHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();
            maxMaturation = (int) (2000 * maxHealth + 10000 * this.getDamage());
            this.setHealth(1);
            foodWanted = (int) this.getMaxHealth();
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private PeacockVariant choosePeacockVariant() {
        PeacockVariant variant;
        Holder<Biome> biome = this.level().getBiome(this.blockPosition());
        int chance = this.random.nextInt(100);
        if (biome.is(Biomes.FLOWER_FOREST)) {
            if (chance <= 50) variant = PeacockVariant.RED;
            else variant = PeacockVariant.BLUE;
        } else if (biome.is(Biomes.MEADOW)) {
            if (chance <= 50) variant = PeacockVariant.GREEN;
            else variant = PeacockVariant.DEFAULT;
        } else variant = PeacockVariant.DEFAULT;
        if (RANDOM(50)) {
            variant = PeacockVariant.ALBINO;
        }
        return variant;
    }

    private void setupAnimationState() {
        createAttackAnimation(11, true);
        createIdleAnimation(54, true);
        createSitAnimation(80, true);

        if (this.isFearEntities()) {
            if (this.fearAnimationTimeout <= 0) {
                this.fearAnimationTimeout = 43;
                this.fearAnimationState.start(this.tickCount);
            } else --this.fearAnimationTimeout;
        }
        if (!this.isFearEntities()) {
            fearAnimationState.stop();
        }

        if (this.isDeploying()) {
            if (this.deployingAnimationTimeout <= 0) {
                this.deployingAnimationTimeout = 40;
                this.deployingAnimationState.start(this.tickCount);
            } else --this.deployingAnimationTimeout;
        }
        if (!this.isDeploying()) {
            deployingAnimationState.stop();
        }

        if (this.isStayingDeploying()) {
            if (this.stayingDeployingAnimationTimeout <= 0) {
                this.stayingDeployingAnimationTimeout = 80;
                this.stayingDeployingAnimationState.start(this.tickCount);
            } else --this.stayingDeployingAnimationTimeout;
        }
        if (!this.isStayingDeploying()) {
            stayingDeployingAnimationState.stop();
        }

        if (this.isStoppingDeploying()) {
            if (this.stoppingDeployingAnimationTimeout <= 0) {
                this.stoppingDeployingAnimationTimeout = 30;
                this.stoppingDeployingAnimationState.start(this.tickCount);
            } else --this.stoppingDeployingAnimationTimeout;
        }
        if (!this.isStoppingDeploying()) {
            stoppingDeployingAnimationState.stop();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(FEAR_ENTITIES, false);
        builder.define(CAN_FLEE, false);
        builder.define(STAY_DEPLOYING, false);
        builder.define(START_DEPLOYING, false);
        builder.define(STOP_DEPLOYING, false);
        builder.define(IS_MAGE_SKIN, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putBoolean("isDeploying", this.isDeploying());
        tag.putBoolean("isCanFlee", this.isCanFlee());
        tag.putBoolean("isMage", this.isMage());

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(FEAR_ENTITIES, tag.getBoolean("isFearEntities"));
        this.entityData.set(CAN_FLEE, tag.getBoolean("isCanFlee"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_MAGE_SKIN, tag.getBoolean("isMage"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
    }
}
