package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWBreakoutGoal extends Goal {

    private final OWEntity mob;

    public OWBreakoutGoal(OWEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.breakout.wantsControl() && !mob.isPassenger();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.setRunning(false);
        mob.resetState();
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        mob.breakout.drive();
        if (mob.breakout.isCharging()) {
            mob.setRunning(true);
            if (mob.getState() != 1) mob.setState(2);
        } else if (mob.isRunning()) {
            mob.setRunning(false);
            if (mob.getState() == 2) mob.resetState();
        }
    }

    @Override
    public void stop() {
        mob.breakout.release();
        mob.setRunning(false);
        if (mob.getState() == 2) mob.resetState();
    }
}
