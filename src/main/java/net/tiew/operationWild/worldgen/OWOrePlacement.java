package net.tiew.operationWild.worldgen;

import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class OWOrePlacement {

    public static List<PlacementModifier> orePlacement(PlacementModifier count, PlacementModifier heightRange) {
        return List.of(count, InSquarePlacement.spread(), heightRange, BiomeFilter.biome());
    }

    public static List<PlacementModifier> commonOrePlacement(int veinsPerChunck, PlacementModifier pHeightRange) {
        return orePlacement(CountPlacement.of(veinsPerChunck), pHeightRange);
    }

    public static List<PlacementModifier> rareOrePlacement(int pChance, PlacementModifier pHeightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(pChance), pHeightRange);
    }
}
