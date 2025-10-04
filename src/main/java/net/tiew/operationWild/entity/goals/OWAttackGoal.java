package net.tiew.operationWild.entity.goals;

import com.google.common.base.Enums;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWAttackGoal extends Goal {
    private OWEntity attacker;
    private LivingEntity target;
    private float speedModifier;
    private int timeAttackMax;
    private int timeAttack = 0;
    private boolean canHurt = true;
    private boolean conditionToWork;
    private double distanceToAttack;
    private boolean followingTargetEvenIfNotSeen;

    public OWAttackGoal(OWEntity attacker, float speedModifier, int timeAttackMax, double distanceToAttack, boolean conditionToWork, boolean followingTargetEvenIfNotSeen) {
        this.attacker = attacker;
        this.speedModifier = speedModifier;
        this.timeAttackMax = timeAttackMax;
        this.distanceToAttack = distanceToAttack;
        this.conditionToWork = conditionToWork;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public OWAttackGoal(OWEntity attacker, float speedModifier, int timeAttackMax, double distanceToAttack, boolean conditionToWork) {
        this(attacker, speedModifier, timeAttackMax, distanceToAttack, conditionToWork, false);
    }

    public OWAttackGoal(OWEntity attacker, float speedModifier, int timeAttackMax, double distanceToAttack) {
        this(attacker, speedModifier, timeAttackMax, distanceToAttack, true, false);
    }


    @Override
    public void tick() {
        super.tick();

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
                        if (!this.attacker.isCombo()) {
                            if (vehicle != null && vehicle.isAlive()) {
                                this.attacker.setCombo(true, 1);
                            } else this.attacker.setCombo(true, 1);
                        } else if (this.attacker.isPauseCombo()) {
                            this.attacker.playerContinueCombo = true;
                        }
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
        if (attacker.isBaby() || attacker.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
            return false;
        }

        LivingEntity potentialTarget = this.attacker.getTarget();
        if (potentialTarget == null || !potentialTarget.isAlive()) {
            return false;
        }

        return this.attacker.canAttack() && conditionToWork;
    }

    @Override
    public boolean canContinueToUse() {
        if (attacker.isBaby() || attacker.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
            return false;
        }

        if (this.target == null || !this.target.isAlive() || !conditionToWork) {
            return false;
        }

        if (!this.followingTargetEvenIfNotSeen) {
            return !this.attacker.getNavigation().isDone();
        } else {
            if (!this.attacker.isWithinRestriction(this.target.blockPosition())) {
                return false;
            }

            if (this.target instanceof Player) {
                Player player = (Player) this.target;
                if (player.isSpectator() || player.isCreative()) {
                    return false;
                }
            }

            return true;
        }
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