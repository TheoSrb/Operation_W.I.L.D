package net.tiew.operationWild.entity.variants;

import java.util.Arrays;
import java.util.Comparator;

public enum SeaBugShardVariant {

    DEFAULT_SHARD_0(0),
    DEFAULT_SHARD_1(1),
    DEFAULT_SHARD_2(2);

    public static final SeaBugShardVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(SeaBugShardVariant::getId)).toArray(SeaBugShardVariant[]::new);

    private final int id;

    SeaBugShardVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SeaBugShardVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
