package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class KodiakRandomStrollGoal extends RandomStrollGoal {
    private final KodiakEntity kodiak;

    public KodiakRandomStrollGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, mob instanceof KodiakEntity kodiak && kodiak.isTame() ? 120 : 60);
        kodiak = (KodiakEntity) mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !kodiak.isNapping() && !kodiak.isRubs();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !kodiak.isNapping() && !kodiak.isRubs();
    }
}
