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
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.client.model.RedPandaModel;
import net.tiew.operationWild.entity.client.render.RedPandaRenderer;
import net.tiew.operationWild.entity.client.skin.RedPandaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class RedPandaSkinRenderLayer extends RenderLayer<RedPandaEntity, RedPandaModel<RedPandaEntity>> {

    private final Map<ModelLayerLocation, RedPandaModel<RedPandaEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public RedPandaSkinRenderLayer(RedPandaRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       RedPandaEntity redPanda,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        RedPandaSkin skin = SkinRegistry.RedPandaSkins.get(redPanda.getVariant());

        if (skin.getMode() == RedPandaSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    RedPandaModel<RedPandaEntity> overlayModel = getOrBakeModel(layer);
                    overlayModel.copyPoseFrom(this.getParentModel());

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, -1);
                })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(redPanda, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, redPanda, this.getParentModel());
    }

    private RedPandaModel<RedPandaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new RedPandaModel<>(context.bakeLayer(l)));
    }
}
