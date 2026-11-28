package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum GiraffeVariant {
    DEFAULT(0),
    RETICULATED(1),
    GOLDEN(2);

    public static final GiraffeVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(GiraffeVariant::getId))
            .toArray(GiraffeVariant[]::new);

    private final int id;

    GiraffeVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GiraffeVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
