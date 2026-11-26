package net.tiew.operationWild.entity.goals.flying;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Flight;

import java.util.EnumSet;

public class OWAerialWanderGoal extends Goal {

    private static final int CENTER_SHIFT_MIN_TICKS = 200;
    private static final int CENTER_SHIFT_MAX_TICKS = 420;
    private static final double CENTER_SHIFT_RANGE = 24.0;
    private static final double LANDING_APPROACH_HEIGHT = 6.0;
    private static final int KEEP_AIRBORNE_TICKS = 10;

    private final OWSemiFlyingEntity flyer;

    private int duration;
    private int elapsed;
    private int centerShiftTimer;
    private double centerX;
    private double centerZ;
    private double altitude;
    private double radius;
    private double angle;
    private int direction;
    private boolean landing;
    private Vec3 landingSpot;

    public OWAerialWanderGoal(OWSemiFlyingEntity flyer) {
        this.flyer = flyer;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!isFree()) return false;
        if (flyer.isAirborneFlight()) return true;
        if (flyer.flightEnergyRatio() > Flight.WILD_TAKEOFF_MAX_ENERGY_RATIO) return false;
        return flyer.getRandom().nextInt(Flight.WILD_TAKEOFF_CHANCE) == 0 && flyer.canFlyNow() && flyer.canTakeoffFromHere();
    }

    @Override
    public boolean canContinueToUse() {
        return isFree() && flyer.isAirborneFlight() && elapsed < duration + 400;
    }

    private boolean isFree() {
        if (flyer.getControllingPassenger() instanceof Player || flyer.isVehicle()) return false;
        if (flyer.getTarget() != null || flyer.isSedatedFleeing() || flyer.isSitting() || flyer.isBaby()) return false;
        if (flyer.isTame() && flyer.isFollowingOwner()) return false;
        return !flyer.isNapping() && !flyer.isSleeping();
    }

    @Override
    public void start() {
        if (!flyer.isAirborneFlight() && !flyer.requestWildTakeoff()) {
            duration = 0;
            return;
        }
        duration = Flight.WILD_FLIGHT_MIN_TICKS + flyer.getRandom().nextInt(Flight.WILD_FLIGHT_MAX_TICKS - Flight.WILD_FLIGHT_MIN_TICKS);
        elapsed = 0;
        landing = false;
        landingSpot = null;
        direction = flyer.getRandom().nextBoolean() ? 1 : -1;
        pickCenter(flyer.getX(), flyer.getZ());
        angle = Math.atan2(flyer.getZ() - centerZ, flyer.getX() - centerX);
    }

    @Override
    public void stop() {
        flyer.getNavigation().stop();
        if (landing && landingSpot != null && flyer.isAirborneFlight()) flyer.requestWildLanding(landingSpot);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        elapsed++;
        if (duration <= 0) return;

        boolean tired = flyer.flightEnergyRatio() >= Flight.WILD_LAND_ENERGY_RATIO;
        if (!landing && (elapsed >= duration || tired)) {
            landing = true;
            landingSpot = flyer.findLandingSpot(true);
        }

        if (landing) {
            double dx = landingSpot.x - flyer.getX();
            double dz = landingSpot.z - flyer.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double height = horizontal > 6.0 ? landingSpot.y + LANDING_APPROACH_HEIGHT : landingSpot.y;
            flyer.getMoveControl().setWantedPosition(landingSpot.x, height, landingSpot.z, 1.0);
            if (horizontal > 4.0) {
                flyer.keepAirborne(KEEP_AIRBORNE_TICKS);
            } else {
                flyer.requestWildLanding(landingSpot);
                elapsed = duration + 400;
            }
            return;
        }

        flyer.keepAirborne(KEEP_AIRBORNE_TICKS);
        if (--centerShiftTimer <= 0) {
            double nx = centerX + (flyer.getRandom().nextDouble() - 0.5) * 2.0 * CENTER_SHIFT_RANGE;
            double nz = centerZ + (flyer.getRandom().nextDouble() - 0.5) * 2.0 * CENTER_SHIFT_RANGE;
            pickCenter(nx, nz);
        }

        double step = flyer.wildGlideSpeed() / Math.max(radius, 1.0);
        angle += step * direction;
        double x = centerX + Math.cos(angle) * radius;
        double z = centerZ + Math.sin(angle) * radius;
        flyer.getMoveControl().setWantedPosition(x, altitude, z, 1.0);
    }

    private void pickCenter(double x, double z) {
        centerX = x;
        centerZ = z;
        radius = Mth.lerp(flyer.getRandom().nextDouble(), Flight.WILD_CIRCLE_RADIUS_MIN, Flight.WILD_CIRCLE_RADIUS_MAX);
        int ground = flyer.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        int lift = Flight.WILD_ALTITUDE_MIN + flyer.getRandom().nextInt(Flight.WILD_ALTITUDE_MAX - Flight.WILD_ALTITUDE_MIN + 1);
        altitude = Math.min(ground + lift, flyer.level().getMaxBuildHeight() - 4);
        centerShiftTimer = CENTER_SHIFT_MIN_TICKS + flyer.getRandom().nextInt(CENTER_SHIFT_MAX_TICKS - CENTER_SHIFT_MIN_TICKS);
    }
}
