package net.tiew.operationWild.team;

public enum OWTeamMosaicPattern {

    GRADIENT_DOWN             (0,  "owteams.pattern.gradient_down"),
    GRADIENT_RIGHT            (2,  "owteams.pattern.gradient_right"),
    SPLIT_H                   (4,  "owteams.pattern.split_h"),
    SPLIT_V                   (5,  "owteams.pattern.split_v"),
    DIAGONAL_TL_BR            (6,  "owteams.pattern.diagonal_tl_br"),
    THIRDS_H                  (7,  "owteams.pattern.thirds_h"),
    THIRDS_V                  (8,  "owteams.pattern.thirds_v"),
    CIRCLE_PRI                (9,  "owteams.pattern.circle_pri"),
    STRIPES                   (10, "owteams.pattern.stripes"),
    CHECKER                   (11, "owteams.pattern.checker"),
    DIAMOND                   (12, "owteams.pattern.diamond"),
    BOSS_PLANT_EMPRESS_PATTERN(14, "owteams.pattern.boss_plant_empress", true),
    CUSTOM_PAINT              (13, "owteams.pattern.custom_paint");

    private final int    id;
    private final String translationKey;
    private boolean isUnlockable;

    OWTeamMosaicPattern(int id, String translationKey) {
        this.id             = id;
        this.translationKey = translationKey;
    }

    OWTeamMosaicPattern(int id, String translationKey, boolean isUnlockable) {
        this.id             = id;
        this.translationKey = translationKey;
        this.isUnlockable = isUnlockable;
    }

    public int    getId()          { return id; }
    public String getDisplayName() { return translationKey; }

    public static OWTeamMosaicPattern byId(int id) {
        for (OWTeamMosaicPattern p : values()) {
            if (p.id == id) return p;
        }
        return GRADIENT_DOWN;
    }

    public static final int CUSTOM_PAINT_PIXEL_COUNT = 55 * 93; // 5115

    // ── Utilitaires pixel pour CUSTOM_PAINT ──────────────────────────────────

    /** Compacte un tableau boolean[] en byte[] (8 pixels par octet). */
    public static byte[] packPixels(boolean[] pixels) {
        if (pixels == null || pixels.length == 0) return new byte[0];
        byte[] packed = new byte[(pixels.length + 7) / 8];
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i]) packed[i >> 3] |= (byte) (1 << (i & 7));
        }
        return packed;
    }

    /** Décompacte un byte[] en boolean[] de longueur `count`. */
    public static boolean[] unpackPixels(byte[] packed, int count) {
        boolean[] pixels = new boolean[count];
        if (packed == null || packed.length == 0) return pixels;
        int limit = Math.min(count, packed.length * 8);
        for (int i = 0; i < limit; i++) {
            pixels[i] = (packed[i >> 3] & (1 << (i & 7))) != 0;
        }
        return pixels;
    }

    // ── Peinture 3 couleurs : 2 bits par pixel (valeurs 0 / 1 / 2) ──────────────
    /** Compacte un tableau byte[] (valeurs 0-2) en byte[], 4 pixels par octet. */
    public static byte[] packPixels3(byte[] pixels) {
        if (pixels == null || pixels.length == 0) return new byte[0];
        byte[] packed = new byte[(pixels.length + 3) / 4];
        for (int i = 0; i < pixels.length; i++) {
            int v = pixels[i] & 3;
            packed[i >> 2] |= (byte) (v << ((i & 3) * 2));
        }
        return packed;
    }

    /** Décompacte un byte[] (2 bits/pixel) en byte[] de longueur `count`. */
    public static byte[] unpackPixels3(byte[] packed, int count) {
        byte[] pixels = new byte[count];
        if (packed == null || packed.length == 0) return pixels;
        int limit = Math.min(count, packed.length * 4);
        for (int i = 0; i < limit; i++) {
            pixels[i] = (byte) ((packed[i >> 2] >> ((i & 3) * 2)) & 3);
        }
        return pixels;
    }

    public boolean isUnlockable() {
        return isUnlockable;
    }

    public void setUnlockable(boolean unlockable) {
        isUnlockable = unlockable;
    }
}