package net.tiew.operationWild.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.client.OWClientHooks;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Flight;
import net.tiew.operationWild.particle.OWParticles;

public final class OWFlightController {

    private static final int STALE_RESEND_TICKS = 10;
    private static final int TAKEOFF_GROUND_GRACE = 4;
    private static final float HEAD_FREEDOM = 70f;

    private enum Mode { GROUND, TAKEOFF, AIR, GLIDE }

    private final OWSemiFlyingEntity owner;

    private Mode mode = Mode.GROUND;
    private int localState = OWSemiFlyingEntity.FLIGHT_GROUND;
    private double speed = 0;
    private double vx = 0;
    private double vy = 0;
    private double vz = 0;
    private float yaw = 0f;
    private float yawRate = 0f;
    private float pitch = 0f;
    private int modeTicks = 0;
    private int cooldown = 0;
    private boolean jumpWasDown = false;
    private boolean exhausted = false;
    private int staleTicks = 0;
    private int liftProbeTicks = 0;
    private double cachedLift = 0;
    private double k = 1.0;

    private float fastBlend, fastBlendO;
    private double visLastX, visLastY, visLastZ;
    private boolean visValid = false;
    private float speedFactor = 0f;
    private boolean windStarted = false;

    OWFlightController(OWSemiFlyingEntity owner) {
        this.owner = owner;
    }

    boolean isLocalPilot() {
        return owner.level().isClientSide() && owner.isControlledByLocalInstance() && owner.getControllingPassenger() instanceof Player;
    }

    int localState() {
        return localState;
    }

    boolean ownsMotion() {
        return mode != Mode.GROUND;
    }

    void afterRiddenRotation(Player player, float yawOBefore, float yawBefore, float bodyBefore) {
        if (!owner.isControlledByLocalInstance()) {
            if (owner.isAirborneFlight()) {
                owner.setYRot(yawBefore);
                owner.yBodyRot = bodyBefore;
                owner.yRotO = yawOBefore;
            }
            return;
        }
        tickLocal(player);
        if (mode != Mode.GROUND) owner.yRotO = yawOBefore;
    }

    private void tickLocal(Player player) {
        k = owner.flightSpeedFactor();
        boolean jump = OWClientHooks.isJumpKeyDown() && !OWClientHooks.isScreenOpen();
        if (cooldown > 0) cooldown--;
        modeTicks++;

        float capacity = owner.getVitalEnergyCapacity();
        if (owner.getVitalEnergy() >= capacity - 0.5f) exhausted = true;
        else if (owner.getVitalEnergy() <= capacity * Flight.EXHAUSTED_RECOVERY_RATIO) exhausted = false;

        if (mode == Mode.GROUND && cooldown == 0 && owner.getSyncedFlightState() != OWSemiFlyingEntity.FLIGHT_GROUND
                && !owner.onGround() && !owner.isInWater() && owner.canFlyNow()) {
            adoptAirborne(owner.getSyncedFlightState());
        }

        if (mode == Mode.GROUND) {
            if (jump && !jumpWasDown && cooldown == 0 && owner.canFlyNow() && !exhausted && owner.canTakeoffFromHere()
                    && owner.getVitalEnergy() <= capacity - owner.takeoffEnergy()) {
                takeoff();
            }
            jumpWasDown = jump;
            resendIfStale();
            return;
        }

        if (!owner.canFlyNow() || owner.isInLava()) {
            land(false);
            jumpWasDown = jump;
            return;
        }

        boolean grounded = owner.onGround() || owner.isInWater();
        if (grounded && (mode != Mode.TAKEOFF || modeTicks > TAKEOFF_GROUND_GRACE) && vy <= 0.05) {
            land(true);
            jumpWasDown = jump;
            return;
        }

        steer(player);

        switch (mode) {
            case TAKEOFF -> driveTakeoff(player);
            case AIR -> driveAir(player, jump);
            case GLIDE -> driveGlide(player, jump);
            default -> { }
        }

        if (owner.horizontalCollision) {
            speed *= 0.5;
            vx *= 0.5;
            vz *= 0.5;
        }
        if (owner.verticalCollision && vy > 0) vy = 0;

        apply(player);
        owner.resetFallDistance();
        jumpWasDown = jump;
        resendIfStale();
    }

