package net.tiew.operationWild.entity.breakout;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class OWBreachPlanner {

    private static final int SEARCH_RADIUS = OWEnclosureProbe.RADIUS + 4;
    private static final int VERTICAL_RANGE = 6;
    private static final int MAX_EXPANSIONS = 3000;
    private static final int MAX_TARGET_EXPANSIONS = 6000;
    private static final int TARGET_MARGIN = 6;
    private static final double MIN_STRIKE_REACH = 2.0;
    private static final double STRIKE_REACH_MARGIN = 1.2;
    private static final double MIN_STRIKE_RISE = 1.0;
    private static final double STRIKE_RISE_MARGIN = 0.5;
    private static final double LINE_STEP = 0.25;
    private static final double GOAL_MARGIN = 0.5;
    private static final int MAX_FALL = 3;
    private static final float STEP_COST = 4f;
    private static final float CLIMB_COST = 4f;
    private static final float FALL_COST = 2f;
    private static final float STRIKE_COST = 26f;
    private static final float DANGER_STEP_COST = 8f;
    private static final double DANGER_RADIUS = 16.0;
    private static final float MAX_PLAN_COST = 6000f;
    private static final float BLOCKED = Float.POSITIVE_INFINITY;
    private static final long NO_PARENT = Long.MIN_VALUE;
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[] RISES = {0, 1, -1};

    public record Step(BlockPos from, BlockPos to, List<BlockPos> blocks) {
    }

    public record Plan(List<Step> steps, float cost, BlockPos end) {
    }

    private final OWEntity mob;
    private final ServerLevel level;
    private final OWBlockDamageTracker tracker;
    private final int breakLevel;
    private final float strikePower;
    @Nullable
    private final Vec3 anchor;
    private final boolean seek;
    private final int footprint;
    private final int height;
    private final boolean waterBound;
    private final BlockPos origin;
    private final double escapeDistance;
    @Nullable
    private final LivingEntity target;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final int minY;
    private final int maxY;
    private final int maxExpansions;
    private final double strikeReach;
    private final double strikeRise;
    private final Long2FloatOpenHashMap bodyCost = new Long2FloatOpenHashMap();
    private final Long2FloatOpenHashMap feetCost = new Long2FloatOpenHashMap();

    private OWBreachPlanner(OWEntity mob, ServerLevel level, int breakLevel, float strikePower,
                            @Nullable Vec3 anchor, boolean seek, double escapeDistance, @Nullable LivingEntity target) {
        this.mob = mob;
        this.level = level;
        this.tracker = OWBlockDamageTracker.of(level);
        this.breakLevel = breakLevel;
        this.strikePower = Math.max(0.01f, strikePower);
        this.anchor = anchor;
        this.seek = seek;
        this.footprint = Mth.floor(mob.getBbWidth() + 1.0f);
        this.height = Mth.ceil(mob.getBbHeight());
        this.waterBound = mob instanceof OWWaterEntity;
        this.origin = startNode(mob, footprint);
        this.escapeDistance = escapeDistance;
        this.target = target;
        this.strikeReach = Math.max(MIN_STRIKE_REACH, mob.getBbWidth() * 0.5 + STRIKE_REACH_MARGIN) - GOAL_MARGIN;
        this.strikeRise = Math.max(MIN_STRIKE_RISE, mob.getBbHeight() - STRIKE_RISE_MARGIN);
        if (target == null) {
            this.minX = origin.getX() - SEARCH_RADIUS;
            this.maxX = origin.getX() + SEARCH_RADIUS;
            this.minZ = origin.getZ() - SEARCH_RADIUS;
            this.maxZ = origin.getZ() + SEARCH_RADIUS;
            this.minY = origin.getY() - VERTICAL_RANGE;
            this.maxY = origin.getY() + VERTICAL_RANGE;
            this.maxExpansions = MAX_EXPANSIONS;
        } else {
            BlockPos aim = target.blockPosition();
            this.minX = Math.min(origin.getX(), aim.getX()) - TARGET_MARGIN;
            this.maxX = Math.max(origin.getX(), aim.getX()) + TARGET_MARGIN;
            this.minZ = Math.min(origin.getZ(), aim.getZ()) - TARGET_MARGIN;
            this.maxZ = Math.max(origin.getZ(), aim.getZ()) + TARGET_MARGIN;
            this.minY = Math.min(origin.getY(), aim.getY()) - VERTICAL_RANGE;
            this.maxY = Math.max(origin.getY(), aim.getY()) + VERTICAL_RANGE;
            this.maxExpansions = MAX_TARGET_EXPANSIONS;
        }
        this.bodyCost.defaultReturnValue(Float.NaN);
        this.feetCost.defaultReturnValue(Float.NaN);
    }

    @Nullable
    public static Plan plan(OWEntity mob, int breakLevel, float strikePower, @Nullable Vec3 anchor, boolean seek, double escapeDistance) {
        if (!(mob.level() instanceof ServerLevel level)) return null;
        return new OWBreachPlanner(mob, level, breakLevel, strikePower, anchor, seek, escapeDistance, null).search();
    }

    @Nullable
    public static Plan planTo(OWEntity mob, int breakLevel, float strikePower, LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel level)) return null;
        return new OWBreachPlanner(mob, level, breakLevel, strikePower, null, false, 0.0, target).search();
    }

    public static BlockPos startNode(OWEntity mob, int footprint) {
        double half = footprint / 2.0;
        return BlockPos.containing(mob.getX() - half + 0.5, mob.getY() + 0.05, mob.getZ() - half + 0.5);
    }

    public static Vec3 nodeCenter(BlockPos node, int footprint) {
        double half = footprint / 2.0;
        return new Vec3(node.getX() + half, node.getY(), node.getZ() + half);
    }

    @Nullable
    private Plan search() {
        Long2FloatOpenHashMap best = new Long2FloatOpenHashMap();
        best.defaultReturnValue(Float.POSITIVE_INFINITY);
        Long2LongOpenHashMap parent = new Long2LongOpenHashMap();
        parent.defaultReturnValue(NO_PARENT);
        PriorityQueue<Frontier> open = new PriorityQueue<>(Comparator.comparingDouble(Frontier::priority));

        long start = origin.asLong();
        best.put(start, 0f);
        open.add(new Frontier(start, 0f, estimate(origin)));
        int expansions = 0;

        while (!open.isEmpty()) {
            Frontier current = open.poll();
            if (current.cost > best.get(current.key)) continue;
            if (current.cost > MAX_PLAN_COST) return null;
            BlockPos node = BlockPos.of(current.key);
            if (isGoal(node, current.key == start)) return rebuild(parent, current.key, current.cost);
            if (++expansions > maxExpansions) return null;

            for (int[] direction : DIRECTIONS) {
                for (int rise : RISES) {
                    BlockPos next = node.offset(direction[0], rise, direction[1]);
                    Transition transition = transition(node, next);
                    if (transition == null) continue;
                    float cost = current.cost + transition.cost;
                    long key = transition.to.asLong();
                    if (cost >= best.get(key)) continue;
                    best.put(key, cost);
                    parent.put(key, current.key);
                    open.add(new Frontier(key, cost, cost + estimate(transition.to)));
                }
            }
        }
        return null;
    }

    private boolean isGoal(BlockPos node, boolean isStart) {
        if (target == null) return !isStart && isEscape(node);
        return canStrikeFrom(node);
    }

    private float estimate(BlockPos node) {
        if (target == null) return 0f;
        double distance = horizontalDistance(nodeCenter(node, footprint), target.position()) - strikeReach - target.getBbWidth() * 0.5;
        return distance > 0.0 ? (float) distance * STEP_COST : 0f;
    }

    private boolean canStrikeFrom(BlockPos node) {
        Vec3 center = nodeCenter(node, footprint);
        double gap = horizontalDistance(center, target.position()) - target.getBbWidth() * 0.5;
        double rise = target.getY() - node.getY();
        if (gap > strikeReach || rise > strikeRise || rise <= -(mob.getBbHeight() + strikeReach)) return false;
        Vec3 eye = new Vec3(center.x, node.getY() + mob.getEyeHeight(), center.z);
        return clearLine(node, eye, target.getEyePosition());
    }

    private boolean clearLine(BlockPos node, Vec3 from, Vec3 to) {
        double length = from.distanceTo(to);
        int samples = Math.max(1, Mth.ceil(length / LINE_STEP));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long previous = Long.MIN_VALUE;
        for (int i = 1; i < samples; i++) {
            Vec3 point = from.lerp(to, i / (double) samples);
            if (target.getBoundingBox().contains(point)) return true;
            cursor.set(point.x, point.y, point.z);
            long key = cursor.asLong();
            if (key == previous) continue;
            previous = key;
            if (insideBody(node, cursor.getX(), cursor.getY(), cursor.getZ())) continue;
            BlockState state = level.getBlockState(cursor);
            if (OWBlockToughness.isObstacle(state, level, cursor) && !OWBlockToughness.isSteppable(state, level, cursor)) return false;
        }
        return true;
    }

    private boolean isEscape(BlockPos node) {
        Vec3 center = nodeCenter(node, footprint);
        double dx = center.x - mob.getX();
        double dz = center.z - mob.getZ();
        return Math.sqrt(dx * dx + dz * dz) >= escapeDistance && volumeCost(node, null, node.getY(), node.getY() + height - 1, false) == 0f;
    }

    @Nullable
    private Transition transition(BlockPos from, BlockPos to) {
        if (to.getX() < minX || to.getX() > maxX || to.getZ() < minZ || to.getZ() > maxZ) return null;
        if (to.getY() < minY || to.getY() > maxY) return null;
        int rise = to.getY() - from.getY();

        float cost = STEP_COST + approachPenalty(from, to);
        if (rise > 0) {
            float headroom = headroomCost(from, to);
            if (headroom == BLOCKED) return null;
            float body = volumeCost(to, from, to.getY(), to.getY() + height - 1, true);
            if (body == BLOCKED) return null;
            if (!supported(to)) return null;
            return new Transition(to, cost + CLIMB_COST + headroom + body);
        }
        if (rise < 0) {
            float body = volumeCost(to, from, to.getY(), from.getY() + height - 1, true);
            if (body != 0f || !supported(to)) return null;
            return new Transition(to, cost + FALL_COST);
        }

        float body = volumeCost(to, from, to.getY(), to.getY() + height - 1, true);
        if (body == BLOCKED) return null;
        if (supported(to)) return new Transition(to, cost + body);
        if (body != 0f) return null;
        for (int drop = 1; drop <= MAX_FALL; drop++) {
            BlockPos landing = to.below(drop);
            if (landing.getY() < minY) return null;
            if (layerCost(to, landing.getY()) != 0f) return null;
            if (supported(landing)) return new Transition(landing, cost + FALL_COST * (drop + 1));
        }
        return null;
    }

    private float approachPenalty(BlockPos from, BlockPos to) {
        if (anchor == null) return 0f;
        double before = horizontalDistance(nodeCenter(from, footprint), anchor);
        double after = horizontalDistance(nodeCenter(to, footprint), anchor);
        if (seek) return after > before ? DANGER_STEP_COST * 0.5f : 0f;
        if (after >= before || after > DANGER_RADIUS) return 0f;
        return DANGER_STEP_COST * (float) (1.0 - after / DANGER_RADIUS);
    }

    private float volumeCost(BlockPos node, @Nullable BlockPos previous, int minY, int maxY, boolean skipPrevious) {
        float total = 0f;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = node.getX(); x < node.getX() + footprint; x++) {
            for (int z = node.getZ(); z < node.getZ() + footprint; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (skipPrevious && previous != null && insideBody(previous, x, y, z)) continue;
                    cursor.set(x, y, z);
                    float cost = y == node.getY() ? feetCost(cursor) : bodyCost(cursor);
                    if (cost == BLOCKED) return BLOCKED;
                    total += cost;
                }
            }
        }
        return total;
    }

    private float layerCost(BlockPos node, int y) {
        float total = 0f;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = node.getX(); x < node.getX() + footprint; x++) {
            for (int z = node.getZ(); z < node.getZ() + footprint; z++) {
                float cost = bodyCost(cursor.set(x, y, z));
                if (cost == BLOCKED) return BLOCKED;
                total += cost;
            }
        }
        return total;
    }

    private float headroomCost(BlockPos from, BlockPos to) {
        int y = from.getY() + height;
        float total = 0f;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = from.getX(); x < from.getX() + footprint; x++) {
            for (int z = from.getZ(); z < from.getZ() + footprint; z++) {
                if (insideFootprint(to, x, z)) continue;
                float cost = bodyCost(cursor.set(x, y, z));
                if (cost == BLOCKED) return BLOCKED;
                total += cost;
            }
        }
        return total;
    }

    private boolean insideFootprint(BlockPos node, int x, int z) {
        return x >= node.getX() && x < node.getX() + footprint && z >= node.getZ() && z < node.getZ() + footprint;
    }

    private boolean insideBody(BlockPos node, int x, int y, int z) {
        return x >= node.getX() && x < node.getX() + footprint
                && z >= node.getZ() && z < node.getZ() + footprint
                && y >= node.getY() && y < node.getY() + height;
    }

    private float feetCost(BlockPos pos) {
        long key = pos.asLong();
        float cached = feetCost.get(key);
        if (!Float.isNaN(cached)) return cached;
        BlockState state = level.getBlockState(pos);
        float cost;
        if (waterBound) {
            if (state.getFluidState().is(FluidTags.WATER) && !OWBlockToughness.isObstacle(state, level, pos)) cost = 0f;
            else cost = OWBlockToughness.isObstacle(state, level, pos) ? bodyCost(pos) : BLOCKED;
        } else {
            cost = OWBlockToughness.isSteppable(state, level, pos) && !OWBlockToughness.isHazard(state) ? 0f : bodyCost(pos);
        }
        feetCost.put(key, cost);
        return cost;
    }

    private float bodyCost(BlockPos pos) {
        long key = pos.asLong();
        float cached = bodyCost.get(key);
        if (!Float.isNaN(cached)) return cached;
        float cost = computeBlockCost(pos);
        bodyCost.put(key, cost);
        return cost;
    }

    private float computeBlockCost(BlockPos pos) {
        if (!level.isLoaded(pos)) return BLOCKED;
        BlockState state = level.getBlockState(pos);
        if (OWBlockToughness.isHazard(state)) return BLOCKED;
        int tier = OWBlockToughness.tierOf(state, level, pos);
        if (tier == OWBlockToughness.PASSABLE) return 0f;
        if (!OWBreakLevel.canBreak(breakLevel, tier) || !state.canEntityDestroy(level, pos, mob)) return BLOCKED;
        float power = strikePower * OWBreakLevel.advantage(breakLevel, tier);
        int strikes = Mth.ceil(tracker.remainingHealth(pos, state) / power);
        return Math.max(1, strikes) * STRIKE_COST;
    }

    private boolean supported(BlockPos node) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = node.getX(); x < node.getX() + footprint; x++) {
            for (int z = node.getZ(); z < node.getZ() + footprint; z++) {
                BlockState feet = level.getBlockState(cursor.set(x, node.getY(), z));
                if (feet.getFluidState().is(FluidTags.WATER)) return true;
                boolean obstacle = OWBlockToughness.isObstacle(feet, level, cursor);
                if (waterBound) {
                    if (obstacle) return true;
                    continue;
                }
                if (obstacle && OWBlockToughness.isSteppable(feet, level, cursor)) return true;
                BlockState below = level.getBlockState(cursor.set(x, node.getY() - 1, z));
                if (OWBlockToughness.isObstacle(below, level, cursor) && !OWBlockToughness.isHazard(below)) return true;
            }
        }
        return false;
    }

    private Plan rebuild(Long2LongOpenHashMap parent, long goal, float cost) {
        List<BlockPos> path = new ArrayList<>();
        long key = goal;
        while (key != NO_PARENT) {
            path.add(BlockPos.of(key));
            key = parent.get(key);
        }
        Collections.reverse(path);

        List<Step> steps = new ArrayList<>();
        for (int i = 1; i < path.size(); i++) {
            BlockPos from = path.get(i - 1);
            BlockPos to = path.get(i);
            List<BlockPos> blocks = obstaclesBetween(from, to);
            if (!blocks.isEmpty()) steps.add(new Step(from, to, blocks));
        }
        return new Plan(List.copyOf(steps), cost, BlockPos.of(goal));
    }

    private List<BlockPos> obstaclesBetween(BlockPos from, BlockPos to) {
        List<BlockPos> blocks = new ArrayList<>();
        int rise = to.getY() - from.getY();
        if (rise > 0) collectHeadroom(from, to, blocks);
        for (int x = to.getX(); x < to.getX() + footprint; x++) {
            for (int z = to.getZ(); z < to.getZ() + footprint; z++) {
                for (int y = to.getY(); y < to.getY() + height; y++) {
                    if (insideBody(from, x, y, z)) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    float cost = y == to.getY() ? feetCost(pos) : bodyCost(pos);
                    if (cost > 0f && cost != BLOCKED) blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    private void collectHeadroom(BlockPos from, BlockPos to, List<BlockPos> blocks) {
        int y = from.getY() + height;
        for (int x = from.getX(); x < from.getX() + footprint; x++) {
            for (int z = from.getZ(); z < from.getZ() + footprint; z++) {
                if (insideFootprint(to, x, z)) continue;
                BlockPos pos = new BlockPos(x, y, z);
                float cost = bodyCost(pos);
                if (cost > 0f && cost != BLOCKED) blocks.add(pos);
            }
        }
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private record Frontier(long key, float cost, float priority) {
    }

    private record Transition(BlockPos to, float cost) {
    }
}
