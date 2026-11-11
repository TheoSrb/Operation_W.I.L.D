package net.tiew.operationWild.entity.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.PeacockLayer;
import net.tiew.operationWild.entity.client.model.PeacockModel;
import net.tiew.operationWild.entity.variants.PeacockVariant;

import java.util.EnumMap;
import java.util.Map;

public class PeacockRenderer extends OWEntityRenderer<PeacockEntity, PeacockModel<PeacockEntity>> {

    private static final Map<PeacockVariant, ResourceLocation> TEXTURES = new EnumMap<>(PeacockVariant.class);

    static {
        TEXTURES.put(PeacockVariant.DEFAULT, tex("peacock_default.png"));
        TEXTURES.put(PeacockVariant.GREEN, tex("peacock_green.png"));
        TEXTURES.put(PeacockVariant.PURPLE, tex("peacock_purple.png"));
        TEXTURES.put(PeacockVariant.CAMEO, tex("peacock_cameo.png"));
        TEXTURES.put(PeacockVariant.ALBINO, tex("peacock_albino.png"));
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/" + path);
    }

    public PeacockRenderer(EntityRendererProvider.Context context) {
        super(context, new PeacockModel<>(context.bakeLayer(PeacockModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new PeacockLayer(this));
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PeacockEntity peacock) {
        return TEXTURES.getOrDefault(peacock.getVariant(), TEXTURES.get(PeacockVariant.DEFAULT));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return -0.1;
    }
}
