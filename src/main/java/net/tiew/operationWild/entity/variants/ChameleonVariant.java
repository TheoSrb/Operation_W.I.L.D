package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ChameleonVariant {
    DEFAULT(0);

    public static final ChameleonVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ChameleonVariant::getId)).toArray(ChameleonVariant[]::new);

    private final int id;

    ChameleonVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ChameleonVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
