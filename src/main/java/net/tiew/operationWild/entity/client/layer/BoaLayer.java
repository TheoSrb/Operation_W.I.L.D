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
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.render.BoaRenderer;
import net.tiew.operationWild.entity.client.util.OWFrameOffsetVertexConsumer;
import net.tiew.operationWild.entity.variants.BoaVariant;

public class BoaLayer extends RenderLayer<BoaEntity, BoaModel<BoaEntity>> {

    private static final ResourceLocation MESMERIZING_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/mesmerizing_eyes.png");
    private static final int EYES_FRAME_COUNT = 3;
    private static final int EYES_TICKS_PER_FRAME = 3;

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_necklace.png");
    private static final ResourceLocation TEETHS_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_teeths.png");
    private static final ResourceLocation LEVIATHAN_SKIN_TEETHS_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_leviathan_teeths.png");

    public BoaLayer(BoaRenderer boaRenderer) {
        super(boaRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight,
                       BoaEntity boa, float v, float v1, float v2, float v3, float v4, float v5) {

        if (boa.isHypnotizing()) {
            renderMesmerizingEyes(poseStack, multiBufferSource, packedLight, boa, v2);
        }

        float healthTier = boa.getMaxHealth() / 4;

        if (boa.isTame() && !boa.isInResurrection()) {
        }

        if (boa.isCombo()) {
            ResourceLocation teeths = boa.getVariant() == BoaVariant.Cosmetics.LEVIATHAN.variant ? LEVIATHAN_SKIN_TEETHS_TEXTURE : TEETHS_TEXTURE;
            renderOverlay(poseStack, multiBufferSource, teeths, false, packedLight);
        }

        if (boa.getHealth() < healthTier)
            renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (boa.getHealth() < (healthTier * 2))
            renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (boa.getHealth() < (healthTier * 3))
            renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderMesmerizingEyes(PoseStack poseStack, MultiBufferSource bufferSource,
                                       int packedLight, BoaEntity boa, float partialTick) {
        float time = boa.tickCount + partialTick;
        int frame = (int) (time / EYES_TICKS_PER_FRAME) % EYES_FRAME_COUNT;

        VertexConsumer base = bufferSource.getBuffer(RenderType.eyes(MESMERIZING_EYES_TEXTURE));
        VertexConsumer animated = new OWFrameOffsetVertexConsumer(base, frame, EYES_FRAME_COUNT);
        this.getParentModel().renderToBuffer(poseStack, animated, 15728640, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource,
                               ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vc = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vc, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource,
                                        ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vc = bufferSource.getBuffer(renderType);
        int opaqueColor = (color & 0x00FFFFFF) | 0xFF000000;

        this.getParentModel().renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, opaqueColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
