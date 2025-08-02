package net.tiew.operationWild.item.custom.platinum_tools;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.component.OWDataComponentTypes;

import java.util.List;
import java.util.Random;

public class PlatinumPlatedSwordItem extends SwordItem {

    public int bonusDamages = 1;
    public int bonusEnchantmentValue = 6;
    public int bonusDurability = 864;

    public PlatinumPlatedSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public int getRandomAttributes(ItemStack stack) {
        Integer value = stack.get(OWDataComponentTypes.PLATINUM_RANDOM_ATTRIBUTES.get());
        if (value == null) {
            Random random = new Random(System.currentTimeMillis() + stack.hashCode());
            int randomLevel = 1 + random.nextInt(4);
            stack.set(OWDataComponentTypes.PLATINUM_RANDOM_ATTRIBUTES.get(), randomLevel);
            return randomLevel;
        }
        return value;
    }

    public void initializeRandomAttributes(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.PLATINUM_RANDOM_ATTRIBUTES.get()) == null) {
            getRandomAttributes(stack);
        }
    }

    @Override
    public float getAttackDamageBonus(Entity entity, float damages, DamageSource damageSource) {
        Entity attacker = damageSource.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldItem = livingAttacker.getMainHandItem();
            if (heldItem.getItem() == this) {
                int level = getRandomAttributes(heldItem);
                float baseBonus = super.getAttackDamageBonus(entity, damages, damageSource);
                if (level == 1) return baseBonus + bonusDamages;
                return baseBonus;
            }
        }

        return super.getAttackDamageBonus(entity, damages, damageSource);
    }


    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(itemStack, context, tooltip, flag);

        int level = getRandomAttributes(itemStack);
        if (level == 1) tooltip.add(Component.translatable("platinum_plated_tooltip_1", bonusDamages).setStyle(Style.EMPTY.withItalic(true).withColor(0x82b0ff)));
        if (level == 3) tooltip.add(Component.translatable("platinum_plated_tooltip_3", bonusDurability).setStyle(Style.EMPTY.withItalic(true).withColor(0x82b0ff)));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        initializeRandomAttributes(stack);
    }
}