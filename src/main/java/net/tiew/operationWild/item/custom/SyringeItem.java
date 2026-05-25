package net.tiew.operationWild.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SyringeData;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.item.OWItems;

public class SyringeItem extends Item {

    public SyringeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof OWEntity owEntity)) {
            return InteractionResult.PASS;
        }
        // Check cooldown before isClientSide so the client also returns PASS → no use animation
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel level = (ServerLevel) player.level();
        float maxHealth = owEntity.getMaxHealth();

        // successChance = clamp(15 / maxHealth, 5%, 95%)
        // Example: 50 HP → 30%, 20 HP → 75%, 100 HP → 15%
        float successChance = Math.max(0.05f, Math.min(0.95f, 15.0f / maxHealth));

        double ex = owEntity.getX();
        double ey = owEntity.getY() + owEntity.getBbHeight() * 0.5;
        double ez = owEntity.getZ();

        player.getCooldowns().addCooldown(this, 100);

        if (player.getRandom().nextFloat() > successChance) {
            // Failure: consume syringe (setItemInHand works in creative too)
            int newCount = stack.getCount() - 1;
            player.setItemInHand(hand, newCount > 0 ? stack.copyWithCount(newCount) : ItemStack.EMPTY);
            level.playSound(null, ex, ey, ez, SoundEvents.ITEM_BREAK, owEntity.getSoundSource(), 1.0f, 0.8f);
            level.sendParticles(ParticleTypes.SMOKE, ex, ey, ez, 15, 0.3, 0.4, 0.3, 0.04);
            return InteractionResult.SUCCESS;
        }

        level.playSound(null, ex, ey, ez, SoundEvents.BOTTLE_FILL, owEntity.getSoundSource(), 1.0f, 1.5f);
        level.playSound(null, ex, ey, ez, SoundEvents.PLAYER_HURT, owEntity.getSoundSource(), 0.5f, 1.4f);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, ex, ey, ez, 8, 0.25, 0.25, 0.25, 0.1);

        SyringeData data = new SyringeData(
                BuiltInRegistries.ENTITY_TYPE.getKey(owEntity.getType()).toString(),
                owEntity.getUUID(),
                maxHealth,
                owEntity.getDamage(),
                owEntity.getSpeed(),
                owEntity.isFemale(),
                (float) owEntity.getTamingExperience(),
                owEntity.getEntityColor(),
                owEntity.getSkinIndex()
        );

        ItemStack bloodyStack = new ItemStack(OWItems.BLOODY_SYRINGE.get());
        bloodyStack.set(OWDataComponentTypes.SYRINGE_DATA.get(), data);

        int remaining = stack.getCount() - 1;
        if (remaining == 0) {
            // Single syringe: replace it directly in the same slot
            player.setItemInHand(hand, bloodyStack);
        } else {
            // Stack of syringes: keep remaining in hand, bloody goes to inventory
            player.setItemInHand(hand, stack.copyWithCount(remaining));
            if (!player.addItem(bloodyStack)) {
                player.drop(bloodyStack, false);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
