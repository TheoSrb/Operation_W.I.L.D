package net.tiew.operationWild.entity.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiew.operationWild.networking.packets.to_server.PlatformActionPacket;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OWPlatformBuilding {

    private static final double REACH_MARGIN = 2.5;
    private static final Map<UUID, BreakProgress> BREAKING = new HashMap<>();

    private record BreakProgress(int entityId, BlockPos pos, long startTick) {}

    public record Placement(Map<BlockPos, BlockState> writes, Map<BlockPos, BlockEntity> blockEntities, boolean vanillaSound) {}

    private OWPlatformBuilding() {
    }

    public static boolean isAllowed(BlockState state) {
        return !state.isAir() && !(state.getBlock() instanceof LiquidBlock);
    }

    public static void handle(ServerPlayer player, PlatformActionPacket packet) {
        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(packet.entityId());
        if (!(entity instanceof IOWPlatformCarrier carrier)) return;
        OWPlatform platform = carrier.getPlatform();
        if (platform == null || !platform.isActive()) return;
        if (packet.action() != PlatformActionPacket.ABORT_BREAK && !platform.isCollidable()) return;
        if (packet.action() == PlatformActionPacket.ABORT_BREAK) {
            BREAKING.remove(player.getUUID());
            return;
        }
        if (!carrier.canBuildOnPlatform(player)) {
            player.displayClientMessage(Component.translatable("message.ow.platform.not_allowed"), true);
            return;
        }
        Vec3 hitWorld = platform.current().toWorld(packet.hitX(), packet.hitY(), packet.hitZ());
        if (player.getEyePosition().distanceTo(hitWorld) > player.blockInteractionRange() + REACH_MARGIN) return;

        BlockPos pos = new BlockPos(packet.x(), packet.y(), packet.z());
        Direction face = Direction.from3DDataValue(packet.face());
        Vec3 gridHit = new Vec3(packet.hitX(), packet.hitY(), packet.hitZ());
        switch (packet.action()) {
            case PlatformActionPacket.PLACE -> interact(player, level, carrier, platform, pos, face, gridHit, packet.hand(), true);
            case PlatformActionPacket.USE -> interact(player, level, carrier, platform, pos, face, gridHit, InteractionHand.MAIN_HAND, false);
            case PlatformActionPacket.START_BREAK -> startBreak(player, level, carrier, platform, pos, packet.entityId());
            case PlatformActionPacket.FINISH_BREAK -> finishBreak(player, level, carrier, platform, pos, packet.entityId());
            default -> {
            }
        }
    }

    private static BlockHitResult voidHit(OWPlatform platform, BlockPos clicked, Direction face, Vec3 gridHit) {
        return new BlockHitResult(OWPlatformSpace.toVoid(platform, gridHit), face, OWPlatformSpace.toVoid(platform, clicked), false);
    }

    public static boolean useBlock(Player player, net.minecraft.world.level.Level level, OWPlatform platform,
                                   BlockPos clicked, Direction face, Vec3 gridHit) {
        if (!platform.contains(clicked) || platform.get(clicked).isAir()) return false;
        BlockState state = platform.get(clicked);
        BlockHitResult hit = voidHit(platform, clicked, face, gridHit);
        float yaw = player.getYRot();
        player.setYRot(Mth.wrapDegrees(yaw - platform.current().yaw));
        try {
            ItemStack held = player.getMainHandItem();
            ItemInteractionResult itemResult = state.useItemOn(held, level, player, InteractionHand.MAIN_HAND, hit);
            if (itemResult.consumesAction()) return true;
            if (itemResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                return state.useWithoutItem(level, player, hit).consumesAction();
            }
            return false;
        } finally {
            player.setYRot(yaw);
        }
    }

    private static void interact(ServerPlayer player, ServerLevel level, IOWPlatformCarrier carrier, OWPlatform platform,
                                 BlockPos clicked, Direction face, Vec3 gridHit, InteractionHand placeHand, boolean allowPlace) {
        boolean bypass = player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty());
        if (!bypass && useBlock(player, level, platform, clicked, face, gridHit)) {
            player.swing(InteractionHand.MAIN_HAND, true);
            platform.markDirty();
            carrier.onPlatformChanged();
            return;
        }
        if (allowPlace) place(player, level, carrier, platform, clicked, face, gridHit, placeHand);
    }

    private static void place(ServerPlayer player, ServerLevel level, IOWPlatformCarrier carrier, OWPlatform platform,
                              BlockPos clicked, Direction face, Vec3 gridHit, InteractionHand hand) {
        ItemStack before = player.getItemInHand(hand).copy();
        Placement placement = simulatePlacement(level, platform, player, hand, clicked, face, gridHit);
        if (placement == null || !validWrites(platform, placement.writes())) {
            player.setItemInHand(hand, before);
            return;
        }
        BlockPos first = commit(level, platform, placement);
        BlockState placed = first == null ? null : platform.get(first);
        if (!placement.vanillaSound() && placed != null && !placed.isAir()) {
            SoundType sound = placed.getSoundType();
            BlockPos at = worldBlockPos(platform, first);
            level.playSound(null, at, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        }
        player.swing(hand, true);
        carrier.onPlatformChanged();
    }

    @Nullable
    public static BlockPos commit(ServerLevel level, OWPlatform platform, Placement placement) {
        List<BlockPos> changed = new ArrayList<>(placement.writes().size());
        BlockPos first = null;
        for (Map.Entry<BlockPos, BlockState> write : placement.writes().entrySet()) {
            platform.set(write.getKey(), write.getValue());
            changed.add(write.getKey());
            if (first == null && !write.getValue().isAir()) first = write.getKey();
        }
        for (Map.Entry<BlockPos, BlockEntity> entry : placement.blockEntities().entrySet()) {
            BlockState state = platform.get(entry.getKey());
            if (state.hasBlockEntity() && entry.getValue().getType().isValid(state)) {
                platform.installBlockEntity(entry.getKey(), entry.getValue());
            }
        }
        settle(platform, level, changed);
        return first;
    }

    @Nullable
    public static Placement simulatePlacement(ServerLevel level, OWPlatform platform, Player player,
                                              InteractionHand hand, BlockPos clicked, Direction face, Vec3 gridHit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        if (clicked.getY() < -1 || clicked.getY() >= platform.profile().height()) return null;

        BlockHitResult hit = voidHit(platform, clicked, face, gridHit);
        float yaw = player.getYRot();
        float localYaw = Mth.wrapDegrees(yaw - platform.current().yaw);
        player.setYRot(localYaw);
        InteractionResult result;
        OWPlatformSpace.Transaction transaction;
        OWPlatformSpace.begin(level, platform);
        try {
            result = blockItem.place(new BlockPlaceContext(level, player, hand, stack, hit));
        } finally {
            transaction = OWPlatformSpace.end();
            player.setYRot(yaw);
        }

        if (!result.consumesAction() && transaction.writes().isEmpty()) {
            Map<BlockPos, BlockState> fallback = doubleBlockFallback(platform, blockItem, clicked, face, localYaw);
            if (fallback == null) return null;
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return new Placement(fallback, Map.of(), false);
        }
        if (!result.consumesAction() || transaction.writes().isEmpty()) return null;
        return new Placement(transaction.writes(), transaction.blockEntities(), true);
    }

    @Nullable
    private static Map<BlockPos, BlockState> doubleBlockFallback(OWPlatform platform, BlockItem item, BlockPos clicked,
                                                                 Direction face, float localYaw) {
        BlockState base = item.getBlock().defaultBlockState();
        if (!base.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) return null;
        BlockPos lower = platform.contains(clicked) && platform.get(clicked).canBeReplaced() ? clicked : clicked.relative(face);
        BlockPos upper = lower.above();
        if (!platform.contains(lower) || !platform.contains(upper)) return null;
        if (!platform.get(lower).canBeReplaced() || !platform.get(upper).canBeReplaced()) return null;
        BlockState lowerState = base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        if (lowerState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            lowerState = lowerState.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.fromYRot(localYaw));
        }
        if (!lowerState.canSurvive(platform.level(), lower)) return null;
        Map<BlockPos, BlockState> writes = new LinkedHashMap<>();
        writes.put(lower.immutable(), lowerState);
        writes.put(upper.immutable(), lowerState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        return writes;
    }

    private static boolean validWrites(OWPlatform platform, Map<BlockPos, BlockState> writes) {
        OWPlatformProfile profile = platform.profile();
        List<AABB> occupants = occupantBoxes(platform);
        for (Map.Entry<BlockPos, BlockState> write : writes.entrySet()) {
            BlockPos pos = write.getKey();
            BlockState state = write.getValue();
            if (!profile.contains(pos.getX(), pos.getY(), pos.getZ())) return false;
            if (profile.isReserved(pos.getX(), pos.getY(), pos.getZ())) return false;
            if (state.isAir()) continue;
            if (!isAllowed(state)) return false;
            VoxelShape shape = state.getCollisionShape(platform.level(), pos);
            if (shape.isEmpty()) continue;
            for (AABB part : shape.toAabbs()) {
                AABB moved = part.move(pos);
                for (AABB occupant : occupants) {
                    if (moved.intersects(occupant)) return false;
                }
            }
        }
        return true;
    }

    private static List<AABB> occupantBoxes(OWPlatform platform) {
        OWPlatformTransform transform = platform.current();
        List<AABB> boxes = new ArrayList<>();
        for (Entity other : platform.carrier().level().getEntities(platform.carrier(), platform.worldBounds())) {
            if (OWPlatformPhysics.isExcluded(other, platform) || !other.isAlive()) continue;
            if (other instanceof ItemEntity) continue;
            AABB box = other.getBoundingBox();
            Vec3 center = transform.toGrid(box.getCenter());
            double hx = box.getXsize() / 2.0 - 1.0E-3;
            double hy = box.getYsize() / 2.0 - 1.0E-3;
            double hz = box.getZsize() / 2.0 - 1.0E-3;
            boxes.add(new AABB(center.x - hx, center.y - hy, center.z - hz, center.x + hx, center.y + hy, center.z + hz));
        }
        return boxes;
    }

    private static int requiredTicks(ServerPlayer player, OWPlatform platform, BlockPos pos, BlockState state) {
        float progress = state.getDestroyProgress(player, platform.level(), pos);
        if (progress <= 0f) return -1;
        return Mth.ceil(1.0F / progress);
    }

    private static void startBreak(ServerPlayer player, ServerLevel level, IOWPlatformCarrier carrier, OWPlatform platform,
                                   BlockPos pos, int entityId) {
        if (!platform.contains(pos)) return;
        BlockState state = platform.get(pos);
        if (state.isAir()) return;
        if (player.getAbilities().instabuild) {
            destroy(player, level, platform, pos, false);
            carrier.onPlatformChanged();
            return;
        }
        if (requiredTicks(player, platform, pos, state) < 0) return;
        BREAKING.put(player.getUUID(), new BreakProgress(entityId, pos.immutable(), level.getGameTime()));
    }

    private static void finishBreak(ServerPlayer player, ServerLevel level, IOWPlatformCarrier carrier, OWPlatform platform,
                                    BlockPos pos, int entityId) {
        BreakProgress progress = BREAKING.remove(player.getUUID());
        if (progress == null || progress.entityId() != entityId || !progress.pos().equals(pos)) return;
        if (!platform.contains(pos)) return;
        BlockState state = platform.get(pos);
        if (state.isAir()) return;
        int required = requiredTicks(player, platform, pos, state);
        if (required < 0) return;
        long elapsed = level.getGameTime() - progress.startTick();
        if (elapsed + 2 < required * 0.7) return;
        destroy(player, level, platform, pos, true);
        carrier.onPlatformChanged();
    }

    public static void destroy(Player player, ServerLevel level, OWPlatform platform, BlockPos pos, boolean survival) {
        BlockState state = platform.get(pos);
        if (state.isAir()) return;
        BlockPos voidPos = OWPlatformSpace.toVoid(platform, pos);
        BlockEntity blockEntity = platform.blockEntity(pos);
        BlockState removed = state.getBlock().playerWillDestroy(level, voidPos, state, player);
        BlockState current = platform.get(pos);
        if (current.isAir()) return;
        BlockEntity currentEntity = platform.blockEntity(pos);
        if (currentEntity != null) blockEntity = currentEntity;
        platform.replaceVanilla(level, pos, Blocks.AIR.defaultBlockState());
        if (survival) {
            ItemStack tool = player.getMainHandItem();
            ItemStack toolCopy = tool.copy();
            boolean canHarvest = player.hasCorrectToolForDrops(current);
            tool.mineBlock(level, current, voidPos, player);
            if (canHarvest) current.getBlock().playerDestroy(level, player, voidPos, removed, blockEntity, toolCopy);
        }
        List<BlockPos> changed = new ArrayList<>();
        changed.add(pos.immutable());
        settle(platform, level, changed);
    }

    public static void settle(OWPlatform platform, ServerLevel level, List<BlockPos> changed) {
        Deque<BlockPos> queue = new ArrayDeque<>(changed);
        int guard = 0;
        while (!queue.isEmpty() && guard++ < 1024) {
            BlockPos pos = queue.poll();
            BlockState state = platform.stateAt(pos);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (!platform.contains(neighbor)) continue;
                BlockState neighborState = platform.get(neighbor);
                if (neighborState.isAir()) continue;
                BlockState updated = neighborState.updateShape(direction.getOpposite(), state, platform.level(), neighbor, pos);
                if (!updated.isAir() && !updated.canSurvive(platform.level(), neighbor)) updated = Blocks.AIR.defaultBlockState();
                if (updated == neighborState) continue;
                if (updated.isAir()) {
                    removeWithDrops(platform, level, neighbor.immutable(), neighborState);
                } else {
                    platform.set(neighbor, updated);
                }
                queue.add(neighbor.immutable());
            }
        }
    }

    private static void removeWithDrops(OWPlatform platform, ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos voidPos = OWPlatformSpace.toVoid(platform, pos);
        BlockEntity blockEntity = platform.blockEntity(pos);
        platform.replaceVanilla(level, pos, Blocks.AIR.defaultBlockState());
        Block.dropResources(state, level, voidPos, blockEntity);
    }

    public static BlockPos worldBlockPos(OWPlatform platform, BlockPos grid) {
        return BlockPos.containing(platform.current().toWorld(grid.getX() + 0.5, grid.getY() + 0.5, grid.getZ() + 0.5));
    }
}
