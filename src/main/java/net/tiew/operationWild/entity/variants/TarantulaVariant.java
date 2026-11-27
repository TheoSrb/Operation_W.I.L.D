package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum TarantulaVariant {
    DEFAULT(0, "default"),
    RED_KNEE(1, "red_knee"),
    CHILEAN_ROSE(2, "chilean_rose");

    public static final TarantulaVariant[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(TarantulaVariant::getId))
            .toArray(TarantulaVariant[]::new);

    private final int id;
    private final String textureName;

    TarantulaVariant(int id, String textureName) {
        this.id = id;
        this.textureName = textureName;
    }

    public int getId() {
        return id;
    }

    public String getTextureName() {
        return textureName;
    }

    public static TarantulaVariant byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : DEFAULT;
    }
}
