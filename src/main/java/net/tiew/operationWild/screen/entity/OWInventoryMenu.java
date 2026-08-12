package net.tiew.operationWild.screen.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.ElephantSaddle;
import net.tiew.operationWild.screen.OWMenuRegister;

import java.util.ArrayList;
import java.util.List;

public class OWInventoryMenu extends AbstractContainerMenu {

    private OWEntity entity;

    private boolean itemIsMeat(ItemStack item) {
        if (entity.isOmnivorous()) {
            return item.is(ItemTags.MEAT) || item.is(ItemTags.FISHES) || item.is(Tags.Items.FOODS_VEGETABLE) || item.is(Tags.Items.FOODS_FRUIT);
        }
        return entity.isCarnivorous() ? (item.is(ItemTags.MEAT) || item.is(ItemTags.FISHES)) : (item.is(Tags.Items.FOODS_VEGETABLE) || item.is(Tags.Items.FOODS_FRUIT));
    }

    public OWInventoryMenu(int containerId, Inventory playerInventory, IItemHandler dataInventory) {
        super(OWMenuRegister.OW_INVENTORY_MENU.get(), containerId);
        entity = net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.resolveControlledEntity(playerInventory.player);
        if (entity == null) return;


        this.addSlot(new SlotItemHandler(dataInventory, 0, 6, 18) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(chooseSaddleWithEntity(entity)) && !this.hasItem();
            }
            public boolean isActive() {
                return true;
            }

            public void setChanged() {
                super.setChanged();
                boolean saddled = this.getItem().is(chooseSaddleWithEntity(entity));
                entity.setSaddle(saddled);
                entity.onSaddleEquipped(saddled ? this.getItem() : ItemStack.EMPTY);
                entity.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
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

        // 3e slot (sous le slot nourriture) — gants de boxe, kangourou uniquement.
        if (entity instanceof KangarooEntity kangaroo) {
            this.addSlot(new SlotItemHandler(dataInventory, 2, 6, 54) {
                public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.is(OWItems.BOXING_GLOVES.get()) && !this.hasItem();
                }
                public boolean isActive() {
                    return true;
                }

                public void setChanged() {
                    super.setChanged();
                    kangaroo.setWearingBoxingGloves(this.getItem().is(OWItems.BOXING_GLOVES.get()));
                    entity.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
                }
            });
        }

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
        this(containerId, playerInventory, new ItemStackHandler(3));
    }

    public Item chooseSaddleWithEntity(OWEntity entity) {
        return entity.acceptSaddle();
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