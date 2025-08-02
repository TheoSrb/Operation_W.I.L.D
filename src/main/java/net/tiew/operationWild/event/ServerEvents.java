package net.tiew.operationWild.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.object.SeabugShard;
import net.tiew.operationWild.entity.custom.vehicle.Submarine;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.BookNotificationPacket;
import net.tiew.operationWild.networking.packets.to_server.SyncKillDataPacket;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ClientEvents.tamingExperience >= 165) {

        } else if (ClientEvents.tamingExperience >= 80) {

        } else if (ClientEvents.tamingExperience >= 25) {

        }
    }

    @SubscribeEvent
    public static void onPlayerKill(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source == null) {
            source = event.getSource().getDirectEntity();
        }
        Entity target = event.getEntity();

        if (source instanceof ServerPlayer player) {
            if (target instanceof OWEntity owEntity && !owEntity.isTame() && !(owEntity instanceof Submarine) && !(owEntity instanceof SeabugShard)) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag modData = playerData.getCompound("ow");

                boolean wasAlreadyKilled = false;

                String entityType = "";
                switch (target.getClass().getSimpleName()) {
                    case "BoaEntity":
                        wasAlreadyKilled = modData.getBoolean("has_killed_boa");
                        modData.putBoolean("has_killed_boa", true);
                        entityType = "boa";
                        break;
                    case "PeacockEntity":
                        wasAlreadyKilled = modData.getBoolean("has_killed_peacock");
                        modData.putBoolean("has_killed_peacock", true);
                        entityType = "peacock";
                        break;
                    case "TigerEntity":
                        wasAlreadyKilled = modData.getBoolean("has_killed_tiger");
                        modData.putBoolean("has_killed_tiger", true);
                        entityType = "tiger";
                        break;
                    case "TigerSharkEntity":
                        wasAlreadyKilled = modData.getBoolean("has_killed_tiger_shark");
                        modData.putBoolean("has_killed_tiger_shark", true);
                        entityType = "tiger_shark";
                        break;
                }

                playerData.put("ow", modData);

                if (!entityType.isEmpty()) {
                    String worldName = ClientEvents.getWorldName(player);
                    SyncKillDataPacket packet = new SyncKillDataPacket(entityType, worldName);
                    OWNetworkHandler.sendToClient(packet, player);
                }

                if (!wasAlreadyKilled) sendNotificationBook(player, entityType, false);
            }
        }
    }

    public static void sendNotificationBook(ServerPlayer player, String entityType, boolean isTaming) {
        BookNotificationPacket packet = new BookNotificationPacket(entityType, isTaming);
        OWNetworkHandler.sendToClient(packet, player);
    }
}