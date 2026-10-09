package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.client.layer.HippopotamusLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.skins.HippopotamusSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.HippopotamusModel;
import net.tiew.operationWild.entity.client.skin.HippopotamusSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class HippopotamusRenderer extends OWEntityRenderer<HippopotamusEntity, HippopotamusModel<HippopotamusEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, HippopotamusModel<HippopotamusEntity>> modelCache = new HashMap<>();
    private final Map<Integer, Float> smoothedWildPitch = new HashMap<>();

    public HippopotamusRenderer(EntityRendererProvider.Context context) {
        super(context, new HippopotamusModel<>(context.bakeLayer(HippopotamusModel.LAYER_LOCATION)), 1.1f);
        this.context = context;
        this.addLayer(new HippopotamusLayer(this));
        this.addLayer(new HippopotamusSkinRenderLayer(this, context));
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(HippopotamusEntity hippo) {
        HippopotamusSkin skin = SkinRegistry.HippopotamusSkins.get(hippo.getVariant());
        if (skin.getMode() == HippopotamusSkin.Mode.OVERLAY) {
            return SkinRegistry.HippopotamusSkins.get(hippo.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(HippopotamusEntity hippo, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        HippopotamusSkin skin = SkinRegistry.HippopotamusSkins.get(hippo.getVariant());
        this.model = skin.getMode() == HippopotamusSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(HippopotamusModel.LAYER_LOCATION))
                : getOrBakeModel(HippopotamusModel.LAYER_LOCATION);

        float pitch;
        if (hippo.isTame() && hippo.isVehicle() && !hippo.isSitting() && hippo.isInWater()) {
            pitch = hippo.getRidePitch(partialTicks);
            smoothedWildPitch.put(hippo.getId(), pitch);
        } else {
            float target = !hippo.isTame() ? Mth.clamp(hippo.getTargetPitch(), -30f, 30f) : 0f;
            float previous = smoothedWildPitch.getOrDefault(hippo.getId(), 0f);
            pitch = Mth.lerp(0.18f, previous, target);
            if (Math.abs(pitch) < 0.01f) smoothedWildPitch.remove(hippo.getId());
            else smoothedWildPitch.put(hippo.getId(), pitch);
        }

        this.model.externalRiderPitch = pitch;
        this.model.externalBankRoll = hippo.getBankRoll(partialTicks);

        super.render(hippo, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private HippopotamusModel<HippopotamusEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new HippopotamusModel<>(context.bakeLayer(l)));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0.1;
    }
}
