package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum BoaVariant {

    DEFAULT(0),
    YELLOW(1),
    BROWN(2),
    DARK(3),

    @Deprecated SKIN_GOLD(4),
    @Deprecated SKIN_LEVIATHAN(5),
    @Deprecated SKIN_PLUSH(6);

    public enum Cosmetics {
        GOLD(BoaVariant.SKIN_GOLD),
        LEVIATHAN(BoaVariant.SKIN_LEVIATHAN),
        PLUSH(BoaVariant.SKIN_PLUSH);

        public final BoaVariant variant;

        Cosmetics(BoaVariant variant) {
            this.variant = variant;
        }
    }

    public static final BoaVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(BoaVariant::getId))
            .toArray(BoaVariant[]::new);

    private final int id;

    BoaVariant(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public boolean isCosmetic() {
        for (Cosmetics c : Cosmetics.values()) {
            if (c.variant == this) return true;
        }
        return false;
    }

    public static BoaVariant byId(int id) {
        if (id < 0 || id >= BY_ID.length) return BY_ID[0];
        return BY_ID[id];
    }
}
