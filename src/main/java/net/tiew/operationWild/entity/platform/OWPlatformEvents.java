package net.tiew.operationWild.entity.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.packets.to_client.PlatformSyncPacket;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OWPlatformEvents {

    private OWPlatformEvents() {
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        OWPlatformPhysics.carry(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof IOWPlatformCarrier carrier) {
            OWPlatform platform = carrier.getPlatform();
            if (platform != null) platform.tick();
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (!(target instanceof IOWPlatformCarrier carrier) || !(event.getEntity() instanceof ServerPlayer player)) return;
        OWPlatform platform = carrier.getPlatform();
        if (platform == null) return;
        PacketDistributor.sendToPlayer(player, new PlatformSyncPacket(target.getId(), platform.snapshot(false)));
    }

    public static void broadcast(Entity carrier, OWPlatformBlocks blocks) {
        if (carrier.level().isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingEntity(carrier, new PlatformSyncPacket(carrier.getId(), blocks));
    }
}
