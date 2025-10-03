package net.tiew.operationWild.entity.goals.kodiak;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class KodiakNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    public KodiakNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        super(mob, targetType, mustSee);
    }

    public KodiakNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, mustSee, targetPredicate);
    }

    public KodiakNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach) {
        super(mob, targetType, mustSee, mustReach);
    }

    public KodiakNearestAttackableTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    public boolean canUse() {
        if (this.mob instanceof KodiakEntity) {
            KodiakEntity kodiak = (KodiakEntity) this.mob;

            if (!kodiak.isHungry()) {
                return false;
            }
        }

        return super.canUse();
    }
}