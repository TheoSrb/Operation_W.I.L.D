package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum MantaVariant {
    DEFAULT(0);

    public static final MantaVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(MantaVariant::getId)).toArray(MantaVariant[]::new);

    private final int id;

    MantaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MantaVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
