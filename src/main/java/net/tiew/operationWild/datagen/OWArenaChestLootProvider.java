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
 * <p>Chaque table est bâtie sur trois pools : <b>matériaux</b> (le gros du volume), <b>trophées</b>
 * (pièces d'animaux, plus rares) et <b>bonus</b> (équipement / objets de prestige, avec une part de
 * vide sur les paliers bas). Le contenu des pools dépend de la <i>matière</i> (Or / Jade / Rubis) ;
 * le <i>gabarit</i> (petit / normal / grand) multiplie le nombre de jets, si bien qu'un grand coffre
 * de jade est à la fois plus généreux et plus variable qu'un petit.</p>
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

    /** Multiplicateur de jets du gabarit : un grand coffre tire nettement plus qu'un petit. */
    private static float rollFactor(OWArena.Size size) {
        return switch (size) { case SMALL -> 0.6f; case NORMAL -> 1.0f; case LARGE -> 1.55f; };
    }

    private LootTable.Builder build(OWArena.Chest chest) {
        float k = rollFactor(chest.size());
        return switch (chest.material()) {
            case GOLD -> LootTable.lootTable()
                    .withPool(pool(2f * k, 3f * k)
                            .add(entry(OWItems.PLANT_FIBER.get(), 10, 4, 12))
                            .add(entry(OWItems.SAVAGE_BERRIES.get(), 8, 2, 6))
                            .add(entry(OWItems.LAVENDER_BOUQUET.get(), 6, 1, 3))
                            .add(entry(OWItems.EMPTY_SYRINGE.get(), 5, 1, 2))
                            .add(entry(Items.GOLD_INGOT, 5, 2, 5))
                            .add(entry(OWItems.BATTERY.get(), 3, 1, 1)))
                    .withPool(pool(1f * k, 2f * k)
                            .add(entry(OWItems.PEACOCK_FEATHER.get(), 8, 1, 3))
                            .add(entry(OWItems.TIGER_FUR.get(), 6, 1, 2))
                            .add(entry(OWItems.BOA_TONG.get(), 5, 1, 2))
                            .add(entry(OWItems.VENOMOUS_TOOTH.get(), 4, 1, 2))
                            .add(EmptyLootItem.emptyItem().setWeight(6)))
                    .withPool(pool(0f, 1f * k)
                            .add(entry(OWItems.JADE.get(), 6, 1, 2))
                            .add(entry(OWItems.MEDAL_0.get(), 3, 1, 1))
                            .add(EmptyLootItem.emptyItem().setWeight(9)));

            case JADE -> LootTable.lootTable()
                    .withPool(pool(3f * k, 5f * k)
                            .add(entry(OWItems.PLANT_FIBER.get(), 8, 8, 24))
                            .add(entry(OWItems.SAVAGE_BERRIES.get(), 7, 4, 12))
                            .add(entry(OWItems.JADE.get(), 7, 2, 7))
                            .add(entry(Items.GOLD_INGOT, 5, 4, 10))
                            .add(entry(OWItems.BATTERY.get(), 5, 1, 3))
                            .add(entry(OWItems.EMPTY_SYRINGE.get(), 4, 2, 5)))
                    .withPool(pool(1f * k, 3f * k)
                            .add(entry(OWItems.PREDATOR_TOOTH.get(), 7, 1, 4))
                            .add(entry(OWItems.TIGER_FUR.get(), 6, 2, 5))
                            .add(entry(OWItems.VENOMOUS_GLANDS.get(), 5, 1, 3))
                            .add(entry(OWItems.SHARK_FIN.get(), 5, 1, 3))
                            .add(entry(OWItems.BIOLUMINESCENT_JELLY.get(), 4, 1, 3))
                            .add(EmptyLootItem.emptyItem().setWeight(4)))
                    .withPool(pool(1f * k, 2f * k)
                            .add(entry(OWItems.RUBY.get(), 7, 1, 3))
                            .add(entry(OWItems.JADE_SWORD.get(), 4, 1, 1))
                            .add(entry(OWItems.JADE_PICKAXE.get(), 4, 1, 1))
                            .add(entry(OWItems.JADE_AXE.get(), 3, 1, 1))
                            .add(entry(OWItems.MEDAL_1.get(), 3, 1, 1))
                            .add(EmptyLootItem.emptyItem().setWeight(6)));

            case RUBY -> LootTable.lootTable()
                    .withPool(pool(5f * k, 7f * k)
                            .add(entry(OWItems.JADE.get(), 8, 4, 14))
                            .add(entry(OWItems.RUBY.get(), 8, 3, 11))
                            .add(entry(OWItems.PLATINUM_INGOT.get(), 6, 2, 8))
                            .add(entry(Items.DIAMOND, 5, 2, 7))
                            .add(entry(OWItems.BATTERY.get(), 5, 2, 6))
                            .add(entry(OWItems.PLANT_FIBER.get(), 4, 16, 40)))
                    .withPool(pool(2f * k, 4f * k)
                            .add(entry(OWItems.PREDATOR_TOOTH.get(), 7, 3, 9))
                            .add(entry(OWItems.VENOMOUS_GLANDS.get(), 6, 2, 6))
                            .add(entry(OWItems.SHARK_FIN.get(), 6, 2, 6))
                            .add(entry(OWItems.STINGING_FILAMENT.get(), 5, 2, 6))
                            .add(entry(OWItems.BIOLUMINESCENT_JELLY.get(), 5, 2, 6))
                            .add(entry(OWItems.TIGER_FUR.get(), 5, 4, 10)))
                    .withPool(pool(2f * k, 3f * k)
                            .add(entry(OWItems.RUBY_SWORD.get(), 6, 1, 1))
                            .add(entry(OWItems.RUBY_PICKAXE.get(), 6, 1, 1))
                            .add(entry(OWItems.RUBY_AXE.get(), 5, 1, 1))
                            .add(entry(OWItems.PLATINUM_PLATED_RUBY_SWORD.get(), 2, 1, 1))
                            .add(entry(OWItems.PLATINUM_PLATED_RUBY_PICKAXE.get(), 2, 1, 1))
                            .add(entry(OWItems.MEDAL_2.get(), 4, 1, 1))
                            .add(entry(OWItems.MEDAL_3.get(), 2, 1, 1))
                            .add(entry(Items.ENCHANTED_GOLDEN_APPLE, 2, 1, 1)));
        };
    }

    /** Pool dont le nombre de jets est arrondi au moins à 1 (0 autorisé pour les pools « bonus »). */
    private static LootPool.Builder pool(float min, float max) {
        int lo = Math.round(min), hi = Math.max(Math.round(max), Math.round(min));
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
