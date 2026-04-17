package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.layer.TigerLayer;
import net.tiew.operationWild.entity.client.layer.skins.TigerSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

import java.util.HashMap;
import java.util.Map;

public class TigerRenderer extends OWEntityRenderer<TigerEntity, TigerModel<TigerEntity>> {

    // Alpha fade system (hidden tigers)
    private static final float ALPHA_SPEED = 1f / 1000f;
    private static final Map<Integer, Float> ALPHA_BY_ENTITY = new HashMap<>();
    public static float currentAlpha = 1f;

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, TigerModel<TigerEntity>> modelCache = new HashMap<>();

    public TigerRenderer(EntityRendererProvider.Context context) {
        super(context, new TigerModel<>(context.bakeLayer(TigerModel.LAYER_LOCATION)), 0.8f);
        this.context = context;
        this.addLayer(new TigerLayer(this));
        this.addLayer(new TigerSkinRenderLayer(this, context)); // replaces TigerSkins
    }

    @Override
    public void render(
            TigerEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight
    ) {
        TigerSkin skin = SkinRegistry.TigerSkins.get(entity.getVariant());

        // Set model: REPLACEMENT uses its own model, everything else uses the base
        this.model = skin.getMode() == TigerSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(TigerModel.LAYER_LOCATION))
                : getOrBakeModel(TigerModel.LAYER_LOCATION);

        // Alpha fade for hidden tigers
        float targetAlpha = entity.isHidden() ? 0.1f : 1f;
        float alpha = ALPHA_BY_ENTITY.getOrDefault(entity.getId(), 1f);
        alpha = alpha < targetAlpha
                ? Math.min(alpha + ALPHA_SPEED, targetAlpha)
                : Math.max(alpha - ALPHA_SPEED, targetAlpha);
        ALPHA_BY_ENTITY.put(entity.getId(), alpha);
        currentAlpha = alpha;

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TigerEntity tiger) {
        TigerSkin skin = SkinRegistry.TigerSkins.get(tiger.getVariant());
        // For OVERLAY skins, the base model must show the tiger's natural variant texture,
        // not the skin's own texture (which is the overlay accessory).
        if (skin.getMode() == TigerSkin.Mode.OVERLAY) {
            return SkinRegistry.TigerSkins.get(tiger.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    protected RenderType getRenderType(TigerEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (currentAlpha < 1f) {
            // Reuse the same translucent render type logic from before
            return RenderType.entityTranslucent(this.getTextureLocation(entity));
        }
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    private TigerModel<TigerEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new TigerModel<>(context.bakeLayer(l)));
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