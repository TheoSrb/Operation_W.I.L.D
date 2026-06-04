package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum CrocodileVariant {
    DEFAULT(0),
    GREEN(1),
    DARK(2),

    @Deprecated SKIN_GOLD(3),
    @Deprecated SKIN_VERMILION_GUARDIAN(4),
    @Deprecated SKIN_TOY(5);

    public enum Cosmetics {
        GOLD(CrocodileVariant.SKIN_GOLD),
        VERMILION_GUARDIAN(CrocodileVariant.SKIN_VERMILION_GUARDIAN),
        TOY(CrocodileVariant.SKIN_TOY);

        public final CrocodileVariant variant;

        Cosmetics(CrocodileVariant variant) {
            this.variant = variant;
        }
    }

    public static final CrocodileVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(CrocodileVariant::getId)).toArray(CrocodileVariant[]::new);

    private final int id;

    CrocodileVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CrocodileVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
