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
import net.tiew.operationWild.entity.custom.living.PeacockEntity;

public record PeacockFoodsSendToClient(int entityId, int foodGiven, int foodWanted) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PeacockFoodsSendToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "peacock_foods_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, PeacockFoodsSendToClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, PeacockFoodsSendToClient::entityId,
                    ByteBufCodecs.INT, PeacockFoodsSendToClient::foodGiven,
                    ByteBufCodecs.INT, PeacockFoodsSendToClient::foodWanted,
                    PeacockFoodsSendToClient::new
            );

    @Override
    public CustomPacketPayload.Type<PeacockFoodsSendToClient> type() {
        return TYPE;
    }

    public static void handle(PeacockFoodsSendToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof PeacockEntity peacock) {
                peacock.foodGiven = packet.foodGiven;
                peacock.foodWanted = packet.foodWanted;
            }
        });
    }
}