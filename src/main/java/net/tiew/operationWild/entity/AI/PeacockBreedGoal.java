package net.tiew.operationWild.entity.AI;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class PeacockBreedGoal extends Goal {
    private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range((double)8.0F).ignoreLineOfSight();
    protected final PeacockEntity animal;
    private final Class<? extends PeacockEntity> partnerClass;
    protected final Level level;
    @Nullable
    protected PeacockEntity partner;
    private int loveTime;
    private final double speedModifier;

    public PeacockBreedGoal(PeacockEntity p_25122_, double p_25123_) {
        this(p_25122_, p_25123_, p_25122_.getClass());
    }

    public PeacockBreedGoal(PeacockEntity p_25125_, double p_25126_, Class<? extends PeacockEntity> p_25127_) {
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
        this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
        this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
        ++this.loveTime;
        if (this.loveTime >= this.adjustedTickDelay(60) && this.animal.distanceToSqr(this.partner) < (double)9.0F) {
            this.breed();
        }

    }

    @Nullable
    private PeacockEntity getFreePartner() {
        List<? extends PeacockEntity> $$0 = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate((double)8.0F));
        double $$1 = Double.MAX_VALUE;
        PeacockEntity $$2 = null;

        for(PeacockEntity $$3 : $$0) {
            if (this.animal.canMate($$3) && !$$3.isPanicking() && this.animal.distanceToSqr($$3) < $$1) {
                $$2 = $$3;
                $$1 = this.animal.distanceToSqr($$3);
            }
        }

        return $$2;
    }

    protected void breed() {
        this.animal.createEgg(partner != null ? this.partner : this.animal);
    }
}