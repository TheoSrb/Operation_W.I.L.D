package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.ChameleonLayer;
import net.tiew.operationWild.entity.client.model.ChameleonModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.variants.ChameleonVariant;

import java.util.Map;

public class ChameleonRenderer extends OWEntityRenderer<ChameleonEntity, ChameleonModel<ChameleonEntity>> {
    private static final Map<ChameleonVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(ChameleonVariant.class), map -> {
        map.put(ChameleonVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/chameleon/chameleon_default.png"));
    });

    public ChameleonRenderer(EntityRendererProvider.Context context) {
        super(context, new ChameleonModel<>(context.bakeLayer(ChameleonModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new ChameleonLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ChameleonEntity chameleon) {
        return LOCATION_BY_VARIANT.get(chameleon.getVariant());
    }

    @Override
    public double distanceToShowRealInfos() {
        return 2;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
