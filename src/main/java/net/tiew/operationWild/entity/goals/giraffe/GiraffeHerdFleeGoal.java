package net.tiew.operationWild.entity.goals.giraffe;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;

import java.util.EnumSet;

public class GiraffeHerdFleeGoal extends Goal {

    private static final double FLEE_SPEED = 2.4;
    private static final int FLEE_HORIZONTAL = 24;
    private static final int FLEE_VERTICAL = 7;

    private final GiraffeEntity giraffe;

    public GiraffeHerdFleeGoal(GiraffeEntity giraffe) {
        this.giraffe = giraffe;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!giraffe.isHerdFleeing() || giraffe.isKnockedOut() || giraffe.isSedatedFleeing()) return false;
        return moveAway();
    }

    @Override
    public boolean canContinueToUse() {
        return giraffe.isHerdFleeing() && !giraffe.isKnockedOut() && !giraffe.isSedatedFleeing();
    }

    @Override
    public void start() {
        giraffe.setRunning(true);
    }

    @Override
    public void tick() {
        if (giraffe.getNavigation().isDone()) moveAway();
    }

    @Override
    public void stop() {
        giraffe.setRunning(false);
        giraffe.getNavigation().stop();
    }

    private boolean moveAway() {
        Vec3 threat = giraffe.getHerdThreat();
        if (threat == null) return false;
        Vec3 destination = DefaultRandomPos.getPosAway(giraffe, FLEE_HORIZONTAL, FLEE_VERTICAL, threat);
        if (destination == null) return false;
        return giraffe.getNavigation().moveTo(destination.x, destination.y, destination.z, FLEE_SPEED);
    }
}
