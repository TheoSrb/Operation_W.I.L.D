package net.tiew.operationWild.networking.packets.to_client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.platform.IOWPlatformCarrier;
import net.tiew.operationWild.entity.platform.OWPlatformBlocks;

public record PlatformSyncPacket(int entityId, OWPlatformBlocks blocks) implements CustomPacketPayload {

    public static final Type<PlatformSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "platform_sync"));

    public static final StreamCodec<ByteBuf, PlatformSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlatformSyncPacket::entityId,
            OWPlatformBlocks.STREAM_CODEC, PlatformSyncPacket::blocks,
            PlatformSyncPacket::new);

    @Override
    public Type<PlatformSyncPacket> type() {
        return TYPE;
    }

    public static void handle(PlatformSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            Entity entity = context.player().level().getEntity(packet.entityId());
            if (entity instanceof IOWPlatformCarrier carrier) carrier.receivePlatformBlocks(packet.blocks());
        });
    }
}
