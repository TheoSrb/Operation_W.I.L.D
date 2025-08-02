package net.tiew.operationWild.screen.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.screen.OWMenuRegister;

public class OWInventoryMenu extends AbstractContainerMenu {

    private OWEntity entity;

    private boolean itemIsMeat(ItemStack item) {
        return OWEntity.CARNIVOROUS_ENTITIES.contains(entity.getType()) ? OWEntity.FOOD_FOR_HEALING_MEAT.contains(item.getItem()) : OWEntity.FOOD_FOR_HEALING_VEGETABLES.contains(item.getItem());
    }

    public OWInventoryMenu(int containerId, Inventory playerInventory, IItemHandler dataInventory) {
        super(OWMenuRegister.OW_INVENTORY_MENU.get(), containerId);
        if (!(playerInventory.player.getRootVehicle() instanceof OWEntity)) return;
        entity = (OWEntity) playerInventory.player.getRootVehicle();


        this.addSlot(new SlotItemHandler(dataInventory, 0, 6, 18) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(chooseSaddleWithEntity(entity)) && !this.hasItem();
            }
            public boolean isActive() {
                return true;
            }

            public void setChanged() {
                super.setChanged();
                if (this.getItem().is(chooseSaddleWithEntity(entity))) {
                    entity.setSaddle(true);
                    entity.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
                } else {
                    entity.setSaddle(false);
                    entity.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
                }
            }
        });

        this.addSlot(new SlotItemHandler(dataInventory, 1, 6, 36) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemIsMeat(itemStack);
            }
            public boolean isActive() {
                return true;
            }

            public void setChanged() {
                super.setChanged();
                if (itemIsMeat(this.getItem())) {
                    entity.setFed(true);
                    entity.setItemFood(this.getItem());
                } else {
                    entity.setFed(false);
                    entity.setItemFood(ItemStack.EMPTY);
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

    public OWInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(2));
    }

    public Item chooseSaddleWithEntity(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return OWItems.TIGER_SADDLE.get();
            case "BoaEntity": return OWItems.BOA_SADDLE.get();
            case "PeacockEntity": return OWItems.PEACOCK_SADDLE.get();
            case "TigerSharkEntity": return OWItems.TIGER_SHARK_SADDLE.get();
            default: return null;
        }
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