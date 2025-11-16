package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.layer.LionLayer;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.variants.LionVariant;

import java.util.Map;

public class LionRenderer extends OWEntityRenderer<LionEntity, LionModel<LionEntity>> {
    private static final Map<LionVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(LionVariant.class), map -> {
        map.put(LionVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_default.png"));
        map.put(LionVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_dark.png"));
        map.put(LionVariant.WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_white.png"));
        map.put(LionVariant.LIONESS_DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_default.png"));
        map.put(LionVariant.LIONESS_DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_dark.png"));
        map.put(LionVariant.LIONESS_WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_white.png"));
        map.put(LionVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_skin_gold.png"));
        map.put(LionVariant.LIONESS_SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_skin_gold.png"));
    });

    public LionRenderer(EntityRendererProvider.Context context) {
        super(context, new LionModel<>(context.bakeLayer(LionModel.LAYER_LOCATION)), 0.9f);
        this.addLayer(new LionLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LionEntity lionEntity) {
        return LOCATION_BY_VARIANT.get(lionEntity.getVariant());
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return 0.1;
    }
}