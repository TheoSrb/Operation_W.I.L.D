package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum CrocodileVariant {
    DEFAULT(0),
    GREEN(1),
    DARK(2),

    SKIN_GOLD(3);

    public static final CrocodileVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(CrocodileVariant::getId)).toArray(CrocodileVariant[]::new);

    private final int id;

    CrocodileVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CrocodileVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
