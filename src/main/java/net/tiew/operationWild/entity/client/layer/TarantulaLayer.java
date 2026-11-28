package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.client.model.TarantulaModel;

import java.util.HashMap;
import java.util.Map;

public class TarantulaLayer<T extends TarantulaEntity> extends RenderLayer<T, TarantulaModel<T>> {

    public static final ResourceLocation TAMED_EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tarantula/tarantula_tamed_eyes.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("tarantula_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("tarantula_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("tarantula_bloody_stage_2.png");

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    public TarantulaLayer(RenderLayerParent<T, TarantulaModel<T>> parent) {
        super(parent);
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tarantula/" + path);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T tarantula,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!tarantula.isTame() || tarantula.isInvisible()) return;

        double healthTier = tarantula.getMaxHealth() / 4;
        if (tarantula.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (tarantula.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (tarantula.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);

        this.getParentModel().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TAMED_EYES)), packedLight,
                LivingEntityRenderer.getOverlayCoords(tarantula, 0f), -1);
    }

    private static boolean isAvailable(ResourceLocation texture) {
        return AVAILABLE.computeIfAbsent(texture,
                location -> Minecraft.getInstance().getResourceManager().getResource(location).isPresent());
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}
