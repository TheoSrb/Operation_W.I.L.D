package net.tiew.operationWild.waypoint;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.SyncWaypointsPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Acheminement des waypoints du monde vers chaque joueur.
 *
 * <p>Chaque joueur ne reçoit que ses propres entrées : la position des compagnons d'autrui ne
 * quitte jamais le serveur. L'envoi est piloté par le compteur de version du registre, si bien
 * qu'une écurie immobile — cas de très loin le plus courant — ne coûte aucun paquet.</p>
 */
public final class OWWaypointManager {

    private OWWaypointManager() {}

    /** playerUUID → dernière version qui lui a été envoyée. */
    private static final Map<UUID, Integer> lastSentRevision = new HashMap<>();

    /** Envoi complet, sans condition (connexion, changement de dimension, bascule d'un repère). */
    public static void syncTo(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null) return;
        OWWaypointData data = OWWaypointData.get(server);
        OWNetworkHandler.sendToClient(new SyncWaypointsPacket(data.entriesFor(player.getUUID())), player);
        lastSentRevision.put(player.getUUID(), data.revisionOf(player.getUUID()));
    }

    /** Envoi aux seuls joueurs dont les repères ont bougé depuis leur dernier instantané. */
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        OWWaypointData data = OWWaypointData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int revision = data.revisionOf(player.getUUID());
            Integer sent = lastSentRevision.get(player.getUUID());
            if (sent == null || sent != revision) syncTo(server, player);
        }
    }

    public static void forget(UUID playerUuid) {
        if (playerUuid != null) lastSentRevision.remove(playerUuid);
    }

    /**
     * Oublie tous les instantanés envoyés.
     *
     * <p>Indispensable à l'arrêt du serveur : en solo, la JVM survit au changement de monde, et une
     * version retenue de la partie précédente ferait sauter la première synchronisation de la
     * suivante — le joueur repartirait avec des repères vides jusqu'au premier mouvement.</p>
     */
    public static void clear() {
        lastSentRevision.clear();
    }
}
