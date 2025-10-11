package net.tiew.operationWild.item.custom;

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
import net.minecraft.server.level.ServerLevel;
import net.tiew.operationWild.entity.misc.TranquilizerWoodenStinger;
import net.tiew.operationWild.entity.misc.WoodenStinger;
import net.tiew.operationWild.item.custom.projectile.TranquilizerWoodenStingerItem;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.sound.OWSounds;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import static net.tiew.operationWild.core.OWUtils.generateRandomInterval;

public class MayaBlowpipeItem extends ProjectileWeaponItem {

    public static final Predicate<ItemStack> BLOWPIPE_PROJECTILES = (itemStack) -> itemStack.is(OWTags.Items.BLOWPIPE_PROJECTILES);
    public static final int MAX_DRAW_DURATION = 10;

    public MayaBlowpipeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        ItemStack projectileStack = player.getProjectile(stack);
        if (projectileStack.isEmpty()) return;

        int charge = getUseDuration(stack, entity) - timeLeft;
        charge = EventHooks.onArrowLoose(stack, level, player, charge, true);
        if (charge < 0) return;

        float power = getPowerForTime(charge);
        if (power < 0.1f) return;

        draw(stack, projectileStack, player);

        if (level instanceof ServerLevel serverLevel) {
            if (projectileStack.getItem() instanceof TranquilizerWoodenStingerItem) {
                TranquilizerWoodenStinger tranq = new TranquilizerWoodenStinger(player, serverLevel);
                tranq.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                tranq.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.0F, 1.0F);
                serverLevel.addFreshEntity(tranq);
            } else {
                WoodenStinger wooden = new WoodenStinger(player, serverLevel);
                wooden.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                wooden.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.0F, 1.0F);
                serverLevel.addFreshEntity(wooden);
            }

            if (!player.hasInfiniteMaterials()) {
                stack.hurtAndBreak(this.getDurabilityUse(projectileStack), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                if (projectileStack.isEmpty()) player.getInventory().removeItem(projectileStack);
            }
        }

        float pitch = (float) generateRandomInterval(1.0f, 1.2f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), OWSounds.BLOWPIPE_LUNCH.get(), player.getSoundSource(), 1.0F, pitch);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int time) {
        float f = (float) time / MAX_DRAW_DURATION;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    public static float getChargeProgress(ItemStack stack, LivingEntity entity) {
        if (entity.getUseItem() != stack) return 0f;
        return Math.min(getPowerForTime(entity.getTicksUsingItem()), 1f);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean hasProjectile = !player.getProjectile(stack).isEmpty();
        InteractionResultHolder<ItemStack> hook = EventHooks.onArrowNock(stack, level, player, hand, hasProjectile);
        if (hook != null) return hook;

        if (!player.hasInfiniteMaterials() && !hasProjectile) return InteractionResultHolder.fail(stack);

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return BLOWPIPE_PROJECTILES;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 20;
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int unused, float velocity, float inaccuracy, float adjustYaw, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + adjustYaw, 0.0F, velocity, inaccuracy);
    }
}
