package net.tiew.operationWild.entity.AI;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.utils.OWUtils;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class OWBreedGoal extends Goal {
    private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range((double)8.0F).ignoreLineOfSight();
    protected final OWEntity animal;
    private final Class<? extends OWEntity> partnerClass;
    protected final Level level;
    @Nullable
    protected OWEntity partner;
    private int loveTime;
    private final double speedModifier;

    public OWBreedGoal(OWEntity p_25122_, double p_25123_) {
        this(p_25122_, p_25123_, p_25122_.getClass());
    }

    public OWBreedGoal(OWEntity p_25125_, double p_25126_, Class<? extends OWEntity> p_25127_) {
        this.animal = p_25125_;
        this.level = p_25125_.level();
        this.partnerClass = p_25127_;
        this.speedModifier = p_25126_;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.animal.isInLove()) {
            return false;
        } else {
            this.partner = this.getFreePartner();
            return this.partner != null;
        }
    }

    public boolean canContinueToUse() {
        return this.partner.isAlive() && this.partner.isInLove() && this.loveTime < 60 && !this.partner.isPanicking();
    }

    public void stop() {
        this.partner = null;
        this.loveTime = 0;
    }

    public void tick() {
        if (!animal.isTame()) return;
        if ((animal.isFemale() && partner.isMale()) || (animal.isMale() && partner.isFemale())) {
            this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float) this.animal.getMaxHeadXRot());
            this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
            ++this.loveTime;
            if (this.loveTime >= this.adjustedTickDelay(60) && this.animal.distanceToSqr(this.partner) < (double) 9.0F) {
                this.breed();
            }
        }

    }

    @Nullable
    private OWEntity getFreePartner() {
        List<? extends OWEntity> $$0 = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate((double)8.0F));
        double $$1 = Double.MAX_VALUE;
        OWEntity $$2 = null;

        for(OWEntity $$3 : $$0) {
            if (this.animal.canMate($$3) && !$$3.isPanicking() && this.animal.distanceToSqr($$3) < $$1) {
                $$2 = $$3;
                $$1 = this.animal.distanceToSqr($$3);
            }
        }

        return $$2;
    }

    protected void breed() {
        this.animal.spawnBabyOfParents(animal, partner, OWUtils.RANDOM(20));
    }
}
