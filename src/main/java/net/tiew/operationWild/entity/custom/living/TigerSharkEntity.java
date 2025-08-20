package net.tiew.operationWild.entity.custom.living;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.entity.OWTameImplementation;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.AI.*;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;
import net.tiew.operationWild.entity.variants.TigerSharkVariant;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.TigerSharkDatasSendToClient;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWTags;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;

public class TigerSharkEntity extends OWWaterEntity implements FoodsPreference, OWEntityUtils, OWTameImplementation {

    public static final double TAMING_EXPERIENCE = 115.0;

    public int foodGiven = 0;
    public int foodWanted;

    public boolean canGoDown = false;
    private int goDownTimer = 0;
    public boolean canGoUp = false;
    private int goUpTimer = 0;

    public boolean isSmellingBlood = false;
    public boolean wantsToApproachPlayer = false;

    public final AnimationState sleepAnimationState = new AnimationState();
    public int sleepAnimationTimeout = 0;

    private int currentDirectionCooldown = 200 + this.random.nextInt(201);
    private int currentGoDownDuration = 50 + this.random.nextInt(41);
    private int currentGoUpDuration = 50 + this.random.nextInt(41);
    private float currentDownIntensity = 5 + this.random.nextFloat() * 35;
    private float currentUpIntensity = 5 + this.random.nextFloat() * 35;
    private float xRotAmplifier = 1 + this.random.nextFloat() * 10;

    public float accelerationLevel = 0.0f;
    public float rightAccelerationLevel = 0.0f;
    public float leftAccelerationLevel = 0.0f;
    public float upAccelerationLevel = 0.0f;
    public float backwardAccelerationLevel = 0.0f;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(TigerSharkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GO_HIT_TARGET = SynchedEntityData.defineId(TigerSharkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SHAKING_PREY = SynchedEntityData.defineId(TigerSharkEntity.class, EntityDataSerializers.BOOLEAN);

    public TigerSharkVariant getVariant() { return TigerSharkVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(TigerSharkVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public TigerSharkVariant getInitialVariant() { return TigerSharkVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(TigerSharkVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public TigerSharkEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x565047;
    }

    @Override
    public float getEntityScale() {
        return 9f;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.TIGER_SHARK_SADDLE.get();
    }

    @Override
    public List<Class<?>> getEntityType() {
        return ASSASSIN_ENTITIES;
    }

    @Override
    public String getTamingAdvancement() {
        return "";
    }

    @Override
    public float getMaxVitalEnergy() {
        return 215 * (1 + ((float) this.getLevel() / 100));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.75f;
    }

    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new SharkSwimmingGoal(this, 1.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new FollowBoatGoal(this));
        this.goalSelector.addGoal(1, new OWPanicGoal(this, this.getSpeed() * 20f, 3, 50));

        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Squid.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, SeaBugEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Turtle.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this).setAlertOthers(new Class[0])));

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 35D).add(Attributes.MOVEMENT_SPEED, 0.19D).add(Attributes.FOLLOW_RANGE, 45D).add(Attributes.ATTACK_DAMAGE, 7D).add(Attributes.ATTACK_KNOCKBACK, 0.7f).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource p_21239_) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
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

