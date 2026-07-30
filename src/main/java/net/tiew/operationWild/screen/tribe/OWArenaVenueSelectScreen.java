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
import net.tiew.operationWild.client.OWClientArenaVenueUnlocks;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWArenaVenue;
import net.tiew.operationWild.networking.ClientCoinData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.BuyArenaVenuePacket;
import net.tiew.operationWild.networking.packets.to_server.ChallengeTribePacket;

import java.util.List;

/**
 * Dernière étape d'une déclaration de combat : le choix du <b>décor</b>, une fois le terrain arrêté.
 *
 * <p>La liste ne montre que les décors du terrain retenu. Le décor par défaut est gratuit — une
 * tribu qui n'a rien acheté peut toujours se battre — les autres se paient en Pièces Sauvages, une
 * fois pour toutes.</p>
 */
public class OWArenaVenueSelectScreen extends OWTribeScreen {

    private static final ResourceLocation OW_TEAMS =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");
    private static final ResourceLocation COIN_4 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_4.png");

    private static final int LIST_X = 8, LIST_Y = 27, LIST_W = 153, LIST_H = 107;
    private static final int CARD_H = 40, CARD_GAP = 3;
    private static final int SCROLLBAR_X = LIST_X + LIST_W + 2;
    private static final int BUTTON_W = 52, BUTTON_H = 18;

    /**
     * Bande découpée dans la vignette, au ratio de la carte.
     *
     * <p>Une capture est bien plus haute, proportionnellement, qu'une carte presque quatre fois plus
     * large que haute : l'étirer écraserait le décor. On y prélève donc une bande centrale au bon
     * rapport.</p>
     *
     * <p>Le découpage est exprimé dans un carré virtuel de {@link #UV_UNIT} et non en pixels : le
     * {@code blit} ne travaillant qu'en rapports, il reste juste quelle que soit la définition des
     * images livrées. Seul leur <b>rapport</b> compte, et il est déclaré une fois pour toutes.</p>
     */
    private static final int UV_UNIT = 1000;
    private static final int BAND_H =
            Math.round(UV_UNIT * OWArenaVenue.TEXTURE_ASPECT / ((float) LIST_W / CARD_H));
    private static final int BAND_V = (UV_UNIT - BAND_H) / 2;

    private final int targetTeamId;
    private final String targetName;
    private final OWArena.Terrain terrain;
    private final List<OWArenaVenue> venues;

    private OWArenaVenue selected;
    private OWArenaVenue pendingBuy = null;

    private int scroll = 0;
    private boolean draggingThumb = false;

    private Button confirmBtn, cancelBtn, buyYesBtn, buyNoBtn;

    public OWArenaVenueSelectScreen(int targetTeamId, String targetName, OWArena.Terrain terrain) {
        super(Component.translatable("owteams.arena.venue.title"));
        this.targetTeamId = targetTeamId;
        this.targetName = targetName;
        this.terrain = terrain;
        this.venues = OWArenaVenue.forTerrain(terrain);
        this.selected = OWArenaVenue.defaultFor(terrain);
    }

    @Override
    protected void init() {
        super.init();

        confirmBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.arena.venue.declare")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        b -> sendChallenge())
                .bounds(leftPos + IMG_W - 8 - BUTTON_W, topPos + IMG_H - 24, BUTTON_W, BUTTON_H).build());

        cancelBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.creation.cancel")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> Minecraft.getInstance().setScreen(new OWTribeArenaScreen()))
                .bounds(leftPos + 8, topPos + IMG_H - 24, BUTTON_W, BUTTON_H).build());

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

    private void sendChallenge() {
        if (!OWClientArenaVenueUnlocks.isUnlocked(selected)) return;
        OWNetworkHandler.sendToServer(
                new ChallengeTribePacket(targetTeamId, terrain.ordinal(), selected.getId()));
        Minecraft.getInstance().setScreen(new OWTribeArenaScreen());
    }

    private void confirmPurchase() {
        if (pendingBuy == null) return;
        OWArenaVenue v = pendingBuy;
        OWNetworkHandler.sendToServer(new BuyArenaVenuePacket(v.getId()));
        selected = v;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f));
        pendingBuy = null;
    }

    // ── Défilement ───────────────────────────────────────────────────────────────

    private int contentHeight() {
        return venues.size() * (CARD_H + CARD_GAP) - CARD_GAP;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - LIST_H);
    }

    private int cardY(int index) {
        return topPos + LIST_Y + index * (CARD_H + CARD_GAP) - scroll;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (pendingBuy == null && maxScroll() > 0) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) (dy * 14)));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (pendingBuy != null) return super.mouseClicked(mx, my, button);

        if (button == 0 && maxScroll() > 0
                && mx >= leftPos + SCROLLBAR_X && mx < leftPos + SCROLLBAR_X + 5
                && my >= topPos + LIST_Y && my < topPos + LIST_Y + LIST_H) {
            draggingThumb = true;
            dragThumbTo(my);
            return true;
        }

        if (button == 0 && mx >= leftPos + LIST_X && mx < leftPos + LIST_X + LIST_W
                && my >= topPos + LIST_Y && my < topPos + LIST_Y + LIST_H) {
            for (int i = 0; i < venues.size(); i++) {
                int cy = cardY(i);
                if (my >= cy && my < cy + CARD_H) {
                    OWArenaVenue v = venues.get(i);
                    if (OWClientArenaVenueUnlocks.isUnlocked(v)) {
                        selected = v;
                        playTabSwitch();
                    } else if (ClientCoinData.wildCoins >= v.getPrice()) {
                        pendingBuy = v;
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

    // ── Rendu ────────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        boolean buying = pendingBuy != null;
        confirmBtn.visible = !buying;
        cancelBtn.visible = !buying;
        buyYesBtn.visible = buying;
        buyNoBtn.visible = buying;
        confirmBtn.active = OWClientArenaVenueUnlocks.isUnlocked(selected);

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.arena.venue.title"));

        Component sub = Component.translatable("owteams.arena.venue.subtitle",
                Component.translatable(terrain.translationKey()), targetName);
        drawLabelFitted(g, sub, leftPos + LIST_X, topPos + 17, LIST_W, 8, 0x707070);

        g.enableScissor(leftPos + LIST_X, topPos + LIST_Y, leftPos + LIST_X + LIST_W, topPos + LIST_Y + LIST_H);
        for (int i = 0; i < venues.size(); i++) {
            int cy = cardY(i);
            if (cy + CARD_H < topPos + LIST_Y || cy > topPos + LIST_Y + LIST_H) continue;
            boolean hov = !buying
                    && mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_W
                    && mouseY >= cy && mouseY < cy + CARD_H
                    && mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_H;
            renderCard(g, venues.get(i), leftPos + LIST_X, cy, hov);
        }
        g.disableScissor();

        if (maxScroll() > 0) {
            drawScrollbar(g, leftPos + SCROLLBAR_X, topPos + LIST_Y, LIST_H,
                    scroll, maxScroll(), LIST_H, contentHeight());
        }

        // Solde du joueur, entre les deux boutons : décider sans quitter l'écran.
        String coins = String.valueOf(ClientCoinData.wildCoins);
        int groupW = this.font.width(coins) + 2 + 10;
        int coinsX = leftPos + IMG_W / 2 - groupW / 2;
        int coinsY = topPos + IMG_H - 19;
        g.drawString(this.font, coins, coinsX, coinsY, ClientCoinData.COLOR, false);
        g.blit(COIN_4, coinsX + this.font.width(coins) + 2, coinsY - 1, 10, 10, 0f, 0f, 16, 16, 16, 16);

        super.render(g, mouseX, mouseY, partial);

        if (buying) renderBuyConfirm(g, mouseX, mouseY, partial);
    }

    /**
     * Une vignette de décor : l'image en fond, le nom par-dessus, le cadenas et le prix si le décor
     * n'est pas acquis.
     *
     * <p>Tant que les images ne sont pas livrées, le fond est un cartouche sombre frappé d'un point
     * d'interrogation. Le passage aux vraies vignettes se fera ici même, {@code getTexture()} étant
     * déjà résolu : image grisée si verrouillée, pleine sinon.</p>
     */
    private void renderCard(GuiGraphics g, OWArenaVenue venue, int x, int y, boolean hovered) {
        boolean unlocked = OWClientArenaVenueUnlocks.isUnlocked(venue);
        boolean sel = venue == selected;

        g.fill(x, y, x + LIST_W, y + CARD_H, 0xFF1E1E1E);

        // Vignette du décor : pleine une fois acquis, assombrie tant qu'elle ne l'est pas.
        float tint = unlocked ? (hovered ? 1.0f : 0.88f) : 0.38f;
        RenderSystem.setShaderColor(tint, tint, tint, 1f);
        g.blit(venue.getDisplayTexture(), x, y, LIST_W, CARD_H,
                0f, BAND_V, UV_UNIT, BAND_H, UV_UNIT, UV_UNIT);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Component label = venue.getLabel();
        int labelW = this.font.width(label);
        int labelY = y + CARD_H - 13;
        g.fill(x + LIST_W / 2 - labelW / 2 - 3, labelY - 2,
                x + LIST_W / 2 + labelW / 2 + 3, labelY + 9, 0xAA000000);
        g.drawString(this.font, label, x + LIST_W / 2 - labelW / 2, labelY,
                unlocked ? 0xFFFFFF : 0xA0A0A0, false);

        if (!unlocked) {
            g.blit(OW_TEAMS, x + 6, y + 6, 53, 167, 12, 14);

            String price = String.valueOf(venue.getPrice());
            int pw = this.font.width(price);
            boolean afford = ClientCoinData.wildCoins >= venue.getPrice();
            int px = x + LIST_W - 8 - 10 - 2 - pw;
            g.fill(px - 3, y + 5, x + LIST_W - 5, y + 17, 0xAA000000);
            g.drawString(this.font, price, px, y + 8, afford ? ClientCoinData.COLOR : 0xC85454, false);
            g.blit(COIN_4, px + pw + 2, y + 6, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }

        drawBorder(g, x, y, LIST_W, CARD_H, sel ? 0xFFC8A000 : (hovered ? 0xFF888888 : 0xFF444444));
        if (sel) drawBorder(g, x + 1, y + 1, LIST_W - 2, CARD_H - 2, 0x66C8A000);
    }

    private void renderBuyConfirm(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, 0x99000000);

        Component q = Component.translatable("owteams.arena.venue.buy.question", pendingBuy.getLabel());
        String price = String.valueOf(pendingBuy.getPrice());
        int pw = this.font.width(price);

        int ow = Math.min(this.width - 20, Math.max(210, this.font.width(q) + 24));
        int oh = 80, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
        drawBorder(g, ox, oy, ow, oh, 0xFFC8A000);
        g.drawCenteredString(this.font, q, cx, oy + 12, 0xFFDD77);

        int gx = cx - (pw + 3 + 12) / 2, gy = oy + 32;
        g.drawString(this.font, price, gx, gy, ClientCoinData.COLOR, false);
        g.blit(COIN_4, gx + pw + 3, gy - 2, 12, 12, 0f, 0f, 16, 16, 16, 16);

        buyYesBtn.setPosition(cx - 74, oy + oh - 22);
        buyNoBtn.setPosition(cx + 4, oy + oh - 22);
        buyYesBtn.render(g, mouseX, mouseY, partial);
        buyNoBtn.render(g, mouseX, mouseY, partial);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(new OWTribeArenaScreen());
    }
}
