package net.tiew.operationWild.screen.entity;

import com.mojang.blaze3d.platform.InputConstants;
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

import java.util.ArrayDeque;
import java.util.Arrays;

public class OWTeamCreationScreen extends Screen {

    private static final ResourceLocation OW_TEAMS_LOC =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");

    private static final int ELEM_U = 200, ELEM_V = 0, ELEM_W = 55, ELEM_H = 93;

    private static final int PW = 176, PH = 166;

    private static final int SP_GAP = 4;
    private static final int SP_W = 24;
    private static final int SP_BTN_W = 20;
    private static final int SP_BTN_H = 12;
    private static final int SP_BTN_X = 2;

    private static final int SP_Y_BRUSH = 4;
    private static final int SP_Y_MIRX = SP_Y_BRUSH + SP_BTN_H + 4;
    private static final int SP_Y_MIRY = SP_Y_MIRX + SP_BTN_H + 4;
    private static final int SP_Y_GRID = SP_Y_MIRY + SP_BTN_H + 4;
    private static final int SP_Y_UNDO = SP_Y_GRID + SP_BTN_H + 4;
    private static final int SP_Y_VBAR = SP_Y_UNDO + SP_BTN_H + 4;
    private static final int SP_VBAR_H = PH - SP_Y_VBAR - 4;

    private static final int MAX_UNDO = 30;
    private final ArrayDeque<boolean[]> undoStack = new ArrayDeque<>();
    private boolean[] snapshotBeforeStroke = null;

    private static final int NAME_BOX_Y = 18;
    private static final int PREV_X = 5;
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

    private static final int BRUSH_MIN = 1;
    private static final int BRUSH_MAX = 8;
    private static final int PAINT_PIXEL_COUNT = ELEM_W * ELEM_H;

    private enum BrushShape {
        ROUND("owteams.paint.brush.round"),
        SQUARE("owteams.paint.brush.square"),
        DIAMOND("owteams.paint.brush.diamond");

        private final String key;

        BrushShape(String key) {
            this.key = key;
        }

        String label() {
            return Component.translatable(key).getString();
        }
    }

    private static final int[] GRID_SIZES = {1, 2, 4, 8, 16};
    private static final String[] GRID_LABELS = {"256\u00d7256", "128\u00d7128", "64\u00d764", "32\u00d732", "16\u00d716"};

    private boolean[] paintPixels = new boolean[PAINT_PIXEL_COUNT];
    private int brushSize = 3;

    private BrushShape brushShape = BrushShape.ROUND;
    private boolean mirrorX = false;
    private boolean mirrorY = false;
    private int gridSize = 1;

    private boolean showBrushMenu = false;
    private boolean showGridMenu = false;
    private boolean showPatternOverflow = false;

    private boolean draggingCanvas = false;
    private boolean draggingVBar = false;
    private boolean draggingHue = false;
    private boolean draggingSV = false;

    private int lastPaintCX = Integer.MIN_VALUE;
    private int lastPaintCY = Integer.MIN_VALUE;

    private boolean ctrlZWasDown = false;

    private Button clearPaintBtn;
    private Button nameErrorOkBtn;
    private EditBox nameBox;
    private Button confirmBtn, cancelBtn;
    private Button primTabBtn, secTabBtn;

    private int leftPos, topPos;
    private final OWEntity entity;

    private int primaryColor = 0xD12020;
    private int secondaryColor = 0x2050D1;
    private OWTeamMosaicPattern selectedPattern = OWTeamMosaicPattern.GRADIENT_DOWN;

    private float primH = 0f, primS = 1f, primV = 0.82f;
    private float secH = 0.62f, secS = 1f, secV = 0.82f;
    private boolean editingPrimary = true;

    private boolean showNameTakenError = false;
    private String nameError = "";

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    public OWTeamCreationScreen(OWEntity entity) {
        super(Component.translatable("owteams.creation.title"));
        this.entity = entity;
        primaryColor = hsvToRgb(primH, primS, primV);
        secondaryColor = hsvToRgb(secH, secS, secV);
    }

    // ── Ordre d'affichage des patterns : DIAMOND et CUSTOM_PAINT sont échangés ──
    private static OWTeamMosaicPattern[] getOrderedPatterns() {
        OWTeamMosaicPattern[] ordered = OWTeamMosaicPattern.values().clone();
        int di = -1, ci = -1;
        for (int i = 0; i < ordered.length; i++) {
            if (ordered[i] == OWTeamMosaicPattern.DIAMOND)      di = i;
            if (ordered[i] == OWTeamMosaicPattern.CUSTOM_PAINT) ci = i;
        }
        if (di >= 0 && ci >= 0) {
            OWTeamMosaicPattern tmp = ordered[di];
            ordered[di] = ordered[ci];
            ordered[ci] = tmp;
        }
        return ordered;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - PW) / 2;
        this.topPos = (this.height - PH) / 2;

        nameBox = new EditBox(this.font,
                leftPos + 36, topPos + NAME_BOX_Y, PW - 44, 12,
                Component.translatable("owteams.creation.name_placeholder"));
        nameBox.setMaxLength(15);
        String defaultTeamName = Component.translatable("owteams.creation.default_name",
                entity.getOwner().getName().getString()).getString();
        if (defaultTeamName.length() > 15) defaultTeamName = defaultTeamName.substring(0, 15);
        nameBox.setValue(defaultTeamName);
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
                .bounds(leftPos + 6, topPos + ACT_Y, 64, 12)
                .build();
        this.addRenderableWidget(confirmBtn);

