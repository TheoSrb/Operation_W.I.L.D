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
import net.tiew.operationWild.entity.client.layer.PeacockLayer;
import net.tiew.operationWild.entity.client.layer.skins.PeacockSkins;
import net.tiew.operationWild.entity.client.model.PeacockModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.variants.PeacockVariant;

import java.util.Map;

public class PeacockRenderer extends OWEntityRenderer<PeacockEntity, PeacockModel<PeacockEntity>> {
    private static final Map<PeacockVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(PeacockVariant.class), map -> {
        map.put(PeacockVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_default.png"));
        map.put(PeacockVariant.RED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_red.png"));
        map.put(PeacockVariant.ALBINO, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_albino.png"));
        map.put(PeacockVariant.GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_green.png"));
        map.put(PeacockVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_blue.png"));

        map.put(PeacockVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/skins/peacock_skin_gold.png"));
    });

    public PeacockRenderer(EntityRendererProvider.Context context) {
        super(context, new PeacockModel<>(context.bakeLayer(PeacockModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new PeacockSkins(this));
        this.addLayer(new PeacockLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PeacockEntity tigerEntity) {
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
