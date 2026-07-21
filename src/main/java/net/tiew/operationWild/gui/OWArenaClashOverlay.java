package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.tiew.operationWild.networking.packets.to_client.ArenaClashPacket;
import net.tiew.operationWild.screen.tribe.OWBannerRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Animation d'ouverture d'un duel d'arène, jouée en plein écran chez les deux chefs.
 *
 * <p>Mise en scène en six temps : bandes cinéma, charge des deux bannières depuis les bords avec
 * lignes de vitesse, <b>ralenti</b> juste avant le contact, impact (éclair, secousse, rayons, ondes,
 * éclats), face-à-face avec « VS » et noms, puis écartement hors champ. La bannière du joueur qui
 * regarde est toujours à gauche.</p>
 *
 * <p>Aucune transparence n'est appliquée aux étendards eux-mêmes : {@link OWBannerRenderer}
 * réinitialise la couleur du shader en interne et force l'alpha à 1. Les effets de vitesse et la
 * sortie passent donc par des aplats translucides et par des déplacements, jamais par un fondu.</p>
 *
 * <p>Purement cosmétique et purement client : rien ici n'influence le combat.</p>
 */
public final class OWArenaClashOverlay {

    private OWArenaClashOverlay() {}

    // ── Découpage temporel (ms depuis le début) ──────────────────────────────────
    private static final long BARS_END    = 750;    // bandes cinéma + assombrissement
    private static final long CHARGE_END  = 2500;   // les bannières fondent l'une sur l'autre
    private static final long SLOWMO_END  = 3050;   // suspension juste avant le contact
    private static final long IMPACT_END  = 3450;   // choc
    private static final long HOLD_END    = 5500;   // face-à-face, « VS » et noms
    private static final long TOTAL       = 6400;   // fondu de sortie

    private static final int BANNER_H = 132;        // hauteur de rendu des bannières

    private static long startTime = -1L;
    private static ArenaClashPacket data = null;
    private static boolean impactSoundPlayed = false;

    /** Éclats de l'impact : {angle, vitesse px/s, taille, teinte 0 = gauche / 1 = droite}. */
    private static final List<float[]> sparks = new ArrayList<>();
    /** Rayons de lumière de l'impact : {angle, longueur, épaisseur}. */
    private static final List<float[]> rays = new ArrayList<>();

