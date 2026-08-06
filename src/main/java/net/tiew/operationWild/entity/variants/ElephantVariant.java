package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ElephantVariant {

    DEFAULT(0),
    GREY(1),
    PINK(2),

    @Deprecated SKIN_GOLD(3),
    @Deprecated SKIN_DEMON(4),
    @Deprecated SKIN_ZOMBIE(5);

    // ==================================================
    //                    COSMETICS
    // ==================================================

    public enum Cosmetics {
        GOLD(ElephantVariant.SKIN_GOLD),
        DEMON(ElephantVariant.SKIN_DEMON),
        ZOMBIE(ElephantVariant.SKIN_ZOMBIE);

        public final ElephantVariant variant;

        Cosmetics(ElephantVariant variant) {
            this.variant = variant;
        }
    }

    public static final ElephantVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(ElephantVariant::getId))
            .toArray(ElephantVariant[]::new);

    private final int id;

    ElephantVariant(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public boolean isCosmetic() {
        for (Cosmetics c : Cosmetics.values()) {
            if (c.variant == this) return true;
        }
        return false;
    }

    public static ElephantVariant byId(int id) {
        if (id < 0 || id >= BY_ID.length) return BY_ID[0];
        return BY_ID[id];
    }
}
