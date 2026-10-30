package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.euler;
import static net.tiew.operationWild.client.pose.OWPoseMath.inBodyFrame;
import static net.tiew.operationWild.client.pose.OWPoseMath.rad;

@OnlyIn(Dist.CLIENT)
public final class OWGrabbedPoses {

    public static final OWPlayerPose<CrocodileData> CROCODILE = new Crocodile();
    public static final OWPlayerPose<TigerData> TIGER = new Tiger();
    public static final OWPlayerPose<BoaData> BOA = new Boa();
    public static final OWPlayerPose<KangarooData> KANGAROO = new Kangaroo();

    private OWGrabbedPoses() {}

    static final class CrocodileData {
        final OWStruggle struggle = new OWStruggle();
        final OWPoseSpring swing = new OWPoseSpring(0.16f, 0.82f);
        final OWPoseSpring bounce = new OWPoseSpring(0.2f, 0.78f);
        final float seed;
        float yaw, yawO, rawYaw;
        float pitch, pitchO;
        float rigid, rigidO;
        boolean holding;
        int held;

        CrocodileData(float seed) {
            this.seed = seed;
        }

        float swingOffset(float partialTick) {
            return Mth.clamp(swing.get(partialTick) - Mth.lerp(partialTick, yawO, yaw), -45f, 45f);
        }

        float bounceOffset(float partialTick) {
            return Mth.clamp(bounce.get(partialTick) - Mth.lerp(partialTick, pitchO, pitch), -35f, 35f);
        }
    }

    private static final class Crocodile extends OWPlayerPose<CrocodileData> {

        private static final int DIZZY_AFTER_TICKS = 30;
        private static final int DIZZY_TICKS = 34;
        private static final float FOLD = 130f;
        private static final float FOLD_RIGID = 92f;
        private static final float LEG_HANG = 35f;
        private static final float WAIST = 0.85f;

