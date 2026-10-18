package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;

public final class OWMountAlertOverlay {

    public static final float WOUND_THRESHOLD = 0.35f;

    private static final ResourceLocation VIGNETTE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/mount_alert_vignette.png");
    private static final int VIGNETTE_SIZE = 256;

    private static final int WOUND_COLOR = 0xbc0c0c;
    private static final int FATIGUE_COLOR = 0x6442ac;

    private static final float WOUND_MIN_ALPHA = 0.35f;
    private static final float WOUND_MAX_ALPHA = 0.8f;
    private static final float WOUND_LIGHT_SPREAD = 1.45f;
    private static final float WOUND_HEAVY_SPREAD = 1.12f;
    private static final float WOUND_BEAT_ALPHA = 0.18f;
    private static final float WOUND_BEAT_SPREAD = 0.06f;

    private static final float WOUND_SLOW_PERIOD_MS = 1100f;
    private static final float WOUND_FAST_PERIOD_MS = 600f;
    private static final float LUB_CENTER = 0.08f;
    private static final float DUB_CENTER = 0.28f;
    private static final float BEAT_WIDTH = 0.11f;
    private static final float DUB_STRENGTH = 0.65f;

    private static final float FATIGUE_ALPHA = 0.65f;
    private static final float FATIGUE_SPREAD = 1.3f;
    private static final float FATIGUE_GROW_SPREAD = 0.08f;
    private static final float FATIGUE_RISE_MS = 140f;
    private static final float FATIGUE_FALL_MS = 520f;
    private static final long MAX_FRAME_GAP_MS = 1000L;

    private static float woundPhase;
    private static long lastWoundMs = -1L;
    private static float fatigueLevel;
    private static long lastFatigueMs = -1L;

    private OWMountAlertOverlay() {
    }

    public static void renderWound(GuiGraphics graphics, float healthRatio) {
        float severity = Mth.clamp(1f - healthRatio / WOUND_THRESHOLD, 0f, 1f);
        float eased = severity * (2f - severity);

        long now = Util.getMillis();
        if (lastWoundMs >= 0L) {
            float period = Mth.lerp(severity, WOUND_SLOW_PERIOD_MS, WOUND_FAST_PERIOD_MS);
            woundPhase = (woundPhase + Math.min(now - lastWoundMs, 250L) / period) % 1f;
        }
        lastWoundMs = now;

        float beat = heartbeat(woundPhase);
        float alpha = Mth.lerp(eased, WOUND_MIN_ALPHA, WOUND_MAX_ALPHA) * (1f - WOUND_BEAT_ALPHA + WOUND_BEAT_ALPHA * beat);
        float spread = Mth.lerp(eased, WOUND_LIGHT_SPREAD, WOUND_HEAVY_SPREAD) - WOUND_BEAT_SPREAD * beat;
        drawVignette(graphics, WOUND_COLOR, alpha, spread);
    }

    public static void renderFatigue(GuiGraphics graphics, boolean lacking) {
        long now = Util.getMillis();
        float elapsed = lastFatigueMs < 0L ? 0f : Math.min(now - lastFatigueMs, MAX_FRAME_GAP_MS);
        lastFatigueMs = now;
        if (lacking) fatigueLevel = Math.min(1f, fatigueLevel + elapsed / FATIGUE_RISE_MS);
        else fatigueLevel = Math.max(0f, fatigueLevel - elapsed / FATIGUE_FALL_MS);
        if (fatigueLevel <= 0.001f) return;

        float level = smoothStep(fatigueLevel);
        drawVignette(graphics, FATIGUE_COLOR, FATIGUE_ALPHA * level, FATIGUE_SPREAD - FATIGUE_GROW_SPREAD * level);
    }

    private static void drawVignette(GuiGraphics graphics, int rgb, float alpha, float spread) {
        if (alpha <= 0.002f) return;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int width = Math.round(screenWidth * spread);
        int height = Math.round(screenHeight * spread);
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f,
                Mth.clamp(alpha, 0f, 1f));
        graphics.blit(VIGNETTE, x, y, width, height, 0f, 0f, VIGNETTE_SIZE, VIGNETTE_SIZE, VIGNETTE_SIZE, VIGNETTE_SIZE);
        graphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private static float heartbeat(float phase) {
        return bump(phase, LUB_CENTER) + DUB_STRENGTH * bump(phase, DUB_CENTER);
    }

    private static float bump(float phase, float center) {
        float d = (phase - center) / BEAT_WIDTH;
        if (d <= -1f || d >= 1f) return 0f;
        float t = 1f - d * d;
        return t * t;
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }
}
