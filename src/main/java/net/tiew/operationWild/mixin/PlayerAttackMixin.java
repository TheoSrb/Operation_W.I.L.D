package net.tiew.operationWild.mixin;

import net.minecraft.client.Minecraft;
import net.tiew.operationWild.item.OWItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class PlayerAttackMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.player != null && (minecraft.player.getMainHandItem().is(OWItems.SEABUG.get()) || minecraft.player.getOffhandItem().is(OWItems.SEABUG.get()))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean leftClick, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.player != null && (minecraft.player.getMainHandItem().is(OWItems.SEABUG.get()) || minecraft.player.getOffhandItem().is(OWItems.SEABUG.get()))) {
            ci.cancel();
        }
    }
}