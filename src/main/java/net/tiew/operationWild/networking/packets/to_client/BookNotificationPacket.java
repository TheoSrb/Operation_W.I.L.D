package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.screen.player.OWEntityJournalScreen;

public record BookNotificationPacket(String entityType, boolean isTaming) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BookNotificationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "book_notification"));

    public static final StreamCodec<FriendlyByteBuf, BookNotificationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BookNotificationPacket::entityType,
                    ByteBufCodecs.BOOL, BookNotificationPacket::isTaming,
                    BookNotificationPacket::new
            );

    @Override
    public CustomPacketPayload.Type<BookNotificationPacket> type() {
        return TYPE;
    }

    public static void handle(BookNotificationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEvents.isNotifiedOWBook = true;
            if (!packet.isTaming) {
                if (!OWEntityJournalScreen.newEntitiesDiscovered.contains(packet.entityType)) {
                    OWEntityJournalScreen.newEntitiesDiscovered.add(packet.entityType);
                }
            }
            if (!OWEntityJournalScreen.newEntitiesTamed.contains(packet.entityType)) {
                OWEntityJournalScreen.newEntitiesTamed.add(packet.entityType);
            }
        });
    }
}