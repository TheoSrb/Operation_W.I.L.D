package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class LionIntruderTargetGoal extends Goal {

    private static final double ENGAGE_RANGE = 48.0;
    private static final double LEASH_FACTOR = 1.4;
    private static final double LEASH_WITHOUT_HOME = 32.0;

    private final LionEntity lion;
    @Nullable
    private Player intruder;

    public LionIntruderTargetGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean valid(@Nullable Player player) {
        if (player == null || lion.isTame() || !LionPride.isIntrudable(player)) return false;
        if (lion.isKnockedOut() || lion.isSedatedFleeing() || lion.isFleeingDanger() || lion.wild.isConceding()) return false;
        if (lion.sedationResponse.blocksTarget(player) || lion.distanceTo(player) > ENGAGE_RANGE) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan != null && clan.home != null) {
            return LionPride.horizontalDistance(player.position(), LionPride.homeCenter(clan)) <= LionPride.territoryRadius(clan) * LEASH_FACTOR;
        }
        return lion.distanceTo(player) <= LEASH_WITHOUT_HOME;
    }

    @Override
    public boolean canUse() {
        Player player = lion.wild.intruder();
        if (!valid(player) || lion.getTarget() == player) return false;
        intruder = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return intruder != null && lion.getTarget() == intruder && lion.wild.intruder() == intruder && valid(intruder);
    }

    @Override
    public void start() {
        if (lion.isNapping()) lion.setNap(false);
        if (lion.isSitting()) lion.setSitting(false);
        lion.setTarget(intruder);
    }

    @Override
    public void stop() {
        if (intruder != null && lion.getTarget() == intruder) lion.setTarget(null);
        intruder = null;
    }
}
