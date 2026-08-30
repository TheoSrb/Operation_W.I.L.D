package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.client.model.GorillaModel;
import net.tiew.operationWild.entity.client.render.GorillaRenderer;

public class GorillaLayer extends RenderLayer<GorillaEntity, GorillaModel<GorillaEntity>> {

    private static final ResourceLocation ANGRY_EYES_TEXTURE = tex("gorilla_default_angry_eyes.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("gorilla_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("gorilla_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("gorilla_bloody_stage_2.png");

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/gorilla/" + path);
    }

    public GorillaLayer(GorillaRenderer gorillaRenderer) {
        super(gorillaRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GorillaEntity gorilla,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (gorilla.isMad()) {
            renderOverlay(poseStack, bufferSource, ANGRY_EYES_TEXTURE, packedLight);
        }

        double healthTier = gorilla.getMaxHealth() / 4;

        if (gorilla.getHealth() < healthTier) {
            renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        } else if (gorilla.getHealth() < healthTier * 2) {
            renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        } else if (gorilla.getHealth() < healthTier * 3) {
            renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);
        }
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        this.getParentModel().renderGeometryOnly(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}
