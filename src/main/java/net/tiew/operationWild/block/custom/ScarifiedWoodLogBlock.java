package net.tiew.operationWild.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ScarifiedWoodLogBlock extends RotatedPillarBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ScarifiedWoodLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing;
        if (context.getPlayer() != null) {
            facing = context.getHorizontalDirection();
        } else {
            facing = context.getHorizontalDirection().getOpposite();
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS);
    }
}