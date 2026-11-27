package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.client.model.TarantulaModel;

public class TarantulaLayer<T extends TarantulaEntity> extends RenderLayer<T, TarantulaModel<T>> {

    public static final ResourceLocation TAMED_EYES = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tarantula/tarantula_tamed_eyes.png");

    public TarantulaLayer(RenderLayerParent<T, TarantulaModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T tarantula,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!tarantula.isTame() || tarantula.isInvisible()) return;
        this.getParentModel().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TAMED_EYES)), packedLight,
                LivingEntityRenderer.getOverlayCoords(tarantula, 0f), -1);
    }
}
