package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.custom.living.TigerEntity;

public class TigerLayer extends RenderLayer<TigerEntity, TigerModel<TigerEntity>> {
    private static final ResourceLocation BABY_EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/baby_eyes.png");

    private static final ResourceLocation ULTIMATE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/ultimate.png");
    private static final ResourceLocation ULTIMATE_GLOWING = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/ultimate_glowing.png");

    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/in_resurrection.png");
    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_gold_glowing.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_2.png");

    private static final ResourceLocation ANGRY_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_angry_eyes.png");
    private static final ResourceLocation ANGRY_EYES_WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_white_angry_eyes.png");

    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_saddle.png");

    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_necklace_spikes.png");

    public TigerLayer(TigerRenderer tigerRenderer) {
        super(tigerRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, TigerEntity tiger, float v, float v1, float v2, float v3, float v4, float v5) {
        double tigerHealthTier = tiger.getMaxHealth() / 4;

        if (tiger.getMaturationPercentage() < 60 && tiger.getMaturationPercentage() > 0) renderOverlay(poseStack, multiBufferSource, BABY_EYES, false, packedLight);

        if (tiger.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - tiger.getResurrectionPercentage() / 100.0f));
            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);
            renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, true, packedLight);
        }

        if (tiger.isUltimate()) {
            float opacity = (float) (0.65 * (1 - ((float) tiger.ultimateTimer / 300)));
            renderOverlayWithOpacity(poseStack, multiBufferSource, ULTIMATE, false, packedLight, opacity);
            renderOverlay(poseStack, multiBufferSource, ULTIMATE_GLOWING, true, packedLight);
        }

        if (tiger.getHealth() < tigerHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (tiger.getHealth() < (tigerHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (tiger.getHealth() < (tigerHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);

        if (tiger.isTame() && !tiger.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, tiger.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }
        if (tiger.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);
        if (tiger.isMad()) {
            if (tiger.isWhite()) renderOverlay(poseStack, multiBufferSource, ANGRY_EYES_WHITE_TEXTURE, true, packedLight);
            else renderOverlay(poseStack, multiBufferSource, ANGRY_EYES_TEXTURE, true, packedLight);
        }
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
