package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum RedPandaVariant {
    DEFAULT(0),
    DARK(1);

    public static final RedPandaVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(RedPandaVariant::getId)).toArray(RedPandaVariant[]::new);

    private final int id;

    RedPandaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RedPandaVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
