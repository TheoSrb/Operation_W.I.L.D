package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.client.render.misc.ChameleonCamouflage;

public class ChameleonCamouflageLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public ChameleonCamouflageLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ChameleonEntity source = ChameleonCamouflage.mimicSource(entity);
        if (source == null) return;

        Entity anchor = source.getCarrier() != null ? source.getCarrier() : source;
        M model = this.getParentModel();
        ChameleonCamouflage.render(anchor, partialTicks, ChameleonCamouflage.envelope(source),
                packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0f), bufferSource,
                target -> model.renderToBuffer(poseStack, target.consumer(), target.light(), target.overlay(), target.color()));
    }
}
