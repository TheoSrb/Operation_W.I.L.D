package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ElephantVariant {
    DEFAULT(0),
    GREY(1),
    PINK(2);

    public static final ElephantVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ElephantVariant::getId)).toArray(ElephantVariant[]::new);

    private final int id;

    ElephantVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ElephantVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
