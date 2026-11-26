package net.tiew.operationWild.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.tiew.operationWild.entity.client.render.misc.ChameleonCamouflage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MimicryShadowMixin {

    @Inject(method = "renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V",
            at = @At("HEAD"), cancellable = true)
    private static void hideCamouflagedShadow(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight,
                                              float partialTicks, LevelReader level, float size, CallbackInfo ci) {
        if (ChameleonCamouflage.hidesExtras(entity)) ci.cancel();
    }
}
