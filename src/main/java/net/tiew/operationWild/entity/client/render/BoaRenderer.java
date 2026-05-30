package net.tiew.operationWild.entity.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.layer.BoaLayer;
import net.tiew.operationWild.entity.client.layer.skins.BoaSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.skin.BoaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class BoaRenderer extends OWEntityRenderer<BoaEntity, BoaModel<BoaEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, BoaModel<BoaEntity>> modelCache = new HashMap<>();

    public BoaRenderer(EntityRendererProvider.Context context) {
        super(context, new BoaModel<>(context.bakeLayer(BoaModel.LAYER_LOCATION)), 0.6f);
        this.context = context;
        this.addLayer(new BoaLayer(this));
        this.addLayer(new BoaSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(BoaEntity boa) {
        BoaSkin skin = SkinRegistry.BoaSkins.get(boa.getVariant());
        if (skin.getMode() == BoaSkin.Mode.OVERLAY) {
            return SkinRegistry.BoaSkins.get(boa.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return -0.2;
    }
}