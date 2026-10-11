package net.tiew.operationWild.entity.goals.global;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.breakout.OWEnclosureProbe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OWFleeRoute {

    private static final int STUCK_WINDOW = 40;
    private static final double STUCK_PROGRESS_SQR = 1.0;
    private static final int STUCK_STRIKES_BEFORE_PROBE = 2;
    private static final int FAILED_MEMORY = 200;
    private static final double FAILED_RADIUS_SQR = 16.0;
    private static final double ARRIVAL_DISTANCE_SQR = 16.0;
    private static final int CORNER_PACE_MIN = 80;
    private static final int CORNER_PACE_MAX = 160;
    private static final int CORNER_RESCAN = 40;
    private static final float CORNER_WHIMPER_CHANCE = 0.008f;
    private static final double CORNER_SPEED_FACTOR = 0.55;
    private static final int SCOUT_GAP = 10;

    private final OWEntity mob;
    private final int radius;
    private final int height;
    private final int repathInterval;
    private final OWEvasion evasion;
    private final List<Failed> failed = new ArrayList<>();
    @Nullable
    private Vec3 escape;
    private Vec3 anchor = Vec3.ZERO;
    private int repathTimer;
    private int scoutCooldown;
    private int stuckTimer;
    private int stuckStrikes;
    private boolean cornered;
    @Nullable
    private BlockPos refuge;
    private int paceTimer;
    private int rescanTimer;

    public OWFleeRoute(OWEntity mob, int radius, int height, int repathInterval) {
        this.mob = mob;
        this.radius = radius;
        this.height = height;
        this.repathInterval = repathInterval;
        this.evasion = new OWEvasion(mob);
    }

    public boolean prepare(@Nullable Vec3 danger) {
        cornered = false;
        refuge = null;
        escape = OWEscapeScout.find(mob, danger, radius, height, this::isFailed);
        if (escape != null) return true;
        return enterCorner(danger);
    }

    public void begin(double speed) {
        anchor = mob.position();
        stuckTimer = 0;
        stuckStrikes = 0;
        scoutCooldown = SCOUT_GAP;
        evasion.reset();
        if (cornered) moveToRefuge(speed);
        else moveToEscape(speed);
    }

    public void stop() {
        escape = null;
        refuge = null;
        cornered = false;
        mob.getNavigation().stop();
    }

    public boolean isHolding() {
        return cornered && mob.getNavigation().isDone();
    }

    public void tick(@Nullable Vec3 danger, @Nullable Entity threat, double speed, float drowsiness) {
        forgetExpired();
        evasion.tick(threat, drowsiness);
        if (cornered) {
            tickCorner(danger, threat, speed);
            return;
        }

        if (++stuckTimer >= STUCK_WINDOW) {
            boolean moving = !mob.getNavigation().isDone();
            boolean progressed = mob.position().distanceToSqr(anchor) >= STUCK_PROGRESS_SQR;
            stuckTimer = 0;
            anchor = mob.position();
            if (moving && !progressed) {
                if (escape != null) remember(escape);
                stuckStrikes++;
                repathTimer = 0;
            } else if (progressed) {
                stuckStrikes = 0;
            }
        }
        if (stuckStrikes >= STUCK_STRIKES_BEFORE_PROBE || escape == null) {
            stuckStrikes = 0;
            if (enterCorner(danger)) {
                moveToRefuge(speed);
                return;
            }
        }

        if (scoutCooldown > 0) scoutCooldown--;
        boolean due = --repathTimer <= 0;
        boolean needed = escape == null || mob.getNavigation().isDone() || arrived(escape) || compromised(escape, danger);
        if (scoutCooldown <= 0 && (due || needed)) {
            scoutCooldown = SCOUT_GAP;
            Vec3 next = OWEscapeScout.find(mob, danger, radius, height, this::isFailed);
            if (next != null) escape = next;
            moveToEscape(speed);
        }
    }

    private boolean enterCorner(@Nullable Vec3 danger) {
        OWEnclosureProbe.Result enclosure = mob.breakout.probe().scan(danger);
        if (!enclosure.enclosed() || enclosure.refuges().isEmpty()) return false;
        cornered = true;
        escape = null;
        refuge = enclosure.refuges().get(0);
        paceTimer = CORNER_PACE_MIN + mob.getRandom().nextInt(CORNER_PACE_MAX - CORNER_PACE_MIN + 1);
        rescanTimer = CORNER_RESCAN;
        return true;
    }

    private void tickCorner(@Nullable Vec3 danger, @Nullable Entity threat, double speed) {
        if (--rescanTimer <= 0) {
            rescanTimer = CORNER_RESCAN;
            OWEnclosureProbe.Result enclosure = mob.breakout.probe().scan(danger);
            if (!enclosure.enclosed()) {
                cornered = false;
                refuge = null;
                repathTimer = 0;
                return;
            }
        }

        if (mob.getNavigation().isDone()) {
            if (threat != null) mob.getLookControl().setLookAt(threat, 30f, 30f);
            if (--paceTimer <= 0) {
                List<BlockPos> refuges = mob.breakout.probe().last().refuges();
                if (!refuges.isEmpty()) refuge = refuges.get(mob.getRandom().nextInt(refuges.size()));
                paceTimer = CORNER_PACE_MIN + mob.getRandom().nextInt(CORNER_PACE_MAX - CORNER_PACE_MIN + 1);
                moveToRefuge(speed);
            }
        }
        if (mob.getRandom().nextFloat() < CORNER_WHIMPER_CHANCE) {
            mob.playSedationVoice(1.25f + mob.getRandom().nextFloat() * 0.15f);
        }
    }

    private void moveToEscape(double speed) {
        repathTimer = repathInterval + mob.getRandom().nextInt(10);
        if (escape == null) return;
        mob.getNavigation().moveTo(escape.x, escape.y, escape.z, speed);
    }

    private void moveToRefuge(double speed) {
        if (refuge == null) return;
        mob.getNavigation().moveTo(refuge.getX() + 0.5, refuge.getY(), refuge.getZ() + 0.5, speed * CORNER_SPEED_FACTOR);
    }

    private boolean arrived(@Nullable Vec3 target) {
        return target != null && mob.position().distanceToSqr(target) <= ARRIVAL_DISTANCE_SQR;
    }

    private boolean compromised(@Nullable Vec3 target, @Nullable Vec3 danger) {
        if (target == null || danger == null) return false;
        return target.distanceToSqr(danger) < target.distanceToSqr(mob.position());
    }

    private void remember(Vec3 position) {
        failed.add(new Failed(position, mob.tickCount + FAILED_MEMORY));
    }

    private boolean isFailed(Vec3 position) {
        for (Failed entry : failed) {
            if (entry.position().distanceToSqr(position) <= FAILED_RADIUS_SQR) return true;
        }
        return false;
    }

    private void forgetExpired() {
        failed.removeIf(entry -> entry.expiry() <= mob.tickCount);
    }

    private record Failed(Vec3 position, int expiry) {
    }
}
