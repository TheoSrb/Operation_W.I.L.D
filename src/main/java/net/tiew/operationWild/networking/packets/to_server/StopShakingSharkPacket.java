package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.living.TigerSharkEntity;

public record StopShakingSharkPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StopShakingSharkPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "stop_shaking_shark"));

    public static final StreamCodec<FriendlyByteBuf, StopShakingSharkPacket> STREAM_CODEC =
            StreamCodec.unit(new StopShakingSharkPacket());

    @Override
    public CustomPacketPayload.Type<StopShakingSharkPacket> type() {
        return TYPE;
    }

    public static void handle(StopShakingSharkPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player != null) {
                    player.level().getEntitiesOfClass(TigerSharkEntity.class,
                                    player.getBoundingBox().inflate(5.0))
                            .stream()
                            .filter(tigerShark -> tigerShark.isShakingPrey() && tigerShark.getTarget() == player)
                            .forEach(tigerShark -> tigerShark.setShakingPrey(false));
                }
            }
        });
    }
}

