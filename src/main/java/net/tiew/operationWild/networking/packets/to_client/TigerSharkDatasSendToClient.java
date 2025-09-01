package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;

public record TigerSharkDatasSendToClient(
        int entityId,
        int livingEntityId,
        boolean smellingBlood,
        int foodGiven,
        int foodWanted,
        boolean isSmellingBlood
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TigerSharkDatasSendToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_shark_datas_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, TigerSharkDatasSendToClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, TigerSharkDatasSendToClient::entityId,
                    ByteBufCodecs.INT, TigerSharkDatasSendToClient::livingEntityId,
                    ByteBufCodecs.BOOL, TigerSharkDatasSendToClient::smellingBlood,
                    ByteBufCodecs.INT, TigerSharkDatasSendToClient::foodGiven,
                    ByteBufCodecs.INT, TigerSharkDatasSendToClient::foodWanted,
                    ByteBufCodecs.BOOL, TigerSharkDatasSendToClient::isSmellingBlood,
                    TigerSharkDatasSendToClient::new
            );

    @Override
    public CustomPacketPayload.Type<TigerSharkDatasSendToClient> type() {
        return TYPE;
    }

    public static void handle(TigerSharkDatasSendToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof TigerSharkEntity tigerShark) {
                if (packet.livingEntityId() != -1) {
                    Entity livingEntity = Minecraft.getInstance().level.getEntity(packet.livingEntityId());
                    if (livingEntity instanceof LivingEntity living) {
                        tigerShark.setTarget(living);
                    }
                } else {
                    tigerShark.setTarget(null);
                }
                tigerShark.isSmellingBlood = packet.smellingBlood();
                tigerShark.foodGiven = packet.foodGiven();
                tigerShark.foodWanted = packet.foodWanted();
                tigerShark.isSmellingBlood = packet.isSmellingBlood();
            }
        });
    }
}