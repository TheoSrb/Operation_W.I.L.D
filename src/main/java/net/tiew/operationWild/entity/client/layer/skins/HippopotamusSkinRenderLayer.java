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
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.client.model.HippopotamusModel;
import net.tiew.operationWild.entity.client.render.HippopotamusRenderer;
import net.tiew.operationWild.entity.client.skin.HippopotamusSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class HippopotamusSkinRenderLayer extends RenderLayer<HippopotamusEntity, HippopotamusModel<HippopotamusEntity>> {

    private final Map<ModelLayerLocation, HippopotamusModel<HippopotamusEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public HippopotamusSkinRenderLayer(HippopotamusRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       HippopotamusEntity hippo,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        HippopotamusSkin skin = SkinRegistry.HippopotamusSkins.get(hippo.getVariant());

        if (skin.getMode() == HippopotamusSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        HippopotamusModel<HippopotamusEntity> overlayModel = getOrBakeModel(layer);
                        overlayModel.copyPoseFrom(this.getParentModel());

                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(hippo, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, hippo, this.getParentModel());
    }

    private HippopotamusModel<HippopotamusEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new HippopotamusModel<>(context.bakeLayer(l)));
    }
}
