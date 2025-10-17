package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

import java.util.EnumSet;

public class OWAttackGoal extends Goal {
    private final OWEntity mob;
    private final double speedModifier;
    private final int attackCooldown;
    private final double attackRange;
    private static final double MAX_CHASE_DISTANCE = 48.0;
    private static final float BODY_TURN_SPEED_DEG = 6.0F; // smooth body rotation per tick
    private static final float TURN_IN_PLACE_THRESHOLD_DEG = 100.0F; // if target is largely behind
    private static final int PATH_RECALC_BASE_TICKS = 10; // slower, more stable recalc cadence
    private static final int PATH_RECALC_RANDOM_TICKS = 10;
    private static final double TARGET_MOVE_RECALC_SQR = 4.0; // recalc if target moved >2 blocks

    private Path path;
    private int ticksUntilNextAttack;
    private int ticksUntilNextPathRecalc;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;
    private boolean turningInPlace;

    public OWAttackGoal(OWEntity mob, double speedModifier, int attackCooldown, double attackRange, boolean unused) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackCooldown = attackCooldown;
        this.attackRange = attackRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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

        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target instanceof Player && (target.isSpectator() || ((Player)target).isCreative())) {
            return false;
        }

        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
        double distance = this.mob.distanceTo(target);

        if (distance > MAX_CHASE_DISTANCE) {
            return false;
        }

        if (!canSeeTarget) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextAttack = 0;
        this.ticksUntilNextPathRecalc = 0;

        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.lastTargetX = target.getX();
            this.lastTargetY = target.getY();
            this.lastTargetZ = target.getZ();
        }
        this.turningInPlace = false;
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();

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
        if (target == null || this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) {
            return;
        }

        // Smoothly rotate body towards target and let LookControl handle head
        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        float desiredYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float currentYaw = this.mob.getYRot();
        float newBodyYaw = Mth.approachDegrees(currentYaw, desiredYaw, BODY_TURN_SPEED_DEG);
        this.mob.setYRot(newBodyYaw);
        this.mob.setYBodyRot(newBodyYaw);

        // Use LookControl for smooth head tracking
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        this.ticksUntilNextPathRecalc--;
        float yawDelta = Mth.wrapDegrees(desiredYaw - newBodyYaw);
        boolean targetBehind = Math.abs(yawDelta) > TURN_IN_PLACE_THRESHOLD_DEG;

        // If the target is behind, rotate in place to avoid unrealistic strafing/spinning
        if (targetBehind && this.mob.onGround()) {
            this.turningInPlace = true;
            this.mob.getNavigation().stop();
            // Delay next path recalculation while turning to face the target
            if (this.ticksUntilNextPathRecalc <= 0) {
                this.ticksUntilNextPathRecalc = 4; // short delay while turning
            }
        } else {
            this.turningInPlace = false;

            // Recalculate path less frequently and only when useful
            boolean targetMovedFar = target.distanceToSqr(this.lastTargetX, this.lastTargetY, this.lastTargetZ) > TARGET_MOVE_RECALC_SQR;
            boolean pathDoneOrMissing = this.mob.getNavigation().isDone() || this.path == null;

            if (this.ticksUntilNextPathRecalc <= 0 || targetMovedFar || pathDoneOrMissing) {
                this.ticksUntilNextPathRecalc = PATH_RECALC_BASE_TICKS + this.mob.getRandom().nextInt(PATH_RECALC_RANDOM_TICKS + 1);
                this.path = this.mob.getNavigation().createPath(target, 0);
                if (this.path != null) {
                    this.mob.getNavigation().moveTo(this.path, this.speedModifier);
                } else {
                    // Fallback to direct move attempt if no path is found (keeps pursuit feeling responsive)
                    this.mob.getNavigation().moveTo(target, this.speedModifier);
                }
                this.lastTargetX = target.getX();
                this.lastTargetY = target.getY();
                this.lastTargetZ = target.getZ();
            }
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        double distance = this.mob.distanceTo(target);
        if (!this.turningInPlace && distance <= this.attackRange && this.ticksUntilNextAttack <= 0 && this.mob.getSensing().hasLineOfSight(target)) {
            this.performAttack(target);
            this.ticksUntilNextAttack = this.attackCooldown;
        }
    }

    private void performAttack(LivingEntity target) {
        if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
        if (this.mob instanceof CrocodileEntity crocodile && crocodile.isChargingMouth()) return;
        if (!this.mob.isCombo()) {
            this.mob.setCombo(true, 1);
        } else if (this.mob.isPauseCombo()) {
            this.mob.playerContinueCombo = true;
        }
    }
}