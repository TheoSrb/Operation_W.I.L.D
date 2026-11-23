package net.tiew.operationWild.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.client.OWClientHooks;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Glide;

public final class OWGlideController {

    public static final int FLAP_TICKS = 14;
    public static final int LAND_TICKS = 9;

    private static final int STALE_RESEND_TICKS = 10;
    private static final int SERVER_COOLDOWN_SLACK = 4;
    private static final int SERVER_GROUND_TICKS = 3;
    private static final int HOP_MIN_TICKS = 2;
    private static final double HOP_GRAVITY = 0.08;
    private static final float HOP_STEER = 5f;
    private static final float HEAD_FREEDOM = 70f;
    private static final double MAX_CLIMB = 0.12;
    private static final float BANK_RESPONSE = 0.2f;
    private static final float WILD_FALL_DISTANCE = 0.5f;

    private enum Mode { NONE, HOP, GLIDE }

    private final OWEntity owner;

    private int serverCooldown = 0;
    private int serverGrace = 0;
    private int serverGroundTicks = 0;

    private Mode mode = Mode.NONE;
    private double airSpeed = 0;
    private double vy = 0;
    private float pitch = 0f;
    private float yaw = 0f;
    private float bank = 0f;
    private boolean jumpWasDown = false;
    private int cooldown = 0;
    private int hopTicks = 0;
    private int staleTicks = 0;

    private float blend, blendO;
    private float roll, rollO;
    private float tilt, tiltO;
    private float speedFactor;
    private int flapAge = -1;
    private int landAge = -1;
    private int seenTakeoff = 0;
    private boolean visWasGliding = false;
    private float visLastYaw = 0f;
    private double visLastX, visLastY, visLastZ;
    private boolean visValid = false;

    OWGlideController(OWEntity owner) {
        this.owner = owner;
    }

    void tick() {
        OWGlideProfile profile = owner.glideProfile();
        if (owner.level().isClientSide()) {
            if (profile != null) tickVisuals(profile);
        } else {
            tickServer(profile);
        }
    }

    private void tickServer(OWGlideProfile profile) {
        if (serverCooldown > 0) serverCooldown--;
        if (serverGrace > 0) serverGrace--;

        if (profile == null) {
            if (owner.isGlidingSynced()) owner.setGlidingSynced(false);
            return;
        }

        boolean ridden = owner.getControllingPassenger() instanceof Player;
        if (owner.isGlidingSynced()) {
            owner.resetFallDistance();
            serverGrace = Glide.FALL_GRACE_TICKS;
            serverGroundTicks = owner.onGround() ? serverGroundTicks + 1 : 0;

            boolean stop = owner.isInWater() || owner.isInLava() || !owner.isAlive() || owner.isKnockedOut()
                    || serverGroundTicks > SERVER_GROUND_TICKS;
            if (!ridden && (!profile.wildGlide() || !owner.canGlideNow())) stop = true;

            if (stop) {
                owner.setGlidingSynced(false);
                serverGroundTicks = 0;
                return;
            }
            if (!ridden) clampWildFall(profile);
            return;
        }

        if (!ridden && profile.wildGlide() && owner.canGlideNow() && !owner.onGround() && !owner.isInWater()
                && !owner.isInLava() && owner.getDeltaMovement().y < -profile.wildMinFall()
                && owner.fallDistance > WILD_FALL_DISTANCE) {
            owner.setGlidingSynced(true);
            serverGroundTicks = 0;
            clampWildFall(profile);
        }
    }

    private void clampWildFall(OWGlideProfile profile) {
        Vec3 motion = owner.getDeltaMovement();
        if (motion.y < -profile.wildSink()) {
            owner.setDeltaMovement(motion.x, -profile.wildSink(), motion.z);
            owner.hasImpulse = true;
        }
        owner.resetFallDistance();
    }

