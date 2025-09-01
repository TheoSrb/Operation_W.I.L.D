package net.tiew.operationWild.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.variants.SeaBugShardVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.utils.OWUtils;

public class SeaBugShard2Entity extends SeabugShard {

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(SeaBugShard2Entity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> YROT = SynchedEntityData.defineId(SeaBugShard2Entity.class, EntityDataSerializers.FLOAT);

    public SeaBugShardVariant getVariant() { return SeaBugShardVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(SeaBugShardVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}

    public SeaBugShard2Entity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 25.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 10.0D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource p_21239_) {
        return SoundEvents.GLASS_BREAK;
    }

    public void setYRotShard(float getYRot) {this.entityData.set(YROT, getYRot);}
    public float getYRotShard() { return this.entityData.get(YROT);}

    @Override
    protected void registerGoals() {
    }

    public void tick() {
        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            ItemStack itemToGive = new ItemStack(OWItems.SEABUG_PORTHOLE.get(), 1);
            if (!player.getInventory().add(itemToGive)) {
                player.drop(itemToGive, false);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(SeaBugShardVariant.DEFAULT_SHARD_2);
        this.setYRotShard((float) OWUtils.generateRandomInterval(-30, 30));
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(YROT, 0.0f);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getTypeVariant());
        tag.putFloat("getYRotShard", this.getYRotShard());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(YROT, tag.getFloat("getYRotShard"));
    }
}