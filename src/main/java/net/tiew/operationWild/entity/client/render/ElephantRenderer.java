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
import net.tiew.operationWild.entity.client.layer.ElephantLayer;
import net.tiew.operationWild.entity.client.layer.skins.ElephantSkins;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.variants.ElephantVariant;

import java.util.Map;

public class ElephantRenderer extends OWEntityRenderer<ElephantEntity, ElephantModel<ElephantEntity>> {
    private static final Map<ElephantVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(ElephantVariant.class), map -> {
        map.put(ElephantVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_default.png"));
        map.put(ElephantVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_grey.png"));
        map.put(ElephantVariant.PINK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_pink.png"));

        map.put(ElephantVariant.SKIN_DEMON, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_demon.png"));
        map.put(ElephantVariant.SKIN_ZOMBIE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_zombie.png"));
        map.put(ElephantVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_gold.png"));
    });

    public final ElephantLayer elephantLayer;

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION)), 1.5f);
        this.elephantLayer = new ElephantLayer(this);
        this.addLayer(this.elephantLayer);
        this.addLayer(new ElephantSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElephantEntity elephant) {
        return LOCATION_BY_VARIANT.get(elephant.getVariant());
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
