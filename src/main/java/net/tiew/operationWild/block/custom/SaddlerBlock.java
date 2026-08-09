package net.tiew.operationWild.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.tiew.operationWild.screen.blocks.SaddlerMenu;

import javax.annotation.Nullable;

public class SaddlerBlock extends Block {

    private static final Component CONTAINER_TITLE = Component.translatable("container.saddler");

    public SaddlerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        MenuProvider provider = this.getMenuProvider(state, level, pos);
        if (provider != null) {
            player.openMenu(provider);
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new SaddlerMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        );
    }
}
