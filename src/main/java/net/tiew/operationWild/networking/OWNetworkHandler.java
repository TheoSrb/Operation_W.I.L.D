package net.tiew.operationWild.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.packets.to_client.*;
import net.tiew.operationWild.networking.packets.to_server.*;

public class OWNetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OperationWild.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        // To Server packets
        registrar.playToServer(OpenOWInventoryPacket.TYPE, OpenOWInventoryPacket.STREAM_CODEC, OpenOWInventoryPacket::handle);
        registrar.playToServer(ClientPressedLeftClick.TYPE, ClientPressedLeftClick.STREAM_CODEC, ClientPressedLeftClick::handle);
        registrar.playToServer(ClientPressedRightClick.TYPE, ClientPressedRightClick.STREAM_CODEC, ClientPressedRightClick::handle);
        registrar.playToServer(LevelUpOWInventoryPacket.TYPE, LevelUpOWInventoryPacket.STREAM_CODEC, LevelUpOWInventoryPacket::handle);
        registrar.playToServer(OWRunningPacket.TYPE, OWRunningPacket.STREAM_CODEC, OWRunningPacket::handle);
        registrar.playToServer(OWVariantsSkinsPacket.TYPE, OWVariantsSkinsPacket.STREAM_CODEC, OWVariantsSkinsPacket::handle);
        registrar.playToServer(SkinBuyingPacket.TYPE, SkinBuyingPacket.STREAM_CODEC, SkinBuyingPacket::handle);
        registrar.playToServer(TigerChargePacket.TYPE, TigerChargePacket.STREAM_CODEC, TigerChargePacket::handle);
        registrar.playToServer(ConsumeItemPacket.TYPE, ConsumeItemPacket.STREAM_CODEC, ConsumeItemPacket::handle);
        registrar.playToServer(BoaVenomPacket.TYPE, BoaVenomPacket.STREAM_CODEC, BoaVenomPacket::handle);
        registrar.playToServer(OpenDailyQuestScreen.TYPE, OpenDailyQuestScreen.STREAM_CODEC, OpenDailyQuestScreen::handle);
        registrar.playToServer(SendUltimateCapacityPacket.TYPE, SendUltimateCapacityPacket.STREAM_CODEC, SendUltimateCapacityPacket::handle);
        registrar.playToServer(CreateNewFilePacket.TYPE, CreateNewFilePacket.STREAM_CODEC, CreateNewFilePacket::handle);
        registrar.playToServer(StopNipsBoaPacket.TYPE, StopNipsBoaPacket.STREAM_CODEC, StopNipsBoaPacket::handle);
        registrar.playToServer(StopShakingSharkPacket.TYPE, StopShakingSharkPacket.STREAM_CODEC, StopShakingSharkPacket::handle);
        registrar.playToServer(ElephantFootstepPacket.TYPE, ElephantFootstepPacket.STREAM_CODEC, ElephantFootstepPacket::handle);

        // To Client packets
        registrar.playToClient(OWEntityUtilsToClient.TYPE, OWEntityUtilsToClient.STREAM_CODEC, OWEntityUtilsToClient::handle);
        registrar.playToClient(OWQuestProgressToClient.TYPE, OWQuestProgressToClient.STREAM_CODEC, OWQuestProgressToClient::handle);
        registrar.playToClient(BoaFoodsSendToClient.TYPE, BoaFoodsSendToClient.STREAM_CODEC, BoaFoodsSendToClient::handle);
        registrar.playToClient(PeacockFoodsSendToClient.TYPE, PeacockFoodsSendToClient.STREAM_CODEC, PeacockFoodsSendToClient::handle);
        registrar.playToClient(OWPacketSendToClient.TYPE, OWPacketSendToClient.STREAM_CODEC, OWPacketSendToClient::handle);
        registrar.playToClient(TigerUtilsSendToClientPacket.TYPE, TigerUtilsSendToClientPacket.STREAM_CODEC, TigerUtilsSendToClientPacket::handle);
        registrar.playToClient(BookNotificationPacket.TYPE, BookNotificationPacket.STREAM_CODEC, BookNotificationPacket::handle);
        registrar.playToClient(TigerSharkDatasSendToClient.TYPE, TigerSharkDatasSendToClient.STREAM_CODEC, TigerSharkDatasSendToClient::handle);
        registrar.playToClient(SyncKillDataPacket.TYPE, SyncKillDataPacket.STREAM_CODEC, SyncKillDataPacket::handle);
        registrar.playToClient(ChameleonUtilsSendToClient.TYPE, ChameleonUtilsSendToClient.STREAM_CODEC, ChameleonUtilsSendToClient::handle);
        registrar.playToClient(OWFoodPacketClient.TYPE, OWFoodPacketClient.STREAM_CODEC, OWFoodPacketClient::handle);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}