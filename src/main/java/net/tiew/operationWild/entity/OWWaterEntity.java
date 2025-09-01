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
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;

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
        if (this instanceof TigerSharkEntity shark && (shark.canGoDown || shark.canGoUp)) {
            super.travel(travelVector);
            return;
        }


        if (this.isEffectiveAi() && this.isInWater() && !isSleeping()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getTarget() == null && !(this instanceof JellyfishEntity)) {
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

    public static class SharkSwimmingGoal extends Goal {
        TigerSharkEntity shark;
        float speed;
        float circlingTime = 0;
        float circleDistance = 15;
        float maxCirclingTime = 80;
        boolean clockwise = false;
        boolean forceAttack = false;

        public SharkSwimmingGoal(TigerSharkEntity shark, float speed) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.shark = shark;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            return shark.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return shark.isInWater();
        }

        public void start(){
            circlingTime = 0;
            maxCirclingTime = 360 + this.shark.random.nextInt(80);
            circleDistance = 15 + this.shark.random.nextFloat() * 15;
            clockwise = this.shark.random.nextBoolean();
            forceAttack = false;
        }

        public void stop(){
            circlingTime = 0;
            maxCirclingTime = 360 + this.shark.random.nextInt(80);
            circleDistance = 15 + this.shark.random.nextFloat() * 15;
            clockwise = this.shark.random.nextBoolean();
            forceAttack = false;
            shark.setGoingToHitTarget(false);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    shark.resetState();
                }
            }, 480);
        }

        public void triggerAttack(LivingEntity target) {
            forceAttack = true;
            this.shark.setTarget(target);
        }

        public void tick(){
            LivingEntity prey = this.shark.getTarget();
            if (!shark.wantsToApproachPlayer && !shark.isSleeping() && !shark.isShakingPrey()) {
                if (prey != null) {
                    double dist = this.shark.distanceTo(prey);
                    float healthPercent = shark.getHealth() / shark.getMaxHealth();
                    if (circlingTime >= ((maxCirclingTime * Math.max(0.15, healthPercent)) * (shark.isSmellingBlood ? 0.5 : 1)) || forceAttack) {
                        shark.setGoingToHitTarget(true);

                        if (dist < 3D) {
                            shark.doHurtTarget(prey);
                            shark.swing(InteractionHand.MAIN_HAND);

                            stop();
                        }
                    } else {
                        if (dist <= 35) {
                            double angle = (circlingTime * 0.1) % (2 * Math.PI);
                            double circleRadius = 100.0;
                            double targetX = prey.getX() + Math.cos(angle) * circleRadius;
                            double targetZ = prey.getZ() + Math.sin(angle) * circleRadius;

                            shark.getNavigation().moveTo(targetX, prey.getY(), targetZ, 1D * (shark.isSmellingBlood ? 1.35f : 1f));
                            circlingTime++;
                        }
                    }
                } else {
                    if (!shark.canGoDown && !shark.canGoUp) {
                        double angle = (circlingTime * 0.3) % (2 * Math.PI);
                        double circleRadius = 150.0;
                        double targetX = shark.getX() + Math.cos(angle) * circleRadius;
                        double targetZ = shark.getZ() + Math.sin(angle) * circleRadius;

                        shark.getNavigation().moveTo(targetX, shark.getY(), targetZ, (0.9D * (shark.isShakingPrey() ? 1.5f : 1)) * (shark.isSmellingBlood ? 1.35f : 1f));
                        circlingTime++;
                    }
                }
            }
        }
    }
}
