package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

/**
 * Base commune des écrans de tribu (refonte visuelle). Calquée sur {@link net.tiew.operationWild.screen.entity.OWOptionsScreen}
 * et {@code OWDailyQuestScreen} : même panneau gris opaque ({@code ow_options_screen.png}, 176×166),
 * assombrissement du monde derrière, boutons vanilla, listes déroulantes avec scrollbar 5 px.
 */
public abstract class OWTribeScreen extends Screen {

    protected static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");

    protected static final int IMG_W = 176;
    protected static final int IMG_H = 166;

    protected int leftPos, topPos;

    protected OWTribeScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;
    }

    /**
     * Assombrit le monde derrière (voile dégradé, comme les écrans de l'inventaire d'entité) SANS
     * flou, puis pose le panneau gris opaque. On utilise {@code renderTransparentBackground} et non
     * {@code renderBackground} (qui déclencherait le flou de menu de 1.21).
     */
    protected void drawPanel(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Voile plus clair que renderTransparentBackground (qui était un chouilla trop sombre).
        g.fill(0, 0, this.width, this.height, 0xA0000000);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, IMG_W, IMG_H);
    }

    /** Fond géré manuellement par {@link #drawPanel} ; no-op pour éviter le flou de menu de 1.21. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // no-op volontaire
    }

    /** Titre centré dans l'en-tête, en gris foncé (lisible sur le panneau gris clair). */
    protected void drawHeader(GuiGraphics g, Component title) {
        g.drawString(this.font, title, leftPos + IMG_W / 2 - this.font.width(title) / 2, topPos + 7, 0x404040, false);
    }

    /** Scrollbar 5 px à droite d'une liste (fond sombre + pouce gris), style {@code OWScrollPanel}. */
    protected void drawScrollbar(GuiGraphics g, int x, int y, int h, int scroll, int maxScroll, int visibleH, int totalH) {
        g.fill(x, y, x + 5, y + h, 0xFF111111);
        int thumbH = Math.max(16, h * visibleH / Math.max(1, totalH));
        int thumbY = maxScroll > 0 ? y + (h - thumbH) * scroll / maxScroll : y;
        g.fill(x + 1, thumbY, x + 4, thumbY + thumbH, 0xFF888888);
    }

    /** Petit bouton « sprite vanilla » dessiné à la main (pour les boutons de ligne d'une liste). */
    protected void drawSpriteButton(GuiGraphics g, int x, int y, int w, int h,
                                    Component label, boolean hovered, int textColor) {
        g.blitSprite(hovered
                        ? ResourceLocation.withDefaultNamespace("widget/button_highlighted")
                        : ResourceLocation.withDefaultNamespace("widget/button"),
                x, y, w, h);
        int tx = x + w / 2 - this.font.width(label) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(this.font, label, tx, ty, textColor, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
