package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum BoaVariant {
    DEFAULT(0),
    YELLOW(1),
    BROWN(2),
    DARK_GREEN(3),
    LIME(4),
    ALBINO(5),
    CORAL(6),

    SKIN_GOLD(7),
    SKIN_LEVIATHAN(8),
    SKIN_PLUSH(9);

    public static final BoaVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(BoaVariant::getId)).toArray(BoaVariant[]::new);

    private final int id;

    BoaVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static BoaVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
