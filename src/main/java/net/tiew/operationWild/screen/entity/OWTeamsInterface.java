package net.tiew.operationWild.screen.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.gui.OWEntityHud;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.CreateOWTeamPacket;
import net.tiew.operationWild.networking.packets.to_server.DeleteOWTeamPacket;
import net.tiew.operationWild.networking.packets.to_server.RemoveEntityFromTeamPacket;
import net.tiew.operationWild.networking.packets.to_server.RemovePlayerFromTeamPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;

public class OWTeamsInterface extends Screen {

    private static final ResourceLocation OW_TEAMS_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private static final int ELEMENT_U = 200;
    private static final int ELEMENT_V = 0;
    private static final int ELEMENT_WIDTH = 56;
    private static final int ELEMENT_HEIGHT = 93;
    private static final int ELEMENT_X = 7;
    private static final int ELEMENT_Y = 25;

    private static final int RIGHT_X = 68;
    private static final int LIST_W = 100;
    private static final int ROW_H = 11;
    private static final int REMOVE_W = 10;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_H = VISIBLE_ROWS * ROW_H + 2;
    private static final int ICON_SIZE = 9;
    private static final int ICON_RESERVED = ICON_SIZE + 3;

    private static final int PLAYERS_LABEL_Y = 19;
    private static final int PLAYERS_LIST_Y = 29;
    private static final int ENTITIES_LABEL_Y = PLAYERS_LIST_Y + LIST_H + 10;
    private static final int ENTITIES_LIST_Y = ENTITIES_LABEL_Y + 10;

    private static final int BOTTOM_BTN_Y = 145;
    private static final int BOTTOM_BTN_SIZE = 18;

    private static final int ICON_ADD_PLAYER_U = 0;
    private static final int ICON_ADD_PLAYER_V = 166;
    private static final int ICON_ADD_ENTITY_U = 16;
    private static final int ICON_ADD_ENTITY_V = 166;
    private static final int ICON_BTN_SIZE = 16;

    private int leftPos, topPos;

    private final OWEntity entity;
    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    // ── Scroll ─────────────────────────────────────────────────────────────
    private int playerScrollOffset = 0;
    private int entityScrollOffset = 0;

    // ── États de confirmation ───────────────────────────────────────────────
    private enum ConfirmState {
        NONE,
        REMOVE_PLAYER,
        REMOVE_ENTITY,
        LEAVE_TEAM
    }

    private ConfirmState confirmState = ConfirmState.NONE;
    private String confirmTargetName = "";
    private int confirmTargetIndex = -1;
    private Button confirmYesBtn, confirmNoBtn;

    // ── Boutons du bas ──────────────────────────────────────────────────────
    private Button addPlayerBtn, addEntityBtn, leaveTeamBtn;

    // ── Tooltip entité survolée ─────────────────────────────────────────────
    private String hoveredEntityName = null;
    private int hoveredMouseX, hoveredMouseY;

    // ───────────────────────────────────────────────────────────────────────
    public OWTeamsInterface(Component title) {
        super(title);
        Minecraft mc = Minecraft.getInstance();
        this.entity = mc.player != null && mc.player.getRootVehicle() instanceof OWEntity e ? e : null;
    }

    // ── Init ────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;

        tabsRenderer.init(this.width, this.height, IMAGE_WIDTH, IMAGE_HEIGHT, entity, this::addRenderableWidget);
        tabsRenderer.setActiveTab(OWTabsRenderer.Tab.TEAM);

