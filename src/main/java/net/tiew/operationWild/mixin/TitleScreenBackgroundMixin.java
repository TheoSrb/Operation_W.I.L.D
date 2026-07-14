package net.tiew.operationWild.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.tiew.operationWild.client.OWMenuBackground;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remplace le panorama vanilla de l'écran-titre par le wallpaper custom d'Operation W.I.L.D,
 * étiré en plein écran, avec le même fondu d'apparition que le panorama d'origine.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenBackgroundMixin {

    @Shadow private float panoramaFade;

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void ow$renderCustomBackground(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        OWMenuBackground.render(guiGraphics, this.panoramaFade);
        ci.cancel();
    }
}
