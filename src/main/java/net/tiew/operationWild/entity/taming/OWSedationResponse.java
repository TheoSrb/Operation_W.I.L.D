package net.tiew.operationWild.entity.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.breakout.OWBreakout;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.navigation.OWPathProbe;
import org.jetbrains.annotations.Nullable;

public class OWSedationResponse {

    private enum Stance { CALM, CHARGE, RETREAT, WARY, AMBUSH }

    private enum Reach { YES, OPEN, BREACH, NO, FAR, UNSURE }

    private static final int REEVALUATE_INTERVAL = 20;
    private static final int WATCH_INTERVAL = 10;
    private static final int UNREACHABLE_CHECKS = 4;
    private static final int REACHABLE_CHECKS = 2;
    private static final int CHARGE_MEMORY = 600;
    private static final int RETREAT_MIN_TICKS = 200;
    private static final int RETREAT_MAX_TICKS = 1400;
    private static final int WARY_TICKS = 1200;
    private static final double RETREAT_SAFE_DISTANCE = 72.0;
    private static final double WARY_KEEP_DISTANCE = 40.0;
    private static final int DEEP_WATER_SCAN = 3;
    private static final int DEEP_WATER_BLOCKS = 2;
    private static final double CHARGE_GIVE_UP_DISTANCE = 56.0;
    private static final double MIN_STRIKE_REACH = 2.0;
    private static final double STRIKE_REACH_MARGIN = 1.2;
    private static final double STRIKE_RISE_MARGIN = 0.5;
    private static final double MIN_STRIKE_RISE = 1.0;
    private static final double SEARCH_FRONTIER_MARGIN = 3.0;
    private static final double UNSEEN_SHOOTER_DISTANCE = 16.0;
    private static final int PROBE_GROUND_SEARCH = 8;
    private static final int FRUSTRATION_LIMIT = 600;
    private static final int FRUSTRATION_COOLDOWN = 300;
    private static final int BREAK_PROGRESS_GRACE = 100;
    private static final int PATH_DOUBT_TICKS = 100;
    private static final int HARASS_MIN_FRUSTRATION = 100;
    private static final int HARASS_LIMIT_MIN = 4;
    private static final int HARASS_LIMIT_SPREAD = 2;
    private static final int MOMENTUM_SAMPLE = 20;
    private static final double MOMENTUM_GAIN = 1.0;
    private static final double MOMENTUM_RANGE = 6.0;
    private static final double AMBUSH_RETREAT_DISTANCE = 22.0;
    private static final int AMBUSH_RETREAT_MIN_TICKS = 60;
    private static final int AMBUSH_RETREAT_MAX_TICKS = 200;
    private static final int AMBUSH_MAX_TICKS = 1200;
    private static final double AMBUSH_TRIGGER_DISTANCE = 14.0;
    private static final int SURPRISE_TICKS = 100;
    private static final float SURPRISE_SPEED_BONUS = 1.35f;
    private static final int STALL_TICKS = 40;
    private static final double STALL_MOVE_SQR = 0.25;

    private final OWEntity entity;
    private Stance stance = Stance.CALM;
    @Nullable
    private LivingEntity provoker;
    @Nullable
    private Vec3 threatPosition;
    private int stanceTicks;
    private int sinceProvoked;
    private int reevaluateTimer;
    private int unreachableChecks;
    private int reachableChecks;
    private boolean provisional;
    private boolean reachedByPath;
    private int frustration;
    private int lastHitStamp = Integer.MIN_VALUE;
    private int frustratedUntil = Integer.MIN_VALUE / 2;
    private int harassment;
    private int harassLimit = HARASS_LIMIT_MIN;
    private boolean ambushPlanned;
    private int surpriseTicks;
    private Vec3 stallAnchor = Vec3.ZERO;
    private int stallTicks;
    private double pursuitDistance = Double.MAX_VALUE;
    private int pursuitSampleTimer;
    private boolean momentum;

    public OWSedationResponse(OWEntity entity) {
        this.entity = entity;
    }

    public boolean isRetreating() {
        return stance == Stance.RETREAT;
    }

    public boolean isAlarmed() {
        return stance != Stance.CALM;
    }

    @Nullable
    public LivingEntity getProvoker() {
        return provoker;
    }

    public boolean blocksTarget(@Nullable LivingEntity target) {
        if (target == null) return false;
        if (stance == Stance.RETREAT) return true;
        return (stance == Stance.WARY || stance == Stance.AMBUSH) && target == provoker;
    }

    public boolean isLurking() {
        return stance == Stance.AMBUSH;
    }

