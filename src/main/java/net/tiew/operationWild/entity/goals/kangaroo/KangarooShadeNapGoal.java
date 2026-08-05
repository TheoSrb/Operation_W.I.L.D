package net.tiew.operationWild.entity.goals.kangaroo;

import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.goals.NapGoal;

public class KangarooShadeNapGoal extends NapGoal {

    private final KangarooEntity kangaroo;

    public KangarooShadeNapGoal(KangarooEntity kangaroo, float wantNapMultiplier, int napTimerMax) {
        super(kangaroo, wantNapMultiplier, napTimerMax, true);
        this.kangaroo = kangaroo;
    }

    @Override
    public boolean canUse() {
        return kangaroo.isHotHours()
                && kangaroo.isInHotBiome()
                && kangaroo.isInShade()
                && !kangaroo.isAngry()
                && !kangaroo.isAlerted()
                && !kangaroo.isThumping()
                && !kangaroo.isGrazing()
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return kangaroo.isInShade()
                && !kangaroo.isAngry()
                && !kangaroo.isAlerted()
                && super.canContinueToUse();
    }
}
