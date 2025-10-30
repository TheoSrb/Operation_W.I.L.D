package net.tiew.operationWild.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.sound.OWSounds;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BearTrapBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BearTrapBlock> CODEC = simpleCodec(BearTrapBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
    public static final BooleanProperty ON_COOLDOWN = BooleanProperty.create("on_cooldown");

    private static final int TRAP_DURATION = 600;
    private static final int COOLDOWN_DURATION = 100;

    private final Map<BlockPos, UUID> trappedEntities = new HashMap<>();

    public BearTrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CLOSED, false)
                .setValue(ON_COOLDOWN, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CLOSED, ON_COOLDOWN);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (!state.getValue(CLOSED) && !state.getValue(ON_COOLDOWN) && isValidEntity(entity)) {
            level.setBlock(pos, state.setValue(CLOSED, true), 3);

            trappedEntities.put(pos, entity.getUUID());

            level.scheduleTick(pos, this, TRAP_DURATION);

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(level.damageSources().generic(), 4.0F);

                float pitch = (float) (OWUtils.generateRandomInterval(0.9, 1.1));

                level.playSound(null, pos, OWSounds.BEAR_TRAP_CLOSED.get(), SoundSource.BLOCKS, 1.0F, pitch);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (state.getValue(CLOSED) && isTrappedEntity(pos, entity)) {
            if (entity instanceof LivingEntity livingEntity) {
                Vec3 blockCenter = new Vec3(pos.getX() + 0.5, entity.getY(), pos.getZ() + 0.5);
                entity.teleportTo(blockCenter.x, blockCenter.y, blockCenter.z);

                entity.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(CLOSED)) {
            level.setBlock(pos, state.setValue(CLOSED, false).setValue(ON_COOLDOWN, true), 3);

            trappedEntities.remove(pos);

            level.scheduleTick(pos, this, COOLDOWN_DURATION);
        }
        else if (state.getValue(ON_COOLDOWN)) {
            level.setBlock(pos, state.setValue(ON_COOLDOWN, false), 3);
        }
    }

    private boolean isValidEntity(Entity entity) {
        return entity instanceof LivingEntity living
                && living.getMaxHealth() <= 50
                && !living.isInWater()
                && living.onGround()
                && !(entity instanceof Player && ((Player) entity).isCreative());
    }

    private boolean isTrappedEntity(BlockPos pos, Entity entity) {
        UUID trappedUUID = trappedEntities.get(pos);
        return trappedUUID != null && trappedUUID.equals(entity.getUUID());
    }
}