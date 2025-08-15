package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWRunningPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWRunningPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_running_packet"));

    public static final StreamCodec<FriendlyByteBuf, OWRunningPacket> STREAM_CODEC =
            StreamCodec.unit(new OWRunningPacket());

    @Override
    public CustomPacketPayload.Type<OWRunningPacket> type() {
        return TYPE;
    }

    public static void handle(OWRunningPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof OWEntity owEntity) {
                        if (Minecraft.getInstance().options.keySprint.isDown() && owEntity.getVitalEnergy() < owEntity.getMaxVitalEnergy()) {
                            owEntity.setRunning(true);
                        } else owEntity.setRunning(false);
                    }
                }
            }
        });
    }
}

