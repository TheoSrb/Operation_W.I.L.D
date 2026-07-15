package net.tiew.operationWild.team;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

/**
 * Forme (silhouette) d'une bannière de tribu. Indépendante des couleurs / motifs
 * ({@link OWTeamMosaicPattern}) : la forme définit le contour, le motif remplit l'intérieur.
 *
 * <p>Toutes les formes sont stockées dans la texture {@code textures/gui/ow_teams_banners_styles.png}
 * (256×256). Chaque forme possède :
 * <ul>
 *   <li>une <b>base</b> 55×93 : la silhouette, utilisée comme masque tinté (on blit la région et
 *       les pixels transparents restent transparents — comme le fait déjà le rendu de drapeau) ;</li>
 *   <li>des <b>highlights</b> : un calque d'ombrage/brillance superposé par-dessus, aligné en bas
 *       de la bannière.</li>
 * </ul>
 */
public enum OWTeamBannerShape {

    CLASSIC  (0, "owteams.banner.classic",   0,   0,  0,  98, 25),
    DIAMOND  (1, "owteams.banner.diamond",  55,   0, 55,  93, 28),
    RECTANGLE(2, "owteams.banner.rectangle",110,  0,110,  93,  1),
    ROUND    (3, "owteams.banner.round",   165,   0,165,  93, 23),
    SPIKES   (4, "owteams.banner.spikes",    0, 125,  0, 218, 27),
    TORN     (5, "owteams.banner.torn",     55, 125, 55, 218, 23),
    BANDEAUX (6, "owteams.banner.bandeaux",110, 125,165, 141, 78);

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_banners_styles.png");

    public static final int TEXTURE_SIZE = 256;
    /** Dimensions communes de la silhouette de chaque forme. */
    public static final int BASE_W = 55;
    public static final int BASE_H = 93;

    private final int id;
    private final String translationKey;
    private final int baseU, baseV;
    private final int hlU, hlV, hlH;

    OWTeamBannerShape(int id, String translationKey,
                      int baseU, int baseV, int hlU, int hlV, int hlH) {
        this.id = id;
        this.translationKey = translationKey;
        this.baseU = baseU;
        this.baseV = baseV;
        this.hlU = hlU;
        this.hlV = hlV;
        this.hlH = hlH;
    }

    public int getId()             { return id; }
    public String getDisplayName() { return translationKey; }
    public Component getLabel()     { return Component.translatable(translationKey); }

    public int getBaseU() { return baseU; }
    public int getBaseV() { return baseV; }

    public int getHighlightU() { return hlU; }
    public int getHighlightV() { return hlV; }
    /** Largeur des highlights = largeur de la bannière. */
    public int getHighlightW() { return BASE_W; }
    public int getHighlightH() { return hlH; }

    public static OWTeamBannerShape byId(int id) {
        for (OWTeamBannerShape s : values()) if (s.id == id) return s;
        return CLASSIC;
    }
}