        confirmYesBtn = Button.builder(
                        Component.translatable("owteams.confirm.yes")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))),
                        btn -> onConfirmYes())
                .bounds(0, 0, 48, 14).build();
        this.addRenderableWidget(confirmYesBtn);

        confirmNoBtn = Button.builder(
                        Component.translatable("owteams.confirm.no")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xdd4444))),
                        btn -> confirmState = ConfirmState.NONE)
                .bounds(0, 0, 48, 14).build();
        this.addRenderableWidget(confirmNoBtn);

        // Bouton ajouter joueur → scan des joueurs proches puis invitation
        addPlayerBtn = Button.builder(
                        Component.empty(),
                        btn -> {
                            if (isOwner()) Minecraft.getInstance().setScreen(new OWAddPlayerScanScreen(entity));
                        })
                .bounds(0, 0, BOTTOM_BTN_SIZE, BOTTOM_BTN_SIZE).build();
        this.addRenderableWidget(addPlayerBtn);

        // Bouton ajouter entité
        addEntityBtn = Button.builder(
                        Component.empty(),
                        btn -> {
                            if (isOwner()) Minecraft.getInstance().setScreen(new OWAddEntityScanScreen(entity));
                        })
                .bounds(0, 0, BOTTOM_BTN_SIZE, BOTTOM_BTN_SIZE).build();
        this.addRenderableWidget(addEntityBtn);

        // Bouton quitter la tribu (remplace l'ancienne suppression)
        leaveTeamBtn = Button.builder(
                        Component.empty(),
                        btn -> {
                            if (entity != null && entity.currentTeam != null) {
                                confirmState = ConfirmState.LEAVE_TEAM;
                            }
                        })
                .bounds(0, 0, BOTTOM_BTN_SIZE, BOTTOM_BTN_SIZE).build();
        this.addRenderableWidget(leaveTeamBtn);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private boolean isOwner() {
        if (entity == null || entity.currentTeam == null) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(entity.currentTeam.getTeamOwnerUUID());
    }

    /** Conservé pour compatibilité avec OWEntityAlreadyInTeamPacket — géré côté client dans OWAddEntityScanScreen. */
    public void onEntityAlreadyInTeam(String targetNickname, String currentTeamName) {
    }

    @Override
    public void tick() {
        super.tick();

        // Si l'entité n'est plus dans une team, fermer l'écran
        if (entity == null || entity.currentTeam == null) {
            this.onClose();
            return;
        }

        // Si le scrollOffset dépasse la taille réelle (suite à une suppression), corriger
        OWTeam team = entity.currentTeam;
        if (team.getPlayerNames() != null)
            playerScrollOffset = clamp(playerScrollOffset, team.getPlayerNames().size());
        if (team.getEntityNames() != null)
            entityScrollOffset = clamp(entityScrollOffset, team.getEntityNames().size());
    }

    // ── Confirmation ─────────────────────────────────────────────────────────
    private void onConfirmYes() {
        if (entity == null) {
            confirmState = ConfirmState.NONE;
            return;
        }

        switch (confirmState) {
            case REMOVE_PLAYER -> OWNetworkHandler.sendToServer(
                    new RemovePlayerFromTeamPacket(entity.getId(), confirmTargetIndex));

            case REMOVE_ENTITY -> OWNetworkHandler.sendToServer(
                    new RemoveEntityFromTeamPacket(entity.getId(), confirmTargetIndex));

            case LEAVE_TEAM -> {
                // Index -1 = « self-leave » : le serveur retire l'entité par sa propre identité
                // (robuste même si son UUID n'est pas dans la liste synchronisée — entrées héritées).
                OWNetworkHandler.sendToServer(new RemoveEntityFromTeamPacket(entity.getId(), -1));
                this.onClose();
                return;
            }

            default -> { /* rien */ }
        }

        confirmState = ConfirmState.NONE;
    }

    // ── Scroll ───────────────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        OWTeam team = entity != null ? entity.currentTeam : null;
        if (team == null) return super.mouseScrolled(mx, my, sx, sy);

        if (isOverList(mx, my, PLAYERS_LIST_Y)) {
            playerScrollOffset = clamp(playerScrollOffset - (int) sy, team.getPlayerNames().size());
            return true;
        }
        if (isOverList(mx, my, ENTITIES_LIST_Y)) {
            entityScrollOffset = clamp(entityScrollOffset - (int) sy, team.getEntityNames().size());
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private boolean isOverList(double mx, double my, int listRelY) {
        int lx = leftPos + RIGHT_X, ly = topPos + listRelY;
        return mx >= lx && mx <= lx + LIST_W && my >= ly && my <= ly + LIST_H;
    }

    private int clamp(int offset, int count) {
        return Math.max(0, Math.min(Math.max(0, count - VISIBLE_ROWS), offset));
    }

    // ── Clics ────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (confirmState != ConfirmState.NONE)
            return super.mouseClicked(mx, my, button);

        if (button == 0 && isOwner() && entity != null && entity.currentTeam != null) {
            if (handleRemoveClick(mx, my, entity.currentTeam.getPlayerNames(),
                    PLAYERS_LIST_Y, playerScrollOffset, true)) return true;
            if (handleRemoveClick(mx, my, entity.currentTeam.getEntityNames(),
                    ENTITIES_LIST_Y, entityScrollOffset, false)) return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean handleRemoveClick(double mx, double my, List<String> names,
                                      int listRelY, int scrollOff, boolean isPlayer) {
        if (names == null || names.isEmpty()) return false;
        int btnX = leftPos + RIGHT_X + LIST_W - REMOVE_W - 3;

        OWTeam team = entity != null ? entity.currentTeam : null;

        for (int i = scrollOff; i < Math.min(scrollOff + VISIBLE_ROWS, names.size()); i++) {
            // Le chef (par UUID) n'est jamais retirable.
            if (isPlayer && team != null && i < team.getPlayerUUIDs().size()
                    && team.getPlayerUUIDs().get(i).equals(team.getTeamOwnerUUID())) continue;
            // L'entité fondatrice (l'entité depuis laquelle l'écran est ouvert) n'est pas retirable.
            if (!isPlayer && team != null
                    && i < team.getEntityUUIDs().size()
                    && entity.getUUID().equals(team.getEntityUUIDs().get(i))) continue;

            int rowY = topPos + listRelY + (i - scrollOff) * ROW_H + 1;
            if (mx >= btnX && mx < btnX + REMOVE_W && my >= rowY && my < rowY + ROW_H - 1) {
                confirmTargetIndex = i;
                confirmTargetName = names.get(i);
                confirmState = isPlayer ? ConfirmState.REMOVE_PLAYER : ConfirmState.REMOVE_ENTITY;
                return true;
            }
        }
        return false;
    }

    // ── Render ───────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        boolean isConfirm = confirmState != ConfirmState.NONE;

        hoveredEntityName = null;

        // Masquer tous les widgets ; ils sont réactivés manuellement ci-dessous
        confirmYesBtn.visible = false;
        confirmNoBtn.visible = false;
        addPlayerBtn.visible = false;
        addEntityBtn.visible = false;
        leaveTeamBtn.visible = false;

        super.render(g, mouseX, mouseY, partial);

        // ── Texture de fond ──────────────────────────────────────────────────
        g.blit(OW_TEAMS_LOCATION, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        tabsRenderer.renderTabs(g, this.font, entity, leftPos, topPos, mouseX, mouseY);

        OWTeam team = entity != null ? entity.currentTeam : null;

        // ── En-tête ──────────────────────────────────────────────────────────
        String teamNameStr = team != null
                ? team.getTeamName()
                : Component.translatable("owteams.no_team").getString();
        g.drawString(this.font, teamNameStr, leftPos + 7, topPos + 7, 0x555555, false);

        if (team != null) {
            String ownerName = entity.getCachedOwnerName();
            if (ownerName == null || ownerName.isEmpty())
                ownerName = team.getTeamOwnerUUID().toString().substring(0, 8);
            Component chefComp = Component.translatable("owteams.chef", ownerName)
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)));
            g.drawString(this.font, chefComp,
                    leftPos + IMAGE_WIDTH - this.font.width(chefComp) - 8, topPos + 7, 0x555555, false);
        }

        // ── Drapeau ──────────────────────────────────────────────────────────
        if (team != null) {
            renderFlagWithPattern(g, team.getTeamColor(), team.getTeamSecondaryColor(),
                    team.getTeamMosaicPattern(), team.getPaintPixels());
        } else {
            setElementColor(0xFFFFFF);
            g.blit(OW_TEAMS_LOCATION, leftPos + ELEMENT_X, topPos + ELEMENT_Y,
                    ELEMENT_U, ELEMENT_V, ELEMENT_WIDTH, ELEMENT_HEIGHT);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        g.blit(OW_TEAMS_LOCATION, leftPos + 10, topPos + 93, 203, 98, 50, 25);

        if (team != null) {
            final float SCALE = 0.7f;
            final int LINE_H = (int) (this.font.lineHeight * SCALE) + 1;
            final int BLOCK_H = LINE_H * 3;

            final int infoX = leftPos + 5;
            final int infoY = topPos + IMAGE_HEIGHT - BLOCK_H - 4;

            String[] lines = {
                    Component.translatable("owteams.info.players",
                            team.getPlayerNames().size(), team.getMaxPlayers()).getString(),
                    Component.translatable("owteams.info.entities",
                            team.getEntityNames().size(), team.getMaxEntities()).getString(),
                    Component.translatable("owteams.info.created",
                            team.getFormattedCreationDate()).getString()
            };

            for (int i = 0; i < lines.length; i++) {
                int sy = infoY + i * LINE_H;
                g.pose().pushPose();
                g.pose().translate(infoX, sy, 0);
                g.pose().scale(SCALE, SCALE, 1f);
                // dans l'espace scalé les coords sont /SCALE, on dessine à (0,0)
                g.drawString(this.font, lines[i], 0, 0, 0x555555, false);
                g.pose().popPose();
            }
        }

        // ── Listes membres ───────────────────────────────────────────────────
        if (team != null) {
            renderMemberList(g, Component.translatable("owteams.players_label").getString(),
                    team.getPlayerNames(), PLAYERS_LABEL_Y, PLAYERS_LIST_Y,
                    playerScrollOffset, true, mouseX, mouseY);
            renderMemberList(g, Component.translatable("owteams.entities_label").getString(),
                    team.getEntityNames(), ENTITIES_LABEL_Y, ENTITIES_LIST_Y,
                    entityScrollOffset, false, mouseX, mouseY);
        }

        // ── Boutons bas ───────────────────────────────────────────────────────
        renderBottomButtons(g, mouseX, mouseY, partial, team, isConfirm);

        // ── Tooltips boutons bas (hors overlay) ───────────────────────────────
        if (!isConfirm) {
            if (addPlayerBtn.isMouseOver(mouseX, mouseY)) {
                renderSimpleTooltip(g, mouseX, mouseY,
                        Component.translatable("owteams.button.add_player.tooltip").getString());
            } else if (addEntityBtn.isMouseOver(mouseX, mouseY)) {
                renderSimpleTooltip(g, mouseX, mouseY,
                        Component.translatable("owteams.button.add_entity.tooltip").getString());
            } else if (leaveTeamBtn.isMouseOver(mouseX, mouseY)) {
                renderSimpleTooltip(g, mouseX, mouseY,
                        Component.translatable("owteams.button.leave_team.tooltip").getString());
            }
        }

        // ── Tooltip entité survolée ───────────────────────────────────────────
        if (hoveredEntityName != null && !isConfirm) {
            renderEntityTooltip(g, hoveredEntityName, hoveredMouseX, hoveredMouseY);
        }

        // ── Overlay confirmation ──────────────────────────────────────────────
        if (isConfirm) {
            g.pose().pushPose();
            g.pose().translate(0.0, 0.0, 100.0);
            // Voile sur tout l'écran : le panneau de confirmation peut être plus large que la GUI.
            g.fill(0, 0, this.width, this.height, 0xBB000000);
            renderConfirmOverlay(g, mouseX, mouseY, partial);
            g.pose().popPose();
        }
    }

    private void renderFlagCircle(GuiGraphics g, int fx, int fy, int bg, int circle) {
        float cx = ELEMENT_WIDTH / 2f, cy = ELEMENT_HEIGHT / 2.5f;
        float rx = Math.min(ELEMENT_WIDTH, ELEMENT_HEIGHT) / 3f + 1f;
        float ry = Math.min(ELEMENT_WIDTH, ELEMENT_HEIGHT) / 3f;
        for (int y = 0; y < ELEMENT_HEIGHT; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= ry) {
                float dx = rx * (float) Math.sqrt(1f - (dy * dy) / (ry * ry));
                int x1 = Math.max(0, (int) (cx - dx));
                int x2 = Math.min(ELEMENT_WIDTH, (int) (cx + dx));
                if (x1 > 0) renderFlagRect(g, fx, fy + y, ELEMENT_U, ELEMENT_V + y, x1, 1, bg);
                if (x2 > x1) renderFlagRect(g, fx + x1, fy + y, ELEMENT_U + x1, ELEMENT_V + y, x2 - x1, 1, circle);
                if (x2 < ELEMENT_WIDTH)
                    renderFlagRect(g, fx + x2, fy + y, ELEMENT_U + x2, ELEMENT_V + y, ELEMENT_WIDTH - x2, 1, bg);
            } else {
                renderFlagRect(g, fx, fy + y, ELEMENT_U, ELEMENT_V + y, ELEMENT_WIDTH, 1, bg);
            }
        }
    }

    // ── Boutons bas factorisés ────────────────────────────────────────────────
    private void renderBottomButtons(GuiGraphics g, int mouseX, int mouseY, float partial,
                                     OWTeam team, boolean isConfirm) {
        addPlayerBtn.setX(leftPos + IMAGE_WIDTH - 1);
        addPlayerBtn.setY(topPos + 3);
        addPlayerBtn.active = isOwner();
        addPlayerBtn.visible = true;
        addPlayerBtn.render(g, mouseX, mouseY, partial);
        g.blit(OW_TEAMS_LOCATION,
                addPlayerBtn.getX() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                addPlayerBtn.getY() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                ICON_ADD_PLAYER_U, ICON_ADD_PLAYER_V, ICON_BTN_SIZE, ICON_BTN_SIZE);

        addEntityBtn.setX(leftPos + IMAGE_WIDTH - 1);
        addEntityBtn.setY(topPos + BOTTOM_BTN_SIZE + 4 + 3);
        addEntityBtn.active = isOwner();
        addEntityBtn.visible = true;
        addEntityBtn.render(g, mouseX, mouseY, partial);
        g.blit(OW_TEAMS_LOCATION,
                addEntityBtn.getX() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                addEntityBtn.getY() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                ICON_ADD_ENTITY_U, ICON_ADD_ENTITY_V, ICON_BTN_SIZE, ICON_BTN_SIZE);

        leaveTeamBtn.setX(leftPos + IMAGE_WIDTH - 1);
        leaveTeamBtn.setY(topPos + (BOTTOM_BTN_SIZE + 4) * 2 + 3);
        // N'importe quel membre peut faire quitter la tribu à son entité
        leaveTeamBtn.active = entity != null && entity.currentTeam != null;
        leaveTeamBtn.visible = true;
        leaveTeamBtn.render(g, mouseX, mouseY, partial);

        leaveTeamBtn.render(g, mouseX, mouseY, partial);
        g.blit(OW_TEAMS_LOCATION,
                leaveTeamBtn.getX() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                leaveTeamBtn.getY() + (BOTTOM_BTN_SIZE - ICON_BTN_SIZE) / 2,
                32, 166, ICON_BTN_SIZE, ICON_BTN_SIZE);
    }

    // ── Tooltip simple ────────────────────────────────────────────────────────
    private void renderSimpleTooltip(GuiGraphics g, int mx, int my, String text) {
        int tw = this.font.width(text) + 6;
        int tx = mx + 8;
        int ty = my - 16;
        if (tx + tw > leftPos + IMAGE_WIDTH) tx = mx - tw - 4;
        if (ty < topPos) ty = my + 4;

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.fill(tx - 1, ty - 1, tx + tw + 1, ty + 11, 0xFF000000);
        g.fill(tx, ty, tx + tw, ty + 10, 0xFF1A1A1A);
        g.drawString(this.font, text, tx + 3, ty + 1, 0xFFFFFF, false);
        g.pose().popPose();
    }

    // ── Rendu d'une liste de membres ─────────────────────────────────────────
    private void renderMemberList(GuiGraphics g, String label, List<String> names,
                                  int labelRelY, int listRelY, int scrollOff,
                                  boolean isPlayer, int mouseX, int mouseY) {
        int lx = leftPos + RIGHT_X;
        int ly = topPos + listRelY;

        g.drawString(this.font, label, lx, topPos + labelRelY, 0x555555, true);

        g.fill(lx, ly, lx + LIST_W, ly + LIST_H, 0xCC111111);
        g.fill(lx, ly, lx + LIST_W, ly + 1, 0xFF555555);
        g.fill(lx, ly + LIST_H - 1, lx + LIST_W, ly + LIST_H, 0xFF555555);
        g.fill(lx, ly, lx + 1, ly + LIST_H, 0xFF555555);
        g.fill(lx + LIST_W - 1, ly, lx + LIST_W, ly + LIST_H, 0xFF555555);

        if (names == null || names.isEmpty()) {
            String empty = isPlayer
                    ? Component.translatable("owteams.no_player").getString()
                    : Component.translatable("owteams.no_entity").getString();
            g.drawString(this.font,
                    Component.literal(empty).setStyle(
                            Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true)),
                    lx + 4, ly + 3, 0x555555, false);
            return;
        }

        g.enableScissor(lx + 1, ly + 1, lx + LIST_W - 1, ly + LIST_H - 1);

        OWTeam team = entity != null ? entity.currentTeam : null;

        for (int i = scrollOff; i < Math.min(scrollOff + VISIBLE_ROWS, names.size()); i++) {
            int rowY = ly + (i - scrollOff) * ROW_H + 1;

            if (i % 2 == 0) g.fill(lx + 1, rowY, lx + LIST_W - 1, rowY + ROW_H, 0x18FFFFFF);

            String rawName = names.get(i);
            boolean isOwnerRow = isPlayer && team != null && i < team.getPlayerUUIDs().size()
                    && team.getPlayerUUIDs().get(i).equals(team.getTeamOwnerUUID());
            int nameColor = isPlayer ? (isOwnerRow ? 0xFFD700 : 0xFFFFFF) : getEntityNameColor(rawName);

            boolean isFounderEntity = !isPlayer && team != null
                    && i < team.getEntityUUIDs().size()
                    && entity.getUUID().equals(team.getEntityUUIDs().get(i));
            boolean showRemoveBtn = isOwner() && !(isPlayer && isOwnerRow) && !isFounderEntity;
            int removeReserved = showRemoveBtn ? (REMOVE_W + 4) : 0;
            int iconReserved = isPlayer ? 0 : ICON_RESERVED;
            int maxNameW = LIST_W - 6 - iconReserved - removeReserved;

            String name = rawName.length() > 20 ? rawName.substring(0, 20) : rawName;
            while (this.font.width(name) > maxNameW && name.length() > 1)
                name = name.substring(0, name.length() - 1);

            g.drawString(this.font, name, lx + 3, rowY + 1, nameColor, false);

            if (!isPlayer) {
                int iconX = lx + LIST_W - removeReserved - iconReserved + 1;
                drawEntityHeadIcon(g, rawName, iconX, rowY);
            }

            if (showRemoveBtn) {
                int btnX = lx + LIST_W - REMOVE_W - 3;
                boolean hovered = mouseX >= btnX && mouseX < btnX + REMOVE_W
                        && mouseY >= rowY && mouseY < rowY + ROW_H - 1;
                g.fill(btnX, rowY + 1, btnX + REMOVE_W, rowY + ROW_H - 1,
                        hovered ? 0xBBCC2222 : 0x99881111);
                g.drawString(this.font,
                        Component.literal("−").setStyle(
                                Style.EMPTY.withColor(TextColor.fromRgb(0xFF6666)).withBold(true)),
                        btnX + 2, rowY + 1, 0xFF6666, false);
            }

            if (!isPlayer && mouseX >= lx + 1 && mouseX < lx + LIST_W - 1
                    && mouseY >= rowY && mouseY < rowY + ROW_H) {
                hoveredEntityName = rawName;
                hoveredMouseX = mouseX;
                hoveredMouseY = mouseY;
            }
        }

        g.disableScissor();

        if (names.size() > VISIBLE_ROWS) {
            renderScrollbar(g, lx + LIST_W - 3, ly + 1, 2, VISIBLE_ROWS * ROW_H, scrollOff, names.size());
        }
    }

    // ── Couleur de nom d'entité ──────────────────────────────────────────────
    private int getEntityNameColor(String nickname) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0xFFFFFF;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof OWEntity owE && owE.getNickname().equals(nickname))
                return owE.getEntityColor();
        }
        return 0xFFFFFF;
    }

    // ── Icône de tête d'entité ───────────────────────────────────────────────
    private void drawEntityHeadIcon(GuiGraphics g, String nickname, int x, int rowY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof OWEntity owE) || !owE.getNickname().equals(nickname)) continue;
            OWEntityHud.EntityIconData icon = OWEntityHud.getEntityIconData(owE);
            if (icon != null) {
                int drawY = rowY + (ROW_H - ICON_SIZE) / 2;
                g.blit(OWEntityHud.HUD, x, drawY, ICON_SIZE, ICON_SIZE,
                        (float) icon.textureX, (float) icon.textureY,
                        icon.width, icon.height, 256, 256);
            }
            return;
        }
    }

    // ── Tooltip entité ────────────────────────────────────────────────────────
    private void renderEntityTooltip(GuiGraphics g, String nickname, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        OWEntity owE = null;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof OWEntity candidate && candidate.getNickname().equals(nickname)) {
                owE = candidate;
                break;
            }
        }
        if (owE == null) return;

        String typeName = owE.getType().getDescription().getString();
        String ownerName = owE.getCachedOwnerName();
        if (ownerName == null || ownerName.isEmpty()) ownerName = "?";

        float maxHp = owE.getMaxHealth();
        double damage = owE.getDamageToClient();
        double speed = Math.round(OWUtils.getSpeedBlocksPerSecond(entity) * 100) / 100.0;
        int level = owE.getLevel();

        OWEntityHud.EntityIconData icon = OWEntityHud.getEntityIconData(owE);

        final int TW = 148, TH = 82, PAD = 5, ICON_TT_SIZE = 12;
        int tx = mouseX + 10, ty = mouseY - 10;
        if (tx + TW > this.width) tx = mouseX - TW - 4;
        if (ty + TH > this.height) ty = this.height - TH - 2;
        if (ty < 0) ty = 2;

        drawOverlayPanel(g, tx, ty, TW, TH);

        int cx = tx + TW / 2;
        int lineY = ty + PAD;

        g.drawCenteredString(this.font, Component.literal(nickname).setStyle(Style.EMPTY.withBold(true)), cx, lineY, 0xFFFFFF);
        lineY += this.font.lineHeight + 3;
        g.fill(tx + PAD, lineY, tx + TW - PAD, lineY + 1, 0xFF555555);
        lineY += 4;

        Component typeComp = Component.translatable("owteams.tooltip.type", typeName)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA)));
        int typeW = this.font.width(typeComp);
        int totalW = typeW + (icon != null ? ICON_TT_SIZE + 3 : 0);
        int startX = cx - totalW / 2;
        g.drawString(this.font, typeComp, startX, lineY, 0xAAAAAA, false);

        if (icon != null) {
            int iconX = startX + typeW + 3;
            int iconY = lineY + (this.font.lineHeight - ICON_TT_SIZE) / 2;
            g.blit(OWEntityHud.HUD, iconX, iconY, ICON_TT_SIZE, ICON_TT_SIZE,
                    (float) icon.textureX, (float) icon.textureY,
                    icon.width, icon.height, 256, 256);
        }
        lineY += this.font.lineHeight + 2;

        g.drawCenteredString(this.font,
                Component.translatable("owteams.tooltip.owner", ownerName)
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700))),
                cx, lineY, 0xFFD700);
        lineY += this.font.lineHeight + 3;
        g.fill(tx + PAD, lineY, tx + TW - PAD, lineY + 1, 0xFF555555);
        lineY += 4;

        int col1X = tx + PAD + 2;
        int col2X = tx + TW / 2 + 2;

        g.drawString(this.font,
                Component.translatable("owteams.tooltip.health", String.format("%.0f", maxHp)).getString(),
                col1X, lineY, 0xFF5555, false);
        g.drawString(this.font,
                Component.translatable("owteams.tooltip.speed", String.format("%.1f", speed)).getString(),
                col2X, lineY, 0x55FFFF, false);
        g.drawString(this.font,
                Component.translatable("owteams.tooltip.damage", String.format("%.1f", damage)).getString(),
                col1X, lineY + this.font.lineHeight + 1, 0xFFAA00, false);
        g.drawString(this.font,
                Component.translatable("owteams.tooltip.level", String.valueOf(level)).getString(),
                col2X, lineY + this.font.lineHeight + 1, 0xFFFF55, false);
    }

    private void renderScrollbar(GuiGraphics g, int x, int y, int w, int h, int offset, int total) {
        g.fill(x, y, x + w, y + h, 0x44222222);
        int thumbH = Math.max(6, h * VISIBLE_ROWS / total);
        float maxOffset = Math.max(1, total - VISIBLE_ROWS);
        int thumbY = y + (int) ((h - thumbH) * (offset / maxOffset));
        g.fill(x, thumbY, x + w, thumbY + thumbH, 0xBB999999);
    }

    // ── Overlay confirmation ──────────────────────────────────────────────────
    private void renderConfirmOverlay(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Construire d'abord les deux lignes de texte pour dimensionner le panneau autour d'elles.
        String teamName = entity != null && entity.currentTeam != null
                ? entity.currentTeam.getTeamName() : "?";
        Component title, body;
        if (confirmState == ConfirmState.LEAVE_TEAM) {
            String entityDisplayName = entity != null ? entity.getNickname() : "?";
            title = Component.translatable("owteams.confirm.leave_title");
            body  = Component.translatable("owteams.confirm.leave_body", entityDisplayName, teamName);
        } else {
            title = Component.translatable("owteams.confirm.remove_title", confirmTargetName);
            body  = Component.translatable("owteams.confirm.from_team", teamName);
        }

        // Largeur dynamique : la plus longue des lignes (ou la rangée de boutons), + marges,
        // bornée à l'écran pour ne jamais déborder.
        final int PAD = 12;
        final int BUTTONS_W = 48 + 8 + 48; // deux boutons + écart central
        int contentW = Math.max(BUTTONS_W, Math.max(this.font.width(title), this.font.width(body)));
        int ow = Math.min(this.width - 12, contentW + PAD * 2);
        int oh = 54;
        int cx = leftPos + IMAGE_WIDTH / 2;
        int ox = cx - ow / 2;
        int oy = topPos + IMAGE_HEIGHT / 2 - oh / 2;

        drawOverlayPanel(g, ox, oy, ow, oh);

        g.drawCenteredString(this.font, title, cx, oy + 7, 0xFFFFFF);
        g.drawCenteredString(this.font, body,  cx, oy + 19, 0xAAAAAA);

        confirmYesBtn.setX(cx - 52);
        confirmYesBtn.setY(oy + oh - 18);
        confirmNoBtn.setX(cx + 4);
        confirmNoBtn.setY(oy + oh - 18);
        confirmYesBtn.visible = true;
        confirmNoBtn.visible = true;
        confirmYesBtn.render(g, mouseX, mouseY, partial);
        confirmNoBtn.render(g, mouseX, mouseY, partial);
    }

    private void drawOverlayPanel(GuiGraphics g, int ox, int oy, int ow, int oh) {
        g.fill(ox, oy, ox + ow, oy + oh, 0xEE0D0D0D);
        g.fill(ox, oy, ox + ow, oy + 1, 0xFF666666);
        g.fill(ox, oy + oh - 1, ox + ow, oy + oh, 0xFF666666);
        g.fill(ox, oy, ox + 1, oy + oh, 0xFF666666);
        g.fill(ox + ow - 1, oy, ox + ow, oy + oh, 0xFF666666);
    }

    // ── Utils flag ────────────────────────────────────────────────────────────
    public void setElementColor(int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float gr = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(r, gr, b, 1.0f);
    }

    private void renderFlagWithPattern(GuiGraphics g, int primary, int secondary,
                                       OWTeamMosaicPattern pattern, boolean[] paintPixels) {
        int fx = leftPos + ELEMENT_X;
        int fy = topPos + ELEMENT_Y;
        switch (pattern) {
            case GRADIENT_DOWN -> renderFlagGradientY(g, fx, fy, primary, secondary);
            case GRADIENT_RIGHT -> renderFlagGradientX(g, fx, fy, primary, secondary);
            case SPLIT_H -> {
                int half = ELEMENT_HEIGHT / 2;
                renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, ELEMENT_WIDTH, half, primary);
                renderFlagRect(g, fx, fy + half, ELEMENT_U, ELEMENT_V + half, ELEMENT_WIDTH, ELEMENT_HEIGHT - half, secondary);
            }
            case SPLIT_V -> {
                int half = ELEMENT_WIDTH / 2;
                renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, half, ELEMENT_HEIGHT, primary);
                renderFlagRect(g, fx + half, fy, ELEMENT_U + half, ELEMENT_V, ELEMENT_WIDTH - half, ELEMENT_HEIGHT, secondary);
            }
            case DIAGONAL_TL_BR -> renderFlagDiagonal(g, fx, fy, primary, secondary, false);
            case THIRDS_H -> {
                int t1 = ELEMENT_HEIGHT / 3;
                int t2 = ELEMENT_HEIGHT * 2 / 3;
                renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, ELEMENT_WIDTH, t1, primary);
                renderFlagRect(g, fx, fy + t1, ELEMENT_U, ELEMENT_V + t1, ELEMENT_WIDTH, t2 - t1, secondary);
                renderFlagRect(g, fx, fy + t2, ELEMENT_U, ELEMENT_V + t2, ELEMENT_WIDTH, ELEMENT_HEIGHT - t2, primary);
            }
            case THIRDS_V -> {
                int t1 = ELEMENT_WIDTH / 3;
                int t2 = ELEMENT_WIDTH * 2 / 3;
                renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, t1, ELEMENT_HEIGHT, primary);
                renderFlagRect(g, fx + t1, fy, ELEMENT_U + t1, ELEMENT_V, t2 - t1, ELEMENT_HEIGHT, secondary);
                renderFlagRect(g, fx + t2, fy, ELEMENT_U + t2, ELEMENT_V, ELEMENT_WIDTH - t2, ELEMENT_HEIGHT, primary);
            }
            case CIRCLE_PRI -> renderFlagCircle(g, fx, fy, primary, secondary);
            case STRIPES -> {
                int total = ELEMENT_WIDTH + ELEMENT_HEIGHT;
                for (int row = 0; row < ELEMENT_HEIGHT; row++) {
                    int b1 = total / 3 - row;
                    int b2 = total * 2 / 3 - row;
                    int s1 = Math.max(0, Math.min(b1, ELEMENT_WIDTH));
                    int s2 = Math.max(0, Math.min(b2, ELEMENT_WIDTH));
                    if (s1 > 0)               renderFlagRect(g, fx,      fy + row, ELEMENT_U,      ELEMENT_V + row, s1,               1, primary);
                    if (s2 > s1)              renderFlagRect(g, fx + s1, fy + row, ELEMENT_U + s1, ELEMENT_V + row, s2 - s1,          1, secondary);
                    if (ELEMENT_WIDTH > s2)   renderFlagRect(g, fx + s2, fy + row, ELEMENT_U + s2, ELEMENT_V + row, ELEMENT_WIDTH - s2, 1, primary);
                }
            }
            case CHECKER -> {
                int hw = ELEMENT_WIDTH / 2, hh = ELEMENT_HEIGHT / 2;
                renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, hw, hh, primary);
                renderFlagRect(g, fx + hw, fy, ELEMENT_U + hw, ELEMENT_V, ELEMENT_WIDTH - hw, hh, secondary);
                renderFlagRect(g, fx, fy + hh, ELEMENT_U, ELEMENT_V + hh, hw, ELEMENT_HEIGHT - hh, secondary);
                renderFlagRect(g, fx + hw, fy + hh, ELEMENT_U + hw, ELEMENT_V + hh, ELEMENT_WIDTH - hw, ELEMENT_HEIGHT - hh, primary);
            }
            case DIAMOND -> {
                float scale = 0.78f;
                float halfH = (ELEMENT_HEIGHT / 2f) * scale;
                for (int row = 0; row < ELEMENT_HEIGHT; row++) {
                    float distY = Math.abs(row - ELEMENT_HEIGHT / 2f);
                    if (distY >= halfH) {
                        renderFlagRect(g, fx, fy + row, ELEMENT_U, ELEMENT_V + row, ELEMENT_WIDTH, 1, primary);
                        continue;
                    }
                    float halfW = (1f - distY / halfH) * (ELEMENT_WIDTH / 2f) * scale;
                    int x1 = (int)(ELEMENT_WIDTH / 2f - halfW);
                    int x2 = (int)(ELEMENT_WIDTH / 2f + halfW);
                    if (x1 > 0)             renderFlagRect(g, fx,      fy + row, ELEMENT_U,      ELEMENT_V + row, x1,               1, primary);
                    if (x2 > x1)            renderFlagRect(g, fx + x1, fy + row, ELEMENT_U + x1, ELEMENT_V + row, x2 - x1,          1, secondary);
                    if (x2 < ELEMENT_WIDTH) renderFlagRect(g, fx + x2, fy + row, ELEMENT_U + x2, ELEMENT_V + row, ELEMENT_WIDTH - x2, 1, primary);
                }
            }
            case CUSTOM_PAINT -> {
                if (paintPixels != null && paintPixels.length > 0) {
                    renderFlagCustomPaint(g, fx, fy, primary, secondary, paintPixels);
                } else {
                    renderFlagRect(g, fx, fy, ELEMENT_U, ELEMENT_V, ELEMENT_WIDTH, ELEMENT_HEIGHT, primary);
                }
            }
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderFlagCustomPaint(GuiGraphics g, int fx, int fy,
                                       int primary, int secondary, boolean[] pixels) {
        final int PX_W = 55, PX_H = 93; // dimensions d'encodage de OWTeamCreationScreen
        for (int py = 0; py < PX_H; py++) {
            int     startPx = 0;
            boolean curSec  = pixels[py * PX_W];
            for (int px = 1; px <= PX_W; px++) {
                boolean nextSec = (px < PX_W) && pixels[py * PX_W + px];
                if (px == PX_W || nextSec != curSec) {
                    renderFlagRect(g,
                            fx + startPx, fy + py,
                            ELEMENT_U + startPx, ELEMENT_V + py,
                            px - startPx, 1,
                            curSec ? secondary : primary);
                    startPx = px;
                    curSec  = nextSec;
                }
            }
        }
    }

    private void renderFlagGradientY(GuiGraphics g, int fx, int fy, int topColor, int botColor) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = lerpColor(topColor, botColor, t);
            int y0 = fy + i * ELEMENT_HEIGHT / STRIPS;
            int y1 = fy + (i + 1) * ELEMENT_HEIGHT / STRIPS;
            int v0 = ELEMENT_V + i * ELEMENT_HEIGHT / STRIPS;
            setElementColor(col);
            g.blit(OW_TEAMS_LOCATION, fx, y0, ELEMENT_U, v0, ELEMENT_WIDTH, y1 - y0);
        }
    }

    private void renderFlagGradientX(GuiGraphics g, int fx, int fy, int leftColor, int rightColor) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = lerpColor(leftColor, rightColor, t);
            int x0 = fx + i * ELEMENT_WIDTH / STRIPS;
            int x1 = fx + (i + 1) * ELEMENT_WIDTH / STRIPS;
            int u0 = ELEMENT_U + i * ELEMENT_WIDTH / STRIPS;
            setElementColor(col);
            g.blit(OW_TEAMS_LOCATION, x0, fy, u0, ELEMENT_V, x1 - x0, ELEMENT_HEIGHT);
        }
    }

    private void renderFlagDiagonal(GuiGraphics g, int fx, int fy,
                                    int primary, int secondary, boolean mirrored) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            int y0 = fy + i * ELEMENT_HEIGHT / STRIPS;
            int stripH = fy + (i + 1) * ELEMENT_HEIGHT / STRIPS - y0;
            int v0 = ELEMENT_V + i * ELEMENT_HEIGHT / STRIPS;
            int splitW = ELEMENT_WIDTH * i / STRIPS;
            int priW = ELEMENT_WIDTH - splitW;
            if (!mirrored) {
                if (priW > 0) {
                    setElementColor(primary);
                    g.blit(OW_TEAMS_LOCATION, fx, y0, ELEMENT_U, v0, priW, stripH);
                }
                if (splitW > 0) {
                    setElementColor(secondary);
                    g.blit(OW_TEAMS_LOCATION, fx + priW, y0, ELEMENT_U + priW, v0, splitW, stripH);
                }
            } else {
                if (splitW > 0) {
                    setElementColor(secondary);
                    g.blit(OW_TEAMS_LOCATION, fx, y0, ELEMENT_U, v0, splitW, stripH);
                }
                if (priW > 0) {
                    setElementColor(primary);
                    g.blit(OW_TEAMS_LOCATION, fx + splitW, y0, ELEMENT_U + splitW, v0, priW, stripH);
                }
            }
        }
    }

    private void renderFlagRect(GuiGraphics g, int screenX, int screenY,
                                int u, int v, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        setElementColor(color);
        g.blit(OW_TEAMS_LOCATION, screenX, screenY, u, v, w, h);
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}