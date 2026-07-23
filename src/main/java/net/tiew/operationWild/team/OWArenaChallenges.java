package net.tiew.operationWild.team;

import net.tiew.operationWild.core.OWArena;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Défis d'arène en attente, côté serveur. Transitoire (jamais persisté) et calqué sur
 * {@link OWTribeInvites} : une seule invitation en attente <b>par tribu cible</b>, la plus récente
 * écrase la précédente, TTL {@link #TTL_MS}.
 *
 * <p>Émission et réponse sont réservées aux chefs — la vérification est faite par
 * {@link OWArenaManager}, pas ici.</p>
 */
public final class OWArenaChallenges {

    public static final long TTL_MS = 120_000L;

    /**
     * Défi lancé par {@code challengerTeamId} à la tribu cible.
     *
     * <p>Le terrain est fixé par le défiant à l'émission et voyage avec le défi : le défié le voit
     * avant d'accepter, et il conditionne ensuite la composition des deux camps.</p>
     */
    public record Challenge(int challengerTeamId, String challengerTeamName,
                            UUID challengerChief, OWArena.Terrain terrain, long expiresAt) {}

    /** teamId cible → défi en attente. */
    private static final Map<Integer, Challenge> PENDING = new ConcurrentHashMap<>();

    private OWArenaChallenges() {}

    public static void put(int targetTeamId, int challengerTeamId, String challengerTeamName,
                           UUID challengerChief, OWArena.Terrain terrain) {
        PENDING.put(targetTeamId, new Challenge(challengerTeamId, challengerTeamName, challengerChief,
                terrain != null ? terrain : OWArena.Terrain.TERRESTRIAL,
                System.currentTimeMillis() + TTL_MS));
    }

    /** Défi en attente pour {@code targetTeamId}, ou {@code null} (expiré = purgé). */
    public static Challenge get(int targetTeamId) {
        Challenge c = PENDING.get(targetTeamId);
        if (c == null) return null;
        if (System.currentTimeMillis() > c.expiresAt()) {
            PENDING.remove(targetTeamId, c);
            return null;
        }
        return c;
    }

    /** Défi <b>émis</b> par {@code challengerTeamId}, ou {@code null}. Sert à afficher « en attente ». */
    public static Challenge findOutgoing(int challengerTeamId) {
        for (Map.Entry<Integer, Challenge> e : PENDING.entrySet()) {
            Challenge c = e.getValue();
            if (c.challengerTeamId() != challengerTeamId) continue;
            if (System.currentTimeMillis() > c.expiresAt()) { PENDING.remove(e.getKey(), c); continue; }
            return c;
        }
        return null;
    }

    /** Tribu cible du défi émis par {@code challengerTeamId}, ou 0. */
    public static int outgoingTarget(int challengerTeamId) {
        for (Map.Entry<Integer, Challenge> e : PENDING.entrySet()) {
            Challenge c = e.getValue();
            if (c.challengerTeamId() == challengerTeamId
                    && System.currentTimeMillis() <= c.expiresAt()) return e.getKey();
        }
        return 0;
    }

    public static Challenge consume(int targetTeamId) {
        Challenge c = get(targetTeamId);
        if (c != null) PENDING.remove(targetTeamId, c);
        return c;
    }

    /** Retire tout défi impliquant cette tribu (dissolution, entrée en combat…). */
    public static void clearFor(int teamId) {
        PENDING.remove(teamId);
        PENDING.entrySet().removeIf(e -> e.getValue().challengerTeamId() == teamId);
    }
}
