package net.tiew.operationWild.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.misc.PrimitiveSpearProjectileEntity;

public class PrimitiveSpearItem extends Item implements ProjectileItem {

    public PrimitiveSpearItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 6;
    }

    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack itemStack, LivingEntity living) {
        return 72000;
    }

    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity living, int i) {

        ItemEnchantments enchantment = itemStack.getEnchantments();
        Holder<Enchantment> lightnessHolder = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(OWEnchantments.LIGHTNESS);

        int piercingPointLevel = enchantment.getLevel(lightnessHolder);

        if (living instanceof Player $$4) {
            int $$6 = this.getUseDuration(itemStack, living) - i;
            if ($$6 >= (10 - (piercingPointLevel * 3))) {
                float $$7 = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, $$4);
                if (!($$7 > 0.0F) || $$4.isInWaterOrRain()) {
                    if (!isTooDamagedToUse(itemStack)) {
                        SoundEvent $$8 = SoundEvents.ARROW_SHOOT;
                        if (!level.isClientSide) {
                            itemStack.hurtAndBreak(1, $$4, LivingEntity.getSlotForHand(living.getUsedItemHand()));
                            if ($$7 == 0.0F) {
                                PrimitiveSpearProjectileEntity $$9 = new PrimitiveSpearProjectileEntity(level, $$4, itemStack);
                                $$9.shootFromRotation($$4, $$4.getXRot(), $$4.getYRot(), 0.0F, 1.5F, 1.0F);
                                if ($$4.hasInfiniteMaterials()) {
                                    $$9.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                }

                                level.addFreshEntity($$9);
                                level.playSound((Player)null, $$9, (SoundEvent)$$8, SoundSource.PLAYERS, 1.0F, 0.75F);
                                if (!$$4.hasInfiniteMaterials()) {
                                    $$4.getInventory().removeItem(itemStack);
                                }
                            }
                        }

                        $$4.awardStat(Stats.ITEM_USED.get(this));
                        if ($$7 > 0.0F) {
                            float $$10 = $$4.getYRot();
                            float $$11 = $$4.getXRot();
                            float $$12 = -Mth.sin($$10 * ((float)Math.PI / 180F)) * Mth.cos($$11 * ((float)Math.PI / 180F));
                            float $$13 = -Mth.sin($$11 * ((float)Math.PI / 180F));
                            float $$14 = Mth.cos($$10 * ((float)Math.PI / 180F)) * Mth.cos($$11 * ((float)Math.PI / 180F));
                            float $$15 = Mth.sqrt($$12 * $$12 + $$13 * $$13 + $$14 * $$14);
                            $$12 *= $$7 / $$15;
                            $$13 *= $$7 / $$15;
                            $$14 *= $$7 / $$15;
                            $$4.push((double)$$12, (double)$$13, (double)$$14);
                            $$4.startAutoSpinAttack(20, 8.0F, itemStack);
                            if ($$4.onGround()) {
                                float $$16 = 1.1999999F;
                                $$4.move(MoverType.SELF, new Vec3((double)0.0F, (double)1.1999999F, (double)0.0F));
                            }

                            level.playSound((Player)null, $$4, (SoundEvent)$$8, SoundSource.PLAYERS, 1.0F, 0.75F);
                        }

                    }
                }
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack $$3 = player.getItemInHand(hand);
        if (isTooDamagedToUse($$3)) {
            return InteractionResultHolder.fail($$3);
        } else if (EnchantmentHelper.getTridentSpinAttackStrength($$3, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail($$3);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume($$3);
        }
    }

    private static boolean isTooDamagedToUse(ItemStack itemStack) {
        return itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1;
    }

    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownTrident $$4 = new ThrownTrident(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        $$4.pickup = AbstractArrow.Pickup.ALLOWED;
        return $$4;
    }
}
