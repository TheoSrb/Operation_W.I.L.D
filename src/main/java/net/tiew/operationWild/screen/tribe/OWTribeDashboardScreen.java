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
import net.tiew.operationWild.networking.packets.to_server.SetDeputyPacket;
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
    // Onglets à GAUCHE au-dessus du panneau : Permissions puis Paramètres. Icônes inviter/quitter à droite.
    private static final int TAB_Y = -18, TAB_W = 20, TAB_H = 18;
    private static final int PERM_TAB_X_OFF = 2, SETTINGS_TAB_X_OFF = 2 + TAB_W + 1;
    private static final int RIGHT_X_OFF = IMG_W - 1, ICON_SIZE = 18;
    private static final int INVITE_Y = 3, LEAVE_Y = INVITE_Y + ICON_SIZE + 4;

    private int listX, listW;
    private int scroll = 0;

    private Button inviteBtn, leaveBtn, exitBtn;
    private Button confirmYes, confirmNo;

    // Tooltip de rôle (rendu en fin de frame) : 0 aucun, 1 chef, 2 adjoint.
    private int hoverRole = 0;
    private int roleMx, roleMy;

    private enum Confirm { NONE, LEAVE, KICK, DEPUTY }
    private Confirm confirm = Confirm.NONE;
    private UUID confirmTarget = null;
    private String confirmTargetName = "";
    private boolean confirmDeputyPromote = false; // pour Confirm.DEPUTY : true = promouvoir, false = rétrograder

    public OWTribeDashboardScreen() {
        super(Component.translatable("owteams.dashboard.title"));
    }

    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    private boolean isDeputyLocal() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && t.isDeputy(mc.player.getUUID());
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
            case KICK -> { if (confirmTarget != null) OWNetworkHandler.sendToServer(new KickMemberPacket(confirmTarget.toString())); }
            case DEPUTY -> { if (confirmTarget != null) OWNetworkHandler.sendToServer(new SetDeputyPacket(confirmTarget.toString(), confirmDeputyPromote)); }
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

    private boolean overTab(double mx, double my, int xOff) {
        int x = leftPos + xOff, y = topPos + TAB_Y;
        return mx >= x && mx < x + TAB_W && my >= y && my < y + TAB_H;
    }

    /** Positions (offsets depuis listX) des boutons d'une ligne : {adjoint, kick, sprite} ; -1 si absent. */
    private int[] rowButtonsX(int contentW, boolean tChief, boolean tDeputy, boolean chiefV, boolean deputyV) {
        int rx = contentW, spriteX = -1;
        if (tChief || tDeputy) { spriteX = rx - 9; rx -= 10; }
        int kickX = -1, deputyX = -1;
        boolean canKick = !tChief && (chiefV || (deputyV && !tDeputy));
        boolean canManage = chiefV && !tChief; // promouvoir/rétrograder adjoint = chef uniquement
        if (canKick) { kickX = rx - 10; rx -= 11; }
        if (canManage) { deputyX = rx - 10; rx -= 11; }
        return new int[]{ deputyX, kickX, spriteX };
    }

    private boolean hitBtn(double mx, double my, int x, int rowY) {
        return mx >= x && mx < x + 10 && my >= rowY && my < rowY + LIST_ROW_H - 1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (confirm != Confirm.NONE) return super.mouseClicked(mx, my, button);
        boolean chief = isChief(), deputy = isDeputyLocal();
        if (button == 0 && (chief || deputy) && overTab(mx, my, PERM_TAB_X_OFF)) {
            Minecraft.getInstance().setScreen(new OWTribePermissionsScreen());
            return true;
        }
        if (button == 0 && chief && overTab(mx, my, SETTINGS_TAB_X_OFF)) {
            Minecraft.getInstance().setScreen(new OWTribeSettingsScreen());
            return true;
        }
        OWTeam t = OWClientTribeData.get();
        if (button == 0 && t != null && (chief || deputy)) {
            List<UUID> uuids = t.getPlayerUUIDs();
            boolean hasScroll = uuids.size() > LIST_ROWS;
            int contentW = hasScroll ? listW - 6 : listW;
            for (int i = scroll; i < Math.min(scroll + LIST_ROWS, uuids.size()); i++) {
                UUID u = uuids.get(i);
                if (u.equals(t.getTeamOwnerUUID())) continue;
                boolean tDeputy = t.isDeputy(u);
                int rowY = topPos + LIST_Y + (i - scroll) * LIST_ROW_H;
                int[] lay = rowButtonsX(contentW, false, tDeputy, chief, deputy); // {adjoint, kick, sprite}
                String name = i < t.getPlayerNames().size() ? t.getPlayerNames().get(i) : u.toString().substring(0, 6);
                if (lay[1] >= 0 && hitBtn(mx, my, listX + lay[1], rowY)) {
                    confirm = Confirm.KICK; confirmTarget = u; confirmTargetName = name; return true;
                }
                if (lay[0] >= 0 && hitBtn(mx, my, listX + lay[0], rowY)) {
                    confirm = Confirm.DEPUTY; confirmTarget = u; confirmTargetName = name; confirmDeputyPromote = !tDeputy; return true;
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

        boolean deputy = isDeputyLocal();
        inviteBtn.visible = chief && !isConfirm;
        leaveBtn.visible = !isConfirm;
        exitBtn.visible = !isConfirm;
        confirmYes.visible = isConfirm;
        confirmNo.visible = isConfirm;
        hoverRole = 0;

        drawPanel(g, mouseX, mouseY, partial);

        if (!isConfirm) {
            // Onglet Permissions : icône de ow_teams_sprites.png (0,24) ; Paramètres : icône d'inventaire.
            if (chief || deputy) renderTab(g, mouseX, mouseY, PERM_TAB_X_OFF, OW_SPRITES, 0, 24, "owteams.permissions.title");
            if (chief) renderTab(g, mouseX, mouseY, SETTINGS_TAB_X_OFF, OW_INVENTORY, 176, 80, "owteams.settings.title");
        }

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

        // Tooltip de rôle (Chef en doré / Chef Adjoint en bleu clair), au-dessus de tout.
        if (hoverRole != 0 && !isConfirm) {
            int col = hoverRole == 1 ? 0xFFD700 : 0x66CCFF;
            String key = hoverRole == 1 ? "owteams.chief_role" : "owteams.deputy_role";
            g.renderTooltip(this.font,
                    Component.translatable(key).withStyle(net.minecraft.network.chat.Style.EMPTY
                            .withColor(net.minecraft.network.chat.TextColor.fromRgb(col))),
                    roleMx, roleMy);
        }

        if (isConfirm) {
            g.fill(0, 0, this.width, this.height, 0x99000000);
            renderConfirmOverlay(g, mouseX, mouseY, partial, t);
        }
    }

    private void renderTab(GuiGraphics g, int mouseX, int mouseY, int xOff,
                           ResourceLocation iconTex, int iconU, int iconV, String tooltipKey) {
        int x = leftPos + xOff, y = topPos + TAB_Y;
        boolean hov = overTab(mouseX, mouseY, xOff);
        g.blit(OW_INVENTORY, x, y, hov ? 20 : 0, 206, TAB_W, TAB_H);
        g.blit(iconTex, x + 2, y + 2, iconU, iconV, 16, 16);
        if (hov) g.renderTooltip(this.font, Component.translatable(tooltipKey), mouseX, mouseY);
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }

    private void drawRowBtn(GuiGraphics g, int x, int rowY, String glyph, int textCol, int bg, int bgHov, int mx, int my) {
        boolean hov = mx >= x && mx < x + 10 && my >= rowY && my < rowY + LIST_ROW_H - 1;
        g.fill(x, rowY + 1, x + 10, rowY + LIST_ROW_H - 1, hov ? bgHov : bg);
        g.drawString(this.font, glyph, x + 2, rowY + 2, textCol, false);
    }

    private void renderMemberList(GuiGraphics g, OWTeam t, int mouseX, int mouseY) {
        int ly = topPos + LIST_Y, h = LIST_ROWS * LIST_ROW_H;
        List<UUID> uuids = t.getPlayerUUIDs();
        List<String> names = t.getPlayerNames();
        boolean chief = isChief(), deputy = isDeputyLocal();
        boolean hasScroll = uuids.size() > LIST_ROWS;
        int contentW = hasScroll ? listW - 6 : listW;

        // Fond sombre + zébrage clair discret, comme la liste de l'écran entité.
        g.fill(listX, ly, listX + contentW, ly + h, 0xCC111111);

        g.enableScissor(listX, ly, listX + contentW, ly + h);
        for (int i = scroll; i < Math.min(scroll + LIST_ROWS, uuids.size()); i++) {
            UUID u = uuids.get(i);
            int rowY = ly + (i - scroll) * LIST_ROW_H;
            boolean tChief = u.equals(t.getTeamOwnerUUID());
            boolean tDeputy = t.isDeputy(u);
            String name = i < names.size() ? names.get(i) : u.toString().substring(0, 6);
            if ((i & 1) == 0) g.fill(listX, rowY, listX + contentW, rowY + LIST_ROW_H, 0x18FFFFFF);

            int[] lay = rowButtonsX(contentW, tChief, tDeputy, chief, deputy); // {adjoint, kick, sprite}
            int deputyX = lay[0], kickX = lay[1], spriteX = lay[2];

            int rightUsed = contentW;
            for (int v : new int[]{ spriteX, kickX, deputyX }) if (v >= 0) rightUsed = Math.min(rightUsed, v);
            int nameMaxW = rightUsed - 3 - 2;
            int nameCol = tChief ? 0xFFD700 : (tDeputy ? 0x66CCFF : 0xFFFFFF);
            g.drawString(this.font, trim(name, nameMaxW), listX + 3, rowY + 2, nameCol, false);

            if (spriteX >= 0) {
                int sx = listX + spriteX, sy = rowY + 2;
                g.blit(OW_SPRITES, sx, sy, 0, tChief ? 0 : 8, 8, 8, 256, 256); // couronne (0,0) / adjoint (0,8)
                if (mouseX >= sx && mouseX < sx + 8 && mouseY >= sy && mouseY < sy + 8) {
                    hoverRole = tChief ? 1 : 2; roleMx = mouseX; roleMy = mouseY;
                }
            }
            if (kickX >= 0) drawRowBtn(g, listX + kickX, rowY, "−", 0xFFDDDD, 0x66AA2222, 0xCCCC2222, mouseX, mouseY);
            if (deputyX >= 0) drawRowBtn(g, listX + deputyX, rowY, "★", tDeputy ? 0x66CCFF : 0xAAAAAA,
                    tDeputy ? 0x664488AA : 0x55444444, 0xCC5588BB, mouseX, mouseY);
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
        int accent;
        switch (confirm) {
            case DEPUTY -> {
                title = Component.translatable(confirmDeputyPromote ? "owteams.confirm.promote_title" : "owteams.confirm.demote_title");
                body = Component.translatable(confirmDeputyPromote ? "owteams.confirm.promote_body" : "owteams.confirm.demote_body", confirmTargetName);
                accent = confirmDeputyPromote ? 0x4CA3FF : 0xE06A2A; // bleu (gratifier) / orange-rouge (rétrograder)
            }
            case KICK -> { title = Component.translatable("owteams.confirm.kick_title", confirmTargetName);
                body = Component.translatable("owteams.confirm.kick_body"); accent = 0xE04444; } // rouge
            default -> { title = Component.translatable("owteams.confirm.leave_title");
                body = Component.translatable("owteams.dashboard.leave_body", t.getTeamName()); accent = 0xE0A030; } // ambre
        }
        int ow = Math.min(this.width - 20, Math.max(180, Math.max(this.font.width(title), this.font.width(body)) + 24));
        int oh = 58, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        drawConfirmBox(g, ox, oy, ow, oh, title, body, accent);
        confirmYes.setPosition(cx - 64, oy + oh - 20);
        confirmNo.setPosition(cx + 4, oy + oh - 20);
        confirmYes.render(g, mouseX, mouseY, partial);
        confirmNo.render(g, mouseX, mouseY, partial);
    }

    /** Boîte de confirmation avec une couleur d'accent (barre supérieure, bordure et titre). */
    static void drawConfirmBox(GuiGraphics g, int ox, int oy, int ow, int oh,
                               Component title, Component body, int accent) {
        int a = 0xFF000000 | (accent & 0xFFFFFF);
        g.fill(ox, oy, ox + ow, oy + oh, 0xF0121214);
        // bordure teintée
        g.fill(ox, oy, ox + ow, oy + 1, a);
        g.fill(ox, oy + oh - 1, ox + ow, oy + oh, a);
        g.fill(ox, oy, ox + 1, oy + oh, a);
        g.fill(ox + ow - 1, oy, ox + ow, oy + oh, a);
        // barre d'accent sous le titre
        g.fill(ox + 6, oy + 17, ox + ow - 6, oy + 18, (0x66 << 24) | (accent & 0xFFFFFF));
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        g.drawCenteredString(font, title, ox + ow / 2, oy + 6, accent);
        g.drawCenteredString(font, body, ox + ow / 2, oy + 24, 0xCCCCCC);
    }
}
