package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum WalrusVariant {
    WALRUS(0);

    public static final WalrusVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(WalrusVariant::getId)).toArray(WalrusVariant[]::new);

    private final int id;

    WalrusVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static WalrusVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
