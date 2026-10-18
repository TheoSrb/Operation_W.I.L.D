package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantGrudgeTargetGoal extends Goal {

    private static final double RECOGNITION_RANGE = 24.0;
    private static final double GIVE_UP_RANGE = 36.0;
    private static final int SCAN_INTERVAL = 20;
    private static final int PURSUIT_TICKS = 700;
    private static final int UNSEEN_LIMIT = 120;

    private final ElephantEntity elephant;
    @Nullable
    private LivingEntity enemy;
    private int scanCooldown;
    private int pursuitTicks;
    private int unseenTicks;

    public ElephantGrudgeTargetGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (elephant.isTame() || elephant.isVehicle() || !elephant.wild.hasGrudges()) return false;
        if (elephant.getTarget() != null || !ElephantHerd.canAnswer(elephant)) return false;
        if (--scanCooldown > 0) return false;
        scanCooldown = SCAN_INTERVAL;

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : elephant.level().getEntitiesOfClass(LivingEntity.class,
                elephant.getBoundingBox().inflate(RECOGNITION_RANGE))) {
            if (!elephant.wild.holdsGrudgeAgainst(candidate)) continue;
            if (!ElephantHerd.isValidThreat(elephant, candidate)) continue;
            if (elephant.sedationResponse.blocksTarget(candidate)) continue;
            if (!elephant.getSensing().hasLineOfSight(candidate)) continue;
            double distance = elephant.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        enemy = best;
        return enemy != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (enemy == null || pursuitTicks <= 0 || unseenTicks > UNSEEN_LIMIT) return false;
        if (!ElephantHerd.isValidThreat(elephant, enemy) || elephant.sedationResponse.blocksTarget(enemy)) return false;
        return elephant.distanceToSqr(enemy) <= GIVE_UP_RANGE * GIVE_UP_RANGE;
    }

    @Override
    public void start() {
        pursuitTicks = PURSUIT_TICKS;
        unseenTicks = 0;
        elephant.setTarget(enemy);
        elephant.wild.tryAlarm(enemy);
    }

    @Override
    public void tick() {
        pursuitTicks--;
        if (elephant.getSensing().hasLineOfSight(enemy)) unseenTicks = 0;
        else unseenTicks++;
        if (elephant.getTarget() != enemy && !elephant.isAlarming()) elephant.setTarget(enemy);
    }

    @Override
    public void stop() {
        if (enemy != null && elephant.getTarget() == enemy && elephant.wild.getDefenseTarget() != enemy) {
            elephant.setTarget(null);
        }
        enemy = null;
        scanCooldown = SCAN_INTERVAL;
    }
}
