package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantDouseFireGoal extends Goal {

    private static final int SCAN_INTERVAL = 20;
    private static final int FIRE_SCAN_INTERVAL = 80;
    private static final double MATE_RANGE = 14.0;
    private static final int FIRE_RADIUS = 6;
    private static final int FIRE_HEIGHT = 3;
    private static final int MIN_WATER = 12;
    private static final int SHOWER_TICKS = 30;
    private static final int SHOWER_DOUSE_TICK = 10;
    private static final int SHOWER_COST_PER_TICK = 1;
    private static final double SPRAY_RANGE = 12.0;
    private static final double SPRAY_MIN_RANGE = 5.0;
    private static final double BACK_OFF_RANGE = 7.0;
    private static final float SPRAY_FACE_TOLERANCE = 25f;
    private static final int FETCH_RADIUS = 20;
    private static final int FILL_TO = 160;
    private static final int BATHE_TIMEOUT = 220;
    private static final int GIVE_UP_TICKS = 600;
    private static final double APPROACH_SPEED = 1.3;

    private enum Mode { SHOWER, SPRAY, FETCH, BATHE }

    private final ElephantEntity elephant;
    @Nullable
    private LivingEntity burning;
    @Nullable
    private BlockPos fire;
    private Mode mode = Mode.SPRAY;
    @Nullable
    private ElephantWaterRun waterRun;
    @Nullable
    private ElephantGoals.WaterSpot bath;
    private int scanCooldown;
    private int fireScanCooldown;
    private int timer;
    private int totalTicks;
    private boolean done;

    public ElephantDouseFireGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!ElephantGoals.isFreeWild(elephant)) return false;
        boolean selfBurning = elephant.isOnFire() && !elephant.wild.isWet();
        if (!selfBurning && --scanCooldown > 0) return false;
        scanCooldown = SCAN_INTERVAL;

        burning = null;
        fire = null;
        boolean fighting = elephant.getTarget() != null;

        if (selfBurning) {
            if (elephant.getTrunkWater() >= MIN_WATER) {
                burning = elephant;
                return true;
            }
            if (fighting) return false;
            bath = ElephantGoals.findWaterSpot(elephant, elephant.blockPosition(), FETCH_RADIUS);
            if (bath == null) return false;
            burning = elephant;
            return true;
        }
        if (fighting || !ElephantGoals.isCalm(elephant)) return false;

        burning = burningHerdmate();
        if (burning == null && --fireScanCooldown <= 0) {
            fireScanCooldown = FIRE_SCAN_INTERVAL / SCAN_INTERVAL;
            fire = findFire();
        }
        if (burning == null && fire == null) return false;
        if (elephant.getTrunkWater() >= MIN_WATER) return true;
        bath = ElephantGoals.findWaterSpot(elephant, elephant.blockPosition(), FETCH_RADIUS);
        return bath != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (done || totalTicks > GIVE_UP_TICKS || !ElephantGoals.isFreeWild(elephant)) return false;
        if (burning == elephant) return elephant.isOnFire() || mode == Mode.SHOWER;
        if (elephant.getTarget() != null) return false;
        if (burning != null) return burning.isAlive() && burning.isOnFire();
        return fire != null && elephant.level().getBlockState(fire).is(BlockTags.FIRE);
    }

    @Override
    public void start() {
        timer = 0;
        totalTicks = 0;
        done = false;
        waterRun = null;
        if (burning == elephant) {
            mode = elephant.getTrunkWater() >= MIN_WATER ? Mode.SHOWER : Mode.BATHE;
        } else {
            mode = elephant.getTrunkWater() >= MIN_WATER ? Mode.SPRAY : Mode.FETCH;
        }
        if (mode == Mode.FETCH && bath != null) waterRun = new ElephantWaterRun(elephant, bath, FILL_TO, APPROACH_SPEED);
    }

    @Override
    public void stop() {
        elephant.stopAiTrunk();
        elephant.getNavigation().stop();
        burning = null;
        fire = null;
        bath = null;
        waterRun = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (done) return;
        timer++;
        totalTicks++;
        switch (mode) {
            case SHOWER -> tickShower();
            case SPRAY -> tickSpray();
            case FETCH -> tickFetch();
            case BATHE -> tickBathe();
        }
    }

    private void tickShower() {
        elephant.getNavigation().stop();
        if (!elephant.startAiTrunk(ElephantEntity.TRUNK_SHOWERING)) {
            done = true;
            return;
        }
        elephant.aimAiTrunk(elephant.yBodyRot, -75f);
        elephant.setTrunkWater(elephant.getTrunkWater() - SHOWER_COST_PER_TICK);
        if (timer % 6 == 1) {
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 0.6f, 1.2f + elephant.getRandom().nextFloat() * 0.3f);
        }
        if (timer == SHOWER_DOUSE_TICK) {
            elephant.clearFire();
            elephant.wild.soak();
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.8f, 1.4f);
        }
        if (timer >= SHOWER_TICKS || elephant.getTrunkWater() <= 0) {
            elephant.stopAiTrunk();
            done = true;
        }
    }

    private void tickSpray() {
        Vec3 point = sprayPoint();
        if (point == null) {
            done = true;
            return;
        }
        if (elephant.getTrunkWater() <= 0) {
            elephant.stopAiTrunk();
            bath = ElephantGoals.findWaterSpot(elephant, elephant.blockPosition(), FETCH_RADIUS);
            if (bath == null) {
                done = true;
                return;
            }
            waterRun = new ElephantWaterRun(elephant, bath, FILL_TO, APPROACH_SPEED);
            mode = Mode.FETCH;
            return;
        }

        double distance = ElephantGoals.horizontalDistance(elephant, point);
        boolean visible = burning != null ? elephant.getSensing().hasLineOfSight(burning) : distance <= SPRAY_RANGE;
        if (distance > SPRAY_RANGE || !visible) {
            elephant.stopAiTrunk();
            if (timer % 10 == 1) elephant.getNavigation().moveTo(point.x, point.y, point.z, APPROACH_SPEED);
            return;
        }

        if (distance < SPRAY_MIN_RANGE) {
            elephant.stopAiTrunk();
            if (timer % 10 == 1) ElephantGoals.backOff(elephant, point, BACK_OFF_RANGE, APPROACH_SPEED);
            return;
        }

        elephant.getNavigation().stop();
        elephant.turnBodyTowards(point, 6f);
        elephant.getLookControl().setLookAt(point);
        if (ElephantGoals.yawGap(elephant, point) <= SPRAY_FACE_TOLERANCE && elephant.aimAiSprayAt(point)) {
            elephant.startAiTrunk(ElephantEntity.TRUNK_SPRAYING);
        } else if (elephant.isSprayingWater()) {
            elephant.stopAiTrunk();
        }
    }

    private void tickFetch() {
        if (waterRun == null) {
            done = true;
            return;
        }
        switch (waterRun.tick()) {
            case FILLED -> {
                waterRun = null;
                timer = 0;
                mode = burning == elephant ? Mode.SHOWER : Mode.SPRAY;
            }
            case FAILED -> done = true;
            default -> {
            }
        }
    }

    private void tickBathe() {
        if (bath == null || !elephant.isOnFire() || elephant.isInWater() || timer > BATHE_TIMEOUT) {
            done = true;
            return;
        }
        if (timer % 10 == 1) {
            Vec3 water = bath.waterCenter();
            elephant.getNavigation().moveTo(water.x, water.y, water.z, APPROACH_SPEED + 0.4);
        }
    }

    @Nullable
    private Vec3 sprayPoint() {
        if (burning != null && burning != elephant) {
            if (!burning.isOnFire()) return null;
            return burning.position().add(0, burning.getBbHeight() * 0.6, 0);
        }
        if (fire != null && elephant.level().getBlockState(fire).is(BlockTags.FIRE)) return Vec3.atCenterOf(fire);
        return null;
    }

    @Nullable
    private LivingEntity burningHerdmate() {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ElephantEntity mate : ElephantHerd.herdmates(elephant, MATE_RANGE)) {
            if (!mate.isOnFire()) continue;
            double distance = mate.distanceToSqr(elephant);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = mate;
            }
        }
        return best;
    }

    @Nullable
    private BlockPos findFire() {
        BlockPos origin = elephant.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-FIRE_RADIUS, -1, -FIRE_RADIUS),
                origin.offset(FIRE_RADIUS, FIRE_HEIGHT, FIRE_RADIUS))) {
            if (!elephant.level().getBlockState(pos).is(BlockTags.FIRE)) continue;
            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }
}
