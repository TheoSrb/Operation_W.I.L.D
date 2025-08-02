package net.tiew.operationWild.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.utils.OWUtils;

public class PrimitiveSickleItem extends Item {
    public PrimitiveSickleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {

        ItemEnchantments enchantment = itemStack.getEnchantments();
        Holder<Enchantment> plantProvidenceHolder = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(OWEnchantments.PLANT_PROVIDENCE);

        int plantProvidenceLevel = enchantment.getLevel(plantProvidenceHolder);

        int chance = blockState.getBlock() == Blocks.SHORT_GRASS ? (10 / (1 + (plantProvidenceLevel / 2))) : blockState.getBlock() == Blocks.TALL_GRASS ? (7 / (1 + (plantProvidenceLevel / 2))) : 0;
        if (OWUtils.RANDOM(chance)) {
            ItemEntity droppedItem = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(OWItems.PLANT_FIBER.get()));
            level.addFreshEntity(droppedItem);
        }
        itemStack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);
        return super.mineBlock(itemStack, level, blockState, blockPos, livingEntity);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        itemStack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return super.hurtEnemy(itemStack, target, attacker);
    }
}
