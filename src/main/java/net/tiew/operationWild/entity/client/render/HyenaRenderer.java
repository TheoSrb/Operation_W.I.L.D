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
import net.tiew.operationWild.entity.client.layer.HyenaLayer;
import net.tiew.operationWild.entity.client.model.HyenaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.HyenaEntity;
import net.tiew.operationWild.entity.variants.HyenaVariant;

import java.util.Map;

public class HyenaRenderer extends OWEntityRenderer<HyenaEntity, HyenaModel<HyenaEntity>> {
    private static final Map<HyenaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(HyenaVariant.class), map -> {
        map.put(HyenaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hyena/hyena_default.png"));
        map.put(HyenaVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hyena/hyena_grey.png"));
        map.put(HyenaVariant.YELLOW, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hyena/hyena_yellow.png"));
        map.put(HyenaVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hyena/hyena_dark.png"));
    });

    public HyenaRenderer(EntityRendererProvider.Context context) {
        super(context, new HyenaModel<>(context.bakeLayer(HyenaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new HyenaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(HyenaEntity hyena) {
        return LOCATION_BY_VARIANT.get(hyena.getVariant());
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
