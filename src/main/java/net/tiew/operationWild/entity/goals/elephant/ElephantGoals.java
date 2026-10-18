package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import org.jetbrains.annotations.Nullable;

public final class ElephantGoals {

    private static final int WATER_SEARCH_STEP = 2;
    private static final int MAX_WATER_DROP = 6;
    private static final int SCAN_ABOVE = 3;

    private ElephantGoals() {
    }

    public static boolean isFreeWild(ElephantEntity elephant) {
        return elephant.isAlive() && !elephant.isTame() && !elephant.isVehicle() && !elephant.isPassenger()
                && !elephant.isSleeping() && !elephant.isKnockedOut() && !elephant.isSedatedFleeing()
                && !elephant.isInResurrection() && !elephant.isAlarming() && !elephant.isEarthquakeGesture()
                && !elephant.isShoulderBashing() && !elephant.isSitting() && !elephant.isNapping();
    }

    public static boolean isCalm(ElephantEntity elephant) {
        return isFreeWild(elephant) && elephant.getTarget() == null && !elephant.wild.isDefending()
                && !elephant.sedationResponse.isAlarmed() && !elephant.isSedationAgitated()
                && !elephant.breakout.isActive();
    }

    public static double horizontalDistance(ElephantEntity elephant, Vec3 point) {
        double dx = point.x - elephant.getX();
        double dz = point.z - elephant.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static float yawGap(ElephantEntity elephant, Vec3 point) {
        return Math.abs(net.minecraft.util.Mth.wrapDegrees(elephant.yawTowards(point) - elephant.yBodyRot));
    }

    public static void backOff(ElephantEntity elephant, Vec3 from, double range, double speed) {
        Vec3 away = elephant.position().subtract(from);
        away = new Vec3(away.x, 0, away.z);
        if (away.lengthSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, elephant.yBodyRot + 180f);
        Vec3 destination = new Vec3(from.x, elephant.getY(), from.z).add(away.normalize().scale(range));
        elephant.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
    }

    public record WaterSpot(BlockPos stand, BlockPos water) {
        public Vec3 standCenter() {
            return Vec3.atBottomCenterOf(stand);
        }

        public Vec3 waterCenter() {
            return Vec3.atCenterOf(water);
        }
    }

    @Nullable
    public static WaterSpot findWaterSpot(ElephantEntity elephant, BlockPos center, int radius) {
        Level level = elephant.level();
        WaterSpot best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos origin = elephant.blockPosition();

        for (int dx = -radius; dx <= radius; dx += WATER_SEARCH_STEP) {
            for (int dz = -radius; dz <= radius; dz += WATER_SEARCH_STEP) {
                if (dx * dx + dz * dz > radius * radius) continue;
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) continue;

                BlockPos surface = waterSurface(level, x, z, origin.getY());
                if (surface == null) continue;

                WaterSpot spot = shoreFor(level, surface);
                if (spot == null) continue;

                double distance = spot.stand().distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = spot;
                }
            }
        }
        return best;
    }

    @Nullable
    private static BlockPos waterSurface(Level level, int x, int z, int aroundY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, aroundY + SCAN_ABOVE, z);
        for (int y = aroundY + SCAN_ABOVE; y >= aroundY - MAX_WATER_DROP; y--) {
            cursor.setY(y);
            if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                return level.getFluidState(cursor.above()).isEmpty() ? cursor.immutable() : null;
            }
            if (level.getBlockState(cursor).blocksMotion()) return null;
        }
        return null;
    }

    @Nullable
    private static BlockPos groundTop(Level level, int x, int z, int fromY, int toY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, fromY, z);
        for (int y = fromY; y >= toY; y--) {
            cursor.setY(y);
            if (!level.getFluidState(cursor).isEmpty()) return null;
            if (level.getBlockState(cursor).isFaceSturdy(level, cursor, Direction.UP)) return cursor.immutable();
        }
        return null;
    }

    @Nullable
    private static WaterSpot shoreFor(Level level, BlockPos surface) {
        for (int step = 1; step <= 2; step++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos column = surface.relative(direction, step);
                if (!level.hasChunkAt(column)) continue;
                BlockPos ground = groundTop(level, column.getX(), column.getZ(), surface.getY() + 2, surface.getY() - 1);
                if (ground == null) continue;
                BlockPos stand = ground.above();
                if (!level.getBlockState(stand).getCollisionShape(level, stand).isEmpty()) continue;
                if (!level.getBlockState(stand.above()).getCollisionShape(level, stand.above()).isEmpty()) continue;
                return new WaterSpot(stand, surface);
            }
        }
        return null;
    }
}
