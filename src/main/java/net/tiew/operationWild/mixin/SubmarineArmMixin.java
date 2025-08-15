package net.tiew.operationWild.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.vehicle.Submarine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class SubmarineArmMixin {

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void cancelRightArm(AbstractClientPlayer abstractClientPlayer, float v, float v1, InteractionHand hand, float v2, ItemStack itemStack,
                                float v3, PoseStack poseStack, MultiBufferSource bufferSource, int i, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof OWEntity && hand == InteractionHand.MAIN_HAND) {
            ci.cancel();
        }
    }
}