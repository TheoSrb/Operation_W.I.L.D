package net.tiew.operationWild.entity.config;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.goals.global.OWHurtByTargetGoal;

import java.util.List;

public interface IOWEntity {
    int getEntityColor();
    OWEntityConfig.Archetypes getArchetype();
    OWEntityConfig.Diet getDiet();
    OWEntityConfig.Temperament getTemperament();

    default List<Class<?>> getFavoriteTargets() {
        return null;
    }

    default List<Class<?>> getFavoriteTargetsByBeingNonTame() {
        return null;
    }

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

    default void registerBehaviorGoals(OWEntity owEntity) {
        switch (getTemperament()) {
            case PASSIVE:
                break;

            case NEUTRAL:
                owEntity.targetSelector.addGoal(1, new OWHurtByTargetGoal(owEntity));

                List<Class<?>> nonTameTargets = getFavoriteTargetsByBeingNonTame();
                if (nonTameTargets != null && !nonTameTargets.isEmpty()) {
                    int priority = 2;
                    for (Class<?> targetClass : nonTameTargets) {
                        if (LivingEntity.class.isAssignableFrom(targetClass)) {
                            owEntity.targetSelector.addGoal(priority, new NonTameRandomTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, false, null));
                            priority++;
                        }
                    }
                }
                break;

            case AGGRESSIVE:
                owEntity.targetSelector.addGoal(1, new OWHurtByTargetGoal(owEntity));

                List<Class<?>> favoriteTargets = getFavoriteTargets();

                if (favoriteTargets == null || favoriteTargets.isEmpty()) {
                    throw new IllegalStateException("[OW.ERROR] Entities with an aggressive temperament must necessarily possess the getFavoriteTargets() method /!\\" + owEntity.getClass().getName());
                }

                int priority = 3;
                for (Class<?> targetClass : favoriteTargets) {
                    if (LivingEntity.class.isAssignableFrom(targetClass)) {
                        owEntity.targetSelector.addGoal(priority, new NearestAttackableTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, true));
                        priority++;
                    }
                }

                List<Class<?>> nonTameTargetsAggr = getFavoriteTargetsByBeingNonTame();
                if (nonTameTargetsAggr != null && !nonTameTargetsAggr.isEmpty()) {
                    for (Class<?> targetClass : nonTameTargetsAggr) {
                        if (LivingEntity.class.isAssignableFrom(targetClass)) {
                            owEntity.targetSelector.addGoal(priority, new NonTameRandomTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, false, null));
                            priority++;
                        }
                    }
                }
                break;
        }
    }
}