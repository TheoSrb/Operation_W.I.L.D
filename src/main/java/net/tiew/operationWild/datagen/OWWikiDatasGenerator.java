package net.tiew.operationWild.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.tiew.operationWild.OperationWild;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OWWikiDatasGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                true,
                new OWWikiDatasProvider(event.getGenerator().getPackOutput())
        );
    }
}