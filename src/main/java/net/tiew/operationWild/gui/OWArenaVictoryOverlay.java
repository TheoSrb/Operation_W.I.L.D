package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.tiew.operationWild.networking.packets.to_client.ArenaVictoryPacket;
import net.tiew.operationWild.screen.tribe.OWBannerRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cinématique de verdict : la bannière victorieuse s'élance et <b>fracasse</b> celle du vaincu, qui
 * se disloque en morceaux emportés par la gravité. L'étendard vainqueur reste ensuite seul au
 * centre, nom de tribu à l'appui.
 *
 * <p>L'éclatement n'est pas simulé avec des aplats de couleur : chaque morceau est un
 * <b>fragment réel de la bannière</b>, obtenu en la redessinant décalée à l'intérieur d'une découpe
 * ({@code scissor}) qui suit le morceau. On garde donc les vrais motifs, formes et peintures custom
 * — au prix d'une contrainte, les fragments ne peuvent pas tourner sur eux-mêmes (la découpe reste
 * alignée sur les axes).</p>
 */
public final class OWArenaVictoryOverlay {

    private OWArenaVictoryOverlay() {}

    // ── Découpage temporel (ms depuis le début) ──────────────────────────────────
    private static final long BARS_END    = 650;    // bandes cinéma, les deux étendards en place
    private static final long WINDUP_END  = 1450;   // le vainqueur prend son élan (recul)
    private static final long STRIKE_END  = 1800;   // il fond sur l'adversaire
    private static final long SHATTER_END = 4400;   // éclatement + retombée des morceaux
    private static final long HOLD_END    = 6000;   // l'étendard vainqueur seul, nom affiché
    private static final long TOTAL       = 6900;   // sortie

    private static final int BANNER_H = 132;

    /** Découpage de la bannière vaincue en morceaux. */
    private static final int COLS = 4, ROWS = 6;

    private static long startTime = -1L;
    private static ArenaVictoryPacket data = null;
    private static boolean strikeSoundPlayed = false;

    /** Vecteurs de dispersion par morceau : {vx, vy, retard}. */
    private static final float[][] shards = new float[COLS * ROWS][3];
    private static final List<float[]> sparks = new ArrayList<>();
    private static final List<float[]> rays = new ArrayList<>();

    public static void trigger(ArenaVictoryPacket packet) {
        data = packet;
        startTime = System.currentTimeMillis();
        strikeSoundPlayed = false;

        Random r = new Random();
        // Dispersion : les morceaux partent vers la droite (sens du coup) et vers le bas.
        for (int i = 0; i < shards.length; i++) {
            int col = i % COLS, row = i / COLS;
            float towardEdge = (col + 0.5f) / COLS;              // les bords fuient plus loin
            shards[i][0] = 90f + towardEdge * 320f + r.nextFloat() * 120f;
            shards[i][1] = -140f + (row / (float) ROWS) * 90f + r.nextFloat() * 90f;
            shards[i][2] = r.nextFloat() * 0.18f;                 // léger décalage d'éclatement
        }

        sparks.clear();
        for (int i = 0; i < 130; i++) {
            sparks.add(new float[]{
                    (float) (r.nextDouble() * Math.PI * 2),
                    80f + r.nextFloat() * 360f,
                    1.3f + r.nextFloat() * 3.6f,
                    r.nextBoolean() ? 0f : 1f });
        }
        rays.clear();
        for (int i = 0; i < 20; i++) {
            rays.add(new float[]{
                    (float) (r.nextDouble() * Math.PI * 2),
                    150f + r.nextFloat() * 300f,
                    2f + r.nextFloat() * 6f });
        }

        Minecraft mc = Minecraft.getInstance();
        // Le HUD n'est pas rendu tant qu'un écran est ouvert (cf. OWArenaClashOverlay).
        if (mc.screen != null) mc.setScreen(null);
    }

    public static void clear() {
        startTime = -1L;
        data = null;
        sparks.clear();
        rays.clear();
    }

