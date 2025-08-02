package net.tiew.operationWild.item.custom.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.custom.misc.VenomousArrow;

public class VenomousArrowItem extends ArrowItem {

    public VenomousArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack arrowStack, LivingEntity shooter, @Nullable ItemStack bowStack) {
        return new VenomousArrow(shooter, level);
    }
}
