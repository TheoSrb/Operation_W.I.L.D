package net.tiew.operationWild.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiew.operationWild.component.OWDataComponentTypes;

import java.util.List;

public class ElephantSaddle extends Item {
    public ElephantSaddle(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        List<Item> wools = stack.get(OWDataComponentTypes.SADDLE_WOOLS.get());
        if (wools != null && !wools.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.composition"));
            for (Item wool : wools) {
                tooltipComponents.add(Component.literal("- ").append(wool.getDescription()));
            }
        }
    }
}