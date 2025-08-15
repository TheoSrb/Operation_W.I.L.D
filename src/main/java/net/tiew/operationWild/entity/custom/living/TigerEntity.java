package net.tiew.operationWild.entity.custom.living;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.OWTameImplementation;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.AI.*;
import net.tiew.operationWild.entity.AI.NapGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.TigerVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.TigerUtilsSendToClientPacket;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWTags;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;
import static net.tiew.operationWild.utils.OWUtils.determinateMinAndMax;

public class TigerEntity extends OWEntity implements OWTameImplementation, PlayerRideableJumping, FoodsPreference, OWEntityUtils {

    public static final double TAMING_EXPERIENCE = 195.0;

    public String[] quests = {};
    public boolean wantToScarifyWood = false;
    public boolean goAway = false;
    private boolean canPlaySound = true;
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState trappingAnimationState = new AnimationState();
    public final AnimationState sleepingAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState preparingToNapAnimationState = new AnimationState();
    public int jumpAnimationTimeout = 0;
    public int trappingAnimationTimeout = 0;
    public int sleepingAnimationTimeout = 0;
    public int napAnimationTimeout = 0;
    public int preparingToNapAnimationTimeout = 0;
    private boolean isSprinting;
    private int timeMaxBeforeJumping;
    private int tameJumpTime;
    public int delayBeforeJumpingInTicks = 30;
    public int cooldownInTicks = 400;
    private boolean applyCooldown = false;
    private int prepareJumpInTicks;
    private int hitTrappingCount;
    public int cooldownJump = 0;
    private int cooldownTime = 0;
    private int numberFeedsWanted;
    public int ultimateTimer = 0;
    private int runTime;
    public LivingEntity TRAPPED_ENTITY_TAMED = null;
    public int chargeTimer = 0;
    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUMPING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRAP_ENTITY = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPAWN_PARTICLE = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TAME_JUMPING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_BOSS_SKIN = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PIZZA_CHEF_SKIN = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DETECTIVE_SKIN = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> SKIN_PIZZA_CHEF_IS_BUYING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SKIN_DETECTIVE_IS_BUYING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SKIN_VIRUS_IS_BUYING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);

    public TigerVariant getVariant() { return TigerVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(TigerVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public TigerVariant getInitialVariant() { return TigerVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(TigerVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public TigerEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0xc47037;
    }

    @Override
    public float getEntityScale() {
        return 7.5f;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.TIGER_SADDLE.get();
    }

    @Override
    public List<Class<?>> getEntityType() {
        return ASSASSIN_ENTITIES;
    }

    @Override
    public List<Object> getEntityDiet() {
        return CARNIVOROUS_ENTITIES;
    }

    @Override
    public String getTamingAdvancement() {
        return "wild_meow";
    }

    @Override
    public float getMaxVitalEnergy() {
        return 300 * (1 + ((float) this.getLevel() / 100));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1f;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(1, new OWPanicGoal(this, this.getSpeed() * 16f, 3, 75));
        this.goalSelector.addGoal(0, new NapGoal(this, 200, 800, 20, 100,null, false, () -> getDayOrNightTimeInterval(11,16) && !this.isJumpingOnTarget() && !this.isTrappingEntity() && this.onGround()));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 15f,20, 3,!this.isJumpingOnTarget() && !this.isTrappingEntity()));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(2, new TigerScarifyTreeGoal(this, 20, 0.9D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, PeacockEntity.class, true));
        this.goalSelector.addGoal(2, new OWBreedGoal(this, 1.0D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 35D).add(Attributes.MOVEMENT_SPEED, 0.21D).add(Attributes.FOLLOW_RANGE, 25D).add(Attributes.ATTACK_DAMAGE, 6D).add(Attributes.KNOCKBACK_RESISTANCE, 0.4D).add(Attributes.JUMP_STRENGTH, 0.6D);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) { return OWEntityRegistry.TIGER.get().create(serverLevel);}

    @Override
    public boolean isFood(ItemStack itemStack) { return itemStack.is(OWItems.RAW_PEACOCK.get());}

    protected float getSoundVolume() { return 1.75f;}

    protected SoundEvent getAmbientSound() { return !this.isNapping() && !this.isPreparingNapping() && !this.isSleeping() ? RANDOM(3) ? RANDOM(2) ? isVirus() ?  OWSounds.TIGER_IDLE_2_VIRUS.get() :  OWSounds.TIGER_IDLE_2.get() : isVirus() ? OWSounds.TIGER_IDLE_3_VIRUS.get() : OWSounds.TIGER_IDLE_3.get() : null : null;}

    protected SoundEvent getHurtSound(DamageSource damageSource) { return isVirus() ? OWSounds.TIGER_IDLE_VIRUS.get() : OWSounds.TIGER_IDLE.get();}

    protected SoundEvent getDeathSound() { return isVirus() ? OWSounds.TIGER_ROAR_VIRUS.get() : OWSounds.TIGER_ROAR.get();}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!this.isVehicle()) super.playStepSound(blockPos, blockState);
    }

    public void setJumpingOnTarget(boolean jumping) { this.entityData.set(JUMPING, jumping);}

    public boolean isJumpingOnTarget() { return this.entityData.get(JUMPING);}

    public void setTameJumping(boolean jumping) { this.entityData.set(TAME_JUMPING, jumping);}

    public boolean isTameJumping() { return this.entityData.get(TAME_JUMPING);}

    public boolean isSprinting() { return this.isSprinting;}

    public void setSprinting(boolean sprinting) { this.isSprinting = sprinting;}

    public void setTrappingEntity(boolean trapping) { this.entityData.set(TRAP_ENTITY, trapping);}

    public boolean isTrappingEntity() { return this.entityData.get(TRAP_ENTITY);}

    public void setCanSpawnParticles(boolean canSpawnParticles) { this.entityData.set(SPAWN_PARTICLE, canSpawnParticles);}

    public boolean canSpawnParticles() { return this.entityData.get(SPAWN_PARTICLE);}

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD);}

    public void setSkinBoss(boolean isBoss) { this.entityData.set(IS_BOSS_SKIN, isBoss);}
    public boolean isBoss() { return this.entityData.get(IS_BOSS_SKIN);}
    public void setSkinPizzaChef(boolean isPizzaChef) { this.entityData.set(IS_PIZZA_CHEF_SKIN, isPizzaChef);}
    public boolean isPizzaChef() { return this.entityData.get(IS_PIZZA_CHEF_SKIN);}
    public void setSkinDetective(boolean isPizzaChef) { this.entityData.set(IS_DETECTIVE_SKIN, isPizzaChef);}
    public boolean isDetective() { return this.entityData.get(IS_DETECTIVE_SKIN);}

    public boolean isWhite() { return this.getVariant() == TigerVariant.WHITE;}

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            case 3 -> this.entityData.set(SKIN_PIZZA_CHEF_IS_BUYING, true);
            case 5 -> this.entityData.set(SKIN_DETECTIVE_IS_BUYING, true);
            case 6 -> this.entityData.set(SKIN_VIRUS_IS_BUYING, true);
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }

    public boolean skinPizzaChefIsAlreadyBuying() { return this.entityData.get(SKIN_PIZZA_CHEF_IS_BUYING);}
    public boolean skinDetectiveIsAlreadyBuying() { return this.entityData.get(SKIN_DETECTIVE_IS_BUYING);}
    public boolean skinVirusIsAlreadyBuying() { return this.entityData.get(SKIN_VIRUS_IS_BUYING);}

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();
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
        if (this.isSaddled()) this.spawnAtLocation(OWItems.TIGER_SADDLE.get());
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping()) {
            super.setTarget(null);
            return;
        }

        if (isSleeping() || getSleepBarPercent() >= 75) {
            super.setTarget(null);
            resetTrappedEntity();
            return;
        }

        if (isTameJumping()) {
            super.setTarget(null);
            return;
        }

        if (!isTame()) setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting() && lastVisibleTarget != null);

        super.setTarget(target);
    }

    public boolean isVirus() {
        return this.getVariant() == TigerVariant.SKIN_VIRUS;
    }

    public void changeSkin(int skinIndex) {
        setSkinBoss(false);
        setSkinPizzaChef(false);
        setSkinDetective(false);
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) setVariant(TigerVariant.SKIN_GOLD);
        else if (skinIndex == 2) setSkinBoss(true);
        else if (skinIndex == 3) setSkinPizzaChef(true);
        else if (skinIndex == 4) setVariant(TigerVariant.SKIN_MAGMA);
        else if (skinIndex == 5) setSkinDetective(true);
        else if (skinIndex == 6) setVariant(TigerVariant.SKIN_VIRUS);
        else if (skinIndex == 7) setVariant(getInitialVariant());
        else if (skinIndex == 8) setVariant(TigerVariant.SKIN_DAMNED);

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

    @Override
    public void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        if (this.isUltimate() && this.isRunning() && this.onGround()) {
            if (player.zza != 0) {
                Vec3 deltaMovement = this.getDeltaMovement();
                this.setDeltaMovement(deltaMovement.scale(1.2));
            }
        }
    }

    public void tick() {
        super.tick();

        if (isUltimate()) {
            this.ultimateTimer++;

            if (this.ultimateTimer >= 300) {
                this.setUltimate(false);
                this.ultimateCooldown = 1;
            }
        }

        if (!this.level().isClientSide()) {
            if (TRAPPED_ENTITY_TAMED != null && TRAPPED_ENTITY_TAMED.isAlive()) {
                if (this.TRAPPED_ENTITY_TAMED instanceof Player) {
                    this.TRAPPED_ENTITY_TAMED.teleportTo(
                            this.getX() - 1.35,
                            this.getY() - 0.8,
                            this.getZ() + 0.6);
                } else {
                    this.TRAPPED_ENTITY_TAMED.teleportTo(
                            this.getX() - 1,
                            this.getY(),
                            this.getZ() + 0.5);
                }
                this.setTrappingEntity(true);

                TRAPPED_ENTITY_TAMED.hurt(this.damageSource, this.getDamage() / 4);
                TRAPPED_ENTITY_TAMED.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(this.getX(), this.getY(), this.getZ()));
                hitTrappingCount++;
                if (hitTrappingCount % 10 == 0) {
                    double pitch = OWUtils.generateRandomInterval(0.9, 1.0);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), isVirus() ? OWSounds.TIGER_HURTING_VIRUS.get() : OWSounds.TIGER_HURTING.get(), SoundSource.NEUTRAL, 1.0F, (float) pitch);
                    this.heal(0.5f);
                }

                this.setYRot(0);
                this.setXRot(0);


                if (hitTrappingCount >= 150) {
                    TRAPPED_ENTITY_TAMED.teleportTo(this.getX(), this.getY(), this.getZ());
                    TRAPPED_ENTITY_TAMED = null;
                    this.setTrappingEntity(false);
                }

                if (this.onGround()) this.setDeltaMovement(new Vec3(0,0,0));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 5, false, false, false));
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 2, false, false, false));
            }
        }

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new TigerUtilsSendToClientPacket(this.getId(), this.ultimateTimer), player);
                }
            }
        }

        LivingEntity target = this.getTarget();
        createAttackSystem(9);
        createTameAttackSystem(this.isRunning() ? 13 : 9, 3, this.isVirus() ? OWSounds.TIGER_HURTING_VIRUS.get() : OWSounds.TIGER_HURTING.get(), 3.0, 3.5, 1.5, true);
        if (this.isVehicle() && this.isTame() && !this.isSitting() && TRAPPED_ENTITY_TAMED == null && !isTrappingEntity()) setMad(this.isAttacking());
        if (!this.level().isClientSide()) setTamingPercentage(this.numberFeedsGiven, this.numberFeedsWanted);

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        double targetX = target != null ? target.getX() : 0;
        double targetY = target != null ? target.getY() : 0;
        double targetZ = target != null ? target.getZ() : 0;

        double distanceY = targetY - y;

        List<LivingEntity> enemies = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2));

        if (this.isInResurrection()) this.setSleeping(true);

        for (LivingEntity entity: enemies) {
            if (this.isNapping() && !this.isTame() && entity != this) {
                if (entity instanceof Player player && player.isCreative()) {
                    return;
                }
                this.setNap(false, 0);
            }
        }

        if (this.getVariant() == TigerVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }

        if (cooldownJump >= 1) {
            cooldownJump++;

            if (cooldownJump >= 600) cooldownJump = 0;
        }

        if (this.isTame() && this.isTameJumping()) {
            this.setMad(true);
            tameJumpTime++;
            cooldownJump = 1;

            List<Entity> passengers = this.getPassengers();
            Entity controllingPassenger = null;


            if (tameJumpTime == 1) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), isVirus() ? OWSounds.TIGER_JUMP_VIRUS.get() : OWSounds.TIGER_JUMP.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
                this.jumpFromGround();
            }

            float speed = 1.1f;
            float angle = (float) Math.toRadians(this.getYRot());
            double forwardX = -Math.sin(angle) * speed;
            double forwardZ = Math.cos(angle) * speed;

            this.setDeltaMovement(forwardX, this.getDeltaMovement().y, forwardZ);
            if (!passengers.isEmpty()) {
                controllingPassenger = passengers.get(0);
            }
            if (!onGround()) {
                destroyingBlock(2, Blocks.BAMBOO);
                List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2));
                for (LivingEntity entity : livingEntities) {
                    if (entity.getMaxHealth() <= 20 && entity.getHealth() > 4 && !entity.isAlliedTo(this) && entity != this && entity != controllingPassenger && entity != this.getControllingPassenger()) {
                        if (!passengers.isEmpty()) {
                            controllingPassenger = passengers.get(0);
                            controllingPassenger.stopRiding();
                        }
                        target = entity;
                        TRAPPED_ENTITY_TAMED = entity;
                        tameJumpTime = 0;
                        this.setTameJumping(false);
                    }
                }
            }

            if ((onGround() || isInWater()) && tameJumpTime >= 20) {
                tameJumpTime = 0;
                this.setTameJumping(false);
            }
        }

        if (((this.isVehicle() && this.isRunning()) || getTarget() != null)) {
            if (this.level().isClientSide()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.zza > 0) {
                    runTime++;
                    if (runTime % 12 == 0 && runTime != 0 && this.onGround()) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.HORSE_STEP, 1.0F, 0.3f)
                        );
                    }
                } else {
                    runTime = 0;
                }
            }
        }


        if (isJumpingOnTarget() && canSpawnParticles()) {
            OWUtils.spawnParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, -0.75, 0.5, 10, 1);
            setCanSpawnParticles(false);
        }

        if (tickCount % 800 == 0 && !isBaby()) this.wantToScarifyWood = true;
        if (this.level().isClientSide()) setupAnimationState();


        if (this.isSleeping()) {
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
            }
        }

        if (applyCooldown) {
            if (cooldownTime < this.cooldownInTicks) {
                cooldownTime += 2;
                return;
            }
        }

        if (this.isPreparingNapping() || this.isNapping() || this.isSleeping()) return;

        if (target == null) return;

        if (target.isDeadOrDying() || target.isInWall() || (target instanceof Player player && player.isCreative())) {
            resetTrappedEntity();
            return;
        }

        if (this.distanceTo(target) > 5 && !isBaby() && !this.ownerIsRiding() && !this.isSitting() && !this.isInWater() && !this.isUnderWater() && !this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
            if (prepareJumpInTicks < delayBeforeJumpingInTicks) prepareJumpInTicks++;
        }

        if (prepareJumpInTicks >= delayBeforeJumpingInTicks && distanceY <= 5) {
            this.setJumpingOnTarget(true);
        }

        if (this.isJumpingOnTarget()) {
            if (ownerIsRiding()) {
                resetTrappedEntity();
                return;
            }

            this.setLookAt(targetX, targetY, targetZ);
            if (this.horizontalCollision || target.isDeadOrDying()) {
                this.setJumpingOnTarget(false);
                resetTrappedEntity();
                return;
            }
            this.jumpingOnTarget();
            if (canPlaySound) {
                this.level().playSound(null, x, y, z, isVirus() ? OWSounds.TIGER_JUMP_VIRUS.get() : OWSounds.TIGER_JUMP.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
                canPlaySound = false;
            }

            if (this.distanceTo(target) <= 2) {
                if (target.getMaxHealth() <= 25) {
                    for (TigerEntity otherTiger : this.level().getEntitiesOfClass(TigerEntity.class, this.getBoundingBox().inflate(10))) {
                        if (otherTiger != this && otherTiger.TRAPPED_ENTITY == target) {
                            otherTiger.resetTrappedEntity();
                            return;
                        }
                    }

                    if (target.isVehicle()) {
                        resetTrappedEntity();
                        return;
                    }
                    if (target.isDeadOrDying()) {
                        this.setJumpingOnTarget(false);
                        resetTrappedEntity();
                        return;
                    }
                    if (target.getVehicle() != null) {
                        this.setJumpingOnTarget(false);
                        resetTrappedEntity();
                        return;
                    }
                    if (target.isInWater()) {
                        this.setJumpingOnTarget(false);
                        resetTrappedEntity();
                        return;
                    }

                    if (target instanceof LivingEntity livingTarget) {
                        Holder<Enchantment> slidingHolder = livingTarget.level().registryAccess()
                                .registryOrThrow(Registries.ENCHANTMENT)
                                .getHolderOrThrow(OWEnchantments.SLIDING);

                        int slidingLevel = EnchantmentHelper.getEnchantmentLevel(slidingHolder, livingTarget);

                        if (slidingLevel > 0) {
                            float chanceToAvoid = 0.25f * slidingLevel;
                            if (livingTarget.getRandom().nextFloat() < (1 - chanceToAvoid)) {
                                this.setJumpingOnTarget(false);
                                resetTrappedEntity();
                                return;
                            }
                        }
                    }

                    this.TRAPPED_ENTITY = target;
                    this.setTrappingEntity(true);
                }

                this.setJumpingOnTarget(false);
                setCanSpawnParticles(true);
                prepareJumpInTicks = 0;
            }
        }

        if (hasSomethingInTheirArms()) {
            if (target.isDeadOrDying()) {
                resetTrappedEntity();
                return;
            }
            if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
                resetTrappedEntity();
                return;
            }
            if (ownerIsRiding()) {
                resetTrappedEntity();
                return;
            }
            TRAPPED_ENTITY.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(x, y, z));
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
            }
            canPlaySound = true;
            if (this.TRAPPED_ENTITY == null || this.TRAPPED_ENTITY.isDeadOrDying()) {
                resetTrappedEntity();
                return;
            }

            if (TRAPPED_ENTITY instanceof Player playerTrapped) {
                if (playerTrapped.isCreative()) {
                    resetTrappedEntity();
                    return;
                }
            }


            this.TRAPPED_ENTITY.hurt(this.damageSource, this.getDamage() / 4);
            hitTrappingCount++;
            if (hitTrappingCount % 10 == 0) {
                double pitch = OWUtils.generateRandomInterval(0.9, 1.0);
                this.level().playSound(null, x, y, z, isVirus() ? OWSounds.TIGER_HURTING_VIRUS.get() : OWSounds.TIGER_HURTING.get(), SoundSource.NEUTRAL, 1.0F, (float) pitch);
                this.heal(0.5f);
            }


            if (hitTrappingCount >= 150) {
                this.TRAPPED_ENTITY.teleportTo(x, y, z);
                resetTrappedEntity();
            }

            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 5, false, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 2, false, false, false));

            try {
                if (this.TRAPPED_ENTITY instanceof Player) {
                    this.TRAPPED_ENTITY.teleportTo(
                            x - 1.35,
                            y - 0.8,
                            z + 0.6);
                } else {
                    this.TRAPPED_ENTITY.teleportTo(
                            x - 1,
                            y,
                            z + 0.5);
                }
            } catch (Exception e) {
                resetTrappedEntity();
                return;
            }

            this.setYRot(0);
            this.setXRot(0);
        }
    }

    public boolean hasSomethingInTheirArms() { return this.isTrappingEntity() && this.TRAPPED_ENTITY != null && !this.TRAPPED_ENTITY.isDeadOrDying();}

    private void resetTrappedEntity() {
        this.TRAPPED_ENTITY = null;
        this.setCanSpawnParticles(true);
        this.setTrappingEntity(false);
        this.setJumpingOnTarget(false);
        this.hitTrappingCount = 0;
        this.prepareJumpInTicks = 0;
        canPlaySound = true;
        cooldownTime = 0;
        applyCooldown = true;
    }

    public void disarmTarget(LivingEntity target) {
        if (target == null) return;
        ItemStack itemstack = target.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemstack.isEmpty()) {
            ItemStack droppedItem = itemstack.copy();
            this.spawnAtLocation(droppedItem);
            target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    public void jumpingOnTarget() {
        if (isJumpingOnTarget() && !this.horizontalCollision) {
            Vec3 targetPos;
            final float radius = 1.0F;
            final float angle = (0.0174532925F * this.yBodyRot);
            final double extraX = radius * Mth.sin(Mth.PI + angle);
            final double extraZ = radius * Mth.cos(angle);
            final double extraY = -0.5F;

            if (getTarget() != null) targetPos = getTarget().position();
            else return;

            if (canPlaySound) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), isVirus() ? OWSounds.TIGER_JUMP_VIRUS.get() : OWSounds.TIGER_JUMP.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
                canPlaySound = false;
            }

            Vec3 direction = new Vec3(
                    targetPos.x - (this.getX() + extraX),
                    targetPos.y - (this.getY() + extraY) + 0.5,
                    targetPos.z - (this.getZ() + extraZ));

            Vec3 movement = direction.normalize().scale(1.0);

            this.setDeltaMovement(movement);

            destroyingBlock(2, Blocks.BAMBOO);
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.isTameJumping() ? null : super.getControllingPassenger();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (isJumpingOnTarget() || isTrappingEntity()) return false;
        double pitch = OWUtils.generateRandomInterval(0.9, 1.0);
        if (RANDOM(5)) disarmTarget((LivingEntity) entity);
        this.playSound(isVirus() ? OWSounds.TIGER_HURTING_VIRUS.get() : OWSounds.TIGER_HURTING.get(), 1.25f, (float) pitch);
        OWUtils.spawnBlurrParticle(this.level(), this, 1, 1, 1);
        return super.doHurtTarget(entity);
    }

    protected float getRiddenSpeed(Player player) {
        return this.isTameJumping() ? 0.0f : super.getRiddenSpeed(player);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        float basePosition = 0.65f * (getScale() * 1.3f);
        Vec3 movement = this.getDeltaMovement();
        Vec3 look = this.getLookAngle();
        double dot = movement.normalize().dot(look.normalize());

        if (entity instanceof Player player) {
            if (player.zza == 0) {
                moveFunction.accept(entity, this.getX(), this.getY() + basePosition + (isUltimate() ? 0.5f : 0), this.getZ());
            } else if (this.isSaddled() && this.isRunning() && dot >= 0.1) {
                float animLength = 0.43077F;
                float speedMultiplier = 0.75f;
                float heightMax = 8.0F;
                float heightMin = -3.0F;

                float walkSpeed = this.walkAnimation.speed();
                float walkPos = this.walkAnimation.position();

                float animProgress = (walkPos * speedMultiplier / 20.0F) % animLength / animLength;
                float yOffset;

                if (animProgress < 0.12308F / animLength) {
                    float progress = animProgress / (0.12308F / animLength);
                    yOffset = heightMax + (1.86F - heightMax) * progress;
                } else if (animProgress < 0.21538F / animLength) {
                    float progress = (animProgress - (0.12308F / animLength)) / ((0.21538F - 0.12308F) / animLength);
                    yOffset = 1.86F + (heightMin - 1.86F) * progress;
                } else {
                    float progress = (animProgress - (0.21538F / animLength)) / ((animLength - 0.21538F) / animLength);
                    yOffset = heightMin + (heightMax + -(heightMin)) * progress;
                }

                float amplitudeFactor = Math.min(1.0F, walkSpeed * 0.6F);
                yOffset *= amplitudeFactor;

                yOffset /= 16.0F;

                moveFunction.accept(entity,
                        this.getX(),
                        this.getY() + basePosition + yOffset + (isUltimate() ? 0.5f : 0),
                        this.getZ());
            }
        }
    }

    @Override
    public void onPlayerJump(int i) {
        if (!this.onGround() || this.isInWater() || this.isUnderWater()) return;
        if (this.getVitalEnergy() > (this.getMaxVitalEnergy() - 10)) return;

        float pitch = (float) OWUtils.generateRandomInterval(0.7, 0.9);
        float jumpCharge = Math.min(i, 100) / 100.0f;
        double d0 = (double)this.getJumpPower(jumpCharge);
        Vec3 vec3 = this.getDeltaMovement();

        this.setDeltaMovement(vec3.x, d0, vec3.z);
        this.hasImpulse = true;
        OWUtils.spawnParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, -0.75, 0.5, 10,1);

        float angle = (float) Math.toRadians(this.getYRot());
        double forwardX = -Math.sin(angle) * ((1.25 * i) / 100);
        double forwardZ = Math.cos(angle) * ((1.25 * i) / 100);

        this.setDeltaMovement(forwardX, this.getDeltaMovement().y, forwardZ);
        this.setVitalEnergy(this.getVitalEnergy() + 10);

        if (this.level().isClientSide() && i > 50) this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), isVirus() ? OWSounds.TIGER_HURTING_VIRUS.get() : OWSounds.TIGER_HURTING.get(), SoundSource.NEUTRAL, 1.0F, pitch, false);
        if (vec3.z > (double)0.0F) {
            float f = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
            float f1 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
            this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f * jumpCharge), (double)0.0F, (double)(0.4F * f1 * jumpCharge)));
        }

        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(this));
    }

    @Override
    public boolean canJump() { return this.isRunning();}

    @Override
    public void handleStartJump(int i) {}

    @Override
    public void handleStopJump() {}

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow && this.isTrappingEntity()) return false;
        if (this.isUltimate()) {
            v = v * 0.5f;
        }
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        resetTrappedEntity();
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof TigerEntity otherTiger) {
            if (this.isTame()) return otherTiger.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherTiger.getOwnerUUID());
            else return !otherTiger.isTame();
        }
        return super.isAlliedTo(entity);
    }

    private boolean isValidFood(ItemStack stack) { return stack.is(OWTags.Items.TIGER_TAMING_FOOD);}

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
        if (!isTrappingEntity() && !isSleeping() && !isJumpingOnTarget() && !wantToScarifyWood && !isNapping() && !isPreparingNapping() && isFood(itemStack) && !isBaby() && isTame()) {
            return super.mobInteract(player, hand);
        }

        createFoodHealingSystem(player, itemStack, preferRawMeat(), preferCookedMeat(), preferVegetables(), 1.5f);

        if (isValidFood(itemStack) && !this.isTame() && (this.isSleeping() || this.isBaby())) {
            numberFeedsGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            itemStack.shrink(1);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && numberFeedsGiven >= numberFeedsWanted) {
                    addTamingExperience(TAMING_EXPERIENCE / 2, player);
                    this.setTame(true, player);
                    this.setSleeping(false);
                    resetSleepBar();
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (isValidFood(itemStack)) return InteractionResult.PASS;
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
        this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
        this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
        this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

        timeMaxBeforeJumping = (int) OWUtils.generateRandomInterval(25, 50);
        numberFeedsWanted = (int) determinateMinAndMax((int) (this.getMaxHealth() * 0.35 + this.getDamage() * 0.2), 20);

        Holder<Biome> biome = this.level().getBiome(this.blockPosition());
        TigerVariant tigerVariant;

        tigerVariant = biome.is(Biomes.BAMBOO_JUNGLE) ? TigerVariant.LIGHT_ORANGE : TigerVariant.DEFAULT;    // Choose Tiger's color with biome where he spawned.
        if (RANDOM(10)) tigerVariant = TigerVariant.GOLDEN;  // 10% chance to spawn golden tiger.
        if (RANDOM(10)) tigerVariant = TigerVariant.WHITE;  // 10% chance to spawn white tiger.

        this.setVariant(tigerVariant);  // Apply Tiger's Color to the entity.
        this.setInitialVariant(this.getVariant());
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createAttackAnimation(9, true);
        createSitAnimation(80, true);

        if (this.isPreparingNapping()) {
            if (this.preparingToNapAnimationTimeout <= 0) {
                this.preparingToNapAnimationTimeout = 20;
                this.preparingToNapAnimationState.start(this.tickCount);
            } else --this.preparingToNapAnimationTimeout;
        }

        if (!this.isPreparingNapping()) {
            this.preparingToNapAnimationTimeout = 0;
            this.preparingToNapAnimationState.stop();
        }

        if (this.isNapping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 100;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        }

        if (!this.isNapping()) {
            this.napAnimationTimeout = 0;
            this.napAnimationState.stop();
        }

        if (this.isJumpingOnTarget() || this.isTameJumping() || this.isFalling()) {
            if (this.jumpAnimationTimeout <= 0) {
                this.jumpAnimationTimeout = 44;
                this.jumpAnimationState.start(this.tickCount);
            } else --this.jumpAnimationTimeout;
        }

        if (!this.isJumpingOnTarget() && !this.isTameJumping() && !this.isFalling()) {
            this.jumpAnimationTimeout = 0;
            this.jumpAnimationState.stop();
        }

        if (this.isSleeping()) {
            if (this.sleepingAnimationTimeout <= 0) {
                this.sleepingAnimationTimeout = 96;
                this.sleepingAnimationState.start(this.tickCount);
            } else --this.sleepingAnimationTimeout;
        }

        if (this.isTrappingEntity()) {
            if (this.trappingAnimationTimeout <= 0) {
                this.trappingAnimationTimeout = 23;
                this.trappingAnimationState.start(this.tickCount);
            } else --this.trappingAnimationTimeout;
        }

        if (!this.isTrappingEntity()) {
            this.trappingAnimationTimeout = 0;
            this.trappingAnimationState.stop();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(JUMPING, false);
        builder.define(TRAP_ENTITY, false);
        builder.define(IS_MAD, false);
        builder.define(SPAWN_PARTICLE, true);
        builder.define(TAME_JUMPING, false);

        builder.define(IS_BOSS_SKIN, false);
        builder.define(IS_PIZZA_CHEF_SKIN, false);
        builder.define(IS_DETECTIVE_SKIN, false);

        builder.define(SKIN_PIZZA_CHEF_IS_BUYING, false);
        builder.define(SKIN_DETECTIVE_IS_BUYING, false);
        builder.define(SKIN_VIRUS_IS_BUYING, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());

        tag.putBoolean("isBoss", this.isBoss());
        tag.putBoolean("isPizzaChef", this.isPizzaChef());
        tag.putBoolean("isDetective", this.isDetective());

        tag.putBoolean("skinPizzaChefIsAlreadyBuying", this.skinPizzaChefIsAlreadyBuying());
        tag.putBoolean("skinDetectiveIsAlreadyBuying", this.skinDetectiveIsAlreadyBuying());
        tag.putBoolean("skinVirusIsAlreadyBuying", this.skinVirusIsAlreadyBuying());

        tag.putInt("timeMaxBeforeJumping", this.timeMaxBeforeJumping);
        tag.putInt("numberFeedsWanted", this.numberFeedsWanted);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("cooldownJump", this.cooldownJump);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));

        this.entityData.set(IS_BOSS_SKIN, tag.getBoolean("isBoss"));
        this.entityData.set(IS_PIZZA_CHEF_SKIN, tag.getBoolean("isPizzaChef"));
        this.entityData.set(IS_DETECTIVE_SKIN, tag.getBoolean("isDetective"));

        this.entityData.set(SKIN_PIZZA_CHEF_IS_BUYING, tag.getBoolean("skinPizzaChefIsAlreadyBuying"));
        this.entityData.set(SKIN_DETECTIVE_IS_BUYING, tag.getBoolean("skinDetectiveIsAlreadyBuying"));
        this.entityData.set(SKIN_VIRUS_IS_BUYING, tag.getBoolean("skinVirusIsAlreadyBuying"));

        this.timeMaxBeforeJumping = tag.getInt("timeMaxBeforeJumping");
        this.numberFeedsWanted = tag.getInt("numberFeedsWanted");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.cooldownJump = tag.getInt("cooldownJump");
    }
}
