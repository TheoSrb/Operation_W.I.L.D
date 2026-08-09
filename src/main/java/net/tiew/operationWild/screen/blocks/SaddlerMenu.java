package net.tiew.operationWild.screen.blocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.core.OWSaddleCrafting;
import net.tiew.operationWild.core.OWSaddlerUnlocks;
import net.tiew.operationWild.screen.OWMenuRegister;

public class SaddlerMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot unlockMask = DataSlot.standalone();
    private int inventorySignature = Integer.MIN_VALUE;

    public SaddlerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public SaddlerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(OWMenuRegister.SADDLER_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        this.addDataSlot(unlockMask);

        if (!this.player.level().isClientSide()) {
            unlockMask.set(OWSaddlerUnlocks.refresh(this.player));
        }
    }

    public int getUnlockMask() {
        return unlockMask.get();
    }

    public boolean isUnlocked(int index) {
        return OWSaddlerUnlocks.isUnlocked(unlockMask.get(), index);
    }

    @Override
    public void broadcastChanges() {
        if (!player.level().isClientSide()) {
            unlockMask.set(OWSaddlerUnlocks.getMask(player));

            int signature = inventorySignature(player);
            if (signature != inventorySignature) {
                inventorySignature = signature;
                if (player instanceof ServerPlayer serverPlayer) serverPlayer.inventoryMenu.broadcastFullState();
            }
        }
        super.broadcastChanges();
    }

    private static int inventorySignature(Player player) {
        int signature = 1;
        Inventory inventory = player.getInventory();

        for (ItemStack stack : inventory.items) signature = signature * 31 + slotSignature(stack);
        for (ItemStack stack : inventory.offhand) signature = signature * 31 + slotSignature(stack);

        return signature;
    }

    private static int slotSignature(ItemStack stack) {
        return stack.isEmpty() ? 0 : BuiltInRegistries.ITEM.getId(stack.getItem()) * 31 + stack.getCount();
    }

    public boolean craftSaddle(Player clicker, int index, int primaryColor, int secondaryColor) {
        if (clicker.level().isClientSide()) return true;
        if (!stillValid(clicker)) return false;

        unlockMask.set(OWSaddlerUnlocks.refresh(clicker));
        if (!OWSaddleCrafting.craft(clicker, index, primaryColor, secondaryColor)) return false;

        access.execute((level, pos) -> level.playSound(null, pos,
                SoundEvents.HORSE_SADDLE, SoundSource.BLOCKS, 0.9f, 1.0f));

        this.broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, OWBlocks.SADDLER.get());
    }
}
