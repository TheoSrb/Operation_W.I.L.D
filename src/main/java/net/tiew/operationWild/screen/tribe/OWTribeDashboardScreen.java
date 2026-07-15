package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.KickMemberPacket;
import net.tiew.operationWild.networking.packets.to_server.LeaveTribePacket;
import net.tiew.operationWild.networking.packets.to_server.TransferChiefPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.List;
import java.util.UUID;

/** Dashboard de gestion de la tribu : grande bannière, liste de membres, onglet paramètres, invitation. */
public class OWTribeDashboardScreen extends OWTribeScreen {

    private static final ResourceLocation OW_TEAMS =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");
    private static final ResourceLocation OW_INVENTORY =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");
    private static final ResourceLocation OW_SPRITES =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_sprites.png");

    private static final int BANNER_X = 6, BANNER_Y = 18;   // remontée de 15 px
    private static final float BANNER_SCALE = 1.15f;   // un peu plus grande que la pleine taille
    private static final int LIST_X_OFF = 76, LIST_Y = 34, LIST_ROW_H = 12, LIST_ROWS = 9;
    // Onglet paramètres à GAUCHE au-dessus du panneau ; icônes inviter / quitter à droite (comme l'ancien écran).
    private static final int SETTINGS_TAB_X_OFF = 2, SETTINGS_TAB_Y = -18, TAB_W = 20, TAB_H = 18;
    private static final int RIGHT_X_OFF = IMG_W - 1, ICON_SIZE = 18;
    private static final int INVITE_Y = 3, LEAVE_Y = INVITE_Y + ICON_SIZE + 4;

    private int listX, listW;
    private int scroll = 0;

    private Button inviteBtn, leaveBtn, exitBtn;
    private Button confirmYes, confirmNo;

    // Tooltip de la couronne du chef (rendu en fin de frame)
    private boolean hoverChiefCrown = false;
    private int crownMx, crownMy;

    private enum Confirm { NONE, LEAVE, TRANSFER, KICK }
    private Confirm confirm = Confirm.NONE;
    private UUID confirmTarget = null;
    private String confirmTargetName = "";

