package net.tiew.operationWild.entity.breakout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiew.operationWild.core.OWTags;

public final class OWBlockToughness {

    public static final int PASSABLE = 0;
    public static final int UNBREAKABLE = Integer.MAX_VALUE;

    private static final float OBSIDIAN_HARDNESS = 20f;
    private static final float METAL_HARDNESS = 5f;
    private static final float SOFT_HARDNESS = 0.5f;
    private static final float HEALTH_BASE = 3f;
    private static final float HEALTH_PER_HARDNESS = 1.6f;
    private static final double STEPPABLE_HEIGHT = 0.5625;

    private OWBlockToughness() {
    }

    public static boolean isObstacle(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    public static boolean isSteppable(BlockState state, BlockGetter level, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() || shape.max(Direction.Axis.Y) <= STEPPABLE_HEIGHT;
    }

    public static boolean isHazard(BlockState state) {
        return NodeEvaluator.isBurningBlock(state)
                || state.getFluidState().is(FluidTags.LAVA)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.WITHER_ROSE);
    }

    public static int tierOf(BlockState state, BlockGetter level, BlockPos pos) {
        if (!isObstacle(state, level, pos)) return PASSABLE;
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0f || state.is(OWTags.Blocks.BEAST_PROOF)) return UNBREAKABLE;
        if (state.is(OWTags.Blocks.BEAST_TOUGHNESS_OBSIDIAN)) return OWBreakLevel.OBSIDIAN;
        if (state.is(OWTags.Blocks.BEAST_TOUGHNESS_METAL)) return OWBreakLevel.METAL;
        if (state.is(OWTags.Blocks.BEAST_TOUGHNESS_STONE)) return OWBreakLevel.STONE;
        if (state.is(OWTags.Blocks.BEAST_TOUGHNESS_WOOD)) return OWBreakLevel.WOOD;
        if (state.is(OWTags.Blocks.BEAST_TOUGHNESS_SOFT)) return OWBreakLevel.SOFT;
        if (hardness >= OBSIDIAN_HARDNESS) return OWBreakLevel.OBSIDIAN;
        if (needsMetalTool(state) || (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && hardness >= METAL_HARDNESS)) {
            return OWBreakLevel.METAL;
        }
        if (hardness <= SOFT_HARDNESS) return OWBreakLevel.SOFT;
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return OWBreakLevel.STONE;
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) return OWBreakLevel.WOOD;
        return OWBreakLevel.SOFT;
    }

    public static float healthOf(BlockState state, BlockGetter level, BlockPos pos) {
        return HEALTH_BASE + Math.max(0f, state.getDestroySpeed(level, pos)) * HEALTH_PER_HARDNESS;
    }

    private static boolean needsMetalTool(BlockState state) {
        return state.is(BlockTags.NEEDS_STONE_TOOL)
                || state.is(BlockTags.NEEDS_IRON_TOOL)
                || state.is(BlockTags.NEEDS_DIAMOND_TOOL)
                || state.is(OWTags.Blocks.NEEDS_JADE_TOOL)
                || state.is(OWTags.Blocks.NEEDS_RUBY_TOOL);
    }
}
