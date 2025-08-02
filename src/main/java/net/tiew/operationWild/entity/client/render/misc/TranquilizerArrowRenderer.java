package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.misc.TranquilizerArrowModel;
import net.tiew.operationWild.entity.custom.misc.TranquilizerArrow;

public class TranquilizerArrowRenderer extends EntityRenderer<TranquilizerArrow> {
    private TranquilizerArrowModel model;

    public TranquilizerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TranquilizerArrowModel(context.bakeLayer(TranquilizerArrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(TranquilizerArrow pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();

        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));

        pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));

        float shakeTime = (float) pEntity.shakeTime - pPartialTicks;
        if (shakeTime > 0.0F) {
            float shakeAngle = -Mth.sin(shakeTime * 3.0F) * shakeTime;
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(shakeAngle));
        }

        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));

        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);

        pPoseStack.translate(-4.0F, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(pEntity)));
        PoseStack.Pose lastPose = pPoseStack.last();

        this.vertex(lastPose, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, pPackedLight);
        this.vertex(lastPose, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, pPackedLight);

        for (int i = 0; i < 4; ++i) {
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            this.vertex(lastPose, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(lastPose, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(lastPose, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(lastPose, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
        }

        pPoseStack.popPose();

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TranquilizerArrow tranquilizerArrowEntity) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tranquilizer_arrow/tranquilizer_arrow.png");
    }

    public void vertex(PoseStack.Pose p_327779_, VertexConsumer p_253902_, int p_254058_, int p_254338_, int p_254196_, float p_254003_, float p_254165_, int p_253982_, int p_254037_, int p_254038_, int p_254271_) {
        p_253902_.addVertex(p_327779_, (float)p_254058_, (float)p_254338_, (float)p_254196_).setColor(-1).setUv(p_254003_, p_254165_).setOverlay(OverlayTexture.NO_OVERLAY).setLight(p_254271_).setNormal(p_327779_, (float)p_253982_, (float)p_254038_, (float)p_254037_);
    }
}
