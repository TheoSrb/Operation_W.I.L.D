package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.model.skin.BoaModelSkins;
import net.tiew.operationWild.entity.client.skin.BoaSkin;

/**
 * Skin "Boa Leviathan".
 *
 * Calque sur {@link VirusTigerSkin} cote tigre : skin en mode REPLACEMENT qui remplace
 * totalement le modele (cf. {@link BoaModelSkins#LAYER_LEVIATHAN}) et la texture. La
 * queue multipart recupere automatiquement la texture du skin cote
 * BoaTailPartRenderer.getTextureLocation(), donc tout le serpent porte le skin.
 *
 * Les dents possedent une texture emissive separee (boa_skin_leviathan_teeths.png) qui
 * est rendue en glow par-dessus le modele, exactement comme le glow du virus du tigre :
 * seuls les pixels des dents sont opaques dans cette texture, donc seules les dents
 * brillent.
 */
public class LeviathanBoaSkin extends BoaSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_leviathan.png");
    private static final ResourceLocation TEETH_GLOW =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_leviathan_teeths.png");

    public LeviathanBoaSkin() {
        super(Mode.REPLACEMENT, TEXTURE, null, BoaModelSkins.LAYER_LEVIATHAN, BoaModelSkins::createSkinReplacement);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  BoaEntity boa, BoaModel<BoaEntity> model) {
        renderGlow(poseStack, bufferSource, TEETH_GLOW, model);
    }
}
