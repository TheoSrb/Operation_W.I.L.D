package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

import java.util.EnumSet;

public class RedPandaAlertedFleeGoal extends Goal {

    private enum Phase { FLEE, COWER, RECOVER }

    private static final double KEEP_AWAY_DISTANCE = 15.0;
    private static final int FLEE_HORIZONTAL = 13;
    private static final int FLEE_VERTICAL = 6;
    private static final int REPATH_INTERVAL = 20;

    private static final int SPRINT_MIN_TICKS = 50;
    private static final int SPRINT_MAX_TICKS = 90;
    private static final double SAFE_DISTANCE_SQR = 9.0 * 9.0;

    private static final int COWER_MIN_TICKS = 200;
    private static final int COWER_MAX_TICKS = 400;
    private static final int COWER_ALERT_REFRESH = 20;
    private static final int COWER_WHIMPER_INTERVAL = 55;

    private final RedPandaEntity panda;
    private final double speedModifier;

    private Phase phase = Phase.FLEE;
    private boolean finished;

    private LivingEntity threat;
    private int repathTimer;
    private int sprintTicks;
    private int phaseTicks;
    private int cowerBudget;

    public RedPandaAlertedFleeGoal(RedPandaEntity panda, double speedModifier) {
        this.panda = panda;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        if (finished) return false;
        if (panda.isPassenger() || panda.isVehicle() || panda.isIntimidating()) return false;

        if (phase != Phase.FLEE) return true;
        return panda.isAlerted() && threat != null && threat.isAlive() && isWithinRange();
    }

    @Override
    public void start() {
        finished = false;
        repathTimer = 0;
        phaseTicks = 0;
        cowerBudget = COWER_MIN_TICKS + panda.getRandom().nextInt(COWER_MAX_TICKS - COWER_MIN_TICKS);
        beginFlee();
        panda.setSitting(false);
    }

    @Override
    public void stop() {
        panda.setCowering(false);
        panda.clearAlert();
        panda.getNavigation().stop();

        threat = null;
        phase = Phase.FLEE;
        finished = false;
        phaseTicks = 0;
        cowerBudget = 0;
    }

    @Override
    public void tick() {
        switch (phase) {
            case FLEE -> tickFlee();
            case COWER -> tickCower();
            case RECOVER -> tickRecover();
        }
    }

    private void tickFlee() {
        if (threat == null) return;
        panda.getLookControl().setLookAt(threat, 30f, 30f);

        if (--sprintTicks <= 0 || panda.distanceToSqr(threat) >= SAFE_DISTANCE_SQR) {
            beginCower();
            return;
        }

        if (repathTimer > 0) {
            repathTimer--;
            if (!panda.getNavigation().isDone()) return;
        }
        repathTimer = REPATH_INTERVAL;

        Vec3 away = DefaultRandomPos.getPosAway(panda, FLEE_HORIZONTAL, FLEE_VERTICAL, threat.position());
        if (away == null) return;

        panda.getNavigation().moveTo(away.x, away.y, away.z, speedModifier);
    }

    private void beginFlee() {
        phase = Phase.FLEE;
        repathTimer = 0;
        sprintTicks = SPRINT_MIN_TICKS + panda.getRandom().nextInt(SPRINT_MAX_TICKS - SPRINT_MIN_TICKS);
        panda.setCowering(false);

        LivingEntity current = panda.getAlertSource();
        if (current != null && current.isAlive()) threat = current;
    }

    private void beginCower() {
        if (cowerBudget <= 0) {
            beginRecover();
            return;
        }

        phase = Phase.COWER;
        panda.getNavigation().stop();
        panda.setCowering(true);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SCREECH, SoundSource.NEUTRAL, 0.55f,
                (float) OWUtils.generateRandomInterval(1.35, 1.55));
    }

    private void tickCower() {
        if (panda.hurtTime > 0) {
            beginFlee();
            return;
        }

        holdStill();

        if (cowerBudget % COWER_ALERT_REFRESH == 0) {
            panda.raiseAlert(threat != null && threat.isAlive() ? threat : null);
        }
        if (cowerBudget % COWER_WHIMPER_INTERVAL == 0) {
            panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                    SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.3f,
                    (float) OWUtils.generateRandomInterval(1.5, 1.8));
        }

        if (--cowerBudget > 0) return;
        beginRecover();
    }

    private void beginRecover() {
        phase = Phase.RECOVER;
        phaseTicks = RedPandaEntity.FEAR_EXIT_TICKS;

        panda.setCowering(false);
        panda.setFearRecoverTimer(RedPandaEntity.FEAR_EXIT_TICKS);
    }

    private void tickRecover() {
        holdStill();
        if (--phaseTicks > 0) return;
        finished = true;
    }

    private void holdStill() {
        panda.getNavigation().stop();
        if (panda.onGround()) {
            panda.setDeltaMovement(panda.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }
    }

    private boolean isWithinRange() {
        return panda.distanceToSqr(threat) <= KEEP_AWAY_DISTANCE * KEEP_AWAY_DISTANCE;
    }
}
