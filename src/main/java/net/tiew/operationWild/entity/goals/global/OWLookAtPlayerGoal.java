package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class OWLookAtPlayerGoal extends LookAtPlayerGoal {
    public OWLookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance) {
        super(mob, lookAtType, lookDistance);
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof KodiakEntity kodiak && kodiak.isRolling()) return false;
        if (mob instanceof OWEntity owEntity && (owEntity.isNapping())) return false;
        return super.canContinueToUse();
    }

    @Override
    public boolean canUse() {
        if (mob instanceof KodiakEntity kodiak && kodiak.isRolling()) return false;
        if (mob instanceof OWEntity owEntity && (owEntity.isNapping())) return false;
        return super.canUse();
    }
}
