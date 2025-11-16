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
import net.tiew.operationWild.entity.client.layer.TigerLayer;
import net.tiew.operationWild.entity.client.layer.skins.TigerSkins;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.variants.TigerVariant;

import java.util.Map;

public class TigerRenderer extends OWEntityRenderer<TigerEntity, TigerModel<TigerEntity>> {
    private static final Map<TigerVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(TigerVariant.class), map -> {
        map.put(TigerVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_default.png"));
        map.put(TigerVariant.LIGHT_ORANGE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_light_orange.png"));
        map.put(TigerVariant.GOLDEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_golden.png"));
        map.put(TigerVariant.WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_white.png"));

        map.put(TigerVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_gold.png"));
        map.put(TigerVariant.SKIN_MAGMA, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_magma.png"));
        map.put(TigerVariant.SKIN_VIRUS, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus.png"));
        map.put(TigerVariant.SKIN_DAMNED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_damned.png"));
    });

    public TigerRenderer(EntityRendererProvider.Context context) {
        super(context, new TigerModel<>(context.bakeLayer(TigerModel.LAYER_LOCATION)), 1.0f);
        this.addLayer(new TigerSkins(this));
        this.addLayer(new TigerLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TigerEntity tigerEntity) {
        return LOCATION_BY_VARIANT.get(tigerEntity.getVariant());
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
