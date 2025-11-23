
package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;

public class LionAttackGoal extends OWAttackGoal {

    private final LionEntity lion;

    public LionAttackGoal(LionEntity lion, double speedModifier, int attackCooldown, double attackRange, boolean unused) {
        super(lion, speedModifier, attackCooldown, attackRange, unused);
        this.lion = lion;
    }

    @Override
    public boolean canUse() {
        if (lion.isBaby()) return false;
        if (lion.isRoaring()) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (lion.isBaby()) return false;
        if (lion.isRoaring()) return false;
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

        double distance = this.mob.distanceTo(target);

        if (distance <= attackRange && this.ticksUntilNextAttack <= 0 && this.mob.getSensing().hasLineOfSight(target)) {
            this.performAttack(target);
            this.ticksUntilNextAttack = this.attackCooldown;
        }
    }

    @Override
    protected void performAttack(LivingEntity target) {
        if (lion.isRoaring()) return;
        super.performAttack(target);
    }
}