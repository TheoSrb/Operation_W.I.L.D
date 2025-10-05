package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.tiew.operationWild.entity.OWEntity;

public class OWRandomStrollGoal extends RandomStrollGoal {
    private final OWEntity owEntity;

    public OWRandomStrollGoal(PathfinderMob mob, double speedModifier, int interval) {
        super(mob, speedModifier, mob instanceof OWEntity owEntity && owEntity.isTame() ? interval * 2 : interval);
        this.owEntity = (OWEntity) mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !owEntity.isNapping();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !owEntity.isNapping();
    }
}