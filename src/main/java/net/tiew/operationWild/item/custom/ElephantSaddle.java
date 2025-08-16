package net.tiew.operationWild.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.tiew.operationWild.component.OWDataComponentTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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