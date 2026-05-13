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
import net.tiew.operationWild.networking.packets.to_client.OWEntityAlreadyInTeamPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;

/**
 * Paramètre {@code force} :
 * - false  → le serveur vérifie si l'entité cible est déjà dans une autre tribu ;
 *            si oui, il renvoie {@link OWEntityAlreadyInTeamPacket} au client
 *            pour déclencher la boîte de confirmation.
 * - true   → le joueur a confirmé : l'entité est transférée sans vérification supplémentaire.
 */
public record AddEntityToTeamPacket(int teamEntityId, String targetNickname, boolean force)
        implements CustomPacketPayload {

    public static final Type<AddEntityToTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "add_entity_to_team"));

    public static final StreamCodec<ByteBuf, AddEntityToTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,         AddEntityToTeamPacket::teamEntityId,
                    ByteBufCodecs.STRING_UTF8, AddEntityToTeamPacket::targetNickname,
                    ByteBufCodecs.BOOL,        AddEntityToTeamPacket::force,
                    AddEntityToTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(AddEntityToTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;

            Entity teamEntity = serverLevel.getEntity(packet.teamEntityId());
            if (!(teamEntity instanceof OWEntity owEntity)) return;
            if (owEntity.currentTeam == null) return;
            if (!context.player().getUUID().equals(owEntity.currentTeam.getTeamOwnerUUID())) return;

            String nickname = packet.targetNickname().trim();
            if (nickname.isEmpty()) return;
            if (owEntity.currentTeam.getEntityNames().contains(nickname)) return;

            for (Entity e : serverLevel.getAllEntities()) {
                if (!(e instanceof OWEntity target)) continue;
                if (!target.getNickname().equals(nickname)) continue;
                if (target.getOwnerUUID() == null) return;
                if (!target.getOwnerUUID().equals(owEntity.currentTeam.getTeamOwnerUUID())) return;

                OWTeam team = owEntity.currentTeam;

                // ── Vérification côté serveur : l'entité est-elle dans une autre tribu ? ──
                if (!packet.force()
                        && target.currentTeam != null
                        && target.currentTeam.getTeamId() != team.getTeamId()) {
                    // Informer le client pour qu'il affiche la confirmation
                    if (context.player() instanceof ServerPlayer sp) {
                        OWNetworkHandler.sendToClient(
                                new OWEntityAlreadyInTeamPacket(nickname, target.currentTeam.getTeamName()),
                                sp);
                    }
                    return;
                }

                // ── Retrait propre de l'ancienne équipe si nécessaire ─────────────────
                if (target.currentTeam != null && target.currentTeam.getTeamId() != team.getTeamId()) {
                    target.currentTeam.getEntityNames().remove(target.getNickname());
                }

                team.getEntityNames().add(nickname);
                target.currentTeam = team;

                // ── Collecter TOUS les membres de la nouvelle équipe ──────────────────
                List<OWEntity> allTeamEntities = new ArrayList<>();
                for (Entity candidate : serverLevel.getAllEntities()) {
                    if (candidate instanceof OWEntity m
                            && m.currentTeam != null
                            && m.currentTeam.getTeamId() == team.getTeamId()) {
                        allTeamEntities.add(m);
                    }
                }

                // ── Synchroniser chaque membre auprès de tous les joueurs ─────────────
                for (ServerPlayer player : serverLevel.players()) {
                    for (OWEntity member : allTeamEntities) {
                        OWNetworkHandler.sendToClient(new SyncOWTeamPacket(
                                member.getId(),
                                team.getTeamId(),
                                team.getTeamName(),
                                team.getTeamOwnerUUID().toString(),
                                team.getTeamColor(),
                                team.getTeamSecondaryColor(),
                                team.getTeamMosaicPattern().getId(),
                                team.getTeamCreationDate(),
                                team.getPlayerNames(),
                                team.getEntityNames()
                        ), player);
                    }
                }
                return;
            }
        });
    }
}