        if (skinIndex == 1) setVariant(TigerSharkVariant.SKIN_GOLD);
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
        List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20));
        setTamingPercentage(this.foodGiven, this.foodWanted);
        createTameAttackSystem(15, 3, OWSounds.TIGER_SHARK_CRUSH_MOUTH.get(), 3.0, 3.5, 1.5, false);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                LivingEntity target = this.getTarget();
                int targetId = target != null ? target.getId() : -1;

                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new TigerSharkDatasSendToClient(this.getId(), targetId, isSmellingBlood, this.foodGiven, this.foodWanted, this.isSmellingBlood), player);
                }
            }
        }

        for (LivingEntity livingEntity : livingEntities) {
            if (livingEntity instanceof Player player && player.isCreative() && !isSleeping() && !isTame() && !isSitting()) continue;
            if (livingEntity.hasEffect(OWEffects.BLEEDING_EFFECT.getDelegate()) && !isSleeping() && !isTame() && !isSitting()) {
                this.isSmellingBlood = true;
                this.setTarget(livingEntity);
                break;
            } else this.isSmellingBlood = false;
        }

        if (this.getVariant() == TigerSharkVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }

        if (isShakingPrey()) {
            Vec3 forwardDirection = this.getLookAngle().normalize();
            if (this.getTarget() != null) {
                Vec3 targetPos = new Vec3(this.getX() + forwardDirection.x * 1, this.getY() - 0.5, this.getZ() + forwardDirection.z * 1);

                BlockPos blockPos = new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z);
                if ((this.level().getBlockState(blockPos).is(Blocks.WATER) && this.level().getBlockState(blockPos.above()).is(Blocks.WATER)) ||
                        this.level().getBlockState(blockPos).is(Blocks.AIR) && this.level().getBlockState(blockPos.above()).is(Blocks.AIR)) {
                    this.getTarget().teleportTo(targetPos.x, targetPos.y, targetPos.z);
                } else {
                    this.setShakingPrey(false);
                }

                this.doHurtTarget(this.getTarget());

                if (!this.getTarget().isAlive()) {
                    this.setShakingPrey(false);
                }
            }
        }

        if (!this.isGoingToHitTarget() && !this.level().isClientSide() && !isSleeping() && !isTame() && !isSitting()) {
            if (tickCount % 1000 == 0) {
                wantsToApproachPlayer = true;
            }
        }

        if (wantsToApproachPlayer && !this.isGoingToHitTarget() && !this.level().isClientSide() && !isSleeping() && !isTame() && !isSitting()) {
            for (LivingEntity livingEntity : livingEntities) {
                if (livingEntity instanceof Player player) {
                    double dist = this.distanceTo(player);
                    if (!player.isCreative() && !ClientEvents.isInSubmarine(player)) {
                        this.setLookAt(player.getX(), player.getY(), player.getZ());
                        Vec3 lookDirection = this.getLookAngle().scale(1.35);
                        float speed = 0.06f;
                        if (this.isInWater()) this.setDeltaMovement(lookDirection.scale(speed));
                    }
                    if (dist < 5) {
                        this.wantsToApproachPlayer = false;
                    }
                    break;
                }
            }
        }

        if (!this.isGoingToHitTarget() && !this.level().isClientSide() && !isSleeping() && !isSaddled() && !isSitting()) {
            int depth = (int) (this.level().getSeaLevel() - this.getY());

            if (tickCount % currentDirectionCooldown == 0) {
                if (!canGoUp && !canGoDown) {
                    if (depth <= 10) {
                        canGoDown = true;
                        currentGoDownDuration = 80 + this.random.nextInt(41);
                        currentDownIntensity = 50 + this.random.nextFloat() * 20;
                    } else {
                        canGoUp = true;
                        currentGoUpDuration = 80 + this.random.nextInt(41);
                        currentUpIntensity = 50 + this.random.nextFloat() * 20;
                    }
                    xRotAmplifier = 1 + this.random.nextFloat() * 10;
                }
                currentDirectionCooldown = 300 + this.random.nextInt(201);
            }

            if (canGoDown) {
                goDownTimer++;

                float xRot = (currentDownIntensity * (float)Math.pow(Math.sin((goDownTimer / (float)currentGoDownDuration) * Math.PI), 0.6)) / xRotAmplifier;
                this.setXRot(xRot);
                Vec3 lookDirection = this.getLookAngle().scale(2.5);
                this.setDeltaMovement(lookDirection.scale((0.06f * (isShakingPrey() ? 1.5f : 1)) * (isSmellingBlood ? 1.35f : 1f)));

                if (goDownTimer >= currentGoDownDuration) {
                    goDownTimer = 0;
                    canGoDown = false;
                }
            }

            if (canGoUp) {
                goUpTimer++;

                float xRot = (currentUpIntensity * (float)Math.pow(Math.sin((goUpTimer / (float)currentGoUpDuration) * Math.PI), 0.6)) / xRotAmplifier;
                this.setXRot(-xRot);
                Vec3 lookDirection = this.getLookAngle().scale(2.5);
                this.setDeltaMovement(lookDirection.scale((0.06f * (isShakingPrey() ? 1.5f : 1)) * (isSmellingBlood ? 1.35f : 1f)));

                if (goUpTimer >= currentGoUpDuration) {
                    goUpTimer = 0;
                    canGoUp = false;
                }
            }
        }

        if (this.getTarget() != null && this.getState() != 1 && isGoingToHitTarget() && !isSleeping() && !isTame() && !isSitting()) {
            canGoDown = false;
            canGoUp = false;
            goDownTimer = 0;
            goUpTimer = 0;
            LivingEntity target = this.getTarget();

            double deltaX = target.getX() - this.getX();
            double deltaY = target.getY() - this.getY();
            double deltaZ = target.getZ() - this.getZ();

            float targetYRot = (float) (Mth.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0f;
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            float targetXRot = (float) (-(Mth.atan2(deltaY, horizontalDistance) * (180.0 / Math.PI)));

            float maxTurnSpeed = 100f;

            float currentYRot = this.getYRot();
            float yRotDiff = Mth.wrapDegrees(targetYRot - currentYRot);
            yRotDiff = Mth.clamp(yRotDiff, -maxTurnSpeed, maxTurnSpeed);
            float newYRot = currentYRot + yRotDiff;

            float currentXRot = this.getXRot();
            float xRotDiff = targetXRot - currentXRot;
            xRotDiff = Mth.clamp(xRotDiff, -maxTurnSpeed, maxTurnSpeed);
            float newXRot = currentXRot + xRotDiff;

            this.setYRot(newYRot);
            this.setXRot(newXRot);

            Vec3 lookDirection = this.getLookAngle().scale(1.35);
            float speed = 0.25f * (isSmellingBlood ? 1.35f : 1f);
            if (this.isInWater()) this.setDeltaMovement(lookDirection.scale(speed));
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity source = damageSource.getEntity();
        if (source == null) {
            source = damageSource.getDirectEntity();
        }
        if (damageSource.getEntity() instanceof Player player && player.isCreative()) return super.hurt(damageSource, amount);

        if (source instanceof LivingEntity attacker && !this.isShakingPrey()) {
            this.goalSelector.getAvailableGoals().forEach(wrappedGoal -> {
                if (wrappedGoal.getGoal() instanceof SharkSwimmingGoal goal) {
                    goal.triggerAttack(attacker);
                }
            });
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        float pitch = (float) OWUtils.generateRandomInterval(0.8, 1.0);
        boolean canShake = this.isSmellingBlood && entity instanceof LivingEntity livingEntity && livingEntity.getMaxHealth() <= 20;
        boolean canApplyBleeding = this.random.nextInt(10) == 0 && !isSmellingBlood;

        if (isShakingPrey()) {
            float originalDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage / 2.5f);
            boolean result = super.doHurtTarget(entity);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage);
            return result;
        }

        if (canShake && !isShakingPrey()) {
            this.setShakingPrey(true);
            canGoDown = true;
            currentGoDownDuration = 120;
            currentDownIntensity = 60;
        }

        if (canApplyBleeding && living != null && !isShakingPrey()) {
            living.addEffect(new MobEffectInstance(OWEffects.BLEEDING_EFFECT.getDelegate(), 300 + this.random.nextInt(120), 0));
        }

        this.playSound(OWSounds.TIGER_SHARK_CRUSH_MOUTH.get(), 1.0f, pitch);
        return super.doHurtTarget(entity);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        float basePosition = 0.2f * (getScale() * 1.3f);
        Vec3 forwardDirection = this.getLookAngle().normalize();
        moveFunction.accept(entity, this.getX() + forwardDirection.x * 0.5f, this.getY() + basePosition, this.getZ() + forwardDirection.z * 0.5f);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        setShakingPrey(false);
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof TigerSharkEntity otherPeacock) {
            if (this.isTame()) return otherPeacock.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherPeacock.getOwnerUUID());
            else return !otherPeacock.isTame();
        }
        return super.isAlliedTo(entity);
    }

    public void setGoingToHitTarget(boolean isGoingToHitTarget) { this.entityData.set(GO_HIT_TARGET, isGoingToHitTarget);}
    public boolean isGoingToHitTarget() { return this.entityData.get(GO_HIT_TARGET);}
    public void setShakingPrey(boolean isShakingPrey) {this.entityData.set(IS_SHAKING_PREY, isShakingPrey);}
    public boolean isShakingPrey() { return this.entityData.get(IS_SHAKING_PREY);}

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

    private boolean isValidFood(ItemStack stack) { return stack.is(OWTags.Items.TIGER_SHARK_TAMING_FOOD);}

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        createFoodHealingSystem(player, itemStack, preferRawMeat(), preferCookedMeat(), preferVegetables(), 1.5f);

        if (!isTame() && isGoingToHitTarget() && this.getTarget() != null) {
            if (this.getTarget() == player && this.distanceTo(player) <= 6) {
                if (player.getMainHandItem().is(Items.SALMON)) {
                    player.getMainHandItem().shrink(1);
                    this.playSound(SoundEvents.CAMEL_EAT);
                    this.goalSelector.getAvailableGoals().forEach(wrappedGoal -> {
                        if (wrappedGoal.getGoal() instanceof SharkSwimmingGoal goal) {
                            goal.stop();
                        }
                    });
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (isValidFood(itemStack) && !this.isTame() && (this.isSleeping() || this.isBaby())) {
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

        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));


            this.setVariant(chooseTigerSharkVariant());
            this.setInitialVariant(this.getVariant());

            foodWanted = (int) this.getMaxHealth() / 2;
        }

        if (this.isBaby()) {
            maxHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();
            maxMaturation = (int) (2000 * maxHealth + 10000 * this.getDamage());
            this.setHealth(1);
            foodWanted = (int) this.getMaxHealth();
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private TigerSharkVariant chooseTigerSharkVariant() {
        TigerSharkVariant variant;
        int random = this.random.nextInt(3);
        switch (random) {
            case 0: variant = TigerSharkVariant.DEFAULT; break;
            case 1: variant = TigerSharkVariant.BLUE; break;
            case 2: variant = TigerSharkVariant.GREY; break;
            default: variant = TigerSharkVariant.DEFAULT; break;
        }
        return variant;
    }

    private void setupAnimationState() {
        createAttackAnimation(15, true);
        createIdleAnimation(54, true);
        createSitAnimation(57, true);

        if (this.isSleeping()) {
            if (this.sleepAnimationTimeout <= 0) {
                this.sleepAnimationTimeout = 57;
                this.sleepAnimationState.start(this.tickCount);
            } else --this.sleepAnimationTimeout;
        }

        if (!this.isSleeping()) {
            this.sleepAnimationTimeout = 0;
            this.sleepAnimationState.stop();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(GO_HIT_TARGET, false);
        builder.define(IS_SHAKING_PREY, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isGoingToHitTarget", this.isGoingToHitTarget());
        tag.putBoolean("isShakingPrey", this.isShakingPrey());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(GO_HIT_TARGET, tag.getBoolean("isGoingToHitTarget"));
        this.entityData.set(IS_SHAKING_PREY, tag.getBoolean("isShakingPrey"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
    }
}
