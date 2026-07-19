package net.tiew.operationWild.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;
import net.tiew.operationWild.OperationWild;

/**
 * Arène de tribu — règles communes client/serveur du système de coffres de prestige.
 *
 * <p><b>Modèle de progression.</b> Le compteur de points de prestige ({@code arenaPrestige}) est
 * <b>partagé par la tribu</b> : il est alimenté par les victoires en arène. Chaque tranche de
 * {@link #PRESTIGE_PER_CHEST} points franchie débloque <b>un coffre pour chaque membre</b>, que
 * chacun réclame individuellement (compteur {@code arenaChestsClaimed} par joueur).</p>
 *
 * <p>On raisonne en <i>paliers cumulés</i> plutôt qu'en « reset à 0 » : les coffres disponibles d'un
 * membre valent {@code arenaPrestige / 300 - coffresRéclamés}. Visuellement le résultat est
 * identique (la barre repart de zéro à chaque palier, cf. {@link #barProgress(int)}), mais un membre
 * hors-ligne — ou qui n'ouvre jamais son coffre — ne bloque jamais la progression des autres, et
 * aucun coffre n'est perdu.</p>
 *
 * <p>La qualité du butin dépend du badge de réputation de la tribu au moment de l'ouverture
 * (cf. {@link Tier}). Les tables de butin sont générées par le datagen
 * ({@code net.tiew.operationWild.datagen.OWArenaChestLootProvider}).</p>
 */
public final class OWArena {

    private OWArena() {}

    /** Points de prestige nécessaires pour débloquer un coffre. */
    public static final int PRESTIGE_PER_CHEST = 300;

    /** Réputation minimale gagnée par une victoire en arène, même face à une tribu plus faible. */
    public static final int MIN_REPUTATION_GAIN = 100;

    /** Nombre maximum de combattants par camp — tous d'archétypes différents. */
    public static final int MAX_FIGHTERS = 5;

    /** Points de prestige gagnés par la tribu victorieuse / vaincue. */
    public static final int PRESTIGE_WIN = 150, PRESTIGE_LOSS = 40;

    // ── Dimension d'arène ───────────────────────────────────────────────────────
    /** Écart en X entre deux aires de combat simultanées (monde plat, aires alignées sur l'axe X). */
    public static final int ARENA_SPACING = 512;
    /** Altitude de plain-pied de la dimension d'arène (surface du monde plat + 1). */
    public static final int ARENA_Y = 64;
    /** Demi-écart entre les deux camps de départ. */
    public static final int ARENA_HALF_SPAN = 16;
    /** Recul du chef derrière sa ligne, pour qu'il observe sans être au contact. */
    public static final int ARENA_CHIEF_BACK = 10;

    // ── Zone de combat rétrécissante ────────────────────────────────────────────
    /** Côté de la zone au début du combat (world border : la taille est le diamètre). */
    public static final int BORDER_START = 100;
    /** Côté final de la zone, une fois le rétrécissement terminé. */
    public static final int BORDER_END = 20;
    /** Répit avant que la zone ne commence à se refermer. */
    public static final long BORDER_HOLD_MS = 60_000L;
    /** Durée du rétrécissement, de {@link #BORDER_START} à {@link #BORDER_END}. */
    public static final long BORDER_SHRINK_MS = 9 * 60_000L;

    /**
     * Durée maximale d'un combat avant verdict aux survivants (ms). Volontairement supérieure à
     * {@link #BORDER_HOLD_MS} + {@link #BORDER_SHRINK_MS} : la zone doit avoir le temps d'achever sa
     * fermeture et de trancher elle-même, le minuteur n'étant qu'un filet de sécurité.
     */
    public static final long FIGHT_TIMEOUT_MS = BORDER_HOLD_MS + BORDER_SHRINK_MS + 60_000L;
    /** Délai de sélection des combattants avant annulation automatique du match (ms). */
    public static final long SELECTION_TIMEOUT_MS = 5 * 60_000L;
    /** Temps d'affichage du verdict avant renvoi de tout le monde chez soi (ms). */
    public static final long ENDED_LINGER_MS = 8_000L;

    /**
     * Réputation gagnée par le vainqueur : écart de réputation en sa défaveur, plancher
     * {@link #MIN_REPUTATION_GAIN}. Battre plus fort que soi rapporte donc beaucoup plus.
     */
    public static int reputationGain(int winnerReputation, int loserReputation) {
        return Math.max(MIN_REPUTATION_GAIN, loserReputation - winnerReputation);
    }

    /** Matière du coffre, calquée sur le métal du badge. */
    public enum Material { GOLD(0xE9B115), JADE(0x388C3C), RUBY(0xBF3131);
        private final int accent;
        Material(int accent) { this.accent = accent; }
        public int accent() { return accent; }
    }

    /** Gabarit du coffre, calqué sur la division du badge (I → petit, II → normal, III → grand). */
    public enum Size { SMALL(0.72f), NORMAL(1.0f), LARGE(1.28f);
        private final float scale;
        Size(float scale) { this.scale = scale; }
        /** Facteur d'échelle du dessin du coffre. */
        public float scale() { return scale; }
    }

