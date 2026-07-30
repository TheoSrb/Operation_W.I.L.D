package net.tiew.operationWild.networking.packets.to_client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientArenaVenueUnlocks;

/** Pousse au joueur le bitmask des décors d'arène qu'il a débloqués. */
public record SyncArenaVenueUnlocksPacket(int mask) implements CustomPacketPayload {

    public static final Type<SyncArenaVenueUnlocksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_arena_venue_unlocks"));

    public static final StreamCodec<ByteBuf, SyncArenaVenueUnlocksPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, SyncArenaVenueUnlocksPacket::mask, SyncArenaVenueUnlocksPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncArenaVenueUnlocksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWClientArenaVenueUnlocks.mask = packet.mask());
    }
}
