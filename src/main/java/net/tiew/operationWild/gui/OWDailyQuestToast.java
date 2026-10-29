package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;

@OnlyIn(Dist.CLIENT)
public class OWDailyQuestToast implements Toast {

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    private static final int W = 160;
    private static final int H = 32;
    private static final long DISPLAY_MS = 4500L;
    private static final long FILL_MS = 1400L;
    private static final long SWEEP_MS = 900L;

    private static final int OUTLINE = 0xFF0D0A07;
    private static final int BODY = 0xF2261F18;
    private static final int RIM_LIGHT = 0xFF4D4032;
    private static final int RIM_DARK = 0xFF140F0B;
    private static final int SLOT = 0xFF130F0B;
    private static final int SLOT_EDGE = 0xFF3A3024;
    static final int TEXT = 0xFFF4EBDD;
    static final int MUTED = 0xFFB3A07F;
    private static final int GAIN = 0xFF9BE36A;
    static final int GOLD = 0xFFF6C94E;
    private static final int GOLD_DARK = 0xFF9A6E1C;
    private static final int TRACK = 0xFF35291E;
    private static final int TRACK_EDGE = 0xFF1A130E;
    private static final int FILL = 0xFF7FCB4E;
    private static final int FILL_LIGHT = 0xFFB9F08C;
    private static final int FILL_NEW = 0xFFE6FFC9;

    private static final int TEXT_X = 33;
    private static final int BAR_X = 33;
    private static final int BAR_Y = 25;
    private static final int BAR_W = W - BAR_X - 8;
    private static final int SEGMENT = 4;
    private static final int SEGMENT_GAP = 1;

    private final Component title;
    private final int tierColor;
    private final Object token;
    private final boolean isProgress;
    private final int delta;
    private final int fromProg;
    private final int toProg;
    private final int max;
    private final Component completionText;

    private OWDailyQuestToast(Component title, int tierColor, Object token, boolean isProgress,
                              int delta, int fromProg, int toProg, int max, Component completionText) {
        this.title = title;
        this.tierColor = 0xFF000000 | (tierColor & 0xFFFFFF);
        this.token = token;
        this.isProgress = isProgress;
        this.delta = delta;
        this.fromProg = fromProg;
        this.toProg = toProg;
        this.max = max;
        this.completionText = completionText;
    }

    public static OWDailyQuestToast progress(Component questName, int delta, int prog, int max, int tierColor, int questId) {
        return new OWDailyQuestToast(questName, tierColor, Integer.valueOf(questId), true, delta,
                Math.max(0, prog - delta), prog, Math.max(1, max), null);
    }

    public static OWDailyQuestToast completed(Component questName, int tierColor) {
        return new OWDailyQuestToast(questName, tierColor, null, false, 0, 0, 0, 0,
                Component.translatable("tooltip.questFinished"));
    }

    @Override
    public Object getToken() {
        return token != null ? token : Toast.NO_TOKEN;
    }

    @Override
    public int width() {
        return W;
    }

