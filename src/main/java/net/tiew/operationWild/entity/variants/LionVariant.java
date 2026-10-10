package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum LionVariant {
    DEFAULT(0),
    ALBINO(1);

    public enum Cosmetics {
        ;

        public final LionVariant variant;

        Cosmetics(LionVariant variant) {
            this.variant = variant;
        }
    }

    public static final LionVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(LionVariant::getId))
            .toArray(LionVariant[]::new);

    private final int id;

    LionVariant(int id) {
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

    public static LionVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
