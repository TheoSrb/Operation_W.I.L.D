package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.tiew.operationWild.component.OWDataComponentTypes;

import java.util.List;

public final class OWSaddleCrafting {

    private OWSaddleCrafting() {}

    public static Ingredient resolve(OWSaddleRecipe.Need need, DyeColor primary, DyeColor secondary) {
        if (need.colorSlot() == OWSaddleRecipe.NO_COLOR_SLOT) return need.ingredient();

        DyeColor color = need.colorSlot() == 0 ? primary : secondary;
        return color == null ? need.ingredient() : Ingredient.of(OWWoolColors.wool(color));
    }

    public static int countIn(Player player, Ingredient ingredient) {
        int total = 0;
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (!stack.isEmpty() && ingredient.test(stack)) total += stack.getCount();
        }
        for (ItemStack stack : inventory.offhand) {
            if (!stack.isEmpty() && ingredient.test(stack)) total += stack.getCount();
        }
        return total;
    }

    public static boolean hasMaterials(Player player, OWSaddleRecipe recipe, DyeColor primary, DyeColor secondary) {
        for (OWSaddleRecipe.Need need : recipe.needs()) {
            if (countIn(player, resolve(need, primary, secondary)) < need.count()) return false;
        }
        return true;
    }

    public static boolean craft(Player player, int index, int primaryId, int secondaryId) {
        OWSaddleRecipe recipe = OWSaddleRecipes.byIndex(index);
        if (recipe == null) return false;

        int mask = OWSaddlerUnlocks.refresh(player);
        if (!OWSaddlerUnlocks.isUnlocked(mask, index)) return false;

        DyeColor primary = null, secondary = null;
        if (recipe.needsColors()) {
            primary = OWWoolColors.byId(primaryId);
            secondary = OWWoolColors.byId(secondaryId);
            if (primary == null || secondary == null || primary == secondary) return false;
        }

        if (!hasMaterials(player, recipe, primary, secondary)) return false;

        for (OWSaddleRecipe.Need need : recipe.needs()) {
            take(player, resolve(need, primary, secondary), need.count());
        }

        ItemStack result = new ItemStack(recipe.result().get());
        if (primary != null) {
            result.set(OWDataComponentTypes.SADDLE_WOOLS.get(),
                    List.of(OWWoolColors.wool(primary), OWWoolColors.wool(secondary)));
        }
        if (!player.getInventory().add(result)) player.drop(result, false);

        player.getInventory().setChanged();
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.inventoryMenu.broadcastFullState();
        return true;
    }

    private static void take(Player player, Ingredient ingredient, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = takeFrom(inventory.items, ingredient, amount);
        takeFrom(inventory.offhand, ingredient, remaining);
    }

    private static int takeFrom(List<ItemStack> stacks, Ingredient ingredient, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) break;
            if (stack.isEmpty() || !ingredient.test(stack)) continue;

            int removed = Math.min(stack.getCount(), remaining);
            stack.shrink(removed);
            remaining -= removed;
        }
        return remaining;
    }
}