    private void steer(Player player) {
        float maxTurn = switch (mode) {
            case GLIDE -> owner.vehicleGlideTurnSpeed();
            case TAKEOFF -> owner.vehicleFlyTurnSpeed() * 0.6f;
            default -> speed < (owner.vehicleFlySpeed() * k) * 0.3 ? Flight.HOVER_TURN_SPEED
                    : owner.isRunning() ? owner.vehicleFlyRunTurnSpeed() : owner.vehicleFlyTurnSpeed();
        };
        boolean moving = player.zza != 0 || player.xxa != 0 || mode == Mode.GLIDE;
        float error = Mth.wrapDegrees(player.getYRot() - yaw);
        float wantedRate = moving ? Mth.clamp(error * Flight.TURN_GAIN, -maxTurn, maxTurn) : 0f;
        yawRate += (wantedRate - yawRate) * Flight.TURN_SMOOTHING;
        yaw += yawRate;

        float targetPitch = Mth.clamp(player.getXRot(), Flight.PITCH_MIN, Flight.PITCH_MAX);
        pitch += (targetPitch - pitch) * (mode == Mode.GLIDE ? Flight.GLIDE_PITCH_RESPONSE : Flight.PITCH_RESPONSE);
    }

    private double takeoffTimeScale() {
        return Flight.TAKEOFF_TICKS / (double) Math.max(1, owner.vehicleTakeoffDuration());
    }

    private void adoptAirborne(int syncedState) {
        Vec3 motion = owner.getDeltaMovement();
        vx = motion.x;
        vy = motion.y;
        vz = motion.z;
        speed = Math.sqrt(vx * vx + vz * vz);
        yaw = owner.getYRot();
        yawRate = 0f;
        pitch = 0f;
        modeTicks = 0;
        mode = syncedState == OWSemiFlyingEntity.FLIGHT_GLIDE && owner.canGlideInFlight() ? Mode.GLIDE : Mode.AIR;
        localState = syncedState;
    }

    private void takeoff() {
        Vec3 motion = owner.getDeltaMovement();
        speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        yaw = owner.getYRot();
        yawRate = 0f;
        pitch = -10f;
        vx = motion.x;
        vz = motion.z;
        vy = owner.vehicleTakeoffLift() * takeoffTimeScale();
        mode = Mode.TAKEOFF;
        modeTicks = 0;
        cooldown = Flight.TAKEOFF_COOLDOWN_TICKS;
        setState(OWSemiFlyingEntity.FLIGHT_HOVER);
        OWClientHooks.sendFlight(OWClientHooks.FLIGHT_TAKEOFF, OWSemiFlyingEntity.FLIGHT_HOVER);
        owner.playTakeoffEffectsLocal();
        owner.setDeltaMovement(vx, vy, vz);
    }

    private void driveTakeoff(Player player) {
        int duration = owner.vehicleTakeoffDuration();
        double scale = takeoffTimeScale();
        double progress = Math.min(1.0, modeTicks / (double) duration);
        double sustain = Flight.TAKEOFF_SUSTAIN * scale * (1.0 - progress);
        vy += (sustain - vy) * 0.12 * scale;
        double target = player.zza > 0 ? (owner.vehicleFlySpeed() * k) * 0.5 : 0;
        speed += (target - speed) * 0.06;
        setHorizontal(speed, Flight.HOVER_DRIFT_RESPONSE);
        if (modeTicks >= duration) {
            mode = Mode.AIR;
            modeTicks = 0;
        }
    }

