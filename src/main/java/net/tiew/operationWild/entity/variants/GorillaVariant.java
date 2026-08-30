package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum GorillaVariant {
    DEFAULT(0),
    DARK(1),
    SILVER(2),
    ALBINOS(3);

    // ==================================================
    //                    COSMETICS
    // ==================================================

    public enum Cosmetics {
        ;

        public final GorillaVariant variant;

        Cosmetics(GorillaVariant variant) {
            this.variant = variant;
        }
    }

    public static final GorillaVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(GorillaVariant::getId))
            .toArray(GorillaVariant[]::new);

    private final int id;

    GorillaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isCosmetic() {
        for (Cosmetics c : Cosmetics.values()) {
            if (c.variant == this) return true;
        }
        return false;
    }

    public static GorillaVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
