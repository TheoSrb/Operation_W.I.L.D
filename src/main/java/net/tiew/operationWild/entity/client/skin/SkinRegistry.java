package net.tiew.operationWild.entity.client.skin;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.skin.skins.*;
import net.tiew.operationWild.entity.variants.BoaVariant;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.entity.variants.KangarooVariant;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.entity.variants.OrcaVariant;
import net.tiew.operationWild.entity.variants.TigerVariant;

import java.util.EnumMap;
import java.util.Map;

public final class SkinRegistry {

    // ──────────────────────────────────────────────────────────────────────────
    //  CROCODILE SKINS
    // ──────────────────────────────────────────────────────────────────────────

    public static class CrocodileSkins {

        private static final Map<CrocodileVariant, CrocodileSkin> REGISTRY = new EnumMap<>(CrocodileVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/" + path);
        }

        static {
            // --- Base skins: texture swap only ---
            register(CrocodileVariant.DEFAULT, CrocodileSkin.base(tex("crocodile_default.png")));
            register(CrocodileVariant.GREEN,   CrocodileSkin.base(tex("crocodile_green.png")));
            register(CrocodileVariant.DARK,    CrocodileSkin.base(tex("crocodile_dark.png")));

            // --- SKIN_GOLD: base model + glowing overlay layer ---
            register(CrocodileVariant.Cosmetics.GOLD.variant, new GoldCrocodileSkin());

            register(CrocodileVariant.Cosmetics.VERMILION_GUARDIAN.variant, new VermilionGuardianCrocodileSkin());
            register(CrocodileVariant.Cosmetics.TOY.variant, new ToyCrocodileSkin());

        }

        public static void register(CrocodileVariant variant, CrocodileSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static CrocodileSkin get(CrocodileVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(CrocodileVariant.DEFAULT));
        }

