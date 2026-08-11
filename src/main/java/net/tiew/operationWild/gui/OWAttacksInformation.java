package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Overlay d'information sur les attaques — affiché tant que la touche dédiée est maintenue.
 *
 * ─── Ajouter une entité ──────────────────────────────────────────────────────
 *   1. Créer un nouveau bloc dans la section "DÉFINITIONS" ci-dessous.
 *   2. PROFILES.put(MonEntity.class, new EntityProfile(...)).
 *   3. Pour les cartes, cardTexX = index × 20, cardTexY = entityRow × 40 — sauf carte secondaire
 *      interchangeable, qui porte ses propres coordonnées où qu'elles soient dans l'atlas.
 *
 * ─── Formater du texte ───────────────────────────────────────────────────────
 *   e -> desc("ow.attacks.xxx.desc", val("3"), val(e.getDamage()))
 */
public class OWAttacksInformation {

    // ── Textures ──────────────────────────────────────────────────────────────
    private static final ResourceLocation BG    = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/attacks_information.png");
    private static final ResourceLocation CARDS = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/ow_tamed_attacks.png");

    private static final int CARD_SIZE = 20;
    private static final int TEX_SIZE  = 256;
    private static final int BG_W      = 256;
    private static final int BG_H      = 136;

    // ── Animation ouverte / fermée ────────────────────────────────────────────
    private static long    openStartMs  = -1L;
    private static long    closeStartMs = -1L;
    private static boolean wasKeyDown   = false;
    private static final long ANIM_MS   = 160L;

    /** Appelé chaque tick client (depuis ClientEvents). */
    public static void tick() {
        boolean keyDown = OWKeysBinding.OW_ATTACKS_INFO.isDown();
        long    now     = System.currentTimeMillis();

        if (keyDown && !wasKeyDown) {
            openStartMs  = now;
            closeStartMs = -1L;
        } else if (!keyDown && wasKeyDown) {
            closeStartMs = now;
            openStartMs  = -1L;
        }
        wasKeyDown = keyDown;
    }

    /** true pendant l'ouverture, la phase active et la fermeture animée. */
    public static boolean shouldRender() {
        long now = System.currentTimeMillis();
        return wasKeyDown || (closeStartMs >= 0 && now - closeStartMs < ANIM_MS);
    }

    private static float animScale() {
        long now = System.currentTimeMillis();
        if (openStartMs  >= 0) return easeOut(Math.min(1f, (float)(now - openStartMs)  / ANIM_MS));
        if (closeStartMs >= 0) return easeOut(Math.max(0f, 1f - (float)(now - closeStartMs) / ANIM_MS));
        return 1f;
    }

    private static float easeOut(float t) { return 1f - (1f - t) * (1f - t); }

    // =========================================================================
    //  MODÈLE DE DONNÉES
    // =========================================================================

    record AttackSlot(
            int cardTexX, int cardTexY,
            String keyLabel,
            Component title,
            Function<OWEntity, Component> desc
    ) {
        boolean hasCard() { return cardTexX >= 0; }
    }

    record EntityProfile(
            AttackSlot combo,
            AttackSlot charged,
            AttackSlot charged2,
            AttackSlot ultimate,
            AttackSlot passive
    ) {
        /** Espèce à carte secondaire unique : la seconde reste vide. */
        EntityProfile(AttackSlot combo, AttackSlot charged, AttackSlot ultimate, AttackSlot passive) {
            this(combo, charged, null, ultimate, passive);
        }

        /**
         * Colonnes d'attaque (combo/secondaire/ultime) ; les null sont filtrés → moins de colonnes.
         * Le passif est affiché à part (bandeau du bas), pas comme une colonne.
         *
         * <p>Une espèce qui porte plusieurs cartes secondaires n'en documente qu'une : celle qui est
         * en place. La fiche reste ainsi sur trois colonnes lisibles, et dit ce que fait vraiment le
         * clic droit à cet instant — comme le HUD, qui n'affiche lui aussi que la carte équipée.</p>
         */
        AttackSlot[] columnSlots(OWEntity entity) {
            AttackSlot secondary = charged2 != null && entity.getSecondaryAttackIndex() == 1 ? charged2 : charged;
            return java.util.stream.Stream.of(combo, secondary, ultimate)
                    .filter(java.util.Objects::nonNull)
                    .toArray(AttackSlot[]::new);
        }
    }

