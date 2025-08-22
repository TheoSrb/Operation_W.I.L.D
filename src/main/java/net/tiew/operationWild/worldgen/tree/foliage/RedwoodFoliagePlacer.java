package net.tiew.operationWild.worldgen.tree.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.tiew.operationWild.worldgen.tree.trunk.RedwoodTrunkPlacer;

public class RedwoodFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<RedwoodFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance
            -> foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height")
            .forGetter(fp -> fp.height)).apply(instance, RedwoodFoliagePlacer::new));

    private final int height;

    public RedwoodFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return OWFoliagePlacerType.REDWOOD_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter setter, RandomSource randomSource, TreeConfiguration config,
                                 int maxFreeTreeHeight, FoliageAttachment attachement, int foliageHeight, int foliageRadius, int offset) {

        for (int $0 = 0; $0 < maxFreeTreeHeight; $0++) {
            int radius;
            if ($0 < (maxFreeTreeHeight * 0.6)) {
                radius = (int) (1.5 + ($0 * 0.2));
            } else {
                int remainingHeight = maxFreeTreeHeight - $0;
                radius = (int) (1 + (remainingHeight * 0.15));
            }
            BlockPos layerPos = attachement.pos().below($0);

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        if (randomSource.nextFloat() > 0.15f) {
                            BlockPos leafPos = layerPos.offset(x, 0, z);
                            if (level.isStateAtPosition(leafPos, BlockBehaviour.BlockStateBase::isAir)) {
                                setter.set(leafPos, config.foliageProvider.getState(randomSource, leafPos));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int i1, int i2, int i3, boolean b) {
        return false;
    }
}
