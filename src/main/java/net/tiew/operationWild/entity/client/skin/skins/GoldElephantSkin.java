package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.skin.ElephantSkin;

public class GoldElephantSkin extends ElephantSkin {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_gold.png");
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_gold_glowing.png");

    public GoldElephantSkin() {
        super(Mode.BASE, TEXTURE, null, null, null);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  ElephantEntity elephant, ElephantModel<ElephantEntity> model) {
        renderCutout(poseStack, bufferSource, TEXTURE, packedLight, packedOverlay, model);
        renderGlow(poseStack, bufferSource, GLOW, model);
    }
}
