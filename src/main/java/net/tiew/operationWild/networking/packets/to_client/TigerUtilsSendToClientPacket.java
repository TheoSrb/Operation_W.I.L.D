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
import net.tiew.operationWild.entity.custom.living.TigerEntity;

public record TigerUtilsSendToClientPacket(int entityId, int ultimateTimer) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TigerUtilsSendToClientPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_utils_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, TigerUtilsSendToClientPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, TigerUtilsSendToClientPacket::entityId,
                    ByteBufCodecs.INT, TigerUtilsSendToClientPacket::ultimateTimer,
                    TigerUtilsSendToClientPacket::new
            );

    @Override
    public CustomPacketPayload.Type<TigerUtilsSendToClientPacket> type() {
        return TYPE;
    }

    public static void handle(TigerUtilsSendToClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof TigerEntity tiger) {
                tiger.ultimateTimer = packet.ultimateTimer;
            }
        });
    }
}
