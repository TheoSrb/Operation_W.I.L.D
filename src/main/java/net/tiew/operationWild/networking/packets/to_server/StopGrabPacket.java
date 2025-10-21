package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;

public record StopGrabPacket() implements CustomPacketPayload {

    public static final Type<StopGrabPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "stop_grab"));

    public static final StreamCodec<FriendlyByteBuf, StopGrabPacket> STREAM_CODEC =
            StreamCodec.unit(new StopGrabPacket());

    @Override
    public Type<StopGrabPacket> type() {
        return TYPE;
    }

    public static void handle(StopGrabPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof CrocodileEntity crocodile) {
                        crocodile.getGrabbedTarget().stopRiding();
                        crocodile.setGrabbing(false, null);
                        crocodile.setTarget(null);
                    }
                }
            }
        });
    }
}

