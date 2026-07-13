package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.InvitePlayerToTeamPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Écran de scan des joueurs proches pour les inviter dans la tribu. Miroir de
 * {@link OWAddEntityScanScreen} : le clic sur « + » envoie une invitation (le joueur cible reste
 * libre de l'accepter ou de la refuser), il ne force jamais l'entrée dans la tribu.
 */
public class OWAddPlayerScanScreen extends Screen {

    private static final int W            = 220;
    private static final int LIST_PAD_X   = 5;
    private static final int LIST_START_Y = 22;
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_H        = 14;
    private static final int ADD_BTN_SIZE = 12;
    private static final int SCAN_RADIUS  = 64;
    private static final int H            = LIST_START_Y + VISIBLE_ROWS * ROW_H + 22;

    private final OWEntity mountedEntity;
    private List<Player> candidates = new ArrayList<>();
    private final Set<UUID> invited = new HashSet<>();
    private int scrollOffset = 0;

    private Button closeBtn;
    private int leftPos, topPos;

    public OWAddPlayerScanScreen(OWEntity mountedEntity) {
        super(Component.translatable("owteams.scan_player.title"));
        this.mountedEntity = mountedEntity;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - W) / 2;
        topPos  = (height - H) / 2;

        candidates   = scanPlayers();
        scrollOffset = 0;

        closeBtn = Button.builder(
                        Component.translatable("owteams.scan.back")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))),
                        btn -> Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.empty())))
                .bounds(0, 0, 80, 14).build();
        addRenderableWidget(closeBtn);
    }

    private void playUI(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(sound.getLocation(), SoundSource.NEUTRAL, volume, pitch,
                        RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE,
                        0.0, 0.0, 0.0, true));
    }

    private List<Player> scanPlayers() {
        Minecraft mc = Minecraft.getInstance();
        List<Player> result = new ArrayList<>();
        if (mc.level == null || mc.player == null
                || mountedEntity == null || mountedEntity.currentTeam == null) return result;

        double range2 = (double) SCAN_RADIUS * SCAN_RADIUS;
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            if (mountedEntity.currentTeam.isMember(p.getUUID())) continue;

            double dx = p.getX() - mountedEntity.getX();
            double dy = p.getY() - mountedEntity.getY();
            double dz = p.getZ() - mountedEntity.getZ();
            if (dx * dx + dy * dy + dz * dz <= range2) result.add(p);
        }
        result.sort((a, b) -> a.getName().getString().compareToIgnoreCase(b.getName().getString()));
        return result;
    }

    private void onInviteClick(Player target) {
        if (mountedEntity == null || invited.contains(target.getUUID())) return;
        playUI(SoundEvents.PLAYER_LEVELUP, 0.55f, 1.6f);
        OWNetworkHandler.sendToServer(new InvitePlayerToTeamPacket(mountedEntity.getId(), target.getUUID()));
        invited.add(target.getUUID());
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int lx = leftPos + LIST_PAD_X, ly = topPos + LIST_START_Y;
        int lw = W - LIST_PAD_X * 2,   lh = VISIBLE_ROWS * ROW_H;
        if (mx >= lx && mx <= lx + lw && my >= ly && my <= ly + lh) {
            scrollOffset = Math.max(0, Math.min(Math.max(0, candidates.size() - VISIBLE_ROWS),
                    scrollOffset - (int) sy));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int lx   = leftPos + LIST_PAD_X;
            int ly   = topPos + LIST_START_Y;
            int lw   = W - LIST_PAD_X * 2;
            int addX = lx + lw - ADD_BTN_SIZE - 3;
            for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE_ROWS, candidates.size()); i++) {
                int rowY = ly + (i - scrollOffset) * ROW_H;
                int btnY = rowY + (ROW_H - ADD_BTN_SIZE) / 2;
                if (mx >= addX && mx < addX + ADD_BTN_SIZE && my >= btnY && my < btnY + ADD_BTN_SIZE) {
                    onInviteClick(candidates.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        closeBtn.visible = false;
        super.render(g, mouseX, mouseY, partial);

        drawPanel(g, leftPos, topPos, W, H);

        g.drawCenteredString(this.font,
                Component.translatable("owteams.scan_player.title").setStyle(Style.EMPTY.withBold(true)),
                leftPos + W / 2, topPos + 6, 0xFFFFFF);
        g.fill(leftPos + 4, topPos + 18, leftPos + W - 4, topPos + 19, 0xFF555555);

        renderPlayerList(g, mouseX, mouseY);

        closeBtn.setX(leftPos + W / 2 - 40);
        closeBtn.setY(topPos + H - 18);
        closeBtn.visible = true;
        closeBtn.render(g, mouseX, mouseY, partial);
    }

    private void renderPlayerList(GuiGraphics g, int mouseX, int mouseY) {
        int lx   = leftPos + LIST_PAD_X;
        int ly   = topPos + LIST_START_Y;
        int lw   = W - LIST_PAD_X * 2;
        int lh   = VISIBLE_ROWS * ROW_H;
        int addX = lx + lw - ADD_BTN_SIZE - 3;

        g.fill(lx, ly, lx + lw, ly + lh, 0xCC111111);
        g.fill(lx, ly,          lx + lw, ly + 1,      0xFF555555);
        g.fill(lx, ly + lh - 1, lx + lw, ly + lh,     0xFF555555);
        g.fill(lx, ly,          lx + 1,  ly + lh,     0xFF555555);
        g.fill(lx + lw - 1, ly, lx + lw, ly + lh,     0xFF555555);

        if (candidates.isEmpty()) {
            g.drawCenteredString(this.font,
                    Component.translatable("owteams.scan_player.empty")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true)),
                    lx + lw / 2, ly + lh / 2 - font.lineHeight / 2, 0x555555);
            return;
        }

        g.enableScissor(lx + 1, ly + 1, lx + lw - 1, ly + lh - 1);

        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE_ROWS, candidates.size()); i++) {
            Player p      = candidates.get(i);
            int rowY      = ly + (i - scrollOffset) * ROW_H;
            int textY     = rowY + (ROW_H - font.lineHeight) / 2;
            boolean rowHov = mouseX >= lx + 1 && mouseX < lx + lw - 1
                    && mouseY >= rowY && mouseY < rowY + ROW_H;

            if (rowHov) g.fill(lx + 1, rowY, lx + lw - 1, rowY + ROW_H, 0x28FFFFFF);
            else if (i % 2 == 0) g.fill(lx + 1, rowY, lx + lw - 1, rowY + ROW_H, 0x18FFFFFF);

            g.drawString(this.font,
                    Component.literal(p.getName().getString()).setStyle(Style.EMPTY.withBold(true)),
                    lx + 5, textY, 0xFFFFFF, false);

            boolean isInvited = invited.contains(p.getUUID());
            int btnY = rowY + (ROW_H - ADD_BTN_SIZE) / 2;
            boolean btnHov = !isInvited
                    && mouseX >= addX && mouseX < addX + ADD_BTN_SIZE
                    && mouseY >= btnY && mouseY < btnY + ADD_BTN_SIZE;

            if (isInvited) {
                // Invitation déjà envoyée : coche grisée.
                g.fill(addX, btnY, addX + ADD_BTN_SIZE, btnY + ADD_BTN_SIZE, 0xFF3A3A3A);
                g.drawCenteredString(this.font,
                        Component.literal("✓").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x888888))),
                        addX + ADD_BTN_SIZE / 2, btnY + (ADD_BTN_SIZE - font.lineHeight) / 2 + 1, 0x888888);
            } else {
                g.fill(addX, btnY, addX + ADD_BTN_SIZE, btnY + ADD_BTN_SIZE,
                        btnHov ? 0xFF3DB83D : 0xFF1D6B1D);
                g.drawCenteredString(this.font,
                        Component.literal("+").setStyle(Style.EMPTY.withBold(true)
                                .withColor(TextColor.fromRgb(0x7ddd73))),
                        addX + ADD_BTN_SIZE / 2, btnY + (ADD_BTN_SIZE - font.lineHeight) / 2 + 1, 0x7ddd73);
            }
        }

        g.disableScissor();

        if (candidates.size() > VISIBLE_ROWS)
            renderScrollbar(g, lx + lw - 3, ly + 1, 2, lh - 2, scrollOffset, candidates.size());
    }

    private void drawPanel(GuiGraphics g, int ox, int oy, int ow, int oh) {
        g.fill(ox, oy,          ox + ow, oy + oh, 0xEE0D0D0D);
        g.fill(ox, oy,          ox + ow, oy + 1,  0xFF666666);
        g.fill(ox, oy + oh - 1, ox + ow, oy + oh, 0xFF666666);
        g.fill(ox, oy,          ox + 1,  oy + oh, 0xFF666666);
        g.fill(ox + ow - 1, oy, ox + ow, oy + oh, 0xFF666666);
    }

    private void renderScrollbar(GuiGraphics g, int x, int y, int w, int h, int offset, int total) {
        g.fill(x, y, x + w, y + h, 0x44222222);
        int thumbH   = Math.max(6, h * VISIBLE_ROWS / total);
        float maxOff = Math.max(1, total - VISIBLE_ROWS);
        int thumbY   = y + (int) ((h - thumbH) * (offset / maxOff));
        g.fill(x, thumbY, x + w, thumbY + thumbH, 0xBB999999);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
