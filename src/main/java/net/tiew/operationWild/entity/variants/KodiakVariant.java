package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum KodiakVariant {
    DEFAULT(0),
    BLACK(1),
    GREY(2),

    SKIN_GOLD(3);

    public static final KodiakVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(KodiakVariant::getId)).toArray(KodiakVariant[]::new);

    private final int id;

    KodiakVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static KodiakVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
