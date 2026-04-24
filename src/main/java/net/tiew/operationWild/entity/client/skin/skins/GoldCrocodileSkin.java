package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.CrocodileModel;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.client.skin.CrocodileSkin;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

public class GoldCrocodileSkin extends CrocodileSkin {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skins/crocodile_skin_gold.png");
    private static final ResourceLocation EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skins/crocodile_skin_gold_eyes.png");

    public GoldCrocodileSkin() {
        super(Mode.BASE, TEXTURE, null, null, null);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  CrocodileEntity crocodile, CrocodileModel<CrocodileEntity> model) {
        renderCutout(poseStack, bufferSource, TEXTURE, packedLight, packedOverlay, model);
        renderGlow(poseStack, bufferSource, EYES, model);
    }
}