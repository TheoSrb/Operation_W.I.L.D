package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;

public class KodiakSitGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float sitFrequencyMultiplier;

    public KodiakSitGoal(KodiakEntity kodiak, float sitFrequencyMultiplier) {
        this.kodiak = kodiak;
        this.sitFrequencyMultiplier = sitFrequencyMultiplier;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public void start() {
        super.start();
        kodiak.setSitting(true);
        if (kodiak.getFoodPick() != null && !kodiak.getFoodPick().isEmpty()) {
            kodiak.kodiakBehaviorHandler.eatFoodInHisMouth(kodiak.getFoodPick());
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canUse() {
        return kodiak.getRandom().nextInt((int) (((kodiak.isDirty()) ? 300 : 500) / sitFrequencyMultiplier)) == 0 && !kodiak.isTame() &&
                !kodiak.isDeadOrDying() &&
                kodiak.getTarget() == null &&
                !kodiak.isInWater() &&
                kodiak.onGround() &&
                kodiak.getHealth() > (kodiak.getMaxHealth() * 0.5f) &&
                !kodiak.isNapping() &&
                !kodiak.isSearchingInsideChest && !kodiak.isRolling() && !kodiak.isCatchingSalmon() && !kodiak.isTame() && !kodiak.isHungry();
    }

    @Override
    public boolean canContinueToUse() {
        if (kodiak.isSearchingInsideChest) return false;
        return kodiak.isSitting() && kodiak.getTarget() == null && !kodiak.isCatchingSalmon() && !kodiak.isTame() && !kodiak.isHungry();
    }
}