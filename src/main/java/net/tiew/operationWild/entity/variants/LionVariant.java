package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum LionVariant {
    DEFAULT(0),
    DARK(1),
    WHITE(2),

    LIONESS_DEFAULT(3),
    LIONESS_DARK(4),
    LIONESS_WHITE(5),

    SKIN_GOLD(6),
    LIONESS_SKIN_GOLD(7);

    public static final LionVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(LionVariant::getId)).toArray(LionVariant[]::new);

    private final int id;

    LionVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LionVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
