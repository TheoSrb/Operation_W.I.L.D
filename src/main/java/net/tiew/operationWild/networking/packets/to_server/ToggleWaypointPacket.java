package net.tiew.operationWild.networking.packets.to_server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.waypoint.OWWaypointData;
import net.tiew.operationWild.waypoint.OWWaypointManager;

import java.util.UUID;

/**
 * Bascule l'affichage du waypoint d'une créature depuis l'écran d'options.
 *
 * <p>L'identifiant vient du client, donc la propriété est revérifiée côté serveur
 * ({@link OWWaypointData#toggle}) : personne ne peut éteindre les repères d'un autre joueur.
 * Une créature apprivoisée à l'instant n'a pas forcément encore d'entrée — son tick l'inscrit
 * périodiquement —, d'où l'inscription immédiate quand c'est la monture du demandeur.</p>
 */
public record ToggleWaypointPacket(UUID entityUuid) implements CustomPacketPayload {

    public static final Type<ToggleWaypointPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "toggle_waypoint"));

    public static final StreamCodec<ByteBuf, ToggleWaypointPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> UUIDUtil.STREAM_CODEC.encode(buf, p.entityUuid()),
            buf -> new ToggleWaypointPacket(UUIDUtil.STREAM_CODEC.decode(buf)));

    @Override
    public Type<ToggleWaypointPacket> type() { return TYPE; }

    public static void handle(ToggleWaypointPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MinecraftServer server = player.getServer();
            if (server == null) return;

            OWWaypointData data = OWWaypointData.get(server);
            if (player.getRootVehicle() instanceof OWEntity mount
                    && mount.getUUID().equals(packet.entityUuid())) {
                data.upsert(mount);
            }
            if (data.toggle(packet.entityUuid(), player.getUUID())) {
                OWWaypointManager.syncTo(server, player);
            }
        });
    }
}
