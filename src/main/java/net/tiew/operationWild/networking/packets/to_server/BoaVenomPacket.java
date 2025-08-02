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
import net.tiew.operationWild.entity.custom.living.BoaEntity;

public record BoaVenomPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BoaVenomPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "boa_venom"));

    public static final StreamCodec<FriendlyByteBuf, BoaVenomPacket> STREAM_CODEC =
            StreamCodec.unit(new BoaVenomPacket());

    @Override
    public CustomPacketPayload.Type<BoaVenomPacket> type() {
        return TYPE;
    }

    public static void handle(BoaVenomPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof BoaEntity boa) {
                        boa.canVenom = true;
                    }
                }
            }
        });
    }
}

