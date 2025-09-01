package net.tiew.operationWild.entity.misc;

import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;

public class TranquilizerWoodenStinger extends AbstractArrow {
    public Vec2 groundedOffset;
    public int tranquilizerEffectiveness = 75;

    public TranquilizerWoodenStinger(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setBaseDamage(0);
    }

    public TranquilizerWoodenStinger(LivingEntity shooter, Level level) {
        super(OWEntityRegistry.WOODEN_STINGER.get(), shooter, level, new ItemStack(OWItems.WOODEN_STINGER.get()), null);
        this.setBaseDamage(0);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(OWItems.WOODEN_STINGER.get());
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource damageSource) {
        super.doKnockback(target, damageSource);
        Vec3 currentVelocity = target.getDeltaMovement();
        target.setDeltaMovement(currentVelocity.scale(0.25));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if(result.getDirection() == Direction.SOUTH) {
            groundedOffset = new Vec2(215f,180f);
        }
        if(result.getDirection() == Direction.NORTH) {
            groundedOffset = new Vec2(215f, 0f);
        }
        if(result.getDirection() == Direction.EAST) {
            groundedOffset = new Vec2(215f,-90f);
        }
        if(result.getDirection() == Direction.WEST) {
            groundedOffset = new Vec2(215f,90f);
        }

        if(result.getDirection() == Direction.DOWN) {
            groundedOffset = new Vec2(115f,180f);
        }
        if(result.getDirection() == Direction.UP) {
            groundedOffset = new Vec2(285f,180f);
        }
    }
}
