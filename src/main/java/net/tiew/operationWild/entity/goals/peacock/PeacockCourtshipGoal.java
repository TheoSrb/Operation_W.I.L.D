package net.tiew.operationWild.entity.goals.peacock;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;

import java.util.EnumSet;
import java.util.List;

public class PeacockCourtshipGoal extends Goal {

    private static final int SCAN_INTERVAL = 40;
    private static final double FEMALE_RANGE = 16.0;
    private static final double DISPLAY_DISTANCE = 3.5;
    private static final double FOLLOW_DISTANCE = 3.0;
    private static final double APPROACH_SPEED = 0.9;
    private static final double SHOW_FOLLOW_SPEED = 0.55;
    private static final int APPROACH_TIMEOUT = 200;
    private static final float COURT_CHANCE = 0.25f;
    private static final float STORM_CHANCE = 0.35f;
    private static final int SHOW_MIN_TICKS = 160;
    private static final int SHOW_MAX_TICKS = 280;
    private static final int COOLDOWN_MIN = 900;
    private static final int COOLDOWN_MAX = 1800;
    private static final float TURN_SPEED = 15f;

    private final PeacockEntity peacock;

    private PeacockEntity female;
    private boolean storm;
    private boolean started;
    private int approachTicks;
    private int scanCooldown;
    private long nextTryTick;
    private boolean stormDanced;

    public PeacockCourtshipGoal(PeacockEntity peacock) {
        this.peacock = peacock;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!peacock.isStormIncoming()) stormDanced = false;
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;
        if (!canCourt() || peacock.level().getGameTime() < nextTryTick) return false;

        if (peacock.isStormIncoming() && !stormDanced && peacock.getRandom().nextFloat() < STORM_CHANCE) {
            storm = true;
            female = null;
            return true;
        }
        PeacockEntity candidate = findFemale();
        if (candidate == null || peacock.getRandom().nextFloat() >= COURT_CHANCE) return false;
        storm = false;
        female = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (peacock.getTarget() != null || peacock.isKnockedOut() || peacock.isFleeingDanger()) return false;
        if (!started) return approachTicks < APPROACH_TIMEOUT && (storm || isValidFemale(female)) && canCourt();
        return peacock.isCourtingPhase() || peacock.getFanPhase() == PeacockEntity.FAN_CLOSE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        started = false;
        approachTicks = 0;
        peacock.setRunning(false);
        if (storm) begin();
    }

    @Override
    public void stop() {
        if (peacock.isCourtingPhase()) peacock.closeFan();
        peacock.getNavigation().stop();
        if (storm && started) stormDanced = true;
        female = null;
        started = false;
        nextTryTick = peacock.level().getGameTime() + COOLDOWN_MIN
                + peacock.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN + 1);
    }

    @Override
    public void tick() {
        if (!started) {
            approachTicks++;
            if (female == null) return;
            peacock.getLookControl().setLookAt(female, 30f, 30f);
            if (peacock.distanceTo(female) > DISPLAY_DISTANCE) {
                if (approachTicks % 10 == 1) peacock.getNavigation().moveTo(female, APPROACH_SPEED);
                return;
            }
            peacock.getNavigation().stop();
            begin();
            return;
        }

        int phase = peacock.getFanPhase();
        if (female != null && isValidFemale(female)) {
            peacock.getLookControl().setLookAt(female, 30f, 30f);
            if (phase == PeacockEntity.FAN_SHOW) {
                if (peacock.distanceTo(female) > FOLLOW_DISTANCE) {
                    if (peacock.tickCount % 10 == 0) peacock.getNavigation().moveTo(female, SHOW_FOLLOW_SPEED);
                } else {
                    peacock.getNavigation().stop();
                    peacock.faceTowards(female, TURN_SPEED);
                }
            } else {
                peacock.faceTowards(female, TURN_SPEED);
            }
            if (phase == PeacockEntity.FAN_OPEN || phase == PeacockEntity.FAN_DANCE) watchedBy(female);
        } else if (phase == PeacockEntity.FAN_SHOW && peacock.getNavigation().isDone() && peacock.tickCount % 60 == 0) {
            double angle = peacock.getRandom().nextDouble() * Math.PI * 2.0;
            peacock.getNavigation().moveTo(peacock.getX() + Math.cos(angle) * 3.0, peacock.getY(),
                    peacock.getZ() + Math.sin(angle) * 3.0, SHOW_FOLLOW_SPEED);
        }
    }

    private void begin() {
        int show = SHOW_MIN_TICKS + peacock.getRandom().nextInt(SHOW_MAX_TICKS - SHOW_MIN_TICKS + 1);
        started = peacock.startCourtship(female, show, storm);
        if (!started) approachTicks = APPROACH_TIMEOUT;
    }

    private void watchedBy(PeacockEntity hen) {
        if (hen.getTarget() != null || hen.isFanDisplaying() || hen.isNapping() || hen.isSitting()) return;
        hen.getNavigation().stop();
        hen.getLookControl().setLookAt(peacock, 30f, 30f);
    }

    private boolean canCourt() {
        return !peacock.isTame() && !peacock.isBaby() && peacock.isMale() && peacock.isAlive() && peacock.onGround()
                && peacock.getTarget() == null && peacock.getControllingPassenger() == null && !peacock.isVehicle()
                && !peacock.isKnockedOut() && !peacock.isFleeingDanger() && !peacock.isNapping() && !peacock.isSitting()
                && !peacock.isSweeping() && !peacock.isGliding() && !peacock.isInWater()
                && (!peacock.isFanDisplaying() || started);
    }

    private PeacockEntity findFemale() {
        List<PeacockEntity> hens = peacock.level().getEntitiesOfClass(PeacockEntity.class,
                peacock.getBoundingBox().inflate(FEMALE_RANGE), this::isValidFemale);
        PeacockEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PeacockEntity hen : hens) {
            double distance = peacock.distanceToSqr(hen);
            if (distance < bestDistance && peacock.getSensing().hasLineOfSight(hen)) {
                bestDistance = distance;
                best = hen;
            }
        }
        return best;
    }

    private boolean isValidFemale(PeacockEntity hen) {
        return hen != null && hen != peacock && hen.isAlive() && hen.isFemale() && !hen.isBaby() && !hen.isTame()
                && !hen.isKnockedOut() && !hen.isNapping() && peacock.distanceTo(hen) <= FEMALE_RANGE + 6.0;
    }
}
