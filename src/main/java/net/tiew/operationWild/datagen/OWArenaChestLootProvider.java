package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.item.OWItems;

import java.util.function.BiConsumer;

/**
 * Tables de butin des neuf coffres d'arène ({@code ow:chests/arena_<matière>_<gabarit>}).
 *
 * <p><b>Échelle.</b> Les quantités sont volontairement modestes, à l'image des Pièces Sauvages
 * (quelques unités, pas quelques milliers) : un coffre récompense une victoire, il ne remplace pas
 * une session de minage. Un petit coffre doré rend deux ou trois piles minuscules ; un grand coffre
 * de rubis reste sous la dizaine d'objets par entrée.</p>
 *
 * <p><b>Structure.</b> Quatre pools par matière — minerais, butin de créatures, ressources, et
 * équipement. Le contenu dépend de la <i>matière</i> (Or / Jade / Rubis) ; le <i>gabarit</i>
 * (petit / normal / grand) multiplie seulement le nombre de jets, si bien que trois tables
 * suffisent pour neuf coffres.</p>
 *
 * <p><b>Progression.</b> L'or reste sur du vanilla courant et les premières ressources du mod ; le
 * jade ouvre sur les minerais intermédiaires, les trophées de créatures et l'équipement de jade ; le
 * rubis donne accès aux matières rares, aux trophées de boss et à l'équipement de haut niveau. La
 * rareté monte, mais les <b>quantités restent contenues</b> à tous les paliers.</p>
 *
 * <p>Les Pièces Sauvages ne sont <b>pas</b> dans ces tables : elles sont tirées séparément côté
 * serveur dans la fourchette propre à chaque coffre ({@link OWArena.Chest#rollCoins}).</p>
 */
public class OWArenaChestLootProvider implements LootTableSubProvider {

