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
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.render.LionRenderer;

import java.util.HashMap;
import java.util.Map;

public class LionLayer extends RenderLayer<LionEntity, LionModel<LionEntity>> {

    private static final ResourceLocation ANGRY_EYES_TEXTURE = tex("lion_default_angry_eyes.png");
    private static final ResourceLocation SADDLE_TEXTURE = tex("lion_saddle.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = tex("lion_flag_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = tex("lion_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = tex("lion_necklace_spikes.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("lion_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("lion_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("lion_bloody_stage_2.png");

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/" + path);
    }

    public LionLayer(LionRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LionEntity lion,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (lion.isTame() && !lion.isInResurrection()) {
            renderOverlayWithColor(poseStack, bufferSource, NECKLACE_TEXTURE, packedLight, lion.getNecklaceColor());
            renderOverlay(poseStack, bufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }

        if (lion.isMad() || lion.isEmpowered() || lion.isRallied()) renderOverlay(poseStack, bufferSource, ANGRY_EYES_TEXTURE, true, packedLight);
        if (lion.isSaddled()) renderOverlay(poseStack, bufferSource, SADDLE_TEXTURE, false, packedLight);
        if (lion.carriesTribeFlag()) renderOverlay(poseStack, bufferSource, FLAG_SADDLE_TEXTURE, false, packedLight);

        double healthTier = lion.getMaxHealth() / 4;
        if (lion.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (lion.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (lion.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                               boolean glowLayer, int packedLight) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, -1);
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
