package net.tiew.operationWild.entity.client.layer.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.ElephantRenderer;
import net.tiew.operationWild.entity.client.skin.ElephantSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class ElephantSkinRenderLayer extends RenderLayer<ElephantEntity, ElephantModel<ElephantEntity>> {

    private final Map<ModelLayerLocation, ElephantModel<ElephantEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public ElephantSkinRenderLayer(ElephantRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       ElephantEntity elephant,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        ElephantSkin skin = SkinRegistry.ElephantSkins.get(elephant.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model
        if (skin.getMode() == ElephantSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        ElephantModel<ElephantEntity> overlayModel = getOrBakeModel(layer);
                        // Recopie la pose du modèle de base déjà animé (évite désync + z-fighting).
                        overlayModel.copyPoseFrom(this.getParentModel());

                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(elephant, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, elephant, this.getParentModel());
    }

    private ElephantModel<ElephantEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new ElephantModel<>(context.bakeLayer(l)));
    }
}
