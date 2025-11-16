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
import net.tiew.operationWild.entity.client.layer.WalrusLayer;
import net.tiew.operationWild.entity.client.model.WalrusModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.variants.WalrusVariant;

import java.util.Map;

public class WalrusRenderer extends OWEntityRenderer<WalrusEntity, WalrusModel<WalrusEntity>> {
    private static final Map<WalrusVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(WalrusVariant.class), map -> {
        map.put(WalrusVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/walrus/walrus_default.png"));
        map.put(WalrusVariant.RED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/walrus/walrus_red.png"));
    });

    public WalrusRenderer(EntityRendererProvider.Context context) {
        super(context, new WalrusModel<>(context.bakeLayer(WalrusModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new WalrusLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(WalrusEntity walrus) {
        return LOCATION_BY_VARIANT.get(walrus.getVariant());
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
