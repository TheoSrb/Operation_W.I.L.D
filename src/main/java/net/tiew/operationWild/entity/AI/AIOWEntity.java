package net.tiew.operationWild.entity.AI;

import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public abstract class AIOWEntity {

    private OWEntity entity;

    public enum Temperament {
        PASSIVE,
        NEUTRAL,
        AGGRESSIVE,
    }

    public AIOWEntity(OWEntity entity) {
        this.entity = entity;
    }

    public abstract Temperament getTemperament();

    public void tick() {

    }
}
