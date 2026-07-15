package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

/**
 * Rendu partagé d'une bannière de tribu : dessine le motif de couleurs ({@link OWTeamMosaicPattern})
 * <b>masqué par la silhouette</b> d'une {@link OWTeamBannerShape}, puis superpose les highlights.
 *
 * <p>Technique identique au rendu de drapeau historique : on blit une région-silhouette (base de la
 * forme) tintée à la couleur voulue ; les pixels transparents de la silhouette restent transparents,
 * ce qui découpe le motif à la forme. Les sous-rectangles de couleur partagent la même silhouette.</p>
 */
public final class OWBannerRenderer {

    public static final int W = OWTeamBannerShape.BASE_W; // 55
    public static final int H = OWTeamBannerShape.BASE_H; // 93

    private OWBannerRenderer() {}

    /** Version 2 couleurs (rétrocompatible). */
    public static void render(GuiGraphics g, int x, int y, OWTeamBannerShape shape,
                              int primary, int secondary, OWTeamMosaicPattern pattern, byte[] paintPixels) {
        render(g, x, y, shape, primary, secondary, secondary, false, pattern, paintPixels);
    }

    /**
     * Dessine la bannière complète avec une 3ᵉ couleur optionnelle. Si {@code useTertiary} est faux,
     * le rendu est identique à la version 2 couleurs. Sinon, les motifs qui le supportent (tiers H/V,
     * rayures, dégradés) utilisent {@code tertiary} comme 3ᵉ teinte.
     */
    public static void render(GuiGraphics g, int x, int y, OWTeamBannerShape shape,
                              int primary, int secondary, int tertiary, boolean useTertiary,
                              OWTeamMosaicPattern pattern, byte[] paintPixels) {
        if (shape == null) shape = OWTeamBannerShape.CLASSIC;
        if (pattern == null) pattern = OWTeamMosaicPattern.GRADIENT_DOWN;

        renderPattern(g, x, y, shape, primary, secondary, tertiary, useTertiary, pattern, paintPixels);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        renderHighlights(g, x, y, shape);
    }

    private static void renderHighlights(GuiGraphics g, int x, int y, OWTeamBannerShape shape) {
        int hlH = shape.getHighlightH();
        if (hlH <= 0) return;
        // Aligné en bas de la bannière.
        int drawY = y + H - hlH;
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blit(OWTeamBannerShape.TEXTURE, x, drawY,
                shape.getHighlightU(), shape.getHighlightV(), shape.getHighlightW(), hlH);
    }

