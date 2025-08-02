package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum PlantEmpressVariant {
    PLANT_EMPRESS(0);

    public static final PlantEmpressVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(PlantEmpressVariant::getId)).toArray(PlantEmpressVariant[]::new);

    private final int id;

    PlantEmpressVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PlantEmpressVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
