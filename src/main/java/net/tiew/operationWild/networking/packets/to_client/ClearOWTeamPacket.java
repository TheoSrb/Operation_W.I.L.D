package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

public record ClearOWTeamPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ClearOWTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "clear_ow_team"));

    public static final StreamCodec<ByteBuf, ClearOWTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ClearOWTeamPacket::entityId,
                    ClearOWTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ClearOWTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> net.tiew.operationWild.client.OWClientHooks.clearClientTeam(packet.entityId()));
    }
}