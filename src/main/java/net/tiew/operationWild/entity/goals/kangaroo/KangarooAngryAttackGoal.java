package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;

public class KangarooAngryAttackGoal extends OWAttackGoal {

    private final KangarooEntity kangaroo;

    public KangarooAngryAttackGoal(KangarooEntity kangaroo, double speedModifier, int attackCooldown, double attackRange) {
        super(kangaroo, speedModifier, attackCooldown, attackRange, false);
        this.kangaroo = kangaroo;
    }

    @Override
    public boolean canUse() {
        return isAngryAndFree() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isAngryAndFree() && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        if (kangaroo.getRemainingPersistentAngerTime() <= 0) kangaroo.startPersistentAngerTimer();
        kangaroo.setMad(true);
        kangaroo.setRunning(true);
    }

    @Override
    public void stop() {
        LivingEntity previousTarget = kangaroo.getTarget();

        super.stop();
        kangaroo.setMad(false);
        kangaroo.setRunning(false);

        if (kangaroo.isAngry() && previousTarget != null && previousTarget.isAlive()) {
            kangaroo.setTarget(previousTarget);
        }
    }

    private boolean isAngryAndFree() {
        return !kangaroo.isTame()
                && kangaroo.isAngry()
                && !kangaroo.isDrowningSomeone()
                && !kangaroo.isSitting()
                && !kangaroo.isNapping();
    }
}
