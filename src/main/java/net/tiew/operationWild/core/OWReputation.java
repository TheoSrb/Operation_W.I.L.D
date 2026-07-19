package net.tiew.operationWild.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWReputationData;
import net.tiew.operationWild.team.OWTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Réputation de tribu — un score / une « monnaie de prestige » agrégé au niveau de la tribu, dérivé
 * des créatures apprivoisées de tous ses membres (chef + adjoints + membres simples) et de leur
 * Expérience d'Apprivoisement.
 *
 * <p>Le calcul est <b>serveur-autoritatif</b> et lu depuis le registre persistant
 * {@link OWReputationData} : il reste exact même pour les membres hors-ligne (les créatures ne sont
 * pas rescannées, on utilise le dernier état persistant connu).</p>
 *
 * <p>Facteurs pris en compte&nbsp;: nombre de créatures possédées, niveau de chaque créature,
 * dangerosité de l'espèce (tigre &gt; boa…), nombre d'expérience d'apprivoisement de chaque membre,
 * et bonus fort pour les créatures niveau max (50). Un multiplicateur de <b>synergie</b> récompense
 * les tribus comptant plusieurs membres actifs&nbsp;: monter haut est quasi impossible en solo.</p>
 */
public final class OWReputation {

    private OWReputation() {}

    // ── Constantes de formule (invEntées ; ajustables) ──────────────────────────
    /** Valeur plancher qu'apporte n'importe quelle créature apprivoisée. */
    private static final double BASE_PER_ENTITY = 10.0;
    /** Valeur ajoutée par niveau de la créature (1 → 50). */
    private static final double LEVEL_WEIGHT = 1.4;
    /** Bonus (multiplié par la dangerosité) accordé aux créatures niveau max. */
    private static final double MAX_LEVEL_BONUS = 175.0;
    /** Poids de l'Expérience d'Apprivoisement d'un membre dans sa contribution. */
    private static final double TAMING_WEIGHT = 0.10;
    /** Gain de synergie par membre actif supplémentaire (2 membres actifs → ×1.35, 5 → ×2.4…). */
    private static final double SYNERGY_STEP = 0.35;

    /** Dangerosité par espèce (path du {@link EntityType}). Défaut = 1.0. */
    private static final Map<String, Double> DANGER = new HashMap<>();
    static {
        DANGER.put("orca", 2.2);
        DANGER.put("kodiak", 2.0);
        DANGER.put("crocodile", 1.7);
        DANGER.put("tiger", 1.5);
        DANGER.put("kangaroo", 1.1);
        DANGER.put("boa", 1.0);
    }

    public static double dangerOf(String species) {
        return DANGER.getOrDefault(species, 1.0);
    }

    /** Path du type d'entité (ex. {@code "tiger"}), utilisé comme clé d'espèce dans le registre. */
    public static String speciesKey(OWEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
    }

    /** Valeur de réputation d'une seule créature (dangerosité × (base + niveau) + bonus niveau max). */
    public static double entityValue(String species, int level) {
        int lvl = Math.max(1, Math.min(50, level));
        double danger = dangerOf(species);
        double value = danger * (BASE_PER_ENTITY + lvl * LEVEL_WEIGHT);
        if (lvl >= 50) value += danger * MAX_LEVEL_BONUS;
        return value;
    }