    public static boolean isPlaying() {
        return startTime >= 0 && System.currentTimeMillis() - startTime < TOTAL;
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
        if (startTime < 0 || data == null) return;
        long t = System.currentTimeMillis() - startTime;
        if (t >= TOTAL) { clear(); return; }

        int cx = screenW / 2, cy = screenH / 2;
        float exit = t > HOLD_END ? 1f - (float) (t - HOLD_END) / (TOTAL - HOLD_END) : 1f;
        exit = OWCinematicFx.clamp(exit);

        float veil = OWCinematicFx.clamp(t < BARS_END ? (float) t / BARS_END : 1f) * exit;
        g.fill(0, 0, screenW, screenH, ((int) (228 * veil) << 24));
        OWCinematicFx.drawVignette(g, screenW, screenH, veil);
        OWCinematicFx.drawBars(g, screenW, screenH, veil);

        float bw = OWCinematicFx.bannerWidth(BANNER_H);
        int by = cy - BANNER_H / 2;
        float restGap = bw * 0.66f + 30f;

        boolean striking = t >= WINDUP_END && t < STRIKE_END;
        boolean shattered = t >= STRIKE_END;

        if (shattered && !strikeSoundPlayed) {
            strikeSoundPlayed = true;
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, 0.85f));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.8f));
        }

        // ── Secousse au moment de la casse ──────────────────────────────────────
        int shakeX = 0, shakeY = 0;
        if (shattered && t < STRIKE_END + 400) {
            float k = 1f - (float) (t - STRIKE_END) / 400f;
            shakeX = (int) (Math.sin(t / 15.0) * 13 * k);
            shakeY = (int) (Math.cos(t / 11.0) * 8 * k);
        }

        // ── Position du vainqueur : place → recul → frappe → centre ─────────────
        float winnerX;
        float tilt = 0f;
        if (t < BARS_END) {
            winnerX = -restGap;
        } else if (t < WINDUP_END) {
            // Recul : il arme le coup.
            float p = OWCinematicFx.easeOut((float) (t - BARS_END) / (WINDUP_END - BARS_END));
            winnerX = -restGap - 46f * p;
            tilt = -11f * p;
        } else if (striking) {
            float p = OWCinematicFx.easeIn((float) (t - WINDUP_END) / (STRIKE_END - WINDUP_END));
            winnerX = (-restGap - 46f) + (restGap + 46f + restGap) * p;
            tilt = -11f + 26f * p;
        } else {
            // Après la casse, il glisse au centre et se redresse.
            float p = OWCinematicFx.easeOut(
                    OWCinematicFx.clamp((float) (t - STRIKE_END) / (SHATTER_END - STRIKE_END)));
            winnerX = restGap * (1f - p);
            tilt = 15f * (1f - p);
        }

        // Lignes de vitesse pendant la frappe.
        if (striking) {
            float rush = (float) (t - WINDUP_END) / (STRIKE_END - WINDUP_END);
            OWCinematicFx.drawSpeedLines(g, (int) (cx + winnerX - bw / 2), by,
                    (int) bw, BANNER_H, data.winnerLook().primary(), rush, true);
        }

        // ── Bannière vaincue : intacte, puis en morceaux ────────────────────────
        int loserX = (int) (cx + restGap - bw / 2);
        if (!shattered) {
            OWCinematicFx.drawBanner(g, data.loserLook(), loserX, by, BANNER_H, 0f);
        } else {
            float since = (t - STRIKE_END) / 1000f;
            drawShatteredBanner(g, loserX + shakeX, by + shakeY, (int) bw, since, exit);
        }

        // ── Bannière victorieuse ────────────────────────────────────────────────
        OWCinematicFx.drawBanner(g, data.winnerLook(),
                (int) (cx + winnerX - bw / 2) + shakeX, by + shakeY, BANNER_H, tilt);

        // ── Impact ──────────────────────────────────────────────────────────────
        if (shattered) {
            float since = (t - STRIKE_END) / 1000f;
            if (since < 0.3f) {
                float k = 1f - since / 0.3f;
                g.fill(0, 0, screenW, screenH, ((int) (215 * k * k) << 24) | 0xFFFFFF);
            }
            int impactX = (int) (cx + restGap);
            OWCinematicFx.drawRays(g, impactX, cy, rays, since, exit, 0xFFFFFF, 0.9f);
            OWCinematicFx.drawShockwave(g, impactX, cy, since, exit);
            OWCinematicFx.drawSparks(g, impactX, cy, sparks, since, exit,
                    data.loserLook().primary(), data.loserLook().secondary(), 2.0f);
        }

        // ── Verdict et nom du vainqueur ─────────────────────────────────────────
        if (t >= STRIKE_END) {
            float in = OWCinematicFx.clamp((float) (t - STRIKE_END) / 420f);
            drawVerdict(g, cx, cy, in * exit, t);
        }
    }

    /**
     * Redessine la bannière vaincue morceau par morceau. Chaque morceau est une découpe rectangulaire
     * de l'étendard, translatée selon son vecteur de dispersion et rattrapée par la gravité.
     */
    private static void drawShatteredBanner(GuiGraphics g, int x, int y, int w, float since, float fade) {
        if (fade <= 0.01f) return;
        int cellW = Math.max(1, w / COLS);
        int cellH = Math.max(1, BANNER_H / ROWS);

        for (int i = 0; i < shards.length; i++) {
            int col = i % COLS, row = i / COLS;
            float local = Math.max(0f, since - shards[i][2]);
            if (local <= 0f) {
                // Ce morceau n'a pas encore décroché : on le laisse en place.
                drawShard(g, x, y, x + col * cellW, y + row * cellH, cellW, cellH, 0, 0);
                continue;
            }
            int dx = (int) (shards[i][0] * local);
            int dy = (int) (shards[i][1] * local + 0.5f * 520f * local * local);

            int sx = x + col * cellW, sy = y + row * cellH;
            // Une fois le morceau sorti de l'écran par le bas, inutile de le dessiner.
            if (sy + dy > y + BANNER_H + 600) continue;
            drawShard(g, x, y, sx, sy, cellW, cellH, dx, dy);
        }
    }

    /** Dessine un morceau : découpe à la position d'arrivée, bannière décalée pour l'y faire tomber. */
    private static void drawShard(GuiGraphics g, int bannerX, int bannerY,
                                  int cellX, int cellY, int cellW, int cellH, int dx, int dy) {
        int x1 = cellX + dx, y1 = cellY + dy;
        g.enableScissor(x1, y1, x1 + cellW, y1 + cellH);
        OWCinematicFx.drawBanner(g, data.loserLook(), bannerX + dx, bannerY + dy, BANNER_H, 0f);
        g.disableScissor();
    }

    /** Bandeau « Victoire » / « Défaite » et nom de la tribu qui l'emporte. */
    private static void drawVerdict(GuiGraphics g, int cx, int cy, float alpha, long t) {
        if (alpha <= 0.01f) return;
        var font = Minecraft.getInstance().font;
        int a = (int) (255 * alpha);
        boolean won = data.viewerWon();
        int color = won ? 0x7ddd73 : 0xE04444;

        Component verdict = Component.translatable(won
                        ? "owteams.arena.result.win_title" : "owteams.arena.result.loss_title")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(color)));

        // Le bandeau s'abat depuis une grande échelle, puis respire.
        float slam = OWCinematicFx.easeOut(OWCinematicFx.clamp((float) (t - STRIKE_END) / 420f));
        float beat = 1f + 0.05f * (float) Math.sin(t / 280.0);
        float scale = (2.6f + 3.0f * (1f - slam)) * beat;

        int titleY = cy - BANNER_H / 2 - 42;
        g.pose().pushPose();
        g.pose().translate(cx, titleY, 0);
        g.pose().scale(scale, scale, 1f);
        g.pose().pushPose();
        g.pose().scale(1.15f, 1.15f, 1f);
        g.drawString(font, verdict, -font.width(verdict) / 2, 0, ((int) (a * 0.32f) << 24) | (color & 0xFFFFFF), false);
        g.pose().popPose();
        g.drawString(font, verdict, -font.width(verdict) / 2, 0, (a << 24) | (color & 0xFFFFFF), true);
        g.pose().popPose();

        // Nom de la tribu victorieuse, glissant depuis le bas, dans sa propre couleur.
        float slide = OWCinematicFx.easeOut(OWCinematicFx.clamp((float) (t - STRIKE_END - 250) / 520f));
        if (slide <= 0.01f) return;
        int nameY = cy + BANNER_H / 2 + 18 + (int) (40 * (1f - slide));
        int tribeColor = readable(data.winnerLook().primary());
        int na = (int) (255 * alpha * slide);
        g.pose().pushPose();
        g.pose().translate(cx, nameY, 0);
        g.pose().scale(1.6f, 1.6f, 1f);
        // Cerné de blanc : une tribu aux couleurs sombres resterait invisible sur ce fond noir.
        OWCinematicFx.drawCenteredOutlined(g, font, data.winnerName(), 0, 0, tribeColor, true, na);
        g.pose().popPose();
    }

    /**
     * Éclaircit une couleur de tribu trop sombre pour rester lisible sur le fond noir de la
     * cinématique, en conservant sa teinte. Une tribu peut légitimement choisir un bleu nuit ou un
     * bordeaux profond : affiché tel quel, son nom serait invisible au moment de sa victoire.
     */
    private static int readable(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        float luma = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        final float floor = 0.48f;
        if (luma >= floor) return rgb & 0xFFFFFF;
        // Remontée proportionnelle : la teinte est préservée, seule l'intensité change.
        float boost = floor / Math.max(0.04f, luma);
        return (Math.min(255, Math.round(r * boost)) << 16)
                | (Math.min(255, Math.round(g * boost)) << 8)
                | Math.min(255, Math.round(b * boost));
    }
}
