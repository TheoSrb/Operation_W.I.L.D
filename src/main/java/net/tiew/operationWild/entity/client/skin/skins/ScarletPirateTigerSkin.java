package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScarletPirateTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_scarlet_pirate.png");

    public ScarletPirateTigerSkin() {
        // REPLACEMENT mode: swaps model entirely
        super(Mode.REPLACEMENT, TEXTURE, null, TigerModelSkins.LAYER_SCARLET_PIRATE, TigerModelSkins::createSkinScarletPirate);
    }
}