package net.tiew.operationWild.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.client.OWClientHooks;

import java.util.HashSet;
import java.util.Set;

public final class OWSlideController {

    private static final int CONFIRM_TIMEOUT_TICKS = 12;
    private static final int REQUEST_RETRY_TICKS = 6;
    private static final int SERVER_COOLDOWN_SLACK = 4;
    private static final int HOP_MIN_TICKS = 3;
    private static final double STEP_UP_MIN = 0.3;
    private static final double MIN_SPEED_RATIO = 0.12;
    private static final float HEAD_FREEDOM = 70f;
    private static final float STEER_VISUAL_DEGREES = 35f;
    private static final double GROUND_SNAP_MAX = 1.1;
    private static final double GROUND_SNAP_STEP = 0.125;
    private static final int GROUND_SNAP_AIR_TICKS = 2;
    private static final double GROUND_SNAP_FALL_SPEED = 0.45;
    private static final double AIR_STICK_GRAVITY = 0.035;

    private final OWEntity owner;

    private int serverCooldown = 0;
    private double serverLastX, serverLastZ;
    private boolean serverTracked = false;
    private final Set<Integer> shoved = new HashSet<>();

    private boolean active = false;
    private boolean carry = false;
    private boolean pendingStart = false;
    private boolean confirmed = false;
    private boolean hopUsed = false;
    private int unconfirmedTicks = 0;
    private int elapsed = 0;
    private int handoff = 0;
    private int requestCooldown = 0;
    private int airTicks = 0;
    private double speed = 0;
    private double lastSpeed = 0;
    private float slick = 0f;
    private float yaw = 0f;
    private double trackX, trackY, trackZ;
    private boolean trackValid = false;

    private double visX, visZ;
    private boolean visValid = false;
    private float visLastYaw = 0f;
    private boolean visWasSliding = false;

    private float blend, blendO;
    private float steer, steerO;
    private float impact, impactO;
    private float speedFactor, speedFactorO;

    OWSlideController(OWEntity owner) {
        this.owner = owner;
    }

    void tick() {
        OWSlideProfile profile = owner.slideProfile();
        if (owner.level().isClientSide()) {
            if (profile != null) tickVisuals(profile);
        } else {
            tickServer(profile);
        }
    }

    void request() {
        OWSlideProfile profile = owner.slideProfile();
        if (profile == null || owner.level().isClientSide()) return;
        if (owner.isSliding() || serverCooldown > 0 || !owner.canSlideNow()) return;

        if (owner.getVitalEnergy() > owner.getVitalEnergyCapacity() - profile.energyCost()) {
            owner.canShowVitalEnergyLack = true;
            return;
        }
        owner.setVitalEnergy(owner.getVitalEnergy() + profile.energyCost());

        shoved.clear();
        serverTracked = false;
        owner.setSlideTick(1);
    }

    void end() {
        if (owner.level().isClientSide() || !owner.isSliding()) return;
        owner.setSlideTick(0);
        OWSlideProfile profile = owner.slideProfile();
        serverCooldown = profile == null ? 0 : Math.max(0, profile.cooldownTicks() - SERVER_COOLDOWN_SLACK);
        shoved.clear();
    }

    private void tickServer(OWSlideProfile profile) {
        if (serverCooldown > 0) serverCooldown--;

        int tick = owner.getSlideTick();
        if (tick <= 0) return;

        if (profile == null || owner.getControllingPassenger() == null || owner.isInWater() || owner.isInLava()
                || tick > profile.durationTicks(1f) + 10) {
            end();
            return;
        }

        owner.setSlideTick(tick + 1);
        shoveTargets(profile);
    }

