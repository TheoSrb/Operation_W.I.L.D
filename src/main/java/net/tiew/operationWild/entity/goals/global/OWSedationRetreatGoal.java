package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWSedationRetreatGoal extends Goal {

    private static final int REPATH_INTERVAL = 40;
    private static final int ESCAPE_RADIUS = 24;
    private static final int ESCAPE_HEIGHT = 7;

    private final OWEntity mob;
    private final OWFleeRoute route;

    public OWSedationRetreatGoal(OWEntity mob) {
        this.mob = mob;
        this.route = new OWFleeRoute(mob, ESCAPE_RADIUS, ESCAPE_HEIGHT, REPATH_INTERVAL);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!canContinueToUse()) return false;
        route.prepare(mob.sedationResponse.getThreatPosition());
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.sedationResponse.isRetreating() && !mob.isPassenger() && !mob.breakout.wantsControl();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.setRunning(true);
        mob.setState(2);
        route.begin(mob.sedatedFleeSpeed());
    }

    @Override
    public void tick() {
        route.tick(mob.sedationResponse.getThreatPosition(), mob.sedationResponse.getProvoker(), mob.sedatedFleeSpeed(), drowsiness());
        mob.setRunning(!route.isHolding());
    }

    private float drowsiness() {
        if (mob.getMaxSleepingBar() <= 0) return 0f;
        return Mth.clamp(mob.getSleepBarPercent() / 100f, 0f, 1f);
    }

    @Override
    public void stop() {
        route.stop();
        mob.setRunning(false);
        mob.resetState();
    }
}
