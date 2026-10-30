package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;

import static net.tiew.operationWild.client.pose.OWPoseMath.add;
import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.euler;
import static net.tiew.operationWild.client.pose.OWPoseMath.inBodyFrame;
import static net.tiew.operationWild.client.pose.OWPoseMath.rad;

@OnlyIn(Dist.CLIENT)
public final class OWAssailedPoses {

    public static final OWPlayerPose<LatchData> LATCH = new Latch();
    public static final OWPlayerPose<HypnosisData> HYPNOSIS = new Hypnosis();
    public static final OWPlayerPose<DizzyData> DIZZY = new Dizzy();

    private OWAssailedPoses() {}

    static final class LatchData {
        final OWPoseSpring jolt = new OWPoseSpring(0.3f, 0.6f);
        final float seed;
        float angle;
        int lastShake;

        LatchData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Latch extends OWPlayerPose<LatchData> {

        private static final double SEARCH = 6.0;

        @Override
        protected LatchData createData(Player player) {
            return new LatchData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, LatchData data, OWPlayerPoseState state) {
            LionEntity latcher = null;
            for (LionEntity lion : player.level().getEntitiesOfClass(LionEntity.class, player.getBoundingBox().inflate(SEARCH))) {
                if (lion.getLatchPhase() == LionEntity.LATCH_HOLD && lion.getLatchTarget() == player) {
                    latcher = lion;
                    break;
                }
            }
            if (latcher == null) {
                data.lastShake = 0;
                data.jolt.step(0f);
                return false;
            }
            data.angle = latcher.getLatchAngle();
            int shake = latcher.getLatchShakeTicks();
            if (shake > data.lastShake) data.jolt.kick(1f);
            data.lastShake = shake;
            data.jolt.step(0f);
            return true;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, LatchData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float angle = rad(data.angle);
            float lateral = Mth.sin(angle);
            float front = Mth.cos(angle);
            float frontWeight = square(Math.max(0f, front));
            float backWeight = square(Math.max(0f, -front));
            float sideWeight = lateral * lateral;
            boolean onRight = lateral >= 0f;

            float pushRight = Mth.sin(t * 0.5f + data.seed) * 0.15f;
            float pushLeft = Mth.sin(t * 0.5f + data.seed + 1.7f) * 0.15f;
            float hammer = 1.55f + 0.6f * Mth.sin(t * 0.55f + data.seed);
            float balance = Mth.sin(t * 0.3f + data.seed) * 0.15f;
            float swatRight = 0.8f + 0.35f * Mth.sin(t * 0.6f + data.seed);
            float swatLeft = 0.8f + 0.35f * Mth.sin(t * 0.6f + data.seed + 2.4f);

            float[] reachRight = {-0.3f, 0f, hammer};
            float[] reachLeft = {-0.3f, 0f, -hammer};
            float[] balanceRight = {-0.2f, 0f, 1.05f + balance};
            float[] balanceLeft = {-0.2f, 0f, -1.05f - balance};

            float[] sideRight = onRight ? reachRight : balanceRight;
            float[] sideLeft = onRight ? balanceLeft : reachLeft;

            mix(model.rightArm, weight, frontWeight, backWeight, sideWeight,
                    -1.45f + pushRight, -0.35f, -0.1f, swatRight, 0f, 0.25f, sideRight);
            mix(model.leftArm, weight, frontWeight, backWeight, sideWeight,
                    -1.45f + pushLeft, 0.35f, 0.1f, swatLeft, 0f, -0.25f, sideLeft);

            add(model.rightLeg, weight, 0f, 0f, 0.1f);
            add(model.leftLeg, weight, 0f, 0f, -0.1f);
            add(model.head, weight, 0.15f + Math.max(0f, front) * 0.2f, lateral * 0.6f, 0f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, LatchData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float angle = rad(data.angle);
            float lateral = Mth.sin(angle);
            float front = Mth.cos(angle);
            float jolt = data.jolt.get(partialTick);

            float pitch = 12f + Math.max(0f, front) * 6f + jolt * 6f;
            float yaw = Mth.sin(t * 0.16f + data.seed) * 10f;
            float roll = lateral * (8f + jolt * 10f);
            inBodyFrame(poseStack, bodyYaw, 0f, 0f, 0f, euler(pitch * weight, yaw * weight, roll * weight), 0f, 0f, 0f);
        }

        private static void mix(ModelPart part, float weight, float a, float b, float c,
                                float ax, float ay, float az, float bx, float by, float bz, float[] side) {
            blend(part, weight, a * ax + b * bx + c * side[0], a * ay + b * by + c * side[1], a * az + b * bz + c * side[2]);
        }

        private static float square(float value) {
            return value * value;
        }
    }

    static final class HypnosisData {
        final float seed;

        HypnosisData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Hypnosis extends OWPlayerPose<HypnosisData> {

        private static final double SEARCH = 20.0;
        private static final float ARM_WEIGHT = 0.85f;

        @Override
        protected HypnosisData createData(Player player) {
            return new HypnosisData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, HypnosisData data, OWPlayerPoseState state) {
            for (BoaEntity boa : player.level().getEntitiesOfClass(BoaEntity.class, player.getBoundingBox().inflate(SEARCH))) {
                if (boa.isHypnotizing() && boa.getHypnosisTargetId() == player.getId()) return true;
            }
            return false;
        }

        @Override
        protected float blendIn() {
            return 0.08f;
        }

        @Override
        protected float blendOut() {
            return 0.1f;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, HypnosisData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float arms = weight * ARM_WEIGHT;
            blend(model.rightArm, arms, -0.12f + Mth.sin(t * 0.11f + data.seed + 0.6f) * 0.08f, 0f,
                    0.06f + Mth.sin(t * 0.22f + data.seed) * 0.04f);
            blend(model.leftArm, arms, -0.12f + Mth.sin(t * 0.11f + data.seed + 0.9f) * 0.08f, 0f,
                    -0.06f - Mth.sin(t * 0.22f + data.seed + 0.4f) * 0.04f);
            add(model.head, weight, 0.28f, 0f, Mth.sin(t * 0.11f + data.seed - 0.8f) * 0.22f);
            add(model.rightLeg, weight, 0f, 0f, Mth.sin(t * 0.11f + data.seed) * 0.04f);
            add(model.leftLeg, weight, 0f, 0f, Mth.sin(t * 0.11f + data.seed) * 0.04f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, HypnosisData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float roll = Mth.sin(t * 0.11f + data.seed) * 6f;
            float pitch = 3f + Mth.sin(t * 0.22f + data.seed) * 3.5f;
            inBodyFrame(poseStack, bodyYaw, 0f, 0f, 0f, euler(pitch * weight, 0f, roll * weight), 0f, 0f, 0f);
        }
    }

    static final class DizzyData {
        final float seed;
        int ticks;

        DizzyData(float seed) {
            this.seed = seed;
        }

        float amplitude(float partialTick) {
            return Mth.clamp((ticks - partialTick) / 12f, 0f, 1f);
        }
    }

    private static final class Dizzy extends OWPlayerPose<DizzyData> {

        @Override
        protected DizzyData createData(Player player) {
            return new DizzyData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, DizzyData data, OWPlayerPoseState state) {
            if (state.dizzyRequest > 0) {
                data.ticks = Math.max(data.ticks, state.dizzyRequest);
                state.dizzyRequest = 0;
            }
            if (data.ticks > 0) data.ticks--;
            return data.ticks > 0 && !player.isPassenger();
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, DizzyData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float a = data.amplitude(partialTick);
            float circle = t * 0.35f + data.seed;

            add(model.head, weight, (0.22f + Mth.cos(circle) * 0.2f) * a, Mth.sin(t * 0.17f) * 0.15f * a, Mth.sin(circle) * 0.3f * a);
            blend(model.rightArm, weight * a, -0.3f + Mth.sin(t * 0.3f) * 0.15f, 0f, 1.15f + Mth.sin(t * 0.3f + data.seed) * 0.25f);
            blend(model.leftArm, weight * a, -2.5f, 0.85f, 0.05f);
            add(model.rightLeg, weight, Mth.sin(t * 0.27f) * 0.18f * a, 0f, 0.12f * a);
            add(model.leftLeg, weight, -Mth.sin(t * 0.27f) * 0.18f * a, 0f, -0.12f * a);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, DizzyData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float a = data.amplitude(partialTick) * weight;
            float roll = Mth.sin(t * 0.3f + data.seed) * 8f * a;
            float pitch = Mth.cos(t * 0.21f + data.seed) * 5f * a;
            inBodyFrame(poseStack, bodyYaw, 0f, 0f, 0f, euler(pitch, 0f, roll), 0f, 0f, 0f);
        }
    }
}
