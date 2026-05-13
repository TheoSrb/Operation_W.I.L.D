package net.tiew.operationWild.team;

public enum OWTeamMosaicPattern {

    GRADIENT_DOWN   (0, "Dégradé ↓"),
    GRADIENT_UP     (1, "Dégradé ↑"),
    GRADIENT_RIGHT  (2, "Dégradé →"),
    GRADIENT_LEFT   (3, "Dégradé ←"),
    SPLIT_H         (4, "Moitié H"),
    SPLIT_V         (5, "Moitié V"),
    DIAGONAL_TL_BR  (6, "Diagonale ↘"),
    DIAGONAL_TR_BL  (7, "Diagonale ↙"),
    THIRDS_H        (8,  "Tiers H"),
    THIRDS_V        (9,  "Tiers V"),
    CIRCLE_PRI      (10, "Rond (pri)"),  // fond primaire, cercle secondaire
    CIRCLE_SEC      (11, "Rond (sec)");  // fond secondaire, cercle primaire

    private final int    id;
    private final String displayName;

    OWTeamMosaicPattern(int id, String displayName) {
        this.id          = id;
        this.displayName = displayName;
    }

    public int    getId()          { return id; }
    public String getDisplayName() { return displayName; }

    public static OWTeamMosaicPattern byId(int id) {
        for (OWTeamMosaicPattern p : values()) {
            if (p.id == id) return p;
        }
        return GRADIENT_DOWN;
    }
}