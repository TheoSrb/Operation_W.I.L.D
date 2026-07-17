package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.EnableTribeReputationPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Onglet « Réputation » de la tribu : badge de prestige, score de réputation et barre de progression
 * vers le palier suivant. Lecture seule (le score est calculé côté serveur). Accessible à tous les
 * membres.
 */
public class OWTribeReputationScreen extends OWTribeScreen {

    static final ResourceLocation OW_SPRITES =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_sprites.png");

    private static final int BADGE_DISPLAY = 44;              // taille du gros badge affiché
    private static final int BAR_W = 144, BAR_H = 9;

    private int badgeX, badgeY;   // coin haut-gauche du gros badge (pour le survol)

    // ── Animation de changement de palier (particules + pop du badge) ────────────
    private static final long BADGE_ANIM_MS = 1700;
    private long badgeAnimStart = -1;
    private int badgeAnimColor = 0xFFFFFF;
    private final List<float[]> badgeParticles = new ArrayList<>(); // {angle, vitesse px/s, taille}
    private int lastBadgeOrdinal = -2; // -2 = pas encore observé sur cet écran

    private Button activateBtn;

    public OWTribeReputationScreen() {
        super(Component.translatable("owteams.reputation.title"));
    }

    @Override
    protected void init() {
        super.init();
        activateBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.reputation.activate.button"),
                        b -> OWNetworkHandler.sendToServer(new EnableTribeReputationPacket()))
                .bounds(leftPos + IMG_W / 2 - 62, topPos + IMG_H - 26, 124, 18).build());
    }

    /** Le joueur local peut-il activer la réputation (chef ou adjoint) ? */
    private boolean canManage() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        if (t == null || mc.player == null) return false;
        UUID me = mc.player.getUUID();
        return t.isChief(me) || t.isDeputy(me);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && tribeTabClicked(mx, my, Tab.REPUTATION)) return true;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void tick() {
        super.tick();
        if (!OWClientTribeData.hasTribe()) Minecraft.getInstance().setScreen(new OWTribeMenuScreen());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.reputation.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A);

        OWTeam t = OWClientTribeData.get();
        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }

        boolean enabled = t.isReputationEnabled();
        activateBtn.visible = !enabled && canManage();
        if (!enabled) {
            renderActivationPrompt(g);
            super.render(g, mouseX, mouseY, partial); // bouton « Activer » si visible
            renderTribeTabs(g, mouseX, mouseY, Tab.REPUTATION);
            return;
        }

        int reputation = t.getReputation();
        OWReputation.Badge badge = OWReputation.badgeFor(reputation);
        OWReputation.Badge next = OWReputation.nextBadge(badge);

        // Détecte un changement de palier (à l'ouverture ou en direct) → animation.
        detectBadgeChange(t.getTeamId(), badge);

        // ── Gros badge centré (avec « pop » et particules pendant l'animation) ────
        badgeX = leftPos + IMG_W / 2 - BADGE_DISPLAY / 2;
        badgeY = topPos + 23;
        int bcx = badgeX + BADGE_DISPLAY / 2, bcy = badgeY + BADGE_DISPLAY / 2;
        boolean animActive = badgeAnimStart >= 0 && (System.currentTimeMillis() - badgeAnimStart) < BADGE_ANIM_MS;
        float animT = animActive ? (System.currentTimeMillis() - badgeAnimStart) / 1000f : 0f;

        if (animActive) renderBadgeHalo(g, bcx, bcy, animT);

        if (badge.hasSprite()) {
            float pop = animActive
                    ? 1f + 0.35f * (float) Math.sin(Math.min(1f, animT / (BADGE_ANIM_MS / 1000f) / 0.25f) * Math.PI)
                    : 1f;
            if (pop != 1f) {
                g.pose().pushPose();
                g.pose().translate(bcx, bcy, 0);
                g.pose().scale(pop, pop, 1f);
                renderBadge(g, badge, -BADGE_DISPLAY / 2, -BADGE_DISPLAY / 2, BADGE_DISPLAY);
                g.pose().popPose();
            } else {
                renderBadge(g, badge, badgeX, badgeY, BADGE_DISPLAY);
            }
        } else {
            // État « aucun badge » : silhouette grisée + point d'interrogation.
            g.fill(badgeX, badgeY, badgeX + BADGE_DISPLAY, badgeY + BADGE_DISPLAY, 0x33000000);
            g.drawCenteredString(this.font, "?", badgeX + BADGE_DISPLAY / 2, badgeY + BADGE_DISPLAY / 2 - 4, 0x777777);
        }

        if (animActive) renderBadgeParticles(g, bcx, bcy, animT);

        // ── Nom du badge ────────────────────────────────────────────────────────
        int cx = leftPos + IMG_W / 2;
        Component badgeName = badge.hasSprite()
                ? Component.translatable(badge.translationKey())
                : Component.translatable("owteams.reputation.badge.none");
        int nameY = badgeY + BADGE_DISPLAY + 4;
        g.drawCenteredString(this.font, badgeName.copy().withStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(badge.accent())).withBold(true)), cx, nameY, badge.accent());

        // ── Score de réputation ─────────────────────────────────────────────────
        int scoreY = nameY + 12;
        Component score = Component.translatable("owteams.reputation.score", formatNumber(reputation));
        g.drawCenteredString(this.font, score, cx, scoreY, 0xFFFFFF);

        // ── Barre de progression du palier courant (dégradé badge actuel → badge suivant) ─────
        int barX = cx - BAR_W / 2, barY = scoreY + 14;
        int startColor = badge.accent();
        int endColor = next != null ? next.accent() : badge.accent();
        drawProgressBar(g, barX, barY, OWReputation.progress(reputation, badge), startColor, endColor);

        // Sous la barre : rep actuelle / seuil suivant (ou « palier max »).
        String progressLabel = next != null
                ? formatNumber(reputation) + " / " + formatNumber(next.threshold())
                : Component.translatable("owteams.reputation.max_tier").getString();
        g.drawCenteredString(this.font, progressLabel, cx, barY + BAR_H + 3, 0xBBBBBB);

        super.render(g, mouseX, mouseY, partial);
        renderTribeTabs(g, mouseX, mouseY, Tab.REPUTATION);

        // Tooltip détaillé au survol du badge : badge courant + les 9 paliers (image + nom + seuil).
        if (mouseX >= badgeX && mouseX < badgeX + BADGE_DISPLAY && mouseY >= badgeY && mouseY < badgeY + BADGE_DISPLAY) {
            renderBadgeTooltip(g, badge, mouseX, mouseY);
        }
    }

    /**
     * Tooltip dessiné à la main : en-tête (badge courant) puis une <b>frise horizontale</b> façon
     * Clash of Clans — les 9 paliers côte à côte, du plus faible (gauche) au plus prestigieux
     * (droite), sprite au-dessus et seuil en dessous ; le palier courant est surligné.
     */
    private void renderBadgeTooltip(GuiGraphics g, OWReputation.Badge current, int mouseX, int mouseY) {
        final int hIcon = 18, hGap = 5;     // en-tête
        final int bSize = 18;               // sprite dans la frise
        final float tScale = 0.65f;         // échelle des seuils
        OWReputation.Badge[] all = OWReputation.Badge.values();
        int n = all.length - 1;             // 9 paliers (hors NONE)

        // En-tête : badge courant.
        Component header = (current.hasSprite()
                ? Component.translatable(current.translationKey())
                : Component.translatable("owteams.reputation.badge.none"))
                .copy().withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(current.accent())));
        int headerW = (current.hasSprite() ? hIcon + hGap : 0) + this.font.width(header);

        Component tiersLabel = Component.translatable("owteams.reputation.tiers")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFE070)));

        // Frise : une cellule par palier (largeur = max(sprite, seuil)).
        String[] vals = new String[n];
        int cellW = 0;
        for (int i = 0; i < n; i++) {
            vals[i] = formatNumber(all[i + 1].threshold());
            cellW = Math.max(cellW, Math.max(bSize, Math.round(this.font.width(vals[i]) * tScale)));
        }
        cellW += 4; // gouttière
        final int step = 4;                 // décalage vertical entre deux paliers (effet escalier)
        int riseTotal = (n - 1) * step;
        int valueH = Math.round(this.font.lineHeight * tScale);
        int stripH = riseTotal + bSize + 2 + valueH;
        int stripW = cellW * n;

        int width = Math.max(Math.max(headerW, this.font.width(tiersLabel)), stripW);
        int height = (hIcon + 1) + 5 + 11 + stripH;

        // Position : la frise est CENTRÉE horizontalement sur le curseur, puis recalée si elle
        // dépasse d'un bord de l'écran.
        int tx = mouseX - width / 2, ty = mouseY - 12;
        if (tx + width + 3 > this.width) tx = this.width - width - 4;
        if (tx < 4) tx = 4;
        if (ty < 4) ty = 4;
        if (ty + height + 3 > this.height) ty = Math.max(4, this.height - height - 4);

        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        drawTooltipFrame(g, tx, ty, width, height);

        int y = ty;
        // En-tête.
        int hx = tx;
        if (current.hasSprite()) { renderBadge(g, current, tx, y, hIcon); hx = tx + hIcon + hGap; }
        g.drawString(this.font, header, hx, y + (hIcon - 9) / 2, 0xFFFFFF, true);
        y += hIcon + 1 + 5;

        // Libellé « Paliers : ».
        g.drawString(this.font, tiersLabel, tx, y, 0xFFFFFF, true);
        y += 11;

        // Frise horizontale en escalier : le plus faible en bas à gauche, le plus fort en haut à droite.
        // Chaque badge repose sur un « podium » à sa couleur, descendant jusqu'à une base commune.
        int baseline = y + stripH;
        for (int i = 0; i < n; i++) {
            OWReputation.Badge b = all[i + 1];
            boolean cur = b == current;
            int cellX = tx + i * cellW;
            int cellCx = cellX + cellW / 2;
            int by = y + (riseTotal - i * step);   // chaque palier monte d'un cran
            int col = b.accent() & 0xFFFFFF;

            if (cur) g.fill(cellX, by - 2, cellX + cellW, baseline + 1, 0x26FFFFFF);

            // Podium teinté (corps + arête supérieure plus vive).
            int pw = cellW - 6, px = cellCx - pw / 2, pTop = by + bSize - 1;
            g.fill(px, pTop, px + pw, baseline, (cur ? 0x77 << 24 : 0x55 << 24) | col);
            g.fill(px, pTop, px + pw, pTop + 2, (0xCC << 24) | col);

            renderBadge(g, b, cellCx - bSize / 2, by, bSize);

            // Seuil écrit sur le podium (en blanc pour le contraste).
            float vw = this.font.width(vals[i]) * tScale;
            Style st = Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF));
            if (cur) st = st.withBold(true);
            g.pose().pushPose();
            g.pose().translate(cellCx - vw / 2f, by + bSize + 2, 0);
            g.pose().scale(tScale, tScale, 1f);
            g.drawString(this.font, Component.literal(vals[i]).withStyle(st), 0, 0, 0xFFFFFF, true);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    /** Cadre de tooltip façon vanilla (fond sombre + bordure dégradée violette). */
    private void drawTooltipFrame(GuiGraphics g, int x, int y, int w, int h) {
        final int bg = 0xF0100010, b1 = 0x505000FF, b2 = 0x5028007F;
        int left = x - 3, top = y - 3, right = x + w + 3, bottom = y + h + 3;
        g.fill(left, top + 1, right, bottom - 1, bg);
        g.fill(left + 1, top, right - 1, top + 1, bg);
        g.fill(left + 1, bottom - 1, right - 1, bottom, bg);
        g.fillGradient(left + 1, top + 2, left + 2, bottom - 2, b1, b2);
        g.fillGradient(right - 2, top + 2, right - 1, bottom - 2, b1, b2);
        g.fill(left + 1, top + 1, right - 1, top + 2, b1);
        g.fill(left + 1, bottom - 2, right - 1, bottom - 1, b2);
    }

    /** Écran de première activation : badge grisé cadenassé, encart de description centré, puis bouton. */
    private void renderActivationPrompt(GuiGraphics g) {
        int cx = leftPos + IMG_W / 2;

        // Badge grisé + cadenas (état « verrouillé »).
        renderLockedBadge(g, cx, topPos + 22, 28);

        // Titre jaune, réduit automatiquement s'il est trop large pour le panneau.
        drawFitCenteredTitle(g, Component.translatable("owteams.reputation.activate.title")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFE070))), cx, topPos + 53, IMG_W - 14);

        // Encart discret (léger renfoncement) qui accueille la description.
        int cx1 = leftPos + 12, cy1 = topPos + 64;
        int cx2 = leftPos + IMG_W - 12, cy2 = topPos + IMG_H - 30;
        g.fill(cx1, cy1, cx2, cy2, 0x14000000);
        g.fill(cx1, cy1, cx2, cy1 + 1, 0x33000000);        // haut (ombre)
        g.fill(cx1, cy1, cx1 + 1, cy2, 0x33000000);        // gauche (ombre)
        g.fill(cx1, cy2 - 1, cx2, cy2, 0x22FFFFFF);        // bas (lumière)
        g.fill(cx2 - 1, cy1, cx2, cy2, 0x22FFFFFF);        // droite (lumière)

        // Description : plus petite, centrée ligne par ligne, centrée verticalement dans l'encart
        // (bornée en haut pour ne jamais déborder du cadre).
        final float s = 0.8f;
        int wrapVisual = (cx2 - cx1) - 12;
        List<FormattedCharSequence> lines = this.font.split(
                Component.translatable("owteams.reputation.activate.desc"), (int) (wrapVisual / s));
        int lineH = Math.round(this.font.lineHeight * s) + 2;
        int y = Math.max(cy1 + 4, (cy1 + cy2) / 2 - (lines.size() * lineH) / 2);
        for (FormattedCharSequence line : lines) {
            float lw = this.font.width(line) * s;
            g.pose().pushPose();
            g.pose().translate(cx - lw / 2f, y, 0);
            g.pose().scale(s, s, 1f);
            g.drawString(this.font, line, 0, 0, 0x4A4A4A, false);
            g.pose().popPose();
            y += lineH;
        }

        // Message d'attente (membre sans droit d'activation), à la place du bouton.
        if (!canManage()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.reputation.activate.wait"),
                    cx, topPos + IMG_H - 19, 0x707070);
        }
    }

    /** Titre centré ; si trop large pour {@code maxW}, il est mis à l'échelle pour tenir. */
    private void drawFitCenteredTitle(GuiGraphics g, Component text, int cx, int y, int maxW) {
        int w = this.font.width(text);
        if (w <= maxW) {
            g.drawCenteredString(this.font, text, cx, y, 0xFFFFFF);
            return;
        }
        float s = maxW / (float) w;
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(s, s, 1f);
        g.drawString(this.font, text, -w / 2, 0, 0xFFFFFF, true); // ombre portée
        g.pose().popPose();
    }

    /** Médaille grisée (sprite du 1er badge teinté sombre) surmontée d'un cadenas : état « verrouillé ». */
    private void renderLockedBadge(GuiGraphics g, int cx, int yTop, int size) {
        RenderSystem.setShaderColor(0.42f, 0.42f, 0.46f, 1f);
        renderBadge(g, OWReputation.Badge.GOLD_FIRST, cx - size / 2, yTop, size);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        drawPadlock(g, cx, yTop + size / 2);
    }

    /** Petit cadenas dessiné à la main (anse en U + corps + trou de serrure). */
    private void drawPadlock(GuiGraphics g, int cx, int cy) {
        int dark = 0xFF2B2B2E, body = 0xFF4B4B50, edge = 0x55FFFFFF, hole = 0xFF17171A;
        // Anse (U) — posts + barre supérieure, centre creux.
        g.fill(cx - 4, cy - 6, cx - 2, cy, dark);
        g.fill(cx + 2, cy - 6, cx + 4, cy, dark);
        g.fill(cx - 4, cy - 7, cx + 4, cy - 5, dark);
        // Corps (contour sombre + intérieur clair + reflet).
        g.fill(cx - 6, cy, cx + 6, cy + 9, dark);
        g.fill(cx - 5, cy + 1, cx + 5, cy + 8, body);
        g.fill(cx - 5, cy + 1, cx + 5, cy + 2, edge);
        // Trou de serrure.
        g.fill(cx - 1, cy + 3, cx + 1, cy + 6, hole);
    }

    /**
     * Barre de progression du palier courant : dégradé horizontal <b>à deux couleurs</b> (couleur du
     * badge actuel à gauche → couleur du badge suivant à droite). Le remplissage suit la progression
     * intra-palier ; la partie non atteinte est assombrie et un curseur marque la position.
     */
    private void drawProgressBar(GuiGraphics g, int x, int y, float progress, int startColor, int endColor) {
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF101010);
        // Dégradé horizontal des deux couleurs (une tranche de 1 px par pas).
        for (int i = 0; i < BAR_W; i++) {
            float t = BAR_W <= 1 ? 0f : i / (float) (BAR_W - 1);
            int col = lerpColor(startColor & 0xFFFFFF, endColor & 0xFFFFFF, t);
            g.fill(x + i, y, x + i + 1, y + BAR_H, 0xFF000000 | col);
        }
        int fillW = Math.max(0, Math.min(BAR_W, Math.round(BAR_W * progress)));
        if (fillW < BAR_W) g.fill(x + fillW, y, x + BAR_W, y + BAR_H, 0xC00A0A0A); // assombrit le reste
        if (fillW > 0) g.fill(x, y, x + fillW, y + 2, 0x33FFFFFF);                 // reflet supérieur
        if (fillW > 0 && fillW < BAR_W) g.fill(x + fillW - 1, y - 1, x + fillW + 1, y + BAR_H + 1, 0xFFFFFFFF); // curseur
        // Bordure noire.
        g.fill(x - 1, y - 1, x + BAR_W + 1, y, 0xFF000000);
        g.fill(x - 1, y + BAR_H, x + BAR_W + 1, y + BAR_H + 1, 0xFF000000);
        g.fill(x - 1, y, x, y + BAR_H, 0xFF000000);
        g.fill(x + BAR_W, y, x + BAR_W + 1, y + BAR_H, 0xFF000000);
    }

    /** Interpolation linéaire entre deux couleurs RGB. */
    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int gg = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    // ── Animation de changement de palier ────────────────────────────────────────
    /** Compare le palier courant au dernier vu (persistant) : déclenche l'animation s'il a changé. */
    private void detectBadgeChange(int teamId, OWReputation.Badge badge) {
        int cur = badge.ordinal();
        int prev = lastBadgeOrdinal == -2
                ? net.tiew.operationWild.client.OWClientReputationSeen.get(teamId)
                : lastBadgeOrdinal;
        if (cur != prev) {
            if (badge.hasSprite()) triggerBadgeAnim(badge, prev >= 0 && cur < prev); // rétrogradation ?
            net.tiew.operationWild.client.OWClientReputationSeen.set(teamId, cur);
        }
        lastBadgeOrdinal = cur;
    }

    /** Lance l'explosion de particules (couleur du badge) + son (montée = level up / descente = défaite). */
    private void triggerBadgeAnim(OWReputation.Badge b, boolean demotion) {
        badgeAnimStart = System.currentTimeMillis();
        badgeAnimColor = b.accent();
        badgeParticles.clear();
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 64; i++) {
            float angle = (float) (r.nextDouble() * Math.PI * 2);
            float speed = 45f + r.nextFloat() * 135f;
            float size = 1.6f + r.nextFloat() * 3.0f;
            badgeParticles.add(new float[]{ angle, speed, size });
        }
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        demotion ? net.minecraft.sounds.SoundEvents.VILLAGER_NO
                                 : net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                        demotion ? 0.8f : 1.0f));
    }

    /** Halo coloré expansif derrière le badge. */
    private void renderBadgeHalo(GuiGraphics g, int cx, int cy, float t) {
        float p = Math.min(1f, t / (BADGE_ANIM_MS / 1000f));
        int base = badgeAnimColor & 0xFFFFFF;
        for (int k = 0; k < 3; k++) {
            float kp = p - k * 0.14f;
            if (kp < 0f || kp > 1f) continue;
            int r = (int) (10 + kp * 46);
            int a = (int) (95 * (1 - kp));
            if (a <= 0) continue;
            g.fill(cx - r, cy - r, cx + r, cy + r, (a << 24) | base);
        }
    }

    /** Particules radiales colorées (avec gravité et fondu) par-dessus le badge. */
    private void renderBadgeParticles(GuiGraphics g, int cx, int cy, float t) {
        float p = Math.min(1f, t / (BADGE_ANIM_MS / 1000f));
        int base = badgeAnimColor & 0xFFFFFF;
        for (float[] pt : badgeParticles) {
            float ang = pt[0], speed = pt[1], size = pt[2];
            float px = cx + (float) Math.cos(ang) * speed * t;
            float py = cy + (float) Math.sin(ang) * speed * t + 0.5f * 165f * t * t; // gravité
            float life = 1f - p;
            int a = (int) (255 * Math.max(0f, life));
            if (a <= 0) continue;
            int s = Math.max(1, (int) (size * (0.55f + 0.45f * life)));
            int ix = (int) px, iy = (int) py;
            g.fill(ix - s, iy - s, ix + s, iy + s, (a << 24) | base);
            int ha = Math.min(255, a + 70);
            g.fill(ix - 1, iy - 1, ix + 1, iy + 1, (ha << 24) | 0xFFFFFF); // cœur clair
        }
    }

    /**
     * Dessine un badge (sprite 23×23 de {@code ow_teams_sprites.png}) mis à l'échelle {@code size}.
     * Réutilisé par le dashboard et la liste des tribus.
     */
    public static void renderBadge(GuiGraphics g, OWReputation.Badge badge, int x, int y, int size) {
        if (badge == null || !badge.hasSprite()) return;
        g.blit(OW_SPRITES, x, y, size, size,
                (float) badge.u(), (float) badge.v(),
                OWReputation.Badge.SPRITE_SIZE, OWReputation.Badge.SPRITE_SIZE, 256, 256);
    }

    /** Formate un entier avec des espaces fines comme séparateurs de milliers (1 234 567). */
    static String formatNumber(int value) {
        String digits = Integer.toString(Math.abs(value));
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            sb.append(digits.charAt(i));
            if (++count % 3 == 0 && i > 0) sb.append(' ');
        }
        if (value < 0) sb.append('-');
        return sb.reverse().toString();
    }
}
