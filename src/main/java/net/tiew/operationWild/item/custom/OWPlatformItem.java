package net.tiew.operationWild.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.platform.OWPlatformBlocks;

import java.util.List;

public class OWPlatformItem extends Item {

    public OWPlatformItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        OWPlatformBlocks blocks = stack.get(OWDataComponentTypes.PLATFORM_BLOCKS.get());
        int count = blocks == null ? 0 : blocks.entries().size();
        tooltip.add(Component.translatable("item.ow.platform.blocks", count).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
