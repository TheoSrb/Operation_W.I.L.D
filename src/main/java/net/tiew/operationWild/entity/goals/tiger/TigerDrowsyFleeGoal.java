package net.tiew.operationWild.entity.goals.tiger;

import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.goals.global.OWPanicGoal;

public class TigerDrowsyFleeGoal extends OWPanicGoal {

    private final TigerEntity tiger;

    public TigerDrowsyFleeGoal(TigerEntity tiger, float speedModifier) {
        super(tiger, speedModifier, 1.5, TigerEntity.DROWSY_FLEE_THRESHOLD);
        this.tiger = tiger;
    }

    @Override
    public boolean canUse() {
        if (tiger.isTame() || tiger.isBaby()) return false;
        if (tiger.getSleepBarPercent() < TigerEntity.DROWSY_FLEE_THRESHOLD) return false;
        return findRandomPosition();
    }

    @Override
    public boolean canContinueToUse() {
        if (tiger.isTame() || tiger.getSleepBarPercent() < TigerEntity.DROWSY_FLEE_THRESHOLD) return false;
        return !tiger.getNavigation().isDone();
    }

    @Override
    public void start() {
        super.start();
        tiger.setRunning(true);
        tiger.setState(2);
        tiger.setMad(false);
        tiger.forceSetTarget(null);
    }

    @Override
    public void stop() {
        super.stop();
        tiger.setRunning(false);
        tiger.resetState();
    }
}
