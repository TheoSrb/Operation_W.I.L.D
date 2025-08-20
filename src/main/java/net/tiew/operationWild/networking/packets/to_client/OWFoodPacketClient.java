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
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.BoaEntity;

public record OWFoodPacketClient(int entityId, int foodGiven, int foodWanted) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWFoodPacketClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_food_packet"));

    public static final StreamCodec<FriendlyByteBuf, OWFoodPacketClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OWFoodPacketClient::entityId,
                    ByteBufCodecs.INT, OWFoodPacketClient::foodGiven,
                    ByteBufCodecs.INT, OWFoodPacketClient::foodWanted,
                    OWFoodPacketClient::new
            );

    @Override
    public CustomPacketPayload.Type<OWFoodPacketClient> type() {
        return TYPE;
    }

    public static void handle(OWFoodPacketClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof OWEntity owEntity) {
                owEntity.foodGiven = packet.foodGiven;
                owEntity.foodWanted = packet.foodWanted;
            }
        });
    }
}
