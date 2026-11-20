package net.tiew.operationWild.entity.bosses;

import net.minecraft.world.entity.Entity;

public interface OWBossTarget {

    Entity strikeGroup();

    int strikePriority();
}
