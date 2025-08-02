package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum TigerVariant {
    DEFAULT(0),
    LIGHT_ORANGE(1),
    GOLDEN(2),
    WHITE(3),

    SKIN_GOLD(4),
    SKIN_MAGMA(5),
    SKIN_VIRUS(6),
    SKIN_DAMNED(7);

    public static final TigerVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(TigerVariant::getId)).toArray(TigerVariant[]::new);

    private final int id;

    TigerVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TigerVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
