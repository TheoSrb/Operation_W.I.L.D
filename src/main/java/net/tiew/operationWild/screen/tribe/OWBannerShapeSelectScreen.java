package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientBannerUnlocks;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.ClientCoinData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.BuyBannerShapePacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

/**
 * Étape 1 de la bannière : choix de la <b>forme</b>, avant l'éditeur de couleurs / motifs
 * ({@link OWTribeCreationScreen}). Utilisé en création comme en édition.
 *
 * <p>Posé sur le panneau commun des écrans de tribu ({@code ow_options_screen.png}) : la grille y
 * tient sur deux colonnes et défile, ce qui la rend indifférente au nombre de formes — en ajouter
 * une ne demande plus de redimensionner quoi que ce soit.</p>
 */
public class OWBannerShapeSelectScreen extends OWTribeScreen {

    /** Feuille GUI contenant l'icône cadenas (12×14 aux coords 53,167). */
    private static final ResourceLocation OW_TEAMS =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");
    /** Icône de Pièce Sauvage (16×16) affichée à côté du prix. */
    private static final ResourceLocation COIN_4 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_4.png");

    private static final int LIST_X = 8, LIST_Y = 20, LIST_W = 153, LIST_H = 106;
    private static final int SCROLLBAR_X = LIST_X + LIST_W + 2;
    private static final int COLS = 2;
    private static final int CELL_W = LIST_W / COLS, CELL_H = 60;
    private static final float PREVIEW_SCALE = 0.42f;

    private final boolean editMode;

    private OWTeamBannerShape selected = OWTeamBannerShape.CLASSIC;
    private int previewPrimary = 0xD12020;
    private int previewSecondary = 0x2050D1;
    private OWTeamMosaicPattern previewPattern = OWTeamMosaicPattern.GRADIENT_DOWN;
    private byte[] previewPixels = null;

    private Button nextBtn, cancelBtn, confirmYesBtn, confirmNoBtn, buyYesBtn, buyNoBtn;
    private boolean confirmNext = false;
    /** Forme dont l'achat attend confirmation (null = aucune confirmation ouverte). */
    private OWTeamBannerShape pendingBuy = null;

    private int scroll = 0;
    private boolean draggingThumb = false;

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

        nextBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.banner.select.next")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> confirmNext = true)
                .bounds(leftPos + IMG_W / 2 - 2, topPos + IMG_H - 24, 84, 18).build());

        cancelBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.creation.cancel")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> this.onClose())
                .bounds(leftPos + 6, topPos + IMG_H - 24, 80, 18).build());

        confirmYesBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.confirm.yes")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> Minecraft.getInstance().setScreen(new OWTribeCreationScreen(selected, editMode)))
                .bounds(0, 0, 70, 16).build());
        confirmNoBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> confirmNext = false)
                .bounds(0, 0, 70, 16).build());

        buyYesBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.banner.buy.confirm")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> confirmPurchase())
                .bounds(0, 0, 70, 16).build());
        buyNoBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> pendingBuy = null)
                .bounds(0, 0, 70, 16).build());
    }

    /** Envoie l'achat au serveur (débit + déblocage autoritatifs) et sélectionne la forme. */
    private void confirmPurchase() {
        if (pendingBuy == null) return;
        OWTeamBannerShape s = pendingBuy;
        OWNetworkHandler.sendToServer(new BuyBannerShapePacket(s.getId()));
        selected = s;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f));
        pendingBuy = null;
    }

    // ── Grille défilante ─────────────────────────────────────────────────────────

    private int rowCount() {
        return (OWTeamBannerShape.values().length + COLS - 1) / COLS;
    }

    private int contentHeight() { return rowCount() * CELL_H; }

    private int maxScroll() { return Math.max(0, contentHeight() - LIST_H); }

    private int cellX(int index) { return leftPos + LIST_X + (index % COLS) * CELL_W; }

    private int cellY(int index) { return topPos + LIST_Y + (index / COLS) * CELL_H - scroll; }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (!anyDialog() && maxScroll() > 0) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) (dy * 16)));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (anyDialog()) return super.mouseClicked(mx, my, button);

        if (button == 0 && maxScroll() > 0
                && mx >= leftPos + SCROLLBAR_X && mx < leftPos + SCROLLBAR_X + 5
                && my >= topPos + LIST_Y && my < topPos + LIST_Y + LIST_H) {
            draggingThumb = true;
            dragThumbTo(my);
            return true;
        }

        if (button == 0 && mx >= leftPos + LIST_X && mx < leftPos + LIST_X + LIST_W
                && my >= topPos + LIST_Y && my < topPos + LIST_Y + LIST_H) {
            OWTeamBannerShape[] shapes = OWTeamBannerShape.values();
            for (int i = 0; i < shapes.length; i++) {
                int cx = cellX(i), cy = cellY(i);
                if (mx >= cx && mx < cx + CELL_W && my >= cy && my < cy + CELL_H) {
                    OWTeamBannerShape s = shapes[i];
                    if (OWClientBannerUnlocks.isUnlocked(s)) {
                        selected = s;
                        playTabSwitch();
                    } else if (ClientCoinData.wildCoins >= s.getPrice()) {
                        pendingBuy = s;
                    } else {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0f));
                    }
                    return true;
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingThumb) { dragThumbTo(my); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingThumb = false;
        return super.mouseReleased(mx, my, button);
    }

    private void dragThumbTo(double my) {
        double ratio = (my - (topPos + LIST_Y)) / (double) LIST_H;
        scroll = Math.max(0, Math.min(maxScroll(), (int) Math.round(ratio * maxScroll())));
    }

    private boolean anyDialog() { return confirmNext || pendingBuy != null; }

    // ── Rendu ────────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        boolean dialog = anyDialog();
        nextBtn.visible = !dialog;
        cancelBtn.visible = !dialog;
        confirmYesBtn.visible = confirmNext;
        confirmNoBtn.visible = confirmNext;
        buyYesBtn.visible = pendingBuy != null;
        buyNoBtn.visible = pendingBuy != null;

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.banner.select.title"));

        g.enableScissor(leftPos + LIST_X, topPos + LIST_Y, leftPos + LIST_X + LIST_W, topPos + LIST_Y + LIST_H);
        OWTeamBannerShape[] shapes = OWTeamBannerShape.values();
        for (int i = 0; i < shapes.length; i++) {
            int cx = cellX(i), cy = cellY(i);
            if (cy + CELL_H < topPos + LIST_Y || cy > topPos + LIST_Y + LIST_H) continue;
            boolean hov = !dialog
                    && mouseX >= cx && mouseX < cx + CELL_W
                    && mouseY >= cy && mouseY < cy + CELL_H
                    && mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_H;
            renderCell(g, shapes[i], cx, cy, hov);
        }
        g.disableScissor();

        if (maxScroll() > 0) {
            drawScrollbar(g, leftPos + SCROLLBAR_X, topPos + LIST_Y, LIST_H,
                    scroll, maxScroll(), LIST_H, contentHeight());
        }

        String coins = String.valueOf(ClientCoinData.wildCoins);
        int coinsX = leftPos + IMG_W - 10 - this.font.width(coins) - 12;
        g.drawString(this.font, coins, coinsX, topPos + IMG_H - 38, ClientCoinData.COLOR, false);
        g.blit(COIN_4, coinsX + this.font.width(coins) + 2, topPos + IMG_H - 40, 10, 10, 0f, 0f, 16, 16, 16, 16);

        super.render(g, mouseX, mouseY, partial);

        if (confirmNext) renderNextConfirm(g, mouseX, mouseY, partial);
        if (pendingBuy != null) renderBuyConfirm(g, mouseX, mouseY, partial);
    }

    private void renderCell(GuiGraphics g, OWTeamBannerShape shape, int cx, int cy, boolean hovered) {
        boolean sel = shape == selected;
        boolean unlocked = OWClientBannerUnlocks.isUnlocked(shape);

        g.fill(cx + 2, cy + 2, cx + CELL_W - 2, cy + CELL_H - 2,
                sel ? 0x553CA03C : (hovered ? 0x40FFFFFF : 0x33000000));
        drawBorder(g, cx + 2, cy + 2, CELL_W - 4, CELL_H - 4, sel ? 0xFFC8A000 : 0xFF555555);

        int bw = (int) (OWBannerRenderer.W * PREVIEW_SCALE);
        int bx = cx + (CELL_W - bw) / 2, by = cy + 6;
        g.pose().pushPose();
        g.pose().translate(bx, by, 0);
        g.pose().scale(PREVIEW_SCALE, PREVIEW_SCALE, 1f);
        OWBannerRenderer.render(g, 0, 0, shape,
                previewPrimary, previewSecondary, previewPattern, previewPixels);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (!unlocked) {
            g.fill(cx + 2, cy + 2, cx + CELL_W - 2, cy + CELL_H - 2, 0xB0606060);
            int lockX = cx + CELL_W / 2 - 6, lockY = cy + CELL_H / 2 - 14;
            g.blit(OW_TEAMS, lockX, lockY, 53, 167, 12, 14);

            boolean afford = ClientCoinData.wildCoins >= shape.getPrice();
            String price = String.valueOf(shape.getPrice());
            int pw = this.font.width(price);
            int gx = cx + CELL_W / 2 - (pw + 2 + 10) / 2, gy = lockY + 18;
            g.drawString(this.font, price, gx, gy, afford ? ClientCoinData.COLOR : 0xC85454, false);
            g.blit(COIN_4, gx + pw + 2, gy - 2, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
    }

    private void renderNextConfirm(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        String title = "⚠  " + Component.translatable("owteams.banner.select.warning").getString() + "  ⚠";
        Component sub = Component.translatable("owteams.banner.select.warning2");
        int contentW = Math.max(this.font.width(title), this.font.width(sub));
        int ow = Math.min(this.width - 20, Math.max(220, contentW + 28));
        int oh = 78, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
        drawBorder(g, ox, oy, ow, oh, 0xFFD9A62E);
        g.drawCenteredString(this.font, title, cx, oy + 13, 0xFFFFFFFF);
        g.drawCenteredString(this.font, sub, cx, oy + 27, 0xFFE6E6E6);
        confirmYesBtn.setPosition(cx - 74, oy + oh - 22);
        confirmNoBtn.setPosition(cx + 4, oy + oh - 22);
        confirmYesBtn.render(g, mouseX, mouseY, partial);
        confirmNoBtn.render(g, mouseX, mouseY, partial);
    }

    private void renderBuyConfirm(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        Component q = Component.translatable("owteams.banner.buy.question", pendingBuy.getLabel());
        String price = String.valueOf(pendingBuy.getPrice());
        int priceGroupW = this.font.width(price) + 3 + 12;

        final float previewScale = 0.55f;
        int bw = (int) (OWBannerRenderer.W * previewScale);
        int bh = (int) (OWBannerRenderer.H * previewScale);

        int ow = Math.min(this.width - 20, Math.max(210, this.font.width(q) + 24));
        int oh = 46 + bh + 34, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
        drawBorder(g, ox, oy, ow, oh, 0xFFC8A000);
        g.drawCenteredString(this.font, q, cx, oy + 10, 0xFFDD77);

        int bx = cx - bw / 2, by = oy + 26;
        g.pose().pushPose();
        g.pose().translate(bx, by, 0);
        g.pose().scale(previewScale, previewScale, 1f);
        OWBannerRenderer.render(g, 0, 0, pendingBuy,
                previewPrimary, previewSecondary, previewPattern, previewPixels);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int gx = cx - priceGroupW / 2, gy = by + bh + 6;
        g.drawString(this.font, price, gx, gy, ClientCoinData.COLOR, false);
        g.blit(COIN_4, gx + this.font.width(price) + 3, gy - 2, 12, 12, 0f, 0f, 16, 16, 16, 16);
        buyYesBtn.setPosition(cx - 74, oy + oh - 20);
        buyNoBtn.setPosition(cx + 4, oy + oh - 20);
        buyYesBtn.render(g, mouseX, mouseY, partial);
        buyNoBtn.render(g, mouseX, mouseY, partial);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
