package net.tiew.operationWild.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class KodiakCheckChestGoal extends Goal {

    private final KodiakEntity kodiak;

    private boolean canCheckChestItem = true;
    private boolean canOpenChest = true;
    private boolean canCloseChest = true;

    private ChestBlockEntity currentChestEntity = null;
    private BlockPos currentChestPos = null;
    private int chestCloseTimer = 0;

    private final int MAX_COOLDOWN = 800;
    private int actualCooldown = MAX_COOLDOWN;

    public List<ItemStack> chestItems = new ArrayList<>();
    public List<ItemStack> foodItems = new ArrayList<>();

    public KodiakCheckChestGoal(KodiakEntity kodiak) {
        this.kodiak = kodiak;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos nearestChest = getNearestChestPos(15);

        if (chestCloseTimer > 0) {
            chestCloseTimer--;

            kodiak.setLookAt(currentChestPos.getX(), currentChestPos.getY(), currentChestPos.getZ());
            kodiak.setDeltaMovement(0, kodiak.getDeltaMovement().y, 0);
            kodiak.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 255, false, false, false));

            if (chestCloseTimer <= 0 && currentChestEntity != null && currentChestPos != null) {
                closeChest(currentChestPos);
            }
        }

        if (actualCooldown >= MAX_COOLDOWN) {
            if (distanceToChest(nearestChest) > 3) {
                kodiak.getNavigation().moveTo(nearestChest.getX(), nearestChest.getY(), nearestChest.getZ(), kodiak.getSpeed() * 7);
            }
            else {
                kodiak.getNavigation().stop();
                if (canOpenChest) {
                    openChest(nearestChest);
                    actualCooldown = 0;
                    chestCloseTimer = 50;
                }
            }
        } else {
            actualCooldown++;
        }
    }

    @Override
    public boolean canUse() {
        return foundChestNearby(15) && !kodiak.isTame() && !kodiak.isBaby() && kodiak.getTarget() == null && !kodiak.isNapping() && kodiak.getFoodChooseFromChest().isEmpty();
    }

    private boolean foundChestNearby(int searchRadius) {
        BlockPos kodiakPos = kodiak.blockPosition();
        Level level = kodiak.level();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = kodiakPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);

                    if (blockState.getBlock() instanceof ChestBlock) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openChest(BlockPos pos) {
        if (pos == null) return;

        Level level = kodiak.level();
        BlockState blockState = level.getBlockState(pos);

        if (!(blockState.getBlock() instanceof ChestBlock)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chestEntity)) {
            return;
        }

        if (canOpenChest) {
            this.currentChestEntity = chestEntity;
            this.currentChestPos = pos;

            canCloseChest = true;
            if (!level.isClientSide) {
                level.blockEvent(pos, blockState.getBlock(), 1, 1);
                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1.0f);
                level.gameEvent(kodiak, GameEvent.CONTAINER_OPEN, pos);

                if (canCheckChestItem) {
                    for (int i = 0; i < chestEntity.getContainerSize(); i++) {
                        chestItems.add(chestEntity.getItem(i));
                    }
                    canCheckChestItem = false;
                }

                for (ItemStack itemStack : chestItems) {
                    if (itemStack.is(Tags.Items.FOODS)) {
                        foodItems.add(itemStack);
                    }
                }
            }
            canOpenChest = false;
        }
    }

    private void closeChest(BlockPos pos) {
        if (pos == null) return;

        Level level = kodiak.level();
        BlockState blockState = level.getBlockState(pos);

        if (canCloseChest && currentChestEntity != null && currentChestPos != null && currentChestPos.equals(pos)) {

            ChestBlockEntity chestEntity = currentChestEntity;

            if (!level.isClientSide) {
                level.blockEvent(pos, blockState.getBlock(), 1, 0);
                level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, 1.0f);
                level.gameEvent(kodiak, GameEvent.CONTAINER_CLOSE, pos);

                if (!foodItems.isEmpty()) {
                    ItemStack foodChoose = foodItems.get(kodiak.getRandom().nextInt(foodItems.size()));
                    kodiak.setFoodChooseFromChest(new ItemStack(foodChoose.getItem(), 1), true);

                    for (int i = 0; i < chestEntity.getContainerSize(); i++) {
                        ItemStack slotStack = chestEntity.getItem(i);
                        if (slotStack.equals(foodChoose)) {
                            slotStack.shrink(1);
                            chestEntity.setItem(i, slotStack);
                            chestEntity.setChanged();
                            level.sendBlockUpdated(pos, blockState, blockState, 3);
                            level.updateNeighbourForOutputSignal(pos, blockState.getBlock());
                            break;
                        }
                    }
                }
            }

            chestItems.clear();
            foodItems.clear();
            canCheckChestItem = true;
            canOpenChest = true;
            canCloseChest = false;
            currentChestEntity = null;
            currentChestPos = null;
            chestCloseTimer = 0;
        }
    }

    public float distanceToChest(BlockPos pos) {
        float f = (float)(kodiak.getX() - pos.getX());
        float f1 = (float)(kodiak.getY() - pos.getY());
        float f2 = (float)(kodiak.getZ() - pos.getZ());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    private BlockPos getNearestChestPos(int searchRadius) {
        BlockPos kodiakPos = kodiak.blockPosition();
        Level level = kodiak.level();
        BlockPos nearestChest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = kodiakPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);

                    if (blockState.getBlock() instanceof ChestBlock) {
                        double distance = kodiakPos.distSqr(checkPos);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            nearestChest = checkPos;
                        }
                    }
                }
            }
        }

        return nearestChest;
    }
}
