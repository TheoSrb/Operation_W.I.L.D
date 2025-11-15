package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.render.KodiakRenderer;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.render.LionRenderer;

public class LionLayer extends RenderLayer<LionEntity, LionModel<LionEntity>> {
    private static final ResourceLocation ANGRY_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_default_angry_eyes.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_saddle.png");

    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_necklace_spikes.png");


    public LionLayer(LionRenderer lionRenderer) {
        super(lionRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, LionEntity lion, float v, float v1, float v2, float v3, float v4, float v5) {
        double lionHealthTier = lion.getMaxHealth() / 4;

        /*if (kodiak.isTame() && !kodiak.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, kodiak.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }*/

        if (lion.isMad()) {
            renderOverlay(poseStack, multiBufferSource, ANGRY_EYES_TEXTURE, false, packedLight);
        }

        if (lion.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);

        if (lion.getHealth() < lionHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (lion.getHealth() < (lionHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (lion.getHealth() < (lionHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        int opaqueColor = (color & 0x00FFFFFF) | 0xFF000000;

        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, opaqueColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
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
