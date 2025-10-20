package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

public class OWRandomStrollGoal extends RandomStrollGoal {
    private final OWEntity owEntity;

    public OWRandomStrollGoal(PathfinderMob mob, double speedModifier, int interval) {
        super(mob, speedModifier, mob instanceof OWEntity owEntity && owEntity.isTame() ? interval * 2 : interval);
        this.owEntity = (OWEntity) mob;
    }

    @Override
    public boolean canUse() {
        if (mob instanceof CrocodileEntity crocodile && (crocodile.isFakeNap())) return false;
        return super.canUse() && !owEntity.isNapping();
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof CrocodileEntity crocodile && (crocodile.isFakeNap())) return false;
        return super.canContinueToUse() && !owEntity.isNapping();
    }
}