package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class KodiakRubsAgainstTreeGoal extends Goal {

    private final KodiakEntity kodiak;

    public KodiakRubsAgainstTreeGoal(KodiakEntity kodiak) {
        this.kodiak = kodiak;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public boolean canUse() {
        return true;
    }
}
