package net.tiew.operationWild.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import net.tiew.operationWild.sound.OWSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.Nullable;

/**
 * Remplace la musique de menu vanilla (Musics.MENU) par le thème custom d'Operation W.I.L.D,
 * joué en boucle continue tant qu'aucun monde n'est chargé (contexte menu).
 */
@Mixin(Minecraft.class)
public abstract class MenuMusicMixin {

    // Boucle continue (0/0 = relance immédiate en fin de piste), remplace toute musique en cours.
    private static final Music OW_MENU_MUSIC = new Music(OWSounds.MENU_MUSIC, 0, 0, true);

    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public Screen screen;

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void ow$menuMusic(CallbackInfoReturnable<Music> cir) {
        // Contexte menu = pas de joueur en jeu ; on respecte une musique propre à l'écran si elle existe.
        if (this.player == null && (this.screen == null || this.screen.getBackgroundMusic() == null)) {
            cir.setReturnValue(OW_MENU_MUSIC);
        }
    }
}
