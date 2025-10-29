package net.tiew.operationWild.entity.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

public class JumpOutOfTheWaterGoal extends Goal {

    private final OWEntity entity;
    private static final int COOLDOWN_TICKS = 200;
    private int cooldownTimer = 0;

    public JumpOutOfTheWaterGoal(OWEntity entity) {
        this.entity = entity;
    }

    @Override
    public void start() {
        super.start();
        if (this.entity instanceof CrocodileEntity crocodile) {
            if (crocodile.getGrabbedTarget() != null) {
                return;
            }
        }

        entity.hasImpulse = true;

        if (entity.getTarget() != null) {
            Vec3 targetPos = entity.getTarget().position();
            Vec3 entityPos = entity.position();

            double dx = targetPos.x - entityPos.x;
            double dz = targetPos.z - entityPos.z;
            double distance = Math.sqrt(dx * dx + dz * dz);

            entity.setDeltaMovement(dx / distance * 0.7, 0.55, dz / distance * 0.7);

            if (entity instanceof CrocodileEntity crocodile) {
                crocodile.canGrabOnLand = true;
            }
        }

        entity.playSound(SoundEvents.GENERIC_SWIM);

        cooldownTimer = COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        super.tick();

        if (cooldownTimer > 0) {
            cooldownTimer--;
        }

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
        if (cooldownTimer > 0) {
            return false;
        }

        return entity.getVehicle() == null && entity.getControllingPassenger() == null && entity.getTarget() != null && entity.isInWater() &&
                (entity.getTarget().onGround() && !entity.getTarget().isInWater()) && entity.distanceTo(entity.getTarget()) <= 6 && !entity.isTame();
    }
}