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
 * (cf. {@link Chest}). Les tables de butin sont générées par le datagen
 * ({@code net.tiew.operationWild.datagen.OWArenaChestLootProvider}).</p>
 */
public final class OWArena {

    private OWArena() {}

    /**
     * Badge minimal pour que l'arène soit accessible.
     *
     * <p>Une tribu sans badge n'entre pas dans l'arène : les coffres y sont indexés sur le palier
     * de réputation, et sans badge il n'y a tout simplement pas de coffre à donner. C'est aussi ce
     * qui évite qu'une tribu créée à l'instant vienne défier une maison établie pour lui rafler sa
     * réputation sans rien risquer de son côté.</p>
     */
    public static final OWReputation.Badge ARENA_MIN_BADGE = OWReputation.Badge.GOLD_FIRST;

    /** La tribu a-t-elle le rang requis pour accéder à l'arène ? */
    public static boolean arenaUnlocked(int reputation) {
        return OWReputation.badgeFor(reputation).ordinal() >= ARENA_MIN_BADGE.ordinal();
    }

    /** Points de prestige nécessaires pour débloquer un coffre. */
    public static final int PRESTIGE_PER_CHEST = 300;

    /**
     * Coffres de badge à ouvrir pour mériter un <b>coffre de tribu</b>, et badge minimal exigé.
     *
     * <p>Récompense de fidélité, hors du système de badges : elle ne dépend pas du palier de
     * réputation atteint mais de l'assiduité, et n'est accessible qu'aux tribus déjà établies.</p>
     */
    public static final int CHESTS_PER_TRIBE_CHEST = 10;
    public static final OWReputation.Badge TRIBE_CHEST_MIN_BADGE = OWReputation.Badge.GOLD_LAST;

    /** La tribu a-t-elle le rang requis pour recevoir des coffres de tribu ? */
    public static boolean tribeChestUnlocked(int reputation) {
        return OWReputation.badgeFor(reputation).ordinal() >= TRIBE_CHEST_MIN_BADGE.ordinal();
    }

    /** Coffres de tribu restant à réclamer, d'après les coffres de badge déjà ouverts. */
    public static int pendingTribeChests(int badgeChestsClaimed, int tribeChestsClaimed) {
        return Math.max(0, badgeChestsClaimed / CHESTS_PER_TRIBE_CHEST - Math.max(0, tribeChestsClaimed));
    }

    /** Réputation minimale gagnée par une victoire en arène, même face à une tribu plus faible. */
    public static final int MIN_REPUTATION_GAIN = 100;

    /** Nombre maximum de combattants par camp — tous d'archétypes différents. */
    public static final int MAX_FIGHTERS = 5;

    /** Bornes du gain de prestige d'une victoire, et part reversée au vaincu. */
    public static final int PRESTIGE_MIN = 10, PRESTIGE_MAX = 300;
    private static final double PRESTIGE_LOSER_SHARE = 0.25;

    /**
     * Points de prestige gagnés en battant une tribu de réputation {@code opponentReputation}.
     *
     * <p>{@code ((adverse − mienne) / 1000) × 2 × 40}, borné entre {@link #PRESTIGE_MIN} et
     * {@link #PRESTIGE_MAX}. Battre plus fort que soi rapporte donc beaucoup ; s'acharner sur
     * plus faible ne rapporte que le plancher.</p>
     *
     * <p>Le calcul est en <b>virgule flottante</b> : en division entière, un écart de 1100 points
     * donnerait 1 au lieu de 1,1, et le gain tomberait à 80 au lieu de 88.</p>
     */
    public static int prestigeGain(int ownReputation, int opponentReputation) {
        double raw = (opponentReputation - ownReputation) / 1000.0 * 2.0 * 40.0;
        return (int) Math.max(PRESTIGE_MIN, Math.min(PRESTIGE_MAX, Math.round(raw)));
    }

    /**
     * Prestige de consolation du vaincu : une fraction de ce qu'aurait valu sa victoire.
     *
     * <p>Indexé sur le gain plutôt que fixe, sans quoi une victoire au plancher (10) rapporterait
     * moins qu'une défaite — le vaincu serait mieux loti que le vainqueur.</p>
     */
    public static int prestigeConsolation(int winnerGain) {
        return (int) Math.max(1, Math.round(winnerGain * PRESTIGE_LOSER_SHARE));
    }

