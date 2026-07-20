package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.tiew.operationWild.screen.tribe.OWBannerRenderer;
import net.tiew.operationWild.team.OWBannerLook;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

/**
 * Boîte à outils des cinématiques d'arène (choc d'ouverture, victoire) : bandes cinéma, vignette,
 * ondes de choc, rayons, éclats, et rendu de bannière.
 *
 * <p><b>Contrainte à connaître</b> : {@link OWBannerRenderer} réinitialise la couleur du shader en
 * interne et force l'alpha à 1 sur chaque région. Une bannière ne peut donc <b>pas</b> être rendue
 * translucide. Tous les effets d'apparition et de disparition passent par des déplacements, des
 * découpes ({@code scissor}) ou des aplats posés par-dessus — jamais par un fondu de la bannière.</p>
 */
public final class OWCinematicFx {

    private OWCinematicFx() {}

    public static final int BAR_H = 40;

    // ── Courbes ──────────────────────────────────────────────────────────────────
    public static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    /** Départ lent puis accélération franche. */
    public static float easeIn(float t) { float c = clamp(t); return c * c * c; }

    /** Arrivée amortie. */
    public static float easeOut(float t) { float c = clamp(t); return 1f - (1f - c) * (1f - c) * (1f - c); }

    // ── Cadre cinéma ─────────────────────────────────────────────────────────────
    /** Bandes noires liserées d'or, en haut et en bas. */
    public static void drawBars(GuiGraphics g, int w, int h, float amount) {
        int barH = (int) (BAR_H * easeOut(amount));
        if (barH <= 0) return;
        g.fill(0, 0, w, barH, 0xFF000000);
        g.fill(0, h - barH, w, h, 0xFF000000);
        g.fill(0, barH, w, barH + 1, 0x66FFD257);
        g.fill(0, h - barH - 1, w, h - barH, 0x66FFD257);
    }

    /** Assombrissement progressif des bords, pour concentrer le regard au centre. */
    public static void drawVignette(GuiGraphics g, int w, int h, float amount) {
        if (amount <= 0.01f) return;
        int steps = 14;
        for (int i = 0; i < steps; i++) {
            int a = (int) (12 * amount * (1f - i / (float) steps));
            if (a <= 0) continue;
            int inset = i * 6;
            g.fill(inset, inset, w - inset, inset + 6, a << 24);
            g.fill(inset, h - inset - 6, w - inset, h - inset, a << 24);
            g.fill(inset, inset, inset + 6, h - inset, a << 24);
            g.fill(w - inset - 6, inset, w - inset, h - inset, a << 24);
        }
    }