    private void driveAir(Player player, boolean jump) {
        if (exhausted) {
            if (owner.canGlideInFlight()) {
                enterGlide();
                return;
            }
            speed += (0 - speed) * (owner.vehicleFlyDeceleration());
            vy += (-Flight.EXHAUSTED_SINK - vy) * Flight.VERTICAL_RESPONSE;
            setHorizontal(speed, Flight.HOVER_DRIFT_RESPONSE);
            setState(OWSemiFlyingEntity.FLIGHT_HOVER);
            return;
        }

        boolean forward = player.zza > 0;
        boolean back = player.zza < 0;
        boolean climb = jump;
        boolean fast = forward && owner.isRunning();
        double sin = Mth.sin(pitch * Mth.DEG_TO_RAD);
        double cos = Mth.cos(pitch * Mth.DEG_TO_RAD);
        double vyTarget;
        int state;

        if (!forward) {
            speed += (0 - speed) * (back ? Flight.BRAKE_DECELERATION : owner.vehicleFlyDeceleration());
            if (speed < 1.0E-3) speed = 0;
            double lookDown = Mth.clamp((pitch - Flight.HOVER_DESCEND_PITCH) / (Flight.PITCH_MAX - Flight.HOVER_DESCEND_PITCH), 0f, 1f);
            vyTarget = climb ? (owner.vehicleClimbSpeed() * k) : -(owner.vehicleDescendSpeed() * k) * lookDown;
            setHorizontal(speed, back ? Flight.DRIFT_RESPONSE : Flight.HOVER_DRIFT_RESPONSE);
            state = climb ? OWSemiFlyingEntity.FLIGHT_CLIMB : OWSemiFlyingEntity.FLIGHT_HOVER;
        } else {
            double base = fast ? (owner.vehicleFlyRunSpeed() * k) : (owner.vehicleFlySpeed() * k);
            double target = climb ? base * 0.8 : base;
            double response = target > speed ? owner.vehicleFlyAcceleration() : owner.vehicleFlyDeceleration();
            speed += (target - speed) * response;

            vyTarget = -sin * speed;
            if (climb) vyTarget = Math.max(vyTarget, (owner.vehicleClimbSpeed() * k));
            vyTarget = Math.min(vyTarget, (owner.vehicleClimbSpeed() * k) * 1.3);
            setHorizontal(speed * Math.max(cos, 0.3), Flight.DRIFT_RESPONSE);
            state = climb ? OWSemiFlyingEntity.FLIGHT_CLIMB : fast ? OWSemiFlyingEntity.FLIGHT_FAST : OWSemiFlyingEntity.FLIGHT_FLAP;

            if (owner.canGlideInFlight() && !climb && speed > (owner.vehicleFlySpeed() * k) * 0.5 && pitch > owner.vehicleGlideEntryPitch()) {
                vy += (vyTarget - vy) * Flight.VERTICAL_RESPONSE;
                enterGlide();
                return;
            }
        }

        vy += (vyTarget - vy) * Flight.VERTICAL_RESPONSE;
        setState(state);
    }

    private void enterGlide() {
        mode = Mode.GLIDE;
        modeTicks = 0;
        setState(OWSemiFlyingEntity.FLIGHT_GLIDE);
    }

