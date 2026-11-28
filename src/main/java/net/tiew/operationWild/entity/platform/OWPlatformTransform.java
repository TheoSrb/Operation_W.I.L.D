package net.tiew.operationWild.entity.platform;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class OWPlatformTransform {

    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    private final double cos;
    private final double sin;
    private final double halfWidth;
    private final double halfLength;

    public OWPlatformTransform(double x, double y, double z, float yaw, double halfWidth, double halfLength) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        double radians = Math.toRadians(yaw);
        this.cos = Math.cos(radians);
        this.sin = Math.sin(radians);
        this.halfWidth = halfWidth;
        this.halfLength = halfLength;
    }

    public static OWPlatformTransform of(Entity carrier, OWPlatformProfile profile, float scale, double drop) {
        return build(carrier.getX(), carrier.getY() - drop * scale, carrier.getZ(), carrier.getYRot(), profile, scale);
    }

    public static OWPlatformTransform interpolated(Entity carrier, OWPlatformProfile profile, float scale, double drop, float partialTick) {
        double px = Mth.lerp(partialTick, carrier.xo, carrier.getX());
        double py = Mth.lerp(partialTick, carrier.yo, carrier.getY()) - drop * scale;
        double pz = Mth.lerp(partialTick, carrier.zo, carrier.getZ());
        float yaw = carrier instanceof net.minecraft.world.entity.LivingEntity living
                ? Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot)
                : Mth.rotLerp(partialTick, carrier.yRotO, carrier.getYRot());
        return build(px, py, pz, yaw, profile, scale);
    }

    private static OWPlatformTransform build(double px, double py, double pz, float yaw, OWPlatformProfile profile, float scale) {
        double radians = Math.toRadians(yaw);
        double forward = profile.anchorForward() * scale;
        double ax = px - Math.sin(radians) * forward;
        double az = pz + Math.cos(radians) * forward;
        double ay = py + profile.anchorUp() * scale;
        return new OWPlatformTransform(ax, ay, az, yaw, profile.centerX(), profile.centerZ());
    }

    public Vec3 toWorld(double gx, double gy, double gz) {
        double lx = gx - halfWidth;
        double lz = gz - halfLength;
        return new Vec3(x + lx * cos - lz * sin, y + gy, z + lx * sin + lz * cos);
    }

    public Vec3 toWorld(Vec3 grid) {
        return toWorld(grid.x, grid.y, grid.z);
    }

    public Vec3 toGrid(double wx, double wy, double wz) {
        double dx = wx - x;
        double dz = wz - z;
        double lx = dx * cos + dz * sin;
        double lz = -dx * sin + dz * cos;
        return new Vec3(lx + halfWidth, wy - y, lz + halfLength);
    }

    public Vec3 toGrid(Vec3 world) {
        return toGrid(world.x, world.y, world.z);
    }

    public Vec3 vectorToWorld(Vec3 grid) {
        return new Vec3(grid.x * cos - grid.z * sin, grid.y, grid.x * sin + grid.z * cos);
    }

    public Vec3 vectorToGrid(Vec3 world) {
        return new Vec3(world.x * cos + world.z * sin, world.y, -world.x * sin + world.z * cos);
    }

    public AABB worldBounds(double radius, double below, double above) {
        return new AABB(x - radius, y - below, z - radius, x + radius, y + above, z + radius);
    }
}
