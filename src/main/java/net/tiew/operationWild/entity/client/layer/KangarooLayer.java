package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.model.KangarooModel;
import net.tiew.operationWild.entity.client.render.KangarooRenderer;

public class KangarooLayer extends RenderLayer<KangarooEntity, KangarooModel<KangarooEntity>> {

    private static final ResourceLocation BOXING_GLOVES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/kangaroo.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/kangaroo_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/kangaroo_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/kangaroo_bloody_stage_2.png");

    public KangarooLayer(KangarooRenderer kangarooRenderer) {
        super(kangarooRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, KangarooEntity kangaroo, float v, float v1, float v2, float v3, float v4, float v5) {

        if (kangaroo.isWearingBoxingGloves()) renderOverlay(poseStack, multiBufferSource, BOXING_GLOVES_TEXTURE, packedLight);

        double kangarooHealthTier = kangaroo.getMaxHealth() / 4;

        if (kangaroo.getHealth() < kangarooHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (kangaroo.getHealth() < (kangarooHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (kangaroo.getHealth() < (kangarooHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        this.getParentModel().renderGeometryOnly(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}
