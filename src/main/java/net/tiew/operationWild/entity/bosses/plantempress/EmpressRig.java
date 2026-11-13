package net.tiew.operationWild.entity.bosses.plantempress;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public final class EmpressRig {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String RESOURCE = "/data/ow/boss_rig/plant_empress.json";

    public static final String[] BONES = {
            "ALL",
            "body_1", "body_2", "body_3", "body_10", "head",
            "body_4", "body_5", "body_6", "head2",
            "body_7", "body_8", "body_9", "head3"};
    public static final int[] PARENT = {-1, 0, 1, 2, 3, 4, 0, 6, 7, 8, 0, 10, 11, 12};
    public static final int[] CHAIN_OF = {-1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2};
    public static final int[] HEAD_BONE = {5, 9, 13};
    public static final int[] ROOT_BONE = {1, 6, 10};
    public static final int[][] SEGMENTS = {{1, 2, 3, 4}, {6, 7, 8}, {10, 11, 12}};
    public static final float[] HEAD_SCALE = {1.0f, 0.7f, 0.7f};
    public static final float RENDER_SCALE = 1.2f;
    public static final int COUNT = BONES.length;

    private static final float MODEL_GROUND_OFFSET = 1.501f;
    private static final float PIXEL = 1f / 16f;
    private static final float SKULL_SHRINK = 0.85f;
    private static final double SKULL_MARGIN = 0.2;
    private static final double MIN_SKULL_HALF_HEIGHT = 0.6;

    private static final int TARGET_POSITION = 0;
    private static final int TARGET_ROTATION = 1;

    private static EmpressRig instance;

    private final float[][] restPivot = new float[COUNT][3];
    private final float[][] restRotation = new float[COUNT][3];
    private final float[][] skullCenter = new float[3][3];
    private final float[][] skullSize = new float[3][3];
    private final Map<EmpressAnim, Anim> anims = new EnumMap<>(EmpressAnim.class);

    private record Channel(int bone, int target, float[] times, float[] values, boolean[] catmull) {}

    private record Anim(float length, boolean looping, Channel[] channels) {}

    public static final class Pose {
        public final float[] x = new float[COUNT];
        public final float[] y = new float[COUNT];
        public final float[] z = new float[COUNT];
        public final float[] rx = new float[COUNT];
        public final float[] ry = new float[COUNT];
        public final float[] rz = new float[COUNT];
    }

    public static EmpressRig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static EmpressRig load() {
        EmpressRig rig = new EmpressRig();
        try (InputStream stream = EmpressRig.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                LOGGER.error("Plant Empress rig missing at {} (run runData)", RESOURCE);
                return rig;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            rig.read(json);
        } catch (Exception e) {
            LOGGER.error("Unable to read the Plant Empress rig", e);
        }
        return rig;
    }

    private void read(JsonObject json) {
        JsonArray bones = json.getAsJsonArray("bones");
        for (JsonElement element : bones) {
            JsonObject bone = element.getAsJsonObject();
            int index = indexOf(bone.get("name").getAsString());
            if (index < 0) continue;
            readVec(bone.getAsJsonArray("pivot"), restPivot[index]);
            readVec(bone.getAsJsonArray("rotation"), restRotation[index]);
        }
        JsonArray skulls = json.getAsJsonArray("skulls");
        for (int i = 0; i < skulls.size() && i < 3; i++) {
            JsonObject skull = skulls.get(i).getAsJsonObject();
            readVec(skull.getAsJsonArray("center"), skullCenter[i]);
            readVec(skull.getAsJsonArray("size"), skullSize[i]);
        }
        JsonObject animations = json.getAsJsonObject("animations");
        for (EmpressAnim anim : EmpressAnim.values()) {
            if (!animations.has(anim.name())) continue;
            JsonObject data = animations.getAsJsonObject(anim.name());
            JsonArray channelArray = data.getAsJsonArray("channels");
            Channel[] channels = new Channel[channelArray.size()];
            for (int c = 0; c < channels.length; c++) {
                JsonObject channel = channelArray.get(c).getAsJsonObject();
                JsonArray keys = channel.getAsJsonArray("keys");
                float[] times = new float[keys.size()];
                float[] values = new float[keys.size() * 3];
                boolean[] catmull = new boolean[keys.size()];
                for (int k = 0; k < keys.size(); k++) {
                    JsonArray key = keys.get(k).getAsJsonArray();
                    times[k] = key.get(0).getAsFloat();
                    values[k * 3] = key.get(1).getAsFloat();
                    values[k * 3 + 1] = key.get(2).getAsFloat();
                    values[k * 3 + 2] = key.get(3).getAsFloat();
                    catmull[k] = key.get(4).getAsInt() == 1;
                }
                channels[c] = new Channel(indexOf(channel.get("bone").getAsString()),
                        "position".equals(channel.get("target").getAsString()) ? TARGET_POSITION : TARGET_ROTATION,
                        times, values, catmull);
            }
            anims.put(anim, new Anim(data.get("length").getAsFloat(), data.get("looping").getAsBoolean(), channels));
        }
    }

    private static void readVec(JsonArray array, float[] out) {
        for (int i = 0; i < 3; i++) out[i] = array.get(i).getAsFloat();
    }

    public static int indexOf(String name) {
        for (int i = 0; i < COUNT; i++) if (BONES[i].equals(name)) return i;
        return -1;
    }

    public boolean isLoaded() {
        return !anims.isEmpty();
    }

    public float lengthSeconds(EmpressAnim anim) {
        Anim data = anims.get(anim);
        return data == null ? 0f : data.length();
    }

    public int lengthTicks(EmpressAnim anim) {
        return Mth.ceil(lengthSeconds(anim) * 20f);
    }

    public float restPivotX(int bone) { return restPivot[bone][0]; }
    public float restPivotY(int bone) { return restPivot[bone][1]; }
    public float restPivotZ(int bone) { return restPivot[bone][2]; }

    public void reset(Pose pose) {
        for (int i = 0; i < COUNT; i++) {
            pose.x[i] = restPivot[i][0];
            pose.y[i] = restPivot[i][1];
            pose.z[i] = restPivot[i][2];
            pose.rx[i] = restRotation[i][0];
            pose.ry[i] = restRotation[i][1];
            pose.rz[i] = restRotation[i][2];
        }
    }

    public void apply(Pose pose, EmpressAnim animation, float seconds, float weight, int mask) {
        if (weight <= 0f) return;
        Anim anim = anims.get(animation);
        if (anim == null) return;
        float time = anim.looping() ? positiveModulo(seconds, anim.length()) : Math.max(0f, seconds);
        float[] out = new float[3];
        for (Channel channel : anim.channels()) {
            int bone = channel.bone();
            if (bone < 0) continue;
            if (mask >= 0 && CHAIN_OF[bone] != mask) continue;
            sample(channel, time, weight, out);
            if (channel.target() == TARGET_POSITION) {
                pose.x[bone] += out[0];
                pose.y[bone] += out[1];
                pose.z[bone] += out[2];
            } else {
                pose.rx[bone] += out[0];
                pose.ry[bone] += out[1];
                pose.rz[bone] += out[2];
            }
        }
    }

    public void relaxChain(Pose pose, int chain, float keep) {
        for (int bone = 0; bone < COUNT; bone++) {
            if (CHAIN_OF[bone] != chain) continue;
            pose.x[bone] = Mth.lerp(keep, restPivot[bone][0], pose.x[bone]);
            pose.y[bone] = Mth.lerp(keep, restPivot[bone][1], pose.y[bone]);
            pose.z[bone] = Mth.lerp(keep, restPivot[bone][2], pose.z[bone]);
            pose.rx[bone] = Mth.lerp(keep, restRotation[bone][0], pose.rx[bone]);
            pose.ry[bone] = Mth.lerp(keep, restRotation[bone][1], pose.ry[bone]);
            pose.rz[bone] = Mth.lerp(keep, restRotation[bone][2], pose.rz[bone]);
        }
    }

    private static void sample(Channel channel, float time, float weight, float[] out) {
        float[] times = channel.times();
        int n = times.length;
        int lo = 0;
        int hi = n;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (time <= times[mid]) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        int i = Math.max(0, lo - 1);
        int j = Math.min(n - 1, i + 1);
        float delta = time - times[i];
        float f = j != i ? Mth.clamp(delta / (times[j] - times[i]), 0f, 1f) : 0f;
        float[] v = channel.values();
        if (channel.catmull()[j]) {
            int a = Math.max(0, i - 1);
            int d = Math.min(n - 1, j + 1);
            for (int k = 0; k < 3; k++) {
                out[k] = Mth.catmullrom(f, v[a * 3 + k], v[i * 3 + k], v[j * 3 + k], v[d * 3 + k]) * weight;
            }
        } else {
            for (int k = 0; k < 3; k++) {
                out[k] = Mth.lerp(f, v[i * 3 + k], v[j * 3 + k]) * weight;
            }
        }
    }

    private static float positiveModulo(float value, float length) {
        if (length <= 0f) return 0f;
        float result = value % length;
        return result < 0f ? result + length : result;
    }

    public static Matrix4f entityMatrix(double x, double y, double z, float bodyYaw) {
        return new Matrix4f()
                .translate((float) x, (float) y, (float) z)
                .scale(RENDER_SCALE)
                .rotateY((180f - bodyYaw) * Mth.DEG_TO_RAD)
                .scale(-1f, -1f, 1f)
                .translate(0f, -MODEL_GROUND_OFFSET, 0f);
    }

    public Matrix4f[] solve(Pose pose, Matrix4f entity) {
        Matrix4f[] world = new Matrix4f[COUNT];
        for (int bone = 0; bone < COUNT; bone++) {
            Matrix4f parent = PARENT[bone] < 0 ? entity : world[PARENT[bone]];
            Matrix4f m = new Matrix4f(parent).translate(pose.x[bone] * PIXEL, pose.y[bone] * PIXEL, pose.z[bone] * PIXEL);
            if (pose.rx[bone] != 0f || pose.ry[bone] != 0f || pose.rz[bone] != 0f) {
                m.rotateZYX(pose.rz[bone], pose.ry[bone], pose.rx[bone]);
            }
            for (int chain = 0; chain < 3; chain++) {
                if (HEAD_BONE[chain] == bone && HEAD_SCALE[chain] != 1f) m.scale(HEAD_SCALE[chain]);
            }
            world[bone] = m;
        }
        return world;
    }

    public static Vec3 origin(Matrix4f[] world, int bone) {
        Vector3f v = world[bone].transformPosition(new Vector3f());
        return new Vec3(v.x, v.y, v.z);
    }

    public static Vec3 point(Matrix4f[] world, int bone, float px, float py, float pz) {
        Vector3f v = world[bone].transformPosition(new Vector3f(px * PIXEL, py * PIXEL, pz * PIXEL));
        return new Vec3(v.x, v.y, v.z);
    }

    public Vec3 skullCenter(Matrix4f[] world, int chain) {
        float[] c = skullCenter[chain];
        return point(world, HEAD_BONE[chain], c[0], c[1], c[2]);
    }

    public Vec3 mouthPoint(Matrix4f[] world, int chain) {
        float[] c = skullCenter[chain];
        float[] s = skullSize[chain];
        return point(world, HEAD_BONE[chain], c[0], c[1], c[2] - s[2] * 0.42f);
    }

    public AABB skullBox(Matrix4f[] world, int chain) {
        float[] c = skullCenter[chain];
        float[] s = skullSize[chain];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int corner = 0; corner < 8; corner++) {
            float px = c[0] + ((corner & 1) == 0 ? -0.5f : 0.5f) * s[0];
            float py = c[1] + ((corner & 2) == 0 ? -0.5f : 0.5f) * s[1];
            float pz = c[2] + ((corner & 4) == 0 ? -0.5f : 0.5f) * s[2];
            Vec3 p = point(world, HEAD_BONE[chain], px, py, pz);
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
            minZ = Math.min(minZ, p.z); maxZ = Math.max(maxZ, p.z);
        }
        Vec3 center = skullCenter(world, chain);
        double hx = (maxX - minX) * 0.5 * SKULL_SHRINK;
        double hy = (maxY - minY) * 0.5 * SKULL_SHRINK;
        double hz = (maxZ - minZ) * 0.5 * SKULL_SHRINK;
        double horizontal = Math.max(hx, hz) + SKULL_MARGIN;
        hy = Math.max(hy, MIN_SKULL_HALF_HEIGHT) + SKULL_MARGIN;
        return new AABB(center.x - horizontal, center.y - hy, center.z - horizontal,
                center.x + horizontal, center.y + hy, center.z + horizontal);
    }

    public Vec3 segmentMiddle(Matrix4f[] world, int bone) {
        Vec3 start = origin(world, bone);
        int child = -1;
        for (int i = 0; i < COUNT; i++) if (PARENT[i] == bone) child = i;
        Vec3 end = child >= 0 ? origin(world, child) : start;
        return start.add(end).scale(0.5);
    }

    public Vector3f toModelSpace(Matrix4f entity, int bone, Pose pose, Vec3 target) {
        Matrix4f frame = new Matrix4f(entity);
        int current = bone;
        java.util.ArrayDeque<Integer> chain = new java.util.ArrayDeque<>();
        while (current >= 0) {
            chain.push(current);
            current = PARENT[current];
        }
        for (int b : chain) {
            frame.translate(pose.x[b] * PIXEL, pose.y[b] * PIXEL, pose.z[b] * PIXEL);
            if (b != bone && (pose.rx[b] != 0f || pose.ry[b] != 0f || pose.rz[b] != 0f)) {
                frame.rotateZYX(pose.rz[b], pose.ry[b], pose.rx[b]);
            }
        }
        return frame.invert().transformPosition(new Vector3f((float) target.x, (float) target.y, (float) target.z));
    }

    public static float yawToward(Vector3f local) {
        return (float) Math.atan2(-local.x, -local.z);
    }
}
