package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;

public class KangarooFleeToWaterGoal extends Goal {

    private static final float HEALTH_THRESHOLD = 0.45f;
    private static final double PURSUER_RANGE = 20.0;
    private static final int SEARCH_RADIUS = 18;
    private static final int SEARCH_SAMPLES = 64;
    private static final int TRAVEL_TIMEOUT = 400;
    private static final int RETRY_COOLDOWN = 60;

    private final KangarooEntity kangaroo;
    private final double speedModifier;

    private BlockPos waterPos;
    private LivingEntity pursuer;
    private int travelTicks;
    private int cooldown;

    public KangarooFleeToWaterGoal(KangarooEntity kangaroo, double speedModifier) {
        this.kangaroo = kangaroo;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;

        pursuer = findPursuer();
        if (pursuer == null) return false;

        waterPos = findDeepWater();
        return waterPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return waterPos != null
                && isAvailable()
                && pursuer != null
                && pursuer.isAlive()
                && travelTicks < TRAVEL_TIMEOUT
                && !kangaroo.isUnderWater();
    }

    @Override
    public void start() {
        travelTicks = 0;
        kangaroo.setSitting(false);
        kangaroo.setNap(false);
        if (waterPos != null) {
            kangaroo.getNavigation().moveTo(waterPos.getX() + 0.5, waterPos.getY(), waterPos.getZ() + 0.5, speedModifier);
        }
    }

    @Override
    public void stop() {
        waterPos = null;
        pursuer = null;
        travelTicks = 0;
        cooldown = RETRY_COOLDOWN;
        kangaroo.getNavigation().stop();
    }

    @Override
    public void tick() {
        travelTicks++;

        if (waterPos == null) return;

        if (kangaroo.getNavigation().isDone()) {
            kangaroo.getNavigation().moveTo(waterPos.getX() + 0.5, waterPos.getY(), waterPos.getZ() + 0.5, speedModifier);
        }
    }

    private boolean isAvailable() {
        return !kangaroo.isTame()
                && !kangaroo.isBaby()
                && !kangaroo.isVehicle()
                && !kangaroo.isDrowningSomeone()
                && kangaroo.getHealth() < kangaroo.getMaxHealth() * HEALTH_THRESHOLD;
    }

    private LivingEntity findPursuer() {
        LivingEntity candidate = kangaroo.getLastHurtByMob();
        if (candidate == null) candidate = kangaroo.getTarget();
        if (candidate == null || !candidate.isAlive()) return null;
        if (kangaroo.distanceToSqr(candidate) > PURSUER_RANGE * PURSUER_RANGE) return null;
        return candidate;
    }

    private BlockPos findDeepWater() {
        BlockPos origin = kangaroo.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < SEARCH_SAMPLES; i++) {
            BlockPos candidate = origin.offset(
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    kangaroo.getRandom().nextInt(7) - 3,
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS);

            if (!isDeepWater(candidate)) continue;

            double distance = kangaroo.distanceToSqr(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
            if (distance < bestDistance) {
                best = candidate.immutable();
                bestDistance = distance;
            }
        }

        if (best == null) return null;

        Path path = kangaroo.getNavigation().createPath(best, 0);
        return path != null ? best : null;
    }

    private boolean isDeepWater(BlockPos pos) {
        return kangaroo.level().getFluidState(pos).is(FluidTags.WATER)
                && kangaroo.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }
}