    private void shoveTargets(OWSlideProfile profile) {
        double dx = serverTracked ? owner.getX() - serverLastX : 0.0;
        double dz = serverTracked ? owner.getZ() - serverLastZ : 0.0;
        serverLastX = owner.getX();
        serverLastZ = owner.getZ();
        serverTracked = true;

        double power = profile.shove();
        double moved = Math.sqrt(dx * dx + dz * dz);
        if (power <= 0.0 || moved < 0.08 || moved > 3.0) return;

        Vec3 dir = new Vec3(dx / moved, 0, dz / moved);
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        double factor = Mth.clamp(moved / (owner.slideRunReference() * profile.topSpeedRatio(0f)), 0.0, 1.0);
        AABB sweep = owner.getBoundingBox().expandTowards(dir.scale(0.9)).inflate(0.25, 0.0, 0.25);

        for (LivingEntity living : owner.level().getEntitiesOfClass(LivingEntity.class, sweep)) {
            if (living == owner || owner.hasPassenger(living) || living.isPassenger()) continue;
            if (shoved.contains(living.getId()) || owner.isAlliedTo(living)) continue;
            if (living instanceof Player player && (player.isCreative() || player.isSpectator() || owner.isOwnedBy(player))) continue;
            if (living instanceof TamableAnimal tamable && owner.getOwnerUUID() != null
                    && owner.getOwnerUUID().equals(tamable.getOwnerUUID())) continue;

            shoved.add(living.getId());

            double sideSign = living.position().subtract(owner.position()).dot(side) >= 0 ? 1.0 : -1.0;
            double resistance = 1.0 - Mth.clamp(living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0) * 0.7;
            double push = power * (0.4 + 0.6 * factor) * resistance;

            living.push(dir.x * push + side.x * sideSign * push * 0.8,
                    0.28 + 0.12 * factor,
                    dir.z * push + side.z * sideSign * push * 0.8);
            living.hurtMarked = true;

            owner.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.NEUTRAL, 0.9f, 0.8f);
        }
    }

    void clientRequest() {
        OWSlideProfile profile = owner.slideProfile();
        if (profile == null || !owner.level().isClientSide() || !owner.isControlledByLocalInstance()) return;
        if (owner.isSliding() || active || carry || requestCooldown > 0) return;
        if (!owner.onGround() || !owner.canSlideNow()) return;
        if (lastSpeed < owner.slideRunReference() * profile.minEntryRatio()) return;
        if (owner.getVitalEnergy() > owner.getVitalEnergyCapacity() - profile.energyCost()) return;

        requestCooldown = REQUEST_RETRY_TICKS;
        pendingStart = true;
        OWClientHooks.sendSlide(true);
    }

    void afterRiddenRotation(Player player, float yawOBefore, float yawBefore, float bodyBefore) {
        OWSlideProfile profile = owner.slideProfile();
        if (profile == null) return;

        if (!owner.isControlledByLocalInstance()) {
            if (owner.isSliding()) {
                owner.setYRot(yawBefore);
                owner.yBodyRot = bodyBefore;
                owner.yRotO = yawOBefore;
            }
            return;
        }

        tickMotion(player, profile);
        if (active || carry) owner.yRotO = yawOBefore;
    }

    boolean ownsMotion() {
        return active || carry;
    }

    float handoffResponse(float base) {
        OWSlideProfile profile = owner.slideProfile();
        if (profile == null || handoff <= 0 || profile.handoffTicks() <= 0) return base;
        float t = 1f - (float) handoff / profile.handoffTicks();
        return Mth.lerp(t * t, 0.05f, base);
    }

    private void tickMotion(Player player, OWSlideProfile profile) {
        double mx = owner.getX() - trackX;
        double my = owner.getY() - trackY;
        double mz = owner.getZ() - trackZ;
        if (!trackValid || mx * mx + mz * mz > 4.0 || Math.abs(my) > 3.0) {
            mx = 0.0;
            my = 0.0;
            mz = 0.0;
        }
        trackX = owner.getX();
        trackY = owner.getY();
        trackZ = owner.getZ();
        trackValid = true;
        lastSpeed = Math.sqrt(mx * mx + mz * mz);

        if (requestCooldown > 0) requestCooldown--;

        if (pendingStart) {
            pendingStart = false;
            if (!active && !carry) begin(profile, mx, mz);
        }

        if (active) {
            if (owner.isSliding()) confirmed = true;
            else if (confirmed || ++unconfirmedTicks > CONFIRM_TIMEOUT_TICKS) finish(profile, false);
        }

        if (active) drive(player, profile, my);
        else if (carry) driveCarry(player, profile);
        else if (handoff > 0) handoff--;
    }

    private void begin(OWSlideProfile profile, double mx, double mz) {
        double entry = Math.sqrt(mx * mx + mz * mz);
        double run = owner.slideRunReference();
        double max = run * profile.topSpeedRatio(0f);

        yaw = entry > 1.0E-3 ? (float) (Mth.atan2(-mx, mz) * Mth.RAD_TO_DEG) : owner.getYRot();

        double boost = run * profile.boostRatio(owner.isRunning()) * Mth.clamp(1.0 - entry / max, 0.0, 1.0);
        speed = Math.min(Math.max(entry, run * profile.minEntryRatio()) + boost, max);

        elapsed = 0;
        airTicks = 0;
        hopUsed = false;
        handoff = 0;
        confirmed = false;
        unconfirmedTicks = 0;
        active = true;
        carry = false;
    }

    private void drive(Player player, OWSlideProfile profile, double my) {
        elapsed++;
        double run = owner.slideRunReference();

        if (owner.isInWater() || owner.isInLava()) {
            finish(profile, true);
            return;
        }

        if (owner.onGround()) slick = groundSlickness();
        double max = run * profile.topSpeedRatio(slick);

        float grip = (float) Mth.lerp(slick, 1.0, profile.iceSteerGrip());
        yaw = Mth.approachDegrees(yaw, player.getYRot(), profile.steerDegrees() * grip);

        if (owner.onGround()) {
            speed *= profile.drag(slick);
            if (my > STEP_UP_MIN) speed -= my * profile.uphillLoss();
        } else {
            speed *= profile.airDrag();
        }
        if (my < -0.02) speed += Math.min(-my, 1.0) * profile.downhillGain();
        if (owner.horizontalCollision && lastSpeed < speed * 0.7) speed = Mth.lerp(0.5, speed, lastSpeed);
        speed = Mth.clamp(speed, 0.0, max);

        applyVelocity(player);
        stickToGround();

        if (profile.canHop() && OWClientHooks.isJumpKeyDown() && owner.onGround() && !hopUsed && elapsed >= HOP_MIN_TICKS) {
            hop(profile, max);
            return;
        }

        boolean held = OWClientHooks.isSlideKeyDown();
        boolean spent = speed < run * profile.exitSpeedRatio();
        boolean minDone = elapsed >= profile.minTicks();
        if (elapsed >= profile.durationTicks(slick)
                || speed < run * MIN_SPEED_RATIO
                || (minDone && (!held || spent))) {
            finish(profile, true);
        }
    }

    private void stickToGround() {
        if (owner.onGround()) {
            airTicks = 0;
            return;
        }
        airTicks++;

        Vec3 motion = owner.getDeltaMovement();
        if (airTicks <= GROUND_SNAP_AIR_TICKS && motion.y <= 0.0) {
            AABB box = owner.getBoundingBox();
            for (double drop = GROUND_SNAP_STEP; drop <= GROUND_SNAP_MAX; drop += GROUND_SNAP_STEP) {
                if (!owner.level().noCollision(owner, box.move(0.0, -drop, 0.0))) {
                    owner.setDeltaMovement(motion.x, Math.min(motion.y, -GROUND_SNAP_FALL_SPEED), motion.z);
                    return;
                }
            }
        }

        owner.setDeltaMovement(motion.x, motion.y - AIR_STICK_GRAVITY, motion.z);
    }

    private void driveCarry(Player player, OWSlideProfile profile) {
        if (owner.onGround() || owner.isInWater() || owner.isInLava()) {
            handOff(profile);
            return;
        }
        yaw = Mth.approachDegrees(yaw, player.getYRot(), profile.steerDegrees() * 0.4f);
        speed *= profile.airDrag();
        if (owner.horizontalCollision) speed *= 0.5;
        applyVelocity(player);
    }

    private float groundSlickness() {
        BlockPos below = BlockPos.containing(owner.getX(), owner.getBoundingBox().minY - 0.5000001, owner.getZ());
        float friction = owner.level().getBlockState(below).getFriction(owner.level(), below, owner);
        return Mth.clamp((friction - 0.6f) / 0.38f, 0f, 1f);
    }

    private void applyVelocity(Player player) {
        Vec3 dir = Vec3.directionFromRotation(0f, yaw);
        owner.setDeltaMovement(dir.x * speed, owner.getDeltaMovement().y, dir.z * speed);
        owner.hasImpulse = true;
        owner.setYRot(yaw);
        owner.yBodyRot = yaw;
        float look = Mth.wrapDegrees(player.getYRot() - yaw);
        owner.yHeadRot = yaw + Mth.clamp(look, -HEAD_FREEDOM, HEAD_FREEDOM);
    }

    private void hop(OWSlideProfile profile, double max) {
        hopUsed = true;
        speed = Math.min(speed * profile.hopCarry(), max * 1.05);

        Vec3 dir = Vec3.directionFromRotation(0f, yaw);
        owner.setDeltaMovement(dir.x * speed, profile.hopLift(), dir.z * speed);
        owner.hasImpulse = true;

        owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.RAVAGER_STEP, owner.getSoundSource(), 1.0f, 0.75f, false);
        SoundEvent voice = owner.slideVoice();
        if (voice != null) {
            owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(),
                    voice, owner.getSoundSource(), 0.6f, 1.05f, false);
        }

        active = false;
        requestCooldown = profile.cooldownTicks();
        OWClientHooks.sendSlide(false);
        carry = true;
    }

    private void finish(OWSlideProfile profile, boolean notifyServer) {
        active = false;
        requestCooldown = profile.cooldownTicks();
        if (notifyServer) OWClientHooks.sendSlide(false);
        if (!owner.onGround()) carry = true;
        else handOff(profile);
    }

    private void handOff(OWSlideProfile profile) {
        carry = false;
        owner.carryRiddenSpeed((float) speed);
        handoff = profile.handoffTicks();
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    private static float lerpTick(float partialTick, float previous, float current) {
        return previous + (current - previous) * partialTick;
    }

    private void tickVisuals(OWSlideProfile profile) {
        double dx = 0.0;
        double dz = 0.0;
        if (visValid) {
            dx = owner.getX() - visX;
            dz = owner.getZ() - visZ;
            if (dx * dx + dz * dz > 16.0) {
                dx = 0.0;
                dz = 0.0;
            }
        }
        visX = owner.getX();
        visZ = owner.getZ();
        visValid = true;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        boolean local = owner.isControlledByLocalInstance();
        if (!local) {
            active = false;
            carry = false;
            pendingStart = false;
            handoff = 0;
            trackValid = false;
        }

        boolean sliding = local ? active : owner.isSliding();

        blendO = blend;
        blend = approach(blend, sliding ? 1f : 0f, sliding ? 0.3f : 0.16f);

        impactO = impact;
        if (sliding && !visWasSliding) {
            impact = 1f;
            playStart();
        } else {
            impact *= 0.78f;
        }
        visWasSliding = sliding;

        double maxSlide = owner.slideRunReference() * profile.topSpeedRatio(0f);
        double measured = local && sliding ? speed : horizontal;
        speedFactorO = speedFactor;
        float speedTarget = sliding ? (float) Mth.clamp(measured / maxSlide, 0.0, 1.0) : 0f;
        speedFactor += (speedTarget - speedFactor) * 0.2f;

        float yawDelta = Mth.wrapDegrees(owner.yBodyRot - visLastYaw);
        visLastYaw = owner.yBodyRot;
        float steerTarget = 0f;
        if (sliding) {
            LivingEntity rider = owner.getControllingPassenger();
            steerTarget = local && rider != null
                    ? Mth.clamp(Mth.wrapDegrees(rider.getYRot() - owner.yBodyRot) / STEER_VISUAL_DEGREES, -1f, 1f)
                    : Mth.clamp(yawDelta / profile.steerDegrees(), -1f, 1f);
        }
        steerO = steer;
        steer += (steerTarget - steer) * 0.16f;

        if (blend > 0.4f) spawnTrail();
    }

    private void playStart() {
        BlockState ground = owner.getBlockStateOn();
        if (!ground.isAir()) {
            SoundType type = ground.getSoundType(owner.level(), owner.getOnPos(), owner);
            owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(), type.getFallSound(),
                    owner.getSoundSource(), type.getVolume() * 1.1f, type.getPitch() * 0.7f, false);
        }
        SoundEvent voice = owner.slideVoice();
        if (voice != null) {
            owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(), voice,
                    owner.getSoundSource(), 0.7f, 0.85f + owner.getRandom().nextFloat() * 0.15f, false);
        }
    }

    private void spawnTrail() {
        if (!owner.onGround()) return;
        BlockState ground = owner.getBlockStateOn();
        if (ground.isAir()) return;

        RandomSource random = owner.getRandom();
        float intensity = speedFactor;
        float yawRad = owner.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double sx = -fz;
        double sz = fx;
        double halfWidth = owner.getBbWidth() * 0.3;

        int count = 1 + (int) (intensity * 4f);
        for (int i = 0; i < count; i++) {
            double lateral = (random.nextBoolean() ? 1.0 : -1.0) * (halfWidth + random.nextDouble() * 0.25);
            double along = owner.getBbWidth() * 0.6 - random.nextDouble() * owner.getBbWidth() * 1.07;
            owner.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    owner.getX() + fx * along + sx * lateral,
                    owner.getY() + 0.05,
                    owner.getZ() + fz * along + sz * lateral,
                    -fx * 0.15 * intensity + (random.nextDouble() - 0.5) * 0.1,
                    0.12 + random.nextDouble() * 0.15 * intensity,
                    -fz * 0.15 * intensity + (random.nextDouble() - 0.5) * 0.1);
        }

        if (intensity > 0.35f && owner.tickCount % 3 == 0) {
            owner.level().addParticle(ParticleTypes.CLOUD,
                    owner.getX() - fx * owner.getBbWidth() * 0.6, owner.getY() + 0.15, owner.getZ() - fz * owner.getBbWidth() * 0.6,
                    -fx * 0.05, 0.02, -fz * 0.05);
        }

        if (owner.tickCount % 3 == 0) {
            SoundType type = ground.getSoundType(owner.level(), owner.getOnPos(), owner);
            owner.level().playLocalSound(owner.getX(), owner.getY(), owner.getZ(), type.getStepSound(),
                    owner.getSoundSource(), type.getVolume() * (0.25f + 0.45f * intensity),
                    type.getPitch() * (0.55f + 0.25f * intensity), false);
        }
    }

    boolean isMotionActive() {
        return active || carry;
    }

    float blend(float partialTick) { return lerpTick(partialTick, blendO, blend); }

    float steer(float partialTick) { return lerpTick(partialTick, steerO, steer); }

    float impact(float partialTick) { return lerpTick(partialTick, impactO, impact); }

    float speedFactor(float partialTick) { return lerpTick(partialTick, speedFactorO, speedFactor); }

    float fovModifier() {
        OWSlideProfile profile = owner.slideProfile();
        if (profile == null) return 1f;
        return 1f + profile.fovGain() * blend * (0.5f + 0.5f * speedFactor);
    }
}
