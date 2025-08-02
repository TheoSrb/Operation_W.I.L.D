package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum TigerSharkVariant {
    DEFAULT(0),
    BLUE(1),
    GREY(2),

    SKIN_GOLD(3);

    public static final TigerSharkVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(TigerSharkVariant::getId)).toArray(TigerSharkVariant[]::new);

    private final int id;

    TigerSharkVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TigerSharkVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
