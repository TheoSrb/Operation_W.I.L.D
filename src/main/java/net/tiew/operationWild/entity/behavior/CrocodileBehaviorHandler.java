package net.tiew.operationWild.entity.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

import static net.tiew.operationWild.core.OWUtils.RANDOM;

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
        return crocodile.getTarget() == null && !crocodile.isChargingMouth() && !crocodile.isNapping() && !crocodile.isNapping() && !crocodile.isMoving() && !crocodile.isVehicle() && !crocodile.isInWater();
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

    public void trampleLilyPads(BlockPos pos) {
        int radius = 2;

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                BlockPos checkPos = pos.offset(xOffset, 0, zOffset);
                checkAndBreakLilyPad(checkPos);
            }
        }
    }

    private void checkAndBreakLilyPad(BlockPos pos) {
        BlockState blockState = crocodile.level().getBlockState(pos);

        if (blockState.getBlock() instanceof WaterlilyBlock) {
            crocodile.level().destroyBlock(pos, false);
        }
    }

    public boolean isReadyForTaming() {
        return this.crocodile.getSacrificesUnity() >= 100;
    }

    public BlockPos findNearestWaterSource(int searchRadius) {
        BlockPos crocodilePos = this.crocodile.blockPosition();
        BlockPos bestWaterPos = null;
        double bestScore = Double.MIN_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                crocodilePos.offset(-searchRadius, -searchRadius, -searchRadius),
                crocodilePos.offset(searchRadius, searchRadius, searchRadius))) {

            if (this.crocodile.level().getFluidState(pos).is(Fluids.WATER)) {
                if (isValidWaterArea(pos)) {
                    double distance = Math.sqrt(crocodilePos.distSqr(pos));

                    int waterDepth = calculateWaterDepth(pos);

                    int waterDensity = countWaterBlocksAround(pos, 3);

                    double score = (waterDepth * 2.0) + (waterDensity * 0.5) - (distance * 0.1);

                    if (score > bestScore) {
                        bestScore = score;
                        bestWaterPos = pos.immutable();
                    }
                }
            }
        }

        return bestWaterPos;
    }

    private int calculateWaterDepth(BlockPos pos) {
        int depth = 0;
        BlockPos checkPos = pos.mutable();

        for (int i = 0; i < 10; i++) {
            checkPos = checkPos.below();
            if (this.crocodile.level().getFluidState(checkPos).is(Fluids.WATER)) {
                depth++;
            } else {
                break;
            }
        }

        return depth;
    }

    private int countWaterBlocksAround(BlockPos pos, int radius) {
        int count = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-radius, -1, -radius),
                pos.offset(radius, 1, radius))) {

            if (this.crocodile.level().getFluidState(checkPos).is(Fluids.WATER)) {
                count++;
            }
        }

        return count;
    }

    public boolean isValidWaterArea(BlockPos center) {
        int minWaterBlocks = 4;
        int waterCount = 0;
        int checkRadius = 2;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-checkRadius, -1, -checkRadius),
                center.offset(checkRadius, 1, checkRadius))) {

            if (this.crocodile.level().getFluidState(pos).is(Fluids.WATER)) {
                waterCount++;
                if (waterCount >= minWaterBlocks) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isNearOfWater(int searchRadius) {
        boolean waterNearby = false;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = this.crocodile.blockPosition().offset(x, y, z);
                    BlockState state = this.crocodile.level().getBlockState(checkPos);

                    if (state.getBlock() == Blocks.WATER) {
                        waterNearby = true;
                        break;
                    }
                }
                if (waterNearby) break;
            }
            if (waterNearby) break;
        }

        return waterNearby;
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

        Entity passenger = this.crocodile.getControllingPassenger();

        List<LivingEntity> targets = this.crocodile.level().getEntitiesOfClass(
                LivingEntity.class,
                attackBox,
                entity -> entity != crocodile && !this.crocodile.isAlliedTo(entity) && entity != passenger
        );

        for (LivingEntity target : targets) {
            if (target.getHealth() < target.getMaxHealth() * 0.3f) {
                this.crocodile.killedEntity((ServerLevel) this.crocodile.level(), target);
            } else target.hurt(this.crocodile.damageSources().mobAttack(this.crocodile), attackDamage);

            Vec3 knockbackDirection = target.position().subtract(this.crocodile.position()).normalize();
            Vec3 knockback = knockbackDirection.scale(2.5);
            target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, knockback.y * 0.5, knockback.z));

            target.addEffect(new MobEffectInstance(OWEffects.FRACTURE.getDelegate(), OWUtils.generateExponentialExp(150, 300), 0));

            this.crocodile.hurtAfterCombo(target, crocodile.getComboAttack());
        }

        this.crocodile.setChargingMouthTimer(0);
        this.crocodile.setChargingMouth(false);
        this.crocodile.level().playSound(null, this.crocodile.getX(), this.crocodile.getY(), this.crocodile.getZ(), sound, SoundSource.NEUTRAL, 1.0F, 0.75f);

        float pitch = (float) (OWUtils.generateRandomInterval(1.1, 1.25));
        SoundEvent sound2 = RANDOM(2) ? OWSounds.CROCODILE_HIT_1.get() : OWSounds.CROCODILE_HIT_2.get();
        crocodile.level().playSound(null, crocodile.getX(), crocodile.getY(), crocodile.getZ(), sound2, SoundSource.HOSTILE, 1.0f, pitch);
    }
}
