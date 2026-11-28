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
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;
import net.tiew.operationWild.entity.client.model.GiraffeModel;
import net.tiew.operationWild.entity.client.render.GiraffeRenderer;

import java.util.HashMap;
import java.util.Map;

public class GiraffeLayer extends RenderLayer<GiraffeEntity, GiraffeModel<GiraffeEntity>> {

    private static final ResourceLocation SADDLE_TEXTURE = tex("giraffe_saddle.png");
    private static final ResourceLocation PLATFORM_TEXTURE = tex("giraffe_platform.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = tex("giraffe_flag_saddle.png");
    private static final ResourceLocation NECKLACE_TEXTURE = tex("giraffe_necklace.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("giraffe_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("giraffe_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("giraffe_bloody_stage_2.png");
    private static final int HORN_GLOW_COLOR = 0xFFD86B;

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/giraffe/" + path);
    }

    public GiraffeLayer(GiraffeRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GiraffeEntity giraffe,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (giraffe.isTame() && !giraffe.isInResurrection()) {
            renderOverlayWithColor(poseStack, bufferSource, NECKLACE_TEXTURE, packedLight, giraffe.getNecklaceColor());
        }

        if (giraffe.isSaddled()) renderOverlay(poseStack, bufferSource, SADDLE_TEXTURE, packedLight);
        if (giraffe.hasPlatform()) renderOverlay(poseStack, bufferSource, PLATFORM_TEXTURE, packedLight);
        if (giraffe.carriesTribeFlag()) renderOverlay(poseStack, bufferSource, FLAG_SADDLE_TEXTURE, packedLight);

        double healthTier = giraffe.getMaxHealth() / 4;
        if (giraffe.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (giraffe.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (giraffe.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);

        float glow = hornGlow(giraffe, partialTick, ageInTicks);
        if (glow > 0.01f) {
            ResourceLocation texture = this.getTextureLocation(giraffe);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(texture));
            int alpha = Mth.clamp((int) (glow * 255f), 0, 255);
            this.getParentModel().renderHorns(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, (alpha << 24) | HORN_GLOW_COLOR);
        }
    }

    private static float hornGlow(GiraffeEntity giraffe, float partialTick, float ageInTicks) {
        float glow = 0f;
        if (giraffe.isBlessing()) glow = 0.85f + 0.15f * Mth.sin(ageInTicks * 0.8f);
        if (giraffe.isSentinelCasting() || giraffe.isSentinelActive()) glow = Math.max(glow, 0.7f + 0.3f * Mth.sin(ageInTicks * 0.35f));
        return glow;
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
