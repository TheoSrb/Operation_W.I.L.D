package net.tiew.operationWild.entity.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.ScheduledTick;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OWPlatformSpace {

    public static final int VOID_Y = -2000;
    private static final int MIN_Y = VOID_Y - 16;
    private static final int MAX_Y = VOID_Y + 256;
    private static final int STRIDE = 32;
    private static final int MARGIN = 4;
    private static final int IDS_PER_ROW = 40000;

    private static Level transactionLevel;
    private static OWPlatform transactionPlatform;
    private static final Map<BlockPos, BlockState> WRITES = new LinkedHashMap<>();
    private static final Map<BlockPos, BlockEntity> PENDING_ENTITIES = new LinkedHashMap<>();

    public record Transaction(Map<BlockPos, BlockState> writes, Map<BlockPos, BlockEntity> blockEntities) {}

    private OWPlatformSpace() {
    }

    public static boolean isVoidY(int y) {
        return y >= MIN_Y && y < MAX_Y;
    }

    public static boolean isVoidY(double y) {
        return y >= MIN_Y && y < MAX_Y;
    }

    public static BlockPos toVoid(OWPlatform platform, int gx, int gy, int gz) {
        int id = platform.carrier().getId();
        return new BlockPos((id % IDS_PER_ROW) * STRIDE + MARGIN + gx, VOID_Y + gy, (id / IDS_PER_ROW) * STRIDE + MARGIN + gz);
    }

    public static BlockPos toVoid(OWPlatform platform, BlockPos grid) {
        return toVoid(platform, grid.getX(), grid.getY(), grid.getZ());
    }

    public static Vec3 toVoid(OWPlatform platform, Vec3 grid) {
        BlockPos origin = toVoid(platform, 0, 0, 0);
        return new Vec3(origin.getX() + grid.x, origin.getY() + grid.y, origin.getZ() + grid.z);
    }

    private static int carrierId(int x, int z) {
        return Math.floorDiv(z, STRIDE) * IDS_PER_ROW + Math.floorDiv(x, STRIDE);
    }

    public static BlockPos toGrid(BlockPos voidPos) {
        return new BlockPos(Math.floorMod(voidPos.getX(), STRIDE) - MARGIN, voidPos.getY() - VOID_Y,
                Math.floorMod(voidPos.getZ(), STRIDE) - MARGIN);
    }

    @Nullable
    public static OWPlatform resolve(Level level, int x, int y, int z) {
        if (!isVoidY(y)) return null;
        int id = carrierId(x, z);
        if (id < 0) return null;
        if (transactionPlatform != null && transactionLevel == level && transactionPlatform.carrier().getId() == id) {
            return transactionPlatform;
        }
        Entity entity = level.getEntity(id);
        if (entity instanceof IOWPlatformCarrier carrier) {
            OWPlatform platform = carrier.getPlatform();
            if (platform != null && platform.isActive()) return platform;
        }
        return null;
    }

    @Nullable
    public static OWPlatform resolve(Level level, BlockPos pos) {
        return resolve(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    public static Vec3 toWorld(Level level, double x, double y, double z) {
        OWPlatform platform = resolve(level, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        if (platform == null) return null;
        BlockPos origin = toVoid(platform, 0, 0, 0);
        return platform.current().toWorld(x - origin.getX(), y - origin.getY(), z - origin.getZ());
    }

    public static AABB remapBox(Level level, AABB box) {
        if (!isVoidY(box.minY) && !isVoidY(box.maxY)) return box;
        Vec3 center = box.getCenter();
        Vec3 world = toWorld(level, center.x, center.y, center.z);
        if (world == null) return box;
        double radius = Math.max(box.getXsize(), box.getZsize()) * 0.7072;
        double halfY = box.getYsize() / 2.0;
        return new AABB(world.x - radius, world.y - halfY, world.z - radius, world.x + radius, world.y + halfY, world.z + radius);
    }

    public static void scheduleTick(ScheduledTick<?> tick) {
        if (!(tick.type() instanceof Block block)) return;
        BlockPos pos = tick.pos();
        OWPlatform platform = OWPlatformRegistry.findServer(carrierId(pos.getX(), pos.getZ()));
        if (platform == null) return;
        platform.scheduleBlockTick(toGrid(pos), block, tick.triggerTick());
    }

    private static boolean inTransaction(Level level, OWPlatform platform) {
        return transactionPlatform == platform && transactionLevel == level;
    }

    public static BlockState read(Level level, BlockPos pos, BlockState original) {
        OWPlatform platform = resolve(level, pos);
        if (platform == null) return original;
        BlockPos grid = toGrid(pos);
        if (inTransaction(level, platform)) {
            BlockState written = WRITES.get(grid);
            if (written != null) return written;
        }
        return platform.stateAt(grid);
    }

    @Nullable
    public static BlockEntity blockEntity(Level level, BlockPos pos, @Nullable BlockEntity original) {
        OWPlatform platform = resolve(level, pos);
        if (platform == null) return original;
        BlockPos grid = toGrid(pos);
        if (inTransaction(level, platform)) {
            if (PENDING_ENTITIES.containsKey(grid)) return PENDING_ENTITIES.get(grid);
            if (WRITES.containsKey(grid)) return null;
        }
        return platform.blockEntity(grid);
    }

    public static boolean write(Level level, BlockPos pos, BlockState state) {
        OWPlatform platform = resolve(level, pos);
        if (platform == null) return false;
        BlockPos grid = toGrid(pos).immutable();
        if (inTransaction(level, platform)) {
            BlockState previous = WRITES.containsKey(grid) ? WRITES.get(grid) : platform.stateAt(grid);
            WRITES.put(grid, state);
            if (!state.hasBlockEntity()) {
                PENDING_ENTITIES.remove(grid);
            } else if (!previous.is(state.getBlock()) || !PENDING_ENTITIES.containsKey(grid)) {
                if (state.getBlock() instanceof EntityBlock entityBlock) {
                    BlockEntity created = entityBlock.newBlockEntity(pos.immutable(), state);
                    if (created != null) {
                        created.setLevel(level);
                        PENDING_ENTITIES.put(grid, created);
                    }
                }
            } else {
                PENDING_ENTITIES.get(grid).setBlockState(state);
            }
            return true;
        }
        platform.applyWorldWrite(level, grid, state);
        return true;
    }

    public static void begin(Level level, OWPlatform platform) {
        WRITES.clear();
        PENDING_ENTITIES.clear();
        transactionPlatform = platform;
        transactionLevel = level;
    }

    public static Transaction end() {
        transactionLevel = null;
        transactionPlatform = null;
        Transaction transaction = new Transaction(new LinkedHashMap<>(WRITES), new LinkedHashMap<>(PENDING_ENTITIES));
        WRITES.clear();
        PENDING_ENTITIES.clear();
        return transaction;
    }
}
