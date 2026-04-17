package net.tiew.operationWild.entity.client.skin.skins;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

public class PizzaChefTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_pizza_chef.png");

    public PizzaChefTigerSkin() {
        super(Mode.OVERLAY, TEXTURE, TEXTURE, TigerModelSkins.LAYER_PIZZA_CHEF, TigerModelSkins::createSkinPizzaChef);
    }
}