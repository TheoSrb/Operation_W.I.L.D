package net.tiew.operationWild.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.worldgen.feature.OWDeepTrenchFeature;

public class OWFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, OperationWild.MOD_ID);

    public static final DeferredHolder<Feature<?>, OWDeepTrenchFeature> DEEP_TRENCH =
            FEATURES.register("deep_trench", () -> new OWDeepTrenchFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
