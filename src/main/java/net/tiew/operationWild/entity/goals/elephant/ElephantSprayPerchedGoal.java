package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.navigation.OWPathProbe;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantSprayPerchedGoal extends Goal {

    private static final int EVALUATE_INTERVAL = 20;
    private static final int REQUIRED_STREAK = 2;
    private static final int MIN_WATER = 20;
    private static final int FETCH_RADIUS = 24;
    private static final int FILL_TO = 240;
    private static final double SPRAY_MAX_RANGE = 22.0;
    private static final double SPRAY_MIN_RANGE = 6.0;
    private static final double BACK_OFF_RANGE = 9.0;
    private static final int BURST_TICKS = 36;
    private static final int PAUSE_TICKS = 14;
    private static final int GIVE_UP_TICKS = 600;
    private static final int COOLDOWN_TICKS = 300;
    private static final float PROBE_RANGE = 28f;
    private static final float FACE_TOLERANCE = 20f;
    private static final double PERCH_HORIZONTAL = 6.0;
    private static final double STRIKE_MARGIN = 2.5;
    private static final double APPROACH_SPEED = 1.2;

    private final ElephantEntity elephant;
    @Nullable
    private LivingEntity target;
    @Nullable
    private ElephantWaterRun waterRun;
    private int evaluateCooldown;
    private int streak;
    private long cooldownUntil;
    private int burstTimer;
    private int totalTicks;
    private boolean done;

    public ElephantSprayPerchedGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (elephant.level().getGameTime() < cooldownUntil) return false;
        if (!ElephantGoals.isFreeWild(elephant) || elephant.isCombo()) {
            streak = 0;
            return false;
        }
        LivingEntity aim = elephant.getTarget();
        if (aim == null || !aim.isAlive() || aim instanceof Player player && (player.isCreative() || player.isSpectator())) {
            streak = 0;
            return false;
        }
        if (--evaluateCooldown > 0) return false;
        evaluateCooldown = EVALUATE_INTERVAL;

        if (!isUnreachable(aim)) {
            streak = 0;
            return false;
        }
        if (++streak < REQUIRED_STREAK) return false;

        target = aim;
        waterRun = null;
        if (elephant.getTrunkWater() >= MIN_WATER) return true;
        ElephantGoals.WaterSpot spot = ElephantGoals.findWaterSpot(elephant, elephant.blockPosition(), FETCH_RADIUS);
        if (spot == null) {
            setCooldown(COOLDOWN_TICKS);
            streak = 0;
            return false;
        }
        waterRun = new ElephantWaterRun(elephant, spot, FILL_TO, APPROACH_SPEED + 0.4);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (done || totalTicks > GIVE_UP_TICKS || target == null || !target.isAlive()) return false;
        if (elephant.getTarget() != target || !ElephantGoals.isFreeWild(elephant)) return false;
        return !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    @Override
    public void start() {
        burstTimer = 0;
        totalTicks = 0;
        done = false;
    }

    @Override
    public void stop() {
        elephant.stopAiTrunk();
        elephant.getNavigation().stop();
        target = null;
        waterRun = null;
        streak = 0;
        if (done || totalTicks > GIVE_UP_TICKS) setCooldown(COOLDOWN_TICKS / 3);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (done) return;
        totalTicks++;
        if (target == null) return;

        if (waterRun != null) {
            switch (waterRun.tick()) {
                case FILLED -> waterRun = null;
                case FAILED -> {
                    done = true;
                    setCooldown(COOLDOWN_TICKS);
                }
                default -> {
                }
            }
            return;
        }

        if (totalTicks % EVALUATE_INTERVAL == 0 && !isUnreachable(target)) {
            done = true;
            return;
        }

        if (elephant.getTrunkWater() <= 0) {
            elephant.stopAiTrunk();
            ElephantGoals.WaterSpot spot = ElephantGoals.findWaterSpot(elephant, elephant.blockPosition(), FETCH_RADIUS);
            if (spot == null) {
                done = true;
                setCooldown(COOLDOWN_TICKS);
                return;
            }
            waterRun = new ElephantWaterRun(elephant, spot, FILL_TO, APPROACH_SPEED + 0.4);
            return;
        }

        Vec3 aim = target.position().add(0, target.getBbHeight() * 0.5, 0);
        double distance = ElephantGoals.horizontalDistance(elephant, aim);
        boolean visible = elephant.getSensing().hasLineOfSight(target);
        if (!visible || distance > SPRAY_MAX_RANGE) {
            elephant.stopAiTrunk();
            if (totalTicks % 10 == 1) elephant.getNavigation().moveTo(target, APPROACH_SPEED);
            return;
        }

        if (distance < SPRAY_MIN_RANGE) {
            elephant.stopAiTrunk();
            if (totalTicks % 10 == 1) ElephantGoals.backOff(elephant, aim, BACK_OFF_RANGE, APPROACH_SPEED);
            return;
        }

        elephant.getNavigation().stop();
        elephant.turnBodyTowards(aim, 5f);
        elephant.getLookControl().setLookAt(target, 30f, 30f);

        burstTimer = (burstTimer + 1) % (BURST_TICKS + PAUSE_TICKS);
        boolean bursting = burstTimer < BURST_TICKS;
        boolean aimed = ElephantGoals.yawGap(elephant, aim) <= FACE_TOLERANCE && elephant.aimAiSprayAt(aim);
        if (bursting && aimed) {
            elephant.startAiTrunk(ElephantEntity.TRUNK_SPRAYING);
        } else if (elephant.isSprayingWater()) {
            elephant.stopAiTrunk();
        }
    }

    private void setCooldown(int ticks) {
        cooldownUntil = Math.max(cooldownUntil, elephant.level().getGameTime() + ticks);
    }

    private boolean isUnreachable(LivingEntity aim) {
        double rise = aim.getY() - elephant.getY();
        double reachRise = elephant.getBbHeight() - 0.3;
        if (rise <= 1.0) return false;
        if (rise > reachRise && ElephantGoals.horizontalDistance(elephant, aim.position()) < PERCH_HORIZONTAL) return true;

        Path path = OWPathProbe.find(elephant, aim.blockPosition(), PROBE_RANGE, 1);
        if (path == null) return true;
        Node end = path.getEndNode();
        if (end == null) return true;
        Vec3 standing = Vec3.atBottomCenterOf(end.asBlockPos());
        double dx = aim.getX() - standing.x;
        double dz = aim.getZ() - standing.z;
        double gap = Math.sqrt(dx * dx + dz * dz) - aim.getBbWidth() * 0.5;
        return aim.getY() - standing.y > reachRise || gap > elephant.getBbWidth() * 0.5 + STRIKE_MARGIN;
    }
}
