package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum RedPandaVariant {
    DEFAULT(0),
    RED(1),
    BROWN(2),

    @Deprecated SKIN_GOLD(3);

    // ==================================================
    //                    COSMETICS
    // ==================================================

    public enum Cosmetics {
        GOLD(RedPandaVariant.SKIN_GOLD);

        public final RedPandaVariant variant;

        Cosmetics(RedPandaVariant variant) {
            this.variant = variant;
        }
    }

    public static final RedPandaVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(RedPandaVariant::getId))
            .toArray(RedPandaVariant[]::new);

    private final int id;

    RedPandaVariant(int id) {
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

    public static RedPandaVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