    // ── Motifs ─────────────────────────────────────────────────────────────────
    private static void renderPattern(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                                      int primary, int secondary, int tertiary, boolean useTertiary,
                                      OWTeamMosaicPattern pattern, byte[] paintPixels) {
        // 3ᵉ bande = tertiary si activé, sinon on retombe sur primary (comportement 2 couleurs).
        int third = useTertiary ? tertiary : primary;
        switch (pattern) {
            case GRADIENT_DOWN -> gradientY(g, fx, fy, shape, primary, secondary, tertiary, useTertiary);
            case GRADIENT_RIGHT -> gradientX(g, fx, fy, shape, primary, secondary, tertiary, useTertiary);
            case SPLIT_H -> {
                int half = H / 2;
                rect(g, fx, fy, shape, 0, 0, W, half, primary);
                rect(g, fx, fy, shape, 0, half, W, H - half, secondary);
            }
            case SPLIT_V -> {
                int half = W / 2;
                rect(g, fx, fy, shape, 0, 0, half, H, primary);
                rect(g, fx, fy, shape, half, 0, W - half, H, secondary);
            }
            case DIAGONAL_TL_BR -> {
                if (useTertiary) diagonal3(g, fx, fy, shape, primary, secondary, tertiary);
                else diagonal(g, fx, fy, shape, primary, secondary);
            }
            case THIRDS_H -> {
                int t1 = H / 3, t2 = H * 2 / 3;
                rect(g, fx, fy, shape, 0, 0, W, t1, primary);
                rect(g, fx, fy, shape, 0, t1, W, t2 - t1, secondary);
                rect(g, fx, fy, shape, 0, t2, W, H - t2, third);
            }
            case THIRDS_V -> {
                int t1 = W / 3, t2 = W * 2 / 3;
                rect(g, fx, fy, shape, 0, 0, t1, H, primary);
                rect(g, fx, fy, shape, t1, 0, t2 - t1, H, secondary);
                rect(g, fx, fy, shape, t2, 0, W - t2, H, third);
            }
            case CIRCLE_PRI -> circle(g, fx, fy, shape, primary, secondary);
            case STRIPES -> {
                int total = W + H;
                for (int row = 0; row < H; row++) {
                    int b1 = total / 3 - row, b2 = total * 2 / 3 - row;
                    int s1 = Math.max(0, Math.min(b1, W));
                    int s2 = Math.max(0, Math.min(b2, W));
                    if (s1 > 0) rect(g, fx, fy, shape, 0, row, s1, 1, primary);
                    if (s2 > s1) rect(g, fx, fy, shape, s1, row, s2 - s1, 1, secondary);
                    if (W > s2) rect(g, fx, fy, shape, s2, row, W - s2, 1, third);
                }
            }
            case CHECKER -> {
                int hw = W / 2, hh = H / 2;
                rect(g, fx, fy, shape, 0, 0, hw, hh, primary);
                rect(g, fx, fy, shape, hw, 0, W - hw, hh, secondary);
                rect(g, fx, fy, shape, 0, hh, hw, H - hh, secondary);
                rect(g, fx, fy, shape, hw, hh, W - hw, H - hh, primary);
            }
            case DIAMOND -> {
                float scale = 0.78f;
                float halfH = (H / 2f) * scale;
                for (int row = 0; row < H; row++) {
                    float distY = Math.abs(row - H / 2f);
                    if (distY >= halfH) { rect(g, fx, fy, shape, 0, row, W, 1, primary); continue; }
                    float halfW = (1f - distY / halfH) * (W / 2f) * scale;
                    int x1 = (int) (W / 2f - halfW), x2 = (int) (W / 2f + halfW);
                    if (x1 > 0) rect(g, fx, fy, shape, 0, row, x1, 1, primary);
                    if (x2 > x1) rect(g, fx, fy, shape, x1, row, x2 - x1, 1, secondary);
                    if (x2 < W) rect(g, fx, fy, shape, x2, row, W - x2, 1, primary);
                }
            }
            case CUSTOM_PAINT -> {
                if (paintPixels != null && paintPixels.length >= W * H) {
                    customPaint(g, fx, fy, shape, primary, secondary, tertiary, paintPixels);
                } else {
                    rect(g, fx, fy, shape, 0, 0, W, H, primary);
                }
            }
            default -> rect(g, fx, fy, shape, 0, 0, W, H, primary);
        }
    }

    private static void customPaint(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                                    int primary, int secondary, int tertiary, byte[] pixels) {
        int[] palette = { primary, secondary, tertiary };
        for (int py = 0; py < H; py++) {
            int startPx = 0;
            int cur = pixels[py * W] & 3;
            for (int px = 1; px <= W; px++) {
                int next = (px < W) ? (pixels[py * W + px] & 3) : -1;
                if (px == W || next != cur) {
                    rect(g, fx, fy, shape, startPx, py, px - startPx, 1, palette[Math.min(cur, 2)]);
                    startPx = px;
                    cur = next;
                }
            }
        }
    }