    public float chaseSpeedFactor() {
        return stance == Stance.CHARGE && surpriseTicks > 0 ? SURPRISE_SPEED_BONUS : 1f;
    }

    @Nullable
    public LivingEntity getChargeTarget() {
        return stance == Stance.CHARGE ? provoker : null;
    }

    @Nullable
    public Vec3 getThreatPosition() {
        return threatPosition;
    }

    public void onSedativeHit(DamageSource source) {
        if (!reacts() || entity.isSleeping() || entity.isSedatedFleeing() || entity.isVehicle()) return;

        sinceProvoked = 0;
        if (source.getEntity() instanceof LivingEntity shooter && shooter != entity) {
            if (stance == Stance.CHARGE && shooter == provoker && harassedOut(shooter)) {
                tacticalRetreat();
                return;
            }
            if (stance == Stance.AMBUSH && shooter == provoker
                    && entity.distanceToSqr(shooter) > AMBUSH_TRIGGER_DISTANCE * AMBUSH_TRIGGER_DISTANCE) {
                tacticalRetreat();
                return;
            }
            boolean ambushed = stance == Stance.AMBUSH;
            provoker = shooter;
            threatPosition = shooter.position();
            decide(shooter);
            if (ambushed && stance == Stance.CHARGE) surpriseTicks = SURPRISE_TICKS;
            return;
        }

        provoker = null;
        threatPosition = guessShotOrigin(source.getDirectEntity());
        retreat();
    }

    public void tick() {
        if (stance == Stance.CALM) return;
        if (!reacts() || !entity.isAlive() || entity.isSleeping() || entity.isSedatedFleeing()) {
            reset();
            return;
        }

        stanceTicks++;
        sinceProvoked++;
        if (surpriseTicks > 0) surpriseTicks--;
        if (provoker != null && !isPresent(provoker)) provoker = null;
        if (provoker != null) threatPosition = provoker.position();

        switch (stance) {
            case CHARGE -> tickCharge();
            case RETREAT -> tickRetreat();
            case WARY -> tickWary();
            case AMBUSH -> tickAmbush();
            default -> {
            }
        }
    }

    public void reset() {
        entity.breakout.endSiege();
        stance = Stance.CALM;
        provoker = null;
        threatPosition = null;
        stanceTicks = 0;
        unreachableChecks = 0;
        reachableChecks = 0;
        provisional = false;
        frustration = 0;
        harassment = 0;
        ambushPlanned = false;
        surpriseTicks = 0;
    }

    public boolean isFrustrated() {
        return entity.tickCount < frustratedUntil;
    }

    private void decide(LivingEntity shooter) {
        if (entity.isBaby() || !isAttackable(shooter) || isFrustrated()) {
            retreat();
            return;
        }

        switch (reach(shooter)) {
            case YES -> charge(shooter);
            case BREACH -> siege(shooter);
            case NO -> retreat();
            case OPEN, FAR, UNSURE -> {
                if (stance == Stance.RETREAT || stance == Stance.WARY) {
                    retreat();
                } else if (stance != Stance.CHARGE) {
                    charge(shooter);
                    if (stance == Stance.CHARGE) {
                        provisional = true;
                        reevaluateTimer = WATCH_INTERVAL;
                    }
                } else {
                    charge(shooter);
                }
            }
        }
    }

    private void charge(LivingEntity shooter) {
        if (stance != Stance.CHARGE) {
            stanceTicks = 0;
            unreachableChecks = 0;
            reachableChecks = 0;
            provisional = false;
            reevaluateTimer = REEVALUATE_INTERVAL;
            frustration = 0;
            harassment = 0;
            harassLimit = HARASS_LIMIT_MIN + entity.getRandom().nextInt(HARASS_LIMIT_SPREAD);
            lastHitStamp = shooter.getLastHurtByMobTimestamp();
            pursuitDistance = Double.MAX_VALUE;
            pursuitSampleTimer = 0;
            momentum = false;
        }
        ambushPlanned = false;
        stance = Stance.CHARGE;
        entity.setNap(false);
        entity.setSitting(false);
        keepAngry(shooter);
        if (entity.getTarget() != shooter) entity.setTarget(shooter);
        if (entity.getTarget() != shooter) retreat();
    }

    private void siege(LivingEntity shooter) {
        charge(shooter);
        if (stance != Stance.CHARGE) return;
        if (!entity.breakout.startSiege(shooter)) retreat();
    }

