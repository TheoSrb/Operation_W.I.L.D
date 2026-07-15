package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.DisbandTribePacket;
import net.tiew.operationWild.networking.packets.to_server.UpdateTribeSettingsPacket;
import net.tiew.operationWild.team.OWTeam;

/**
 * Écran « Paramètres » de la tribu, présenté façon options d'entité : une ligne par réglage (libellé +
 * description + contrôle à droite), fond zébré. Regroupe visibilité, pièces min, transfert et dissolution.
 */
public class OWTribeSettingsScreen extends OWTribeScreen {

    private static final int PAD = 4, ROW_H = 28, CONTENT_Y = 22;
    private static final int BTN_W = 64, BTN_H = 14, COIN_BTN = 14;

    private Button visibilityBtn, coinMinusBtn, coinPlusBtn, transferBtn, disbandBtn, backBtn, confirmYes, confirmNo;
    private boolean confirmDisband = false;

    public OWTribeSettingsScreen() {
        super(Component.translatable("owteams.settings.title"));
    }

    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    @Override
    protected void init() {
        super.init();
        // Bounds (re)positionnés à chaque frame dans render() ; seule la largeur/hauteur compte ici.
        visibilityBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            OWTeam t = OWClientTribeData.get();
            if (t != null && isChief())
                OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(!t.isPublic(), t.getMinWildCoins()));
        }).bounds(0, 0, BTN_W, BTN_H).build());
        coinMinusBtn = addRenderableWidget(Button.builder(Component.literal("-"), b -> adjustCoins(-10)).bounds(0, 0, COIN_BTN, BTN_H).build());
        coinPlusBtn = addRenderableWidget(Button.builder(Component.literal("+"), b -> adjustCoins(10)).bounds(0, 0, COIN_BTN, BTN_H).build());
        transferBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.settings.transfer_action"),
                b -> Minecraft.getInstance().setScreen(new OWTribeTransferScreen())).bounds(0, 0, BTN_W, BTN_H).build());
        disbandBtn = addRenderableWidget(Button.builder(
                Component.translatable("owteams.settings.disband_action").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE86A6A))),
                b -> confirmDisband = true).bounds(0, 0, BTN_W, BTN_H).build());

        backBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.scan.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeDashboardScreen()))
                .bounds(leftPos + IMG_W / 2 - 40, topPos + IMG_H - 22, 80, 16).build());

        confirmYes = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"),
                b -> { OWNetworkHandler.sendToServer(new DisbandTribePacket()); confirmDisband = false; }).bounds(0, 0, 60, 16).build());
        confirmNo = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.no"),
                b -> confirmDisband = false).bounds(0, 0, 60, 16).build());
    }

    private void adjustCoins(int d) {
        OWTeam t = OWClientTribeData.get();
        if (t != null && isChief())
            OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(t.isPublic(), Math.max(0, t.getMinWildCoins() + d)));
    }

    @Override
    public void tick() {
        super.tick();
        if (!OWClientTribeData.hasTribe()) Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
    }

    /** Dessine une ligne de réglage (fond zébré + survol, libellé + description tronqués). */
    private void drawRow(GuiGraphics g, int x, int y, int w, boolean alt, Component label, Component desc,
                         int textMaxW, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H;
        g.fill(x, y, x + w, y + ROW_H, alt ? 0xAA111111: 0xCC111111);
        if (hov) g.fill(x, y, x + w, y + ROW_H, 0x12FFFFFF);
        g.fill(x, y + ROW_H - 1, x + w, y + ROW_H, 0x40000000);
        g.drawString(this.font, trim(label.getString(), textMaxW), x + PAD, y + 5, 0xE8E8E8, false);
        g.drawString(this.font, trim(desc.getString(), textMaxW), x + PAD, y + 16, 0x9A9A9A, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        OWTeam t = OWClientTribeData.get();
        boolean chief = isChief();
        boolean pub = t != null && t.isPublic();
        boolean canTransfer = t != null && t.getPlayerUUIDs().size() > 1;
        boolean rows = chief && !confirmDisband && t != null;

        visibilityBtn.visible = rows;
        coinMinusBtn.visible = rows && pub;
        coinPlusBtn.visible = rows && pub;
        transferBtn.visible = rows && canTransfer;
        disbandBtn.visible = rows;
        backBtn.visible = !confirmDisband;
        confirmYes.visible = confirmDisband;
        confirmNo.visible = confirmDisband;

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.settings.title"));

        if (t == null || !chief) { super.render(g, mouseX, mouseY, partial); return; }

        int x = leftPos + 6, w = IMG_W - 12, y = topPos + CONTENT_Y, alt = 0;

        // Visibilité
        int btnX = x + w - PAD - BTN_W;
        drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.dashboard.visibility"),
                Component.translatable("owteams.settings.visibility.desc"), btnX - x - PAD - 4, mouseX, mouseY);
        visibilityBtn.setMessage(Component.translatable(pub ? "owteams.dashboard.public" : "owteams.dashboard.private")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pub ? 0x66DD77 : 0xDD8866))));
        visibilityBtn.setPosition(btnX, y + (ROW_H - BTN_H) / 2 - 1);
        y += ROW_H; alt++;

        // Pièces minimum (tribu publique)
        if (pub) {
            int plusX = x + w - PAD - COIN_BTN;
            int minusX = plusX - 34;
            drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.dashboard.min_coins"),
                    Component.translatable("owteams.settings.coins.desc"), minusX - x - PAD - 4, mouseX, mouseY);
            coinMinusBtn.setPosition(minusX, y + (ROW_H - BTN_H) / 2 - 1);
            coinPlusBtn.setPosition(plusX, y + (ROW_H - BTN_H) / 2 - 1);
            g.drawCenteredString(this.font, String.valueOf(t.getMinWildCoins()), (minusX + COIN_BTN + plusX) / 2, y + (ROW_H - 8) / 2, 0xFFFFFF);
            y += ROW_H; alt++;
        }

        // Transfert de chef
        if (canTransfer) {
            int tbx = x + w - PAD - BTN_W;
            drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.transfer.title"),
                    Component.translatable("owteams.settings.transfer.desc"), tbx - x - PAD - 4, mouseX, mouseY);
            transferBtn.setPosition(tbx, y + (ROW_H - BTN_H) / 2 - 1);
            y += ROW_H; alt++;
        }

        // Dissolution
        int dbx = x + w - PAD - BTN_W;
        drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.dashboard.disband"),
                Component.translatable("owteams.settings.disband.desc"), dbx - x - PAD - 4, mouseX, mouseY);
        disbandBtn.setPosition(dbx, y + (ROW_H - BTN_H) / 2 - 1);

        super.render(g, mouseX, mouseY, partial);

        if (confirmDisband) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            Component title = Component.translatable("owteams.confirm.disband_title");
            Component body = Component.translatable("owteams.confirm.disband_body", t.getTeamName());
            int ow = Math.min(this.width - 20, Math.max(180, Math.max(this.font.width(title), this.font.width(body)) + 24));
            int oh = 58, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            OWTribeDashboardScreen.drawConfirmBox(g, ox, oy, ow, oh, title, body, 0xE04444);
            confirmYes.setPosition(cx - 64, oy + oh - 20);
            confirmNo.setPosition(cx + 4, oy + oh - 20);
            confirmYes.render(g, mouseX, mouseY, partial);
            confirmNo.render(g, mouseX, mouseY, partial);
        }
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
