package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OWIndicationOverlay {

    public enum Anchor { HEALTH, ENERGY, ATTACKS, CENTER, BOTTOM }

    private record Entry(Component text, long durationMs, Anchor anchor, boolean holdAttackKey,
                         boolean hasPos, int posX, int posY, boolean above,
                         boolean hasGlow, int glowX, int glowY, int glowW, int glowH,
                         int maxWidth) {}

    private static final Deque<Entry> QUEUE = new ArrayDeque<>();
    private static Entry current = null;
    private static long startTime = -1L;

    private static long keyHoldStart = -1L;
    private static long holdSatisfiedAt = -1L;

    private static final long FADE_IN_MS = 300L;
    private static final long FADE_OUT_MS = 450L;
    private static final long HOLD_REQUIRED_MS = 1000L;
    private static final int MAX_WIDTH = 200;
    private static final int PAD_X = 10;
    private static final int PAD_Y = 8;
    private static final int LINE_GAP = 2;
    private static final int Z = 1000;
    private static final int ACCENT_COLOR = 0x6E8751;
    private static final int GLOW_COLOR = 0xFFE066;

    public static void enqueue(Component text, int durationTicks, Anchor anchor) {
        enqueue(text, durationTicks, anchor, false);
    }

    public static void enqueue(Component text, int durationTicks, Anchor anchor, boolean holdAttackKey) {
        QUEUE.add(new Entry(text, Math.max(durationTicks, 1) * 50L, anchor, holdAttackKey, false, 0, 0, true, false, 0, 0, 0, 0, 0));
    }

    public static void enqueue(Component text, int durationTicks, int anchorX, int anchorY, boolean above) {
        QUEUE.add(new Entry(text, Math.max(durationTicks, 1) * 50L, Anchor.CENTER, false, true, anchorX, anchorY, above, false, 0, 0, 0, 0, 0));
    }

    public static void enqueue(Component text, int durationTicks, int anchorX, int anchorY, boolean above,
                               int glowX, int glowY, int glowW, int glowH) {
        enqueue(text, durationTicks, anchorX, anchorY, above, glowX, glowY, glowW, glowH, 0);
    }

    public static void enqueue(Component text, int durationTicks, int anchorX, int anchorY, boolean above,
                               int glowX, int glowY, int glowW, int glowH, int maxWidth) {
        QUEUE.add(new Entry(text, Math.max(durationTicks, 1) * 50L, Anchor.CENTER, false, true, anchorX, anchorY, above,
                true, glowX, glowY, glowW, glowH, maxWidth));
    }

    public static void show(Component text, int durationTicks) {
        enqueue(text, durationTicks, Anchor.BOTTOM, false);
    }

    public static boolean isActive() {
        return current != null || !QUEUE.isEmpty();
    }

    public static void clear() {
        QUEUE.clear();
        current = null;
        startTime = -1L;
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long now = System.currentTimeMillis();

        if (current == null) {
            current = QUEUE.poll();
            if (current == null) return;
            startTime = now;
            keyHoldStart = -1L;
            holdSatisfiedAt = -1L;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f));
        }

        long el = now - startTime;
        float alpha = computeAlphaAndAdvance(now, el);
        if (current == null) return;
        if (alpha <= 0.02f) return;

        Font font = mc.font;
        int baseMax = current.maxWidth > 0 ? current.maxWidth : MAX_WIDTH;
        int maxWidth = Math.min(baseMax, (int) (screenW * 0.6f));
        List<FormattedCharSequence> lines = font.split(current.text, maxWidth);
        if (lines.isEmpty()) return;

        int textW = 0;
        for (FormattedCharSequence line : lines) textW = Math.max(textW, font.width(line));
        int lineH = font.lineHeight + LINE_GAP;
        int textH = lines.size() * lineH - LINE_GAP;
        int boxW = textW + PAD_X * 2;
        int boxH = textH + PAD_Y * 2;

        int[] box = computeBox(current, screenW, screenH, boxW, boxH);
        int boxX = box[0];
        int boxY = box[1];
        int boxCenterX = boxX + boxW / 2;

        int aBg = (int) (alpha * 0xFF) & 0xFF;
        int aBorder = (int) (alpha * 0xFF) & 0xFF;
        int aText = (int) (alpha * 0xFF) & 0xFF;
        int panel = aBg << 24;
        int border = (aBorder << 24) | ACCENT_COLOR;
        int textColor = (aText << 24) | 0xFFFFFF;

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        g.pose().pushPose();
        g.pose().translate(0, 0, Z);

        g.fill(boxX, boxY, boxX + boxW, boxY + boxH, panel);
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, border);
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, border);
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, border);
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, border);
        g.fill(boxX, boxY, boxX + 2, boxY + boxH, border);

        int ty = boxY + PAD_Y;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, boxCenterX - font.width(line) / 2, ty, textColor, true);
            ty += lineH;
        }

        int[] elem;
        float glowStrength;
        if (current.hasGlow) { elem = new int[]{current.glowX, current.glowY, current.glowW, current.glowH}; glowStrength = 0.65f; }
        else if (!current.hasPos) { elem = elementBounds(current.anchor, screenW, screenH); glowStrength = 0.06f; }
        else { elem = null; glowStrength = 0f; }
        if (elem != null) drawGlow(g, elem[0], elem[1], elem[2], elem[3], alpha, now, glowStrength);

        g.pose().popPose();
        g.flush();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static float computeAlphaAndAdvance(long now, long el) {
        Entry e = current;

        if (e.holdAttackKey) {
            if (el < FADE_IN_MS) return el / (float) FADE_IN_MS;

            if (holdSatisfiedAt < 0) {
                if (OWKeysBinding.OW_ATTACKS_INFO.isDown()) {
                    if (keyHoldStart < 0) keyHoldStart = now;
                    else if (now - keyHoldStart >= HOLD_REQUIRED_MS) holdSatisfiedAt = now;
                } else {
                    keyHoldStart = -1L;
                }
                return 1f;
            }

            long since = now - holdSatisfiedAt;
            if (since >= FADE_OUT_MS) { current = null; return 0f; }
            return 1f - since / (float) FADE_OUT_MS;
        }

        if (el > e.durationMs) { current = null; return 0f; }
        if (el < FADE_IN_MS) return el / (float) FADE_IN_MS;
        if (el > e.durationMs - FADE_OUT_MS) return (e.durationMs - el) / (float) FADE_OUT_MS;
        return 1f;
    }

    private static int[] computeBox(Entry e, int sw, int sh, int boxW, int boxH) {
        int cx = sw / 2;
        int anchorX, anchorY;
        boolean above;

        if (e.hasPos) {
            anchorX = e.posX;
            anchorY = e.posY;
            above = e.above;
        } else {
            above = true;
            switch (e.anchor) {
                case HEALTH -> { anchorX = cx + 50; anchorY = sh - 39; }
                case ENERGY -> { anchorX = cx + 100; anchorY = sh - 39; }
                case ATTACKS -> { anchorX = cx + 130; anchorY = sh - 22; }
                case CENTER -> { anchorX = cx; anchorY = (int) (sh * 0.40f) + 40; above = false; }
                default -> { anchorX = cx; anchorY = sh - 64 - boxH; above = false; }
            }
        }

        int boxX = anchorX - boxW / 2;
        int boxY = above ? anchorY - boxH - 8 : anchorY;
        boxX = Mth.clamp(boxX, 4, Math.max(4, sw - boxW - 4));
        boxY = Mth.clamp(boxY, 4, Math.max(4, sh - boxH - 4));
        return new int[]{boxX, boxY};
    }

    private static int[] elementBounds(Anchor anchor, int sw, int sh) {
        int cx = sw / 2;
        switch (anchor) {
            case HEALTH -> { return new int[]{cx + 10, sh - 39, 81, 9}; }
            case ENERGY -> { return new int[]{cx + 96, sh - 39, 8, 14}; }
            case ATTACKS -> { return new int[]{cx + 96, sh - 22, 20 + attackCardCount() * 25, 20}; }
            default -> { return null; }
        }
    }

    private static int attackCardCount() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getRootVehicle() instanceof OWEntity ow) {
            try {
                return OWAttacksHandler.getAttacks(ow.getClass()).size();
            } catch (Exception ignored) {
            }
        }
        return 4;
    }

    private static void drawGlow(GuiGraphics g, int x, int y, int w, int h, float baseAlpha, long now, float strength) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(now / 180.0);
        for (int i = 3; i >= 1; i--) {
            int inflate = i * 2;
            int a = (int) (Mth.clamp(baseAlpha * pulse * (strength / i), 0f, 1f) * 255);
            if (a <= 2) continue;
            int color = (a << 24) | GLOW_COLOR;
            int x0 = x - inflate, y0 = y - inflate, x1 = x + w + inflate, y1 = y + h + inflate;
            g.fill(x0, y0, x1, y0 + 2, color);
            g.fill(x0, y1 - 2, x1, y1, color);
            g.fill(x0, y0, x0 + 2, y1, color);
            g.fill(x1 - 2, y0, x1, y1, color);
        }
    }
}
