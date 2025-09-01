package net.tiew.operationWild.item.custom.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.misc.TranquilizerWoodenStinger;

public class TranquilizerWoodenStingerItem extends ArrowItem {

    public TranquilizerWoodenStingerItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack arrowStack, LivingEntity shooter, @Nullable ItemStack bowStack) {
        return new TranquilizerWoodenStinger(shooter, level);
    }
}