    // ── Dimension d'arène ───────────────────────────────────────────────────────
    /** Rayon de l'aire de combat autour de l'origine : chunks maintenus chargés et protégés. */
    public static final int ARENA_RADIUS = 64;
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
    /**
     * Délai avant que le verdict ne puisse tomber, une fois le combat lancé.
     *
     * <p>Une entité qui vient de changer de dimension n'apparaît pas immédiatement dans l'index du
     * niveau : le chargement des chunks est asynchrone, et sa section doit devenir visible. Sans ce
     * répit, un camp encore en cours d'enregistrement passait pour anéanti et perdait sur-le-champ.</p>
     */
    public static final long FIGHT_GRACE_MS = 6_000L;

    /**
     * Nombre de relevés consécutifs pendant lesquels un combattant peut rester introuvable avant
     * d'être compté comme mort. Une absence est presque toujours un chunk qui n'a pas suivi, pas un
     * décès — celui-ci se constate directement sur l'entité.
     */
    public static final int MISSING_TOLERANCE = 5;

    /** Temps d'affichage du verdict avant renvoi de tout le monde chez soi (ms). */
    public static final long ENDED_LINGER_MS = 8_000L;

    /**
     * Réputation gagnée par le vainqueur : écart de réputation en sa défaveur, plancher
     * {@link #MIN_REPUTATION_GAIN}. Battre plus fort que soi rapporte donc beaucoup plus.
     */
    public static int reputationGain(int winnerReputation, int loserReputation) {
        return Math.max(MIN_REPUTATION_GAIN, loserReputation - winnerReputation);
    }

    // ── Rareté d'un lot ─────────────────────────────────────────────────────────
    /**
     * Un objet est-il une <b>trouvaille remarquable</b> dans ce coffre précis ?
     *
     * <p>La rareté est <b>relative au coffre</b> : du jade sorti d'un coffre doré est une belle
     * surprise, alors que le même jade dans un coffre de jade est l'ordinaire. On compare donc le
     * rang de convoitise de l'objet au rang de la matière du coffre.</p>
     *
     * <p>Classement tenu à la main, en regard des tables de butin
     * ({@code OWArenaChestLootProvider}) : un objet qu'on y rend plus généreux doit descendre de
     * rang ici, sous peine d'être annoncé comme rare alors qu'il tombe tout le temps.</p>
     */
    public static boolean isRareReward(Chest chest, net.minecraft.world.item.Item item) {
        return rewardRank(item) > materialRank(chest.material());
    }

    private static int materialRank(Material material) {
        return switch (material) { case GOLD -> 0; case JADE, TRIBE -> 1; case RUBY -> 2; };
    }

    /** 0 = courant, 1 = gamme jade, 2 = gamme rubis, 3 = exceptionnel. */
    private static int rewardRank(net.minecraft.world.item.Item item) {
        // Exceptionnel : reste remarquable jusque dans un coffre de rubis.
        if (item == net.minecraft.world.item.Items.NETHERITE_INGOT
                || item == net.minecraft.world.item.Items.ANCIENT_DEBRIS
                || item == net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE
                || item == net.minecraft.world.item.Items.WITHER_SKELETON_SKULL
                || item == net.minecraft.world.item.Items.SHULKER_SHELL
                || item == net.minecraft.world.item.Items.NAUTILUS_SHELL) return 3;

        // Gamme rubis.
        if (item == net.tiew.operationWild.item.OWItems.RUBY.get()
                || item == net.tiew.operationWild.item.OWItems.RUBY_SWORD.get()
                || item == net.tiew.operationWild.item.OWItems.RUBY_PICKAXE.get()
                || item == net.tiew.operationWild.item.OWItems.RUBY_AXE.get()
                || item == net.tiew.operationWild.item.OWItems.RUBY_SHOVEL.get()
                || item == net.tiew.operationWild.item.OWItems.RUBY_HOE.get()
                || item == net.tiew.operationWild.item.OWItems.REPTILIAN_DAGGER.get()
                || item == net.tiew.operationWild.item.OWItems.BOXING_GLOVES.get()
                || item == net.tiew.operationWild.item.OWItems.CAMOUFLAGE_CHESTPLATE.get()
                || item == net.tiew.operationWild.item.OWItems.CAMOUFLAGE_LEGGINGS.get()
                || item == net.tiew.operationWild.item.OWItems.VENOMOUS_GLANDS.get()
                || item == net.tiew.operationWild.item.OWItems.STINGING_FILAMENT.get()
                || item == net.minecraft.world.item.Items.DIAMOND
                || item == net.minecraft.world.item.Items.EMERALD
                || item == net.minecraft.world.item.Items.NETHERITE_SCRAP
                || item == net.minecraft.world.item.Items.GHAST_TEAR
                || item == net.minecraft.world.item.Items.DIAMOND_CHESTPLATE
                || item == net.minecraft.world.item.Items.DIAMOND_LEGGINGS
                || item == net.minecraft.world.item.Items.DIAMOND_HELMET
                || item == net.minecraft.world.item.Items.DIAMOND_BOOTS) return 2;

        // Gamme jade.
        if (item == net.tiew.operationWild.item.OWItems.JADE.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_SWORD.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_PICKAXE.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_AXE.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_SHOVEL.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_HOE.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_HELMET.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_CHESTPLATE.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_LEGGINGS.get()
                || item == net.tiew.operationWild.item.OWItems.JADE_BOOTS.get()
                || item == net.tiew.operationWild.item.OWItems.PREDATOR_TOOTH.get()
                || item == net.tiew.operationWild.item.OWItems.KODIAK_COAT.get()
                || item == net.tiew.operationWild.item.OWItems.SHARK_FIN.get()
                || item == net.tiew.operationWild.item.OWItems.CROCODILE_SCALE.get()
                || item == net.tiew.operationWild.item.OWItems.BIOLUMINESCENT_JELLY.get()
                || item == net.tiew.operationWild.item.OWItems.MAYA_BLOWPIPE.get()
                || item == net.minecraft.world.item.Items.AMETHYST_SHARD
                || item == net.minecraft.world.item.Items.ENDER_PEARL
                || item == net.minecraft.world.item.Items.BLAZE_ROD
                || item == net.minecraft.world.item.Items.PHANTOM_MEMBRANE
                || item == net.minecraft.world.item.Items.GOLDEN_APPLE
                || item == net.minecraft.world.item.Items.DIAMOND_SWORD
                || item == net.minecraft.world.item.Items.DIAMOND_PICKAXE) return 1;

        return 0;
    }

