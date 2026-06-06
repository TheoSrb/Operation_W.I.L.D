package net.tiew.operationWild.entity.config;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.goals.global.OWHurtByTargetGoal;

import java.util.List;
import java.util.function.Predicate;

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

    void hurtAfterCombo(LivingEntity entity, int comboAttack);

    boolean preferRawMeat();
    boolean preferCookedMeat();
    boolean preferVegetables();

    float getRotationSpeed();

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

    /**
     * Vrai si {@code potentialTarget} est le propriétaire de {@code owEntity} ou un animal apprivoisé
     * appartenant au même propriétaire. Sert à empêcher un pet en mode Agressif de s'en prendre à son
     * maître ou aux autres pets de son maître (cf. {@link #aggressiveTargetSelector}).
     */
    default boolean isFriendlyToOwner(OWEntity owEntity, LivingEntity potentialTarget) {
        LivingEntity owner = owEntity.getOwner();
        if (owner == null) {
            return false;
        }
        if (potentialTarget == owner) {
            return true;
        }
        return potentialTarget instanceof TamableAnimal tamable && tamable.isOwnedBy(owner);
    }

    /**
     * Filtre de ciblage commun aux goals offensifs.
     * <ul>
     *     <li>Sauvage : comportement d'origine, attaque toutes les cibles favorites.</li>
     *     <li>Apprivoisé + mode Agressif : attaque tout SAUF le propriétaire et les pets de ce dernier.</li>
     *     <li>Apprivoisé + mode Passif : ne cible rien.</li>
     * </ul>
     *
     * @param onlyWhenTamed si vrai, le goal ne s'active jamais à l'état sauvage (utilisé pour compléter
     *                      {@link NonTameRandomTargetGoal} sur les entités neutres, dont le ciblage sauvage
     *                      est déjà géré séparément).
     */
    default Predicate<LivingEntity> aggressiveTargetSelector(OWEntity owEntity, boolean onlyWhenTamed) {
        return potentialTarget -> {
            if (!owEntity.isTame()) {
                return !onlyWhenTamed;
            }
            if (owEntity.getCurrentMode() != OWEntity.Mode.Aggressive) {
                return false;
            }
            return !isFriendlyToOwner(owEntity, potentialTarget);
        };
    }

    default void registerBehaviorGoals(OWEntity owEntity) {
        switch (getTemperament()) {
            case PASSIVE:
                break;

            case NEUTRAL:
                owEntity.targetSelector.addGoal(1, new OWHurtByTargetGoal(owEntity));

                List<Class<?>> nonTameTargets = getFavoriteTargetsByBeingNonTame();
                if (nonTameTargets != null && !nonTameTargets.isEmpty()) {
                    // Goal sauvage d'origine (désactivé dès l'apprivoisement) + un goal jumeau qui ne
                    // s'active qu'une fois apprivoisé et en mode Agressif, sinon le pet neutre ne cible
                    // plus rien proactivement une fois apprivoisé.
                    Predicate<LivingEntity> tamedSelector = aggressiveTargetSelector(owEntity, true);
                    int priority = 2;
                    for (Class<?> targetClass : nonTameTargets) {
                        if (LivingEntity.class.isAssignableFrom(targetClass)) {
                            owEntity.targetSelector.addGoal(priority, new NonTameRandomTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, false, null));
                            priority++;
                            owEntity.targetSelector.addGoal(priority, new NearestAttackableTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, 10, false, false, tamedSelector));
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

                Predicate<LivingEntity> selector = aggressiveTargetSelector(owEntity, false);

                int priority = 3;
                for (Class<?> targetClass : favoriteTargets) {
                    if (LivingEntity.class.isAssignableFrom(targetClass)) {
                        boolean entityIsCrocodile = owEntity instanceof CrocodileEntity;
                        if (entityIsCrocodile) {
                            owEntity.targetSelector.addGoal(priority, new CrocodileEntity.CrocodileNearestAttackableTargetGoal(owEntity, targetClass, true, 1.0f, selector));
                        } else {
                            owEntity.targetSelector.addGoal(priority, new NearestAttackableTargetGoal<>(owEntity, (Class<? extends LivingEntity>) targetClass, 10, true, false, selector));
                        }
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