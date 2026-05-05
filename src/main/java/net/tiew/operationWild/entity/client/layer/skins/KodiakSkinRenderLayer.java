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
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.KodiakRenderer;
import net.tiew.operationWild.entity.client.skin.KodiakSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class KodiakSkinRenderLayer extends RenderLayer<KodiakEntity, KodiakModel<KodiakEntity>> {

    // Shade overlay (applied on top of any variant when isShade() is true)
    private static final ResourceLocation SKIN_SHADE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_shade.png");

    private final Map<ModelLayerLocation, KodiakModel<KodiakEntity>> modelCache = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public KodiakSkinRenderLayer(KodiakRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.context = context;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       KodiakEntity kodiak,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        KodiakSkin skin = SkinRegistry.KodiakSkins.get(kodiak.getVariant());

        // OVERLAY mode: render the overlay model on top of the base model
        if (skin.getMode() == KodiakSkin.Mode.OVERLAY) {
            skin.getModelLayer().ifPresent(layer ->
                skin.getOverlayTexture().ifPresent(overlayTex -> {
                    KodiakModel<KodiakEntity> overlayModel = getOrBakeModel(layer);
                    overlayModel.setupAnim(kodiak, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                    RenderType renderType = RenderType.entityTranslucent(overlayTex);
                    VertexConsumer vc = bufferSource.getBuffer(renderType);
                    overlayModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                })
            );
        }

        // Extra custom layers driven by the skin (glow, animated elements, etc.)
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(kodiak, 0.0f);
        skin.renderExtraLayers(poseStack, bufferSource, packedLight, packedOverlay, kodiak, this.getParentModel());

        // Shade overlay — independent of variant, triggered by isShade() flag
        if (kodiak.isShade()) {
            renderShade(poseStack, bufferSource, this.getParentModel());
        }
    }

    private void renderShade(PoseStack poseStack, MultiBufferSource bufferSource, KodiakModel<KodiakEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.beaconBeam(SKIN_SHADE_TEXTURE, true));
        model.renderGeometryOnly(poseStack, vc, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    private KodiakModel<KodiakEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new KodiakModel<>(context.bakeLayer(l)));
    }
}
