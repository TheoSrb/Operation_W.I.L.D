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
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.client.model.OrcaModel;
import net.tiew.operationWild.entity.client.render.OrcaRenderer;
import net.tiew.operationWild.entity.client.skin.OrcaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class OrcaSkinRenderLayer extends RenderLayer<OrcaEntity, OrcaModel<OrcaEntity>> {

    private final Map<ModelLayerLocation, OrcaModel<OrcaEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public OrcaSkinRenderLayer(OrcaRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       OrcaEntity orca,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        OrcaSkin skin = SkinRegistry.OrcaSkins.get(orca.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model
        if (skin.getMode() == OrcaSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    OrcaModel<OrcaEntity> overlayModel = getOrBakeModel(layer);
                    overlayModel.setupAnim(orca, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                })
            );
        }

        // Extra custom layers (glow, animated elements, etc.)
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(orca, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, orca, this.getParentModel());
    }

    private OrcaModel<OrcaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new OrcaModel<>(context.bakeLayer(l)));
    }
}
