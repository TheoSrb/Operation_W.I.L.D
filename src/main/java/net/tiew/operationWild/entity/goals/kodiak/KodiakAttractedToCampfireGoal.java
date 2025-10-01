package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class KodiakAttractedToCampfireGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float speedModifier;
    private final int radiusToSearch;
    private final float attractionFrequencyMultiplier;
    private final Runnable actionAtTheEnd;
    private final boolean conditionToWork;

    private BlockPos targetPos;
    protected List<ItemStack> campfireItems = new ArrayList<>();
    private ItemStack foodPick = ItemStack.EMPTY;

    public KodiakAttractedToCampfireGoal(KodiakEntity kodiak, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable actionAtTheEnd, boolean conditionToWork) {
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
        boolean random = kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0;
        boolean noTarget = kodiak.getTarget() == null;
        boolean onGround = kodiak.onGround();
        boolean notNapping = !kodiak.isNapping();
        boolean notDirty = !kodiak.isDirty();
        boolean notSearching = !kodiak.isSearchingInsideChest;

        return random && noTarget && onGround && notNapping && conditionToWork && notDirty && notSearching;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) {
            return false;
        }
        if (!conditionToWork) {
            return false;
        }
        if (kodiak.isDirty()) {
            return false;
        }
        if (kodiak.isSearchingInsideChest) {
            return false;
        }
        if (!kodiak.level().getBlockState(targetPos).is(Blocks.CAMPFIRE)) {
            return false;
        }
        double distance = OWUtils.distanceRest(kodiak, targetPos);
        boolean canContinue = distance > 2 && kodiak.getFoodPick().isEmpty();
        return canContinue;
    }

    @Override
    public void tick() {
        if (!kodiak.getFoodPick().isEmpty()) {
            stop();
            return;
        }
        if (targetPos != null) {
            BlockEntity blockEntity = kodiak.level().getBlockEntity(targetPos);
            if (blockEntity instanceof CampfireBlockEntity campfire) {
                campfireItems = campfire.getItems();
                if (!campfireItems.isEmpty() || kodiak.level().isNight()) {
                    kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
                    if (!campfireItems.isEmpty()) {
                        kodiak.setSniffing(true);
                    }

                    double distanceBetweenKodiakAndTarget = OWUtils.distanceRest(kodiak, targetPos);
                    boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

                    if (isArrived) {
                        if (campfireItems != null) {
                            int $$0 = kodiak.getRandom().nextInt(campfireItems.size());
                            foodPick = campfireItems.get($$0);
                            campfire.getItems().set($$0, ItemStack.EMPTY);
                            campfire.setChanged();
                            if (!kodiak.level().isClientSide) {
                                BlockState state = kodiak.level().getBlockState(targetPos);
                                kodiak.level().sendBlockUpdated(targetPos, state, state, Block.UPDATE_CLIENTS);
                            }
                            kodiak.foodPick = foodPick;
                        }
                        actionAtTheEnd.run();
                        stop();
                    }
                } else {
                    stop();
                }
            }
        }
    }

    private BlockPos findTargetPos() {
        BlockPos kodiakPos = kodiak.blockPosition();

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);
                    if (kodiak.level().getBlockState(pos).is(Blocks.CAMPFIRE)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}