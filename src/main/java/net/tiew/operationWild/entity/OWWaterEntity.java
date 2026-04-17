package net.tiew.operationWild.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.tiew.operationWild.entity.goals.AquaticMoveController;
import net.tiew.operationWild.entity.goals.AquaticPathNavigator;

import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;

public class OWWaterEntity extends OWEntity {

    protected OWWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.moveControl = new AquaticMoveController(this, 1F);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    protected PathNavigation createNavigation(Level worldIn) {
        return new AquaticPathNavigator(this, worldIn);
    }

    public void travel(Vec3 travelVector) {
        if (isSleeping() || isSitting()) return;

        if (this.isEffectiveAi() && this.isInWater() && !isSleeping()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void onInsideBubbleColumn(boolean b) {
        this.resetFallDistance();
    }

    @Override
    public void onAboveBubbleCol(boolean b) {
    }

    public void aiStep() {
        if (!this.isInWater() && this.onGround() && this.verticalCollision) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), (double)0.5F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F)));
            this.setOnGround(false);
            this.hasImpulse = true;
            this.makeSound(SoundEvents.SALMON_FLOP);
        }

        super.aiStep();
    }
}
