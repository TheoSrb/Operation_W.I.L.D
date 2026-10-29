package net.tiew.operationWild.event;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWItemUseRestrictions {

    public static boolean isRestrained(LivingEntity entity) {
        return entity instanceof Player && entity.getVehicle() instanceof OWEntity;
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
