package net.tiew.operationWild.datagen.wiki;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.datagen.OWDatapackEntries;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OWWikiDatasGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                true,
                new OWWikiDatasProvider(event.getGenerator().getPackOutput(),
                        RegistryPatchGenerator.createLookup(event.getLookupProvider(), OWDatapackEntries.BUILDER)
                                .thenApply(RegistrySetBuilder.PatchedRegistries::full))
        );
    }
}