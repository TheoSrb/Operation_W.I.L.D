package net.tiew.operationWild.entity.custom.misc;

import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.utils.OWUtils;

public class VenomousArrow extends AbstractArrow {
    public Vec2 groundedOffset;

    public VenomousArrow(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public VenomousArrow(LivingEntity shooter, Level level) {
        super(OWEntityRegistry.VENOMOUS_ARROW.get(), shooter, level, new ItemStack(OWItems.VENOMOUS_ARROW.get()), null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(OWItems.VENOMOUS_ARROW.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        int effectDuration = (int) OWUtils.generateRandomInterval(200, 300);
        Entity entity = result.getEntity();
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            if (entity != null && entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), effectDuration, 0));
            }
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
