package net.tiew.operationWild.entity.goals.global;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.taming.OWAggressiveTaming;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class OWSedatedFleeGoal extends Goal {

    private static final int ESCAPE_RADIUS = 48;
    private static final int ESCAPE_HEIGHT = 7;
    private static final double JOURNEY_DISTANCE = 120.0;
    private static final double SETTLE_DISTANCE = 48.0;
    private static final double ALERT_DISTANCE = 24.0;
    private static final float DROWSY_SLOWDOWN = 0.3f;
    private static final float STAGGER_BASE_CHANCE = 0.006f;
    private static final float STAGGER_DROWSY_CHANCE = 0.012f;
    private static final int STAGGER_MIN_TICKS = 5;
    private static final int STAGGER_MAX_TICKS = 9;
    private static final int STAGGER_COOLDOWN = 80;
    private static final float STAGGER_SPEED_FACTOR = 0.7f;
    private static final double STAGGER_SWAY = 0.08;
    private static final int RETRY_DELAY = 10;
    private static final int STRIKE_COOLDOWN = 20;
    private static final int STRIKE_TIMEOUT = 80;
    private static final double MIN_STRIKE_REACH = 2.0;
    private static final double STRIKE_REACH_MARGIN = 1.2;
    private static final double MIN_STRIKE_RISE = 1.0;
    private static final double STRIKE_RISE_MARGIN = 0.5;
    private static final float STRIKE_TURN_SPEED = 30f;
    private static final float STRIKE_FACING_TOLERANCE = 30f;

    private final OWEntity mob;
    private final OWFleeRoute route;
    private int staggerTicks;
    private int staggerCooldown;
    private int retryAt;
    private Vec3 journeyStart = Vec3.ZERO;
    private boolean settled;
    private int hurtStamp;
    private boolean bracing;
    @Nullable
    private LivingEntity victim;
    private int strikeTicks;
    private int strikeCooldown;

    public OWSedatedFleeGoal(OWEntity mob) {
        this.mob = mob;
        this.route = new OWFleeRoute(mob, ESCAPE_RADIUS, ESCAPE_HEIGHT);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isSedatedFleeing() || mob.isPassenger() || mob.breakout.wantsControl()) return false;
        if (mob.tickCount < retryAt) return false;
        if (route.prepare(danger())) return true;
        retryAt = mob.tickCount + RETRY_DELAY;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isSedatedFleeing() && !mob.isPassenger() && !mob.breakout.wantsControl();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.forceSetTarget(null);
        bracing = false;
        victim = null;
        beginJourney();
    }

    @Override
    public void tick() {
        Entity threat = mob.aggressiveTaming.getThreat();
        if (tickStrike(threat)) return;
        if (settled) {
            tickSettled(threat);
            return;
        }
        tickStagger();
        route.tick(threat != null ? threat.position() : null, threat, currentSpeed(), drowsiness());
        mob.setRunning(!route.isHolding());
        if (journeyDone(threat)) settle();
    }

    @Override
    public void stop() {
        route.stop();
        staggerTicks = 0;
        settled = false;
        bracing = false;
        victim = null;
        mob.setRunning(false);
        mob.resetState();
    }

    @Nullable
    private Vec3 danger() {
        Entity threat = mob.aggressiveTaming.getThreat();
        return threat != null ? threat.position() : null;
    }

    private void beginJourney() {
        settled = false;
        journeyStart = mob.position();
        hurtStamp = mob.getLastHurtByMobTimestamp();
        mob.setRunning(true);
        mob.setState(2);
        route.begin(currentSpeed());
    }

    private boolean journeyDone(@Nullable Entity threat) {
        if (route.isCornered() || mob.isInWater()) return false;
        double dx = mob.getX() - journeyStart.x;
        double dz = mob.getZ() - journeyStart.z;
        if (dx * dx + dz * dz < JOURNEY_DISTANCE * JOURNEY_DISTANCE) return false;
        return threat == null || mob.distanceToSqr(threat) >= SETTLE_DISTANCE * SETTLE_DISTANCE;
    }

    private void settle() {
        settled = true;
        staggerTicks = 0;
        route.stop();
        mob.setRunning(false);
        mob.resetState();
    }

    private void tickSettled(@Nullable Entity threat) {
        if (threat != null) mob.getLookControl().setLookAt(threat, 10f, 10f);
        boolean hurt = mob.getLastHurtByMobTimestamp() != hurtStamp;
        boolean close = threat != null && mob.distanceToSqr(threat) < ALERT_DISTANCE * ALERT_DISTANCE;
        if ((!hurt && !close) || mob.tickCount < retryAt) return;
        hurtStamp = mob.getLastHurtByMobTimestamp();
        if (route.prepare(danger())) beginJourney();
        else retryAt = mob.tickCount + RETRY_DELAY;
    }

    private boolean tickStrike(@Nullable Entity threat) {
        if (strikeCooldown > 0) strikeCooldown--;
        if (victim != null && mob.isCombo() && ++strikeTicks <= STRIKE_TIMEOUT) {
            holdStrike(victim);
            return true;
        }
        if (victim != null) endStrike();

        boolean ready = strikeCooldown <= 0 && !mob.isCombo() && mob.onGround() && mob.canStartCombo();
        LivingEntity target = ready ? strikeVictim(threat) : null;
        if (target == null) {
            if (bracing) resumeFlight();
            return false;
        }
        if (!bracing) brace();
        if (faceTowards(target)) {
            victim = target;
            strikeTicks = 0;
            mob.setCombo(true, 1);
        }
        return true;
    }

    private void brace() {
        bracing = true;
        staggerTicks = 0;
        mob.getNavigation().stop();
        mob.setRunning(false);
        if (mob.getState() == 2) mob.resetState();
    }

    private void holdStrike(LivingEntity target) {
        mob.getNavigation().stop();
        if (!canStrike(target)) return;
        faceTowards(target);
        if (mob.isPauseCombo() && !mob.playerContinueCombo && mob.actualAttackNumber < mob.MAX_ATTACKS_IN_COMBO - 1) {
            mob.playerContinueCombo = true;
        }
    }

    private void endStrike() {
        if (mob.isCombo()) {
            mob.resetCombo(0);
            mob.actualAttackNumber = 0;
        }
        victim = null;
        strikeCooldown = STRIKE_COOLDOWN;
    }

    private void resumeFlight() {
        bracing = false;
        if (settled) return;
        mob.setRunning(true);
        mob.setState(2);
        route.begin(currentSpeed());
    }

    @Nullable
    private LivingEntity strikeVictim(@Nullable Entity threat) {
        if (threat instanceof LivingEntity living && canStrike(living)) return living;
        return mob.level().getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), strikeReach() + mob.getBbWidth(),
                candidate -> candidate instanceof Player player && canStrike(player));
    }

    private boolean canStrike(LivingEntity target) {
        if (!target.isAlive() || target.isRemoved() || target.isInvulnerable() || mob.isAlliedTo(target)) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        double reach = strikeReach();
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        double gap = Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
        double rise = target.getY() - mob.getY();
        if (gap > reach || rise > Math.max(MIN_STRIKE_RISE, mob.getBbHeight() - STRIKE_RISE_MARGIN)) return false;
        return rise > -(mob.getBbHeight() + reach) && mob.hasLineOfSight(target);
    }

    private double strikeReach() {
        return Math.max(MIN_STRIKE_REACH, mob.getBbWidth() * 0.5 + STRIKE_REACH_MARGIN);
    }

    private boolean faceTowards(LivingEntity target) {
        mob.getLookControl().setLookAt(target, 30f, 30f);
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        if (dx * dx + dz * dz < 0.25) return true;
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float yaw = Mth.approachDegrees(mob.getYRot(), wanted, STRIKE_TURN_SPEED);
        mob.setYRot(yaw);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        return Math.abs(Mth.wrapDegrees(wanted - yaw)) <= STRIKE_FACING_TOLERANCE;
    }

    private void tickStagger() {
        if (staggerTicks > 0) {
            if (--staggerTicks == 0) mob.getNavigation().setSpeedModifier(fleeSpeed());
            return;
        }
        if (staggerCooldown > 0) {
            staggerCooldown--;
            return;
        }
        if (!mob.onGround() || mob.isInWater() || route.isHolding()) return;
        float chance = STAGGER_BASE_CHANCE + STAGGER_DROWSY_CHANCE * drowsiness();
        if (mob.getRandom().nextFloat() >= chance) return;

        staggerTicks = STAGGER_MIN_TICKS + mob.getRandom().nextInt(STAGGER_MAX_TICKS - STAGGER_MIN_TICKS + 1);
        staggerCooldown = STAGGER_COOLDOWN;
        mob.getNavigation().setSpeedModifier(fleeSpeed() * STAGGER_SPEED_FACTOR);
        double yaw = Math.toRadians(mob.getYRot());
        double side = mob.getRandom().nextBoolean() ? 1.0 : -1.0;
        mob.setDeltaMovement(mob.getDeltaMovement().add(Math.cos(yaw) * STAGGER_SWAY * side, 0.0, Math.sin(yaw) * STAGGER_SWAY * side));
        mob.hasImpulse = true;
    }

    private float drowsiness() {
        float range = 100f - OWAggressiveTaming.FLEE_THRESHOLD;
        return Mth.clamp((mob.getSleepBarPercent() - OWAggressiveTaming.FLEE_THRESHOLD) / range, 0f, 1f);
    }

    private double fleeSpeed() {
        return mob.sedatedFleeSpeed() * (1.0f - DROWSY_SLOWDOWN * drowsiness());
    }

    private double currentSpeed() {
        return fleeSpeed() * (staggerTicks > 0 ? STAGGER_SPEED_FACTOR : 1.0f);
    }
}