    /** Lance l'animation (appelé à la réception de {@link ArenaClashPacket}). */
    public static void trigger(ArenaClashPacket packet) {
        data = packet;
        startTime = System.currentTimeMillis();
        impactSoundPlayed = false;

        Random r = new Random();
        sparks.clear();
        for (int i = 0; i < 140; i++) {
            float angle = (float) (r.nextDouble() * Math.PI * 2);
            float speed = 70f + r.nextFloat() * 380f;
            float size = 1.3f + r.nextFloat() * 3.8f;
            sparks.add(new float[]{ angle, speed, size, r.nextBoolean() ? 0f : 1f });
        }
        rays.clear();
        for (int i = 0; i < 22; i++) {
            rays.add(new float[]{
                    (float) (r.nextDouble() * Math.PI * 2),
                    160f + r.nextFloat() * 320f,
                    2f + r.nextFloat() * 7f });
        }

        Minecraft mc = Minecraft.getInstance();
        // Le HUD n'est pas rendu tant qu'un écran est ouvert : or le chef vient précisément de
        // cliquer « Commencer » dans le menu de tribu. Sans cette fermeture, l'animation
        // ne serait jamais visible par celui qui a lancé le combat.
        if (mc.screen != null) mc.setScreen(null);
        if (mc.player != null) {
            // Grondement de montée en tension pendant la charge.
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_IN, 0.55f));
        }
    }

    /** Coupe l'animation (changement de dimension raté, déconnexion…). */
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
        exit = clamp(exit);

        // ── Voile + vignette ────────────────────────────────────────────────────
        float veil = clamp(t < BARS_END ? (float) t / BARS_END : 1f) * exit;
        g.fill(0, 0, screenW, screenH, ((int) (225 * veil) << 24));
        OWCinematicFx.drawVignette(g, screenW, screenH, veil);
        OWCinematicFx.drawBars(g, screenW, screenH, veil);

        boolean impact = t >= SLOWMO_END && t < IMPACT_END;
        if (impact && !impactSoundPlayed) {
            impactSoundPlayed = true;
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.9f));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 0.7f));
        }

        // ── Secousse d'écran ────────────────────────────────────────────────────
        int shakeX = 0, shakeY = 0;
        if (impact) {
            float k = 1f - (float) (t - SLOWMO_END) / (IMPACT_END - SLOWMO_END);
            shakeX = (int) (Math.sin(t / 16.0) * 14 * k);
            shakeY = (int) (Math.cos(t / 11.0) * 9 * k);
        }

        // ── Trajectoire des bannières ───────────────────────────────────────────
        float bw = OWBannerRenderer.W * (BANNER_H / (float) OWBannerRenderer.H);
        float restGap = bw * 0.66f + 30f;
        float startOff = screenW / 2f + bw * 1.4f;

        float x;            // distance au centre
        float tilt;         // inclinaison (les étendards se penchent dans la charge)
        if (t < BARS_END) {
            x = startOff; tilt = 0f;
        } else if (t < CHARGE_END) {
            float p = (float) (t - BARS_END) / (CHARGE_END - BARS_END);
            // Élan qui s'accélère, mais on garde une marge : le contact n'a pas encore lieu.
            x = startOff * (1f - easeIn(p) * 0.86f);
            tilt = 9f * easeIn(p);
        } else if (t < SLOWMO_END) {
            // Ralenti : les deux derniers pixels s'étirent, la tension monte.
            float p = (float) (t - CHARGE_END) / (SLOWMO_END - CHARGE_END);
            x = startOff * 0.14f * (1f - easeOut(p));
            tilt = 9f + 5f * easeOut(p);
        } else {
            float p = clamp((float) (t - SLOWMO_END) / (IMPACT_END - SLOWMO_END));
            x = restGap * easeOut(p);
            tilt = 14f * (1f - easeOut(p));
            // Sortie : on repousse les étendards hors champ plutôt que de les estomper — leur rendu
            // ne se prête pas à la transparence, alors qu'un écartement se lit très bien.
            if (t > HOLD_END) {
                float out = easeIn((float) (t - HOLD_END) / (TOTAL - HOLD_END));
                x += (startOff - restGap) * out;
            }
        }

        int by = cy - BANNER_H / 2 + shakeY;

        // Lignes de vitesse derrière chaque bannière. On les dessine en aplats translucides plutôt
        // qu'en copies de la bannière : OWBannerRenderer réinitialise la couleur du shader en
        // interne et force l'alpha à 1, donc des « fantômes » ressortiraient parfaitement opaques.
        if (t >= BARS_END && t < SLOWMO_END) {
            float rush = clamp((float) (t - BARS_END) / (CHARGE_END - BARS_END));
            OWCinematicFx.drawSpeedLines(g, (int) (cx - x - bw / 2) + shakeX, by, (int) bw, BANNER_H,
                    data.leftLook().primary(), rush, true);
            OWCinematicFx.drawSpeedLines(g, (int) (cx + x - bw / 2) - shakeX, by, (int) bw, BANNER_H,
                    data.rightLook().primary(), rush, false);
        }

        OWCinematicFx.drawBanner(g, data.leftLook(), (int) (cx - x - bw / 2) + shakeX, by, BANNER_H, -tilt);
        OWCinematicFx.drawBanner(g, data.rightLook(), (int) (cx + x - bw / 2) - shakeX, by, BANNER_H, tilt);

        // ── Impact ──────────────────────────────────────────────────────────────
        if (impact) {
            float k = 1f - (float) (t - SLOWMO_END) / (IMPACT_END - SLOWMO_END);
            g.fill(0, 0, screenW, screenH, ((int) (235 * k * k) << 24) | 0xFFFFFF);
        }
        if (t >= SLOWMO_END) {
            float since = (t - SLOWMO_END) / 1000f;
            OWCinematicFx.drawRays(g, cx, cy, rays, since, exit, 0xFFFFFF, 0.9f);
            OWCinematicFx.drawShockwave(g, cx, cy, since, exit);
            OWCinematicFx.drawSparks(g, cx, cy, sparks, since, exit,
                    data.leftLook().primary(), data.rightLook().primary(), 2.0f);
        }

        // ── Noms des deux tribus ────────────────────────────────────────────────
        // Affichés dès la charge et rivés sous leur propre bannière : on sait immédiatement qui
        // affronte qui, au lieu d'attendre l'impact pour le découvrir.
        if (t >= BARS_END) {
            float nameFade = clamp((float) (t - BARS_END) / 420f) * exit;
            drawNames(g, cx, by + BANNER_H + 12, nameFade, x, shakeX);
        }

        // ── « VS » ──────────────────────────────────────────────────────────────
        if (t >= SLOWMO_END) {
            drawVersus(g, cx, cy, clamp((float) (t - SLOWMO_END) / 320f) * exit, t);
        }
    }

    // ── Éléments ────────────────────────────────────────────────────────────

    /** « VS » qui s'abat depuis une grande échelle, puis bat lentement. */
    private static void drawVersus(GuiGraphics g, int cx, int cy, float alpha, long t) {
        if (alpha <= 0.01f) return;
        var font = Minecraft.getInstance().font;
        Component vs = Component.translatable("owteams.arena.clash.versus")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFD257)));

        float slam = clamp((float) (t - SLOWMO_END) / 320f);
        float beat = 1f + 0.06f * (float) Math.sin(t / 260.0);
        // Entrée en fracas : très gros puis resserré sur la taille de croisière.
        float scale = (3.0f + 3.6f * (1f - easeOut(slam))) * beat;

        int a = (int) (255 * alpha);
        g.pose().pushPose();
        g.pose().translate(cx, cy - 8, 0);
        g.pose().scale(scale, scale, 1f);
        // Lueur : le même texte, plus large et translucide, posé dessous.
        g.pose().pushPose();
        g.pose().scale(1.16f, 1.16f, 1f);
        g.drawString(font, vs, -font.width(vs) / 2, 0, ((int) (a * 0.35f) << 24) | 0xFFAA33, false);
        g.pose().popPose();
        g.drawString(font, vs, -font.width(vs) / 2, 0, (a << 24) | 0xFFD257, true);
        g.pose().popPose();
    }

    /**
     * Noms de tribu, chacun centré sous SA bannière et donc emporté par elle pendant la charge.
     * Le nom reste ainsi toujours associé au bon étendard, y compris au moment du choc.
     */
    private static void drawNames(GuiGraphics g, int cx, int y, float alpha,
                                  float bannerOffset, int shakeX) {
        if (alpha <= 0.01f) return;
        var font = Minecraft.getInstance().font;
        int a = (int) (255 * alpha);
        drawCenteredShadow(g, font, data.leftName(), (int) (cx - bannerOffset) + shakeX, y, 0x8FE8B0, a);
        drawCenteredShadow(g, font, data.rightName(), (int) (cx + bannerOffset) - shakeX, y, 0xE8956A, a);
    }

    private static void drawCenteredShadow(GuiGraphics g, net.minecraft.client.gui.Font font,
                                           String text, int cx, int y, int rgb, int alpha) {
        if (text == null || text.isEmpty()) return;
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(1.45f, 1.45f, 1f);
        OWCinematicFx.drawCenteredOutlined(g, font, text, 0, 0, rgb, false, alpha);
        g.pose().popPose();
    }

    // ── Courbes (déléguées à la boîte à outils commune) ──────────────────────
    private static float clamp(float v) { return OWCinematicFx.clamp(v); }

    private static float easeIn(float t) { return OWCinematicFx.easeIn(t); }

    private static float easeOut(float t) { return OWCinematicFx.easeOut(t); }
}
