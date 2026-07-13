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
import net.tiew.operationWild.networking.packets.to_client.OWEntityAlreadyInTeamPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            // Find target by nickname (initial lookup from client input)
            OWEntity target = null;
            for (Entity e : serverLevel.getAllEntities()) {
                if (!(e instanceof OWEntity owE)) continue;
                if (!owE.getNickname().equals(nickname)) continue;
                if (owE.getOwnerUUID() == null) continue;
                if (!owE.getOwnerUUID().equals(owEntity.currentTeam.getTeamOwnerUUID())) continue;
                target = owE;
                break;
            }
            if (target == null) return;

            OWTeam team = owEntity.currentTeam;

            // ── Already in this team? Check by UUID ────────────────────────────
            if (team.getEntityUUIDs().contains(target.getUUID())) return;

            // ── Vérification : l'entité est-elle dans une autre tribu ? ────────
            if (!packet.force()
                    && target.currentTeam != null
                    && target.currentTeam.getTeamId() != team.getTeamId()) {
                if (context.player() instanceof ServerPlayer sp) {
                    OWNetworkHandler.sendToClient(
                            new OWEntityAlreadyInTeamPacket(nickname, target.currentTeam.getTeamName()),
                            sp);
                }
                return;
            }

            OWTeam oldTeam = target.currentTeam;
            boolean wasInAnotherTeam = oldTeam != null && oldTeam.getTeamId() != team.getTeamId();

            // ── Retrait propre de l'ancienne équipe (par UUID) ─────────────────
            if (wasInAnotherTeam) {
                int oldIdx = oldTeam.getEntityUUIDs().indexOf(target.getUUID());
                if (oldIdx >= 0) {
                    oldTeam.getEntityUUIDs().remove(oldIdx);
                    if (oldIdx < oldTeam.getEntityNames().size())
                        oldTeam.getEntityNames().remove(oldIdx);
                }
            }

            team.getEntityUUIDs().add(target.getUUID());
            team.getEntityNames().add(target.getNickname());
            target.currentTeam = team;

            // ── Membres de la NOUVELLE équipe (par UUID) ───────────────────────
            List<OWEntity> newTeamMembers = new ArrayList<>();
            for (UUID memberUUID : new ArrayList<>(team.getEntityUUIDs())) {
                for (Entity candidate : serverLevel.getAllEntities()) {
                    if (candidate instanceof OWEntity m && m.getUUID().equals(memberUUID)) {
                        m.currentTeam = team;
                        newTeamMembers.add(m);
                        break;
                    }
                }
            }

            // ── Membres restants de l'ANCIENNE équipe (par UUID) ───────────────
            List<OWEntity> oldTeamMembers = new ArrayList<>();
            if (wasInAnotherTeam) {
                for (UUID memberUUID : new ArrayList<>(oldTeam.getEntityUUIDs())) {
                    for (Entity candidate : serverLevel.getAllEntities()) {
                        if (candidate instanceof OWEntity m && m.getUUID().equals(memberUUID)) {
                            oldTeamMembers.add(m);
                            break;
                        }
                    }
                }
            }

            // ── Refresh display names from current nicknames ───────────────────
            refreshEntityNames(team, serverLevel);
            if (wasInAnotherTeam) refreshEntityNames(oldTeam, serverLevel);

            final OWTeam finalOldTeam = wasInAnotherTeam ? oldTeam : null;

            for (ServerPlayer player : serverLevel.players()) {
                for (OWEntity member : newTeamMembers) {
                    OWNetworkHandler.sendToClient(SyncOWTeamPacket.of(member.getId(), team), player);
                }

                if (wasInAnotherTeam) {
                    for (OWEntity member : oldTeamMembers) {
                        OWNetworkHandler.sendToClient(SyncOWTeamPacket.of(member.getId(), finalOldTeam), player);
                    }
                }
            }
        });
    }

    private static void refreshEntityNames(OWTeam team, ServerLevel level) {
        List<String> names = new ArrayList<>();
        for (UUID uuid : team.getEntityUUIDs()) {
            String name = uuid.toString(); // fallback
            for (Entity e : level.getAllEntities()) {
                if (e instanceof OWEntity m && m.getUUID().equals(uuid)) {
                    name = m.getNickname();
                    break;
                }
            }
            names.add(name);
        }
        team.setEntityNames(names);
    }
}