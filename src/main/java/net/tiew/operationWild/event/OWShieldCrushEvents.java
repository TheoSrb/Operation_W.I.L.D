package net.tiew.operationWild.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.breakout.OWBreakLevel;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWShieldCrushEvents {

    private static final float STONE_MIN_HITS = 7f;
    private static final float STONE_MAX_HITS = 11f;
    private static final float METAL_MIN_HITS = 3f;
    private static final float METAL_MAX_HITS = 5f;

    private static final float MIN_STRENGTH = 0.5f;
    private static final float MAX_STRENGTH = 2f;
    private static final float ROLL_SPREAD = 0.7f;
    private static final float ROLL_FLOOR = 0.15f;
    private static final float STRENGTH_WEIGHT = 0.6f;
    private static final float MIN_VANILLA_SHIELD_DAMAGE = 3f;

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getOriginalBlock() || !event.getBlocked()) return;
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        DamageSource source = event.getDamageSource();
        if (source.is(DamageTypeTags.IS_PROJECTILE)) return;
        if (!(source.getDirectEntity() instanceof OWEntity attacker)) return;

        ItemStack shield = player.getUseItem();
        if (shield.isEmpty() || !shield.canPerformAction(ItemAbilities.SHIELD_BLOCK) || !shield.isDamageableItem()) return;

        switch (attacker.effectiveBlockBreakLevel()) {
            case OWBreakLevel.STONE -> crush(event, attacker, player, shield, STONE_MIN_HITS, STONE_MAX_HITS);
            case OWBreakLevel.METAL -> crush(event, attacker, player, shield, METAL_MIN_HITS, METAL_MAX_HITS);
            case OWBreakLevel.OBSIDIAN -> {
                event.setShieldDamage(0f);
                shatter(player, shield);
            }
            default -> {
            }
        }
    }

    private static void crush(LivingShieldBlockEvent event, OWEntity attacker, Player player, ItemStack shield, float minHits, float maxHits) {
        float strength = Mth.clamp(event.getOriginalBlockedDamage() / Math.max(attacker.getCombatDamage(), 0.01f), MIN_STRENGTH, MAX_STRENGTH);
        float roll = Mth.clamp(player.getRandom().nextFloat() * ROLL_SPREAD + ROLL_FLOOR + (strength - 1f) * STRENGTH_WEIGHT, 0f, 1f);
        float hits = Mth.lerp(roll, maxHits, minHits);
        int durabilityLoss = Math.max(1, Math.round(shield.getMaxDamage() / hits));
        event.setShieldDamage(Math.max(MIN_VANILLA_SHIELD_DAMAGE, durabilityLoss - 1));
    }

    private static void shatter(Player player, ItemStack shield) {
        if (player.hasInfiniteMaterials()) return;

        InteractionHand hand = player.getUsedItemHand();
        EquipmentSlot slot = LivingEntity.getSlotForHand(hand);
        Item item = shield.getItem();
        ItemStack broken = shield.copy();

        shield.shrink(1);
        player.onEquippedItemBroken(item, slot);
        EventHooks.onPlayerDestroyItem(player, broken, hand);
        player.setItemSlot(slot, ItemStack.EMPTY);
        player.stopUsingItem();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BREAK,
                player.getSoundSource(), 0.8f, 0.8f + player.getRandom().nextFloat() * 0.4f);
    }
}
