package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.particle.OWParticles;

import java.util.List;

public class CrocodileNapGoal extends Goal {

    private final CrocodileEntity crocodile;
    private final float wantNapMultiplier;
    private final boolean conditionToWork;

    private int NAP_DURATION_MAX;
    private final int napTimerMax;
    private int napTimer = 0;
    private int napTickCounter = 0;
    private boolean shouldStop = false;

    private boolean isFakeNap = false;

    private float yaw = 0.0f;

    public CrocodileNapGoal(CrocodileEntity crocodile, float wantNapMultiplier, int napTimerMax, boolean conditionToWork) {
        this.crocodile = crocodile;
        this.wantNapMultiplier = wantNapMultiplier;
        this.napTimerMax = napTimerMax;
        this.conditionToWork = conditionToWork;
    }

    @Override
    public void tick() {
        super.tick();

        if (shouldStop) {
            startAwaken();
            stop();
            return;
        }

        if (crocodile.isNapping()) {
            if (isFakeNap) {
                List<LivingEntity> entities = this.crocodile.level().getEntitiesOfClass(LivingEntity.class, this.crocodile.getBoundingBox().inflate(4));

                for (LivingEntity entity : entities) {
                    if (entity instanceof Player player && player.isCreative()) continue;
                    if (entity.isAlive() && entity.getHealth() < 50 && !(entity instanceof CrocodileEntity)) {
                        startAwaken();
                        this.crocodile.setTarget(entity);
                        this.crocodile.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 2, false, false, false));
                        stop();
                        break;
                    }
                }
            }

            napTimer--;
            napTickCounter++;

            if (crocodile.onGround()) {
                crocodile.setDeltaMovement(0, 0, 0);
            }

            if (napTimer <= 0) {
                startAwaken();
                stop();
                shouldStop = true;
            }

            if (!isFakeNap) {
                handleNappingEffects();
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (crocodile.isBaby()) return false;
        boolean shouldStopCheck = !shouldStop;
        boolean conditionCheck = conditionToWork;
        boolean timerCheck = napTimer > 0;
        boolean tameCheck = !crocodile.isTame();
        boolean targetCheck = crocodile.getTarget() == null;

        return shouldStopCheck && conditionCheck && timerCheck && tameCheck && targetCheck && !this.crocodile.isInWater();
    }

    @Override
    public void start() {
        super.start();
        generateMaxNapTimer();
        napTickCounter = 0;
        shouldStop = false;
        startNapping();

        crocodile.setYRot(crocodile.getYRot());
        crocodile.yRotO = crocodile.getYRot();
        crocodile.setYHeadRot(crocodile.getYRot());
    }

    @Override
    public void stop() {
        super.stop();
        napTimer = 0;
        napTickCounter = 0;
        shouldStop = false;
        startAwaken();
    }

    @Override
    public boolean canUse() {
        if (crocodile.isBaby()) return false;
        int randomValue = crocodile.getRandom().nextInt((int) (500 / wantNapMultiplier));
        boolean randomCheck = randomValue == 0;
        boolean canNapCheck = canNap();
        boolean conditionCheck = conditionToWork;
        boolean tameCheck = !crocodile.isTame();
        boolean targetCheck = crocodile.getTarget() == null;
        boolean dayCheck = this.crocodile.level().isDay();

        return randomCheck && canNapCheck && conditionCheck && tameCheck && targetCheck && dayCheck;
    }

    private boolean canNap() {
        boolean result = !crocodile.isTame()
                && !crocodile.isDeadOrDying()
                && crocodile.getTarget() == null
                && !crocodile.isInWater()
                && crocodile.onGround()
                && crocodile.getHealth() > (crocodile.getMaxHealth() * 0.3f);

        return result;
    }

    private void handleNappingEffects() {
        crocodile.setTarget(null);

        crocodile.setDeltaMovement(Vec3.ZERO);
        crocodile.setYRot(yaw);
        crocodile.yRotO = yaw;
        crocodile.setYHeadRot(yaw);
        crocodile.yHeadRotO = yaw;
        crocodile.setXRot(0);
        crocodile.xRotO = 0;

        if (napTickCounter % 20 == 0) {
            Vec3 lookDirection = crocodile.getLookAngle();
            double entityX = crocodile.getX();
            double entityY = crocodile.getY() + 1.15;
            double entityZ = crocodile.getZ();

            Vec3 rightDirection = new Vec3(-lookDirection.z, 0, lookDirection.x).normalize();

            double rightOffset = 0.0;

            double fixedX = entityX + lookDirection.x * 1.25 + rightDirection.x * rightOffset;
            double fixedY = entityY;
            double fixedZ = entityZ + lookDirection.z * 1.25 + rightDirection.z * rightOffset;

            if (!crocodile.level().isClientSide()) {
                if (crocodile.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(OWParticles.NAP_PARTICLES.get(),
                            fixedX, fixedY, fixedZ,
                            1,
                            0.1,
                            0.1,
                            0.1,
                            0.0);
                }
            } else {
                if (crocodile.level() instanceof ClientLevel clientLevel) {
                    for (int i = 0; i < 1; i++) {
                        double offsetX = (crocodile.getRandom().nextDouble() - 0.5) * 0.2;
                        double offsetY = (crocodile.getRandom().nextDouble() - 0.5) * 0.2;
                        double offsetZ = (crocodile.getRandom().nextDouble() - 0.5) * 0.2;

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
        NAP_DURATION_MAX = napTimerMax + crocodile.getRandom().nextInt(napTimerMax);
        napTimer = NAP_DURATION_MAX;
    }

    private void startNapping() {
        List<LivingEntity> entities = this.crocodile.level().getEntitiesOfClass(LivingEntity.class, this.crocodile.getBoundingBox().inflate(15));

        for (LivingEntity entity : entities) {
            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }
            if (entity.isAlive() && !(entity instanceof CrocodileEntity)) {
                this.isFakeNap = true;
                break;
            }
        }
        this.crocodile.setNap(true);
        yaw = this.crocodile.getYRot();
    }

    private void startAwaken() {
        crocodile.setNap(false);
        this.isFakeNap = false;
    }
}