    public OWArenaChestLootProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        for (OWArena.Chest chest : OWArena.Chest.values()) {
            consumer.accept(chest.lootTable(), build(chest));
        }
    }

    /** Multiplicateur de jets du gabarit : un grand coffre tire plus souvent, pas plus gros. */
    private static float rollFactor(OWArena.Size size) {
        return switch (size) { case SMALL -> 0.6f; case NORMAL -> 1.0f; case LARGE -> 1.5f; };
    }

    private LootTable.Builder build(OWArena.Chest chest) {
        float k = rollFactor(chest.size());
        return switch (chest.material()) {
            case GOLD -> goldTable(k);
            case JADE -> jadeTable(k);
            case RUBY -> rubyTable(k);
            case TRIBE -> tribeTable();
        };
    }

    // ══ Or — le tout-venant : minerais communs, dépouilles ordinaires, outils de fer ══
    private LootTable.Builder goldTable(float k) {
        return LootTable.lootTable()
                // Minerais courants.
                .withPool(pool(1f * k, 2f * k)
                        .add(entry(Items.COAL, 10, 2, 6))
                        .add(entry(Items.COPPER_INGOT, 9, 2, 5))
                        .add(entry(Items.RAW_IRON, 8, 1, 4))
                        .add(entry(Items.IRON_INGOT, 7, 1, 3))
                        .add(entry(Items.REDSTONE, 7, 2, 5))
                        .add(entry(Items.LAPIS_LAZULI, 6, 1, 4))
                        .add(entry(Items.RAW_GOLD, 5, 1, 3))
                        .add(entry(Items.GOLD_INGOT, 4, 1, 2))
                        .add(entry(OWItems.JADE.get(), 3, 1, 2)))

                // Dépouilles de créatures vanilla et premiers trophées du mod.
                .withPool(pool(1f * k, 2f * k)
                        .add(entry(Items.BONE, 9, 1, 4))
                        .add(entry(Items.STRING, 9, 1, 4))
                        .add(entry(Items.ROTTEN_FLESH, 8, 1, 4))
                        .add(entry(Items.FEATHER, 8, 1, 4))
                        .add(entry(Items.LEATHER, 7, 1, 3))
                        .add(entry(Items.GUNPOWDER, 6, 1, 3))
                        .add(entry(Items.SPIDER_EYE, 5, 1, 2))
                        .add(entry(Items.RABBIT_HIDE, 5, 1, 3))
                        .add(entry(Items.INK_SAC, 4, 1, 3))
                        .add(entry(OWItems.PEACOCK_FEATHER.get(), 6, 1, 3))
                        .add(entry(OWItems.TIGER_FUR.get(), 5, 1, 2))
                        .add(entry(OWItems.BOA_TONG.get(), 4, 1, 2)))

                // Ressources et vivres.
                .withPool(pool(1f * k, 2f * k)
                        .add(entry(OWItems.PLANT_FIBER.get(), 12, 3, 8))
                        .add(entry(OWItems.SAVAGE_BERRIES.get(), 8, 2, 5))
                        .add(entry(Items.BEEF, 6, 1, 3))
                        .add(entry(Items.PORKCHOP, 6, 1, 3))
                        .add(entry(Items.CHICKEN, 6, 1, 3))
                        .add(entry(OWItems.RAW_BOA.get(), 5, 1, 2))
                        .add(entry(OWItems.RAW_PEACOCK.get(), 5, 1, 2)))

                // Équipement d'entrée de gamme — souvent rien.
                .withPool(pool(0f, 1f * k)
                        .add(entry(Items.IRON_SWORD, 4, 1, 1))
                        .add(entry(Items.IRON_PICKAXE, 4, 1, 1))
                        .add(entry(Items.IRON_AXE, 3, 1, 1))
                        .add(entry(Items.IRON_SHOVEL, 3, 1, 1))
                        .add(entry(Items.IRON_HELMET, 3, 1, 1))
                        .add(entry(Items.IRON_CHESTPLATE, 2, 1, 1))
                        .add(entry(Items.IRON_LEGGINGS, 2, 1, 1))
                        .add(entry(Items.IRON_BOOTS, 3, 1, 1))
                        .add(entry(OWItems.PRIMITIVE_SPEAR.get(), 3, 1, 1))
                        .add(entry(OWItems.PRIMITIVE_SICKLE.get(), 3, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(14)));
    }

    // ══ Jade — minerais intermédiaires, trophées de bêtes, équipement de jade ══
    private LootTable.Builder jadeTable(float k) {
        return LootTable.lootTable()
                .withPool(pool(2f * k, 3f * k)
                        .add(entry(OWItems.JADE.get(), 10, 1, 4))
                        .add(entry(Items.IRON_INGOT, 8, 2, 5))
                        .add(entry(Items.GOLD_INGOT, 8, 2, 4))
                        .add(entry(Items.LAPIS_LAZULI, 7, 3, 6))
                        .add(entry(Items.REDSTONE, 7, 3, 6))
                        .add(entry(Items.AMETHYST_SHARD, 6, 1, 3))
                        .add(entry(Items.QUARTZ, 6, 2, 5))
                        .add(entry(Items.COPPER_INGOT, 5, 3, 7))
                        .add(entry(Items.DIAMOND, 3, 1, 2))
                        .add(entry(OWItems.RUBY.get(), 3, 1, 2)))

                .withPool(pool(1f * k, 3f * k)
                        .add(entry(OWItems.PREDATOR_TOOTH.get(), 8, 1, 3))
                        .add(entry(OWItems.CROCODILE_SCALE.get(), 7, 1, 3))
                        .add(entry(OWItems.TIGER_FUR.get(), 7, 2, 4))
                        .add(entry(OWItems.VENOMOUS_TOOTH.get(), 6, 1, 2))
                        .add(entry(OWItems.SHARK_FIN.get(), 5, 1, 2))
                        .add(entry(OWItems.KODIAK_COAT.get(), 5, 1, 2))
                        .add(entry(OWItems.BIOLUMINESCENT_JELLY.get(), 4, 1, 2))
                        .add(entry(Items.SLIME_BALL, 6, 1, 3))
                        .add(entry(Items.ENDER_PEARL, 5, 1, 2))
                        .add(entry(Items.PHANTOM_MEMBRANE, 5, 1, 2))
                        .add(entry(Items.BLAZE_ROD, 4, 1, 2))
                        .add(entry(Items.HONEYCOMB, 4, 1, 3))
                        .add(entry(Items.PRISMARINE_SHARD, 4, 1, 3))
                        .add(entry(Items.GLOW_INK_SAC, 3, 1, 2)))

                .withPool(pool(1f * k, 2f * k)
                        .add(entry(OWItems.PLANT_FIBER.get(), 10, 5, 12))
                        .add(entry(OWItems.SAVAGE_BERRIES.get(), 7, 3, 7))
                        .add(entry(OWItems.BATTERY.get(), 6, 1, 2))
                        .add(entry(OWItems.COOKED_BOA.get(), 5, 1, 3))
                        .add(entry(OWItems.COOKED_PEACOCK.get(), 5, 1, 3))
                        .add(entry(OWItems.RAW_TIGER.get(), 4, 1, 2))
                        .add(entry(Items.GOLDEN_APPLE, 2, 1, 1)))

                .withPool(pool(1f * k, 2f * k)
                        .add(entry(OWItems.JADE_SWORD.get(), 6, 1, 1))
                        .add(entry(OWItems.JADE_PICKAXE.get(), 6, 1, 1))
                        .add(entry(OWItems.JADE_AXE.get(), 5, 1, 1))
                        .add(entry(OWItems.JADE_SHOVEL.get(), 4, 1, 1))
                        .add(entry(OWItems.JADE_HOE.get(), 3, 1, 1))
                        .add(entry(OWItems.JADE_HELMET.get(), 4, 1, 1))
                        .add(entry(OWItems.JADE_CHESTPLATE.get(), 3, 1, 1))
                        .add(entry(OWItems.JADE_LEGGINGS.get(), 3, 1, 1))
                        .add(entry(OWItems.JADE_BOOTS.get(), 4, 1, 1))
                        .add(entry(Items.DIAMOND_SWORD, 3, 1, 1))
                        .add(entry(Items.DIAMOND_PICKAXE, 3, 1, 1))
                        .add(entry(OWItems.MAYA_BLOWPIPE.get(), 3, 1, 1))
                        .add(entry(OWItems.PRIMITIVE_SLINGSHOT.get(), 3, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(8)));
    }

    // ══ Rubis — matières rares, trophées de haut vol, équipement de pointe ══
    private LootTable.Builder rubyTable(float k) {
        return LootTable.lootTable()
                .withPool(pool(2f * k, 4f * k)
                        .add(entry(OWItems.RUBY.get(), 10, 1, 4))
                        .add(entry(OWItems.JADE.get(), 9, 2, 5))
                        .add(entry(Items.DIAMOND, 7, 1, 3))
                        .add(entry(Items.EMERALD, 7, 1, 4))
                        .add(entry(Items.AMETHYST_SHARD, 6, 2, 5))
                        .add(entry(Items.QUARTZ, 5, 3, 7))
                        .add(entry(Items.GOLD_INGOT, 5, 3, 6))
                        .add(entry(Items.NETHERITE_SCRAP, 3, 1, 2))
                        .add(entry(Items.ANCIENT_DEBRIS, 2, 1, 1)))

                .withPool(pool(2f * k, 4f * k)
                        .add(entry(OWItems.PREDATOR_TOOTH.get(), 8, 2, 5))
                        .add(entry(OWItems.CROCODILE_SCALE.get(), 7, 2, 5))
                        .add(entry(OWItems.VENOMOUS_GLANDS.get(), 7, 1, 3))
                        .add(entry(OWItems.KODIAK_COAT.get(), 6, 2, 4))
                        .add(entry(OWItems.SHARK_FIN.get(), 6, 1, 3))
                        .add(entry(OWItems.STINGING_FILAMENT.get(), 5, 1, 3))
                        .add(entry(OWItems.BIOLUMINESCENT_JELLY.get(), 5, 1, 3))
                        .add(entry(Items.GHAST_TEAR, 4, 1, 2))
                        .add(entry(Items.MAGMA_CREAM, 5, 1, 3))
                        .add(entry(Items.PRISMARINE_CRYSTALS, 5, 1, 3))
                        .add(entry(Items.NAUTILUS_SHELL, 3, 1, 2))
                        .add(entry(Items.SHULKER_SHELL, 3, 1, 2))
                        .add(entry(Items.ENDER_PEARL, 4, 2, 4))
                        .add(entry(Items.WITHER_SKELETON_SKULL, 1, 1, 1)))

                .withPool(pool(1f * k, 2f * k)
                        .add(entry(OWItems.PLANT_FIBER.get(), 9, 8, 16))
                        .add(entry(OWItems.BATTERY.get(), 7, 1, 3))
                        .add(entry(OWItems.COOKED_TIGER.get(), 6, 2, 4))
                        .add(entry(OWItems.COOKED_KODIAK.get(), 6, 2, 4))
                        .add(entry(OWItems.SAVAGE_BERRIES.get(), 5, 4, 9))
                        .add(entry(Items.GOLDEN_APPLE, 4, 1, 2))
                        .add(entry(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1)))

                .withPool(pool(1f * k, 2f * k)
                        .add(entry(OWItems.RUBY_SWORD.get(), 6, 1, 1))
                        .add(entry(OWItems.RUBY_PICKAXE.get(), 6, 1, 1))
                        .add(entry(OWItems.RUBY_AXE.get(), 5, 1, 1))
                        .add(entry(OWItems.RUBY_SHOVEL.get(), 4, 1, 1))
                        .add(entry(OWItems.RUBY_HOE.get(), 3, 1, 1))
                        .add(entry(OWItems.JADE_CHESTPLATE.get(), 4, 1, 1))
                        .add(entry(OWItems.JADE_LEGGINGS.get(), 4, 1, 1))
                        .add(entry(OWItems.CAMOUFLAGE_CHESTPLATE.get(), 3, 1, 1))
                        .add(entry(OWItems.CAMOUFLAGE_LEGGINGS.get(), 3, 1, 1))
                        .add(entry(Items.DIAMOND_CHESTPLATE, 4, 1, 1))
                        .add(entry(Items.DIAMOND_LEGGINGS, 4, 1, 1))
                        .add(entry(Items.DIAMOND_HELMET, 4, 1, 1))
                        .add(entry(Items.DIAMOND_BOOTS, 4, 1, 1))
                        .add(entry(OWItems.REPTILIAN_DAGGER.get(), 3, 1, 1))
                        .add(entry(OWItems.BOXING_GLOVES.get(), 3, 1, 1))
                        .add(entry(Items.NETHERITE_INGOT, 1, 1, 1)));
    }

    /**
     * Coffre de tribu — <b>uniquement des minerais</b>, en quantité généreuse.
     *
     * <p>Ni outil, ni armure, ni dépouille : c'est une réserve de matière première, pas un coffre de
     * trésor. Sa générosité se situe autour d'un coffre de jade moyen, mais concentrée sur un seul
     * type de contenu, donc bien plus lisible.</p>
     *
     * <p>Aucun facteur de gabarit : le coffre de tribu est unique, il n'a pas de déclinaison
     * petit / moyen / grand.</p>
     */
    private LootTable.Builder tribeTable() {
        return LootTable.lootTable()
                // Minerais courants, en volume.
                .withPool(pool(3f, 4f)
                        .add(entry(Items.COAL, 10, 4, 10))
                        .add(entry(Items.COPPER_INGOT, 10, 4, 9))
                        .add(entry(Items.IRON_INGOT, 9, 3, 7))
                        .add(entry(Items.REDSTONE, 8, 4, 9))
                        .add(entry(Items.LAPIS_LAZULI, 8, 3, 8))
                        .add(entry(Items.GOLD_INGOT, 7, 2, 6))
                        .add(entry(Items.QUARTZ, 6, 3, 7)))

                // Minerais de valeur.
                .withPool(pool(2f, 3f)
                        .add(entry(OWItems.JADE.get(), 10, 2, 5))
                        .add(entry(Items.AMETHYST_SHARD, 8, 2, 5))
                        .add(entry(OWItems.RUBY.get(), 6, 1, 3))
                        .add(entry(Items.DIAMOND, 5, 1, 3))
                        .add(entry(Items.EMERALD, 5, 1, 4))
                        .add(entry(Items.NETHERITE_SCRAP, 2, 1, 1)))

                // Éclat final : rare, et toujours du minerai.
                .withPool(pool(0f, 1f)
                        .add(entry(Items.DIAMOND, 6, 2, 4))
                        .add(entry(OWItems.RUBY.get(), 5, 2, 4))
                        .add(entry(Items.ANCIENT_DEBRIS, 2, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(9)));
    }

    /** Pool dont le nombre de jets est arrondi ; au moins un jet, sauf pour les pools « bonus ». */
    private static LootPool.Builder pool(float min, float max) {
        int lo = Math.round(min);
        int hi = Math.max(Math.round(max), lo);
        if (hi < 1) hi = 1;
        return LootPool.lootPool().setRolls(UniformGenerator.between(lo, hi));
    }

    /** Entrée pondérée dont la quantité est tirée uniformément dans {@code [min, max]}. */
    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }
}
