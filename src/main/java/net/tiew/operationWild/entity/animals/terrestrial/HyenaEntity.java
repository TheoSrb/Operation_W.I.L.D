package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.goals.OWFollowOwnerGoal;
import net.tiew.operationWild.entity.goals.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.HyenaVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.core.OWUtils;


public class HyenaEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, PlayerRideableJumping {

    public static final double TAMING_EXPERIENCE = 55.0;

    public String[] quests = {};

    private int runTime;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(HyenaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEAD_X_ROT = SynchedEntityData.defineId(HyenaEntity.class, EntityDataSerializers.FLOAT);

    public HyenaVariant getVariant() { return HyenaVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(HyenaVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public HyenaVariant getInitialVariant() { return HyenaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(HyenaVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public HyenaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 9662025;
    }

    @Override
    public float getTheoreticalScale() {
        return 4.5f;
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
    public float vehicleRunSpeedMultiplier() {
        return 3.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return -1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 4f;
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
        return OWAdvancements.HYENA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return Float.MAX_VALUE;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.1f * (1 + ((float) this.getLevel() / 50));
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

    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 18.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 30.0D).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return OWUtils.RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() { return 1f;}

    public void setHeadX(float xHead) { this.entityData.set(HEAD_X_ROT, xHead);}
    public float getHeadX() { return this.entityData.get(HEAD_X_ROT);}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        super.playStepSound(blockPos, blockState);
    }

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }
    
    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();
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
        /*if (this.isSaddled()) this.spawnAtLocation(OWItems.HYENA_SADDLE.get());*/
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
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

    public void tick() {
        super.tick();

        createCombo(10, 6, random.nextInt(2) == 0 ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 1.5, false, 0.1f);

        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (((this.isVehicle() && this.isRunning()) || getTarget() != null)) {
            if (this.level().isClientSide()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.zza > 0) {
                    runTime++;

                    if (runTime >= 10) runTime = 0;

                    if (runTime == 5 && this.onGround()) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.HORSE_STEP, 1.2f, 0.5f)
                        );
                    }
                } else {
                    runTime = 0;
                }
            }
        }
        
        
        
        

        /*if (this.getVariant() == HyenaVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }*/
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
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY(), entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof HyenaEntity otherHyena) {
            if (otherHyena.isBaby()) return true;
            if (this.isTame()) return otherHyena.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherHyena.getOwnerUUID());
            else return !otherHyena.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWItems.RAW_KODIAK.get());
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


            this.setVariant(chooseHyenaVariant());
            this.setInitialVariant(this.getVariant());

            this.foodWanted = (int) OWUtils.generateRandomInterval(17, 24);
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private HyenaVariant chooseHyenaVariant() {
        HyenaVariant variant;
        if (chance >= 75) variant = HyenaVariant.GREY;
        else if (chance >= 50) variant = HyenaVariant.DARK;
        else if (chance >= 25) variant = HyenaVariant.YELLOW;
        else variant = HyenaVariant.DEFAULT;
        return variant;
    }

    @Override
    public void onPlayerJump(int i) {
        if (!this.onGround() || this.isInWater() || this.isUnderWater()) return;
        if (this.getVitalEnergy() > (this.getMaxVitalEnergy() - 10)) return;

        float pitch = (float) OWUtils.generateRandomInterval(0.7, 0.9);
        float jumpCharge = Math.min(i, 100) / 100.0f;
        double d0 = (double)this.getJumpPower(jumpCharge) * 1.35f;
        Vec3 vec3 = this.getDeltaMovement();

        this.setDeltaMovement(vec3.x, d0, vec3.z);
        this.hasImpulse = true;
        OWUtils.spawnParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, -0.75, 0.5, 10,1);

        float angle = (float) Math.toRadians(this.getYRot());
        double forwardX = -Math.sin(angle) * ((1.25 * i) / 100);
        double forwardZ = Math.cos(angle) * ((1.25 * i) / 100);

        this.setDeltaMovement(forwardX, this.getDeltaMovement().y, forwardZ);
        this.setVitalEnergy(this.getVitalEnergy() + 10);

        if (this.level().isClientSide() && i > 50) this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_HURTING.get(), SoundSource.NEUTRAL, 1.0F, pitch, false);
        if (vec3.z > (double)0.0F) {
            float f = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));

            Vec3 lookDirection = this.getLookAngle();
            Vec3 forwardPush = lookDirection.scale((-0.4 * f * jumpCharge) * 2);

            this.move(MoverType.SELF, forwardPush);

            this.hasImpulse = true;
        }

        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(this));
    }

    @Override
    public boolean canJump() { return this.isRunning();}

    @Override
    public void handleStartJump(int i) {}

    @Override
    public void handleStopJump() {}

    private void setupAnimationState() {
        createIdleAnimation(48, true);
        createSitAnimation(80, true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(HEAD_X_ROT, (float) OWUtils.generateRandomInterval(0, 40));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putFloat("getHeadX", this.getHeadX());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(HEAD_X_ROT, tag.getFloat("getHeadX"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
    }
}