    public OWTribeDashboardScreen() {
        super(Component.translatable("owteams.dashboard.title"));
    }

    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    @Override
    protected void init() {
        super.init();
        listX = leftPos + LIST_X_OFF;
        listW = IMG_W - LIST_X_OFF - 8;

        // Bouton retour en haut à DROITE : même glyphe « ← » que le bouton Retour des paramètres.
        exitBtn = addRenderableWidget(Button.builder(Component.literal("←"), b -> this.onClose())
                .bounds(leftPos + IMG_W - 17, topPos + 4, 13, 13).build());

        inviteBtn = addRenderableWidget(Button.builder(Component.empty(),
                        b -> Minecraft.getInstance().setScreen(new OWTribeInvitePlayerScreen()))
                .bounds(leftPos + RIGHT_X_OFF, topPos + INVITE_Y, ICON_SIZE, ICON_SIZE).build());
        leaveBtn = addRenderableWidget(Button.builder(Component.empty(),
                        b -> confirm = Confirm.LEAVE)
                .bounds(leftPos + RIGHT_X_OFF, topPos + LEAVE_Y, ICON_SIZE, ICON_SIZE).build());

        confirmYes = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"),
                b -> onConfirmYes()).bounds(0, 0, 60, 16).build());
        confirmNo = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.no"),
                b -> confirm = Confirm.NONE).bounds(0, 0, 60, 16).build());
    }

    private void onConfirmYes() {
        switch (confirm) {
            case LEAVE -> OWNetworkHandler.sendToServer(new LeaveTribePacket());
            case TRANSFER -> { if (confirmTarget != null) OWNetworkHandler.sendToServer(new TransferChiefPacket(confirmTarget.toString())); }
            case KICK -> { if (confirmTarget != null) OWNetworkHandler.sendToServer(new KickMemberPacket(confirmTarget.toString())); }
            default -> {}
        }
        confirm = Confirm.NONE;
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
            int max = Math.max(0, t.getPlayerUUIDs().size() - LIST_ROWS);
            scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(sy)));
        }
        return true;
    }

    private boolean overSettingsTab(double mx, double my) {
        int x = leftPos + SETTINGS_TAB_X_OFF, y = topPos + SETTINGS_TAB_Y;
        return mx >= x && mx < x + TAB_W && my >= y && my < y + TAB_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (confirm != Confirm.NONE) return super.mouseClicked(mx, my, button);
        if (button == 0 && isChief() && overSettingsTab(mx, my)) {
            Minecraft.getInstance().setScreen(new OWTribeSettingsScreen());
            return true;
        }
        OWTeam t = OWClientTribeData.get();
        if (button == 0 && t != null && isChief()) {
            List<UUID> uuids = t.getPlayerUUIDs();
            for (int i = scroll; i < Math.min(scroll + LIST_ROWS, uuids.size()); i++) {
                UUID u = uuids.get(i);
                if (u.equals(t.getTeamOwnerUUID())) continue;
                int rowY = topPos + LIST_Y + (i - scroll) * LIST_ROW_H;
                int kickX = listX + listW - 11, crownX = listX + listW - 23;
                String name = i < t.getPlayerNames().size() ? t.getPlayerNames().get(i) : u.toString().substring(0, 6);
                if (mx >= kickX && mx < kickX + 10 && my >= rowY && my < rowY + LIST_ROW_H - 1) {
                    confirm = Confirm.KICK; confirmTarget = u; confirmTargetName = name; return true;
                }
                if (mx >= crownX && mx < crownX + 10 && my >= rowY && my < rowY + LIST_ROW_H - 1) {
                    confirm = Confirm.TRANSFER; confirmTarget = u; confirmTargetName = name; return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        OWTeam t = OWClientTribeData.get();
        boolean chief = isChief();
        boolean isConfirm = confirm != Confirm.NONE;

        inviteBtn.visible = chief && !isConfirm;
        leaveBtn.visible = !isConfirm;
        exitBtn.visible = !isConfirm;
        confirmYes.visible = isConfirm;
        confirmNo.visible = isConfirm;
        hoverChiefCrown = false;

        drawPanel(g, mouseX, mouseY, partial);

        if (chief && !isConfirm) renderSettingsTab(g, mouseX, mouseY);

        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }

        // Titre de la tribu tout en haut, aligné à gauche.
        g.drawString(this.font, Component.literal(t.getTeamName()), leftPos + 8, topPos + 7, 0x404040, false);

        // Grande bannière (un chouilla plus grande que la pleine taille)
        g.pose().pushPose();
        g.pose().translate(leftPos + BANNER_X, topPos + BANNER_Y, 0);
        g.pose().scale(BANNER_SCALE, BANNER_SCALE, 1f);
        OWBannerRenderer.render(g, 0, 0, t.getBannerShape(),
                t.getTeamColor(), t.getTeamSecondaryColor(), t.getTertiaryColor(), t.isUseTertiary(),
                t.getTeamMosaicPattern(), t.getPaintPixels());
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Liste des membres (droite)
        g.drawString(this.font, Component.translatable("owteams.players_label"), listX, topPos + 24, 0x404040, false);
        renderMemberList(g, t, mouseX, mouseY);

        // Infos en petit, tout en bas à gauche (nombre de joueurs + date de création — sans les entités).
        renderInfoBlock(g, t);

        super.render(g, mouseX, mouseY, partial);

        // Icônes par-dessus les boutons : inviter (haut) + quitter (dessous), mêmes images que l'ancien écran.
        if (inviteBtn.visible)
            g.blit(OW_TEAMS, inviteBtn.getX() + (ICON_SIZE - 16) / 2, inviteBtn.getY() + (ICON_SIZE - 16) / 2, 0, 166, 16, 16);
        if (leaveBtn.visible)
            g.blit(OW_TEAMS, leaveBtn.getX() + (ICON_SIZE - 16) / 2, leaveBtn.getY() + (ICON_SIZE - 16) / 2, 32, 166, 16, 16);

        // Tooltip couronne du chef (« Chef » en doré), au-dessus de tout.
        if (hoverChiefCrown && !isConfirm) {
            g.renderTooltip(this.font,
                    Component.translatable("owteams.chief_role").withStyle(net.minecraft.network.chat.Style.EMPTY
                            .withColor(net.minecraft.network.chat.TextColor.fromRgb(0xFFD700))),
                    crownMx, crownMy);
        }

        if (isConfirm) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            renderConfirmOverlay(g, mouseX, mouseY, partial, t);
        }
    }

    private void renderSettingsTab(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos + SETTINGS_TAB_X_OFF, y = topPos + SETTINGS_TAB_Y;
        boolean hov = overSettingsTab(mouseX, mouseY);
        g.blit(OW_INVENTORY, x, y, hov ? 20 : 0, 206, TAB_W, TAB_H);
        g.blit(OW_INVENTORY, x + 2, y + 2, 176, 80, 16, 16);
        if (hov) g.renderTooltip(this.font, Component.translatable("owteams.settings.title"), mouseX, mouseY);
    }

    private void renderMemberList(GuiGraphics g, OWTeam t, int mouseX, int mouseY) {
        int ly = topPos + LIST_Y, h = LIST_ROWS * LIST_ROW_H;
        List<UUID> uuids = t.getPlayerUUIDs();
        List<String> names = t.getPlayerNames();
        boolean chief = isChief();
        boolean hasScroll = uuids.size() > LIST_ROWS;
        int contentW = hasScroll ? listW - 6 : listW;

        // Fond sombre + zébrage clair discret, comme la liste de l'écran entité.
        g.fill(listX, ly, listX + contentW, ly + h, 0xCC111111);

        g.enableScissor(listX, ly, listX + contentW, ly + h);
        for (int i = scroll; i < Math.min(scroll + LIST_ROWS, uuids.size()); i++) {
            UUID u = uuids.get(i);
            int rowY = ly + (i - scroll) * LIST_ROW_H;
            boolean chiefRow = u.equals(t.getTeamOwnerUUID());
            String name = i < names.size() ? names.get(i) : u.toString().substring(0, 6);
            if ((i & 1) == 0) g.fill(listX, rowY, listX + contentW, rowY + LIST_ROW_H, 0x18FFFFFF);
            g.drawString(this.font, name, listX + 3, rowY + 2, chiefRow ? 0xFFD700 : 0xFFFFFF, false);

            if (chiefRow) {
                // Couronne (8×8, sprites @0,0) à droite de la case du chef.
                int crx = listX + contentW - 10, cry = rowY + 2;
                g.blit(OW_SPRITES, crx, cry, 0, 0, 8, 8, 256, 256);
                if (mouseX >= crx && mouseX < crx + 8 && mouseY >= cry && mouseY < cry + 8) {
                    hoverChiefCrown = true; crownMx = mouseX; crownMy = mouseY;
                }
            } else if (chief) {
                int kickX = listX + contentW - 11, crownX = listX + contentW - 23;
                boolean kHov = mouseX >= kickX && mouseX < kickX + 10 && mouseY >= rowY && mouseY < rowY + LIST_ROW_H - 1;
                boolean cHov = mouseX >= crownX && mouseX < crownX + 10 && mouseY >= rowY && mouseY < rowY + LIST_ROW_H - 1;
                g.fill(kickX, rowY + 1, kickX + 10, rowY + LIST_ROW_H - 1, kHov ? 0xCCCC2222 : 0x66AA2222);
                g.drawString(this.font, "−", kickX + 2, rowY + 2, 0xFFDDDD, false);
                g.fill(crownX, rowY + 1, crownX + 10, rowY + LIST_ROW_H - 1, cHov ? 0xCCBFA030 : 0x66907010);
                g.drawString(this.font, "★", crownX + 2, rowY + 2, 0xFFE070, false);
            }
        }
        g.disableScissor();

        // Bordure biseautée : haut/gauche blancs, droite/bas noirs.
        g.fill(listX - 1, ly - 1, listX + listW, ly, 0xFFFFFFFF);              // haut
        g.fill(listX - 1, ly - 1, listX, ly + h, 0xFFFFFFFF);                  // gauche
        g.fill(listX + listW, ly - 1, listX + listW + 1, ly + h + 1, 0xFF000000); // droite
        g.fill(listX - 1, ly + h, listX + listW + 1, ly + h + 1, 0xFF000000);  // bas

        if (hasScroll) {
            drawScrollbar(g, listX + listW - 5, ly, h, scroll, Math.max(0, uuids.size() - LIST_ROWS), LIST_ROWS, uuids.size());
        }
    }

    /** Petit bloc d'infos aligné en bas à gauche : joueurs + date de création (comme l'écran entité). */
    private void renderInfoBlock(GuiGraphics g, OWTeam t) {
        final float SCALE = 0.7f;
        final int LINE_H = (int) (this.font.lineHeight * SCALE) + 1;
        String[] lines = {
                Component.translatable("owteams.info.players", t.getPlayerUUIDs().size(), t.getMaxPlayers()).getString(),
                Component.translatable("owteams.info.created", t.getFormattedCreationDate()).getString()
        };
        int infoX = leftPos + 5;
        int infoY = topPos + IMG_H - LINE_H * lines.length - 4;
        for (int i = 0; i < lines.length; i++) {
            g.pose().pushPose();
            g.pose().translate(infoX, infoY + i * LINE_H, 0);
            g.pose().scale(SCALE, SCALE, 1f);
            g.drawString(this.font, lines[i], 0, 0, 0x404040, false);
            g.pose().popPose();
        }
    }

    private void renderConfirmOverlay(GuiGraphics g, int mouseX, int mouseY, float partial, OWTeam t) {
        Component title, body;
        switch (confirm) {
            case TRANSFER -> { title = Component.translatable("owteams.confirm.transfer_title");
                body = Component.translatable("owteams.confirm.transfer_body", confirmTargetName); }
            case KICK -> { title = Component.translatable("owteams.confirm.kick_title", confirmTargetName);
                body = Component.translatable("owteams.confirm.kick_body"); }
            default -> { title = Component.translatable("owteams.confirm.leave_title");
                body = Component.translatable("owteams.dashboard.leave_body", t.getTeamName()); }
        }
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
