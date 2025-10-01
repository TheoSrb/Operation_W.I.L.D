package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.EnumSet;
import java.util.List;

public class KodiakAttractedToFoodItemGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float speedModifier;
    private final int radiusToSearch;
    private final float attractionFrequencyMultiplier;
    private final Runnable actionAtTheEnd;
    private final boolean conditionToWork;

    private BlockPos targetPos;

    public KodiakAttractedToFoodItemGoal(KodiakEntity kodiak, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable actionAtTheEnd, boolean conditionToWork) {
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
                !kodiak.isNapping() && conditionToWork && !kodiak.isDirty() && !kodiak.isSearchingInsideChest;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null || !conditionToWork || kodiak.isDirty() || kodiak.isSearchingInsideChest) return false;
        double distance = OWUtils.distanceRest(kodiak, targetPos);
        return distance > 3;
    }

    @Override
    public void tick() {
        if (!kodiak.getFoodPick().isEmpty()) {
            stop();
            return;
        }
        if (targetPos != null) {
            List<ItemEntity> items = kodiak.level().getEntitiesOfClass(ItemEntity.class,
                    new AABB(targetPos).inflate(2.0));

            kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
            kodiak.setSitting(false);
            kodiak.setSniffing(true);
            double distanceBetweenKodiakAndTarget = OWUtils.distanceRest(kodiak, targetPos);
            boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

            if (isArrived && !items.isEmpty()) {
                ItemEntity itemEntity = items.getFirst();
                kodiak.foodPick = itemEntity.getItem().copy();
                itemEntity.discard();

                actionAtTheEnd.run();
                stop();
            } else if (items.isEmpty()) {
                stop();
            }
        }
    }

    private BlockPos findTargetPos() {
        List<ItemEntity> items = kodiak.level().getEntitiesOfClass(ItemEntity.class,
                kodiak.getBoundingBox().inflate(radiusToSearch));
        for (ItemEntity item : items) {
            if (item.getItem().is(Tags.Items.FOODS) || item.getItem().is(Items.HONEYCOMB)) {
                return item.blockPosition();
            }
        }
        return null;
    }
}