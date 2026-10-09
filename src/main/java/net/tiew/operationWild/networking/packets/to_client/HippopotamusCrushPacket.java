package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

public record HippopotamusCrushPacket(int entityId, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HippopotamusCrushPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "hippopotamus_crush"));

    public static final StreamCodec<FriendlyByteBuf, HippopotamusCrushPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HippopotamusCrushPacket::entityId,
            ByteBufCodecs.VAR_INT, HippopotamusCrushPacket::durationTicks,
            HippopotamusCrushPacket::new);

    @Override
    public CustomPacketPayload.Type<HippopotamusCrushPacket> type() {
        return TYPE;
    }

    public static void handle(HippopotamusCrushPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> net.tiew.operationWild.entity.client.render.misc.OWCrushedEntityRenderer
                .crush(packet.entityId(), packet.durationTicks()));
    }
}
