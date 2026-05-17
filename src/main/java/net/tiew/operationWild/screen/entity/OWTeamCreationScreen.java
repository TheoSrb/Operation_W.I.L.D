package net.tiew.operationWild.screen.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.CreateOWTeamWithParamsPacket;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

public class OWTeamCreationScreen extends Screen {

    private static final ResourceLocation OW_TEAMS_LOC =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");

    private static final int ELEM_U = 200, ELEM_V = 0, ELEM_W = 56, ELEM_H = 93;
    private static final int PW = 176, PH = 166;
    private static final int NAME_BOX_Y = 18;
    private static final int PREV_X = 8;
    private static final int PREV_Y = 33;
    private static final int CP_X = 72;
    private static final int CP_W = PW - CP_X - 8;
    private static final int TAB_Y = 33;
    private static final int TAB_W = CP_W / 2 - 2;
    private static final int TAB_H = 12;
    private static final int HUE_Y = 50;
    private static final int HUE_H = 10;
    private static final int SV_Y = 63;
    private static final int SV_H = 63;
    private static final int PAT_BTN_SIZE = 12;
    private static final int PAT_BTN_GAP = 2;
    private static final int PAT_Y = 130;
    private static final int ACT_Y = 149;

    private int leftPos, topPos;
    private final OWEntity entity;

    private int primaryColor = 0xD12020;
    private int secondaryColor = 0x2050D1;
    private OWTeamMosaicPattern selectedPattern = OWTeamMosaicPattern.GRADIENT_DOWN;

    private float primH = 0f, primS = 1f, primV = 0.82f;
    private float secH = 0.62f, secS = 1f, secV = 0.82f;
    private boolean editingPrimary = true;

    private boolean draggingHue = false;
    private boolean draggingSV = false;

    private boolean showNameTakenError = false;
    private Button nameErrorOkBtn;

    private EditBox nameBox;
    private Button confirmBtn, cancelBtn;
    private Button primTabBtn, secTabBtn;

    /**
     * Message d'erreur affiché sous le champ nom (ex : nom déjà utilisé).
     */
    private String nameError = "";

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    // ─────────────────────────────────────────────────────────────────────────
    public OWTeamCreationScreen(OWEntity entity) {
        super(Component.translatable("owteams.creation.title"));
        this.entity = entity;
        primaryColor = hsvToRgb(primH, primS, primV);
        secondaryColor = hsvToRgb(secH, secS, secV);
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - PW) / 2;
        this.topPos = (this.height - PH) / 2;

        nameBox = new EditBox(this.font,
                leftPos + 36, topPos + NAME_BOX_Y,
                PW - 44, 12,
                Component.translatable("owteams.creation.name_placeholder"));
        nameBox.setMaxLength(24);
        nameBox.setValue(
                Component.translatable("owteams.creation.default_name",
                        entity.getOwner().getName().getString()).getString());
        // Effacer l'erreur dès que le joueur retape dans le champ
        nameBox.setResponder(text -> nameError = "");
        this.addRenderableWidget(nameBox);

        primTabBtn = Button.builder(
                        Component.translatable("owteams.creation.primary"),
                        btn -> {
                            editingPrimary = true;
                            refreshTabs();
                        })
                .bounds(leftPos + CP_X, topPos + TAB_Y, TAB_W, TAB_H)
                .build();
        this.addRenderableWidget(primTabBtn);

        secTabBtn = Button.builder(
                        Component.translatable("owteams.creation.secondary"),
                        btn -> {
                            editingPrimary = false;
                            refreshTabs();
                        })
                .bounds(leftPos + CP_X + TAB_W + 4, topPos + TAB_Y, TAB_W, TAB_H)
                .build();
        this.addRenderableWidget(secTabBtn);

        refreshTabs();

