package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWSedationRetreatGoal extends Goal {

    private static final int REPATH_INTERVAL = 40;
    private static final int ESCAPE_RADIUS = 24;
    private static final int ESCAPE_HEIGHT = 7;

    private final OWEntity mob;
    private int repathTimer;

    public OWSedationRetreatGoal(OWEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.sedationResponse.isRetreating() && !mob.isPassenger();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repathTimer = 0;
        mob.setRunning(true);
        mob.setState(2);
        runAway();
    }

    @Override
    public void tick() {
        if (--repathTimer > 0 && !mob.getNavigation().isDone()) return;
        runAway();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setRunning(false);
        mob.resetState();
    }

    private void runAway() {
        repathTimer = REPATH_INTERVAL + mob.getRandom().nextInt(10);
        Vec3 escape = OWSedatedFleeGoal.findEscapeFrom(mob, mob.sedationResponse.getThreatPosition(), ESCAPE_RADIUS, ESCAPE_HEIGHT);
        if (escape != null) mob.getNavigation().moveTo(escape.x, escape.y, escape.z, mob.sedatedFleeSpeed());
    }
}
