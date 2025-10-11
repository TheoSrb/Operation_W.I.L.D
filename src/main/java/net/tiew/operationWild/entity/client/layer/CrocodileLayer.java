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
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.model.CrocodileModel;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.CrocodileRenderer;
import net.tiew.operationWild.entity.client.render.KodiakRenderer;

public class CrocodileLayer extends RenderLayer<CrocodileEntity, CrocodileModel<CrocodileEntity>> {
    private static final ResourceLocation BABY_EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/baby_eyes.png");

    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/in_resurrection.png");
    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skins/crocodile_skin_gold_glowing.png");

    private static final ResourceLocation ANGRY_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_angry_eyes.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_saddle.png");

    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_necklace_spikes.png");


    public CrocodileLayer(CrocodileRenderer kodiakRenderer) {
        super(kodiakRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CrocodileEntity crocodile, float v, float v1, float v2, float v3, float v4, float v5) {
        double kodiakHealthTier = crocodile.getMaxHealth() / 4;

        if (crocodile.getMaturationPercentage() < 60 && crocodile.getMaturationPercentage() > 0) renderOverlay(poseStack, multiBufferSource, BABY_EYES, false, packedLight);
        /*else if (crocodile.isTame() && !crocodile.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, crocodile.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }*/

        if (crocodile.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - crocodile.getResurrectionPercentage() / 100.0f));
            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);
            renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, true, packedLight);
        }

        /*if (crocodile.isMad()) {
            renderOverlay(poseStack, multiBufferSource, ANGRY_EYES_TEXTURE, false, packedLight);
        }*/

        //if (crocodile.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);

        if (crocodile.getHealth() < kodiakHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (crocodile.getHealth() < (kodiakHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (crocodile.getHealth() < (kodiakHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
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