    /**
     * Les neuf coffres d'arène, en correspondance <b>exacte</b> avec les neuf badges de réputation :
     * la division du badge donne le gabarit (I → petit, II → normal, III → grand) et le métal donne
     * la matière. Un « petit coffre doré » est donc l'exact reflet d'un badge Or I.
     *
     * <p>Le butin (items <i>et</i> Pièces Sauvages) monte en gamme sur les neuf crans, sans palier
     * mort : chaque division de badge gagnée se voit immédiatement dans le coffre.</p>
     */
    public enum Chest {
        GOLD_SMALL (Material.GOLD, Size.SMALL,   120,  300),
        GOLD_NORMAL(Material.GOLD, Size.NORMAL,  250,  560),
        GOLD_LARGE (Material.GOLD, Size.LARGE,   450,  920),
        JADE_SMALL (Material.JADE, Size.SMALL,   700, 1300),
        JADE_NORMAL(Material.JADE, Size.NORMAL, 1100, 1950),
        JADE_LARGE (Material.JADE, Size.LARGE,  1600, 2750),
        RUBY_SMALL (Material.RUBY, Size.SMALL,  2300, 3800),
        RUBY_NORMAL(Material.RUBY, Size.NORMAL, 3400, 5400),
        RUBY_LARGE (Material.RUBY, Size.LARGE,  5000, 8200);

        private final Material material;
        private final Size size;
        private final int minCoins, maxCoins;

        Chest(Material material, Size size, int minCoins, int maxCoins) {
            this.material = material;
            this.size = size;
            this.minCoins = minCoins;
            this.maxCoins = maxCoins;
        }

        public Material material() { return material; }
        public Size size() { return size; }

        /** Couleur d'accent (ferrures, barre de progression, particules d'ouverture). */
        public int accent() { return material.accent(); }

        public int minCoins() { return minCoins; }
        public int maxCoins() { return maxCoins; }

        /** Nom complet du coffre (ex. {@code owteams.arena.chest.jade_large} → « Grand coffre de jade »). */
        public String translationKey() { return "owteams.arena.chest." + name().toLowerCase(); }

        /** Chemin de la table de butin ({@code ow:chests/arena_jade_large}). */
        public String lootPath() { return "arena_" + name().toLowerCase(); }

        public ResourceKey<LootTable> lootTable() {
            return ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "chests/" + lootPath()));
        }

        /** Montant de Pièces Sauvages tiré au sort dans la fourchette du coffre. */
        public int rollCoins(RandomSource random) {
            return minCoins + random.nextInt(maxCoins - minCoins + 1);
        }

        public static Chest byOrdinal(int i) {
            Chest[] all = values();
            return all[Math.max(0, Math.min(all.length - 1, i))];
        }
    }

    /** Où en est la tribu du joueur local vis-à-vis du système de combat. */
    public enum Phase {
        /** Aucun défi, aucun match : on peut en lancer un. */
        IDLE,
        /** Défi émis, en attente de la réponse du chef adverse. */
        CHALLENGE_SENT,
        /** Défi reçu, en attente de notre réponse. */
        CHALLENGE_RECEIVED,
        /** Match ouvert : les deux chefs composent leur équipe. */
        SELECTION,
        /** Combat en cours dans la dimension d'arène. */
        FIGHTING,
        /** Verdict rendu. */
        ENDED;

        public static Phase byOrdinal(int i) {
            Phase[] all = values();
            return all[Math.max(0, Math.min(all.length - 1, i))];
        }
    }

    /** Issue d'un match, telle qu'affichée au joueur. */
    public enum Result { NONE, WIN, LOSS, DRAW }

    /**
     * Coffre correspondant à un badge — correspondance directe, badge par badge. Sans badge, on
     * accorde tout de même le plus petit coffre doré plutôt que rien.
     */
    public static Chest chestFor(OWReputation.Badge badge) {
        if (badge == null) return Chest.GOLD_SMALL;
        return switch (badge) {
            case NONE, GOLD_FIRST -> Chest.GOLD_SMALL;
            case GOLD_SECOND -> Chest.GOLD_NORMAL;
            case GOLD_LAST -> Chest.GOLD_LARGE;
            case JADE_FIRST -> Chest.JADE_SMALL;
            case JADE_SECOND -> Chest.JADE_NORMAL;
            case JADE_LAST -> Chest.JADE_LARGE;
            case RUBY_FIRST -> Chest.RUBY_SMALL;
            case RUBY_SECOND -> Chest.RUBY_NORMAL;
            case RUBY_LAST -> Chest.RUBY_LARGE;
        };
    }

    /** Coffre correspondant à un score de réputation. */
    public static Chest chestForReputation(int reputation) {
        return chestFor(OWReputation.badgeFor(reputation));
    }

    /** Nombre total de coffres débloqués par la tribu depuis le début (paliers franchis). */
    public static int totalChestsUnlocked(int prestige) {
        return Math.max(0, prestige) / PRESTIGE_PER_CHEST;
    }

    /** Coffres qu'il reste à réclamer à un membre donné (jamais négatif). */
    public static int pendingChests(int prestige, int claimed) {
        return Math.max(0, totalChestsUnlocked(prestige) - Math.max(0, claimed));
    }

    /** Points accumulés dans le palier en cours (0 → 299). */
    public static int prestigeInTier(int prestige) {
        return Math.max(0, prestige) % PRESTIGE_PER_CHEST;
    }

    /** Progression [0..1] de la barre vers le prochain coffre. */
    public static float barProgress(int prestige) {
        return prestigeInTier(prestige) / (float) PRESTIGE_PER_CHEST;
    }
}
