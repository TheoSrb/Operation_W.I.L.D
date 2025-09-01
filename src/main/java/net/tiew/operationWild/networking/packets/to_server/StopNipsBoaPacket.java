package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;

public record StopNipsBoaPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StopNipsBoaPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "stop_nips_boa"));

    public static final StreamCodec<FriendlyByteBuf, StopNipsBoaPacket> STREAM_CODEC =
            StreamCodec.unit(new StopNipsBoaPacket());

    @Override
    public CustomPacketPayload.Type<StopNipsBoaPacket> type() {
        return TYPE;
    }

    public static void handle(StopNipsBoaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player != null) {
                    BoaEntity boaEntity = player.getPassengers().stream()
                            .filter(passenger -> passenger instanceof BoaEntity)
                            .map(passenger -> (BoaEntity) passenger)
                            .findFirst()
                            .orElse(null);

                    if (boaEntity != null) {
                        boaEntity.stopNips();
                    }
                }
            }
        });
    }
}

