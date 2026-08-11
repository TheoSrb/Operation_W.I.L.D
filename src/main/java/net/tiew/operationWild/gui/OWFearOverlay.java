package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.behavior.OWFearHandler;
import net.tiew.operationWild.entity.misc.Submarine;

/**
 * Ce que voit le cavalier d'une monture affolée. Il n'y a rien à faire, rien à presser : la peur se
 * subit, et tout l'écran ne sert qu'à la faire ressentir.
 *
 * <p>Les ténèbres sont un <b>tunnel elliptique tracé par balayage</b>, et non un empilement de cadres :
 * l'opacité suit la distance elliptique au centre, si bien que les coins s'éteignent avant les milieux
 * de bords — c'est le comportement d'un champ de vision qui se rétrécit, qu'aucun cadre rectangulaire
 * ne sait imiter. Un pouls à deux temps le resserre, des griffes d'ombre lèchent le bord du champ clair,
 * et chaque ruade fait claquer un éclair rouge.</p>
 *
 * <p>{@code GuiGraphics.fill} accumule dans un {@code MultiBufferSource} vidé une fois par image : les
 * quelques milliers de rectangles du dégradé radial ne coûtent qu'une passe de sommets, là où un tracé
 * en mode immédiat aurait interdit tout autre chose qu'une vignette à vingt bandes.</p>
 */
public final class OWFearOverlay {

    private OWFearOverlay() {}

    private static final int VEIL_BANDS = 12;
    private static final int VEIL_ROW_STEP = 3;
    private static final float VEIL_SPREAD = 2.3f;
    private static final float VEIL_MAX_ALPHA = 236f;

    private static final int TENDRILS = 15;
    private static final int TENDRIL_SAMPLES = 8;

    private static final int COLOR_PANIC = 0xE04A2F;
    private static final int COLOR_BLOOD = 0x5A0E0E;

    private static int heartbeatTimer = 0;

    public static OWEntity panickingMount(Player player) {
        if (player == null) return null;
        if (!(player.getVehicle() instanceof OWEntity mount) || mount instanceof Submarine) return null;
        if (mount.getPassengers().indexOf(player) != 0) return null;
        return mount.isPanicking() ? mount : null;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        OWEntity mount = panickingMount(mc.player);
        if (mount == null) {
            heartbeatTimer = 0;
            return;
        }

        float panic = mount.getPanicLevel();
        if (--heartbeatTimer > 0) return;

        heartbeatTimer = Mth.floor(Mth.lerp(panic, 20f, 10f));
        mc.player.playSound(SoundEvents.WARDEN_HEARTBEAT, 0.55f + 0.45f * panic, 0.8f + 0.35f * panic);
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        OWEntity mount = panickingMount(mc.player);
        if (mount == null) return;

        float panic = Mth.clamp(mount.getPanicLevel(), 0f, 1f);
        if (panic <= 0.01f) return;

        float buck = OWFearHandler.buckCurve(mount, mc.getTimer().getGameTimeDeltaPartialTick(false));
        long now = System.currentTimeMillis();
        float pulse = heartbeat(now, panic);

        float openness = Mth.clamp(Mth.lerp(panic, 0.95f, 0.50f) - 0.08f * pulse - 0.07f * buck, 0.30f, 0.98f);
        float radiusX = screenWidth * 0.5f * openness;
        float radiusY = screenHeight * 0.5f * openness;

        RenderSystem.enableBlend();
        drawTunnel(graphics, screenWidth, screenHeight, radiusX, radiusY, panic);
        drawTendrils(graphics, screenWidth, screenHeight, radiusX, radiusY, panic, now);
        drawBuckFlash(graphics, screenWidth, screenHeight, buck);
        RenderSystem.disableBlend();

        if (mc.options.hideGui) return;

        drawCaption(graphics, mc.font, mount, screenWidth / 2, screenHeight / 2, panic, pulse, buck);
    }

    // ── Ténèbres ─────────────────────────────────────────────────────────────

