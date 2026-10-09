package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.client.model.HippopotamusModel;
import net.tiew.operationWild.entity.client.render.HippopotamusRenderer;

import java.util.HashMap;
import java.util.Map;

public class HippopotamusLayer extends RenderLayer<HippopotamusEntity, HippopotamusModel<HippopotamusEntity>> {

    private static final ResourceLocation ANGRY_EYES_TEXTURE = tex("hippopotamus_default_angry_eyes.png");
    private static final ResourceLocation SADDLE_TEXTURE = tex("hippopotamus_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = tex("hippopotamus_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = tex("hippopotamus_necklace_spikes.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("hippopotamus_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("hippopotamus_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("hippopotamus_bloody_stage_2.png");

    private static final int FURY_TINT_RGB = 0xFF2A1E;
    private static final float FURY_TINT_MAX_ALPHA = 0.42f;

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private final HippopotamusRenderer renderer;

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hippopotamus/" + path);
    }

    public HippopotamusLayer(HippopotamusRenderer renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HippopotamusEntity hippo,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (hippo.isTame() && !hippo.isInResurrection()) {
            renderOverlayWithColor(poseStack, bufferSource, NECKLACE_TEXTURE, packedLight, hippo.getNecklaceColor());
            renderOverlay(poseStack, bufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }

        if (hippo.isMad()) renderOverlay(poseStack, bufferSource, ANGRY_EYES_TEXTURE, false, packedLight);
        if (hippo.isSaddled()) renderOverlay(poseStack, bufferSource, SADDLE_TEXTURE, false, packedLight);

        double healthTier = hippo.getMaxHealth() / 4;
        if (hippo.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (hippo.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (hippo.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);

        renderFuryTint(poseStack, bufferSource, packedLight, hippo, partialTick, ageInTicks);
    }

    private void renderFuryTint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                HippopotamusEntity hippo, float partialTick, float ageInTicks) {
        float fury = HippopotamusEntity.smoothStep(hippo.furyBlend(partialTick));
        if (fury <= 0.01f) return;

        float pulse = 0.72f + 0.28f * Mth.sin(ageInTicks * 0.35f);
        int alpha = Mth.clamp((int) (255f * FURY_TINT_MAX_ALPHA * fury * pulse), 0, 255);
        int color = (alpha << 24) | FURY_TINT_RGB;

        ResourceLocation texture = this.renderer.getTextureLocation(hippo);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
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
