package net.tiew.operationWild.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

/**
 * Rendu du wallpaper custom d'Operation W.I.L.D en fond des écrans de menu
 * (écran-titre et sous-menus), en remplacement du panorama vanilla.
 */
public final class OWMenuBackground {

    public static final ResourceLocation WALLPAPER =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/title/ow_main_wallpaper.png");

    // Dimensions réelles du PNG source (2560x1440) — la texture entière est étirée à l'écran.
    private static final int TEX_WIDTH = 2560;
    private static final int TEX_HEIGHT = 1440;

    private OWMenuBackground() {
    }

    /**
     * Dessine le wallpaper plein écran. {@code alpha} gère le fondu (1.0 = opaque).
     * Reproduit le nettoyage d'état du {@code PanoramaRenderer} vanilla
     * (désactivation du depth test pour éviter les artefacts de composition des couches d'écran).
     */
    public static void render(GuiGraphics guiGraphics, float alpha) {
        RenderSystem.enableBlend();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(WALLPAPER, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0.0F, 0.0F, TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}
