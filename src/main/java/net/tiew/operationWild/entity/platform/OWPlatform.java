package net.tiew.operationWild.entity.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class OWPlatform {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final int FLUSH_INTERVAL_TICKS = 100;
    private static final double POSE_DROP_SPEED = 0.07;

    private final Entity carrier;
    private final OWPlatformProfile profile;
    private final BlockState[] cells;
    private final OWPlatformLevel level;
    private final Map<Integer, BlockEntity> blockEntities = new HashMap<>();
    private final Map<Integer, BlockEntityTicker<BlockEntity>> tickers = new HashMap<>();
    private final Set<BlockPos> pendingSettle = new LinkedHashSet<>();
    private final List<ScheduledBlockTick> scheduledTicks = new ArrayList<>();

    private record ScheduledBlockTick(BlockPos pos, net.minecraft.world.level.block.Block block, long dueTick) {}
    private int blockCount;
    private int revision;
    private boolean active = true;
    private boolean dirty;
    private int age;
    private double poseDrop;
    private double poseDropO;
    private OWPlatformTransform previous;
    private OWPlatformTransform current;
    private OWPlatformTransform lighting;
    private List<VoxelShape> shapes = List.of();
    private int shapesRevision = -1;

    public OWPlatform(Entity carrier, OWPlatformProfile profile) {
        this.carrier = carrier;
        this.profile = profile;
        this.cells = new BlockState[profile.volume()];
        Arrays.fill(this.cells, AIR);
        this.level = new OWPlatformLevel(this);
    }

    public Entity carrier() {
        return carrier;
    }

    public OWPlatformProfile profile() {
        return profile;
    }

    public OWPlatformLevel level() {
        return level;
    }

    public int revision() {
        return revision;
    }

    public int blockCount() {
        return blockCount;
    }

    public boolean isActive() {
        return active && !carrier.isRemoved();
    }

    public void deactivate() {
        active = false;
        OWPlatformRegistry.untrack(this);
    }

    public void activate() {
        active = true;
    }

    public void scheduleBlockTick(BlockPos grid, net.minecraft.world.level.block.Block block, long dueTick) {
        if (!contains(grid)) return;
        BlockPos pos = grid.immutable();
        for (ScheduledBlockTick existing : scheduledTicks) {
            if (existing.pos().equals(pos) && existing.block() == block) return;
        }
        scheduledTicks.add(new ScheduledBlockTick(pos, block, dueTick));
    }

    private void runScheduledTicks(net.minecraft.server.level.ServerLevel serverLevel) {
        if (scheduledTicks.isEmpty()) return;
        long now = serverLevel.getGameTime();
        List<ScheduledBlockTick> due = new ArrayList<>();
        scheduledTicks.removeIf(tick -> {
            if (tick.dueTick() > now) return false;
            due.add(tick);
            return true;
        });
        for (ScheduledBlockTick tick : due) {
            BlockState state = get(tick.pos());
            if (state.is(tick.block())) state.tick(serverLevel, OWPlatformSpace.toVoid(this, tick.pos()), serverLevel.getRandom());
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public float scale() {
        if (carrier instanceof IOWPlatformCarrier platformCarrier) return platformCarrier.platformScale();
        return carrier instanceof LivingEntity living ? living.getScale() : 1f;
    }

    public boolean isCollidable() {
        return !(carrier instanceof IOWPlatformCarrier platformCarrier) || platformCarrier.platformCollidable();
    }

    private void tickPose() {
        poseDropO = poseDrop;
        double target = carrier instanceof IOWPlatformCarrier platformCarrier ? platformCarrier.platformPoseDrop() : 0.0;
        double step = POSE_DROP_SPEED;
        poseDrop += Math.max(-step, Math.min(step, target - poseDrop));
    }

    public void tick() {
        tickPose();
        OWPlatformTransform next = OWPlatformTransform.of(carrier, profile, scale(), poseDrop);
        previous = current == null ? next : current;
        current = next;
        if (!active) return;
        OWPlatformRegistry.track(this);
        age++;
        tickBlockEntities();
        Level world = carrier.level();
        if (world.isClientSide() || !(carrier instanceof IOWPlatformCarrier platformCarrier)) return;
        if (world instanceof net.minecraft.server.level.ServerLevel ticking) runScheduledTicks(ticking);
        if (!pendingSettle.isEmpty() && world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            OWPlatformBuilding.settle(this, serverLevel, new ArrayList<>(pendingSettle));
            pendingSettle.clear();
        }
        if (dirty) {
            dirty = false;
            platformCarrier.onPlatformChanged();
        } else if (!blockEntities.isEmpty() && age % FLUSH_INTERVAL_TICKS == 0) {
            platformCarrier.flushPlatform();
        }
    }

    private void tickBlockEntities() {
        if (tickers.isEmpty()) return;
        Level world = carrier.level();
        for (Map.Entry<Integer, BlockEntityTicker<BlockEntity>> entry : new ArrayList<>(tickers.entrySet())) {
            BlockEntity blockEntity = blockEntities.get(entry.getKey());
            if (blockEntity == null || blockEntity.isRemoved()) continue;
            entry.getValue().tick(world, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
        }
    }

    public OWPlatformTransform current() {
        if (current == null) {
            poseDrop = carrier instanceof IOWPlatformCarrier platformCarrier ? platformCarrier.platformPoseDrop() : 0.0;
            poseDropO = poseDrop;
            current = OWPlatformTransform.of(carrier, profile, scale(), poseDrop);
            previous = current;
        }
        return current;
    }

    public OWPlatformTransform previous() {
        current();
        return previous;
    }

    public OWPlatformTransform interpolated(float partialTick) {
        return OWPlatformTransform.interpolated(carrier, profile, scale(), poseDropO + (poseDrop - poseDropO) * partialTick, partialTick);
    }

    public OWPlatformTransform lightingTransform() {
        return lighting != null ? lighting : current();
    }

    public void setLightingTransform(@Nullable OWPlatformTransform transform) {
        this.lighting = transform;
    }

    public BlockState get(int x, int y, int z) {
        return profile.contains(x, y, z) ? cells[profile.index(x, y, z)] : AIR;
    }

    public BlockState get(BlockPos pos) {
        return get(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState stateAt(int x, int y, int z) {
        if (profile.contains(x, y, z)) return cells[profile.index(x, y, z)];
        if (y == -1 && profile.onDeck(x, z)) return OWPlatformLevel.DECK_STATE;
        return AIR;
    }

    public BlockState stateAt(BlockPos pos) {
        return stateAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean contains(BlockPos pos) {
        return profile.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    public BlockEntity blockEntity(BlockPos pos) {
        if (!contains(pos)) return null;
        return blockEntities.get(profile.index(pos.getX(), pos.getY(), pos.getZ()));
    }

    public Iterable<BlockEntity> blockEntities() {
        return blockEntities.values();
    }

    public boolean hasBlockEntities() {
        return !blockEntities.isEmpty();
    }

    public void set(int x, int y, int z, BlockState state) {
        if (!profile.contains(x, y, z)) return;
        int index = profile.index(x, y, z);
        BlockState old = cells[index];
        if (old == state) return;
        if (!old.isAir()) blockCount--;
        if (!state.isAir()) blockCount++;
        cells[index] = state;
        revision++;
        BlockEntity existing = blockEntities.get(index);
        if (existing != null && (!state.hasBlockEntity() || !old.is(state.getBlock()))) {
            existing.setRemoved();
            blockEntities.remove(index);
            tickers.remove(index);
            existing = null;
        }
        if (state.hasBlockEntity()) {
            if (existing != null) {
                existing.setBlockState(state);
                refreshTicker(index, existing);
            } else {
                createBlockEntity(index, x, y, z, state);
            }
        }
    }

    public void set(BlockPos pos, BlockState state) {
        set(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    public void installBlockEntity(BlockPos pos, BlockEntity blockEntity) {
        if (!contains(pos)) return;
        int index = profile.index(pos.getX(), pos.getY(), pos.getZ());
        BlockEntity existing = blockEntities.remove(index);
        if (existing != null && existing != blockEntity) existing.setRemoved();
        blockEntity.setLevel(carrier.level());
        blockEntity.setBlockState(cells[index]);
        blockEntities.put(index, blockEntity);
        refreshTicker(index, blockEntity);
    }

    @Nullable
    private BlockEntity createBlockEntity(int index, int x, int y, int z, BlockState state) {
        if (!(state.getBlock() instanceof EntityBlock entityBlock)) return null;
        BlockEntity created = entityBlock.newBlockEntity(OWPlatformSpace.toVoid(this, x, y, z), state);
        if (created == null) return null;
        created.setLevel(carrier.level());
        blockEntities.put(index, created);
        refreshTicker(index, created);
        return created;
    }

    @SuppressWarnings("unchecked")
    private void refreshTicker(int index, BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        BlockEntityTicker<BlockEntity> ticker = state.getBlock() instanceof EntityBlock entityBlock
                ? (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(carrier.level(), state, (net.minecraft.world.level.block.entity.BlockEntityType<BlockEntity>) blockEntity.getType())
                : null;
        if (ticker == null) tickers.remove(index);
        else tickers.put(index, ticker);
    }

    public void applyWorldWrite(Level world, BlockPos grid, BlockState state) {
        if (!contains(grid)) return;
        BlockState old = get(grid);
        if (old == state) return;
        if (!old.isAir() && !old.is(state.getBlock())) {
            replaceVanilla(world, grid, state);
        } else {
            set(grid, state);
        }
        pendingSettle.add(grid.immutable());
        dirty = true;
    }

    public void replaceVanilla(Level world, BlockPos grid, BlockState state) {
        if (!contains(grid)) return;
        int index = profile.index(grid.getX(), grid.getY(), grid.getZ());
        BlockState old = cells[index];
        if (old == state) return;
        if (!old.isAir()) blockCount--;
        if (!state.isAir()) blockCount++;
        cells[index] = state;
        revision++;
        old.onRemove(world, OWPlatformSpace.toVoid(this, grid), state, false);
        BlockEntity existing = blockEntities.get(index);
        if (existing != null && (!state.hasBlockEntity() || !old.is(state.getBlock()))) {
            existing.setRemoved();
            blockEntities.remove(index);
            tickers.remove(index);
        }
        if (state.hasBlockEntity() && !blockEntities.containsKey(index)) {
            createBlockEntity(index, grid.getX(), grid.getY(), grid.getZ(), state);
        }
    }

    public void clear() {
        scheduledTicks.clear();
        for (BlockEntity blockEntity : blockEntities.values()) blockEntity.setRemoved();
        blockEntities.clear();
        tickers.clear();
        Arrays.fill(cells, AIR);
        blockCount = 0;
        revision++;
    }

    public void load(OWPlatformBlocks blocks) {
        HolderLookup.Provider registries = carrier.level().registryAccess();
        Map<Integer, BlockEntity> previousEntities = new HashMap<>(blockEntities);
        blockEntities.clear();
        tickers.clear();
        Arrays.fill(cells, AIR);
        blockCount = 0;
        for (OWPlatformBlocks.Entry entry : blocks.entries()) {
            if (!profile.contains(entry.x(), entry.y(), entry.z()) || entry.state().isAir()) continue;
            int index = profile.index(entry.x(), entry.y(), entry.z());
            cells[index] = entry.state();
            blockCount++;
            if (!entry.state().hasBlockEntity()) continue;
            BlockEntity reused = previousEntities.remove(index);
            BlockEntity blockEntity;
            if (reused != null && reused.getBlockState().is(entry.state().getBlock())) {
                reused.setBlockState(entry.state());
                blockEntities.put(index, reused);
                refreshTicker(index, reused);
                blockEntity = reused;
            } else {
                if (reused != null) reused.setRemoved();
                blockEntity = createBlockEntity(index, entry.x(), entry.y(), entry.z(), entry.state());
            }
            if (blockEntity != null && entry.data().isPresent()) {
                blockEntity.loadWithComponents(entry.data().get(), registries);
            }
        }
        for (BlockEntity stale : previousEntities.values()) stale.setRemoved();
        revision++;
    }

    public OWPlatformBlocks snapshot(boolean full) {
        HolderLookup.Provider registries = carrier.level().registryAccess();
        List<OWPlatformBlocks.Entry> entries = new ArrayList<>(blockCount);
        forEachBlock((pos, state) -> {
            Optional<CompoundTag> data = Optional.empty();
            BlockEntity blockEntity = blockEntities.get(profile.index(pos.getX(), pos.getY(), pos.getZ()));
            if (blockEntity != null) {
                CompoundTag tag = full ? blockEntity.saveWithoutMetadata(registries) : blockEntity.getUpdateTag(registries);
                if (!tag.isEmpty()) data = Optional.of(tag);
            }
            entries.add(new OWPlatformBlocks.Entry(pos.getX(), pos.getY(), pos.getZ(), state, data));
        });
        return entries.isEmpty() ? OWPlatformBlocks.EMPTY : new OWPlatformBlocks(List.copyOf(entries));
    }

    public interface BlockVisitor {
        void visit(BlockPos pos, BlockState state);
    }

    public void forEachBlock(BlockVisitor visitor) {
        if (blockCount == 0) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < profile.height(); y++) {
            for (int z = 0; z < profile.length(); z++) {
                for (int x = 0; x < profile.width(); x++) {
                    BlockState state = cells[profile.index(x, y, z)];
                    if (state.isAir()) continue;
                    visitor.visit(pos.set(x, y, z), state);
                }
            }
        }
    }

    public List<AABB> deckBoxes() {
        List<AABB> boxes = new ArrayList<>(2);
        int start = -1;
        for (int x = 0; x <= profile.width(); x++) {
            boolean deck = x < profile.width() && profile.onDeck(x, profile.deckZ());
            if (deck && start < 0) start = x;
            if (!deck && start >= 0) {
                boxes.add(new AABB(start, -profile.deckThickness(), profile.deckZ(), x, 0, profile.deckZ() + profile.deckLength()));
                start = -1;
            }
        }
        return boxes;
    }

    public AABB gridBounds() {
        return new AABB(0, -profile.deckThickness(), 0, profile.width(), profile.height(), profile.length());
    }

    public List<VoxelShape> collisionShapes() {
        if (shapesRevision == revision) return shapes;
        List<VoxelShape> list = new ArrayList<>();
        for (AABB deck : deckBoxes()) list.add(Shapes.create(deck));
        forEachBlock((pos, state) -> {
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) list.add(shape.move(pos.getX(), pos.getY(), pos.getZ()));
        });
        shapes = List.copyOf(list);
        shapesRevision = revision;
        return shapes;
    }

    public AABB worldBounds() {
        return current().worldBounds(profile.radius() + 0.5, profile.deckThickness() + 0.5, profile.height() + 0.5);
    }

    @Nullable
    public BlockHitResult raycastGrid(Vec3 from, Vec3 to) {
        BlockHitResult[] best = new BlockHitResult[1];
        double[] bestDistance = {Double.MAX_VALUE};
        forEachBlock((pos, state) -> {
            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) return;
            BlockHitResult hit = shape.clip(from, to, pos.immutable());
            if (hit == null) return;
            double distance = hit.getLocation().distanceToSqr(from);
            if (distance < bestDistance[0]) {
                bestDistance[0] = distance;
                best[0] = hit;
            }
        });
        for (AABB deck : deckBoxes()) {
            Optional<Vec3> deckHit = deck.clip(from, to);
            if (deckHit.isEmpty()) continue;
            Vec3 location = deckHit.get();
            double distance = location.distanceToSqr(from);
            if (distance >= bestDistance[0]) continue;
            Direction face = deckFace(location, deck);
            int cx = Math.min((int) deck.maxX - 1, Math.max((int) deck.minX, (int) Math.floor(location.x)));
            int cz = Math.min((int) deck.maxZ - 1, Math.max((int) deck.minZ, (int) Math.floor(location.z)));
            bestDistance[0] = distance;
            best[0] = new BlockHitResult(location, face, new BlockPos(cx, -1, cz), false);
        }
        return best[0];
    }

    private static Direction deckFace(Vec3 hit, AABB deck) {
        double eps = 1.0E-4;
        if (Math.abs(hit.y - deck.maxY) < eps) return Direction.UP;
        if (Math.abs(hit.y - deck.minY) < eps) return Direction.DOWN;
        if (Math.abs(hit.x - deck.minX) < eps) return Direction.WEST;
        if (Math.abs(hit.x - deck.maxX) < eps) return Direction.EAST;
        if (Math.abs(hit.z - deck.minZ) < eps) return Direction.NORTH;
        return Direction.SOUTH;
    }
}