    private void retreat() {
        entity.breakout.endSiege();
        ambushPlanned = false;
        surpriseTicks = 0;
        boolean wasRetreating = stance == Stance.RETREAT;
        stance = Stance.RETREAT;
        stanceTicks = 0;
        unreachableChecks = 0;
        reachableChecks = 0;
        reevaluateTimer = WATCH_INTERVAL;
        entity.setNap(false);
        if (!wasRetreating) stopMovementAndTargeting();
        calmDown();
    }

    private void becomeWary() {
        entity.breakout.endSiege();
        if (provoker == null) {
            reset();
            return;
        }
        stance = Stance.WARY;
        stanceTicks = 0;
        reachableChecks = 0;
        reevaluateTimer = WATCH_INTERVAL;
    }

    private void tickCharge() {
        boolean besieging = provoker != null && entity.breakout.isSieging(provoker);
        if (besieging) sinceProvoked = 0;
        if (provoker == null || (sinceProvoked > CHARGE_MEMORY && !besieging)
                || entity.distanceToSqr(provoker) > CHARGE_GIVE_UP_DISTANCE * CHARGE_GIVE_UP_DISTANCE) {
            reset();
            return;
        }
        if (!isAttackable(provoker)) {
            retreat();
            return;
        }
        if (landedHit(provoker)) {
            frustration = 0;
            sinceProvoked = 0;
            harassment = 0;
            surpriseTicks = 0;
        } else if (besieging && entity.breakout.ticksSinceProgress() < BREAK_PROGRESS_GRACE) {
            frustration = 0;
            sinceProvoked = 0;
        } else if (++frustration >= FRUSTRATION_LIMIT) {
            giveUp();
            return;
        }

        keepAngry(provoker);
        tickMomentum(provoker);
        if (stalled(provoker)) unstick(provoker);
        if (--reevaluateTimer > 0) return;
        reevaluateTimer = provisional ? WATCH_INTERVAL : REEVALUATE_INTERVAL;

        Reach reach = reach(provoker);
        if (reach == Reach.YES) {
            unreachableChecks = 0;
            provisional = false;
            if (!reachedByPath) entity.breakout.endSiege();
            else if (frustration >= PATH_DOUBT_TICKS && !entity.breakout.isSieging(provoker)
                    && entity.breakout.siegeOutlook(provoker) == OWBreakout.SiegeOutlook.BREACH) {
                entity.breakout.startSiege(provoker);
            }
        } else if (reach == Reach.BREACH) {
            unreachableChecks = 0;
            provisional = false;
            if (!entity.breakout.startSiege(provoker)) retreat();
        } else if (reach == Reach.NO && (provisional || ++unreachableChecks >= UNREACHABLE_CHECKS)) {
            retreat();
        }
    }

    private void tickMomentum(LivingEntity target) {
        if (--pursuitSampleTimer > 0) return;
        pursuitSampleTimer = MOMENTUM_SAMPLE;
        double distance = entity.distanceTo(target);
        double range = Math.max(MOMENTUM_RANGE, entity.getBbWidth() * 2.0);
        momentum = distance <= range || pursuitDistance - distance >= MOMENTUM_GAIN;
        pursuitDistance = distance;
    }

    private boolean stalled(LivingEntity target) {
        boolean idle = !entity.isCombo() && entity.getNavigation().isDone() && !entity.breakout.isSieging(target)
                && chasesByGoal() && !withinStrike(entity.position(), target);
        if (!idle || entity.position().distanceToSqr(stallAnchor) > STALL_MOVE_SQR) {
            stallAnchor = entity.position();
            stallTicks = 0;
            return false;
        }
        return ++stallTicks >= STALL_TICKS;
    }

    private boolean chasesByGoal() {
        for (WrappedGoal goal : entity.goalSelector.getAvailableGoals()) {
            if (goal.isRunning() && (goal.getGoal() instanceof MeleeAttackGoal || goal.getGoal() instanceof OWAttackGoal)) return true;
        }
        return false;
    }

