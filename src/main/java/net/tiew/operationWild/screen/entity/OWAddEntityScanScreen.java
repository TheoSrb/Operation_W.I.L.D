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
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.gui.OWEntityHud;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.AddEntityToTeamPacket;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.List;

public class OWAddEntityScanScreen extends Screen {

    private static final int W            = 220;
    private static final int LIST_PAD_X   = 5;
    private static final int LIST_START_Y = 22;
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_H        = 14;
    private static final int ICON_SIZE    = 10;
    private static final int ADD_BTN_SIZE = 12;
    private static final int SCAN_RADIUS  = 64;
    private static final int H            = LIST_START_Y + VISIBLE_ROWS * ROW_H + 22;

    private final OWEntity mountedEntity;
    private List<OWEntity> candidates = new ArrayList<>();
    private int scrollOffset  = 0;
    private int lastHoveredRow = -1;

    private boolean showConfirm           = false;
    private OWEntity confirmTarget        = null;
    private boolean confirmAlreadyInTeam  = false;
    private String confirmCurrentTeamName = "";

    private OWEntity hoveredEntity = null;
    private int hoveredMouseX, hoveredMouseY;

    private Button confirmYesBtn, confirmNoBtn, closeBtn;
    private int leftPos, topPos;

    public OWAddEntityScanScreen(OWEntity mountedEntity) {
        super(Component.translatable("owteams.scan.title"));
        this.mountedEntity = mountedEntity;
    }

    // ── Init ─────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        leftPos = (width - W) / 2;
        topPos  = (height - H) / 2;

        candidates     = scanEntities();
        scrollOffset   = 0;
        lastHoveredRow = -1;

