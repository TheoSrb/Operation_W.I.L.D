package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;

import java.util.EnumSet;

public class ElephantListenGoal extends Goal {

    private static final float TURN_SPEED = 5f;

    private final ElephantEntity elephant;

    public ElephantListenGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return elephant.wild.getListenTicks() > 0 && elephant.wild.getListenSource() != null
                && ElephantGoals.isCalm(elephant) && elephant.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        elephant.getNavigation().stop();
        elephant.wild.setListeningActive(true);
    }

    @Override
    public void stop() {
        elephant.wild.stopListening();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Vec3 source = elephant.wild.getListenSource();
        if (source == null) return;
        elephant.getNavigation().stop();
        elephant.turnBodyTowards(source, TURN_SPEED);
        elephant.getLookControl().setLookAt(source.x, source.y + 1.0, source.z, 6f, 20f);
    }
}
