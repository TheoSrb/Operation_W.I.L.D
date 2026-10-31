package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ColossalSquidVariant {
    DEFAULT(0);

    public enum Cosmetics {
    }

    public static final ColossalSquidVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ColossalSquidVariant::getId)).toArray(ColossalSquidVariant[]::new);

    private final int id;

    ColossalSquidVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ColossalSquidVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
