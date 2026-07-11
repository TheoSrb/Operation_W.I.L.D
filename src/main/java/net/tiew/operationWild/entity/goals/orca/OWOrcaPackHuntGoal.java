package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.behavior.OrcaBehaviorHandler;

import java.util.EnumSet;
import java.util.List;

/**
 * Wild-Orca pack-hunting goal.
 *
 * <p>Each orca runs its own instance. Coordination is delegated to the leader's
 * {@link OrcaBehaviorHandler}: an orca only charges when it is the leader's current attacker.
 * The per-orca state machine is WAIT → ENGAGE → RETREAT:
 * <ul>
 *   <li><b>WAIT</b> — hold at {@link OrcaBehaviorHandler#SAFE_DISTANCE} from the shared target.</li>
 *   <li><b>ENGAGE</b> — swim in, land a single melee combo, then hold facing during the strike window.</li>
 *   <li><b>RETREAT</b> — back off to a safe distance, then hand the turn to the next member.</li>
 * </ul>
 *
 * <p>Leader loss is handled by re-formation: an orphaned member (dead / invalid leader) drops
 * back to pack-less and the standard formation path rebuilds a fresh pack on the next tick, so
 * there is a single code path for building a pack.
 *
 * <p>Registered above the solo attack goal so packs take precedence; a lone orca with no
 * recruitable packmates fails {@code tryFormPack} and falls back to solo combat.
 */
public class OWOrcaPackHuntGoal extends Goal {

    private enum SubState { WAIT, ENGAGE, RETREAT }

    /** Distance at which the melee combo is triggered. */
    private static final double ATTACK_RANGE   = 4.5;
    /** Ticks allowed to reach the target before the attacker yields its turn. */
    private static final int    CHARGE_TIMEOUT = 60;
    /** Ticks the attacker holds facing after triggering the combo, so the hit lands. */
    private static final int    STRIKE_WINDOW  = 24;
    /** Safety cap on the retreat phase. */
    private static final int    RETREAT_TIMEOUT = 60;
    /** Angular step (radians per tick) at which a waiting orca orbits the target. */
    private static final double ORBIT_STEP     = 0.14;

    private final OrcaEntity orca;

    private SubState sub = SubState.WAIT;
    private boolean hasHitThisTurn = false;
    private int engageTicks = 0;
    private int strikeTicks = 0;
    private int retreatTicks = 0;
    /** Orbit direction while waiting (+1 / -1), randomised per engagement for variety. */
    private int orbitDir = 1;

    public OWOrcaPackHuntGoal(OrcaEntity orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        if (!isEligible()) return false;

        int role = orca.getPackRole();
        if (role != OrcaBehaviorHandler.PACK_ROLE_NONE) {
            // Already in a pack: keep participating while the leader is valid,
            // otherwise release ourselves so the pack can re-form cleanly.
            if (isLeaderValid()) return true;
            leavePack();
            return false;
        }

        // Pack-less: find prey and try to become a leader (needs >= 1 recruitable packmate).
        LivingEntity target = findHuntTarget();
        if (target == null) return false;
        return orca.getOrcaBehaviorHandler().tryFormPack(target);
    }

    @Override
    public boolean canContinueToUse() {
        if (!isEligible()) return false;
        if (orca.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_NONE) return false;
        if (!isLeaderValid()) return false;

        OrcaBehaviorHandler lh = leaderHandler();
        LivingEntity target = lh == null ? null : lh.getPackTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        sub = SubState.WAIT;
        hasHitThisTurn = false;
        engageTicks = 0;
        strikeTicks = 0;
        retreatTicks = 0;
        orbitDir = orca.getRandom().nextBoolean() ? 1 : -1;
    }

    @Override
    public void stop() {
        leavePack();
        orca.setAggressive(false);
        orca.setRunning(false);
        orca.getNavigation().stop();
        sub = SubState.WAIT;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // ── Main tick ────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        if (orca.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_NONE) return;

        // The leader drives coordination once per tick (prune / validate / stagger).
        if (orca.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_LEADER) {
            orca.getOrcaBehaviorHandler().tickLeader();
            if (orca.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_NONE) return; // disbanded this tick
        }

        OrcaBehaviorHandler lh = leaderHandler();
        if (lh == null) return;
        LivingEntity target = lh.getPackTarget();
        if (target == null || !target.isAlive()) return;

        // Facing is applied per sub-state: waiting orcas orient along their swim path
        // (so they circle instead of spinning in place); the attacker faces the target.
        boolean myTurn = lh.getCurrentAttacker() == orca && lh.getStaggerTicks() <= 0;

        switch (sub) {
            case WAIT -> {
                holdPosition(target);
                if (myTurn) {
                    sub = SubState.ENGAGE;
                    hasHitThisTurn = false;
                    engageTicks = 0;
                    strikeTicks = 0;
                }
            }
            case ENGAGE -> tickEngage(target);
            case RETREAT -> tickRetreat(target, lh);
        }
    }

    // ── WAIT ─────────────────────────────────────────────────────────────────

