package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWAttackGoal extends Goal {
    protected final OWEntity mob;
    protected final double speedModifier;
    protected final int attackCooldown;
    protected final double attackRange;
    protected static final double MAX_CHASE_DISTANCE = 48.0;

    protected boolean isRotatingToTarget;

    protected Path path;
    protected int ticksUntilNextAttack;
    protected int ticksUntilNextPathRecalc;

    public OWAttackGoal(OWEntity mob, double speedModifier, int attackCooldown, double attackRange, boolean unused) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackCooldown = attackCooldown;
        this.attackRange = attackRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.isRotatingToTarget = false;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        this.path = this.mob.getNavigation().createPath(target, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();

        if (target == null || !target.isAlive()) return false;

        if (target instanceof Player player && (player.isSpectator() || player.isCreative())) return false;

        if (this.mob.distanceTo(target) > MAX_CHASE_DISTANCE) return false;

        return !this.mob.getNavigation().isDone() || this.mob.distanceTo(target) <= this.attackRange;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextAttack = 0;
        this.ticksUntilNextPathRecalc = 0;
        this.isRotatingToTarget = false;
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        this.isRotatingToTarget = false;

        if (this.mob instanceof OWEntity owEntity) {
            owEntity.forceSetTarget(null);
            owEntity.setRunning(false);
        } else {
            this.mob.setTarget(null);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;

        this.ticksUntilNextPathRecalc--;
        if (this.ticksUntilNextPathRecalc <= 0) {
            this.ticksUntilNextPathRecalc = 4 + this.mob.getRandom().nextInt(7);
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        }

        this.mob.setLookAt(target.getX(), target.getY(), target.getZ());

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        double distance = this.mob.distanceTo(target);
        if (distance <= this.attackRange && this.ticksUntilNextAttack <= 0 && this.mob.getSensing().hasLineOfSight(target)) {
            this.performAttack(target);
            this.ticksUntilNextAttack = this.attackCooldown;
        }
    }

    protected void performAttack(LivingEntity target) {
        if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
        if (!this.mob.isCombo()) {
            this.mob.setCombo(true, 1);
        } else if (this.mob.isPauseCombo()) {
            this.mob.playerContinueCombo = true;
        }
    }
}