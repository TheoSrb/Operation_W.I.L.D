package net.tiew.operationWild.team;

import net.minecraft.network.chat.Component;

/**
 * Condition d'entrée d'une tribu <b>publique</b> — le seul prérequis qu'un joueur doit remplir pour
 * la rejoindre sans invitation. Une tribu porte <b>une seule</b> condition à la fois ({@link #NONE}
 * = ouverte à tous), accompagnée d'un seuil entier ({@link OWTeam#getJoinThreshold()}).
 *
 * <p>L'{@link #getId() id} est stable : il est écrit dans le NBT du registre des tribus et transite
 * sur le réseau. Ne jamais le réutiliser ni le réattribuer.</p>
 *
 * <p>Cette classe est <b>client-safe</b> (libellés, bornes, formatage). La mesure de la valeur d'un
 * joueur est serveur-autoritative et vit dans {@link OWTribeJoinCheck}.</p>
 */
public enum OWTribeJoinCondition {

    /** Aucune condition : n'importe qui peut rejoindre la tribu publique. */
    NONE               (0,  0,      0,   0,   0),
    /** Pièces Sauvages du porte-monnaie du joueur ({@link net.tiew.operationWild.core.OWCurrency}). */
    WILD_COINS         (1,  100,    10,  1,   100_000),
    /** Nombre de créatures apprivoisées possédées. */
    ENTITY_COUNT       (2,  3,      1,   1,   50),
    /** Nombre de créatures au niveau maximum (50). */
    MAX_LEVEL_ENTITIES (3,  1,      1,   1,   50),
    /** Niveau de la meilleure créature possédée. */
    BEST_ENTITY_LEVEL  (4,  25,     1,   1,   50),
    /** Somme des niveaux de toutes les créatures possédées. */
    TOTAL_LEVELS       (5,  100,    10,  1,   2_500),
    /** Nombre d'espèces différentes possédées. */
    SPECIES_COUNT      (6,  2,      1,   1,   12),
    // id 7 = ancienne condition « créatures dangereuses », retirée (voir la note sur l'id 12).
    /** Réputation apportée par les créatures du joueur ({@link net.tiew.operationWild.core.OWReputation}). */
    REPUTATION_VALUE   (8,  500,    50,  1,   500_000),
    /** Expérience d'Apprivoisement du joueur ({@link net.tiew.operationWild.core.OWTamingXp}). */
    TAMING_XP          (9,  100,    10,  1,   100_000),
    /** Niveau d'expérience vanilla du joueur. */
    XP_LEVEL           (10, 10,     1,   1,   200),
    /** Temps de jeu total sur le monde / serveur, en heures. */
    PLAYTIME_HOURS     (11, 5,      1,   1,   1_000);
    // ids retirés, à NE PAS réattribuer : 7 (« créatures dangereuses »), 12 (« formes de bannière »).
    // Les tribus qui les portaient retombent sur NONE et perdent simplement la condition.

    private final int id;
    private final int defaultThreshold;
    private final int step;
    private final int minThreshold;
    private final int maxThreshold;

    OWTribeJoinCondition(int id, int defaultThreshold, int step, int minThreshold, int maxThreshold) {
        this.id = id;
        this.defaultThreshold = defaultThreshold;
        this.step = step;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public int getId() { return id; }
    public int getDefaultThreshold() { return defaultThreshold; }
    public int getStep() { return step; }
    public int getMinThreshold() { return minThreshold; }
    public int getMaxThreshold() { return maxThreshold; }

    /** Vrai si la condition se règle avec un seuil (tout sauf {@link #NONE}). */
    public boolean hasThreshold() { return this != NONE; }

    /** Ramène {@code v} dans les bornes de la condition. */
    public int clamp(int v) { return Math.max(minThreshold, Math.min(maxThreshold, v)); }

    private String key() { return "owteams.cond." + name().toLowerCase(); }

    /** Libellé court (ex. « Créatures niveau max »). */
    public Component getLabel() { return Component.translatable(key()); }

    /** Description d'une ligne, affichée sous le libellé dans l'écran de réglage. */
    public Component getDescription() { return Component.translatable(key() + ".desc"); }

    /** Exigence formatée avec le seuil (ex. « 3 créatures niveau 50 »), pour la liste et les erreurs. */
    public Component getRequirement(int threshold) {
        return Component.translatable(key() + ".req", threshold);
    }

    public static OWTribeJoinCondition byId(int id) {
        for (OWTribeJoinCondition c : values()) if (c.id == id) return c;
        return NONE;
    }
}
