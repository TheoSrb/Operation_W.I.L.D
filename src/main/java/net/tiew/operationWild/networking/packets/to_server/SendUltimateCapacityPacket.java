package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record SendUltimateCapacityPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SendUltimateCapacityPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "send_ultimate_capacity"));

    public static final StreamCodec<FriendlyByteBuf, SendUltimateCapacityPacket> STREAM_CODEC =
            StreamCodec.unit(new SendUltimateCapacityPacket());

    @Override
    public CustomPacketPayload.Type<SendUltimateCapacityPacket> type() {
        return TYPE;
    }

    public static void handle(SendUltimateCapacityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof OWEntity owEntity && !owEntity.isUltimate()) {
                    }
                }
            }
        });
    }
}