        @Override
        protected CrocodileData createData(Player player) {
            return new CrocodileData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, CrocodileData data, OWPlayerPoseState state) {
            CrocodileEntity croc = player.getVehicle() instanceof CrocodileEntity c && c.getGrabbedTarget() == player ? c : null;

            data.rigidO = data.rigid;
            if (croc == null) {
                if (data.holding && data.held > DIZZY_AFTER_TICKS) state.requestDizzy(DIZZY_TICKS);
                data.holding = false;
                data.held = 0;
                data.struggle.reset();
                data.struggle.track(0);
                data.yawO = data.yaw;
                data.pitchO = data.pitch;
                data.swing.step(data.yaw);
                data.bounce.step(data.pitch);
                data.rigid = OWPoseMath.approach(data.rigid, 0f, 0.2f);
                return false;
            }

            float rawYaw = croc.yBodyRot + croc.mouthYRotDeg;
            if (!data.holding) {
                data.holding = true;
                data.rawYaw = rawYaw;
                data.yaw = data.yawO = rawYaw;
                data.pitch = data.pitchO = croc.mouthXRotDeg;
                data.swing.snap(rawYaw);
                data.bounce.snap(data.pitch);
            }
            data.held++;

            data.yawO = data.yaw;
            data.yaw += Mth.wrapDegrees(rawYaw - data.rawYaw);
            data.rawYaw = rawYaw;
            data.pitchO = data.pitch;
            data.pitch = croc.mouthXRotDeg;
            data.swing.step(data.yaw);
            data.bounce.step(data.pitch);

            data.rigid = OWPoseMath.approach(data.rigid, croc.isDeathRolling() ? 1f : 0f, 0.2f);
            data.struggle.track(croc.getGrabTimeout());
            return true;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, CrocodileData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float rigid = OWPoseMath.smoothStep(Mth.lerp(partialTick, data.rigidO, data.rigid));
            float loose = 1f - rigid;
            float swing = rad(data.swingOffset(partialTick));
            float bounce = rad(data.bounceOffset(partialTick));
            float fold = rad(Mth.lerp(rigid, FOLD, FOLD_RIGID));
            float fervour = data.struggle.fervour(partialTick);
            float right = Math.min(1.3f, data.struggle.right(partialTick)) * loose;
            float left = Math.min(1.3f, data.struggle.left(partialTick)) * loose;

            float kickAmp = (0.2f + 0.16f * fervour) * loose;
            float kick = Mth.sin(t * (0.42f + 0.25f * fervour) + data.seed) * kickAmp;
            float legBase = Mth.lerp(rigid, -(fold - rad(LEG_HANG)), 0f);

            blend(model.rightLeg, weight, legBase + swing * 0.45f + kick - right * 0.55f, 0f,
                    Mth.lerp(rigid, 0.1f + bounce * 0.25f, 0.02f));
            blend(model.leftLeg, weight, legBase + swing * 0.45f - kick - left * 0.55f, 0f,
                    Mth.lerp(rigid, -0.1f - bounce * 0.25f, -0.02f));

            float armHang = -fold + 0.2f;
            float flailRight = Mth.sin(t * 0.31f + data.seed) * 0.3f * loose;
            float flailLeft = Mth.sin(t * 0.27f + data.seed + 2.1f) * 0.3f * loose;

            float rightArmX = Mth.lerp(rigid, armHang + swing * 0.7f + flailRight, -3.0f);
            float leftArmX = Mth.lerp(rigid, armHang + swing * 0.7f + flailLeft, -3.0f);
            blend(model.rightArm, weight, Mth.lerp(Math.min(1f, right), rightArmX, -0.45f), 0f,
                    Mth.lerp(rigid, 0.25f + bounce * 0.3f, -0.18f) - right * 0.35f);
            blend(model.leftArm, weight, Mth.lerp(Math.min(1f, left), leftArmX, -0.45f), 0f,
                    Mth.lerp(rigid, -0.25f - bounce * 0.3f, 0.18f) - left * 0.25f);

            blend(model.head, weight, Mth.lerp(rigid, -0.75f, -0.25f) + bounce * 0.35f,
                    Mth.sin(t * 0.19f + data.seed) * 0.35f * loose, swing * 0.25f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, CrocodileData data, float weight, float partialTick, float bodyYaw) {
            float rigid = OWPoseMath.smoothStep(Mth.lerp(partialTick, data.rigidO, data.rigid));
            float fold = Mth.lerp(rigid, FOLD, FOLD_RIGID) + data.swingOffset(partialTick) * 0.55f * (1f - rigid);
            float drop = -data.bounceOffset(partialTick) * 0.004f;
            float waist = player.getVehicle() instanceof CrocodileEntity croc ? (float) croc.grabHang(player) : WAIST;
            inBodyFrame(poseStack, bodyYaw, 0f, waist, 0f, euler(fold * weight, 90f * weight, 0f), 0f, drop * weight, 0f);
        }
    }

    static final class TigerData {
        final OWStruggle struggle = new OWStruggle();
        final OWPoseSpring bite = new OWPoseSpring(0.35f, 0.55f);
        final float seed;

