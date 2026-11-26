package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;

public record OWFlightPacket(int action, int state) implements CustomPacketPayload {

    public static final int TAKEOFF = 0;
    public static final int STATE = 1;
    public static final int LAND = 2;

    public static final CustomPacketPayload.Type<OWFlightPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "entity_flight"));

    public static final StreamCodec<FriendlyByteBuf, OWFlightPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, OWFlightPacket::action, ByteBufCodecs.VAR_INT, OWFlightPacket::state, OWFlightPacket::new);

    @Override
    public CustomPacketPayload.Type<OWFlightPacket> type() {
        return TYPE;
    }

    public static void handle(OWFlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof OWSemiFlyingEntity flyer)) return;
            if (flyer.getControllingPassenger() != player) return;

            switch (packet.action()) {
                case TAKEOFF -> flyer.serverRiderTakeoff(player);
                case LAND -> flyer.serverRiderLand();
                default -> flyer.serverRiderState(packet.state());
            }
        });
    }
}
