package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum KangarooVariant {
    DEFAULT(0),
    ORANGE(1),
    BROWN(2);

    public static final KangarooVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(KangarooVariant::getId))
            .toArray(KangarooVariant[]::new);

    private final int id;

    KangarooVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static KangarooVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
