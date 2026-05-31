package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.skin.BoaSkin;

/**
 * Skin par defaut du Boa.
 *
 * Calque sur les classes de skin du tigre (cf. GoldTigerSkin) : une classe dediee qui
 * etend BoaSkin en mode BASE avec la texture par defaut. Permet d'avoir, pour le Boa,
 * la meme architecture de skins (classes dediees) que le tigre, et de surcharger
 * renderExtraLayers plus tard si besoin (glow, etc.).
 */
public class DefaultBoaSkin extends BoaSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_default.png");

    public DefaultBoaSkin() {
        super(Mode.BASE, TEXTURE, null, null, null);
    }
}
