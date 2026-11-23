package net.tiew.operationWild.entity.goals.peacock;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;

import java.util.EnumSet;

public class PeacockWildFeatherGoal extends Goal {

    private static final double MIN_RANGE = 4.0;
    private static final double MAX_RANGE = 15.0;
    private static final double CLOSE_RANGE = 2.8;
    private static final double GIVE_UP_RANGE = 19.0;
    private static final int MIN_SHOTS = 2;
    private static final int MAX_SHOTS = 4;
    private static final int FEARED_BONUS_SHOTS = 2;
    private static final int AIM_DELAY_TICKS = 6;
    private static final int LOST_SIGHT_TICKS = 20;
    private static final int BOUT_COOLDOWN_MIN = 40;
    private static final int BOUT_COOLDOWN_MAX = 90;
    private static final int FEARED_COOLDOWN_MIN = 15;
    private static final int FEARED_COOLDOWN_MAX = 35;
    private static final float TURN_SPEED = 25f;

    private final PeacockEntity peacock;

    private LivingEntity target;
    private int shotsLeft;
    private int aimDelay;
    private int unseenTicks;
    private long nextBoutTick;

    public PeacockWildFeatherGoal(PeacockEntity peacock) {
        this.peacock = peacock;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!canFight()) return false;
        if (peacock.level().getGameTime() < nextBoutTick) return false;
        LivingEntity candidate = peacock.getTarget();
        if (!isValidTarget(candidate) || peacock.getFeatherStock() <= 0) return false;
        double distance = peacock.distanceTo(candidate);
        if (distance < MIN_RANGE || distance > MAX_RANGE) return false;
        if (!peacock.getSensing().hasLineOfSight(candidate)) return false;
        target = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!canFight() || !isValidTarget(target) || target != peacock.getTarget()) return false;
        if (shotsLeft <= 0) return !peacock.isFeatherReady();
        if (peacock.getFeatherStock() <= 0) return false;
        double distance = peacock.distanceTo(target);
        return distance >= CLOSE_RANGE && distance <= GIVE_UP_RANGE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        shotsLeft = MIN_SHOTS + peacock.getRandom().nextInt(MAX_SHOTS - MIN_SHOTS + 1);
        if (isFeared(target)) shotsLeft += FEARED_BONUS_SHOTS;
        aimDelay = AIM_DELAY_TICKS;
        unseenTicks = 0;
        peacock.getNavigation().stop();
        peacock.setRunning(false);
        peacock.setMad(true);
    }

    @Override
    public void stop() {
        boolean feared = isFeared(target);
        int min = feared ? FEARED_COOLDOWN_MIN : BOUT_COOLDOWN_MIN;
        int max = feared ? FEARED_COOLDOWN_MAX : BOUT_COOLDOWN_MAX;
        nextBoutTick = peacock.level().getGameTime() + min + peacock.getRandom().nextInt(max - min + 1);
        target = null;
        shotsLeft = 0;
    }

    @Override
    public void tick() {
        if (target == null) return;
        peacock.getNavigation().stop();
        peacock.getLookControl().setLookAt(target, 30f, 30f);
        faceTarget();

        if (!peacock.getSensing().hasLineOfSight(target)) {
            if (++unseenTicks > LOST_SIGHT_TICKS) shotsLeft = 0;
            return;
        }
        unseenTicks = 0;

        if (aimDelay > 0) {
            aimDelay--;
            return;
        }
        if (shotsLeft > 0 && peacock.isFeatherReady() && peacock.wildShootFeatherAt(target)) shotsLeft--;
    }

    private void faceTarget() {
        double dx = target.getX() - peacock.getX();
        double dz = target.getZ() - peacock.getZ();
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float yaw = Mth.approachDegrees(peacock.getYRot(), wanted, TURN_SPEED);
        peacock.setYRot(yaw);
        peacock.setYBodyRot(yaw);
        peacock.setYHeadRot(Mth.approachDegrees(peacock.getYHeadRot(), wanted, TURN_SPEED * 1.5f));
    }

    private boolean canFight() {
        return !peacock.isTame() && !peacock.isBaby() && peacock.isAlive()
                && peacock.getControllingPassenger() == null
                && !peacock.isKnockedOut() && !peacock.isFleeingDanger() && !peacock.isNapping()
                && !peacock.isSitting() && !peacock.isFanDisplaying() && !peacock.isSweeping()
                && !peacock.isGliding() && !peacock.isInWater();
    }

    private static boolean isValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) return false;
        return !(entity instanceof Player player) || !player.isCreative();
    }

    private static boolean isFeared(LivingEntity entity) {
        if (entity == null) return false;
        LivingEntity body = entity.getRootVehicle() instanceof LivingEntity mount ? mount : entity;
        return body.hasEffect(OWEffects.FEAR_EFFECT.getDelegate());
    }
}
