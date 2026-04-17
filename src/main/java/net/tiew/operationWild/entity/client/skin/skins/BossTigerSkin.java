package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

public class BossTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_boss.png");

    public BossTigerSkin() {
        super(Mode.OVERLAY, TEXTURE, TEXTURE, TigerModelSkins.LAYER_BOSS_CHIEF, TigerModelSkins::createSkinBossChief);
    }
}