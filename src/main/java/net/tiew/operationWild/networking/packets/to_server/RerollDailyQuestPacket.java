package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

/**
 * Demande serveur de reroll d'UNE quête quotidienne (emplacement 0..2) de l'entité chevauchée.
 * Le serveur vérifie que le reroll du jour est encore disponible.
 */
public record RerollDailyQuestPacket(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RerollDailyQuestPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "reroll_daily_quest"));

    public static final StreamCodec<FriendlyByteBuf, RerollDailyQuestPacket> STREAM_CODEC =
            StreamCodec.of((buf, pkt) -> buf.writeInt(pkt.slot()), buf -> new RerollDailyQuestPacket(buf.readInt()));

    @Override
    public CustomPacketPayload.Type<RerollDailyQuestPacket> type() {
        return TYPE;
    }

    public static void handle(RerollDailyQuestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getRootVehicle() instanceof OWEntity owEntity) {
                owEntity.rerollSingleQuest(packet.slot());
            }
        });
    }
}
