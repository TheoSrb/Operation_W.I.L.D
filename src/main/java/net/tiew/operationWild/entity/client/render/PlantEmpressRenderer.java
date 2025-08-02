package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.PlantEmpressLayer;
import net.tiew.operationWild.entity.client.model.PlantEmpressModel;
import net.tiew.operationWild.entity.custom.living.boss.PlantEmpressEntity;
import net.tiew.operationWild.entity.variants.PlantEmpressVariant;

import java.util.Map;public class PlantEmpressRenderer extends MobRenderer<PlantEmpressEntity, PlantEmpressModel<PlantEmpressEntity>> {
    private static final Map<PlantEmpressVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(PlantEmpressVariant.class), map -> {
        map.put(PlantEmpressVariant.PLANT_EMPRESS, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/plant_empress/plant_empress.png"));
    });

    public PlantEmpressRenderer(EntityRendererProvider.Context context) {
        super(context, new PlantEmpressModel<>(context.bakeLayer(PlantEmpressModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new PlantEmpressLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PlantEmpressEntity plant_empress) {
        return LOCATION_BY_VARIANT.get(plant_empress.getVariant());
    }

    @Override
    public void render(PlantEmpressEntity plant_empress, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = plant_empress.getScale() / 1.4f;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        super.render(plant_empress, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    protected void renderNameTag(PlantEmpressEntity plant_empress, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(plant_empress, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
