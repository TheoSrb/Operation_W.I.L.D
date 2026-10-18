package net.tiew.operationWild.entity.goals.global;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.navigation.OWPathProbe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class OWWaterRefuge {

    public static final int SEARCH_RADIUS = 48;
    private static final int STEP = 2;
    private static final int MAX_RISE = 6;
    private static final int MAX_DROP = 10;
    private static final int MIN_DEPTH = 2;
    private static final int BODY_PROBE = 3;
    private static final int MIN_BODY = 5;
    private static final double DISTANCE_WEIGHT = 1.0;
    private static final double BODY_WEIGHT = 0.6;
    private static final double AWAY_WEIGHT = 6.0;
    private static final double DANGER_MARGIN = 2.0;
    private static final int PATH_CHECKS = 4;
    private static final double REACH_TOLERANCE = 3.0;
    private static final float PATH_RANGE_MARGIN = 8f;

    private OWWaterRefuge() {
    }

    @Nullable
    public static Vec3 find(OWEntity mob, @Nullable Vec3 danger, Predicate<Vec3> rejected) {
        Level level = mob.level();
        BlockPos origin = mob.blockPosition();
        List<Spot> spots = new ArrayList<>();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx += STEP) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz += STEP) {
                if (dx * dx + dz * dz > SEARCH_RADIUS * SEARCH_RADIUS) continue;
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) continue;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
                if (top - origin.getY() > MAX_RISE || origin.getY() - top > MAX_DROP) continue;
                BlockPos surface = new BlockPos(x, top, z);
                if (!isWater(level, surface) || depth(level, surface) < MIN_DEPTH) continue;
                Vec3 spot = Vec3.atBottomCenterOf(surface);
                if (rejected.test(spot) || !awayFrom(mob, spot, danger)) continue;
                int body = body(level, surface);
                if (body < MIN_BODY) continue;
                spots.add(new Spot(spot, score(mob, spot, danger, body)));
            }
        }
        if (spots.isEmpty()) return null;
        spots.sort(Comparator.comparingDouble(Spot::score).reversed());

        int checks = Math.min(PATH_CHECKS, spots.size());
        for (int i = 0; i < checks; i++) {
            Vec3 spot = spots.get(i).position();
            float range = (float) Math.sqrt(mob.position().distanceToSqr(spot)) + PATH_RANGE_MARGIN;
            Path path = OWPathProbe.find(mob, BlockPos.containing(spot), range, 1);
            if (path != null && reaches(level, path, spot)) return spot;
        }
        return null;
    }

    private static boolean isWater(Level level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    private static int depth(Level level, BlockPos surface) {
        int depth = 0;
        BlockPos pos = surface;
        while (depth < MIN_DEPTH + 1 && isWater(level, pos)) {
            depth++;
            pos = pos.below();
        }
        return depth;
    }

    private static int body(Level level, BlockPos surface) {
        int count = 0;
        for (int dx = -BODY_PROBE; dx <= BODY_PROBE; dx += BODY_PROBE) {
            for (int dz = -BODY_PROBE; dz <= BODY_PROBE; dz += BODY_PROBE) {
                if (isWater(level, surface.offset(dx, 0, dz))) count++;
            }
        }
        return count;
    }

    private static boolean awayFrom(OWEntity mob, Vec3 spot, @Nullable Vec3 danger) {
        if (danger == null) return true;
        return horizontal(spot, danger) >= horizontal(spot, mob.position()) + DANGER_MARGIN;
    }

    private static double score(OWEntity mob, Vec3 spot, @Nullable Vec3 danger, int body) {
        double score = BODY_WEIGHT * body - DISTANCE_WEIGHT * horizontal(spot, mob.position()) / STEP;
        if (danger != null) {
            Vec3 run = flatten(spot.subtract(mob.position()));
            Vec3 away = flatten(mob.position().subtract(danger));
            score += AWAY_WEIGHT * run.dot(away);
        }
        return score;
    }

    private static boolean reaches(Level level, Path path, Vec3 spot) {
        if (path.canReach()) return true;
        Node end = path.getEndNode();
        if (end == null) return false;
        BlockPos endPos = end.asBlockPos();
        return Vec3.atBottomCenterOf(endPos).distanceTo(spot) <= REACH_TOLERANCE
                || isWater(level, endPos) || isWater(level, endPos.below());
    }

    private static Vec3 flatten(Vec3 vector) {
        Vec3 flat = new Vec3(vector.x, 0, vector.z);
        return flat.lengthSqr() < 1.0E-6 ? Vec3.ZERO : flat.normalize();
    }

    private static double horizontal(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private record Spot(Vec3 position, double score) {
    }
}