    private void driveGlide(Player player, boolean jump) {
        boolean back = player.zza < 0;

        if (!exhausted && (jump || (speed < (owner.vehicleGlideExitSpeed() * k) && pitch < owner.vehicleGlideEntryPitch()))) {
            mode = Mode.AIR;
            modeTicks = 0;
            driveAir(player, jump);
            return;
        }

        double sin = Mth.sin(pitch * Mth.DEG_TO_RAD);
        double diveSin = Math.max(0.0, Mth.sin((pitch - Flight.GLIDE_DIVE_DEAD_ZONE) * Mth.DEG_TO_RAD));
        speed += sin > 0 ? diveSin * (owner.vehicleGlideDiveGain() * k) : sin * (owner.vehicleGlideClimbLoss() * k);
        if (sin < 0) speed += sin * speed * Flight.GLIDE_PULL_UP_BRAKE;
        speed -= (owner.vehicleGlideDrag() * k) * (0.5 + speed / (owner.vehicleFlySpeed() * k) * 0.5);
        if (back) speed -= (owner.vehicleGlideDrag() * k) * 5.0;
        speed = Mth.clamp(speed, 0.0, (owner.vehicleDiveSpeed() * k));

        double vyTarget;
        if (exhausted && speed < (owner.vehicleGlideExitSpeed() * k)) {
            vyTarget = -Flight.EXHAUSTED_SINK;
        } else {
            double pitchFollow = sin > 0 ? 1.0 : owner.vehicleGlideLiftFactor();
            vyTarget = -owner.vehicleGlideSink() - sin * speed * pitchFollow + probeLift();
            vyTarget = Math.min(vyTarget, Flight.GLIDE_MAX_CLIMB);
        }
        vy += (vyTarget - vy) * Flight.GLIDE_VERTICAL_RESPONSE;
        setHorizontal(speed * Math.max(Mth.cos(pitch * Mth.DEG_TO_RAD), 0.3), Flight.DRIFT_RESPONSE * 0.6);
        setState(OWSemiFlyingEntity.FLIGHT_GLIDE);
    }

    private double probeLift() {
        if (++liftProbeTicks >= Flight.LIFT_PROBE_INTERVAL) {
            liftProbeTicks = 0;
            cachedLift = computeLift();
        }
        if (cachedLift > 0 && owner.tickCount % 3 == 0) {
            owner.level().addParticle(OWParticles.WIND_TRAIL.get(),
                    owner.getX() + (owner.getRandom().nextDouble() - 0.5) * 3.0, owner.getY() - 0.4,
                    owner.getZ() + (owner.getRandom().nextDouble() - 0.5) * 3.0, 0, 0.08, 0);
        }
        return cachedLift;
    }

