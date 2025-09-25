package net.tiew.operationWild.entity.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;

public class NapGoal extends Goal {

    private final OWEntity entity;
    private final float wantNapMultiplier;
    private final boolean conditionToWork;

    private int NAP_DURATION_MAX;
    private final int napTimerMax;
    private int napTimer = 0;

    public NapGoal(OWEntity entity, float wantNapMultiplier, int napTimerMax, boolean conditionToWork) {
        this.entity = entity;
        this.wantNapMultiplier = wantNapMultiplier;
        this.napTimerMax = napTimerMax;
        this.conditionToWork = conditionToWork;
    }

    @Override
    public void tick() {
        super.tick();
        if (napTimer <= NAP_DURATION_MAX) {
            napTimer--;
        } else {
            startNapping();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return conditionToWork && napTimer > 0;
    }

    @Override
    public void start() {
        super.start();
        generateMaxNapTimer();
    }

    @Override
    public boolean canUse() {
        return entity.getRandom().nextInt((int) (800 / wantNapMultiplier)) == 0 && conditionToWork;
    }

    private void generateMaxNapTimer() {
        NAP_DURATION_MAX = napTimerMax + entity.getRandom().nextInt(napTimerMax);
        napTimer = NAP_DURATION_MAX;
    }

    private void startNapping() {

    }
}
