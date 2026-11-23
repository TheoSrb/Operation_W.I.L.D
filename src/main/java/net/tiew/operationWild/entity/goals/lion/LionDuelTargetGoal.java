package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class LionDuelTargetGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final double SCAN_RANGE = 32.0;
    private static final double GIVE_UP_RANGE = 40.0;

    private final LionEntity lion;
    @Nullable
    private LionEntity opponent;

    public LionDuelTargetGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean eligible() {
        return lion.isMale() && !lion.isBaby() && !lion.isTame() && !lion.isKnockedOut() && !lion.isSedatedFleeing()
                && !lion.isFleeingDanger() && !lion.wild.isConceding() && !lion.isVehicle();
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !eligible() || lion.getTarget() != null) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || !(lion.level() instanceof ServerLevel level)) return false;
        long now = level.getGameTime();
        opponent = level.getEntitiesOfClass(LionEntity.class, lion.getBoundingBox().inflate(SCAN_RANGE),
                        other -> other != lion && isOpponent(clan, other, now) && lion.getSensing().hasLineOfSight(other))
                .stream()
                .min(Comparator.comparingDouble(lion::distanceToSqr))
                .orElse(null);
        return opponent != null;
    }

    private boolean isOpponent(LionClanData.Clan clan, LionEntity other, long now) {
        if (!LionPride.isChallenger(clan, other, now)) return false;
        boolean rival = clan.state.hasRival(now) && clan.state.rival.equals(other.getClanId());
        if (clan.isNomad()) return rival;
        return rival || LionPride.isInTerritory(clan, other.position(), 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        return opponent != null && opponent.isAlive() && !opponent.wild.isConceding() && eligible()
                && lion.getTarget() == opponent && lion.distanceTo(opponent) <= GIVE_UP_RANGE;
    }

    @Override
    public void start() {
        if (lion.isNapping()) lion.setNap(false);
        if (lion.isSitting()) lion.setSitting(false);
        lion.setTarget(opponent);
    }

    @Override
    public void stop() {
        if (opponent != null && lion.getTarget() == opponent) lion.setTarget(null);
        opponent = null;
    }
}
