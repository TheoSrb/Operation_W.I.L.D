package net.tiew.operationWild.entity.quests.daily_quests;

public class DailyQuestRegistry {

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

    public static void run() {
        quest1 = new DailyQuest(0,"dailyQuest.0",2000, 0);
        quest2 = new DailyQuest(1,"dailyQuest.1",8, 5);
        quest3 = new DailyQuest(2,"dailyQuest.2",50, 2);
        quest4 = new DailyQuest(3,"dailyQuest.3",4, 1);
        quest5 = new DailyQuest(4,"dailyQuest.4",1, 5);
        quest6 = new DailyQuest(5,"dailyQuest.5",850, 3);
        quest7 = new DailyQuest(6,"dailyQuest.6",1200, 2);
        quest8 = new DailyQuest(7,"dailyQuest.7",10, 2);
        quest9 = new DailyQuest(8,"dailyQuest.8",85, 1);
        quest10 = new DailyQuest(9,"dailyQuest.9",3, 0);
        quest11 = new DailyQuest(10,"dailyQuest.10",2000, 5);

    }
}
