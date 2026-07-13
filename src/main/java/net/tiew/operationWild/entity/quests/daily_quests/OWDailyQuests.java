package net.tiew.operationWild.entity.quests.daily_quests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilitaires des quêtes quotidiennes.
 *
 * <p>Les quêtes sont désormais <b>propres à chaque individu</b> : chaque {@code OWEntity} tire et
 * réinitialise ses 3 quêtes toute seule sur son tick serveur, en comparant sa dernière période de
 * reset à {@link #computePeriodDay()}. Il n'y a donc plus d'état global, de {@code SavedData} ni de
 * planificateur centralisé.</p>
 */
public class OWDailyQuests {

    public static final int DAILY_QUEST_HOUR = 14;
    public static final int DAILY_QUEST_MINUTES = 0;
    public static final int DAILY_QUEST_SECONDS = 0;

    /** Heure réelle locale de réinitialisation quotidienne. */
    public static final LocalTime RESET_TIME =
            LocalTime.of(DAILY_QUEST_HOUR, DAILY_QUEST_MINUTES, DAILY_QUEST_SECONDS);

    private OWDailyQuests() {}

    /**
     * Jour (epoch day) de la période de quêtes courante d'après l'horloge réelle : c'est le jour du
     * dernier passage de {@link #RESET_TIME}. Avant l'heure de reset, on est encore rattaché à la
     * journée précédente. Deux entités qui tickent le même jour partagent donc la même période.
     */
    public static long computePeriodDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate day = now.toLocalTime().isBefore(RESET_TIME)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
        return day.toEpochDay();
    }

    /** Tire 3 ids de quêtes distincts parmi le registre (complète avec -1 s'il y en a moins de 3). */
    public static int[] pickRandomQuestIds() {
        return pickRandomQuestIds(id -> true);
    }

    /** Comme {@link #pickRandomQuestIds()} mais ne retient que les ids acceptés par {@code allowed}. */
    public static int[] pickRandomQuestIds(java.util.function.IntPredicate allowed) {
        DailyQuestRegistry.init();
        int n = DailyQuestRegistry.ALL.length;
        List<Integer> pool = new ArrayList<>(n);
        for (int i = 0; i < n; i++) if (allowed.test(i)) pool.add(i);
        Collections.shuffle(pool);

        int count = Math.min(3, pool.size());
        int[] ids = {-1, -1, -1};
        for (int i = 0; i < count; i++) ids[i] = pool.get(i);
        return ids;
    }
}
