package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.client.layer.CrocodileLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.layer.skins.CrocodileSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.CrocodileModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.client.skin.CrocodileSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class CrocodileRenderer extends OWEntityRenderer<CrocodileEntity, CrocodileModel<CrocodileEntity>> {

    private static final ResourceLocation CROCODILE_TAMING = ResourceLocation.fromNamespaceAndPath(
            net.tiew.operationWild.OperationWild.MOD_ID, "textures/gui/crocodile_taming.png");

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, CrocodileModel<CrocodileEntity>> modelCache = new HashMap<>();

    private float smoothedRiderPitch = 0f;

    public CrocodileRenderer(EntityRendererProvider.Context context) {
        super(context, new CrocodileModel<>(context.bakeLayer(CrocodileModel.LAYER_LOCATION)), 1.2f);
        this.context = context;
        this.addLayer(new CrocodileLayer(this));
        this.addLayer(new CrocodileSkinRenderLayer(this, context));
        // En dernier : le drapeau porte la banniere de tribu par-dessus le skin actif.
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CrocodileEntity crocodile) {
        CrocodileSkin skin = SkinRegistry.CrocodileSkins.get(crocodile.getVariant());
        if (skin.getMode() == CrocodileSkin.Mode.OVERLAY) {
            return SkinRegistry.CrocodileSkins.get(crocodile.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(CrocodileEntity crocodile, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        float pitchTarget;
        if (crocodile.isTame() && crocodile.isVehicle() && !crocodile.isSitting() && crocodile.isInWater()) {
            // Monté : même voie que l'orque. Valeur PAR ENTITÉ, lissée au tick et interpolée à
            // l'image, prise sur le regard du cavalier — donc exactement l'angle qui construit la
            // direction de nage, corps et trajectoire alignés. L'ancienne source passait par le
            // serveur puis par le champ de lissage partagé ci-dessous, d'où un tangage à la fois en
            // retard et commun à tous les crocodiles visibles.
            pitchTarget = crocodile.getRidePitch(partialTicks);
        } else {
            // Pas de vérification isInWater() ici : elle peut clignoter côté client à la surface
            // Le serveur gère le retour à 0 du pitch quand le croco sort de l'eau
            float wildTarget = !crocodile.isTame() ? Mth.clamp(crocodile.getTargetPitch(), -40f, 40f) : 0f;
            // Lerp fixe par frame, sans dépendre de partialTicks comme exposant (évite le saut à chaque tick)
            smoothedRiderPitch = Mth.lerp(0.18f, smoothedRiderPitch, wildTarget);
            pitchTarget = smoothedRiderPitch;
        }

        CrocodileSkin skin = SkinRegistry.CrocodileSkins.get(crocodile.getVariant());
        this.model = skin.getMode() == CrocodileSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(CrocodileModel.LAYER_LOCATION))
                : getOrBakeModel(CrocodileModel.LAYER_LOCATION);
        this.model.externalRiderPitch = pitchTarget;
        this.model.externalBankRoll = crocodile.getBankRoll(partialTicks);

        super.render(crocodile, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!crocodile.isTame() && crocodile.getSacrificesUnity() > 0 && !crocodile.isVehicle()) {
            OWRendererUtils.displayImageAboveEntity(CROCODILE_TAMING, 0, 15, 23, 21, 256, 2, -1.85f, -0.1f, 0, crocodile, poseStack, bufferSource, packedLight, true);

            int step = getSacrificesSteps(crocodile);
            OWRendererUtils.displayImageAboveEntity(CROCODILE_TAMING, 23 + ((step - 1) * 21), 15, 21, 19, 256, 1.85f, 0.05f - 1.85f, -0.1f, 0.01f, crocodile, poseStack, bufferSource, packedLight, true);
        }
    }

    private int getSacrificesSteps(CrocodileEntity crocodile) {
        float sacrificesUnity = crocodile.getSacrificesUnity();
        if (sacrificesUnity >= 100) return 11;
        if (sacrificesUnity >= 90)  return 10;
        if (sacrificesUnity >= 80)  return 9;
        if (sacrificesUnity >= 70)  return 8;
        if (sacrificesUnity >= 60)  return 7;
        if (sacrificesUnity >= 50)  return 6;
        if (sacrificesUnity >= 40)  return 5;
        if (sacrificesUnity >= 30)  return 4;
        if (sacrificesUnity >= 20)  return 3;
        if (sacrificesUnity >= 10)  return 2;
        return 1;
    }

    private CrocodileModel<CrocodileEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new CrocodileModel<>(context.bakeLayer(l)));
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
