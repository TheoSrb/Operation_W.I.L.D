package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.layer.ElephantLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.skins.ElephantSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.skin.ElephantSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class ElephantRenderer extends OWEntityRenderer<ElephantEntity, ElephantModel<ElephantEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, ElephantModel<ElephantEntity>> modelCache = new HashMap<>();

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION)), 1.8f);
        this.context = context;
        this.addLayer(new ElephantLayer(this));
        this.addLayer(new ElephantSkinRenderLayer(this, context));
        // En dernier : le drapeau porte la bannière de tribu par-dessus le skin actif.
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElephantEntity elephant) {
        ElephantSkin skin = SkinRegistry.ElephantSkins.get(elephant.getVariant());
        // For OVERLAY skins, the base model shows the elephant's natural variant texture
        if (skin.getMode() == ElephantSkin.Mode.OVERLAY) {
            return SkinRegistry.ElephantSkins.get(elephant.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(ElephantEntity elephant, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        ElephantSkin skin = SkinRegistry.ElephantSkins.get(elephant.getVariant());
        this.model = skin.getMode() == ElephantSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(ElephantModel.LAYER_LOCATION))
                : getOrBakeModel(ElephantModel.LAYER_LOCATION);

        // Après l'échange de modèle : c'est celui qui va être dessiné qui doit porter le roulis.
        this.model.externalBankRoll = elephant.getBankRoll(partialTicks);

        super.render(elephant, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private ElephantModel<ElephantEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new ElephantModel<>(context.bakeLayer(l)));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0.4;
    }
}
