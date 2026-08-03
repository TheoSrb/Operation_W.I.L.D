package net.tiew.operationWild.networking.packets.to_client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.waypoint.OWClientWaypoints;
import net.tiew.operationWild.waypoint.OWWaypointEntry;

import java.util.List;

/**
 * Instantané complet des waypoints appartenant au destinataire.
 *
 * <p>Remplacement intégral et non delta : la liste d'un joueur tient en quelques dizaines
 * d'entrées, et un remplacement ne peut pas dériver — un paquet perdu de vue ne laisse pas de
 * repère fantôme derrière lui.</p>
 */
public record SyncWaypointsPacket(List<OWWaypointEntry> entries) implements CustomPacketPayload {

    public static final Type<SyncWaypointsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_waypoints"));

    public static final StreamCodec<ByteBuf, SyncWaypointsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> OWWaypointEntry.LIST_STREAM_CODEC.encode(buf, p.entries()),
            buf -> new SyncWaypointsPacket(OWWaypointEntry.LIST_STREAM_CODEC.decode(buf)));

    @Override
    public Type<SyncWaypointsPacket> type() { return TYPE; }

    public static void handle(SyncWaypointsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWClientWaypoints.set(packet.entries()));
    }
}
