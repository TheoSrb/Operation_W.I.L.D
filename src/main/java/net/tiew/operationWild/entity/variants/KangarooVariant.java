package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum KangarooVariant {
    DEFAULT(0),
    ORANGE(1),
    BROWN(2),

    @Deprecated SKIN_GOLD(3);

    // ==================================================
    //                    COSMETICS
    // ==================================================

    public enum Cosmetics {
        GOLD(KangarooVariant.SKIN_GOLD);

        public final KangarooVariant variant;

        Cosmetics(KangarooVariant variant) {
            this.variant = variant;
        }
    }

    public static final KangarooVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(KangarooVariant::getId))
            .toArray(KangarooVariant[]::new);

    private final int id;

    KangarooVariant(int id) {
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

    public static KangarooVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
