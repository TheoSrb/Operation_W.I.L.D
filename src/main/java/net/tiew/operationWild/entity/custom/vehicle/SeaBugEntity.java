package net.tiew.operationWild.entity.custom.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.entity.OWEntityUtils;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.SeaBugVariant;
import net.tiew.operationWild.item.OWItems;

public class SeaBugEntity extends Submarine implements OWEntityUtils {

    public static final int MAX_DEPTH = 50;
    public static final int MAX_BATTERY = 2;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ALIMENTED_BY_BATTERY_1 = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ALIMENTED_BY_BATTERY_2 = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_CHEST_AMELIORATED = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.FLOAT);


    public SeaBugVariant getVariant() { return SeaBugVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(SeaBugVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public SeaBugVariant getInitialVariant() { return SeaBugVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(SeaBugVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public SeaBugEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity's AI
    protected void registerGoals() {
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {

    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (isAlimentedByBattery1()) {
            ItemStack battery1 = getItemInSlot(0);
            if (!battery1.isEmpty()) {
                this.spawnAtLocation(battery1);
            }
        }
        if (isAlimentedByBattery2()) {
            ItemStack battery2 = getItemInSlot(1);
            if (!battery2.isEmpty()) {
                this.spawnAtLocation(battery2);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 10.0D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public void setAlimentedByBattery1(boolean isAlimentedByBattery1) {this.entityData.set(IS_ALIMENTED_BY_BATTERY_1, isAlimentedByBattery1);}
    public boolean isAlimentedByBattery1() { return this.entityData.get(IS_ALIMENTED_BY_BATTERY_1);}

    public void setAlimentedByBattery2(boolean isAlimentedByBattery2) {this.entityData.set(IS_ALIMENTED_BY_BATTERY_2, isAlimentedByBattery2);}
    public boolean isAlimentedByBattery2() { return this.entityData.get(IS_ALIMENTED_BY_BATTERY_2);}

    public void setChestAmelioration(boolean isChestAmeliorated) {this.entityData.set(IS_CHEST_AMELIORATED, isChestAmeliorated);}
    public boolean isChestAmeliorated() { return this.entityData.get(IS_CHEST_AMELIORATED);}

    public void setLastPlayerPitch(float getLastPlayerPitch) {this.entityData.set(PITCH, getLastPlayerPitch);}
    public float getLastPlayerPitch() { return this.entityData.get(PITCH);}

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    public void tick() {
        super.tick();

        Player rider = (Player) this.getControllingPassenger();
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (this.level().isClientSide()) setupAnimationState();

        showSubmarine(15);

        if (depth >= MAX_DEPTH) {
            if (rider != null) {
                if (!rider.isCreative()) {
                    applyWaterPressureDamage(depth, rider);
                }
            } else applyWaterPressureDamage(depth, null);
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
        }

        if (!this.level().isClientSide()) {
            ItemStack itemInSlot0 = getItemInSlot(0);
            ItemStack itemInSlot1 = getItemInSlot(1);

            if (!itemInSlot0.isEmpty()) {
                Item item = itemInSlot0.getItem();
                if (item == OWItems.BATTERY.get()) {
                    this.setAlimentedByBattery1(true);
                } else this.setAlimentedByBattery1(false);
            } else this.setAlimentedByBattery1(false);

            if (!itemInSlot1.isEmpty()) {
                Item item = itemInSlot1.getItem();
                if (item == OWItems.BATTERY.get()) {
                    this.setAlimentedByBattery2(true);
                } else this.setAlimentedByBattery2(false);
            } else this.setAlimentedByBattery2(false);
        }


        if (!this.level().isClientSide()) {
            ItemStack stack1 = getItemInSlot(0);
            ItemStack stack2 = getItemInSlot(1);

            int maxDurability1 = stack1.getMaxDamage();
            int currentDurability1 = stack1.getDamageValue();
            int durabilityRemaining1 = maxDurability1 - currentDurability1;

            int maxDurability2 = stack2.getMaxDamage();
            int currentDurability2 = stack2.getDamageValue();
            int durabilityRemaining2 = maxDurability2 - currentDurability2;

            setEnergy(Math.max(0, (durabilityRemaining1 / MAX_BATTERY) + (durabilityRemaining2 / MAX_BATTERY)));
        }

        int interval = isLightOn() ? 600 : 800;

        if (tickCount % interval == 0) {
            boolean consumedPower = false;

            if (isAlimentedByBattery1()) {
                ItemStack itemInSlot0 = getItemInSlot(0);
                if (!itemInSlot0.isEmpty() && itemInSlot0.getDamageValue() < itemInSlot0.getMaxDamage() && (itemInSlot0.getMaxDamage() - itemInSlot0.getDamageValue()) > 0) {
                    itemInSlot0.setDamageValue(itemInSlot0.getDamageValue() + 1);
                    consumedPower = true;
                }
            }

            if (!consumedPower && isAlimentedByBattery2()) {
                ItemStack itemInSlot1 = getItemInSlot(1);
                if (!itemInSlot1.isEmpty() && itemInSlot1.getDamageValue() < itemInSlot1.getMaxDamage() && (itemInSlot1.getMaxDamage() - itemInSlot1.getDamageValue()) > 0) {
                    itemInSlot1.setDamageValue(itemInSlot1.getDamageValue() + 1);
                }
            }
        }
    }

    public ItemStack getItemInSlot(int slot) {
        if (this.itemStackHandlerSeaBug != null && slot < this.itemStackHandlerSeaBug.getSlots()) {
            return this.itemStackHandlerSeaBug.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY() - 1.4f, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(SeaBugVariant.DEFAULT);
        this.setInitialVariant(this.getVariant());
        this.setSaddle(true);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private void setupAnimationState() {
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_ALIMENTED_BY_BATTERY_1, false);
        builder.define(IS_ALIMENTED_BY_BATTERY_2, false);

        builder.define(IS_CHEST_AMELIORATED, false);

        builder.define(PITCH, 0.0f);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isAlimentedByBattery1", this.isAlimentedByBattery1());
        tag.putBoolean("isAlimentedByBattery2", this.isAlimentedByBattery2());

        tag.putBoolean("isChestAmeliorated", this.isChestAmeliorated());

        tag.putFloat("getLastPlayerPitch", this.getLastPlayerPitch());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_1, tag.getBoolean("isAlimentedByBattery1"));
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_2, tag.getBoolean("isAlimentedByBattery2"));

        this.entityData.set(IS_CHEST_AMELIORATED, tag.getBoolean("isChestAmeliorated"));

        this.entityData.set(PITCH, tag.getFloat("getLastPlayerPitch"));
    }

    @Override
    public int getEntityColor() {
        return 14212580;
    }
}

