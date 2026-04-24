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
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.client.model.CrocodileModel;
import net.tiew.operationWild.entity.client.render.CrocodileRenderer;
import net.tiew.operationWild.entity.client.skin.CrocodileSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class CrocodileSkinRenderLayer extends RenderLayer<CrocodileEntity, CrocodileModel<CrocodileEntity>> {

    private final Map<ModelLayerLocation, CrocodileModel<CrocodileEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public CrocodileSkinRenderLayer(CrocodileRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       CrocodileEntity crocodile,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        CrocodileSkin skin = SkinRegistry.CrocodileSkins.get(crocodile.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model
        if (skin.getMode() == CrocodileSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    CrocodileModel<CrocodileEntity> overlayModel = getOrBakeModel(layer);
                    overlayModel.setupAnim(crocodile, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                })
            );
        }

        // Extra custom layers (glow, animated elements, etc.)
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(crocodile, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, crocodile, this.getParentModel());
    }

    private CrocodileModel<CrocodileEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new CrocodileModel<>(context.bakeLayer(l)));
    }
}
