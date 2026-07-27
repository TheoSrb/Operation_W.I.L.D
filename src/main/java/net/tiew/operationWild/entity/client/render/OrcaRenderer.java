package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
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

        // Une seule source, montée ou libre : le tangage est calculé par OWWaterEntity, lissé au tick
        // et interpolé à l'image. La branche sauvage lisait TARGET_PITCH, que seule la phase verticale
        // de la vadrouille alimentait — l'orque restait donc à plat en poursuite, en fuite ou sur un
        // chemin montant. Elle passait en prime par un champ de lissage PARTAGÉ par toutes les orques
        // à l'écran, qui se contaminaient mutuellement.
        float pitchTarget = orca.getRidePitch(partialTicks);

        OrcaSkin skin = SkinRegistry.OrcaSkins.get(orca.getVariant());
        this.model = skin.getMode() == OrcaSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(OrcaModel.LAYER_LOCATION))
                : getOrBakeModel(OrcaModel.LAYER_LOCATION);
        this.model.externalRiderPitch = pitchTarget;
        // Le tonneau s'ajoute à l'assiette de virage sur le même os : posé sur ALL2, il emporte donc
        // aussi le cavalier et son assise, que captureBodyState en déduit. Le siège tombant sur l'axe
        // même du roulis, le joueur pivote sur place, collé au dos, au lieu d'être promené en cercle.
        this.model.externalBankRoll = orca.getBankRoll(partialTicks) + orca.getBarrelRoll(partialTicks);

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
