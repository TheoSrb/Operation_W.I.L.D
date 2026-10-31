package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.behavior.OWFearHandler;

import static net.tiew.operationWild.client.pose.OWPoseMath.add;
import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.euler;
import static net.tiew.operationWild.client.pose.OWPoseMath.inBodyFrame;
import static net.tiew.operationWild.client.pose.OWPoseMath.rad;

@OnlyIn(Dist.CLIENT)
public final class OWRiderPoses {

    public static final OWPlayerPose<RodeoData> RODEO = new Rodeo();
    public static final OWPlayerPose<SurfData> SURF = new Surf();
    public static final OWPlayerPose<ProneData> PRONE = new Prone();

    private OWRiderPoses() {}

    static final class RodeoData {
        final OWPoseSpring lag = new OWPoseSpring(0.18f, 0.7f);
        final float seed;
        int side = 1;

        RodeoData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Rodeo extends OWPlayerPose<RodeoData> {

        @Override
        protected RodeoData createData(Player player) {
            return new RodeoData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, RodeoData data, OWPlayerPoseState state) {
            OWEntity mount = player.getVehicle() instanceof OWEntity entity ? entity : null;
            boolean rolling = mount instanceof HippopotamusEntity hippo && hippo.rollBlend(1f) > 0.01f;
            if (mount == null || !mount.isPanicking() || rolling) {
                data.lag.step(0f);
                return false;
            }
            if (mount.getBuckSide() != 0) data.side = mount.getBuckSide();
            data.lag.step(OWFearHandler.buckCurve(mount, 1f));
            return true;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, RodeoData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float buck = player.getVehicle() instanceof OWEntity mount ? OWFearHandler.buckCurve(mount, partialTick) : 0f;
            float lag = data.lag.get(partialTick);
            float wave = t * 0.6f + data.seed;

            blend(model.rightArm, weight, -0.95f + lag * 0.25f, -0.1f, -0.12f);
            blend(model.leftArm, weight, -2.85f + 0.25f * Mth.sin(wave), 0f, -0.45f - 0.3f * Mth.cos(wave));
            add(model.rightLeg, weight, buck * 0.75f, 0f, buck * 0.5f);
            add(model.leftLeg, weight, buck * 0.75f, 0f, -buck * 0.5f);
            add(model.head, weight, -lag * 0.35f, 0f, -data.side * lag * 0.25f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, RodeoData data, float weight, float partialTick, float bodyYaw) {
            float lag = data.lag.get(partialTick) * weight;
            inBodyFrame(poseStack, bodyYaw, 0f, 0.6f, 0f, euler(-lag * 14f, 0f, -data.side * lag * 8f), 0f, 0f, 0f);
        }
    }

    static final class SurfData {
        final OWPoseSpring turn = new OWPoseSpring(0.2f, 0.72f);
        final OWPoseSpring jolt = new OWPoseSpring(0.3f, 0.6f);
        final float seed;

        SurfData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Surf extends OWPlayerPose<SurfData> {

        private static final float MAX_TURN = 15f;
        private static final float LEAN = 14f;

        @Override
        protected SurfData createData(Player player) {
            return new SurfData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, SurfData data, OWPlayerPoseState state) {
            GorillaEntity gorilla = player.getVehicle() instanceof GorillaEntity g && g.getControllingPassenger() == player ? g : null;
            if (gorilla == null) {
                data.turn.step(0f);
                data.jolt.step(0f);
                return false;
            }
            float yawRate = Mth.clamp(Mth.wrapDegrees(gorilla.yBodyRot - gorilla.yBodyRotO), -MAX_TURN, MAX_TURN);
            data.turn.step(yawRate / MAX_TURN);
            data.jolt.step(gorilla.slideImpact(1f));
            return gorilla.slideBlend(1f) > 0.02f;
        }

        @Override
        protected float blendIn() {
            return 0.25f;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, SurfData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float turn = Mth.clamp(data.turn.get(partialTick), -1f, 1f);
            float jolt = data.jolt.get(partialTick);
            float seesaw = -turn * 0.55f;

            blend(model.rightArm, weight, -0.25f - jolt * 0.4f, 0f, 1.35f + seesaw + Mth.sin(t * 0.4f + data.seed) * 0.06f);
            blend(model.leftArm, weight, -0.25f - jolt * 0.4f, 0f, -1.35f + seesaw + Mth.sin(t * 0.4f + data.seed + 1f) * 0.06f);
            add(model.head, weight, 0f, 0f, rad(turn * LEAN) * 0.6f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, SurfData data, float weight, float partialTick, float bodyYaw) {
            float turn = Mth.clamp(data.turn.get(partialTick), -1f, 1f);
            inBodyFrame(poseStack, bodyYaw, 0f, 0.6f, 0f, euler(10f * weight, 0f, turn * LEAN * weight), 0f, -0.04f * weight, 0f);
        }
    }

    static final class ProneData {
        final OWPoseSpring pitch = new OWPoseSpring(0.3f, 0.6f);
        final OWPoseSpring grip = new OWPoseSpring(0.25f, 0.65f);
        final float seed;

        ProneData(float seed) {
            this.seed = seed;
        }
    }

    /**
     * Calamar Colossal : le cavalier est couché à plat ventre sur le manteau ({@code front_head}),
     * dont il épouse l'inclinaison, et le tient à bras-le-corps, un bras de chaque côté. Seul le
     * modèle du joueur bouge, jamais la caméra.
     */
    private static final class Prone extends OWPlayerPose<ProneData> {

        private static final float PIVOT_Y = 0.6f;
        private static final float LIE_DOWN = -0.45f;
        private static final float SLIDE_BACK = -0.3f;

        @Override
        protected ProneData createData(Player player) {
            return new ProneData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, ProneData data, OWPlayerPoseState state) {
            ColossalSquidEntity squid = player.getVehicle() instanceof ColossalSquidEntity s && s.getGrabbedTarget() != player ? s : null;
            if (squid == null) {
                data.grip.step(0f);
                return false;
            }
            data.pitch.step(mantlePitch(squid));
            data.grip.step(Mth.clamp(squid.walkAnimation.speed() * 2f, 0f, 1f));
            return true;
        }

        /**
         * Inclinaison du manteau en degrés, positive nez vers le bas. Elle est mesurée par rapport à
         * {@code ALL2}, dont le tangage et le roulis sont déjà appliqués au cavalier ailleurs.
         */
        private static float mantlePitch(ColossalSquidEntity squid) {
            org.joml.Matrix4f seat = squid.seatMatrix;
            org.joml.Matrix4f root = squid.riderMatrix;
            if (seat == null || root == null) return 0f;
            org.joml.Matrix4f relative = new org.joml.Matrix4f(root).invert().mul(seat);
            org.joml.Vector3f axis = relative.transformDirection(new org.joml.Vector3f(0f, 0f, 1f));
            if (axis.z > 0f) axis.negate();
            float horizontal = (float) Math.sqrt(axis.x * axis.x + axis.z * axis.z);
            return (float) Math.toDegrees(Mth.atan2(axis.y, horizontal));
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, ProneData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float grip = data.grip.get(partialTick);
            float breath = Mth.sin(t * 0.3f + data.seed) * 0.04f;

            blend(model.rightArm, weight, -0.75f - 0.15f * grip, 0.15f, 1.05f + breath);
            blend(model.leftArm, weight, -0.75f - 0.15f * grip, -0.15f, -1.05f - breath);
            blend(model.rightLeg, weight, 0.05f, 0f, 0.07f);
            blend(model.leftLeg, weight, 0.05f, 0f, -0.07f);
            blend(model.body, weight, 0f, 0f, 0f);
            blend(model.head, weight, model.head.xRot * 0.4f - 1.2f, model.head.yRot * 0.5f, 0f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, ProneData data, float weight, float partialTick, float bodyYaw) {
            float pitch = (90f + data.pitch.get(partialTick)) * weight;
            inBodyFrame(poseStack, bodyYaw, 0f, PIVOT_Y, 0f, euler(pitch, 0f, 0f), 0f, LIE_DOWN * weight, SLIDE_BACK * weight);
        }
    }
}
