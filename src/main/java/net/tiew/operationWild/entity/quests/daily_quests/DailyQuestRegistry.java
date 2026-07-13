package net.tiew.operationWild.entity.quests.daily_quests;

/**
 * Registre statique et indexable des quêtes quotidiennes possibles.
 *
 * <p>Chaque quête a un id fixe (0..10) qui sert d'index dans {@link #ALL} et de clé partout
 * ailleurs (progression sur l'entité, sélection du jour, écran). {@code maxValue} est le seuil
 * de complétion : il doit rester cohérent avec les seuils utilisés dans
 * {@code OWEntity.executeQuestProgression} et avec l'affichage de l'écran.</p>
 *
 * <p>{@link #init()} est idempotent : il peut être appelé plusieurs fois sans dupliquer les
 * quêtes (l'ancien système s'auto-enregistrait dans une liste et créait des doublons).</p>
 */
public class DailyQuestRegistry {

    /** Toutes les quêtes, indexées par id. */
    public static final DailyQuest[] ALL = new DailyQuest[11];

    // Références nommées conservées pour les nombreux appels existants dans OWEntity
    // (ex : isQuestInProgress(DailyQuestRegistry.quest3)). questN correspond à l'id N-1.
    public static DailyQuest quest1 = null;
    public static DailyQuest quest2 = null;
    public static DailyQuest quest3 = null;
    public static DailyQuest quest4 = null;
    public static DailyQuest quest5 = null;
    public static DailyQuest quest6 = null;
    public static DailyQuest quest7 = null;
    public static DailyQuest quest8 = null;
    public static DailyQuest quest9 = null;
    public static DailyQuest quest10 = null;
    public static DailyQuest quest11 = null;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        //                                   id  clé de langue      seuil  difficulté  palier
        ALL[0]  = quest1  = new DailyQuest(0,  "dailyQuest.0",   300,   0, DailyQuestTier.II);   // infliger 300 dégâts
        ALL[1]  = quest2  = new DailyQuest(1,  "dailyQuest.1",   200,   5, DailyQuestTier.II);   // absorber 200 dégâts
        ALL[2]  = quest3  = new DailyQuest(2,  "dailyQuest.2",   100,   2, DailyQuestTier.I);    // régénérer 100 PV
        ALL[3]  = quest4  = new DailyQuest(3,  "dailyQuest.3",   2000,  1, DailyQuestTier.I);    // parcourir 2000 blocs
        ALL[4]  = quest5  = new DailyQuest(4,  "dailyQuest.4",   1,     5, DailyQuestTier.II);   // 40 dégâts en 5 s
        ALL[5]  = quest6  = new DailyQuest(5,  "dailyQuest.5",   1,     3, DailyQuestTier.II).setRequiresLevelUp(true);   // passer 1 niveau (impossible au niv. max)
        ALL[6]  = quest7  = new DailyQuest(6,  "dailyQuest.6",   25,    2, DailyQuestTier.III);  // éliminer 25 cibles
        ALL[7]  = quest8  = new DailyQuest(7,  "dailyQuest.7",   1,     2, DailyQuestTier.III);  // 5 cibles d'un seul coup
        ALL[8]  = quest9  = new DailyQuest(8,  "dailyQuest.8",   1,     1, DailyQuestTier.III);  // 10 cibles en 5 s
        ALL[9]  = quest10 = new DailyQuest(9,  "dailyQuest.9",   50,    0, DailyQuestTier.II);   // 50 dégâts aux + puissants
        ALL[10] = quest11 = new DailyQuest(10, "dailyQuest.10",  8,     5, DailyQuestTier.III);  // 8 cibles sans être touché
    }

    public static DailyQuest getById(int id) {
        return (id >= 0 && id < ALL.length) ? ALL[id] : null;
    }
}
