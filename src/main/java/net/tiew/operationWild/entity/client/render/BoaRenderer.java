package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void render(BoaEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // REPLACEMENT : on echange le modele de la tete par celui du skin avant le rendu,
        // exactement comme TigerRenderer.render(). La queue (entites BoaTailPart) est
        // rendue par BoaTailPartRenderer, qui prend deja la texture du skin.
        BoaSkin skin = SkinRegistry.BoaSkins.get(entity.getVariant());
        this.model = skin.getMode() == BoaSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(BoaModel.LAYER_LOCATION))
                : getOrBakeModel(BoaModel.LAYER_LOCATION);

        // Lissage vertical de la tete (montee progressive sur les blocs, comme la queue).
        poseStack.pushPose();
        poseStack.translate(0.0, entity.getHeadRenderYLag(), 0.0);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    private BoaModel<BoaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new BoaModel<>(context.bakeLayer(l)));
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