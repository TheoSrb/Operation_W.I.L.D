package net.tiew.operationWild.screen.blocks;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.ElephantSaddle;
import net.tiew.operationWild.screen.OWMenuRegister;

import java.util.*;

public class SaddlerMenu extends AbstractContainerMenu {
    public static int[] boaSaddleCraftAmount = {22, 8, 26};
    public static Item[] boaSaddleCraftItems = {Items.LEATHER, Items.IRON_INGOT, OWItems.PLANT_FIBER.get()};
    public static int[] tigerSaddleCraftAmount = {38, 24, 16};
    public static Item[] tigerSaddleCraftItems = {Items.LEATHER, Items.IRON_INGOT, OWItems.PLANT_FIBER.get()};
    public static int[] peacockSaddleCraftAmount = {9, 4, 11};
    public static Item[] peacockSaddleCraftItems = {Items.LEATHER, Items.IRON_INGOT, OWItems.PLANT_FIBER.get()};
    public static int[] tigerSharkSaddleCraftAmount = {29, 18, 20};
    public static Item[] tigerSharkSaddleCraftItems = {Items.LEATHER, Items.IRON_INGOT, OWItems.PLANT_FIBER.get()};
    public static int[] elephantSaddleCraftAmount = {21, 44, 18, 18};
    public static Item[] elephantSaddleCraftItems = {Items.GOLD_INGOT, OWItems.PLANT_FIBER.get()};

    private final IItemHandler dataInventory;
    private final Player player;

