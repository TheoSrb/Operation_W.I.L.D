package net.tiew.operationWild.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class MimicryArmorLayerMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void hideArmorDuringMimicry(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity,
                                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                                        float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && ChameleonEntity.getMimicking(player) != null) ci.cancel();
    }
}
