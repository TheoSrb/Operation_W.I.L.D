package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.entity.AI.AIKodiak;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class KodiakAttractedToGoal<T> extends Goal {

    private final AIKodiak aiKodiak;
    private final KodiakEntity kodiak;
    private final T target;
    private final float speedModifier;
    private final int radiusToSearch;
    private final float attractionFrequencyMultiplier;
    private final Runnable actionAtTheEnd;
    private final boolean conditionToWork;
    private final AIKodiak.KodiakState associatedState;

    private BlockPos targetPos;
    protected List<ItemStack> campfireItems = new ArrayList<>();
    private ItemStack foodPick = ItemStack.EMPTY;
    private int beeNestCooldown = 0;
    private final int MAX_BEE_NEST_COOLDOWN = 3600;

    public KodiakAttractedToGoal(AIKodiak aiKodiak, KodiakEntity kodiak, T target, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable actionAtTheEnd, boolean conditionToWork) {
        this.aiKodiak = aiKodiak;
        this.kodiak = kodiak;
        this.target = target;
        this.speedModifier = speedModifier;
        this.radiusToSearch = radiusToSearch;
        this.conditionToWork = conditionToWork;
        this.actionAtTheEnd = actionAtTheEnd;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;

        if (target instanceof Block && target == Blocks.BEE_NEST) {
            this.associatedState = AIKodiak.KodiakState.GOING_TO_BEE_NEST;
        } else if (target instanceof Block && target == Blocks.CAMPFIRE) {
            this.associatedState = AIKodiak.KodiakState.GOING_TO_CAMPFIRE;
        } else if (target instanceof Class<?> && target == ItemEntity.class) {
            this.associatedState = AIKodiak.KodiakState.GOING_TO_ITEMS;
        } else if (target instanceof TagKey<?>) {
            this.associatedState = AIKodiak.KodiakState.GOING_TO_CROPS;
        } else {
            this.associatedState = AIKodiak.KodiakState.IDLE;
        }

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void start() {
        targetPos = findTargetPos(target);
        if (targetPos != null) {
            aiKodiak.setKodiakState(associatedState);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (aiKodiak.getKodiakState() == associatedState) {
            aiKodiak.setKodiakState(AIKodiak.KodiakState.IDLE);
        }
    }

    @Override
    public boolean canUse() {
        if (kodiak instanceof AIKodiak aiKodiak) {
            if (aiKodiak.getKodiakState() == AIKodiak.KodiakState.NAPPING) {
                return false;
            }
        }
        if (target instanceof Block && target == Blocks.BEE_NEST && beeNestCooldown > 0) {
            return false;
        }

        return aiKodiak.canStartNewGoal(associatedState) &&
                kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && conditionToWork && !kodiak.isDirty() && !aiKodiak.isSearchingInsideChest;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null || !conditionToWork || kodiak.isDirty() || aiKodiak.isSearchingInsideChest) return false;

        if (aiKodiak.getKodiakState() != associatedState && aiKodiak.isCommittedToGoal()) {
            return false;
        }

        if (target instanceof Block) {
            if (!kodiak.level().getBlockState(targetPos).is((Block) target)) {
                return false;
            }
        }

        double distance = AIKodiak.distanceRest(kodiak, targetPos);

        if (target instanceof Block && ((Block) target) == Blocks.CAMPFIRE) {
            return distance > 2 && kodiak.getFoodPick().isEmpty();
        } else {
            return distance > 3;
        }
    }

    @Override
    public void tick() {
        if (beeNestCooldown > 0) {
            beeNestCooldown--;
        }

        if (!kodiak.getFoodPick().isEmpty()) {
            stop();
            return;
        }
        if (targetPos != null) {
            if (target instanceof Block && ((Block) target) == Blocks.CAMPFIRE) {
                BlockEntity blockEntity = kodiak.level().getBlockEntity(targetPos);
                if (blockEntity instanceof CampfireBlockEntity campfire) {
                    campfireItems = campfire.getItems();
                    if (!campfireItems.isEmpty() || kodiak.level().isNight()) {
                        kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);

                        double distanceBetweenKodiakAndTarget = AIKodiak.distanceRest(kodiak, targetPos);
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
                                aiKodiak.foodPick = foodPick;
                            }
                            actionAtTheEnd.run();
                            stop();
                        }

                    } else {
                        stop();
                        return;
                    }
                }
            } else if (target instanceof Class<?> && target == ItemEntity.class) {
                List<ItemEntity> items = kodiak.level().getEntitiesOfClass(ItemEntity.class,
                        new AABB(targetPos).inflate(2.0));

                kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
                double distanceBetweenKodiakAndTarget = AIKodiak.distanceRest(kodiak, targetPos);
                boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

                if (isArrived && !items.isEmpty()) {
                    ItemEntity itemEntity = items.getFirst();
                    aiKodiak.foodPick = itemEntity.getItem().copy();
                    itemEntity.discard();

                    actionAtTheEnd.run();
                    stop();
                } else if (items.isEmpty()) {
                    stop();
                }
            } else {
                kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
                double distanceBetweenKodiakAndTarget = AIKodiak.distanceRest(kodiak, targetPos);
                boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

                if (isArrived) {
                    if (target instanceof Block && target == Blocks.BEE_NEST) {
                        beeNestCooldown = MAX_BEE_NEST_COOLDOWN;
                    }
                    actionAtTheEnd.run();
                }
            }
        }
    }

    public T getTarget() {
        return target;
    }

    private BlockPos findTargetPos(T target) {
        BlockPos kodiakPos = kodiak.blockPosition();

        if (target instanceof Class<?> && target == ItemEntity.class) {
            List<ItemEntity> items = kodiak.level().getEntitiesOfClass(ItemEntity.class,
                    kodiak.getBoundingBox().inflate(radiusToSearch));
            for (ItemEntity item : items) {
                if (item.getItem().is(Tags.Items.FOODS) || item.getItem().is(Items.HONEYCOMB)) {
                    return item.blockPosition();
                }
            }
        }

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);
                    BlockState blockState = kodiak.level().getBlockState(pos);

                    if (target instanceof Block) {
                        if (kodiak.level().getBlockState(pos).is((Block) target)) {
                            if (kodiak.level().getBlockState(pos).is(Blocks.CAMPFIRE)) {
                                if (AIKodiak.distanceRest(kodiak, pos) >= (float) radiusToSearch / 3) {
                                    return pos;
                                }
                            } else {
                                return pos;
                            }
                        }
                    } else if (target instanceof TagKey<?>) {
                        if (blockState.is((TagKey<Block>) target)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }
}