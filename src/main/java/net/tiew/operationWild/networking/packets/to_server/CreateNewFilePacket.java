package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.ClientKillData;

public record CreateNewFilePacket(String worldName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateNewFilePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "create_new_file"));

    public static final StreamCodec<FriendlyByteBuf, CreateNewFilePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, CreateNewFilePacket::worldName,
                    CreateNewFilePacket::new
            );

    public CreateNewFilePacket() {
        this("default_world");
    }

    @Override
    public CustomPacketPayload.Type<CreateNewFilePacket> type() {
        return TYPE;
    }

    public static void handle(CreateNewFilePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientKillData.createEmptyFile(packet.worldName()));
    }
}