    // ── Effets d'impact ──────────────────────────────────────────────────────────
    /** Anneaux concentriques qui s'écartent d'un point. */
    /**
     * Disque plein translucide, tracé par lignes de balayage.
     *
     * <p>{@code GuiGraphics} ne sait remplir que des rectangles ; approcher un halo par un carré se
     * voit immédiatement dès que le rayon dépasse quelques dizaines de pixels — le « halo » devient
     * une plaque. Une demi-largeur calculée par ligne donne un vrai cercle pour un coût négligeable
     * à ces tailles.</p>
     */
    public static void drawDisc(GuiGraphics g, int cx, int cy, int radius, int argb) {
        if (radius <= 0 || (argb >>> 24) == 0) return;
        int r2 = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.sqrt(Math.max(0, r2 - dy * dy));
            if (half <= 0) continue;
            g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, argb);
        }
    }

    /**
     * Halo radial : disques concentriques d'opacité croissante vers le centre. Rend une vraie
     * lueur ronde là où des rectangles empilés donnaient des angles bien visibles.
     */
    public static void drawGlow(GuiGraphics g, int cx, int cy, int radius, int rgb, float intensity) {
        if (intensity <= 0.01f || radius <= 0) return;
        final int layers = 5;
        int base = rgb & 0xFFFFFF;
        for (int i = layers; i >= 1; i--) {
            int r = radius * i / layers;
            int a = (int) (26 * intensity * (1f - (i - 1) / (float) layers));
            if (a <= 0) continue;
            drawDisc(g, cx, cy, r, (Math.min(255, a) << 24) | base);
        }
    }

    /** Anneau circulaire d'épaisseur {@code thickness}, tracé par lignes de balayage. */
    public static void drawRing(GuiGraphics g, int cx, int cy, int radius, int thickness, int argb) {
        if (radius <= 0 || thickness <= 0 || (argb >>> 24) == 0) return;
        int inner = Math.max(0, radius - thickness);
        int r2 = radius * radius, i2 = inner * inner;
        for (int dy = -radius; dy <= radius; dy++) {
            int outerHalf = (int) Math.sqrt(Math.max(0, r2 - dy * dy));
            if (outerHalf <= 0) continue;
            int innerHalf = Math.abs(dy) < inner ? (int) Math.sqrt(Math.max(0, i2 - dy * dy)) : 0;
            if (innerHalf <= 0) {
                g.fill(cx - outerHalf, cy + dy, cx + outerHalf, cy + dy + 1, argb);
            } else {
                g.fill(cx - outerHalf, cy + dy, cx - innerHalf, cy + dy + 1, argb);
                g.fill(cx + innerHalf, cy + dy, cx + outerHalf, cy + dy + 1, argb);
            }
        }
    }

    public static void drawShockwave(GuiGraphics g, int cx, int cy, float since, float fade) {
        for (int k = 0; k < 4; k++) {
            float p = since * 1.5f - k * 0.13f;
            if (p <= 0f || p > 1f) continue;
            int r = (int) (20 + p * 360);
            int alpha = (int) (165 * (1f - p) * fade);
            if (alpha <= 0) continue;
            int thickness = Math.max(1, (int) (7 * (1f - p)));
            drawRing(g, cx, cy, r, thickness, (alpha << 24) | 0xFFFFFF);
        }
    }

    /** Rayons de lumière jaillissant d'un point. {@code rays} : {angle, longueur, épaisseur}. */
    public static void drawRays(GuiGraphics g, int cx, int cy, java.util.List<float[]> rays,
                                float since, float fade, int color, float duration) {
        float life = 1f - clamp(since / duration);
        if (life <= 0f) return;
        int alpha = (int) (170 * life * fade);
        if (alpha <= 0) return;
        for (float[] ray : rays) {
            float len = ray[1] * (0.35f + 0.65f * clamp(since / (duration * 0.4f)));
            int thick = Math.max(1, (int) (ray[2] * life));
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().mulPose(Axis.ZP.rotation(ray[0]));
            g.fill(14, -thick / 2 - 1, (int) len, -thick / 2 - 1 + thick, (alpha << 24) | (color & 0xFFFFFF));
            g.pose().popPose();
        }
    }

    /**
     * Éclats projetés depuis un point. {@code sparks} : {angle, vitesse px/s, taille, teinte}.
     * La teinte choisit entre {@code colorA} (&lt; 0.5) et {@code colorB}.
     */
    public static void drawSparks(GuiGraphics g, int cx, int cy, java.util.List<float[]> sparks,
                                  float since, float fade, int colorA, int colorB, float duration) {
        float life = 1f - clamp(since / duration);
        if (life <= 0f) return;
        for (float[] s : sparks) {
            float px = cx + (float) Math.cos(s[0]) * s[1] * since;
            float py = cy + (float) Math.sin(s[0]) * s[1] * since + 0.5f * 230f * since * since;
            int alpha = (int) (255 * life * fade);
            if (alpha <= 0) continue;
            int color = s[3] < 0.5f ? colorA : colorB;
            int size = Math.max(1, (int) (s[2] * (0.35f + 0.65f * life)));
            int ix = (int) px, iy = (int) py;
            g.fill(ix - size, iy - size, ix + size, iy + size, (alpha << 24) | (color & 0xFFFFFF));
            g.fill(ix - 1, iy - 1, ix + 1, iy + 1, (Math.min(255, alpha + 70) << 24) | 0xFFFFFF);
        }
    }

    // ── Bannières ────────────────────────────────────────────────────────────────
    /** Largeur de rendu d'une bannière pour une hauteur donnée. */
    public static float bannerWidth(int height) {
        return OWBannerRenderer.W * (height / (float) OWBannerRenderer.H);
    }

    /** Dessine une bannière à l'échelle voulue, éventuellement inclinée autour de son centre. */
    public static void drawBanner(GuiGraphics g, OWBannerLook look, int x, int y, int height, float tiltDegrees) {
        if (look == null) return;
        float s = height / (float) OWBannerRenderer.H;
        g.pose().pushPose();
        g.pose().translate(x + OWBannerRenderer.W * s / 2f, y + height / 2f, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(tiltDegrees));
        g.pose().scale(s, s, 1f);
        g.pose().translate(-OWBannerRenderer.W / 2f, -OWBannerRenderer.H / 2f, 0);
        OWBannerRenderer.render(g, 0, 0,
                OWTeamBannerShape.byId(look.shapeId()),
                look.primary(), look.secondary(), look.tertiary(), look.useTertiary(),
                OWTeamMosaicPattern.byId(look.patternId()), look.paintOrNull());
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /** Lignes de vitesse horizontales traînant derrière une bannière lancée. */
    public static void drawSpeedLines(GuiGraphics g, int x, int y, int w, int h,
                                      int color, float rush, boolean towardRight) {
        if (rush <= 0.05f) return;
        int base = color & 0xFFFFFF;
        for (int i = 0; i < 9; i++) {
            // Longueurs et hauteurs pseudo-aléatoires mais stables d'une frame à l'autre.
            float seed = (i * 37 % 11) / 11f;
            int lineY = y + (int) (h * (0.10f + 0.80f * ((i * 7 % 9) / 9f)));
            int len = (int) ((40 + seed * 130) * rush);
            int thick = 1 + (i % 3);
            int alpha = (int) (110 * rush * (1f - seed * 0.5f));
            if (alpha <= 0 || len <= 0) continue;
            int x1 = towardRight ? x - len : x + w;
            int x2 = towardRight ? x : x + w + len;
            g.fill(x1, lineY, x2, lineY + thick, (alpha << 24) | base);
        }
    }
}
