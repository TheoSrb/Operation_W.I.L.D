package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * Animation « RPG » de passage de niveau : des anneaux concentriques (à la couleur de l'entité)
 * jaillissent du centre de l'écran, avec le chiffre du nouveau niveau qui pop et un libellé traduit
 * en dessous. Entièrement procédural, déclenché par {@code OWLevelUpPacket}.
 */
public class OWLevelUpOverlay {

    private static long startTime = -1L;
    private static int number = 0;
    private static int accentColor = 0xFFFFFF;

    private static final long DURATION_MS   = 2400L;
    private static final long RING_INTERVAL = 260L;
    private static final long RING_LIFETIME = 1100L;
    private static final int  RING_COUNT    = 5;

    /** Couleur fixe du chiffre du niveau et du libellé. */
    private static final int NUMBER_COLOR = 0xA8E677;

    public static void trigger(int level, int entityColor) {
        // Anti-stacking : si une animation est déjà en cours (gain de plusieurs niveaux d'un coup),
        // on met juste à jour le chiffre sans rejouer le son par-dessus lui-même.
        boolean alreadyActive = startTime >= 0 && System.currentTimeMillis() - startTime < DURATION_MS;

        startTime = System.currentTimeMillis();
        number = level;
        accentColor = entityColor;

        if (!alreadyActive) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f));
            }
        }
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
        if (startTime < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long el = System.currentTimeMillis() - startTime;
        if (el > DURATION_MS) {
            startTime = -1L;
            return;
        }

        float t = el / (float) DURATION_MS;
        float globalAlpha;
        if (t < 0.06f)       globalAlpha = t / 0.06f;                                   // fade-in
        else if (t > 0.75f)  globalAlpha = Mth.clamp(1f - (t - 0.75f) / 0.25f, 0f, 1f); // fade-out
        else                 globalAlpha = 1f;

        int ringColor = accentColor & 0xFFFFFF; // anneaux à la couleur de l'entité
        float cx = screenW / 2f;
        float cy = screenH / 2f - 12f;
        float maxR = Math.min(screenW, screenH) * 0.42f;

        RenderSystem.enableBlend();

        // Anneaux concentriques qui s'étendent.
        for (int i = 0; i < RING_COUNT; i++) {
            long ringEl = el - i * RING_INTERVAL;
            if (ringEl < 0 || ringEl > RING_LIFETIME) continue;
            float rp = ringEl / (float) RING_LIFETIME;            // 0 → 1
            float eased = 1f - (float) Math.pow(1f - rp, 3);      // easeOutCubic
            drawRing(g, cx, cy, eased * maxR, ringColor, (1f - rp) * globalAlpha);
        }

        Font font = mc.font;

        // Chiffre central avec pop + léger pouls.
        float pop = 1f - (float) Math.pow(1f - Math.min(t / 0.16f, 1f), 3);
        float pulse = 1f + 0.04f * (float) Math.sin(el / 140.0);
        float numScale = (0.6f + 1.9f * pop) * pulse;
        String numStr = String.valueOf(number);
        int numAlpha = ((int) (globalAlpha * 255) & 0xFF) << 24;

        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(numScale, numScale, 1f);
        g.drawString(font, numStr, -font.width(numStr) / 2, -4, NUMBER_COLOR | numAlpha, true);
        g.pose().popPose();

        // Libellé traduit ("- Level Up -").
        Component label = Component.translatable("tooltip.levelUp");
        int labelAlpha = ((int) (globalAlpha * 255) & 0xFF) << 24;
        g.pose().pushPose();
        g.pose().translate(cx, cy + 28f, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        g.drawString(font, label, -font.width(label) / 2, 0, NUMBER_COLOR | labelAlpha, true);
        g.pose().popPose();

        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    /** Trace un anneau pointillé (points répartis sur le cercle) — pas de primitive cercle dans GuiGraphics. */
    private static void drawRing(GuiGraphics g, float cx, float cy, float radius, int rgb, float alpha) {
        if (alpha <= 0.01f || radius < 1f) return;
        int a = (int) (Mth.clamp(alpha, 0f, 1f) * 255);
        int color = (a << 24) | (rgb & 0xFFFFFF);
        int segments = (int) Mth.clamp(radius * 1.9f, 48, 280);
        int dot = (int) Mth.clamp(radius * 0.018f, 1f, 3f);
        for (int s = 0; s < segments; s++) {
            double ang = (Math.PI * 2.0 * s) / segments;
            int px = (int) (cx + Math.cos(ang) * radius);
            int py = (int) (cy + Math.sin(ang) * radius);
            g.fill(px - dot, py - dot, px + dot, py + dot, color);
        }
    }
}
