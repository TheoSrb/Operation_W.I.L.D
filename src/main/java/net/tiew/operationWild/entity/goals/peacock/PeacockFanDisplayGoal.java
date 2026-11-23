package net.tiew.operationWild.entity.goals.peacock;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;

import java.util.EnumSet;

public class PeacockFanDisplayGoal extends Goal {

    private static final double TRIGGER_RANGE = 9.0;
    private static final float BASE_CHANCE = 0.4f;
    private static final float WOUNDED_BONUS = 0.3f;
    private static final float WOUNDED_RATIO = 0.5f;
    private static final int RETRY_MIN = 60;
    private static final int RETRY_MAX = 140;
    private static final int COOLDOWN_MIN = 500;
    private static final int COOLDOWN_MAX = 800;
    private static final float TURN_SPEED = 20f;

    private final PeacockEntity peacock;

    private LivingEntity threat;
    private long nextTryTick;

    public PeacockFanDisplayGoal(PeacockEntity peacock) {
        this.peacock = peacock;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!canDisplay()) return false;
        long now = peacock.level().getGameTime();
        if (now < nextTryTick) return false;

        LivingEntity candidate = findThreat();
        if (candidate == null) return false;

        float chance = BASE_CHANCE;
        if (peacock.getHealth() < peacock.getMaxHealth() * WOUNDED_RATIO) chance += WOUNDED_BONUS;
        if (peacock.getRandom().nextFloat() >= chance) {
            nextTryTick = now + RETRY_MIN + peacock.getRandom().nextInt(RETRY_MAX - RETRY_MIN + 1);
            return false;
        }
        threat = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return peacock.isFanDisplaying();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        peacock.setRunning(false);
        if (!peacock.startFanDisplay(threat)) threat = null;
    }

    @Override
    public void stop() {
        if (peacock.isFanDisplaying()) peacock.closeFan();
        threat = null;
        nextTryTick = peacock.level().getGameTime() + COOLDOWN_MIN
                + peacock.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN + 1);
    }

    @Override
    public void tick() {
        peacock.getNavigation().stop();
        if (threat == null || !threat.isAlive()) return;
        peacock.getLookControl().setLookAt(threat, 30f, 30f);
        double dx = threat.getX() - peacock.getX();
        double dz = threat.getZ() - peacock.getZ();
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float yaw = Mth.approachDegrees(peacock.getYRot(), wanted, TURN_SPEED);
        peacock.setYRot(yaw);
        peacock.setYBodyRot(yaw);
    }

    private LivingEntity findThreat() {
        LivingEntity closest = null;
        double best = TRIGGER_RANGE * TRIGGER_RANGE;
        for (LivingEntity aggressor : peacock.fanAggressors()) {
            LivingEntity body = aggressor.getRootVehicle() instanceof LivingEntity mount ? mount : aggressor;
            if (body.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) continue;
            double distance = peacock.distanceToSqr(aggressor);
            if (distance <= best) {
                best = distance;
                closest = aggressor;
            }
        }
        return closest;
    }

    private boolean canDisplay() {
        return !peacock.isTame() && !peacock.isBaby() && peacock.isAlive() && peacock.onGround()
                && peacock.getControllingPassenger() == null && !peacock.isVehicle()
                && !peacock.isKnockedOut() && !peacock.isFleeingDanger() && !peacock.isNapping()
                && !peacock.isSitting() && !peacock.isSweeping() && !peacock.isGliding()
                && !peacock.isInWater() && !peacock.isFanDisplaying();
    }
}
