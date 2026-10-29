package net.tiew.operationWild.entity.goals.global;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
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
    private static final double LEG_CHAIN_DISTANCE_SQR = 64.0;
    private static final double HEADING_REALIGN = 0.25;
    private static final int CORNER_PACE_MIN = 80;
    private static final int CORNER_PACE_MAX = 160;
    private static final int CORNER_RESCAN = 40;
    private static final float CORNER_WHIMPER_CHANCE = 0.008f;
    private static final double CORNER_SPEED_FACTOR = 0.55;
    private static final int SCOUT_GAP = 10;
    private static final double STEER_DISTANCE = 8.0;
    private static final int WATER_RESCAN = 40;
    private static final int WATER_EXIT_MEMORY = 400;

    private final OWEntity mob;
    private final int radius;
    private final int height;
    private final OWEvasion evasion;
    private final List<Failed> failed = new ArrayList<>();
    @Nullable
    private Vec3 escape;
    @Nullable
    private Vec3 heading;
    private Vec3 anchor = Vec3.ZERO;
    private boolean rescout;
    private int scoutCooldown;
    private int stuckTimer;
    private int stuckStrikes;
    private boolean cornered;
    @Nullable
    private BlockPos refuge;
    private int paceTimer;
    private int rescanTimer;
    @Nullable
    private Vec3 lastDanger;
    @Nullable
    private Vec3 water;
    private int waterScanTimer;
    private int waterExitExpiry = Integer.MIN_VALUE;
    private boolean wasSwimming;

    public OWFleeRoute(OWEntity mob, int radius, int height) {
        this.mob = mob;
        this.radius = radius;
        this.height = height;
        this.evasion = new OWEvasion(mob);
    }

    public boolean prepare(@Nullable Vec3 danger) {
        cornered = false;
        refuge = null;
        lastDanger = danger;
        heading = awayFrom(danger);
        water = seeksWater() ? OWWaterRefuge.find(mob, danger, this::isRejectedWater) : null;
        waterScanTimer = WATER_RESCAN;
        escape = water != null ? water : scout(danger);
        if (escape != null) return true;
        return enterCorner(danger) || danger != null;
    }

    public void begin(double speed) {
        anchor = mob.position();
        stuckTimer = 0;
        stuckStrikes = 0;
        rescout = false;
        scoutCooldown = SCOUT_GAP;
        evasion.reset();
        if (cornered) moveToRefuge(speed);
        else if (escape != null) moveToEscape(speed);
        else steerAway(lastDanger, speed);
    }

    public void stop() {
        escape = null;
        water = null;
        refuge = null;
        cornered = false;
        mob.getNavigation().stop();
    }

    public boolean isHolding() {
        return cornered && mob.getNavigation().isDone();
    }

    public boolean isCornered() {
        return cornered;
    }

    public void tick(@Nullable Vec3 danger, @Nullable Entity threat, double speed, float drowsiness) {
        forgetExpired();
        trackWaterExit();
        if (danger != null) lastDanger = danger;
        evasion.tick(threat, drowsiness);
        if (cornered) {
            tickCorner(danger, threat, speed);
            return;
        }
        tickHeading(danger);
        tickWater(danger);

        if (++stuckTimer >= STUCK_WINDOW) {
            boolean moving = !mob.getNavigation().isDone();
            boolean progressed = mob.position().distanceToSqr(anchor) >= STUCK_PROGRESS_SQR;
            stuckTimer = 0;
            anchor = mob.position();
            if (!progressed && (moving || !arrived(escape))) {
                if (escape != null) remember(escape);
                if (water != null && isFailed(water)) water = null;
                stuckStrikes++;
                rescout = true;
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
            if (mob.getNavigation().isDone()) steerAway(lastDanger, speed);
        }

        if (scoutCooldown > 0) scoutCooldown--;
        boolean needed = rescout || escape == null || mob.getNavigation().isDone() || nearing(escape) || compromised(escape, danger);
        if (scoutCooldown <= 0 && needed) {
            scoutCooldown = SCOUT_GAP;
            rescout = false;
            Vec3 next = water != null ? water : scout(danger);
            if (next != null && (escape == null || !nearing(next) || arrived(escape))) escape = next;
            else if (next == null && arrived(escape)) escape = null;
            moveToEscape(speed);
        }
    }

    @Nullable
    private Vec3 scout(@Nullable Vec3 danger) {
        return OWEscapeScout.find(mob, danger, heading, radius, height, this::isFailed);
    }

    private void tickHeading(@Nullable Vec3 danger) {
        if (danger == null) return;
        Vec3 away = awayFrom(danger);
        if (heading == null || heading.dot(away) < HEADING_REALIGN || compromised(escape, danger)) heading = away;
    }

    private Vec3 awayFrom(@Nullable Vec3 danger) {
        Vec3 away = danger != null ? new Vec3(mob.getX() - danger.x, 0.0, mob.getZ() - danger.z) : Vec3.ZERO;
        if (away.lengthSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, mob.getYRot()).multiply(1.0, 0.0, 1.0);
        return away.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : away.normalize();
    }

    private boolean seeksWater() {
        return mob instanceof OWSemiWaterEntity && !mob.isInWater();
    }

    private void tickWater(@Nullable Vec3 danger) {
        if (!seeksWater()) {
            water = null;
            return;
        }
        if (water != null && (isRejectedWater(water) || compromised(water, danger))) {
            water = null;
            waterScanTimer = 0;
        }
        if (water != null && arrived(water) && mob.getNavigation().isDone()) {
            mob.getMoveControl().setWantedPosition(water.x, water.y, water.z, mob.sedatedFleeSpeed());
            return;
        }
        if (water != null || --waterScanTimer > 0) return;
        waterScanTimer = WATER_RESCAN;
        water = OWWaterRefuge.find(mob, danger, this::isRejectedWater);
        if (water != null && !water.equals(escape)) {
            escape = water;
            rescout = true;
            scoutCooldown = 0;
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
                rescout = true;
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
        if (escape == null) return;
        mob.getNavigation().moveTo(escape.x, escape.y, escape.z, speed);
    }

    private void steerAway(@Nullable Vec3 danger, double speed) {
        Vec3 away = danger != null ? awayFrom(danger) : heading;
        if (away == null) return;
        away = away.scale(STEER_DISTANCE);
        mob.getMoveControl().setWantedPosition(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, speed);
    }

    private void moveToRefuge(double speed) {
        if (refuge == null) return;
        mob.getNavigation().moveTo(refuge.getX() + 0.5, refuge.getY(), refuge.getZ() + 0.5, speed * CORNER_SPEED_FACTOR);
    }

    private boolean arrived(@Nullable Vec3 target) {
        return target != null && mob.position().distanceToSqr(target) <= ARRIVAL_DISTANCE_SQR;
    }

    private boolean nearing(@Nullable Vec3 target) {
        return target != null && mob.position().distanceToSqr(target) <= LEG_CHAIN_DISTANCE_SQR;
    }

    private boolean compromised(@Nullable Vec3 target, @Nullable Vec3 danger) {
        if (target == null || danger == null) return false;
        return target.distanceToSqr(danger) < target.distanceToSqr(mob.position());
    }

    private void remember(Vec3 position) {
        failed.add(new Failed(position, mob.tickCount + FAILED_MEMORY));
    }

    private void trackWaterExit() {
        if (mob.isInWater()) {
            if (!(mob instanceof OWSemiWaterEntity semi && semi.isWading())) wasSwimming = true;
            return;
        }
        if (wasSwimming) waterExitExpiry = mob.tickCount + WATER_EXIT_MEMORY;
        wasSwimming = false;
    }

    private boolean isRejectedWater(Vec3 position) {
        return isFailed(position) || mob.tickCount < waterExitExpiry;
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
