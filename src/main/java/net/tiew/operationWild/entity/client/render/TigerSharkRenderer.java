package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.TigerSharkLayer;
import net.tiew.operationWild.entity.client.model.TigerSharkModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.variants.TigerSharkVariant;

import java.util.Map;

public class TigerSharkRenderer extends OWEntityRenderer<TigerSharkEntity, TigerSharkModel<TigerSharkEntity>> {
    private static final Map<TigerSharkVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(TigerSharkVariant.class), map -> {
        map.put(TigerSharkVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_default.png"));
        map.put(TigerSharkVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_blue.png"));
        map.put(TigerSharkVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_grey.png"));

        map.put(TigerSharkVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/skins/tiger_shark_skin_gold.png"));
    });

    public TigerSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new TigerSharkModel<>(context.bakeLayer(TigerSharkModel.LAYER_LOCATION)), 0.6f);
        //this.addLayer(new TigerSharkSkins(this));
        this.addLayer(new TigerSharkLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TigerSharkEntity tigerSharkEntity) {
        return LOCATION_BY_VARIANT.get(tigerSharkEntity.getVariant());
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
