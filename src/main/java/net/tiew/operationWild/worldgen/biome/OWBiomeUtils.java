package net.tiew.operationWild.worldgen.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.tiew.operationWild.OperationWild;

public class OWBiomeUtils {

    public static int calculateSkyColor(float temperature) {
        float $$1 = temperature / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }

    public static class OWNoises {
        public static final ResourceKey<NormalNoise.NoiseParameters> PODZOL_PATCH = createKey("podzol_patch");
        public static final ResourceKey<NormalNoise.NoiseParameters> ROOTED_PATCH = createKey("rooted_patch");

        private static ResourceKey<NormalNoise.NoiseParameters> createKey(String name) {
            return ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
        }

    }
}

