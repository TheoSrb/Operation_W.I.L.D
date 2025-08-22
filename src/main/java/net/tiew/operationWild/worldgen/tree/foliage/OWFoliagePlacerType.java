package net.tiew.operationWild.worldgen.tree.foliage;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;

public class OWFoliagePlacerType {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, OperationWild.MOD_ID);
    public static void register(IEventBus bus) { FOLIAGE_PLACERS.register(bus);}

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<RedwoodFoliagePlacer>> REDWOOD_FOLIAGE =
            FOLIAGE_PLACERS.register("rewood_foliage", () -> new FoliagePlacerType<>(RedwoodFoliagePlacer.CODEC));
}