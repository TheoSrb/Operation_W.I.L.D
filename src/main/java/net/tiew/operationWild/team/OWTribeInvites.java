package net.tiew.operationWild.team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage serveur transitoire des invitations de tribu <b>player-centric</b> : un joueur cible est
 * invité à rejoindre une tribu (identifiée par son id dans {@link OWTribesSavedData}). Non persisté,
 * TTL {@link #TTL_MS}. Une seule invitation en attente par joueur (la nouvelle écrase l'ancienne).
 */
public final class OWTribeInvites {

    public static final long TTL_MS = 60_000L;

    public record Invite(int teamId, UUID inviterUUID, String inviterName, String teamName, long expiresAt) {}

    private static final Map<UUID, Invite> PENDING = new ConcurrentHashMap<>();

    private OWTribeInvites() {}

    public static void put(UUID target, int teamId, UUID inviterUUID, String inviterName, String teamName) {
        PENDING.put(target, new Invite(teamId, inviterUUID, inviterName, teamName,
                System.currentTimeMillis() + TTL_MS));
    }

    public static Invite get(UUID target) {
        Invite inv = PENDING.get(target);
        if (inv == null) return null;
        if (System.currentTimeMillis() > inv.expiresAt()) {
            PENDING.remove(target, inv);
            return null;
        }
        return inv;
    }

    public static Invite consume(UUID target) {
        Invite inv = get(target);
        if (inv != null) PENDING.remove(target, inv);
        return inv;
    }
}
