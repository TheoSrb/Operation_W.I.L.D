package net.tiew.operationWild.entity.custom.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;

import javax.annotation.Nullable;

public class PrimitiveSpearProjectileEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY;
    private static final EntityDataAccessor<Boolean> ID_FOIL;
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public PrimitiveSpearProjectileEntity(EntityType<? extends PrimitiveSpearProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PrimitiveSpearProjectileEntity(Level level, LivingEntity living, ItemStack itemStack) {
        super(OWEntityRegistry.PRIMITIVE_SPEAR_PROJECTILE.get(), living, level, itemStack, (ItemStack)null);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(itemStack));
        this.entityData.set(ID_FOIL, itemStack.hasFoil());
    }

    public PrimitiveSpearProjectileEntity(Level level, double v, double v1, double v2, ItemStack itemStack) {
        super(OWEntityRegistry.PRIMITIVE_SPEAR_PROJECTILE.get(), v, v1, v2, level, itemStack, itemStack);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(itemStack));
        this.entityData.set(ID_FOIL, itemStack.hasFoil());
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte)0);
        builder.define(ID_FOIL, false);
    }

    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity $$0 = this.getOwner();
        int $$1 = (Byte)this.entityData.get(ID_LOYALTY);
        if ($$1 > 0 && (this.dealtDamage || this.isNoPhysics()) && $$0 != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 $$2 = $$0.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + $$2.y * 0.015 * (double)$$1, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double $$3 = 0.05 * (double)$$1;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add($$2.normalize().scale($$3)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnTridentTickCount;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity $$0 = this.getOwner();
        if ($$0 != null && $$0.isAlive()) {
            return !($$0 instanceof ServerPlayer) || !$$0.isSpectator();
        } else {
            return false;
        }
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec4) {
        return this.dealtDamage ? null : super.findHitEntity(vec3, vec4);
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        Entity $$1 = hitResult.getEntity();
        float $$2 = 4.0F;
        Entity $$3 = this.getOwner();

        DamageSource $$4 = this.damageSources().trident(this, (Entity) ($$3 == null ? this : $$3));

        Level var7 = this.level();
        if (var7 instanceof ServerLevel $$5) {
            $$2 = EnchantmentHelper.modifyDamage($$5, this.getWeaponItem(), $$1, $$4, $$2);
        }

        this.dealtDamage = true;

        boolean wasBlocking = false;
        if ($$1 instanceof LivingEntity livingEntity && livingEntity.isBlocking()) {
            wasBlocking = true;
            livingEntity.stopUsingItem();
        }

        if ($$1.hurt($$4, $$2)) {
            if ($$1.getType() == EntityType.ENDERMAN) {
                return;
            }

            var7 = this.level();
            if (var7 instanceof ServerLevel) {
                ServerLevel $$6 = (ServerLevel) var7;
                EnchantmentHelper.doPostAttackEffectsWithItemSource($$6, $$1, $$4, this.getWeaponItem());
            }

            if ($$1 instanceof LivingEntity) {
                LivingEntity $$7 = (LivingEntity) $$1;
                this.doKnockback($$7, $$4);
                this.doPostHurtEffects($$7);
            }
        }


        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(OWSounds.SPEAR_HIT.get(), 1.0F, 1.0F);

    }

    protected void hitBlockEnchantmentEffects(ServerLevel serverLevel, BlockHitResult hitResult, ItemStack stack) {
        Vec3 $$3 = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        Entity var6 = this.getOwner();
        LivingEntity var10002;
        if (var6 instanceof LivingEntity $$4) {
            var10002 = $$4;
        } else {
            var10002 = null;
        }

        EnchantmentHelper.onHitBlock(serverLevel, stack, var10002, this, (EquipmentSlot)null, $$3, serverLevel.getBlockState(hitResult.getBlockPos()), (p_343806_) -> this.kill());
    }

    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(OWItems.PRIMITIVE_SPEAR.get());
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return OWSounds.SPEAR_HIT.get();
    }

    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(this.getPickupItemStackOrigin()));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyaltyFromItem(ItemStack itemStack) {
        Level var3 = this.level();
        if (var3 instanceof ServerLevel $$1) {
            return (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration($$1, itemStack, this), 0, 127);
        } else {
            return 0;
        }
    }

    public void tickDespawn() {
        int $$0 = (Byte)this.entityData.get(ID_LOYALTY);
        if (this.pickup != Pickup.ALLOWED || $$0 <= 0) {
            super.tickDespawn();
        }

    }

    protected float getWaterInertia() {
        return 0.5F;
    }

    public boolean shouldRender(double v, double v1, double v2) {
        return true;
    }

    static {
        ID_LOYALTY = SynchedEntityData.defineId(PrimitiveSpearProjectileEntity.class, EntityDataSerializers.BYTE);
        ID_FOIL = SynchedEntityData.defineId(PrimitiveSpearProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    }
}
