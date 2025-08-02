package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.worldgen.OWBiomeModifiers;
import net.tiew.operationWild.worldgen.OWConfiguredFeatures;
import net.tiew.operationWild.worldgen.OWPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class OWDatapackEntries extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, OWBiomeModifiers::bootstrap)
            .add(Registries.ENCHANTMENT, OWEnchantments::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, OWConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, OWPlacedFeatures::bootstrap);



    public OWDatapackEntries(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(OperationWild.MOD_ID));
    }
}