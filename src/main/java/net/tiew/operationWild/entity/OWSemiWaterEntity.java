package net.tiew.operationWild.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class OWSemiWaterEntity extends OWEntity {

    public OWSemiWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public abstract int getMaxDepth();

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1F, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));

            int currentAir = this.getAirSupply();
            int maxAir = this.getMaxAirSupply();
            double airPercentage = (double) currentAir / maxAir * 100.0;

            int depth = (int) (this.level().getSeaLevel() - this.getY());

            if (airPercentage < 10.0 || depth >= getMaxDepth()) {
                if (!this.isAtSurface()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.015D, 0.0D));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        switchNavigation();
        handleUnderwaterMovement();
    }

    protected boolean isAtSurface() {
        BlockPos posAbove = this.blockPosition().above();
        return !this.level().getFluidState(posAbove).isEmpty() == false || this.level().isEmptyBlock(posAbove);
    }

    protected void switchNavigation() {
        if (this.isInWater()) {
            this.navigation = new WaterBoundPathNavigation(this, this.level());
        } else {
            this.navigation = new GroundPathNavigation(this, this.level());
        }
    }

    protected void handleUnderwaterMovement() {
        if (this.isInWater() && this.isEffectiveAi() && !this.isVehicle()) {
            int currentAir = this.getAirSupply();
            int maxAir = this.getMaxAirSupply();
            double airPercentage = (double) currentAir / maxAir * 100.0;

            if (airPercentage < 10.0 && !isAtSurface()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.1D, 0.0D));
                return;
            }

            if (this.getTarget() != null) {
                LivingEntity target = this.getTarget();
                double yDiff = target.getY() - this.getY();

                if (yDiff > 1.0D) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
                } else if (yDiff < -1.0D) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
                }
            } else if (this.getNavigation().getPath() != null) {
                BlockPos targetPos = this.getNavigation().getTargetPos();
                if (targetPos != null) {
                    double yDiff = targetPos.getY() - this.getY();

                    if (yDiff > 0.5D) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.03D, 0.0D));
                    } else if (yDiff < -0.5D) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
                    }
                }
            }

            double wave = Math.sin(this.tickCount * 0.05) * 0.005;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, wave, 0.0D));

            if (this.getTarget() == null && this.getNavigation().getPath() == null) {
                Vec3 lookAngle = this.getLookAngle();
                this.setDeltaMovement(this.getDeltaMovement().add(
                        lookAngle.x * 0.01,
                        0.0D,
                        lookAngle.z * 0.01
                ));
            }
        }
        this.handleUnderwaterCollisions();
    }

    protected void handleUnderwaterCollisions() {
        if (this.isInWater() && this.horizontalCollision) {
            float randomYaw = (float) (this.getRandom().nextDouble() * 360.0 - 180.0);
            this.setYRot(this.getYRot() + randomYaw);
            this.yRotO = this.getYRot();

            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.02D, 0.0D));

            Vec3 newLookAngle = this.getLookAngle();
            this.setDeltaMovement(
                    newLookAngle.x * 0.15,
                    this.getDeltaMovement().y,
                    newLookAngle.z * 0.15
            );
        }
    }
}
