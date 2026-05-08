package net.tiew.operationWild.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

public class OWDeepTrenchFeature extends Feature<NoneFeatureConfiguration> {

    private static final int FIXED_FLOOR_Y = 15;
    private static final int SAMPLE_RADIUS = 12; // max safe : 16 → crash, 12 = safe
    // Amplitude du bruit de relief (±N blocs autour de FIXED_FLOOR_Y)
    private static final double FLOOR_NOISE_AMP = 3.0;

    public OWDeepTrenchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level  = ctx.level();
        BlockPos      origin = ctx.origin();
        RandomSource  random = ctx.random();

        int chunkMinX = (origin.getX() >> 4) << 4;
        int chunkMinZ = (origin.getZ() >> 4) << 4;

        BlockState water  = Blocks.WATER.defaultBlockState();
        BlockState gravel = Blocks.GRAVEL.defaultBlockState();
        BlockState stone  = Blocks.STONE.defaultBlockState();

        // ── Phase 1 : ratio Mine Field — sampling par colonne ───────────────
        float[][] rawRatios = new float[16][16];
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkMinX + dx;
                int z = chunkMinZ + dz;
                int mineCount = 0;
                for (int sx = -SAMPLE_RADIUS; sx <= SAMPLE_RADIUS; sx += SAMPLE_RADIUS)
                    for (int sz = -SAMPLE_RADIUS; sz <= SAMPLE_RADIUS; sz += SAMPLE_RADIUS)
                        if (level.getBiome(new BlockPos(x + sx, 64, z + sz)).is(OWBiomes.MINE_FIELD_BIOME))
                            mineCount++;
                rawRatios[dx][dz] = mineCount / 9.0f;
            }
        }

        // ── Phase 2 : blur 3×3 ───────────────────────────────────────────────
        float[][] ratios = new float[16][16];
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                float sum = 0; int count = 0;
                for (int bx = Math.max(0, dx-1); bx <= Math.min(15, dx+1); bx++)
                    for (int bz = Math.max(0, dz-1); bz <= Math.min(15, dz+1); bz++) { sum += rawRatios[bx][bz]; count++; }
                ratios[dx][dz] = sum / count;
            }
        }

        // ── Phase 3 : creuser + sceller ──────────────────────────────────────
        // targetYs[dx][dz] = Y du gravier posé, Integer.MAX_VALUE si pas creusé
        int[][] targetYs = new int[16][16];
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                targetYs[dx][dz] = Integer.MAX_VALUE;
                float ratio = ratios[dx][dz];
                if (ratio < 0.05f) continue;

                int x = chunkMinX + dx;
                int z = chunkMinZ + dz;
                int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

                // Bruit sinusoïdal pour casser la monotonie du sol plat
                double noise = (Math.sin(x * 0.08) * Math.cos(z * 0.11)
                              + Math.sin(x * 0.13 + 0.7) * Math.cos(z * 0.07 + 1.3));
                int noisedFloor = FIXED_FLOOR_Y + (int)(noise * FLOOR_NOISE_AMP);

                int targetY = Math.max(
                        (int)(floorY * (1.0f - ratio) + noisedFloor * ratio),
                        level.getMinBuildHeight() + 10);

                if (targetY >= floorY) continue;

                // Creuser — flag 3 (UPDATE_NEIGHBORS|UPDATE_CLIENTS) pour que l'eau
                // notifie ses voisins et évite les cubes d'eau non rendus correctement
                for (int y = floorY; y > targetY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).is(Blocks.BEDROCK))
                        level.setBlock(pos, water, 3);
                }

                // Gravier au sol
                if (!level.getBlockState(new BlockPos(x, targetY, z)).is(Blocks.BEDROCK))
                    level.setBlock(new BlockPos(x, targetY, z), gravel, 2);

                // Stone pour boucher les grottes en dessous
                int solidStreak = 0;
                for (int y = targetY - 1; solidStreak < 3 && y >= level.getMinBuildHeight() + 1; y--) {
                    BlockPos sp = new BlockPos(x, y, z);
                    BlockState st = level.getBlockState(sp);
                    if (st.is(Blocks.BEDROCK)) break;
                    if (st.isAir() || !st.isSolid()) { level.setBlock(sp, stone, 2); solidStreak = 0; }
                    else solidStreak++;
                }

                targetYs[dx][dz] = targetY;
            }
        }

        // ── Phase 4 : végétation ─────────────────────────────────────────────
        // On utilise directement targetY (qu'on a posé nous-mêmes), pas le heightmap
        // car OCEAN_FLOOR_WG n'est pas mis à jour en temps réel pendant le feature.
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int targetY = targetYs[dx][dz];
                if (targetY == Integer.MAX_VALUE) continue;

                int x = chunkMinX + dx;
                int z = chunkMinZ + dz;

                // targetY = gravier, targetY+1 = eau (rempli dans la phase 3), targetY+2 = eau
                BlockPos above1 = new BlockPos(x, targetY + 1, z);
                BlockPos above2 = new BlockPos(x, targetY + 2, z);

                float r = random.nextFloat();
                if (r < 0.12f) {
                    // Algues hautes (12%)
                    level.setBlock(above1, Blocks.TALL_SEAGRASS.defaultBlockState()
                            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 2);
                    level.setBlock(above2, Blocks.TALL_SEAGRASS.defaultBlockState()
                            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 2);
                } else if (r < 0.42f) {
                    // Algues courtes (30%)
                    level.setBlock(above1, Blocks.SEAGRASS.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}
