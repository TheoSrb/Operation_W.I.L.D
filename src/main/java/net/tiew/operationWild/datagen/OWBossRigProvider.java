package net.tiew.operationWild.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressAnim;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressRig;
import net.tiew.operationWild.entity.client.animation.PlantEmpressAnimationSet;
import net.tiew.operationWild.entity.client.model.PlantEmpressModel;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OWBossRigProvider implements DataProvider {

    private static final float PLANE = 0.01f;

    private final PackOutput output;

    public OWBossRigProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path target = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(OperationWild.MOD_ID).resolve("boss_rig").resolve("plant_empress.json");
        return DataProvider.saveStable(cache, build(), target);
    }

    private JsonObject build() {
        ModelPart root = PlantEmpressModel.createBodyLayer().bakeRoot();
        PlantEmpressModel<?> model = new PlantEmpressModel<>(root);
        ModelPart[] bones = new ModelPart[EmpressRig.COUNT];
        for (int i = 0; i < EmpressRig.COUNT; i++) {
            String name = EmpressRig.BONES[i];
            bones[i] = model.getAnyDescendantWithName(name).orElseThrow(() -> new IllegalStateException("Missing bone " + name));
        }

        JsonObject json = new JsonObject();
        JsonArray boneArray = new JsonArray();
        for (int i = 0; i < EmpressRig.COUNT; i++) {
            ModelPart part = bones[i];
            JsonObject bone = new JsonObject();
            bone.addProperty("name", EmpressRig.BONES[i]);
            bone.add("pivot", vector(part.x, part.y, part.z));
            bone.add("rotation", vector(part.xRot, part.yRot, part.zRot));
            boneArray.add(bone);
        }
        json.add("bones", boneArray);

        JsonArray skulls = new JsonArray();
        for (int chain = 0; chain < 3; chain++) {
            ModelPart head = bones[EmpressRig.HEAD_BONE[chain]];
            float[] min = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
            float[] max = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            PoseStack stack = new PoseStack();
            stack.translate(-head.x / 16f, -head.y / 16f, -head.z / 16f);
            head.visit(stack, (pose, path, index, cube) -> {
                float dx = cube.maxX - cube.minX;
                float dy = cube.maxY - cube.minY;
                float dz = cube.maxZ - cube.minZ;
                if (dx < PLANE || dy < PLANE || dz < PLANE) return;
                for (int corner = 0; corner < 8; corner++) {
                    float x = (corner & 1) == 0 ? cube.minX : cube.maxX;
                    float y = (corner & 2) == 0 ? cube.minY : cube.maxY;
                    float z = (corner & 4) == 0 ? cube.minZ : cube.maxZ;
                    Vector3f v = pose.pose().transformPosition(new Vector3f(x / 16f, y / 16f, z / 16f)).mul(16f);
                    min[0] = Math.min(min[0], v.x); max[0] = Math.max(max[0], v.x);
                    min[1] = Math.min(min[1], v.y); max[1] = Math.max(max[1], v.y);
                    min[2] = Math.min(min[2], v.z); max[2] = Math.max(max[2], v.z);
                }
            });
            JsonObject skull = new JsonObject();
            skull.add("center", vector((min[0] + max[0]) / 2f, (min[1] + max[1]) / 2f, (min[2] + max[2]) / 2f));
            skull.add("size", vector(max[0] - min[0], max[1] - min[1], max[2] - min[2]));
            skulls.add(skull);
        }
        json.add("skulls", skulls);

        JsonObject animations = new JsonObject();
        for (EmpressAnim anim : EmpressAnim.values()) {
            AnimationDefinition definition = PlantEmpressAnimationSet.get(anim);
            JsonObject data = new JsonObject();
            data.addProperty("length", definition.lengthInSeconds());
            data.addProperty("looping", definition.looping());
            JsonArray channels = new JsonArray();
            for (Map.Entry<String, List<AnimationChannel>> entry : definition.boneAnimations().entrySet()) {
                if (EmpressRig.indexOf(entry.getKey()) < 0) continue;
                for (AnimationChannel channel : entry.getValue()) {
                    String target;
                    if (channel.target() == AnimationChannel.Targets.POSITION) target = "position";
                    else if (channel.target() == AnimationChannel.Targets.ROTATION) target = "rotation";
                    else continue;
                    JsonObject c = new JsonObject();
                    c.addProperty("bone", entry.getKey());
                    c.addProperty("target", target);
                    JsonArray keys = new JsonArray();
                    for (Keyframe keyframe : channel.keyframes()) {
                        JsonArray key = new JsonArray();
                        key.add(keyframe.timestamp());
                        key.add(keyframe.target().x());
                        key.add(keyframe.target().y());
                        key.add(keyframe.target().z());
                        key.add(keyframe.interpolation() == AnimationChannel.Interpolations.CATMULLROM ? 1 : 0);
                        keys.add(key);
                    }
                    c.add("keys", keys);
                    channels.add(c);
                }
            }
            data.add("channels", channels);
            animations.add(anim.name(), data);
        }
        json.add("animations", animations);
        return json;
    }

    private static JsonArray vector(float x, float y, float z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    @Override
    public String getName() {
        return "Operation W.I.L.D boss rigs";
    }
}
