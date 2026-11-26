package net.tiew.operationWild.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class ChameleonClimbNavigation extends OWGroundPathNavigation {

    private @Nullable BlockPos directTarget;

    public ChameleonClimbNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    public @Nullable BlockPos directTarget() {
        return this.directTarget;
    }

    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        this.directTarget = pos;
        return super.createPath(pos, accuracy);
    }

    @Override
    public Path createPath(Entity entity, int accuracy) {
        this.directTarget = entity.blockPosition();
        return super.createPath(entity, accuracy);
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        Path path = this.createPath(entity, 0);
        if (path != null) return this.moveTo(path, speed);
        this.directTarget = entity.blockPosition();
        this.speedModifier = speed;
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        this.directTarget = null;
    }

    @Override
    public void tick() {
        if (!this.isDone()) {
            super.tick();
            return;
        }
        if (this.directTarget == null) return;

        double reach = Math.max(this.mob.getBbWidth(), 1.0);
        boolean arrived = this.directTarget.closerToCenterThan(this.mob.position(), reach)
                || this.mob.getY() > this.directTarget.getY()
                && BlockPos.containing(this.directTarget.getX(), this.mob.getY(), this.directTarget.getZ())
                .closerToCenterThan(this.mob.position(), reach);
        if (arrived) {
            this.directTarget = null;
            return;
        }
        this.mob.getMoveControl().setWantedPosition(this.directTarget.getX() + 0.5, this.directTarget.getY(),
                this.directTarget.getZ() + 0.5, this.speedModifier);
    }
}
