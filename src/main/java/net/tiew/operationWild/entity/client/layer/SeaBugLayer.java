package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.SeaBugModel;
import net.tiew.operationWild.entity.client.render.SeaBugRenderer;
import net.tiew.operationWild.entity.misc.SeaBugEntity;

public class SeaBugLayer extends RenderLayer<SeaBugEntity, SeaBugModel<SeaBugEntity>> {
    private static final ResourceLocation LIGHTS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug_light.png");
    private static final ResourceLocation SEABUG_CRITICAL = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug_critical.png");

    private static final ResourceLocation GLASS_CRITICAL_STAGE_0 = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug_glass_critical_stage_0.png");
    private static final ResourceLocation GLASS_CRITICAL_STAGE_1 = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug_glass_critical_stage_1.png");
    private static final ResourceLocation GLASS_CRITICAL_STAGE_2 = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug_glass_critical_stage_2.png");

    public SeaBugLayer(SeaBugRenderer seaBugRenderer) {
        super(seaBugRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, SeaBugEntity seaBug, float v, float v1, float v2, float v3, float v4, float v5) {
        if (seaBug.isLightOn()) {
            renderOverlay(poseStack, multiBufferSource, LIGHTS, true, packedLight);
        }

        float opacity = 1 - (seaBug.getHealth() / seaBug.getMaxHealth());
        renderOverlayWithOpacity(poseStack, multiBufferSource, SEABUG_CRITICAL, false, packedLight, opacity);

        if (seaBug.getHealth() < seaBug.getMaxHealth() / 4) renderOverlayNoCull(poseStack, multiBufferSource, GLASS_CRITICAL_STAGE_2, packedLight);
        else if (seaBug.getHealth() < seaBug.getMaxHealth() / 3) renderOverlayNoCull(poseStack, multiBufferSource, GLASS_CRITICAL_STAGE_1, packedLight);
        else if (seaBug.getHealth() < seaBug.getMaxHealth() / 2) renderOverlayNoCull(poseStack, multiBufferSource, GLASS_CRITICAL_STAGE_0, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayNoCull(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
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