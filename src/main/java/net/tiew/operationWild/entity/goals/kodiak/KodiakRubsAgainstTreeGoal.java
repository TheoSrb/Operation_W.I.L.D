package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
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
    private int cooldownTicks = 0;
    private static final int COOLDOWN_DURATION = 1000;

    public KodiakRubsAgainstTreeGoal(KodiakEntity kodiak, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable action) {
        this.kodiak = kodiak;
        this.speedModifier = speedModifier;
        this.radiusToSearch = radiusToSearch;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.action = action;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean isAdjacentToLog() {
        BlockPos kodiakPos = kodiak.blockPosition();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = kodiakPos.relative(dir);
            if (kodiak.level().getBlockState(adjacentPos).is(BlockTags.LOGS)) {
                return true;
            }
        }

        if (kodiak.level().getBlockState(kodiakPos.above()).is(BlockTags.LOGS)) {
            return true;
        }
        if (kodiak.level().getBlockState(kodiakPos.below()).is(BlockTags.LOGS)) {
            return true;
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        if (targetPos != null) {
            Vec3 targetVec = Vec3.atCenterOf(targetPos);
            Vec3 direction = targetVec.subtract(kodiak.position()).normalize();
            double distance = kodiak.position().distanceTo(targetVec);

            kodiak.setLookAt(targetVec.x, targetVec.y, targetVec.z);

            if (distance <= 2.0 && isAdjacentToLog()) {
                float blockYaw = kodiak.getYRot();
                kodiak.setRubYaw(blockYaw);

                action.run();

                kodiak.getNavigation().stop();
                cooldownTicks = COOLDOWN_DURATION;
                stop();
                return;
            }

            kodiak.setDeltaMovement(
                    direction.x * speedModifier * 0.1,
                    kodiak.getDeltaMovement().y,
                    direction.z * speedModifier * 0.1
            );
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

        if (kodiak.isRubs() && !isAdjacentToLog()) {
            return false;
        }

        double distance = OWUtils.distanceRest(kodiak, targetPos);
        return distance > 1.5 && !kodiak.isCatchingSalmon() && !kodiak.isTame() && !kodiak.isHungry() && !kodiak.isRubs();
    }

    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) return false;

        return kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && !kodiak.isSearchingInsideChest && !kodiak.isInWater() && !kodiak.isCatchingSalmon() && !kodiak.isTame() && !kodiak.isHungry() && !kodiak.isRubs();
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