package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.taming.OWAggressiveTaming;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class OWSedatedFleeGoal extends Goal {

    private static final int REPATH_INTERVAL = 30;
    private static final int ESCAPE_RADIUS = 16;
    private static final int ESCAPE_HEIGHT = 7;
    private static final int SWIM_SAMPLES = 8;
    private static final float DROWSY_SLOWDOWN = 0.45f;
    private static final float STAGGER_BASE_CHANCE = 0.012f;
    private static final float STAGGER_DROWSY_CHANCE = 0.035f;
    private static final int STAGGER_MIN_TICKS = 8;
    private static final int STAGGER_MAX_TICKS = 14;
    private static final float STAGGER_SPEED_FACTOR = 0.35f;
    private static final double STAGGER_SWAY = 0.12;

    private final OWEntity mob;
    @Nullable
    private Vec3 escape;
    private int repathTimer;
    private int staggerTicks;

    public OWSedatedFleeGoal(OWEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isSedatedFleeing() || mob.isPassenger()) return false;
        escape = findEscape();
        return escape != null;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isSedatedFleeing() && !mob.isPassenger();
    }

    @Override
    public void start() {
        mob.forceSetTarget(null);
        mob.setRunning(true);
        mob.setState(2);
        runToEscape();
    }

    @Override
    public void tick() {
        tickStagger();
        if (--repathTimer > 0 && !mob.getNavigation().isDone()) return;
        Vec3 next = findEscape();
        if (next != null) escape = next;
        runToEscape();
    }

    private void tickStagger() {
        if (staggerTicks > 0) {
            if (--staggerTicks == 0) mob.getNavigation().setSpeedModifier(fleeSpeed());
            return;
        }
        if (!mob.onGround() || mob.isInWater()) return;
        float chance = STAGGER_BASE_CHANCE + STAGGER_DROWSY_CHANCE * drowsiness();
        if (mob.getRandom().nextFloat() >= chance) return;

        staggerTicks = STAGGER_MIN_TICKS + mob.getRandom().nextInt(STAGGER_MAX_TICKS - STAGGER_MIN_TICKS + 1);
        mob.getNavigation().setSpeedModifier(fleeSpeed() * STAGGER_SPEED_FACTOR);
        double yaw = Math.toRadians(mob.getYRot());
        double side = mob.getRandom().nextBoolean() ? 1.0 : -1.0;
        mob.setDeltaMovement(mob.getDeltaMovement().add(Math.cos(yaw) * STAGGER_SWAY * side, 0.0, Math.sin(yaw) * STAGGER_SWAY * side));
        mob.hasImpulse = true;
    }

    private float drowsiness() {
        float range = 100f - OWAggressiveTaming.FLEE_THRESHOLD;
        return Mth.clamp((mob.getSleepBarPercent() - OWAggressiveTaming.FLEE_THRESHOLD) / range, 0f, 1f);
    }

    private double fleeSpeed() {
        return mob.sedatedFleeSpeed() * (1.0f - DROWSY_SLOWDOWN * drowsiness());
    }

    @Override
    public void stop() {
        escape = null;
        staggerTicks = 0;
        mob.getNavigation().stop();
        mob.setRunning(false);
        mob.resetState();
    }

    private void runToEscape() {
        repathTimer = REPATH_INTERVAL + mob.getRandom().nextInt(10);
        if (escape == null) return;
        double speed = fleeSpeed() * (staggerTicks > 0 ? STAGGER_SPEED_FACTOR : 1.0f);
        mob.getNavigation().moveTo(escape.x, escape.y, escape.z, speed);
    }

    @Nullable
    private Vec3 findEscape() {
        Entity threat = mob.aggressiveTaming.getThreat();
        return findEscapeFrom(mob, threat != null ? threat.position() : null, ESCAPE_RADIUS, ESCAPE_HEIGHT);
    }

    @Nullable
    public static Vec3 findEscapeFrom(OWEntity mob, @Nullable Vec3 danger, int radius, int height) {
        boolean swimmer = mob.isInWater() && (mob instanceof OWWaterEntity || mob instanceof OWSemiWaterEntity);
        if (swimmer) return findSwimEscape(mob, danger, radius, height);
        Vec3 target = danger != null
                ? DefaultRandomPos.getPosAway(mob, radius, height, danger)
                : DefaultRandomPos.getPos(mob, radius, height);
        return target != null ? target : DefaultRandomPos.getPos(mob, radius / 2, height);
    }

    @Nullable
    private static Vec3 findSwimEscape(OWEntity mob, @Nullable Vec3 danger, int radius, int height) {
        Vec3 best = null;
        double bestDistance = -1;
        for (int i = 0; i < SWIM_SAMPLES; i++) {
            Vec3 candidate = BehaviorUtils.getRandomSwimmablePos(mob, radius, height);
            if (candidate == null) continue;
            if (danger == null) return candidate;
            double distance = candidate.distanceToSqr(danger);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}
