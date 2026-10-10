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
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.render.LionRenderer;
import net.tiew.operationWild.entity.client.skin.LionSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class LionSkinRenderLayer extends RenderLayer<LionEntity, LionModel<LionEntity>> {

    private final Map<ModelLayerLocation, LionModel<LionEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public LionSkinRenderLayer(LionRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       LionEntity lion,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        LionSkin skin = SkinRegistry.LionSkins.get(lion.getVariant());

        if (skin.getMode() == LionSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                    skin.getOverlayTexture().ifPresent(overlayTex -> {
                        LionModel<LionEntity> overlayModel = getOrBakeModel(layer);
                        overlayModel.copyPoseFrom(this.getParentModel());

                        RenderType renderType = RenderType.entityTranslucent(overlayTex);
                        VertexConsumer vc = bufferSource.getBuffer(renderType);
                        overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    })
            );
        }

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(lion, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, lion, this.getParentModel());
    }

    private LionModel<LionEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new LionModel<>(context.bakeLayer(l)));
    }
}
