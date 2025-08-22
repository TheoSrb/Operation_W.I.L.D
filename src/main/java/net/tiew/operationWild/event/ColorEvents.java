package net.tiew.operationWild.event;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;

public class ColorEvents {

    public static void register(IEventBus modBus) {
        modBus.addListener(ColorEvents::registerBlockColors);
        modBus.addListener(ColorEvents::registerItemColors);
    }

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(world, pos);
            }
            return FoliageColor.getDefaultColor();
        }, OWBlocks.REDWOOD_LEAVES.get());
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            return FoliageColor.getDefaultColor();
        }, OWBlocks.REDWOOD_LEAVES.get());
    }
}