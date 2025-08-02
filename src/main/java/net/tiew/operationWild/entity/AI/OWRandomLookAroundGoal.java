package net.tiew.operationWild.entity.AI;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWRandomLookAroundGoal extends Goal {
    private final OWEntity owEntity;
    private double relX;
    private double relZ;
    private int lookTime;

    public OWRandomLookAroundGoal(OWEntity p_25720_) {
        this.owEntity = p_25720_;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        return this.owEntity.getRandom().nextFloat() < 0.02F;
    }

    public boolean canContinueToUse() {
        return this.lookTime >= 0;
    }

    public void start() {
        double $$0 = (Math.PI * 2D) * this.owEntity.getRandom().nextDouble();
        this.relX = Math.cos($$0);
        this.relZ = Math.sin($$0);
        this.lookTime = 20 + this.owEntity.getRandom().nextInt(20);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        if (owEntity.isSleeping() || owEntity.isNapping() || owEntity.isInResurrection()) return;
        --this.lookTime;
        this.owEntity.getLookControl().setLookAt(this.owEntity.getX() + this.relX, this.owEntity.getEyeY(), this.owEntity.getZ() + this.relZ);
    }
}
