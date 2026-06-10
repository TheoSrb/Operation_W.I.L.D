package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.model.KangarooModel;
import net.tiew.operationWild.entity.client.skin.KangarooSkin;

public class GoldKangarooSkin extends KangarooSkin {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/skins/kangaroo_skin_gold.png");
    private static final ResourceLocation GLOW    = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kangaroo/skins/kangaroo_skin_gold_glowing.png");

    public GoldKangarooSkin() {
        super(Mode.BASE, TEXTURE, null, null, null);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  KangarooEntity kangaroo, KangarooModel<KangarooEntity> model) {
        renderCutout(poseStack, bufferSource, TEXTURE, packedLight, packedOverlay, model);
        renderGlow(poseStack, bufferSource, GLOW, model);
    }
}
