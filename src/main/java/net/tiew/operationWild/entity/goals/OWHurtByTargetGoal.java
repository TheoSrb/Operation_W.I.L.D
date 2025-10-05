package net.tiew.operationWild.entity.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.tiew.operationWild.entity.OWEntity;

public class OWHurtByTargetGoal extends HurtByTargetGoal {
    private final OWEntity owEntity;

    public OWHurtByTargetGoal(OWEntity owEntity, Class<?>... toIgnoreDamage) {
        super(owEntity, toIgnoreDamage);
        this.owEntity = owEntity;
    }

    @Override
    public boolean canUse() {
        LivingEntity lastHurtByMob = this.owEntity.getLastHurtByMob();

        if (lastHurtByMob == null) {
            return false;
        }

        if (!this.owEntity.getSensing().hasLineOfSight(lastHurtByMob)) {
            return false;
        }

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.owEntity.getTarget();

        if (target == null) {
            return false;
        }

        if (!this.owEntity.getSensing().hasLineOfSight(target)) {
            return false;
        }

        if (this.owEntity.distanceTo(target) > 24.0) {
            return false;
        }

        return super.canContinueToUse();
    }
}