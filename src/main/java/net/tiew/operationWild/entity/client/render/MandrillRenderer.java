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
import net.tiew.operationWild.entity.client.layer.MandrillLayer;
import net.tiew.operationWild.entity.client.model.MandrillModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.MandrillEntity;
import net.tiew.operationWild.entity.variants.MandrillVariant;

import java.util.Map;public class MandrillRenderer extends OWEntityRenderer<MandrillEntity, MandrillModel<MandrillEntity>> {
    private static final Map<MandrillVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(MandrillVariant.class), map -> {
        map.put(MandrillVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_default.png"));
        map.put(MandrillVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_blue.png"));
        map.put(MandrillVariant.SILVER, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_silver.png"));
    });

    public MandrillRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrillModel<>(context.bakeLayer(MandrillModel.LAYER_LOCATION)), 0.7f);
        this.addLayer(new MandrillLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MandrillEntity mandrill) {
        return LOCATION_BY_VARIANT.get(mandrill.getVariant());
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
