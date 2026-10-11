package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.taming.OWAggressiveTaming;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class OWSedatedFleeGoal extends Goal {

    private static final int REPATH_INTERVAL = 30;
    private static final int ESCAPE_RADIUS = 16;
    private static final int ESCAPE_HEIGHT = 7;
    private static final float DROWSY_SLOWDOWN = 0.45f;
    private static final float STAGGER_BASE_CHANCE = 0.012f;
    private static final float STAGGER_DROWSY_CHANCE = 0.035f;
    private static final int STAGGER_MIN_TICKS = 8;
    private static final int STAGGER_MAX_TICKS = 14;
    private static final float STAGGER_SPEED_FACTOR = 0.35f;
    private static final double STAGGER_SWAY = 0.12;
    private static final int RETRY_DELAY = 10;

    private final OWEntity mob;
    private final OWFleeRoute route;
    private int staggerTicks;
    private int retryAt;

    public OWSedatedFleeGoal(OWEntity mob) {
        this.mob = mob;
        this.route = new OWFleeRoute(mob, ESCAPE_RADIUS, ESCAPE_HEIGHT, REPATH_INTERVAL);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isSedatedFleeing() || mob.isPassenger() || mob.breakout.wantsControl()) return false;
        if (mob.tickCount < retryAt) return false;
        if (route.prepare(danger())) return true;
        retryAt = mob.tickCount + RETRY_DELAY;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isSedatedFleeing() && !mob.isPassenger() && !mob.breakout.wantsControl();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.forceSetTarget(null);
        mob.setRunning(true);
        mob.setState(2);
        route.begin(currentSpeed());
    }

    @Override
    public void tick() {
        tickStagger();
        Entity threat = mob.aggressiveTaming.getThreat();
        route.tick(threat != null ? threat.position() : null, threat, currentSpeed(), drowsiness());
        mob.setRunning(!route.isHolding());
    }

    @Override
    public void stop() {
        route.stop();
        staggerTicks = 0;
        mob.setRunning(false);
        mob.resetState();
    }

    @Nullable
    private Vec3 danger() {
        Entity threat = mob.aggressiveTaming.getThreat();
        return threat != null ? threat.position() : null;
    }

    private void tickStagger() {
        if (staggerTicks > 0) {
            if (--staggerTicks == 0) mob.getNavigation().setSpeedModifier(fleeSpeed());
            return;
        }
        if (!mob.onGround() || mob.isInWater() || route.isHolding()) return;
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

    private double currentSpeed() {
        return fleeSpeed() * (staggerTicks > 0 ? STAGGER_SPEED_FACTOR : 1.0f);
    }
}
