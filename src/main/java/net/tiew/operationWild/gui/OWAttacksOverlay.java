package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;

import java.util.List;

/**
 * Cartes d'attaque affichées en bas à droite.
 *
 * ── Texture : ow_tamed_attacksl.png ─────────────────────────────────────────
 *   Colonne X  = index de carte × 20  (0=combo, 20=attaque1, 40=attaque2…)
 *   Rangée normale  Y = entityRow × 40
 *   Rangée grisée   Y = entityRow × 40 + 20
 *   → 2 rangées de 20px par entité, donc +40 pour chaque nouvelle entité.
 *
 * ── Comportement des cartes ──────────────────────────────────────────────────
 *   Carte 0 (combo)    : toujours colorée ; barre de cooldown selon attackTimer.
 *   Cartes 1+ (chargée): grisée par défaut.
 *     • En cooldown    : coloré monte de bas en haut à mesure que le cooldown expire.
 *     • En charge      : coloré monte de bas en haut à mesure que la charge augmente.
 *     • Charge max     : colorée complète + légère lueur blanche.
 *
 * ── Pour ajouter une entité ──────────────────────────────────────────────────
 *   Dans OWAttacksHandler.registerAll() :
 *     registerEntityRow(MonEntity.class, N);       // N=0 pour rangée Y=0/20, N=1→Y=40/60…
 *     registerComboMaxTimer(MonEntity.class, 14);  // timeMax du createCombo de l'entité
 *     register(MonEntity.class, MonAttacks.ATTAQUE);
 */
public class OWAttacksOverlay {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/ow_tamed_attacks.png");

    private static final int CARD_SIZE    = 20;
    private static final int TEX_SIZE     = 256;
    private static final int CARD_SPACING = 25;

    // Bordure animée quand la carte est prête / chargée à fond
    private static final int GLOW_BORDER_ULTIMATE = 0x40E0FF; // or — ultime prêt
    private static final int GLOW_BORDER_CHARGED  = 0xFFD700; // cyan — attaque chargée au max
    private static final int ERROR_CARD_COLOR = 0xFF0000;

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        Player player = mc.player;
        if (player == null) return;

        if (!(player.getRootVehicle() instanceof OWEntity entity)) return;

        if (entity.getPassengers().indexOf(player) != 0) return;

        if (!player.getUUID().equals(entity.getOwnerUUID())) return;

        Class<?> entityClass = entity.getClass();
        List<OWAttack> attacks = OWAttacksHandler.getAttacks(entityClass);

        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;

        int entityRow  = OWAttacksHandler.getEntityRow(entityClass);
        int normalTexY = entityRow * 40;        // rangée colorée dans la texture
        int grayTexY   = entityRow * 40 + 20;  // rangée grisée (juste en dessous)

        boolean isGrabbing = entity instanceof CrocodileEntity && entity.isGrabbing();

        // ── Carte 0 : combo ───────────────────────────────────────────────────
        applyCardScale(g, baseX, baseY, OWAttackLogic.getComboClickScale(), () -> {
            if (isGrabbing) {
                // Tout gris quand grab actif
                g.blit(TEXTURE, baseX, baseY, 0, grayTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);
                return;
            }
            g.blit(TEXTURE, baseX, baseY, 0, normalTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);

            if (entity.attackTimer >= 1) {
                int comboTimeMax = OWAttacksHandler.getComboMaxTimer(entityClass);
                int coloredH = Math.min(CARD_SIZE, (int)((float) CARD_SIZE / comboTimeMax * entity.attackTimer));
                int grayH    = CARD_SIZE - coloredH;

                g.blit(TEXTURE, baseX, baseY, 0, grayTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);
                if (coloredH > 0) {
                    g.blit(TEXTURE, baseX, baseY + grayH,
                            0, normalTexY + grayH,
                            CARD_SIZE, coloredH,
                            TEX_SIZE, TEX_SIZE);
                }
            }

            if (OWAttackLogic.comboClickAnimInvalid && OWAttackLogic.comboClickAnimTimeMs >= 0) {
                float p = (float)(System.currentTimeMillis() - OWAttackLogic.comboClickAnimTimeMs) / 200f;
                if (p >= 0f && p < 1f) {
                    int alpha = (int)((1f - p) * 80);
                    g.fill(baseX, baseY, baseX + CARD_SIZE, baseY + CARD_SIZE,
                            (alpha << 24) | ERROR_CARD_COLOR);
                }
            }
        });

        // ── Cartes 1+ : attaques enregistrées ────────────────────────────────
        if (attacks == null) return;

