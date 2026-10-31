package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.client.model.ColossalSquidModel;
import net.tiew.operationWild.entity.client.render.ColossalSquidRenderer;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

public class ColossalSquidLayer extends RenderLayer<ColossalSquidEntity, ColossalSquidModel<ColossalSquidEntity>> {

    public ColossalSquidLayer(ColossalSquidRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, ColossalSquidEntity squid,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (squid.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - squid.getResurrectionPercentage() / 100.0f));
            ResourceLocation texture = SkinRegistry.ColossalSquidSkins.get(squid.getVariant()).getTexture();
            renderOverlayWithOpacity(poseStack, multiBufferSource, texture, packedLight, opacity);
        }
    }

    private void renderOverlayWithOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight, float opacity) {
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = (int) (opacity * 255.0f);
        int color = 0xFFFFFF | (alpha << 24);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
