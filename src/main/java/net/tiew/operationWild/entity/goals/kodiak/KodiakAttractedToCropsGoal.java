package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.EnumSet;

public class KodiakAttractedToCropsGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float speedModifier;
    private final int radiusToSearch;
    private final float attractionFrequencyMultiplier;
    private final Runnable actionAtTheEnd;
    private final boolean conditionToWork;

    private BlockPos targetPos;

    public KodiakAttractedToCropsGoal(KodiakEntity kodiak, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable actionAtTheEnd, boolean conditionToWork) {
        this.kodiak = kodiak;
        this.speedModifier = speedModifier;
        this.radiusToSearch = radiusToSearch;
        this.conditionToWork = conditionToWork;
        this.actionAtTheEnd = actionAtTheEnd;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void start() {
        targetPos = findTargetPos();
    }

    @Override
    public void stop() {
        super.stop();
        kodiak.setSniffing(false);
    }

    @Override
    public boolean canUse() {
        return kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && !kodiak.isTame() && conditionToWork && !kodiak.isDirty() && !kodiak.isSearchingInsideChest && !kodiak.isCatchingSalmon() && !kodiak.isRubs();
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null || kodiak.isTame() || !conditionToWork || kodiak.isDirty() || kodiak.isSearchingInsideChest) return false;
        BlockState blockState = kodiak.level().getBlockState(targetPos);
        if (!blockState.is(BlockTags.CROPS)) return false;
        double distance = OWUtils.distanceRest(kodiak, targetPos);
        return distance > 3 && !kodiak.isCatchingSalmon() && !kodiak.isRubs();
    }

    @Override
    public void tick() {
        if (!kodiak.getFoodPick().isEmpty()) {
            stop();
            return;
        }
        if (targetPos != null) {
            kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
            kodiak.setSniffing(true);
            kodiak.setSitting(false);
            double distanceBetweenKodiakAndTarget = OWUtils.distanceRest(kodiak, targetPos);
            boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

            if (isArrived) {
                actionAtTheEnd.run();
            }
        }
    }

    private BlockPos findTargetPos() {
        BlockPos kodiakPos = kodiak.blockPosition();

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);
                    BlockState blockState = kodiak.level().getBlockState(pos);
                    if (blockState.is(BlockTags.CROPS)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}