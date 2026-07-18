package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.DisbandTribePacket;
import net.tiew.operationWild.networking.packets.to_server.UpdateTribeSettingsPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeJoinRequirement;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran « Paramètres » de la tribu, présenté façon options d'entité : une ligne par réglage (libellé +
 * description + contrôle à droite), fond zébré. Regroupe visibilité, conditions d'entrée (déléguées à
 * {@link OWTribeJoinConditionScreen}), transfert et dissolution.
 */
public class OWTribeSettingsScreen extends OWTribeScreen {

    private static final int PAD = 4, ROW_H = 28, CONTENT_Y = 22;
    private static final int BTN_W = 64, BTN_H = 14;

    private Button visibilityBtn, conditionBtn, directJoinBtn, transferBtn, disbandBtn, confirmYes, confirmNo;
    private boolean confirmDisband = false;
    /** Ligne survolée : les textes y sont tronqués, le tooltip les redonne en entier. */
    private Component hoveredLabel = null, hoveredDesc = null;

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
                OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(
                        !t.isPublic(), new ArrayList<>(t.getJoinRequirements()), t.isDirectJoin()));
        }).bounds(0, 0, BTN_W, BTN_H).build());
        conditionBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.settings.condition_action"),
                b -> Minecraft.getInstance().setScreen(new OWTribeJoinConditionScreen())).bounds(0, 0, BTN_W, BTN_H).build());
        directJoinBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            OWTeam t = OWClientTribeData.get();
            if (t != null && isChief())
                OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(
                        t.isPublic(), new ArrayList<>(t.getJoinRequirements()), !t.isDirectJoin()));
        }).bounds(0, 0, BTN_W, BTN_H).build());
        transferBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.settings.transfer_action"),
                b -> Minecraft.getInstance().setScreen(new OWTribeTransferScreen())).bounds(0, 0, BTN_W, BTN_H).build());
        disbandBtn = addRenderableWidget(Button.builder(
                Component.translatable("owteams.settings.disband_action").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE86A6A))),
                b -> confirmDisband = true).bounds(0, 0, BTN_W, BTN_H).build());

        confirmYes = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"),
                b -> { OWNetworkHandler.sendToServer(new DisbandTribePacket()); confirmDisband = false; }).bounds(0, 0, 60, 16).build());
        confirmNo = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.no"),
                b -> confirmDisband = false).bounds(0, 0, 60, 16).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (!OWClientTribeData.hasTribe()) Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && !confirmDisband && tribeTabClicked(mx, my, Tab.SETTINGS)) return true;
        return super.mouseClicked(mx, my, button);
    }

    /** Dessine une ligne de réglage (fond zébré + survol, libellé + description tronqués). */
    private void drawRow(GuiGraphics g, int x, int y, int w, boolean alt, Component label, Component desc,
                         int textMaxW, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H;
        if (hov) { hoveredLabel = label; hoveredDesc = desc; }
        g.fill(x, y, x + w, y + ROW_H, alt ? 0xAA111111: 0xCC111111);
        if (hov) g.fill(x, y, x + w, y + ROW_H, 0x12FFFFFF);
        g.fill(x, y + ROW_H - 1, x + w, y + ROW_H, 0x40000000);
        g.drawString(this.font, trim(label.getString(), textMaxW), x + PAD, y + 5, 0xE8E8E8, false);
        g.drawString(this.font, trim(desc.getString(), textMaxW), x + PAD, y + 16, 0x9A9A9A, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        hoveredLabel = null; hoveredDesc = null;
        OWTeam t = OWClientTribeData.get();
        boolean chief = isChief();
        boolean pub = t != null && t.isPublic();
        boolean canTransfer = t != null && t.getPlayerUUIDs().size() > 1;
        boolean rows = chief && !confirmDisband && t != null;

        visibilityBtn.visible = rows;
        conditionBtn.visible = rows && pub;
        directJoinBtn.visible = rows && pub;
        transferBtn.visible = rows && canTransfer;
        disbandBtn.visible = rows;
        confirmYes.visible = confirmDisband;
        confirmNo.visible = confirmDisband;

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.settings.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A); // séparateur d'en-tête
        if (!confirmDisband) renderTribeTabs(g, mouseX, mouseY, Tab.SETTINGS);

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

        // Conditions d'entrée (tribu publique uniquement : une tribu privée se rejoint sur invitation)
        if (pub) {
            int cbx = x + w - PAD - BTN_W;
            // Une seule condition tient sur la ligne ; au-delà, on n'annonce que le nombre (détail dans l'écran).
            List<OWTribeJoinRequirement> reqs = t.getJoinRequirements();
            Component current = switch (reqs.size()) {
                case 0 -> Component.translatable("owteams.cond.none.band");
                case 1 -> Component.translatable("owteams.settings.condition.current", reqs.get(0).describe());
                default -> Component.translatable("owteams.settings.condition.count", reqs.size());
            };
            drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.settings.condition"),
                    current, cbx - x - PAD - 4, mouseX, mouseY);
            conditionBtn.setPosition(cbx, y + (ROW_H - BTN_H) / 2 - 1);
            y += ROW_H; alt++;

            // Entrée directe : sinon, la demande passe par le chef et les adjoints.
            boolean direct = t.isDirectJoin();
            int djx = x + w - PAD - BTN_W;
            drawRow(g, x, y, w, alt % 2 == 1, Component.translatable("owteams.settings.direct_join"),
                    Component.translatable("owteams.settings.direct_join.desc"), djx - x - PAD - 4, mouseX, mouseY);
            directJoinBtn.setMessage(Component.translatable(direct ? "owteams.confirm.yes" : "owteams.confirm.no")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(direct ? 0x66DD77 : 0xDD8866))));
            directJoinBtn.setPosition(djx, y + (ROW_H - BTN_H) / 2 - 1);
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
            return; // pas de tooltip de ligne derrière le voile de confirmation
        }

        // Tooltip : libellé + description complète de la ligne survolée (la ligne les tronque).
        if (hoveredLabel != null) {
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(hoveredLabel.copy().withStyle(Style.EMPTY.withBold(true)
                    .withColor(TextColor.fromRgb(0xFFE070))).getVisualOrderText());
            for (FormattedCharSequence line : this.font.split(
                    hoveredDesc.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))), 160)) {
                tip.add(line);
            }
            g.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
