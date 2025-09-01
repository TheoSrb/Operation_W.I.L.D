package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidType;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.OWTameImplementation;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.BoaVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.BoaFoodsSendToClient;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.tiew.operationWild.core.OWUtils.*;

public class BoaEntity extends OWEntity implements FoodsPreference, OWEntityUtils, OWTameImplementation {

    public static final double TAMING_EXPERIENCE = 80.0;

    public String[] quests = {};
    private boolean isSprinting;

    public final AnimationState tongAnimationState = new AnimationState();
    public final AnimationState nipsAnimationState = new AnimationState();
    public int tongAnimationTimeout = 0;
    public int nipsAnimationTimeout = 0;
    private int nipsTimer = 1;
    private boolean canNips = true;
    private int tongTime;
    private boolean canGoEatItem = true;
    public int foodGiven = 0;
    public int foodWanted = 5;
    public int numberOfError = 0;
    public int venomCooldown = 801;
    public boolean canVenom = false;
    private int numberOfToothDropped = 0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SOUND_TIME = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> NIPS = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TONG = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DIGESTS = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_DECREASE_DIGESTS_TIME = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DIGESTS_TIME = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NUMBER_OF_BODY = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DIGESTS_TIME_MAX = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_VIKING = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CYBORG = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_MINOR = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SKIN_VIKING_IS_BUYING = SynchedEntityData.defineId(BoaEntity.class, EntityDataSerializers.BOOLEAN);


    public BoaVariant getVariant() { return BoaVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(BoaVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public BoaVariant getInitialVariant() { return BoaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(BoaVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public BoaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x838549;
    }

    @Override
    public float getEntityScale() {
        return 5.5f;
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
        return 4f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 1f;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.BOA_SADDLE.get();
    }

    @Override
    public List<Class<?>> getEntityType() {
        return MARAUDER_ENTITIES;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.BOA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 225 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.5f * (1 + ((float) this.getLevel() / 50));
    }

    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 9f,20, 3,true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, PeacockEntity.class, true));
        this.goalSelector.addGoal(2, new OWBreedGoal(this, 1.0D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 25D).add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.FOLLOW_RANGE, 20D).add(Attributes.ATTACK_DAMAGE, 2D).add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) { return OWEntityRegistry.BOA.get().create(serverLevel);}

    @Override
    public boolean isFood(ItemStack itemStack) {return itemStack.getItem() == OWBlocks.PEACOCK_EGG.get().asItem(); }

    protected float getSoundVolume() { return 1.75f;}

    protected SoundEvent getAmbientSound() { return RANDOM(2) ? RANDOM(2) ? OWSounds.BOA_IDLE_1.get() : RANDOM(2) ? OWSounds.BOA_IDLE_2.get() : RANDOM(2) ? OWSounds.BOA_IDLE_3.get() : OWSounds.BOA_IDLE_4.get() : null; }

    protected SoundEvent getHurtSound(DamageSource damageSource) { return OWSounds.BOA_HURT.get();}

    protected SoundEvent getDeathSound() { return OWSounds.BOA_IDLE_4.get();}

    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {}

    public boolean isSprinting() { return this.isSprinting;}

