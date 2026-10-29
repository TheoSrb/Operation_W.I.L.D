package net.tiew.operationWild.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;

public class OWUtilsOverlay {

    private static final int H = 32;
    private static final int MIN_W = 150;
    private static final int TEXT_X = 33;
    private static final int TOP = 8;
    private static final long ENTER_MS = 420L;
    private static final long EXIT_MS = 260L;
    private static final long FRAME_GAP_MS = 300L;
    private static final long FIRST_SWEEP_MS = 300L;
    private static final long SWEEP_EVERY_MS = 4200L;
    private static final long SWEEP_MS = 900L;
    private static final int SPARK = 0xFFFFF3C4;

    private static boolean visible;
    private static int shownId = -1;
    private static long enterAt;
    private static long exitAt;
    private static long lastFrame;
    private static Component title = Component.empty();
    private static Component subtitle = Component.empty();
    private static int accent = OWDailyQuestToast.GOLD;

    public static void render(GuiGraphics g, int screenWidth, int screenHeight, boolean active) {
        long now = Util.getMillis();
        if (now - lastFrame > FRAME_GAP_MS) visible = false;
        lastFrame = now;

        Player rider = Minecraft.getInstance().player;
        OWEntity entity = rider != null && rider.getVehicle() instanceof OWEntity owEntity && !(owEntity instanceof Submarine)
                ? owEntity : null;

        if (active && entity != null) {
            if (!visible || exitAt > 0 || entity.getId() != shownId) {
                if (!visible || entity.getId() != shownId) enterAt = now;
                visible = true;
                exitAt = 0;
                shownId = entity.getId();
            }
            capture(entity);
        } else if (visible && exitAt == 0) {
            exitAt = now;
        }
        if (!visible) return;
        if (exitAt > 0 && now - exitAt >= EXIT_MS) {
            visible = false;
            return;
        }

        Font font = Minecraft.getInstance().font;
        int w = Math.max(MIN_W, TEXT_X + Math.max(font.width(title), font.width(subtitle)) + 9);
        float hidden = -H - 4;
        float y = Mth.lerp(easeOutBack(progress(now - enterAt, ENTER_MS)), hidden, TOP);
        if (exitAt > 0) y = Mth.lerp(easeIn(progress(now - exitAt, EXIT_MS)), y, hidden);

        g.pose().pushPose();
        g.pose().translate((screenWidth - w) / 2f, y, 0f);
        OWDailyQuestToast.drawPanel(g, w, H, accent);
        OWDailyQuestToast.drawIconSlot(g, H);
        drawSpark(g, now - enterAt);
        g.drawString(font, title, TEXT_X, 7, OWDailyQuestToast.TEXT, false);
        g.drawString(font, subtitle, TEXT_X, 18, OWDailyQuestToast.MUTED, false);
        long sinceSweep = now - enterAt - FIRST_SWEEP_MS;
        if (sinceSweep >= 0 && sinceSweep % SWEEP_EVERY_MS < SWEEP_MS) {
            OWDailyQuestToast.drawSweep(g, w, H, (sinceSweep % SWEEP_EVERY_MS) / (float) SWEEP_MS);
        }
        g.pose().popPose();
    }

    private static void capture(OWEntity entity) {
        title = Component.translatable("tooltip.questsUpdated");
        String name = entity.getNickname();
        subtitle = name != null && !name.isEmpty() ? Component.literal(name) : entity.getType().getDescription();
        accent = 0xFF000000 | readable(entity.getEntityColor());
    }

    private static void drawSpark(GuiGraphics g, long time) {
        float wave = 0.5f + 0.5f * Mth.sin(time / 1000f * 4.2f);
        int x = 25;
        int y = (H - 20) / 2 - 1;
        int arm = wave > 0.66f ? 2 : 1;
        g.fill(x - arm, y, x + arm + 1, y + 1, SPARK);
        g.fill(x, y - arm, x + 1, y + arm + 1, SPARK);
        g.fill(x, y, x + 1, y + 1, 0xFFFFFFFF);
    }

    private static float progress(long elapsed, long duration) {
        return Mth.clamp(elapsed / (float) duration, 0f, 1f);
    }

    private static float easeOutBack(float t) {
        float c = 1.6f;
        float u = t - 1f;
        return 1f + (c + 1f) * u * u * u + c * u * u;
    }

    private static float easeIn(float t) {
        return t * t * t;
    }

    private static int readable(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        double lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        if (lum >= 0.35) return rgb & 0xFFFFFF;
        float amount = (float) ((0.35 - lum) / 0.35) * 0.7f;
        return mix(rgb, 0xFFFFFF, amount);
    }

    private static int mix(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }
}