    @Override
    public int height() {
        return H;
    }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent toastComponent, long timeSinceLastVisible) {
        Font font = Minecraft.getInstance().font;
        drawPanel(g, W, H, isProgress ? tierColor : GOLD);
        drawIconSlot(g, H);

        int reserved = isProgress ? font.width("+" + delta) + 6 : 0;
        g.drawString(font, fit(font, title, W - TEXT_X - 6 - reserved), TEXT_X, 6, TEXT, false);

        if (isProgress) {
            float t = ease(Mth.clamp(timeSinceLastVisible / (float) FILL_MS, 0f, 1f));
            String gain = "+" + delta;
            g.drawString(font, gain, W - 6 - font.width(gain), 6, GAIN, false);
            int shown = Math.round(Mth.lerp(t, fromProg, toProg));
            g.drawString(font, shown + " / " + max, TEXT_X, 15, MUTED, false);
            drawBar(g, fromProg / (float) max, Mth.lerp(t, fromProg, toProg) / max);
        } else {
            drawCheck(g, TEXT_X, 17);
            g.drawString(font, completionText, TEXT_X + 10, 16, GOLD, false);
            if (timeSinceLastVisible < SWEEP_MS) drawSweep(g, W, H, timeSinceLastVisible / (float) SWEEP_MS);
        }
        return timeSinceLastVisible < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }

    static void drawPanel(GuiGraphics g, int W, int H, int accent) {
        g.fill(1, 0, W - 1, H, OUTLINE);
        g.fill(0, 1, W, H - 1, OUTLINE);
        g.fill(1, 1, W - 1, H - 1, BODY);
        g.fill(2, 1, W - 2, 2, RIM_LIGHT);
        g.fill(1, 2, 2, H - 2, RIM_LIGHT);
        g.fill(2, H - 2, W - 2, H - 1, RIM_DARK);
        g.fill(W - 2, 2, W - 1, H - 2, RIM_DARK);
        g.fill(2, 3, 4, H - 3, accent);
    }

    static void drawIconSlot(GuiGraphics g, int H) {
        int x = 7;
        int y = (H - 20) / 2;
        g.fill(x, y, x + 20, y + 20, SLOT_EDGE);
        g.fill(x + 1, y + 1, x + 19, y + 19, SLOT);
        g.blit(ICON, x + 2, y + 2, 176, 48, 16, 16);
    }

    private static void drawBar(GuiGraphics g, float before, float now) {
        int filledBefore = Math.round(BAR_W * Mth.clamp(before, 0f, 1f));
        int filledNow = Math.round(BAR_W * Mth.clamp(now, 0f, 1f));
        g.fill(BAR_X - 1, BAR_Y - 1, BAR_X + BAR_W + 1, BAR_Y + 4, TRACK_EDGE);
        for (int x = 0; x < BAR_W; x += SEGMENT + SEGMENT_GAP) {
            int width = Math.min(SEGMENT, BAR_W - x);
            int left = BAR_X + x;
            g.fill(left, BAR_Y, left + width, BAR_Y + 3, TRACK);
            int lit = Mth.clamp(filledNow - x, 0, width);
            int kept = Mth.clamp(filledBefore - x, 0, lit);
            if (kept > 0) {
                g.fill(left, BAR_Y, left + kept, BAR_Y + 3, FILL);
                g.fill(left, BAR_Y, left + kept, BAR_Y + 1, FILL_LIGHT);
            }
            if (lit > kept) {
                g.fill(left + kept, BAR_Y, left + lit, BAR_Y + 3, FILL_NEW);
                g.fill(left + kept, BAR_Y, left + lit, BAR_Y + 1, 0xFFFFFFFF);
            }
        }
    }

    private static void drawCheck(GuiGraphics g, int x, int y) {
        int[][] pixels = {{0, 3}, {1, 4}, {2, 5}, {3, 4}, {4, 3}, {5, 2}, {6, 1}, {7, 0}};
        for (int[] p : pixels) {
            g.fill(x + p[0], y + p[1] + 1, x + p[0] + 1, y + p[1] + 2, GOLD_DARK);
            g.fill(x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, GOLD);
        }
    }

    static void drawSweep(GuiGraphics g, int W, int H, float t) {
        int center = Math.round(Mth.lerp(t, -20f, W + 20f));
        for (int i = -8; i <= 8; i++) {
            int x = center + i;
            if (x < 2 || x >= W - 2) continue;
            int alpha = Math.round(70 * (1f - Math.abs(i) / 9f) * (1f - t * 0.5f));
            g.fill(x, 2, x + 1, H - 2, (alpha << 24) | 0xFFE7A0);
        }
    }

    private static Component fit(Font font, Component text, int width) {
        if (font.width(text) <= width) return text;
        return Component.literal(font.plainSubstrByWidth(text.getString(), width - font.width("…")) + "…");
    }

    private static float ease(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }
}
