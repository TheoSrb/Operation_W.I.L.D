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
import net.tiew.operationWild.entity.client.layer.MantaLayer;
import net.tiew.operationWild.entity.client.model.MantaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.MantaEntity;
import net.tiew.operationWild.entity.variants.MantaVariant;

import java.util.Map;

public class MantaRenderer extends OWEntityRenderer<MantaEntity, MantaModel<MantaEntity>> {
    private static final Map<MantaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(MantaVariant.class), map -> {
        map.put(MantaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/manta/manta_default.png"));
    });

    public MantaRenderer(EntityRendererProvider.Context context) {
        super(context, new MantaModel<>(context.bakeLayer(MantaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new MantaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MantaEntity manta) {
        return LOCATION_BY_VARIANT.get(manta.getVariant());
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
