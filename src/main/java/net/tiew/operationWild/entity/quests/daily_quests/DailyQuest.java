package net.tiew.operationWild.entity.quests.daily_quests;

public class DailyQuest {

    private int id, maxValue, difficultyLevel;
    private float reward;
    private String name;
    private boolean isLocked;
    private final DailyQuestTier tier;
    /** Quête impossible pour une entité au niveau max (ex : « passer un niveau »). */
    private boolean requiresLevelUp = false;


    public DailyQuest(int id, String name, int maxValue, int difficultyLevel, DailyQuestTier tier) {
        this.id = id;
        this.name = name;
        this.maxValue = maxValue;
        this.difficultyLevel = Math.min(Math.max(0, difficultyLevel), 5);
        this.reward = this.difficultyLevel * 4;
        this.tier = tier;
    }

    public DailyQuestTier getTier() {
        return tier;
    }

    public boolean requiresLevelUp() {
        return requiresLevelUp;
    }

    public DailyQuest setRequiresLevelUp(boolean value) {
        this.requiresLevelUp = value;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
