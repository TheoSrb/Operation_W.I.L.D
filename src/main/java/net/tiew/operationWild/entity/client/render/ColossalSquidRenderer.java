package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.client.layer.ColossalSquidLayer;
import net.tiew.operationWild.entity.client.layer.skins.ColossalSquidSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.ColossalSquidModel;
import net.tiew.operationWild.entity.client.skin.ColossalSquidSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class ColossalSquidRenderer extends OWEntityRenderer<ColossalSquidEntity, ColossalSquidModel<ColossalSquidEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, ColossalSquidModel<ColossalSquidEntity>> modelCache = new HashMap<>();

    public ColossalSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new ColossalSquidModel<>(context.bakeLayer(ColossalSquidModel.LAYER_LOCATION)), 1.6f);
        this.context = context;
        this.addLayer(new ColossalSquidLayer(this));
        this.addLayer(new ColossalSquidSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(ColossalSquidEntity squid) {
        ColossalSquidSkin skin = SkinRegistry.ColossalSquidSkins.get(squid.getVariant());
        if (skin.getMode() == ColossalSquidSkin.Mode.OVERLAY) {
            return SkinRegistry.ColossalSquidSkins.get(squid.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(ColossalSquidEntity squid, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ColossalSquidSkin skin = SkinRegistry.ColossalSquidSkins.get(squid.getVariant());
        this.model = skin.getMode() == ColossalSquidSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(ColossalSquidModel.LAYER_LOCATION))
                : getOrBakeModel(ColossalSquidModel.LAYER_LOCATION);
        this.model.externalRiderPitch = squid.getRidePitch(partialTicks);
        this.model.externalBankRoll = squid.getBankRoll(partialTicks);

        super.render(squid, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private ColossalSquidModel<ColossalSquidEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new ColossalSquidModel<>(context.bakeLayer(l)));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
