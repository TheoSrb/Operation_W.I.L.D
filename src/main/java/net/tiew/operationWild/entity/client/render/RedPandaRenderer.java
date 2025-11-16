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
import net.tiew.operationWild.entity.client.layer.RedPandaLayer;
import net.tiew.operationWild.entity.client.model.RedPandaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.variants.RedPandaVariant;

import java.util.Map;

public class RedPandaRenderer extends OWEntityRenderer<RedPandaEntity, RedPandaModel<RedPandaEntity>> {
    private static final Map<RedPandaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(RedPandaVariant.class), map -> {
        map.put(RedPandaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/red_panda/red_panda_default.png"));
        map.put(RedPandaVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/red_panda/red_panda_dark.png"));
    });

    public RedPandaRenderer(EntityRendererProvider.Context context) {
        super(context, new RedPandaModel<>(context.bakeLayer(RedPandaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new RedPandaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(RedPandaEntity red_panda) {
        return LOCATION_BY_VARIANT.get(red_panda.getVariant());
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
