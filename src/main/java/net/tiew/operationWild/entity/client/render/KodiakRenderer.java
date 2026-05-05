package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.layer.KodiakLayer;
import net.tiew.operationWild.entity.client.layer.skins.KodiakSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.skin.KodiakSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class KodiakRenderer extends OWEntityRenderer<KodiakEntity, KodiakModel<KodiakEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, KodiakModel<KodiakEntity>> modelCache = new HashMap<>();

    public KodiakRenderer(EntityRendererProvider.Context context) {
        super(context, new KodiakModel<>(context.bakeLayer(KodiakModel.LAYER_LOCATION)), 1.2f);
        this.context = context;
        this.addLayer(new KodiakLayer(this));
        this.addLayer(new KodiakSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(KodiakEntity kodiak) {
        KodiakSkin skin = SkinRegistry.KodiakSkins.get(kodiak.getVariant());
        // For OVERLAY skins, the base model shows the kodiak's natural variant texture
        if (skin.getMode() == KodiakSkin.Mode.OVERLAY) {
            return SkinRegistry.KodiakSkins.get(kodiak.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(KodiakEntity kodiak, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        KodiakSkin skin = SkinRegistry.KodiakSkins.get(kodiak.getVariant());
        this.model = skin.getMode() == KodiakSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(KodiakModel.LAYER_LOCATION))
                : getOrBakeModel(KodiakModel.LAYER_LOCATION);

        this.getModel().setBufferSource(bufferSource);
        super.render(kodiak, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private KodiakModel<KodiakEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new KodiakModel<>(context.bakeLayer(l)));
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
