package net.tiew.operationWild.client;

import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.networking.packets.to_client.SyncArenaStatePacket;
import net.tiew.operationWild.team.OWArenaFighter;

import java.util.List;

/**
 * Miroir client de l'état d'arène de la tribu du joueur local, alimenté par
 * {@link SyncArenaStatePacket}. Purement en lecture : l'écran de combat s'y réfère et n'invente rien.
 */
public final class OWClientArenaState {

    private static SyncArenaStatePacket state = SyncArenaStatePacket.empty();

    private OWClientArenaState() {}

    public static void set(SyncArenaStatePacket s) {
        state = s != null ? s : SyncArenaStatePacket.empty();
    }

    public static SyncArenaStatePacket get() { return state; }

    public static OWArena.Phase phase() { return OWArena.Phase.byOrdinal(state.phaseOrdinal()); }

    public static OWArena.Result result() {
        OWArena.Result[] all = OWArena.Result.values();
        return all[Math.max(0, Math.min(all.length - 1, state.resultOrdinal()))];
    }

    public static List<OWArenaFighter> myFighters() { return state.myFighters(); }
    public static List<OWArenaFighter> opponentFighters() { return state.opponentFighters(); }
    public static List<OWArenaFighter> candidates() { return state.candidates(); }

    /** Vrai si {@code entityUuid} est déjà engagée par le joueur. */
    public static boolean isSelected(java.util.UUID entityUuid) {
        for (OWArenaFighter f : state.myFighters()) {
            if (f.entityUuid().equals(entityUuid)) return true;
        }
        return false;
    }

    /** Vrai si l'archétype est déjà pris par un autre combattant engagé (règle d'unicité). */
    public static boolean archetypeTaken(int archetypeOrdinal, java.util.UUID exclude) {
        for (OWArenaFighter f : state.myFighters()) {
            if (f.entityUuid().equals(exclude)) continue;
            if (f.archetypeOrdinal() == archetypeOrdinal) return true;
        }
        return false;
    }

    public static void clear() { state = SyncArenaStatePacket.empty(); }
}
