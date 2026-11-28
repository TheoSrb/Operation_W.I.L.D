package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.client.render.misc.ChameleonCamouflage;

import java.util.HashMap;
import java.util.Map;

public class ChameleonCamouflageLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = tex("chameleon_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = tex("chameleon_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = tex("chameleon_bloody_stage_2.png");

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/chameleon/" + path);
    }

    public ChameleonCamouflageLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        double healthTier = entity.getMaxHealth() / 4;
        if (entity.getHealth() < healthTier) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_2_TEXTURE, packedLight);
        else if (entity.getHealth() < healthTier * 2) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_1_TEXTURE, packedLight);
        else if (entity.getHealth() < healthTier * 3) renderOverlay(poseStack, bufferSource, BLOODY_STAGE_0_TEXTURE, packedLight);

        ChameleonEntity source = ChameleonCamouflage.mimicSource(entity);
        if (source == null) return;

        M model = this.getParentModel();
        ChameleonCamouflage.render(entity, source, entity instanceof ChameleonEntity ? this.getTextureLocation(entity) : null,
                partialTicks, bufferSource,
                consumer -> model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1));
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        if (!isAvailable(texture)) return;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }

    private static boolean isAvailable(ResourceLocation texture) {
        return AVAILABLE.computeIfAbsent(texture,
                location -> Minecraft.getInstance().getResourceManager().getResource(location).isPresent());
    }
}
