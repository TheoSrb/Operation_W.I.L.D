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
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.client.model.GorillaModel;
import net.tiew.operationWild.entity.client.render.GorillaRenderer;
import net.tiew.operationWild.entity.client.skin.GorillaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class GorillaSkinRenderLayer extends RenderLayer<GorillaEntity, GorillaModel<GorillaEntity>> {

    private final Map<ModelLayerLocation, GorillaModel<GorillaEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public GorillaSkinRenderLayer(GorillaRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       GorillaEntity gorilla,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        GorillaSkin skin = SkinRegistry.GorillaSkins.get(gorilla.getVariant());

        if (skin.getMode() == GorillaSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        GorillaModel<GorillaEntity> overlayModel = getOrBakeModel(layer);
                        overlayModel.copyPoseFrom(this.getParentModel());

                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(gorilla, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, gorilla, this.getParentModel());
    }

    private GorillaModel<GorillaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new GorillaModel<>(context.bakeLayer(l)));
    }
}
