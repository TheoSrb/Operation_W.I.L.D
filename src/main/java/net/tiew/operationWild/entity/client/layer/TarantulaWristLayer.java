package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.client.model.TarantulaModel;
import net.tiew.operationWild.entity.client.render.TarantulaRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TarantulaWristLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float WRIST_DOWN = 8.5f / 16f;
    private static final float ARM_CENTER_X = 1f / 16f;
    private static final float ARM_FRONT = 2f / 16f;
    private static final float WRIST_SINK = 2f / 16f;
    private static final float SHOULDER_X = 6f / 16f;
    private static final float SHOULDER_TOP = 0f;
    private static final float SHOULDER_BACK = 0.5f / 16f;
    private static final float SHOULDER_YAW = 18f;
    private static final float SHOULDER_SINK = 1.5f / 16f;
    private static final float MODEL_FEET = 24f / 16f;
    private static final float ARM_SCALE = 0.8f;
    private static final float CLIMB_LIFT = 0.22f;
    private static final float GLOW_PIVOT = 20f / 16f;
    private static final float GLOW_SCALE = 1.14f;
    private static final float GLOW_ALPHA = 200f;
    private static final int GLOW_RGB = 0xFFE9A8;

    private final TarantulaModel<TarantulaEntity> model;

    public TarantulaWristLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.model = new TarantulaModel<>(models.bakeLayer(TarantulaModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;
        for (Entity passenger : player.getPassengers()) {
            if (!(passenger instanceof TarantulaEntity tarantula) || tarantula.isClimbing()) continue;
            int slot = tarantula.carrySlot();
            float side = slot == TarantulaEntity.SLOT_LEFT ? 1f : -1f;
            ModelPart arm = slot == TarantulaEntity.SLOT_LEFT ? getParentModel().leftArm : getParentModel().rightArm;
            float blend = tarantula.shoulderBlend(partialTicks);

            poseStack.pushPose();
            arm.translateAndRotate(poseStack);
            wristFrame(poseStack, side);
            Matrix4f wrist = new Matrix4f(poseStack.last().pose());
            poseStack.popPose();

            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);
            shoulderFrame(poseStack, side);
            Matrix4f shoulder = new Matrix4f(poseStack.last().pose());
            poseStack.popPose();

            poseStack.pushPose();
            applyBlended(poseStack, wrist, shoulder, blend);
            float sink = Mth.lerp(blend, WRIST_SINK, SHOULDER_SINK);
            TarantulaModel.ARM_SCUTTLE = Mth.sin(blend * Mth.PI);
            try {
                renderModel(this.model, tarantula, partialTicks, poseStack, bufferSource, packedLight, sink);
            } finally {
                TarantulaModel.ARM_SCUTTLE = 0f;
            }
            poseStack.popPose();
        }
    }

    private static void applyBlended(PoseStack poseStack, Matrix4f wrist, Matrix4f shoulder, float blend) {
        Matrix4f target;
        if (blend <= 0f) {
            target = wrist;
        } else if (blend >= 1f) {
            target = shoulder;
        } else {
            Vector3f from = wrist.getTranslation(new Vector3f());
            Vector3f to = shoulder.getTranslation(new Vector3f());
            Vector3f scaleFrom = wrist.getScale(new Vector3f());
            Vector3f scaleTo = shoulder.getScale(new Vector3f());
            Quaternionf rotationFrom = wrist.getNormalizedRotation(new Quaternionf());
            Quaternionf rotationTo = shoulder.getNormalizedRotation(new Quaternionf());
            Vector3f position = from.lerp(to, blend).add(0f, CLIMB_LIFT * Mth.sin(blend * Mth.PI), 0f);
            target = new Matrix4f().translation(position)
                    .rotate(rotationFrom.slerp(rotationTo, blend))
                    .scale(scaleFrom.lerp(scaleTo, blend));
        }
        poseStack.last().pose().set(target);
        poseStack.last().normal().set(new Matrix3f(target).normal());
    }

    private static void wristFrame(PoseStack poseStack, float side) {
        poseStack.translate(side * ARM_CENTER_X, WRIST_DOWN, -ARM_FRONT);
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
    }

    private static void shoulderFrame(PoseStack poseStack, float side) {
        poseStack.translate(side * SHOULDER_X, SHOULDER_TOP, SHOULDER_BACK);
        poseStack.mulPose(Axis.YP.rotationDegrees(-side * SHOULDER_YAW));
    }

    public static void renderOnArm(TarantulaModel<TarantulaEntity> model, TarantulaEntity tarantula, int slot, float partialTicks,
                                   PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        wristFrame(poseStack, slot == TarantulaEntity.SLOT_LEFT ? 1f : -1f);
        renderModel(model, tarantula, partialTicks, poseStack, bufferSource, packedLight, WRIST_SINK);
    }

    public static void renderModel(TarantulaModel<TarantulaEntity> model, TarantulaEntity tarantula, float partialTicks,
                                   PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float sink) {
        float scale = ARM_SCALE * tarantula.getScale();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0f, -MODEL_FEET + sink / scale, 0f);

        float ageInTicks = tarantula.tickCount + partialTicks;
        TarantulaModel.RENDER_ON_ARM = true;
        try {
            model.prepareMobModel(tarantula, 0f, 0f, partialTicks);
            model.setupAnim(tarantula, 0f, 0f, ageInTicks, 0f, 0f);
            int overlay = LivingEntityRenderer.getOverlayCoords(tarantula, 0f);
            model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TarantulaRenderer.textureFor(tarantula))),
                    packedLight, overlay, -1);
            if (tarantula.isTame()) {
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TarantulaLayer.TAMED_EYES)),
                        packedLight, overlay, -1);
            }
            float glow = tarantula.selectionGlow();
            if (glow > 0f) {
                poseStack.pushPose();
                poseStack.translate(0f, GLOW_PIVOT, 0f);
                poseStack.scale(GLOW_SCALE, GLOW_SCALE, GLOW_SCALE);
                poseStack.translate(0f, -GLOW_PIVOT, 0f);
                int alpha = (int) (glow * GLOW_ALPHA);
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TarantulaRenderer.textureFor(tarantula))),
                        LightTexture.FULL_BRIGHT, overlay, (alpha << 24) | GLOW_RGB);
                poseStack.popPose();
            }
        } finally {
            TarantulaModel.RENDER_ON_ARM = false;
        }
    }
}
