package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

public class CrocodileGoToWaterWithFoodGoal extends Goal {

    private final CrocodileEntity crocodile;

    private BlockPos targetPos = null;

    private int timer = 0;

    public CrocodileGoToWaterWithFoodGoal(CrocodileEntity crocodile) {
        this.crocodile = crocodile;
    }

    @Override
    public void tick() {
        super.tick();

        if (targetPos != null && !this.crocodile.isInWater()) {
            this.crocodile.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 3.0f);
        }

        if (this.crocodile.isInWater()) {
            timer++;

            Vec3 lookDirection = this.crocodile.getLookAngle();
            this.crocodile.setDeltaMovement(lookDirection.x * 0.2f, -0.05, lookDirection.z * 0.2f);

            if (timer % 20 == 0) {
                this.crocodile.getGrabbedTarget().hurt(this.crocodile.damageSources().mobAttack(this.crocodile), 5);
            }
        } else {
            if (crocodile.tickCount % 20 == 0) {
                this.crocodile.getGrabbedTarget().hurt(this.crocodile.damageSources().mobAttack(this.crocodile), 3);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        targetPos = findNearestWaterSource(20);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.crocodile.hasSomeoneInHisMouth() && this.crocodile.getHealth() >= 10;
    }

    @Override
    public boolean canUse() {
        return this.crocodile.hasSomeoneInHisMouth();
    }

    private BlockPos findNearestWaterSource(int searchRadius) {
        BlockPos crocodilePos = this.crocodile.blockPosition();
        BlockPos nearestWater = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                crocodilePos.offset(-searchRadius, -searchRadius, -searchRadius),
                crocodilePos.offset(searchRadius, searchRadius, searchRadius))) {

            if (this.crocodile.level().getBlockState(pos).getFluidState().is(Fluids.WATER)) {
                if (isValidWaterArea(pos)) {
                    double distance = crocodilePos.distSqr(pos);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestWater = pos.immutable();
                    }
                }
            }
        }

        return nearestWater;
    }

    private boolean isValidWaterArea(BlockPos center) {
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
}