    public void setSprinting(boolean sprinting) { this.isSprinting = sprinting;}

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            case 2 -> this.entityData.set(SKIN_VIKING_IS_BUYING, true);
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }

    public void setSoundTime(int soundTime) { this.entityData.set(SOUND_TIME, soundTime);}
    public int getSoundTime() { return this.entityData.get(SOUND_TIME);}

    public void setNips(boolean isNips) { this.entityData.set(NIPS, isNips);}
    public boolean isNips() { return this.entityData.get(NIPS);}

    public void setDigests(boolean isDigests) { this.entityData.set(DIGESTS, isDigests);}
    public boolean isDigests() { return this.entityData.get(DIGESTS);}

    public void setCanDecreaseDigestsTime(boolean isDigests) { this.entityData.set(CAN_DECREASE_DIGESTS_TIME, isDigests);}
    public boolean isCanDecreaseDigestTime() { return this.entityData.get(CAN_DECREASE_DIGESTS_TIME);}

    public void setDigestsTime(int digestTime) { this.entityData.set(DIGESTS_TIME, digestTime);}
    public int getDigestsTime() { return this.entityData.get(DIGESTS_TIME);}

    public void setDigestsTimeMax(int digestTime) { this.entityData.set(DIGESTS_TIME_MAX, digestTime);}
    public int getDigestsTimeMax() { return this.entityData.get(DIGESTS_TIME_MAX);}

    public void setNumberOfBody(int numberOfBody) { this.entityData.set(NUMBER_OF_BODY, numberOfBody);}
    public int getNumberOfBody() { return this.entityData.get(NUMBER_OF_BODY);}

    public void startDigests(int time) {
        setDigests(true);
        setDigestsTimeMax(time);
    }

    public void setTong(boolean isTong) { this.entityData.set(TONG, isTong);}
    public boolean isTong() { return this.entityData.get(TONG);}

    public void setViking(boolean isViking) { this.entityData.set(IS_VIKING, isViking);}
    public boolean isViking() { return this.entityData.get(IS_VIKING);}

    public void setCyborg(boolean isCyborg) { this.entityData.set(IS_CYBORG, isCyborg);}
    public boolean isCyborg() { return this.entityData.get(IS_CYBORG);}

    public void setMinor(boolean isMinor) { this.entityData.set(IS_MINOR, isMinor);}
    public boolean isMinor() { return this.entityData.get(IS_MINOR);}

    public boolean skinVikingIsAlreadyBuying() { return this.entityData.get(SKIN_VIKING_IS_BUYING);}

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        //if (this.onGround() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();
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
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || isBaby()) {
            super.setTarget(null);
            return;
        }

        if (target != null) {
            Entity vehicle = target.getVehicle();
            if (vehicle instanceof LivingEntity) {
                target = (LivingEntity) vehicle;
            }
        }

        super.setTarget(target);
    }


    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());
        this.setViking(false);
        this.setCyborg(false);
        this.setMinor(false);

        if (skinIndex == 1) setVariant(BoaVariant.SKIN_GOLD);
        else if (skinIndex == 2) setViking(true);
        else if (skinIndex == 3) setMinor(true);
        else if (skinIndex == 4) setVariant(BoaVariant.SKIN_LEVIATHAN);
        else if (skinIndex == 5) setVariant(BoaVariant.SKIN_PLUSH);
        else if (skinIndex == 6) setCyborg(true);

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


        createCombo(10, 6, OWSounds.BOA_HITTING.get(), 3.0, 3.5, 1.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 0 : -1);


        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new BoaFoodsSendToClient(this.getId(), this.foodGiven, this.foodWanted, this.numberOfError, this.venomCooldown, this.canVenom), player);
                }
            }
        }

        if (venomCooldown <= 800) {
            if (venomCooldown > 0) venomCooldown--;
            else venomCooldown = 801;
        }

        if (this.isDigests()) {
            if (getNumberOfBody() < 9) {
                if (isCanDecreaseDigestTime()) {
                    this.setDigestsTime(getDigestsTime() - 1);
                    if (getDigestsTime() <= 0) {
                        setCanDecreaseDigestsTime(false);
                        setNumberOfBody(getNumberOfBody() + 1);
                    }
                } else {
                    this.setDigestsTime(getDigestsTime() + 1);
                    if (getDigestsTime() >= getDigestsTimeMax()) {
                        setCanDecreaseDigestsTime(true);
                    }
                }
            } else {
                setCanDecreaseDigestsTime(false);
                setDigestsTime(0);
                setNumberOfBody(0);
                setDigests(false);
            }
        }


        if (!isTame() && !isBaby()) {
            List<ItemEntity> nearbyItems = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(10)); // search radius = 10 blocks
            for (ItemEntity itemEntity : nearbyItems) {
                if (itemEntity.getItem().getItem() == OWItems.RAW_TIGER.get()) {
                    goToFood(itemEntity, 0.9f);
                    break;
                }
            }
        }


        if (this.getTarget() != null && !this.isTame()) {
            if (tickCount % 100 == 0) {
                if (foodGiven > 0) {
                    foodGiven--;
                    if (numberOfError < 5) numberOfError++;
                }
            }
        }

        if (this.tickCount % 150 == 0) this.setTong(true);

        if (isTong()) {
            tongTime++;
            if (tongTime >= 16) {
                this.setTong(false);
                this.tongTime = 0;
            }
        }

        if (this.isNips() && !isBaby()) {
            LivingEntity trappedEntity = (LivingEntity) this.getRootVehicle();
            float damages = nipsTimer / 70f;
            this.nipsTimer++;

            this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(trappedEntity.getX(), trappedEntity.getY(), trappedEntity.getZ()));

            if (trappedEntity instanceof Player player) {
                if (player.isCreative()) stopNips();
            }

            if (trappedEntity.isInWater() || trappedEntity.isUnderWater()) {
                stopNips();
            }

            if (trappedEntity != null) {
                if (trappedEntity.isAlive()) {
                    trappedEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5, 2, false, false, false));
                    if (trappedEntity.onGround()) {
                        trappedEntity.setDeltaMovement(0, 0, 0);
                        trappedEntity.hasImpulse = false;
                    }
                    if (tickCount % 10 == 0) {
                        trappedEntity.hurt(damageSource, damages);
                    }
                    if (nipsTimer % 200 == 0) {
                        stopNips();
                    }
                } else stopNips();
            } else stopNips();

            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 1, false, false, false));
        }

        if (this.getTarget() != null && !this.isRunning()) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ()));
        }

        if (this.getVariant() == BoaVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }

        if (this.level().isClientSide()) setupAnimationState();

    }

    public void stopNips() {
        Entity vehicle = this.getVehicle();
        this.stopRiding();
        this.refreshDimensions();
        this.setNips(false);
        this.nipsTimer = 1;

        if (vehicle instanceof Player && !this.level().isClientSide()) {
            ServerLevel level = (ServerLevel) this.level();

            level.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));

            ClientboundSetPassengersPacket passengersPacket = new ClientboundSetPassengersPacket(vehicle);
            level.getChunkSource().broadcast(vehicle, passengersPacket);

            if (vehicle instanceof ServerPlayer player) {
                player.connection.send(passengersPacket);
            }
        }

        canNips = false;

        Timer canNipsCooldown = new Timer();
        canNipsCooldown.schedule(new TimerTask() {
            @Override
            public void run() {
                canNips = true;
            }
        }, 20000);
    }

    private void goToFood(ItemEntity food, float speed) {
        if (!canGoEatItem || this.getTarget() != null || food == null || isBaby()) return;
        int distanceToFood = (int) this.distanceTo(food);
        Player player = (Player) food.getOwner();

        if (distanceToFood > 2 && food != null) this.getNavigation().moveTo(food.getX(), food.getY(), food.getZ(), speed);
        else {
            if (!this.level().isClientSide()) {
                this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(food.getX(), food.getY(), food.getZ()));
                this.getNavigation().stop();
                this.playSound(SoundEvents.CAMEL_EAT);
                createFoodHealingSystem(player, food.getItem(), preferRawMeat(), preferCookedMeat(), preferVegetables(), 1.5f);
                food.discard();
                foodGiven++;
                System.out.println(foodGiven + " / " + foodWanted);
                this.setAttacking(true);
                startDigests(20);

                if (foodGiven >= foodWanted) {
                    if (!EventHooks.onAnimalTame(this, player)) {
                        if (!this.level().isClientSide() && player != null)  {
                            addTamingExperience(TAMING_EXPERIENCE / 2, player);
                            this.setTame(true, player);
                            this.setSleeping(false);
                            resetSleepBar();
                        }
                    }
                }
                applyEatingItemCooldown(40);
                canGoEatItem = false;
            }
        }
    }

    private void applyEatingItemCooldown(int seconds) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                canGoEatItem = true;
            }
        }, seconds * 1000L);
    }


    private boolean targetCanBeGrabbed(LivingEntity target) {
        return target.getMaxHealth() <= 40 && !target.isUnderWater() && !target.isInWater() && target.getHealth() >= 10 && !(target instanceof BoaEntity);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (this.isNips() || isBaby()) return false;

        if (!this.isTame() && !this.isNips()) {
            if (numberOfToothDropped < 2) {
                if (RANDOM(15)) {
                    this.spawnAtLocation(OWItems.VENOMOUS_TOOTH.get());
                }
            }
        }


        LivingEntity rider = this.getControllingPassenger();

        if (entity instanceof LivingEntity livingTarget) {
            if (canNips) {
                if (targetCanBeGrabbed(livingTarget) && rider == null && RANDOM(3)) {
                    boolean alreadyGrabbed = false;
                    for (Entity e : livingTarget.getPassengers()) {
                        if (e instanceof BoaEntity boa && boa.isNips()) {
                            alreadyGrabbed = true;
                            break;
                        }
                    }

                    if (!alreadyGrabbed) {
                        if (livingTarget instanceof TigerEntity tiger && tiger.hasSomethingInTheirArms()) {
                            return false;
                        }
                        Holder<Enchantment> slidingHolder = livingTarget.level().registryAccess()
                                .registryOrThrow(Registries.ENCHANTMENT)
                                .getHolderOrThrow(OWEnchantments.SLIDING);

                        int slidingLevel = EnchantmentHelper.getEnchantmentLevel(slidingHolder, livingTarget);

                        if (slidingLevel > 0) {
                            float chanceToAvoid = 0.25f * slidingLevel;
                            if (livingTarget.getRandom().nextFloat() < (1 - chanceToAvoid)) {

                                if (entity instanceof Player) {
                                    if (!this.level().isClientSide()) {
                                        this.teleportTo(livingTarget.getX(), livingTarget.getY(), livingTarget.getZ());
                                        this.unRide();
                                        this.startRiding(livingTarget, true);

                                        ServerLevel serverLevel = (ServerLevel) this.level();
                                        serverLevel.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
                                        serverLevel.getChunkSource().broadcast(this, new ClientboundSetPassengersPacket(livingTarget));

                                        this.setNips(true);
                                    }
                                } else {
                                    this.startRiding(livingTarget);
                                    this.setNips(true);
                                }
                            }
                        } else {
                            if (entity instanceof Player) {
                                if (!this.level().isClientSide()) {
                                    this.teleportTo(livingTarget.getX(), livingTarget.getY(), livingTarget.getZ());
                                    this.unRide();
                                    this.startRiding(livingTarget, true);

                                    ServerLevel serverLevel = (ServerLevel) this.level();
                                    serverLevel.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
                                    serverLevel.getChunkSource().broadcast(this, new ClientboundSetPassengersPacket(livingTarget));

                                    this.setNips(true);
                                }
                            } else {
                                this.startRiding(livingTarget);
                                this.setNips(true);
                            }
                        }
                    }
                }
            }

            if (!livingTarget.hasEffect(MobEffects.POISON) && !this.isNips()) {
                if (RANDOM(3)) livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, (int) generateRandomInterval(176, 352), 0));
            }
            if (!this.isNips() && !(livingTarget instanceof BoaEntity)) {
                if (RANDOM(5)) livingTarget.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) generateRandomInterval(3600, 6000), 0));
            }
        }

        double pitch = OWUtils.generateRandomInterval(0.9, 1.1);
        this.playSound(OWSounds.BOA_HITTING.get(), 1.25f, (float) pitch);

        return super.doHurtTarget(entity);
    }


    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.getHealth() <= 5 || !this.isAlive()) stopNips();
        return super.hurt(damageSource, amount);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        startDigests((int) entity.getMaxHealth() * 2);
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof BoaEntity otherBoa) {
            if (otherBoa.isBaby()) return true;
            if (this.isTame()) return otherBoa.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherBoa.getOwnerUUID());
            else return !otherBoa.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean preferRawMeat() { return true;}

    @Override
    public boolean preferCookedMeat() { return false;}

    @Override
    public boolean preferVegetables() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        createFoodHealingSystem(player, itemStack, preferRawMeat(), preferCookedMeat(), preferVegetables(), 1.5f);
        if (!isNips() && isSleeping() && !isNapping() && !isPreparingNapping() && isFood(itemStack) && !isBaby() && isTame()) {
            return super.mobInteract(player, hand);
        }
        if (isBaby() && !isTame() && itemStack.is(OWItems.RAW_TIGER.get())) {
            foodGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            if (!player.isCreative()) itemStack.shrink(1);

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

            this.setVariant(chooseBoaVariant());
            this.setInitialVariant(this.getVariant());

            foodWanted = (int) OWUtils.generateRandomInterval(5, 12);
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private BoaVariant chooseBoaVariant() {
        Holder<Biome> biome = this.level().getBiome(this.blockPosition());
        if (biome.is(Biomes.SWAMP)) {
            int randomTexture = this.random.nextInt(3) + 1;
            switch (randomTexture) {
                case 1 -> { return BoaVariant.DEFAULT; }
                case 2 -> { return BoaVariant.BROWN; }
                case 3 -> { return BoaVariant.DARK_GREEN; }
                default -> { return BoaVariant.DEFAULT; }
            }
        } else if (biome.is(Biomes.MANGROVE_SWAMP)) {
            int randomTexture = this.random.nextInt(3) + 1;
            switch (randomTexture) {
                case 1 -> { return BoaVariant.ALBINO; }
                case 2 -> { return BoaVariant.CORAL; }
                case 3 -> { return BoaVariant.LIME; }
                default -> { return BoaVariant.DEFAULT; }
            }
        } else if (biome.is(Biomes.JUNGLE)) {
            int randomTexture = this.random.nextInt(3) + 1;
            switch (randomTexture) {
                case 1 -> { return BoaVariant.LIME; }
                case 2 -> { return BoaVariant.YELLOW; }
                case 3 -> { return BoaVariant.DEFAULT; }
                default -> { return BoaVariant.DEFAULT; }
            }
        } else {
            int randomTexture = this.random.nextInt(7) + 1;
            switch (randomTexture) {
                case 1 -> { return BoaVariant.DEFAULT; }
                case 2 -> { return BoaVariant.DARK_GREEN; }
                case 3 -> { return BoaVariant.YELLOW; }
                case 4 -> { return BoaVariant.ALBINO; }
                case 5 -> { return BoaVariant.CORAL; }
                case 6 -> { return BoaVariant.BROWN; }
                case 7 -> { return BoaVariant.LIME; }
                default -> { return BoaVariant.DEFAULT; }
            }
        }
    }

    private void setupAnimationState() {
        createAttackAnimation(11, !isNips());
        createSitAnimation(80, true);

        if (this.isTong()) {
            if (this.tongAnimationTimeout <= 0) {
                this.tongAnimationTimeout = 16;
                this.tongAnimationState.start(this.tickCount);
            } else {
                --this.tongAnimationTimeout;
            }
        }

        if (this.isNips()) {
            if (this.nipsAnimationTimeout <= 0) {
                this.nipsAnimationTimeout = 133;
                this.nipsAnimationState.start(this.tickCount);
            } else {
                --this.nipsAnimationTimeout;
            }
        }

    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(SOUND_TIME, 345);
        builder.define(NIPS, false);
        builder.define(TONG, false);
        builder.define(IS_VIKING, false);
        builder.define(IS_CYBORG, false);
        builder.define(IS_MINOR, false);
        builder.define(SKIN_VIKING_IS_BUYING, false);
        builder.define(DIGESTS, false);
        builder.define(CAN_DECREASE_DIGESTS_TIME, false);
        builder.define(DIGESTS_TIME, 0);
        builder.define(DIGESTS_TIME_MAX, 0);
        builder.define(NUMBER_OF_BODY, 0);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isNips", this.isNips());
        tag.putBoolean("isDigests", this.isDigests());
        tag.putBoolean("isCanDecreaseDigestTime", this.isCanDecreaseDigestTime());
        tag.putInt("getDigestsTime", this.getDigestsTime());
        tag.putInt("getDigestsTimeMax", this.getDigestsTimeMax());
        tag.putInt("getNumberOfBody", this.getNumberOfBody());
        tag.putBoolean("isViking", this.isViking());
        tag.putBoolean("isCyborg", this.isCyborg());
        tag.putBoolean("isMinor", this.isMinor());
        tag.putBoolean("skinVikingIsAlreadyBuying", this.skinVikingIsAlreadyBuying());

        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("nipsTimer", this.nipsTimer);
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("numberOfError", this.numberOfError);
        tag.putInt("numberOfToothDropped", this.numberOfToothDropped);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(NIPS, tag.getBoolean("isNips"));
        this.entityData.set(DIGESTS, tag.getBoolean("isDigests"));
        this.entityData.set(CAN_DECREASE_DIGESTS_TIME, tag.getBoolean("isCanDecreaseDigestTime"));
        this.entityData.set(DIGESTS_TIME, tag.getInt("getDigestsTime"));
        this.entityData.set(DIGESTS_TIME_MAX, tag.getInt("getDigestsTimeMax"));
        this.entityData.set(NUMBER_OF_BODY, tag.getInt("getNumberOfBody"));
        this.entityData.set(IS_VIKING, tag.getBoolean("isViking"));
        this.entityData.set(IS_CYBORG, tag.getBoolean("isCyborg"));
        this.entityData.set(IS_MINOR, tag.getBoolean("isMinor"));
        this.entityData.set(SKIN_VIKING_IS_BUYING, tag.getBoolean("skinVikingIsAlreadyBuying"));

        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.nipsTimer = tag.getInt("nipsTimer");
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.numberOfError = tag.getInt("numberOfError");
        this.numberOfToothDropped = tag.getInt("numberOfToothDropped");
    }
}
