package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.ClientKillData;

public record SyncKillDataPacket(String entityType, String worldName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncKillDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_kill_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncKillDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SyncKillDataPacket::entityType,
                    ByteBufCodecs.STRING_UTF8, SyncKillDataPacket::worldName,
                    SyncKillDataPacket::new
            );

    @Override
    public CustomPacketPayload.Type<SyncKillDataPacket> type() {
        return TYPE;
    }

    public static void handle(SyncKillDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientKillData.saveKillData(packet.worldName(), packet.entityType());
        });
    }
}