        confirmBtn = Button.builder(
                        Component.translatable("owteams.creation.confirm")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))),
                        btn -> onConfirm())
                .bounds(leftPos + 6, topPos + ACT_Y, 78, 12)
                .build();
        this.addRenderableWidget(confirmBtn);

        cancelBtn = Button.builder(
                        Component.translatable("owteams.creation.cancel")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        btn -> onCancel())
                .bounds(leftPos + 92, topPos + ACT_Y, 78, 12)
                .build();
        this.addRenderableWidget(cancelBtn);

        nameErrorOkBtn = Button.builder(
                        Component.translatable("owteams.creation.error.ok"),
                        btn -> showNameTakenError = false)
                .bounds(0, 0, 60, 14).build();
        this.addRenderableWidget(nameErrorOkBtn);

        tabsRenderer.init(this.width, this.height, PW, PH, entity, this::addRenderableWidget);
        tabsRenderer.setActiveTab(OWTabsRenderer.Tab.TEAM);
    }

    private void refreshTabs() {
        if (primTabBtn == null || secTabBtn == null) return;
        primTabBtn.active = !editingPrimary;
        secTabBtn.active = editingPrimary;
    }

    // ── Confirmation / Annulation ─────────────────────────────────────────────
    private void onConfirm() {
        if (entity == null) return;

        String defaultName = Component.translatable("owteams.creation.default_name", "").getString().trim();
        String name = nameBox.getValue().trim().isEmpty() ? defaultName : nameBox.getValue().trim();

        // ── Vérification côté client : nom déjà utilisé dans le monde chargé ──
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof OWEntity owE
                        && owE.currentTeam != null
                        && owE.currentTeam.getTeamName().equalsIgnoreCase(name)) {
                    showNameTakenError = true;
                    return;
                }
            }
        }

        OWNetworkHandler.sendToServer(new CreateOWTeamWithParamsPacket(
                entity.getId(), name, primaryColor, secondaryColor, selectedPattern.getId()));
        Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.translatable("owteams.title")));
    }

    private void onCancel() {
        Minecraft.getInstance().setScreen(null);
    }

    // ── Interactions souris ───────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isInHueBar(mx, my)) {
            draggingHue = true;
            applyHuePick(mx);
            return true;
        }
        if (isInSVSquare(mx, my)) {
            draggingSV = true;
            applySVPick(mx, my);
            return true;
        }

        int n = OWTeamMosaicPattern.values().length;
        int patStartX = leftPos + (PW - (n * PAT_BTN_SIZE + (n - 1) * PAT_BTN_GAP)) / 2;
        for (int i = 0; i < OWTeamMosaicPattern.values().length; i++) {
            int bx = patStartX + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
            int by = topPos + PAT_Y;
            if (mx >= bx && mx < bx + PAT_BTN_SIZE && my >= by && my < by + PAT_BTN_SIZE) {
                selectedPattern = OWTeamMosaicPattern.values()[i];
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingHue) {
            applyHuePick(mx);
            return true;
        }
        if (draggingSV) {
            applySVPick(mx, my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingHue = false;
        draggingSV = false;
        return super.mouseReleased(mx, my, button);
    }

    private boolean isInHueBar(double mx, double my) {
        int hx = leftPos + CP_X, hy = topPos + HUE_Y;
        return mx >= hx && mx < hx + CP_W && my >= hy && my < hy + HUE_H;
    }

    private boolean isInSVSquare(double mx, double my) {
        int sx = leftPos + CP_X, sy = topPos + SV_Y;
        return mx >= sx && mx < sx + CP_W && my >= sy && my < sy + SV_H;
    }

    private void applyHuePick(double mx) {
        float h = (float) clamp01((mx - (leftPos + CP_X)) / CP_W);
        if (editingPrimary) {
            primH = h;
            primaryColor = hsvToRgb(primH, primS, primV);
        } else {
            secH = h;
            secondaryColor = hsvToRgb(secH, secS, secV);
        }
    }

    private void applySVPick(double mx, double my) {
        float s = (float) clamp01((mx - (leftPos + CP_X)) / CP_W);
        float v = (float) clamp01(1.0 - (my - (topPos + SV_Y)) / SV_H);
        if (editingPrimary) {
            primS = s;
            primV = v;
            primaryColor = hsvToRgb(primH, primS, primV);
        } else {
            secS = s;
            secV = v;
            secondaryColor = hsvToRgb(secH, secS, secV);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // vide — voile placé manuellement dans render()
    }

    // ── Rendu principal ───────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderTransparentBackground(g);

        g.blit(OW_TEAMS_LOC, leftPos, topPos, 0, 0, PW, PH);
        tabsRenderer.renderTabs(g, this.font, entity, leftPos, topPos, mouseX, mouseY);

        Component title = Component.translatable("owteams.creation.title")
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFD700)));
        g.drawCenteredString(this.font, title, leftPos + PW / 2, topPos + 6, 0xFFD700);

        g.fill(leftPos + 8, topPos + 15, leftPos + PW - 8, topPos + 16, 0xFF333333);

        // ── Label "Nom :" ou erreur ────────────────────────────────────────────
        if (!nameError.isEmpty()) {
            g.drawString(this.font, nameError,
                    leftPos + 8, topPos + NAME_BOX_Y + 2, 0xFF4444, false);
        } else {
            g.drawString(this.font,
                    Component.translatable("owteams.creation.name_label"),
                    leftPos + 8, topPos + NAME_BOX_Y + 2, 0x555555, false);
        }

        g.fill(leftPos + 8, topPos + 33, leftPos + PW - 8, topPos + 34, 0xFF333333);

        super.render(g, mouseX, mouseY, partial);

        renderFlagWithPattern(g, leftPos + PREV_X, topPos + PREV_Y,
                primaryColor, secondaryColor, selectedPattern);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        renderColorPicker(g, mouseX, mouseY);
        renderPatternSelector(g, mouseX, mouseY);

        g.fill(leftPos + 8, topPos + ACT_Y - 4, leftPos + PW - 8, topPos + ACT_Y - 3, 0xFF333333);

        nameErrorOkBtn.visible = false;

        if (showNameTakenError) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 100);
            g.fill(leftPos, topPos, leftPos + PW, topPos + PH, 0xBB000000);

            int ow = 130, oh = 48;
            int ox = leftPos + (PW - ow) / 2;
            int oy = topPos + (PH - oh) / 2;

            g.fill(ox, oy, ox + ow, oy + oh, 0xEE0D0D0D);
            g.fill(ox, oy, ox + ow, oy + 1, 0xFF666666);
            g.fill(ox, oy + oh - 1, ox + ow, oy + oh, 0xFF666666);
            g.fill(ox, oy, ox + 1, oy + oh, 0xFF666666);
            g.fill(ox + ow - 1, oy, ox + ow, oy + oh, 0xFF666666);

            int cx = ox + ow / 2;
            g.drawCenteredString(this.font,
                    Component.translatable("owteams.creation.error.name_taken_title").getString(),
                    cx, oy + 7, 0xFFFFFF);
            g.drawCenteredString(this.font,
                    Component.translatable("owteams.creation.error.name_taken_body").getString(),
                    cx, oy + 18, 0xAAAAAA);

            nameErrorOkBtn.setX(cx - 30);
            nameErrorOkBtn.setY(oy + oh - 18);
            nameErrorOkBtn.visible = true;
            nameErrorOkBtn.render(g, mouseX, mouseY, partial);

            g.pose().popPose();
        }
    }

    // ── Color picker ──────────────────────────────────────────────────────────
    private void renderColorPicker(GuiGraphics g, int mouseX, int mouseY) {
        int cpAbsX = leftPos + CP_X;

        int hueAbsY = topPos + HUE_Y;
        for (int px = 0; px < CP_W; px++) {
            float h = (float) px / CP_W;
            g.fill(cpAbsX + px, hueAbsY, cpAbsX + px + 1, hueAbsY + HUE_H,
                    0xFF000000 | hsvToRgb(h, 1f, 1f));
        }
        drawBorder(g, cpAbsX, hueAbsY, CP_W, HUE_H, 0xFF555555);

        float curH = editingPrimary ? primH : secH;
        int hueCurX = cpAbsX + (int) (curH * CP_W);
        g.fill(hueCurX - 1, hueAbsY - 1, hueCurX + 2, hueAbsY + HUE_H + 1, 0xFFFFFFFF);
        g.fill(hueCurX, hueAbsY, hueCurX + 1, hueAbsY + HUE_H, 0xFF000000);

        int svAbsY = topPos + SV_Y;
        int step = 3;
        for (int px = 0; px < CP_W; px += step) {
            float s = (float) px / CP_W;
            for (int py = 0; py < SV_H; py += step) {
                float v = 1f - (float) py / SV_H;
                g.fill(cpAbsX + px, svAbsY + py,
                        cpAbsX + px + step, svAbsY + py + step,
                        0xFF000000 | hsvToRgb(curH, s, v));
            }
        }
        drawBorder(g, cpAbsX, svAbsY, CP_W, SV_H, 0xFF555555);

        float curS = editingPrimary ? primS : secS;
        float curV = editingPrimary ? primV : secV;
        int svCurX = cpAbsX + (int) (curS * CP_W);
        int svCurY = svAbsY + (int) ((1f - curV) * SV_H);
        g.fill(svCurX - 3, svCurY - 1, svCurX + 4, svCurY + 2, 0xFFFFFFFF);
        g.fill(svCurX - 1, svCurY - 3, svCurX + 2, svCurY + 4, 0xFFFFFFFF);
        g.fill(svCurX - 2, svCurY, svCurX + 3, svCurY + 1, 0xFF000000);
        g.fill(svCurX, svCurY - 2, svCurX + 1, svCurY + 3, 0xFF000000);
    }

    // ── Sélecteur de motif ────────────────────────────────────────────────────
    private void renderPatternSelector(GuiGraphics g, int mouseX, int mouseY) {
        int n = OWTeamMosaicPattern.values().length;
        int patStartX = leftPos + (PW - (n * PAT_BTN_SIZE + (n - 1) * PAT_BTN_GAP)) / 2;
        int patAbsY = topPos + PAT_Y;

        OWTeamMosaicPattern[] patterns = OWTeamMosaicPattern.values();
        for (int i = 0; i < patterns.length; i++) {
            int bx = patStartX + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
            int by = patAbsY;
            boolean selected = patterns[i] == selectedPattern;
            boolean hovered = mouseX >= bx && mouseX < bx + PAT_BTN_SIZE
                    && mouseY >= by && mouseY < by + PAT_BTN_SIZE;

            g.fill(bx, by, bx + PAT_BTN_SIZE, by + PAT_BTN_SIZE,
                    selected ? 0xFF666666 : (hovered ? 0xFF3A3A3A : 0xFF1E1E1E));

            renderMiniPattern(g, bx + 1, by + 1, PAT_BTN_SIZE - 2, patterns[i]);

            drawBorder(g, bx, by, PAT_BTN_SIZE, PAT_BTN_SIZE,
                    selected ? 0xFFFFD700 : 0xFF555555);

            if (hovered) {
                String label = Component.translatable(patterns[i].getDisplayName()).getString();
                int ttW = this.font.width(label) + 6;
                int ttX = mouseX + 6, ttY = mouseY - 14;

                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                g.fill(ttX - 1, ttY - 1, ttX + ttW + 1, ttY + 10, 0xFF000000);
                g.drawString(this.font, label, ttX + 2, ttY + 1, 0xFFFFFF, false);
                g.pose().popPose();
            }
        }
    }

    private void renderMiniCircle(GuiGraphics g, int bx, int by, int size, int bg, int circle) {
        float cx = size / 2f, cy = size / 2f, r = size / 3f;
        for (int y = 0; y < size; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= r) {
                float dx = (float) Math.sqrt(r * r - dy * dy);
                int x1 = Math.max(0, (int) (cx - dx));
                int x2 = Math.min(size, (int) (cx + dx));
                if (x1 > 0) g.fill(bx, by + y, bx + x1, by + y + 1, bg | 0xFF000000);
                if (x2 > x1) g.fill(bx + x1, by + y, bx + x2, by + y + 1, circle | 0xFF000000);
                if (x2 < size) g.fill(bx + x2, by + y, bx + size, by + y + 1, bg | 0xFF000000);
            } else {
                g.fill(bx, by + y, bx + size, by + y + 1, bg | 0xFF000000);
            }
        }
    }

    // ── Mini aperçu d'un motif ────────────────────────────────────────────────
    private void renderMiniPattern(GuiGraphics g, int bx, int by, int size,
                                   OWTeamMosaicPattern pattern) {
        int p = primaryColor | 0xFF000000;
        int s = secondaryColor | 0xFF000000;
        int strips = Math.max(1, size / 3);

        switch (pattern) {
            case GRADIENT_DOWN -> {
                for (int i = 0; i < strips; i++) {
                    float t = (float) i / (strips - 1 == 0 ? 1 : strips - 1);
                    int col = 0xFF000000 | lerpColor(primaryColor, secondaryColor, t);
                    g.fill(bx, by + i * size / strips, bx + size, by + (i + 1) * size / strips, col);
                }
            }
            case GRADIENT_RIGHT -> {
                for (int i = 0; i < strips; i++) {
                    float t = (float) i / (strips - 1 == 0 ? 1 : strips - 1);
                    int col = 0xFF000000 | lerpColor(primaryColor, secondaryColor, t);
                    g.fill(bx + i * size / strips, by, bx + (i + 1) * size / strips, by + size, col);
                }
            }
            case SPLIT_H -> {
                int half = size / 2;
                g.fill(bx, by, bx + size, by + half, p);
                g.fill(bx, by + half, bx + size, by + size, s);
            }
            case SPLIT_V -> {
                int half = size / 2;
                g.fill(bx, by, bx + half, by + size, p);
                g.fill(bx + half, by, bx + size, by + size, s);
            }
            case DIAGONAL_TL_BR -> {
                for (int row = 0; row < size; row++) {
                    int split = size - row;
                    if (split > 0) g.fill(bx, by + row, bx + Math.min(split, size), by + row + 1, p);
                    if (split < size) g.fill(bx + Math.max(split, 0), by + row, bx + size, by + row + 1, s);
                }
            }
            case DIAGONAL_TR_BL -> {
                for (int row = 0; row < size; row++) {
                    int split = row;
                    if (split > 0) g.fill(bx, by + row, bx + split, by + row + 1, s);
                    if (split < size) g.fill(bx + split, by + row, bx + size, by + row + 1, p);
                }
            }
            case THIRDS_H -> {
                int t1 = size / 3, t2 = size * 2 / 3;
                g.fill(bx, by, bx + size, by + t1, p);
                g.fill(bx, by + t1, bx + size, by + t2, s);
                g.fill(bx, by + t2, bx + size, by + size, p);
            }
            case THIRDS_V -> {
                int t1 = size / 3, t2 = size * 2 / 3;
                g.fill(bx, by, bx + t1, by + size, p);
                g.fill(bx + t1, by, bx + t2, by + size, s);
                g.fill(bx + t2, by, bx + size, by + size, p);
            }
            case CIRCLE_PRI -> renderMiniCircle(g, bx, by, size, primaryColor, secondaryColor);
            case STRIPES -> {
                int total = 2 * size;
                for (int row = 0; row < size; row++) {
                    int b1 = total / 3 - row;
                    int b2 = total * 2 / 3 - row;
                    int s1 = Math.max(0, Math.min(b1, size));
                    int s2 = Math.max(0, Math.min(b2, size));
                    if (s1 > 0) g.fill(bx, by + row, bx + s1, by + row + 1, p);
                    if (s2 > s1) g.fill(bx + s1, by + row, bx + s2, by + row + 1, s);
                    if (size > s2) g.fill(bx + s2, by + row, bx + size, by + row + 1, p);
                }
            }
            case CHECKER -> {
                int half = size / 2;
                g.fill(bx, by, bx + half, by + half, p);
                g.fill(bx + half, by, bx + size, by + half, s);
                g.fill(bx, by + half, bx + half, by + size, s);
                g.fill(bx + half, by + half, bx + size, by + size, p);
            }
            case DIAMOND -> {
                float scale = 0.78f;
                float halfH = (size / 2f) * scale;
                for (int row = 0; row < size; row++) {
                    float distY = Math.abs(row - size / 2f);
                    if (distY >= halfH) {
                        g.fill(bx, by + row, bx + size, by + row + 1, p);
                        continue;
                    }
                    float halfW = (1f - distY / halfH) * (size / 2f) * scale;
                    int x1 = (int) (size / 2f - halfW);
                    int x2 = (int) (size / 2f + halfW);
                    if (x1 > 0) g.fill(bx, by + row, bx + x1, by + row + 1, p);
                    if (x2 > x1) g.fill(bx + x1, by + row, bx + Math.min(x2, size), by + row + 1, s);
                    if (x2 < size) g.fill(bx + Math.max(x2, 0), by + row, bx + size, by + row + 1, p);
                }
            }
        }
    }

    private void renderFlagCircle(GuiGraphics g, int fx, int fy, int bg, int circle) {
        float cx = ELEM_W / 2f, cy = ELEM_H / 2.5f;
        float rx = Math.min(ELEM_W, ELEM_H) / 3f + 1f;
        float ry = Math.min(ELEM_W, ELEM_H) / 3f;
        for (int y = 0; y < ELEM_H; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= ry) {
                float dx = rx * (float) Math.sqrt(1f - (dy * dy) / (ry * ry));
                int x1 = Math.max(0, (int) (cx - dx));
                int x2 = Math.min(ELEM_W, (int) (cx + dx));
                if (x1 > 0) renderFlagRect(g, fx, fy + y, ELEM_U, ELEM_V + y, x1, 1, bg);
                if (x2 > x1) renderFlagRect(g, fx + x1, fy + y, ELEM_U + x1, ELEM_V + y, x2 - x1, 1, circle);
                if (x2 < ELEM_W) renderFlagRect(g, fx + x2, fy + y, ELEM_U + x2, ELEM_V + y, ELEM_W - x2, 1, bg);
            } else {
                renderFlagRect(g, fx, fy + y, ELEM_U, ELEM_V + y, ELEM_W, 1, bg);
            }
        }
    }

    // ── Rendu du drapeau avec motif ────────────────────────────────────────────
    private void renderFlagWithPattern(GuiGraphics g, int fx, int fy,
                                       int primary, int secondary, OWTeamMosaicPattern pattern) {
        switch (pattern) {
            case GRADIENT_DOWN -> renderFlagGradientY(g, fx, fy, primary, secondary);
            case GRADIENT_RIGHT -> renderFlagGradientX(g, fx, fy, primary, secondary);
            case SPLIT_H -> {
                int half = ELEM_H / 2;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, ELEM_W, half, primary);
                renderFlagRect(g, fx, fy + half, ELEM_U, ELEM_V + half, ELEM_W, ELEM_H - half, secondary);
            }
            case SPLIT_V -> {
                int half = ELEM_W / 2;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, half, ELEM_H, primary);
                renderFlagRect(g, fx + half, fy, ELEM_U + half, ELEM_V, ELEM_W - half, ELEM_H, secondary);
            }
            case DIAGONAL_TL_BR -> renderFlagDiagonal(g, fx, fy, primary, secondary, false);
            case DIAGONAL_TR_BL -> renderFlagDiagonal(g, fx, fy, primary, secondary, true);
            case THIRDS_H -> {
                int t1 = ELEM_H / 3, t2 = ELEM_H * 2 / 3;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, ELEM_W, t1, primary);
                renderFlagRect(g, fx, fy + t1, ELEM_U, ELEM_V + t1, ELEM_W, t2 - t1, secondary);
                renderFlagRect(g, fx, fy + t2, ELEM_U, ELEM_V + t2, ELEM_W, ELEM_H - t2, primary);
            }
            case THIRDS_V -> {
                int t1 = ELEM_W / 3, t2 = ELEM_W * 2 / 3;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, t1, ELEM_H, primary);
                renderFlagRect(g, fx + t1, fy, ELEM_U + t1, ELEM_V, t2 - t1, ELEM_H, secondary);
                renderFlagRect(g, fx + t2, fy, ELEM_U + t2, ELEM_V, ELEM_W - t2, ELEM_H, primary);
            }
            case CIRCLE_PRI -> renderFlagCircle(g, fx, fy, primaryColor, secondaryColor);
            case STRIPES -> {
                int total = ELEM_W + ELEM_H;
                for (int row = 0; row < ELEM_H; row++) {
                    int b1 = total / 3 - row;
                    int b2 = total * 2 / 3 - row;
                    int s1 = Math.max(0, Math.min(b1, ELEM_W));
                    int s2 = Math.max(0, Math.min(b2, ELEM_W));
                    if (s1 > 0) renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, s1, 1, primary);
                    if (s2 > s1) renderFlagRect(g, fx + s1, fy + row, ELEM_U + s1, ELEM_V + row, s2 - s1, 1, secondary);
                    if (ELEM_W > s2)
                        renderFlagRect(g, fx + s2, fy + row, ELEM_U + s2, ELEM_V + row, ELEM_W - s2, 1, primary);
                }
            }
            case CHECKER -> {
                int hw = ELEM_W / 2, hh = ELEM_H / 2;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, hw, hh, primary);
                renderFlagRect(g, fx + hw, fy, ELEM_U + hw, ELEM_V, ELEM_W - hw, hh, secondary);
                renderFlagRect(g, fx, fy + hh, ELEM_U, ELEM_V + hh, hw, ELEM_H - hh, secondary);
                renderFlagRect(g, fx + hw, fy + hh, ELEM_U + hw, ELEM_V + hh, ELEM_W - hw, ELEM_H - hh, primary);
            }
            case DIAMOND -> {
                float scale = 0.78f;
                float halfH = (ELEM_H / 2f) * scale;
                for (int row = 0; row < ELEM_H; row++) {
                    float distY = Math.abs(row - ELEM_H / 2f);
                    if (distY >= halfH) {
                        renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, ELEM_W, 1, primary);
                        continue;
                    }
                    float halfW = (1f - distY / halfH) * (ELEM_W / 2f) * scale;
                    int x1 = (int) (ELEM_W / 2f - halfW);
                    int x2 = (int) (ELEM_W / 2f + halfW);
                    if (x1 > 0) renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, x1, 1, primary);
                    if (x2 > x1) renderFlagRect(g, fx + x1, fy + row, ELEM_U + x1, ELEM_V + row, x2 - x1, 1, secondary);
                    if (x2 < ELEM_W)
                        renderFlagRect(g, fx + x2, fy + row, ELEM_U + x2, ELEM_V + row, ELEM_W - x2, 1, primary);
                }
            }
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderFlagGradientY(GuiGraphics g, int fx, int fy, int topColor, int botColor) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = lerpColor(topColor, botColor, t);
            int y0 = fy + i * ELEM_H / STRIPS;
            int y1 = fy + (i + 1) * ELEM_H / STRIPS;
            int v0 = ELEM_V + i * ELEM_H / STRIPS;
            setFlagColor(col);
            g.blit(OW_TEAMS_LOC, fx, y0, ELEM_U, v0, ELEM_W, y1 - y0);
        }
    }

    private void renderFlagGradientX(GuiGraphics g, int fx, int fy, int leftColor, int rightColor) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = lerpColor(leftColor, rightColor, t);
            int x0 = fx + i * ELEM_W / STRIPS;
            int x1 = fx + (i + 1) * ELEM_W / STRIPS;
            int u0 = ELEM_U + i * ELEM_W / STRIPS;
            setFlagColor(col);
            g.blit(OW_TEAMS_LOC, x0, fy, u0, ELEM_V, x1 - x0, ELEM_H);
        }
    }

    private void renderFlagDiagonal(GuiGraphics g, int fx, int fy,
                                    int primary, int secondary, boolean mirrored) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            int y0 = fy + i * ELEM_H / STRIPS;
            int stripH = fy + (i + 1) * ELEM_H / STRIPS - y0;
            int v0 = ELEM_V + i * ELEM_H / STRIPS;
            int splitW = ELEM_W * i / STRIPS;
            int priW = ELEM_W - splitW;
            if (!mirrored) {
                if (priW > 0) {
                    setFlagColor(primary);
                    g.blit(OW_TEAMS_LOC, fx, y0, ELEM_U, v0, priW, stripH);
                }
                if (splitW > 0) {
                    setFlagColor(secondary);
                    g.blit(OW_TEAMS_LOC, fx + priW, y0, ELEM_U + priW, v0, splitW, stripH);
                }
            } else {
                if (splitW > 0) {
                    setFlagColor(secondary);
                    g.blit(OW_TEAMS_LOC, fx, y0, ELEM_U, v0, splitW, stripH);
                }
                if (priW > 0) {
                    setFlagColor(primary);
                    g.blit(OW_TEAMS_LOC, fx + splitW, y0, ELEM_U + splitW, v0, priW, stripH);
                }
            }
        }
    }

    private void renderFlagRect(GuiGraphics g, int sx, int sy, int u, int v, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        setFlagColor(color);
        g.blit(OW_TEAMS_LOC, sx, sy, u, v, w, h);
    }

    private void setFlagColor(int hex) {
        RenderSystem.setShaderColor(
                ((hex >> 16) & 0xFF) / 255f,
                ((hex >> 8) & 0xFF) / 255f,
                (hex & 0xFF) / 255f,
                1f);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────
    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int hsvToRgb(float h, float s, float v) {
        if (s == 0f) {
            int c = (int) (v * 255f);
            return (c << 16) | (c << 8) | c;
        }
        float hh = ((h % 1f) + 1f) % 1f * 6f;
        int i = (int) hh;
        float f = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        float r, gr, b;
        switch (i % 6) {
            case 0:
                r = v;
                gr = t;
                b = p;
                break;
            case 1:
                r = q;
                gr = v;
                b = p;
                break;
            case 2:
                r = p;
                gr = v;
                b = t;
                break;
            case 3:
                r = p;
                gr = q;
                b = v;
                break;
            case 4:
                r = t;
                gr = p;
                b = v;
                break;
            default:
                r = v;
                gr = p;
                b = q;
                break;
        }
        return ((int) (r * 255f) << 16) | ((int) (gr * 255f) << 8) | (int) (b * 255f);
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}