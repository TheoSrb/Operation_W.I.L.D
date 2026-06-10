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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Rendu d'indications / didacticiels : un encart avec fondu d'entrée/sortie, fond stylé et texte traduit
 * (word-wrap automatique). Les indications s'enchaînent via une file d'attente (une à la fois), et chaque
 * indication est ancrée près de l'élément concerné du HUD (vie, énergie, cartes d'attaque…).
 */
@OnlyIn(Dist.CLIENT)
public class OWIndicationOverlay {

    /** Position d'ancrage de l'encart par rapport au HUD. */
    public enum Anchor { HEALTH, ENERGY, ATTACKS, CENTER, BOTTOM }

    private record Entry(Component text, long durationMs, Anchor anchor) {}

    private static final Deque<Entry> QUEUE = new ArrayDeque<>();
    private static Entry current = null;
    private static long startTime = -1L;

    private static final long FADE_IN_MS = 300L;
    private static final long FADE_OUT_MS = 450L;
    private static final int MAX_WIDTH = 200;
    private static final int PAD_X = 10;
    private static final int PAD_Y = 8;
    private static final int LINE_GAP = 2;
    private static final int ACCENT_COLOR = 0x6E8751;

    /** Ajoute une indication à la file (s'affichera après celles déjà en attente). */
    public static void enqueue(Component text, int durationTicks, Anchor anchor) {
        QUEUE.add(new Entry(text, Math.max(durationTicks, 1) * 50L, anchor));
    }

    /** Compat. API générique ({@code OWIndicationPacket}) : encart en bas. */
    public static void show(Component text, int durationTicks) {
        enqueue(text, durationTicks, Anchor.BOTTOM);
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

        // Avance dans la file si rien en cours ou si l'indication courante est terminée.
        if (current == null || now - startTime > current.durationMs) {
            current = QUEUE.poll();
            if (current == null) return;
            startTime = now;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f));
        }

        long el = now - startTime;
        float alpha;
        if (el < FADE_IN_MS) alpha = el / (float) FADE_IN_MS;
        else if (el > current.durationMs - FADE_OUT_MS) alpha = (current.durationMs - el) / (float) FADE_OUT_MS;
        else alpha = 1f;
        alpha = Mth.clamp(alpha, 0f, 1f);
        if (alpha <= 0.02f) return;

        Font font = mc.font;
        int maxWidth = Math.min(MAX_WIDTH, (int) (screenW * 0.6f));
        List<FormattedCharSequence> lines = font.split(current.text, maxWidth);
        if (lines.isEmpty()) return;

        int textW = 0;
        for (FormattedCharSequence line : lines) textW = Math.max(textW, font.width(line));
        int lineH = font.lineHeight + LINE_GAP;
        int textH = lines.size() * lineH - LINE_GAP;
        int boxW = textW + PAD_X * 2;
        int boxH = textH + PAD_Y * 2;

        int[] box = computeBox(current.anchor, screenW, screenH, boxW, boxH);
        int boxX = box[0];
        int boxY = box[1];
        int boxCenterX = boxX + boxW / 2;

        int aBg = (int) (alpha * 0xC8) & 0xFF;
        int aBorder = (int) (alpha * 0xCC) & 0xFF;
        int aText = (int) (alpha * 0xFF) & 0xFF;
        int panel = aBg << 24;
        int border = (aBorder << 24) | ACCENT_COLOR;
        int textColor = (aText << 24) | 0xFFFFFF;

        RenderSystem.enableBlend();

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

        RenderSystem.disableBlend();
    }

    /** Calcule le coin haut-gauche de l'encart selon l'ancrage, en restant dans l'écran. */
    private static int[] computeBox(Anchor anchor, int sw, int sh, int boxW, int boxH) {
        int cx = sw / 2;
        int anchorX;
        int anchorY;
        boolean above = true;

        switch (anchor) {
            case HEALTH -> { anchorX = cx + 50; anchorY = sh - 39; }   // barre de vie (cx+10 → cx+91)
            case ENERGY -> { anchorX = cx + 99; anchorY = sh - 39; }   // jauge d'énergie (cx+96)
            case ATTACKS -> { anchorX = cx + 110; anchorY = sh - 22; } // cartes d'attaque (cx+96)
            case CENTER -> { anchorX = cx; anchorY = (int) (sh * 0.40f); above = false; }
            default -> { anchorX = cx; anchorY = sh - 64 - boxH; above = false; } // BOTTOM
        }

        int boxX = anchorX - boxW / 2;
        int boxY = above ? anchorY - boxH - 6 : anchorY;

        boxX = Mth.clamp(boxX, 4, Math.max(4, sw - boxW - 4));
        boxY = Mth.clamp(boxY, 4, Math.max(4, sh - boxH - 4));
        return new int[]{boxX, boxY};
    }
}
