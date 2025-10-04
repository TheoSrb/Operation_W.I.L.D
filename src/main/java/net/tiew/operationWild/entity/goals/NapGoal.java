package net.tiew.operationWild.entity.goals;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
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
    private int napTickCounter = 0;
    private boolean shouldStop = false;

    public NapGoal(OWEntity entity, float wantNapMultiplier, int napTimerMax, boolean conditionToWork) {
        this.entity = entity;
        this.wantNapMultiplier = wantNapMultiplier;
        this.napTimerMax = napTimerMax;
        this.conditionToWork = conditionToWork;
    }

    @Override
    public void tick() {
        super.tick();

        if (shouldStop) {
            startAwaken();
            return;
        }

        if (entity.isNapping()) {
            napTimer--;
            napTickCounter++;

            if (entity.onGround()) {
                entity.setDeltaMovement(0, 0, 0);
            }

            if (napTimer <= 0) {
                startAwaken();
                shouldStop = true;
            }

            handleNappingEffects();
        } else {
            shouldStop = true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (entity instanceof KodiakEntity kodiak) {
            if (kodiak.isRolling() || kodiak.isSniffing() || kodiak.isCatchingSalmon() || kodiak.isHungry()) return false;
        }
        return !shouldStop && conditionToWork && entity.isNapping() && !entity.isTame() && entity.getTarget() == null;
    }

    @Override
    public void start() {
        super.start();
        generateMaxNapTimer();
        napTickCounter = 0;
        shouldStop = false;
        startNapping();

        entity.setYRot(entity.getYRot());
        entity.yRotO = entity.getYRot();
        entity.setYHeadRot(entity.getYRot());

        if (entity instanceof KodiakEntity kodiak) {
            if (kodiak.getFoodPick() != null && !kodiak.getFoodPick().isEmpty()) {
                kodiak.kodiakBehaviorHandler.eatFoodInHisMouth(kodiak.getFoodPick());
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        napTimer = 0;
        napTickCounter = 0;
        shouldStop = false;
    }

    @Override
    public boolean canUse() {
        if (entity instanceof KodiakEntity kodiak) {
            if (kodiak.isRolling() || kodiak.isSniffing() || kodiak.isCatchingSalmon() || kodiak.isHungry()) return false;
        }
        return entity.getRandom().nextInt((int) (600 / wantNapMultiplier)) == 0 && canNap() && conditionToWork && !entity.isTame() && entity.getTarget() == null;
    }

    private boolean canNap() {
        return !entity.isTame()
                && !entity.isDeadOrDying()
                && entity.getTarget() == null
                && !entity.isInWater()
                && entity.onGround()
                && entity.getHealth() > (entity.getMaxHealth() * 0.3f);
    }

    private void handleNappingEffects() {
        entity.setTarget(null);

        if (napTickCounter % 20 == 0) {
            Vec3 lookDirection = entity.getLookAngle();
            double entityX = entity.getX();
            double entityY = entity.getY() + 1.15;
            double entityZ = entity.getZ();
            double fixedX = entityX + lookDirection.x * 1.25;
            double fixedY = entityY;
            double fixedZ = entityZ + lookDirection.z * 1.25;

            if (!entity.level().isClientSide()) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(OWParticles.NAP_PARTICLES.get(),
                            fixedX, fixedY, fixedZ,
                            1,
                            0.1,
                            0.1,
                            0.1,
                            0.0);
                }
            } else {
                if (entity.level() instanceof ClientLevel clientLevel) {
                    for (int i = 0; i < 1; i++) {
                        double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.2;
                        double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.2;
                        double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.2;

                        clientLevel.addParticle(OWParticles.NAP_PARTICLES.get(),
                                fixedX + offsetX,
                                fixedY + offsetY,
                                fixedZ + offsetZ,
                                0.0, 0.0, 0.0);
                    }
                }
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