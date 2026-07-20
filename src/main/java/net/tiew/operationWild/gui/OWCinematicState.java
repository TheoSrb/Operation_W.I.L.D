package net.tiew.operationWild.gui;

/**
 * Sait si une cinématique d'arène est en train de jouer côté client.
 *
 * <p>Sert à écarter les éléments d'interface qui passeraient par-dessus : le tchat, notamment, est
 * dessiné par le HUD et se superpose sinon aux animations plein écran.</p>
 *
 * <p>Les deux cinématiques d'arène portent leur propre état ; celle du coffre vit dans l'écran de
 * tribu, qui le recopie ici — un écran étant instancié à la volée, il ne peut pas être interrogé
 * directement depuis un gestionnaire d'événements.</p>
 */
public final class OWCinematicState {

    private OWCinematicState() {}

    /**
     * Garde-fou : au-delà de cette durée, une ouverture de coffre est tenue pour terminée quoi qu'il
     * arrive. Les deux cinématiques de duel expirent d'elles-mêmes ; celle du coffre est un drapeau
     * qu'un chemin de code oublié pourrait laisser levé — et une interface masquée pour toujours
     * serait bien pire que la fin manquée d'une animation.
     */
    private static final long CHEST_MAX_MS = 30_000L;

    /** Ouverture de coffre en cours (piloté par {@code OWTribeArenaScreen}). */
    private static volatile boolean chestOpening = false;
    private static volatile long chestOpeningSince = 0L;

    public static void setChestOpening(boolean active) {
        chestOpening = active;
        chestOpeningSince = active ? System.currentTimeMillis() : 0L;
    }

    private static boolean chestOpeningActive() {
        if (!chestOpening) return false;
        if (System.currentTimeMillis() - chestOpeningSince > CHEST_MAX_MS) {
            chestOpening = false;
            return false;
        }
        return true;
    }

    /** Vrai si l'une des cinématiques d'arène occupe l'écran. */
    public static boolean anyPlaying() {
        return chestOpeningActive()
                || OWArenaClashOverlay.isPlaying()
                || OWArenaVictoryOverlay.isPlaying();
    }
}
