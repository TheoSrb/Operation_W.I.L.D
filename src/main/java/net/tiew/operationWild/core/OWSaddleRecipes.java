package net.tiew.operationWild.core;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.item.OWItems;

import java.util.List;

public final class OWSaddleRecipes {

    private OWSaddleRecipes() {}

    private static final class Holder {
        static final List<OWSaddleRecipe> LIST = build();
    }

    public static List<OWSaddleRecipe> all() {
        return Holder.LIST;
    }

    public static int size() {
        return all().size();
    }

    public static OWSaddleRecipe byIndex(int index) {
        List<OWSaddleRecipe> all = all();
        return index >= 0 && index < all.size() ? all.get(index) : null;
    }

    public static int indexOf(String id) {
        List<OWSaddleRecipe> all = all();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id().equals(id)) return i;
        }
        return -1;
    }

    private static List<OWSaddleRecipe> build() {
        return List.of(
                new OWSaddleRecipe("boa", "entity.ow.boa", OWItems.BOA_SADDLE,
                        BoaEntity.TAMING_EXPERIENCE, BoaEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.LEATHER, 7),
                                OWSaddleRecipe.Need.of(Items.IRON_INGOT, 4),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 19))),

                new OWSaddleRecipe("kodiak", "entity.ow.kodiak", OWItems.KODIAK_SADDLE,
                        KodiakEntity.TAMING_EXPERIENCE, KodiakEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.LEATHER, 28),
                                OWSaddleRecipe.Need.of(Items.IRON_INGOT, 16),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 15),
                                OWSaddleRecipe.Need.of(Items.SHIELD, 1))),

                new OWSaddleRecipe("tiger", "entity.ow.tiger", OWItems.TIGER_SADDLE,
                        TigerEntity.TAMING_EXPERIENCE, TigerEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.LEATHER, 22),
                                OWSaddleRecipe.Need.of(Items.IRON_INGOT, 13),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 26))),

                new OWSaddleRecipe("crocodile", "entity.ow.crocodile", OWItems.CROCODILE_SADDLE,
                        CrocodileEntity.TAMING_EXPERIENCE, CrocodileEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.LEATHER, 25),
                                OWSaddleRecipe.Need.of(Items.IRON_INGOT, 18),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 28))),

                new OWSaddleRecipe("orca", "entity.ow.orca", OWItems.ORCA_SADDLE,
                        OrcaEntity.TAMING_EXPERIENCE, OrcaEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.LEATHER, 36),
                                OWSaddleRecipe.Need.of(Items.IRON_INGOT, 9),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 32))),

                new OWSaddleRecipe("elephant", "entity.ow.elephant", OWItems.ELEPHANT_SADDLE,
                        ElephantEntity.TAMING_EXPERIENCE, ElephantEntity.ENTITY_COLOR,
                        List.of(OWSaddleRecipe.Need.of(Items.GOLD_INGOT, 16),
                                OWSaddleRecipe.Need.of(OWItems.PLANT_FIBER, 31),
                                OWSaddleRecipe.Need.colored(Ingredient.of(ItemTags.WOOL), 18,
                                        "saddler.material.wool.primary", 0),
                                OWSaddleRecipe.Need.colored(Ingredient.of(ItemTags.WOOL), 12,
                                        "saddler.material.wool.secondary", 1)))
        );
    }
}
