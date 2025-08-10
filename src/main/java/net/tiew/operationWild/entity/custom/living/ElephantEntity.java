package net.tiew.operationWild.entity.custom.living;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.entity.AI.OWAttackGoal;
import net.tiew.operationWild.entity.variants.JellyfishVariant;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.AI.OWFollowOwnerGoal;
import net.tiew.operationWild.entity.AI.OWPanicGoal;
import net.tiew.operationWild.entity.AI.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class ElephantEntity extends OWEntity implements OWEntityUtils {

    public static final double TAMING_EXPERIENCE = 345.0;

    public String[] quests = {};
    public int foodGiven = 0;
    public int foodWanted;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    public ElephantVariant getVariant() { return ElephantVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(ElephantVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public ElephantVariant getInitialVariant() { return ElephantVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(ElephantVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public ElephantEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }


    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 15f,20, 3,true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 65.0D).add(Attributes.MOVEMENT_SPEED, 0.14D).add(Attributes.FOLLOW_RANGE, 30.0D).add(Attributes.ATTACK_DAMAGE, 8.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
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
        /*if (this.isSaddled()) this.spawnAtLocation(OWItems.ELEPHANT_SADDLE.get());*/
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

    private float getLimbSwing() {
        return this.walkAnimation.position();
    }

    private float getLimbSwingAmount() {
        return this.walkAnimation.speed();
    }

    public void tick() {
        super.tick();
        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);


        final int maxDistance = 20;

        List<LivingEntity> entitiesAround = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(maxDistance));
        float shakeIntensity = 0;

        for (LivingEntity living : entitiesAround) {
            if (!living.isAlive()) continue;
            if (living instanceof Player player && player.isCreative()) continue;
            if (living instanceof ElephantEntity) continue;
            shakeIntensity = living.distanceTo(this);

            shakeIntensity = ((maxDistance - shakeIntensity) / 10) / 3;

            int firstInteraction = (int) (24 / (this.getTarget() != null ? 1.5f : 1.0f));
            int secondInteraction = (int) (58 / (this.getTarget() != null ? 1.5f : 1.0f));

            if (shakeIntensity > 0 && getLimbSwingAmount() > 0.1f) {
                int walkAnimationTick = (int) ((int) (getLimbSwing() * (68 / (this.getTarget() != null ? 1.5f : 1.0f)) / (2 * Math.PI)) % (68 / (this.getTarget() != null ? 1.5f : 1.0f)));

                if ((walkAnimationTick >= (firstInteraction - 5) && walkAnimationTick <= (firstInteraction + 5)) ||
                        (walkAnimationTick >= (secondInteraction - 5) && walkAnimationTick <= (secondInteraction + 5))) {

                    if (living instanceof Player player && !player.isCreative()) {
                        ClientEvents.shakeCamera(shakeIntensity * 3, player);
                    }
                }

                if ((walkAnimationTick >= (firstInteraction - 1) && (walkAnimationTick <= firstInteraction + 1)) ||
                        (walkAnimationTick >= (secondInteraction - 1) && walkAnimationTick <= (secondInteraction + 1))) {

                    double yawRadians = Math.toRadians(this.getYRot());
                    double distance = 1.5;
                    double rightOffset = (walkAnimationTick >= (secondInteraction - 1) && walkAnimationTick <= (secondInteraction + 1)) ? 1.0 : -1.0;

                    double frontX = this.getX() - Math.sin(yawRadians) * distance;
                    double frontY = this.getY();
                    double frontZ = this.getZ() + Math.cos(yawRadians) * distance;

                    double rightX = frontX + Math.cos(yawRadians) * rightOffset;
                    double rightZ = frontZ + Math.sin(yawRadians) * rightOffset;

                    double backX = this.getX() - Math.sin(yawRadians) * (-1.0);
                    double backZ = this.getZ() + Math.cos(yawRadians) * (-1.0);

                    double rightBackX = backX + Math.cos(yawRadians) * -rightOffset;
                    double rightBackZ = backZ + Math.sin(yawRadians) * -rightOffset;

                    if (this.level().isClientSide()) {
                        for (int i = 0; i < 30; i++) {
                            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                                    rightX, frontY, rightZ,
                                    0, 0, 0);
                            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                                    rightBackX, frontY, rightBackZ,
                                    0, 0, 0);
                        }
                    }



                    if (!living.isInWater() && !living.isInLava() && !living.isInvulnerable() && living.onGround()) {
                        if (this.getTarget() == null) {
                            this.playSound(OWSounds.ELEPHANT_FOOTSTEP.get(), 1.5f, 1.0f);
                            living.setDeltaMovement(living.getDeltaMovement().x, shakeIntensity / 2, living.getDeltaMovement().z);
                        }
                    }
                }
                if (this.getTarget() != null && tickCount % 10 == 0) {
                    this.playSound(OWSounds.ELEPHANT_FOOTSTEP.get(), 1.5f, 1.0f);
                    living.setDeltaMovement(living.getDeltaMovement().x, shakeIntensity / 2, living.getDeltaMovement().z);
                }
            } else this.walkAnimation.position(0);
        }

        /*if (this.getVariant() == ElephantVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
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
        function.accept(entity, entity.getX(), entity.getY() - 1, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof ElephantEntity otherElephant) {
            if (this.isTame()) return otherElephant.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherElephant.getOwnerUUID());
            else return !otherElephant.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (/*itemStack.is(OWItems.SAVAGE_BERRIES.get()) &&*/ !this.isTame() && this.isBaby()) {
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


            this.setVariant(chooseElephantVariant());
            this.setInitialVariant(this.getVariant());
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private ElephantVariant chooseElephantVariant() {
        ElephantVariant variant;
        if (chance >= 66.67) variant = ElephantVariant.PINK;
        else if (chance >= 33.33) variant = ElephantVariant.GREY;
        else variant = ElephantVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
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

    @Override
    public int getEntityColor() {
        return 8749692;
    }

    @Override
    public float getEntityScale() {
        return 15;
    }
}

