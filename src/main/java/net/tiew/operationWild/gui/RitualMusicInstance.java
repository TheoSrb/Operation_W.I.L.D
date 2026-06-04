package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.tiew.operationWild.sound.OWSounds;

/**
 * Musique du Rituel de Communion en 3 phases : <b>intro</b> (une fois) → <b>combat</b> (boucle)
 * → <b>outro/end</b> (une fois).
 *
 * <p>La boucle de combat est gérée manuellement et relancée ~7 s AVANT la fin réelle du morceau
 * (pour que la traîne recouvre la couture). <b>Important :</b> la relance ({@code play}) se fait
 * via {@link #onCombatLoopTick()} appelé depuis le tick CLIENT — surtout pas depuis {@link #tick()}
 * de l'instance, qui s'exécute pendant l'itération du moteur audio (sinon
 * {@link java.util.ConcurrentModificationException}).</p>
 */
public class RitualMusicInstance extends AbstractTickableSoundInstance {

    /**
     * Instant (en ticks depuis le départ) où la boucle de combat repart. Le morceau dure ~151,8 s
     * (≈3036 ticks) ; on relance ~7 s avant la fin → 3036 - 140 = 2896.
     */
    public static int COMBAT_LOOP_TICKS = 2896;

    private static RitualMusicInstance current;
    private static int currentPhase = -1;
    private static int combatAge = 0;

    private RitualMusicInstance(SoundEvent event) {
        super(event, SoundSource.MUSIC, RandomSource.create());
        this.looping = false;       // boucle gérée à la main (cf. onCombatLoopTick)
        this.delay = 0;
        this.volume = 1.6f;
        this.relative = true;       // musique globale
        this.attenuation = Attenuation.NONE;
    }

    /** Joue la piste de la phase (0 = intro, 1 = combat, 2 = outro). Appelé depuis le tick client. */
    public static void update(int phase) {
        if (phase == currentPhase && current != null) return;

        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
            current = null;
        }
        Minecraft.getInstance().getMusicManager().stopPlaying(); // pas de musique vanilla

        SoundEvent event;
        switch (phase) {
            case 0 -> event = OWSounds.RITUAL_INTRO.get();
            case 2 -> event = OWSounds.RITUAL_END.get();
            default -> event = OWSounds.RITUAL_THEME.get();
        }
        current = new RitualMusicInstance(event);
        Minecraft.getInstance().getSoundManager().play(current);
        currentPhase = phase;
        combatAge = 0;
    }

    /**
     * À appeler une fois par tick CLIENT (hors tick du moteur audio). Gère la boucle de combat :
     * relance une nouvelle instance ~7 s avant la fin, l'ancienne finissant sa traîne seule.
     */
    public static void onCombatLoopTick() {
        if (currentPhase != 1 || current == null) return;
        // En pause, l'audio est figé : on n'avance PAS le compteur, sinon il se désynchronise
        // de la lecture réelle et la boucle repart bien trop tôt.
        if (Minecraft.getInstance().isPaused()) return;
        combatAge++;
        if (combatAge >= COMBAT_LOOP_TICKS) {
            current = new RitualMusicInstance(OWSounds.RITUAL_THEME.get());
            Minecraft.getInstance().getSoundManager().play(current);
            combatAge = 0;
        }
    }

    /** Coupe toute musique de rituel (fin / interruption). */
    public static void stopAll() {
        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
            current = null;
        }
        currentPhase = -1;
        combatAge = 0;
    }

    @Override
    public void tick() {
        // Sécurité : si le rituel n'est plus actif, on s'arrête. (this.stop() est sûr ici ;
        // ne JAMAIS appeler play() depuis ce tick → ConcurrentModificationException du moteur audio.)
        if (!ClientRitualState.active) {
            this.stop();
        }
    }
}