    /**
     * Réputation totale d'une tribu, calculée depuis le registre serveur. Somme des contributions
     * de chaque membre (créatures + expérience d'apprivoisement), amplifiée par la synergie des
     * membres actifs.
     */
    public static int compute(OWReputationData data, OWTeam team) {
        if (team == null) return 0;
        if (team.getReputationOverride() >= 0) return team.getReputationOverride(); // valeur forcée (commande admin)
        if (!team.isReputationEnabled()) return 0;                                  // pas encore activée par la tribu
        if (data == null) return 0;
        double raw = 0.0;
        int activeMembers = 0;
        for (UUID member : team.getPlayerUUIDs()) {
            double contribution = data.playerEntityValue(member)
                    + data.getTamingXp(member) * TAMING_WEIGHT;
            if (contribution > 0.0) activeMembers++;
            raw += contribution;
        }
        double synergy = 1.0 + SYNERGY_STEP * Math.max(0, activeMembers - 1);
        // La réputation gagnée en arène s'ajoute au score des créatures sans passer par la synergie :
        // elle a déjà été méritée collectivement sur le terrain.
        return (int) Math.round(raw * synergy) + team.getArenaReputationBonus();
    }

    // ── Paliers de badge ────────────────────────────────────────────────────────
    /**
     * Badges de tribu, du plus faible au plus prestigieux&nbsp;: Or &lt; Jade &lt; Rubis, et au sein
     * d'un métal Première &lt; Seconde &lt; <b>Dernière</b> Division (la Dernière Division est la plus
     * puissante). {@link #NONE} = aucun badge (réputation trop faible). Coordonnées {@code (u,v)} dans
     * {@code gui/ow_teams_sprites.png}, sprites 23×23. L'ordre de déclaration est croissant.
     */
    public enum Badge {
        NONE      (0,     -1,   -1, 0x808080),
        GOLD_FIRST    (750,   233,   0, 0xFDF55F), // Or I
        GOLD_SECOND   (2000,  233,  23, 0xE9B115), // Or II
        GOLD_LAST     (4500,  233,  46, 0xB26411), // Or III
        JADE_FIRST    (9000,  233,  69, 0x71A147), // Jade I
        JADE_SECOND   (16000, 233,  92, 0x388C3C), // Jade II
        JADE_LAST     (28000, 233, 115, 0x256825), // Jade III
        RUBY_FIRST    (45000, 233, 138, 0xE26954), // Rubis I
        RUBY_SECOND   (70000, 233, 161, 0xBF3131), // Rubis II
        RUBY_LAST     (110000,233, 184, 0x751122); // Rubis III

        public static final int SPRITE_SIZE = 23;

        private final int threshold;
        private final int u, v;
        private final int accent;

        Badge(int threshold, int u, int v, int accent) {
            this.threshold = threshold;
            this.u = u;
            this.v = v;
            this.accent = accent;
        }

        public int threshold() { return threshold; }
        public int u() { return u; }
        public int v() { return v; }
        public int accent() { return accent; }
        public boolean hasSprite() { return this != NONE; }

        /** Clé de traduction du nom du badge (ex. {@code owteams.reputation.badge.gold_last}). */
        public String translationKey() {
            return "owteams.reputation.badge." + name().toLowerCase();
        }
    }

    /** Badge correspondant à une réputation donnée (le plus élevé dont le seuil est atteint). */
    public static Badge badgeFor(int reputation) {
        Badge result = Badge.NONE;
        for (Badge b : Badge.values()) {
            if (b != Badge.NONE && reputation >= b.threshold()) result = b;
        }
        return result;
    }

    /** Badge immédiatement supérieur, ou {@code null} si {@code badge} est déjà le sommet. */
    public static Badge nextBadge(Badge badge) {
        Badge[] all = Badge.values();
        int idx = badge.ordinal() + 1;
        return idx < all.length ? all[idx] : null;
    }

    /**
     * Progression [0..1] vers le badge suivant. Au palier maximum, renvoie 1. Sous le premier
     * badge, progresse de 0 (réputation nulle) au seuil du premier badge.
     */
    public static float progress(int reputation, Badge current) {
        Badge next = nextBadge(current);
        if (next == null) return 1.0f;
        int lo = current.threshold(); // NONE.threshold() == 0
        int hi = next.threshold();
        if (hi <= lo) return 1.0f;
        float p = (reputation - lo) / (float) (hi - lo);
        return Math.max(0.0f, Math.min(1.0f, p));
    }
}
