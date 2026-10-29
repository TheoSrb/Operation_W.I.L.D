package net.tiew.operationWild.entity;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public record OWSaddleShield(String part, Pose rest, Vec3 cubeSize, int thinAxis, double outward, double coverage) {

    public static final int AXE_DISABLE_TICKS = 100;
    public static final float BLOCK_CHANCE = 0.5f;
    public static final int POSE_REPORT_LIFETIME = 20;
    public static final int MAX_SHIELDS = 4;
    public static final int FLOATS_PER_POSE = 9;

    private static final double PIXEL = 1.0 / 16.0;
    private static final double MODEL_GROUND = 24.0;
    private static final double HIT_MARGIN = 0.05;
    private static final double RAY_LENGTH = 16.0;
    private static final double PROJECTILE_BACKTRACK = 1.0;
    private static final double PARALLEL_EPSILON = 1.0E-9;
    private static final double MIN_AXIS_SQR = 1.0E-6;
    private static final double BASE_DRIFT = 0.5;
    private static final double BODY_DRIFT = 0.6;
    private static final double SIZE_TOLERANCE = 0.01;

    public record Pose(Vec3 center, Vec3 axisX, Vec3 axisY, Vec3 axisZ) {

        Vec3 axis(int index) {
            return index == 0 ? axisX : index == 1 ? axisY : axisZ;
        }
    }

    private record Ray(Vec3 from, Vec3 to) {}

    public static OWSaddleShield modelPanel(String part,
                                            float rootPivotX, float rootPivotY, float rootPivotZ,
                                            float xRot, float yRot, float zRot,
                                            float boxX, float boxY, float boxZ,
                                            float sizeX, float sizeY, float sizeZ) {
        Quaternionf rotation = new Quaternionf().rotationZYX(zRot, yRot, xRot);
        Vector3f center = new Vector3f(boxX + sizeX / 2f, boxY + sizeY / 2f, boxZ + sizeZ / 2f)
                .rotate(rotation)
                .add(rootPivotX, rootPivotY, rootPivotZ);

        Pose rest = new Pose(
                new Vec3(center.x * PIXEL, (MODEL_GROUND - center.y) * PIXEL, -center.z * PIXEL),
                modelAxis(rotation, 1f, 0f, 0f),
                modelAxis(rotation, 0f, 1f, 0f),
                modelAxis(rotation, 0f, 0f, 1f));

        int thinAxis = sizeX <= sizeY && sizeX <= sizeZ ? 0 : sizeY <= sizeZ ? 1 : 2;
        Vec3 flank = new Vec3(rest.center().x, 0, rest.center().z);
        double facing = rest.axis(thinAxis).dot(flank);
        return new OWSaddleShield(part, rest, new Vec3(sizeX, sizeY, sizeZ), thinAxis, facing < 0 ? -1.0 : 1.0, 1.0);
    }

    public OWSaddleShield withCoverage(double coverage) {
        return new OWSaddleShield(part, rest, cubeSize, thinAxis, outward, coverage);
    }

    private static Vec3 modelAxis(Quaternionf rotation, float x, float y, float z) {
        Vector3f axis = new Vector3f(x, y, z).rotate(rotation);
        return new Vec3(axis.x, -axis.y, -axis.z);
    }

    public boolean matchesCube(float sizeX, float sizeY, float sizeZ) {
        return Math.abs(sizeX - cubeSize.x) < SIZE_TOLERANCE
                && Math.abs(sizeY - cubeSize.y) < SIZE_TOLERANCE
                && Math.abs(sizeZ - cubeSize.z) < SIZE_TOLERANCE;
    }

    private double halfSize(int axis, double scale) {
        double size = axis == 0 ? cubeSize.x : axis == 1 ? cubeSize.y : cubeSize.z;
        return size * PIXEL * 0.5 * scale * (axis == thinAxis ? 1.0 : coverage);
    }

    private Pose pose(OWEntity mount, int index, double scale) {
        Pose reported = mount.reportedSaddleShieldPose(index);
        if (reported != null) return reported;
        return new Pose(rest.center().scale(scale), rest.axisX(), rest.axisY(), rest.axisZ());
    }

    @Nullable
    public Pose acceptReport(OWEntity mount, float[] data, int offset) {
        Vec3 center = vector(data, offset);
        Vec3 axisX = vector(data, offset + 3);
        Vec3 axisY = vector(data, offset + 6);
        if (axisX.lengthSqr() < MIN_AXIS_SQR || axisY.lengthSqr() < MIN_AXIS_SQR) return null;

        axisX = axisX.normalize();
        axisY = axisY.subtract(axisX.scale(axisY.dot(axisX)));
        if (axisY.lengthSqr() < MIN_AXIS_SQR) return null;
        axisY = axisY.normalize();

        double maxDrift = BASE_DRIFT + Math.max(mount.getBbWidth(), mount.getBbHeight()) * BODY_DRIFT;
        if (center.distanceToSqr(rest.center().scale(mount.modelRenderScale())) > maxDrift * maxDrift) return null;

        return new Pose(center, axisX, axisY, axisX.cross(axisY));
    }

    private static Vec3 vector(float[] data, int offset) {
        return new Vec3(data[offset], data[offset + 1], data[offset + 2]);
    }

    @Nullable
    public static Vec3 intercept(OWEntity mount, LivingEntity victim, DamageSource source) {
        if (!mount.isSaddled() || mount.isSaddleShieldDisabled()) return null;

        List<OWSaddleShield> shields = mount.saddleShields();
        if (shields.isEmpty()) return null;
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) return null;

        Entity direct = source.getDirectEntity();
        Entity attacker = source.getEntity();
        if (direct == null && attacker == null) return null;
        if (direct instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) return null;
        if (isOwnSide(mount, direct) || isOwnSide(mount, attacker)) return null;

        Ray ray = attackRay(source, direct, victim);
        if (ray == null) return null;

        Vec3 from = toLocal(mount, ray.from());
        Vec3 path = toLocal(mount, ray.to()).subtract(from);
        double scale = mount.modelRenderScale();

        double closest = Double.MAX_VALUE;
        for (int index = 0; index < shields.size(); index++) {
            OWSaddleShield shield = shields.get(index);
            double entry = shield.entry(shield.pose(mount, index, scale), from, path, scale);
            if (entry >= 0 && entry < closest) closest = entry;
        }
        return closest == Double.MAX_VALUE ? null : toWorld(mount, from.add(path.scale(closest)));
    }

    private double entry(Pose pose, Vec3 from, Vec3 path, double scale) {
        Vec3 offset = from.subtract(pose.center());

        double outer = halfSize(thinAxis, scale) + HIT_MARGIN;
        if (offset.dot(pose.axis(thinAxis)) * outward <= outer) return -1;

        double near = 0.0;
        double far = 1.0;
        for (int axis = 0; axis < 3; axis++) {
            Vec3 direction = pose.axis(axis);
            double origin = offset.dot(direction);
            double step = path.dot(direction);
            double half = halfSize(axis, scale) + HIT_MARGIN;

            if (Math.abs(step) < PARALLEL_EPSILON) {
                if (Math.abs(origin) > half) return -1;
                continue;
            }
            double first = (-half - origin) / step;
            double second = (half - origin) / step;
            near = Math.max(near, Math.min(first, second));
            far = Math.min(far, Math.max(first, second));
            if (near > far) return -1;
        }
        return near;
    }

    public static boolean isPoseReporter(OWEntity mount, Player player) {
        Entity rider = mount.getControllingPassenger();
        if (rider instanceof Player) return rider == player;
        return mount.isOwnedBy(player);
    }

    private static boolean isOwnSide(OWEntity mount, @Nullable Entity entity) {
        return entity != null && (entity == mount || mount.hasIndirectPassenger(entity));
    }

    @Nullable
    private static Ray attackRay(DamageSource source, @Nullable Entity direct, LivingEntity victim) {
        Vec3 target = victim.getBoundingBox().getCenter();

        if (direct != null && !(direct instanceof LivingEntity)) {
            Vec3 motion = direct.getDeltaMovement();
            if (motion.lengthSqr() > 1.0E-4) {
                Vec3 heading = motion.normalize();
                return ray(direct.position().subtract(heading.scale(PROJECTILE_BACKTRACK)), heading);
            }
            return ray(direct.position(), target.subtract(direct.position()));
        }

        if (direct instanceof Player player) return ray(player.getEyePosition(), player.getViewVector(1.0f));

        if (direct instanceof LivingEntity living) {
            Vec3 eye = living.getEyePosition();
            return ray(eye, target.subtract(eye));
        }

        Vec3 origin = source.getSourcePosition();
        return origin == null ? null : ray(origin, target.subtract(origin));
    }

    @Nullable
    private static Ray ray(Vec3 from, Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-8) return null;
        return new Ray(from, from.add(direction.normalize().scale(RAY_LENGTH)));
    }

    public static Vec3 toLocal(Vec3 origin, float bodyYaw, Vec3 world) {
        return rotateToLocal(bodyYaw, world.subtract(origin));
    }

    public static Vec3 rotateToLocal(float bodyYaw, Vec3 direction) {
        double yaw = Math.toRadians(bodyYaw);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new Vec3(direction.x * cos + direction.z * sin, direction.y, -direction.x * sin + direction.z * cos);
    }

    private static Vec3 toLocal(OWEntity mount, Vec3 world) {
        return toLocal(mount.position(), mount.yBodyRot, world);
    }

    private static Vec3 toWorld(OWEntity mount, Vec3 local) {
        double yaw = Math.toRadians(mount.yBodyRot);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new Vec3(
                mount.getX() + local.x * cos - local.z * sin,
                mount.getY() + local.y,
                mount.getZ() + local.x * sin + local.z * cos);
    }
}
