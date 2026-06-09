package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.layer.KangarooLayer;
import net.tiew.operationWild.entity.client.layer.skins.KangarooSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.KangarooModel;
import net.tiew.operationWild.entity.client.skin.KangarooSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class KangarooRenderer extends OWEntityRenderer<KangarooEntity, KangarooModel<KangarooEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, KangarooModel<KangarooEntity>> modelCache = new HashMap<>();

    public KangarooRenderer(EntityRendererProvider.Context context) {
        super(context, new KangarooModel<>(context.bakeLayer(KangarooModel.LAYER_LOCATION)), 0.6f);
        this.context = context;
        this.addLayer(new KangarooLayer(this));
        this.addLayer(new KangarooSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(KangarooEntity kangaroo) {
        KangarooSkin skin = SkinRegistry.KangarooSkins.get(kangaroo.getVariant());
        // For OVERLAY skins, the base model shows the kangaroo's natural variant texture
        if (skin.getMode() == KangarooSkin.Mode.OVERLAY) {
            return SkinRegistry.KangarooSkins.get(kangaroo.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(KangarooEntity kangaroo, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        KangarooSkin skin = SkinRegistry.KangarooSkins.get(kangaroo.getVariant());
        this.model = skin.getMode() == KangarooSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(KangarooModel.LAYER_LOCATION))
                : getOrBakeModel(KangarooModel.LAYER_LOCATION);

        super.render(kangaroo, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private KangarooModel<KangarooEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new KangarooModel<>(context.bakeLayer(l)));
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
