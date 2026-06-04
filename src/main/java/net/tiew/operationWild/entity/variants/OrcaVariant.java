package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum OrcaVariant {
    DEFAULT(0),
    BLACK(1),
    AQUA(2),

    @Deprecated SKIN_GOLD(3);

    public enum Cosmetics {
        GOLD(OrcaVariant.SKIN_GOLD);

        public final OrcaVariant variant;

        Cosmetics(OrcaVariant variant) {
            this.variant = variant;
        }
    }

    public static final OrcaVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(OrcaVariant::getId)).toArray(OrcaVariant[]::new);

    private final int id;

    OrcaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static OrcaVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
