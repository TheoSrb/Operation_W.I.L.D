package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum HippopotamusVariant {
    DEFAULT(0);

    public enum Cosmetics {
        ;

        public final HippopotamusVariant variant;

        Cosmetics(HippopotamusVariant variant) {
            this.variant = variant;
        }
    }

    public static final HippopotamusVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(HippopotamusVariant::getId))
            .toArray(HippopotamusVariant[]::new);

    private final int id;

    HippopotamusVariant(int id) {
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

    public static HippopotamusVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
