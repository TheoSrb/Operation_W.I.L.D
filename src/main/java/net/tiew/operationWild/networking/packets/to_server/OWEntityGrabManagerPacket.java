package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.sound.OWSounds;

public record OWEntityGrabManagerPacket(boolean isRightClickDown) implements CustomPacketPayload {

    public static final Type<OWEntityGrabManagerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_entity_grab_manager"));

    public static final StreamCodec<FriendlyByteBuf, OWEntityGrabManagerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            FriendlyByteBuf::writeBoolean,
                            FriendlyByteBuf::readBoolean
                    ),
                    OWEntityGrabManagerPacket::isRightClickDown,
                    OWEntityGrabManagerPacket::new
            );

    @Override
    public Type<OWEntityGrabManagerPacket> type() {
        return TYPE;
    }

    public static void handle(OWEntityGrabManagerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null) {
                LivingEntity entity = (LivingEntity) player.getVehicle();

                if (entity instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() != null && crocodile.getGrabbedTarget() == player) {
                    if (packet.isRightClickDown()) {
                        crocodile.setGrabTimeout(crocodile.getGrabTimeout() - 15);
                    }
                }
            }
        });
    }
}