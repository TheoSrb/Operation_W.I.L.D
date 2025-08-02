package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWEntityUtilsToClient(int entityId, int resurrectionTimer, int attackTimer) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWEntityUtilsToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_entity_utils_to_client"));

    public static final StreamCodec<FriendlyByteBuf, OWEntityUtilsToClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OWEntityUtilsToClient::entityId,
                    ByteBufCodecs.INT, OWEntityUtilsToClient::resurrectionTimer,
                    ByteBufCodecs.INT, OWEntityUtilsToClient::attackTimer,
                    OWEntityUtilsToClient::new
            );

    @Override
    public CustomPacketPayload.Type<OWEntityUtilsToClient> type() {
        return TYPE;
    }

    public static void handle(OWEntityUtilsToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof OWEntity owEntity) {
                owEntity.resurrectionTimer = packet.resurrectionTimer();
                owEntity.attackTimer = packet.attackTimer();
            }
        });
    }
}