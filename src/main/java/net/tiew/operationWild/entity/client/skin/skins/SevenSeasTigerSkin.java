package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SevenSeasTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_seven_seas.png");

    public SevenSeasTigerSkin() {
        // REPLACEMENT mode: swaps model entirely
        super(Mode.REPLACEMENT, TEXTURE, null, TigerModelSkins.LAYER_SEVEN_SEAS, TigerModelSkins::createSkinSevenSeas);
    }
}