package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.EnumSet;

public class KodiakRubsAgainstTreeGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float speedModifier;
    private final int radiusToSearch;
    private final float attractionFrequencyMultiplier;
    private final Runnable action;

    private BlockPos targetPos;

    public KodiakRubsAgainstTreeGoal(KodiakEntity kodiak, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable action) {
        this.kodiak = kodiak;
        this.speedModifier = speedModifier;
        this.radiusToSearch = radiusToSearch;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.action = action;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void tick() {
        super.tick();

        if (targetPos != null) {
            if (OWUtils.distanceRest(kodiak, targetPos) <= 3) {
                action.run();
                kodiak.getNavigation().stop();
            } else {
                kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        targetPos = findNearestTreeLog();
    }

    @Override
    public void stop() {
        super.stop();
        targetPos = null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null || kodiak.isSearchingInsideChest || kodiak.isInWater()) return false;
        if (!kodiak.level().getBlockState(targetPos).is(BlockTags.LOGS)) return false;
        double distance = OWUtils.distanceRest(kodiak, targetPos);
        return distance > 1.5 && !kodiak.isCatchingSalmon() && !kodiak.isTame();
    }

    @Override
    public boolean canUse() {
        return kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && !kodiak.isSearchingInsideChest && !kodiak.isInWater() && !kodiak.isCatchingSalmon() && !kodiak.isTame();
    }

    private BlockPos findNearestTreeLog() {
        BlockPos kodiakPos = kodiak.blockPosition();
        BlockPos nearestLog = null;
        double shortestDistance = Double.MAX_VALUE;

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);
                    if (kodiak.level().getBlockState(pos).is(BlockTags.LOGS)) {
                        double distance = kodiakPos.distSqr(pos);
                        if (distance < shortestDistance) {
                            shortestDistance = distance;
                            nearestLog = pos;
                        }
                    }
                }
            }
        }
        return nearestLog;
    }
}