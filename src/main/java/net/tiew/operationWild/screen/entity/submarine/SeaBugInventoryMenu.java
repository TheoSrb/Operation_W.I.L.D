package net.tiew.operationWild.screen.entity.submarine;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.screen.OWMenuRegister;
import net.tiew.operationWild.sound.OWSounds;

public class SeaBugInventoryMenu extends AbstractContainerMenu {

    private SeaBugEntity seaBug;

    public SeaBugInventoryMenu(int containerId, Inventory playerInventory, IItemHandler dataInventory) {
        super(OWMenuRegister.SEABUG_INVENTORY_MENU.get(), containerId);
        if (!(playerInventory.player.getRootVehicle() instanceof SeaBugEntity)) return;
        seaBug = (SeaBugEntity) playerInventory.player.getRootVehicle();


        this.addSlot(new SlotItemHandler(dataInventory, 0, 8, 63) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(OWItems.BATTERY.get());
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                seaBug.playSound(SoundEvents.HORSE_SADDLE, 1.0f, 2.0f);
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 1, 152, 63) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(OWItems.BATTERY.get());
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                seaBug.playSound(SoundEvents.HORSE_SADDLE, 1.0f, 2.0f);
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 2, 47, 12) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ItemStack stack = this.getItem();

                if (!stack.isEmpty()) {
                    seaBug.playSound(OWSounds.SUBMARINE_AMELIORATION.get(), 1.0f, 2.0f);
                }
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 3, 47, 36) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ItemStack stack = this.getItem();

                if (!stack.isEmpty()) {
                    seaBug.playSound(OWSounds.SUBMARINE_AMELIORATION.get(), 1.0f, 2.0f);
                }
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 4, 114, 12) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ItemStack stack = this.getItem();

                if (!stack.isEmpty()) {
                    seaBug.playSound(OWSounds.SUBMARINE_AMELIORATION.get(), 1.0f, 2.0f);
                }
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 5, 114, 36) {
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            public boolean isActive() {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ItemStack stack = this.getItem();

                if (!stack.isEmpty()) {
                    seaBug.playSound(OWSounds.SUBMARINE_AMELIORATION.get(), 1.0f, 2.0f);
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

    public SeaBugInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(15));
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
