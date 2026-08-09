package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

/**
 * Écran de la victime saisie : qui la tient, comment se dégager, et surtout à quel point elle est
 * près d'y rester.
 *
 * <p>La mécanique ne change pas — clic droit pour se débattre, une jauge qui monte vers la mort —
 * mais l'affichage est entièrement redessiné, sans aucune texture : plus de carré tramé ni de
 * remplissage teinté au nuanceur, tout est tracé au rectangle plein. La jauge devient une échelle
 * de <b>segments</b> qui court du vert au rouge : la couleur du dernier segment allumé dit d'un
 * coup d'œil s'il reste de la marge ou s'il faut cliquer pour sa vie, ce qu'un remplissage d'une
 * seule teinte ne pouvait pas dire.</p>
 */
public class RightClickAlertOverlay {

    /** Largeur de la jauge, calée sur la barre d'expérience vanilla pour rester dans le ton. */
    private static final int SEGMENTS = 20;
    private static final int SEGMENT_WIDTH = 8;
    private static final int SEGMENT_GAP = 1;
    private static final int GAUGE_HEIGHT = 11;
    private static final int GAUGE_WIDTH = SEGMENTS * SEGMENT_WIDTH + (SEGMENTS - 1) * SEGMENT_GAP;

    /** Seuil à partir duquel la mort est proche : clignotement et vignette rouge. */
    private static final float CRITICAL = 0.82f;

    private static final float TITLE_SCALE = 1.45f;

    public static boolean hasClicked = false;
    public static int clickAnimationTimer = 0;
    private static int lastTickCount = -1;

    public static OWEntity captorOf(Player player) {
        if (player.getVehicle() instanceof OWEntity mount && holds(mount, player)) return mount;

        return player.level()
                .getEntitiesOfClass(OWEntity.class, player.getBoundingBox().inflate(6.0))
                .stream()
                .filter(entity -> holds(entity, player))
                .findFirst().orElse(null);
    }

    private static boolean holds(OWEntity entity, Player player) {
        if (entity instanceof CrocodileEntity crocodile) return crocodile.isGrabbing() && crocodile.getGrabbedTarget() == player;
        if (entity instanceof TigerEntity tiger) return tiger.isGrabbing() && tiger.getGrabbedTarget() == player;
        if (entity instanceof BoaEntity boa) return boa.isGrabbing() && boa.getGrabbedTarget() == player;
        if (entity instanceof KangarooEntity kangaroo) return kangaroo.getDrownVictim() == player;
        return false;
    }

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        OWEntity captor = captorOf(player);
        if (captor == null) return;

        int currentTick = player.tickCount;
        if (currentTick != lastTickCount) {
            lastTickCount = currentTick;
            if (clickAnimationTimer > 0 && --clickAnimationTimer <= 0) {
                clickAnimationTimer = 0;
                hasClicked = false;
            }
        }

        int timeout = grabTimeout(captor);
        int maxTimeout = grabMaxTimeout(captor);
        if (maxTimeout <= 0) return;

        float danger = Mth.clamp(timeout / (float) maxTimeout, 0f, 1f);
        boolean critical = danger >= CRITICAL;
        long now = System.currentTimeMillis();

        renderDangerVignette(guiGraphics, screenWidth, screenHeight, danger, now);

        Font font = Minecraft.getInstance().font;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Battement commun à tout l'écran, de plus en plus pressant : c'est lui qui dit « vite »
        // sans le moindre mot. À l'aise il respire lentement, à l'agonie il tambourine.
        float period = Mth.lerp(danger, 560f, 150f);
        float beat = (float) Math.abs(Math.sin(now / (double) period));

