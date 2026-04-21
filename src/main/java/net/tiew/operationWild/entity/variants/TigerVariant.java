package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum TigerVariant {

    DEFAULT(0),
    LIGHT_ORANGE(1),
    GOLDEN(2),
    WHITE(3),

    @Deprecated SKIN_GOLD(4),
    @Deprecated SKIN_BOSS(5),
    @Deprecated SKIN_VIRUS(6),
    @Deprecated SKIN_SEVEN_SEAS(7),
    @Deprecated SKIN_SCARLET_PIRATE(8),
    @Deprecated SKIN_CARTOON(9),
    @Deprecated SKIN_PIZZA_CHEF(10);

    // ==================================================
    //                    COSMETICS
    // ==================================================

    public enum Cosmetics {
        GOLD(TigerVariant.SKIN_GOLD),
        BOSS(TigerVariant.SKIN_BOSS),
        VIRUS(TigerVariant.SKIN_VIRUS),
        SEVEN_SEAS(TigerVariant.SKIN_SEVEN_SEAS),
        SCARLET_PIRATE(TigerVariant.SKIN_SCARLET_PIRATE),
        CARTOON(TigerVariant.SKIN_CARTOON),
        PIZZA_CHEF(TigerVariant.SKIN_PIZZA_CHEF);

        public final TigerVariant variant;

        Cosmetics(TigerVariant variant) {
            this.variant = variant;
        }
    }

    public static final TigerVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(TigerVariant::getId))
            .toArray(TigerVariant[]::new);

    private final int id;

    TigerVariant(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public boolean isCosmetic() {
        for (Cosmetics c : Cosmetics.values()) {
            if (c.variant == this) return true;
        }
        return false;
    }

    public static TigerVariant byId(int id) {
        if (id < 0 || id >= BY_ID.length) return BY_ID[0];
        return BY_ID[id];
    }
}