    /** Orbit the target at a safe distance, circling instead of holding a fixed point. */
    private void holdPosition(LivingEntity target) {
        if (orca.getTarget() != null) orca.forceSetTarget(null);
        orca.setAggressive(false);
        orca.setRunning(false);

        // Current bearing of the orca around the target, advanced by one angular step so the
        // navigation target always sits slightly ahead on the circle → continuous circling.
        double dx = orca.getX() - target.getX();
        double dz = orca.getZ() - target.getZ();
        double angle = Math.atan2(dz, dx) + orbitDir * ORBIT_STEP;

        double radius = OrcaBehaviorHandler.SAFE_DISTANCE;
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double y = target.getY();

        orca.getNavigation().moveTo(x, y, z, orca.getSwimSpeed() * 0.7);
    }

    // ── ENGAGE ───────────────────────────────────────────────────────────────

    private void tickEngage(LivingEntity target) {
        engageTicks++;
        orca.setTarget(target);
        orca.setAggressive(true);
        orca.getLookControl().setLookAt(target, 30f, 30f);

        if (!hasHitThisTurn) {
            orca.setRunning(true);
            orca.getNavigation().moveTo(target, orca.getSwimSpeed());

            if (orca.distanceTo(target) <= ATTACK_RANGE) {
                // Single melee combo — same trigger as OWAttackGoal / OWOrcaBeachingGoal.
                if (!orca.isCombo()) {
                    orca.setCombo(true, 1);
                } else if (orca.isPauseCombo()) {
                    orca.playerContinueCombo = true;
                }
                hasHitThisTurn = true;
                strikeTicks = 0;
            } else if (engageTicks > CHARGE_TIMEOUT) {
                // Could not reach the target in time → yield the turn.
                beginRetreat();
            }
        } else {
            // Strike window: hold position and keep facing so the combo damage lands
            // (the combo hurts the AABB in front at attackTimer == timeToHit).
            orca.setRunning(false);
            orca.getNavigation().stop();
            strikeTicks++;
            if (!orca.isCombo() || strikeTicks > STRIKE_WINDOW) {
                beginRetreat();
            }
        }
    }

    private void beginRetreat() {
        sub = SubState.RETREAT;
        retreatTicks = 0;
    }

    // ── RETREAT ──────────────────────────────────────────────────────────────

    private void tickRetreat(LivingEntity target, OrcaBehaviorHandler lh) {
        retreatTicks++;
        if (orca.getTarget() != null) orca.forceSetTarget(null);
        orca.setAggressive(false);
        orca.setRunning(true);

        Vec3 point = safePoint(target, OrcaBehaviorHandler.SAFE_DISTANCE + 1.0);
        orca.getNavigation().moveTo(point.x, point.y, point.z, orca.getSwimSpeed());

        if (orca.distanceTo(target) >= OrcaBehaviorHandler.SAFE_DISTANCE || retreatTicks > RETREAT_TIMEOUT) {
            lh.advanceRotation(); // hand the turn to the next member
            orca.setRunning(false);
            sub = SubState.WAIT;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** A point at {@code distance} from the target, along the direction target → orca. */
    private Vec3 safePoint(LivingEntity target, double distance) {
        Vec3 away = orca.position().subtract(target.position());
        if (away.lengthSqr() < 1.0e-4) away = new Vec3(1.0, 0.0, 0.0);
        return target.position().add(away.normalize().scale(distance));
    }

    private boolean isEligible() {
        return !orca.isTame()
                && !orca.isBaby()
                && !orca.isNapping()
                && !orca.isSitting()
                && !orca.isVehicle()
                && orca.isInWater();
    }

    private boolean isLeaderValid() {
        OrcaEntity leader = orca.getPackLeader();
        return leader != null
                && leader.isAlive()
                && !leader.isTame()
                && leader.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_LEADER;
    }

    private OrcaBehaviorHandler leaderHandler() {
        OrcaEntity leader = orca.getPackLeader();
        return leader == null ? null : leader.getOrcaBehaviorHandler();
    }

    /** Leave the pack: the leader dissolves it entirely, a follower just detaches itself. */
    private void leavePack() {
        if (orca.getPackRole() == OrcaBehaviorHandler.PACK_ROLE_LEADER) {
            orca.getOrcaBehaviorHandler().disband();
        } else if (orca.getPackRole() != OrcaBehaviorHandler.PACK_ROLE_NONE) {
            orca.setPackRole(OrcaBehaviorHandler.PACK_ROLE_NONE);
            orca.setPackLeader(null);
            orca.forceSetTarget(null);
        }
    }

    private LivingEntity findHuntTarget() {
        List<LivingEntity> candidates = orca.level().getEntitiesOfClass(
                LivingEntity.class,
                orca.getBoundingBox().inflate(OrcaBehaviorHandler.PACK_DETECTION_RADIUS),
                e -> OrcaBehaviorHandler.isHuntableTarget(e, orca)
        );
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : candidates) {
            double d = orca.distanceTo(e);
            if (d < best) {
                best = d;
                nearest = e;
            }
        }
        return nearest;
    }
}
