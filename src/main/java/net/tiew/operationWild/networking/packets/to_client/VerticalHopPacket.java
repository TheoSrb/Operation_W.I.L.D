package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

public record VerticalHopPacket(float hop) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VerticalHopPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "vertical_hop"));

    public static final StreamCodec<FriendlyByteBuf, VerticalHopPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, VerticalHopPacket::hop,
            VerticalHopPacket::new);

    @Override
    public CustomPacketPayload.Type<VerticalHopPacket> type() {
        return TYPE;
    }

    public static void handle(VerticalHopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.max(motion.y, packet.hop()), motion.z);
        });
    }
}
