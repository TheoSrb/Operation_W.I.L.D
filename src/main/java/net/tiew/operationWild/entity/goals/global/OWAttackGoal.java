package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
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

    private static final float MAX_HEAD_ROTATION_SPEED = 15.0F;
    private static final float MAX_BODY_ROTATION_SPEED = 5.0F;
    private static final float HEAD_BODY_ANGLE_THRESHOLD = 75.0F;

    private float targetYaw;
    private float targetPitch;
    private boolean isRotatingToTarget;

    private Path path;
    private int ticksUntilNextAttack;
    private int ticksUntilNextPathRecalc;

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

        if (this.mob instanceof CrocodileEntity crocodile && crocodile.hasSomeoneInHisMouth()) return false;

        if (target == null || !target.isAlive()) {
            return false;
        }

        this.path = this.mob.getNavigation().createPath(target, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();

        if (this.mob instanceof CrocodileEntity crocodile && crocodile.hasSomeoneInHisMouth()) return false;

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
        if (target == null || this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) {
            return;
        }

        this.ticksUntilNextPathRecalc--;
        if (this.ticksUntilNextPathRecalc <= 0) {
            this.ticksUntilNextPathRecalc = 4 + this.mob.getRandom().nextInt(7);
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        }

        this.updateSmoothLookAt(target);

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        double distance = this.mob.distanceTo(target);
        if (distance <= this.attackRange && this.ticksUntilNextAttack <= 0 && this.mob.getSensing().hasLineOfSight(target)) {
            this.performAttack(target);
            this.ticksUntilNextAttack = this.attackCooldown;
        }
    }

    private void updateSmoothLookAt(LivingEntity target) {
        double dx = target.getX() - this.mob.getX();
        double dy = target.getY() + target.getEyeHeight() - (this.mob.getY() + this.mob.getEyeHeight());
        double dz = target.getZ() - this.mob.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        this.targetYaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        this.targetPitch = (float)(-(Mth.atan2(dy, horizontalDist) * (180.0 / Math.PI)));

        this.targetYaw = Mth.wrapDegrees(this.targetYaw);
        this.targetPitch = Mth.clamp(this.targetPitch, -90.0F, 90.0F);

        float currentHeadYaw = this.mob.getYHeadRot();
        float headYawDiff = Mth.wrapDegrees(this.targetYaw - currentHeadYaw);

        float headYawChange = Mth.clamp(headYawDiff, -MAX_HEAD_ROTATION_SPEED, MAX_HEAD_ROTATION_SPEED);
        float newHeadYaw = currentHeadYaw + headYawChange;

        this.mob.setYHeadRot(newHeadYaw);
        this.mob.yHeadRotO = newHeadYaw;

        float currentBodyYaw = this.mob.getYRot();
        float bodyHeadDiff = Mth.wrapDegrees(newHeadYaw - currentBodyYaw);

        if (Math.abs(bodyHeadDiff) > HEAD_BODY_ANGLE_THRESHOLD) {
            float bodyYawDiff = Mth.wrapDegrees(this.targetYaw - currentBodyYaw);
            float bodyYawChange = Mth.clamp(bodyYawDiff, -MAX_BODY_ROTATION_SPEED, MAX_BODY_ROTATION_SPEED);
            float newBodyYaw = currentBodyYaw + bodyYawChange;

            this.mob.setYRot(newBodyYaw);
            this.mob.yRotO = newBodyYaw;
            this.mob.yBodyRot = newBodyYaw;
            this.mob.yBodyRotO = newBodyYaw;
        }

        float currentPitch = this.mob.getXRot();
        float pitchDiff = this.targetPitch - currentPitch;
        float pitchChange = Mth.clamp(pitchDiff, -MAX_HEAD_ROTATION_SPEED * 0.5F, MAX_HEAD_ROTATION_SPEED * 0.5F);
        float newPitch = currentPitch + pitchChange;

        this.mob.setXRot(newPitch);
        this.mob.xRotO = newPitch;
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