package net.tiew.operationWild.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.tiew.operationWild.OperationWild;

import java.util.List;

public class OWPlacedFeatures {
    public static final ResourceKey<PlacedFeature> JADE_ORE_PLACED = registerKey("jade_ore_placed");
    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED = registerKey("ruby_ore_placed");

    public static final ResourceKey<PlacedFeature> SAVAGE_BERRY_BUSH_PLACED = registerKey("savage_berry_bush_placed");

    public static final ResourceKey<PlacedFeature> REDWOOD_TREE_PLACED = registerKey("redwood_tree_placed");


    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, JADE_ORE_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.JADE_ORE),
                OWOrePlacement.commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.absolute(-30), VerticalAnchor.absolute(40))));

        register(context, RUBY_ORE_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.RUBY_ORE),
                OWOrePlacement.commonOrePlacement(6, HeightRangePlacement.triangle(VerticalAnchor.absolute(-50), VerticalAnchor.absolute(10))));

        register(context, SAVAGE_BERRY_BUSH_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.SAVAGE_BERRY_BUSH),
                List.of(RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()));

        register(context, REDWOOD_TREE_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.REDWOOD),
                List.of(
                        CountPlacement.of(3),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BlockPredicateFilter.forPredicate(BlockPredicate.not(BlockPredicate.anyOf(
                                BlockPredicate.matchesBlocks(Direction.NORTH.getNormal(), Blocks.WATER),
                                BlockPredicate.matchesBlocks(Direction.SOUTH.getNormal(), Blocks.WATER),
                                BlockPredicate.matchesBlocks(Direction.EAST.getNormal(), Blocks.WATER),
                                BlockPredicate.matchesBlocks(Direction.WEST.getNormal(), Blocks.WATER),
                                BlockPredicate.matchesBlocks(Direction.UP.getNormal(), Blocks.WATER),
                                BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.WATER)
                        ))),
                        BiomeFilter.biome()
                ));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
