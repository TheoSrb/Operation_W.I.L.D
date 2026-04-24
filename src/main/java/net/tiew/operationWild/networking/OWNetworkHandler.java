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
        // Populate the attack registry before any packets can be received
        net.tiew.operationWild.entity.attacks.OWAttacksHandler.registerAll();

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
        registrar.playToServer(ConsumeItemPacket.TYPE, ConsumeItemPacket.STREAM_CODEC, ConsumeItemPacket::handle);
        registrar.playToServer(OpenDailyQuestScreen.TYPE, OpenDailyQuestScreen.STREAM_CODEC, OpenDailyQuestScreen::handle);
        registrar.playToServer(SendUltimateCapacityPacket.TYPE, SendUltimateCapacityPacket.STREAM_CODEC, SendUltimateCapacityPacket::handle);
        registrar.playToServer(CreateNewFilePacket.TYPE, CreateNewFilePacket.STREAM_CODEC, CreateNewFilePacket::handle);
        registrar.playToServer(ElephantFootstepPacket.TYPE, ElephantFootstepPacket.STREAM_CODEC, ElephantFootstepPacket::handle);
        registrar.playToServer(CheckManuscriptEntityPacket.TYPE, CheckManuscriptEntityPacket.STREAM_CODEC, CheckManuscriptEntityPacket::handle);
        registrar.playToServer(StopGrabPacket.TYPE, StopGrabPacket.STREAM_CODEC, StopGrabPacket::handle);
        registrar.playToServer(OWEntityGrabManagerPacket.TYPE, OWEntityGrabManagerPacket.STREAM_CODEC, OWEntityGrabManagerPacket::handle);
        registrar.playToServer(OWNameEntityPacket.TYPE, OWNameEntityPacket.STREAM_CODEC, OWNameEntityPacket::handle);
        registrar.playToServer(OWAttackPacket.TYPE, OWAttackPacket.STREAM_CODEC, OWAttackPacket::handle);

        // To Client packets
        registrar.playToClient(OWEntityUtilsToClient.TYPE, OWEntityUtilsToClient.STREAM_CODEC, OWEntityUtilsToClient::handle);
        registrar.playToClient(OWQuestProgressToClient.TYPE, OWQuestProgressToClient.STREAM_CODEC, OWQuestProgressToClient::handle);
        registrar.playToClient(OWPacketSendToClient.TYPE, OWPacketSendToClient.STREAM_CODEC, OWPacketSendToClient::handle);
        registrar.playToClient(BookNotificationPacket.TYPE, BookNotificationPacket.STREAM_CODEC, BookNotificationPacket::handle);
        registrar.playToClient(SyncKillDataPacket.TYPE, SyncKillDataPacket.STREAM_CODEC, SyncKillDataPacket::handle);
        registrar.playToClient(OWFoodPacketClient.TYPE, OWFoodPacketClient.STREAM_CODEC, OWFoodPacketClient::handle);
        registrar.playToClient(AddEntityToManuscriptPacket.TYPE, AddEntityToManuscriptPacket.STREAM_CODEC, AddEntityToManuscriptPacket::handle);
        registrar.playToClient(OpenChooseNameScreen.TYPE, OpenChooseNameScreen.STREAM_CODEC, OpenChooseNameScreen::handle);
        registrar.playToClient(TigerLeapStatePacket.TYPE, TigerLeapStatePacket.STREAM_CODEC, TigerLeapStatePacket::handle);
        registrar.playToClient(SkinUnlockedPacket.TYPE, SkinUnlockedPacket.STREAM_CODEC, SkinUnlockedPacket::handle);

    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}