package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.living.BoaEntity;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;

public record ElephantFootstepPacket(int elephantId) implements CustomPacketPayload {

    public static final Type<ElephantFootstepPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_footstep"));

    public static final StreamCodec<FriendlyByteBuf, ElephantFootstepPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ElephantFootstepPacket::elephantId,
                    ElephantFootstepPacket::new
            );

    @Override
    public Type<ElephantFootstepPacket> type() {
        return TYPE;
    }

    public static void handle(ElephantFootstepPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.elephantId());

                if (entity instanceof ElephantEntity elephant) {
                    elephant.applyFootstep();
                }
            }
        });
    }
}