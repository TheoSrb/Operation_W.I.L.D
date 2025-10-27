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
            if (this.crocodile.canGrabOnLand) {
                Vec3 crocodilePos = this.crocodile.position();
                Vec3 waterPos = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                Vec3 directionToWater = waterPos.subtract(crocodilePos).normalize();

                if (this.crocodile.getGrabbedTarget() != null) {
                    this.crocodile.getLookControl().setLookAt(this.crocodile.getGrabbedTarget(), 30.0F, 30.0F);
                }

                Vec3 movement = directionToWater.scale(0.075);
                this.crocodile.setDeltaMovement(movement.x, this.crocodile.getDeltaMovement().y, movement.z);
                this.crocodile.setOnGround(true);
            } else {
                this.crocodile.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 3.0f);
            }
        }

        if (this.crocodile.isInWater()) {
            timer++;

            this.crocodile.getNavigation().stop();
        } else {
            if (crocodile.tickCount % 20 == 0) {
                this.crocodile.getGrabbedTarget().hurt(this.crocodile.damageSources().mobAttack(this.crocodile), 2);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        targetPos = crocodile.crocodileBehaviorHandler.findNearestWaterSource(20);
    }

    @Override
    public void stop() {
        super.stop();
        crocodile.canGrabOnLand = false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.crocodile.hasGrabSomething() && this.crocodile.getHealth() >= 10;
    }

    @Override
    public boolean canUse() {
        return this.crocodile.hasGrabSomething();
    }
}