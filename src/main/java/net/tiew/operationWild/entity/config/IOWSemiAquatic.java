package net.tiew.operationWild.entity.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

public interface IOWSemiAquatic {

    default boolean isAtSurface(OWEntity entity) {
        BlockPos posAbove = entity.blockPosition().above();
        return !entity.level().getFluidState(posAbove).isEmpty() == false ||
                entity.level().isEmptyBlock(posAbove);
    }

    default void handleUnderwaterMovement(OWEntity entity) {
        if (entity.isInWater() && entity.isEffectiveAi() && !entity.isVehicle()) {
            int currentAir = entity.getAirSupply();
            int maxAir = entity.getMaxAirSupply();
            double airPercentage = (double) currentAir / maxAir * 100.0;

            if (airPercentage < 10.0 && !isAtSurface(entity)) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.1D, 0.0D));
                return;
            }

            if (entity.getTarget() != null) {
                LivingEntity target = entity.getTarget();
                double yDiff = target.getY() - entity.getY();

                if (yDiff > 1.0D) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
                } else if (yDiff < -1.0D) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
                }
            } else if (entity.getNavigation().getPath() != null) {
                BlockPos targetPos = entity.getNavigation().getTargetPos();
                if (targetPos != null) {
                    double yDiff = targetPos.getY() - entity.getY();

                    if (yDiff > 0.5D) {
                        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.03D, 0.0D));
                    } else if (yDiff < -0.5D) {
                        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
                    }
                }
            }

            double wave = Math.sin(entity.tickCount * 0.05) * 0.005;
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, wave, 0.0D));

            if (entity.getTarget() == null && entity.getNavigation().getPath() == null) {
                Vec3 lookAngle = entity.getLookAngle();
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        lookAngle.x * 0.01,
                        0.0D,
                        lookAngle.z * 0.01
                ));
            }
        }
    }
}
