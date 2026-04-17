package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

public class CartoonTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_cartoon.png");

    public CartoonTigerSkin() {
        // REPLACEMENT mode: swaps model entirely
        super(Mode.REPLACEMENT, TEXTURE, null, TigerModelSkins.LAYER_CARTOON, TigerModelSkins::createSkinCartoon);
    }
}