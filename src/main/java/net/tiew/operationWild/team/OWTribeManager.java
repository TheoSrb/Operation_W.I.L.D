package net.tiew.operationWild.team;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ClearOWTeamPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncPlayerTribePacket;
import net.tiew.operationWild.networking.packets.to_client.SyncTribeListPacket;

import java.util.UUID;

/**
 * Point d'entrée serveur pour toutes les mutations de tribu et la synchronisation client.
 *
 * <p>Modèle player-centric : l'appartenance est portée par le joueur (chef + membres UUID) dans
 * {@link OWTribesSavedData} ; les entités <b>suivent</b> leur propriétaire. Après toute mutation, on
 * recalcule {@link OWEntity#currentTeam} des entités chargées concernées et on pousse les paquets de
 * sync appropriés.</p>
 */
public final class OWTribeManager {

    private OWTribeManager() {}

    // ── Recalcul & sync des entités ────────────────────────────────────────────
    /** Recalcule {@code currentTeam} d'une entité depuis la tribu de son propriétaire (serveur). */
    public static void refreshEntityTeam(MinecraftServer server, OWEntity entity) {
        if (server == null || entity == null) return;
        OWTribesSavedData data = OWTribesSavedData.get(server);
        UUID owner = entity.getOwnerUUID();
        OWTeam team = owner != null ? data.findTeamByMember(owner) : null;
        entity.currentTeam = team;
        pushEntityTeam(entity, team);
    }

    /** Envoie l'état de tribu d'une entité aux joueurs de son niveau (sync ou clear). */
    private static void pushEntityTeam(OWEntity entity, OWTeam team) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (team != null) {
            SyncOWTeamPacket pkt = SyncOWTeamPacket.of(entity.getId(), team);
            for (ServerPlayer p : level.players()) OWNetworkHandler.sendToClient(pkt, p);
        } else {
            ClearOWTeamPacket pkt = new ClearOWTeamPacket(entity.getId());
            for (ServerPlayer p : level.players()) OWNetworkHandler.sendToClient(pkt, p);
        }
    }

    /** Recalcule et resynchronise toutes les entités chargées possédées par {@code playerUuid}. */
    public static void refreshEntitiesOfPlayer(MinecraftServer server, UUID playerUuid) {
        if (server == null || playerUuid == null) return;
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam team = data.findTeamByMember(playerUuid);
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof OWEntity owE)) continue;
                if (!playerUuid.equals(owE.getOwnerUUID())) continue;
                owE.currentTeam = team;
                pushEntityTeam(owE, team);
            }
        }
    }

    /** Resynchronise toutes les entités chargées dont le propriétaire est membre de {@code team}. */
    public static void refreshEntitiesOfTribe(MinecraftServer server, OWTeam team) {
        if (server == null || team == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof OWEntity owE)) continue;
                UUID owner = owE.getOwnerUUID();
                if (owner == null || !team.isMember(owner)) continue;
                owE.currentTeam = team;
                pushEntityTeam(owE, team);
            }
        }
    }

    // ── Sync des écrans joueur ─────────────────────────────────────────────────
    /** Pousse au joueur SA tribu (ou « aucune ») pour son écran de tribu. */
    public static void syncPlayerTribe(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null) return;
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        int reputation = team != null
                ? net.tiew.operationWild.core.OWReputation.compute(OWReputationData.get(server), team) : 0;
        OWNetworkHandler.sendToClient(SyncPlayerTribePacket.of(team, reputation), player);
    }

    /** Pousse au joueur la liste de toutes les tribus du serveur (pour l'écran liste). */
    public static void syncTribeList(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null) return;
        OWNetworkHandler.sendToClient(SyncTribeListPacket.of(server), player);
    }

    /** Diffuse la liste des tribus à tous les joueurs connectés (après création / dissolution / réglages). */
    public static void broadcastTribeList(MinecraftServer server) {
        if (server == null) return;
        SyncTribeListPacket pkt = SyncTribeListPacket.of(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) OWNetworkHandler.sendToClient(pkt, p);
    }

    /** Pousse SA tribu à chaque membre en ligne de {@code team} (liste des membres modifiée). */
    public static void syncTribeToOnlineMembers(MinecraftServer server, OWTeam team) {
        if (server == null || team == null) return;
        for (UUID member : team.getPlayerUUIDs()) {
            ServerPlayer p = server.getPlayerList().getPlayer(member);
            if (p != null) syncPlayerTribe(server, p);
        }
    }

    // ── Membre le plus ancien (hors chef) pour le transfert automatique ─────────
    public static UUID oldestMemberExcludingChief(OWTeam team) {
        if (team == null) return null;
        for (UUID u : team.getPlayerUUIDs()) {
            if (!u.equals(team.getTeamOwnerUUID())) return u;
        }
        return null;
    }
}
