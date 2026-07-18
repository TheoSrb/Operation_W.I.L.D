package net.tiew.operationWild.client;

import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;
import net.tiew.operationWild.team.OWTribeJoinRequirement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Miroir client de la liste des tribus du serveur, pour l'écran de découverte / adhésion.
 * Alimenté par {@code SyncTribeListPacket}.
 */
public final class OWClientTribeList {

    /**
     * Condition d'entrée + verdict calculé par le serveur pour le joueur local — le client ne mesure
     * jamais rien lui-même.
     */
    public record Req(OWTribeJoinRequirement requirement, boolean met) {}

    /** Entrée légère d'affichage d'une tribu dans la liste. */
    public record Entry(int teamId, String name, String chiefName, int memberCount,
                        int primaryColor, int secondaryColor,
                        OWTeamMosaicPattern pattern, OWTeamBannerShape bannerShape,
                        boolean isPublic, List<Req> joinRequirements, boolean directJoin,
                        byte[] paintPixels,
                        int tertiaryColor, boolean useTertiary, int reputation) {

        /** Vrai si le joueur local remplit toutes les conditions (vrai aussi s'il n'y en a aucune). */
        public boolean allConditionsMet() {
            return joinRequirements.stream().allMatch(Req::met);
        }

        public long conditionsMetCount() {
            return joinRequirements.stream().filter(Req::met).count();
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private OWClientTribeList() {}

    public static synchronized void set(List<Entry> entries) {
        ENTRIES.clear();
        if (entries != null) ENTRIES.addAll(entries);
        // Tri : publiques d'abord (alpha), privées grisées à la toute fin.
        ENTRIES.sort(Comparator
                .comparing((Entry e) -> !e.isPublic())          // publiques (false) avant privées (true)
                .thenComparing(e -> e.name() == null ? "" : e.name().toLowerCase()));
    }

    public static synchronized List<Entry> get() {
        return new ArrayList<>(ENTRIES);
    }
}
