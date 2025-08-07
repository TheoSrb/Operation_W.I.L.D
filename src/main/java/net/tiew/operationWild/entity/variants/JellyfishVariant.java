package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum JellyfishVariant {
    DEFAULT(0),
    ORANGE(1),
    PINK(2),
    GREEN(3),
    PURPLE(4),
    WHITE(5),
    ELECTRIFIED(6);

    public static final JellyfishVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(JellyfishVariant::getId)).toArray(JellyfishVariant[]::new);

    private final int id;

    JellyfishVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static JellyfishVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
