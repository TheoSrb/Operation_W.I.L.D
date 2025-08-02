package net.tiew.operationWild.entity.AI;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;

public class OWAttackGoal extends Goal {
    private OWEntity attacker;
    private LivingEntity target;
    private float speedModifier;
    private int timeAttackMax;
    private int timeAttack = 0;
    private boolean canHurt = true;
    private boolean conditionToWork;
    private double distanceToAttack;

    public OWAttackGoal(OWEntity attacker, float speedModifier, int timeAttackMax, double distanceToAttack, boolean conditionToWork) {
        this.attacker = attacker;
        this.speedModifier = speedModifier;
        this.timeAttackMax = timeAttackMax;
        this.distanceToAttack = distanceToAttack;
        this.conditionToWork = conditionToWork;
    }

    @Override
    public void tick() {
        super.tick();

        if (!conditionToWork || attacker.isBaby() || attacker.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) return;

        if (this.target == null || !this.target.isAlive()) {
            stop();
            return;
        }

        double targetX = this.target.getX();
        double targetY = this.target.getY();
        double targetZ = this.target.getZ();

        if (this.attacker.getTarget() != null) {
            this.attacker.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            this.attacker.getNavigation().moveTo(targetX, targetY, targetZ, this.speedModifier);

            if (this.attacker.distanceTo(this.target) <= distanceToAttack) {
                if (canHurt) {
                    if (target.isAlive()) {
                        LivingEntity vehicle = (LivingEntity) this.target.getRootVehicle();
                        if (vehicle != null && vehicle.isAlive()) {
                            this.attacker.doHurtTarget(vehicle);
                        } else this.attacker.doHurtTarget(target);
                        this.attacker.swing(InteractionHand.MAIN_HAND);
                        canHurt = false;
                    }
                }
            }

            if (!canHurt) {
                if (timeAttack < timeAttackMax) timeAttack+=2;
                else {
                    timeAttack = 0;
                    canHurt = true;
                }
            }
        } else stop();
    }

    @Override
    public boolean canUse() {
        LivingEntity potentialTarget = this.attacker.getTarget();
        return potentialTarget != null && potentialTarget.isAlive() && conditionToWork;
    }

    @Override
    public void start() {
        super.start();
        this.target = this.attacker.getTarget();
        timeAttack = 0;
        canHurt = true;
    }

    @Override
    public void stop() {
        super.stop();
        this.target = null;
        timeAttack = 0;
        this.attacker.getNavigation().stop();
        this.attacker.resetState();
    }
}
