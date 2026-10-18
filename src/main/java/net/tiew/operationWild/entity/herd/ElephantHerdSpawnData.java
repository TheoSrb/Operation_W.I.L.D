package net.tiew.operationWild.entity.herd;

import net.minecraft.world.entity.AgeableMob;

import java.util.UUID;

public class ElephantHerdSpawnData extends AgeableMob.AgeableMobGroupData {

    private final UUID herdId;

    public ElephantHerdSpawnData(UUID herdId) {
        super(false);
        this.herdId = herdId;
    }

    public UUID getHerdId() {
        return herdId;
    }
}
