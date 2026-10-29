package net.tiew.operationWild.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class OWGroundPathNavigation extends GroundPathNavigation {

    private static final int MAX_SKIPS_PER_TICK = 3;
    private static final double MIN_SKIP_REACH = 4.0;
    private static final double MAX_SKIP_RISE = 1.5;

    public OWGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    public double speed() {
        return this.speedModifier;
    }

    @Override
    protected void followThePath() {
        super.followThePath();
        skipPassedNodes();
    }

    private void skipPassedNodes() {
        for (int skips = 0; skips < MAX_SKIPS_PER_TICK; skips++) {
            Path current = this.path;
            if (current == null || current.isDone()) return;
            int index = current.getNextNodeIndex();
            if (index + 1 >= current.getNodeCount()) return;
            Vec3 next = current.getEntityPosAtNode(this.mob, index);
            Vec3 after = current.getEntityPosAtNode(this.mob, index + 1);
            Vec3 position = this.mob.position();
            if (Math.abs(position.y - next.y) > MAX_SKIP_RISE) return;
            if (horizontalSqr(position, next) > reachSqr()) return;
            if (horizontalSqr(position, after) >= horizontalSqr(next, after) && !behind(position, next, after)) return;
            Vec3 body = position.add(0.0, this.mob.getBbHeight() * 0.5, 0.0);
            if (!isClearForMovementBetween(this.mob, body, after, true)) return;
            current.advance();
        }
    }

    private static boolean behind(Vec3 position, Vec3 next, Vec3 after) {
        double nx = next.x - position.x, nz = next.z - position.z;
        double ax = after.x - position.x, az = after.z - position.z;
        return nx * ax + nz * az < 0.0;
    }

    private double reachSqr() {
        double reach = Math.max(MIN_SKIP_REACH, this.mob.getBbWidth());
        return reach * reach;
    }

    private static double horizontalSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }
}
