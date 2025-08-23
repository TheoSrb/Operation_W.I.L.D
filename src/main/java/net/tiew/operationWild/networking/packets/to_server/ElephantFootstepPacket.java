package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.sound.OWSounds;

public record ElephantFootstepPacket(double x, double y, double z) implements CustomPacketPayload {

    public static final Type<ElephantFootstepPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_footstep"));

    public static final StreamCodec<FriendlyByteBuf, ElephantFootstepPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, ElephantFootstepPacket::x,
                    ByteBufCodecs.DOUBLE, ElephantFootstepPacket::y,
                    ByteBufCodecs.DOUBLE, ElephantFootstepPacket::z,
                    ElephantFootstepPacket::new
            );

    @Override
    public Type<ElephantFootstepPacket> type() {
        return TYPE;
    }

    public static void handle(ElephantFootstepPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            for (int i = 0; i < 3; i++) {
                context.player().level().playSound(null, packet.x(), packet.y(), packet.z(), OWSounds.ELEPHANT_FOOTSTEP.get(), SoundSource.NEUTRAL, 1.5f, 1.0f);
            }
        });
    }
}
