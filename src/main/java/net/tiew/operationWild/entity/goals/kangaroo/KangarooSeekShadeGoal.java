package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;

public class KangarooSeekShadeGoal extends Goal {

    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_SAMPLES = 40;
    private static final int TRAVEL_TIMEOUT = 300;
    private static final int RETRY_COOLDOWN = 120;

    private final KangarooEntity kangaroo;
    private final double speedModifier;

    private BlockPos shadePos;
    private int travelTicks;
    private int cooldown;

    public KangarooSeekShadeGoal(KangarooEntity kangaroo, double speedModifier) {
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
        if (kangaroo.isInShade()) return false;

        shadePos = findShade();
        return shadePos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return shadePos != null
                && isAvailable()
                && !kangaroo.isInShade()
                && travelTicks < TRAVEL_TIMEOUT
                && !kangaroo.getNavigation().isDone();
    }

    @Override
    public void start() {
        travelTicks = 0;
        if (shadePos != null) {
            kangaroo.getNavigation().moveTo(shadePos.getX() + 0.5, shadePos.getY(), shadePos.getZ() + 0.5, speedModifier);
        }
    }

    @Override
    public void stop() {
        shadePos = null;
        travelTicks = 0;
        cooldown = RETRY_COOLDOWN;
        kangaroo.getNavigation().stop();
    }

    @Override
    public void tick() {
        travelTicks++;
    }

    private boolean isAvailable() {
        return !kangaroo.isTame()
                && !kangaroo.isVehicle()
                && !kangaroo.isAngry()
                && !kangaroo.isAlerted()
                && !kangaroo.isThumping()
                && !kangaroo.isNapping()
                && !kangaroo.isSitting()
                && !kangaroo.isInWater()
                && kangaroo.getTarget() == null
                && kangaroo.isHotHours()
                && kangaroo.isInHotBiome();
    }

    private BlockPos findShade() {
        BlockPos origin = kangaroo.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < SEARCH_SAMPLES; i++) {
            BlockPos candidate = origin.offset(
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    kangaroo.getRandom().nextInt(5) - 2,
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS);

            if (kangaroo.level().canSeeSky(candidate)) continue;
            if (!kangaroo.level().getBlockState(candidate).isAir()) continue;
            if (kangaroo.level().getBlockState(candidate.below()).isAir()) continue;

            double distance = kangaroo.distanceToSqr(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
            if (distance < bestDistance) {
                best = candidate.immutable();
                bestDistance = distance;
            }
        }

        if (best == null) return null;

        Path path = kangaroo.getNavigation().createPath(best, 0);
        return path != null && path.canReach() ? best : null;
    }
}
