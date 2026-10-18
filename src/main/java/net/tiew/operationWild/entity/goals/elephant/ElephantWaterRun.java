package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;

public class ElephantWaterRun {

    public enum Status { RUNNING, FILLED, FAILED }

    private static final int TRAVEL = 0;
    private static final int WADE = 1;
    private static final int FACE = 2;
    private static final int FILL = 3;

    private static final int TRAVEL_TIMEOUT = 900;
    private static final int WADE_TIMEOUT = 140;
    private static final int FACE_TIMEOUT = 40;
    private static final int FILL_TIMEOUT = 170;
    private static final int REPATH_INTERVAL = 20;
    private static final int MAX_WADES = 3;
    private static final int STALL_TICKS = 40;
    private static final double APPROACH_SWITCH_DISTANCE = 3.0;
    private static final double WADE_STOP_DISTANCE = 0.8;
    private static final float FACE_TOLERANCE = 18f;
    private static final double WADE_SPEED_FACTOR = 0.8;

    private final ElephantEntity elephant;
    private final ElephantGoals.WaterSpot spot;
    private final int fillTo;
    private final double speed;
    private int phase = TRAVEL;
    private int timer;
    private int repath;
    private int wades;

    public ElephantWaterRun(ElephantEntity elephant, ElephantGoals.WaterSpot spot, int fillTo, double speed) {
        this.elephant = elephant;
        this.spot = spot;
        this.fillTo = fillTo;
        this.speed = speed;
    }

    public ElephantGoals.WaterSpot spot() {
        return spot;
    }

    public Status tick() {
        timer++;
        switch (phase) {
            case TRAVEL -> {
                if (closeToWaterFacing()) {
                    toFace();
                    return Status.RUNNING;
                }
                boolean near = ElephantGoals.horizontalDistance(elephant, spot.standCenter()) <= APPROACH_SWITCH_DISTANCE;
                boolean stalled = timer > STALL_TICKS && elephant.getNavigation().isDone();
                if (near || stalled) {
                    toWade();
                    return Status.RUNNING;
                }
                if (timer > TRAVEL_TIMEOUT) return Status.FAILED;
                if (--repath <= 0) {
                    repath = REPATH_INTERVAL;
                    Vec3 stand = spot.standCenter();
                    elephant.getNavigation().moveTo(stand.x, stand.y, stand.z, speed);
                }
            }
            case WADE -> {
                if (closeToWaterFacing()) {
                    toFace();
                    return Status.RUNNING;
                }
                Vec3 water = spot.waterCenter();
                if (timer > WADE_TIMEOUT || ElephantGoals.horizontalDistance(elephant, water) <= WADE_STOP_DISTANCE) {
                    toFace();
                    return Status.RUNNING;
                }
                elephant.getNavigation().stop();
                elephant.getMoveControl().setWantedPosition(water.x, elephant.getY(), water.z, speed * WADE_SPEED_FACTOR);
                elephant.getLookControl().setLookAt(water);
            }
            case FACE -> {
                elephant.getNavigation().stop();
                elephant.turnBodyTowards(spot.waterCenter(), 6f);
                elephant.getLookControl().setLookAt(spot.waterCenter());
                boolean facing = ElephantGoals.yawGap(elephant, spot.waterCenter()) <= FACE_TOLERANCE;
                if (facing && !elephant.hasWaterUnderTrunk() && wades < MAX_WADES) {
                    toWade();
                    return Status.RUNNING;
                }
                if (facing && elephant.startAiTrunk(ElephantEntity.TRUNK_FILLING)) {
                    phase = FILL;
                    timer = 0;
                    return Status.RUNNING;
                }
                if (timer > FACE_TIMEOUT) {
                    if (wades >= MAX_WADES) return Status.FAILED;
                    toWade();
                }
            }
            case FILL -> {
                elephant.getNavigation().stop();
                elephant.getLookControl().setLookAt(spot.waterCenter());
                if (elephant.getTrunkWater() >= fillTo || timer > FILL_TIMEOUT) {
                    elephant.stopAiTrunk();
                    return Status.FILLED;
                }
                if (!elephant.isFillingTrunk()) {
                    elephant.stopAiTrunk();
                    if (elephant.getTrunkWater() >= fillTo / 2) return Status.FILLED;
                    if (wades >= MAX_WADES) return elephant.getTrunkWater() > 0 ? Status.FILLED : Status.FAILED;
                    toWade();
                }
            }
            default -> {
                return Status.FAILED;
            }
        }
        return Status.RUNNING;
    }

    private boolean closeToWaterFacing() {
        return ElephantGoals.yawGap(elephant, spot.waterCenter()) <= 60f && elephant.hasWaterUnderTrunk();
    }

    private void toFace() {
        elephant.getNavigation().stop();
        phase = FACE;
        timer = 0;
    }

    private void toWade() {
        wades++;
        phase = WADE;
        timer = 0;
        repath = 0;
    }
}
