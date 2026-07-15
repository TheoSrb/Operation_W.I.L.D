package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.CreateTribePacket;
import net.tiew.operationWild.networking.packets.to_server.UpdateTribeBannerPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Étape 2 : éditeur de couleurs / motifs / peinture (panneau gris). Reprend l'éditeur de peinture
 * complet de l'ancien écran (panneau latéral : pinceau, miroirs X/Y, grille, undo, barre de taille).
 */
public class OWTribeCreationScreen extends OWTribeScreen {

    private static final int W = OWBannerRenderer.W, H = OWBannerRenderer.H; // 55 × 93
    private static final int NAME_Y = 20, NAME_H = 12;
    private static final int PREV_X = 8, PREV_Y = 34;
    private static final int CP_X = 76;
    private static final int TAB_Y = 34, TAB_H = 12;
    private static final int HUE_Y = 50, HUE_H = 10;
    private static final int SV_Y = 63, SV_H = 50;
    private static final int SWATCH_Y = 116, SWATCH_SIZE = 8, SWATCH_GAP = 1;
    private static final int PAT_Y = 130, PAT_SIZE = 12, PAT_GAP = 1;
    private static final int ACT_Y = 146;

    // ── Panneau latéral de peinture (identique à l'ancien écran) ────────────────
    private static final int SP_GAP = 4, SP_W = 24, SP_BTN_W = 20, SP_BTN_H = 12, SP_BTN_X = 2;
    private static final int SP_Y_BRUSH = 4;
    private static final int SP_Y_MIRX = SP_Y_BRUSH + SP_BTN_H + 4;
    private static final int SP_Y_MIRY = SP_Y_MIRX + SP_BTN_H + 4;
    private static final int SP_Y_GRID = SP_Y_MIRY + SP_BTN_H + 4;
    private static final int SP_Y_UNDO = SP_Y_GRID + SP_BTN_H + 4;
    private static final int SP_Y_VBAR = SP_Y_UNDO + SP_BTN_H + 4;
    private static final int SP_VBAR_H = IMG_H - SP_Y_VBAR - 4;

    private static final int BRUSH_MIN = 1, BRUSH_MAX = 8, PAINT_COUNT = W * H;
    private static final int MAX_UNDO = 30;

    private enum BrushShape {
        ROUND("owteams.paint.brush.round"),
        SQUARE("owteams.paint.brush.square"),
        DIAMOND("owteams.paint.brush.diamond");
        private final String key;
        BrushShape(String key) { this.key = key; }
        String label() { return Component.translatable(key).getString(); }
    }

    private static final int[] GRID_SIZES = {1, 2, 4, 8, 16};
    private static final String[] GRID_LABELS = {"1", "2", "4", "8", "16"};

    private final OWTeamBannerShape bannerShape;
    private final boolean editMode;
    private int cpW;

    private EditBox nameBox;
    private Button primTabBtn, secTabBtn, confirmBtn, cancelBtn, clearBtn;

    private int primaryColor = 0xD12020, secondaryColor = 0x2050D1;
    private float primH = 0f, primS = 1f, primV = 0.82f;
    private float secH = 0.62f, secS = 1f, secV = 0.82f;
    private boolean editingPrimary = true;

    private OWTeamMosaicPattern selectedPattern = OWTeamMosaicPattern.GRADIENT_DOWN;
    private final List<OWTeamMosaicPattern> patterns = new ArrayList<>();

    private boolean[] paintPixels = new boolean[PAINT_COUNT];
    private int brushSize = 3;
    private BrushShape brushShape = BrushShape.ROUND;
    private boolean mirrorX = false, mirrorY = false;
    private int gridSize = 1;
    private boolean showBrushMenu = false, showGridMenu = false;

    private boolean draggingCanvas, draggingVBar, draggingHue, draggingSV;
    private int lastPaintCX = Integer.MIN_VALUE, lastPaintCY = Integer.MIN_VALUE;
    private final ArrayDeque<boolean[]> undoStack = new ArrayDeque<>();
    private boolean[] snapshotBeforeStroke = null;
    private boolean ctrlZWasDown = false;

