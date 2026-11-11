package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum PeacockVariant {
    DEFAULT(0),
    GREEN(1),
    PURPLE(2),
    CAMEO(3),
    ALBINO(4);

    public enum Cosmetics {
        ;

        public final PeacockVariant variant;

        Cosmetics(PeacockVariant variant) {
            this.variant = variant;
        }
    }

    public static final PeacockVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(PeacockVariant::getId))
            .toArray(PeacockVariant[]::new);

    private final int id;

    PeacockVariant(int id) {
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

    public static PeacockVariant byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)];
    }
}
