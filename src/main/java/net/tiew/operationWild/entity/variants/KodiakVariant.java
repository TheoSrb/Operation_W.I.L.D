package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum KodiakVariant {
    DEFAULT(0),
    BLACK(1),
    GREY(2),

    SKIN_SKELETON(3),
    SKIN_GOLD(4);

    public static final KodiakVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(KodiakVariant::getId)).toArray(KodiakVariant[]::new);

    private final int id;

    KodiakVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static KodiakVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }

    /** Cosmetic skins unlockable via the skin system (skin index ≥ 1). */
    public enum Cosmetics {
        GOLD(SKIN_GOLD),
        SKELETON(SKIN_SKELETON);
        // SHADE (skinIndex 3) est géré séparément via le flag isShade() — pas un variant

        public final KodiakVariant variant;

        Cosmetics(KodiakVariant variant) {
            this.variant = variant;
        }
    }
}
