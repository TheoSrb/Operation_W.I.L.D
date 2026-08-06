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
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.ElephantRenderer;

public class ElephantLayer extends RenderLayer<ElephantEntity, ElephantModel<ElephantEntity>> {

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("elephant_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("elephant_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("elephant_bloody_stage_2.png");

    private static final ResourceLocation SADDLE_TEXTURE = tex("elephant_saddle.png");
    private static final ResourceLocation SADDLE_WOOL_0_TEXTURE = tex("elephant_saddle_wool_layer_0.png");
    private static final ResourceLocation SADDLE_WOOL_1_TEXTURE = tex("elephant_saddle_wool_layer_1.png");

    private static final ResourceLocation NECKLACE_TEXTURE = tex("elephant_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = tex("elephant_necklace_spikes.png");

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/" + path);
    }

    public ElephantLayer(ElephantRenderer elephantRenderer) {
        super(elephantRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, ElephantEntity elephant, float v, float v1, float v2, float v3, float v4, float v5) {
        double elephantHealthTier = elephant.getMaxHealth() / 4;

        if (elephant.isTame() && !elephant.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, elephant.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }

        // La selle se peint en trois passes : le cuir et les ferrures d'abord, puis chacune des deux
        // bandes de laine avec sa propre teinte. Les deux calques sont dessinés séparément — un seul
        // appel teinté les colorerait de la même couleur, ce qui vide de son sens le choix de deux
        // laines au Sellier.
        if (elephant.isSaddled()) {
            renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);
            renderOverlayWithColor(poseStack, multiBufferSource, SADDLE_WOOL_0_TEXTURE, false, packedLight, elephant.getSaddleWoolColor(0));
            renderOverlayWithColor(poseStack, multiBufferSource, SADDLE_WOOL_1_TEXTURE, false, packedLight, elephant.getSaddleWoolColor(1));
        }

        if (elephant.getHealth() < elephantHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (elephant.getHealth() < (elephantHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (elephant.getHealth() < (elephantHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, -1);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        int opaqueColor = (color & 0x00FFFFFF) | 0xFF000000;

        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, opaqueColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
