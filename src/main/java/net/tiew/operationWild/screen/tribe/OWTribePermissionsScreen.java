package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.SetMemberPermissionPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribePermission;

import java.util.List;
import java.util.UUID;

/**
 * Gestion des permissions par membre (Discord-like). Colonne gauche : liste des membres. Colonne
 * droite : interrupteurs de permissions du membre sélectionné (membre simple uniquement ; chef et
 * adjoints ont l'accès total). Accessible au chef et aux adjoints.
 */
public class OWTribePermissionsScreen extends OWTribeScreen {

    private static final ResourceLocation OW_SPRITES =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_sprites.png");

    private static final int LIST_X = 6, LIST_Y = 22, LIST_W = 60, ROW_H = 12;
    private static final int DETAIL_X = 70, DETAIL_Y = 22;
    private static final int DETAIL_W = IMG_W - DETAIL_X - 6;
    private static final int PERM_ROW_H = 15, TOGGLE_W = 22, TOGGLE_H = 10;
    private static final int PERM_TOP = 26;          // y (relatif à DETAIL_Y) de la 1re permission
    private static final int FOOTER_H = 22;

    /** x (absolu) de l'interrupteur d'une rangée de permission. */
    private int toggleX() { return leftPos + IMG_W - 10 - TOGGLE_W; }
    /** y (absolu) de l'interrupteur de la permission d'index i. */
    private int toggleY(int i) { return topPos + DETAIL_Y + PERM_TOP + i * PERM_ROW_H + (PERM_ROW_H - TOGGLE_H) / 2; }

    private int listRows;
    private int scroll = 0;
    private int selected = -1;
    private int hoveredPerm = -1; // permission survolée (pour le tooltip descriptif)

    public OWTribePermissionsScreen() {
        super(Component.translatable("owteams.permissions.title"));
    }

    @Override
    protected void init() {
        super.init();
        listRows = (IMG_H - LIST_Y - FOOTER_H) / ROW_H;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("owteams.scan.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeDashboardScreen()))
                .bounds(leftPos + 8, topPos + IMG_H - FOOTER_H + 4, IMG_W - 16, 14).build());
    }

