package net.tiew.operationWild.entity.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

public class JumpOutOfTheWaterGoal extends Goal {

    private final OWEntity entity;

    public JumpOutOfTheWaterGoal(OWEntity entity) {
        this.entity = entity;
    }

    @Override
    public void start() {
        super.start();
        if (this.entity instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() != null) return;

        entity.hasImpulse = true;

        if (entity.getTarget() != null) {
            Vec3 targetPos = entity.getTarget().position();
            Vec3 entityPos = entity.position();

            double dx = targetPos.x - entityPos.x;
            double dz = targetPos.z - entityPos.z;
            double distance = Math.sqrt(dx * dx + dz * dz);

            entity.setDeltaMovement(dx / distance * 0.8, 0.5, dz / distance * 0.8);
        }

        entity.playSound(SoundEvents.GENERIC_SWIM);
    }

    @Override
    public void tick() {
        super.tick();
        if (entity.isInWater()) {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.99));
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.getTarget() != null && entity.distanceTo(entity.getTarget()) <= 1;
    }

    @Override
    public boolean canUse() {
        return entity.getVehicle() == null && entity.getControllingPassenger() == null && entity.getTarget() != null && entity.isInWater() &&
                entity.getTarget().onGround() && entity.distanceTo(entity.getTarget()) <= 6 && !entity.isTame();
    }
}
