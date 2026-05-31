package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.client.render.BoaTailPartRenderer;

public class BoaTailPartLayer extends RenderLayer<BoaTailPart, EntityModel<BoaTailPart>> {

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_2.png");

    public BoaTailPartLayer(BoaTailPartRenderer boaRenderer) {
        super(boaRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight,
                       BoaTailPart boaTailPart, float v, float v1, float v2, float v3, float v4, float v5) {

        float ratio = boaTailPart.getParentHealthRatio();

        if      (ratio < 0.25f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (ratio < 0.50f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (ratio < 0.75f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource,
                               ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vc = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vc, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}