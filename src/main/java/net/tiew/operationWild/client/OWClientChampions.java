package net.tiew.operationWild.client;

import net.tiew.operationWild.networking.packets.to_client.SyncChampionsPacket;
import net.tiew.operationWild.team.OWArenaFighter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Miroir client des champions de la tribu du joueur local, alimenté par {@link SyncChampionsPacket}.
 * Lecture seule : l'écran s'y réfère et n'invente rien — toute nomination passe par le serveur.
 */
public final class OWClientChampions {

    private static SyncChampionsPacket state = SyncChampionsPacket.empty();

    private OWClientChampions() {}

    public static void set(SyncChampionsPacket s) {
        state = s != null ? s : SyncChampionsPacket.empty();
    }

    public static List<OWArenaFighter> candidates() { return state.candidates(); }
    public static List<UUID> championUuids() { return state.champions(); }

    public static boolean isChampion(UUID entityUuid) {
        return state.championUUIDs().contains(entityUuid.toString());
    }

    /**
     * Les champions sous forme d'instantanés, dans l'ordre de nomination.
     *
     * <p>Un champion dont la créature est déchargée n'a pas d'instantané : sa case reste vide
     * plutôt que de décaler les suivantes, car l'ordre des emplacements est celui de la liste.</p>
     */
    public static List<OWArenaFighter> championFighters() {
        List<OWArenaFighter> out = new ArrayList<>();
        for (UUID id : championUuids()) {
            OWArenaFighter found = null;
            for (OWArenaFighter c : candidates()) {
                if (c.entityUuid().equals(id)) { found = c; break; }
            }
            out.add(found); // peut être null : créature hors des chunks chargés
        }
        return out;
    }
}
