package net.tiew.operationWild.entity.animals.aquatic;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.MarkedMudBlock;
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
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.behavior.CrocodileBehaviorHandler;
import net.tiew.operationWild.entity.behavior.KodiakBehaviorHandler;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileAttackGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileChargingMouthGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileGoToWaterWithFoodGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileNapGoal;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.kodiak.*;
import net.tiew.operationWild.entity.taming.TamingCrocodile;
import net.tiew.operationWild.entity.taming.TamingKodiak;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class CrocodileEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 205.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_MOUTH = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CHARGING_MOUTH_TIMER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_GRABBING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_DEATH_ROLLING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_GRAB_UNDERWATER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING_GRAB = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DEATH_ROLLING_PROGRESS = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);

    public CrocodileBehaviorHandler crocodileBehaviorHandler;
    public TamingCrocodile crocodileTaming;

    public final AnimationState idleWaterAnimationState = new AnimationState();
    public final AnimationState growlsAnimationState = new AnimationState();
    public final AnimationState gruntAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState deathRollAnimationState = new AnimationState();

    public int idleWaterAnimationTimeout = 0;
    private int growlsAnimationStartTime = 0;
    private int gruntAnimationStartTime = 0;
    private int napAnimationStartTime = 0;
    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int deathRollAnimationTimeout = 0;

    private int attackingGrabTimer = 0;
    public int attackingGrabCooldown = 0;
    private int grabUnderwaterCooldown = 0;

    public boolean canGrabOnLand = false;

    private static final int GROWLS_DURATION = 75;
    private static final int GRUNT_DURATION = 55;

    private int growlsInterval = (int) OWUtils.generateRandomInterval(400, 1200);
    private int gruntInterval = (int) OWUtils.generateRandomInterval(300, 500);

    public CrocodileEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initCrocodileBehaviorAndTaming();
    }

    private void initCrocodileBehaviorAndTaming() {
        this.crocodileBehaviorHandler = new CrocodileBehaviorHandler(this);
        this.crocodileTaming = new TamingCrocodile(this, crocodileBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 37.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ARMOR, 0.2D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initCrocodileBehaviorAndTaming(); // Create the AI before the goals, otherwise, null error

        this.goalSelector.addGoal(0, new CrocodileGoToWaterWithFoodGoal(this));
        this.goalSelector.addGoal(0, new JumpOutOfTheWaterGoal(this));
        this.goalSelector.addGoal(0, new FollowBoatGoal(this));
        this.goalSelector.addGoal(1, new CrocodileAttackGoal(this, this.getSpeed() * 15f, 15, 4, false));
        this.goalSelector.addGoal(2, new CrocodileChargingMouthGoal(this));
        this.goalSelector.addGoal(3, new CrocodileNapGoal(this, 1.25f, 500, true));
        this.goalSelector.addGoal(4, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new OWRandomLookAroundGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof CrocodileEntity crocodile && !crocodile.isSleeping() && !crocodile.isNapping() && crocodile.getGrabbedTarget() == null) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_CHARGING_MOUTH, false);
        builder.define(CHARGING_MOUTH_TIMER, 0.0f);
        builder.define(IS_GRABBING, false);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(IS_DEATH_ROLLING, false);
        builder.define(CAN_GRAB_UNDERWATER, false);
        builder.define(IS_ATTACKING_GRAB, false);
        builder.define(DEATH_ROLLING_PROGRESS, 0);
        builder.define(GRAB_TIMEOUT, 0);
    }

    public static boolean checkCrocodileSpawnRules(EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState blockBelow = level.getBlockState(pos.below());
        Block blockType = blockBelow.getBlock();

        boolean validBlock = blockType == Blocks.MUD || blockType == Blocks.GRASS_BLOCK || blockType == Blocks.WATER;

        if (!validBlock) {
            return false;
        }

        boolean waterNearby = false;
        int searchRadius = 16;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    if (state.getBlock() == Blocks.WATER) {
                        waterNearby = true;
                        break;
                    }
                }
                if (waterNearby) break;
            }
            if (waterNearby) break;
        }

        return waterNearby;
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x727957;
    }

    @Override
    public float getTheoreticalScale() {
        return 9;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.ASSASSIN;
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
        return List.of(Boat.class, Player.class, Animal.class, Monster.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 3f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.75f;
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
        return OWItems.CROCODILE_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.CROCODILE_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 300 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1f * (1 + ((float) this.getLevel() / 50));
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
        return 300 * 10;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir + 10;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 30 : 5;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.4F;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.CROCODILE.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CROCODILE_FOOD);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        return RANDOM(3) ? RANDOM(2) ? OWSounds.CROCODILE_IDLE_2.get() : OWSounds.CROCODILE_IDLE_4.get() : null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.CROCODILE_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.CROCODILE_HURT.get();
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!isRunning() && !isInWater()) super.playStepSound(blockPos, blockState);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater() || this.onGround()) {
            BlockPos currentPos = this.blockPosition();
            crocodileBehaviorHandler.trampleLilyPads(currentPos);
            crocodileBehaviorHandler.trampleLilyPads(currentPos.above());
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isChargingMouth()) {
            Vec3 movement = this.getDeltaMovement();
            float multiplier = this.isVehicle() ? 0.45f : 0.15f;
            this.setDeltaMovement(movement.x * multiplier, movement.y, movement.z * multiplier);
        }

        super.travel(vec3);

        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();
    }

    public void tick() {
        super.tick();
        crocodileTaming.tick();

        if (!this.isChargingMouth()) {
            createCombo(32, 15, OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0, 2, this.isTame() ? 2.25 : 1.5, false, 0.15f);
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMad(this.isCombo());

        if (this.getTarget() != null && this.getTarget().hasEffect(OWEffects.FRACTURE.getDelegate())) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.25, 1.0, 1.25));
        }

        if (attackingGrabCooldown > 0) {
            attackingGrabCooldown--;
        }

        if (this.isAttackingGrab() && this.getTarget() != null && this.getTarget().isInWater() && this.isInWater()) {
            attackingGrabTimer++;

            if (this.getTarget() != null) {
                this.setLookAt(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
            }

            if (this.getTarget() != null && this.distanceTo(this.getTarget()) <= 1.0) {
                this.playSound(OWSounds.CROCODILE_MOUTH_CRUSH.get());
                this.getTarget().hurt(this.damageSource, 2);
                this.grabEntity(this.getTarget());
                attackingGrabTimer = 0;
                this.setAttackingGrab(false);
                return;
            }

            if (attackingGrabTimer > 8) {
                this.hasImpulse = true;

                if (this.getTarget() != null) {
                    Vec3 targetPos = this.getTarget().position();
                    Vec3 entityPos = this.position();

                    double dx = targetPos.x - entityPos.x;
                    double dy = targetPos.y - entityPos.y;
                    double dz = targetPos.z - entityPos.z;
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    this.setDeltaMovement(dx / distance * 1.0, dy / distance * 0.2, dz / distance * 1.0);
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
                }
            }

            if (attackingGrabTimer >= 15) {
                attackingGrabTimer = 0;
                this.setAttackingGrab(false);
            }
        }

        if (this.isInWater()) {
            this.setChargingMouth(false);
            this.setChargingMouthTimer(0);

            if (this.getTarget() != null && !this.isTame()) {
                if (grabUnderwaterCooldown > 0) {
                    grabUnderwaterCooldown--;
                }

                if (this.tickCount % 600 == 0 && grabUnderwaterCooldown == 0) {
                    this.setCanGrabUnderwater(!this.canGrabUnderwater());
                }
            }

            if (this.getGrabbedTarget() != null) {
                if (this.tickCount % 70 == 0) {
                    this.setDeathRolling(true);
                }
            }
        } else {
            this.setCanGrabUnderwater(false);
            this.setAttackingGrab(false);
            grabUnderwaterCooldown = 0;
        }

        if (this.isDeathRolling()) {
            this.setDeathRollProgress(this.getDeathRollProgress() + 1);
            if (this.isInWater()) {
                if (this.level().getSeaLevel() - this.getY() >= 2) {
                    this.setDeltaMovement(0, 0.02, 0);
                }
            }

            try {
                this.getGrabbedTarget().invulnerableTime = 0;

                if (this.getDeathRollProgress() % 5 == 0) {
                    this.getGrabbedTarget().hurt(this.damageSource, 0.5f);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SPLASH,
                                this.getX(), this.getY() + 0.5, this.getZ(),
                                20,
                                0.5, 0.4, 0.5,
                                0.1);
                    }
                }

            } catch (NullPointerException e) {
            }

            if (this.getDeathRollProgress() >= 40) {
                this.setDeathRollProgress(0);
                this.setDeathRolling(false);
            }
        }

        if (hasSomeoneInHisMouth()) {
            LivingEntity grabbed = this.getGrabbedTarget();
            
            if (grabbed instanceof Player) {
                this.setGrabTimeout(this.getGrabTimeout() + 1);
            }

            try {
                this.getGrabbedTarget().noPhysics = true;

                if (this.isInWater()) {
                    this.setLookAt(this.getGrabbedTarget().getX(), this.getGrabbedTarget().getY(), this.getGrabbedTarget().getZ());
                }

                if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                    this.setGrabTimeout(0);
                    this.getGrabbedTarget().kill();
                }
            } catch (NullPointerException e) {
            }

            if (!this.getGrabbedTarget().isAlive() || (this.getGrabbedTarget() instanceof Player player && player.isCreative())) {
                grabbed.stopRiding();
                this.getGrabbedTarget().noPhysics = false;
                this.setGrabbing(false, null);
                this.setTarget(null);
            } else {
                if (grabbed != null && !grabbed.isPassenger()) {
                    if (grabbed instanceof Player) {
                        grabbed.startRiding(this);
                    } else {
                        Vec3 look = this.getLookAngle();
                        grabbed.setPos(this.getX() + look.x * 1.75f , this.getY() - 0.2, this.getZ() + look.z * 1.75f);
                    }
                }
            }
        }

        markMudWithFootprints();

        handleRunningEffects(17, SoundEvents.HORSE_STEP, 0.2f, new int[]{4, 9});
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

    public int getGrabMaxTimeout() {
        return 600;
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
        if (this.isNapping()) {
            return;
        }

        super.setTarget(target);

        if (!isTame()) {
            setMad(!isBaby() && this.getTarget() != null && getSleepBarPercent() < 75 && !this.isSitting());
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        boolean targetIsNearOfWater = crocodileBehaviorHandler.findNearestWaterSource(10) != null;
        boolean isAlreadyGrabbed = entity.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getOwner() != entity;
        boolean canGrab = targetIsNearOfWater && !this.level().isClientSide()  &&
                !this.isTame() && !this.isSleeping() && !this.isNapping() && !this.isChargingMouth() && !isAlreadyGrabbed && this.getHealth() >= 10 && !(entity instanceof CrocodileEntity);

        if (canGrabOnLand) {
            if (!isAlreadyGrabbed && this.getHealth() >= 10 && !(entity instanceof CrocodileEntity)) {
                this.grabEntity(entity);
            }
        }

        if (canGrab) {
            if (this.onGround()) {
                if (comboAttack == 3) {
                    this.grabEntity(entity);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame()) {
            if (this.isSitting()) this.setSitting(false);
        }

        if (this.isInWater()) {
            Vec3 knockback = this.getDeltaMovement();
            boolean wasHurt = super.hurt(damageSource, v);

            if (wasHurt) {
                Vec3 newKnockback = this.getDeltaMovement();
                Vec3 appliedKnockback = newKnockback.subtract(knockback);

                this.setDeltaMovement(knockback.add(appliedKnockback.scale(0.05)));
            }

            return wasHurt;
        }

        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CrocodileEntity otherCrocodile) {
            if (otherCrocodile.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherCrocodile.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherCrocodile.getOwnerUUID());
            } else {
                return !otherCrocodile.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        Vec3 look = this.getLookAngle();

        if (passenger == this.getGrabbedTarget()) {
            function.accept(passenger, this.getX() + look.x * 1.75f , this.getY() - 0.2, this.getZ() + look.z * 1.75f);
        } else {
            super.positionRider(passenger, function);

            function.accept(passenger, this.getX(), this.getY() + 0.35f, this.getZ());
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
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseCrocodileVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private void markMudWithFootprints() {
        if (this.getTarget() == null && this.tickCount % 40 == 0 && isMoving()) {
            BlockPos blockPos = this.blockPosition();
            BlockState blockState = this.level().getBlockState(blockPos);

            if (blockState.is(Blocks.MUD)) {
                Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(this.level().getRandom());
                BlockState mudState = OWBlocks.MARKED_MUD.get().defaultBlockState().setValue(MarkedMudBlock.FACING, facing);

                this.level().setBlockAndUpdate(blockPos, mudState);
            }
        }
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) {
            setVariant(CrocodileVariant.SKIN_GOLD);
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

    private void grabEntity(LivingEntity entity) {
        if (entity instanceof OWEntity owEntity) {
            if (owEntity.getTheoreticalScale() <= 20) {
                this.setGrabbing(true, entity);
            }
        } else {
            this.setGrabbing(true, entity);
        }

        this.setGrabTimeout(300);
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == CrocodileVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private CrocodileVariant chooseCrocodileVariant() {
        CrocodileVariant variant;
        if (chance >= 95) variant = CrocodileVariant.BLACK;
        else if (chance >= 85) variant = CrocodileVariant.GREY;
        else if (chance >= 40) variant = CrocodileVariant.GREEN;
        else if (chance >= 20) variant = CrocodileVariant.DARK;
        else variant = CrocodileVariant.DEFAULT;
        return variant;
    }

    protected void handleMiscIdleAnimations() {
        if (this.growlsAnimationState.isStarted() &&
                this.tickCount - growlsAnimationStartTime > GROWLS_DURATION) {
            this.growlsAnimationState.stop();
        }

        if (this.gruntAnimationState.isStarted() &&
                this.tickCount - gruntAnimationStartTime > GRUNT_DURATION) {
            this.gruntAnimationState.stop();
        }

        if (tickCount % growlsInterval == 0) {
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), OWSounds.CROCODILE_IDLE_3.get(), this.getSoundSource(), 1.0F, 1.0F, false);
            }
            if (crocodileBehaviorHandler.canGrowl() && crocodileBehaviorHandler.canPlayIdleAnimation() && !crocodileBehaviorHandler.isAnyIdleAnimationPlaying()) {
                this.growlsAnimationState.start(this.tickCount);
                growlsAnimationStartTime = this.tickCount;
            }

            growlsInterval = (int) OWUtils.generateRandomInterval(400, 800);
        }

        if (tickCount % gruntInterval == 0) {
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), OWSounds.CROCODILE_IDLE_1.get(), this.getSoundSource(), 1.0F, 1.0F, false);
            }
            if (crocodileBehaviorHandler.canGrunt() && crocodileBehaviorHandler.canPlayIdleAnimation() && !crocodileBehaviorHandler.isAnyIdleAnimationPlaying()) {
                this.gruntAnimationState.start(this.tickCount);
                gruntAnimationStartTime = this.tickCount;
            }

            gruntInterval = (int) OWUtils.generateRandomInterval(300, 500);
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(96, true);

        handleMiscIdleAnimations();

        if (this.isInWater()) {
            if (this.idleWaterAnimationTimeout <= 0) {
                this.idleWaterAnimationTimeout = 57;
                this.idleWaterAnimationState.start(this.tickCount);
            } else --this.idleWaterAnimationTimeout;
        }

        if (!this.isInWater()) {
            this.idleWaterAnimationTimeout = 0;
            this.idleWaterAnimationState.stop();
        }

        if (this.isNapping()) {
            if (this.napAnimationStartTime <= 0) {
                this.napAnimationStartTime = 200;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationStartTime;
        }

        if (!this.isNapping()) {
            this.napAnimationStartTime = 0;
            this.napAnimationState.stop();
        }

        if (this.isDeathRolling()) {
            if (this.deathRollAnimationTimeout <= 0) {
                this.deathRollAnimationTimeout = 40;
                this.deathRollAnimationState.start(this.tickCount);
            } else --this.deathRollAnimationTimeout;
        }

        if (!this.isDeathRolling()) {
            this.deathRollAnimationTimeout = 0;
            this.deathRollAnimationState.stop();
        }

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, 37);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, 37);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 37);
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

    public CrocodileVariant getVariant() {
        return CrocodileVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(CrocodileVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public CrocodileVariant getInitialVariant() {
        return CrocodileVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(CrocodileVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD);}

    public void setDeathRollProgress(int getDeathRollProgress) {
        this.entityData.set(DEATH_ROLLING_PROGRESS, getDeathRollProgress);
    }

    public int getDeathRollProgress() {
        return Math.min(this.entityData.get(DEATH_ROLLING_PROGRESS), 40);
    }

    public void setGrabTimeout(int getGrabMaxTimeout) {
        this.entityData.set(GRAB_TIMEOUT, getGrabMaxTimeout);
    }

    public int getGrabTimeout() {
        return this.entityData.get(GRAB_TIMEOUT);
    }

    public void setChargingMouth(boolean isChargingMouth) {
        this.entityData.set(IS_CHARGING_MOUTH, isChargingMouth);
    }

    public boolean isChargingMouth() { return this.entityData.get(IS_CHARGING_MOUTH);}

    public void setGrabbing(boolean isGrabbing, LivingEntity entity) {
        this.entityData.set(IS_GRABBING, isGrabbing);
        this.setGrabbedTarget(entity);
    }

    public boolean isGrabbing() { return this.entityData.get(IS_GRABBING);}

    public void setCanGrabUnderwater(boolean canGrabUnderwater) {
        this.entityData.set(CAN_GRAB_UNDERWATER, canGrabUnderwater);
    }

    public boolean canGrabUnderwater() { return this.entityData.get(CAN_GRAB_UNDERWATER);}

    public void setAttackingGrab(boolean isAttackingGrab) {
        this.entityData.set(IS_ATTACKING_GRAB, isAttackingGrab);
        if (!isAttackingGrab) grabUnderwaterCooldown = 600;
    }

    public boolean isAttackingGrab() { return this.entityData.get(IS_ATTACKING_GRAB);}

    public void setDeathRolling(boolean isDeathRolling) {
        this.entityData.set(IS_DEATH_ROLLING, isDeathRolling);
    }

    public boolean isDeathRolling() { return this.entityData.get(IS_DEATH_ROLLING);}

    public boolean hasSomeoneInHisMouth() {
        if (this.level().isClientSide()) return false;
        return this.getGrabbedTarget() != null && this.isGrabbing();
    }

    public LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    public void setGrabbedTarget(LivingEntity target) {
        this.entityData.set(GRABBED_TARGET_ID, target == null ? -1 : target.getId());
    }

    public void setChargingMouthTimer(float chargingMouthTimer) {
        this.entityData.set(CHARGING_MOUTH_TIMER, chargingMouthTimer);
    }

    public float getChargingMouthTimer() { return this.entityData.get(CHARGING_MOUTH_TIMER);}

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
    }

    public static class CrocodileNearestAttackableTargetGoal extends NearestAttackableTargetGoal {

        private final CrocodileEntity crocodile;
        private final float attacksMultiplier;

        public CrocodileNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee, float attacksMultiplier) {
            super(mob, targetType, mustSee);
            this.crocodile = (CrocodileEntity) mob;
            this.attacksMultiplier = attacksMultiplier;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }

            if (this.target == null || this.crocodile.isNapping()) return false;

            if (this.crocodile.distanceTo(this.target) <= 6) {
                return true;
            }

            if (this.crocodile.level().isDay() && !this.target.isInWater()) {
                return this.crocodile.getRandom().nextInt((int) (50 / this.attacksMultiplier)) == 0;
            }

            return true;
        }
    }
}