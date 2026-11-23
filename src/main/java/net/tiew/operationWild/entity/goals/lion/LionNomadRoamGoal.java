package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import net.tiew.operationWild.entity.clan.LionPrideState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class LionNomadRoamGoal extends Goal {

    private static final int CHECK_INTERVAL = 40;
    private static final double ROAM_MIN = 48.0;
    private static final double ROAM_SPREAD = 48.0;
    private static final double STEP = 24.0;
    private static final double ARRIVED = 5.0;
    private static final int REPATH_TICKS = 40;
    private static final int MAX_TICKS = 2400;
    private static final int PAUSE_MIN = 200;
    private static final int PAUSE_SPREAD = 400;
    private static final long RANDOM_WAYPOINT_TICKS = 3600L;

    private final LionEntity lion;
    @Nullable
    private Vec3 waypoint;
    private int repath;
    private int ticks;
    private long nextRoam;

    public LionNomadRoamGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !eligible()) return false;
        if (!(lion.level() instanceof ServerLevel level) || level.getGameTime() < nextRoam) return false;
        waypoint = resolveWaypoint(level);
        return waypoint != null;
    }

    private boolean eligible() {
        if (!lion.isMale() || lion.isBaby() || !LionGoals.isCalm(lion) || lion.isNapping() || lion.isSitting()) return false;
        LionClanData.Clan clan = lion.wildClan();
        return clan != null && clan.isNomad() && lion.getUUID().equals(clan.male);
    }

    @Nullable
    private Vec3 resolveWaypoint(ServerLevel level) {
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null) return null;
        LionPrideState state = clan.state;
        long now = level.getGameTime();
        if (state.waypoint == null || now >= state.waypointUntil) {
            float yaw = lion.getRandom().nextFloat() * 360f;
            double distance = ROAM_MIN + lion.getRandom().nextDouble() * ROAM_SPREAD;
            Vec3 far = lion.position().add(Vec3.directionFromRotation(0f, yaw).scale(distance));
            state.waypoint = LionPride.surface(level, far);
            state.waypointUntil = now + RANDOM_WAYPOINT_TICKS;
            state.waypointIsChallenge = false;
        }
        return Vec3.atBottomCenterOf(state.waypoint);
    }

    @Override
    public boolean canContinueToUse() {
        return waypoint != null && ++ticks < MAX_TICKS && eligible() && LionGoals.horizontalDistance(lion, waypoint) > ARRIVED;
    }

    @Override
    public void start() {
        repath = 0;
        ticks = 0;
    }

    @Override
    public void tick() {
        if (waypoint == null || --repath > 0) return;
        repath = REPATH_TICKS;
        Vec3 toward = waypoint.subtract(lion.position());
        double length = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
        Vec3 step = length > STEP ? lion.position().add(toward.scale(STEP / length)) : waypoint;
        BlockPos ground = LionPride.surface(lion.level(), step);
        LionClanData.Clan clan = lion.wildClan();
        double speed = clan != null && clan.state.waypointIsChallenge ? 1.3 : 1.0;
        lion.getNavigation().moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5, speed);
    }

    @Override
    public void stop() {
        lion.getNavigation().stop();
        LionClanData.Clan clan = lion.wildClan();
        if (waypoint != null && LionGoals.horizontalDistance(lion, waypoint) <= ARRIVED && clan != null && !clan.state.waypointIsChallenge) {
            clan.state.waypoint = null;
        }
        nextRoam = lion.level().getGameTime() + PAUSE_MIN + lion.getRandom().nextInt(PAUSE_SPREAD);
        waypoint = null;
    }
}
