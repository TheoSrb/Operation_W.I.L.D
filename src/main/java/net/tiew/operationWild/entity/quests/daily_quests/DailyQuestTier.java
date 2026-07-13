package net.tiew.operationWild.entity.quests.daily_quests;

import net.minecraft.util.RandomSource;

/**
 * Palier (« tier ») d'une quête quotidienne : plus il est élevé, plus la récompense est grande.
 *
 * <p>La récompense est <b>soit</b> de l'expérience (orbes), <b>soit</b> des Pièces Sauvages — jamais
 * les deux. {@link #rollReward(RandomSource)} tire la récompense et l'encode dans un entier :
 * valeur {@code > 0} = nombre d'orbes d'XP, valeur {@code < 0} = {@code -nombre de pièces}.</p>
 *
 * <ul>
 *   <li><b>TIER I</b> : 5–10 orbes, pas de pièces.</li>
 *   <li><b>TIER II</b> : 7–15 orbes, ou (1 chance sur 3) 1 pièce à la place.</li>
 *   <li><b>TIER III</b> : 10–20 orbes, ou (1 chance sur 2) 1–2 pièces à la place.</li>
 * </ul>
 */
public enum DailyQuestTier {

    I(5, 10, 0.0f, 0, 0),
    II(7, 15, 1.0f / 3.0f, 1, 1),
    III(10, 20, 0.5f, 1, 2);

    private final int xpMin, xpMax;
    private final float coinChance;
    private final int coinMin, coinMax;

    DailyQuestTier(int xpMin, int xpMax, float coinChance, int coinMin, int coinMax) {
        this.xpMin = xpMin;
        this.xpMax = xpMax;
        this.coinChance = coinChance;
        this.coinMin = coinMin;
        this.coinMax = coinMax;
    }

    /**
     * Tire la récompense de ce palier.
     *
     * @return code encodé : {@code > 0} = orbes d'XP, {@code < 0} = {@code -pièces}.
     */
    /** Couleur du palier : vert (facile) → orange (moyen) → rouge (difficile). */
    public int color() {
        return switch (this) {
            case I -> 0x5FB84A;
            case II -> 0xE8952E;
            case III -> 0xD1442E;
        };
    }

    public int rollReward(RandomSource rng) {
        if (coinChance > 0f && rng.nextFloat() < coinChance) {
            int coins = coinMin + (coinMax > coinMin ? rng.nextInt(coinMax - coinMin + 1) : 0);
            return -Math.max(1, coins);
        }
        int xp = xpMin + (xpMax > xpMin ? rng.nextInt(xpMax - xpMin + 1) : 0);
        return Math.max(1, xp);
    }
}
