package net.tiew.operationWild.team;

public enum OWTeamMosaicPattern {

    GRADIENT_DOWN   (0,  "owteams.pattern.gradient_down"),
    GRADIENT_RIGHT  (2,  "owteams.pattern.gradient_right"),
    SPLIT_H         (4,  "owteams.pattern.split_h"),
    SPLIT_V         (5,  "owteams.pattern.split_v"),
    DIAGONAL_TL_BR  (6,  "owteams.pattern.diagonal_tl_br"),
    DIAGONAL_TR_BL  (7,  "owteams.pattern.diagonal_tr_bl"),
    THIRDS_H        (8,  "owteams.pattern.thirds_h"),
    THIRDS_V        (9,  "owteams.pattern.thirds_v"),
    CIRCLE_PRI      (10, "owteams.pattern.circle_pri"),
    STRIPES         (11, "owteams.pattern.stripes"),
    CHECKER         (12, "owteams.pattern.checker"),
    DIAMOND         (13, "owteams.pattern.diamond");

    private final int    id;
    private final String translationKey;

    OWTeamMosaicPattern(int id, String translationKey) {
        this.id             = id;
        this.translationKey = translationKey;
    }

    public int    getId()          { return id; }
    public String getDisplayName() { return translationKey; }

    public static OWTeamMosaicPattern byId(int id) {
        for (OWTeamMosaicPattern p : values()) {
            if (p.id == id) return p;
        }
        return GRADIENT_DOWN;
    }
}