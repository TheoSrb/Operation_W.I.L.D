package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.misc.PrimitiveSpearProjectileModel;
import net.tiew.operationWild.entity.custom.misc.PrimitiveSpearProjectileEntity;

public class PrimitiveSpearProjectileRenderer extends EntityRenderer<PrimitiveSpearProjectileEntity> {
    private PrimitiveSpearProjectileModel model;

    public PrimitiveSpearProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new PrimitiveSpearProjectileModel(pContext.bakeLayer(PrimitiveSpearProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(PrimitiveSpearProjectileEntity pEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, pEntity.xRotO, pEntity.getXRot()) + 90.0F));
        VertexConsumer $$6 = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(this.getTextureLocation(pEntity)), false, false);
        this.model.renderToBuffer(poseStack, $$6, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(pEntity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimitiveSpearProjectileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/primitive_spear/primitive_spear.png");
    }
}
