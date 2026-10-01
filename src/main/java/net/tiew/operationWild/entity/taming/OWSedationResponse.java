package net.tiew.operationWild.entity.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OWSedationResponse {

    private enum Stance { CALM, CHARGE, RETREAT, WARY }

    private enum Reach { YES, NO, FAR, UNSURE }

    private static final int REEVALUATE_INTERVAL = 20;
    private static final int WATCH_INTERVAL = 10;
    private static final int UNREACHABLE_CHECKS = 2;
    private static final int REACHABLE_CHECKS = 2;
    private static final int CHARGE_MEMORY = 600;
    private static final int RETREAT_MIN_TICKS = 100;
    private static final int RETREAT_MAX_TICKS = 700;
    private static final int WARY_TICKS = 600;
    private static final double RETREAT_SAFE_DISTANCE = 48.0;
    private static final double CHARGE_GIVE_UP_DISTANCE = 56.0;
    private static final double MIN_STRIKE_REACH = 2.0;
    private static final double STRIKE_REACH_MARGIN = 1.2;
    private static final double STRIKE_RISE_MARGIN = 0.5;
    private static final double MIN_STRIKE_RISE = 1.0;
    private static final double SEARCH_FRONTIER_MARGIN = 3.0;
    private static final double UNSEEN_SHOOTER_DISTANCE = 16.0;
    private static final int PROBE_REGION_MARGIN = 8;
    private static final int PROBE_GROUND_SEARCH = 8;

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

    public OWSedationResponse(OWEntity entity) {
        this.entity = entity;
    }

    public boolean isRetreating() {
        return stance == Stance.RETREAT;
    }

    public boolean blocksTarget(@Nullable LivingEntity target) {
        if (target == null) return false;
        if (stance == Stance.RETREAT) return true;
        return stance == Stance.WARY && target == provoker;
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
            provoker = shooter;
            threatPosition = shooter.position();
            decide(shooter);
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
        if (provoker != null && !isPresent(provoker)) provoker = null;
        if (provoker != null) threatPosition = provoker.position();

        switch (stance) {
            case CHARGE -> tickCharge();
            case RETREAT -> tickRetreat();
            case WARY -> tickWary();
            default -> {
            }
        }
    }

    public void reset() {
        stance = Stance.CALM;
        provoker = null;
        threatPosition = null;
        stanceTicks = 0;
        unreachableChecks = 0;
        reachableChecks = 0;
        provisional = false;
    }

    private void decide(LivingEntity shooter) {
        if (entity.isBaby() || !isAttackable(shooter)) {
            retreat();
            return;
        }

        switch (reach(shooter)) {
            case YES -> charge(shooter);
            case NO -> retreat();
            case FAR -> {
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
            case UNSURE -> {
                if (stance == Stance.CHARGE) charge(shooter);
                else retreat();
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
        }
        stance = Stance.CHARGE;
        entity.setNap(false);
        entity.setSitting(false);
        keepAngry(shooter);
        if (entity.getTarget() != shooter) entity.setTarget(shooter);
        if (entity.getTarget() != shooter) retreat();
    }

    private void retreat() {
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
        if (provoker == null || sinceProvoked > CHARGE_MEMORY
                || entity.distanceToSqr(provoker) > CHARGE_GIVE_UP_DISTANCE * CHARGE_GIVE_UP_DISTANCE) {
            reset();
            return;
        }
        if (!isAttackable(provoker)) {
            retreat();
            return;
        }

        keepAngry(provoker);
        if (--reevaluateTimer > 0) return;
        reevaluateTimer = provisional ? WATCH_INTERVAL : REEVALUATE_INTERVAL;

        Reach reach = reach(provoker);
        if (reach == Reach.YES) {
            unreachableChecks = 0;
            provisional = false;
        } else if (reach == Reach.NO && (provisional || ++unreachableChecks >= UNREACHABLE_CHECKS)) {
            retreat();
        }
    }

    private void tickRetreat() {
        calmDown();
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
        watchForOpening();
    }

    private boolean watchForOpening() {
        if (provoker == null || --reevaluateTimer > 0) return false;
        reevaluateTimer = WATCH_INTERVAL;

        boolean opening = !entity.isBaby() && isAttackable(provoker)
                && entity.distanceToSqr(provoker) <= CHARGE_GIVE_UP_DISTANCE * CHARGE_GIVE_UP_DISTANCE
                && reach(provoker) == Reach.YES;
        reachableChecks = opening ? reachableChecks + 1 : 0;
        if (reachableChecks < REACHABLE_CHECKS) return false;

        sinceProvoked = 0;
        charge(provoker);
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
        if (withinStrike(entity.position(), target)) return Reach.YES;
        if (!entity.onGround() && !entity.isInWater() && !entity.isInLava()) return Reach.UNSURE;

        float range = (float) entity.getAttributeValue(Attributes.FOLLOW_RANGE);
        Path path = probePath(target, range);
        if (path == null) return Reach.NO;

        Node end = path.getEndNode();
        if (end == null) return Reach.UNSURE;

        Vec3 standing = Vec3.atBottomCenterOf(end.asBlockPos());
        if (withinStrike(standing, target)) return Reach.YES;

        boolean stoppedAtFrontier = standing.distanceTo(entity.position()) >= range - SEARCH_FRONTIER_MARGIN;
        return stoppedAtFrontier ? Reach.FAR : Reach.NO;
    }

    @Nullable
    private Path probePath(LivingEntity target, float range) {
        BlockPos origin = entity.blockPosition();
        int radius = (int) range + PROBE_REGION_MARGIN;
        PathNavigationRegion region = new PathNavigationRegion(entity.level(),
                origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius));
        PathFinder finder = new PathFinder(entity.getNavigation().getNodeEvaluator(), Mth.floor(range * 16.0f));
        return finder.findPath(region, entity, Set.of(groundUnder(target)), range, 0, 1.0f);
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
        return gap <= reach && rise <= maxRise && rise > -(entity.getBbHeight() + reach);
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