    private void unstick(LivingEntity target) {
        stallTicks = 0;
        if (entity.hasLineOfSight(target)) {
            entity.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), Math.max(1.0, entity.sedatedFleeSpeed()));
        } else if (entity.breakout.siegeOutlook(target) != OWBreakout.SiegeOutlook.IMPOSSIBLE) {
            entity.breakout.startSiege(target);
        }
    }

    private boolean landedHit(LivingEntity target) {
        if (target.getLastHurtByMob() != entity) return false;
        int stamp = target.getLastHurtByMobTimestamp();
        if (stamp == lastHitStamp) return false;
        lastHitStamp = stamp;
        return true;
    }

    private void giveUp() {
        frustratedUntil = entity.tickCount + FRUSTRATION_COOLDOWN;
        retreat();
    }

    private boolean harassedOut(LivingEntity shooter) {
        if (entity.isBaby() || frustration < HARASS_MIN_FRUSTRATION || momentum) return false;
        if (entity.breakout.isSieging(shooter) && entity.breakout.ticksSinceProgress() < BREAK_PROGRESS_GRACE) return false;
        return ++harassment >= harassLimit;
    }

    private void tacticalRetreat() {
        retreat();
        ambushPlanned = provoker != null;
    }

    private void tickTacticalRetreat() {
        calmDown();
        if (provoker == null) {
            ambushPlanned = false;
            return;
        }
        boolean farEnough = entity.distanceToSqr(provoker) >= AMBUSH_RETREAT_DISTANCE * AMBUSH_RETREAT_DISTANCE;
        if ((farEnough && stanceTicks >= AMBUSH_RETREAT_MIN_TICKS) || stanceTicks >= AMBUSH_RETREAT_MAX_TICKS) lurk();
    }

    private void lurk() {
        entity.breakout.endSiege();
        ambushPlanned = false;
        stance = Stance.AMBUSH;
        stanceTicks = 0;
        reachableChecks = 0;
        reevaluateTimer = WATCH_INTERVAL;
        entity.getNavigation().stop();
    }

    private void tickAmbush() {
        if (provoker == null || stanceTicks >= AMBUSH_MAX_TICKS) {
            reset();
            return;
        }
        calmDown();
        entity.getNavigation().stop();
        entity.getLookControl().setLookAt(provoker, 10f, 10f);
        if (--reevaluateTimer > 0) return;
        reevaluateTimer = WATCH_INTERVAL;
        if (entity.distanceToSqr(provoker) > AMBUSH_TRIGGER_DISTANCE * AMBUSH_TRIGGER_DISTANCE) return;
        if (entity.isBaby() || !isAttackable(provoker)) return;

        Reach reach = reach(provoker);
        if (reach == Reach.NO || reach == Reach.UNSURE) return;
        sinceProvoked = 0;
        if (reach == Reach.BREACH) siege(provoker);
        else charge(provoker);
        if (stance == Stance.CHARGE) surpriseTicks = SURPRISE_TICKS;
    }

    private void tickRetreat() {
        calmDown();
        if (ambushPlanned) {
            tickTacticalRetreat();
            return;
        }
        if (watchForOpening()) return;
        boolean safe = threatPosition == null
                || entity.position().distanceToSqr(threatPosition) >= RETREAT_SAFE_DISTANCE * RETREAT_SAFE_DISTANCE;
        if ((safe && stanceTicks >= RETREAT_MIN_TICKS) || stanceTicks >= RETREAT_MAX_TICKS) becomeWary();
    }

    private void tickWary() {
        if (provoker == null || stanceTicks >= WARY_TICKS) {
            reset();
            return;
        }
        if (entity.getTarget() == provoker) entity.setTarget(null);
        if (watchForOpening()) return;
        if (entity.distanceToSqr(provoker) < WARY_KEEP_DISTANCE * WARY_KEEP_DISTANCE) retreat();
    }

    private boolean watchForOpening() {
        if (provoker == null || isFrustrated() || --reevaluateTimer > 0) return false;
        reevaluateTimer = WATCH_INTERVAL;

        Reach reach = !entity.isBaby() && isAttackable(provoker)
                && entity.distanceToSqr(provoker) <= CHARGE_GIVE_UP_DISTANCE * CHARGE_GIVE_UP_DISTANCE
                ? reach(provoker) : Reach.NO;
        boolean opening = reach == Reach.YES || reach == Reach.OPEN || reach == Reach.BREACH;
        reachableChecks = opening ? reachableChecks + 1 : 0;
        if (reachableChecks < REACHABLE_CHECKS) return false;

        sinceProvoked = 0;
        if (reach == Reach.BREACH) siege(provoker);
        else charge(provoker);
        return stance == Stance.CHARGE;
    }

    private boolean reacts() {
        if (entity.level().isClientSide() || !entity.usesAggressiveTaming() || entity.isTame()) return false;
        OWEntityConfig.Temperament temperament = entity.getTemperament();
        return temperament == OWEntityConfig.Temperament.NEUTRAL || temperament == OWEntityConfig.Temperament.AGGRESSIVE;
    }

    private boolean isPresent(LivingEntity target) {
        return target.isAlive() && !target.isRemoved() && target.level() == entity.level();
    }

    private boolean isAttackable(LivingEntity target) {
        if (!isPresent(target) || target.isInvulnerable()) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return !entity.isAlliedTo(target);
    }

    private Reach reach(LivingEntity target) {
        reachedByPath = false;
        if (sheltersInWater(target)) return Reach.NO;
        if (withinStrike(entity.position(), target)) return Reach.YES;
        if (!entity.onGround() && !entity.isInWater() && !entity.isInLava()) return Reach.UNSURE;

        float range = (float) entity.getAttributeValue(Attributes.FOLLOW_RANGE);
        Path path = probePath(target, range);
        Node end = path != null ? path.getEndNode() : null;
        if (path != null && end == null) return Reach.UNSURE;

        if (end != null) {
            Vec3 standing = Vec3.atBottomCenterOf(end.asBlockPos());
            if (withinStrike(standing, target)) {
                reachedByPath = true;
                return Reach.YES;
            }
            if (standing.distanceTo(entity.position()) >= range - SEARCH_FRONTIER_MARGIN) return Reach.FAR;
        }
        if (entity.breakout.isSieging(target)) return Reach.BREACH;
        return switch (entity.breakout.siegeOutlook(target)) {
            case BREACH -> Reach.BREACH;
            case OPEN -> Reach.OPEN;
            case IMPOSSIBLE -> Reach.NO;
        };
    }

    private boolean sheltersInWater(LivingEntity target) {
        if (entity instanceof OWWaterEntity || entity instanceof OWSemiWaterEntity) return false;
        BlockPos feet = target.blockPosition();
        int water = 0;
        for (int depth = 0; depth < DEEP_WATER_SCAN; depth++) {
            if (entity.level().getFluidState(feet.below(depth)).is(FluidTags.WATER)) water++;
        }
        return water >= DEEP_WATER_BLOCKS;
    }

    @Nullable
    private Path probePath(LivingEntity target, float range) {
        return OWPathProbe.find(entity, groundUnder(target), range, 0);
    }

    private BlockPos groundUnder(LivingEntity target) {
        BlockPos pos = target.blockPosition();
        if (target.onGround() || target.isInWater() || target.isInLava()) return pos;
        for (int i = 0; i < PROBE_GROUND_SEARCH && entity.level().isEmptyBlock(pos.below()); i++) pos = pos.below();
        return pos;
    }

    private boolean withinStrike(Vec3 standing, LivingEntity target) {
        double reach = Math.max(MIN_STRIKE_REACH, entity.getBbWidth() * 0.5 + STRIKE_REACH_MARGIN);
        double maxRise = Math.max(MIN_STRIKE_RISE, entity.getBbHeight() - STRIKE_RISE_MARGIN);
        double dx = target.getX() - standing.x;
        double dz = target.getZ() - standing.z;
        double gap = Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
        double rise = target.getY() - standing.y;
        if (gap > reach || rise > maxRise || rise <= -(entity.getBbHeight() + reach)) return false;
        Vec3 eye = standing.add(0.0, entity.getEyeHeight(), 0.0);
        HitResult hit = entity.level().clip(new ClipContext(eye, target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void keepAngry(LivingEntity target) {
        if (!(entity instanceof NeutralMob neutral)) return;
        if (!target.getUUID().equals(neutral.getPersistentAngerTarget())) neutral.setPersistentAngerTarget(target.getUUID());
        if (neutral.getRemainingPersistentAngerTime() <= 0) neutral.startPersistentAngerTimer();
    }

    private void calmDown() {
        if (entity instanceof NeutralMob neutral && (neutral.isAngry() || neutral.getPersistentAngerTarget() != null)) {
            neutral.stopBeingAngry();
        }
        if (entity.getTarget() != null) entity.setTarget(null);
        entity.setAggressive(false);
    }

    private void stopMovementAndTargeting() {
        for (WrappedGoal goal : entity.goalSelector.getAvailableGoals()) {
            if (goal.isRunning() && goal.getFlags().contains(Goal.Flag.MOVE)) goal.stop();
        }
        for (WrappedGoal goal : entity.targetSelector.getAvailableGoals()) {
            if (goal.isRunning()) goal.stop();
        }
        entity.getNavigation().stop();
    }

    @Nullable
    private Vec3 guessShotOrigin(@Nullable Entity projectile) {
        if (projectile == null) return null;
        Vec3 motion = projectile.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-4) return projectile.position();
        return projectile.position().subtract(motion.normalize().scale(UNSEEN_SHOOTER_DISTANCE));
    }
}
