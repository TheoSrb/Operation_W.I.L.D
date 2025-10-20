package net.tiew.operationWild.entity.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class only manages methods and functions that are useful and necessary for the proper functioning of Crocodile's artificial intelligence.
 * It is a complementary class to the latter.
 */

public class CrocodileBehaviorHandler {

    private CrocodileEntity crocodile;

    public CrocodileBehaviorHandler(CrocodileEntity crocodile) {
        this.crocodile = crocodile;
    }

    public boolean canPlayIdleAnimation() {
        return crocodile.getTarget() == null && !crocodile.isChargingMouth() && !crocodile.isNapping() && !crocodile.isMoving() && !crocodile.isVehicle() && !crocodile.isInWater();
    }

    public boolean canGrowl() {
        return canPlayIdleAnimation();
    }

    public boolean canGrunt() {
        return canPlayIdleAnimation();
    }

    public boolean isAnyIdleAnimationPlaying() {
        return crocodile.growlsAnimationState.isStarted() || crocodile.gruntAnimationState.isStarted();
    }

    public void makeBigHurt(float attackDamage, SoundEvent sound, double width, double height, double reach) {
        double yaw = Math.toRadians(this.crocodile.getYRot());
        double centerX = this.crocodile.getX() - Math.sin(yaw) * reach;
        double centerZ = this.crocodile.getZ() + Math.cos(yaw) * reach;
        double centerY = this.crocodile.getY() + 0.5;

        AABB attackBox = new AABB(
                centerX - width / 2, centerY - height, centerZ - width / 2,
                centerX + width / 2, centerY + height, centerZ + width / 2
        );

        List<LivingEntity> targets = this.crocodile.level().getEntitiesOfClass(
                LivingEntity.class,
                attackBox,
                entity -> entity != crocodile && !this.crocodile.isAlliedTo(entity)
        );

        for (LivingEntity target : targets) {
            if (target.getHealth() < target.getMaxHealth() * 0.3f) {
                this.crocodile.killedEntity((ServerLevel) this.crocodile.level(), target);
            } else target.hurt(this.crocodile.damageSources().mobAttack(this.crocodile), attackDamage);

            Vec3 knockbackDirection = target.position().subtract(this.crocodile.position()).normalize();
            Vec3 knockback = knockbackDirection.scale(2.5);
            target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, knockback.y * 0.5, knockback.z));

            target.addEffect(new MobEffectInstance(OWEffects.FRACTURE.getDelegate(), OWUtils.generateExponentialExp(150, 300), 0));
        }

        this.crocodile.setChargingMouthTimer(0);
        this.crocodile.setChargingMouth(false);
        this.crocodile.level().playSound(null, this.crocodile.getX(), this.crocodile.getY(), this.crocodile.getZ(), sound, SoundSource.NEUTRAL, 1.0F, 0.75f);
    }
}
