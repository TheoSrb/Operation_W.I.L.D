package net.tiew.operationWild.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.SavageBerryBushBlock;
import net.tiew.operationWild.worldgen.tree.foliage.RedwoodFoliagePlacer;
import net.tiew.operationWild.worldgen.tree.trunk.RedwoodTrunkPlacer;

import java.util.List;

public class OWConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> JADE_ORE = registerKey("jade_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBY_ORE = registerKey("ruby_ore");

    public static final ResourceKey<ConfiguredFeature<?, ?>> SAVAGE_BERRY_BUSH = registerKey("savage_berry_bush");

    public static final ResourceKey<ConfiguredFeature<?, ?>> REDWOOD = registerKey("redwood");


    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceable = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> jadeOres = List.of(
                OreConfiguration.target(stoneReplaceable, OWBlocks.JADE_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceable, OWBlocks.DEEPSLATE_JADE_ORE.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> rubyOres = List.of(
                OreConfiguration.target(stoneReplaceable, OWBlocks.RUBY_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceable, OWBlocks.DEEPSLATE_RUBY_ORE.get().defaultBlockState())
        );

        register(context, JADE_ORE, Feature.ORE, new OreConfiguration(jadeOres, 5));    // countPerVeins
        register(context, RUBY_ORE, Feature.ORE, new OreConfiguration(rubyOres, 3));

        register(context, SAVAGE_BERRY_BUSH, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(OWBlocks.SAVAGE_BERRY_BUSH.get().defaultBlockState().setValue(SavageBerryBushBlock.AGE, Integer.valueOf(3)))), List.of(Blocks.GRASS_BLOCK)));


        createTree(context);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
    }

    public static void createTree(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, REDWOOD, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(OWBlocks.REDWOOD_LOG.get()),
                new RedwoodTrunkPlacer(30, 15, 3),

                BlockStateProvider.simple(OWBlocks.REDWOOD_LEAVES.get()),
                new RedwoodFoliagePlacer(UniformInt.of(5, 7), UniformInt.of(2, 5), 3),

                new TwoLayersFeatureSize(1, 0, 2)).build());
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }

}
