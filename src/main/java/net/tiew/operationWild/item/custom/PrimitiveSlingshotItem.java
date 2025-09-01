package net.tiew.operationWild.item.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.entity.misc.SlingshotProjectile;
import net.tiew.operationWild.utils.OWTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PrimitiveSlingshotItem extends ProjectileWeaponItem {
    public static final Predicate<ItemStack> SLINGSHOT_PROJECTILES = (itemStack) -> itemStack.is(OWTags.Items.SLINGSHOT_PROJECTILES);
    public static final int MAX_DRAW_DURATION = 30;
    public static final int DEFAULT_RANGE = 15;

    public PrimitiveSlingshotItem(Properties p_40660_) {
        super(p_40660_);
    }

    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i1) {
        if (level.isClientSide) return;
        if (livingEntity instanceof Player player) {
            ItemStack itemstack = player.getProjectile(itemStack);
            if (!itemstack.isEmpty()) {
                int i = this.getUseDuration(itemStack, livingEntity) - i1;
                i = EventHooks.onArrowLoose(itemStack, level, player, i, true);
                if (i < 0) {
                    return;
                }

                float f = getPowerForTime(i);
                if (!((double)f < 0.1)) {
                    List<ItemStack> list = draw(itemStack, itemstack, player);
                    if (level instanceof ServerLevel serverLevel) {
                        SlingshotProjectile projectile = new SlingshotProjectile(player, serverLevel);

                        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 1.5f, 1.0F);

                        serverLevel.addFreshEntity(projectile);

                        if (!player.hasInfiniteMaterials()) {
                            itemStack.hurtAndBreak(this.getDurabilityUse(itemstack), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                            if (itemstack.isEmpty()) {
                                player.getInventory().removeItem(itemstack);
                            }
                        }
                    }

                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.5f / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }

    }

    protected void shootProjectile(LivingEntity p_329327_, Projectile p_335269_, int p_331005_, float p_332731_, float p_332848_, float p_332058_, @Nullable LivingEntity p_335061_) {
        p_335269_.shootFromRotation(p_329327_, p_329327_.getXRot(), p_329327_.getYRot() + p_332058_, 0.0F, p_332731_, p_332848_);
    }

    public static float getPowerForTime(int p_40662_) {
        float f = (float)p_40662_ / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getUseDuration(ItemStack p_40680_, LivingEntity p_344246_) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return UseAnim.BOW;
    }

    public InteractionResultHolder<ItemStack> use(Level p_40672_, Player p_40673_, InteractionHand p_40674_) {
        ItemStack itemstack = p_40673_.getItemInHand(p_40674_);
        boolean flag = !p_40673_.getProjectile(itemstack).isEmpty();
        InteractionResultHolder<ItemStack> ret = EventHooks.onArrowNock(itemstack, p_40672_, p_40673_, p_40674_, flag);
        if (ret != null) {
            return ret;
        } else if (!p_40673_.hasInfiniteMaterials() && !flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            p_40673_.startUsingItem(p_40674_);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return SLINGSHOT_PROJECTILES;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

}
