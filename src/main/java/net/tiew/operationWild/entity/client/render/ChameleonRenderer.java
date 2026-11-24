package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.client.layer.ChameleonCamouflageLayer;
import net.tiew.operationWild.entity.client.model.ChameleonModel;

public class ChameleonRenderer extends OWEntityRenderer<ChameleonEntity, ChameleonModel<ChameleonEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/chameleon/chameleon_default.png");

    private static final float TONGUE_WIDTH = 1f / 16f;
    private static final float TONGUE_TIP_SIZE = 2f / 16f;
    private static final float TONGUE_ITEM_SCALE = 0.45f;
    private static final double TONGUE_OVERSHOOT_LIMIT = 1.12;
    private static final double TONGUE_WHIP_AMPLITUDE = 0.45;
    private static final float TONGUE_WHIP_SPEED = 1.6f;
    private static final int TONGUE_WHIP_SEGMENTS = 10;
    private static final double FIRST_PERSON_MOUTH_LIFT = 0.18;
    private static final double FIRST_PERSON_MOUTH_FORWARD = 0.25;

    private static final float TONGUE_U0 = 48f / 64f, TONGUE_U1 = 52f / 64f, TONGUE_V0 = 18f / 64f, TONGUE_V1 = 19f / 64f;
    private static final float TIP_U0 = 50f / 64f, TIP_U1 = 52f / 64f, TIP_V0 = 8f / 64f, TIP_V1 = 10f / 64f;

    public ChameleonRenderer(EntityRendererProvider.Context context) {
        super(context, new ChameleonModel<>(context.bakeLayer(ChameleonModel.LAYER_LOCATION)), 0.25f);
        this.addLayer(new ChameleonCamouflageLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ChameleonEntity chameleon) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(ChameleonEntity chameleon, Frustum frustum, double camX, double camY, double camZ) {
        return chameleon.isTongueOut() || super.shouldRender(chameleon, frustum, camX, camY, camZ);
    }

    @Override
    public void render(ChameleonEntity chameleon, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Player carrier = chameleon.getCarrier();

        if (carrier == null || ChameleonModel.RENDER_AS_GROUNDED) {
            float climbPitch = ChameleonModel.RENDER_AS_GROUNDED ? 0f : chameleon.climbPitchDegrees(partialTicks);
            poseStack.pushPose();
            if (climbPitch != 0f) {
                float yaw = Mth.rotLerp(partialTicks, chameleon.yBodyRotO, chameleon.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(climbPitch));
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            }
            super.render(chameleon, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
            poseStack.popPose();
            if (!ChameleonModel.RENDER_AS_GROUNDED) renderTongue(chameleon, partialTicks, poseStack, bufferSource, packedLight, false);
            return;
        }

        boolean firstPerson = isCarrierView(carrier);
        if (!firstPerson) {
            poseStack.pushPose();
            applyPerch(chameleon, carrier, partialTicks, poseStack);
            super.render(chameleon, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }
        renderTongue(chameleon, partialTicks, poseStack, bufferSource, packedLight, firstPerson);
    }

    private static boolean isCarrierView(Player carrier) {
        Minecraft minecraft = Minecraft.getInstance();
        return carrier == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
    }

    private void applyPerch(ChameleonEntity chameleon, Player carrier, float partialTicks, PoseStack poseStack) {
        float blend = Mth.lerp(partialTicks, chameleon.perchBlendO, chameleon.perchBlend);
        float crouch = Mth.lerp(partialTicks, chameleon.crouchAmountO, chameleon.crouchAmount);
        float bodyYaw = Mth.rotLerp(partialTicks, carrier.yBodyRotO, carrier.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, carrier.yHeadRotO, carrier.yHeadRot);

        Vec3 offset = ChameleonEntity.perchOffset(bodyYaw, headYaw, blend, chameleon.getScale(), crouch);
        poseStack.translate(
                Mth.lerp(partialTicks, carrier.xo, carrier.getX()) + offset.x - Mth.lerp(partialTicks, chameleon.xo, chameleon.getX()),
                Mth.lerp(partialTicks, carrier.yo, carrier.getY()) + offset.y - Mth.lerp(partialTicks, chameleon.yo, chameleon.getY()),
                Mth.lerp(partialTicks, carrier.zo, carrier.getZ()) + offset.z - Mth.lerp(partialTicks, chameleon.zo, chameleon.getZ()));

        poseStack.mulPose(Axis.YP.rotationDegrees(-ChameleonEntity.perchYaw(bodyYaw, headYaw, blend)));
        poseStack.mulPose(Axis.XP.rotationDegrees(ChameleonEntity.perchPitchDegrees(blend)));

        float[] aim = chameleon.aimShare(partialTicks);
        if (aim != null) {
            poseStack.mulPose(Axis.YP.rotation(-aim[0]));
            poseStack.mulPose(Axis.XP.rotation(aim[1]));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(rendererBodyYaw(chameleon, carrier, partialTicks)));
    }

    private static float rendererBodyYaw(ChameleonEntity chameleon, Player carrier, float partialTicks) {
        float head = Mth.rotLerp(partialTicks, chameleon.yHeadRotO, chameleon.yHeadRot);
        float vehicle = Mth.rotLerp(partialTicks, carrier.yBodyRotO, carrier.yBodyRot);
        float offset = Mth.clamp(Mth.wrapDegrees(head - vehicle), -85f, 85f);
        float yaw = head - offset;
        if (offset * offset > 2500f) yaw += offset * 0.2f;
        return yaw;
    }

    private void renderTongue(ChameleonEntity chameleon, float partialTicks, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, boolean firstPerson) {
        if (!chameleon.isTongueOut()) return;

        Vec3 origin = chameleon.getPosition(partialTicks);
        Vec3 end = tongueEnd(chameleon, partialTicks);
        Vec3 mouth = firstPerson ? firstPersonMouth(chameleon.getCarrier(), partialTicks) : chameleon.tongueOrigin(partialTicks, end);

        Vec3 toEnd = end.subtract(mouth);
        double reach = toEnd.length();
        if (reach < 1.0e-3) return;

        double length = chameleon.getTongueState() == ChameleonEntity.TONGUE_ATTACHED
                ? reach
                : Math.min(reach * TONGUE_OVERSHOOT_LIMIT, chameleon.currentTongueLength(partialTicks));
        if (length < 0.05) return;

        Vec3 direction = toEnd.scale(1.0 / reach);
        Vec3 tip = mouth.add(direction.scale(length));

        poseStack.pushPose();
        poseStack.translate(-origin.x, -origin.y, -origin.z);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        float width = TONGUE_WIDTH * chameleon.getScale();
        drawWhip(chameleon, partialTicks, pose, buffer, mouth, direction, length, width, packedLight);
        float tipSize = TONGUE_TIP_SIZE * chameleon.getScale() * (1f + 0.6f * chameleon.tongueWhip(partialTicks));
        drawBeam(pose, buffer, tip.subtract(direction.scale(tipSize * 0.5)), tip.add(direction.scale(tipSize * 0.5)),
                direction, tipSize, packedLight, TIP_U0, TIP_U1, TIP_V0, TIP_V1);

        ItemStack caught = chameleon.getTongueItem();
        if (!caught.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(tip.x, tip.y - 0.1, tip.z);
            poseStack.scale(TONGUE_ITEM_SCALE, TONGUE_ITEM_SCALE, TONGUE_ITEM_SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(caught, ItemDisplayContext.GROUND, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, chameleon.level(), chameleon.getId());
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawWhip(ChameleonEntity chameleon, float partialTicks, PoseStack.Pose pose, VertexConsumer buffer,
                                 Vec3 mouth, Vec3 direction, double length, float width, int light) {
        float whip = chameleon.tongueWhip(partialTicks);
        double amplitude = Math.min(TONGUE_WHIP_AMPLITUDE, length * 0.12) * whip;
        Vec3 reference = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 lift = side.cross(direction).normalize();
        float phase = (chameleon.tickCount + partialTicks) * TONGUE_WHIP_SPEED;

        int segments = amplitude > 1.0e-3 ? TONGUE_WHIP_SEGMENTS : 1;
        Vec3 previous = mouth;
        for (int i = 1; i <= segments; i++) {
            double s = i / (double) segments;
            double envelope = Math.sin(Math.PI * s);
            double wave = Math.sin(s * Math.PI * 3.0 - phase);
            Vec3 point = mouth.add(direction.scale(length * s))
                    .add(side.scale(amplitude * envelope * wave))
                    .add(lift.scale(amplitude * 0.5 * envelope * Math.cos(s * Math.PI * 2.0 - phase)));
            Vec3 segment = point.subtract(previous);
            double segmentLength = segment.length();
            if (segmentLength > 1.0e-4) {
                drawBeam(pose, buffer, previous, point, segment.scale(1.0 / segmentLength), width, light,
                        TONGUE_U0, TONGUE_U1, TONGUE_V0, TONGUE_V1);
            }
            previous = point;
        }
    }

    private static Vec3 firstPersonMouth(Player carrier, float partialTicks) {
        Vec3 look = carrier.getViewVector(partialTicks);
        return carrier.getEyePosition(partialTicks).add(0, FIRST_PERSON_MOUTH_LIFT, 0).add(look.scale(FIRST_PERSON_MOUTH_FORWARD));
    }

    private static Vec3 tongueEnd(ChameleonEntity chameleon, float partialTicks) {
        int targetId = chameleon.getTongueTarget();
        if (targetId >= 0) {
            Entity target = chameleon.level().getEntity(targetId);
            if (target != null) {
                Vec3 position = target.getPosition(partialTicks);
                return position.add(0, target.getBbHeight() * 0.5, 0);
            }
        }
        return chameleon.tongueEnd();
    }

    private static void drawBeam(PoseStack.Pose pose, VertexConsumer buffer, Vec3 from, Vec3 to, Vec3 direction,
                                 float width, int light, float u0, float u1, float v0, float v1) {
        Vec3 reference = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 sideA = direction.cross(reference).normalize().scale(width * 0.5);
        Vec3 sideB = direction.cross(sideA).normalize().scale(width * 0.5);

        Vec3[] corners = {sideA.add(sideB), sideA.subtract(sideB), sideA.scale(-1).subtract(sideB), sideA.scale(-1).add(sideB)};
        for (int i = 0; i < 4; i++) {
            Vec3 c0 = corners[i];
            Vec3 c1 = corners[(i + 1) % 4];
            Vec3 normal = c0.add(c1).normalize();
            vertex(pose, buffer, from.add(c0), u0, v0, light, normal);
            vertex(pose, buffer, to.add(c0), u1, v0, light, normal);
            vertex(pose, buffer, to.add(c1), u1, v1, light, normal);
            vertex(pose, buffer, from.add(c1), u0, v1, light, normal);
        }

        Vec3 back = direction.scale(-1);
        vertex(pose, buffer, from.add(corners[0]), u0, v0, light, back);
        vertex(pose, buffer, from.add(corners[1]), u1, v0, light, back);
        vertex(pose, buffer, from.add(corners[2]), u1, v1, light, back);
        vertex(pose, buffer, from.add(corners[3]), u0, v1, light, back);
        vertex(pose, buffer, to.add(corners[3]), u0, v0, light, direction);
        vertex(pose, buffer, to.add(corners[2]), u1, v0, light, direction);
        vertex(pose, buffer, to.add(corners[1]), u1, v1, light, direction);
        vertex(pose, buffer, to.add(corners[0]), u0, v1, light, direction);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, float u, float v, int light, Vec3 normal) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
