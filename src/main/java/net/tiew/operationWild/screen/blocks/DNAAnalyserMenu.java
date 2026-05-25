package net.tiew.operationWild.screen.blocks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.screen.OWMenuRegister;

public class DNAAnalyserMenu extends AbstractContainerMenu {

    private final IItemHandler dataInventory;
    private final Player player;

    // Slot index constants
    public static final int SYRINGE_SLOT = 0;
    private static final int PLAYER_INV_START = 1;

    public DNAAnalyserMenu(int containerId, Inventory playerInventory, IItemHandler dataInventory) {
        super(OWMenuRegister.DNA_ANALYSER_MENU.get(), containerId);
        this.dataInventory = dataInventory;
        this.player = playerInventory.player;

        // Syringe slot — bottom-left of the content area
        this.addSlot(new SlotItemHandler(dataInventory, 0, 8, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(OWItems.BLOODY_SYRINGE.get());
            }
        });

        // Player main inventory (3 rows)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public DNAAnalyserMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(1));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index == SYRINGE_SLOT) {
                // Move from syringe slot → player inventory
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory → syringe slot (only bloody syringes)
                if (stack.is(OWItems.BLOODY_SYRINGE.get())) {
                    if (!this.moveItemStackTo(stack, SYRINGE_SLOT, PLAYER_INV_START, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            // Return syringe to player inventory when the menu is closed
            ItemStack syringe = dataInventory.getStackInSlot(0);
            if (!syringe.isEmpty()) {
                if (!player.addItem(syringe.copy())) {
                    player.drop(syringe.copy(), false);
                }
                if (dataInventory instanceof ItemStackHandler handler) {
                    handler.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
        }
    }
}