    /** Matière du coffre, calquée sur le métal du badge. */
    public enum Material {
        GOLD(0xE9B115), JADE(0x388C3C), RUBY(0xBF3131),
        /** Coffre de tribu : hors badge, teinte d'ivoire pour le distinguer des trois métaux. */
        TRIBE(0xD8D2C4);
        private final int accent;
        Material(int accent) { this.accent = accent; }
        public int accent() { return accent; }
    }

    /**
     * Gabarit du coffre, calqué sur la division du badge (I → petit, II → moyen, III → grand).
     *
     * <p>Aucun facteur d'échelle : les trois gabarits sont <b>dessinés</b> à leur taille dans la
     * feuille de sprites, sur une toile commune. Redimensionner à l'exécution détruirait le
     * pixel-art, les ratios utiles n'étant pas entiers.</p>
     */
    public enum Size { SMALL, NORMAL, LARGE }

    /**
     * Les neuf coffres d'arène, en correspondance <b>exacte</b> avec les neuf badges de réputation :
     * la division du badge donne le gabarit (I → petit, II → normal, III → grand) et le métal donne
     * la matière. Un « petit coffre doré » est donc l'exact reflet d'un badge Or I.
     *
     * <p>Le butin (items <i>et</i> Pièces Sauvages) monte en gamme sur les neuf crans, sans palier
     * mort : chaque division de badge gagnée se voit immédiatement dans le coffre.</p>
     */
    public enum Chest {
        GOLD_SMALL (Material.GOLD, Size.SMALL,   0,  2),
        GOLD_NORMAL(Material.GOLD, Size.NORMAL,  1,  4),
        GOLD_LARGE (Material.GOLD, Size.LARGE,   3,  7),

        JADE_SMALL (Material.JADE, Size.SMALL,   5, 10),
        JADE_NORMAL(Material.JADE, Size.NORMAL, 11, 16),
        JADE_LARGE (Material.JADE, Size.LARGE,  15, 20),

        RUBY_SMALL (Material.RUBY, Size.SMALL,  22, 27),
        RUBY_NORMAL(Material.RUBY, Size.NORMAL, 30, 39),
        RUBY_LARGE (Material.RUBY, Size.LARGE,  35, 50),

        /**
         * Coffre de tribu — récompense d'assiduité, indépendante du badge. Un seul gabarit :
         * {@link Size#NORMAL} n'est ici qu'une valeur de remplissage, son sprite est unique.
         */
        TRIBE (Material.TRIBE, Size.NORMAL, 5, 20);

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
