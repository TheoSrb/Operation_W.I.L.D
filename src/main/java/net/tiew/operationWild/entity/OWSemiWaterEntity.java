package net.tiew.operationWild.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;

import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;

public abstract class OWSemiWaterEntity extends OWEntity {

    public float i = 0;
    protected float interval = 0;
    public boolean changeDirection = false;

    public OWSemiWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        //this.goalSelector.addGoal(0, new OWSwimmingGoal(this, 0.2f));
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

        float rotationSpeed = (float) (OWUtils.generateRandomInterval(0.1, 0.8));

        if (tickCount % 100 == 0) {
            interval = 300 + this.getRandom().nextInt(501);
        }

        if (changeDirection) {
            i -= rotationSpeed;
        } else {
            i += rotationSpeed;
        }

        if (tickCount % interval == 0) {
            changeDirection = !changeDirection;
        }

        this.setYHeadRot(i);
        this.setYRot(i);
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

            double wave = Math.sin(this.tickCount * 0.05) * 0.01;
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

    /*public static class OWSwimmingGoal extends Goal {
        OWEntity entity;
        float speed;
        float circlingTime = 0;
        float circleDistance = 15;
        float maxCirclingTime = 80;
        boolean clockwise = false;
        boolean forceAttack = false;

        private double centerX;
        private double centerZ;
        private int directionChangeTimer = 0;
        private boolean changingDirection = false;
        private int transitionTime = 0;
        private static final int TRANSITION_DURATION = 60;

        public OWSwimmingGoal(OWEntity entity, float speed) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.entity = entity;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            return entity.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return entity.isInWater();
        }

        public void start(){
            circlingTime = 0;
            maxCirclingTime = 500 + this.entity.getRandom().nextInt(300);
            circleDistance = 15 + this.entity.getRandom().nextFloat() * 15;
            clockwise = this.entity.getRandom().nextBoolean();
            forceAttack = false;
            directionChangeTimer = 400 + this.entity.getRandom().nextInt(400);
            changingDirection = false;
            transitionTime = 0;

            centerX = entity.getX();
            centerZ = entity.getZ();
        }

        public void stop(){
            circlingTime = 0;
            maxCirclingTime = 500 + this.entity.getRandom().nextInt(300);
            circleDistance = 15 + this.entity.getRandom().nextFloat() * 15;
            clockwise = this.entity.getRandom().nextBoolean();
            forceAttack = false;
            directionChangeTimer = 400 + this.entity.getRandom().nextInt(400);
            changingDirection = false;
            transitionTime = 0;
        }

        public void tick() {
            directionChangeTimer--;

            if (directionChangeTimer <= 0 && !changingDirection) {
                changingDirection = true;
                transitionTime = 0;
                directionChangeTimer = 400 + this.entity.getRandom().nextInt(400);
            }

            double angleDirection = clockwise ? 1.0 : -1.0;

            if (changingDirection) {
                transitionTime++;
                float progress = (float) transitionTime / TRANSITION_DURATION;
                progress = (float) (1.0 - Math.cos(progress * Math.PI)) / 2.0f;

                double oldDirection = clockwise ? 1.0 : -1.0;
                double newDirection = clockwise ? -1.0 : 1.0;
                angleDirection = oldDirection + (newDirection - oldDirection) * progress;

                if (transitionTime >= TRANSITION_DURATION) {
                    changingDirection = false;
                    clockwise = !clockwise;
                }
            }

            double angle = (circlingTime * 0.03 * angleDirection);
            double targetX = centerX + Math.cos(angle) * circleDistance;
            double targetZ = centerZ + Math.sin(angle) * circleDistance;

            entity.getNavigation().moveTo(targetX, entity.getY(), targetZ, speed);

            double deltaX = targetX - entity.getX();
            double deltaZ = targetZ - entity.getZ();

            if (deltaX * deltaX + deltaZ * deltaZ > 0.01) {
                float desiredYaw = (float) (Math.atan2(deltaZ, deltaX) * 57.2957795) - 90.0F;
                float currentYaw = entity.getYRot();
                float yawDiff = desiredYaw - currentYaw;

                while (yawDiff > 180.0F) yawDiff -= 360.0F;
                while (yawDiff < -180.0F) yawDiff += 360.0F;

                float smoothYaw = currentYaw + yawDiff * 0.15F;
                entity.setYRot(smoothYaw);
                entity.yRotO = smoothYaw;
            }

            circlingTime++;

            if (circlingTime > maxCirclingTime) {
                centerX = entity.getX();
                centerZ = entity.getZ();
                circlingTime = 0;
            }
        }
    }*/
}
