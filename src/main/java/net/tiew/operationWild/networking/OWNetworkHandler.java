package net.tiew.operationWild.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.Clear;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.networking.packets.to_client.*;
import net.tiew.operationWild.networking.packets.to_server.*;

public class OWNetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        // Populate the attack registry before any packets can be received
        if (FMLEnvironment.dist.isClient()) {
            OWAttacksHandler.registerAll();
        }

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
        registrar.playToServer(RerollDailyQuestPacket.TYPE, RerollDailyQuestPacket.STREAM_CODEC, RerollDailyQuestPacket::handle);
        registrar.playToServer(SendUltimateCapacityPacket.TYPE, SendUltimateCapacityPacket.STREAM_CODEC, SendUltimateCapacityPacket::handle);
        registrar.playToServer(CreateNewFilePacket.TYPE, CreateNewFilePacket.STREAM_CODEC, CreateNewFilePacket::handle);
        registrar.playToServer(ElephantFootstepPacket.TYPE, ElephantFootstepPacket.STREAM_CODEC, ElephantFootstepPacket::handle);
        registrar.playToServer(CheckManuscriptEntityPacket.TYPE, CheckManuscriptEntityPacket.STREAM_CODEC, CheckManuscriptEntityPacket::handle);
        registrar.playToServer(StopGrabPacket.TYPE, StopGrabPacket.STREAM_CODEC, StopGrabPacket::handle);
        registrar.playToServer(OWEntityGrabManagerPacket.TYPE, OWEntityGrabManagerPacket.STREAM_CODEC, OWEntityGrabManagerPacket::handle);
        registrar.playToServer(OWNameEntityPacket.TYPE, OWNameEntityPacket.STREAM_CODEC, OWNameEntityPacket::handle);
        registrar.playToServer(OWEntityTogglePacket.TYPE, OWEntityTogglePacket.STREAM_CODEC, OWEntityTogglePacket::handle);
        registrar.playToServer(OWAttackPacket.TYPE, OWAttackPacket.STREAM_CODEC, OWAttackPacket::handle);
        registrar.playToServer(CreateOWTeamPacket.TYPE, CreateOWTeamPacket.STREAM_CODEC, CreateOWTeamPacket::handle);
        registrar.playToServer(RemoveEntityFromTeamPacket.TYPE, RemoveEntityFromTeamPacket.STREAM_CODEC, RemoveEntityFromTeamPacket::handle);
        registrar.playToServer(RemovePlayerFromTeamPacket.TYPE, RemovePlayerFromTeamPacket.STREAM_CODEC, RemovePlayerFromTeamPacket::handle);
        registrar.playToServer(InvitePlayerToTeamPacket.TYPE, InvitePlayerToTeamPacket.STREAM_CODEC, InvitePlayerToTeamPacket::handle);
        registrar.playToServer(AddEntityToTeamPacket.TYPE, AddEntityToTeamPacket.STREAM_CODEC, AddEntityToTeamPacket::handle);
        registrar.playToServer(DeleteOWTeamPacket.TYPE, DeleteOWTeamPacket.STREAM_CODEC, DeleteOWTeamPacket::handle);
        registrar.playToServer(CreateOWTeamWithParamsPacket.TYPE, CreateOWTeamWithParamsPacket.STREAM_CODEC, CreateOWTeamWithParamsPacket::handle);
        registrar.playToServer(StartRitualPacket.TYPE, StartRitualPacket.STREAM_CODEC, StartRitualPacket::handle);
        registrar.playToServer(OWPisteAdvancePacket.TYPE, OWPisteAdvancePacket.STREAM_CODEC, OWPisteAdvancePacket::handle);

        // Tribu player-centric (refonte)
        registrar.playToServer(OpenTribeMenuPacket.TYPE, OpenTribeMenuPacket.STREAM_CODEC, OpenTribeMenuPacket::handle);
        registrar.playToServer(CreateTribePacket.TYPE, CreateTribePacket.STREAM_CODEC, CreateTribePacket::handle);
        registrar.playToServer(JoinTribePacket.TYPE, JoinTribePacket.STREAM_CODEC, JoinTribePacket::handle);
        registrar.playToServer(LeaveTribePacket.TYPE, LeaveTribePacket.STREAM_CODEC, LeaveTribePacket::handle);
        registrar.playToServer(DisbandTribePacket.TYPE, DisbandTribePacket.STREAM_CODEC, DisbandTribePacket::handle);
        registrar.playToServer(TransferChiefPacket.TYPE, TransferChiefPacket.STREAM_CODEC, TransferChiefPacket::handle);
        registrar.playToServer(UpdateTribeSettingsPacket.TYPE, UpdateTribeSettingsPacket.STREAM_CODEC, UpdateTribeSettingsPacket::handle);
        registrar.playToServer(UpdateTribeBannerPacket.TYPE, UpdateTribeBannerPacket.STREAM_CODEC, UpdateTribeBannerPacket::handle);
        registrar.playToServer(KickMemberPacket.TYPE, KickMemberPacket.STREAM_CODEC, KickMemberPacket::handle);
        registrar.playToServer(InvitePlayerToTribePacket.TYPE, InvitePlayerToTribePacket.STREAM_CODEC, InvitePlayerToTribePacket::handle);
        registrar.playToServer(SetDeputyPacket.TYPE, SetDeputyPacket.STREAM_CODEC, SetDeputyPacket::handle);
        registrar.playToServer(SetMemberPermissionPacket.TYPE, SetMemberPermissionPacket.STREAM_CODEC, SetMemberPermissionPacket::handle);
        registrar.playToServer(BuyBannerShapePacket.TYPE, BuyBannerShapePacket.STREAM_CODEC, BuyBannerShapePacket::handle);
        registrar.playToServer(EnableTribeReputationPacket.TYPE, EnableTribeReputationPacket.STREAM_CODEC, EnableTribeReputationPacket::handle);

        // To Client packets
        registrar.playToClient(OWEntityUtilsToClient.TYPE, OWEntityUtilsToClient.STREAM_CODEC, OWEntityUtilsToClient::handle);
        registrar.playToClient(OWQuestProgressToClient.TYPE, OWQuestProgressToClient.STREAM_CODEC, OWQuestProgressToClient::handle);
        registrar.playToClient(OWPacketSendToClient.TYPE, OWPacketSendToClient.STREAM_CODEC, OWPacketSendToClient::handle);
        registrar.playToClient(BookNotificationPacket.TYPE, BookNotificationPacket.STREAM_CODEC, BookNotificationPacket::handle);
        registrar.playToClient(SyncKillDataPacket.TYPE, SyncKillDataPacket.STREAM_CODEC, SyncKillDataPacket::handle);
        registrar.playToClient(OWFoodPacketClient.TYPE, OWFoodPacketClient.STREAM_CODEC, OWFoodPacketClient::handle);
        registrar.playToClient(AddEntityToManuscriptPacket.TYPE, AddEntityToManuscriptPacket.STREAM_CODEC, AddEntityToManuscriptPacket::handle);
        registrar.playToClient(OpenChooseNameScreen.TYPE, OpenChooseNameScreen.STREAM_CODEC, (packet, context) -> OpenChooseNameScreenHandler.handle(packet, context));
        registrar.playToClient(TigerLeapStatePacket.TYPE, TigerLeapStatePacket.STREAM_CODEC, TigerLeapStatePacket::handle);
        registrar.playToClient(HeartShotPacket.TYPE, HeartShotPacket.STREAM_CODEC, HeartShotPacket::handle);
        registrar.playToClient(SyncOWTeamPacket.TYPE, SyncOWTeamPacket.STREAM_CODEC, SyncOWTeamPacket::handle);
        registrar.playToClient(ClearOWTeamPacket.TYPE, ClearOWTeamPacket.STREAM_CODEC, ClearOWTeamPacket::handle);
        registrar.playToClient(OWEntityAlreadyInTeamPacket.TYPE, OWEntityAlreadyInTeamPacket.STREAM_CODEC, OWEntityAlreadyInTeamPacket::handle);
        registrar.playToClient(MessageHurtMultipart.TYPE, MessageHurtMultipart.STREAM_CODEC, MessageHurtMultipart::handle);
        registrar.playToClient(RitualSyncPacket.TYPE, RitualSyncPacket.STREAM_CODEC, RitualSyncPacket::handle);
        registrar.playToClient(OWLevelUpPacket.TYPE, OWLevelUpPacket.STREAM_CODEC, OWLevelUpPacket::handle);
        registrar.playToClient(OWCoinsSyncPacket.TYPE, OWCoinsSyncPacket.STREAM_CODEC, OWCoinsSyncPacket::handle);
        registrar.playToClient(OWTamingXpSyncPacket.TYPE, OWTamingXpSyncPacket.STREAM_CODEC, OWTamingXpSyncPacket::handle);
        registrar.playToClient(OWXpGainPacket.TYPE, OWXpGainPacket.STREAM_CODEC, OWXpGainPacket::handle);
        registrar.playToClient(OWIndicationPacket.TYPE, OWIndicationPacket.STREAM_CODEC, OWIndicationPacket::handle);

        // Tribu player-centric (refonte)
        registrar.playToClient(SyncPlayerTribePacket.TYPE, SyncPlayerTribePacket.STREAM_CODEC, SyncPlayerTribePacket::handle);
        registrar.playToClient(SyncTribeListPacket.TYPE, SyncTribeListPacket.STREAM_CODEC, SyncTribeListPacket::handle);
        registrar.playToClient(SyncBannerUnlocksPacket.TYPE, SyncBannerUnlocksPacket.STREAM_CODEC, SyncBannerUnlocksPacket::handle);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAllClients(CustomPacketPayload packet, net.minecraft.server.level.ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}