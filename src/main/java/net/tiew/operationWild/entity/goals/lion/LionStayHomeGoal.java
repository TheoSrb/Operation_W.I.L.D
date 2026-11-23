package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;

import java.util.EnumSet;

public class LionStayHomeGoal extends Goal {

    private static final int CHECK_INTERVAL = 40;
    private static final double LEAVE_FRACTION = 0.75;
    private static final double RETURN_FRACTION = 0.35;
    private static final double MAX_STEP = 24.0;
    private static final int MAX_TICKS = 600;

    private final LionEntity lion;
    private int ticks;

    public LionStayHomeGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !LionGoals.isCalm(lion) || lion.isNapping() || lion.isSitting()) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.home == null || LionGoals.huntRole(lion, clan) != null) return false;
        double radius = LionPride.territoryRadius(clan);
        return LionGoals.horizontalDistance(lion, LionPride.homeCenter(clan)) > radius * LEAVE_FRACTION;
    }

    @Override
    public void start() {
        ticks = 0;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.home == null) return;
        double radius = LionPride.territoryRadius(clan) * RETURN_FRACTION;
        Vec3 home = LionPride.homeCenter(clan);
        Vec3 goal = home.add(LionPride.randomOffset(lion, radius * 2), 0, LionPride.randomOffset(lion, radius * 2));
        Vec3 toward = goal.subtract(lion.position());
        double length = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
        if (length > MAX_STEP) goal = lion.position().add(toward.scale(MAX_STEP / length));
        BlockPos ground = LionPride.surface(lion.level(), goal);
        lion.getNavigation().moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5, 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        return ++ticks < MAX_TICKS && !lion.getNavigation().isDone() && LionGoals.isCalm(lion);
    }

    @Override
    public void stop() {
        lion.getNavigation().stop();
    }
}
