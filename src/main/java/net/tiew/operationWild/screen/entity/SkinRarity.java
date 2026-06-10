package net.tiew.operationWild.screen.entity;

/**
 * Raretés de skins et leur coût d'achat en Pièces Sauvages (Wild Coins).
 * Source unique de vérité du prix : un skin achetable référence une rareté, jamais un nombre brut.
 */
public enum SkinRarity {
    COMMON(5),
    RARE(8),
    EPIC(14),
    LEGENDARY(20);

    /** Coût d'achat en Pièces Sauvages. */
    public final int cost;

    SkinRarity(int cost) {
        this.cost = cost;
    }
}
