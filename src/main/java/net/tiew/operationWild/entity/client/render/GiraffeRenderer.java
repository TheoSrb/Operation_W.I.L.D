package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;
import net.tiew.operationWild.entity.client.layer.GiraffeLayer;
import net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer;
import net.tiew.operationWild.entity.client.model.GiraffeModel;
import net.tiew.operationWild.entity.variants.GiraffeVariant;

import java.util.EnumMap;
import java.util.Map;

public class GiraffeRenderer extends OWEntityRenderer<GiraffeEntity, GiraffeModel<GiraffeEntity>> {

    private static final Map<GiraffeVariant, ResourceLocation> TEXTURES = new EnumMap<>(GiraffeVariant.class);

    static {
        TEXTURES.put(GiraffeVariant.DEFAULT, tex("giraffe_default.png"));
        TEXTURES.put(GiraffeVariant.RETICULATED, tex("giraffe_reticulated.png"));
        TEXTURES.put(GiraffeVariant.GOLDEN, tex("giraffe_golden.png"));
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/giraffe/" + path);
    }

    public GiraffeRenderer(EntityRendererProvider.Context context) {
        super(context, new GiraffeModel<>(context.bakeLayer(GiraffeModel.LAYER_LOCATION)), 1.1f);
        this.addLayer(new GiraffeLayer(this));
        this.addLayer(new OWTribeFlagLayer<>(this));
        this.addLayer(new net.tiew.operationWild.entity.client.layer.OWPlatformLayer<>(this));
    }

    @Override
    public void render(GiraffeEntity giraffe, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        this.model.externalBankRoll = giraffe.getBankRoll(partialTicks);
        super.render(giraffe, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GiraffeEntity giraffe) {
        return TEXTURES.getOrDefault(giraffe.getVariant(), TEXTURES.get(GiraffeVariant.DEFAULT));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 6;
    }

    @Override
    public double infosUpOffset() {
        return 2.2;
    }
}
