package net.tiew.operationWild.entity.goals;

import com.google.common.base.Enums;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.particle.OWParticles;

import java.util.EnumSet;

public class NapGoal extends Goal {

    private final OWEntity entity;
    private final float wantNapMultiplier;
    private final boolean conditionToWork;

    private int NAP_DURATION_MAX;
    private final int napTimerMax;
    private int napTimer = 0;

    public NapGoal(OWEntity entity, float wantNapMultiplier, int napTimerMax, boolean conditionToWork) {
        this.entity = entity;
        this.wantNapMultiplier = wantNapMultiplier;
        this.napTimerMax = napTimerMax;
        this.conditionToWork = conditionToWork;

        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
    }

    @Override
    public void tick() {
        super.tick();

        if (entity.isNapping()) {
            napTimer--;

            if (napTimer <= 0) {
                startAwaken();
                stop();
            }


            handleNappingEffects();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return conditionToWork && napTimer > 0;
    }

    @Override
    public void start() {
        super.start();
        generateMaxNapTimer();
        startNapping();
    }

    @Override
    public void stop() {
        super.stop();
        napTimer = 0;
    }

    @Override
    public boolean canUse() {
        return entity.getRandom().nextInt((int) (800 / wantNapMultiplier)) == 0 && canNap() && conditionToWork;
    }

    private boolean canNap() {
        if (entity instanceof KodiakEntity kodiak && !kodiak.getFoodPick().isEmpty()) return false;
        return !entity.isTame()
                && !entity.isDeadOrDying()
                && entity.getTarget() == null
                && !entity.isInWater()
                && entity.onGround()
                && entity.getHealth() > (entity.getMaxHealth() * 0.3f);
    }

    private void handleNappingEffects() {
        entity.setTarget(null);
        if (!entity.level().isClientSide && entity.tickCount % 20 == 0) {
            Vec3 lookDirection = entity.getLookAngle();
            double entityX = entity.getX();
            double entityY = entity.getY() + 1.15;
            double entityZ = entity.getZ();
            double fixedX = entityX + lookDirection.x * 1.25;
            double fixedY = entityY;
            double fixedZ = entityZ + lookDirection.z * 1.25;

            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(OWParticles.NAP_PARTICLES.get(),
                        fixedX, fixedY, fixedZ,
                        1,
                        0.1,
                        0.1,
                        0.1,
                        0.0);
            }
        }
    }
    private void generateMaxNapTimer() {
        NAP_DURATION_MAX = napTimerMax + entity.getRandom().nextInt(napTimerMax);
        napTimer = NAP_DURATION_MAX;
    }

    private void startNapping() {
        entity.setNap(true);
    }

    private void startAwaken() {
        entity.setNap(false);
    }
}
