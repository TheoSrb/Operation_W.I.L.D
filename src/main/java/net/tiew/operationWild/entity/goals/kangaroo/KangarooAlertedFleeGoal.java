package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;

public class KangarooAlertedFleeGoal extends Goal {

    private static final double KEEP_AWAY_DISTANCE = 16.0;
    private static final int FLEE_HORIZONTAL = 14;
    private static final int FLEE_VERTICAL = 6;
    private static final int REPATH_INTERVAL = 20;

    private final KangarooEntity kangaroo;
    private final double speedModifier;

    private LivingEntity threat;
    private int repathTimer;

    public KangarooAlertedFleeGoal(KangarooEntity kangaroo, double speedModifier) {
        this.kangaroo = kangaroo;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (kangaroo.isTame() || kangaroo.isVehicle() || kangaroo.isThumping() || kangaroo.isAngry()) return false;
        if (!kangaroo.isAlerted()) return false;

        threat = kangaroo.getAlertSource();
        return threat != null && threat.isAlive() && kangaroo.distanceToSqr(threat) <= KEEP_AWAY_DISTANCE * KEEP_AWAY_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return kangaroo.isAlerted()
                && !kangaroo.isAngry()
                && !kangaroo.isThumping()
                && threat != null
                && threat.isAlive()
                && kangaroo.distanceToSqr(threat) <= KEEP_AWAY_DISTANCE * KEEP_AWAY_DISTANCE;
    }

    @Override
    public void start() {
        repathTimer = 0;
        kangaroo.setSitting(false);
        kangaroo.setNap(false);
    }

    @Override
    public void stop() {
        threat = null;
        kangaroo.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (threat == null) return;

        if (repathTimer > 0) {
            repathTimer--;
            if (!kangaroo.getNavigation().isDone()) return;
        }
        repathTimer = REPATH_INTERVAL;

        Vec3 away = DefaultRandomPos.getPosAway(kangaroo, FLEE_HORIZONTAL, FLEE_VERTICAL, threat.position());
        if (away == null) return;

        kangaroo.getNavigation().moveTo(away.x, away.y, away.z, speedModifier);
    }
}
