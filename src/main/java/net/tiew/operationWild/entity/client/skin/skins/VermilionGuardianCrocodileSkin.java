package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.CrocodileModelSkins;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.CrocodileSkin;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

public class VermilionGuardianCrocodileSkin extends CrocodileSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skins/crocodile_skin_vermilion_guardian.png");

    public VermilionGuardianCrocodileSkin() {
        super(Mode.REPLACEMENT, TEXTURE, null, CrocodileModelSkins.LAYER_VERMILION_GUARDIAN, CrocodileModelSkins::createSkinVermilionGuardian);
    }
}