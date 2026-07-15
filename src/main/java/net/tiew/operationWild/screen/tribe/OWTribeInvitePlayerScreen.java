package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.InvitePlayerToTribePacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Scan des joueurs proches pour les inviter dans sa tribu (panneau gris, liste déroulante). */
public class OWTribeInvitePlayerScreen extends OWTribeScreen {

    private static final int LIST_MX = 6, LIST_TOP = 22, ROW_H = 16, FOOTER_H = 24;
    private static final int BTN_W = 44, BTN_H = 14, SCAN_RADIUS = 64;

    private int listX, listY, listW, listH, visibleRows;
    private int scroll = 0;
    private List<Player> candidates = new ArrayList<>();
    private final Set<UUID> invited = new HashSet<>();

    public OWTribeInvitePlayerScreen() {
        super(Component.translatable("owteams.scan_player.title"));
    }

    @Override
    protected void init() {
        super.init();
        listX = leftPos + LIST_MX;
        listY = topPos + LIST_TOP;
        listW = IMG_W - LIST_MX * 2;
        listH = IMG_H - LIST_TOP - FOOTER_H;
        visibleRows = listH / ROW_H;
        candidates = scanPlayers();

        this.addRenderableWidget(Button.builder(
                        Component.translatable("owteams.scan.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeDashboardScreen()))
                .bounds(leftPos + IMG_W / 2 - 40, topPos + IMG_H - FOOTER_H + 4, 80, 16).build());
    }

    private List<Player> scanPlayers() {
        Minecraft mc = Minecraft.getInstance();
        List<Player> result = new ArrayList<>();
        OWTeam team = OWClientTribeData.get();
        if (mc.level == null || mc.player == null || team == null) return result;
        double r2 = (double) SCAN_RADIUS * SCAN_RADIUS;
        for (Player p : mc.level.players()) {
            if (p == mc.player || team.isMember(p.getUUID())) continue;
            double dx = p.getX() - mc.player.getX(), dy = p.getY() - mc.player.getY(), dz = p.getZ() - mc.player.getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) result.add(p);
        }
        result.sort((a, b) -> a.getName().getString().compareToIgnoreCase(b.getName().getString()));
        return result;
    }

    private int maxScroll() {
        return Math.max(0, candidates.size() - visibleRows);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(sy)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = scroll; i < Math.min(scroll + visibleRows, candidates.size()); i++) {
                int rowY = listY + (i - scroll) * ROW_H;
                int bx = listX + listW - BTN_W - 6;
                int by = rowY + (ROW_H - BTN_H) / 2;
                if (mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H) {
                    Player t = candidates.get(i);
                    if (!invited.contains(t.getUUID())) {
                        OWNetworkHandler.sendToServer(new InvitePlayerToTribePacket(t.getUUID()));
                        invited.add(t.getUUID());
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.scan_player.title"));

        boolean hasScroll = candidates.size() > visibleRows;
        int contentW = hasScroll ? listW - 6 : listW;

        if (candidates.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.scan_player.empty"),
                    leftPos + IMG_W / 2, listY + listH / 2 - 4, 0x808080);
        }

        g.enableScissor(listX, listY, listX + contentW, listY + visibleRows * ROW_H);
        for (int i = scroll; i < Math.min(scroll + visibleRows, candidates.size()); i++) {
            Player p = candidates.get(i);
            int rowY = listY + (i - scroll) * ROW_H;
            if ((i & 1) == 0) g.fill(listX, rowY, listX + contentW, rowY + ROW_H, 0x18000000);
            g.drawString(this.font, p.getName().getString(), listX + 5, rowY + (ROW_H - 8) / 2, 0x303030, false);

            int bx = listX + contentW - BTN_W - 6;
            int by = rowY + (ROW_H - BTN_H) / 2;
            boolean isInvited = invited.contains(p.getUUID());
            boolean hov = !isInvited && mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
            if (isInvited) {
                Component tag = Component.literal("✓");
                g.drawString(this.font, tag, bx + BTN_W - this.font.width(tag), rowY + (ROW_H - 8) / 2, 0x559955, false);
            } else {
                drawSpriteButton(g, bx, by, BTN_W, BTN_H, Component.literal("+"), hov, 0xFFFFFF);
            }
        }
        g.disableScissor();

        if (hasScroll) {
            drawScrollbar(g, listX + listW - 5, listY, visibleRows * ROW_H, scroll, maxScroll(), visibleRows, candidates.size());
        }

        super.render(g, mouseX, mouseY, partial);
    }
}
