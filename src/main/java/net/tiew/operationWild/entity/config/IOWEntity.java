package net.tiew.operationWild.entity.config;

public interface IOWEntity {
    int getEntityColor();
    OWEntityConfig.Archetypes getArchetype();
    OWEntityConfig.Diet getDiet();
    boolean preferRawMeat();
    boolean preferCookedMeat();
    boolean preferVegetables();

    default boolean isTank() {
        return getArchetype() == OWEntityConfig.Archetypes.TANK;
    }
    default boolean isAssassin() {
        return getArchetype() == OWEntityConfig.Archetypes.ASSASSIN;
    }
    default boolean isMarauder() {
        return getArchetype() == OWEntityConfig.Archetypes.MARAUDER;
    }
    default boolean isHealer() {
        return getArchetype() == OWEntityConfig.Archetypes.HEALER;
    }
    default boolean isBerserker() {
        return getArchetype() == OWEntityConfig.Archetypes.BERSERKER;
    }
    default boolean isScout() {
        return getArchetype() == OWEntityConfig.Archetypes.SCOUT;
    }
    default boolean isNormal() {
        return getArchetype() == OWEntityConfig.Archetypes.NORMAL;
    }

    default boolean isVegetarian() {
        return getDiet() == OWEntityConfig.Diet.VEGETARIAN;
    }
    default boolean isCarnivorous() {
        return getDiet() == OWEntityConfig.Diet.CARNIVOROUS;
    }
    default boolean isOmnivorous() {
        return getDiet() == OWEntityConfig.Diet.OMNIVOROUS;
    }
}