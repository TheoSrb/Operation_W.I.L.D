package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.skin.KodiakSkin;

public class SkeletonKodiakSkin extends KodiakSkin {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_skeleton.png");
    private static final ResourceLocation GLOW    = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_skeleton_glowing.png");

    public SkeletonKodiakSkin() {
        super(Mode.BASE, TEXTURE, null, null, null);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  KodiakEntity kodiak, KodiakModel<KodiakEntity> model) {
        renderCutout(poseStack, bufferSource, TEXTURE, packedLight, packedOverlay, model);
        renderGlow(poseStack, bufferSource, GLOW, model);
    }
}
