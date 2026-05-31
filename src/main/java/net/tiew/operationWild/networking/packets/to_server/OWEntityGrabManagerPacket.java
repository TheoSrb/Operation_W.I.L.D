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
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
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
            if (player == null || !packet.isRightClickDown()) return;

            // Recherche de proximité — fiable même si le sync passenger n'est pas encore arrivé
            player.level().getEntitiesOfClass(CrocodileEntity.class, player.getBoundingBox().inflate(5.0))
                    .stream()
                    .filter(c -> c.getGrabbedTarget() == player)
                    .findFirst()
                    .ifPresent(croc -> croc.setGrabTimeout(croc.getGrabTimeout() - 15));

            LivingEntity vehicle = (LivingEntity) player.getVehicle();
            if (vehicle instanceof TigerEntity tiger && tiger.getGrabbedTarget() != null && tiger.getGrabbedTarget() == player) {
                tiger.setGrabTimeout(tiger.getGrabTimeout() - 15);
            }
        });
    }
}