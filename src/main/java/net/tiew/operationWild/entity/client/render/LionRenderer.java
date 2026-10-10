package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.layer.LionLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.skins.LionSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.skin.LionSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class LionRenderer extends OWEntityRenderer<LionEntity, LionModel<LionEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, LionModel<LionEntity>> modelCache = new HashMap<>();

    public LionRenderer(EntityRendererProvider.Context context) {
        super(context, new LionModel<>(context.bakeLayer(LionModel.LAYER_LOCATION)), 0.8f);
        this.context = context;
        this.addLayer(new LionLayer(this));
        this.addLayer(new LionSkinRenderLayer(this, context));
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LionEntity lion) {
        LionSkin skin = SkinRegistry.LionSkins.get(lion.getVariant());
        if (skin.getMode() == LionSkin.Mode.OVERLAY) {
            return SkinRegistry.LionSkins.get(lion.getInitialVariant()).getTexture(lion);
        }
        return skin.getTexture(lion);
    }

    @Override
    public void render(LionEntity lion, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        LionSkin skin = SkinRegistry.LionSkins.get(lion.getVariant());
        this.model = skin.getMode() == LionSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(LionModel.LAYER_LOCATION))
                : getOrBakeModel(LionModel.LAYER_LOCATION);

        super.render(lion, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private LionModel<LionEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new LionModel<>(context.bakeLayer(l)));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return -0.25;
    }
}
