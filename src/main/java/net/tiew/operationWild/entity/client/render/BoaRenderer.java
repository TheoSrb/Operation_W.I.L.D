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
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.layer.BoaLayer;
import net.tiew.operationWild.entity.client.layer.skins.BoaSkins;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.variants.BoaVariant;

import java.util.Map;

public class BoaRenderer extends OWEntityRenderer<BoaEntity, BoaModel<BoaEntity>> {
    private static final Map<BoaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(BoaVariant.class), map -> {
        map.put(BoaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_default.png"));
        map.put(BoaVariant.YELLOW, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_yellow.png"));
        map.put(BoaVariant.BROWN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_brown.png"));
        map.put(BoaVariant.DARK_GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_dark.png"));
        map.put(BoaVariant.LIME, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_lime.png"));
        map.put(BoaVariant.ALBINO, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_albino.png"));
        map.put(BoaVariant.CORAL, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_coral.png"));

        map.put(BoaVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_gold.png"));
        map.put(BoaVariant.SKIN_LEVIATHAN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_leviathan.png"));
        map.put(BoaVariant.SKIN_PLUSH, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_plush.png"));

    });

    public BoaRenderer(EntityRendererProvider.Context context) {
        super(context, new BoaModel<>(context.bakeLayer(BoaModel.LAYER_LOCATION)), 1.0f);
        this.addLayer(new BoaSkins(this));
        this.addLayer(new BoaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(BoaEntity tigerEntity) {
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