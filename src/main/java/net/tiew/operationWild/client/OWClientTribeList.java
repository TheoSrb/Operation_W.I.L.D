package net.tiew.operationWild.client;

import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Miroir client de la liste des tribus du serveur, pour l'écran de découverte / adhésion.
 * Alimenté par {@code SyncTribeListPacket}.
 */
public final class OWClientTribeList {

    /** Entrée légère d'affichage d'une tribu dans la liste. */
    public record Entry(int teamId, String name, String chiefName, int memberCount,
                        int primaryColor, int secondaryColor,
                        OWTeamMosaicPattern pattern, OWTeamBannerShape bannerShape,
                        boolean isPublic, int minWildCoins, boolean[] paintPixels) {}

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
