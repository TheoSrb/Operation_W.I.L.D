package net.tiew.operationWild.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Supplier;

public record OWSaddleRecipe(String id,
                             String entityKey,
                             Supplier<Item> result,
                             double tamingThreshold,
                             int accentColor,
                             List<Need> needs) {

    public static final int NO_COLOR_SLOT = -1;

    public boolean needsColors() {
        for (Need need : needs) {
            if (need.colorSlot() != NO_COLOR_SLOT) return true;
        }
        return false;
    }

    public record Need(Ingredient ingredient, int count, String labelKey, int colorSlot) {

        public static Need of(Supplier<Item> item, int count) {
            return new Need(Ingredient.of(item.get()), count, null, NO_COLOR_SLOT);
        }

        public static Need of(Item item, int count) {
            return new Need(Ingredient.of(item), count, null, NO_COLOR_SLOT);
        }

        public static Need colored(Ingredient fallback, int count, String labelKey, int colorSlot) {
            return new Need(fallback, count, labelKey, colorSlot);
        }
    }
}
