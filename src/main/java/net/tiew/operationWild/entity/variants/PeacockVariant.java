package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum PeacockVariant {
    DEFAULT(0),
    BLUE(1),
    GREEN(2),
    RED(3),
    ALBINO(4),

    SKIN_GOLD(5);

    public static final PeacockVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(PeacockVariant::getId)).toArray(PeacockVariant[]::new);

    private final int id;

    PeacockVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PeacockVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
