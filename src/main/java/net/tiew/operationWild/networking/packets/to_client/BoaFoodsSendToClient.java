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
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;

public record BoaFoodsSendToClient(int entityId, int foodGiven, int foodWanted, int numberOfErrors, int venomCooldown, boolean canVenom) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BoaFoodsSendToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "boa_foods_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, BoaFoodsSendToClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, BoaFoodsSendToClient::entityId,
                    ByteBufCodecs.INT, BoaFoodsSendToClient::foodGiven,
                    ByteBufCodecs.INT, BoaFoodsSendToClient::foodWanted,
                    ByteBufCodecs.INT, BoaFoodsSendToClient::numberOfErrors,
                    ByteBufCodecs.INT, BoaFoodsSendToClient::venomCooldown,
                    ByteBufCodecs.BOOL, BoaFoodsSendToClient::canVenom,
                    BoaFoodsSendToClient::new
            );

    @Override
    public CustomPacketPayload.Type<BoaFoodsSendToClient> type() {
        return TYPE;
    }

    public static void handle(BoaFoodsSendToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof BoaEntity boa) {
                boa.foodGiven = packet.foodGiven;
                boa.foodWanted = packet.foodWanted;
                boa.numberOfError = packet.numberOfErrors;
                boa.venomCooldown = packet.venomCooldown;
                boa.canVenom = packet.canVenom;
            }
        });
    }
}
