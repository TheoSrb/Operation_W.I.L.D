package net.tiew.operationWild.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.tiew.operationWild.client.platform.OWPlatformClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererPlatformPickMixin {

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void ow$pickPlatformBlocks(float partialTicks, CallbackInfo ci) {
        OWPlatformClient.afterPick(partialTicks);
    }
}