    private static final Map<Class<? extends OWEntity>, EntityProfile> PROFILES = new HashMap<>();

    // =========================================================================
    //  DÉFINITIONS DES ENTITÉS  ──  MODIFIER / AJOUTER ICI
    // =========================================================================

    static {

        // ──────────────────────────────────────────────────────────────────────
        //  TIGRE  (entityRow = 0 dans ow_tamed_attacks.png)
        // ──────────────────────────────────────────────────────────────────────
        PROFILES.put(TigerEntity.class, new EntityProfile(

            new AttackSlot(0, 0, "LMB",
                title("ow.attacks.tiger.combo.title"),
                e -> desc("ow.attacks.tiger.combo.desc",
                    val("0.5"), val(e.getDamageToClient() / 3))
            ),

            new AttackSlot(20, 0, "RMB",
                title("ow.attacks.tiger.bond.title"),
                e -> desc("ow.attacks.tiger.bond.desc",
                    val(OWAttacksConstants.Tiger.JUMP_ATTACK_COOLDOWN_TICKS / 20))
            ),

            new AttackSlot(40, 0, "X",
                title("ow.attacks.tiger.shadow_strike.title"),
                e -> desc("ow.attacks.tiger.shadow_strike.desc",
                    val(OWAttacksConstants.Tiger.SHADOW_STRIKE_DURATION_TICKS / 20),
                    val("15"),
                    val("25"),
                    val(String.valueOf(OWAttacksConstants.Tiger.SHADOW_STRIKE_KILLS_REQUIRED)),
                    val(String.valueOf(OWAttacksConstants.Tiger.SHADOW_STRIKE_COOLDOWN_TICKS / 20)))
            ),

            new AttackSlot(-1, -1, "",
                title("ow.attacks.tiger.predator_sense.title"),
                e -> desc("ow.attacks.tiger.predator_sense.desc",
                        val("30"),
                        val("32"))
            )

        ));



        PROFILES.put(CrocodileEntity.class, new EntityProfile(

                new AttackSlot(0, 80, "LMB",
                        title("ow.attacks.crocodile.combo.title"),
                        e -> desc("ow.attacks.crocodile.combo.desc",
                                val("0.75"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(20, 80, "RMB",
                        title("ow.attacks.crocodile.mouth.title"),
                        e -> desc("ow.attacks.crocodile.mouth.desc",
                                val("3"), val(OWAttacksConstants.Crocodile.MOUTH_SLAM_COOLDOWN_TICKS / 20), val(e.getDamageToClient()))
                ),

                new AttackSlot(40, 80, "X",
                        title("ow.attacks.crocodile.primal_dive.title"),
                        // Total d'une rotation, déduit des constantes de l'entité : recopier le
                        // chiffre ici l'aurait laissé vieillir au premier rééquilibrage.
                        e -> desc("ow.attacks.crocodile.primal_dive.desc",
                                val(CrocodileEntity.PRIMAL_DIVE_GRAB_TICKS / 20),
                                val(e.getDamageToClient()
                                        * CrocodileEntity.DEATH_ROLL_BITE_RATIO
                                        * CrocodileEntity.DEATH_ROLL_BITES),
                                val(OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED),
                                val(OWAttacksConstants.Crocodile.PRIMAL_DIVE_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(-1, -1, "",
                        title("ow.attacks.crocodile.reptilian.title"),
                        e -> desc("ow.attacks.crocodile.reptilian.desc",
                                val("10"))
                )

        ));


        PROFILES.put(KodiakEntity.class, new EntityProfile(

                new AttackSlot(0, 40, "LMB",
                        title("ow.attacks.kodiak.combo.title"),
                        e -> desc("ow.attacks.kodiak.combo.desc",
                                val("0.75"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(20, 40, "RMB",
                        title("ow.attacks.kodiak.paw_slam.title"),
                        e -> desc("ow.attacks.kodiak.paw_slam.desc",
                                val("3"), val(OWAttacksConstants.Kodiak.PAW_SLAM_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(40, 40, "X",
                        title("ow.attacks.kodiak.nap.title"),
                        e -> desc("ow.attacks.kodiak.nap.desc",
                                val(OWAttacksConstants.Kodiak.NAP_DURATION_TICKS / 20),
                                val("3"), val("80"),
                                val(OWAttacksConstants.Kodiak.NAP_KILLS_REQUIRED),
                                val(OWAttacksConstants.Kodiak.NAP_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(-1, -1, "",
                        title("ow.attacks.kodiak.butcher.title"),
                        e -> desc("ow.attacks.kodiak.butcher.desc",
                                val((OWAttacksHandler.KodiakPassives.BUTCHER_INSTINCT_MULTIPLIER - 1) * 100))
                )

        ));

        // PROFILES.put(KodiakEntity.class, new EntityProfile(
        //     new AttackSlot( 0, 40, "LMB", title("ow.attacks.kodiak.combo.title"),  e -> desc("ow.attacks.kodiak.combo.desc")),
        //     new AttackSlot(20, 40, "RMB", title("ow.attacks.kodiak.charge.title"), e -> desc("ow.attacks.kodiak.charge.desc")),
        //     new AttackSlot(40, 40, "X",   title("ow.attacks.kodiak.ult.title"),    e -> desc("ow.attacks.kodiak.ult.desc")),
        //     new AttackSlot(-1, -1, "",    title("ow.attacks.kodiak.passive.title"),e -> desc("ow.attacks.kodiak.passive.desc"))
        // ));


        // ──────────────────────────────────────────────────────────────────────
        //  BOA  (entityRow = 3 → cardTexY = 120 ; cartes en colonnes 60 / 80 / 100)
        // ──────────────────────────────────────────────────────────────────────
        PROFILES.put(BoaEntity.class, new EntityProfile(

                new AttackSlot(60, 120, "LMB",
                        title("ow.attacks.boa.combo.title"),
                        e -> desc("ow.attacks.boa.combo.desc",
                                val("0.5"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(80, 120, "RMB",
                        title("ow.attacks.boa.venom.title"),
                        e -> desc("ow.attacks.boa.venom.desc",
                                val(OWAttacksConstants.Boa.VENOM_FANGS_MIN_DURATION_TICKS / 20),
                                val(OWAttacksConstants.Boa.VENOM_FANGS_MAX_DURATION_TICKS / 20),
                                val(OWAttacksConstants.Boa.VENOM_FANGS_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(100, 120, "X",
                        title("ow.attacks.boa.ultimate.title"),
                        e -> desc("ow.attacks.boa.ultimate.desc",
                                val(OWAttacksConstants.Boa.CONSTRICT_ULT_KILLS_REQUIRED),
                                val((int) OWAttacksConstants.Boa.CONSTRICT_ULT_RANGE))
                ),

                new AttackSlot(-1, -1, "",
                        title("ow.attacks.boa.thermal_vision.title"),
                        e -> desc("ow.attacks.boa.thermal_vision.desc",
                                val((int) OWAttacksConstants.Boa.THERMAL_MAX_HP))
                )

        ));

        // ──────────────────────────────────────────────────────────────────────
        //  ÉLÉPHANT  (entityRow = 4 → cardTexY = 160 ; cartes en colonnes 60 / 80 / 100)
        // ──────────────────────────────────────────────────────────────────────
        // Même rangée que l'orque, qui occupe les colonnes 0 à 2 : l'éléphant prend les suivantes.
        PROFILES.put(ElephantEntity.class, new EntityProfile(

                new AttackSlot(60, 160, "LMB",
                        title("ow.attacks.elephant.combo.title"),
                        e -> desc("ow.attacks.elephant.combo.desc",
                                val("1.2"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(80, 160, "RMB",
                        title("ow.attacks.elephant.shoulder_bash.title"),
                        e -> desc("ow.attacks.elephant.shoulder_bash.desc",
                                val((int) (e.getDamageToClient()
                                        * OWAttacksConstants.Elephant.SHOULDER_BASH_DAMAGE_RATIO)),
                                val(OWAttacksConstants.Elephant.SHOULDER_BASH_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(236, 216, "RMB",
                        title("ow.attacks.elephant.water_spray.title"),
                        e -> desc("ow.attacks.elephant.water_spray.desc",
                                val((int) OWAttacksConstants.Elephant.WATER_SPRAY_RANGE),
                                val(OWAttacksConstants.Elephant.WATER_SPRAY_LAVA_TICKS / 20))
                ),

                new AttackSlot(100, 160, "X",
                        title("ow.attacks.elephant.earthquake.title"),
                        e -> desc("ow.attacks.elephant.earthquake.desc",
                                val(OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED),
                                val((int) OWAttacksConstants.Elephant.EARTHQUAKE_RADIUS),
                                val((int) OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_DAMAGE),
                                val(OWAttacksConstants.Elephant.EARTHQUAKE_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(-1, -1, "",
                        title("ow.attacks.elephant.lumberjack.title"),
                        e -> desc("ow.attacks.elephant.lumberjack.desc", val("50"))
                )

        ));

        // ──────────────────────────────────────────────────────────────────────
        //  KANGOUROU  (entityRow = 5)  ── combo + secondaire, pas d'ultime/passif
        // ──────────────────────────────────────────────────────────────────────
        // L'orque n'avait aucune fiche : ses trois cartes s'affichaient muettes, sans nom ni
        // description, alors que toutes les autres espèces documentent les leurs.
        PROFILES.put(OrcaEntity.class, new EntityProfile(

                new AttackSlot(0, 160, "LMB",
                        title("ow.attacks.orca.combo.title"),
                        e -> desc("ow.attacks.orca.combo.desc",
                                val("1.4"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(20, 160, "RMB",
                        title("ow.attacks.orca.tidal_rush.title"),
                        e -> desc("ow.attacks.orca.tidal_rush.desc",
                                val(e.getDamageToClient()),
                                val(OWAttacksConstants.Orca.TIDAL_RUSH_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(40, 160, "X",
                        title("ow.attacks.orca.big_mouth.title"),
                        // Durées lues sur les constantes de l'entité, pas sur celle du HUD : cette
                        // dernière ne connaît que le plus long des deux transports.
                        e -> desc("ow.attacks.orca.big_mouth.desc",
                                val(OrcaEntity.MOUTH_HOLD_ALLY_TICKS / 20),
                                val(OrcaEntity.MOUTH_HOLD_ENEMY_TICKS / 20),
                                val(OWAttacksConstants.Orca.BIG_MOUTH_KILLS_REQUIRED),
                                val(OWAttacksConstants.Orca.BIG_MOUTH_COOLDOWN_TICKS / 20))
                ),

                // Passif documenté et non enregistré comme OWPassive : il n'y a rien à surligner à
                // l'écran, seulement un trait de l'espèce à faire connaître au joueur.
                new AttackSlot(-1, -1, "",
                        title("ow.attacks.orca.slipstream.title"),
                        e -> desc("ow.attacks.orca.slipstream.desc",
                                val((int) OrcaEntity.SLIPSTREAM_RADIUS),
                                val((int) (OrcaEntity.SLIPSTREAM_SPEED_BONUS * 100)),
                                val(OrcaEntity.slipstreamBreathPercent()))
                )

        ));


        PROFILES.put(KangarooEntity.class, new EntityProfile(

                new AttackSlot(0, 200, "LMB",
                        title("ow.attacks.kangaroo.combo.title"),
                        e -> desc("ow.attacks.kangaroo.combo.desc",
                                val("0.5"), val(e.getDamageToClient() / 3))
                ),

                new AttackSlot(20, 200, "RMB",
                        title("ow.attacks.kangaroo.whirlwind.title"),
                        e -> desc("ow.attacks.kangaroo.whirlwind.desc",
                                val(OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS / 20),
                                val(OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MAX),
                                val((int) OWAttacksConstants.Kangaroo.WHIRLWIND_RADIUS),
                                val(OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS / 20),
                                val(OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_TICKS / 20),
                                val(OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_THRESHOLD_TICKS / 20))
                ),

                new AttackSlot(40, 200, "X",
                        title("ow.attacks.kangaroo.telluric_stomp.title"),
                        e -> desc("ow.attacks.kangaroo.telluric_stomp.desc",
                                val(OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED),
                                val((int) OWAttacksConstants.Kangaroo.TELLURIC_STOMP_RADIUS),
                                val((int) (OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_CENTER_MULT * 100)),
                                val((int) (OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_EDGE_MULT * 100)),
                                val(OWAttacksConstants.Kangaroo.TELLURIC_STOMP_SLOWNESS_TICKS / 20),
                                val(OWAttacksConstants.Kangaroo.TELLURIC_STOMP_COOLDOWN_TICKS / 20))
                ),

                new AttackSlot(-1, -1, "",
                        title("ow.attacks.kangaroo.spring_step.title"),
                        e -> desc("ow.attacks.kangaroo.spring_step.desc")
                )

        ));

    }

    // =========================================================================
    //  HELPERS DE FORMATAGE
    // =========================================================================

    /** Valeur numérique ou mot-clé mis en avant : blanc gras. */
    public static Component val(String s) {
        return Component.literal(s).withStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF));
    }

    /** Valeur float (arrondie à 1 décimale) en blanc gras. */
    public static Component val(float value) {
        String s = (value == (int) value) ? String.valueOf((int) value)
                                          : String.format("%.1f", value);
        return val(s);
    }

    /** Description traduite avec valeurs dynamiques injectées dans les %s. Le texte statique est en gris. */
    public static Component desc(String key, Object... args) {
        return Component.translatable(key, args).withStyle(Style.EMPTY.withColor(0x999999));
    }

    /** Concatène plusieurs Components en un seul. */
    public static Component line(Component... parts) {
        MutableComponent r = Component.empty();
        for (Component c : parts) r = r.append(c);
        return r;
    }

    /** Titre de slot : blanc souligné, clé de traduction. */
    public static Component title(String key) {
        return Component.translatable(key).withStyle(Style.EMPTY.withUnderlined(true).withColor(0xFFFFFF));
    }

    // =========================================================================
    //  RENDU PRINCIPAL
    // =========================================================================

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof OWEntity entity)) return;

        EntityProfile profile = PROFILES.get(entity.getClass());
        if (profile == null) return;

        float scale = animScale();
        if (scale <= 0f) return;

        int   bgX    = (screenWidth  - BG_W) / 2;
        int   bgY    = (screenHeight - BG_H) / 2;
        float pivotX = bgX + BG_W / 2f;
        float pivotY = bgY + BG_H / 2f;

        g.pose().pushPose();
        g.pose().translate(pivotX, pivotY, 0f);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-pivotX, -pivotY, 0f);

        g.blit(BG, bgX, bgY, 0, 0, BG_W, BG_H);

        Font font      = Minecraft.getInstance().font;
        int  PASSIVE_H = 34;
        int  colW      = BG_W / 3;
        int  innerW    = colW - 14;
        int  maxDescY  = bgY + BG_H - PASSIVE_H - 8;

        AttackSlot[] slots = profile.columnSlots(entity);

        // ── 3 colonnes d'attaque ──────────────────────────────────────────────
        for (int i = 0; i < 3 && i < slots.length; i++) {
            AttackSlot slot  = slots[i];
            int colCX = bgX + i * colW + colW / 2;
            int curY  = bgY + 6;

            if (slot.hasCard()) {
                // Le Y du slot fait foi, et non la rangée de l'espèce : une carte secondaire
                // interchangeable peut vivre ailleurs dans l'atlas que sur la rangée de sa bête.
                g.blit(CARDS,
                        colCX - CARD_SIZE / 2, curY,
                        slot.cardTexX(), slot.cardTexY(),
                        CARD_SIZE, CARD_SIZE,
                        TEX_SIZE, TEX_SIZE);
            }
            curY += CARD_SIZE + 2;

            if (!slot.keyLabel().isEmpty()) {
                drawText(g, font,
                        Component.literal("[" + slot.keyLabel() + "]")
                                .withStyle(Style.EMPTY.withColor(0x666666)),
                        colCX, curY, 0.6f);
            }
            curY += 7;

            // Carte secondaire interchangeable : c'est ici qu'on explique la commande, sous la
            // touche d'usage. Le HUD, lui, ne porte qu'un pictogramme — vingt pixels de carte ne
            // suffisent pas à écrire lisiblement un nom de touche.
            if (profile.charged2() != null && i == 1) {
                drawSwitchLine(g, font, colCX, curY);
                curY += 7;
            }

            drawText(g, font, slot.title(), colCX, curY, 0.7f);
            curY += (int)(font.lineHeight * 0.7f) + 3;

            final float DS = 0.52f;
            int wrapW = (int)(innerW / DS);
            List<FormattedCharSequence> lines = font.split(slot.desc().apply(entity), wrapW);
            int lineH = (int)(font.lineHeight * DS) + 1;
            for (FormattedCharSequence line : lines) {
                if (curY > maxDescY) break;
                drawLine(g, font, line, colCX, curY, DS);
                curY += lineH;
            }
        }

        // ── Séparateur ────────────────────────────────────────────────────────
        int divY = bgY + BG_H - PASSIVE_H - 4;
        g.fill(bgX + 6, divY, bgX + BG_W - 6, divY + 1, 0x33FFFFFF);

        // ── Bandeau passif ────────────────────────────────────────────────────
        if (profile.passive() != null) {
            AttackSlot passive = profile.passive();
            int bannerCX = bgX + BG_W / 2;
            int bannerY  = divY + 5;

            Component header = Component.empty()
                    .append(Component.literal("✦ ").withStyle(Style.EMPTY.withColor(0xFFD080).withBold(true)))
                    .append(Component.translatable("ow.attacks.passive_label")
                            .withStyle(Style.EMPTY.withColor(0x555555)))
                    .append(passive.title());
            drawText(g, font, header, bannerCX, bannerY, 0.65f);

            int descY  = bannerY + (int)(font.lineHeight * 0.65f) + 3;
            final float DS2 = 0.50f;
            int wrapW2 = (int)((BG_W - 24) / DS2);
            List<FormattedCharSequence> descLines = font.split(passive.desc().apply(entity), wrapW2);
            int lineH2 = (int)(font.lineHeight * DS2) + 1;
            for (int li = 0; li < Math.min(2, descLines.size()); li++) {
                drawLine(g, font, descLines.get(li), bannerCX, descY + li * lineH2, DS2);
            }
        }

        g.pose().popPose();
    }

    // ── Utilitaires de rendu ──────────────────────────────────────────────────

    // Pictogramme de molette, pris dans l'atlas des cartes — le même que le HUD porte au coin de la
    // carte interchangeable.
    private static final int WHEEL_U = 0, WHEEL_V = 247, WHEEL_W = 10, WHEEL_H = 9;
    private static final int WHEEL_DRAW_W = 7, WHEEL_DRAW_H = 6;

    /**
     * Ligne de rappel de la commande, avec le <b>dessin</b> de la molette au milieu de la phrase.
     *
     * <p>Le libellé est donc coupé en deux morceaux traduits séparément, l'image se glissant entre
     * les deux. Écrire « molette » en toutes lettres obligeait le lecteur à faire le lien lui-même,
     * là où le pictogramme est celui qu'il a déjà sous les yeux sur la carte.</p>
     */
    private static void drawSwitchLine(GuiGraphics g, Font font, int cx, int y) {
        final float scale = 0.55f;
        final int gap = 2;

        Component before = Component.translatable("ow.attacks.switch_secondary.before",
                OWKeysBinding.OW_SWITCH_ATTACK.getTranslatedKeyMessage());
        Component after = Component.translatable("ow.attacks.switch_secondary.after");

        float beforeW = font.width(before) * scale;
        float afterW = font.width(after) * scale;
        float total = beforeW + gap + WHEEL_DRAW_W + gap + afterW;
        float x = cx - total / 2f;

        drawScaled(g, font, before, x, y, scale);

        g.pose().pushPose();
        // Remontée d'un pixel : le pictogramme est plus haut que la ligne de texte, il faut le
        // recentrer sur elle plutôt que de l'aligner par le haut.
        g.pose().translate(x + beforeW + gap, y - 1f, 0f);
        g.pose().scale((float) WHEEL_DRAW_W / WHEEL_W, (float) WHEEL_DRAW_H / WHEEL_H, 1f);
        g.blit(CARDS, 0, 0, WHEEL_U, WHEEL_V, WHEEL_W, WHEEL_H, TEX_SIZE, TEX_SIZE);
        g.pose().popPose();

        drawScaled(g, font, after, x + beforeW + gap + WHEEL_DRAW_W + gap, y, scale);
    }

    /** Texte calé à gauche, contrairement à {@link #drawText} qui centre sur son abscisse. */
    private static void drawScaled(GuiGraphics g, Font font, Component text, float x, float y, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text.copy().withStyle(Style.EMPTY.withColor(0x7FC8FF)), 0, 0, 0x7FC8FF, false);
        g.pose().popPose();
    }

    private static void drawText(GuiGraphics g, Font font, Component text, int cx, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0f);
        g.pose().scale(scale, scale, 1f);
        g.drawCenteredString(font, text, 0, 0, 0xFFFFFF);
        g.pose().popPose();
    }

    private static void drawLine(GuiGraphics g, Font font, FormattedCharSequence seq, int cx, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0f);
        g.pose().scale(scale, scale, 1f);
        int tw = font.width(seq);
        g.drawString(font, seq, -tw / 2, 0, 0xFFFFFF, false);
        g.pose().popPose();
    }
}
