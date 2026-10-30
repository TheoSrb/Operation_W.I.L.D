package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public final class OWPoseMath {

    private OWPoseMath() {}

    public static void blend(ModelPart part, float weight, float xRot, float yRot, float zRot) {
        part.xRot = Mth.lerp(weight, part.xRot, xRot);
        part.yRot = Mth.lerp(weight, part.yRot, yRot);
        part.zRot = Mth.lerp(weight, part.zRot, zRot);
    }

    public static void add(ModelPart part, float weight, float xRot, float yRot, float zRot) {
        part.xRot += xRot * weight;
        part.yRot += yRot * weight;
        part.zRot += zRot * weight;
    }

    public static void syncLayers(PlayerModel<?> model) {
        model.hat.copyFrom(model.head);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.jacket.copyFrom(model.body);
    }

    public static float smoothStep(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    public static float approach(float current, float target, float rate) {
        if (current < target) return Math.min(target, current + rate);
        return Math.max(target, current - rate);
    }

    public static float rad(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }

    public static float seed(Entity entity) {
        return (entity.getUUID().getLeastSignificantBits() & 1023L) / 1023f * Mth.TWO_PI;
    }

    public static float drift(float time, float seed) {
        return Mth.sin(time * 0.115f + seed) * 0.55f
                + Mth.sin(time * 0.27f + seed * 2.3f) * 0.3f
                + Mth.sin(time * 0.053f + seed * 0.7f) * 0.15f;
    }

    public static float pulse(float progress, float attack) {
        if (progress <= 0f || progress >= 1f) return 0f;
        if (progress < attack) return smoothStep(progress / attack);
        return 1f - smoothStep((progress - attack) / (1f - attack));
    }

    public static void inBodyFrame(PoseStack poseStack, float bodyYaw, float pivotX, float pivotY, float pivotZ,
                                   Quaternionf rotation, float moveX, float moveY, float moveZ) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poseStack.translate(pivotX + moveX, pivotY + moveY, pivotZ + moveZ);
        poseStack.mulPose(rotation);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw));
    }

    public static Quaternionf euler(float pitchDegrees, float yawDegrees, float rollDegrees) {
        Quaternionf rotation = Axis.YP.rotationDegrees(yawDegrees);
        rotation.mul(Axis.XP.rotationDegrees(pitchDegrees));
        rotation.mul(Axis.ZP.rotationDegrees(rollDegrees));
        return rotation;
    }
}
