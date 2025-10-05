package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWPanicGoal extends Goal {
    protected final OWEntity mob;
    protected final float speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected boolean isRunning;
    protected double distanceMultiplicator;
    private int percent;

    public OWPanicGoal(OWEntity mob, float speedModifier, double distanceMultiplicator, int percent) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.distanceMultiplicator = distanceMultiplicator;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.percent = percent;
    }

    public boolean canUse() {
        if (this.mob.getSleepBarPercent() < percent && !this.mob.isTame()) return false;
        if (!this.shouldPanic()) return false;
        else return this.findRandomPosition();
    }

    protected boolean shouldPanic() { return !this.mob.isTame();}

    protected boolean findRandomPosition() {
        Vec3 $$0 = DefaultRandomPos.getPos(this.mob, (int) (20 * distanceMultiplicator), (int) (16 * distanceMultiplicator));
        if ($$0 == null) return false;
        else {
            this.posX = $$0.x;
            this.posY = $$0.y;
            this.posZ = $$0.z;
            return true;
        }
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
        this.isRunning = true;
    }

    public void stop() {
        this.isRunning = false;
    }

    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.mob.isTame();
    }
}
