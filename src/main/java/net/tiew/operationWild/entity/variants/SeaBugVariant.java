package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum SeaBugVariant {
    DEFAULT(0);

    public static final SeaBugVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(SeaBugVariant::getId)).toArray(SeaBugVariant[]::new);

    private final int id;

    SeaBugVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SeaBugVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
