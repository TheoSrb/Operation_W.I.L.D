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
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.client.model.PeacockModel;
import net.tiew.operationWild.entity.client.render.PeacockRenderer;

import java.util.HashMap;
import java.util.Map;

public class PeacockLayer extends RenderLayer<PeacockEntity, PeacockModel<PeacockEntity>> {

    private static final ResourceLocation SADDLE_TEXTURE = tex("peacock_saddle.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = tex("peacock_flag_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = tex("peacock_necklace.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("peacock_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("peacock_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("peacock_bloody_stage_2.png");

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/" + path);
    }

    public PeacockLayer(PeacockRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, PeacockEntity peacock,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (peacock.isTame() && !peacock.isInResurrection()) {
            renderOverlayWithColor(poseStack, bufferSource, NECKLACE_TEXTURE, packedLight, peacock.getNecklaceColor());
        }

        if (peacock.isSaddled()) renderOverlay(poseStack, bufferSource, SADDLE_TEXTURE, packedLight);
        if (peacock.carriesTribeFlag()) renderOverlay(poseStack, bufferSource, FLAG_SADDLE_TEXTURE, packedLight);

        double healthTier = peacock.getMaxHealth() / 4;
        if (peacock.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (peacock.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (peacock.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
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
