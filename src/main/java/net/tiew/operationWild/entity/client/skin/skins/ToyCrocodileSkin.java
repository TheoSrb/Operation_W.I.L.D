package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.CrocodileModelSkins;
import net.tiew.operationWild.entity.client.skin.CrocodileSkin;

public class ToyCrocodileSkin extends CrocodileSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skins/crocodile_skin_toy.png");

    public ToyCrocodileSkin() {
        super(Mode.REPLACEMENT, TEXTURE, null, CrocodileModelSkins.LAYER_TOY, CrocodileModelSkins::createSkinToy);
    }
}