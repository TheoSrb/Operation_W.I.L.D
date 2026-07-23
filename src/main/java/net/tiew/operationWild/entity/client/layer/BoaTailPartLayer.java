package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.client.model.OWFlagModel;
import net.tiew.operationWild.entity.client.render.BoaTailPartRenderer;

public class BoaTailPartLayer extends RenderLayer<BoaTailPart, EntityModel<BoaTailPart>> {

    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_bloody_stage_2.png");
    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_saddle.png");
    private static final ResourceLocation FLAG_SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_flag_saddle.png");

    public BoaTailPartLayer(BoaTailPartRenderer boaRenderer) {
        super(boaRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight,
                       BoaTailPart boaTailPart, float v, float v1, float v2, float v3, float v4, float v5) {

        float ratio = boaTailPart.getParentHealthRatio();

        if      (ratio < 0.25f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (ratio < 0.50f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (ratio < 0.75f)  renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);

        // Selle : chaque segment de queue affiche sa part de la selle quand le Boa est selle,
        // pour que la selle suive tout le serpent (tete via BoaLayer + tous les segments ici).
        if (boaTailPart.isBoaSaddled()) {
            renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);
        }

        // Ferrures du porte-drapeau : le harnais qui s'ajoute a la selle sous la hampe. Il ne
        // concerne que le segment porteur — seul son modele declare les os du drapeau, les six
        // autres repondent false — et n'a de sens que si le boa arbore reellement son etendard.
        if (this.getParentModel() instanceof OWFlagModel model && model.hasTribeFlag()
                && boaTailPart.getParentForRender() instanceof OWEntity boa && boa.carriesTribeFlag()) {
            renderOverlay(poseStack, multiBufferSource, FLAG_SADDLE_TEXTURE, false, packedLight);
        }
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource,
                               ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vc = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vc, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}