        confirmYesBtn = Button.builder(
                        Component.translatable("owteams.confirm.yes")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))),
                        btn -> onConfirmYes())
                .bounds(0, 0, 48, 14).build();
        addRenderableWidget(confirmYesBtn);

        confirmNoBtn = Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xdd4444))),
                        btn -> {
                            closeConfirm();
                        })
                .bounds(0, 0, 48, 14).build();
        addRenderableWidget(confirmNoBtn);

        closeBtn = Button.builder(
                        Component.translatable("owteams.scan.back")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))),
                        btn -> {
                            Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.empty()));
                        })
                .bounds(0, 0, 80, 14).build();
        addRenderableWidget(closeBtn);
    }

    // ── Son ───────────────────────────────────────────────────────────────────
    private void playUI(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(sound.getLocation(), SoundSource.NEUTRAL, volume, pitch,
                        RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE,
                        0.0, 0.0, 0.0, true));
    }

    // ── Scan ──────────────────────────────────────────────────────────────────
    private List<OWEntity> scanEntities() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mountedEntity == null || mountedEntity.currentTeam == null)
            return new ArrayList<>();

        double range2  = (double) SCAN_RADIUS * SCAN_RADIUS;
        int myTeamId   = mountedEntity.currentTeam.getTeamId();
        List<OWEntity> result = new ArrayList<>();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof OWEntity owE)) continue;
            if (!owE.isTame()) continue;
            if (owE == mountedEntity) continue;
            if (owE.getOwnerUUID() == null) continue;
            // Afficher les entités appartenant à N'IMPORTE QUEL membre de la tribu (chef inclus),
            // pas seulement celles du chef — cohérent avec la validation serveur d'AddEntityToTeamPacket.
            if (!mountedEntity.currentTeam.isMember(owE.getOwnerUUID())) continue;
            if (owE instanceof Submarine) continue;
            // Exclure les entités déjà dans la même tribu
            if (owE.currentTeam != null && owE.currentTeam.getTeamId() == myTeamId) continue;

            double dx = owE.getX() - mountedEntity.getX();
            double dy = owE.getY() - mountedEntity.getY();
            double dz = owE.getZ() - mountedEntity.getZ();
            if (dx * dx + dy * dy + dz * dz <= range2) result.add(owE);
        }
        result.sort((a, b) -> {
            int cmp = a.getType().getDescriptionId().compareTo(b.getType().getDescriptionId());
            if (cmp != 0) return cmp;
            return a.getNickname().compareToIgnoreCase(b.getNickname());
        });
        return result;
    }

    // ── Logique ajout ─────────────────────────────────────────────────────────
    private void onAddClick(OWEntity target) {
        if (mountedEntity == null || mountedEntity.currentTeam == null) return;

        if (target.currentTeam != null
                && target.currentTeam.getTeamId() != mountedEntity.currentTeam.getTeamId()) {
            confirmAlreadyInTeam = true;
            confirmCurrentTeamName = target.currentTeam.getTeamName();
        } else {
            confirmAlreadyInTeam = false;
            confirmCurrentTeamName = "";
        }
        confirmTarget = target;
        showConfirm   = true;
    }

    private void closeConfirm() {
        showConfirm           = false;
        confirmTarget         = null;
        confirmAlreadyInTeam  = false;
        confirmCurrentTeamName = "";
    }

    private void onConfirmYes() {
        if (confirmTarget == null || mountedEntity == null) { closeConfirm(); return; }
        playUI(SoundEvents.PLAYER_LEVELUP, 0.55f, 1.6f);
        OWNetworkHandler.sendToServer(new AddEntityToTeamPacket(
                mountedEntity.getId(), confirmTarget.getId(), confirmAlreadyInTeam));
        closeConfirm();
        Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.empty()));
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (showConfirm) return super.mouseScrolled(mx, my, sx, sy);
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
        if (showConfirm) return super.mouseClicked(mx, my, button);
        if (button == 0) {
            int lx   = leftPos + LIST_PAD_X;
            int ly   = topPos + LIST_START_Y;
            int lw   = W - LIST_PAD_X * 2;
            int addX = lx + lw - ADD_BTN_SIZE - 3;
            for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE_ROWS, candidates.size()); i++) {
                int rowY = ly + (i - scrollOffset) * ROW_H;
                int btnY = rowY + (ROW_H - ADD_BTN_SIZE) / 2;
                if (mx >= addX && mx < addX + ADD_BTN_SIZE && my >= btnY && my < btnY + ADD_BTN_SIZE) {
                    onAddClick(candidates.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        confirmYesBtn.visible = false;
        confirmNoBtn.visible  = false;
        closeBtn.visible      = false;

        hoveredEntity = null;

        super.render(g, mouseX, mouseY, partial);

        drawPanel(g, leftPos, topPos, W, H);

        g.drawCenteredString(this.font,
                Component.translatable("owteams.scan.title").setStyle(Style.EMPTY.withBold(true)),
                leftPos + W / 2, topPos + 6, 0xFFFFFF);
        g.fill(leftPos + 4, topPos + 18, leftPos + W - 4, topPos + 19, 0xFF555555);

        renderEntityList(g, mouseX, mouseY);

        if (hoveredEntity != null && !showConfirm)
            renderRowTooltip(g, hoveredEntity, hoveredMouseX, hoveredMouseY);

        if (!showConfirm) {
            closeBtn.setX(leftPos + W / 2 - 40);
            closeBtn.setY(topPos + H - 18);
            closeBtn.visible = true;
            closeBtn.render(g, mouseX, mouseY, partial);
        }

        if (showConfirm) {
            g.pose().pushPose();
            g.pose().translate(0.0, 0.0, 100.0);
            g.fill(leftPos, topPos, leftPos + W, topPos + H, 0xBB000000);
            renderConfirmOverlay(g, mouseX, mouseY, partial);
            g.pose().popPose();
        }
    }

    // ── Liste ─────────────────────────────────────────────────────────────────
    private void renderEntityList(GuiGraphics g, int mouseX, int mouseY) {
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
                    Component.translatable("owteams.scan.empty")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true)),
                    lx + lw / 2, ly + lh / 2 - font.lineHeight / 2, 0x555555);
            return;
        }

        g.enableScissor(lx + 1, ly + 1, lx + lw - 1, ly + lh - 1);

        int currentHoveredRow = -1;

        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE_ROWS, candidates.size()); i++) {
            OWEntity owE  = candidates.get(i);
            int rowY      = ly + (i - scrollOffset) * ROW_H;
            int textY     = rowY + (ROW_H - font.lineHeight) / 2;
            boolean rowHov = !showConfirm
                    && mouseX >= lx + 1 && mouseX < lx + lw - 1
                    && mouseY >= rowY    && mouseY < rowY + ROW_H;

            if (rowHov) {
                currentHoveredRow = i;
                hoveredEntity = owE;
                hoveredMouseX = mouseX;
                hoveredMouseY = mouseY;
                g.fill(lx + 1, rowY, lx + lw - 1, rowY + ROW_H, 0x28FFFFFF);
            } else if (i % 2 == 0) {
                g.fill(lx + 1, rowY, lx + lw - 1, rowY + ROW_H, 0x18FFFFFF);
            }

            int cx = lx + 3;

            // Icône tête
            OWEntityHud.EntityIconData icon = OWEntityHud.getEntityIconData(owE);
            if (icon != null) {
                int iconY = rowY + (ROW_H - ICON_SIZE) / 2;
                g.blit(OWEntityHud.HUD, cx, iconY, ICON_SIZE, ICON_SIZE,
                        (float) icon.textureX, (float) icon.textureY,
                        icon.width, icon.height, 256, 256);
            }
            cx += ICON_SIZE + 2;

            // Préfixe "Lvl: " / "Niv: " en blanc + numéro coloré
            String lvlPrefix = Component.translatable("tooltip.lvlImage").getString();
            g.drawString(this.font, lvlPrefix, cx, textY, 0xFFFFFF, false);
            cx += font.width(lvlPrefix);
            int level = owE.getLevel();
            String lvlStr  = String.valueOf(level);
            int levelColor = (level >= 50) ? 0xdd9847 : 0xFFFFFF;
            g.drawString(this.font, lvlStr, cx, textY, levelColor, false);
            cx += font.width(lvlStr) + 3;

            // Nom en gras + couleur entité
            int nameColor = owE.getEntityColor();
            g.drawString(this.font,
                    Component.literal(owE.getNickname())
                            .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(nameColor))),
                    cx, textY, nameColor, false);

            // Propriétaire (avant bouton +)
            String ownerName = owE.getCachedOwnerName();
            if (ownerName == null || ownerName.isEmpty()) ownerName = "?";
            int ownerX = addX - font.width(ownerName) - 5;
            g.drawString(this.font, ownerName, ownerX, textY, 0xAAAAAA, false);

            // Bouton "+" adaptatif : vert = libre, orange = déjà dans une autre tribu
            boolean inOtherTeam = owE.currentTeam != null;
            int btnY = rowY + (ROW_H - ADD_BTN_SIZE) / 2;
            boolean btnHov = !showConfirm
                    && mouseX >= addX && mouseX < addX + ADD_BTN_SIZE
                    && mouseY >= btnY  && mouseY < btnY  + ADD_BTN_SIZE;

            if (inOtherTeam) {
                g.fill(addX, btnY, addX + ADD_BTN_SIZE, btnY + ADD_BTN_SIZE,
                        btnHov ? 0xFFe5c950 : 0xFFc09623);
                g.drawCenteredString(this.font,
                        Component.literal("+").setStyle(Style.EMPTY.withBold(true)
                                .withColor(TextColor.fromRgb(0xffdf9a))),
                        addX + ADD_BTN_SIZE / 2, btnY + (ADD_BTN_SIZE - font.lineHeight) / 2 + 1, 0xFFCC55);
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

        lastHoveredRow = currentHoveredRow;

        if (candidates.size() > VISIBLE_ROWS)
            renderScrollbar(g, lx + lw - 3, ly + 1, 2, lh - 2, scrollOffset, candidates.size());
    }

    // ── Tooltip ligne ─────────────────────────────────────────────────────────
    private void renderRowTooltip(GuiGraphics g, OWEntity owE, int mouseX, int mouseY) {
        String typeName = owE.getType().getDescription().getString();
        int level       = owE.getLevel();
        float maxHp     = owE.getMaxHealth();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(typeName)
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(owE.getEntityColor()))));
        lines.add(Component.translatable("owteams.tooltip.health", String.format("%.0f", maxHp))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555))));
        lines.add(Component.translatable("owteams.tooltip.level", String.valueOf(level))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(level >= 50 ? 0xdd9847 : 0xFFFFFF))));
        if (owE.currentTeam != null) {
            lines.add(Component.translatable("owteams.scan.confirm.already_in_team",
                    owE.currentTeam.getTeamName())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF9944))));
        }

        int tw = 0;
        for (Component l : lines) tw = Math.max(tw, font.width(l));
        tw += 8;
        int th = lines.size() * (font.lineHeight + 2) + 4;

        int tx = mouseX + 10, ty = mouseY - th / 2;
        if (tx + tw > width - 2) tx = mouseX - tw - 4;
        if (ty < 2) ty = 2;
        if (ty + th > height - 2) ty = height - th - 2;

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, 0xFF000000);
        g.fill(tx, ty, tx + tw, ty + th, 0xFF1A1A1A);
        g.fill(tx, ty, tx + tw, ty + 1, 0xFF555555);
        int lineY = ty + 3;
        for (Component l : lines) {
            g.drawString(this.font, l, tx + 4, lineY, 0xFFFFFF, false);
            lineY += font.lineHeight + 2;
        }
        g.pose().popPose();
    }

    // ── Overlay confirmation ──────────────────────────────────────────────────
    private void renderConfirmOverlay(GuiGraphics g, int mouseX, int mouseY, float partial) {
        int cx = leftPos + W / 2;

        String entityName = confirmTarget != null ? confirmTarget.getNickname() : "?";
        String teamName   = mountedEntity != null && mountedEntity.currentTeam != null
                ? mountedEntity.currentTeam.getTeamName() : "?";

        // Largeur dynamique selon le texte le plus long
        Component joinComp  = Component.translatable("owteams.scan.confirm.join", entityName, teamName);
        Component titleComp = Component.translatable("owteams.scan.confirm.title");
        int ow = Math.max(font.width(titleComp), font.width(joinComp)) + 28;
        if (confirmAlreadyInTeam) {
            Component warnComp = Component.translatable("owteams.scan.confirm.already_in_team", confirmCurrentTeamName);
            ow = Math.max(ow, font.width(warnComp) + 28);
        }
        ow = Math.max(ow, 110); // minimum pour loger les deux boutons
        ow = Math.min(ow, width - 20); // jamais plus large que l'écran

        int oh = confirmAlreadyInTeam ? 72 : 54;
        int ox = cx - ow / 2;
        int oy = topPos + H / 2 - oh / 2;

        drawPanel(g, ox, oy, ow, oh);

        int lineY = oy + 5;
        g.drawCenteredString(this.font,
                Component.translatable("owteams.scan.confirm.title")
                        .setStyle(Style.EMPTY.withBold(true)),
                cx, lineY, 0xFFFFFF);
        lineY += font.lineHeight + 3;

        g.drawCenteredString(this.font,
                Component.translatable("owteams.scan.confirm.join", entityName, teamName),
                cx, lineY, 0xDDDDDD);
        lineY += font.lineHeight + 2;

        if (confirmAlreadyInTeam) {
            g.drawCenteredString(this.font,
                    Component.translatable("owteams.scan.confirm.already_in_team", confirmCurrentTeamName)
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF9944))),
                    cx, lineY, 0xFF9944);
        }

        confirmYesBtn.setX(cx - 52);
        confirmYesBtn.setY(oy + oh - 18);
        confirmNoBtn.setX(cx + 4);
        confirmNoBtn.setY(oy + oh - 18);
        confirmYesBtn.visible = true;
        confirmNoBtn.visible  = true;
        confirmYesBtn.render(g, mouseX, mouseY, partial);
        confirmNoBtn.render(g, mouseX, mouseY, partial);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────
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
