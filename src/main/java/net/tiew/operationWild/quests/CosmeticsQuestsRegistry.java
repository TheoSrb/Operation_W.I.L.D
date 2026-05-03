package net.tiew.operationWild.quests;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.core.OWServerData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registre central de toutes les quêtes cosmétiques.
 * Toutes les progressions sont trackées par UUID d'entité.
 */
@OnlyIn(Dist.CLIENT)
public class CosmeticsQuestsRegistry {

    private static final Map<Integer, CosmeticsQuest> REGISTRY = new LinkedHashMap<>();

    // =========================================================================
    // Définitions des quêtes
    // =========================================================================

    public static final CosmeticsQuest SCRATCHES_AND_PENCIL_MARKS = register(
        new CosmeticsQuest(0, "quest.scratches_and_pencil_marks.title", "quest.scratches_and_pencil_marks.desc", 10) {
            @Override
            public void update(UUID entityId) {
                if (isCompleted(entityId)) return;
                AtomicInteger counter = OWServerData.tigerKillCounts.get(entityId);
                setProgress(entityId, counter != null ? counter.get() : 0);
            }
        }
    );

    public static final CosmeticsQuest THE_TREASURE_HUNT = register(
            new CosmeticsQuest(1, "quest.the_treasure_hunt.title", "quest.the_treasure_hunt.desc", 20) {
                @Override
                public void update(UUID entityId) {
                    if (isCompleted(entityId)) return;
                    AtomicInteger counter = OWServerData.tigerKillCounts.get(entityId);
                    setProgress(entityId, counter != null ? counter.get() : 0);
                }
            }
    );

    // =========================================================================
    // API du registre
    // =========================================================================

    private static CosmeticsQuest register(CosmeticsQuest quest) {
        REGISTRY.put(quest.getId(), quest);
        return quest;
    }

    public static CosmeticsQuest getById(int id) {
        return REGISTRY.get(id);
    }

    public static boolean isCompleted(int id, UUID entityId) {
        CosmeticsQuest q = REGISTRY.get(id);
        return q != null && q.isCompleted(entityId);
    }

    public static Collection<CosmeticsQuest> getAllQuests() {
        return REGISTRY.values();
    }

    /**
     * Remet à zéro la quête {@code questId} pour une entité donnée.
     * Réinitialise aussi le compteur en mémoire (ex: tigerKillCounts).
     *
     * Usage : CosmeticsQuestsRegistry.resetForEntity(0, tiger.getUUID());
     */
    public static void resetForEntity(int questId, UUID entityId) {
        CosmeticsQuest quest = REGISTRY.get(questId);
        if (quest == null) return;
        quest.reset(entityId);
        OWServerData.tigerKillCounts.remove(entityId);
    }

    // =========================================================================
    // Persistance
    // =========================================================================

    /**
     * Charge l'état de toutes les quêtes depuis la sauvegarde.
     * Les clés sont au format {@code quest_progress_<id>_<uuid>}.
     */
    public static void loadAllFromSave(Properties props) {
        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith("quest_progress_")) continue;

            String suffix = key.substring("quest_progress_".length()); // "<id>_<uuid>"
            int sep = suffix.indexOf('_');
            if (sep == -1) continue;

            try {
                int  questId  = Integer.parseInt(suffix.substring(0, sep));
                UUID uuid     = UUID.fromString(suffix.substring(sep + 1));
                int  progress = Integer.parseInt(props.getProperty(key, "0"));
                boolean completed = Boolean.parseBoolean(
                        props.getProperty("quest_completed_" + questId + "_" + uuid, "false"));

                CosmeticsQuest quest = REGISTRY.get(questId);
                if (quest != null) quest.loadState(uuid, completed, progress);
            } catch (Exception ignored) {}
        }

        // Synchronise tigerKillCounts depuis la progression sauvegardée (max des deux quêtes de kills)
        Map<UUID, Integer> combined = new HashMap<>();
        for (int id : new int[]{0, 1}) {
            CosmeticsQuest q = REGISTRY.get(id);
            if (q != null) q.getAllProgress().forEach((uuid, val) -> combined.merge(uuid, val, Math::max));
        }
        combined.forEach((uuid, val) -> OWServerData.tigerKillCounts.put(uuid, new AtomicInteger(val)));
    }

    /**
     * Sauvegarde l'état de toutes les quêtes (toutes les entités).
     * Écrit le fichier une seule fois.
     */
    public static void saveAll() {
        Properties props = OWDatasSave.owDatas;
        for (CosmeticsQuest quest : REGISTRY.values()) {
            for (Map.Entry<UUID, Integer> e : quest.getAllProgress().entrySet()) {
                UUID uuid = e.getKey();
                String base = quest.getId() + "_" + uuid;
                props.setProperty("quest_progress_"  + base, String.valueOf(e.getValue()));
                props.setProperty("quest_completed_" + base, String.valueOf(quest.isCompleted(uuid)));
            }
        }
        OWDatasSave.saveQuestStates(props);
    }
}
