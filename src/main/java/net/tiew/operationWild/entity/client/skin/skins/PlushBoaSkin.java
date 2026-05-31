package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.BoaModelSkins;
import net.tiew.operationWild.entity.client.skin.BoaSkin;

/**
 * Skin "Boa Peluche" (Plush Boa).
 *
 * Skin en mode REPLACEMENT qui remplace totalement le modele (meme geometrie que le
 * Leviathan, cf. {@link BoaModelSkins#LAYER_PLUSH}) et la texture. La queue multipart
 * herite automatiquement de la texture du skin via BoaTailPartRenderer, donc le serpent
 * entier porte le skin. Pas de couche supplementaire (contrairement au Leviathan et ses
 * dents emissives).
 */
public class PlushBoaSkin extends BoaSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_plush.png");

    public PlushBoaSkin() {
        super(Mode.REPLACEMENT, TEXTURE, null, BoaModelSkins.LAYER_PLUSH, BoaModelSkins::createSkinReplacement);
    }
}
