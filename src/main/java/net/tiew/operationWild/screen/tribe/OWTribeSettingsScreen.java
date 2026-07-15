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
 * Écran « Paramètres » de la tribu (ouvert depuis l'onglet paramètres du dashboard). Regroupe la
 * visibilité (public/privé), la condition d'entrée (pièces min) et la dissolution (chef uniquement).
 */
public class OWTribeSettingsScreen extends OWTribeScreen {

    private Button visibilityBtn, coinMinusBtn, coinPlusBtn, disbandBtn, backBtn, confirmYes, confirmNo;
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
        visibilityBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            OWTeam t = OWClientTribeData.get();
            if (t != null && isChief())
                OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(!t.isPublic(), t.getMinWildCoins()));
        }).bounds(leftPos + IMG_W - 8 - 66, topPos + 34, 66, 16).build());

        coinMinusBtn = addRenderableWidget(Button.builder(Component.literal("-"), b -> adjustCoins(-10))
                .bounds(leftPos + IMG_W - 8 - 66, topPos + 62, 16, 16).build());
        coinPlusBtn = addRenderableWidget(Button.builder(Component.literal("+"), b -> adjustCoins(10))
                .bounds(leftPos + IMG_W - 8 - 16, topPos + 62, 16, 16).build());

        disbandBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.dashboard.disband")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        b -> confirmDisband = true)
                .bounds(leftPos + 8, topPos + 96, IMG_W - 16, 16).build());

        backBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.scan.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeDashboardScreen()))
                .bounds(leftPos + IMG_W / 2 - 40, topPos + IMG_H - 22, 80, 16).build());

        confirmYes = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"),
                b -> { OWNetworkHandler.sendToServer(new DisbandTribePacket()); confirmDisband = false; })
                .bounds(0, 0, 60, 16).build());
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        OWTeam t = OWClientTribeData.get();
        boolean chief = isChief();
        boolean coinsRow = chief && t != null && t.isPublic();

        visibilityBtn.visible = chief && !confirmDisband;
        coinMinusBtn.visible = coinsRow && !confirmDisband;
        coinPlusBtn.visible = coinsRow && !confirmDisband;
        disbandBtn.visible = chief && !confirmDisband;
        backBtn.visible = !confirmDisband;
        confirmYes.visible = confirmDisband;
        confirmNo.visible = confirmDisband;

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.settings.title"));

        if (t != null && chief) {
            visibilityBtn.setMessage(Component.translatable(t.isPublic() ? "owteams.dashboard.public" : "owteams.dashboard.private"));
            g.drawString(this.font, Component.translatable("owteams.dashboard.visibility"), leftPos + 8, topPos + 38, 0x404040, false);
            if (t.isPublic()) {
                g.drawString(this.font, Component.translatable("owteams.dashboard.min_coins"), leftPos + 8, topPos + 66, 0x404040, false);
                g.drawCenteredString(this.font, String.valueOf(t.getMinWildCoins()), leftPos + IMG_W - 8 - 33, topPos + 66, 0x303030);
            }
        } else if (t != null) {
            g.drawString(this.font, Component.translatable(t.isPublic() ? "owteams.dashboard.public" : "owteams.dashboard.private"),
                    leftPos + 8, topPos + 38, 0x707070, false);
        }

        super.render(g, mouseX, mouseY, partial);

        if (confirmDisband) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            Component title = Component.translatable("owteams.confirm.disband_title");
            Component body = Component.translatable("owteams.confirm.disband_body", t != null ? t.getTeamName() : "");
            int ow = Math.min(this.width - 20, Math.max(180, Math.max(this.font.width(title), this.font.width(body)) + 24));
            int oh = 58, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            g.fill(ox, oy, ox + ow, oy + oh, 0xF01A1A1A);
            g.fill(ox, oy, ox + ow, oy + 1, 0xFF888888); g.fill(ox, oy + oh - 1, ox + ow, oy + oh, 0xFF888888);
            g.fill(ox, oy, ox + 1, oy + oh, 0xFF888888); g.fill(ox + ow - 1, oy, ox + ow, oy + oh, 0xFF888888);
            g.drawCenteredString(this.font, title, cx, oy + 8, 0xFFFFFF);
            g.drawCenteredString(this.font, body, cx, oy + 20, 0xBBBBBB);
            confirmYes.setPosition(cx - 64, oy + oh - 20);
            confirmNo.setPosition(cx + 4, oy + oh - 20);
            confirmYes.render(g, mouseX, mouseY, partial);
            confirmNo.render(g, mouseX, mouseY, partial);
        }
    }
}