    void serverTakeoff(Player rider) {
        OWGlideProfile profile = owner.glideProfile();
        if (profile == null || owner.level().isClientSide()) return;
        if (serverCooldown > 0 || !owner.canGlideNow()) return;
        if (owner.getVitalEnergy() > owner.getVitalEnergyCapacity() - profile.takeoffEnergy()) {
            owner.canShowVitalEnergyLack = true;
            return;
        }
        owner.setVitalEnergy(owner.getVitalEnergy() + profile.takeoffEnergy());
        serverCooldown = Math.max(0, profile.takeoffCooldownTicks() - SERVER_COOLDOWN_SLACK);
        serverGrace = Glide.FALL_GRACE_TICKS;
        owner.bumpGlideTakeoff();

        SoundEvent flap = owner.glideFlapSound();
        if (flap != null) {
            owner.level().playSound(rider, owner.getX(), owner.getY(), owner.getZ(), flap,
                    SoundSource.NEUTRAL, 0.9f, 1.15f + owner.getRandom().nextFloat() * 0.2f);
        }
    }

    void serverSetGliding(boolean start) {
        OWGlideProfile profile = owner.glideProfile();
        if (profile == null || owner.level().isClientSide()) return;
        serverGrace = Glide.FALL_GRACE_TICKS;
        if (start) {
            if (!owner.canGlideNow() || owner.isInWater() || owner.isInLava()) return;
            serverGroundTicks = 0;
            owner.resetFallDistance();
            owner.setGlidingSynced(true);
        } else {
            owner.setGlidingSynced(false);
        }
    }

    void afterRiddenRotation(Player player, float yawOBefore, float yawBefore, float bodyBefore) {
        OWGlideProfile profile = owner.glideProfile();
        if (profile == null) return;

        if (!owner.isControlledByLocalInstance()) {
            if (owner.isGlidingSynced()) {
                owner.setYRot(yawBefore);
                owner.yBodyRot = bodyBefore;
                owner.yRotO = yawOBefore;
            }
            return;
        }

        tickLocal(player, profile);
        if (mode != Mode.NONE) owner.yRotO = yawOBefore;
    }

    private void tickLocal(Player player, OWGlideProfile profile) {
        boolean jump = OWClientHooks.isJumpKeyDown() && !OWClientHooks.isScreenOpen();
        if (cooldown > 0) cooldown--;

        boolean airborne = !owner.onGround() && !owner.isInWater() && !owner.isInLava();
        boolean can = owner.canGlideNow();

        if (mode == Mode.NONE && owner.onGround() && jump && !jumpWasDown && cooldown == 0 && can
                && owner.getVitalEnergy() <= owner.getVitalEnergyCapacity() - profile.takeoffEnergy()) {
            takeoff(profile);
        } else if (mode != Mode.GLIDE && airborne && jump && can
                && (mode != Mode.HOP || hopTicks >= HOP_MIN_TICKS)
                && (mode == Mode.HOP ? vy < Glide.ENTRY_MIN_FALL : owner.getDeltaMovement().y < Glide.ENTRY_MIN_FALL)) {
            beginGlide(profile);
        }

        if (mode == Mode.GLIDE && (!airborne || !jump || !can)) {
            endGlide(profile);
        } else if (mode == Mode.HOP && ((!airborne && hopTicks >= HOP_MIN_TICKS) || owner.isInWater() || owner.isInLava())) {
            endHop();
        }

        if (mode == Mode.HOP) driveHop(player);
        else if (mode == Mode.GLIDE) driveGlide(player, profile);

        jumpWasDown = jump;

        if (mode != Mode.GLIDE && owner.isGlidingSynced()) {
            if (++staleTicks >= STALE_RESEND_TICKS) {
                staleTicks = 0;
                OWClientHooks.sendGlide(OWClientHooks.GLIDE_STOP);
            }
        } else {
            staleTicks = 0;
        }
    }

    private double horizontalMotion() {
        Vec3 motion = owner.getDeltaMovement();
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    private void takeoff(OWGlideProfile profile) {
        airSpeed = Math.max(horizontalMotion(), profile.takeoffForward());
        yaw = owner.getYRot();
        vy = profile.takeoffLift();
        hopTicks = 0;
        mode = Mode.HOP;
        cooldown = profile.takeoffCooldownTicks();
        flapAge = 0;
        OWClientHooks.sendGlide(OWClientHooks.GLIDE_TAKEOFF);

        SoundEvent flap = owner.glideFlapSound();
        if (flap != null) {
            owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(), flap, owner.getSoundSource(),
                    0.9f, 1.15f + owner.getRandom().nextFloat() * 0.2f, false);
        }
        owner.setDeltaMovement(owner.getDeltaMovement().x, vy, owner.getDeltaMovement().z);
    }

