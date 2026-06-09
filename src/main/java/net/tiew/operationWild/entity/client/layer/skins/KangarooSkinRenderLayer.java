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
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.model.KangarooModel;
import net.tiew.operationWild.entity.client.render.KangarooRenderer;
import net.tiew.operationWild.entity.client.skin.KangarooSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class KangarooSkinRenderLayer extends RenderLayer<KangarooEntity, KangarooModel<KangarooEntity>> {

    private final Map<ModelLayerLocation, KangarooModel<KangarooEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public KangarooSkinRenderLayer(KangarooRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       KangarooEntity kangaroo,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        KangarooSkin skin = SkinRegistry.KangarooSkins.get(kangaroo.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model (boxing gloves).
        if (skin.getMode() == KangarooSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    KangarooModel<KangarooEntity> overlayModel = getOrBakeModel(layer);
                    // Recopie la pose du modèle de base déjà animé (évite désync + z-fighting).
                    overlayModel.copyPoseFrom(this.getParentModel());

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, -1);
                })
            );
        }

        // Extra custom layers driven by the skin (glow, animated elements, etc.)
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(kangaroo, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, kangaroo, this.getParentModel());
    }

    private KangarooModel<KangarooEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new KangarooModel<>(context.bakeLayer(l)));
    }
}
