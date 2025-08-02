package net.tiew.operationWild.block.custom;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.tiew.operationWild.screen.OWMenuRegister;

import javax.annotation.Nullable;

public class SaddlerBlock extends Block {
    private static final Component CONTAINER_TITLE = Component.translatable("container.saddler");

    public SaddlerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            this.openContainer(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
    }

    protected void openContainer(BlockState state, Level level, BlockPos pos, Player player) {
        MenuProvider menuprovider = this.getMenuProvider(state, level, pos);
        if (menuprovider != null) {
            player.openMenu(menuprovider);
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> createMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        );
    }

    private AbstractContainerMenu createMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), inventory.player.registryAccess());
        return OWMenuRegister.SADDLER_MENU.get().create(id, inventory, buffer);
    }
}
