package net.tiew.operationWild.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.tiew.operationWild.client.OWMenuBackground;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remplace le panorama vanilla par le wallpaper custom d'Operation W.I.L.D dans tous les
 * sous-écrans de menu (Options, sélection de monde, etc.) qui n'écrasent pas {@code renderPanorama}.
 * L'écran-titre a sa propre surcharge, gérée par {@link TitleScreenBackgroundMixin}.
 */
@Mixin(Screen.class)
public abstract class ScreenBackgroundMixin {

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void ow$renderCustomBackground(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        OWMenuBackground.render(guiGraphics, 1.0F);
        ci.cancel();
    }
}
