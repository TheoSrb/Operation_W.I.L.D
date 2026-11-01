package net.tiew.operationWild.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWItemUseRestrictions {

    public static boolean isRestrained(LivingEntity entity) {
        if (!(entity instanceof Player player) || !(player.getVehicle() instanceof OWEntity vehicle)) return false;
        return !isFreeRider(player, vehicle);
    }

    public static boolean isFreeRider(Player player, OWEntity vehicle) {
        if (vehicle instanceof Submarine || !vehicle.isTame()) return false;
        LivingEntity owner = vehicle.getOwner();
        return vehicle.isOwnedBy(player) || (owner != null && OWEntity.shareTribe(owner, player));
    }

    public static boolean isUsableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getUseAnimation() != UseAnim.NONE) return true;
        Item item = stack.getItem();
        if (item instanceof ArrowItem) return false;
        return item instanceof ProjectileItem || item instanceof SpawnEggItem || item instanceof EnderpearlItem
                || item instanceof FishingRodItem || stack.has(DataComponents.FOOD);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!isRestrained(event.getEntity())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (isRestrained(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!isRestrained(event.getEntity())) return;
        event.setCanceled(true);
        event.getEntity().stopUsingItem();
    }

    @SubscribeEvent
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        if (isRestrained(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        SyringeRifleItem.tickReloadSlowdown(event.getEntity());
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!SyringeRifleItem.isReloading(SyringeRifleItem.activeRifle(player))) return;
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, Math.min(motion.y, 0.0), motion.z);
        player.hasImpulse = false;
    }
}
