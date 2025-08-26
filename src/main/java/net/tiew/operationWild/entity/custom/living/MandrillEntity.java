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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.entity.OWTameImplementation;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.AI.OWFollowOwnerGoal;
import net.tiew.operationWild.entity.AI.OWPanicGoal;
import net.tiew.operationWild.entity.AI.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.MandrillVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class MandrillEntity extends OWEntity implements OWEntityUtils, OWTameImplementation {

    public static final double TAMING_EXPERIENCE = 65.0;

    public String[] quests = {};
    public int foodGiven = 0;
    public int foodWanted;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(MandrillEntity.class, EntityDataSerializers.INT);

    public MandrillVariant getVariant() { return MandrillVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(MandrillVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public MandrillVariant getInitialVariant() { return MandrillVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(MandrillVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public MandrillEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 3880756;
    }

    @Override
    public float getEntityScale() {
        return 5.5f;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 5f;
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
    public String getTamingAdvancement() {
        return "";
    }

    @Override
    public float getMaxVitalEnergy() {
        return 500 * (1 + ((float) this.getLevel() / 50));
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
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 18.0D).add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() { return 1f;}

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
        /*if (this.isSaddled()) this.spawnAtLocation(OWItems.MANDRILL_SADDLE.get());*/
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
        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);
        
        
        
        
        
        

        /*if (this.getVariant() == MandrillVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
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
        function.accept(entity, entity.getX(), entity.getY() - 0.15f, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof MandrillEntity otherMandrill) {
            if (this.isTame()) return otherMandrill.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherMandrill.getOwnerUUID());
            else return !otherMandrill.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(OWItems.SAVAGE_BERRIES.get()) && !this.isTame()) {
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


            this.setVariant(chooseMandrillVariant());
            this.setInitialVariant(this.getVariant());
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private MandrillVariant chooseMandrillVariant() {
        MandrillVariant variant;
        if (chance >= 66.67) variant = MandrillVariant.BLUE;
        else if (chance >= 33.33) variant = MandrillVariant.SILVER;
        else variant = MandrillVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(71, true);
        createSitAnimation(80, true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
    }
}