    public OWTribeCreationScreen(OWTeamBannerShape shape, boolean editMode) {
        super(Component.translatable("owteams.creation.title"));
        this.bannerShape = shape != null ? shape : OWTeamBannerShape.CLASSIC;
        this.editMode = editMode;
        primaryColor = hsvToRgb(primH, primS, primV);
        secondaryColor = hsvToRgb(secH, secS, secV);
        for (OWTeamMosaicPattern p : OWTeamMosaicPattern.values()) if (!p.isUnlockable()) patterns.add(p);
        if (editMode) {
            OWTeam t = OWClientTribeData.get();
            if (t != null) {
                primaryColor = t.getTeamColor();
                secondaryColor = t.getTeamSecondaryColor();
                selectedPattern = t.getTeamMosaicPattern();
                if (t.getPaintPixels() != null && t.getPaintPixels().length >= PAINT_COUNT) paintPixels = t.getPaintPixels().clone();
                float[] ph = rgbToHsv(primaryColor); primH = ph[0]; primS = ph[1]; primV = ph[2];
                float[] sh = rgbToHsv(secondaryColor); secH = sh[0]; secS = sh[1]; secV = sh[2];
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.cpW = IMG_W - CP_X - 8;

        nameBox = new EditBox(this.font, leftPos + 8, topPos + NAME_Y, IMG_W - 16, NAME_H,
                Component.translatable("owteams.creation.name_placeholder"));
        nameBox.setMaxLength(15);
        String def = Minecraft.getInstance().player != null
                ? Component.translatable("owteams.creation.default_name",
                        Minecraft.getInstance().player.getName().getString()).getString() : "Tribu";
        if (def.length() > 15) def = def.substring(0, 15);
        nameBox.setValue(def);
        if (!editMode) this.addRenderableWidget(nameBox);

        primTabBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.creation.primary"),
                b -> { editingPrimary = true; refreshTabs(); })
                .bounds(leftPos + CP_X, topPos + TAB_Y, cpW / 2 - 2, TAB_H).build());
        secTabBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.creation.secondary"),
                b -> { editingPrimary = false; refreshTabs(); })
                .bounds(leftPos + CP_X + cpW / 2 + 2, topPos + TAB_Y, cpW / 2 - 2, TAB_H).build());
        refreshTabs();

        confirmBtn = addRenderableWidget(Button.builder(
                Component.translatable(editMode ? "owteams.creation.confirm_edit" : "owteams.creation.confirm"),
                b -> onConfirm()).bounds(leftPos + 8, topPos + ACT_Y, 66, 16).build());
        clearBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.paint.clear"),
                b -> { pushUndoFull(); Arrays.fill(paintPixels, false); })
                .bounds(leftPos + 78, topPos + ACT_Y, 38, 16).build());
        cancelBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.creation.cancel"),
                b -> Minecraft.getInstance().setScreen(new OWBannerShapeSelectScreen(editMode)))
                .bounds(leftPos + IMG_W - 8 - 58, topPos + ACT_Y, 58, 16).build());
    }

    private void refreshTabs() { primTabBtn.active = !editingPrimary; secTabBtn.active = editingPrimary; }
    private boolean isPaintMode() { return selectedPattern == OWTeamMosaicPattern.CUSTOM_PAINT; }

    private void onConfirm() {
        byte[] packed = isPaintMode() ? OWTeamMosaicPattern.packPixels(paintPixels) : new byte[0];
        if (editMode) {
            OWNetworkHandler.sendToServer(new UpdateTribeBannerPacket(
                    primaryColor, secondaryColor, selectedPattern.getId(), bannerShape.getId(), packed));
            Minecraft.getInstance().setScreen(new OWTribeDashboardScreen());
        } else {
            OWNetworkHandler.sendToServer(new CreateTribePacket(
                    nameBox.getValue().trim(), primaryColor, secondaryColor,
                    selectedPattern.getId(), bannerShape.getId(), packed));
            Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
        }
    }

    // ── Zones ───────────────────────────────────────────────────────────────────
    private boolean inPreview(double mx, double my) {
        return mx >= leftPos + PREV_X && mx < leftPos + PREV_X + W && my >= topPos + PREV_Y && my < topPos + PREV_Y + H;
    }
    private boolean inHue(double mx, double my) {
        return mx >= leftPos + CP_X && mx < leftPos + CP_X + cpW && my >= topPos + HUE_Y && my < topPos + HUE_Y + HUE_H;
    }
    private boolean inSV(double mx, double my) {
        return mx >= leftPos + CP_X && mx < leftPos + CP_X + cpW && my >= topPos + SV_Y && my < topPos + SV_Y + SV_H;
    }
    private boolean inSidePanel(double mx, double my) {
        int spx = leftPos + IMG_W + SP_GAP;
        return mx >= spx && mx < spx + SP_W && my >= topPos && my < topPos + IMG_H;
    }
    private boolean inSPBtn(double mx, double my, int bx, int by) {
        return mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
    }
    private boolean inVBar(double mx, double my) {
        int bx = leftPos + IMG_W + SP_GAP + SP_BTN_X, by = topPos + SP_Y_VBAR;
        return mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_VBAR_H;
    }
    private int ddX() { return leftPos + IMG_W + SP_GAP - 56; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if ((showBrushMenu || showGridMenu) && !inAnyDropdown(mx, my) && !inSidePanel(mx, my)) {
            showBrushMenu = false; showGridMenu = false;
        }
        if (isPaintMode() && inSidePanel(mx, my)) {
            int bx = leftPos + IMG_W + SP_GAP + SP_BTN_X;
            if (inSPBtn(mx, my, bx, topPos + SP_Y_BRUSH)) { showBrushMenu = !showBrushMenu; showGridMenu = false; return true; }
            if (inSPBtn(mx, my, bx, topPos + SP_Y_MIRX)) { mirrorX = !mirrorX; return true; }
            if (inSPBtn(mx, my, bx, topPos + SP_Y_MIRY)) { mirrorY = !mirrorY; return true; }
            if (inSPBtn(mx, my, bx, topPos + SP_Y_GRID)) { showGridMenu = !showGridMenu; showBrushMenu = false; return true; }
            if (inSPBtn(mx, my, bx, topPos + SP_Y_UNDO)) { performUndo(); return true; }
            if (inVBar(mx, my)) { draggingVBar = true; applyVBarPick(my); return true; }
            return true;
        }
        if (showBrushMenu) {
            int r = getBrushMenuClick(mx, my);
            if (r >= 0) { brushShape = BrushShape.values()[r]; showBrushMenu = false; return true; }
        }
        if (showGridMenu) {
            int r = getGridMenuClick(mx, my);
            if (r >= 0) { gridSize = GRID_SIZES[r]; showGridMenu = false; return true; }
        }
        // Sélection de motif
        int px = leftPos + 8;
        for (int i = 0; i < patterns.size(); i++) {
            int bx = px + i * (PAT_SIZE + PAT_GAP);
            if (mx >= bx && mx < bx + PAT_SIZE && my >= topPos + PAT_Y && my < topPos + PAT_Y + PAT_SIZE) {
                selectedPattern = patterns.get(i); return true;
            }
        }
        if (isPaintMode() && inPreview(mx, my)) {
            lastPaintCX = Integer.MIN_VALUE; lastPaintCY = Integer.MIN_VALUE;
            snapshotBeforeStroke = paintPixels.clone(); draggingCanvas = true; applyBrushAt(mx, my); return true;
        }
        if (inHue(mx, my)) { draggingHue = true; pickHue(mx); return true; }
        if (inSV(mx, my)) { draggingSV = true; pickSV(mx, my); return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingVBar) { applyVBarPick(my); return true; }
        if (draggingCanvas) { applyBrushAt(mx, my); return true; }
        if (draggingHue) { pickHue(mx); return true; }
        if (draggingSV) { pickSV(mx, my); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingCanvas && snapshotBeforeStroke != null) {
            if (undoStack.size() >= MAX_UNDO) undoStack.pollLast();
            undoStack.push(snapshotBeforeStroke); snapshotBeforeStroke = null;
        }
        draggingCanvas = draggingVBar = draggingHue = draggingSV = false;
        lastPaintCX = Integer.MIN_VALUE; lastPaintCY = Integer.MIN_VALUE;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (isPaintMode()) {
            brushSize = Math.max(BRUSH_MIN, Math.min(BRUSH_MAX, brushSize + (int) Math.signum(sy)));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    // ── Peinture (miroir + grille), identique à l'ancien écran ──────────────────
    private int mirX(int px) {
        if (gridSize > 1) { int m = W - 1 - px; return Math.floorDiv(m, gridSize) * gridSize + gridSize / 2; }
        return W - px;
    }
    private int mirY(int py) {
        if (gridSize > 1) { int m = H - 1 - py; return Math.floorDiv(m, gridSize) * gridSize + gridSize / 2; }
        return H - 1 - py;
    }

    private void applyBrushAt(double mx, double my) {
        int cx = (int) (mx - (leftPos + PREV_X));
        int cy = (int) (my - (topPos + PREV_Y));
        boolean sec = !editingPrimary;
        if (gridSize > 1) {
            int ciX0 = lastPaintCX == Integer.MIN_VALUE ? Math.floorDiv(cx, gridSize) : Math.floorDiv(lastPaintCX, gridSize);
            int ciY0 = lastPaintCY == Integer.MIN_VALUE ? Math.floorDiv(cy, gridSize) : Math.floorDiv(lastPaintCY, gridSize);
            int ciX1 = Math.floorDiv(cx, gridSize), ciY1 = Math.floorDiv(cy, gridSize);
            int steps = (lastPaintCX == Integer.MIN_VALUE) ? 0
                    : Math.max(1, Math.max(Math.abs(ciX1 - ciX0), Math.abs(ciY1 - ciY0)));
            for (int i = (steps == 0 ? 0 : 1); i <= steps; i++) {
                float t = (steps == 0) ? 1f : (float) i / steps;
                int iCX = Math.round(ciX0 + (ciX1 - ciX0) * t), iCY = Math.round(ciY0 + (ciY1 - ciY0) * t);
                int px = iCX * gridSize + gridSize / 2, py = iCY * gridSize + gridSize / 2;
                paintBrushAt(px, py, sec);
                if (mirrorX) paintBrushAt(mirX(px), py, sec);
                if (mirrorY) paintBrushAt(px, mirY(py), sec);
                if (mirrorX && mirrorY) paintBrushAt(mirX(px), mirY(py), sec);
            }
        } else {
            if (lastPaintCX != Integer.MIN_VALUE) {
                int steps = Math.max(1, Math.max(Math.abs(cx - lastPaintCX), Math.abs(cy - lastPaintCY)));
                float fdx = cx - lastPaintCX, fdy = cy - lastPaintCY;
                for (int i = 1; i <= steps; i++) {
                    float t = (float) i / steps;
                    int ix = Math.round(lastPaintCX + fdx * t), iy = Math.round(lastPaintCY + fdy * t);
                    paintBrushAt(ix, iy, sec);
                    if (mirrorX) paintBrushAt(W - ix, iy, sec);
                    if (mirrorY) paintBrushAt(ix, H - 1 - iy, sec);
                    if (mirrorX && mirrorY) paintBrushAt(W - ix, H - 1 - iy, sec);
                }
            } else {
                paintBrushAt(cx, cy, sec);
                if (mirrorX) paintBrushAt(mirX(cx), cy, sec);
                if (mirrorY) paintBrushAt(cx, mirY(cy), sec);
                if (mirrorX && mirrorY) paintBrushAt(mirX(cx), mirY(cy), sec);
            }
        }
        lastPaintCX = cx; lastPaintCY = cy;
    }

    private void paintBrushAt(int cx, int cy, boolean useSec) {
        int r = brushSize;
        if (gridSize > 1) {
            for (int dci = -r; dci <= r; dci++) for (int dcj = -r; dcj <= r; dcj++)
                if (insideBrush(dci, dcj, r)) setPixel(cx + dci * gridSize, cy + dcj * gridSize, useSec);
        } else {
            for (int dy = -r; dy <= r; dy++) for (int dx = -r; dx <= r; dx++)
                if (insideBrush(dx, dy, r)) setPixel(cx + dx, cy + dy, useSec);
        }
    }
    private boolean insideBrush(int dx, int dy, int r) {
        return switch (brushShape) {
            case ROUND -> dx * dx + dy * dy <= r * r;
            case SQUARE -> true;
            case DIAMOND -> Math.abs(dx) + Math.abs(dy) <= r;
        };
    }
    private void setPixel(int px, int py, boolean useSec) {
        if (gridSize > 1) {
            int cellX = Math.floorDiv(px, gridSize) * gridSize, cellY = Math.floorDiv(py, gridSize) * gridSize;
            for (int gy = 0; gy < gridSize; gy++) for (int gx = 0; gx < gridSize; gx++) {
                int fpx = cellX + gx, fpy = cellY + gy;
                if (fpx >= 0 && fpx < W && fpy >= 0 && fpy < H) paintPixels[fpy * W + fpx] = useSec;
            }
        } else {
            if (px >= 0 && px < W && py >= 0 && py < H) paintPixels[py * W + px] = useSec;
        }
    }

    private void applyVBarPick(double my) {
        int by = topPos + SP_Y_VBAR;
        float ratio = (float) Math.max(0, Math.min(1, (my - by) / (double) SP_VBAR_H));
        brushSize = BRUSH_MAX - Math.round(ratio * (BRUSH_MAX - BRUSH_MIN));
    }
    private void performUndo() { if (!undoStack.isEmpty()) paintPixels = undoStack.pop(); }
    private void pushUndoFull() {
        if (undoStack.size() >= MAX_UNDO) undoStack.pollLast();
        undoStack.push(paintPixels.clone());
    }

    private boolean inAnyDropdown(double mx, double my) {
        if (showBrushMenu) {
            int ddy = topPos + SP_Y_BRUSH;
            if (mx >= ddX() && mx < ddX() + 54 && my >= ddy && my < ddy + BrushShape.values().length * 14 + 2) return true;
        }
        if (showGridMenu) {
            int ddy = topPos + SP_Y_GRID;
            if (mx >= ddX() && mx < ddX() + 54 && my >= ddy && my < ddy + GRID_SIZES.length * 14 + 2) return true;
        }
        return false;
    }
    private int getBrushMenuClick(double mx, double my) {
        int ddx = ddX(), ddy = topPos + SP_Y_BRUSH;
        for (int i = 0; i < BrushShape.values().length; i++)
            if (mx >= ddx && mx < ddx + 54 && my >= ddy + 1 + i * 14 && my < ddy + 1 + i * 14 + 12) return i;
        return -1;
    }
    private int getGridMenuClick(double mx, double my) {
        int ddx = ddX(), ddy = topPos + SP_Y_GRID;
        for (int i = 0; i < GRID_SIZES.length; i++)
            if (mx >= ddx && mx < ddx + 54 && my >= ddy + 1 + i * 14 && my < ddy + 1 + i * 14 + 12) return i;
        return -1;
    }

    private void pickHue(double mx) {
        float h = clamp01((mx - (leftPos + CP_X)) / cpW);
        if (editingPrimary) { primH = h; primaryColor = hsvToRgb(primH, primS, primV); }
        else { secH = h; secondaryColor = hsvToRgb(secH, secS, secV); }
    }
    private void pickSV(double mx, double my) {
        float s = clamp01((mx - (leftPos + CP_X)) / cpW), v = clamp01(1.0 - (my - (topPos + SV_Y)) / SV_H);
        if (editingPrimary) { primS = s; primV = v; primaryColor = hsvToRgb(primH, primS, primV); }
        else { secS = s; secV = v; secondaryColor = hsvToRgb(secH, secS, secV); }
    }

    // ── Rendu ─────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        if (isPaintMode()) {
            long win = Minecraft.getInstance().getWindow().getWindow();
            boolean ctrl = InputConstants.isKeyDown(win, 341) || InputConstants.isKeyDown(win, 345)
                    || InputConstants.isKeyDown(win, 343) || InputConstants.isKeyDown(win, 347);
            boolean z = InputConstants.isKeyDown(win, 90) || InputConstants.isKeyDown(win, 87)
                    || InputConstants.isKeyDown(win, 89);
            if (ctrl && z && !ctrlZWasDown && !undoStack.isEmpty()) paintPixels = undoStack.pop();
            ctrlZWasDown = ctrl && z;
        } else ctrlZWasDown = false;

        clearBtn.visible = isPaintMode();

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable(editMode ? "owteams.creation.title_edit" : "owteams.creation.title"));

        if (!editMode) {
            g.drawString(this.font, Component.translatable("owteams.creation.name_label"),
                    leftPos + 8, topPos + NAME_Y - 9, 0x404040, false);
        }

        // Preview bannière (forme choisie)
        OWBannerRenderer.render(g, leftPos + PREV_X, topPos + PREV_Y, bannerShape,
                primaryColor, secondaryColor, selectedPattern, paintPixels);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        renderColorPicker(g);
        renderPatternSelector(g, mouseX, mouseY);

        // Widgets vanilla (boutons, EditBox) au-dessus du contenu manuel
        super.render(g, mouseX, mouseY, partial);

        // Panneau latéral de peinture + overlays (au-dessus des widgets)
        if (isPaintMode()) {
            renderGridOverlay(g);
            renderMirrorGuides(g);
            renderSidePanel(g, mouseX, mouseY);
            if (inPreview(mouseX, mouseY)) renderBrushCursor(g, mouseX, mouseY);
            if (showBrushMenu) renderBrushShapeDropdown(g, mouseX, mouseY);
            if (showGridMenu) renderGridSizeDropdown(g, mouseX, mouseY);
        }
    }

    private void renderColorPicker(GuiGraphics g) {
        int cx = leftPos + CP_X, cyHue = topPos + HUE_Y;
        final int STEPS = 60;
        for (int i = 0; i < STEPS; i++) {
            int x0 = cx + i * cpW / STEPS, x1 = cx + (i + 1) * cpW / STEPS;
            g.fill(x0, cyHue, x1, cyHue + HUE_H, 0xFF000000 | hsvToRgb((float) i / STEPS, 1f, 1f));
        }
        drawPaletteBevel(g, cx, cyHue, cpW, HUE_H);
        float curH = editingPrimary ? primH : secH;
        int hx = cx + (int) (curH * cpW);
        g.fill(hx - 1, cyHue - 1, hx + 1, cyHue + HUE_H + 1, 0xFFFFFFFF);

        int svY = topPos + SV_Y;
        for (int yy = 0; yy < SV_H; yy++) {
            float v = 1f - (float) yy / SV_H;
            for (int xx = 0; xx < cpW; xx += 2) {
                float s = (float) xx / cpW;
                g.fill(cx + xx, svY + yy, cx + Math.min(cpW, xx + 2), svY + yy + 1, 0xFF000000 | hsvToRgb(curH, s, v));
            }
        }
        drawPaletteBevel(g, cx, svY, cpW, SV_H);
        float curS = editingPrimary ? primS : secS, curV = editingPrimary ? primV : secV;
        int sx = cx + (int) (curS * cpW), sy = svY + (int) ((1f - curV) * SV_H);
        g.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFFFFFFFF);
        g.fill(sx, sy, sx + 1, sy + 1, 0xFF000000);

        // Pastilles carrées (couleur 1 / 2) avec bordure noire
        int swY = topPos + SWATCH_Y;
        drawSwatch(g, cx, swY, primaryColor, editingPrimary);
        drawSwatch(g, cx + SWATCH_SIZE + SWATCH_GAP, swY, secondaryColor, !editingPrimary);
    }

    private void drawSwatch(GuiGraphics g, int x, int y, int color, boolean active) {
        g.fill(x, y, x + SWATCH_SIZE, y + SWATCH_SIZE, 0xFF000000 | color);
        drawPaletteBevel(g, x, y, SWATCH_SIZE, SWATCH_SIZE);
    }

    /** Bordure biseautée : haut blanc, gauche/droite/bas noirs. */
    private void drawPaletteBevel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 1, 0xFFFFFFFF);              // haut : blanc
        g.fill(x, y + h - 1, x + w, y + h, 0xFF000000);      // bas : noir
        g.fill(x, y, x + 1, y + h, 0xFF000000);              // gauche : noir
        g.fill(x + w - 1, y, x + w, y + h, 0xFF000000);      // droite : noir
    }

    private void renderPatternSelector(GuiGraphics g, int mouseX, int mouseY) {
        int px = leftPos + 8, by = topPos + PAT_Y;
        OWTeamMosaicPattern hovered = null;
        for (int i = 0; i < patterns.size(); i++) {
            int bx = px + i * (PAT_SIZE + PAT_GAP);
            OWTeamMosaicPattern pat = patterns.get(i);
            boolean sel = pat == selectedPattern;
            boolean hov = mouseX >= bx && mouseX < bx + PAT_SIZE && mouseY >= by && mouseY < by + PAT_SIZE;
            if (hov) hovered = pat;
            g.fill(bx, by, bx + PAT_SIZE, by + PAT_SIZE, sel ? 0xFF666666 : (hov ? 0xFF3A3A3A : 0xFF1E1E1E));
            renderMiniPattern(g, bx + 1, by + 1, PAT_SIZE - 2, pat);
            drawBorder(g, bx, by, PAT_SIZE, PAT_SIZE, sel ? 0xFFFFD700 : 0xFF000000);
        }
        if (hovered != null) {
            String label = Component.translatable(hovered.getDisplayName()).getString();
            int ttW = this.font.width(label) + 6, ttX = mouseX + 6, ttY = mouseY - 14;
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.fill(ttX - 1, ttY - 1, ttX + ttW + 1, ttY + 10, 0xFF000000);
            g.drawString(this.font, label, ttX + 2, ttY + 1, 0xFFFFFF, false);
            g.pose().popPose();
        }
    }

    // ── Mini aperçu d'un motif (indépendant de toute texture) ───────────────────
    private void renderMiniPattern(GuiGraphics g, int bx, int by, int size, OWTeamMosaicPattern pattern) {
        int p = primaryColor | 0xFF000000, s = secondaryColor | 0xFF000000;
        int strips = Math.max(1, size / 3);
        switch (pattern) {
            case GRADIENT_DOWN -> { for (int i = 0; i < strips; i++) { float t = (float) i / Math.max(1, strips - 1);
                int col = 0xFF000000 | lerpColor(primaryColor, secondaryColor, t);
                g.fill(bx, by + i * size / strips, bx + size, by + (i + 1) * size / strips, col); } }
            case GRADIENT_RIGHT -> { for (int i = 0; i < strips; i++) { float t = (float) i / Math.max(1, strips - 1);
                int col = 0xFF000000 | lerpColor(primaryColor, secondaryColor, t);
                g.fill(bx + i * size / strips, by, bx + (i + 1) * size / strips, by + size, col); } }
            case SPLIT_H -> { int half = size / 2; g.fill(bx, by, bx + size, by + half, p); g.fill(bx, by + half, bx + size, by + size, s); }
            case SPLIT_V -> { int half = size / 2; g.fill(bx, by, bx + half, by + size, p); g.fill(bx + half, by, bx + size, by + size, s); }
            case DIAGONAL_TL_BR -> { for (int row = 0; row < size; row++) { int split = size - row;
                if (split > 0) g.fill(bx, by + row, bx + Math.min(split, size), by + row + 1, p);
                if (split < size) g.fill(bx + Math.max(split, 0), by + row, bx + size, by + row + 1, s); } }
            case THIRDS_H -> { int t1 = Math.round(size / 3f), t2 = size - t1;
                g.fill(bx, by, bx + size, by + t1, p); g.fill(bx, by + t1, bx + size, by + t2, s); g.fill(bx, by + t2, bx + size, by + size, p); }
            case THIRDS_V -> { int t1 = Math.round(size / 3f), t2 = size - t1;
                g.fill(bx, by, bx + t1, by + size, p); g.fill(bx + t1, by, bx + t2, by + size, s); g.fill(bx + t2, by, bx + size, by + size, p); }
            case CIRCLE_PRI -> renderMiniCircle(g, bx, by, size, primaryColor, secondaryColor);
            case STRIPES -> { int total = 2 * size; for (int row = 0; row < size; row++) {
                int b1 = total / 3 - row, b2 = total * 2 / 3 - row;
                int s1 = Math.max(0, Math.min(b1, size)), s2 = Math.max(0, Math.min(b2, size));
                if (s1 > 0) g.fill(bx, by + row, bx + s1, by + row + 1, p);
                if (s2 > s1) g.fill(bx + s1, by + row, bx + s2, by + row + 1, s);
                if (size > s2) g.fill(bx + s2, by + row, bx + size, by + row + 1, p); } }
            case CHECKER -> { int half = size / 2;
                g.fill(bx, by, bx + half, by + half, p); g.fill(bx + half, by, bx + size, by + half, s);
                g.fill(bx, by + half, bx + half, by + size, s); g.fill(bx + half, by + half, bx + size, by + size, p); }
            case DIAMOND -> { float scale = 0.78f; float halfH = (size / 2f) * scale;
                for (int row = 0; row < size; row++) { float distY = Math.abs(row - size / 2f);
                    if (distY >= halfH) { g.fill(bx, by + row, bx + size, by + row + 1, p); continue; }
                    float halfW = (1f - distY / halfH) * (size / 2f) * scale;
                    int x1 = (int) (size / 2f - halfW), x2 = (int) (size / 2f + halfW);
                    if (x1 > 0) g.fill(bx, by + row, bx + x1, by + row + 1, p);
                    if (x2 > x1) g.fill(bx + x1, by + row, bx + Math.min(x2, size), by + row + 1, s);
                    if (x2 < size) g.fill(bx + Math.max(x2, 0), by + row, bx + size, by + row + 1, p); } }
            case CUSTOM_PAINT -> { for (int py = 0; py < size; py++) for (int px = 0; px < size; px++) {
                int oPx = px * W / size, oPy = py * H / size;
                g.fill(bx + px, by + py, bx + px + 1, by + py + 1, paintPixels[oPy * W + oPx] ? s : p); } }
            default -> g.fill(bx, by, bx + size, by + size, p);
        }
    }
    private void renderMiniCircle(GuiGraphics g, int bx, int by, int size, int bg, int circle) {
        float cx = size / 2f, cy = size / 2f, r = size / 3f;
        for (int y = 0; y < size; y++) { float dy = y - cy;
            if (Math.abs(dy) <= r) { float dx = (float) Math.sqrt(r * r - dy * dy);
                int x1 = Math.max(0, (int) (cx - dx)), x2 = Math.min(size, (int) (cx + dx));
                if (x1 > 0) g.fill(bx, by + y, bx + x1, by + y + 1, bg | 0xFF000000);
                if (x2 > x1) g.fill(bx + x1, by + y, bx + x2, by + y + 1, circle | 0xFF000000);
                if (x2 < size) g.fill(bx + x2, by + y, bx + size, by + y + 1, bg | 0xFF000000);
            } else g.fill(bx, by + y, bx + size, by + y + 1, bg | 0xFF000000); }
    }

    // ── Panneau latéral (pinceau / miroirs / grille / undo / barre) ─────────────
    private void renderGridOverlay(GuiGraphics g) {
        if (gridSize <= 1) return;
        int fx = leftPos + PREV_X, fy = topPos + PREV_Y, col = 0x33FFFFFF;
        for (int gx = 0; gx <= W; gx += gridSize) g.fill(fx + gx, fy, fx + gx + 1, fy + H, col);
        for (int gy = 0; gy <= H; gy += gridSize) g.fill(fx, fy + gy, fx + W, fy + gy + 1, col);
    }
    private void renderMirrorGuides(GuiGraphics g) {
        int fx = leftPos + PREV_X, fy = topPos + PREV_Y;
        if (mirrorX) { int midX = fx + mirrorAxisX(); for (int y = 0; y < H; y += 4) g.fill(midX, fy + y, midX + 1, fy + y + 2, 0xCCFFFF44); }
        if (mirrorY) { int midY = fy + mirrorAxisY(); for (int x = 0; x < W; x += 4) g.fill(fx + x, midY, fx + x + 2, midY + 1, 0xCCFFFF44); }
    }
    private int mirrorAxisX() {
        if (gridSize <= 1) return (W - 1) / 2 + 1;
        int nc = Math.floorDiv((W - 1) / 2, gridSize) * gridSize + gridSize / 2;
        int mc = Math.floorDiv(W - 1 - nc, gridSize) * gridSize + gridSize / 2; return (nc + mc) / 2;
    }
    private int mirrorAxisY() {
        if (gridSize <= 1) return (H - 1) / 2;
        int nc = Math.floorDiv((H - 1) / 2, gridSize) * gridSize + gridSize / 2;
        int mc = Math.floorDiv(H - 1 - nc, gridSize) * gridSize + gridSize / 2; return (nc + mc) / 2;
    }

    private void renderSidePanel(GuiGraphics g, int mouseX, int mouseY) {
        int spx = leftPos + IMG_W + SP_GAP, spy = topPos, bx = spx + SP_BTN_X;
        g.fill(spx, spy, spx + SP_W, spy + IMG_H, 0xDD1A1A1A);
        drawBorder(g, spx, spy, SP_W, IMG_H, 0xFF444444);

        spHamburger(g, bx, spy + SP_Y_BRUSH, showBrushMenu, mouseX, mouseY);
        hamburgerLines(g, bx + 2, spy + SP_Y_BRUSH + 2, 9);
        shapeIcon(g, bx + 13, spy + SP_Y_BRUSH + 3, brushShape);

        spToggle(g, bx, spy + SP_Y_MIRX, mirrorX, mouseX, mouseY);
        centeredMini(g, bx + SP_BTN_W / 2f, spy + SP_Y_MIRX + 2, "↔X", mirrorX ? 0xFFAAFF99 : 0xFF888888);
        spToggle(g, bx, spy + SP_Y_MIRY, mirrorY, mouseX, mouseY);
        centeredMini(g, bx + SP_BTN_W / 2f, spy + SP_Y_MIRY + 2, "↕Y", mirrorY ? 0xFFAAFF99 : 0xFF888888);

        spHamburger(g, bx, spy + SP_Y_GRID, showGridMenu, mouseX, mouseY);
        hamburgerLines(g, bx + 2, spy + SP_Y_GRID + 2, 9);
        String gl = gridSize == 1 ? "1" : (gridSize + "x");
        g.pose().pushPose(); g.pose().translate(bx + SP_BTN_W - 1, spy + SP_Y_GRID + 5, 50); g.pose().scale(0.6f, 0.6f, 1f);
        g.drawString(font, gl, -(int) (font.width(gl) * 0.6f), 0, 0xFFFFD700, true); g.pose().popPose();

        boolean undoAvail = !undoStack.isEmpty();
        boolean undoHov = mouseX >= bx && mouseX < bx + SP_BTN_W && mouseY >= spy + SP_Y_UNDO && mouseY < spy + SP_Y_UNDO + SP_BTN_H;
        g.fill(bx, spy + SP_Y_UNDO, bx + SP_BTN_W, spy + SP_Y_UNDO + SP_BTN_H, undoAvail ? (undoHov ? 0xFF3A2A2A : 0xFF2A1A1A) : 0xFF1A1A1A);
        drawBorder(g, bx, spy + SP_Y_UNDO, SP_BTN_W, SP_BTN_H, undoAvail ? (undoHov ? 0xFFFF7744 : 0xFFAA5533) : 0xFF444444);
        undoArrow(g, bx, spy + SP_Y_UNDO, undoAvail);

        vbrushBar(g, bx, spy + SP_Y_VBAR, mouseX, mouseY);
    }
    private void centeredMini(GuiGraphics g, float cx, float y, String txt, int col) {
        g.pose().pushPose(); g.pose().translate(cx, y, 50); g.pose().scale(0.75f, 0.75f, 1f);
        g.drawCenteredString(font, txt, 0, 0, col); g.pose().popPose();
    }
    private void spHamburger(GuiGraphics g, int bx, int by, boolean open, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
        g.fill(bx, by, bx + SP_BTN_W, by + SP_BTN_H, open ? 0xFF555555 : (hov ? 0xFF333333 : 0xFF222222));
        drawBorder(g, bx, by, SP_BTN_W, SP_BTN_H, open ? 0xFFFFD700 : 0xFF555555);
    }
    private void spToggle(GuiGraphics g, int bx, int by, boolean on, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
        g.fill(bx, by, bx + SP_BTN_W, by + SP_BTN_H, on ? 0xFF1E3B1E : (hov ? 0xFF333333 : 0xFF222222));
        drawBorder(g, bx, by, SP_BTN_W, SP_BTN_H, on ? 0xFF55DD55 : 0xFF555555);
    }
    private void hamburgerLines(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, 0xFFCCCCCC); g.fill(x, y + 3, x + w, y + 4, 0xFFCCCCCC); g.fill(x, y + 6, x + w, y + 7, 0xFFCCCCCC);
    }
    private void shapeIcon(GuiGraphics g, int x, int y, BrushShape shape) {
        int c = 0xFFFFD700;
        switch (shape) {
            case ROUND -> { g.fill(x + 1, y, x + 4, y + 1, c); g.fill(x, y + 1, x + 5, y + 4, c); g.fill(x + 1, y + 4, x + 4, y + 5, c); }
            case SQUARE -> g.fill(x, y, x + 5, y + 5, c);
            case DIAMOND -> { g.fill(x + 2, y, x + 3, y + 1, c); g.fill(x + 1, y + 1, x + 4, y + 2, c); g.fill(x, y + 2, x + 5, y + 3, c);
                g.fill(x + 1, y + 3, x + 4, y + 4, c); g.fill(x + 2, y + 4, x + 3, y + 5, c); }
        }
    }
    private void undoArrow(GuiGraphics g, int bx, int by, boolean active) {
        int c = active ? 0xFFFF9966 : 0xFF555555; int x = bx + 2, y = by + 3;
        g.fill(x + 3, y + 2, x + 13, y + 3, c); g.fill(x, y + 2, x + 1, y + 3, c);
        g.fill(x + 1, y + 1, x + 2, y + 2, c); g.fill(x + 1, y + 3, x + 2, y + 4, c);
        g.fill(x + 2, y, x + 3, y + 1, c); g.fill(x + 2, y + 4, x + 3, y + 5, c);
        g.fill(x + 13, y, x + 15, y + 2, c); g.fill(x + 14, y - 1, x + 15, y, c);
    }
    private void vbrushBar(GuiGraphics g, int bx, int by, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_VBAR_H;
        g.fill(bx, by, bx + SP_BTN_W, by + SP_VBAR_H, 0xFF111111);
        drawBorder(g, bx, by, SP_BTN_W, SP_VBAR_H, hov ? 0xFF777777 : 0xFF444444);
        float ratio = (float) (brushSize - BRUSH_MIN) / (BRUSH_MAX - BRUSH_MIN);
        int cursorY = by + 1 + (int) ((1f - ratio) * (SP_VBAR_H - 3));
        for (int y = by + 1; y < cursorY; y++) g.fill(bx + 2, y, bx + SP_BTN_W - 2, y + 1, 0xFF0D3A55);
        for (int y = cursorY + 2; y < by + SP_VBAR_H - 1; y++) g.fill(bx + 2, y, bx + SP_BTN_W - 2, y + 1, 0xFF1E7FA8);
        g.fill(bx, cursorY, bx + SP_BTN_W, cursorY + 2, 0xFFFFFFFF);
        g.pose().pushPose(); g.pose().translate(bx + SP_BTN_W / 2f, by + 2, 50); g.pose().scale(0.8f, 0.8f, 1f);
        g.drawCenteredString(font, String.valueOf(brushSize), 0, 0, 0xFFFFFFFF); g.pose().popPose();
    }

    private void renderBrushShapeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        BrushShape[] shapes = BrushShape.values(); int ddx = ddX(), ddy = topPos + SP_Y_BRUSH, ddW = 54, ddH = shapes.length * 14 + 2;
        g.pose().pushPose(); g.pose().translate(0, 0, 200);
        g.fill(ddx, ddy, ddx + ddW, ddy + ddH, 0xEE0D0D0D); drawBorder(g, ddx, ddy, ddW, ddH, 0xFF666666);
        for (int i = 0; i < shapes.length; i++) { int iy = ddy + 1 + i * 14;
            boolean hov = mouseX >= ddx && mouseX < ddx + ddW && mouseY >= iy && mouseY < iy + 12;
            boolean sel = shapes[i] == brushShape;
            if (hov || sel) g.fill(ddx + 1, iy, ddx + ddW - 1, iy + 12, sel ? 0xFF2A2A4A : 0xFF2A2A2A);
            if (sel) g.fill(ddx + 1, iy, ddx + 3, iy + 12, 0xFFFFD700);
            shapeIcon(g, ddx + 5, iy + 3, shapes[i]);
            g.drawString(font, shapes[i].label(), ddx + 14, iy + 2, sel ? 0xFFFFD700 : 0xFFCCCCCC, false); }
        g.pose().popPose();
    }
    private void renderGridSizeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        int ddx = ddX(), ddy = topPos + SP_Y_GRID, ddW = 54, ddH = GRID_SIZES.length * 14 + 2;
        g.pose().pushPose(); g.pose().translate(0, 0, 200);
        g.fill(ddx, ddy, ddx + ddW, ddy + ddH, 0xEE0D0D0D); drawBorder(g, ddx, ddy, ddW, ddH, 0xFF666666);
        for (int i = 0; i < GRID_SIZES.length; i++) { int iy = ddy + 1 + i * 14;
            boolean hov = mouseX >= ddx && mouseX < ddx + ddW && mouseY >= iy && mouseY < iy + 12;
            boolean sel = GRID_SIZES[i] == gridSize;
            if (hov || sel) g.fill(ddx + 1, iy, ddx + ddW - 1, iy + 12, sel ? 0xFF2A2A4A : 0xFF2A2A2A);
            if (sel) g.fill(ddx + 1, iy, ddx + 3, iy + 12, 0xFFFFD700);
            g.drawString(font, GRID_LABELS[i], ddx + 8, iy + 2, sel ? 0xFFFFD700 : 0xFFCCCCCC, false); }
        g.pose().popPose();
    }

    private void renderBrushCursor(GuiGraphics g, int mouseX, int mouseY) {
        int originX = leftPos + PREV_X, originY = topPos + PREV_Y, drawX, drawY;
        if (gridSize > 1) {
            int lx = mouseX - originX, ly = mouseY - originY;
            drawX = originX + Math.floorDiv(lx, gridSize) * gridSize + gridSize / 2;
            drawY = originY + Math.floorDiv(ly, gridSize) * gridSize + gridSize / 2;
        } else { drawX = mouseX; drawY = mouseY; }
        int vr = gridSize > 1 ? brushSize + gridSize / 2 : brushSize;
        g.enableScissor(originX, originY, originX + W, originY + H);
        g.pose().pushPose(); g.pose().translate(0, 0, 50);
        int col = 0xCCFFFFFF;
        switch (brushShape) {
            case ROUND -> circleOutline(g, drawX, drawY, vr, col);
            case SQUARE -> { g.fill(drawX - vr, drawY - vr, drawX + vr + 1, drawY - vr + 1, col); g.fill(drawX - vr, drawY + vr, drawX + vr + 1, drawY + vr + 1, col);
                g.fill(drawX - vr, drawY - vr, drawX - vr + 1, drawY + vr + 1, col); g.fill(drawX + vr, drawY - vr, drawX + vr + 1, drawY + vr + 1, col); }
            case DIAMOND -> { line(g, drawX, drawY - vr, drawX + vr, drawY, col); line(g, drawX + vr, drawY, drawX, drawY + vr, col);
                line(g, drawX, drawY + vr, drawX - vr, drawY, col); line(g, drawX - vr, drawY, drawX, drawY - vr, col); }
        }
        g.fill(drawX, drawY, drawX + 1, drawY + 1, 0xFFFFFFFF);
        g.pose().popPose(); g.disableScissor();
    }
    private void circleOutline(GuiGraphics g, int cx, int cy, int r, int color) {
        if (r <= 0) { g.fill(cx, cy, cx + 1, cy + 1, color); return; }
        int x = r, y = 0, err = 0;
        while (x >= y) {
            g.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color); g.fill(cx + y, cy + x, cx + y + 1, cy + x + 1, color);
            g.fill(cx - y, cy + x, cx - y + 1, cy + x + 1, color); g.fill(cx - x, cy + y, cx - x + 1, cy + y + 1, color);
            g.fill(cx - x, cy - y, cx - x + 1, cy - y + 1, color); g.fill(cx - y, cy - x, cx - y + 1, cy - x + 1, color);
            g.fill(cx + y, cy - x, cx + y + 1, cy - x + 1, color); g.fill(cx + x, cy - y, cx + x + 1, cy - y + 1, color);
            y++; err += 1 + 2 * y; if (2 * (err - x) + 1 > 0) { x--; err += 1 - 2 * x; }
        }
    }
    private void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, err = dx - dy, x = x1, y = y1;
        while (true) { g.fill(x, y, x + 1, y + 1, color); if (x == x2 && y == y2) break;
            int e2 = 2 * err; if (e2 > -dy) { err -= dy; x += sx; } if (e2 < dx) { err += dx; y += sy; } }
    }

    // ── Utils ────────────────────────────────────────────────────────────────
    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color); g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color); g.fill(x + w - 1, y, x + w, y + h, color);
    }
    private static float clamp01(double v) { return (float) Math.max(0, Math.min(1, v)); }
    private static int hsvToRgb(float h, float s, float v) {
        float r, gg, b; int i = (int) (h * 6) % 6; if (i < 0) i += 6;
        float f = h * 6 - (int) (h * 6);
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0 -> { r = v; gg = t; b = p; } case 1 -> { r = q; gg = v; b = p; }
            case 2 -> { r = p; gg = v; b = t; } case 3 -> { r = p; gg = q; b = v; }
            case 4 -> { r = t; gg = p; b = v; } default -> { r = v; gg = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (gg * 255) << 8) | (int) (b * 255);
    }
    private static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f, g = ((rgb >> 8) & 0xFF) / 255f, b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min, h = 0f;
        if (d != 0) { if (max == r) h = ((g - b) / d) % 6; else if (max == g) h = (b - r) / d + 2; else h = (r - g) / d + 4;
            h /= 6f; if (h < 0) h += 1f; }
        return new float[]{ h, max == 0 ? 0 : d / max, max };
    }
    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }
}
