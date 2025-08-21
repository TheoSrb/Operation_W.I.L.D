package net.tiew.operationWild.entity.custom.living;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.AI.*;
import net.tiew.operationWild.entity.OWTameImplementation;
import net.tiew.operationWild.entity.categoy.OWGroupEntity;
import net.tiew.operationWild.entity.variants.PeacockVariant;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.item.custom.ElephantSaddle;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWUtils;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class ElephantEntity extends OWGroupEntity implements OWEntityUtils, OWTameImplementation, FoodsPreference {

    public static final double TAMING_EXPERIENCE = 345.0;

    public static final int FOOTSTEP_MAX_DISTANCE = 20;

    public String[] quests = {};

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PLAYER_CAN_JUMP = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> SADDLE_DATA = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.COMPOUND_TAG);

    public ElephantVariant getVariant() { return ElephantVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(ElephantVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public ElephantVariant getInitialVariant() { return ElephantVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(ElephantVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public ElephantEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed, boolean canBeAlpha) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed, canBeAlpha);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 8749692;
    }

    @Override
    public float getEntityScale() {
        return 15;
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
        return 2f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return true;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.ELEPHANT_SADDLE.get();
    }

    @Override
    public List<Class<?>> getEntityType() {
        return TANK_ENTITIES;
    }

    @Override
    public String getTamingAdvancement() {
        return "";
    }

    @Override
    public float getMaxVitalEnergy() {
        return 225 * (1 + ((float) this.getLevel() / 100));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.65f;
    }

    @Override
    public boolean preferRawMeat() { return false;}

    @Override
    public boolean preferCookedMeat() { return false;}

    @Override
    public boolean preferVegetables() {
        return true;
    }


    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 20f, 8, 5, true));
        this.goalSelector.addGoal(4, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 65.0D).add(Attributes.MOVEMENT_SPEED, 0.14D).add(Attributes.FOLLOW_RANGE, 30.0D).add(Attributes.ATTACK_DAMAGE, 12.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() { return 1f;}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
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

        if (this.isSaddled()) {
            ItemStack ancientSaddle = this.getInventory().getStackInSlot(0);
            ItemStack saddle = new ItemStack(OWItems.ELEPHANT_SADDLE.get());
            saddle.set(OWDataComponentTypes.SADDLE_WOOLS.get(), ancientSaddle.get(OWDataComponentTypes.SADDLE_WOOLS.get()));
            this.spawnAtLocation(saddle);
        }

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
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }


    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) setVariant(ElephantVariant.SKIN_GOLD);
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

    public boolean breakWoodAround(Vec3 center, float radius, boolean square, float dropChance) {
        if (this.isBaby() || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(this.level(), this) || level().isClientSide) {
            return false;
        }
        boolean flag = false;
        for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(center.x - radius),
                Mth.floor(center.y - radius), Mth.floor(center.z - radius),
                Mth.floor(center.x + radius), Mth.floor(center.y + radius),
                Mth.floor(center.z + radius))) {
            BlockState blockstate = this.level().getBlockState(blockpos);
            boolean isLog = blockstate.is(BlockTags.LOGS);

            if (blockstate.blocksMotion() && (blockstate.getBlock().getExplosionResistance() <= 15)
                    && (square || blockpos.distToCenterSqr(center.x, center.y, center.z) < radius * radius)
                    && isLog || blockstate.is(BlockTags.LEAVES) || blockstate.is(BlockTags.CROPS) || blockstate.is(BlockTags.SAPLINGS)) {
                level().destroyBlock(blockpos, isLog);
                flag = true;
            }
        }
        return flag;
    }

    public void setPlayerJump(boolean isPlayerJump) { this.entityData.set(PLAYER_CAN_JUMP, isPlayerJump);}
    public boolean isPlayerJump() { return this.entityData.get(PLAYER_CAN_JUMP);}

    public void applyFootstep() {
        List<LivingEntity> livingEntitiesAround = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FOOTSTEP_MAX_DISTANCE));

        for (int i = 0; i < 3; i++) {
            this.playSound(OWSounds.ELEPHANT_FOOTSTEP.get(), 1.5f, 1.0f);
        }

        for (LivingEntity livingEntityAround : livingEntitiesAround) {
            if (livingEntityAround.onGround()) {
                if (livingEntityAround instanceof Player player && player.isCreative()) continue;
                if (livingEntityAround instanceof ElephantEntity) continue;
                if (livingEntityAround.isInWater() || livingEntityAround.isInWall()) continue;
                float shakeIntensity = livingEntityAround.distanceTo(this);
                shakeIntensity = ((FOOTSTEP_MAX_DISTANCE - shakeIntensity) / 10) / 3;

                livingEntityAround.setDeltaMovement(livingEntityAround.getDeltaMovement().x, shakeIntensity / 2, livingEntityAround.getDeltaMovement().z);
            }
        }
        setPlayerJump(true);
    }

    private int shockWaveTimer = 0;
    public Vec3 look;
    public boolean isCreatingShockWave = false;

    public void createShockWave() {
        isCreatingShockWave = true;
        this.playSound(OWSounds.ELEPHANT_FOOTSTEP.get(), 1.5f, 1.0f);
        this.playSound(OWSounds.MINI_EARTHQUAKE.get(), 1.5f, 1.2f);
    }

    public void tick() {
        super.tick();
        Vec3 center = new Vec3(0, 1, 1 * this.getScale()).yRot(-this.yBodyRot * ((float) Math.PI / 180F)).add(position());

        createCombo(33, 22, actualAttackNumber == 2 ? SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR : OWSounds.TIGER_HURTING.get(), 3.0, 3.5, 1.5, false, actualAttackNumber == 2 ? 4 : 2);

        if (this.accelerationIsAtMax()) {
            Vec3 look = this.getLookAngle();
            double x = this.getX() + look.x * 2.0;
            double z = this.getZ() + look.z * 2.0;

            AABB area = new AABB(x - 2, this.getY() - 2, z - 2, x + 2, this.getY() + 3, z + 2);
            List<Entity> entitiesInRange = this.level().getEntities(this, area, entity -> entity instanceof LivingEntity);

            if (this.tickCount % 5 == 0) {
                this.breakWoodAround(center, 4.0F, false, 1.0F);
            }

            for (Entity entity : entitiesInRange) {
                if (!entity.isAlive() || entity == this) continue;
                if (entity instanceof OWEntity owEntity && owEntity.getOwner() == this.getOwner()) continue;
                if (entity == this.getOwner()) continue;

                entity.hurt(this.damageSource, 2);
            }
        }


        if (isCreatingShockWave) {
            shockWaveTimer++;

            if (shockWaveTimer == 1) {
                look = this.getLookAngle();
            }


            double x = this.getX() + look.x * (2.0 + ((double) shockWaveTimer / 3));
            double z = this.getZ() + look.z * (2.0 + ((double) shockWaveTimer / 3));
            AABB area = new AABB(x - 2, this.getY() - 1, z - 2, x + 2, this.getY() + 1, z + 2);
            List<Entity> entitiesInRange = this.level().getEntities(
                    this,
                    area,
                    entity -> entity instanceof LivingEntity
            );

            for (Entity entity : entitiesInRange) {
                if (!entity.isAlive() || entity == this) continue;
                if (entity instanceof OWEntity owEntity && owEntity.getOwner() == this.getOwner()) continue;
                if (entity == this.getOwner()) continue;

                entity.hurt(this.damageSource, this.getDamage() / 3);
                entity.playSound(SoundEvents.ROOTED_DIRT_HIT);
                Vec3 knockback = look.scale(0.5);
                entity.setDeltaMovement(entity.getDeltaMovement().x + knockback.x, 0.75 - ((double) shockWaveTimer / 120), entity.getDeltaMovement().z + knockback.z);
            }

            for (int i = 0; i < 50; i++) {
                double px = area.minX + Math.random() * (area.maxX - area.minX);
                double py = area.minY + Math.random() * (area.maxY - area.minY);
                double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
                BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());

                this.level().addParticle(particleOption, px, py, pz, 0, 0, 0);
            }
            for (int i = 0; i < 5; i++) {
                double px = area.minX + Math.random() * (area.maxX - area.minX);
                double py = area.minY + Math.random() * (area.maxY - area.minY);
                double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
                this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 0, 0, 0);
            }

            if (shockWaveTimer >= 60) {
                shockWaveTimer = 0;
                isCreatingShockWave = false;
            }
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (!this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) createTameAttackSystem(30, 20, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 5, 3.5, 2, false);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        List<Player> playersAround = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(FOOTSTEP_MAX_DISTANCE));

        if (this.isRunning()) {
            for (Player playerAround : playersAround) {
                if (!playerAround.onGround() || this.getControllingPassenger() == playerAround) continue;
                float shakeIntensity = playerAround.distanceTo(this);
                shakeIntensity = ((FOOTSTEP_MAX_DISTANCE - shakeIntensity) / 10);
                ClientEvents.shakeCamera(shakeIntensity, playerAround);
            }
        }

        if (isPlayerJump()) {
            for (Player playerAround : playersAround) {
                if (playerAround.onGround()) {
                    float shakeIntensity = playerAround.distanceTo(this);
                    shakeIntensity = ((FOOTSTEP_MAX_DISTANCE - shakeIntensity) / 10) / 3;

                    playerAround.setDeltaMovement(playerAround.getDeltaMovement().x, shakeIntensity / 2, playerAround.getDeltaMovement().z);

                    if (this.level().isClientSide()) ClientEvents.shakeCamera(shakeIntensity, playerAround);
                }
            }
            if (this.isRunning()) OWUtils.spawnParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, -1.5, 0.5, 10, 0.5);
            setPlayerJump(false);
        }

        if (this.getVariant() == ElephantVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 10, 3);
        }
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
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 3;
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        int passengerIndex = this.getPassengers().indexOf(entity);
        Vec3 look = this.getLookAngle();

        if (passengerIndex == 0) function.accept(entity, entity.getX(), entity.getY() + (getComboAttack() == 3 ? 0.5f : 0), entity.getZ());
        else if (passengerIndex == 1) function.accept(entity, entity.getX() - (look.x * 0.6f), entity.getY() + (getComboAttack() == 3 ? 0.5f : 0), entity.getZ() - (look.z * 0.6f));
        else if (passengerIndex == 2) function.accept(entity, entity.getX() - (look.x * 1.35f), entity.getY() + (getComboAttack() == 3 ? 0.5f : 0), entity.getZ() - (look.z * 1.35f));
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


            this.setVariant(chooseElephantVariant());
            this.setInitialVariant(this.getVariant());

            foodWanted = (int) OWUtils.generateRandomInterval(20, 30);
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    public void setSaddleWools(List<Item> wools) {
        CompoundTag tag = new CompoundTag();
        int[] woolIds = new int[wools.size()];
        for (int i = 0; i < wools.size(); i++) {
            woolIds[i] = BuiltInRegistries.ITEM.getId(wools.get(i));
        }
        tag.putIntArray("wools", woolIds);
        this.entityData.set(SADDLE_DATA, tag);
    }

    public List<Item> getSaddleWools() {
        CompoundTag tag = this.entityData.get(SADDLE_DATA);
        List<Item> wools = new ArrayList<>();
        if (tag.contains("wools")) {
            int[] woolIds = tag.getIntArray("wools");
            for (int woolId : woolIds) {
                Item wool = BuiltInRegistries.ITEM.byId(woolId);
                wools.add(wool);
            }
        }
        return wools;
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
        createSitAnimation(121, true);

        if (this.isCombo(1)) {
            if (this.attack1ComboTimer <= 0) {
                this.attack1ComboTimer = 40;
                this.attack1Combo.start(this.tickCount);
            } else --this.attack1ComboTimer;
        }

        if (!this.isCombo(1)) {
            this.attack1ComboTimer = 0;
            this.attack1Combo.stop();
        }

        if (this.isCombo(2)) {
            if (this.attack2ComboTimer <= 0) {
                this.attack2ComboTimer = 33;
                this.attack2Combo.start(this.tickCount);
            } else --this.attack2ComboTimer;
        }

        if (!this.isCombo(2)) {
            this.attack2ComboTimer = 0;
            this.attack2Combo.stop();
        }

        if (this.isCombo(3)) {
            if (this.attack3ComboTimer <= 0) {
                this.attack3ComboTimer = 33;
                this.attack3Combo.start(this.tickCount);
            } else --this.attack3ComboTimer;
        }

        if (!this.isCombo(3)) {
            this.attack3ComboTimer = 0;
            this.attack3Combo.stop();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(PLAYER_CAN_JUMP, false);
        builder.define(SADDLE_DATA, new CompoundTag());
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("foodGiven", this.foodGiven);

        if (this.getInventory() != null) {
            ItemStack saddleStack = this.getInventory().getStackInSlot(0);
            if (!saddleStack.isEmpty() && saddleStack.getItem() instanceof ElephantSaddle) {
                List<Item> wools = saddleStack.get(OWDataComponentTypes.SADDLE_WOOLS.get());
                if (wools != null && !wools.isEmpty()) {
                    int[] woolIds = new int[wools.size()];
                    for (int i = 0; i < wools.size(); i++) {
                        woolIds[i] = BuiltInRegistries.ITEM.getId(wools.get(i));
                    }
                    tag.putIntArray("SaddleWools", woolIds);
                }
            }
        }

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodWanted = tag.getInt("foodWanted");
        this.foodGiven = tag.getInt("foodGiven");

        if (tag.contains("SaddleWools")) {
            int[] woolIds = tag.getIntArray("SaddleWools");
            List<Item> wools = new ArrayList<>();
            for (int woolId : woolIds) {
                Item wool = BuiltInRegistries.ITEM.byId(woolId);
                wools.add(wool);
            }
            this.setSaddleWools(wools);
        }
    }
}

