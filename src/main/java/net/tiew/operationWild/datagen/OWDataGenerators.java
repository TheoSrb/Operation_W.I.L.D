package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.tiew.operationWild.OperationWild;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OWDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(
                        new LootTableProvider.SubProviderEntry(OWBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(OWEntityLootTableProvider::new, LootContextParamSets.ENTITY)
                ), lookupProvider));

        generator.addProvider(event.includeServer(), new OWRecipeProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeClient(), new OWItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new OWDatapackEntries(packOutput, lookupProvider));

        generator.addProvider(event.includeClient(), new OWBlockStateProvider(packOutput, existingFileHelper));

        generator.addProvider(event.includeServer(), new OWAdvancementProvider(packOutput, lookupProvider, existingFileHelper));

        OWBlockTagProvider blockTagProvider = generator.addProvider(event.includeServer(), new OWBlockTagProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeClient(), new OWItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new OWEntityTypeTagProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new OWEnchantmentTagProvider(packOutput, lookupProvider, existingFileHelper));
    }
}