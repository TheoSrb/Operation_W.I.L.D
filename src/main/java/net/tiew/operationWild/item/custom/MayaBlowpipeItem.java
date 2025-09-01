package net.tiew.operationWild.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.tiew.operationWild.entity.misc.TranquilizerWoodenStinger;
import net.tiew.operationWild.entity.misc.WoodenStinger;
import net.tiew.operationWild.item.custom.projectile.TranquilizerWoodenStingerItem;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static net.tiew.operationWild.utils.OWUtils.*;

public class MayaBlowpipeItem extends ProjectileWeaponItem {

    public static final Predicate<ItemStack> BLOWPIPE_PROJECTILES = (itemStack) -> itemStack.is(OWTags.Items.BLOWPIPE_PROJECTILES);
    public static final int MAX_DRAW_DURATION = 10;
    public static final int DEFAULT_RANGE = 15;

    public MayaBlowpipeItem(Properties p_40660_) {
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
                        boolean hasTranqStinger = itemstack.getItem() instanceof TranquilizerWoodenStingerItem;

                        if (hasTranqStinger) {
                            TranquilizerWoodenStinger tranqProjectile = new TranquilizerWoodenStinger(player, serverLevel);
                            tranqProjectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                            tranqProjectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 2.0f, 1.0F);
                            serverLevel.addFreshEntity(tranqProjectile);
                        } else {
                            WoodenStinger projectile = new WoodenStinger(player, serverLevel);
                            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 2.0f, 1.0F);
                            serverLevel.addFreshEntity(projectile);
                        }

                        if (!player.hasInfiniteMaterials()) {
                            itemStack.hurtAndBreak(this.getDurabilityUse(itemstack), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                            if (itemstack.isEmpty()) {
                                player.getInventory().removeItem(itemstack);
                            }
                        }
                    }
                    float pitch = (float) generateRandomInterval(1.0f, 1.2f);
                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), OWSounds.BLOWPIPE_LUNCH.get(), SoundSource.PLAYERS, 1.0F, pitch);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    protected void shootProjectile(LivingEntity p_329327_, Projectile p_335269_, int p_331005_, float p_332731_, float p_332848_, float p_332058_, @Nullable LivingEntity p_335061_) {
        p_335269_.shootFromRotation(p_329327_, p_329327_.getXRot(), p_329327_.getYRot() + p_332058_, 0.0F, p_332731_, p_332848_);
    }

    public static float getPowerForTime(int p_40662_) {
        float f = (float)p_40662_ / (float) MAX_DRAW_DURATION;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    public static float getChargeProgress(ItemStack stack, LivingEntity entity) {
        if (entity.getUseItem() != stack) {
            return 0.0F;
        }

        int useTime = entity.getTicksUsingItem();
        float f = getPowerForTime(useTime);

        return Mth.clamp(f, 0.0F, 1.0F);
    }

    public int getUseDuration(ItemStack p_40680_, LivingEntity p_344246_) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return UseAnim.NONE;
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
        return BLOWPIPE_PROJECTILES;
    }

    public int getDefaultProjectileRange() {
        return 20;
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlowpipeRenderHandler {

        @SubscribeEvent
        public void onRenderHand(RenderHandEvent event) {
            ItemStack stack = event.getItemStack();

            if (stack.getItem() instanceof MayaBlowpipeItem) {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null && player.isUsingItem() && player.getUseItem() == stack) {
                    float chargeProgress = MayaBlowpipeItem.getChargeProgress(stack, player);

                    applyBlowpipeRotation(event.getPoseStack(), chargeProgress, event.getHand());
                }
            }
        }

        private void applyBlowpipeRotation(PoseStack poseStack, float chargeProgress, InteractionHand hand) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            double yTranslate = 1.0D - (chargeProgress * 0.25D);
            double zTranslate = 0.9D - (chargeProgress * 0.125D);

            if (hand == InteractionHand.OFF_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(-110));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                poseStack.translate(0.35D, yTranslate, zTranslate);
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(110));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                poseStack.translate(-0.35D, yTranslate, zTranslate);
            }

        }
    }

    public static class BlowpipeSpeedHandler {

        @SubscribeEvent
        public void onPlayerTick(PlayerTickEvent event) {
            Player player = event.getEntity();
            ItemStack useItem = player.getUseItem();

            if (!useItem.isEmpty() && useItem.getItem() instanceof MayaBlowpipeItem && player.onGround() && !player.isInWater()) {
                player.setDeltaMovement(player.getDeltaMovement().scale(1.5f));
            }

        }
    }
}