package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

import java.util.UUID;

public record OpenDailyQuestScreen() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenDailyQuestScreen> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "open_daily_quest"));

    public static final StreamCodec<FriendlyByteBuf, OpenDailyQuestScreen> STREAM_CODEC =
            StreamCodec.unit(new OpenDailyQuestScreen());

    @Override
    public CustomPacketPayload.Type<OpenDailyQuestScreen> type() {
        return TYPE;
    }

    public static void handle(OpenDailyQuestScreen packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Quêtes propres à chaque individu : consulter l'écran n'efface la notification QUE de
                // l'entité chevauchée (plus de propagation à tous les compagnons du propriétaire).
                if (player.getRootVehicle() instanceof OWEntity owEntity) {
                    owEntity.setUpdatingQuests(false);
                }
            }
        });
    }
}

