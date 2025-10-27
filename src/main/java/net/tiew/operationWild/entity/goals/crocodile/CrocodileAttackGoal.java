package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;

public class CrocodileAttackGoal extends OWAttackGoal {

    private final CrocodileEntity crocodile;

    public CrocodileAttackGoal(CrocodileEntity crocodile, double speedModifier, int attackCooldown, double attackRange, boolean unused) {
        super(crocodile, speedModifier, attackCooldown, attackRange, unused);
        this.crocodile = crocodile;
    }

    @Override
    public boolean canUse() {
        if (crocodile.hasSomeoneInHisMouth()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (crocodile.hasSomeoneInHisMouth()) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) {
            return;
        }

        this.ticksUntilNextPathRecalc--;
        if (this.ticksUntilNextPathRecalc <= 0) {
            this.ticksUntilNextPathRecalc = 4 + this.mob.getRandom().nextInt(7);
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        }

        this.mob.setLookAt(target.getX(), target.getY(), target.getZ());

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        double effectiveAttackRange = crocodile.canGrabUnderwater() ? attackRange * 2 : attackRange;
        double distance = this.mob.distanceTo(target);

        if (distance <= effectiveAttackRange && this.ticksUntilNextAttack <= 0 && this.mob.getSensing().hasLineOfSight(target)) {
            this.performAttack(target);
            this.ticksUntilNextAttack = this.attackCooldown;
        }
    }

    @Override
    protected void performAttack(LivingEntity target) {
        if (crocodile.isChargingMouth()) return;
        if (this.crocodile.canGrabUnderwater()) {
            if (this.crocodile.getTarget() != null && this.crocodile.attackingGrabCooldown <= 0) {
                this.crocodile.setAttackingGrab(true);
                this.crocodile.attackingGrabCooldown = 400 + this.crocodile.getRandom().nextInt(200);
            }
        } else {
            super.performAttack(target);
        }
    }
}