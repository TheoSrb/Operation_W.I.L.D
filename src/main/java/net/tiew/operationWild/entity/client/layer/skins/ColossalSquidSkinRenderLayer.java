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
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.client.model.ColossalSquidModel;
import net.tiew.operationWild.entity.client.render.ColossalSquidRenderer;
import net.tiew.operationWild.entity.client.skin.ColossalSquidSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class ColossalSquidSkinRenderLayer extends RenderLayer<ColossalSquidEntity, ColossalSquidModel<ColossalSquidEntity>> {

    private final Map<ModelLayerLocation, ColossalSquidModel<ColossalSquidEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public ColossalSquidSkinRenderLayer(ColossalSquidRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       ColossalSquidEntity squid,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        ColossalSquidSkin skin = SkinRegistry.ColossalSquidSkins.get(squid.getVariant());

        if (skin.getMode() == ColossalSquidSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    ColossalSquidModel<ColossalSquidEntity> overlayModel = getOrBakeModel(layer);
                    overlayModel.copyPoseFrom(this.getParentModel());

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(squid, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, squid, this.getParentModel());
    }

    private ColossalSquidModel<ColossalSquidEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new ColossalSquidModel<>(context.bakeLayer(l)));
    }
}
