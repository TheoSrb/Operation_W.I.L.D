package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.flying.CondorEntity;
import net.tiew.operationWild.entity.client.model.CondorModel;
import net.tiew.operationWild.entity.client.render.CondorRenderer;

import java.util.HashMap;
import java.util.Map;

public class CondorLayer extends RenderLayer<CondorEntity, CondorModel<CondorEntity>> {

    private static final ResourceLocation SADDLE_TEXTURE = tex("condor_saddle.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = tex("condor_flag_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = tex("condor_necklace.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("condor_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("condor_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("condor_bloody_stage_2.png");

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/condor/" + path);
    }

    public CondorLayer(CondorRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CondorEntity condor,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (condor.isTame() && !condor.isInResurrection()) {
            renderOverlayWithColor(poseStack, bufferSource, NECKLACE_TEXTURE, packedLight, condor.getNecklaceColor());
        }

        if (condor.isSaddled()) renderOverlay(poseStack, bufferSource, SADDLE_TEXTURE, packedLight);
        if (condor.carriesTribeFlag()) renderOverlay(poseStack, bufferSource, FLAG_SADDLE_TEXTURE, packedLight);

        double healthTier = condor.getMaxHealth() / 4;
        if (condor.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (condor.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (condor.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                                        int packedLight, int color) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                (color & 0x00FFFFFF) | 0xFF000000);
    }

    private static boolean isAvailable(ResourceLocation texture) {
        return AVAILABLE.computeIfAbsent(texture,
                location -> Minecraft.getInstance().getResourceManager().getResource(location).isPresent());
    }
}
