package net.tiew.operationWild.entity.client.skin;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.skin.skins.*;
import net.tiew.operationWild.entity.variants.TigerVariant;

import java.util.EnumMap;
import java.util.Map;

public final class SkinRegistry {



    public static class TigerSkins {

        private static final Map<TigerVariant, TigerSkin> REGISTRY = new EnumMap<>(TigerVariant.class);

        // Helper to shorten resource paths
        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/" + path);
        }

        static {
            // --- Base skins: texture swap only ---
            register(TigerVariant.DEFAULT, TigerSkin.base(tex("tiger_default.png")));
            register(TigerVariant.LIGHT_ORANGE, TigerSkin.base(tex("tiger_light_orange.png")));
            register(TigerVariant.GOLDEN, TigerSkin.base(tex("tiger_golden.png")));
            register(TigerVariant.WHITE, TigerSkin.base(tex("tiger_white.png")));

            // --- SKIN_GOLD: base model + glowing overlay layer ---
            register(TigerVariant.Cosmetics.GOLD.variant, new GoldTigerSkin());

            // --- SKIN_BOSS: overlay ON TOP of the base tiger ---
            // La texture de base est résolue dynamiquement depuis getInitialVariant() du tigre (voir TigerRenderer).
            register(TigerVariant.Cosmetics.BOSS.variant, new BossTigerSkin());
            register(TigerVariant.Cosmetics.PIZZA_CHEF.variant, new PizzaChefTigerSkin());

            // --- SKIN_VIRUS: full model + texture replacement ---
            register(TigerVariant.Cosmetics.VIRUS.variant, new VirusTigerSkin());
            register(TigerVariant.Cosmetics.SEVEN_SEAS.variant, new SevenSeasTigerSkin());
            register(TigerVariant.Cosmetics.SCARLET_PIRATE.variant, new ScarletPirateTigerSkin());
            register(TigerVariant.Cosmetics.CARTOON.variant, new CartoonTigerSkin());
        }

        // ---

        public static void register(TigerVariant variant, TigerSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static TigerSkin get(TigerVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(TigerVariant.DEFAULT));
        }

        /**
         * Called from ModClientEventBusEvents to register all model layers
         * declared by skins in the registry. No need to touch that file
         * when adding new skins with model layers.
         */
        public static void registerAllLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            REGISTRY.values().stream()
                    .distinct()
                    .forEach(skin -> skin.getModelLayer().ifPresent(layer ->
                            skin.getLayerDefinitionSupplier().ifPresent(supplier ->
                                    event.registerLayerDefinition(layer, supplier)
                            )
                    ));
        }
    }
}