package net.tiew.operationWild.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class OWHeadHitbox {

    private static final float PIXEL = 1f / 16f;
    private static final float MODEL_GROUND_OFFSET = 1.501f;
    private static final float ROOT_PITCH_THRESHOLD = 0.01f;
    private static final double BACKTRACK = 0.5;
    private static final double MIN_SPEED_SQR = 1.0E-4;
    private static final float PARALLEL_EPSILON = 1.0E-7f;

    private record Bone(float x, float y, float z, float xRot, float yRot, float zRot) {}

    private record Box(String part, int bone, boolean head, @Nullable Matrix4f local,
                       float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}

    private final List<Bone> bones;
    private final List<Box> boxes;
    private final float yawLimit;
    private final float pitchLimit;
    private final float babyHeadScale;
    private final float babyHeadShrink;

    private OWHeadHitbox(List<Bone> bones, List<Box> boxes, float yawLimit, float pitchLimit,
                         float babyHeadScale, float babyHeadShrink) {
        this.bones = List.copyOf(bones);
        this.boxes = List.copyOf(boxes);
        this.yawLimit = yawLimit;
        this.pitchLimit = pitchLimit;
        this.babyHeadScale = babyHeadScale;
        this.babyHeadShrink = babyHeadShrink;
    }

    public static OWHeadHitbox fromJson(JsonObject json) {
        List<Bone> bones = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "bones")) {
            JsonObject bone = element.getAsJsonObject();
            float[] pivot = floats(GsonHelper.getAsJsonArray(bone, "pivot"), 3);
            float[] rotation = floats(GsonHelper.getAsJsonArray(bone, "rotation"), 3);
            bones.add(new Bone(pivot[0], pivot[1], pivot[2], rotation[0], rotation[1], rotation[2]));
        }
        if (bones.isEmpty()) throw new IllegalArgumentException("no bones");

        List<Box> boxes = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "boxes")) {
            JsonObject box = element.getAsJsonObject();
            int bone = GsonHelper.getAsInt(box, "bone");
            if (bone < 0 || bone >= bones.size()) throw new IllegalArgumentException("bad bone index " + bone);
            float[] min = floats(GsonHelper.getAsJsonArray(box, "min"), 3);
            float[] max = floats(GsonHelper.getAsJsonArray(box, "max"), 3);
            Matrix4f local = box.has("transform") ? new Matrix4f().set(floats(GsonHelper.getAsJsonArray(box, "transform"), 16)) : null;
            boxes.add(new Box(GsonHelper.getAsString(box, "part", ""), bone, GsonHelper.getAsBoolean(box, "head"), local,
                    min[0] * PIXEL, min[1] * PIXEL, min[2] * PIXEL, max[0] * PIXEL, max[1] * PIXEL, max[2] * PIXEL));
        }
        if (boxes.stream().noneMatch(Box::head)) throw new IllegalArgumentException("no head box");

        float babyScale = 0f;
        float babyShrink = 0f;
        if (json.has("baby_head")) {
            float[] baby = floats(GsonHelper.getAsJsonArray(json, "baby_head"), 2);
            babyScale = baby[0];
            babyShrink = baby[1];
        }
        return new OWHeadHitbox(bones, boxes, GsonHelper.getAsFloat(json, "yaw_limit", 30f),
                GsonHelper.getAsFloat(json, "pitch_limit", 30f), babyScale, babyShrink);
    }

    private static float[] floats(JsonArray array, int size) {
        if (array.size() != size) throw new IllegalArgumentException("expected " + size + " values, got " + array.size());
        float[] values = new float[size];
        for (int i = 0; i < size; i++) values[i] = array.get(i).getAsFloat();
        return values;
    }

    public boolean isHeadshot(OWEntity entity, Entity projectile, Set<String> excludedParts) {
        Vec3 motion = projectile.getDeltaMovement();
        if (motion.lengthSqr() < MIN_SPEED_SQR) return false;

        Vec3 heading = motion.normalize();
        Vec3 start = projectile.position().subtract(heading.scale(BACKTRACK)).subtract(entity.position());
        double reach = BACKTRACK + motion.length() + Math.max(entity.getBbWidth(), entity.getBbHeight());

        Matrix4f[] poses = pose(entity);
        Matrix4f frame = new Matrix4f();
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();

        double nearest = Double.MAX_VALUE;
        boolean head = false;
        for (Box box : boxes) {
            if (!excludedParts.isEmpty() && excludedParts.contains(box.part())) continue;
            frame.set(poses[box.bone()]);
            if (box.local() != null) frame.mul(box.local());
            frame.invert();
            frame.transformPosition((float) start.x, (float) start.y, (float) start.z, origin);
            frame.transformDirection((float) heading.x, (float) heading.y, (float) heading.z, direction);
            double entry = entry(box, origin, direction);
            if (entry < 0 || entry > reach) continue;
            if (entry < nearest || (entry == nearest && box.head())) {
                nearest = entry;
                head = box.head();
            }
        }
        return head;
    }

    private Matrix4f[] pose(OWEntity entity) {
        Matrix4f matrix = new Matrix4f()
                .scale(entity.modelRenderScale())
                .rotate(new Quaternionf().rotationY((180f - entity.yBodyRot) * Mth.DEG_TO_RAD))
                .scale(-1f, -1f, 1f)
                .translate(0f, -MODEL_GROUND_OFFSET, 0f);

        float rootPitch = entity.headHitboxRootPitch();
        float yaw = clampLook(Mth.wrapDegrees(entity.getYHeadRot() - entity.yBodyRot), yawLimit);
        float pitch = clampLook(entity.getXRot(), pitchLimit);
        int headIndex = bones.size() - 1;

        Matrix4f[] poses = new Matrix4f[bones.size()];
        for (int i = 0; i < bones.size(); i++) {
            Bone bone = bones.get(i);
            matrix = new Matrix4f(matrix).translate(bone.x() * PIXEL, bone.y() * PIXEL, bone.z() * PIXEL);

            float xRot = bone.xRot();
            float yRot = bone.yRot();
            float zRot = bone.zRot();
            if (i == 0 && Math.abs(rootPitch) > ROOT_PITCH_THRESHOLD) xRot = rootPitch * Mth.DEG_TO_RAD;
            if (i == headIndex) {
                xRot = pitch * Mth.DEG_TO_RAD;
                yRot = yaw * Mth.DEG_TO_RAD;
            }
            if (xRot != 0f || yRot != 0f || zRot != 0f) matrix.rotate(new Quaternionf().rotationZYX(zRot, yRot, xRot));

            if (i == headIndex && entity.isBaby() && babyHeadScale > 0f) {
                matrix.scale(babyHeadScale - babyHeadShrink * entity.getMaturationPercentage() / 100f);
            }
            poses[i] = matrix;
        }
        return poses;
    }

    private static float clampLook(float angle, float limit) {
        return limit <= 0f ? 0f : Mth.clamp(angle, -limit, limit);
    }

    private static double entry(Box box, Vector3f origin, Vector3f direction) {
        double near = 0.0;
        double far = Double.MAX_VALUE;
        float[] from = {origin.x, origin.y, origin.z};
        float[] towards = {direction.x, direction.y, direction.z};
        float[] min = {box.minX(), box.minY(), box.minZ()};
        float[] max = {box.maxX(), box.maxY(), box.maxZ()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(towards[axis]) < PARALLEL_EPSILON) {
                if (from[axis] < min[axis] || from[axis] > max[axis]) return -1;
                continue;
            }
            double first = (min[axis] - from[axis]) / towards[axis];
            double second = (max[axis] - from[axis]) / towards[axis];
            near = Math.max(near, Math.min(first, second));
            far = Math.min(far, Math.max(first, second));
            if (near > far) return -1;
        }
        return near;
    }
}
