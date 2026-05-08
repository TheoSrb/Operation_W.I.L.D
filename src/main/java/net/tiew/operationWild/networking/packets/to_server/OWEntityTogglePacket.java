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

public record OWEntityTogglePacket(String option) implements CustomPacketPayload {

    public static final Type<OWEntityTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_entity_toggle"));

    public static final StreamCodec<FriendlyByteBuf, OWEntityTogglePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, OWEntityTogglePacket::option,
                    OWEntityTogglePacket::new
            );

    @Override
    public Type<OWEntityTogglePacket> type() { return TYPE; }

    public static void handle(OWEntityTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getRootVehicle() instanceof OWEntity entity) {
                switch (packet.option()) {
                    case "passive"    -> entity.setPassive(!entity.isPassive());
                    case "autoPickup" -> entity.setAutoPickup(!entity.isAutoPickup());
                    case "sit"        -> entity.setSitting(!entity.isSitting());
                }
            }
        });
    }
}
