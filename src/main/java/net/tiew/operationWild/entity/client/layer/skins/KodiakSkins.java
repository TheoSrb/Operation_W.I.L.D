package net.tiew.operationWild.entity.client.layer.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.KodiakRenderer;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.variants.KodiakVariant;

public class KodiakSkins extends RenderLayer<KodiakEntity, KodiakModel<KodiakEntity>> {
    private static final ResourceLocation SKIN_GOLD_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_gold_glowing.png");
    private static final ResourceLocation SKIN_SKELETON_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_skeleton_glowing.png");
    private static final ResourceLocation SKIN_SHADE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_shade.png");

    public KodiakSkins(KodiakRenderer tigerRenderer) {
        super(tigerRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, KodiakEntity kodiak, float v, float v1, float v2, float v3, float v4, float v5) {
        if (kodiak.getVariant() == KodiakVariant.SKIN_GOLD) {
            renderOverlay(poseStack, multiBufferSource, SKIN_GOLD_GLOWING_TEXTURE, true, packedLight);
        }

        if (kodiak.getVariant() == KodiakVariant.SKIN_SKELETON) {
            renderOverlay(poseStack, multiBufferSource, SKIN_SKELETON_GLOWING_TEXTURE, true, packedLight);
        }

        if (kodiak.isShade()) {
            renderOverlayWithGlowAndOpacity(poseStack, multiBufferSource, SKIN_SHADE_TEXTURE, true, packedLight, 1.0f);
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
        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    private void renderOverlayWithGlowAndOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, float opacity) {
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = (int)(opacity * 255.0f);
        int color = (alpha << 24) | 0xFFFFFF;

        if (glowLayer) {
            RenderType renderType = RenderType.beaconBeam(texture, true);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        } else {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
        }
    }
}