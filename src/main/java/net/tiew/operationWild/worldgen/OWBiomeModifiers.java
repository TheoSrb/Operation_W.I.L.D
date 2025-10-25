package net.tiew.operationWild.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OWBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_JADE_ORE = registerKey("add_jade_ore");
    public static final ResourceKey<BiomeModifier> ADD_RUBY_ORE = registerKey("add_ruby_ore");

    public static final ResourceKey<BiomeModifier> ADD_SAVAGE_BERRY_BUSH = registerKey("add_savage_berry_bush");

    public static final ResourceKey<BiomeModifier> SPAWN_TIGER = registerKey("spawn_tiger");
    public static final ResourceKey<BiomeModifier> SPAWN_BOA = registerKey("spawn_boa");
    public static final ResourceKey<BiomeModifier> SPAWN_PEACOCK = registerKey("spawn_peacock");
    public static final ResourceKey<BiomeModifier> SPAWN_JELLYFISH = registerKey("spawn_jellyfish");
    public static final ResourceKey<BiomeModifier> SPAWN_KODIAK = registerKey("spawn_kodiak");
    public static final ResourceKey<BiomeModifier> SPAWN_CROCODILE = registerKey("spawn_crocodile");

    public static final ArrayList<ResourceKey<Biome>> TIGER_BIOMES = new ArrayList<>(
            List.of(Biomes.BAMBOO_JUNGLE, Biomes.JUNGLE)
    );

    public static final ArrayList<ResourceKey<Biome>> BOA_BIOMES = new ArrayList<>(
            List.of(Biomes.JUNGLE, Biomes.SWAMP, Biomes.MANGROVE_SWAMP)
    );

    public static final ArrayList<ResourceKey<Biome>> PEACOCK_BIOMES = new ArrayList<>(
            List.of(Biomes.FLOWER_FOREST, Biomes.MEADOW)
    );

    public static final ArrayList<ResourceKey<Biome>> JELLYFISH_BIOMES = new ArrayList<>(
            List.of(Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
    );

    public static final ArrayList<ResourceKey<Biome>> KODIAK_BIOMES = new ArrayList<>(
            List.of(OWBiomes.REDWOOD_FOREST_BIOME)
    );

    public static final ArrayList<ResourceKey<Biome>> CROCODILE_BIOMES = new ArrayList<>(
            List.of(Biomes.MANGROVE_SWAMP, Biomes.SWAMP)
    );

    public static int veryCommonEntitySpawnChance = 60;
    public static int commonEntitySpawnChance = 40;
    public static int uncommonEntitySpawnChance = 28;
    public static int rareEntitySpawnChance = 12;
    public static int veryRareEntitySpawnChance = 6;
    public static int legendaryEntitySpawnChance = 2;

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeature = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                //HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.JUNGLE)),
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeature.getOrThrow(OWPlacedFeatures.JADE_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeature.getOrThrow(OWPlacedFeatures.RUBY_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));



        spawnEntity(context, SPAWN_TIGER, TIGER_BIOMES, OWEntityRegistry.TIGER, rareEntitySpawnChance, 1, 1);
        spawnEntity(context, SPAWN_BOA, BOA_BIOMES, OWEntityRegistry.BOA, commonEntitySpawnChance, 1, 2);
        spawnEntity(context, SPAWN_PEACOCK, PEACOCK_BIOMES, OWEntityRegistry.PEACOCK, veryRareEntitySpawnChance, 1, 2);
        spawnEntity(context, SPAWN_JELLYFISH, JELLYFISH_BIOMES, OWEntityRegistry.JELLYFISH, veryRareEntitySpawnChance, 2, 4);
        spawnEntity(context, SPAWN_KODIAK, KODIAK_BIOMES, OWEntityRegistry.KODIAK, uncommonEntitySpawnChance, 1, 1);
        spawnEntity(context, SPAWN_CROCODILE, CROCODILE_BIOMES, OWEntityRegistry.CROCODILE, 80, 2, 4);



        context.register(ADD_SAVAGE_BERRY_BUSH, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SPARSE_JUNGLE), biomes.getOrThrow(Biomes.FOREST)),
                HolderSet.direct(placedFeature.getOrThrow(OWPlacedFeatures.SAVAGE_BERRY_BUSH_PLACED)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    public static void spawnEntity(BootstrapContext<BiomeModifier> context, ResourceKey<BiomeModifier> SPAWN_ENTITY, ArrayList<ResourceKey<Biome>> ENTITY_BIOMES, Supplier<? extends EntityType<?>> entitySupplier, int weight, int minCount, int maxCount) {
        var biomes = context.lookup(Registries.BIOME);
        List<Holder<Biome>> biomeHolders = new ArrayList<>();
        for (ResourceKey<Biome> biomeKey : ENTITY_BIOMES) biomeHolders.add(biomes.getOrThrow(biomeKey));
        context.register(SPAWN_ENTITY, new BiomeModifiers.AddSpawnsBiomeModifier(
                HolderSet.direct(biomeHolders),
                List.of(new MobSpawnSettings.SpawnerData(entitySupplier.get(), weight, minCount, maxCount))));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
    }
}