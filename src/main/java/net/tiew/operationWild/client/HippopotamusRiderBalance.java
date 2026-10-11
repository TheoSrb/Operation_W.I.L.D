package net.tiew.operationWild.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public final class HippopotamusRiderBalance {

    private static final float LEG_REST_Y = 12f;
    private static final float LEG_SWING = 0.62f;
    private static final float LEG_LIFT = 1.6f;
    private static final float LEG_STANCE = 0.07f;

    private static final float ARM_SPREAD = 1.3f;
    private static final float ARM_FORWARD = -0.2f;
    private static final float ARM_SEESAW = 1.6f;
    private static final float ARM_SEESAW_LAG = 4f;
    private static final float ARM_FLAIL = 0.28f;
    private static final float ARM_FLAIL_SPEED = 0.32f;
    private static final float ARM_FLAIL_SPEED_PACE = 0.22f;

    private static final float HOLD_FORWARD = -0.3f;
    private static final float HOLD_SHOULDER_PIXELS = 6f;
    private static final float HOLD_REACH_PIXELS = 10f;

    private static final float HEAD_LEVELING = 0.65f;
    private static final float HEAD_NOD = 0.05f;
    private static final float HEAD_YAW_LIMIT = 50f;
    private static final float HEAD_PITCH_UP = -30f;
    private static final float HEAD_PITCH_DOWN = 40f;

    private static final float SWAY_DEGREES = 6.5f;
    private static final float STEP_ROLL_DEGREES = 2.5f;
    private static final float PITCH_DEGREES = 2.5f;
    private static final float RUN_LEAN_DEGREES = 4f;
    private static final float BOB_BLOCKS = 0.035f;

    private HippopotamusRiderBalance() {}

    public static void applyPose(PlayerModel<?> model, Player player, float ageInTicks) {
        model.head.zRot = 0f;
        model.hat.zRot = 0f;

        boolean firstPersonArm = ageInTicks <= 0f;
        if (firstPersonArm) return;
        if (!(player.getVehicle() instanceof HippopotamusEntity hippo)) return;

        float partialTick = Mth.clamp(ageInTicks - player.tickCount, 0f, 1f);
        float weight = HippopotamusEntity.smoothStep(hippo.rollBlend(partialTick));
        if (weight <= 0.001f) return;

        float pace = hippo.riderPace(partialTick);
        float phase = hippo.riderStepPhase(partialTick);
        float time = hippo.tickCount + partialTick;
        float seed = seed(hippo);

        float swing = Mth.sin(phase) * LEG_SWING * pace;
        float stride = Mth.cos(phase);
        float liftRight = square(Math.max(0f, stride)) * LEG_LIFT * pace;
        float liftLeft = square(Math.max(0f, -stride)) * LEG_LIFT * pace;

        blendRotation(model.rightLeg, weight, swing, 0f, LEG_STANCE);
        blendRotation(model.leftLeg, weight, -swing, 0f, -LEG_STANCE);
        model.rightLeg.y = Mth.lerp(weight, model.rightLeg.y, LEG_REST_Y - liftRight);
        model.leftLeg.y = Mth.lerp(weight, model.leftLeg.y, LEG_REST_Y - liftLeft);

        float flailClock = time * ARM_FLAIL_SPEED + hippo.riderPaceTime(partialTick) * ARM_FLAIL_SPEED_PACE;
        float flail = ARM_FLAIL * (0.6f + 0.4f * pace);
        float rightWave = flailClock + seed;
        float leftWave = flailClock * 1.13f + seed + 2.1f;
        float seesaw = -(float) Math.toRadians(leanDegrees(time - ARM_SEESAW_LAG, seed, pace, phase)) * ARM_SEESAW;

        boolean paired = hippo.hasRollPartner();
        boolean onLeft = hippo.getPassengers().indexOf(player) == 0;
        float hold = holdAngle(hippo);

        if (paired && onLeft) {
            blendRotation(model.rightArm, weight, HOLD_FORWARD, 0f, hold);
        } else {
            blendRotation(model.rightArm, weight,
                    ARM_FORWARD + Mth.sin(rightWave) * flail,
                    Mth.cos(rightWave) * flail * 0.6f,
                    ARM_SPREAD + seesaw);
        }

        if (paired && !onLeft) {
            blendRotation(model.leftArm, weight, HOLD_FORWARD, 0f, -hold);
        } else {
            blendRotation(model.leftArm, weight,
                    ARM_FORWARD + Mth.sin(leftWave) * flail,
                    -Mth.cos(leftWave) * flail * 0.6f,
                    -ARM_SPREAD + seesaw);
        }

        float lean = (float) Math.toRadians(leanDegrees(time, seed, pace, phase));
        model.head.zRot = lean * HEAD_LEVELING * weight;
        model.head.xRot += Mth.sin(phase * 2f) * HEAD_NOD * pace * weight;

        model.hat.copyFrom(model.head);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    public static void applyBodySway(PoseStack poseStack, Player player, HippopotamusEntity hippo, float partialTick) {
        float weight = HippopotamusEntity.smoothStep(hippo.rollBlend(partialTick));
        if (weight <= 0.001f) return;

        float pace = hippo.riderPace(partialTick);
        float phase = hippo.riderStepPhase(partialTick);
        float time = hippo.tickCount + partialTick;
        float seed = seed(hippo);

        float roll = leanDegrees(time, seed, pace, phase) * weight;
        float pitch = (pitchWave(time, seed) * PITCH_DEGREES + RUN_LEAN_DEGREES * pace) * weight;
        float bob = (0.5f + 0.5f * Mth.cos(phase * 2f)) * BOB_BLOCKS * pace * weight;
        float yaw = Mth.rotLerp(partialTick, hippo.yBodyRotO, hippo.yBodyRot);

        Quaternionf sway = Axis.YP.rotationDegrees(-yaw);
        sway.mul(Axis.XP.rotationDegrees(pitch));
        sway.mul(Axis.ZP.rotationDegrees(roll));
        sway.mul(Axis.YP.rotationDegrees(yaw));

        Vec3 pivot = new Vec3(-hippo.rollSeatSide(player, weight), 0.0, 0.0).yRot((float) Math.toRadians(-yaw));

        poseStack.translate(0f, bob, 0f);
        poseStack.rotateAround(sway, (float) pivot.x, 0f, (float) pivot.z);
    }

    public static void steadyHead(Player player, HippopotamusEntity hippo, float partialTick) {
        float weight = HippopotamusEntity.smoothStep(hippo.rollBlend(partialTick));
        if (weight <= 0.001f) return;

        player.setYHeadRot(steadyYaw(player.yHeadRot, hippo.yBodyRot, weight));
        player.yHeadRotO = steadyYaw(player.yHeadRotO, hippo.yBodyRotO, weight);

        player.setXRot(steadyPitch(player.getXRot(), weight));
        player.xRotO = steadyPitch(player.xRotO, weight);
    }

    private static float steadyYaw(float headYaw, float bodyYaw, float weight) {
        float relative = Mth.wrapDegrees(headYaw - bodyYaw);
        float limited = HEAD_YAW_LIMIT * Mth.sin(relative * Mth.DEG_TO_RAD);
        return bodyYaw + Mth.lerp(weight, relative, limited);
    }

    private static float steadyPitch(float pitch, float weight) {
        return Mth.lerp(weight, pitch, Mth.clamp(pitch, HEAD_PITCH_UP, HEAD_PITCH_DOWN));
    }

    private static float holdAngle(HippopotamusEntity hippo) {
        float halfGap = OWAttacksConstants.Hippopotamus.ROLL_SEAT_SPREAD * hippo.getScale() * 16f;
        float reach = Mth.clamp((halfGap - HOLD_SHOULDER_PIXELS) / HOLD_REACH_PIXELS, 0f, 1f);
        return (float) Math.asin(reach);
    }

    private static float leanDegrees(float time, float seed, float pace, float phase) {
        float wave = Mth.sin(time * 0.115f + seed) * 0.55f
                + Mth.sin(time * 0.27f + seed * 2.3f) * 0.3f
                + Mth.sin(time * 0.053f + seed * 0.7f) * 0.15f;
        return wave * SWAY_DEGREES * (0.6f + 0.4f * pace) - Mth.cos(phase) * STEP_ROLL_DEGREES * pace;
    }

    private static float pitchWave(float time, float seed) {
        return Mth.sin(time * 0.09f + seed * 1.3f) * 0.6f + Mth.sin(time * 0.21f + seed * 0.4f) * 0.4f;
    }

    private static void blendRotation(ModelPart part, float weight, float xRot, float yRot, float zRot) {
        part.xRot = Mth.lerp(weight, part.xRot, xRot);
        part.yRot = Mth.lerp(weight, part.yRot, yRot);
        part.zRot = Mth.lerp(weight, part.zRot, zRot);
    }

    private static float seed(HippopotamusEntity hippo) {
        return (hippo.getUUID().getLeastSignificantBits() & 1023L) / 1023f * Mth.TWO_PI;
    }

    private static float square(float value) {
        return value * value;
    }
}
