package net.tiew.operationWild.entity.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.particle.OWParticles;

import java.util.EnumSet;

public class NapGoal extends Goal {

    private final OWEntity entity;
    private final float wantNapMultiplier;
    private final boolean conditionToWork;
    /**
     * Autorise la sieste une fois la bete apprivoisee.
     *
     * <p>Faux partout ailleurs, et c'est voulu : une monture qui s'endort sous son cavalier serait
     * une nuisance. Le panda roux, lui, ne se monte pas — il s'endort au sol comme il le ferait a
     * l'etat sauvage, et rien de ce que fait son maitre n'en depend.</p>
     */
    private final boolean allowWhenTamed;

    private int NAP_DURATION_MAX;
    private final int napTimerMax;
    private int napTimer = 0;
    private int napTickCounter = 0;
    private boolean shouldStop = false;

    public NapGoal(OWEntity entity, float wantNapMultiplier, int napTimerMax, boolean conditionToWork) {
        this(entity, wantNapMultiplier, napTimerMax, conditionToWork, false);
    }

    public NapGoal(OWEntity entity, float wantNapMultiplier, int napTimerMax, boolean conditionToWork,
                   boolean allowWhenTamed) {
        this.entity = entity;
        this.wantNapMultiplier = wantNapMultiplier;
        this.napTimerMax = napTimerMax;
        this.conditionToWork = conditionToWork;
        this.allowWhenTamed = allowWhenTamed;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
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
                return;
            }

            handleNappingEffects();

            entity.setYRot(entity.getYRot());
            entity.yRotO = entity.getYRot();
            entity.setYHeadRot(entity.getYRot());
        } else {
            shouldStop = true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (entity instanceof KodiakEntity kodiak) {
            if (kodiak.isRolling() || kodiak.isSniffing() || kodiak.isCatchingSalmon() || kodiak.isHungry()) return false;
        }
        return !shouldStop && conditionToWork && entity.isNapping() && mayNapWhileTame() && entity.getTarget() == null;
    }

    @Override
    public void start() {
        super.start();
        generateMaxNapTimer();
        napTickCounter = -1;
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
        startAwaken();
        napTimer = 0;
        napTickCounter = 0;
        shouldStop = false;
    }

    @Override
    public boolean canUse() {
        if (entity instanceof KodiakEntity kodiak) {
            if (kodiak.isRolling() || kodiak.isSniffing() || kodiak.isCatchingSalmon() || kodiak.isHungry()) return false;
        }
        if (entity instanceof TigerEntity tiger) {
            if (tiger.level().isNight()) return false;
        }
        return entity.getRandom().nextInt((int) (600 / wantNapMultiplier)) == 0 && canNap() && conditionToWork && mayNapWhileTame() && entity.getTarget() == null;
    }

    private boolean mayNapWhileTame() {
        return allowWhenTamed || !entity.isTame();
    }

    private boolean canNap() {
        return mayNapWhileTame()
                && entity.canStartNap()
                && !entity.isDeadOrDying()
                && entity.getTarget() == null
                // Une bete portee ou montee ne s'endort pas : elle n'a ni sol sous elle ni maitrise
                // de sa position, et la pose de sieste ecraserait celle de son perchoir.
                && !entity.isPassenger()
                && !entity.isInWater()
                && entity.onGround()
                && entity.getHealth() > (entity.getMaxHealth() * 0.3f);
    }

    private void handleNappingEffects() {
        entity.setTarget(null);

        int cycle = napTickCounter % 23;
        if (cycle < 3) {
            Vec3 lookDirection = entity.getLookAngle();
            double entityX = entity.getX();
            double entityY = entity.getY() + entity.napParticleHeight();
            double entityZ = entity.getZ();

            Vec3 rightDirection = new Vec3(-lookDirection.z, 0, lookDirection.x).normalize();
            double rightOffset = 0.0;

            double forward = entity.napParticleForward();
            double fixedX = entityX + lookDirection.x * forward + rightDirection.x * rightOffset;
            double fixedY = entityY;
            double fixedZ = entityZ + lookDirection.z * forward + rightDirection.z * rightOffset;

            if (!entity.level().isClientSide()) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(OWParticles.NAP_PARTICLES.get(),
                            fixedX, fixedY, fixedZ,
                            1, 0.1, 0.1, 0.1, 0.0);
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