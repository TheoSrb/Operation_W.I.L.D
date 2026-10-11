package net.tiew.operationWild.entity.breakout;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OWEnclosureProbe {

    public static final int RADIUS = 10;
    private static final int REGION_MARGIN = 4;
    private static final int MAX_NODES = 700;
    private static final int REFRESH_TICKS = 20;
    private static final double MOVE_TOLERANCE_SQR = 2.25;
    private static final int REFUGE_COUNT = 4;
    private static final double REFUGE_SPACING_SQR = 4.0;
    private static final int RAYS = 8;
    private static final double RAY_STEP = 0.5;
    private static final double BODY_MARGIN = 0.05;
    private static final double STEP_ALLOWANCE = 0.6;

    public record Result(boolean enclosed, int area, double reach, List<BlockPos> refuges) {
        public static final Result OPEN = new Result(false, 0, Double.MAX_VALUE, List.of());
    }

    private final OWEntity mob;
    private Result last = Result.OPEN;
    private int lastScan = Integer.MIN_VALUE / 2;
    private Vec3 lastPosition = Vec3.ZERO;

    public OWEnclosureProbe(OWEntity mob) {
        this.mob = mob;
    }

    public Result last() {
        return last;
    }

    public Result scan(@Nullable Vec3 danger) {
        boolean fresh = mob.tickCount - lastScan < REFRESH_TICKS
                && mob.position().distanceToSqr(lastPosition) < MOVE_TOLERANCE_SQR;
        if (fresh) return last;
        if (!mob.onGround() && !mob.isInWater() && !mob.isInLava()) return last;
        lastScan = mob.tickCount;
        lastPosition = mob.position();
        last = compute(danger);
        return last;
    }

    public void invalidate() {
        lastScan = Integer.MIN_VALUE / 2;
    }

    private Result compute(@Nullable Vec3 danger) {
        if (!(mob.level() instanceof ServerLevel level)) return Result.OPEN;
        if (hasOpenRay(level)) return Result.OPEN;
        NodeEvaluator evaluator = mob.getNavigation().getNodeEvaluator();
        BlockPos origin = mob.blockPosition();
        int span = RADIUS + REGION_MARGIN;
        PathNavigationRegion region = new PathNavigationRegion(level, origin.offset(-span, -span, -span), origin.offset(span, span, span));
        evaluator.prepare(region, mob);
        try {
            Node start = evaluator.getStart();
            if (start == null) return Result.OPEN;
            LongOpenHashSet seen = new LongOpenHashSet();
            ArrayDeque<Node> queue = new ArrayDeque<>();
            List<BlockPos> visited = new ArrayList<>();
            Node[] buffer = new Node[32];
            seen.add(start.asBlockPos().asLong());
            queue.add(start);
            double reach = 0.0;
            while (!queue.isEmpty()) {
                if (seen.size() > MAX_NODES) return Result.OPEN;
                Node node = queue.poll();
                double dx = node.x + 0.5 - mob.getX();
                double dz = node.z + 0.5 - mob.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance >= RADIUS) return Result.OPEN;
                reach = Math.max(reach, distance);
                visited.add(node.asBlockPos());
                int count = evaluator.getNeighbors(buffer, node);
                for (int i = 0; i < count; i++) {
                    Node next = buffer[i];
                    if (seen.add(next.asBlockPos().asLong())) queue.add(next);
                }
            }
            return new Result(true, seen.size(), reach, pickRefuges(visited, danger));
        } finally {
            evaluator.done();
        }
    }

    private boolean hasOpenRay(ServerLevel level) {
        AABB body = mob.getBoundingBox();
        AABB swept = new AABB(body.minX + BODY_MARGIN, body.minY + STEP_ALLOWANCE, body.minZ + BODY_MARGIN,
                body.maxX - BODY_MARGIN, Math.max(body.minY + STEP_ALLOWANCE + 0.1, body.maxY - BODY_MARGIN), body.maxZ - BODY_MARGIN);
        for (int ray = 0; ray < RAYS; ray++) {
            double angle = ray * Math.PI * 2.0 / RAYS;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            boolean open = true;
            for (double distance = RAY_STEP; distance <= RADIUS && open; distance += RAY_STEP) {
                AABB moved = swept.move(cos * distance, 0.0, sin * distance);
                open = level.hasChunksAt(BlockPos.containing(moved.minX, moved.minY, moved.minZ), BlockPos.containing(moved.maxX, moved.maxY, moved.maxZ))
                        && level.noCollision(moved);
            }
            if (open) return true;
        }
        return false;
    }

    private List<BlockPos> pickRefuges(List<BlockPos> visited, @Nullable Vec3 danger) {
        Vec3 from = danger != null ? danger : mob.position();
        visited.sort(Comparator.comparingDouble((BlockPos pos) -> -horizontalDistanceSqr(pos, from)));
        List<BlockPos> refuges = new ArrayList<>();
        for (BlockPos candidate : visited) {
            boolean spaced = refuges.stream().allMatch(other -> other.distSqr(candidate) >= REFUGE_SPACING_SQR);
            if (!spaced) continue;
            refuges.add(candidate);
            if (refuges.size() >= REFUGE_COUNT) break;
        }
        return List.copyOf(refuges);
    }

    private static double horizontalDistanceSqr(BlockPos pos, Vec3 point) {
        double dx = pos.getX() + 0.5 - point.x;
        double dz = pos.getZ() + 0.5 - point.z;
        return dx * dx + dz * dz;
    }
}
