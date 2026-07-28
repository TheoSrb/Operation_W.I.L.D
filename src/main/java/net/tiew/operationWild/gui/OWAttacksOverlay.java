package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
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

        boolean isCrocodileReadyForTaming = entity instanceof CrocodileEntity croc
                && croc.crocodileBehaviorHandler.isReadyForTaming() && !croc.isTame();
        if (!player.getUUID().equals(entity.getOwnerUUID()) && !isCrocodileReadyForTaming
                && !entity.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) return;

        Class<?> entityClass = entity.getClass();
        List<OWAttack> attacks = OWAttacksHandler.getAttacks(entityClass);

        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;

        int entityRow  = OWAttacksHandler.getEntityRow(entityClass);
        int normalTexY = entityRow * 40;        // rangée colorée dans la texture
        int grayTexY   = entityRow * 40 + 20;  // rangée grisée (juste en dessous)

        // Décalage horizontal de la 1ère carte dans la texture (ex : Boa = colonne 60px).
        int columnOffset = OWAttacksHandler.getEntityColumn(entityClass);
        int comboTexX    = columnOffset * CARD_SIZE;

        boolean isGrabbing = entity instanceof CrocodileEntity && entity.isGrabbing();

        // ── Carte 0 : combo ───────────────────────────────────────────────────
        applyCardScale(g, baseX, baseY, OWAttackLogic.getComboClickScale(), () -> {
            if (isGrabbing) {
                // Tout gris quand grab actif
                g.blit(TEXTURE, baseX, baseY, comboTexX, grayTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);
                return;
            }
            g.blit(TEXTURE, baseX, baseY, comboTexX, normalTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);

            if (entity.attackTimer >= 1) {
                int comboTimeMax = OWAttacksHandler.getComboMaxTimer(entityClass);
                int coloredH = Math.min(CARD_SIZE, (int)((float) CARD_SIZE / comboTimeMax * entity.attackTimer));
                int grayH    = CARD_SIZE - coloredH;

                g.blit(TEXTURE, baseX, baseY, comboTexX, grayTexY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);
                if (coloredH > 0) {
                    g.blit(TEXTURE, baseX, baseY + grayH,
                            comboTexX, normalTexY + grayH,
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
            int texX  = (columnOffset + i + 1) * CARD_SIZE;

            // ── Primal Dive special states ────────────────────────────────────
            boolean isPrimalDive = attack.getId() == OWAttacksHandler.PRIMAL_DIVE_ID;
            boolean isTargeting  = isPrimalDive && OWAttackLogic.isCrocTargeting;
            float   grabProgress = isPrimalDive ? OWAttackLogic.getCrocGrabActiveProgress() : 0f;

            // ── Bear Nap special state ────────────────────────────────────────
            boolean isNapUltimate    = attack.getId() == OWAttacksHandler.NAP_ULTIMATE_ID;
            boolean isNapDrainActive = false;

            // ── Boa Crochets Venimeux (toggle) ────────────────────────────────
            boolean isVenomFangs = attack.getId() == OWAttacksHandler.VENOM_FANGS_ID;
            boolean isVenomArmed = isVenomFangs && entity instanceof BoaEntity boa && boa.isVenomArmed();

            // ── Kangourou Tornade de Poings (maintien) ────────────────────────
            boolean isWhirlwind = attack.getId() == OWAttacksHandler.WHIRLWIND_FISTS_ID;

            boolean isCharging = attack instanceof OWChargedAttack
                    && OWAttackLogic.isCharging
                    && OWAttackLogic.getCurrentAttackId() == attack.getId();

            float fillProgress;
            boolean isGlowing = false;

            if (isCharging) {
                fillProgress = OWAttackLogic.getChargeProgress();
                isGlowing    = fillProgress >= 1.0f;
            } else if (isVenomFangs && entity instanceof BoaEntity boa) {
                // Armé : carte pleine qui respire ; en cooldown : remplissage progressif bas→haut ; sinon prête.
                if (isVenomArmed) {
                    fillProgress = 1.0f;
                    isGlowing    = true;
                } else {
                    int cd = boa.getVenomCooldownTicks();
                    fillProgress = cd > 0
                            ? 1.0f - (float) cd / OWAttacksConstants.Boa.VENOM_FANGS_COOLDOWN_TICKS
                            : 1.0f;
                }
            } else if (isWhirlwind && entity instanceof KangarooEntity kangaroo) {
                if (kangaroo.isSpinning()) {
                    // Respiration jaune en continu ; pleine pendant les 3 premières secondes,
                    // puis se vide (gris par-dessus) sur les 12 s restantes (15 s - 3 s).
                    isGlowing = true;
                    int t    = kangaroo.clientSpinTicks;
                    int peak = OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_THRESHOLD_TICKS; // 60 (3 s)
                    int max  = OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS;        // 300 (15 s)
                    fillProgress = t <= peak
                            ? 1.0f
                            : Math.max(0f, 1.0f - (float) (t - peak) / (max - peak));
                } else {
                    int cd = kangaroo.getWhirlwindCooldownTicks();
                    fillProgress = cd > 0
                            ? 1.0f - (float) cd / OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_TICKS
                            : 1.0f;
                }
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

            // Tornade indisponible dans l'eau : carte grisée.
            boolean isWhirlwindBlockedInWater = isWhirlwind && entity.isInWater();

            if (isGrabbing || isWhirlwindBlockedInWater) {
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
