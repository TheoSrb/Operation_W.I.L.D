package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

public record TigerLeapStatePacket(int entityId, boolean isLeaping, boolean isPreparing) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TigerLeapStatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_leap_state"));

    public static final StreamCodec<FriendlyByteBuf, TigerLeapStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, TigerLeapStatePacket::entityId,
                    ByteBufCodecs.BOOL, TigerLeapStatePacket::isLeaping,
                    ByteBufCodecs.BOOL, TigerLeapStatePacket::isPreparing,
                    TigerLeapStatePacket::new
            );

    @Override
    public CustomPacketPayload.Type<TigerLeapStatePacket> type() {
        return TYPE;
    }

    public static void handle(TigerLeapStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = net.tiew.operationWild.client.OWClientHooks.clientEntity(packet.entityId());
            if (entity instanceof TigerEntity tiger) {
                tiger.isLeaping = packet.isLeaping();
                tiger.isPreparing = packet.isPreparing();
            }
        });
    }
}