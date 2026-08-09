package net.tiew.operationWild.item.custom;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.core.OWWoolColors;

import java.util.List;
import java.util.Optional;

public class ElephantSaddle extends Item {

    public ElephantSaddle(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        int[] colors = colorsOf(stack);
        return colors == null ? Optional.empty() : Optional.of(new SaddleWoolsTooltip(colors[0], colors[1]));
    }

    private static int[] colorsOf(ItemStack stack) {
        List<Item> wools = stack.get(OWDataComponentTypes.SADDLE_WOOLS.get());
        if (wools == null || wools.size() < 2) return null;

        DyeColor primary = OWWoolColors.colorOf(wools.get(0));
        DyeColor secondary = OWWoolColors.colorOf(wools.get(1));
        if (primary == null || secondary == null) return null;

        return new int[]{OWWoolColors.rgb(primary), OWWoolColors.rgb(secondary)};
    }
}
