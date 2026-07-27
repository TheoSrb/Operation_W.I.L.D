package net.tiew.operationWild.entity.config;

/**
 * Créature qui plonge, et qui a donc une profondeur au-delà de laquelle la pression blesse.
 *
 * <p>Les deux familles aquatiques — {@code OWWaterEntity} et {@code OWSemiWaterEntity} — déclaraient
 * chacune leur {@code getMaxDepth()} sans rien partager sous {@code OWEntity}. Tout ce qui voulait
 * lire une profondeur devait donc nommer une famille et oubliait l'autre : c'est exactement ce qui
 * privait l'orque de la jauge de profondeur du crocodile.</p>
 */
public interface IOWDiver {

    /** Profondeur, en blocs sous le niveau de la mer, à partir de laquelle la pression fait mal. */
    int getMaxDepth();
}
