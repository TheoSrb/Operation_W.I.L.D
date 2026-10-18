package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantMusthTargetGoal extends Goal {

    private static final double PLAYER_RANGE = 14.0;
    private static final double BEAST_RANGE = 10.0;
    private static final double GIVE_UP_RANGE = 28.0;
    private static final int SCAN_INTERVAL = 20;
    private static final int RAGE_TICKS = 400;

    private final ElephantEntity elephant;
    @Nullable
    private LivingEntity victim;
    private int scanCooldown;
    private int rageTicks;

    public ElephantMusthTargetGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!elephant.isMusth() || elephant.isTame() || elephant.isVehicle() || elephant.getTarget() != null) return false;
        if (!ElephantHerd.canAnswer(elephant)) return false;
        if (--scanCooldown > 0) return false;
        scanCooldown = SCAN_INTERVAL;

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : elephant.level().getEntitiesOfClass(LivingEntity.class,
                elephant.getBoundingBox().inflate(PLAYER_RANGE))) {
            boolean player = candidate instanceof Player;
            boolean beast = candidate instanceof OWEntity;
            if (!player && !beast) continue;
            if (!ElephantHerd.isValidThreat(elephant, candidate)) continue;
            if (elephant.sedationResponse.blocksTarget(candidate)) continue;
            double distance = elephant.distanceToSqr(candidate);
            double range = player ? PLAYER_RANGE : BEAST_RANGE;
            if (distance > range * range) continue;
            if (!elephant.getSensing().hasLineOfSight(candidate)) continue;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        victim = best;
        return victim != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (victim == null || rageTicks <= 0 || !elephant.isMusth()) return false;
        if (!ElephantHerd.isValidThreat(elephant, victim) || elephant.sedationResponse.blocksTarget(victim)) return false;
        return elephant.distanceToSqr(victim) <= GIVE_UP_RANGE * GIVE_UP_RANGE;
    }

    @Override
    public void start() {
        rageTicks = RAGE_TICKS;
        elephant.setTarget(victim);
    }

    @Override
    public void tick() {
        rageTicks--;
        if (elephant.getTarget() != victim && !elephant.isAlarming()) elephant.setTarget(victim);
    }

    @Override
    public void stop() {
        if (victim != null && elephant.getTarget() == victim && elephant.wild.getDefenseTarget() != victim) {
            elephant.setTarget(null);
        }
        victim = null;
        scanCooldown = SCAN_INTERVAL;
    }
}
