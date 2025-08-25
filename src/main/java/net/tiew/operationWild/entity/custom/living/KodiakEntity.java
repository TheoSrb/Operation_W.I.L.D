package net.tiew.operationWild.entity.custom.living;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Salmon;
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
import net.tiew.operationWild.entity.AI.*;
import net.tiew.operationWild.entity.OWTameImplementation;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.OWFoodPacketClient;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class KodiakEntity extends OWEntity implements OWTameImplementation, OWEntityUtils, FoodsPreference {

    public static final double TAMING_EXPERIENCE = 180.0;

    public String[] quests = {};

    private int runTime;

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KodiakEntity.class, EntityDataSerializers.INT);

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

    public KodiakEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 8215109;
    }

    @Override
    public float getEntityScale() {
        return 10;
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
    public List<Class<?>> getEntityType() {
        return TANK_ENTITIES;
    }

    @Override
    public String getTamingAdvancement() {
        return "";
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

    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 30f, 8, 3, true));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(new Class[0]));

        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Salmon.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Bee.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.17D).add(Attributes.FOLLOW_RANGE, 25.0D).add(Attributes.ATTACK_DAMAGE, 9.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (RANDOM(3)) {
            if (RANDOM(2)) return OWSounds.KODIAK_IDLE_3.get();
            else if (RANDOM(2)) return OWSounds.KODIAK_IDLE_2.get();
            else return OWSounds.KODIAK_IDLE_1.get();
        }
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.KODIAK_MISC.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.KODIAK_HURT.get() : OWSounds.KODIAK_MISC.get();
    }

    protected float getSoundVolume() {
        return 1f;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!isRunning()) super.playStepSound(blockPos, blockState);
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
        /*if (this.isSaddled()) this.spawnAtLocation(OWItems.KODIAK_SADDLE.get());*/
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }


    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) setVariant(KodiakVariant.SKIN_GOLD);
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

    public void tick() {
        super.tick();

        createCombo(20, 12, random.nextInt(2) == 0 ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 2.25, false, 2);

        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (((this.isVehicle() && this.isRunning()) || getTarget() != null)) {
            if (this.level().isClientSide()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.zza > 0) {
                    runTime++;

                    if (runTime >= 29) runTime = 0;

                    if ((runTime == 5 || runTime == 19) && this.onGround()) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.HORSE_STEP, 0.8f, 0.5f)
                        );
                    }
                } else {
                    runTime = 0;
                }
            }
        }


        if (this.getVariant() == KodiakVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
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
            if (entity instanceof Player player) {
                if (player.zza == 0) {
                    moveFunction.accept(entity, this.getX() + (look.x / 2.5), entity.getY() + (-0.2f) + (getComboAttack() == 3 ? 0.35f : 0), this.getZ() + (look.z / 2.5));
                } else if (this.isRunning() && dot >= 0.1) {
                    float yOffset = calculateAnimatedYOffset(1.44F, 1.0f, 17.0F, 0.0F, 0.6F);
                    moveFunction.accept(entity, this.getX(), entity.getY() + (-0.2f) + yOffset + (getComboAttack() == 3 ? 0.35f : 0), this.getZ());
                } else {
                    moveFunction.accept(entity, this.getX() + (look.x / 2.5), entity.getY() + (-0.2f) + (getComboAttack() == 3 ? 0.35f : 0), this.getZ() + (look.z / 2.5));
                }
            }
        } else if (passengerIndex == 1) {
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY(), this.getZ() - (look.z / 1.5));
        } else if (this.isRunning() && dot >= 0.1) {
            float yOffset = calculateAnimatedYOffset(1.44F, 1.0f, 17.0F, 0.0F, 0.6F);
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY() + yOffset, this.getZ() - (look.z / 1.5));
        } else {
            moveFunction.accept(entity, this.getX() - (look.x / 1.5), entity.getY(), this.getZ() - (look.z / 1.5));


        }
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof KodiakEntity otherKodiak) {
            if (this.isTame()) return otherKodiak.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherKodiak.getOwnerUUID());
            else return !otherKodiak.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWItems.COOKED_PEACOCK.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (isFood(itemStack) && !this.isTame()) {
            this.foodGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            itemStack.shrink(1);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && this.foodGiven >= this.foodWanted) {
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


            this.setVariant(chooseKodiakVariant());
            this.setInitialVariant(this.getVariant());

            this.foodWanted = (int) OWUtils.generateRandomInterval(10, 16);
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
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

        if (this.isCombo(1)) {
            if (this.attack1ComboTimer <= 0) {
                this.attack1ComboTimer = 20;
                this.attack1Combo.start(this.tickCount);
            } else --this.attack1ComboTimer;
        }

        if (!this.isCombo(1)) {
            this.attack1ComboTimer = 0;
            this.attack1Combo.stop();
        }

        if (this.isCombo(2)) {
            if (this.attack2ComboTimer <= 0) {
                this.attack2ComboTimer = 20;
                this.attack2Combo.start(this.tickCount);
            } else --this.attack2ComboTimer;
        }

        if (!this.isCombo(2)) {
            this.attack2ComboTimer = 0;
            this.attack2Combo.stop();
        }

        if (this.isCombo(3)) {
            if (this.attack3ComboTimer <= 0) {
                this.attack3ComboTimer = 30;
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
    }

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
}

