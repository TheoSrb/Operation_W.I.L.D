package net.tiew.operationWild.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SyringeData;

import java.util.List;

public class BloodyShyringeItem extends Item {

    public BloodyShyringeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        SyringeData data = stack.get(OWDataComponentTypes.SYRINGE_DATA.get());
        if (data == null) return;

        // "ow:tiger" → "entity.ow.tiger"
        String descId = "entity." + data.entityType().replace(":", ".");
        Component entityName = Component.translatable(descId)
                .withStyle(style -> style.withColor(TextColor.fromRgb(data.entityColor())).withBold(true));

        tooltip.add(Component.translatable("tooltip.syringe.blood", entityName)
                .withStyle(style -> style.withItalic(true)));
    }
}
