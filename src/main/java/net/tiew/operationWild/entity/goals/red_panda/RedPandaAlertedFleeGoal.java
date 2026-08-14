package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

import java.util.EnumSet;

public class RedPandaAlertedFleeGoal extends Goal {

    private static final double KEEP_AWAY_DISTANCE = 15.0;
    private static final int FLEE_HORIZONTAL = 13;
    private static final int FLEE_VERTICAL = 6;
    private static final int REPATH_INTERVAL = 20;

    private final RedPandaEntity panda;
    private final double speedModifier;

    private LivingEntity threat;
    private int repathTimer;

    public RedPandaAlertedFleeGoal(RedPandaEntity panda, double speedModifier) {
        this.panda = panda;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (panda.isTame() || panda.isBaby() || panda.isVehicle() || panda.isPassenger()) return false;
        if (panda.isNapping() || panda.isSleeping()) return false;
        if (panda.isIntimidating() || !panda.isAlerted()) return false;

        threat = panda.getAlertSource();
        return threat != null && threat.isAlive() && isWithinRange();
    }

    @Override
    public boolean canContinueToUse() {
        return panda.isAlerted()
                && !panda.isIntimidating()
                && !panda.isPassenger()
                && threat != null
                && threat.isAlive()
                && isWithinRange();
    }

    @Override
    public void start() {
        repathTimer = 0;
        panda.setSitting(false);
    }

    @Override
    public void stop() {
        threat = null;
        panda.clearAlert();
        panda.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (threat == null) return;

        panda.getLookControl().setLookAt(threat, 30f, 30f);

        if (repathTimer > 0) {
            repathTimer--;
            if (!panda.getNavigation().isDone()) return;
        }
        repathTimer = REPATH_INTERVAL;

        Vec3 away = DefaultRandomPos.getPosAway(panda, FLEE_HORIZONTAL, FLEE_VERTICAL, threat.position());
        if (away == null) return;

        panda.getNavigation().moveTo(away.x, away.y, away.z, speedModifier);
    }

    private boolean isWithinRange() {
        return panda.distanceToSqr(threat) <= KEEP_AWAY_DISTANCE * KEEP_AWAY_DISTANCE;
    }
}
