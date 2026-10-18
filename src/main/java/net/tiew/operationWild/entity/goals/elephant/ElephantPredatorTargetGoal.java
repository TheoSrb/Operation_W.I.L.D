package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class ElephantPredatorTargetGoal extends Goal {

    private static final double GUARD_RANGE = 12.0;
    private static final double HERD_SEARCH_RANGE = 24.0;
    private static final double CHASE_LEASH = 28.0;
    private static final int SCAN_INTERVAL = 20;
    private static final int CHASE_TICKS = 400;

    private final ElephantEntity elephant;
    @Nullable
    private LivingEntity predator;
    @Nullable
    private Vec3 home;
    private int scanCooldown;
    private int chaseTicks;

    public ElephantPredatorTargetGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public static boolean isPredator(LivingEntity entity) {
        if (entity instanceof OWEntity owEntity && owEntity.isSleeping()) return false;
        return entity instanceof LionEntity || entity instanceof TigerEntity || entity instanceof CrocodileEntity
                || entity instanceof KodiakEntity || entity instanceof Wolf;
    }

    @Override
    public boolean canUse() {
        if (elephant.isTame() || elephant.isVehicle() || elephant.getTarget() != null) return false;
        if (!ElephantHerd.canAnswer(elephant)) return false;
        if (--scanCooldown > 0) return false;
        scanCooldown = SCAN_INTERVAL;

        List<ElephantEntity> herd = ElephantHerd.herdmates(elephant, HERD_SEARCH_RANGE);
        double scan = herd.isEmpty() ? GUARD_RANGE : HERD_SEARCH_RANGE + GUARD_RANGE;

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : elephant.level().getEntitiesOfClass(LivingEntity.class,
                elephant.getBoundingBox().inflate(scan), ElephantPredatorTargetGoal::isPredator)) {
            if (!ElephantHerd.isValidThreat(elephant, candidate)) continue;
            if (elephant.sedationResponse.blocksTarget(candidate)) continue;
            double distance = elephant.distanceToSqr(candidate);
            boolean nearMe = distance <= GUARD_RANGE * GUARD_RANGE;
            boolean nearHerd = herd.stream().anyMatch(mate -> mate.distanceToSqr(candidate) <= GUARD_RANGE * GUARD_RANGE);
            if (!nearMe && !nearHerd) continue;
            if (!nearHerd && !elephant.getSensing().hasLineOfSight(candidate)) continue;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        predator = best;
        return predator != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (predator == null || chaseTicks <= 0 || home == null) return false;
        if (!ElephantHerd.isValidThreat(elephant, predator) || !isPredator(predator)) return false;
        return predator.position().distanceToSqr(home) <= CHASE_LEASH * CHASE_LEASH;
    }

    @Override
    public void start() {
        chaseTicks = CHASE_TICKS;
        home = elephant.position();
        elephant.setTarget(predator);
        elephant.wild.tryAlarm(predator);
    }

    @Override
    public void tick() {
        chaseTicks--;
        if (elephant.getTarget() != predator && !elephant.isAlarming()) elephant.setTarget(predator);
    }

    @Override
    public void stop() {
        if (predator != null && elephant.getTarget() == predator && elephant.wild.getDefenseTarget() != predator) {
            elephant.setTarget(null);
        }
        predator = null;
        home = null;
        scanCooldown = SCAN_INTERVAL;
    }
}