    /** Diagonale à 3 bandes (teinte1 → teinte2 → teinte3) selon la valeur diagonale x/W + y/H. */
    private static void diagonal3(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                                  int a, int b, int c) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            int y0 = i * H / STRIPS;
            int stripH = (i + 1) * H / STRIPS - y0;
            float t = (i + 0.5f) / STRIPS;
            int x1 = Math.max(0, Math.min(W, Math.round(W * (2f / 3f - t))));
            int x2 = Math.max(0, Math.min(W, Math.round(W * (4f / 3f - t))));
            if (x1 > 0) rect(g, fx, fy, shape, 0, y0, x1, stripH, a);
            if (x2 > x1) rect(g, fx, fy, shape, x1, y0, x2 - x1, stripH, b);
            if (W > x2) rect(g, fx, fy, shape, x2, y0, W - x2, stripH, c);
        }
    }

    private static void gradientY(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                                  int a, int b, int c, boolean useThree) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = threeStop(a, b, c, useThree, t);
            int y0 = i * H / STRIPS, y1 = (i + 1) * H / STRIPS;
            rect(g, fx, fy, shape, 0, y0, W, y1 - y0, col);
        }
    }

    private static void gradientX(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                                  int a, int b, int c, boolean useThree) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            float t = (float) i / (STRIPS - 1);
            int col = threeStop(a, b, c, useThree, t);
            int x0 = i * W / STRIPS, x1 = (i + 1) * W / STRIPS;
            rect(g, fx, fy, shape, x0, 0, x1 - x0, H, col);
        }
    }

    /** Dégradé à 2 stops (a→b) ou 3 stops (a→b→c) selon {@code useThree}. */
    private static int threeStop(int a, int b, int c, boolean useThree, float t) {
        if (!useThree) return lerp(a, b, t);
        return t < 0.5f ? lerp(a, b, t * 2f) : lerp(b, c, (t - 0.5f) * 2f);
    }

    private static void diagonal(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape, int primary, int secondary) {
        final int STRIPS = 32;
        for (int i = 0; i < STRIPS; i++) {
            int y0 = i * H / STRIPS;
            int stripH = (i + 1) * H / STRIPS - y0;
            int splitW = W * i / STRIPS;
            int priW = W - splitW;
            if (priW > 0) rect(g, fx, fy, shape, 0, y0, priW, stripH, primary);
            if (splitW > 0) rect(g, fx, fy, shape, priW, y0, splitW, stripH, secondary);
        }
    }

    private static void circle(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape, int bg, int circ) {
        float cx = W / 2f, cy = H / 2.5f;
        float rx = Math.min(W, H) / 3f + 1f;
        float ry = Math.min(W, H) / 3f;
        for (int y = 0; y < H; y++) {
            float dy = y - cy;
            if (Math.abs(dy) <= ry) {
                float dx = rx * (float) Math.sqrt(1f - (dy * dy) / (ry * ry));
                int x1 = Math.max(0, (int) (cx - dx));
                int x2 = Math.min(W, (int) (cx + dx));
                if (x1 > 0) rect(g, fx, fy, shape, 0, y, x1, 1, bg);
                if (x2 > x1) rect(g, fx, fy, shape, x1, y, x2 - x1, 1, circ);
                if (x2 < W) rect(g, fx, fy, shape, x2, y, W - x2, 1, bg);
            } else {
                rect(g, fx, fy, shape, 0, y, W, 1, bg);
            }
        }
    }

    /**
     * Blit d'un sous-rectangle (localX, localY, w, h) de la silhouette de {@code shape}, tinté en {@code color}.
     * La transparence de la silhouette découpe le rectangle à la forme.
     */
    private static void rect(GuiGraphics g, int fx, int fy, OWTeamBannerShape shape,
                             int localX, int localY, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        setColor(color);
        ResourceLocation tex = OWTeamBannerShape.TEXTURE;
        g.blit(tex, fx + localX, fy + localY,
                shape.getBaseU() + localX, shape.getBaseV() + localY, w, h);
    }

    private static void setColor(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255f;
        float gc = ((hex >> 8) & 0xFF) / 255f;
        float b = (hex & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, gc, b, 1f);
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }
}