    /**
     * Pouls à deux temps : une systole franche suivie d'une diastole plus douce, et non la sinusoïde
     * régulière d'une jauge. C'est cette irrégularité-là qu'on reconnaît comme un cœur.
     */
    private static float heartbeat(long now, float panic) {
        float period = Mth.lerp(panic, 1080f, 520f);
        float phase = (now % (long) period) / period;
        return Mth.clamp(thump(phase, 0f) + thump(phase - 1f, 0f) + 0.62f * thump(phase, 0.28f), 0f, 1f);
    }

    private static float thump(float phase, float center) {
        float d = (phase - center) / 0.065f;
        return (float) Math.exp(-d * d);
    }

    private static void drawTunnel(GuiGraphics graphics, int width, int height,
                                   float radiusX, float radiusY, float panic) {
        if (radiusX < 1f || radiusY < 1f) return;

        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float peak = VEIL_MAX_ALPHA * Math.min(1f, panic * 1.25f);
        int solid = ((int) peak << 24) | 0x000000;

        float[] rings = new float[VEIL_BANDS + 1];
        int[] colors = new int[VEIL_BANDS];
        for (int band = 0; band <= VEIL_BANDS; band++) {
            rings[band] = 1f + band * (VEIL_SPREAD - 1f) / VEIL_BANDS;
        }
        for (int band = 0; band < VEIL_BANDS; band++) {
            float depth = (band + 0.5f) / VEIL_BANDS;
            int alpha = (int) (peak * (float) Math.pow(depth, 0.85));
            int tint = mixRgb(COLOR_BLOOD, 0x000000, Mth.clamp(depth * 1.35f, 0f, 1f));
            colors[band] = (Math.min(255, alpha) << 24) | tint;
        }

        for (int y = 0; y < height; y += VEIL_ROW_STEP) {
            int bottom = Math.min(height, y + VEIL_ROW_STEP);
            float dy = (y + VEIL_ROW_STEP * 0.5f - centerY) / radiusY;
            float dy2 = dy * dy;

            float outermost = 0f;
            for (int band = 0; band < VEIL_BANDS; band++) {
                float inner = halfWidth(radiusX, rings[band], dy2);
                float outer = halfWidth(radiusX, rings[band + 1], dy2);
                if (outer <= inner) {
                    outermost = Math.max(outermost, outer);
                    continue;
                }
                graphics.fill((int) (centerX - outer), y, (int) (centerX - inner), bottom, colors[band]);
                graphics.fill((int) (centerX + inner), y, (int) (centerX + outer), bottom, colors[band]);
                outermost = outer;
            }

            int edge = (int) (centerX - outermost);
            if (edge > 0) {
                graphics.fill(0, y, edge, bottom, solid);
                graphics.fill(width - edge, y, width, bottom, solid);
            }
        }
    }

    private static float halfWidth(float radiusX, float ring, float dy2) {
        float inside = ring * ring - dy2;
        return inside <= 0f ? 0f : radiusX * (float) Math.sqrt(inside);
    }

    /**
     * Griffes d'ombre qui rampent depuis les ténèbres vers le champ clair puis se rétractent, chacune
     * à son rythme. C'est ce qui sépare un noircissement d'un noir <b>vivant</b>.
     */
    private static void drawTendrils(GuiGraphics graphics, int width, int height,
                                     float radiusX, float radiusY, float panic, long now) {
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float time = (now % 200000L) / 1000f;

        for (int i = 0; i < TENDRILS; i++) {
            float angle = (i / (float) TENDRILS) * Mth.TWO_PI + hash(i, 1) * 0.42f;
            float speed = 0.35f + hash(i, 2) * 0.9f;
            float wave = 0.5f + 0.5f * Mth.sin(time * speed + hash(i, 3) * Mth.TWO_PI);
            float reach = Mth.lerp(wave * panic, VEIL_SPREAD, 0.72f + 0.16f * hash(i, 4));

            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            float thickness = (2.5f + hash(i, 5) * 5f) * (0.6f + 0.4f * panic);

            for (int sample = 0; sample < TENDRIL_SAMPLES; sample++) {
                float t = sample / (float) (TENDRIL_SAMPLES - 1);
                float ring = Mth.lerp(t, VEIL_SPREAD, reach);
                float taper = 1f - t * 0.82f;

                int alpha = (int) (215 * panic * taper * (0.55f + 0.45f * wave));
                if (alpha <= 3) continue;

                float sway = Mth.sin(time * speed * 1.7f + t * 3.1f) * 0.05f * t;
                float px = centerX + radiusX * ring * (cos - sin * sway);
                float py = centerY + radiusY * ring * (sin + cos * sway);
                int size = Math.max(1, (int) (thickness * taper));

                graphics.fill((int) px - size, (int) py - size, (int) px + size, (int) py + size,
                        (Math.min(255, alpha) << 24) | COLOR_BLOOD);
            }
        }
    }

