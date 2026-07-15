package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

/**
 * Étape 1 de la bannière : choix de la <b>forme</b> parmi les 6 disponibles. Affiché avant l'éditeur
 * de couleurs / motifs ({@link OWTribeCreationScreen}). Utilisé en création (nouvelle tribu) et en
 * édition (le chef modifie l'apparence de sa tribu).
 */
public class OWBannerShapeSelectScreen extends Screen {

    private static final int PANEL_W = 250;
    private static final int PANEL_H = 250;   // 3 rangées (7 formes)
    private static final int COLS = 3;
    private static final float PREVIEW_SCALE = 0.5f;
    private static final int CELL_W = 74;
    private static final int CELL_H = 64;

    private final boolean editMode;
    private int leftPos, topPos;

    private OWTeamBannerShape selected = OWTeamBannerShape.CLASSIC;
    // Couleurs/motif de prévisualisation (repris de la tribu existante en édition).
    private int previewPrimary = 0xD12020;
    private int previewSecondary = 0x2050D1;
    private OWTeamMosaicPattern previewPattern = OWTeamMosaicPattern.GRADIENT_DOWN;
    private boolean[] previewPixels = null;

    private Button nextBtn, cancelBtn, confirmYesBtn, confirmNoBtn;
    private boolean confirmNext = false;

    public OWBannerShapeSelectScreen() {
        this(false);
    }

    public OWBannerShapeSelectScreen(boolean editMode) {
        super(Component.translatable("owteams.banner.select.title"));
        this.editMode = editMode;
        if (editMode) {
            OWTeam t = OWClientTribeData.get();
            if (t != null) {
                selected = t.getBannerShape();
                previewPrimary = t.getTeamColor();
                previewSecondary = t.getTeamSecondaryColor();
                previewPattern = t.getTeamMosaicPattern();
                previewPixels = t.getPaintPixels();
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;

        nextBtn = Button.builder(
                        Component.translatable("owteams.banner.select.next")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> confirmNext = true)
                .bounds(leftPos + PANEL_W - 108, topPos + PANEL_H - 22, 100, 16).build();
        this.addRenderableWidget(nextBtn);

        cancelBtn = Button.builder(
                        Component.translatable("owteams.creation.cancel")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> this.onClose())
                .bounds(leftPos + 8, topPos + PANEL_H - 22, 60, 16).build();
        this.addRenderableWidget(cancelBtn);

        // Dialogue de confirmation « choix définitif » (masqué tant que confirmNext == false).
        confirmYesBtn = Button.builder(
                        Component.translatable("owteams.confirm.yes")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> Minecraft.getInstance().setScreen(new OWTribeCreationScreen(selected, editMode)))
                .bounds(0, 0, 70, 16).build();
        this.addRenderableWidget(confirmYesBtn);
        confirmNoBtn = Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> confirmNext = false)
                .bounds(0, 0, 70, 16).build();
        this.addRenderableWidget(confirmNoBtn);
    }

    private int cellX(int index) {
        int col = index % COLS;
        int gridW = COLS * CELL_W;
        int startX = leftPos + (PANEL_W - gridW) / 2;
        return startX + col * CELL_W;
    }

    private int cellY(int index) {
        int row = index / COLS;
        return topPos + 28 + row * CELL_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Dialogue de confirmation ouvert : ne pas intercepter le clic avec la sélection de cellule,
        // laisser les boutons Oui/Non (au centre) le recevoir.
        if (confirmNext) return super.mouseClicked(mx, my, button);
        if (button == 0) {
            OWTeamBannerShape[] shapes = OWTeamBannerShape.values();
            for (int i = 0; i < shapes.length; i++) {
                int cx = cellX(i), cy = cellY(i);
                if (mx >= cx && mx < cx + CELL_W && my >= cy && my < cy + CELL_H) {
                    selected = shapes[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Voile géré manuellement dans render() (assombrit sans flou).
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        nextBtn.visible = !confirmNext;
        cancelBtn.visible = !confirmNext;
        confirmYesBtn.visible = confirmNext;
        confirmNoBtn.visible = confirmNext;

        // Voile un peu plus clair (cohérent avec les autres écrans de tribu).
        g.fill(0, 0, this.width, this.height, 0xA0000000);

        // Panneau clair (gris vanilla) + bordure sombre.
        g.fill(leftPos, topPos, leftPos + PANEL_W, topPos + PANEL_H, 0xFFC6C6C6);
        drawBorder(g, leftPos, topPos, PANEL_W, PANEL_H, 0xFF373737);
        g.drawCenteredString(this.font,
                Component.translatable("owteams.banner.select.title").setStyle(Style.EMPTY.withBold(true)),
                leftPos + PANEL_W / 2, topPos + 8, 0x404040);
        g.fill(leftPos + 8, topPos + 22, leftPos + PANEL_W - 8, topPos + 23, 0xFF808080);

        OWTeamBannerShape[] shapes = OWTeamBannerShape.values();
        for (int i = 0; i < shapes.length; i++) {
            int cx = cellX(i), cy = cellY(i);
            boolean sel = shapes[i] == selected;
            boolean hov = mouseX >= cx && mouseX < cx + CELL_W && mouseY >= cy && mouseY < cy + CELL_H;
            g.fill(cx + 2, cy + 2, cx + CELL_W - 2, cy + CELL_H - 2,
                    sel ? 0x553CA03C : (hov ? 0x40FFFFFF : 0x33000000));
            drawBorder(g, cx + 2, cy + 2, CELL_W - 4, CELL_H - 4,
                    sel ? 0xFFC8A000 : 0xFF555555);

            int bw = (int) (OWBannerRenderer.W * PREVIEW_SCALE);
            int bx = cx + (CELL_W - bw) / 2, by = cy + 6;
            g.pose().pushPose();
            g.pose().translate(bx, by, 0);
            g.pose().scale(PREVIEW_SCALE, PREVIEW_SCALE, 1f);
            OWBannerRenderer.render(g, 0, 0, shapes[i],
                    previewPrimary, previewSecondary, previewPattern, previewPixels);
            g.pose().popPose();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        // Boutons (au-dessus du panneau)
        super.render(g, mouseX, mouseY, partial);

        // Dialogue de confirmation « choix définitif »
        if (confirmNext) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            Component warn = Component.translatable("owteams.banner.select.warning");
            int ow = Math.min(this.width - 20, Math.max(210, this.font.width(warn) + 24));
            int oh = 56, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
            drawBorder(g, ox, oy, ow, oh, 0xFF888888);
            g.drawCenteredString(this.font, warn, cx, oy + 12, 0xFFCC66);
            confirmYesBtn.setPosition(cx - 74, oy + oh - 20);
            confirmNoBtn.setPosition(cx + 4, oy + oh - 20);
            confirmYesBtn.render(g, mouseX, mouseY, partial);
            confirmNoBtn.render(g, mouseX, mouseY, partial);
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
