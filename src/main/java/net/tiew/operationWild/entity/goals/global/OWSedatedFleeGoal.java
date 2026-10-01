package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class OWSedatedFleeGoal extends Goal {

    private static final int REPATH_INTERVAL = 30;
    private static final int ESCAPE_RADIUS = 16;
    private static final int ESCAPE_HEIGHT = 7;
    private static final int SWIM_SAMPLES = 8;

    private final OWEntity mob;
    @Nullable
    private Vec3 escape;
    private int repathTimer;

    public OWSedatedFleeGoal(OWEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isSedatedFleeing() || mob.isPassenger()) return false;
        escape = findEscape();
        return escape != null;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isSedatedFleeing() && !mob.isPassenger();
    }

    @Override
    public void start() {
        mob.forceSetTarget(null);
        mob.setRunning(true);
        mob.setState(2);
        runToEscape();
    }

    @Override
    public void tick() {
        if (--repathTimer > 0 && !mob.getNavigation().isDone()) return;
        Vec3 next = findEscape();
        if (next != null) escape = next;
        runToEscape();
    }

    @Override
    public void stop() {
        escape = null;
        mob.getNavigation().stop();
        mob.setRunning(false);
        mob.resetState();
    }

    private void runToEscape() {
        repathTimer = REPATH_INTERVAL + mob.getRandom().nextInt(10);
        if (escape == null) return;
        mob.getNavigation().moveTo(escape.x, escape.y, escape.z, mob.sedatedFleeSpeed());
    }

    @Nullable
    private Vec3 findEscape() {
        Entity threat = mob.aggressiveTaming.getThreat();
        return findEscapeFrom(mob, threat != null ? threat.position() : null, ESCAPE_RADIUS, ESCAPE_HEIGHT);
    }

    @Nullable
    public static Vec3 findEscapeFrom(OWEntity mob, @Nullable Vec3 danger, int radius, int height) {
        boolean swimmer = mob.isInWater() && (mob instanceof OWWaterEntity || mob instanceof OWSemiWaterEntity);
        if (swimmer) return findSwimEscape(mob, danger, radius, height);
        Vec3 target = danger != null
                ? DefaultRandomPos.getPosAway(mob, radius, height, danger)
                : DefaultRandomPos.getPos(mob, radius, height);
        return target != null ? target : DefaultRandomPos.getPos(mob, radius / 2, height);
    }

    @Nullable
    private static Vec3 findSwimEscape(OWEntity mob, @Nullable Vec3 danger, int radius, int height) {
        Vec3 best = null;
        double bestDistance = -1;
        for (int i = 0; i < SWIM_SAMPLES; i++) {
            Vec3 candidate = BehaviorUtils.getRandomSwimmablePos(mob, radius, height);
            if (candidate == null) continue;
            if (danger == null) return candidate;
            double distance = candidate.distanceToSqr(danger);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}
