package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.TigerSharkModel;
import net.tiew.operationWild.entity.client.render.TigerSharkRenderer;
import net.tiew.operationWild.entity.custom.living.TigerSharkEntity;
import net.tiew.operationWild.entity.variants.TigerSharkVariant;

public class TigerSharkLayer extends RenderLayer<TigerSharkEntity, TigerSharkModel<TigerSharkEntity>> {
    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/in_resurrection.png");
    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/skins/peacock_skin_gold_glowing.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_bloody_stage_2.png");

    private static final ResourceLocation DEFAULT_CLOSED_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_default_closed_eyes.png");
    private static final ResourceLocation BLUE_CLOSED_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_blue_closed_eyes.png");
    private static final ResourceLocation GREY_CLOSED_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_grey_closed_eyes.png");

    private static final ResourceLocation BLEEDING_EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_smelling_blood_eyes.png");

    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_saddle.png");

    public TigerSharkLayer(TigerSharkRenderer tigerRenderer) {
        super(tigerRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, TigerSharkEntity tigerSharkEntity, float v, float v1, float v2, float v3, float v4, float v5) {
        double peacockHealthTier = tigerSharkEntity.getMaxHealth() / 4;
        if (tigerSharkEntity.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - tigerSharkEntity.getResurrectionPercentage() / 100.0f));
            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);
            //renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, true, packedLight);
        }

        if (tigerSharkEntity.isSleeping()) {
            if (tigerSharkEntity.getVariant() == TigerSharkVariant.DEFAULT) {
                renderOverlay(poseStack, multiBufferSource, DEFAULT_CLOSED_EYES_TEXTURE, false, packedLight);
            } else if (tigerSharkEntity.getVariant() == TigerSharkVariant.BLUE) {
                renderOverlay(poseStack, multiBufferSource, BLUE_CLOSED_EYES_TEXTURE, false, packedLight);
            } else if (tigerSharkEntity.getVariant() == TigerSharkVariant.GREY) {
                renderOverlay(poseStack, multiBufferSource, GREY_CLOSED_EYES_TEXTURE, false, packedLight);
            }
        }

        if (tigerSharkEntity.isSmellingBlood) renderOverlay(poseStack, multiBufferSource, BLEEDING_EYES, false, packedLight);

        if (tigerSharkEntity.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);

        if (tigerSharkEntity.getHealth() < peacockHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (tigerSharkEntity.getHealth() < (peacockHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (tigerSharkEntity.getHealth() < (peacockHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
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
        RenderType renderType = RenderType.entityTranslucent(texture);
        if (glowLayer) {
            renderType = RenderType.eyes(texture);
        }
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }

}