    private boolean canEdit() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        if (t == null || mc.player == null) return false;
        UUID me = mc.player.getUUID();
        return t.isChief(me) || t.isDeputy(me);
    }

    /** Le joueur local peut-il modifier les permissions de {@code sel} ? Chef → adjoints+membres ; adjoint → membres. */
    private boolean canEditTarget(OWTeam t, UUID sel) {
        Minecraft mc = Minecraft.getInstance();
        if (t == null || mc.player == null) return false;
        UUID me = mc.player.getUUID();
        if (t.isChief(sel)) return false;          // le chef n'a pas de permissions modifiables
        if (t.isChief(me)) return true;
        if (t.isDeputy(me)) return !t.isDeputy(sel);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!OWClientTribeData.hasTribe()) Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        OWTeam t = OWClientTribeData.get();
        if (t != null) {
            int max = Math.max(0, t.getPlayerUUIDs().size() - listRows);
            scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(sy)));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        OWTeam t = OWClientTribeData.get();
        if (t != null && button == 0) {
            List<UUID> uuids = t.getPlayerUUIDs();
            // Sélection dans la liste
            for (int i = scroll; i < Math.min(scroll + listRows, uuids.size()); i++) {
                int rowY = topPos + LIST_Y + (i - scroll) * ROW_H;
                if (mx >= leftPos + LIST_X && mx < leftPos + LIST_X + LIST_W && my >= rowY && my < rowY + ROW_H) {
                    selected = i; return true;
                }
            }
            // Interrupteurs de permission du membre sélectionné
            if (selected >= 0 && selected < uuids.size()) {
                UUID sel = uuids.get(selected);
                if (canEditTarget(t, sel)) {
                    OWTribePermission[] perms = OWTribePermission.values();
                    int tx = toggleX();
                    for (int i = 0; i < perms.length; i++) {
                        int ty = toggleY(i);
                        if (mx >= tx && mx < tx + TOGGLE_W && my >= ty && my < ty + TOGGLE_H) {
                            int newMask = t.getPermissions(sel) ^ perms[i].bit();
                            boolean newOn = (newMask & perms[i].bit()) != 0;
                            OWNetworkHandler.sendToServer(new SetMemberPermissionPacket(sel.toString(), newMask));
                            // Son de bascule : plus aigu quand on active, plus grave quand on désactive.
                            Minecraft.getInstance().getSoundManager().play(
                                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, newOn ? 1.25f : 0.75f));
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        hoveredPerm = -1;
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.permissions.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A); // séparateur d'en-tête

        OWTeam t = OWClientTribeData.get();
        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }
        List<UUID> uuids = t.getPlayerUUIDs();
        List<String> names = t.getPlayerNames();
        int boxH = listRows * ROW_H;

        // ── Liste des membres (gauche) ─────────────────────────────────────────
        int lx = leftPos + LIST_X, lyTop = topPos + LIST_Y;
        g.fill(lx, lyTop, lx + LIST_W, lyTop + boxH, 0xEE141417);
        boolean hasScroll = uuids.size() > listRows;
        int listContentW = hasScroll ? LIST_W - 5 : LIST_W;
        g.enableScissor(lx, lyTop, lx + listContentW, lyTop + boxH);
        for (int i = scroll; i < Math.min(scroll + listRows, uuids.size()); i++) {
            UUID u = uuids.get(i);
            int rowY = lyTop + (i - scroll) * ROW_H;
            boolean tChief = t.isChief(u), tDeputy = t.isDeputy(u);
            if (i == selected) {
                g.fill(lx, rowY, lx + listContentW, rowY + ROW_H, 0x553CA0FF);
                g.fill(lx, rowY, lx + 2, rowY + ROW_H, 0xFF5AB0FF); // accent gauche
            } else if ((i & 1) == 0) g.fill(lx, rowY, lx + listContentW, rowY + ROW_H, 0x14FFFFFF);
            boolean role = tChief || tDeputy;
            String name = i < names.size() ? names.get(i) : u.toString().substring(0, 6);
            int col = tChief ? 0xFFD54F : (tDeputy ? 0x6FC3FF : 0xE8E8E8);
            g.drawString(this.font, trim(name, listContentW - (role ? 13 : 6)), lx + 4, rowY + 2, col, false);
            if (role) g.blit(OW_SPRITES, lx + listContentW - 10, rowY + 2, 0, tChief ? 0 : 8, 8, 8, 256, 256);
        }
        g.disableScissor();
        drawBevel(g, lx, lyTop, LIST_W, boxH);
        if (hasScroll) drawScrollbar(g, lx + LIST_W - 5, lyTop, boxH, scroll, Math.max(0, uuids.size() - listRows), listRows, uuids.size());

        // ── Détail (droite) ────────────────────────────────────────────────────
        int dx = leftPos + DETAIL_X, dyTop = topPos + DETAIL_Y;
        g.fill(dx, dyTop, dx + DETAIL_W, dyTop + boxH, 0xEE141417);
        drawBevel(g, dx, dyTop, DETAIL_W, boxH);

        if (selected < 0 || selected >= uuids.size()) {
            drawCenteredWrapped(g, Component.translatable("owteams.permissions.select").getString(),
                    dx + DETAIL_W / 2, dyTop + boxH / 2 - 4, DETAIL_W - 8, 0x8A8A8A);
        } else {
            UUID sel = uuids.get(selected);
            boolean tChief = t.isChief(sel), tDeputy = t.isDeputy(sel);
            String name = selected < names.size() ? names.get(selected) : sel.toString().substring(0, 6);
            int nameCol = tChief ? 0xFFD54F : (tDeputy ? 0x6FC3FF : 0xFFFFFF);
            g.drawString(this.font, trim(name, DETAIL_W - 8), dx + 4, dyTop + 4, nameCol, false);
            String roleKey = tChief ? "owteams.chief_role" : (tDeputy ? "owteams.deputy_role" : "owteams.permissions.member_role");
            g.drawString(this.font, Component.translatable(roleKey),
                    dx + 4, dyTop + 14, tChief ? 0xC79A2E : (tDeputy ? 0x4E9BD1 : 0x9A9A9A), false);
            g.fill(dx + 4, dyTop + 23, dx + DETAIL_W - 4, dyTop + 24, 0xFF33373D);

            if (tChief) {
                drawCenteredWrapped(g, Component.translatable("owteams.permissions.full_access").getString(),
                        dx + DETAIL_W / 2, dyTop + boxH / 2 + 4, DETAIL_W - 8, 0x8FBF8F);
            } else {
                boolean editable = canEditTarget(t, sel);
                int mask = t.getPermissions(sel);
                OWTribePermission[] perms = OWTribePermission.values();
                int tx = toggleX();
                for (int i = 0; i < perms.length; i++) {
                    int rowY = dyTop + PERM_TOP + i * PERM_ROW_H;
                    boolean rowHov = mouseX >= dx + 2 && mouseX < dx + DETAIL_W - 2 && mouseY >= rowY && mouseY < rowY + PERM_ROW_H;
                    if (rowHov) { hoveredPerm = i; g.fill(dx + 2, rowY, dx + DETAIL_W - 2, rowY + PERM_ROW_H, 0x18FFFFFF); }
                    else if ((i & 1) == 1) g.fill(dx + 2, rowY, dx + DETAIL_W - 2, rowY + PERM_ROW_H, 0x10FFFFFF);
                    boolean on = (mask & perms[i].bit()) != 0;
                    int labelMax = tx - (dx + 5) - 3;
                    g.drawString(this.font, trim(perms[i].getLabel().getString(), labelMax),
                            dx + 5, rowY + (PERM_ROW_H - 8) / 2, 0xD8D8D8, false);
                    int ty = toggleY(i);
                    boolean hov = editable && mouseX >= tx && mouseX < tx + TOGGLE_W && mouseY >= ty && mouseY < ty + TOGGLE_H;
                    drawSwitch(g, tx, ty, on, hov);
                }
            }
        }

        super.render(g, mouseX, mouseY, partial);

        // Tooltip descriptif de la permission survolée (au-dessus de tout).
        if (hoveredPerm >= 0) {
            OWTribePermission p = OWTribePermission.values()[hoveredPerm];
            java.util.List<FormattedCharSequence> tip = new java.util.ArrayList<>();
            tip.add(p.getLabel().copy().withStyle(Style.EMPTY.withBold(true)
                    .withColor(TextColor.fromRgb(0xFFE070))).getVisualOrderText());
            for (FormattedCharSequence line : this.font.split(
                    p.getDescription().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))), 160)) {
                tip.add(line);
            }
            g.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    /** Interrupteur façon « pilule » : fond vert (ON) / gris (OFF) + bouton blanc. */
    private void drawSwitch(GuiGraphics g, int x, int y, boolean on, boolean hov) {
        int bg = on ? (hov ? 0xFF43C25A : 0xFF2FA046) : (hov ? 0xFF5A616B : 0xFF3C424B);
        g.fill(x + 1, y, x + TOGGLE_W - 1, y + TOGGLE_H, bg);
        g.fill(x, y + 1, x + TOGGLE_W, y + TOGGLE_H - 1, bg);
        int kw = TOGGLE_H - 2;
        int kx = on ? x + TOGGLE_W - kw - 1 : x + 1;
        g.fill(kx, y + 1, kx + kw, y + TOGGLE_H - 1, 0xFFF2F2F2);
    }

    /** Texte centré, coupé en deux lignes si trop large. */
    private void drawCenteredWrapped(GuiGraphics g, String text, int cx, int cy, int maxW, int color) {
        if (this.font.width(text) <= maxW) {
            g.drawString(this.font, text, cx - this.font.width(text) / 2, cy, color, false);
            return;
        }
        List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(net.minecraft.network.chat.Component.literal(text), maxW);
        int y = cy - (lines.size() * (this.font.lineHeight)) / 2;
        for (var line : lines) {
            g.drawString(this.font, line, cx - this.font.width(line) / 2, y, color, false);
            y += this.font.lineHeight;
        }
    }

    private void drawBevel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w, y, 0xFFFFFFFF);
        g.fill(x - 1, y - 1, x, y + h, 0xFFFFFFFF);
        g.fill(x + w, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, 0xFF000000);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
