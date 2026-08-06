package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.skin.ElephantSkin;

/**
 * Skin Démoniaque. Les deux ailes membraneuses ne sont pas un modèle à part : elles vivent dans le
 * maillage de base, effacées partout ailleurs par {@code ElephantModel.setupAnim}. Ce skin se
 * contente donc d'échanger la texture — les ailes réapparaissent d'elles-mêmes.
 */
public class DemonElephantSkin extends ElephantSkin {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_demon.png");
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_demon_glowing.png");

    public DemonElephantSkin() {
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
