package net.tiew.operationWild.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class OWOverworldRegion extends Region {
    public OWOverworldRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        //addBiome(mapper, 0.5f, 0.8f, 1.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.3f, OWBiomes.REDWOOD_FOREST_BIOME);

        this.addModifiedVanillaOverworldBiomes(mapper, modifiedVanillaOverworldBuilder -> {
            modifiedVanillaOverworldBuilder.replaceBiome(Biomes.TAIGA, OWBiomes.REDWOOD_FOREST_BIOME);
        });
    }

    private void addBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                                       float temperature, float humidity, float continentality,
                                       float erosion, float depth, float strangeness,
                                       float offset, float scale, ResourceKey<Biome> biome) {

        this.addBiome(mapper,
                Climate.Parameter.span(temperature - scale, temperature + scale),
                Climate.Parameter.span(humidity - scale, humidity + scale),
                Climate.Parameter.span(continentality - scale, continentality + scale),
                Climate.Parameter.span(erosion - scale, erosion + scale),
                Climate.Parameter.span(depth - scale, depth + scale),
                Climate.Parameter.span(strangeness - scale, strangeness + scale),
                offset,
                biome);
    }
}
