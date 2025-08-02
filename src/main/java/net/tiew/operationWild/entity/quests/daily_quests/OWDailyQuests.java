package net.tiew.operationWild.entity.quests.daily_quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;

import java.util.ArrayList;
import java.util.List;

public class OWDailyQuests {
    public static ArrayList<Object> QUESTS = new ArrayList<>();
    public static List<Object> CHOOSE_QUESTS = new ArrayList<>();
    public static MinecraftServer server;

    public static final int DAILY_QUEST_HOUR = 14;
    public static final int DAILY_QUEST_MINUTES = 0;
    public static final int DAILY_QUEST_SECONDS = 0;

    public static void run() {

        DailyQuestRegistry.run();
        DailyQuestsDate.run();

    }

    public static void resetAllOWEntityQuestProgress() {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        List<OWEntity> entities = new ArrayList<>();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof OWEntity owEntity) {
                entities.add(owEntity);
            }
        }

        for (OWEntity owEntity : entities) {
            owEntity.quest0Progression = 0;
            owEntity.quest1Progression = 0;
            owEntity.quest2Progression = 0;
            owEntity.quest3Progression = 0;
            owEntity.quest4Progression = 0;
            owEntity.quest5Progression = 0;
            owEntity.quest6Progression = 0;
            owEntity.quest7Progression = 0;
            owEntity.quest8Progression = 0;
            owEntity.quest9Progression = 0;
            owEntity.quest10Progression = 0;

            owEntity.quest0isLocked = false;
            owEntity.quest1isLocked = false;
            owEntity.quest2isLocked = false;
            owEntity.quest3isLocked = false;
            owEntity.quest4isLocked = false;
            owEntity.quest5isLocked = false;
            owEntity.quest6isLocked = false;
            owEntity.quest7isLocked = false;
            owEntity.quest8isLocked = false;
            owEntity.quest9isLocked = false;
            owEntity.quest10isLocked = false;
        }
    }

    public static MinecraftServer getCurrentServer() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    public static DailyQuest showChooseQuests(int id) {
        id = Math.min(Math.max(0, id), 2);

        if (DailyQuestsDate.savedQuests[id] != null) {
            return DailyQuestsDate.savedQuests[id];
        }

        if (CHOOSE_QUESTS == null || CHOOSE_QUESTS.isEmpty()) {
            CHOOSE_QUESTS = OWDailyQuests.chooseRandomQuests(OWDailyQuests.QUESTS, 3);
        }
        return (DailyQuest) CHOOSE_QUESTS.get(id);
    }

    public static List<Object> chooseRandomQuests(List<Object> QUESTS, int numberOfChooseQuests) {
        List<Object> CHOOSE_QUESTS = new ArrayList<>();
        List<Integer> randomIndex = new ArrayList<>();
        numberOfChooseQuests = Math.min(numberOfChooseQuests, QUESTS.size());

        for (int i = 0; i < numberOfChooseQuests; i++) {
            int random = (int) (Math.random() * QUESTS.size());
            while (randomIndex.contains(random)) {
                random = (int) (Math.random() * QUESTS.size());
            }
            randomIndex.add(random);
        }

        for (int j = 0; j < randomIndex.size(); j++) {
            CHOOSE_QUESTS.add(QUESTS.get(randomIndex.get(j)));
        }

        return CHOOSE_QUESTS;
    }

}
