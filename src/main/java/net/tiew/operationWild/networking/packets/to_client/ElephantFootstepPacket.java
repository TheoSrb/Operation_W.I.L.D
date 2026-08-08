package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;

public record ElephantFootstepPacket(int entityId, boolean right) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ElephantFootstepPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_footstep"));

    public static final StreamCodec<FriendlyByteBuf, ElephantFootstepPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ElephantFootstepPacket::entityId,
                    ByteBufCodecs.BOOL, ElephantFootstepPacket::right,
                    ElephantFootstepPacket::new
            );

    @Override
    public CustomPacketPayload.Type<ElephantFootstepPacket> type() {
        return TYPE;
    }

    public static void handle(ElephantFootstepPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;

            Entity entity = minecraft.level.getEntity(packet.entityId());
            if (!(entity instanceof ElephantEntity elephant)) return;

            elephant.onFootstepFromServer(packet.right());
        });
    }
}