        TigerData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Tiger extends OWPlayerPose<TigerData> {

        private static final float CHEST = 1.3f;
        private static final float SINK = -0.05f;
        private static final float PULL_TO_TIGER = 0.9f;

        @Override
        protected TigerData createData(Player player) {
            return new TigerData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, TigerData data, OWPlayerPoseState state) {
            TigerEntity tiger = player.getVehicle() instanceof TigerEntity t && t.getGrabbedTargetId() == player.getId() ? t : null;
            if (tiger == null) {
                data.bite.step(0f);
                data.struggle.reset();
                data.struggle.track(0);
                return false;
            }
            boolean chewing = !(tiger.isTame() && tiger.isVehicle()) || tiger.getGrabPunchTimer() > 0;
            data.bite.step(chewing ? Math.max(0f, Mth.sin(tiger.tickCount * 0.75f - 0.6f)) : 0f);
            data.struggle.track(tiger.getGrabTimeout());
            return true;
        }

        @Override
        protected float blendIn() {
            return 0.14f;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, TigerData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float bite = Mth.clamp(data.bite.get(partialTick), 0f, 1.2f);
            float fervour = data.struggle.fervour(partialTick);
            float right = Math.min(1.3f, data.struggle.right(partialTick));
            float left = Math.min(1.3f, data.struggle.left(partialTick));

            float guardBreak = bite * 0.5f;
            float push = (right + left) * 0.3f;
            blend(model.rightArm, weight, -1.8f + guardBreak - push + Mth.sin(t * 0.23f + data.seed) * 0.08f,
                    -0.25f, -0.38f + Mth.sin(t * 0.31f + data.seed) * 0.06f);
            blend(model.leftArm, weight, -1.8f + guardBreak - push + Mth.sin(t * 0.21f + data.seed + 1.3f) * 0.08f,
                    0.25f, 0.38f + Mth.sin(t * 0.29f + data.seed + 0.7f) * 0.06f);

            float pedal = t * (0.48f + 0.3f * fervour) + data.seed;
            blend(model.rightLeg, weight, -0.85f + Mth.sin(pedal) * 0.45f - right * 0.7f, 0.08f, 0.12f);
            blend(model.leftLeg, weight, -0.85f + Mth.sin(pedal + Mth.PI) * 0.45f - left * 0.7f, -0.08f, -0.12f);

            float turnAway = Mth.clamp(Mth.sin(t * 0.07f + data.seed) * 1.7f, -1f, 1f) * 0.6f;
            blend(model.head, weight, 0.9f + bite * 0.12f, turnAway, 0f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, TigerData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float bite = Mth.clamp(data.bite.get(partialTick), 0f, 1.2f);
            float right = Math.min(1.3f, data.struggle.right(partialTick));
            float left = Math.min(1.3f, data.struggle.left(partialTick));
            float fervour = data.struggle.fervour(partialTick);

            float pitch = -90f + (right + left) * 10f;
            float roll = Mth.sin(t * (0.24f + 0.15f * fervour) + data.seed) * 5f + (right - left) * 6f;
            inBodyFrame(poseStack, bodyYaw, 0f, CHEST, 0f, euler(pitch * weight, 180f * weight, roll * weight),
                    0f, (SINK - bite * 0.03f) * weight, -PULL_TO_TIGER * weight);
        }
    }

    static final class BoaData {
        final OWStruggle struggle = new OWStruggle();
        final float seed;

        BoaData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Boa extends OWPlayerPose<BoaData> {

        private static final long BREATH_MS = 625L;
        private static final float LIFT = 0.1f;
        private static final float ARM_BACK = 0.18f;
        private static final float PRESS = 0.06f;
        private static final float BREAK_FREE = 0.52f;
        private static final float SHRUG = 1.3f;
        private static final float WIGGLE_ROLL = 12f;
        private static final float WIGGLE_TWIST = 7f;

        @Override
        protected BoaData createData(Player player) {
            return new BoaData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, BoaData data, OWPlayerPoseState state) {
            BoaEntity boa = player.getVehicle() instanceof BoaEntity b && b.getGrabbedTargetId() == player.getId() ? b : null;
            if (boa == null) {
                data.struggle.reset();
                data.struggle.track(0);
                return false;
            }
            data.struggle.track(boa.getGrabTimeout());
            return true;
        }

        private static float squeeze(Player player, float partialTick) {
            if (!(player.getVehicle() instanceof BoaEntity boa)) return 0f;
            float phase;
            if (boa.getFirstTailPart() instanceof BoaTailPart part && part.constrictBreathAnimationState.isStarted()) {
                phase = (part.constrictBreathAnimationState.getAccumulatedTime() % BREATH_MS) / (float) BREATH_MS;
            } else {
                phase = ((boa.tickCount + partialTick) * 50f % BREATH_MS) / BREATH_MS;
            }
            return OWPoseMath.pulse(phase, 0.32f);
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, BoaData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float squeeze = squeeze(player, partialTick);
            float fervour = data.struggle.fervour(partialTick);
            float right = Math.min(1.3f, data.struggle.right(partialTick));
            float left = Math.min(1.3f, data.struggle.left(partialTick));

            float pressed = PRESS + squeeze * 0.05f;
            blend(model.rightArm, weight, ARM_BACK, 0f, -pressed + right * BREAK_FREE);
            blend(model.leftArm, weight, ARM_BACK, 0f, pressed - left * BREAK_FREE);

            float shrug = squeeze * SHRUG;
            model.rightArm.y -= shrug * weight;
            model.leftArm.y -= shrug * weight;
            model.head.y += shrug * 0.55f * weight;

            float kick = Mth.sin(t * 0.45f + data.seed) * 0.1f * (1f + fervour);
            blend(model.rightLeg, weight, 0.1f + kick, 0f, -0.02f);
            blend(model.leftLeg, weight, 0.1f - kick, 0f, 0.02f);

            float gasp = Mth.sin(t * 0.17f + data.seed) * 0.08f;
            blend(model.head, weight, -0.5f - squeeze * 0.2f + gasp, (right - left) * 0.25f,
                    Mth.sin(t * 0.13f + data.seed) * 0.08f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, BoaData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float rawSqueeze = squeeze(player, partialTick);
            float squeeze = rawSqueeze * weight;
            float fervour = data.struggle.fervour(partialTick);
            float wiggle = Mth.clamp(data.struggle.rightSigned(partialTick) - data.struggle.leftSigned(partialTick), -1.3f, 1.3f);

            float squirm = Mth.sin(t * 0.21f + data.seed) * (2f + 3f * fervour);
            float roll = wiggle * WIGGLE_ROLL + squirm;
            float yaw = wiggle * WIGGLE_TWIST;
            float pitch = -2f - rawSqueeze * 2f;
            inBodyFrame(poseStack, bodyYaw, 0f, 0.9f, 0f, euler(pitch * weight, yaw * weight, roll * weight),
                    0f, LIFT * weight, 0f);
            poseStack.scale(1f - 0.06f * squeeze, 1f + 0.02f * squeeze, 1f - 0.06f * squeeze);
        }
    }

    static final class KangarooData {
        final OWStruggle struggle = new OWStruggle();
        final float seed;

        KangarooData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Kangaroo extends OWPlayerPose<KangarooData> {

        @Override
        protected KangarooData createData(Player player) {
            return new KangarooData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, KangarooData data, OWPlayerPoseState state) {
            KangarooEntity kangaroo = player.getVehicle() instanceof KangarooEntity k && k.getDrownVictim() == player ? k : null;
            if (kangaroo == null) {
                data.struggle.reset();
                data.struggle.track(0);
                return false;
            }
            data.struggle.track(kangaroo.getGrabTimeout());
            return true;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, KangarooData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float fervour = data.struggle.fervour(partialTick);
            float right = Math.min(1.3f, data.struggle.right(partialTick));
            float left = Math.min(1.3f, data.struggle.left(partialTick));

            float stroke = t * (0.75f + 0.3f * fervour) + data.seed;
            float strokeLeft = stroke * 1.08f + 2.7f;
            blend(model.rightArm, weight, -2.5f + Mth.sin(stroke) * 0.6f + right * 0.9f, 0f, 0.6f + Mth.cos(stroke) * 0.35f);
            blend(model.leftArm, weight, -2.5f + Mth.sin(strokeLeft) * 0.6f + left * 0.9f, 0f, -0.6f - Mth.cos(strokeLeft) * 0.35f);

            float kick = Mth.sin(t * (0.9f + 0.4f * fervour) + data.seed) * 0.5f;
            blend(model.rightLeg, weight, kick, 0f, 0.08f);
            blend(model.leftLeg, weight, -kick, 0f, -0.08f);

            blend(model.head, weight, -0.3f, Mth.sin(t * 0.6f + data.seed) * 0.45f, Mth.sin(t * 0.37f) * 0.15f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, KangarooData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float arch = Math.min(1.3f, data.struggle.right(partialTick)) + Math.min(1.3f, data.struggle.left(partialTick));

            float pitch = -10f - arch * 10f;
            float yaw = Mth.sin(t * 0.21f + data.seed) * 8f;
            float roll = Mth.sin(t * 0.5f + data.seed) * 6f;
            inBodyFrame(poseStack, bodyYaw, 0f, 0.9f, 0f, euler(pitch * weight, yaw * weight, roll * weight), 0f, 0f, 0f);
        }
    }
}
