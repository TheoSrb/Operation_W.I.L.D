package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum CondorVariant {
    DEFAULT(0),
    GREY(1);

    public enum Cosmetics {
        ;

        public final CondorVariant variant;

        Cosmetics(CondorVariant variant) {
            this.variant = variant;
        }
    }

    public static final CondorVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(CondorVariant::getId))
            .toArray(CondorVariant[]::new);

    private final int id;

    CondorVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isCosmetic() {
        for (Cosmetics c : Cosmetics.values()) {
            if (c.variant == this) return true;
        }
        return false;
    }

    public static CondorVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
