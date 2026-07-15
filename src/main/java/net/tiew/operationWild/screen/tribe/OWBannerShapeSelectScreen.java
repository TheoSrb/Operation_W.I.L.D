package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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

    /** Feuille GUI contenant l'icône cadenas (12×14 aux coords 53,167). */
    private static final ResourceLocation OW_TEAMS =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");
    /** Icône de Pièce Sauvage (16×16) affichée à côté du prix. */
    private static final ResourceLocation COIN_4 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_4.png");

    private final boolean editMode;
    private int leftPos, topPos;

    private OWTeamBannerShape selected = OWTeamBannerShape.CLASSIC;
    // Couleurs/motif de prévisualisation (repris de la tribu existante en édition).
    private int previewPrimary = 0xD12020;
    private int previewSecondary = 0x2050D1;
    private OWTeamMosaicPattern previewPattern = OWTeamMosaicPattern.GRADIENT_DOWN;
    private byte[] previewPixels = null;

    private Button nextBtn, cancelBtn, confirmYesBtn, confirmNoBtn, buyYesBtn, buyNoBtn;
    private boolean confirmNext = false;
    /** Forme dont l'achat attend confirmation (null = aucune confirmation ouverte). */
    private OWTeamBannerShape pendingBuy = null;

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

        // Dialogue de confirmation d'achat d'une forme (masqué tant que pendingBuy == null).
        buyYesBtn = Button.builder(
                        Component.translatable("owteams.banner.buy.confirm")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> confirmPurchase())
                .bounds(0, 0, 70, 16).build();
        this.addRenderableWidget(buyYesBtn);
        buyNoBtn = Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> pendingBuy = null)
                .bounds(0, 0, 70, 16).build();
        this.addRenderableWidget(buyNoBtn);
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
        if (confirmNext || pendingBuy != null) return super.mouseClicked(mx, my, button);
        if (button == 0) {
            OWTeamBannerShape[] shapes = OWTeamBannerShape.values();
            for (int i = 0; i < shapes.length; i++) {
                int cx = cellX(i), cy = cellY(i);
                if (mx >= cx && mx < cx + CELL_W && my >= cy && my < cy + CELL_H) {
                    OWTeamBannerShape s = shapes[i];
                    if (OWClientBannerUnlocks.isUnlocked(s)) {
                        selected = s;
                    } else if (ClientCoinData.wildCoins >= s.getPrice()) {
                        // Verrouillée & abordable : demander confirmation avant de dépenser.
                        pendingBuy = s;
                    } else {
                        // Pièces insuffisantes.
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0f));
                    }
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
        boolean anyDialog = confirmNext || pendingBuy != null;
        nextBtn.visible = !anyDialog;
        cancelBtn.visible = !anyDialog;
        confirmYesBtn.visible = confirmNext;
        confirmNoBtn.visible = confirmNext;
        buyYesBtn.visible = pendingBuy != null;
        buyNoBtn.visible = pendingBuy != null;

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

            // ── Forme verrouillée : voile gris (désaturation/opacité) + cadenas + prix + pièce ──
            if (!OWClientBannerUnlocks.isUnlocked(shapes[i])) {
                g.fill(cx + 2, cy + 2, cx + CELL_W - 2, cy + CELL_H - 2, 0xB0606060);
                int lockX = cx + CELL_W / 2 - 6, lockY = cy + CELL_H / 2 - 12;
                g.blit(OW_TEAMS, lockX, lockY, 53, 167, 12, 14);
                boolean afford = ClientCoinData.wildCoins >= shapes[i].getPrice();
                int priceCol = afford ? ClientCoinData.COLOR : 0xC85454;
                // Prix + icône de pièce (10×10) juste à droite du nombre, groupe centré.
                String priceStr = String.valueOf(shapes[i].getPrice());
                int pw = this.font.width(priceStr);
                int groupW = pw + 2 + 10;
                int gx = cx + CELL_W / 2 - groupW / 2;
                int gy = lockY + 18;
                g.drawString(this.font, priceStr, gx, gy, priceCol, false);
                g.blit(COIN_4, gx + pw + 2, gy - 2, 10, 10, 0f, 0f, 16, 16, 16, 16);
            }
        }

        // Boutons (au-dessus du panneau)
        super.render(g, mouseX, mouseY, partial);

        // Dialogue de confirmation « choix définitif »
        if (confirmNext) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            // Titre encadré de deux icônes d'avertissement, texte en blanc.
            String title = "⚠  " + Component.translatable("owteams.banner.select.warning").getString() + "  ⚠";
            Component sub = Component.translatable("owteams.banner.select.warning2");
            int contentW = Math.max(this.font.width(title), this.font.width(sub));
            int ow = Math.min(this.width - 20, Math.max(220, contentW + 28));
            int oh = 78, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
            drawBorder(g, ox, oy, ow, oh, 0xFFD9A62E);   // bordure ambre d'avertissement
            g.fill(ox, oy, ox + ow, oy + 1, 0xFFD9A62E);
            g.drawCenteredString(this.font, title, cx, oy + 13, 0xFFFFFFFF);
            g.drawCenteredString(this.font, sub, cx, oy + 27, 0xFFE6E6E6);
            confirmYesBtn.setPosition(cx - 74, oy + oh - 22);
            confirmNoBtn.setPosition(cx + 4, oy + oh - 22);
            confirmYesBtn.render(g, mouseX, mouseY, partial);
            confirmNoBtn.render(g, mouseX, mouseY, partial);
        }

        // Dialogue de confirmation d'achat (« dépenser X pièces contre cette bannière »)
        if (pendingBuy != null) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            Component q = Component.translatable("owteams.banner.buy.question", pendingBuy.getLabel());
            String priceStr = String.valueOf(pendingBuy.getPrice());
            int priceGroupW = this.font.width(priceStr) + 3 + 12;

            final float previewScale = 0.55f;
            int bw = (int) (OWBannerRenderer.W * previewScale);
            int bh = (int) (OWBannerRenderer.H * previewScale);

            int ow = Math.min(this.width - 20, Math.max(210, this.font.width(q) + 24));
            int oh = 46 + bh + 34, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
            drawBorder(g, ox, oy, ow, oh, 0xFFC8A000);
            g.drawCenteredString(this.font, q, cx, oy + 10, 0xFFDD77);

            // Aperçu de la bannière (mêmes couleurs/motif que la grille) pour bien la voir avant l'achat.
            int bx = cx - bw / 2, by = oy + 26;
            g.pose().pushPose();
            g.pose().translate(bx, by, 0);
            g.pose().scale(previewScale, previewScale, 1f);
            OWBannerRenderer.render(g, 0, 0, pendingBuy,
                    previewPrimary, previewSecondary, previewPattern, previewPixels);
            g.pose().popPose();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            // Prix + icône de pièce (12×12), groupe centré sous l'aperçu.
            int gx = cx - priceGroupW / 2, gy = by + bh + 6;
            g.drawString(this.font, priceStr, gx, gy, ClientCoinData.COLOR, false);
            g.blit(COIN_4, gx + this.font.width(priceStr) + 3, gy - 2, 12, 12, 0f, 0f, 16, 16, 16, 16);
            buyYesBtn.setPosition(cx - 74, oy + oh - 20);
            buyNoBtn.setPosition(cx + 4, oy + oh - 20);
            buyYesBtn.render(g, mouseX, mouseY, partial);
            buyNoBtn.render(g, mouseX, mouseY, partial);
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
