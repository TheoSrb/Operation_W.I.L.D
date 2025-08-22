package net.tiew.operationWild.worldgen.tree.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.level.LevelSimulatedReader;
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

        for (int $$0 = 0; $$0 < maxFreeTreeHeight; $$0++) {
            this.placeLeavesRow(level, setter, randomSource, config, attachement.pos().below(), 2, 0, attachement.doubleTrunk());
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
