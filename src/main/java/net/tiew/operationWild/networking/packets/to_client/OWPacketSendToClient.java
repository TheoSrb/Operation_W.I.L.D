package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWPacketSendToClient(
        int entityId,
        float actualMaturation,
        float maxMaturation,
        int delayBeforeBabyTask,
        String choosenQuestStr,
        boolean babyQuestIsInProgress,
        int babyQuestProgressTimer,
        Item chooseFood,
        boolean canShowVitalEnergyLack
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWPacketSendToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_packet_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, OWPacketSendToClient> STREAM_CODEC =
            StreamCodec.of(OWPacketSendToClient::encode, OWPacketSendToClient::decode);

    private static void encode(FriendlyByteBuf buffer, OWPacketSendToClient packet) {
        buffer.writeInt(packet.entityId());
        buffer.writeFloat(packet.actualMaturation());
        buffer.writeFloat(packet.maxMaturation());
        buffer.writeInt(packet.delayBeforeBabyTask());
        buffer.writeUtf(packet.choosenQuestStr() != null ? packet.choosenQuestStr() : "");
        buffer.writeBoolean(packet.babyQuestIsInProgress());
        buffer.writeInt(packet.babyQuestProgressTimer());

        if (packet.chooseFood() != null) {
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(packet.chooseFood()));
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeBoolean(packet.canShowVitalEnergyLack());
    }

    private static OWPacketSendToClient decode(FriendlyByteBuf buffer) {
        return new OWPacketSendToClient(
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readBoolean() ? BuiltInRegistries.ITEM.get(buffer.readResourceLocation()) : null,
                buffer.readBoolean()
        );
    }

    @Override
    public CustomPacketPayload.Type<OWPacketSendToClient> type() {
        return TYPE;
    }

    public static void handle(OWPacketSendToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof OWEntity owEntity) {
                owEntity.actualMaturation = packet.actualMaturation();
                owEntity.maxMaturation = packet.maxMaturation();
                owEntity.delayBeforeBabyTask = packet.delayBeforeBabyTask();
                owEntity.choosenQuestStr = packet.choosenQuestStr();
                owEntity.babyQuestIsInProgress = packet.babyQuestIsInProgress();
                owEntity.babyQuestProgressTimer = packet.babyQuestProgressTimer();
                owEntity.choosenFood = packet.chooseFood();
                owEntity.canShowVitalEnergyLack = packet.canShowVitalEnergyLack();
            }
        });
    }
}