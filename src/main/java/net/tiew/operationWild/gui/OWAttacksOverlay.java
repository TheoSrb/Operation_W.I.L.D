package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;

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

        OWEntity entity = net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity
                .resolveControlledEntity(player);
        if (entity == null) return;

        // Le panda roux n'est pas monté mais porté : il n'a pas de cavalier dont vérifier le rang,
        // et ses cartes se posent à gauche de l'écran plutôt qu'à droite.
        boolean carriedOnShoulder = net.tiew.operationWild.entity.attacks.OWAttackLogic.isCarriedOnShoulder(entity);
        if (!carriedOnShoulder && entity.getPassengers().indexOf(player) != 0) return;

        boolean isCrocodileReadyForTaming = entity instanceof CrocodileEntity croc
                && croc.crocodileBehaviorHandler.isReadyForTaming() && !croc.isTame();
        if (!player.getUUID().equals(entity.getOwnerUUID()) && !isCrocodileReadyForTaming
                && !entity.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) return;

        Class<?> entityClass = entity.getClass();
        // Une seule carte secondaire est affichée : celle que le joueur a mise en place à la
        // molette. Le rang des cartes ne bouge donc pas quand il en change, seul le dessin change.
        List<OWAttack> attacks = OWAttacksHandler.getActiveAttacks(entity);

        boolean hasComboCard = !carriedOnShoulder;
        // Meme ancrage que les montures : une interface qui change de cote selon l'espece oblige
        // l'oeil a la rechercher. L'absence de carte de combo suffit a distinguer le porte.
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
        // Une espèce sans enchaînement n'affiche rien ici : la carte de combo serait une case morte,
        // et le clic gauche du joueur qui la porte reste son propre coup, pas celui de la bête.
        if (hasComboCard) applyCardScale(g, baseX, baseY, OWAttackLogic.getComboClickScale(), () -> {
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
            int cardX = baseX + (hasComboCard ? i + 1 : i) * CARD_SPACING;
            // Une carte peut imposer ses propres coordonnées dans l'atlas plutôt que de les déduire
            // de son rang : c'est le cas des secondaires interchangeables, qui partagent un rang mais
            // pas un dessin. La version grisée reste 20 pixels sous l'allumée, comme partout ailleurs.
            // Retournement en cours : tant que la tranche n'est pas passée, c'est encore la face
            // qu'on quitte qui est tournée vers le joueur. Sans cela la nouvelle carte serait déjà
            // là avant même que l'ancienne ait disparu, et le geste ne se lirait plus.
            float flipProgress = OWAttacksHandler.isSecondaryCard(attack)
                    ? OWAttackLogic.getSecondaryFlipProgress(entity.getId())
                    : -1f;
            OWAttack shown = attack;
            if (flipProgress >= 0f && flipProgress < 0.5f) {
                OWAttack leaving = OWAttackPacket.getAttack(OWAttackLogic.getSecondaryFlipFromId());
                if (leaving != null) shown = leaving;
            }

            int texX        = shown.hasCardTexture() ? shown.getCardTexX() : (columnOffset + i + 1) * CARD_SIZE;
            int cardNormalY = shown.hasCardTexture() ? shown.getCardTexY() : normalTexY;
            int cardGrayY   = cardNormalY + CARD_SIZE;

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

            // ── Éléphant Jet de Trompe (maintien) ─────────────────────────────
            boolean isWaterSpray = attack.getId() == OWAttacksHandler.WATER_SPRAY_ID;

            boolean isCharging = attack instanceof OWChargedAttack
                    && OWAttackLogic.isCharging
                    && OWAttackLogic.getCurrentAttackId() == attack.getId();

            // Recharge commune aux cartes secondaires interchangeables : elle passe avant le dessin
            // propre à chaque carte. Le Jet de Trompe montre normalement sa réserve d'eau, mais tant
            // que le compteur tourne c'est lui qui décide de la disponibilité — et c'est donc lui
            // qu'il faut voir se remplir, sans quoi la carte s'annoncerait prête sans l'être.
            float sharedCooldown = OWAttacksHandler.isSecondaryCard(attack)
                    ? OWAttackLogic.getSharedSecondaryProgress(entity)
                    : 0f;

            float fillProgress;
            boolean isGlowing = false;

            if (isCharging) {
                fillProgress = OWAttackLogic.getChargeProgress();
                isGlowing    = fillProgress >= 1.0f;
            } else if (sharedCooldown > 0f) {
                fillProgress = 1.0f - sharedCooldown;
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
            } else if (isWaterSpray && entity instanceof ElephantEntity elephant) {
                // La carte EST la jauge : son remplissage bas→haut donne le volume en trompe.
                // Grisée à sec, elle respire dès qu'il y a de quoi arroser ou de quoi puiser.
                fillProgress = elephant.getTrunkWaterRatio();
                isGlowing    = elephant.canUseTrunkWater();
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

            // Panda roux : accroupi le clic droit rend la bête au sol, et un objet à usage garde son
            // propre clic droit. Dans les deux cas la carte s'éteint, pour que l'indisponibilité se
            // voie avant le clic plutôt qu'après.
            boolean isHealOrbBlocked = attack.getId() == OWAttacksHandler.HEAL_SNACK_ID
                    && carriedOnShoulder && OWAttackLogic.isHealOrbBlocked(player);

            if (isGrabbing || isWhirlwindBlockedInWater || isHealOrbBlocked) {
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

            int counter = attack.isUltimate() && attack.hasUnlockCounter() && attack.isUnlocked(entity)
                    ? attack.getUnlockCount(entity) : 0;

            // L'écrasement du retournement se multiplie à l'échelle courante : la carte peut ainsi
            // se retourner alors même qu'elle respire ou qu'elle rebondit d'un clic.
            float flipScaleX = flipProgress >= 0f
                    ? cardScale * OWAttackLogic.getSecondaryFlipScaleX(flipProgress)
                    : cardScale;

            float flipLift = flipProgress >= 0f ? OWAttackLogic.getSecondaryFlipLift(flipProgress) : 0f;

            applyCardScale(g, cardX, baseY, flipScaleX, cardScale, flipLift, () -> {
                // Rendu unifié : grisé haut→bas pour drain (fillProgress 1→0),
                // coloré bas→haut pour remplissage (fillProgress 0→1).
                // Le branch NAP utilise le même rendu que les autres ultimes.
                if (fGrayH > 0) {
                    g.blit(TEXTURE, cardX, baseY,
                            texX, cardGrayY,
                            CARD_SIZE, fGrayH,
                            TEX_SIZE, TEX_SIZE);
                }
                if (fColoredH > 0) {
                    g.blit(TEXTURE, cardX, baseY + fGrayH,
                            texX, cardNormalY + fGrayH,
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

            // Hors du scale de rebond : le rappel de commande doit rester lisible et immobile,
            // il ne fait pas partie de l'animation de la carte.
            if (OWAttacksHandler.hasSwitchableSecondary(entityClass)
                    && OWAttacksHandler.isSecondaryCard(attack)) {
                drawSwitchHint(g, cardX, baseY, flipProgress);
            }

            // Hors de l'echelle de la carte : la bulle a sa propre vie, elle monte et s'efface
            // pendant que la carte respire dessous.
            if (attack.isUltimate() && attack.hasUnlockCounter()) {
                noteUnlockCounter(entity.getId(), counter);
                drawUnlockPop(g, cardX, baseY, entity.getId());
            }
        }

    }

    // ── Rappel « touche + molette » sur la carte secondaire interchangeable ────

    /**
     * Pictogramme du changement de carte, pris dans l'atlas des cartes.
     *
     * <p>Il remplace le libellé « TOUCHE + » et sa souris dessinée au pixel : sur vingt pixels de
     * carte, un texte à l'échelle 0,35 restait pâteux et mangeait l'illustration. Le sens de la
     * commande est désormais expliqué en toutes lettres dans la fiche d'attaques ; ici, le
     * pictogramme ne sert plus qu'à signaler que la carte est interchangeable.</p>
     */
    private static final int SWITCH_ICON_U = 0;
    private static final int SWITCH_ICON_V = 247;
    private static final int SWITCH_ICON_W = 10;
    private static final int SWITCH_ICON_H = 9;
    /**
     * Débord du rappel hors de la carte, vers le bas et vers la droite.
     *
     * <p>Il déborde plutôt que de se poser dessus : l'illustration reste ainsi entière. Vers le bas
     * la marge est comptée au plus juste — les cartes sont posées à vingt-deux pixels du bord de
     * l'écran, il n'en reste que deux sous elles.</p>
     */
    /** Bulle de surcharge : duree de vie, hauteur de montee, echelle et debord sous la carte. */
    private static final long COUNTER_POP_MS = 950L;
    private static final float COUNTER_POP_RISE = 9f;
    private static final float COUNTER_POP_SCALE = 0.78f;
    private static final int COUNTER_POP_OFFSET_X = 4;
    private static final int COUNTER_POP_OFFSET_Y = 1;

    private static int counterPopEntityId = -1;
    private static int counterPopValue = 0;
    private static int counterPopSeen = -1;
    private static long counterPopStartMs = 0L;

    private static final int HINT_OFFSET_X = 3;
    private static final int HINT_OFFSET_Y = 2;

    /**
     * Marque la carte comme interchangeable, au coin bas-droit.
     *
     * <p>Le pictogramme déborde de l'angle au lieu de se poser dessus : l'illustration reste
     * entière, et seul le coin est effleuré. Il ne dit pas QUELLE touche presser — c'est le rôle
     * de la fiche d'attaques, qui a la place de l'écrire lisiblement.</p>
     */
    /**
     * Compte de charge de l'ultime, en haut a droite de la carte.
     *
     * <p>Hors de l'echelle de rebond, comme le rappel de commande : c'est une information, elle doit
     * rester lisible et immobile pendant que la carte respire.</p>
     */
    /**
     * Jauge de surcharge : une pastille par seconde de bonus, sous la carte.
     *
     * <p>Un nombre ecrit ne tenait pas a cette taille — vingt pixels de carte, une police qu'il
     * fallait reduire encore, et le tout se lisait comme un defaut d'affichage. Les pastilles se
     * comptent d'un coup d'oeil et disent en plus ce qui reste a gagner, ce que le nombre seul ne
     * disait pas.</p>
     */
    /**
     * Anneau de surcharge : le pourtour de la carte se dessine a mesure qu'elle accumule.
     *
     * <p>Trois formes ont echoue avant celle-ci, toutes pour la meme raison : elles cherchaient une
     * place SUR une carte de vingt pixels deja pleine. Le pourtour est le seul espace qu'elle
     * n'utilise pas. Le trait part du coin bas gauche et tourne dans le sens horaire ; sa longueur
     * dit le compte sans qu'aucun chiffre soit ecrit, et le sillon sombre dessous montre ce qui
     * reste a gagner.</p>
     */
    /**
     * Repere le moment ou un en-cas de plus vient d'etre servi.
     *
     * <p>Aucun paquet : le compte est deja synchronise, et une valeur qui MONTE ne peut vouloir dire
     * qu'une chose. Elle retombe a zero quand l'ultime part, ce qui reamorce le guet tout seul.</p>
     */
    private static void noteUnlockCounter(int entityId, int count) {
        if (entityId != counterPopEntityId) {
            counterPopEntityId = entityId;
            counterPopSeen = count;
            counterPopStartMs = 0L;
            return;
        }

        if (count > counterPopSeen) {
            counterPopValue = count;
            counterPopStartMs = System.currentTimeMillis();
        }
        counterPopSeen = count;
    }

    /**
     * Bulle « +N » au coin bas droit de la carte, qui monte et s'efface.
     *
     * <p>Trois affichages permanents ont echoue avant elle, tous pour la meme raison : ils
     * cherchaient une place durable sur une carte de vingt pixels deja pleine. Celui-ci ne dure
     * qu'une seconde — il annonce le gain au moment ou il arrive, puis rend la carte a son etat.</p>
     */
    private static void drawUnlockPop(GuiGraphics g, int cardX, int cardY, int entityId) {
        if (entityId != counterPopEntityId || counterPopValue <= 0) return;

        long age = System.currentTimeMillis() - counterPopStartMs;
        if (counterPopStartMs == 0L || age >= COUNTER_POP_MS) return;

        float life = age / (float) COUNTER_POP_MS;
        int alpha = (int) (net.minecraft.util.Mth.clamp((1f - life) * 2.2f, 0f, 1f) * 255f);
        if (alpha <= 8) return;

        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        String text = "+" + counterPopValue;

        float x = cardX + CARD_SIZE + COUNTER_POP_OFFSET_X - font.width(text) * COUNTER_POP_SCALE;
        float y = cardY + CARD_SIZE + COUNTER_POP_OFFSET_Y
                - font.lineHeight * COUNTER_POP_SCALE - COUNTER_POP_RISE * life;

        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().scale(COUNTER_POP_SCALE, COUNTER_POP_SCALE, 1f);
        g.drawString(font, text, 0, 0, (alpha << 24) | 0xFFFFFF, true);
        g.pose().popPose();
    }

    private static void drawSwitchHint(GuiGraphics g, int cardX, int cardY, float flipProgress) {
        int x = cardX + CARD_SIZE - SWITCH_ICON_W + HINT_OFFSET_X;
        int y = cardY + CARD_SIZE - SWITCH_ICON_H + HINT_OFFSET_Y;

        if (flipProgress < 0f) {
            g.blit(TEXTURE, x, y, SWITCH_ICON_U, SWITCH_ICON_V,
                    SWITCH_ICON_W, SWITCH_ICON_H, TEX_SIZE, TEX_SIZE);
            return;
        }

        // Il suit le bond de la carte — il en est solidaire — et tourne sur lui-même par-dessus.
        float angle = OWAttackLogic.getSecondaryFlipIconAngle(flipProgress);
        float lift = OWAttackLogic.getSecondaryFlipLift(flipProgress);
        float cx = x + SWITCH_ICON_W / 2f;
        float cy = y + SWITCH_ICON_H / 2f;

        g.pose().pushPose();
        g.pose().translate(cx, cy - lift, 0f);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        g.pose().translate(-cx, -cy, 0f);
        g.blit(TEXTURE, x, y, SWITCH_ICON_U, SWITCH_ICON_V,
                SWITCH_ICON_W, SWITCH_ICON_H, TEX_SIZE, TEX_SIZE);
        g.pose().popPose();
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
        applyCardScale(g, cardX, cardY, scale, scale, 0f, draw);
    }

    /**
     * Même chose, mais avec deux échelles distinctes et un décalage vertical.
     *
     * <p>Le retournement de carte n'écrase que l'horizontale : c'est ce qui simule la rotation
     * autour de l'axe vertical. La verticale, elle, continue de porter le rebond du clic ou la
     * respiration de la lueur, sans que les deux effets se marchent dessus. Le décalage, lui, sert
     * au petit bond que la carte fait en se retournant.</p>
     */
    private static void applyCardScale(GuiGraphics g, int cardX, int cardY,
                                       float scaleX, float scaleY, float liftY, Runnable draw) {
        if (scaleX == 1f && scaleY == 1f && liftY == 0f) { draw.run(); return; }
        float cx = cardX + CARD_SIZE / 2f;
        float cy = cardY + CARD_SIZE / 2f;
        g.pose().pushPose();
        g.pose().translate(cx, cy - liftY, 0f);
        g.pose().scale(scaleX, scaleY, 1f);
        g.pose().translate(-cx, -cy, 0f);
        draw.run();
        g.pose().popPose();
    }
}