        /**
         * Called from ModClientEventBusEvents to register model layers declared by skins.
         * No need to touch that file when adding new skins with model layers.
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

    // ──────────────────────────────────────────────────────────────────────────
    //  KODIAK SKINS
    // ──────────────────────────────────────────────────────────────────────────

    public static class KodiakSkins {

        private static final Map<KodiakVariant, KodiakSkin> REGISTRY = new EnumMap<>(KodiakVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/" + path);
        }

        static {
            // --- Base skins: texture swap only ---
            register(KodiakVariant.DEFAULT,  KodiakSkin.base(tex("kodiak_default.png")));
            register(KodiakVariant.BLACK,    KodiakSkin.base(tex("kodiak_black.png")));
            register(KodiakVariant.GREY,     KodiakSkin.base(tex("kodiak_grey.png")));

            // --- SKIN_GOLD: base model + glowing overlay layer ---
            register(KodiakVariant.Cosmetics.GOLD.variant,     new GoldKodiakSkin());

            // --- SKIN_SKELETON: base model + glowing overlay layer ---
            register(KodiakVariant.Cosmetics.SKELETON.variant, new SkeletonKodiakSkin());
            // Note: SHADE (skinIndex 3) is not a variant — handled via isShade() in KodiakSkinRenderLayer
        }

        public static void register(KodiakVariant variant, KodiakSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static KodiakSkin get(KodiakVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(KodiakVariant.DEFAULT));
        }

        /**
         * Called from ModClientEventBusEvents to register model layers declared by skins.
         * No need to touch that file when adding new skins with model layers.
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

    // ──────────────────────────────────────────────────────────────────────────
    //  KANGAROO SKINS
    // ──────────────────────────────────────────────────────────────────────────

    public static class KangarooSkins {

        private static final Map<KangarooVariant, KangarooSkin> REGISTRY = new EnumMap<>(KangarooVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/" + path);
        }

        static {
            // --- Variantes naturelles : simple swap de texture (les gants de boxe sont un layer permanent) ---
            register(KangarooVariant.DEFAULT, KangarooSkin.base(tex("kangaroo_default.png")));
            register(KangarooVariant.ORANGE,  KangarooSkin.base(tex("kangaroo_orange.png")));
            register(KangarooVariant.BROWN,   KangarooSkin.base(tex("kangaroo_brown.png")));

            // --- SKIN_GOLD : modèle de base + overlay brillant ---
            register(KangarooVariant.Cosmetics.GOLD.variant, new GoldKangarooSkin());
        }

        public static void register(KangarooVariant variant, KangarooSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static KangarooSkin get(KangarooVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(KangarooVariant.DEFAULT));
        }

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

    // ──────────────────────────────────────────────────────────────────────────
    //  TIGER SKINS
    // ──────────────────────────────────────────────────────────────────────────

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

    // ──────────────────────────────────────────────────────────────────────────
    //  BOA SKINS
    // ──────────────────────────────────────────────────────────────────────────

    public static class BoaSkins {

        private static final Map<BoaVariant, BoaSkin> REGISTRY = new EnumMap<>(BoaVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/" + path);
        }

        static {
            register(BoaVariant.DEFAULT, new DefaultBoaSkin());
            register(BoaVariant.YELLOW,  BoaSkin.base(tex("boa_yellow.png")));
            register(BoaVariant.BROWN,   BoaSkin.base(tex("boa_brown.png")));
            register(BoaVariant.DARK,    BoaSkin.base(tex("boa_dark.png")));

            register(BoaVariant.Cosmetics.GOLD.variant, BoaSkin.base(tex("skins/boa_skin_gold.png")));

            // --- Skins REPLACEMENT : modele + texture remplaces (queue incluse) ---
            register(BoaVariant.Cosmetics.LEVIATHAN.variant, new LeviathanBoaSkin());
            register(BoaVariant.Cosmetics.PLUSH.variant,     new PlushBoaSkin());
        }

        public static void register(BoaVariant variant, BoaSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static BoaSkin get(BoaVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(BoaVariant.DEFAULT));
        }

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

    public static class OrcaSkins {

        private static final Map<OrcaVariant, OrcaSkin> REGISTRY = new EnumMap<>(OrcaVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/orca/" + path);
        }

        static {
            // --- Base skins: texture swap only ---
            register(OrcaVariant.DEFAULT,  OrcaSkin.base(tex("orca_default.png")));
            register(OrcaVariant.BLACK,    OrcaSkin.base(tex("orca_black.png")));
            register(OrcaVariant.AQUA,     OrcaSkin.base(tex("orca_aqua.png")));

            // --- SKIN_GOLD: base model + glowing overlay layer ---

            // --- SKIN_SKELETON: base model + glowing overlay layer ---
            // Note: SHADE (skinIndex 3) is not a variant — handled via isShade() in KodiakSkinRenderLayer
        }

        public static void register(OrcaVariant variant, OrcaSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static OrcaSkin get(OrcaVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(OrcaVariant.DEFAULT));
        }

        /**
         * Called from ModClientEventBusEvents to register model layers declared by skins.
         * No need to touch that file when adding new skins with model layers.
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

    // ──────────────────────────────────────────────────────────────────────────
    //  ELEPHANT SKINS
    // ──────────────────────────────────────────────────────────────────────────

    public static class ElephantSkins {

        private static final Map<ElephantVariant, ElephantSkin> REGISTRY = new EnumMap<>(ElephantVariant.class);

        private static ResourceLocation tex(String path) {
            return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/" + path);
        }

        static {
            // --- Base skins: texture swap only ---
            register(ElephantVariant.DEFAULT, ElephantSkin.base(tex("elephant_default.png")));
            register(ElephantVariant.GREY,    ElephantSkin.base(tex("elephant_grey.png")));
            register(ElephantVariant.PINK,    ElephantSkin.base(tex("elephant_pink.png")));

            // --- Cosmétiques : texture + calque émissif ---
            register(ElephantVariant.Cosmetics.GOLD.variant,   new GoldElephantSkin());
            register(ElephantVariant.Cosmetics.DEMON.variant,  new DemonElephantSkin());
            register(ElephantVariant.Cosmetics.ZOMBIE.variant, new ZombieElephantSkin());
        }

        public static void register(ElephantVariant variant, ElephantSkin skin) {
            REGISTRY.put(variant, skin);
        }

        public static ElephantSkin get(ElephantVariant variant) {
            return REGISTRY.getOrDefault(variant, REGISTRY.get(ElephantVariant.DEFAULT));
        }

        /**
         * Called from ModClientEventBusEvents to register model layers declared by skins.
         * No need to touch that file when adding new skins with model layers.
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