    private void driveHop(Player player) {
        hopTicks++;
        yaw = Mth.approachDegrees(yaw, player.getYRot(), HOP_STEER);
        airSpeed *= 0.995;
        if (owner.horizontalCollision) airSpeed *= 0.5;
        if (owner.verticalCollision && vy > 0) vy = 0;
        apply(player);
        vy = (vy - HOP_GRAVITY) * 0.98;
    }

    private void beginGlide(OWGlideProfile profile) {
        airSpeed = Math.max(mode == Mode.HOP ? airSpeed : horizontalMotion(), profile.minSpeed() + 0.04);
        if (mode != Mode.HOP) {
            vy = owner.getDeltaMovement().y;
            yaw = owner.getYRot();
        }
        pitch = 0f;
        mode = Mode.GLIDE;
        OWClientHooks.sendGlide(OWClientHooks.GLIDE_START);
    }

    private void driveGlide(Player player, OWGlideProfile profile) {
        float targetPitch = Mth.clamp(player.getXRot(), profile.pitchMin(), profile.pitchMax());
        pitch += (targetPitch - pitch) * profile.pitchResponse();
        double sin = Mth.sin(pitch * Mth.DEG_TO_RAD);

        airSpeed += sin > 0 ? sin * profile.diveGain() : sin * profile.climbLoss();
        airSpeed -= profile.drag() * (airSpeed / profile.cruiseSpeed());
        if (player.zza > 0 && airSpeed < profile.cruiseSpeed()) airSpeed += profile.drag() * 1.6;
        if (player.zza < 0) airSpeed -= profile.drag() * 2.5;
        if (owner.horizontalCollision) airSpeed *= 0.55;
        airSpeed = Mth.clamp(airSpeed, profile.minSpeed() * 0.6, profile.maxSpeed());

        double stall = Mth.clamp(airSpeed / profile.stallSpeed(), 0.0, 1.0);
        double lift = sin < 0 ? stall : 1.0;
        double targetVy = -profile.baseSink() - sin * airSpeed * profile.liftFactor() * lift;
        if (stall < 1.0) targetVy = Mth.lerp(stall, -profile.baseSink() * 2.6, targetVy);
        targetVy = Math.min(targetVy, MAX_CLIMB);
        vy += (targetVy - vy) * profile.verticalResponse();
        if (owner.verticalCollision && vy > 0) vy = 0;

        float speedRatio = (float) Mth.clamp(airSpeed / profile.cruiseSpeed(), 0.0, 1.4);
        float turnRate = profile.turnDegrees() * (0.55f + 0.45f * speedRatio);
        float before = yaw;
        yaw = Mth.approachDegrees(yaw, player.getYRot(), turnRate);
        float bankTarget = Mth.clamp(Mth.wrapDegrees(yaw - before) / profile.turnDegrees() * profile.maxBank(),
                -profile.maxBank(), profile.maxBank());
        bank += (bankTarget - bank) * BANK_RESPONSE;

        apply(player);
        owner.resetFallDistance();
    }

    private void apply(Player player) {
        Vec3 dir = Vec3.directionFromRotation(0f, yaw);
        owner.setDeltaMovement(dir.x * airSpeed, vy, dir.z * airSpeed);
        owner.hasImpulse = true;
        owner.setYRot(yaw);
        owner.yBodyRot = yaw;
        float look = Mth.wrapDegrees(player.getYRot() - yaw);
        owner.yHeadRot = yaw + Mth.clamp(look, -HEAD_FREEDOM, HEAD_FREEDOM);
    }

    private void endGlide(OWGlideProfile profile) {
        boolean landed = owner.onGround();
        mode = Mode.NONE;
        bank = 0f;
        OWClientHooks.sendGlide(OWClientHooks.GLIDE_STOP);
        if (landed) owner.carryRiddenSpeed((float) Math.min(airSpeed, profile.cruiseSpeed() * 1.2));
    }

