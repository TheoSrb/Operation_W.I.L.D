package net.tiew.operationWild.entity.platform;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class OWPlatformPhysics {

    private static final int AIR_GRACE_TICKS = 30;
    private static final double MAX_CARRY_STEP_SQR = 16.0;

    private static final Map<Entity, Contact> CLIENT_CONTACTS = new WeakHashMap<>();
    private static final Map<Entity, Contact> SERVER_CONTACTS = new WeakHashMap<>();

    private OWPlatformPhysics() {
    }

    private static final class Contact {
        OWPlatform platform;
        Vec3 grid;
        float yaw;
        int lastGround;
    }

    private static Map<Entity, Contact> contacts(Entity entity) {
        return entity.level().isClientSide() ? CLIENT_CONTACTS : SERVER_CONTACTS;
    }

    public static boolean isExcluded(Entity entity, OWPlatform platform) {
        Entity carrier = platform.carrier();
        if (entity == carrier || entity.noPhysics || entity.isSpectator()) return true;
        if (entity.isPassenger()) return true;
        return entity.getRootVehicle() == carrier.getRootVehicle();
    }

    public static Vec3 collide(Entity entity, Vec3 collided) {
        if (OWPlatformRegistry.isEmpty(entity.level())) return collided;
        AABB box = entity.getBoundingBox();
        List<OWPlatform> platforms = OWPlatformRegistry.near(entity.level(), box.expandTowards(collided).inflate(1.0));
        Vec3 result = collided;
        for (OWPlatform platform : platforms) {
            if (isExcluded(entity, platform) || !platform.isCollidable()) continue;
            result = collideWith(entity, platform, box, result);
        }
        Contact contact = contacts(entity).get(entity);
        if (contact != null && contact.platform.isActive()) {
            contact.grid = contact.platform.current().toGrid(entity.position().add(result));
        }
        return result;
    }

    private static Vec3 collideWith(Entity entity, OWPlatform platform, AABB box, Vec3 movement) {
        OWPlatformTransform transform = platform.current();
        Vec3 center = transform.toGrid(box.getCenter());
        double hx = box.getXsize() / 2.0;
        double hy = box.getYsize() / 2.0;
        double hz = box.getZsize() / 2.0;
        AABB gridBox = new AABB(center.x - hx, center.y - hy, center.z - hz, center.x + hx, center.y + hy, center.z + hz);
        Vec3 gridMove = transform.vectorToGrid(movement);
        if (!platform.gridBounds().inflate(0.5).intersects(gridBox.expandTowards(gridMove))) return movement;

        List<VoxelShape> shapes = platform.collisionShapes();
        Vec3 result = collideWithShapes(gridMove, gridBox, shapes);
        boolean horizontalBlocked = result.x != gridMove.x || result.z != gridMove.z;
        boolean downBlocked = gridMove.y < 0 && result.y != gridMove.y;

        float step = entity.maxUpStep();
        if (step > 0f && horizontalBlocked && (downBlocked || entity.onGround())) {
            double up = Shapes.collide(Direction.Axis.Y, gridBox, shapes, step);
            AABB raised = gridBox.move(0, up, 0);
            Vec3 horizontal = collideWithShapes(new Vec3(gridMove.x, 0, gridMove.z), raised, shapes);
            AABB moved = raised.move(horizontal.x, 0, horizontal.z);
            double down = Shapes.collide(Direction.Axis.Y, moved, shapes, -up + Math.min(gridMove.y, 0));
            Vec3 stepped = new Vec3(horizontal.x, up + down, horizontal.z);
            if (stepped.horizontalDistanceSqr() > result.horizontalDistanceSqr() + 1.0E-7) {
                result = stepped;
                downBlocked = true;
            }
        }

        if (downBlocked || isRestingOn(gridBox.move(result), shapes)) {
            recordContact(entity, platform, transform);
        }

        if (result.equals(gridMove)) return movement;
        Vec3 world = transform.vectorToWorld(result);
        return new Vec3(result.x == gridMove.x && result.z == gridMove.z ? movement.x : world.x, result.y,
                result.x == gridMove.x && result.z == gridMove.z ? movement.z : world.z);
    }

    private static boolean isRestingOn(AABB gridBox, List<VoxelShape> shapes) {
        return Shapes.collide(Direction.Axis.Y, gridBox, shapes, -0.05) > -0.05;
    }

    private static Vec3 collideWithShapes(Vec3 movement, AABB box, List<VoxelShape> shapes) {
        double dx = movement.x;
        double dy = movement.y;
        double dz = movement.z;
        if (dy != 0.0) {
            dy = Shapes.collide(Direction.Axis.Y, box, shapes, dy);
            if (dy != 0.0) box = box.move(0.0, dy, 0.0);
        }
        boolean zFirst = Math.abs(dx) < Math.abs(dz);
        if (zFirst && dz != 0.0) {
            dz = Shapes.collide(Direction.Axis.Z, box, shapes, dz);
            if (dz != 0.0) box = box.move(0.0, 0.0, dz);
        }
        if (dx != 0.0) {
            dx = Shapes.collide(Direction.Axis.X, box, shapes, dx);
            if (!zFirst && dx != 0.0) box = box.move(dx, 0.0, 0.0);
        }
        if (!zFirst && dz != 0.0) {
            dz = Shapes.collide(Direction.Axis.Z, box, shapes, dz);
        }
        return new Vec3(dx, dy, dz);
    }

    private static void recordContact(Entity entity, OWPlatform platform, OWPlatformTransform transform) {
        Map<Entity, Contact> map = contacts(entity);
        Contact contact = map.get(entity);
        if (contact == null || contact.platform != platform) {
            contact = new Contact();
            contact.platform = platform;
            contact.yaw = transform.yaw;
            contact.grid = transform.toGrid(entity.position());
            map.put(entity, contact);
        }
        contact.lastGround = entity.tickCount;
    }

    private static boolean simulatesHere(Entity entity) {
        if (entity.level().isClientSide()) {
            return (entity instanceof Player player && player.isLocalPlayer()) || entity instanceof ItemEntity;
        }
        return !(entity instanceof Player);
    }

    public static void carry(Entity entity) {
        Map<Entity, Contact> map = contacts(entity);
        if (map.isEmpty()) return;
        Contact contact = map.get(entity);
        if (contact == null) return;
        OWPlatform platform = contact.platform;
        if (!platform.isActive() || !platform.isCollidable() || platform.carrier().level() != entity.level() || entity.isPassenger()
                || entity.tickCount - contact.lastGround > AIR_GRACE_TICKS || isExcluded(entity, platform)) {
            map.remove(entity);
            return;
        }
        if (!simulatesHere(entity) || contact.grid == null) return;

        OWPlatformTransform transform = platform.current();
        Vec3 target = transform.toWorld(contact.grid);
        Vec3 delta = target.subtract(entity.position());
        if (delta.lengthSqr() > MAX_CARRY_STEP_SQR) {
            map.remove(entity);
            return;
        }
        float yawDelta = Mth.wrapDegrees(transform.yaw - contact.yaw);
        contact.yaw = transform.yaw;
        if (delta.lengthSqr() > 1.0E-10) entity.setPos(target);
        if (yawDelta != 0f) {
            entity.setYRot(entity.getYRot() + yawDelta);
            entity.setYHeadRot(entity.getYHeadRot() + yawDelta);
            if (entity instanceof LivingEntity living) living.yBodyRot += yawDelta;
        }
    }

    public static boolean isStandingOn(Entity entity, Entity carrier) {
        Contact contact = contacts(entity).get(entity);
        return contact != null && contact.platform.carrier() == carrier
                && entity.tickCount - contact.lastGround <= AIR_GRACE_TICKS;
    }

    public static boolean hasRecentContact(Entity entity) {
        Contact contact = contacts(entity).get(entity);
        return contact != null && contact.platform.isActive() && entity.tickCount - contact.lastGround <= AIR_GRACE_TICKS;
    }

    @Nullable
    public static OWPlatform contactPlatform(Entity entity) {
        Contact contact = contacts(entity).get(entity);
        return contact == null ? null : contact.platform;
    }
}
