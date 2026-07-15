package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.TransferChiefPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.List;
import java.util.UUID;

/**
 * Transfert du rôle de chef (ouvert depuis les Paramètres, action rare) : liste des membres à choisir
 * puis confirmation. Chef uniquement.
 */
public class OWTribeTransferScreen extends OWTribeScreen {

    private static final int LIST_X = 6, LIST_Y = 22, ROW_H = 13, FOOTER_H = 22;
    private int listW, listRows;
    private int scroll = 0;

    private boolean confirming = false;
    private UUID target = null;
    private String targetName = "";
    private Button confirmYes, confirmNo;

    public OWTribeTransferScreen() {
        super(Component.translatable("owteams.transfer.title"));
    }

    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    @Override
    protected void init() {
        super.init();
        listW = IMG_W - LIST_X * 2;
        listRows = (IMG_H - LIST_Y - FOOTER_H) / ROW_H;
        this.addRenderableWidget(Button.builder(Component.translatable("owteams.scan.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeSettingsScreen()))
                .bounds(leftPos + IMG_W / 2 - 40, topPos + IMG_H - FOOTER_H + 4, 80, 16).build());

        confirmYes = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"), b -> {
            if (target != null) OWNetworkHandler.sendToServer(new TransferChiefPacket(target.toString()));
            Minecraft.getInstance().setScreen(new OWTribeDashboardScreen());
        }).bounds(0, 0, 60, 16).build());
        confirmNo = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.no"),
                b -> confirming = false).bounds(0, 0, 60, 16).build());
    }

    /** Membres transférables (tous sauf le chef). */
    private List<UUID> targets(OWTeam t) {
        List<UUID> out = new java.util.ArrayList<>();
        for (UUID u : t.getPlayerUUIDs()) if (!t.isChief(u)) out.add(u);
        return out;
    }

    private String nameOf(OWTeam t, UUID u) {
        int idx = t.getPlayerUUIDs().indexOf(u);
        return idx >= 0 && idx < t.getPlayerNames().size() ? t.getPlayerNames().get(idx) : u.toString().substring(0, 6);
    }

    @Override
    public void tick() {
        super.tick();
        if (!OWClientTribeData.hasTribe() || !isChief()) Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        OWTeam t = OWClientTribeData.get();
        if (t != null && !confirming) {
            int max = Math.max(0, targets(t).size() - listRows);
            scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(sy)));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (confirming) return super.mouseClicked(mx, my, button);
        OWTeam t = OWClientTribeData.get();
        if (t != null && button == 0) {
            List<UUID> list = targets(t);
            for (int i = scroll; i < Math.min(scroll + listRows, list.size()); i++) {
                int rowY = topPos + LIST_Y + (i - scroll) * ROW_H;
                if (mx >= leftPos + LIST_X && mx < leftPos + LIST_X + listW && my >= rowY && my < rowY + ROW_H) {
                    target = list.get(i); targetName = nameOf(t, target); confirming = true; return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        confirmYes.visible = confirming;
        confirmNo.visible = confirming;

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.transfer.title"));

        OWTeam t = OWClientTribeData.get();
        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }

        g.drawString(this.font, Component.translatable("owteams.transfer.pick"), leftPos + LIST_X, topPos + 12, 0x606060, false);

        List<UUID> list = targets(t);
        int lx = leftPos + LIST_X, ly = topPos + LIST_Y, lh = listRows * ROW_H;
        boolean hasScroll = list.size() > listRows;
        int contentW = hasScroll ? listW - 5 : listW;
        g.fill(lx, ly, lx + contentW, ly + lh, 0xEE141417);
        g.enableScissor(lx, ly, lx + contentW, ly + lh);
        for (int i = scroll; i < Math.min(scroll + listRows, list.size()); i++) {
            UUID u = list.get(i);
            int rowY = ly + (i - scroll) * ROW_H;
            boolean hov = mouseX >= lx && mouseX < lx + contentW && mouseY >= rowY && mouseY < rowY + ROW_H;
            if (hov) g.fill(lx, rowY, lx + contentW, rowY + ROW_H, 0x33FFD700);
            else if ((i & 1) == 0) g.fill(lx, rowY, lx + contentW, rowY + ROW_H, 0x14FFFFFF);
            boolean dep = t.isDeputy(u);
            g.drawString(this.font, nameOf(t, u), lx + 4, rowY + 3, dep ? 0x6FC3FF : 0xE8E8E8, false);
        }
        g.disableScissor();
        // bevel
        g.fill(lx - 1, ly - 1, lx + listW, ly, 0xFFFFFFFF);
        g.fill(lx - 1, ly - 1, lx, ly + lh, 0xFFFFFFFF);
        g.fill(lx + listW, ly - 1, lx + listW + 1, ly + lh + 1, 0xFF000000);
        g.fill(lx - 1, ly + lh, lx + listW + 1, ly + lh + 1, 0xFF000000);
        if (hasScroll) drawScrollbar(g, lx + listW - 5, ly, lh, scroll, Math.max(0, list.size() - listRows), listRows, list.size());

        super.render(g, mouseX, mouseY, partial);

        if (confirming) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            Component title = Component.translatable("owteams.confirm.transfer_title");
            Component body = Component.translatable("owteams.confirm.transfer_body", targetName);
            int ow = Math.min(this.width - 20, Math.max(190, Math.max(this.font.width(title), this.font.width(body)) + 24));
            int oh = 58, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
            OWTribeDashboardScreen.drawConfirmBox(g, ox, oy, ow, oh, title, body, 0xFFD54F); // doré : passation de chef
            confirmYes.setPosition(cx - 64, oy + oh - 20);
            confirmNo.setPosition(cx + 4, oy + oh - 20);
            confirmYes.render(g, mouseX, mouseY, partial);
            confirmNo.render(g, mouseX, mouseY, partial);
        }
    }
}
