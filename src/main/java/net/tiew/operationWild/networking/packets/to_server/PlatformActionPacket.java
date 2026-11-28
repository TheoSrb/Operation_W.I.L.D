package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.platform.OWPlatformBuilding;

public record PlatformActionPacket(int entityId, int action, int x, int y, int z, int face,
                                   float hitX, float hitY, float hitZ, boolean offHand) implements CustomPacketPayload {

    public static final int PLACE = 0;
    public static final int START_BREAK = 1;
    public static final int ABORT_BREAK = 2;
    public static final int FINISH_BREAK = 3;
    public static final int USE = 4;

    public static final Type<PlatformActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "platform_action"));

    public static final StreamCodec<FriendlyByteBuf, PlatformActionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId());
                buf.writeByte(p.action());
                buf.writeVarInt(p.x());
                buf.writeVarInt(p.y());
                buf.writeVarInt(p.z());
                buf.writeByte(p.face());
                buf.writeFloat(p.hitX());
                buf.writeFloat(p.hitY());
                buf.writeFloat(p.hitZ());
                buf.writeBoolean(p.offHand());
            },
            buf -> new PlatformActionPacket(buf.readVarInt(), buf.readByte(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readByte(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean()));

    @Override
    public Type<PlatformActionPacket> type() {
        return TYPE;
    }

    public InteractionHand hand() {
        return offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static void handle(PlatformActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) OWPlatformBuilding.handle(player, packet);
        });
    }
}
