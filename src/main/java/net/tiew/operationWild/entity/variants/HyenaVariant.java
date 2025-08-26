package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum HyenaVariant {
    DEFAULT(0),
    GREY(1),
    YELLOW(2),
    DARK(3);

    public static final HyenaVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(HyenaVariant::getId)).toArray(HyenaVariant[]::new);

    private final int id;

    HyenaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static HyenaVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
