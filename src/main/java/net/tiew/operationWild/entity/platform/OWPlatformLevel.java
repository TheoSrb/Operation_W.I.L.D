package net.tiew.operationWild.entity.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class OWPlatformLevel implements LevelAccessor {

    public static final BlockState DECK_STATE = Blocks.OAK_PLANKS.defaultBlockState();

    private final OWPlatform platform;

    OWPlatformLevel(OWPlatform platform) {
        this.platform = platform;
    }

    private Level real() {
        return platform.carrier().level();
    }

    private BlockPos worldPos(BlockPos gridPos) {
        Vec3 world = platform.lightingTransform().toWorld(gridPos.getX() + 0.5, gridPos.getY() + 0.5, gridPos.getZ() + 0.5);
        return BlockPos.containing(world);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return platform.stateAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return platform.blockEntity(pos);
    }

    @Override
    public int getHeight() {
        return real().getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return real().getMinBuildHeight();
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return real().getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return real().getLightEngine();
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return real().getBrightness(lightType, worldPos(blockPos));
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return real().getRawBrightness(worldPos(blockPos), amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPos) {
        return real().canSeeSky(worldPos(blockPos));
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return real().getBlockTint(worldPos(blockPos), colorResolver);
    }

    @Override
    public long nextSubTickCount() {
        return real().nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelData getLevelData() {
        return real().getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return real().getCurrentDifficultyAt(worldPos(pos));
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return real().getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return real().getChunkSource();
    }

    @Override
    public RandomSource getRandom() {
        return real().getRandom();
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
        return List.of();
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate) {
        return List.of();
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        return null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return 0;
    }

    @Override
    public int getSkyDarken() {
        return real().getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return real().getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return real().getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return real().isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return real().getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return real().dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return real().registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return real().enabledFeatures();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return real().getWorldBorder();
    }

    @Nullable
    @Override
    public net.minecraft.world.level.BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return List.of();
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
        return state.test(getBlockState(pos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        return predicate.test(getFluidState(pos));
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return Optional.empty();
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return pos;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return false;
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return false;
    }
}
