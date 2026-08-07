package net.tiew.operationWild.entity.attacks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic client-side input handler for all OW attacks.
 * Works for any OWEntity + any OWChargedAttack declared in OWAttacksHandler.
 * No changes needed here when adding a new attack — just add it to OWAttacksHandler.
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OWAttackLogic {

    private static long shortChargeCooldownEndMs = -1L;
    // Shared state — read by renderers (TigerGhostRenderer, OWAttacksOverlay…)
    public static boolean isCharging = false;
    public static long chargeStartMs = -1L;
    public static Vec3 chargeDirection = Vec3.ZERO;

    // Wow effect : flash + zoom + shake lors du déclenchement d'un ultime
    public static long ultimateWowEffectStartMs = -1L;
    public static final long ULTIMATE_WOW_DURATION_MS = 700L;

    // Orca Tidal Rush — effet de rush caméra au déclenchement du bond
    public static long orcaDashEffectStartMs = -1L;
    public static final long ORCA_DASH_EFFECT_DURATION_MS = 1000L;

    private static OWChargedAttack currentAttack = null;
    // Cooldown end timestamp — clé = pack(entityId, attackId), valeur = System.currentTimeMillis() de fin
    private static final Map<Long, Long> cooldownEndMs = new HashMap<>();
    // Timestamp de déclenchement des ultimes actifs — même clé packée
    private static final Map<Long, Long> ultimateActiveMs = new HashMap<>();
    // Cooldown de combo par entité — clé = entityId
    private static final Map<Integer, Long> comboEndMsByEntity = new HashMap<>();
    // ID de l'entité dont le Primal Dive targeting est actif (pour nettoyer ultimateActiveMs à l'annulation)
    public static int crocRidingEntityId = -1;

    // Timestamps des clics pour l'animation de rebond de la carte (toutes attaques)
    public static final Map<Integer, Long> clickAnimTimeByAttackId = new HashMap<>();
    public static long comboClickAnimTimeMs = -1L; // carte 0 (combo)

    public static final Map<Integer, Boolean> clickAnimInvalidByAttackId = new HashMap<>();
    public static boolean comboClickAnimInvalid = false;

    // ── Helpers per-entity ────────────────────────────────────────────────────
    private static long packKey(int entityId, int attackId) {
        return ((long) entityId << 32) | (attackId & 0xFFFFFFFFL);
    }
    private static long getCooldownEnd(int entityId, int attackId) {
        return cooldownEndMs.getOrDefault(packKey(entityId, attackId), -1L);
    }
    private static void setCooldownEnd(int entityId, int attackId, long endMs) {
        cooldownEndMs.put(packKey(entityId, attackId), endMs);
    }
    private static long getUltimateStart(int entityId, int attackId) {
        return ultimateActiveMs.getOrDefault(packKey(entityId, attackId), -1L);
    }
    private static void setUltimateStart(int entityId, int attackId, long startMs) {
        ultimateActiveMs.put(packKey(entityId, attackId), startMs);
    }
    private static void removeUltimateStart(int entityId, int attackId) {
        ultimateActiveMs.remove(packKey(entityId, attackId));
    }

    // ── Kodiak Bear Nap (ultime) state ───────────────────────────────────────
    public static boolean isKodiakNapping    = false;
    public static int     kodiakNapEntityId  = -1;

    // ── Croc Primal Dive targeting state ─────────────────────────────────────
    public static boolean isCrocTargeting      = false;
    public static int     crocTargetEntityId   = -1;
    public static long    crocTargetingStartMs = -1L;
    /** Start timestamp of the grab phase (phase 2), used to show the 10s drain on the card. */
    public static long    crocGrabActiveStartMs = -1L;

    // ── Boa "Étreinte Fatale" targeting state ───────────────────────────────
    public static boolean isBoaTargeting      = false;
    public static int     boaTargetEntityId   = -1;
    public static long    boaTargetingStartMs = -1L;
    public static int     boaRidingEntityId   = -1;

    public static long deathRollCooldownEndMs = -1L;

    // ── Kangourou « Tornade de Poings » : maintien local du clic droit ────────
    private static boolean kangarooWhirlwindHeld = false;
    // Phase d'amorce : touche pressée mais maintien minimal pas encore atteint (anti spam-clic).
    private static boolean kangarooWhirlwindPending = false;
    private static long    kangarooWhirlwindPressMs = -1L;
    private static OWAttack kangarooWhirlwindAttack = null;

    // ── Pilon Tellurique : suivi du plongeon pour déclencher la secousse à l'impact ──
    private static boolean kangarooWasTelluricStomping = false;

    private static final long   CLICK_ANIM_DURATION_MS = 200L;
    private static final float  CLICK_ANIM_PEAK_SCALE  = 1.22f;

    // ── Public queries used by overlays ──────────────────────────────────────

    /** ID of the attack currently being charged, or -1 if none. */
    public static int getCurrentAttackId() {
        return currentAttack != null ? currentAttack.getId() : -1;
    }

    /** Max charge duration for the active attack (used by ghost renderer). */
    public static long getCurrentMaxChargeMs() {
        return currentAttack != null ? currentAttack.getMaxChargeMs() : 3000L;
    }

    /** Whether the active attack should display a ghost preview. */
    public static boolean isShowGhost() {
        return currentAttack != null && currentAttack.isShowGhost();
    }

    /**
     * Progression restante [1..0] de la phase active d'un ultime.
     * 1.0 = vient d'être déclenché, 0.0 = terminé (ou pas actif).
     * Utilisé par l'overlay pour l'animation de drain de haut en bas.
     */
    public static float getUltimateActiveProgress(int entityId, int attackId) {
        long startMs = getUltimateStart(entityId, attackId);
        if (startMs == -1L) return 0f;
        OWAttack attack = OWAttackPacket.getAttack(attackId);
        if (attack == null || attack.getUltimateDurationMs() <= 0) return 0f;
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed >= attack.getUltimateDurationMs()) {
            removeUltimateStart(entityId, attackId);
            return 0f;
        }
        return 1f - (float) elapsed / attack.getUltimateDurationMs();
    }

    /**
     * Cooldown fraction remaining for a given entity + attack.
     * 0.0 = ready to use, 1.0 = just triggered (full cooldown remaining).
     */
    public static float getCooldownProgress(int entityId, int attackId) {
        long endMs = getCooldownEnd(entityId, attackId);
        if (endMs == -1L) return 0f;
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0) return 0f;
        OWAttack attack = OWAttackPacket.getAttack(attackId);
        if (attack == null || attack.getCooldownTicks() == 0) return 0f;
        long totalMs = (long) attack.getCooldownTicks() * 50L;
        return Math.min(1f, (float) remaining / totalMs);
    }

    /**
     * Charge progress [0..1] for the currently charging attack.
     * 0 = not charging or just started, 1 = fully charged.
     */
    public static float getChargeProgress() {
        if (!isCharging || currentAttack == null) return 0f;
        long elapsed = System.currentTimeMillis() - chargeStartMs;
        return (float) Math.min((double) elapsed / currentAttack.getMaxChargeMs(), 1.0);
    }

    // ── Animation de rebond des cartes d'attaque ──────────────────────────────

    /** Enregistre un clic sur une carte d'attaque (ID ≥ 1). */
    public static void recordAttackClick(int attackId, boolean invalid) {
        clickAnimTimeByAttackId.put(attackId, System.currentTimeMillis());
        clickAnimInvalidByAttackId.put(attackId, invalid);
    }

    /** Enregistre un clic sur la carte combo (carte 0). */
    public static void recordComboClick(boolean invalid) {
        comboClickAnimTimeMs = System.currentTimeMillis();
        comboClickAnimInvalid = invalid;
    }

    public static boolean isAttackClickInvalid(int attackId) {
        return Boolean.TRUE.equals(clickAnimInvalidByAttackId.get(attackId));
    }

    public static float getClickAnimProgress(int attackId) {
        Long t = clickAnimTimeByAttackId.get(attackId);
        if (t == null) return -1f;
        return (float)(System.currentTimeMillis() - t) / CLICK_ANIM_DURATION_MS;
    }

    /**
     * Facteur de scale oscillant doucement [~0.95..~1.05] pour les cartes en état "glow".
     * S'utilise en remplacement du scale fixe 1.0 quand une carte brille en continu.
     */
    public static float getGlowPulseScale() {
        return 1f + 0.05f * (float) Math.sin(System.currentTimeMillis() / 350.0 * Math.PI * 2);
    }

    /**
     * Facteur de scale [1.0 .. PEAK .. 1.0] pour la carte d'une attaque (ID ≥ 1).
     * Retourne 1.0 si aucune animation en cours.
     */
    public static float getAttackClickScale(int attackId) {
        Long t = clickAnimTimeByAttackId.get(attackId);
        if (t == null) return 1f;
        float progress = (float)(System.currentTimeMillis() - t) / CLICK_ANIM_DURATION_MS;
        if (progress >= 1f) {
            clickAnimTimeByAttackId.remove(attackId);
            clickAnimInvalidByAttackId.remove(attackId);
            return 1f;
        }
        return 1f + (CLICK_ANIM_PEAK_SCALE - 1f) * (float) Math.sin(progress * Math.PI);
    }

    /**
     * Facteur de scale [1.0 .. PEAK .. 1.0] pour la carte combo (carte 0).
     * Retourne 1.0 si aucune animation en cours.
     */
    public static float getComboClickScale() {
        if (comboClickAnimTimeMs < 0) return 1f;
        float progress = (float)(System.currentTimeMillis() - comboClickAnimTimeMs) / CLICK_ANIM_DURATION_MS;
        if (progress >= 1f) { comboClickAnimTimeMs = -1L; return 1f; }
        return 1f + (CLICK_ANIM_PEAK_SCALE - 1f) * (float) Math.sin(progress * Math.PI);
    }

    // ── Effets de caméra pendant la charge ───────────────────────────────────

    // Réduction FOV maximale à charge complète (0.28 = -28%)
    private static final float MAX_FOV_REDUCTION  = 0.28f;
    // Roll maximal (en degrés) à charge complète
    private static final float MAX_CAMERA_ROLL    = 5.0f;
    // Amplitude du pulsing FOV quand la charge est au max
    private static final float FOV_PULSE_AMPLITUDE = 0.03f;
    // Fréquence du pulsing (ms)
    private static final float FOV_PULSE_PERIOD_MS = 400f;

    // Nouveau subscriber — détecte le dismount pendant une charge
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Grande Gueule : la carte cesse de se vider dès que la gueule est vide. Sa durée
        // théorique vaut pour le plus long des deux transports (50 s, un allié) ; sur une proie,
        // relâchée au bout de 10 s, la jauge aurait continué de descendre quarante secondes dans
        // le vide. On la coupe sur l'état réel de la bête plutôt que sur une durée figée.
        if (mc.player != null && mc.player.getRootVehicle() instanceof OrcaEntity orcaMouthCard) {
            boolean busy = orcaMouthCard.hasSwallowed()
                    || orcaMouthCard.getMouthLungeTicks() > 0
                    || orcaMouthCard.getMouthSpitTicks() > 0;
            if (!busy) removeUltimateStart(orcaMouthCard.getId(), OWAttacksHandler.BIG_MOUTH_ID);
        }

        // Pilon Tellurique : la fin du plongeon (stomping → non-stomping) déclenche la secousse d'impact.
        if (mc.player != null && mc.player.getRootVehicle() instanceof KangarooEntity kStomp) {
            boolean stomping = kStomp.isTelluricStomping();
            if (kangarooWasTelluricStomping && !stomping) {
                ultimateWowEffectStartMs = System.currentTimeMillis();
            }
            kangarooWasTelluricStomping = stomping;
        } else {
            kangarooWasTelluricStomping = false;
        }

        // Tornade de Poings : si le joueur n'est plus sur un kangourou, on lâche le maintien/amorce
        // (le serveur arrête déjà la rotation au démontage).
        if ((kangarooWhirlwindHeld || kangarooWhirlwindPending) && mc.player != null
                && !(mc.player.getRootVehicle() instanceof KangarooEntity)) {
            kangarooWhirlwindHeld = false;
            kangarooWhirlwindPending = false;
            kangarooWhirlwindAttack = null;
        }

        // Amorce de la tornade : une fois la touche tenue assez longtemps, on démarre réellement.
        if (kangarooWhirlwindPending && kangarooWhirlwindAttack != null && mc.player != null) {
            if (mc.player.getRootVehicle() instanceof KangarooEntity kangaroo) {
                boolean stillValid = kangaroo.getWhirlwindCooldownTicks() <= 0 && !kangaroo.isCombo();
                if (!stillValid) {
                    kangarooWhirlwindPending = false;
                    kangarooWhirlwindAttack = null;
                } else if (System.currentTimeMillis() - kangarooWhirlwindPressMs
                        >= OWAttacksConstants.Kangaroo.WHIRLWIND_HOLD_TO_START_MS) {
                    kangarooWhirlwindPending = false;
                    kangarooWhirlwindHeld = true;
                    PacketDistributor.sendToServer(
                            new OWAttackPacket(kangarooWhirlwindAttack.getId(), OWAttackPacket.ACTION_CHARGE_START, 0f));
                    recordAttackClick(kangarooWhirlwindAttack.getId(), false);
                    kangarooWhirlwindAttack = null;
                }
            }
        }

        if (mc.player == null || !isCharging || currentAttack == null) return;

        if (!(mc.player.getRootVehicle() instanceof OWEntity)) {
            isCharging = false;
            shortChargeCooldownEndMs = System.currentTimeMillis() + 300L;
            PacketDistributor.sendToServer(
                    new OWAttackPacket(currentAttack.getId(), OWAttackPacket.ACTION_CHARGE_CANCEL, 0f));
            currentAttack = null;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        applyTelluricStompFov(event);

        // Orca Tidal Rush — FOV increase (sensation de vitesse)
        if (orcaDashEffectStartMs >= 0) {
            long elapsed = System.currentTimeMillis() - orcaDashEffectStartMs;
            if (elapsed < ORCA_DASH_EFFECT_DURATION_MS) {
                float t = (float) elapsed / ORCA_DASH_EFFECT_DURATION_MS;
                // Courbe en cloche : montée rapide, descente lente
                float bell = (float) Math.sin(t * Math.PI);
                event.setNewFovModifier(event.getNewFovModifier() * (1.0f + 0.18f * bell));
            } else {
                orcaDashEffectStartMs = -1L;
            }
        }

        // Wow effect : zoom rapide à l'activation d'un ultime
        if (ultimateWowEffectStartMs >= 0) {
            long elapsed = System.currentTimeMillis() - ultimateWowEffectStartMs;
            if (elapsed < ULTIMATE_WOW_DURATION_MS) {
                float t = (float) elapsed / ULTIMATE_WOW_DURATION_MS;
                // Zoom-in sur les 20 % puis retour normal sur les 30 % suivants
                float zoomPhase = t < 0.20f
                        ? (t / 0.20f)
                        : Math.max(0f, 1f - (t - 0.20f) / 0.30f);
                float reduction = 0.40f * zoomPhase;
                event.setNewFovModifier(event.getNewFovModifier() * (1.0f - reduction));
            } else {
                ultimateWowEffectStartMs = -1L;
            }
        }

        if (isCrocTargeting) {
            long elapsed = System.currentTimeMillis() - crocTargetingStartMs;
            float t = (float) Math.min(elapsed / 600.0, 1.0);
            float pulse = 0.04f * (float) Math.sin(System.currentTimeMillis() / 350.0 * Math.PI * 2);
            event.setNewFovModifier(event.getNewFovModifier() * (1.0f + 0.05f * t + pulse));
        }

        if (!isCharging || currentAttack == null || !currentAttack.hasCameraEffect()) return;
        float charge = getChargeProgress();
        if (charge <= 0f) return;

        float reduction = MAX_FOV_REDUCTION * charge;

        // Pulsing léger quand la charge est au maximum (>= 1.0)
        if (charge >= 1.0f) {
            float pulse = FOV_PULSE_AMPLITUDE
                    * (float) Math.sin((System.currentTimeMillis() % (long) FOV_PULSE_PERIOD_MS)
                    / FOV_PULSE_PERIOD_MS * 2.0 * Math.PI);
            reduction += pulse;
        }

        event.setNewFovModifier(event.getNewFovModifier() * (1.0f - reduction));
    }

    /**
     * Secousse de caméra de la Roulade de la Mort, pour le cavalier <b>comme</b> pour la proie.
     *
     * <p>Elle est pilotée par l'état synchronisé du crocodile et non par un horodatage local : tout
     * client concerné la ressent, y compris celui de la victime, et elle reste calée sur la rotation
     * réelle même si le paquet de déclenchement a mis du temps à revenir. Le siège du cavalier, lui,
     * reste d'aplomb — sans quoi trois tours complets renverraient la caméra dans tous les sens.</p>
     */
    private static void applyDeathRollShake(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.player.getRootVehicle() instanceof CrocodileEntity croc)) return;
        if (!croc.isDeathRolling()) return;

        int progress = croc.getDeathRollProgress();
        if (progress <= CrocodileEntity.DEATH_ROLL_WINDUP_TICKS) return;

        float spin = (float) (progress - CrocodileEntity.DEATH_ROLL_WINDUP_TICKS)
                / CrocodileEntity.DEATH_ROLL_SPIN_TICKS;
        // Cloche : montée franche au départ du tonneau, extinction douce à la sortie.
        float envelope = (float) Math.sin(Mth.clamp(spin, 0f, 1f) * Math.PI);
        float amplitude = envelope * (croc.getGrabbedTarget() == mc.player ? 9.0f : 5.0f);

        long t = System.currentTimeMillis();
        event.setRoll(event.getRoll()   + amplitude * 1.6f * (float) Math.sin(t / 45.0));
        event.setPitch(event.getPitch() + amplitude * 0.5f * (float) Math.sin(t / 33.0));
        event.setYaw(event.getYaw()     + amplitude * 0.5f * (float) Math.cos(t / 39.0));
    }

    /**
     * Kangourou monté : le Pilon Tellurique à la première personne.
     *
     * <p>Comme la Roulade de la Mort, l'effet est piloté par l'état synchronisé de la bête et non par
     * un horodatage local — il reste donc calé sur le geste réel, quelle que soit la durée de la
     * chute, et vaut pour tout client concerné. On ne touche qu'aux angles de <b>vue</b> : le regard
     * du joueur, lui, oriente le plongeon et doit rester intact.</p>
     */
    private static KangarooEntity riddenTelluricKangaroo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        if (!(mc.player.getRootVehicle() instanceof KangarooEntity kangaroo)) return null;
        if (!kangaroo.isTelluricStomping() && kangaroo.telluricStompOutroTicks <= 0) return null;
        return kangaroo;
    }

    private static void applyTelluricStompShake(ViewportEvent.ComputeCameraAngles event) {
        KangarooEntity kangaroo = riddenTelluricKangaroo();
        if (kangaroo == null) return;

        long t = System.currentTimeMillis();
        int phase = kangaroo.getTelluricStompPhase();

        switch (phase) {
            case KangarooEntity.STOMP_PHASE_WINDUP -> {
                // Grondement sourd qui monte pendant que la bête se ramasse.
                float build = Mth.clamp((float) kangaroo.telluricStompPhaseTicks
                        / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_WINDUP_TICKS, 0f, 1f);
                float amplitude = build * build * 1.6f;
                event.setRoll(event.getRoll() + amplitude * (float) Math.sin(t / 26.0));
                event.setPitch(event.getPitch() + amplitude * 0.6f * (float) Math.cos(t / 31.0));
            }
            case KangarooEntity.STOMP_PHASE_LEAP -> {
                float fade = 1f - Mth.clamp((float) kangaroo.telluricStompPhaseTicks
                        / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS, 0f, 1f);
                float amplitude = fade * fade * 5.0f;
                event.setPitch(event.getPitch() - amplitude * 0.8f);
                event.setRoll(event.getRoll() + amplitude * (float) Math.sin(t / 24.0));
            }
            case KangarooEntity.STOMP_PHASE_HOVER -> {
                // Le calme avant la chute : un simple roulis, la caméra reprend son souffle.
                event.setRoll(event.getRoll() + 0.9f * (float) Math.sin(t / 220.0));
            }
            case KangarooEntity.STOMP_PHASE_DIVE -> {
                // Vibration qui s'intensifie à mesure que la semelle prend de la vitesse.
                float ramp = Mth.clamp(kangaroo.telluricStompPhaseTicks / 10f, 0f, 1f);
                float amplitude = 0.7f + 2.3f * ramp;
                event.setPitch(event.getPitch() + amplitude * 0.7f * (float) Math.sin(t / 18.0));
                event.setRoll(event.getRoll() + amplitude * (float) Math.sin(t / 21.0));
            }
            default -> {
                // Impact : secousse violente qui s'éteint sur la réception.
                float remaining = (float) kangaroo.telluricStompOutroTicks
                        / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_OUTRO_TICKS;
                float amplitude = remaining * remaining * 17f;
                event.setYaw(event.getYaw() + amplitude * (float) Math.sin(t / 27.0));
                event.setPitch(event.getPitch() + amplitude * 0.9f * (float) Math.cos(t / 34.0));
                event.setRoll(event.getRoll() + amplitude * 0.7f * (float) Math.sin(t / 41.0));
            }
        }
    }

    private static void applyTelluricStompFov(ComputeFovModifierEvent event) {
        KangarooEntity kangaroo = riddenTelluricKangaroo();
        if (kangaroo == null) return;

        int phase = kangaroo.getTelluricStompPhase();
        if (phase == KangarooEntity.STOMP_PHASE_WINDUP) {
            // Resserrement : la vue se ferme sur la bête qui se comprime. Volontairement discret —
            // le zoom d'activation d'ultime (ultimateWowEffectStartMs) tape déjà au même instant.
            float build = Mth.clamp((float) kangaroo.telluricStompPhaseTicks
                    / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_WINDUP_TICKS, 0f, 1f);
            event.setNewFovModifier(event.getNewFovModifier() * (1.0f - 0.10f * build));
        } else if (phase == KangarooEntity.STOMP_PHASE_LEAP) {
            float fade = 1f - Mth.clamp((float) kangaroo.telluricStompPhaseTicks
                    / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS, 0f, 1f);
            event.setNewFovModifier(event.getNewFovModifier() * (1.0f + 0.26f * fade));
        } else if (phase == KangarooEntity.STOMP_PHASE_DIVE) {
            float ramp = Mth.clamp(kangaroo.telluricStompPhaseTicks / 12f, 0f, 1f);
            event.setNewFovModifier(event.getNewFovModifier() * (1.0f + 0.30f * ramp));
        } else if (kangaroo.telluricStompOutroTicks > 0) {
            float remaining = (float) kangaroo.telluricStompOutroTicks
                    / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_OUTRO_TICKS;
            event.setNewFovModifier(event.getNewFovModifier() * (1.0f - 0.22f * remaining * remaining));
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        applyDeathRollShake(event);
        applyTelluricStompShake(event);

        // Orca Tidal Rush — secousse frontale + légère bascule au déclenchement
        if (orcaDashEffectStartMs >= 0) {
            long elapsed = System.currentTimeMillis() - orcaDashEffectStartMs;
            if (elapsed < ORCA_DASH_EFFECT_DURATION_MS) {
                float t = (float) elapsed / ORCA_DASH_EFFECT_DURATION_MS;
                // Tremblement fort au départ qui décroît rapidement
                float shakeFade = (1f - t) * (1f - t);
                long tMs = System.currentTimeMillis();
                float shake = shakeFade * 3.5f * (float) Math.sin(tMs / 22.0);
                // Légère inclinaison avant (sensation de plongeon)
                float bell = (float) Math.sin(t * Math.PI);
                float forwardTilt = bell * 2.5f;
                event.setPitch(event.getPitch() + forwardTilt + shake * 0.8f);
                event.setRoll(event.getRoll()   + shake);
            }
        }

        // Wow effect : tremblement violent décroissant à l'activation d'un ultime
        if (ultimateWowEffectStartMs >= 0) {
            long elapsed = System.currentTimeMillis() - ultimateWowEffectStartMs;
            if (elapsed < 800L) {
                float t = (float) elapsed / 800L;
                float amplitude = (1f - t) * (1f - t) * 18f;
                long tMs = System.currentTimeMillis();
                event.setYaw(event.getYaw()     + amplitude * (float) Math.sin(tMs / 28.0));
                event.setPitch(event.getPitch() + amplitude * (float) Math.cos(tMs / 38.0));
            }
        }

        if (isCrocTargeting) {
            long t = System.currentTimeMillis();
            float sway = 0.6f * (float) Math.sin(t / 650.0);
            event.setRoll(event.getRoll() + sway);
        }

        if (!isCharging || currentAttack == null || !currentAttack.hasCameraEffect()) return;
        float charge = getChargeProgress();
        if (charge <= 0f) return;

        // Roll progressif (bascule légère sur la droite)
        float roll = MAX_CAMERA_ROLL * charge;

        // Tremblement léger à charge maximale
        if (charge >= 1.0f) {
            long t = System.currentTimeMillis();
            roll += 0.8f * (float) Math.sin(t / 80.0);
        }

        event.setRoll(event.getRoll() + roll);
    }

    // ── Input handling ────────────────────────────────────────────────────────

    private static boolean playerHoldsUsableItem(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return false;
        // Items that require holding right-click (food, bow, crossbow, potion, shield, trident…)
        if (stack.getUseAnimation() != UseAnim.NONE) return true;
        // Items with an immediate right-click action (throwables, spawn eggs…)
        if (stack.getItem() instanceof SpawnEggItem) return true;
        if (stack.is(net.minecraft.world.item.Items.EGG)) return true;
        if (stack.is(net.minecraft.world.item.Items.SNOWBALL)) return true;
        if (stack.is(net.minecraft.world.item.Items.ENDER_PEARL)) return true;
        if (stack.is(net.minecraft.world.item.Items.SPLASH_POTION)) return true;
        if (stack.is(net.minecraft.world.item.Items.LINGERING_POTION)) return true;
        if (stack.is(net.minecraft.world.item.Items.FIRE_CHARGE)) return true;
        return false;
    }

    /**
     * Remaining progress [1..0] of the Primal Dive grab phase.
     * 1.0 = grab just started, 0.0 = finished / not active.
     * Drives the 10-s drain on the Primal Dive card after the second click.
     */
    public static float getCrocGrabActiveProgress() {
        if (crocGrabActiveStartMs < 0) return 0f;
        long elapsed = System.currentTimeMillis() - crocGrabActiveStartMs;
        if (elapsed >= OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGETING_MS) {
            crocGrabActiveStartMs = -1L;
            return 0f;
        }
        return 1f - (float) elapsed / OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGETING_MS;
    }

    public static void cancelCrocTargeting(int attackId) {
        isCrocTargeting      = false;
        crocTargetEntityId   = -1;
        crocTargetingStartMs = -1L;
        crocGrabActiveStartMs = -1L;
        if (crocRidingEntityId != -1) removeUltimateStart(crocRidingEntityId, attackId);
        crocRidingEntityId   = -1;
    }

    // ── Kodiak Bear Nap ───────────────────────────────────────────────────────

    /**
     * Nettoyage côté client quand la sieste expire naturellement (25 s).
     * Appelé depuis l'overlay dès que getUltimateActiveProgress retourne 0 alors que isKodiakNapping est vrai.
     */
    public static void onKodiakNapExpired(int entityId, OWAttack attack) {
        isKodiakNapping   = false;
        kodiakNapEntityId = -1;
        setCooldownEnd(entityId, attack.getId(),
                System.currentTimeMillis() + (long) attack.getCooldownTicks() * 50L);
    }

    private static void handleKodiakNapKey(OWAttack attack, OWEntity owEntity) {
        int eid = owEntity.getId();

        if (isKodiakNapping && kodiakNapEntityId == eid) {
            // La sieste a peut-être déjà expiré naturellement côté client
            if (getUltimateActiveProgress(eid, attack.getId()) == 0f) {
                onKodiakNapExpired(eid, attack);
                recordAttackClick(attack.getId(), false);
                return;
            }
            // Annulation manuelle par le joueur
            isKodiakNapping   = false;
            kodiakNapEntityId = -1;
            removeUltimateStart(eid, attack.getId());
            setCooldownEnd(eid, attack.getId(),
                    System.currentTimeMillis() + (long) attack.getCooldownTicks() * 50L);
            PacketDistributor.sendToServer(
                    new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
            recordAttackClick(attack.getId(), false);
            return;
        }

        // Vérifications habituelles avant activation
        long endMs = getCooldownEnd(eid, attack.getId());
        if (endMs != -1L && System.currentTimeMillis() < endMs) {
            recordAttackClick(attack.getId(), true);
            return;
        }
        if (!attack.isUnlocked(owEntity)) {
            recordAttackClick(attack.getId(), true);
            return;
        }
        if (owEntity.getVitalEnergy() > owEntity.getMaxVitalEnergy() - attack.getEnergyRequired()) {
            owEntity.canShowVitalEnergyLack = true;
            recordAttackClick(attack.getId(), true);
            return;
        }

        // Activation
        isKodiakNapping   = true;
        kodiakNapEntityId = eid;
        long now = System.currentTimeMillis();
        setUltimateStart(eid, attack.getId(), now);
        ultimateWowEffectStartMs = now;

        PacketDistributor.sendToServer(
                new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
        recordAttackClick(attack.getId(), false);
    }

    public static void cancelBoaTargeting(int attackId) {
        isBoaTargeting      = false;
        boaTargetEntityId   = -1;
        boaTargetingStartMs = -1L;
        boaRidingEntityId   = -1;
    }

    /** Ultime du Boa : 1er X arme le ciblage (point vert sur les grabables ≤3 blocs), 2e X valide la cible visée. */
    private static void handleBoaConstrictUltimateKey(OWAttack attack, OWEntity owEntity) {
        int eid = owEntity.getId();
        if (!isBoaTargeting) {
            long endMs = getCooldownEnd(eid, attack.getId());
            if (endMs != -1L && System.currentTimeMillis() < endMs) { recordAttackClick(attack.getId(), true); return; }
            if (!attack.isUnlocked(owEntity)) { recordAttackClick(attack.getId(), true); return; }
            if (owEntity.getVitalEnergy() > owEntity.getMaxVitalEnergy() - attack.getEnergyRequired()) {
                owEntity.canShowVitalEnergyLack = true;
                recordAttackClick(attack.getId(), true);
                return;
            }

            isBoaTargeting      = true;
            boaTargetingStartMs = System.currentTimeMillis();
            boaTargetEntityId   = -1;
            boaRidingEntityId   = eid;

            setUltimateStart(eid, attack.getId(), System.currentTimeMillis());
            PacketDistributor.sendToServer(new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
            recordAttackClick(attack.getId(), false);

        } else {
            long elapsed = System.currentTimeMillis() - boaTargetingStartMs;
            if (elapsed >= OWAttacksConstants.Boa.CONSTRICT_ULT_TARGETING_MS) {
                cancelBoaTargeting(attack.getId());
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (elapsed < 500L) { recordAttackClick(attack.getId(), true); return; }   // anti double-déclenchement
            if (boaTargetEntityId == -1) { recordAttackClick(attack.getId(), true); return; }

            ultimateWowEffectStartMs = System.currentTimeMillis();
            removeUltimateStart(eid, attack.getId());
            setCooldownEnd(eid, attack.getId(), System.currentTimeMillis() + (long) attack.getCooldownTicks() * 50L);

            PacketDistributor.sendToServer(new OWAttackPacket(
                    attack.getId(), OWAttackPacket.ACTION_EXECUTE_WITH_TARGET,
                    Float.intBitsToFloat(boaTargetEntityId)));

            cancelBoaTargeting(attack.getId());
            recordAttackClick(attack.getId(), false);
        }
    }

    private static void handleCrocPrimalDiveKey(OWAttack attack, OWEntity owEntity) {
        int eid = owEntity.getId();
        if (!isCrocTargeting) {
            long endMs = getCooldownEnd(eid, attack.getId());
            if (endMs != -1L && System.currentTimeMillis() < endMs) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (!attack.isUnlocked(owEntity)) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (owEntity.getVitalEnergy() > owEntity.getMaxVitalEnergy() - attack.getEnergyRequired()) {
                owEntity.canShowVitalEnergyLack = true;
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (!owEntity.isInWater()) {
                recordAttackClick(attack.getId(), true);
                return;
            }

            isCrocTargeting      = true;
            crocTargetingStartMs = System.currentTimeMillis();
            crocTargetEntityId   = -1;
            crocRidingEntityId   = eid;

            setUltimateStart(eid, attack.getId(), System.currentTimeMillis());

            PacketDistributor.sendToServer(
                    new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
            recordAttackClick(attack.getId(), false);

        } else {
            long elapsed = System.currentTimeMillis() - crocTargetingStartMs;

            if (elapsed >= OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGETING_MS) {
                cancelCrocTargeting(attack.getId());
                recordAttackClick(attack.getId(), true);
                return;
            }
            // Simple garde anti-rebond : il ne s'agit que d'éviter qu'un seul appui physique ne
            // vaille à la fois l'armement et le déclenchement. La seconde pleine d'avant obligeait
            // à marteler la touche — tous les appuis de la première seconde étaient rejetés en
            // silence, et le verrouillage semblait ne marcher qu'un coup sur trois.
            if (elapsed < 220L) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (crocTargetEntityId == -1) {
                recordAttackClick(attack.getId(), true);
                return;
            }

            ultimateWowEffectStartMs = System.currentTimeMillis();
            crocGrabActiveStartMs    = System.currentTimeMillis();

            removeUltimateStart(eid, attack.getId());
            setCooldownEnd(eid, attack.getId(),
                    System.currentTimeMillis() + (long) attack.getCooldownTicks() * 50L);

            PacketDistributor.sendToServer(new OWAttackPacket(
                    attack.getId(),
                    OWAttackPacket.ACTION_EXECUTE_WITH_TARGET,
                    Float.intBitsToFloat(crocTargetEntityId)));

            isCrocTargeting      = false;
            crocTargetEntityId   = -1;
            crocTargetingStartMs = -1L;
            crocRidingEntityId   = -1;
            recordAttackClick(attack.getId(), false);
        }
    }

    /**
     * Toggle client des Crochets Venimeux du Boa.
     * Un clic l'arme, un autre la désarme. Bloqué pendant le cooldown ou si le joueur tient un objet utilisable.
     * L'état réel (armé / cooldown) est synchronisé depuis l'entité ; on n'envoie qu'un packet de toggle.
     */
    private static void handleBoaVenomToggle(OWAttack attack, BoaEntity boa, Player player) {
        if (boa.isVenomArmed()) {
            // Désarmement : toujours autorisé
            PacketDistributor.sendToServer(
                    new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
            recordAttackClick(attack.getId(), false);
            return;
        }

        // Armement : refusé pendant le cooldown, si pas assez d'énergie, ou si un objet utilisable est tenu
        if (boa.getVenomCooldownTicks() > 0) {
            recordAttackClick(attack.getId(), true);
            return;
        }
        if (playerHoldsUsableItem(player)) {
            recordAttackClick(attack.getId(), true);
            return;
        }
        if (boa.getVitalEnergy() > boa.getMaxVitalEnergy() - attack.getEnergyRequired()) {
            boa.canShowVitalEnergyLack = true;
            recordAttackClick(attack.getId(), true);
            return;
        }

        PacketDistributor.sendToServer(
                new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
        recordAttackClick(attack.getId(), false);
    }

    /**
     * Kangourou — Tornade de Poings : attaque secondaire MAINTENUE sur le clic droit.
     * Press → démarre la rotation ; release → l'arrête. Toute la machine d'état (rotation /
     * cooldown) vit sur l'entité ; on n'envoie que des packets start/stop.
     */
    private static void handleKangarooWhirlwind(OWAttack attack, KangarooEntity kangaroo, Player player, int action) {
        if (action == GLFW.GLFW_PRESS) {
            if (kangarooWhirlwindHeld || kangarooWhirlwindPending) return;
            if (kangaroo.getWhirlwindCooldownTicks() > 0) { recordAttackClick(attack.getId(), true); return; }
            if (kangaroo.isCombo()) { recordAttackClick(attack.getId(), true); return; }
            if (playerHoldsUsableItem(player)) { recordAttackClick(attack.getId(), true); return; }
            if (kangaroo.getVitalEnergy() > kangaroo.getMaxVitalEnergy() - attack.getEnergyRequired()) {
                kangaroo.canShowVitalEnergyLack = true;
                recordAttackClick(attack.getId(), true);
                return;
            }

            // On n'amorce que le maintien : la tornade ne démarrera (côté serveur) qu'une fois
            // la touche tenue WHIRLWIND_HOLD_TO_START_MS (voir onClientTick). Empêche le spam-clic.
            kangarooWhirlwindPending = true;
            kangarooWhirlwindPressMs = System.currentTimeMillis();
            kangarooWhirlwindAttack = attack;

        } else if (action == GLFW.GLFW_RELEASE) {
            if (kangarooWhirlwindHeld) {
                kangarooWhirlwindHeld = false;
                PacketDistributor.sendToServer(
                        new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_CHARGE_CANCEL, 0f));
            } else if (kangarooWhirlwindPending) {
                // Relâché avant le seuil de maintien : rien ne se passe (feedback « clic invalide »).
                kangarooWhirlwindPending = false;
                kangarooWhirlwindAttack = null;
                recordAttackClick(attack.getId(), true);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.player.getRootVehicle() instanceof OWEntity owEntity)) return;
        if (isLocalPlayerGrabbed(owEntity, mc.player)) return;
        if (owEntity.getPassengers().indexOf(mc.player) != 0) return;
        if (!owEntity.canPilotAttacks(mc.player)) return;

        OWAttack attack = OWAttacksHandler.findInstantAttack(owEntity.getClass(), event.getKey());
        if (attack == null) return;

        // Kodiak NAP : traité en priorité (peut annuler même si d'autres blocages sont actifs)
        if (attack.getId() == OWAttacksHandler.NAP_ULTIMATE_ID) {
            handleKodiakNapKey(attack, owEntity);
            return;
        }

        // Orque — Grande Gueule : gueule pleine, le second appui recrache. Il passe AVANT le
        // délai de récupération, sinon celui-ci, armé dès la happe, condamnerait la proie à rester
        // dedans jusqu'au bout du transport sans qu'on puisse la libérer.
        if (attack.getId() == OWAttacksHandler.BIG_MOUTH_ID
                && owEntity instanceof OrcaEntity orcaMouthFull && orcaMouthFull.hasSwallowed()) {
            PacketDistributor.sendToServer(
                    new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));
            removeUltimateStart(owEntity.getId(), attack.getId());
            recordAttackClick(attack.getId(), false);
            return;
        }

        boolean isGrabBlocked = owEntity instanceof CrocodileEntity && owEntity.isGrabbing();
        // Bloquer toute autre attaque instantanée pendant la sieste ultime du Kodiak
        if (isKodiakNapping) {
            recordAttackClick(attack.getId(), true);
            return;
        }
        if (isGrabBlocked
                || (isCrocTargeting && attack.getId() != OWAttacksHandler.PRIMAL_DIVE_ID)
                || (isBoaTargeting && attack.getId() != OWAttacksHandler.CONSTRICT_ULTIMATE_ID)) {
            recordAttackClick(attack.getId(), true);
            return;
        }

        // Le SECOND appui d'un ultime déjà armé valide une cible, il ne lance pas d'attaque : un
        // combo encore en cours n'a pas à le refuser. Le combo dure près de deux secondes, et le
        // joueur qui frappait juste avant d'armer se voyait rejeter son verrouillage sans le
        // moindre signe — d'où l'impression qu'il fallait insister plusieurs fois.
        boolean isCrocLockIn = isCrocTargeting && attack.getId() == OWAttacksHandler.PRIMAL_DIVE_ID;

        if ((isCharging || owEntity.isCombo()) && !isCrocLockIn) {
            recordAttackClick(attack.getId(), true);
            return;
        }

        if (attack.getId() == OWAttacksHandler.PRIMAL_DIVE_ID) {
            handleCrocPrimalDiveKey(attack, owEntity);
            return;
        }

        if (attack.getId() == OWAttacksHandler.CONSTRICT_ULTIMATE_ID) {
            handleBoaConstrictUltimateKey(attack, owEntity);
            return;
        }

        long endMs = getCooldownEnd(owEntity.getId(), attack.getId());
        if (endMs != -1L && System.currentTimeMillis() < endMs) {
            recordAttackClick(attack.getId(), true);
            return;
        }

        if (!attack.isUnlocked(owEntity)) {
            recordAttackClick(attack.getId(), true);
            return;
        }

        if (owEntity.getVitalEnergy() > owEntity.getMaxVitalEnergy() - attack.getEnergyRequired()) {
            owEntity.canShowVitalEnergyLack = true;
            recordAttackClick(attack.getId(), true);
            return;
        }

        // Rien devant le museau : la Grande Gueule claquerait dans le vide et l'ultime serait
        // perdu pour rien. Refus net, avec le retour « clic invalide » habituel.
        if (attack.getId() == OWAttacksHandler.BIG_MOUTH_ID
                && owEntity instanceof OrcaEntity orcaMouthAim && !orcaMouthAim.hasMouthTarget()) {
            recordAttackClick(attack.getId(), true);
            return;
        }

        PacketDistributor.sendToServer(
                new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_EXECUTE, 0f));

        long now = System.currentTimeMillis();
        setCooldownEnd(owEntity.getId(), attack.getId(), now + (long) attack.getCooldownTicks() * 50L);
        recordAttackClick(attack.getId(), false);

        if (attack.isUltimate()) {
            ultimateWowEffectStartMs = now;
            if (attack.getUltimateDurationMs() > 0) {
                setUltimateStart(owEntity.getId(), attack.getId(), now);
            }
            // Feedback client immédiat pour les ultimes : son sans attendre le serveur
            if (attack.getId() == OWAttacksHandler.TigerAttacks.SHADOW_STRIKE.getId()
                    && owEntity instanceof TigerEntity tiger) {
                tiger.playShadowStrikeSound();
            }
        }
    }

    /**
     * Le joueur local est-il la <b>proie</b> de cette bête, et non son pilote ?
     *
     * <p>Une victime tenue en gueule est bel et bien un passager, et elle en est même le premier
     * quand la bête est sauvage : rien ici ne la distinguait d'un cavalier. Ses clics étaient donc
     * traités comme des commandes d'attaque puis annulés, et le gestionnaire de débattement
     * ({@code ClientEvents.onDebate}), qui ne reçoit pas les événements annulés, n'en voyait jamais
     * un seul. Se débattre était purement et simplement impossible.</p>
     */
    private static boolean isLocalPlayerGrabbed(OWEntity owEntity, Player player) {
        if (owEntity instanceof CrocodileEntity croc) return croc.isGrabbing() && croc.getGrabbedTarget() == player;
        if (owEntity instanceof TigerEntity tiger) return tiger.isGrabbing() && tiger.getGrabbedTarget() == player;
        return false;
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.player.getRootVehicle() instanceof OWEntity owEntity)) return;
        // Avant toute chose : la proie ne pilote pas. On laisse passer ses clics.
        if (isLocalPlayerGrabbed(owEntity, mc.player)) return;
        if (owEntity.getPassengers().indexOf(mc.player) != 0) return;
        if (!owEntity.canPilotAttacks(mc.player)) return;

        boolean isCrocGrabbing = owEntity instanceof CrocodileEntity && owEntity.isGrabbing();
        boolean isKodiakUltNapping = isKodiakNapping && owEntity instanceof KodiakEntity;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            if (owEntity.isPlayerControlledDeathRoll()) {
                // Test tolérant : isInWater() clignote à la surface, et le clic était alors avalé
                // sans même partir au serveur. C'est lui qui arbitre pour de bon (startDeathRoll).
                if (owEntity instanceof CrocodileEntity crocRoll) {
                    if (!crocRoll.isInWaterForDeathRoll() || crocRoll.isDeathRolling()) {
                        event.setCanceled(true);
                        return;
                    }
                }
                // Simple anti-flood, PAS une reproduction du délai serveur.
                //
                // Le verrou de 5,5 s d'avant partait à chaque envoi, y compris quand le serveur
                // refusait la demande — une roulade encore en récupération, ou une prise vieille de
                // moins d'une demi-seconde. Le joueur se retrouvait alors muselé par son propre
                // client pendant cinq secondes SANS que rien ne se soit passé : il martelait le clic
                // dans le vide, et seul le rare clic tombant après l'expiration du verrou aboutissait.
                // Le serveur tient déjà son propre délai et met la demande en attente ; ici on ne
                // fait plus que borner le débit de paquets.
                if (System.currentTimeMillis() >= deathRollCooldownEndMs) {
                    PacketDistributor.sendToServer(
                            new OWAttackPacket(-1, OWAttackPacket.ACTION_TRIGGER_DEATH_ROLL, 0f));
                    deathRollCooldownEndMs = System.currentTimeMillis() + 200L;
                }
                event.setCanceled(true);
                return;
            }
            // Orque : gueule pleine, le combo est refusé. Le serveur le refuse déjà (setCombo),
            // mais sans ce retour le clic passerait pour valide et lancerait le délai d'affichage.
            boolean isOrcaMouthFull = owEntity instanceof OrcaEntity orcaBite && orcaBite.hasSwallowed();

            if (isCrocGrabbing || isCrocTargeting || isKodiakUltNapping || isBoaTargeting || isOrcaMouthFull
                    || !owEntity.canStartCombo()) {
                recordComboClick(true);
                event.setCanceled(true);
                return;
            }
            // Pendant la Tornade de Poings, le combo clic-gauche est bloqué.
            if (owEntity instanceof KangarooEntity kSpin && kSpin.isSpinning()) {
                recordComboClick(true);
                event.setCanceled(true);
                return;
            }
            if (isCharging) return;
            long comboEnd = comboEndMsByEntity.getOrDefault(owEntity.getId(), -1L);
            boolean onCooldown = comboEnd > System.currentTimeMillis();
            recordComboClick(onCooldown);
            if (!onCooldown) {
                int comboMaxTicks = OWAttacksHandler.getComboMaxTimer(owEntity.getClass());
                comboEndMsByEntity.put(owEntity.getId(), System.currentTimeMillis() + comboMaxTicks * 50L);
            }
        }

        if (isCrocGrabbing || isCrocTargeting || isKodiakUltNapping || isBoaTargeting) {
            event.setCanceled(true);
            return;
        }

        // Boa — Crochets Venimeux : attaque secondaire instantanée en toggle sur le clic droit.
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && event.getAction() == GLFW.GLFW_PRESS
                && owEntity instanceof BoaEntity boa) {
            OWAttack venom = OWAttacksHandler.findInstantAttack(owEntity.getClass(), event.getButton());
            if (venom != null) {
                handleBoaVenomToggle(venom, boa, mc.player);
                return;
            }
        }

        // Kangourou — Tornade de Poings : attaque secondaire MAINTENUE sur le clic droit (press + release).
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && owEntity instanceof KangarooEntity kangaroo) {
            OWAttack whirlwind = OWAttacksHandler.findInstantAttack(owEntity.getClass(), event.getButton());
            if (whirlwind != null) {
                handleKangarooWhirlwind(whirlwind, kangaroo, mc.player, event.getAction());
                return;
            }
        }

        OWChargedAttack attack = OWAttacksHandler.findChargedAttack(owEntity.getClass(), event.getButton());
        if (attack == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            long endMs = getCooldownEnd(owEntity.getId(), attack.getId());
            if (endMs != -1L && System.currentTimeMillis() < endMs) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (isCharging) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (owEntity.isCombo()) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (playerHoldsUsableItem(mc.player)) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (shortChargeCooldownEndMs > System.currentTimeMillis()) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (!attack.canUse(owEntity)) {
                recordAttackClick(attack.getId(), true);
                return;
            }
            if (owEntity.getVitalEnergy() > owEntity.getMaxVitalEnergy() - attack.getEnergyRequired()) {
                owEntity.canShowVitalEnergyLack = true;
                recordAttackClick(attack.getId(), true);
                return;
            }

            isCharging = true;
            chargeStartMs = System.currentTimeMillis();
            chargeDirection = mc.player.getLookAngle();
            currentAttack = attack;

            PacketDistributor.sendToServer(
                    new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_CHARGE_START, 0f));

            // Prédiction côté client : démarre l'animation isPreparing immédiatement sans attendre le serveur
            if (owEntity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger) {
                tiger.isPreparing = true;
            }

        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (!isCharging || currentAttack != attack) return;

            long elapsed = System.currentTimeMillis() - chargeStartMs;
            isCharging = false;
            chargeDirection = mc.player.getLookAngle();

            if (elapsed >= attack.getMinChargeMs()) {
                float chargeFactor = (float) Math.min(
                        (double)(elapsed - attack.getMinChargeMs()) / (attack.getMaxChargeMs() - attack.getMinChargeMs()),
                        1.0);

                PacketDistributor.sendToServer(
                        new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_CHARGE_RELEASE, chargeFactor));

                if (mc.player.getRootVehicle() instanceof OWEntity entity) {
                    boolean hasEnergy = entity.getVitalEnergy() <= entity.getMaxVitalEnergy() - attack.getEnergyRequired();

                    recordAttackClick(attack.getId(), !hasEnergy);

                    if (hasEnergy) {
                        setCooldownEnd(entity.getId(), attack.getId(),
                                System.currentTimeMillis() + (long) attack.getCooldownTicks() * 50L);
                    }
                    attack.applyLocalEffect(entity, chargeFactor, chargeDirection);

                    // Orca Tidal Rush — déclenchement de l'effet caméra côté client
                    if (attack.getId() == OWAttacksHandler.TIDAL_RUSH_ID && hasEnergy) {
                        orcaDashEffectStartMs = System.currentTimeMillis();
                    }
                }
            }  else {
                shortChargeCooldownEndMs = System.currentTimeMillis() + 300L;
                PacketDistributor.sendToServer(
                        new OWAttackPacket(attack.getId(), OWAttackPacket.ACTION_CHARGE_CANCEL, 0f));
                if (owEntity instanceof TigerEntity tiger) {
                    tiger.isPreparing = false;
                }
            }

            currentAttack = null;
        }
    }
}