    public SaddlerMenu(int containerId, Inventory playerInventory, IItemHandler dataInventory) {
        super(OWMenuRegister.SADDLER_MENU.get(), containerId);
        this.dataInventory = dataInventory;
        this.player = playerInventory.player;

        this.addSlot(new SlotItemHandler(dataInventory, 0, 15, 17) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override
            public void setByPlayer(ItemStack itemStack) {
                super.setByPlayer(itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            public boolean isActive() {
                return SaddlerScreen.menuWorking();
            }

            public void setChanged() {
                super.setChanged();
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 1, 33, 17) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override
            public void setByPlayer(ItemStack itemStack) {
                super.setByPlayer(itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            public boolean isActive() {
                return SaddlerScreen.menuWorking();
            }

            public void setChanged() {
                super.setChanged();
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 2, 15, 35) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override
            public void setByPlayer(ItemStack itemStack) {
                super.setByPlayer(itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            public boolean isActive() {
                return SaddlerScreen.menuWorking();
            }

            public void setChanged() {
                super.setChanged();
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 3, 33, 35) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override
            public void setByPlayer(ItemStack itemStack) {
                super.setByPlayer(itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (SaddlerScreen.canShowBoaSaddle) craftWithMultipleItems(boaSaddleCraftItems, boaSaddleCraftAmount, OWItems.BOA_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSaddle) craftWithMultipleItems(tigerSaddleCraftItems, tigerSaddleCraftAmount, OWItems.TIGER_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowPeacockSaddle) craftWithMultipleItems(peacockSaddleCraftItems, peacockSaddleCraftAmount, OWItems.PEACOCK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowTigerSharkSaddle) craftWithMultipleItems(tigerSharkSaddleCraftItems, tigerSharkSaddleCraftAmount, OWItems.TIGER_SHARK_SADDLE.get().getDefaultInstance());
                if (SaddlerScreen.canShowElephantSaddle) craftElephantSaddle();
            }

            public boolean isActive() {
                return SaddlerScreen.menuWorking();
            }

            public void setChanged() {
                super.setChanged();
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 4, 24, 53) {
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
            public boolean isActive() {
                return SaddlerScreen.menuWorking();
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (SaddlerScreen.canShowBoaSaddle) completeMultiItemCraft(boaSaddleCraftAmount, boaSaddleCraftItems);
                if (SaddlerScreen.canShowTigerSaddle) completeMultiItemCraft(tigerSaddleCraftAmount, tigerSaddleCraftItems);
                if (SaddlerScreen.canShowPeacockSaddle) completeMultiItemCraft(peacockSaddleCraftAmount, peacockSaddleCraftItems);
                if (SaddlerScreen.canShowTigerSharkSaddle) completeMultiItemCraft(tigerSharkSaddleCraftAmount, tigerSharkSaddleCraftItems);
                if (SaddlerScreen.canShowElephantSaddle) completeElephantSaddleCraft();
            }

            public void setChanged() {
                super.setChanged();
                if (SaddlerScreen.canShowElephantSaddle) {
                    List<Item> wools = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        ItemStack itemstack = dataInventory.getStackInSlot(i);
                        if (isWoolItem(itemstack.getItem())) {
                            wools.add(itemstack.getItem());
                        }
                    }

                    ItemStack saddle = dataInventory.getStackInSlot(4);
                    if (saddle.getItem() instanceof ElephantSaddle) {
                        saddle.set(OWDataComponentTypes.SADDLE_WOOLS.get(), wools);
                    }
                }
            }
        });

        for(int i1 = 0; i1 < 3; ++i1) {
            for(int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
            }
        }

        for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            dropContents();
        }
    }

    private void dropContents() {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = dataInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                boolean added = false;

                for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                    if (player.getInventory().getItem(j).isEmpty()) {
                        player.getInventory().setItem(j, stack);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    player.drop(stack, false);
                }

                if (dataInventory instanceof ItemStackHandler handler) {
                    handler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public int itemCount = 0;

    public void countItems(Item requiredItem) {
        itemCount = 0;
        for (int i = 0; i <= 3; i++) {
            ItemStack stack = this.getSlot(i).getItem();
            if (stack.is(requiredItem)) {
                itemCount += stack.getCount();
            }
        }
    }

    public void craftCompleted(int maxNumber, Item requiredItem) {
        int remainingToRemove = maxNumber;

        for (int slotIndex = 0; slotIndex <= 3; slotIndex++) {
            if (remainingToRemove <= 0) break;

            Slot currentSlot = this.getSlot(slotIndex);
            ItemStack stackInSlot = currentSlot.getItem();

            if (stackInSlot.is(requiredItem)) {
                int itemsInSlot = stackInSlot.getCount();
                int toRemoveFromSlot = Math.min(itemsInSlot, remainingToRemove);

                if (toRemoveFromSlot > 0) {
                    currentSlot.remove(toRemoveFromSlot);
                    remainingToRemove -= toRemoveFromSlot;
                }
            }
        }

        countItems(requiredItem);
        this.broadcastChanges();
    }

    public void craftWithMultipleItems(Item[] requiredItems, int[] requiredAmounts, ItemStack itemResult) {
        boolean canCraft = true;

        for (int i = 0; i < requiredItems.length; i++) {
            countItems(requiredItems[i]);
            if (itemCount < requiredAmounts[i]) {
                canCraft = false;
                break;
            }
        }

        Slot resultSlot = this.getSlot(4);

        if (canCraft) {
            ItemStack resultCopy = itemResult.copy();
            resultSlot.set(resultCopy);
        } else {
            resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    public void craftElephantSaddle() {
        boolean canCraft = true;

        for (int i = 0; i < elephantSaddleCraftItems.length; i++) {
            countItems(elephantSaddleCraftItems[i]);
            if (itemCount < elephantSaddleCraftAmount[i]) {
                canCraft = false;
                break;
            }
        }

        if (canCraft && !hasTwoDifferentWools()) {
            canCraft = false;
        }

        Slot resultSlot = this.getSlot(4);

        if (canCraft) {
            ItemStack resultCopy = OWItems.ELEPHANT_SADDLE.get().getDefaultInstance();
            resultSlot.set(resultCopy);
        } else {
            resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    public void completeMultiItemCraft(int[] requiredAmounts, Item[] requiredItems) {
        for (int i = 0; i < requiredItems.length; i++) {
            craftCompleted(requiredAmounts[i], requiredItems[i]);
        }
    }

    public void completeElephantSaddleCraft() {
        for (int i = 0; i < elephantSaddleCraftItems.length; i++) {
            craftCompleted(elephantSaddleCraftAmount[i], elephantSaddleCraftItems[i]);
        }

        craftSpecificWoolAmounts();
    }

    public void craftSpecificWoolAmounts() {
        Map<Item, Integer> woolPositions = new LinkedHashMap<>();
        for (int i = 0; i <= 3; i++) {
            ItemStack stack = this.getSlot(i).getItem();
            if (isWoolItem(stack.getItem()) && !woolPositions.containsKey(stack.getItem())) {
                woolPositions.put(stack.getItem(), i);
            }
        }

        List<Item> woolTypes = new ArrayList<>(woolPositions.keySet());
        for (int woolIndex = 0; woolIndex < woolTypes.size() && woolIndex < 2; woolIndex++) {
            Item woolType = woolTypes.get(woolIndex);
            int amountToRemove = elephantSaddleCraftAmount[2 + woolIndex];

            craftSpecificWoolCompleted(amountToRemove, woolType);
        }
    }

    public void craftSpecificWoolCompleted(int maxNumber, Item specificWool) {
        int remainingToRemove = maxNumber;

        for (int slotIndex = 0; slotIndex <= 3; slotIndex++) {
            if (remainingToRemove <= 0) break;

            Slot currentSlot = this.getSlot(slotIndex);
            ItemStack stackInSlot = currentSlot.getItem();

            if (stackInSlot.is(specificWool)) {
                int itemsInSlot = stackInSlot.getCount();
                int toRemoveFromSlot = Math.min(itemsInSlot, remainingToRemove);

                if (toRemoveFromSlot > 0) {
                    currentSlot.remove(toRemoveFromSlot);
                    remainingToRemove -= toRemoveFromSlot;
                }
            }
        }

        this.broadcastChanges();
    }

    public boolean hasTwoDifferentWools() {
        Map<Item, Integer> woolCounts = new HashMap<>();

        for (int i = 0; i <= 3; i++) {
            ItemStack stack = this.getSlot(i).getItem();
            if (isWoolItem(stack.getItem())) {
                woolCounts.put(stack.getItem(), woolCounts.getOrDefault(stack.getItem(), 0) + stack.getCount());
            }
        }

        if (woolCounts.size() < 2) return false;

        List<Integer> counts = new ArrayList<>(woolCounts.values());
        counts.sort(Collections.reverseOrder());

        return counts.get(0) >= 18 && counts.get(1) >= 18;
    }

    private boolean isWoolItem(Item item) {
        return item.toString().contains("wool") || item.toString().contains("_wool");
    }

    public SaddlerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(5));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int id) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}