    private double computeLift() {
        BlockPos base = owner.blockPosition();
        for (int dy = 1; dy <= Flight.THERMAL_SCAN_DEPTH; dy++) {
            BlockPos pos = base.below(dy);
            var state = owner.level().getBlockState(pos);
            if (state.isAir()) continue;
            if (owner.isThermalSource(state)) {
                return Flight.THERMAL_LIFT * (1.0 - dy / (double) (Flight.THERMAL_SCAN_DEPTH + 4));
            }
            if (!state.getCollisionShape(owner.level(), pos).isEmpty() || !state.getFluidState().isEmpty()) break;
        }
        Vec3 ahead = Vec3.directionFromRotation(0f, yaw).scale(Flight.RIDGE_PROBE_DISTANCE);
        int hereGround = owner.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(owner.getX()), Mth.floor(owner.getZ()));
        int aheadGround = owner.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(owner.getX() + ahead.x), Mth.floor(owner.getZ() + ahead.z));
        double clearance = owner.getY() - aheadGround;
        if (aheadGround - hereGround >= 2 && clearance > 1.5 && clearance < Flight.RIDGE_MAX_HEIGHT) {
            return Flight.RIDGE_LIFT * Mth.clamp((aheadGround - hereGround) / 6.0, 0.4, 1.0);
        }
        return 0;
    }

    private void setHorizontal(double horizontalSpeed, double response) {
        Vec3 dir = Vec3.directionFromRotation(0f, yaw);
        vx += (dir.x * horizontalSpeed - vx) * response;
        vz += (dir.z * horizontalSpeed - vz) * response;
    }

    private void apply(Player player) {
        owner.setDeltaMovement(vx, vy, vz);
        owner.hasImpulse = true;
        owner.setYRot(yaw);
        owner.yBodyRot = yaw;
        float look = Mth.wrapDegrees(player.getYRot() - yaw);
        owner.yHeadRot = yaw + Mth.clamp(look, -HEAD_FREEDOM, HEAD_FREEDOM);
    }

    private void land(boolean carry) {
        boolean wasAirborne = mode != Mode.GROUND;
        mode = Mode.GROUND;
        modeTicks = 0;
        yawRate = 0f;
        cooldown = Math.max(cooldown, 6);
        setState(OWSemiFlyingEntity.FLIGHT_GROUND);
        if (!wasAirborne) return;
        OWClientHooks.sendFlight(OWClientHooks.FLIGHT_LAND, OWSemiFlyingEntity.FLIGHT_GROUND);
        if (carry) owner.carryRiddenSpeed((float) Math.min(Math.sqrt(vx * vx + vz * vz), (owner.vehicleFlySpeed() * k)) * 0.5f);
    }

    private void setState(int state) {
        if (state == localState) return;
        localState = state;
        OWClientHooks.sendFlight(OWClientHooks.FLIGHT_STATE, state);
        staleTicks = 0;
    }

    private void resendIfStale() {
        if (owner.getSyncedFlightState() == localState) {
            staleTicks = 0;
            return;
        }
        if (++staleTicks >= STALE_RESEND_TICKS) {
            staleTicks = 0;
            OWClientHooks.sendFlight(OWClientHooks.FLIGHT_STATE, localState);
        }
    }

    void tickVisuals() {
        k = owner.flightSpeedFactor();
        double dx = 0, dy = 0, dz = 0;
        if (visValid) {
            dx = owner.getX() - visLastX;
            dy = owner.getY() - visLastY;
            dz = owner.getZ() - visLastZ;
            if (dx * dx + dy * dy + dz * dz > 25.0) {
                dx = 0;
                dy = 0;
                dz = 0;
            }
        }
        visLastX = owner.getX();
        visLastY = owner.getY();
        visLastZ = owner.getZ();
        visValid = true;

        boolean local = isLocalPilot();
        if (!local && mode != Mode.GROUND) {
            mode = Mode.GROUND;
            localState = OWSemiFlyingEntity.FLIGHT_GROUND;
            speed = 0;
        }

        double travelled = Math.sqrt(dx * dx + dy * dy + dz * dz);
        speedFactor += ((float) Mth.clamp(travelled / (owner.vehicleFlyRunSpeed() * k), 0.0, 1.6) - speedFactor) * 0.15f;

        boolean fastNow = owner.getFlightState() == OWSemiFlyingEntity.FLIGHT_FAST;
        fastBlendO = fastBlend;
        fastBlend += ((fastNow ? 1f : 0f) - fastBlend) * 0.1f;

        boolean soaring = owner.getFlightState() == OWSemiFlyingEntity.FLIGHT_GLIDE;
        if (soaring && speedFactor > 0.55f && owner.tickCount % 2 == 0) spawnWingTrails();

        if (local && owner.isAirborneFlight() && !windStarted) {
            windStarted = true;
            OWClientHooks.startFlightWind(owner);
        } else if (!owner.isAirborneFlight()) {
            windStarted = false;
        }
    }

    private void spawnWingTrails() {
        float yawRad = owner.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double span = owner.wingSpan();
        double y = owner.getY() + owner.getBbHeight() * 0.55;
        for (int side = -1; side <= 1; side += 2) {
            double sx = -fz * side * span;
            double sz = fx * side * span;
            owner.level().addParticle(OWParticles.WIND_TRAIL.get(), owner.getX() + sx - fx * 0.4, y, owner.getZ() + sz - fz * 0.4,
                    -fx * 0.02, 0.0, -fz * 0.02);
        }
    }

    float speedFactor() {
        return speedFactor;
    }

    float fastBlend(float partialTick) {
        return fastBlendO + (fastBlend - fastBlendO) * partialTick;
    }

    float fovModifier() {
        if (!owner.isAirborneFlight() || owner.getFlightState() == OWSemiFlyingEntity.FLIGHT_GLIDE) return 1f;
        return 1f + Flight.FOV_GAIN * Mth.clamp(speedFactor - 0.6f, 0f, 1f);
    }
}
