package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.KodiakRenderer;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.variants.TigerVariant;

public class TigerLayer extends RenderLayer<TigerEntity, TigerModel<TigerEntity>> {

    private static final ResourceLocation DEFAULT_ANGRY_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_default_angry_eyes.png");
    private static final ResourceLocation WHITE_ANGRY_EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_white_angry_eyes.png");

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_saddle.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_flag_saddle.png");

    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_necklace_spikes.png");


    public TigerLayer(TigerRenderer tigerRenderer) {
        super(tigerRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, TigerEntity tiger, float v, float v1, float v2, float v3, float v4, float v5) {
        // Pendant le Shadow Strike tous les layers disparaissent avec le Tigre
        if (tiger.isShadowStrikeActive()) return;

        double kodiakHealthTier = tiger.getMaxHealth() / 4;

        if (tiger.isTame() && !tiger.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, tiger.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }

        if (tiger.isMad() && !tiger.isEating() && tiger.getVariant() != TigerVariant.SKIN_VIRUS) {
            ResourceLocation angryEyes = tiger.getVariant() == TigerVariant.WHITE ? WHITE_ANGRY_EYES_TEXTURE : DEFAULT_ANGRY_EYES_TEXTURE;
            renderOverlay(poseStack, multiBufferSource, angryEyes, true, packedLight);
        }

        if (tiger.isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);

        // Ferrures du porte-drapeau : quelques pixels qui s'ajoutent à la selle et n'ont de sens que
        // lorsque la hampe est plantée dessus. Ils vivent dans une texture à part et non dans celle de
        // la hampe : celle-ci n'est appliquée qu'au sous-arbre du drapeau, et son empreinte UV recouvre
        // celle du corps — les y laisser revenait à ne jamais les dessiner. Passe calquée sur la selle,
        // dont ils sont le prolongement.
        if (tiger.carriesTribeFlag()) renderOverlay(poseStack, multiBufferSource, FLAG_SADDLE_TEXTURE, false, packedLight);

        if (tiger.getHealth() < kodiakHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (tiger.getHealth() < (kodiakHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (tiger.getHealth() < (kodiakHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        int opaqueColor = (color & 0x00FFFFFF) | 0xFF000000;

        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, opaqueColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
