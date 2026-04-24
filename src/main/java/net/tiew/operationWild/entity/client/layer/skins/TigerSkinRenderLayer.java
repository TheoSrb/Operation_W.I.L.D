package net.tiew.operationWild.entity.client.layer.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.client.skin.TigerSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class TigerSkinRenderLayer extends RenderLayer<TigerEntity, TigerModel<TigerEntity>> {

    // Cache for overlay/replacement models (avoids re-baking each frame)
    private final Map<ModelLayerLocation, TigerModel<TigerEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public TigerSkinRenderLayer(TigerRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, TigerEntity tiger,
            float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch
    ) {
        TigerSkin skin = SkinRegistry.TigerSkins.get(tiger.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model
        if (skin.getMode() == TigerSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        TigerModel<TigerEntity> overlayModel = getOrBakeModel(layer);
                        overlayModel.setupAnim(tiger, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                        float alpha = TigerRenderer.currentAlpha;
                        int color = ((int)(alpha * 255) << 24) | 0x00FFFFFF;

                        // Toujours entityTranslucent — compatible Iris/Sodium
                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color);
                    })
            );
        }

        // Extra custom layers (glow, animated face textures, etc.)
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(tiger, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, tiger, this.getParentModel());
    }

    private TigerModel<TigerEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new TigerModel<>(context.bakeLayer(l)));
    }
}