        renderTitle(guiGraphics, font, captor, centerX, centerY - 52, danger, critical, beat);
        renderGauge(guiGraphics, centerX, centerY + 26, danger, critical, now);
        renderHint(guiGraphics, font, centerX, centerY + 46, danger, now, beat);
    }

    // ── Éléments ─────────────────────────────────────────────────────────────

    private static void renderTitle(GuiGraphics guiGraphics, Font font, OWEntity captor,
                                    int centerX, int y, float danger, boolean critical, float beat) {
        String captorKey = "entity.ow." + captor.getClass().getSimpleName().split("Entity")[0].toLowerCase();
        Component name = Component.literal(Component.translatable(captorKey).getString().toUpperCase());
        Component title = Component.translatable("tooltip.grabBy", name);

        // Le titre prend la couleur du danger : il n'y a pas à comparer un texte fixe et une jauge,
        // les deux disent la même chose au même instant.
        int color = dangerColor(danger);
        if (critical && beat > 0.75f) color = 0xFFFFFF;

        // Il enfle au rythme du battement, d'autant plus fort que la fin approche.
        float scale = TITLE_SCALE * (1f + 0.10f * danger * beat);
        int width = font.width(title);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - (width * scale) / 2f, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, title, 0, 0, color, true);
        guiGraphics.pose().popPose();
    }

    private static void renderGauge(GuiGraphics guiGraphics, int centerX, int y,
                                    float danger, boolean critical, long now) {
        int left = centerX - GAUGE_WIDTH / 2;

        // Sursaut au clic réussi, et tremblement quand la mort approche : deux retours distincts,
        // l'un provoqué par le joueur, l'autre subi.
        float punch = clickAnimationTimer > 0 ? 1.0f + 0.06f * clickAnimationTimer : 1.0f;
        float shake = critical ? 1.6f * (float) Math.sin(now / 45.0) : 0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, y + GAUGE_HEIGHT / 2f + shake, 0);
        guiGraphics.pose().scale(punch, punch, 1.0f);
        guiGraphics.pose().translate(-centerX, -(y + GAUGE_HEIGHT / 2f), 0);

        // Fond sombre et liseré, pour que la jauge tienne sur n'importe quel décor.
        guiGraphics.fill(left - 3, y - 3, left + GAUGE_WIDTH + 3, y + GAUGE_HEIGHT + 3, 0xB0080808);
        guiGraphics.fill(left - 3, y - 3, left + GAUGE_WIDTH + 3, y - 2, 0x40FFFFFF);

        int litSegments = Mth.ceil(danger * SEGMENTS);

        for (int i = 0; i < SEGMENTS; i++) {
            int sx = left + i * (SEGMENT_WIDTH + SEGMENT_GAP);
            // Chaque cran porte la couleur de SA position dans l'échelle : le dégradé vert → rouge
            // existe donc en permanence, allumé ou non. On lit la marge restante avant le rouge
            // sans attendre d'y être.
            float stop = (i + 0.5f) / SEGMENTS;
            int hue = dangerColor(stop);

            boolean lit = i < litSegments;
            boolean head = lit && i == litSegments - 1;

            int color;
            if (!lit) {
                color = 0xFF000000 | scaleRgb(hue, 0.16f);
            } else if (head && clickAnimationTimer > 0) {
                color = 0xFFFFFFFF;
            } else if (head) {
                color = 0xFF000000 | hue;
            } else {
                color = 0xFF000000 | scaleRgb(hue, 0.82f);
            }

            guiGraphics.fill(sx, y, sx + SEGMENT_WIDTH, y + GAUGE_HEIGHT, color);

            // Reflet haut : donne du relief sans avoir à texturer.
            if (lit) guiGraphics.fill(sx, y, sx + SEGMENT_WIDTH, y + 2, 0xFF000000 | scaleRgb(hue, 1.25f));
        }

        // Repère du point de non-retour, à l'entrée de la zone critique.
        int markX = left + Mth.floor(CRITICAL * GAUGE_WIDTH);
        guiGraphics.fill(markX, y - 3, markX + 1, y + GAUGE_HEIGHT + 3, 0xC0FFFFFF);

        guiGraphics.pose().popPose();
    }

    private static void renderHint(GuiGraphics guiGraphics, Font font, int centerX, int y,
                                   float danger, long now, float beat) {
        Component hint = Component.translatable("tooltip.rightClickAlert");

        // Souris dessinée + texte, alignés en un seul bloc centré. Le pictogramme porte à lui seul
        // la consigne : le joueur pris à la gorge n'a ni le temps ni le calme de lire une phrase,
        // et rien dans l'ancien écran ne montrait QUEL bouton presser.
        int textWidth = font.width(hint);
        int blockWidth = MOUSE_WIDTH + 8 + textWidth;
        int left = centerX - blockWidth / 2;

        // Tout le bloc tressaute au rythme du battement quand ça devient chaud.
        float jitter = danger > 0.6f ? (danger - 0.6f) / 0.4f * 1.8f * beat : 0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, -jitter, 0);

        drawMouseIcon(guiGraphics, left, y - 4, now, beat, danger);

        int alpha = (int) (150 + 105 * danger) << 24;
        guiGraphics.drawString(font, hint, left + MOUSE_WIDTH + 8, y, alpha | 0xE8E8E8, true);

        guiGraphics.pose().popPose();
    }

    private static final int MOUSE_WIDTH = 11;
    private static final int MOUSE_HEIGHT = 16;

    /**
     * Pictogramme de souris avec le bouton DROIT mis en avant.
     *
     * <p>Il bat en permanence pour appeler le clic, et s'enfonce d'un pixel en s'éclairant à chaque
     * clic réussi — c'est ce retour-là qui manquait le plus : sans lui, rien à l'écran ne confirmait
     * que le geste avait porté, et le joueur croyait le bouton inopérant.</p>
     */
    private static void drawMouseIcon(GuiGraphics guiGraphics, int x, int y, long now,
                                      float beat, float danger) {
        boolean pressed = clickAnimationTimer > 0;
        if (pressed) y += 1;

        int w = MOUSE_WIDTH;
        int h = MOUSE_HEIGHT;
        int cx = x + w / 2;
        int cy = y + h / 2;

        // Halos concentriques qui s'écartent de la souris, à la cadence du battement : le regard
        // est attiré vers le pictogramme même quand il est fixé sur la jauge.
        drawUrgencyRings(guiGraphics, cx, cy, w, h, beat, danger);

        // Corps : contour clair aux angles adoucis, intérieur sombre.
        int outline = 0xFFE8E8E8;
        guiGraphics.fill(x + 1, y, x + w - 1, y + h, outline);
        guiGraphics.fill(x, y + 2, x + w, y + h - 2, outline);
        guiGraphics.fill(x + 2, y + 1, x + w - 2, y + h - 1, 0xFF1A1A1A);
        guiGraphics.fill(x + 1, y + 3, x + w - 1, y + h - 3, 0xFF1A1A1A);

        // Bouton droit : il bat au même rythme que le reste, éclat blanc au clic.
        int accent = pressed ? 0xFFFFFFFF : (0xFF000000 | scaleRgb(0x37C8FF, 0.45f + 0.55f * beat));
        guiGraphics.fill(x + w / 2 + 1, y + 1, x + w - 2, y + 6, accent);
        guiGraphics.fill(x + w / 2 + 1, y + 2, x + w - 1, y + 6, accent);

        // Séparation des deux boutons et bas de la coque.
        guiGraphics.fill(x + w / 2, y + 1, x + w / 2 + 1, y + 7, outline);
        guiGraphics.fill(x + 2, y + 6, x + w - 2, y + 7, outline);

        // Onde de clic : deux traits qui s'écartent du bouton, lisibles même en vision périphérique.
        if (pressed) {
            int ripple = 0xFFFFFFFF;
            guiGraphics.fill(x + w + 1, y + 1, x + w + 3, y + 2, ripple);
            guiGraphics.fill(x + w + 2, y + 4, x + w + 4, y + 5, ripple);
        }
    }

    /**
     * Deux couronnes carrées qui enflent et s'effacent autour du pictogramme.
     *
     * <p>Décalées d'une demi-période l'une de l'autre : il y en a donc toujours une en train de
     * grandir, ce qui donne un appel continu plutôt qu'un clignotement. Leur cadence est celle du
     * battement général — plus la mort approche, plus elles se bousculent.</p>
     */
    private static void drawUrgencyRings(GuiGraphics guiGraphics, int cx, int cy, int w, int h,
                                         float beat, float danger) {
        for (int ring = 0; ring < 2; ring++) {
            float phase = (beat + ring * 0.5f) % 1f;
            int alpha = (int) (150 * (1f - phase) * (0.45f + 0.55f * danger));
            if (alpha <= 4) continue;

            int grow = Math.round(phase * 9f);
            int left = cx - w / 2 - 3 - grow;
            int right = cx + w / 2 + 3 + grow;
            int top = cy - h / 2 - 3 - grow;
            int bottom = cy + h / 2 + 3 + grow;
            int color = (alpha << 24) | 0x37C8FF;

            guiGraphics.fill(left, top, right, top + 1, color);
            guiGraphics.fill(left, bottom - 1, right, bottom, color);
            guiGraphics.fill(left, top, left + 1, bottom, color);
            guiGraphics.fill(right - 1, top, right, bottom, color);
        }
    }

    /**
     * Bandes rouges haut et bas, qui ne se déclarent qu'en zone critique. Elles battent au rythme
     * d'un pouls : le joueur sait qu'il est en train de mourir même les yeux rivés sur le décor.
     */
    private static void renderDangerVignette(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                             float danger, long now) {
        // Seuil haut volontairement : le crocodile démarre sa prise à mi-jauge, une vignette qui
        // partirait plus tôt serait allumée dès la morsure et ne dirait plus rien.
        if (danger < 0.68f) return;

        float intensity = (danger - 0.68f) / 0.32f;
        float pulse = 0.65f + 0.35f * (float) Math.abs(Math.sin(now / 480.0));
        int alpha = (int) (110 * intensity * pulse);
        if (alpha <= 2) return;

        int band = Math.max(28, screenHeight / 7);
        int edge = (alpha << 24) | 0x8C1010;

        guiGraphics.fillGradient(0, 0, screenWidth, band, edge, 0x00000000);
        guiGraphics.fillGradient(0, screenHeight - band, screenWidth, screenHeight, 0x00000000, edge);
    }

    // ── Outils ───────────────────────────────────────────────────────────────

    /**
     * Échelle de danger, du vert au rouge. Les paliers ne sont pas répartis régulièrement : le
     * passage à l'orange arrive tôt, pour que la moitié haute de la jauge soit déjà une alerte.
     */
    private static int dangerColor(float t) {
        t = Mth.clamp(t, 0f, 1f);
        if (t < 0.40f) return lerpRgb(0x3FD44D, 0xC8E03A, t / 0.40f);
        if (t < 0.70f) return lerpRgb(0xC8E03A, 0xF0902A, (t - 0.40f) / 0.30f);
        return lerpRgb(0xF0902A, 0xE01F1F, (t - 0.70f) / 0.30f);
    }

    private static int lerpRgb(int from, int to, float t) {
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

    private static int grabTimeout(OWEntity captor) {
        if (captor instanceof CrocodileEntity crocodile) return crocodile.getGrabTimeout();
        if (captor instanceof TigerEntity tiger) return tiger.getGrabTimeout();
        if (captor instanceof BoaEntity boa) return boa.getGrabTimeout();
        if (captor instanceof KangarooEntity kangaroo) return kangaroo.getGrabTimeout();
        return 0;
    }

    private static int grabMaxTimeout(OWEntity captor) {
        if (captor instanceof CrocodileEntity crocodile) return crocodile.getGrabMaxTimeout();
        if (captor instanceof TigerEntity tiger) return tiger.getGrabMaxTimeout();
        if (captor instanceof BoaEntity boa) return boa.getGrabMaxTimeout();
        if (captor instanceof KangarooEntity kangaroo) return kangaroo.getGrabMaxTimeout();
        return 0;
    }
}
