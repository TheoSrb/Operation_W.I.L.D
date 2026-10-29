package net.tiew.operationWild.entity.goals.global;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.navigation.OWPathProbe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class OWEscapeScout {

    private static final int LAND_SAMPLES = 10;
    private static final int AWAY_SAMPLES = 7;
    private static final int NARROW_SAMPLES = 6;
    private static final double NARROW_CONE = Math.PI / 6.0;
    private static final double WIDE_CONE = Math.PI * 0.4;
    private static final double HEADING_WEIGHT = 2.0;
    private static final double LENGTH_WEIGHT = 1.0;
    private static final double HEADING_JITTER = 0.1;
    private static final int SWIM_SAMPLES = 8;
    private static final int PATH_CHECKS = 3;
    private static final double GAIN_WEIGHT = 2.0;
    private static final double ALIGN_WEIGHT = 1.5;
    private static final double COVER_WEIGHT = 1.0;
    private static final double WATER_WEIGHT = 1.2;
    private static final double CROWD_WEIGHT = 1.5;
    private static final double JITTER = 0.3;
    private static final double CROWD_RADIUS = 6.0;
    private static final double REACH_TOLERANCE = 2.5;
    private static final double DANGER_CLEARANCE = 3.0;
    private static final int SKIPPED_NODES = 2;
    private static final double EYE_HEIGHT = 1.6;
    private static final float PATH_RANGE_MARGIN = 8f;

    private OWEscapeScout() {
    }

    @Nullable
    public static Vec3 find(OWEntity mob, @Nullable Vec3 danger, @Nullable Vec3 heading, int radius, int height, Predicate<Vec3> rejected) {
        boolean swimmer = mob.isInWater() && (mob instanceof OWWaterEntity || mob instanceof OWSemiWaterEntity);
        if (swimmer) return findSwim(mob, danger, heading, radius, height, rejected);

        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < LAND_SAMPLES; i++) {
            Vec3 sample = sample(mob, danger, heading, radius, height, i);
            if (sample == null || rejected.test(sample)) continue;
            candidates.add(new Candidate(sample, score(mob, sample, danger, heading, radius)));
        }
        if (candidates.isEmpty()) {
            Vec3 fallback = heading != null
                    ? DefaultRandomPos.getPosTowards(mob, Math.max(1, radius / 2), height, mob.position().add(heading.scale(radius)), WIDE_CONE)
                    : DefaultRandomPos.getPos(mob, Math.max(1, radius / 2), height);
            return fallback != null && !rejected.test(fallback) ? fallback : null;
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed());

        Vec3 fallback = null;
        int checks = Math.min(PATH_CHECKS, candidates.size());
        for (int i = 0; i < checks; i++) {
            Vec3 spot = candidates.get(i).position();
            Path path = OWPathProbe.find(mob, BlockPos.containing(spot), radius + PATH_RANGE_MARGIN, 1);
            if (path == null) continue;
            if (reaches(path, spot) && !brushesDanger(path, danger)) return spot;
            if (fallback == null && gainsDistance(mob, path, danger)) fallback = spot;
        }
        return fallback;
    }

    @Nullable
    private static Vec3 sample(OWEntity mob, @Nullable Vec3 danger, @Nullable Vec3 heading, int radius, int height, int index) {
        if (heading != null) {
            double cone = index < NARROW_SAMPLES ? NARROW_CONE : WIDE_CONE;
            return DefaultRandomPos.getPosTowards(mob, radius, height, mob.position().add(heading.scale(radius)), cone);
        }
        if (danger != null && index < AWAY_SAMPLES) return DefaultRandomPos.getPosAway(mob, radius, height, danger);
        return DefaultRandomPos.getPos(mob, radius, height);
    }

    private static double score(OWEntity mob, Vec3 spot, @Nullable Vec3 danger, @Nullable Vec3 heading, int radius) {
        double score = mob.getRandom().nextDouble() * (heading != null ? HEADING_JITTER : JITTER);
        Vec3 run = flatten(spot.subtract(mob.position()));
        if (danger != null) {
            double gain = (horizontal(spot, danger) - horizontal(mob.position(), danger)) / Math.max(1, radius);
            Vec3 away = flatten(mob.position().subtract(danger));
            score += GAIN_WEIGHT * gain + ALIGN_WEIGHT * run.dot(away);
            if (!visibleFrom(mob, danger.add(0, EYE_HEIGHT, 0), spot.add(0, mob.getBbHeight() * 0.6, 0))) {
                score += COVER_WEIGHT;
            }
        }
        if (heading != null) {
            score += HEADING_WEIGHT * run.dot(heading) + LENGTH_WEIGHT * horizontal(spot, mob.position()) / Math.max(1, radius);
        }
        if (mob instanceof OWSemiWaterEntity && mob.level().getFluidState(BlockPos.containing(spot)).is(FluidTags.WATER)) {
            score += WATER_WEIGHT;
        }
        if (crowded(mob, spot)) score -= CROWD_WEIGHT;
        return score;
    }

    private static boolean crowded(OWEntity mob, Vec3 spot) {
        for (Player player : mob.level().players()) {
            if (player.isSpectator() || player.isCreative()) continue;
            if (player.distanceToSqr(spot) < CROWD_RADIUS * CROWD_RADIUS) return true;
        }
        return false;
    }

    private static boolean visibleFrom(OWEntity mob, Vec3 eye, Vec3 spot) {
        HitResult hit = mob.level().clip(new ClipContext(eye, spot, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static boolean reaches(Path path, Vec3 spot) {
        if (path.canReach()) return true;
        Node end = path.getEndNode();
        return end != null && Vec3.atBottomCenterOf(end.asBlockPos()).distanceTo(spot) <= REACH_TOLERANCE;
    }

    private static boolean brushesDanger(Path path, @Nullable Vec3 danger) {
        if (danger == null) return false;
        for (int i = SKIPPED_NODES; i < path.getNodeCount(); i++) {
            Node node = path.getNode(i);
            if (horizontal(Vec3.atBottomCenterOf(node.asBlockPos()), danger) < DANGER_CLEARANCE) return true;
        }
        return false;
    }

    private static boolean gainsDistance(OWEntity mob, Path path, @Nullable Vec3 danger) {
        Node end = path.getEndNode();
        if (end == null) return false;
        if (danger == null) return true;
        return horizontal(Vec3.atBottomCenterOf(end.asBlockPos()), danger) > horizontal(mob.position(), danger) + 1.0;
    }

    @Nullable
    private static Vec3 findSwim(OWEntity mob, @Nullable Vec3 danger, @Nullable Vec3 heading, int radius, int height, Predicate<Vec3> rejected) {
        Vec3 best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < SWIM_SAMPLES; i++) {
            Vec3 candidate = BehaviorUtils.getRandomSwimmablePos(mob, radius, height);
            if (candidate == null || rejected.test(candidate)) continue;
            if (danger == null && heading == null) return candidate;
            double score = heading != null
                    ? (candidate.x - mob.getX()) * heading.x + (candidate.z - mob.getZ()) * heading.z
                    : candidate.distanceToSqr(danger);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
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

    private record Candidate(Vec3 position, double score) {
    }
}
