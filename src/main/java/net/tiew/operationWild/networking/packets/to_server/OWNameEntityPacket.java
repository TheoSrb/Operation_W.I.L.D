package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.OWChooseNameScreen;

public record OWNameEntityPacket(int entityId, String nickname) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWNameEntityPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_name_entity"));

    public static final StreamCodec<FriendlyByteBuf, OWNameEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            OWNameEntityPacket::entityId,
            ByteBufCodecs.STRING_UTF8,
            OWNameEntityPacket::nickname,
            OWNameEntityPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWNameEntityPacket> type() {
        return TYPE;
    }

    public static void handle(OWNameEntityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(packet.entityId());
                if (entity instanceof OWEntity owEntity) {
                    owEntity.setNickname(packet.nickname());
                }
            }
        });
    }
}