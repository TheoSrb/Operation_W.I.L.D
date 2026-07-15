package net.tiew.operationWild.client;

import net.tiew.operationWild.team.OWTeam;

/**
 * Miroir client de la tribu du joueur local (chef OU membre), alimenté par
 * {@code SyncPlayerTribePacket}. {@code null} = le joueur n'est dans aucune tribu.
 */
public final class OWClientTribeData {

    private static OWTeam currentTribe = null;
    /** Passe à true dès qu'un premier {@code SyncPlayerTribePacket} a été reçu (fin du « chargement »). */
    private static boolean received = false;

    private OWClientTribeData() {}

    public static OWTeam get() {
        return currentTribe;
    }

    public static void set(OWTeam team) {
        currentTribe = team;
        received = true;
    }

    public static boolean isReceived() {
        return received;
    }

    public static boolean hasTribe() {
        return currentTribe != null;
    }
}
