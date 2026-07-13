package net.tiew.operationWild.team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage serveur (transitoire) des invitations de tribu en attente.
 *
 * <p>Une invitation lie un joueur cible (par UUID) à une {@link OWTeam} (référence directe, même
 * JVM côté serveur) et à l'invitant. Chaque joueur ne peut avoir qu'une invitation en attente à la
 * fois ; une nouvelle invitation écrase la précédente. Les invitations expirent automatiquement
 * après {@link #TTL_MS}. Rien n'est persisté : une invitation non acceptée disparaît au
 * redémarrage / reconnexion, ce qui est le comportement attendu.</p>
 */
public final class OWTeamInvites {

    /** Durée de validité d'une invitation. */
    public static final long TTL_MS = 60_000L;

    public record Invite(OWTeam team, int teamEntityId, UUID inviterUUID,
                         String inviterName, String teamName, long expiresAt) {}

    private static final Map<UUID, Invite> PENDING = new ConcurrentHashMap<>();

    private OWTeamInvites() {}

    public static void put(UUID target, OWTeam team, int teamEntityId,
                           UUID inviterUUID, String inviterName) {
        PENDING.put(target, new Invite(team, teamEntityId, inviterUUID, inviterName,
                team.getTeamName(), System.currentTimeMillis() + TTL_MS));
    }

    /** Renvoie l'invitation valide en attente pour ce joueur, ou {@code null} (expirée / absente). */
    public static Invite get(UUID target) {
        Invite inv = PENDING.get(target);
        if (inv == null) return null;
        if (System.currentTimeMillis() > inv.expiresAt()) {
            PENDING.remove(target, inv);
            return null;
        }
        return inv;
    }

    public static boolean has(UUID target) {
        return get(target) != null;
    }

    public static Invite consume(UUID target) {
        Invite inv = get(target);
        if (inv != null) PENDING.remove(target, inv);
        return inv;
    }

    public static void clear(UUID target) {
        PENDING.remove(target);
    }
}
