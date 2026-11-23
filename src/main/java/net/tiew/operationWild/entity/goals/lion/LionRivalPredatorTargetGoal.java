package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class LionRivalPredatorTargetGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final double LIONESS_RANGE = 24.0;
    private static final double MALE_RANGE = 12.0;
    private static final double BIG_WIDTH = 1.5;
    private static final float BIG_HEALTH = 40f;
    private static final double BACKUP_RANGE = 24.0;
    private static final double CHASE_RANGE = 32.0;
    private static final double LEASH_FACTOR = 1.3;

    private final LionEntity lion;
    @Nullable
    private LivingEntity predator;

    public LionRivalPredatorTargetGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean eligible() {
        return !lion.isTame() && !lion.isBaby() && LionPride.isAwake(lion) && !lion.isSedatedFleeing() && !lion.isFleeingDanger()
                && !lion.wild.isConceding() && !lion.isVehicle();
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !eligible() || lion.getTarget() != null) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null) return false;
        predator = lion.level().getEntitiesOfClass(LivingEntity.class, lion.getBoundingBox().inflate(LIONESS_RANGE),
                        entity -> qualifies(clan, entity))
                .stream()
                .min(Comparator.comparingDouble(lion::distanceToSqr))
                .orElse(null);
        return predator != null;
    }

    private boolean qualifies(LionClanData.Clan clan, LivingEntity entity) {
        if (!LionPride.isRivalPredator(lion, entity) || lion.sedationResponse.blocksTarget(entity)) return false;
        if (lion.isMale()) {
            boolean inCore = LionPride.isInTerritory(clan, entity.position(), LionPride.CORE_FRACTION);
            if (!inCore && lion.distanceTo(entity) > MALE_RANGE) return false;
        } else if (clan.home != null && !LionPride.isInTerritory(clan, entity.position(), 1.0)) {
            return false;
        }
        if (!lion.getSensing().hasLineOfSight(entity)) return false;
        boolean big = entity.getBbWidth() >= BIG_WIDTH || entity.getMaxHealth() >= BIG_HEALTH;
        if (!big) return true;
        return lion.level().getEntitiesOfClass(LionEntity.class, lion.getBoundingBox().inflate(BACKUP_RANGE),
                mate -> mate != lion && lion.isClanmate(mate) && LionPride.isAwake(mate) && !mate.isBaby()).size() >= 1;
    }

    @Override
    public boolean canContinueToUse() {
        if (predator == null || !eligible() || lion.getTarget() != predator) return false;
        if (!LionPride.isRivalPredator(lion, predator) || lion.distanceTo(predator) > CHASE_RANGE) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.home == null) return true;
        Vec3 home = LionPride.homeCenter(clan);
        return LionPride.horizontalDistance(predator.position(), home) <= LionPride.territoryRadius(clan) * LEASH_FACTOR;
    }

    @Override
    public void start() {
        lion.setTarget(predator);
    }

    @Override
    public void stop() {
        if (predator != null && lion.getTarget() == predator) lion.setTarget(null);
        predator = null;
    }
}