        for (int i = 0; i < attacks.size(); i++) {
            OWAttack attack = attacks.get(i);
            int cardX = baseX + (i + 1) * CARD_SPACING;
            int texX  = (i + 1) * CARD_SIZE;

            // ── Primal Dive special states ────────────────────────────────────
            boolean isPrimalDive = attack.getId() == OWAttacksHandler.PRIMAL_DIVE_ID;
            boolean isTargeting  = isPrimalDive && OWAttackLogic.isCrocTargeting;
            float   grabProgress = isPrimalDive ? OWAttackLogic.getCrocGrabActiveProgress() : 0f;

            // ── Bear Nap special state ────────────────────────────────────────
            boolean isNapUltimate    = attack.getId() == OWAttacksHandler.NAP_ULTIMATE_ID;
            boolean isNapDrainActive = false;

            boolean isCharging = attack instanceof OWChargedAttack
                    && OWAttackLogic.isCharging
                    && OWAttackLogic.getCurrentAttackId() == attack.getId();

            float fillProgress;
            boolean isGlowing = false;

            if (isCharging) {
                fillProgress = OWAttackLogic.getChargeProgress();
                isGlowing    = fillProgress >= 1.0f;
            } else if (grabProgress > 0f) {
                // Phase 2 active: drain 1→0 over 10 s (like tiger ultimate timer)
                fillProgress = grabProgress;
            } else if (attack.isUltimate()) {
                float activeProgress = OWAttackLogic.getUltimateActiveProgress(entity.getId(), attack.getId());
                if (activeProgress > 0f) {
                    fillProgress = activeProgress;
                    // NAP : carte toujours glowing (cliquable pour annuler), drain haut→bas
                    if (isNapUltimate && OWAttackLogic.isKodiakNapping) {
                        isNapDrainActive = true;
                        isGlowing        = true;
                    }
                } else {
                    // NAP expirée naturellement → déclencher le cooldown côté client
                    if (isNapUltimate && OWAttackLogic.isKodiakNapping) {
                        OWAttackLogic.onKodiakNapExpired(entity.getId(), attack);
                    }
                    if (!attack.isUnlocked(entity)) {
                        fillProgress = attack.getUnlockProgress(entity);
                    } else {
                        float cooldownRemaining = OWAttackLogic.getCooldownProgress(entity.getId(), attack.getId());
                        if (cooldownRemaining > 0f) {
                            fillProgress = 1.0f - cooldownRemaining;
                        } else {
                            fillProgress = 1.0f;
                            isGlowing    = true;
                        }
                    }
                }
            } else {
                float cooldownRemaining = OWAttackLogic.getCooldownProgress(entity.getId(), attack.getId());
                fillProgress = cooldownRemaining > 0f ? 1.0f - cooldownRemaining : 1.0f;
            }

            if (isGrabbing) {
                fillProgress     = 0f;
                isGlowing        = false;
                isNapDrainActive = false;
            }

            final int     fColoredH = (int)(CARD_SIZE * fillProgress);
            final int     fGrayH    = CARD_SIZE - fColoredH;
            final boolean fGlowing  = isGlowing;
            // Targeting phase → green pulsing border ("still clickable"); else normal border
            final boolean fShowBorder = fGlowing || isTargeting;
            final int fBorderRgb = isTargeting
                    ? 0x00FF55                                             // green
                    : (attack.isUltimate() ? GLOW_BORDER_ULTIMATE : GLOW_BORDER_CHARGED);

            // Pulse scale: glow or targeting both trigger the pulse animation
            float clickScale = OWAttackLogic.getAttackClickScale(attack.getId());
            float cardScale  = (fGlowing || isTargeting) && clickScale == 1f
                    ? OWAttackLogic.getGlowPulseScale()
                    : clickScale;

            boolean fInvalid = OWAttackLogic.isAttackClickInvalid(attack.getId());

            applyCardScale(g, cardX, baseY, cardScale, () -> {
                // Rendu unifié : grisé haut→bas pour drain (fillProgress 1→0),
                // coloré bas→haut pour remplissage (fillProgress 0→1).
                // Le branch NAP utilise le même rendu que les autres ultimes.
                if (fGrayH > 0) {
                    g.blit(TEXTURE, cardX, baseY,
                            texX, grayTexY,
                            CARD_SIZE, fGrayH,
                            TEX_SIZE, TEX_SIZE);
                }
                if (fColoredH > 0) {
                    g.blit(TEXTURE, cardX, baseY + fGrayH,
                            texX, normalTexY + fGrayH,
                            CARD_SIZE, fColoredH,
                            TEX_SIZE, TEX_SIZE);
                }
                if (fShowBorder) {
                    drawGlowBorder(g, cardX, baseY, fBorderRgb);
                }

                if (fInvalid) {
                    float p = OWAttackLogic.getClickAnimProgress(attack.getId());
                    if (p >= 0f && p < 1f) {
                        int alpha = (int)((1f - p) * 80);
                        g.fill(cardX, baseY, cardX + CARD_SIZE, baseY + CARD_SIZE,
                                (alpha << 24) | ERROR_CARD_COLOR);
                    }
                }
            });
        }

    }

    /**
     * Dessine une bordure de 1 px autour de la carte avec une alpha pulsante (effet de lueur).
     * @param rgbColor couleur RGB sans alpha (ex : 0xFFD700 pour l'or)
     */
    private static void drawGlowBorder(GuiGraphics g, int cardX, int cardY, int rgbColor) {
        long t = System.currentTimeMillis();
        float pulse = (float)(Math.sin(t / 400.0 * Math.PI * 2) * 0.5 + 0.5); // 0..1
        int alpha = (int)(pulse * 180) + 75; // 75..255
        int color = (alpha << 24) | (rgbColor & 0x00FFFFFF);
        // top / bottom / left / right (1 px à l'extérieur de la carte)
        g.fill(cardX - 1, cardY - 1, cardX + CARD_SIZE + 1, cardY,                    color);
        g.fill(cardX - 1, cardY + CARD_SIZE, cardX + CARD_SIZE + 1, cardY + CARD_SIZE + 1, color);
        g.fill(cardX - 1, cardY,             cardX,                  cardY + CARD_SIZE, color);
        g.fill(cardX + CARD_SIZE, cardY,     cardX + CARD_SIZE + 1,  cardY + CARD_SIZE, color);
    }

    /**
     * Applique une transformation de scale centrée sur la carte avant de dessiner son contenu,
     * puis restaure la matrice. Si scale == 1.0 le lambda est appelé directement (pas de push/pop).
     */
    private static void applyCardScale(GuiGraphics g, int cardX, int cardY, float scale, Runnable draw) {
        if (scale == 1f) { draw.run(); return; }
        float cx = cardX + CARD_SIZE / 2f;
        float cy = cardY + CARD_SIZE / 2f;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0f);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-cx, -cy, 0f);
        draw.run();
        g.pose().popPose();
    }
}
