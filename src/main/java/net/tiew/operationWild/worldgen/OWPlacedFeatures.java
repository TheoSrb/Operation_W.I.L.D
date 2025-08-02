package net.tiew.operationWild.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.tiew.operationWild.OperationWild;

import java.util.List;

public class OWPlacedFeatures {
    public static final ResourceKey<PlacedFeature> JADE_ORE_PLACED = registerKey("jade_ore_placed");
    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED = registerKey("ruby_ore_placed");

    public static final ResourceKey<PlacedFeature> SAVAGE_BERRY_BUSH_PLACED = registerKey("savage_berry_bush_placed");


    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, JADE_ORE_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.JADE_ORE),
                OWOrePlacement.commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.absolute(-30), VerticalAnchor.absolute(40))));

        register(context, RUBY_ORE_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.RUBY_ORE),
                OWOrePlacement.commonOrePlacement(6, HeightRangePlacement.triangle(VerticalAnchor.absolute(-50), VerticalAnchor.absolute(10))));

        register(context, SAVAGE_BERRY_BUSH_PLACED, configuredFeatures.getOrThrow(OWConfiguredFeatures.SAVAGE_BERRY_BUSH),
                List.of(RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
