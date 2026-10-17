package net.tiew.operationWild.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.OWHeadHitboxes;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class OWHeadHitboxProvider implements DataProvider {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MODEL_PACKAGE = "net.tiew.operationWild.entity.client.model.";
    private static final String HEAD = "head";
    private static final float PLANE = 0.01f;
    private static final float ROD = 1.0f;
    private static final float LOOK_PROBE = 90f;

    private record Options(float babyHeadScale, float babyHeadShrink, Set<String> excludedParts,
                           Set<String> coverParts, Set<String> excludedCubes) {
        static final Options DEFAULT = new Options(0f, 0f, Set.of(), Set.of(), Set.of());

        Options baby(float scale, float shrink) {
            return new Options(scale, shrink, excludedParts, coverParts, excludedCubes);
        }

        Options exclude(String... parts) {
            return new Options(babyHeadScale, babyHeadShrink, Set.of(parts), coverParts, excludedCubes);
        }

        Options cover(String... parts) {
            return new Options(babyHeadScale, babyHeadShrink, excludedParts, Set.of(parts), excludedCubes);
        }

        Options excludeCubes(String... cubes) {
            return new Options(babyHeadScale, babyHeadShrink, excludedParts, coverParts, Set.of(cubes));
        }
    }

    private static final Map<String, Options> OPTIONS = Map.ofEntries(
            Map.entry("tiger", Options.DEFAULT.baby(1.6f, 0.6f)),
            Map.entry("lion", Options.DEFAULT.baby(1.6f, 0.6f)),
            Map.entry("kodiak", Options.DEFAULT.baby(1.6f, 0.6f)),
            Map.entry("elephant", Options.DEFAULT.baby(1.6f, 0.6f).exclude("left_ear", "right_ear", "trunk",
                    "cube_r9", "cube_r10", "cube_r11", "cube_r12", "cube_r13", "cube_r14", "cube_r15", "cube_r16")),
            Map.entry("gorilla", Options.DEFAULT.baby(1.5f, 0.5f)),
            Map.entry("crocodile", Options.DEFAULT.baby(1.5f, 0.6f)),
            Map.entry("hippopotamus", Options.DEFAULT.baby(1.5f, 0.5f)),
            Map.entry("orca", Options.DEFAULT.baby(1.5f, 0.6f).excludeCubes("head#0")),
            Map.entry("boa", Options.DEFAULT.cover("body_0").exclude("body_1")));

    private record Bone(String name, ModelPart part) {}

    private final PackOutput output;

    public OWHeadHitboxProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> entry : OWEntityRegistry.ENTITY_TYPES.getEntries()) {
            ResourceLocation id = entry.getId();
            JsonObject json = build(id.getPath());
            if (json == null) continue;
            Path target = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve(id.getNamespace()).resolve(OWHeadHitboxes.DIRECTORY).resolve(id.getPath() + ".json");
            writes.add(DataProvider.saveStable(cache, json, target));
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private JsonObject build(String id) {
        Class<?> modelClass;
        LayerDefinition layer;
        try {
            modelClass = Class.forName(MODEL_PACKAGE + pascal(id) + "Model");
            Method factory = modelClass.getMethod("createBodyLayer");
            layer = (LayerDefinition) factory.invoke(null);
        } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            return null;
        }

        ModelPart root = layer.bakeRoot();
        List<Bone> chain = findHead(root);
        if (chain == null) return null;

        Options options = OPTIONS.getOrDefault(id, Options.DEFAULT);
        String headPath = pathOf(chain);
        String headPrefix = headPath + "/";
        Map<String, Integer> chainIndex = new java.util.HashMap<>();
        for (int i = 0; i < chain.size(); i++) chainIndex.put(pathOf(chain.subList(0, i + 1)), i);

        PoseStack headPose = new PoseStack();
        root.translateAndRotate(headPose);
        for (Bone bone : chain) bone.part().translateAndRotate(headPose);
        Matrix4f toHead = new Matrix4f(headPose.last().pose()).invert();

        JsonArray boxes = new JsonArray();
        Set<String> seen = new HashSet<>();
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (isPlane(cube)) return;
            String partName = path.substring(path.lastIndexOf('/') + 1);
            Integer link = chainIndex.get(path);
            boolean inHead = path.equals(headPath) || path.startsWith(headPrefix);

            if (link != null && link < chain.size() - 1) {
                boxes.add(box(partName, link, false, null, cube));
                return;
            }
            if (!inHead) return;
            if (options.excludedCubes().contains(partName + "#" + index)) return;
            String relative = path.equals(headPath) ? "" : path.substring(headPrefix.length());
            if (anySegment(relative, options.excludedParts())) return;
            boolean cover = anySegment(relative, options.coverParts());
            if (!cover && isRod(cube)) return;
            String key = path + ":" + cube.minX + "," + cube.minY + "," + cube.minZ + "," + cube.maxX + "," + cube.maxY + "," + cube.maxZ;
            if (!seen.add(key)) return;

            Matrix4f local = path.equals(headPath) ? null : new Matrix4f(toHead).mul(pose.pose());
            boxes.add(box(partName, chain.size() - 1, !cover, local, cube));
        });

        if (boxes.isEmpty()) return null;
        boolean anyHead = false;
        for (int i = 0; i < boxes.size(); i++) anyHead |= boxes.get(i).getAsJsonObject().get("head").getAsBoolean();
        if (!anyHead) return null;

        float[] limits = probeLookLimits(modelClass, root, chain.get(chain.size() - 1).part());

        JsonObject json = new JsonObject();
        json.addProperty("yaw_limit", limits[0]);
        json.addProperty("pitch_limit", limits[1]);
        if (options.babyHeadScale() > 0f) {
            JsonArray baby = new JsonArray();
            baby.add(options.babyHeadScale());
            baby.add(options.babyHeadShrink());
            json.add("baby_head", baby);
        }
        JsonArray bones = new JsonArray();
        for (Bone bone : chain) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", bone.name());
            entry.add("pivot", vector(bone.part().x, bone.part().y, bone.part().z));
            entry.add("rotation", vector(bone.part().xRot, bone.part().yRot, bone.part().zRot));
            bones.add(entry);
        }
        json.add("bones", bones);
        json.add("boxes", boxes);
        return json;
    }

    private static List<Bone> findHead(ModelPart root) {
        List<Bone> found = new ArrayList<>();
        return search(root, found) ? found : null;
    }

    private static boolean search(ModelPart part, List<Bone> path) {
        for (Map.Entry<String, ModelPart> child : children(part).entrySet()) {
            path.add(new Bone(child.getKey(), child.getValue()));
            if (child.getKey().equals(HEAD) || search(child.getValue(), path)) return true;
            path.remove(path.size() - 1);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModelPart> children(ModelPart part) {
        try {
            java.lang.reflect.Field field = ModelPart.class.getDeclaredField("children");
            field.setAccessible(true);
            return (Map<String, ModelPart>) field.get(part);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ModelPart.children inaccessible", e);
        }
    }

    private static float[] probeLookLimits(Class<?> modelClass, ModelPart root, ModelPart head) {
        float restX = head.xRot;
        float restY = head.yRot;
        float[] limits = {30f, 30f};
        try {
            Object model = modelClass.getConstructor(ModelPart.class).newInstance(root);
            Method probe = null;
            for (Method method : modelClass.getDeclaredMethods()) {
                if (!method.getName().equals("applyHeadRotation")) continue;
                Class<?>[] types = method.getParameterTypes();
                boolean floats = types.length >= 2;
                for (Class<?> type : types) floats &= type == float.class;
                if (floats) probe = method;
            }
            if (probe != null) {
                probe.setAccessible(true);
                Object[] args = new Object[probe.getParameterCount()];
                args[0] = LOOK_PROBE;
                args[1] = LOOK_PROBE;
                for (int i = 2; i < args.length; i++) args[i] = 1f;
                probe.invoke(model, args);
                limits[0] = Math.round((float) Math.toDegrees(Math.abs(head.yRot)) * 10f) / 10f;
                limits[1] = Math.round((float) Math.toDegrees(Math.abs(head.xRot)) * 10f) / 10f;
            } else {
                limits[0] = 0f;
                limits[1] = 0f;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Head look limits not probed for {}: {}", modelClass.getSimpleName(), e.toString());
        } finally {
            head.xRot = restX;
            head.yRot = restY;
        }
        return limits;
    }

    private static JsonObject box(String part, int link, boolean head, Matrix4f local, ModelPart.Cube cube) {
        JsonObject json = new JsonObject();
        json.addProperty("part", part);
        json.addProperty("bone", link);
        json.addProperty("head", head);
        json.add("min", vector(cube.minX, cube.minY, cube.minZ));
        json.add("max", vector(cube.maxX, cube.maxY, cube.maxZ));
        if (local != null) {
            JsonArray values = new JsonArray();
            float[] raw = local.get(new float[16]);
            for (float value : raw) values.add(Math.round(value * 1.0e6f) / 1.0e6f);
            json.add("transform", values);
        }
        return json;
    }

    private static JsonArray vector(float x, float y, float z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static boolean isPlane(ModelPart.Cube cube) {
        return cube.maxX - cube.minX < PLANE || cube.maxY - cube.minY < PLANE || cube.maxZ - cube.minZ < PLANE;
    }

    private static boolean isRod(ModelPart.Cube cube) {
        int thin = 0;
        if (cube.maxX - cube.minX <= ROD) thin++;
        if (cube.maxY - cube.minY <= ROD) thin++;
        if (cube.maxZ - cube.minZ <= ROD) thin++;
        return thin >= 2;
    }

    private static boolean anySegment(String relative, Set<String> names) {
        if (relative.isEmpty() || names.isEmpty()) return false;
        for (String segment : relative.split("/")) {
            if (names.contains(segment)) return true;
        }
        return false;
    }

    private static String pathOf(List<Bone> bones) {
        StringBuilder path = new StringBuilder();
        for (Bone bone : bones) path.append('/').append(bone.name());
        return path.toString();
    }

    private static String pascal(String id) {
        StringBuilder name = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) continue;
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    @Override
    public String getName() {
        return "Operation W.I.L.D head hitboxes";
    }
}
