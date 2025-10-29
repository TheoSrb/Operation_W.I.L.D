package net.tiew.operationWild.item.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.item.OWItems;

public class ReptilianDaggerItem extends SwordItem {

    public ReptilianDaggerItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean mainIsDagger = main.is(OWItems.REPTILIAN_DAGGER.get());
        boolean offIsDagger = off.is(OWItems.REPTILIAN_DAGGER.get());

        if (mainIsDagger && !offIsDagger) {
            ItemStack newDagger = stack.copy();
            player.setItemInHand(InteractionHand.OFF_HAND, newDagger);
            off = newDagger;
            offIsDagger = true;
        }

        if (!mainIsDagger && offIsDagger) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            off = ItemStack.EMPTY;
            offIsDagger = false;
        }

        if (mainIsDagger && offIsDagger) {
            int mainDamage = main.getDamageValue();
            int offDamage = off.getDamageValue();
            int syncedDamage = Math.max(mainDamage, offDamage);

            if (mainDamage != syncedDamage) {
                main.setDamageValue(syncedDamage);
            }
            if (offDamage != syncedDamage) {
                off.setDamageValue(syncedDamage);
                player.setItemInHand(InteractionHand.OFF_HAND, off);
            }
        }
    }


    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            String lastHandKey = "reptilian_last_hand";
            String currentHand = player.getMainHandItem() == stack ? "main" : "off";

            String lastHand = player.getPersistentData().getString(lastHandKey);
            if (!lastHand.equals(currentHand)) {
                target.invulnerableTime = 0;
            }

            player.getPersistentData().putString(lastHandKey, currentHand);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().is(OWItems.REPTILIAN_DAGGER.get())) {
            player.swing(InteractionHand.OFF_HAND, true);

            if (!level.isClientSide) {
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();

                player.setItemInHand(InteractionHand.MAIN_HAND, offHandItem);
                player.setItemInHand(InteractionHand.OFF_HAND, mainHandItem);

                Entity target = getTargetEntity(player, level);
                if (target instanceof LivingEntity livingTarget) {
                    String lastHandKey = "reptilian_last_hand";
                    String currentHand = "main";

                    String lastHand = player.getPersistentData().getString(lastHandKey);
                    if (!lastHand.equals(currentHand)) {
                        livingTarget.invulnerableTime = 0;
                    }

                    player.getPersistentData().putString(lastHandKey, currentHand);

                    player.attack(livingTarget);

                    if (!offHandItem.isEmpty() && offHandItem.isDamageableItem()) {
                        offHandItem.setDamageValue(offHandItem.getDamageValue() + 1);

                        if (offHandItem.getDamageValue() >= offHandItem.getMaxDamage()) {
                            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                            player.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
                        }
                    }

                    if (!player.level().isClientSide()) {
                        if (player.getAttackStrengthScale(0.5f) >= 1.0f) {
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                            player.sweepAttack();
                        }
                    }

                    player.resetAttackStrengthTicker();
                }

                player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
                player.setItemInHand(InteractionHand.OFF_HAND, offHandItem);
            }
        }
        return super.use(level, player, hand);
    }


    private Entity getTargetEntity(Player player, Level level) {
        double reach = 4.25;

        return level.getEntities(player, player.getBoundingBox().inflate(reach))
                .stream()
                .filter(entity -> entity instanceof LivingEntity && entity != player)
                .filter(player::hasLineOfSight)
                .min((e1, e2) -> Double.compare(
                        player.distanceToSqr(e1),
                        player.distanceToSqr(e2)
                ))
                .orElse(null);
    }
}
