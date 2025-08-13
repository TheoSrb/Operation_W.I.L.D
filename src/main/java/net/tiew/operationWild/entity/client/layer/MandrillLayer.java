package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.MandrillModel;
import net.tiew.operationWild.entity.client.render.MandrillRenderer;
import net.tiew.operationWild.entity.custom.living.MandrillEntity;

public class MandrillLayer extends RenderLayer<MandrillEntity, MandrillModel<MandrillEntity>> {
    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/in_resurrection.png");
    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/skins/mandrill_skin_gold_glowing.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_saddle.png");

    public MandrillLayer(MandrillRenderer mandrillRenderer) {
        super(mandrillRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, MandrillEntity mandrill, float v, float v1, float v2, float v3, float v4, float v5) {
        double mandrillHealthTier = mandrill.getMaxHealth() / 4;
        if (mandrill.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - mandrill.getResurrectionPercentage() / 100.0f));
            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);
            renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, true, packedLight);
        }
        if (mandrill.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);
        if (mandrill.getHealth() < mandrillHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (mandrill.getHealth() < (mandrillHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (mandrill.getHealth() < (mandrillHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    private void renderOverlayWithOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, float opacity) {
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = (int)(opacity * 255.0f);
        int color = 0xFFFFFF | (alpha << 24);
        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
