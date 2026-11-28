package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.cocoon.CocoonClientState;

public record TarantulaCocoonPacket(int entityId, int layers, int required, int lockTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TarantulaCocoonPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tarantula_cocoon"));

    public static final StreamCodec<FriendlyByteBuf, TarantulaCocoonPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, TarantulaCocoonPacket::entityId,
                    ByteBufCodecs.VAR_INT, TarantulaCocoonPacket::layers,
                    ByteBufCodecs.VAR_INT, TarantulaCocoonPacket::required,
                    ByteBufCodecs.VAR_INT, TarantulaCocoonPacket::lockTicks,
                    TarantulaCocoonPacket::new
            );

    @Override
    public CustomPacketPayload.Type<TarantulaCocoonPacket> type() {
        return TYPE;
    }

    public static void handle(TarantulaCocoonPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CocoonClientState.update(packet.entityId(), packet.layers(), packet.required(), packet.lockTicks()));
    }
}
