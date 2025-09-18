package net.tiew.operationWild.worldgen.biome;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import terrablender.api.Regions;

public class OWTerrablender {
    public static void registerBiomes() {
        Regions.register(new OWOverworldRegion(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "overworld"), 2));
    }
}
