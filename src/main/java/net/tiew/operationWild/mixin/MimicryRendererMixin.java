package net.tiew.operationWild.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.client.layer.ChameleonCamouflageLayer;
import net.tiew.operationWild.entity.client.render.misc.ChameleonCamouflage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MimicryRendererMixin {

    @Inject(method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
            at = @At("HEAD"), cancellable = true)
    private void hideCamouflagedBody(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing,
                                     CallbackInfoReturnable<RenderType> cir) {
        if (ChameleonCamouflage.hidesBody(entity)) cir.setReturnValue(null);
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private void camouflageLayers(RenderLayer<?, ?> layer, PoseStack poseStack, MultiBufferSource buffer, int light,
                                  Entity entity, float limbSwing, float limbSwingAmount, float partialTick,
                                  float ageInTicks, float netHeadYaw, float headPitch, Operation<Void> original) {
        if (layer instanceof ChameleonCamouflageLayer || !ChameleonCamouflage.hidesExtras(entity)) {
            original.call(layer, poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
            return;
        }
        if (!(layer instanceof HumanoidArmorLayer<?, ?, ?>) || !(entity instanceof LivingEntity living)) return;

        if (!ChameleonCamouflage.hidesBody(entity)) {
            original.call(layer, poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        }
        ChameleonCamouflage.renderProjectedLayer(living, partialTick, buffer, projected ->
                original.call(layer, poseStack, projected, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch));
    }
}