    /** Éclair rouge sur la ruade : le coup de reins se ressent avant même de se voir sur la bête. */
    private static void drawBuckFlash(GuiGraphics graphics, int width, int height, float buck) {
        if (buck <= 0.02f) return;
        int alpha = (int) (86 * buck * buck);
        if (alpha <= 2) return;
        graphics.fill(0, 0, width, height, (alpha << 24) | 0xB01818);
    }

    // ── Légende ──────────────────────────────────────────────────────────────

    /**
     * Une phrase, et rien d'autre. Elle nomme l'espèce plutôt que « votre entité » : sur une monture
     * qui vient de partir en vrille, savoir <b>qui</b> a paniqué se lit plus vite qu'un terme générique.
     *
     * <p>Le libellé évite tout adjectif accordé : la moitié du bestiaire est masculine et l'autre non
     * (une orque, un tigre), et un seul texte doit convenir aux deux.</p>
     */
    private static void drawCaption(GuiGraphics graphics, Font font, OWEntity mount,
                                    int centerX, int centerY, float panic, float pulse, float buck) {
        Component line = Component.translatable("tooltip.fearStarted", mount.getType().getDescription());
        int width = font.width(line);
        int y = centerY + 26;
        float jitter = (panic * 1.2f + buck * 3.5f) * (float) (Math.random() - 0.5);

        graphics.pose().pushPose();
        graphics.pose().translate(jitter, 0, 0);

        graphics.fill(centerX - width / 2 - 7, y - 5, centerX + width / 2 + 7, y + font.lineHeight + 4, 0xB0060606);
        int accent = (int) (120 + 100 * pulse) << 24 | mixRgb(COLOR_PANIC, 0xFFFFFF, buck * 0.7f);
        graphics.fill(centerX - width / 2 - 7, y - 5, centerX + width / 2 + 7, y - 4, accent);
        graphics.fill(centerX - width / 2 - 7, y + font.lineHeight + 3, centerX + width / 2 + 7, y + font.lineHeight + 4, accent);

        int textColor = pulse > 0.75f ? 0xFFFFFF : mixRgb(COLOR_PANIC, 0xFFD8CC, 0.35f + pulse * 0.35f);
        graphics.drawString(font, line, centerX - width / 2, y, 0xFF000000 | textColor, true);
        graphics.pose().popPose();
    }

    // ── Outils ───────────────────────────────────────────────────────────────

    private static float hash(int index, int salt) {
        int n = index * 374761393 + salt * 668265263;
        n = (n ^ (n >>> 13)) * 1274126177;
        return ((n ^ (n >>> 16)) & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    private static int mixRgb(int from, int to, float t) {
        t = Mth.clamp(t, 0f, 1f);
        int r = Mth.lerpInt(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = Mth.lerpInt(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = Mth.lerpInt(t, from & 0xFF, to & 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    private static int scaleRgb(int rgb, float factor) {
        int r = Mth.clamp((int) (((rgb >> 16) & 0xFF) * factor), 0, 255);
        int g = Mth.clamp((int) (((rgb >> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp((int) ((rgb & 0xFF) * factor), 0, 255);
        return (r << 16) | (g << 8) | b;
    }
}
