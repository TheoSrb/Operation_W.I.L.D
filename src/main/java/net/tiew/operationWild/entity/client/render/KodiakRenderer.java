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
import net.tiew.operationWild.entity.client.layer.KodiakLayer;
import net.tiew.operationWild.entity.client.layer.skins.KodiakSkins;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.variants.KodiakVariant;

import java.util.Map;

public class KodiakRenderer extends OWEntityRenderer<KodiakEntity, KodiakModel<KodiakEntity>> {
    private static final Map<KodiakVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(KodiakVariant.class), map -> {
        map.put(KodiakVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_default.png"));
        map.put(KodiakVariant.BLACK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_black.png"));
        map.put(KodiakVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_grey.png"));
        map.put(KodiakVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_gold.png"));
        map.put(KodiakVariant.SKIN_SKELETON, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_skeleton.png"));
    });

    public KodiakRenderer(EntityRendererProvider.Context context) {
        super(context, new KodiakModel<>(context.bakeLayer(KodiakModel.LAYER_LOCATION)), 1.2f);
        this.addLayer(new KodiakLayer(this));
        this.addLayer(new KodiakSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(KodiakEntity kodiak) {
        return LOCATION_BY_VARIANT.get(kodiak.getVariant());
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
