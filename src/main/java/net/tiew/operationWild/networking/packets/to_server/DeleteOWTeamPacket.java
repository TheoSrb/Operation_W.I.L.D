package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ClearOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;

public record DeleteOWTeamPacket(int entityId) implements CustomPacketPayload {

    public static final Type<DeleteOWTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "delete_ow_team"));

    public static final StreamCodec<ByteBuf, DeleteOWTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, DeleteOWTeamPacket::entityId,
                    DeleteOWTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DeleteOWTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;

            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity)) return;
            if (owEntity.currentTeam == null) return;
            if (!context.player().getUUID().equals(owEntity.currentTeam.getTeamOwnerUUID())) return;

            OWTeam team = owEntity.currentTeam;

            // ── Collecter TOUS les membres de l'équipe AVANT de les dissoudre ────
            List<OWEntity> allMembers = new ArrayList<>();
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof OWEntity m
                        && m.currentTeam != null
                        && m.currentTeam.getTeamId() == team.getTeamId()) {
                    allMembers.add(m);
                }
            }

            // ── Effacer la référence d'équipe sur tous les membres côté serveur ──
            for (OWEntity member : allMembers) {
                member.currentTeam = null;
            }

            // ── Envoyer ClearOWTeamPacket pour chaque membre à tous les joueurs ──
            for (ServerPlayer player : serverLevel.players()) {
                for (OWEntity member : allMembers) {
                    OWNetworkHandler.sendToClient(new ClearOWTeamPacket(member.getId()), player);
                }
            }
        });
    }
}