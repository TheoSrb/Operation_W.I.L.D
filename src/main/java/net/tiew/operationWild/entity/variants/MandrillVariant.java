package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum MandrillVariant {
    DEFAULT(0),
    BLUE(1),
    SILVER(2);

    public static final MandrillVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(MandrillVariant::getId)).toArray(MandrillVariant[]::new);

    private final int id;

    MandrillVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MandrillVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
