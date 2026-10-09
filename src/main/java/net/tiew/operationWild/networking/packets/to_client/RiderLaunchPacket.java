package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

public record RiderLaunchPacket(double velocityX, double velocityY, double velocityZ) implements CustomPacketPayload {

    private static final int GUARD_TICKS = 14;
    private static net.minecraft.world.phys.Vec3 expected = null;
    private static int guardTicks = 0;

    public static void guardFlight(Player player) {
        if (guardTicks <= 0 || expected == null || player == null) return;
        guardTicks--;
        if (player.isPassenger() || player.isInWater() || player.horizontalCollision
                || (player.onGround() && guardTicks < GUARD_TICKS - 2)) {
            guardTicks = 0;
            return;
        }
        net.minecraft.world.phys.Vec3 current = player.getDeltaMovement();
        if (current.horizontalDistanceSqr() < expected.horizontalDistanceSqr() * 0.25) {
            player.setDeltaMovement(expected.x, Math.max(current.y, expected.y), expected.z);
        }
        expected = new net.minecraft.world.phys.Vec3(expected.x * 0.91, (expected.y - 0.08) * 0.98, expected.z * 0.91);
    }

    public static final CustomPacketPayload.Type<RiderLaunchPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "rider_launch"));

    public static final StreamCodec<FriendlyByteBuf, RiderLaunchPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, RiderLaunchPacket::velocityX,
            ByteBufCodecs.DOUBLE, RiderLaunchPacket::velocityY,
            ByteBufCodecs.DOUBLE, RiderLaunchPacket::velocityZ,
            RiderLaunchPacket::new);

    @Override
    public CustomPacketPayload.Type<RiderLaunchPacket> type() {
        return TYPE;
    }

    public static void handle(RiderLaunchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (player.isPassenger()) player.stopRiding();
            player.setDeltaMovement(packet.velocityX(), packet.velocityY(), packet.velocityZ());
            player.fallDistance = 0f;
            player.hasImpulse = true;
            expected = new net.minecraft.world.phys.Vec3(packet.velocityX(), packet.velocityY(), packet.velocityZ());
            guardTicks = GUARD_TICKS;
        });
    }
}
