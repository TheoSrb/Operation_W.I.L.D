package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.flying.CondorEntity;
import net.tiew.operationWild.entity.client.layer.CondorLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.model.CondorModel;
import net.tiew.operationWild.entity.variants.CondorVariant;

import java.util.EnumMap;
import java.util.Map;

public class CondorRenderer extends OWEntityRenderer<CondorEntity, CondorModel<CondorEntity>> {

    private static final Map<CondorVariant, ResourceLocation> TEXTURES = new EnumMap<>(CondorVariant.class);

    static {
        TEXTURES.put(CondorVariant.DEFAULT, tex("condor_default.png"));
        TEXTURES.put(CondorVariant.GREY, tex("condor_grey.png"));
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/condor/" + path);
    }

    public CondorRenderer(EntityRendererProvider.Context context) {
        super(context, new CondorModel<>(context.bakeLayer(CondorModel.LAYER_LOCATION)), 1.0f);
        this.addLayer(new CondorLayer(this));
        this.addLayer(new OWTribeFlagLayer<>(this));
    }

    @Override
    public void render(CondorEntity condor, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.model.externalRiderPitch = condor.getRidePitch(partialTicks);
        this.model.externalBankRoll = condor.getBankRoll(partialTicks);
        super.render(condor, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CondorEntity condor) {
        return TEXTURES.getOrDefault(condor.getVariant(), TEXTURES.get(CondorVariant.DEFAULT));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0.4;
    }
}
