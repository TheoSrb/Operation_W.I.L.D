package net.tiew.operationWild.entity.quests.daily_quests;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class DailyQuestsDate {

    public static LocalTime timeNow = LocalTime.now();
    public static LocalDate dateNow = LocalDate.now();
    public static final LocalTime timeWanted = LocalTime.of(OWDailyQuests.DAILY_QUEST_HOUR, OWDailyQuests.DAILY_QUEST_MINUTES, OWDailyQuests.DAILY_QUEST_SECONDS);
    public static LocalDate dateWanted = loadDateSaved();

    public static boolean isAlreadyChanged = false;
    public static DailyQuest[] savedQuests = loadQuestsSaved();

    public static void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DailyQuestsDate.timeNow = LocalTime.now();
                DailyQuestsDate.dateNow = LocalDate.now();

                if (dateNow.isAfter(dateWanted)) {
                    isAlreadyChanged = false;
                    dateWanted = dateNow;
                }

                if (timeNow.isAfter(timeWanted) && (dateNow.isEqual(dateWanted) || dateNow.isAfter(dateWanted)) && !isAlreadyChanged) {
                    isAlreadyChanged = false;
                    applyChangements(DailyQuestsDate::makeSomething);
                }
            }
        }, 0, 1000);
    }

    public static void makeSomething() {
        OWDailyQuests.resetAllOWEntityQuestProgress();
        OWDailyQuests.CHOOSE_QUESTS.clear();

        savedQuests[0] = null;
        savedQuests[1] = null;
        savedQuests[2] = null;

        savedQuests[0] = OWDailyQuests.showChooseQuests(0);
        savedQuests[1] = OWDailyQuests.showChooseQuests(1);
        savedQuests[2] = OWDailyQuests.showChooseQuests(2);

        System.out.println(savedQuests[0].getId() + " " + savedQuests[1].getId() + " " + savedQuests[2].getId());

        MinecraftServer currentServer = getCurrentServer();
        if (currentServer != null) {
            for (ServerLevel level : currentServer.getAllLevels()) {
                for (Entity entity : level.getEntities().getAll()) {
                    if (entity instanceof OWEntity owEntity) {
                        owEntity.setUpdatingQuests(true);
                    }
                }
            }
        }

        saveData();
    }

    public static MinecraftServer getCurrentServer() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    public static void applyChangements(Runnable run) {
        isAlreadyChanged = true;
        dateWanted = dateNow.plusDays(1);
        run.run();
    }

    public static LocalDate loadDateSaved() {
        File file = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\dailyQuests.properties");
        try {
            if (file.exists()) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line = buffer.readLine();

                while (line != null) {
                    if (line.contains("dateWanted")) {
                        String values = line.split("=")[1];
                        String[] valueStr = values.split("-");

                        LocalDate loadedDate = LocalDate.of(Integer.parseInt(valueStr[0]), Integer.parseInt(valueStr[1]), Integer.parseInt(valueStr[2]));

                        if (loadedDate.isBefore(LocalDate.now())) {
                            isAlreadyChanged = false;
                            return LocalDate.now();
                        }

                        return loadedDate;
                    }
                    line = buffer.readLine();
                }
                buffer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isAlreadyChanged = false;
        return LocalDate.now();
    }

    public static DailyQuest[] loadQuestsSaved() {
        DailyQuest[] quests = new DailyQuest[3];
        File file = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\dailyQuests.properties");
        try {
            if (file.exists()) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line = buffer.readLine();

                while (line != null) {
                    if (line.contains("quest0")) {
                        String[] data = line.split("=")[1].split("\\|");
                        quests[0] = new DailyQuest(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    } else if (line.contains("quest1")) {
                        String[] data = line.split("=")[1].split("\\|");
                        quests[1] = new DailyQuest(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    } else if (line.contains("quest2")) {
                        String[] data = line.split("=")[1].split("\\|");
                        quests[2] = new DailyQuest(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    }
                    line = buffer.readLine();
                }
                buffer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quests;
    }

    public static void saveData() {
        File file = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\dailyQuests.properties");
        try {
            file.createNewFile();
            BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
            buffer.write("dateWanted=" + dateWanted + "\n");
            if (savedQuests[0] != null) buffer.write("quest0=" + savedQuests[0].getId() + "|" + savedQuests[0].getName() + "|" + savedQuests[0].getMaxValue() + "|" + savedQuests[0].getDifficultyLevel() + "\n");
            if (savedQuests[1] != null) buffer.write("quest1=" + savedQuests[1].getId() + "|" + savedQuests[1].getName() + "|" + savedQuests[1].getMaxValue() + "|" + savedQuests[1].getDifficultyLevel() + "\n");
            if (savedQuests[2] != null) buffer.write("quest2=" + savedQuests[2].getId() + "|" + savedQuests[2].getName() + "|" + savedQuests[2].getMaxValue() + "|" + savedQuests[2].getDifficultyLevel() + "\n");
            buffer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