        clearPaintBtn = Button.builder(
                        Component.translatable("owteams.paint.clear")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD9944))),
                        btn -> {
                            if (undoStack.size() >= MAX_UNDO) undoStack.pollLast();
                            undoStack.push(paintPixels.clone());
                            Arrays.fill(paintPixels, false);
                        })
                .bounds(leftPos + 74, topPos + ACT_Y, 30, 12)
                .build();
        this.addRenderableWidget(clearPaintBtn);

        cancelBtn = Button.builder(
                        Component.translatable("owteams.creation.cancel")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444))),
                        btn -> onCancel())
                .bounds(leftPos + 108, topPos + ACT_Y, 62, 12)
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

    private void onConfirm() {
        if (entity == null) return;
        String def = Component.translatable("owteams.creation.default_name", "").getString().trim();
        String name = nameBox.getValue().trim().isEmpty() ? def : nameBox.getValue().trim();
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
        byte[] packed = selectedPattern == OWTeamMosaicPattern.CUSTOM_PAINT
                ? OWTeamMosaicPattern.packPixels(paintPixels)
                : new byte[0];
        OWNetworkHandler.sendToServer(new CreateOWTeamWithParamsPacket(
                entity.getId(), name, primaryColor, secondaryColor,
                selectedPattern.getId(), packed));
        Minecraft.getInstance().setScreen(
                new OWTeamsInterface(Component.translatable("owteams.title")));
    }

    private void onCancel() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {

        if ((showBrushMenu || showGridMenu) && !isInAnyDropdown(mx, my) && !isInSidePanel(mx, my)) {
            showBrushMenu = false;
            showGridMenu = false;
        }
        // Fermer la popup overflow patterns si clic en dehors
        if (showPatternOverflow && !isInPatternSelectorArea(mx, my)) {
            showPatternOverflow = false;
        }

        if (isPaintMode() && isInSidePanel(mx, my)) {
            int bx = leftPos + PW + SP_GAP + SP_BTN_X;

            if (isInSPBtn(mx, my, bx, topPos + SP_Y_BRUSH)) {
                showBrushMenu = !showBrushMenu;
                showGridMenu = false;
                return true;
            }
            if (isInSPBtn(mx, my, bx, topPos + SP_Y_MIRX)) {
                mirrorX = !mirrorX;
                return true;
            }
            if (isInSPBtn(mx, my, bx, topPos + SP_Y_MIRY)) {
                mirrorY = !mirrorY;
                return true;
            }
            if (isInSPBtn(mx, my, bx, topPos + SP_Y_GRID)) {
                showGridMenu = !showGridMenu;
                showBrushMenu = false;
                return true;
            }
            if (isInSPBtn(mx, my, bx, topPos + SP_Y_UNDO)) {
                performUndo();
                return true;
            }
            if (isInVBar(mx, my)) {
                draggingVBar = true;
                applyVBarPick(my);
                return true;
            }
            return true;
        }

        if (showBrushMenu) {
            int r = getBrushMenuClick(mx, my);
            if (r >= 0) {
                brushShape = BrushShape.values()[r];
                showBrushMenu = false;
                return true;
            }
        }
        if (showGridMenu) {
            int r = getGridMenuClick(mx, my);
            if (r >= 0) {
                gridSize = GRID_SIZES[r];
                showGridMenu = false;
                return true;
            }
        }

        // ── Popup overflow : traité EN PRIORITÉ avant le color picker ─────────
        // La popup remonte au-dessus de PAT_Y et peut chevaucher la zone SV/hue.
        // Il faut donc l'intercepter ici, avant isInHueBar/isInSVSquare.
        if (showPatternOverflow) {
            OWTeamMosaicPattern[] patterns = getOrderedPatterns();
            int maxV = calcMaxVisible();
            boolean hasOvf = patterns.length > maxV;
            if (hasOvf) {
                int normalCount = maxV - 1;
                int totalBtns = maxV;
                int rowW = totalBtns * PAT_BTN_SIZE + (totalBtns - 1) * PAT_BTN_GAP;
                int patStartX = leftPos + (PW - rowW) / 2;
                int patAbsY = topPos + PAT_Y;
                int dotsBtnX = patStartX + normalCount * (PAT_BTN_SIZE + PAT_BTN_GAP);
                int[] popupBounds = calcOverflowPopupBounds(normalCount, patterns.length, dotsBtnX, patAbsY);
                int popX = popupBounds[0], popY = popupBounds[1], popW = popupBounds[2];
                int count = patterns.length - normalCount;
                if (mx >= popX && mx < popX + popW
                        && my >= popY && my < popY + PAT_BTN_SIZE + 8) {
                    for (int i = 0; i < count; i++) {
                        int bx = popX + 4 + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
                        int by = popY + 4;
                        if (mx >= bx && mx < bx + PAT_BTN_SIZE
                                && my >= by && my < by + PAT_BTN_SIZE) {
                            selectedPattern = patterns[normalCount + i];
                            showPatternOverflow = false;
                            return true;
                        }
                    }
                    return true; // absorbe les clics dans la popup même hors bouton
                }
            }
        }

        if (isPaintMode() && isInFlagPreview(mx, my)) {
            nameBox.setFocused(false);
            lastPaintCX = Integer.MIN_VALUE;
            lastPaintCY = Integer.MIN_VALUE;
            snapshotBeforeStroke = paintPixels.clone();
            draggingCanvas = true;
            applyBrushAt(mx, my);
            return true;
        }
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

        // ── Sélection de motif (avec overflow "...") ──────────────────────────
        {
            OWTeamMosaicPattern[] patterns = getOrderedPatterns();
            int maxV = calcMaxVisible();
            boolean hasOvf = patterns.length > maxV;
            int normalCount = hasOvf ? maxV - 1 : patterns.length;
            int totalBtns = hasOvf ? maxV : patterns.length;
            int rowW = totalBtns * PAT_BTN_SIZE + (totalBtns - 1) * PAT_BTN_GAP;
            int patStartX = leftPos + (PW - rowW) / 2;
            int patAbsY = topPos + PAT_Y;

            for (int i = 0; i < normalCount; i++) {
                int bx = patStartX + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
                if (mx >= bx && mx < bx + PAT_BTN_SIZE && my >= patAbsY && my < patAbsY + PAT_BTN_SIZE) {
                    selectedPattern = patterns[i];
                    showPatternOverflow = false;
                    return true;
                }
            }
            if (hasOvf) {
                int dotsBtnX = patStartX + normalCount * (PAT_BTN_SIZE + PAT_BTN_GAP);
                // Clic sur le bouton "..."
                if (mx >= dotsBtnX && mx < dotsBtnX + PAT_BTN_SIZE
                        && my >= patAbsY && my < patAbsY + PAT_BTN_SIZE) {
                    showPatternOverflow = !showPatternOverflow;
                    return true;
                }
                // Note: le clic dans la popup overflow est géré plus haut en priorité
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingVBar) {
            applyVBarPick(my);
            return true;
        }
        if (draggingCanvas) {
            applyBrushAt(mx, my);
            return true;
        }
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
        if (draggingCanvas && snapshotBeforeStroke != null) {
            if (undoStack.size() >= MAX_UNDO) undoStack.pollLast();
            undoStack.push(snapshotBeforeStroke);
            snapshotBeforeStroke = null;
        }
        draggingCanvas = false;
        draggingVBar = false;
        draggingHue = false;
        draggingSV = false;
        lastPaintCX = Integer.MIN_VALUE;
        lastPaintCY = Integer.MIN_VALUE;
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isPaintMode() {
        return selectedPattern == OWTeamMosaicPattern.CUSTOM_PAINT;
    }

    private boolean isInFlagPreview(double mx, double my) {
        return mx >= leftPos + PREV_X && mx < leftPos + PREV_X + ELEM_W
                && my >= topPos + PREV_Y && my < topPos + PREV_Y + ELEM_H;
    }

    // ── Miroir ───────────────────────────────────────────────────────────────
    private int mirX(int px) {
        if (gridSize > 1) {
            int mirPixel = ELEM_W - 1 - px;
            return Math.floorDiv(mirPixel, gridSize) * gridSize + gridSize / 2;
        }
        return ELEM_W - px;
    }

    private int mirY(int py) {
        if (gridSize > 1) {
            int mirPixel = ELEM_H - 1 - py;
            return Math.floorDiv(mirPixel, gridSize) * gridSize + gridSize / 2;
        }
        return ELEM_H - 1 - py;
    }

    private void applyBrushAt(double mx, double my) {
        int cx = (int) (mx - (leftPos + PREV_X + 2));
        int cy = (int) (my - (topPos + PREV_Y));
        boolean sec = !editingPrimary;

        if (gridSize > 1) {
            int ciX0 = lastPaintCX == Integer.MIN_VALUE ? Math.floorDiv(cx, gridSize) : Math.floorDiv(lastPaintCX, gridSize);
            int ciY0 = lastPaintCY == Integer.MIN_VALUE ? Math.floorDiv(cy, gridSize) : Math.floorDiv(lastPaintCY, gridSize);
            int ciX1 = Math.floorDiv(cx, gridSize);
            int ciY1 = Math.floorDiv(cy, gridSize);
            int steps = (lastPaintCX == Integer.MIN_VALUE) ? 0
                    : Math.max(1, Math.max(Math.abs(ciX1 - ciX0), Math.abs(ciY1 - ciY0)));

            for (int i = (steps == 0 ? 0 : 1); i <= steps; i++) {
                float t = (steps == 0) ? 1f : (float) i / steps;
                int iCX = Math.round(ciX0 + (ciX1 - ciX0) * t);
                int iCY = Math.round(ciY0 + (ciY1 - ciY0) * t);
                int px = iCX * gridSize + gridSize / 2;
                int py = iCY * gridSize + gridSize / 2;
                paintBrushAt(px, py, sec);
                if (mirrorX) paintBrushAt(mirX(px), py, sec);
                if (mirrorY) paintBrushAt(px, mirY(py), sec);
                if (mirrorX && mirrorY) paintBrushAt(mirX(px), mirY(py), sec);
            }
        } else {
            if (lastPaintCX != Integer.MIN_VALUE) {
                int steps = Math.max(1, Math.max(
                        Math.abs(cx - lastPaintCX), Math.abs(cy - lastPaintCY)));
                float fdx = cx - lastPaintCX;
                float fdy = cy - lastPaintCY;
                for (int i = 1; i <= steps; i++) {
                    float t = (float) i / steps;
                    int ix = Math.round(lastPaintCX + fdx * t);
                    int iy = Math.round(lastPaintCY + fdy * t);
                    paintBrushAt(ix, iy, sec);
                    if (mirrorX) paintBrushAt(ELEM_W - ix, iy, sec);
                    if (mirrorY) paintBrushAt(ix, ELEM_H - 1 - iy, sec);
                    if (mirrorX && mirrorY) paintBrushAt(ELEM_W - ix, ELEM_H - 1 - iy, sec);
                }
            } else {
                applyBrushAtCoords(cx, cy);
            }
        }

        lastPaintCX = cx;
        lastPaintCY = cy;
    }

    private void applyBrushAtCoords(int cx, int cy) {
        boolean sec = !editingPrimary;
        paintBrushAt(cx, cy, sec);
        if (mirrorX) paintBrushAt(mirX(cx), cy, sec);
        if (mirrorY) paintBrushAt(cx, mirY(cy), sec);
        if (mirrorX && mirrorY) paintBrushAt(mirX(cx), mirY(cy), sec);
    }

    private void paintBrushAt(int cx, int cy, boolean useSec) {
        int r = brushSize;
        if (gridSize > 1) {
            for (int dci = -r; dci <= r; dci++) {
                for (int dcj = -r; dcj <= r; dcj++) {
                    if (isInsideBrush(dci, dcj, r))
                        setPixel(cx + dci * gridSize, cy + dcj * gridSize, useSec);
                }
            }
        } else {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (isInsideBrush(dx, dy, r)) setPixel(cx + dx, cy + dy, useSec);
                }
            }
        }
    }

    private boolean isInsideBrush(int dx, int dy, int r) {
        return switch (brushShape) {
            case ROUND -> dx * dx + dy * dy <= r * r;
            case SQUARE -> true;
            case DIAMOND -> Math.abs(dx) + Math.abs(dy) <= r;
        };
    }

    private void setPixel(int px, int py, boolean useSec) {
        if (gridSize > 1) {
            int cellX = Math.floorDiv(px, gridSize) * gridSize;
            int cellY = Math.floorDiv(py, gridSize) * gridSize;
            for (int gy = 0; gy < gridSize; gy++) {
                for (int gx = 0; gx < gridSize; gx++) {
                    int fpx = cellX + gx, fpy = cellY + gy;
                    if (fpx >= 0 && fpx < ELEM_W && fpy >= 0 && fpy < ELEM_H)
                        paintPixels[fpy * ELEM_W + fpx] = useSec;
                }
            }
        } else {
            if (px >= 0 && px < ELEM_W && py >= 0 && py < ELEM_H)
                paintPixels[py * ELEM_W + px] = useSec;
        }
    }

    private boolean isInVBar(double mx, double my) {
        int bx = leftPos + PW + SP_GAP + SP_BTN_X;
        int by = topPos + SP_Y_VBAR;
        return mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_VBAR_H;
    }

    private void applyVBarPick(double my) {
        int by = topPos + SP_Y_VBAR;
        float ratio = (float) Math.max(0, Math.min(1, (my - by) / (double) SP_VBAR_H));
        brushSize = BRUSH_MAX - Math.round(ratio * (BRUSH_MAX - BRUSH_MIN));
    }

    private boolean isInSidePanel(double mx, double my) {
        int spx = leftPos + PW + SP_GAP;
        return mx >= spx && mx < spx + SP_W && my >= topPos && my < topPos + PH;
    }

    private boolean isInSPBtn(double mx, double my, int bx, int by) {
        return mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
    }

    private int ddX() {
        return leftPos + PW + SP_GAP - 56;
    }

    private boolean isInAnyDropdown(double mx, double my) {
        if (showBrushMenu) {
            int ddy = topPos + SP_Y_BRUSH;
            if (mx >= ddX() && mx < ddX() + 54 && my >= ddy && my < ddy + BrushShape.values().length * 14 + 2)
                return true;
        }
        if (showGridMenu) {
            int ddy = topPos + SP_Y_GRID;
            if (mx >= ddX() && mx < ddX() + 54 && my >= ddy && my < ddy + GRID_SIZES.length * 14 + 2)
                return true;
        }
        return false;
    }

    private int getBrushMenuClick(double mx, double my) {
        int ddx = ddX(), ddy = topPos + SP_Y_BRUSH;
        for (int i = 0; i < BrushShape.values().length; i++) {
            if (mx >= ddx && mx < ddx + 54 && my >= ddy + 1 + i * 14 && my < ddy + 1 + i * 14 + 12)
                return i;
        }
        return -1;
    }

    private int getGridMenuClick(double mx, double my) {
        int ddx = ddX(), ddy = topPos + SP_Y_GRID;
        for (int i = 0; i < GRID_SIZES.length; i++) {
            if (mx >= ddx && mx < ddx + 54 && my >= ddy + 1 + i * 14 && my < ddy + 1 + i * 14 + 12)
                return i;
        }
        return -1;
    }

    private boolean isInHueBar(double mx, double my) {
        return mx >= leftPos + CP_X && mx < leftPos + CP_X + CP_W
                && my >= topPos + HUE_Y && my < topPos + HUE_Y + HUE_H;
    }

    private boolean isInSVSquare(double mx, double my) {
        return mx >= leftPos + CP_X && mx < leftPos + CP_X + CP_W
                && my >= topPos + SV_Y && my < topPos + SV_Y + SV_H;
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
        // voile géré manuellement dans render()
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {

        if (isPaintMode()) {
            long win = Minecraft.getInstance().getWindow().getWindow();
            boolean ctrlNow = InputConstants.isKeyDown(win, 341)
                    || InputConstants.isKeyDown(win, 345)
                    || InputConstants.isKeyDown(win, 343)
                    || InputConstants.isKeyDown(win, 347);
            boolean zNow = InputConstants.isKeyDown(win, 90)
                    || InputConstants.isKeyDown(win, 87)
                    || InputConstants.isKeyDown(win, 89);
            if (ctrlNow && zNow && !ctrlZWasDown) performUndo();
            ctrlZWasDown = ctrlNow && zNow;
        } else {
            ctrlZWasDown = false;
        }

        this.renderTransparentBackground(g);

        g.blit(OW_TEAMS_LOC, leftPos, topPos, 0, 0, PW, PH);
        tabsRenderer.renderTabs(g, this.font, entity, leftPos, topPos, mouseX, mouseY);

        Component title = Component.translatable("owteams.creation.title")
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFD700)));
        g.drawCenteredString(this.font, title, leftPos + PW / 2, topPos + 6, 0xFFD700);
        g.fill(leftPos + 8, topPos + 15, leftPos + PW - 8, topPos + 16, 0xFF333333);

        if (!nameError.isEmpty()) {
            g.drawString(this.font, nameError, leftPos + 8, topPos + NAME_BOX_Y + 2, 0xFF4444, false);
        } else {
            g.drawString(this.font, Component.translatable("owteams.creation.name_label"),
                    leftPos + 8, topPos + NAME_BOX_Y + 2, 0x555555, false);
        }
        g.fill(leftPos + 8, topPos + 33, leftPos + PW - 8, topPos + 34, 0xFF333333);

        clearPaintBtn.visible = isPaintMode();

        super.render(g, mouseX, mouseY, partial);

        renderFlagWithPattern(g, leftPos + PREV_X + 2, topPos + PREV_Y,
                primaryColor, secondaryColor, selectedPattern);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blit(OW_TEAMS_LOC,
                leftPos + PREV_X + 3 + (ELEM_W - 49) / 2,
                topPos + PREV_Y + ELEM_H - 25,
                204, 98, 49, 25);

        if (isPaintMode()) {
            renderGridOverlay(g);
            renderMirrorGuides(g);
        }

        renderColorPicker(g, mouseX, mouseY);
        renderPatternSelector(g, mouseX, mouseY);
        g.fill(leftPos + 8, topPos + ACT_Y - 4, leftPos + PW - 8, topPos + ACT_Y - 3, 0xFF333333);

        if (isPaintMode()) {
            renderSidePanel(g, mouseX, mouseY);
        }

        if (isPaintMode() && isInFlagPreview(mouseX, mouseY)) {
            renderBrushCursor(g, mouseX, mouseY);
        }

        nameErrorOkBtn.visible = false;
        if (showNameTakenError) {
            renderNameTakenOverlay(g, mouseX, mouseY, partial);
        }

        if (showBrushMenu) renderBrushShapeDropdown(g, mouseX, mouseY);
        if (showGridMenu) renderGridSizeDropdown(g, mouseX, mouseY);
    }

    private void renderGridOverlay(GuiGraphics g) {
        if (gridSize <= 1) return;
        int fx = leftPos + PREV_X, fy = topPos + PREV_Y;
        int col = 0x33FFFFFF;
        for (int gx = 0; gx <= ELEM_W; gx += gridSize)
            g.fill(fx + gx, fy, fx + gx + 1, fy + ELEM_H, col);
        for (int gy = 0; gy <= ELEM_H; gy += gridSize)
            g.fill(fx, fy + gy, fx + ELEM_W, fy + gy + 1, col);
    }

    private void renderMirrorGuides(GuiGraphics g) {
        int fx = leftPos + PREV_X + 2, fy = topPos + PREV_Y;
        if (mirrorX) {
            int midX = fx + mirrorAxisX();
            for (int y = 0; y < ELEM_H; y += 4)
                g.fill(midX, fy + y, midX + 1, fy + y + 2, 0xCCFFFF44);
        }
        if (mirrorY) {
            int midY = fy + mirrorAxisY();
            for (int x = 0; x < ELEM_W; x += 4)
                g.fill(fx + x, midY, fx + x + 2, midY + 1, 0xCCFFFF44);
        }
    }

    private int mirrorAxisX() {
        if (gridSize <= 1) return (ELEM_W - 1) / 2 + 1;
        int nearCenter = Math.floorDiv((ELEM_W - 1) / 2, gridSize) * gridSize + gridSize / 2;
        int mirCenter  = Math.floorDiv(ELEM_W - 1 - nearCenter, gridSize) * gridSize + gridSize / 2;
        return (nearCenter + mirCenter) / 2;
    }

    private int mirrorAxisY() {
        if (gridSize <= 1) return (ELEM_H - 1) / 2;
        int nearCenter = Math.floorDiv((ELEM_H - 1) / 2, gridSize) * gridSize + gridSize / 2;
        int mirCenter  = Math.floorDiv(ELEM_H - 1 - nearCenter, gridSize) * gridSize + gridSize / 2;
        return (nearCenter + mirCenter) / 2;
    }

    private void renderSidePanel(GuiGraphics g, int mouseX, int mouseY) {
        int spx = leftPos + PW + SP_GAP;
        int spy = topPos;
        int bx = spx + SP_BTN_X;

        g.fill(spx, spy, spx + SP_W, spy + PH, 0xDD1A1A1A);
        drawBorder(g, spx, spy, SP_W, PH, 0xFF444444);

        renderSPHamburgerBtn(g, bx, spy + SP_Y_BRUSH, showBrushMenu, mouseX, mouseY);
        drawHamburgerLines(g, bx + 2, spy + SP_Y_BRUSH + 2, 9);
        renderShapeIcon(g, bx + 13, spy + SP_Y_BRUSH + 3, brushShape);

        renderSPToggleBtn(g, bx, spy + SP_Y_MIRX, mirrorX, mouseX, mouseY);
        g.pose().pushPose();
        g.pose().translate(bx + SP_BTN_W / 2f, spy + SP_Y_MIRX + 2, 50);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawCenteredString(font, "\u2194X", 0, 0, mirrorX ? 0xFFAAFF99 : 0xFF888888);
        g.pose().popPose();

        renderSPToggleBtn(g, bx, spy + SP_Y_MIRY, mirrorY, mouseX, mouseY);
        g.pose().pushPose();
        g.pose().translate(bx + SP_BTN_W / 2f, spy + SP_Y_MIRY + 2, 50);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawCenteredString(font, "\u2195Y", 0, 0, mirrorY ? 0xFFAAFF99 : 0xFF888888);
        g.pose().popPose();

        renderSPHamburgerBtn(g, bx, spy + SP_Y_GRID, showGridMenu, mouseX, mouseY);
        drawHamburgerLines(g, bx + 2, spy + SP_Y_GRID + 2, 9);
        String gl = gridSize == 1 ? "FR" : (gridSize + "x");
        g.pose().pushPose();
        g.pose().translate(bx + SP_BTN_W - 1, spy + SP_Y_GRID + 5, 50);
        g.pose().scale(0.6f, 0.6f, 1f);
        int glw = (int) (font.width(gl) * 0.6f);
        g.drawString(font, gl, -glw, 0, 0xFFFFD700, true);
        g.pose().popPose();

        boolean undoAvail = !undoStack.isEmpty();
        boolean undoHov = mouseX >= bx && mouseX < bx + SP_BTN_W
                && mouseY >= spy + SP_Y_UNDO && mouseY < spy + SP_Y_UNDO + SP_BTN_H;
        g.fill(bx, spy + SP_Y_UNDO, bx + SP_BTN_W, spy + SP_Y_UNDO + SP_BTN_H,
                undoAvail ? (undoHov ? 0xFF3A2A2A : 0xFF2A1A1A) : 0xFF1A1A1A);
        drawBorder(g, bx, spy + SP_Y_UNDO, SP_BTN_W, SP_BTN_H,
                undoAvail ? (undoHov ? 0xFFFF7744 : 0xFFAA5533) : 0xFF444444);
        drawUndoArrow(g, bx, spy + SP_Y_UNDO, undoAvail);

        renderVerticalBrushBar(g, bx, spy + SP_Y_VBAR, mouseX, mouseY);
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) paintPixels = undoStack.pop();
    }

    private void drawUndoArrow(GuiGraphics g, int bx, int by, boolean active) {
        int c = active ? 0xFFFF9966 : 0xFF555555;
        int x = bx + 2, y = by + 3;
        g.fill(x + 3, y + 2, x + 13, y + 3, c);
        g.fill(x, y + 2, x + 1, y + 3, c);
        g.fill(x + 1, y + 1, x + 2, y + 2, c);
        g.fill(x + 1, y + 3, x + 2, y + 4, c);
        g.fill(x + 2, y, x + 3, y + 1, c);
        g.fill(x + 2, y + 4, x + 3, y + 5, c);
        g.fill(x + 13, y, x + 15, y + 2, c);
        g.fill(x + 14, y - 1, x + 15, y, c);
    }

    private void renderSPHamburgerBtn(GuiGraphics g, int bx, int by, boolean open, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
        g.fill(bx, by, bx + SP_BTN_W, by + SP_BTN_H,
                open ? 0xFF555555 : (hov ? 0xFF333333 : 0xFF222222));
        drawBorder(g, bx, by, SP_BTN_W, SP_BTN_H, open ? 0xFFFFD700 : 0xFF555555);
    }

    private void renderSPToggleBtn(GuiGraphics g, int bx, int by, boolean on, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_BTN_H;
        g.fill(bx, by, bx + SP_BTN_W, by + SP_BTN_H,
                on ? 0xFF1E3B1E : (hov ? 0xFF333333 : 0xFF222222));
        drawBorder(g, bx, by, SP_BTN_W, SP_BTN_H, on ? 0xFF55DD55 : 0xFF555555);
    }

    private void drawHamburgerLines(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, 0xFFCCCCCC);
        g.fill(x, y + 3, x + w, y + 4, 0xFFCCCCCC);
        g.fill(x, y + 6, x + w, y + 7, 0xFFCCCCCC);
    }

    private void renderShapeIcon(GuiGraphics g, int x, int y, BrushShape shape) {
        int c = 0xFFFFD700;
        switch (shape) {
            case ROUND -> {
                g.fill(x + 1, y, x + 4, y + 1, c);
                g.fill(x, y + 1, x + 5, y + 4, c);
                g.fill(x + 1, y + 4, x + 4, y + 5, c);
            }
            case SQUARE -> g.fill(x, y, x + 5, y + 5, c);
            case DIAMOND -> {
                g.fill(x + 2, y, x + 3, y + 1, c);
                g.fill(x + 1, y + 1, x + 4, y + 2, c);
                g.fill(x, y + 2, x + 5, y + 3, c);
                g.fill(x + 1, y + 3, x + 4, y + 4, c);
                g.fill(x + 2, y + 4, x + 3, y + 5, c);
            }
        }
    }

    private void renderVerticalBrushBar(GuiGraphics g, int bx, int by, int mx, int my) {
        boolean hov = mx >= bx && mx < bx + SP_BTN_W && my >= by && my < by + SP_VBAR_H;

        g.fill(bx, by, bx + SP_BTN_W, by + SP_VBAR_H, 0xFF111111);
        drawBorder(g, bx, by, SP_BTN_W, SP_VBAR_H, hov ? 0xFF777777 : 0xFF444444);

        float ratio = (float) (brushSize - BRUSH_MIN) / (BRUSH_MAX - BRUSH_MIN);
        int cursorY = by + 1 + (int) ((1f - ratio) * (SP_VBAR_H - 3));

        for (int y = by + 1; y < cursorY; y++) {
            float t = (float) (y - by - 1) / Math.max(1, cursorY - by - 1);
            g.fill(bx + 2, y, bx + SP_BTN_W - 2, y + 1, 0xFF000000 | dimColor(lerpColor(0x0D6762, 0x0D3057, t)));
        }
        for (int y = cursorY + 2; y < by + SP_VBAR_H - 1; y++) {
            float t = (float) (y - cursorY) / Math.max(1, by + SP_VBAR_H - 1 - cursorY);
            g.fill(bx + 2, y, bx + SP_BTN_W - 2, y + 1, 0xFF000000 | lerpColor(0x1ECFB4, 0x1E5FA8, t));
        }

        g.fill(bx, cursorY, bx + SP_BTN_W, cursorY + 2, 0xFFFFFFFF);

        g.pose().pushPose();
        g.pose().translate(bx + SP_BTN_W / 2f, by + 2, 50);
        g.pose().scale(0.8f, 0.8f, 1f);
        g.drawCenteredString(font, String.valueOf(brushSize), 0, 0, 0xFFFFFFFF);
        g.pose().popPose();
    }

    private void renderBrushShapeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        BrushShape[] shapes = BrushShape.values();
        int ddx = ddX(), ddy = topPos + SP_Y_BRUSH;
        int ddW = 54, ddH = shapes.length * 14 + 2;

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.fill(ddx, ddy, ddx + ddW, ddy + ddH, 0xEE0D0D0D);
        drawBorder(g, ddx, ddy, ddW, ddH, 0xFF666666);

        for (int i = 0; i < shapes.length; i++) {
            int iy = ddy + 1 + i * 14;
            boolean hov = mouseX >= ddx && mouseX < ddx + ddW
                    && mouseY >= iy && mouseY < iy + 12;
            boolean sel = shapes[i] == brushShape;
            if (hov || sel) g.fill(ddx + 1, iy, ddx + ddW - 1, iy + 12,
                    sel ? 0xFF2A2A4A : 0xFF2A2A2A);
            if (sel) g.fill(ddx + 1, iy, ddx + 3, iy + 12, 0xFFFFD700);
            renderShapeIcon(g, ddx + 5, iy + 3, shapes[i]);
            g.drawString(font, shapes[i].label(), ddx + 14, iy + 2,
                    sel ? 0xFFFFD700 : 0xFFCCCCCC, false);
        }
        g.pose().popPose();
    }

    private void renderGridSizeDropdown(GuiGraphics g, int mouseX, int mouseY) {
        int ddx = ddX(), ddy = topPos + SP_Y_GRID;
        int ddW = 54, ddH = GRID_SIZES.length * 14 + 2;

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.fill(ddx, ddy, ddx + ddW, ddy + ddH, 0xEE0D0D0D);
        drawBorder(g, ddx, ddy, ddW, ddH, 0xFF666666);

        for (int i = 0; i < GRID_SIZES.length; i++) {
            int iy = ddy + 1 + i * 14;
            boolean hov = mouseX >= ddx && mouseX < ddx + ddW
                    && mouseY >= iy && mouseY < iy + 12;
            boolean sel = GRID_SIZES[i] == gridSize;
            if (hov || sel) g.fill(ddx + 1, iy, ddx + ddW - 1, iy + 12,
                    sel ? 0xFF2A2A4A : 0xFF2A2A2A);
            if (sel) g.fill(ddx + 1, iy, ddx + 3, iy + 12, 0xFFFFD700);
            g.drawString(font, GRID_LABELS[i], ddx + 8, iy + 2,
                    sel ? 0xFFFFD700 : 0xFFCCCCCC, false);
        }
        g.pose().popPose();
    }

    private void renderBrushCursor(GuiGraphics g, int mouseX, int mouseY) {
        int originX = leftPos + PREV_X + 2;
        int originY = topPos + PREV_Y;

        int drawX, drawY;
        if (gridSize > 1) {
            int localX = mouseX - originX;
            int localY = mouseY - originY;
            drawX = originX + Math.floorDiv(localX, gridSize) * gridSize + gridSize / 2;
            drawY = originY + Math.floorDiv(localY, gridSize) * gridSize + gridSize / 2;
        } else {
            drawX = mouseX;
            drawY = mouseY;
        }

        int visualRadius = gridSize > 1 ? brushSize + gridSize / 2 : brushSize;

        g.enableScissor(originX, originY, originX + ELEM_W, originY + ELEM_H);
        g.pose().pushPose();
        g.pose().translate(0, 0, 50);
        int col = 0xCCFFFFFF;
        switch (brushShape) {
            case ROUND -> drawCircleOutline(g, drawX, drawY, visualRadius, col);
            case SQUARE -> drawSquareOutline(g, drawX, drawY, visualRadius, col);
            case DIAMOND -> drawDiamondOutline(g, drawX, drawY, visualRadius, col);
        }
        g.fill(drawX, drawY, drawX + 1, drawY + 1, 0xFFFFFFFF);
        g.pose().popPose();
        g.disableScissor();
    }

    private void drawCircleOutline(GuiGraphics g, int cx, int cy, int r, int color) {
        if (r <= 0) {
            g.fill(cx, cy, cx + 1, cy + 1, color);
            return;
        }
        int x = r, y = 0, err = 0;
        while (x >= y) {
            g.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
            g.fill(cx + y, cy + x, cx + y + 1, cy + x + 1, color);
            g.fill(cx - y, cy + x, cx - y + 1, cy + x + 1, color);
            g.fill(cx - x, cy + y, cx - x + 1, cy + y + 1, color);
            g.fill(cx - x, cy - y, cx - x + 1, cy - y + 1, color);
            g.fill(cx - y, cy - x, cx - y + 1, cy - x + 1, color);
            g.fill(cx + y, cy - x, cx + y + 1, cy - x + 1, color);
            g.fill(cx + x, cy - y, cx + x + 1, cy - y + 1, color);
            y++;
            err += 1 + 2 * y;
            if (2 * (err - x) + 1 > 0) {
                x--;
                err += 1 - 2 * x;
            }
        }
    }

    private void drawSquareOutline(GuiGraphics g, int cx, int cy, int r, int color) {
        g.fill(cx - r, cy - r, cx + r + 1, cy - r + 1, color);
        g.fill(cx - r, cy + r, cx + r + 1, cy + r + 1, color);
        g.fill(cx - r, cy - r, cx - r + 1, cy + r + 1, color);
        g.fill(cx + r, cy - r, cx + r + 1, cy + r + 1, color);
    }

    private void drawDiamondOutline(GuiGraphics g, int cx, int cy, int r, int color) {
        drawLine(g, cx, cy - r, cx + r, cy, color);
        drawLine(g, cx + r, cy, cx, cy + r, color);
        drawLine(g, cx, cy + r, cx - r, cy, color);
        drawLine(g, cx - r, cy, cx, cy - r, color);
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy, x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

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
        for (int px = 0; px < CP_W; px += 3) {
            float s = (float) px / CP_W;
            for (int py = 0; py < SV_H; py += 3) {
                float v = 1f - (float) py / SV_H;
                g.fill(cpAbsX + px, svAbsY + py, cpAbsX + px + 3, svAbsY + py + 3,
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

    private void renderPatternSelector(GuiGraphics g, int mouseX, int mouseY) {
        OWTeamMosaicPattern[] patterns = getOrderedPatterns();
        int maxV = calcMaxVisible();
        boolean hasOvf = patterns.length > maxV;
        int normalCount = hasOvf ? maxV - 1 : patterns.length;
        int totalBtns = hasOvf ? maxV : patterns.length;
        int rowW = totalBtns * PAT_BTN_SIZE + (totalBtns - 1) * PAT_BTN_GAP;
        int patStartX = leftPos + (PW - rowW) / 2;
        int patAbsY = topPos + PAT_Y;

        // Boutons normaux
        for (int i = 0; i < normalCount; i++) {
            int bx = patStartX + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
            renderOnePatternBtn(g, mouseX, mouseY, bx, patAbsY, patterns[i]);
        }

        // Bouton "..." si nécessaire
        if (hasOvf) {
            int dotsBtnX = patStartX + normalCount * (PAT_BTN_SIZE + PAT_BTN_GAP);
            boolean ovfActive = isOverflowPatternSelected(normalCount, patterns);
            boolean hov = mouseX >= dotsBtnX && mouseX < dotsBtnX + PAT_BTN_SIZE
                    && mouseY >= patAbsY && mouseY < patAbsY + PAT_BTN_SIZE;
            g.fill(dotsBtnX, patAbsY, dotsBtnX + PAT_BTN_SIZE, patAbsY + PAT_BTN_SIZE,
                    showPatternOverflow ? 0xFF555555 : (hov ? 0xFF3A3A3A : 0xFF1E1E1E));
            drawBorder(g, dotsBtnX, patAbsY, PAT_BTN_SIZE, PAT_BTN_SIZE,
                    ovfActive ? 0xFFFFD700 : (showPatternOverflow ? 0xFFAAAAAA : 0xFF555555));
            g.pose().pushPose();
            g.pose().translate(dotsBtnX + PAT_BTN_SIZE / 2f, patAbsY + 3, 50);
            g.pose().scale(0.65f, 0.65f, 1f);
            g.drawCenteredString(font, "+", 0, 0, ovfActive ? 0xFFFFD700 : 0xFFCCCCCC);
            g.pose().popPose();

            // Popup overflow
            if (showPatternOverflow) {
                int[] pb = calcOverflowPopupBounds(normalCount, patterns.length, dotsBtnX, patAbsY);
                int popX = pb[0], popY = pb[1], popW = pb[2];
                int count = patterns.length - normalCount;
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                g.fill(popX, popY, popX + popW, popY + PAT_BTN_SIZE + 8, 0xEE0D0D0D);
                drawBorder(g, popX, popY, popW, PAT_BTN_SIZE + 8, 0xFF666666);
                for (int i = 0; i < count; i++) {
                    int bx = popX + 4 + i * (PAT_BTN_SIZE + PAT_BTN_GAP);
                    renderOnePatternBtn(g, mouseX, mouseY, bx, popY + 4, patterns[normalCount + i]);
                }
                g.pose().popPose();
            }
        }
    }

    private int[] calcOverflowPopupBounds(int normalCount, int totalPatterns,
                                          int dotsBtnX, int patAbsY) {
        int count = totalPatterns - normalCount;
        int popW = count * PAT_BTN_SIZE + (count - 1) * PAT_BTN_GAP + 8;
        int popY = patAbsY - (PAT_BTN_SIZE + 8) - 4;
        int popX = dotsBtnX + PAT_BTN_SIZE - popW;
        if (popX < leftPos + 4) popX = leftPos + 4;
        if (popX + popW > leftPos + PW - 4) popX = leftPos + PW - 4 - popW;
        return new int[]{popX, popY, popW};
    }

    private void renderOnePatternBtn(GuiGraphics g, int mouseX, int mouseY,
                                     int bx, int by, OWTeamMosaicPattern pat) {
        boolean selected = pat == selectedPattern;
        boolean hovered = mouseX >= bx && mouseX < bx + PAT_BTN_SIZE
                && mouseY >= by && mouseY < by + PAT_BTN_SIZE;
        g.fill(bx, by, bx + PAT_BTN_SIZE, by + PAT_BTN_SIZE,
                selected ? 0xFF666666 : (hovered ? 0xFF3A3A3A : 0xFF1E1E1E));
        renderMiniPattern(g, bx + 1, by + 1, PAT_BTN_SIZE - 2, pat);
        drawBorder(g, bx, by, PAT_BTN_SIZE, PAT_BTN_SIZE,
                selected ? 0xFFFFD700 : 0xFF555555);
        if (hovered) {
            String label = Component.translatable(pat.getDisplayName()).getString();
            int ttW = this.font.width(label) + 6;
            int ttX = mouseX + 6, ttY = mouseY - 14;
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            g.fill(ttX - 1, ttY - 1, ttX + ttW + 1, ttY + 10, 0xFF000000);
            g.drawString(this.font, label, ttX + 2, ttY + 1, 0xFFFFFF, false);
            g.pose().popPose();
        }
    }

    private boolean isOverflowPatternSelected(int normalCount, OWTeamMosaicPattern[] patterns) {
        for (int i = normalCount; i < patterns.length; i++)
            if (patterns[i] == selectedPattern) return true;
        return false;
    }

    private int calcMaxVisible() {
        int n = OWTeamMosaicPattern.values().length;
        int usable = PW - 8;
        int totalW = n * PAT_BTN_SIZE + (n - 1) * PAT_BTN_GAP;
        if (totalW <= usable) return n;
        return (usable + PAT_BTN_GAP) / (PAT_BTN_SIZE + PAT_BTN_GAP);
    }

    private boolean isInPatternSelectorArea(double mx, double my) {
        OWTeamMosaicPattern[] patterns = getOrderedPatterns();
        int maxV = calcMaxVisible();
        boolean hasOvf = patterns.length > maxV;
        int totalBtns = hasOvf ? maxV : patterns.length;
        int rowW = totalBtns * PAT_BTN_SIZE + (totalBtns - 1) * PAT_BTN_GAP;
        int patStartX = leftPos + (PW - rowW) / 2;
        int patAbsY = topPos + PAT_Y;
        if (mx >= patStartX && mx < patStartX + rowW
                && my >= patAbsY && my < patAbsY + PAT_BTN_SIZE) return true;
        if (showPatternOverflow && hasOvf) {
            int normalCount = maxV - 1;
            int dotsBtnX = patStartX + normalCount * (PAT_BTN_SIZE + PAT_BTN_GAP);
            int[] pb = calcOverflowPopupBounds(normalCount, patterns.length, dotsBtnX, patAbsY);
            int popX = pb[0], popY = pb[1], popW = pb[2];
            if (mx >= popX && mx < popX + popW
                    && my >= popY && my < popY + PAT_BTN_SIZE + 8) return true;
        }
        return false;
    }

    private void renderMiniPattern(GuiGraphics g, int bx, int by, int size,
                                   OWTeamMosaicPattern pattern) {
        int p = primaryColor | 0xFF000000;
        int s = secondaryColor | 0xFF000000;
        int strips = Math.max(1, size / 3);

        switch (pattern) {
            case GRADIENT_DOWN -> {
                for (int i = 0; i < strips; i++) {
                    float t = (float) i / Math.max(1, strips - 1);
                    int col = 0xFF000000 | lerpColor(primaryColor, secondaryColor, t);
                    g.fill(bx, by + i * size / strips, bx + size, by + (i + 1) * size / strips, col);
                }
            }
            case GRADIENT_RIGHT -> {
                for (int i = 0; i < strips; i++) {
                    float t = (float) i / Math.max(1, strips - 1);
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
            case THIRDS_H -> {
                int t1 = Math.round(size / 3.0f);
                int t2 = size - t1;
                g.fill(bx, by, bx + size, by + t1, p);
                g.fill(bx, by + t1, bx + size, by + t2, s);
                g.fill(bx, by + t2, bx + size, by + size, p);
            }
            case THIRDS_V -> {
                int t1 = Math.round(size / 3.0f);
                int t2 = size - t1;
                g.fill(bx, by, bx + t1, by + size, p);
                g.fill(bx + t1, by, bx + t2, by + size, s);
                g.fill(bx + t2, by, bx + size, by + size, p);
            }
            case CIRCLE_PRI -> renderMiniCircle(g, bx, by, size, primaryColor, secondaryColor);
            case STRIPES -> {
                int total = 2 * size;
                for (int row = 0; row < size; row++) {
                    int b1 = total / 3 - row, b2 = total * 2 / 3 - row;
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
            case CUSTOM_PAINT -> {
                for (int py = 0; py < size; py++) {
                    for (int px = 0; px < size; px++) {
                        int origPx = px * ELEM_W / size;
                        int origPy = py * ELEM_H / size;
                        boolean useSec = paintPixels[origPy * ELEM_W + origPx];
                        g.fill(bx + px, by + py, bx + px + 1, by + py + 1, useSec ? s : p);
                    }
                }
                g.fill(bx + size / 2, by + size / 2, bx + size / 2 + 1, by + size / 2 + 1, 0xAAFFFFFF);
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
                    int x1 = (int) (size / 2f - halfW), x2 = (int) (size / 2f + halfW);
                    if (x1 > 0) g.fill(bx, by + row, bx + x1, by + row + 1, p);
                    if (x2 > x1) g.fill(bx + x1, by + row, bx + Math.min(x2, size), by + row + 1, s);
                    if (x2 < size) g.fill(bx + Math.max(x2, 0), by + row, bx + size, by + row + 1, p);
                }
            }
            case BOSS_PLANT_EMPRESS_PATTERN -> {
                for (int row = 0; row < size; row++) {
                    float wave = (float) (Math.sin(row * Math.PI * 2.0 / size) * size * 0.22f);
                    int split = Math.max(0, Math.min(size, (int) (size * 0.5f + wave)));
                    if (split > 0) g.fill(bx, by + row, bx + split, by + row + 1, p);
                    if (split < size) g.fill(bx + split, by + row, bx + size, by + row + 1, s);
                }
            }
        }
    }

    private void renderMiniCircle(GuiGraphics g, int bx, int by, int size, int bg, int circle) {
        float cx = size / 2f, cy = size / 2f, r = size / 3f;
        for (int y = 0; y < size; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= r) {
                float dx = (float) Math.sqrt(r * r - dy * dy);
                int x1 = Math.max(0, (int) (cx - dx)), x2 = Math.min(size, (int) (cx + dx));
                if (x1 > 0) g.fill(bx, by + y, bx + x1, by + y + 1, bg | 0xFF000000);
                if (x2 > x1) g.fill(bx + x1, by + y, bx + x2, by + y + 1, circle | 0xFF000000);
                if (x2 < size) g.fill(bx + x2, by + y, bx + size, by + y + 1, bg | 0xFF000000);
            } else {
                g.fill(bx, by + y, bx + size, by + y + 1, bg | 0xFF000000);
            }
        }
    }

    private void renderFlagWithPattern(GuiGraphics g, int fx, int fy,
                                       int primary, int secondary, OWTeamMosaicPattern pattern) {
        switch (pattern) {
            case GRADIENT_DOWN -> renderFlagGradientY(g, fx, fy, primary, secondary);
            case GRADIENT_RIGHT -> renderFlagGradientX(g, fx, fy, primary, secondary);
            case SPLIT_H -> {
                int half = (ELEM_H + 1) / 2;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, ELEM_W, half, primary);
                renderFlagRect(g, fx, fy + half, ELEM_U, ELEM_V + half, ELEM_W, ELEM_H - half, secondary);
            }
            case SPLIT_V -> {
                int half = (ELEM_W + 1) / 2;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, half, ELEM_H, primary);
                renderFlagRect(g, fx + half, fy, ELEM_U + half, ELEM_V, ELEM_W - half, ELEM_H, secondary);
            }
            case DIAGONAL_TL_BR -> renderFlagDiagonal(g, fx, fy, primary, secondary, false);
            case THIRDS_H -> {
                int t1 = ELEM_H / 3, t2 = ELEM_H * 2 / 3;
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, ELEM_W, t1, primary);
                renderFlagRect(g, fx, fy + t1, ELEM_U, ELEM_V + t1, ELEM_W, t2 - t1, secondary);
                renderFlagRect(g, fx, fy + t2, ELEM_U, ELEM_V + t2, ELEM_W, ELEM_H - t2, primary);
            }
            case THIRDS_V -> {
                int base = Math.round(ELEM_W / 3.0f);
                int t1 = base + 1, t2 = ELEM_W - (base - 1);
                renderFlagRect(g, fx, fy, ELEM_U, ELEM_V, t1, ELEM_H, primary);
                renderFlagRect(g, fx + t1, fy, ELEM_U + t1, ELEM_V, t2 - t1, ELEM_H, secondary);
                renderFlagRect(g, fx + t2, fy, ELEM_U + t2, ELEM_V, ELEM_W - t2, ELEM_H, primary);
            }
            case CIRCLE_PRI -> renderFlagCircle(g, fx, fy, primaryColor, secondaryColor);
            case STRIPES -> {
                int total = ELEM_W + ELEM_H;
                for (int row = 0; row < ELEM_H; row++) {
                    int b1 = total / 3 - row, b2 = total * 2 / 3 - row;
                    int s1 = Math.max(0, Math.min(b1, ELEM_W));
                    int s2 = Math.max(0, Math.min(b2, ELEM_W));
                    if (s1 > 0) renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, s1, 1, primary);
                    if (s2 > s1) renderFlagRect(g, fx + s1, fy + row, ELEM_U + s1, ELEM_V + row, s2 - s1, 1, secondary);
                    if (ELEM_W > s2)
                        renderFlagRect(g, fx + s2, fy + row, ELEM_U + s2, ELEM_V + row, ELEM_W - s2, 1, primary);
                }
            }
            case CHECKER -> {
                int hw = (ELEM_W + 1) / 2 + 1, hh = (ELEM_H + 1) / 2;
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
                    int x1 = (int) (ELEM_W / 2f + 1f - halfW), x2 = (int) (ELEM_W / 2f + 1f + halfW);
                    if (x1 > 0) renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, x1, 1, primary);
                    if (x2 > x1) renderFlagRect(g, fx + x1, fy + row, ELEM_U + x1, ELEM_V + row, x2 - x1, 1, secondary);
                    if (x2 < ELEM_W)
                        renderFlagRect(g, fx + x2, fy + row, ELEM_U + x2, ELEM_V + row, ELEM_W - x2, 1, primary);
                }
            }
            case BOSS_PLANT_EMPRESS_PATTERN -> {
                for (int row = 0; row < ELEM_H; row++) {
                    float wave = (float) (Math.sin(row * Math.PI * 2.0 / ELEM_H) * ELEM_W * 0.22f);
                    int split = Math.max(0, Math.min(ELEM_W, (int) (ELEM_W * 0.5f + wave)));
                    if (split > 0) renderFlagRect(g, fx, fy + row, ELEM_U, ELEM_V + row, split, 1, primary);
                    if (split < ELEM_W)
                        renderFlagRect(g, fx + split, fy + row, ELEM_U + split, ELEM_V + row, ELEM_W - split, 1, secondary);
                }
            }
            case CUSTOM_PAINT -> renderFlagCustomPaint(g, fx, fy, primary, secondary, paintPixels);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderFlagCustomPaint(GuiGraphics g, int fx, int fy,
                                       int primary, int secondary, boolean[] pixels) {
        for (int py = 0; py < ELEM_H; py++) {
            int startPx = 0;
            boolean curSec = pixels[py * ELEM_W];
            for (int px = 1; px <= ELEM_W; px++) {
                boolean nextSec = (px < ELEM_W) && pixels[py * ELEM_W + px];
                if (px == ELEM_W || nextSec != curSec) {
                    renderFlagRect(g, fx + startPx, fy + py,
                            ELEM_U + startPx, ELEM_V + py,
                            px - startPx, 1,
                            curSec ? secondary : primary);
                    startPx = px;
                    curSec = (px < ELEM_W) && pixels[py * ELEM_W + px];
                }
            }
        }
    }

    private void renderNameTakenOverlay(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 100);
        g.fill(leftPos, topPos, leftPos + PW, topPos + PH, 0xBB000000);

        int ow = 130, oh = 48;
        int ox = leftPos + (PW - ow) / 2, oy = topPos + (PH - oh) / 2;
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

    private void renderFlagCircle(GuiGraphics g, int fx, int fy, int bg, int circle) {
        float cx = ELEM_W / 2f + 1f, cy = ELEM_H / 2.5f;
        float rx = Math.min(ELEM_W, ELEM_H) / 3f + 1f;
        float ry = Math.min(ELEM_W, ELEM_H) / 3f;
        for (int y = 0; y < ELEM_H; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= ry) {
                float dx = rx * (float) Math.sqrt(1f - (dy * dy) / (ry * ry));
                int x1 = Math.max(0, (int) (cx - dx)), x2 = Math.min(ELEM_W, (int) (cx + dx));
                if (x1 > 0) renderFlagRect(g, fx, fy + y, ELEM_U, ELEM_V + y, x1, 1, bg);
                if (x2 > x1) renderFlagRect(g, fx + x1, fy + y, ELEM_U + x1, ELEM_V + y, x2 - x1, 1, circle);
                if (x2 < ELEM_W) renderFlagRect(g, fx + x2, fy + y, ELEM_U + x2, ELEM_V + y, ELEM_W - x2, 1, bg);
            } else {
                renderFlagRect(g, fx, fy + y, ELEM_U, ELEM_V + y, ELEM_W, 1, bg);
            }
        }
    }

    private void renderFlagGradientY(GuiGraphics g, int fx, int fy, int topColor, int botColor) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = lerpColor(topColor, botColor, t);
            int y0 = fy + i * ELEM_H / STRIPS, y1 = fy + (i + 1) * ELEM_H / STRIPS;
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
            int x0 = fx + i * ELEM_W / STRIPS, x1 = fx + (i + 1) * ELEM_W / STRIPS;
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
            int splitW = ELEM_W * i / STRIPS, priW = ELEM_W - splitW;
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

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int dimColor(int rgb) {
        return (((rgb >> 16) & 0xFF) / 2 << 16)
                | (((rgb >> 8) & 0xFF) / 2 << 8)
                | ((rgb & 0xFF) / 2);
    }

    private static int hsvToRgb(float h, float s, float v) {
        if (s == 0f) {
            int c = (int) (v * 255f);
            return (c << 16) | (c << 8) | c;
        }
        float hh = ((h % 1f) + 1f) % 1f * 6f;
        int i = (int) hh;
        float f = hh - i, p = v * (1f - s), q = v * (1f - f * s), t = v * (1f - (1f - f) * s);
        float r, gr, b;
        switch (i % 6) {
            case 0: r = v;  gr = t;  b = p;  break;
            case 1: r = q;  gr = v;  b = p;  break;
            case 2: r = p;  gr = v;  b = t;  break;
            case 3: r = p;  gr = q;  b = v;  break;
            case 4: r = t;  gr = p;  b = v;  break;
            default: r = v; gr = p;  b = q;  break;
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