    private void endHop() {
        mode = Mode.NONE;
        owner.carryRiddenSpeed((float) airSpeed);
    }

    boolean ownsMotion() {
        return mode != Mode.NONE;
    }

    boolean isLocalGliding() {
        return mode == Mode.GLIDE;
    }

    boolean absorbsFall() {
        if (owner.level().isClientSide()) return mode != Mode.NONE || owner.isGlidingSynced();
        return owner.isGlidingSynced() || serverGrace > 0;
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    private static float lerpTick(float partialTick, float previous, float current) {
        return previous + (current - previous) * partialTick;
    }

    private void tickVisuals(OWGlideProfile profile) {
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

        boolean local = owner.isControlledByLocalInstance() && owner.getControllingPassenger() instanceof Player;
        if (!local && mode != Mode.NONE) {
            mode = Mode.NONE;
            bank = 0f;
        }

        boolean gliding = local ? mode == Mode.GLIDE : owner.isGlidingSynced();

        blendO = blend;
        blend = approach(blend, gliding ? 1f : 0f, gliding ? 0.16f : 0.22f);

        int serial = owner.getGlideTakeoffSerial();
        if (serial != seenTakeoff) {
            seenTakeoff = serial;
            if (!local || flapAge < 0) flapAge = 0;
        } else if (flapAge >= 0 && ++flapAge >= FLAP_TICKS) {
            flapAge = -1;
        }

        if (visWasGliding && !gliding && owner.onGround()) landAge = 0;
        else if (landAge >= 0 && ++landAge >= LAND_TICKS) landAge = -1;
        visWasGliding = gliding;

        double horizontal = Math.sqrt(dx * dx + dz * dz);
        speedFactor += ((float) Mth.clamp(horizontal / profile.maxSpeed(), 0.0, 1.0) - speedFactor) * 0.2f;

        float yawDelta = Mth.wrapDegrees(owner.yBodyRot - visLastYaw);
        visLastYaw = owner.yBodyRot;

        float rollTarget = 0f;
        float tiltTarget = 0f;
        if (gliding) {
            rollTarget = local ? bank : Mth.clamp(yawDelta / profile.turnDegrees() * profile.maxBank(), -profile.maxBank(), profile.maxBank());
            tiltTarget = local
                    ? Mth.clamp(pitch * 0.7f, -18f, 40f)
                    : Mth.clamp((float) ((-dy - profile.baseSink()) * 90.0), -18f, 40f);
        }
        rollO = roll;
        roll += (rollTarget - roll) * 0.25f;
        tiltO = tilt;
        tilt += (tiltTarget - tilt) * 0.2f;

        if (gliding && blend > 0.6f && speedFactor > 0.45f && owner.tickCount % 3 == 0) spawnWingTrails();
    }

    private void spawnWingTrails() {
        float yawRad = owner.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double span = owner.getBbWidth() * 1.6 + 0.4;
        double y = owner.getY() + owner.getBbHeight() * 0.55;
        for (int side = -1; side <= 1; side += 2) {
            double sx = -fz * side * span;
            double sz = fx * side * span;
            owner.level().addParticle(ParticleTypes.CLOUD, owner.getX() + sx - fx * 0.3, y, owner.getZ() + sz - fz * 0.3,
                    -fx * 0.04, -0.01, -fz * 0.04);
        }
    }

    float blend(float partialTick) { return lerpTick(partialTick, blendO, blend); }

    float roll(float partialTick) { return lerpTick(partialTick, rollO, roll); }

    float tilt(float partialTick) { return lerpTick(partialTick, tiltO, tilt); }

    float flapSeconds(float partialTick) { return flapAge < 0 ? -1f : (flapAge + partialTick) / 20f; }

    float landSeconds(float partialTick) { return landAge < 0 ? -1f : (landAge + partialTick) / 20f; }

    float fovModifier() {
        OWGlideProfile profile = owner.glideProfile();
        if (profile == null) return 1f;
        return 1f + profile.fovGain() * blend * speedFactor;
    }
}
