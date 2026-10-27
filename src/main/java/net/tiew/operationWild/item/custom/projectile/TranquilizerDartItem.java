package net.tiew.operationWild.item.custom.projectile;

import net.minecraft.world.item.Item;

public class TranquilizerDartItem extends Item {
    private final boolean concentrated;

    public TranquilizerDartItem(Properties properties, boolean concentrated) {
        super(properties);
        this.concentrated = concentrated;
    }

    public boolean isConcentrated() {
        return this.concentrated;
    }
}
