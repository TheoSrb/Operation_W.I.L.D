package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWGlidePacket(int action) implements CustomPacketPayload {

    public static final int STOP = 0;
    public static final int START = 1;
    public static final int TAKEOFF = 2;

    public static final CustomPacketPayload.Type<OWGlidePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "entity_glide"));

    public static final StreamCodec<FriendlyByteBuf, OWGlidePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, OWGlidePacket::action, OWGlidePacket::new);

    @Override
    public CustomPacketPayload.Type<OWGlidePacket> type() {
        return TYPE;
    }

    public static void handle(OWGlidePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof OWEntity entity) || !entity.canGlide()) return;
            if (entity.getControllingPassenger() != player) return;

            switch (packet.action()) {
                case TAKEOFF -> entity.requestGlideTakeoff(player);
                case START -> entity.requestGlide(true);
                default -> entity.requestGlide(false);
            }
        });
    }
}
