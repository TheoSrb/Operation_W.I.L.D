package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.OrcaLayer;
import net.tiew.operationWild.entity.client.layer.skins.OrcaSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.OrcaModel;
import net.tiew.operationWild.entity.client.skin.OrcaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class OrcaRenderer extends OWEntityRenderer<OrcaEntity, OrcaModel<OrcaEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, OrcaModel<OrcaEntity>> modelCache = new HashMap<>();

    private float smoothedRiderPitch = 0f;

    public OrcaRenderer(EntityRendererProvider.Context context) {
        super(context, new OrcaModel<>(context.bakeLayer(OrcaModel.LAYER_LOCATION)), 1.2f);
        this.context = context;
        this.addLayer(new OrcaLayer(this));
        this.addLayer(new OrcaSkinRenderLayer(this, context));
        // En dernier : le drapeau porte la banniere de tribu par-dessus le skin actif.
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(OrcaEntity orca) {
        OrcaSkin skin = SkinRegistry.OrcaSkins.get(orca.getVariant());
        if (skin.getMode() == OrcaSkin.Mode.OVERLAY) {
            return SkinRegistry.OrcaSkins.get(orca.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(OrcaEntity orca, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        float pitchTarget;
        if (orca.isTame() && orca.isVehicle() && !orca.isSitting() && orca.isInWater()) {
            pitchTarget = Mth.clamp(orca.getRiderControlPitch(), -45f, 45f);
        } else if (!orca.isTame()) {
            // Pas de vérification isInWater() ici : elle peut clignoter côté client à la surface
            // Le serveur gère le retour à 0 du pitch quand le croco sort de l'eau
            pitchTarget = Mth.clamp(orca.getTargetPitch(), -40f, 40f);
        } else {
            pitchTarget = 0f;
        }

        // Lerp fixe par frame, sans dépendre de partialTicks comme exposant (évite le saut à chaque tick)
        smoothedRiderPitch = Mth.lerp(0.18f, smoothedRiderPitch, pitchTarget);

        OrcaSkin skin = SkinRegistry.OrcaSkins.get(orca.getVariant());
        this.model = skin.getMode() == OrcaSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(OrcaModel.LAYER_LOCATION))
                : getOrBakeModel(OrcaModel.LAYER_LOCATION);
        this.model.externalRiderPitch = smoothedRiderPitch;
        this.model.externalBankRoll = orca.getBankRoll(partialTicks);

        super.render(orca, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private OrcaModel<OrcaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new OrcaModel<>(context.bakeLayer(l)));
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
