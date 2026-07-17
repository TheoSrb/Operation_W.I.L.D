package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base commune des écrans de tribu (refonte visuelle). Calquée sur {@link net.tiew.operationWild.screen.entity.OWOptionsScreen}
 * et {@code OWDailyQuestScreen} : même panneau gris opaque ({@code ow_options_screen.png}, 176×166),
 * assombrissement du monde derrière, boutons vanilla, listes déroulantes avec scrollbar 5 px.
 */
public abstract class OWTribeScreen extends Screen {

    protected static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");
    protected static final ResourceLocation OW_SPRITES =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_sprites.png");
    protected static final ResourceLocation OW_INVENTORY =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    protected static final int IMG_W = 176;
    protected static final int IMG_H = 166;

    // ── Barre d'onglets partagée (au-dessus du panneau) ──────────────────────────
    /** Onglets de navigation communs à tous les écrans de tribu. */
    protected enum Tab { DASHBOARD, PERMISSIONS, REPUTATION, SETTINGS }
    protected static final int TAB_Y = -18, TAB_W = 20, TAB_H = 18, TAB_GAP = 1, TAB_X0 = 2;

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

    // ── Onglets de navigation ────────────────────────────────────────────────────
    private boolean isChiefLocal(OWTeam t) {
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    private boolean isDeputyLocal(OWTeam t) {
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && t.isDeputy(mc.player.getUUID());
    }

    /** Onglets visibles selon le rôle, dans l'ordre : Tribu, Permissions, Réputation, Paramètres. */
    protected List<Tab> visibleTabs() {
        OWTeam t = OWClientTribeData.get();
        boolean chief = isChiefLocal(t), deputy = isDeputyLocal(t);
        List<Tab> tabs = new ArrayList<>();
        tabs.add(Tab.DASHBOARD);
        if (chief || deputy) tabs.add(Tab.PERMISSIONS);
        tabs.add(Tab.REPUTATION);
        if (chief) tabs.add(Tab.SETTINGS);
        return tabs;
    }

    private int tabX(int index) { return leftPos + TAB_X0 + index * (TAB_W + TAB_GAP); }

    /** Dessine la barre d'onglets (fond + icône) et le tooltip de l'onglet survolé. */
    protected void renderTribeTabs(GuiGraphics g, int mouseX, int mouseY, Tab active) {
        List<Tab> tabs = visibleTabs();
        int y = topPos + TAB_Y;
        int hoveredTooltipX = -1, hoveredTooltipY = -1;
        Tab hovered = null;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int x = tabX(i);
            boolean hov = mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H;
            g.blit(OW_INVENTORY, x, y, (tab == active || hov) ? 20 : 0, 206, TAB_W, TAB_H);
            renderTabIcon(g, tab, x, y);
            if (hov) { hovered = tab; hoveredTooltipX = mouseX; hoveredTooltipY = mouseY; }
        }
        if (hovered != null) {
            g.renderTooltip(this.font, Component.translatable(tabTitleKey(hovered)), hoveredTooltipX, hoveredTooltipY);
        }
    }

    private void renderTabIcon(GuiGraphics g, Tab tab, int x, int y) {
        switch (tab) {
            case DASHBOARD -> {
                // Icône = mini-bannière de la tribu (rendu partagé), centrée dans l'onglet.
                OWTeam t = OWClientTribeData.get();
                if (t != null) {
                    float s = 14f / OWBannerRenderer.H;
                    float w = OWBannerRenderer.W * s;
                    g.pose().pushPose();
                    g.pose().translate(x + (TAB_W - w) / 2f, y + 4, 0);
                    g.pose().scale(s, s, 1f);
                    OWBannerRenderer.render(g, 0, 0, t.getBannerShape(),
                            t.getTeamColor(), t.getTeamSecondaryColor(), t.getTertiaryColor(), t.isUseTertiary(),
                            t.getTeamMosaicPattern(), t.getPaintPixels());
                    g.pose().popPose();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                }
            }
            case PERMISSIONS -> g.blit(OW_SPRITES, x + 2, y + 2, 0, 24, 16, 16);
            case REPUTATION -> g.blit(OW_SPRITES, x + 2, y + 2, 0, 40, 16, 16);
            case SETTINGS -> g.blit(OW_INVENTORY, x + 2, y + 2, 176, 80, 16, 16);
        }
    }

    private String tabTitleKey(Tab tab) {
        return switch (tab) {
            case DASHBOARD -> "owteams.dashboard.title";
            case PERMISSIONS -> "owteams.permissions.title";
            case REPUTATION -> "owteams.reputation.title";
            case SETTINGS -> "owteams.settings.title";
        };
    }

    /** À appeler depuis {@code mouseClicked} : gère un clic sur la barre d'onglets. */
    protected boolean tribeTabClicked(double mx, double my, Tab active) {
        List<Tab> tabs = visibleTabs();
        int y = topPos + TAB_Y;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int x = tabX(i);
            if (mx >= x && mx < x + TAB_W && my >= y && my < y + TAB_H) {
                if (tab != active) { playTabSwitch(); switchToTab(tab); }
                return true;
            }
        }
        return false;
    }

    private void switchToTab(Tab tab) {
        Minecraft mc = Minecraft.getInstance();
        switch (tab) {
            case DASHBOARD -> mc.setScreen(new OWTribeDashboardScreen());
            case PERMISSIONS -> mc.setScreen(new OWTribePermissionsScreen());
            case REPUTATION -> mc.setScreen(new OWTribeReputationScreen());
            case SETTINGS -> mc.setScreen(new OWTribeSettingsScreen());
        }
    }

    /** Son de changement d'onglet (clic bouton vanilla). */
    protected void playTabSwitch() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
