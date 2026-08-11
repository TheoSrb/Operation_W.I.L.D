package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

public record OWAttackRejectedPacket(int entityId, int attackId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWAttackRejectedPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_attack_rejected"));

    public static final StreamCodec<FriendlyByteBuf, OWAttackRejectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OWAttackRejectedPacket::entityId,
                    ByteBufCodecs.INT, OWAttackRejectedPacket::attackId,
                    OWAttackRejectedPacket::new
            );

    @Override
    public CustomPacketPayload.Type<OWAttackRejectedPacket> type() {
        return TYPE;
    }

    public static void handle(OWAttackRejectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> net.tiew.operationWild.client.OWClientHooks
                .onAttackRejected(packet.entityId(), packet.attackId()));
    }
}
