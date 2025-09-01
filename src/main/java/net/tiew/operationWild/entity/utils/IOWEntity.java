package net.tiew.operationWild.entity.utils;

public interface IOWEntity {
    int getEntityColor();
    OWEntityUtils.Archetypes getArchetype();
    boolean preferRawMeat();
    boolean preferCookedMeat();
    boolean preferVegetables();
}