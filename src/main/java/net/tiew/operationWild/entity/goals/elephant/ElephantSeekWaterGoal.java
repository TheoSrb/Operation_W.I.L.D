package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantSeekWaterGoal extends Goal {

    private static final int SEARCH_RADIUS = 32;
    private static final int SEARCH_COOLDOWN = 600;
    private static final double HERD_CALL_RANGE = 48.0;
    private static final int DRINK_FILL = 120;
    private static final int DRINK_TICKS = 36;
    private static final int DRINK_COST = 90;
    private static final int DRINK_CYCLES = 2;
    private static final int SHOWER_FILL = 200;
    private static final int SHOWER_TICKS = 44;
    private static final int SHOWER_SOAK_TICK = 10;
    private static final int SHOWER_COST_PER_TICK = 2;
    private static final int TOP_UP_FILL = 220;
    private static final int GIVE_UP_TICKS = 2400;
    private static final double WALK_SPEED = 1.0;
    private static final int LEAVE_DISTANCE = 6;
    private static final int LEAVE_SEARCH_RADIUS = 9;
    private static final int LEAVE_VERTICAL = 3;
    private static final double LEAVE_MIN_GAIN = 3.0;
    private static final double LEAVE_ARRIVE = 1.5;
    private static final int LEAVE_TIMEOUT = 160;
    private static final int LEAVE_STALL_TICKS = 30;
    private static final int LEAVE_REPATH_INTERVAL = 20;

    private enum Phase { FETCH_DRINK, DRINK, FETCH_SHOWER, SHOWER, TOP_UP, LEAVE, DONE }

    private final ElephantEntity elephant;
    @Nullable
    private ElephantGoals.WaterSpot spot;
    @Nullable
    private ElephantWaterRun run;
    @Nullable
    private Vec3 retreat;
    private boolean retreatFallback;
    private Phase phase = Phase.DONE;
    private long searchUntil;
    private int timer;
    private int totalTicks;
    private int cycles;
    private boolean failed;

    public ElephantSeekWaterGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!ElephantGoals.isCalm(elephant) || !elephant.wild.isThirsty()) return false;
        if (elephant.level().getGameTime() < searchUntil) return false;

        ElephantEntity matriarch = ElephantHerd.findMatriarch(elephant);
        BlockPos center = matriarch != null && matriarch != elephant ? matriarch.blockPosition() : elephant.blockPosition();
        spot = ElephantGoals.findWaterSpot(elephant, center, SEARCH_RADIUS);
        if (spot == null) {
            searchUntil = elephant.level().getGameTime() + SEARCH_COOLDOWN;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.DONE && totalTicks < GIVE_UP_TICKS && ElephantGoals.isCalm(elephant);
    }

    @Override
    public void start() {
        timer = 0;
        totalTicks = 0;
        cycles = 0;
        failed = false;
        phase = Phase.FETCH_DRINK;
        run = new ElephantWaterRun(elephant, spot, DRINK_FILL, WALK_SPEED);

        if (elephant.isMatriarch()) {
            for (ElephantEntity mate : ElephantHerd.herdmates(elephant, HERD_CALL_RANGE)) mate.wild.makeThirsty();
        }
    }

    @Override
    public void stop() {
        elephant.stopAiTrunk();
        elephant.getNavigation().stop();
        if (phase == Phase.DONE && !failed) elephant.wild.quench();
        if (failed) searchUntil = elephant.level().getGameTime() + SEARCH_COOLDOWN;
        phase = Phase.DONE;
        run = null;
        spot = null;
        retreat = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        totalTicks++;
        timer++;
        switch (phase) {
            case FETCH_DRINK -> fetch(Phase.DRINK);
            case DRINK -> tickDrink();
            case FETCH_SHOWER -> fetch(Phase.SHOWER);
            case SHOWER -> tickShower();
            case TOP_UP -> fetch(Phase.LEAVE);
            case LEAVE -> tickLeave();
            default -> {
            }
        }
    }

    private void fetch(Phase next) {
        if (run == null) {
            phase = Phase.DONE;
            return;
        }
        switch (run.tick()) {
            case FILLED -> {
                run = null;
                timer = 0;
                if (next == Phase.LEAVE) startLeaving();
                else phase = next;
            }
            case FAILED -> {
                run = null;
                failed = cycles == 0;
                if (failed) phase = Phase.DONE;
                else startLeaving();
            }
            default -> {
            }
        }
    }

    private void startLeaving() {
        elephant.stopAiTrunk();
        elephant.wild.quench();
        timer = 0;
        retreat = retreatPoint();
        phase = retreat != null ? Phase.LEAVE : Phase.DONE;
    }

    @Nullable
    private Vec3 retreatPoint() {
        if (spot == null) return null;
        retreatFallback = false;
        Vec3 direction = elephant.position().subtract(spot.waterCenter());
        direction = new Vec3(direction.x, 0.0, direction.z);
        if (direction.lengthSqr() < 1.0E-4) direction = Vec3.directionFromRotation(0f, elephant.yBodyRot + 180f);
        return elephant.position().add(direction.normalize().scale(LEAVE_DISTANCE));
    }

    @Nullable
    private Vec3 fallbackRetreat() {
        if (spot == null) return null;
        Vec3 water = spot.waterCenter();
        Vec3 away = DefaultRandomPos.getPosAway(elephant, LEAVE_SEARCH_RADIUS, LEAVE_VERTICAL, water);
        if (away == null || away.distanceTo(water) < elephant.position().distanceTo(water) + LEAVE_MIN_GAIN) return null;
        return away;
    }

    private void tickLeave() {
        if (retreat == null || timer > LEAVE_TIMEOUT
                || ElephantGoals.horizontalDistance(elephant, retreat) <= LEAVE_ARRIVE
                || timer > LEAVE_STALL_TICKS && elephant.getNavigation().isDone()) {
            phase = Phase.DONE;
            return;
        }
        if (timer % LEAVE_REPATH_INTERVAL != 1) return;
        if (elephant.getNavigation().moveTo(retreat.x, retreat.y, retreat.z, WALK_SPEED)) return;
        if (retreatFallback) {
            phase = Phase.DONE;
            return;
        }
        retreatFallback = true;
        retreat = fallbackRetreat();
        if (retreat != null) elephant.getNavigation().moveTo(retreat.x, retreat.y, retreat.z, WALK_SPEED);
    }

    private void tickDrink() {
        elephant.getNavigation().stop();
        if (!elephant.startAiTrunk(ElephantEntity.TRUNK_DRINKING)) {
            phase = Phase.DONE;
            return;
        }
        elephant.aimAiTrunk(elephant.yBodyRot, 22f);
        if (timer == 10 || timer == 22) {
            elephant.level().playSound(null, elephant.getX(), elephant.getY() + elephant.getBbHeight() * 0.6, elephant.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0f, 0.55f + elephant.getRandom().nextFloat() * 0.1f);
        }
        if (timer < DRINK_TICKS) return;

        elephant.setTrunkWater(elephant.getTrunkWater() - DRINK_COST);
        elephant.stopAiTrunk();
        cycles++;
        timer = 0;
        if (cycles < DRINK_CYCLES) {
            phase = Phase.FETCH_DRINK;
            run = new ElephantWaterRun(elephant, spot, DRINK_FILL, WALK_SPEED);
        } else {
            phase = Phase.FETCH_SHOWER;
            run = new ElephantWaterRun(elephant, spot, SHOWER_FILL, WALK_SPEED);
        }
    }

    private void tickShower() {
        elephant.getNavigation().stop();
        if (!elephant.startAiTrunk(ElephantEntity.TRUNK_SHOWERING)) {
            phase = Phase.DONE;
            return;
        }
        elephant.aimAiTrunk(elephant.yBodyRot, -75f);
        elephant.setTrunkWater(elephant.getTrunkWater() - SHOWER_COST_PER_TICK);
        if (timer % 6 == 1) {
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 0.5f, 1.15f + elephant.getRandom().nextFloat() * 0.3f);
        }
        if (timer == SHOWER_SOAK_TICK) elephant.wild.soak();
        if (timer < SHOWER_TICKS && elephant.getTrunkWater() > 0) return;

        elephant.stopAiTrunk();
        timer = 0;
        phase = Phase.TOP_UP;
        run = new ElephantWaterRun(elephant, spot, TOP_UP_FILL, WALK_SPEED);
    }
}
