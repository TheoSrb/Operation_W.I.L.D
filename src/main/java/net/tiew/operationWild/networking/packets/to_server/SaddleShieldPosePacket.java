package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSaddleShield;

import java.util.List;

public record SaddleShieldPosePacket(int entityId, float[] poses) implements CustomPacketPayload {

    private static final double MAX_DISTANCE_SQR = 128.0 * 128.0;

    public static final CustomPacketPayload.Type<SaddleShieldPosePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "saddle_shield_pose"));

    public static final StreamCodec<FriendlyByteBuf, SaddleShieldPosePacket> STREAM_CODEC =
            StreamCodec.of(SaddleShieldPosePacket::write, SaddleShieldPosePacket::read);

    private static void write(FriendlyByteBuf buf, SaddleShieldPosePacket packet) {
        buf.writeVarInt(packet.entityId());
        buf.writeVarInt(packet.poses().length);
        for (float value : packet.poses()) buf.writeFloat(value);
    }

    private static SaddleShieldPosePacket read(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        int length = buf.readVarInt();
        if (length < 0 || length > OWSaddleShield.MAX_SHIELDS * OWSaddleShield.FLOATS_PER_POSE) {
            throw new IllegalArgumentException("Too many saddle shield values: " + length);
        }
        float[] poses = new float[length];
        for (int i = 0; i < length; i++) poses[i] = buf.readFloat();
        return new SaddleShieldPosePacket(entityId, poses);
    }

    @Override
    public CustomPacketPayload.Type<SaddleShieldPosePacket> type() {
        return TYPE;
    }

    public static void handle(SaddleShieldPosePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getEntity(packet.entityId()) instanceof OWEntity mount)) return;
            if (mount.distanceToSqr(player) > MAX_DISTANCE_SQR) return;
            if (!OWSaddleShield.isPoseReporter(mount, player)) return;

            List<OWSaddleShield> shields = mount.saddleShields();
            float[] data = packet.poses();
            if (shields.isEmpty() || data.length != shields.size() * OWSaddleShield.FLOATS_PER_POSE) return;
            for (float value : data) if (!Float.isFinite(value)) return;

            OWSaddleShield.Pose[] poses = new OWSaddleShield.Pose[shields.size()];
            for (int index = 0; index < poses.length; index++) {
                poses[index] = shields.get(index).acceptReport(mount, data, index * OWSaddleShield.FLOATS_PER_POSE);
            }
            mount.reportSaddleShieldPoses(poses);
        });
    }
}
