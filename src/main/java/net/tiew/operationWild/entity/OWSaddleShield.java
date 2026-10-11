package net.tiew.operationWild.entity;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public record OWSaddleShield(AABB panel) {

    public static final int AXE_DISABLE_TICKS = 100;

    private static final double PIXEL = 1.0 / 16.0;
    private static final double MODEL_GROUND = 24.0;
    private static final double HIT_MARGIN = 0.05;
    private static final double RAY_LENGTH = 16.0;
    private static final double PROJECTILE_BACKTRACK = 1.0;

    private record Ray(Vec3 from, Vec3 to) {}

    public static OWSaddleShield modelPanel(float rootPivotX, float rootPivotY, float rootPivotZ,
                                            float xRot, float yRot, float zRot,
                                            float boxX, float boxY, float boxZ,
                                            float sizeX, float sizeY, float sizeZ) {
        Quaternionf rotation = new Quaternionf().rotationZYX(zRot, yRot, xRot);
        double minLeft = Double.MAX_VALUE, minUp = Double.MAX_VALUE, minForward = Double.MAX_VALUE;
        double maxLeft = -Double.MAX_VALUE, maxUp = -Double.MAX_VALUE, maxForward = -Double.MAX_VALUE;

        for (int corner = 0; corner < 8; corner++) {
            Vector3f point = new Vector3f(
                    boxX + ((corner & 1) == 0 ? 0f : sizeX),
                    boxY + ((corner & 2) == 0 ? 0f : sizeY),
                    boxZ + ((corner & 4) == 0 ? 0f : sizeZ))
                    .rotate(rotation)
                    .add(rootPivotX, rootPivotY, rootPivotZ);

            double left = point.x * PIXEL;
            double up = (MODEL_GROUND - point.y) * PIXEL;
            double forward = -point.z * PIXEL;

            minLeft = Math.min(minLeft, left);
            maxLeft = Math.max(maxLeft, left);
            minUp = Math.min(minUp, up);
            maxUp = Math.max(maxUp, up);
            minForward = Math.min(minForward, forward);
            maxForward = Math.max(maxForward, forward);
        }
        return new OWSaddleShield(new AABB(minLeft, minUp, minForward, maxLeft, maxUp, maxForward));
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
        Vec3 to = toLocal(mount, ray.to());
        double scale = mount.getScale();

        Vec3 closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (OWSaddleShield shield : shields) {
            AABB box = scaled(shield.panel(), scale).inflate(HIT_MARGIN);
            double side = Math.signum(box.getCenter().x);
            if (side == 0) continue;

            double outerFace = side > 0 ? box.maxX : box.minX;
            if ((from.x - outerFace) * side <= 0) continue;

            Optional<Vec3> hit = box.clip(from, to);
            if (hit.isEmpty()) continue;

            double distance = hit.get().distanceToSqr(from);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = hit.get();
            }
        }
        return closest == null ? null : toWorld(mount, closest);
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

    private static AABB scaled(AABB box, double scale) {
        return new AABB(box.minX * scale, box.minY * scale, box.minZ * scale,
                box.maxX * scale, box.maxY * scale, box.maxZ * scale);
    }

    private static Vec3 toLocal(OWEntity mount, Vec3 world) {
        double yaw = Math.toRadians(mount.yBodyRot);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double dx = world.x - mount.getX();
        double dz = world.z - mount.getZ();
        return new Vec3(dx * cos + dz * sin, world.y - mount.getY(), -dx * sin + dz * cos);
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
