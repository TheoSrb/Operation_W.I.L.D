package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;

public class CrocodileAttackGoal extends OWAttackGoal {

    private final CrocodileEntity crocodile;

    public CrocodileAttackGoal(CrocodileEntity crocodile, double speedModifier, int attackCooldown, double attackRange, boolean unused) {
        super(crocodile, speedModifier, attackCooldown, attackRange, unused);
        this.crocodile = crocodile;
    }

    private boolean isCrocodileBlocked() {
        return crocodile.crocodileBehaviorHandler.isReadyForTaming()
                || crocodile.isBaby()
                || crocodile.hasGrabSomething()
                || crocodile.isChargingAttack;
    }

    @Override
    public boolean canUse() {
        if (isCrocodileBlocked()) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (isCrocodileBlocked()) return false;
        return super.canContinueToUse();
    }

    @Override
    protected void performAttack(LivingEntity target) {
        if (crocodile.isChargingMouth()) return;
        super.performAttack(target);
    }
}