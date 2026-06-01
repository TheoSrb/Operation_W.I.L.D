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
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.render.BoaRenderer;
import net.tiew.operationWild.entity.client.skin.BoaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class BoaSkinRenderLayer extends RenderLayer<BoaEntity, BoaModel<BoaEntity>> {

    private final Map<ModelLayerLocation, BoaModel<BoaEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public BoaSkinRenderLayer(BoaRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, BoaEntity boa,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        BoaSkin skin = SkinRegistry.BoaSkins.get(boa.getVariant());

        if (skin.getMode() == BoaSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        BoaModel<BoaEntity> overlayModel = getOrBakeModel(layer);
                        // Recopie la pose du modèle de base déjà animé (évite désync + z-fighting).
                        overlayModel.copyPoseFrom(this.getParentModel());

                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(boa, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, boa, this.getParentModel());
    }

    private BoaModel<BoaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new BoaModel<>(context.bakeLayer(l)));
    }
}
