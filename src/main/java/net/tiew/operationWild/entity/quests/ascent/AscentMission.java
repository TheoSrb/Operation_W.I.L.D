package net.tiew.operationWild.entity.quests.ascent;

import net.minecraft.network.chat.Component;

public class AscentMission {
    private int id;
    private double actualValue;
    private double maxValue;
    public boolean missionIsFinished;
    private Component description;

    public AscentMission(int id, Component description, double maxValue) {
        this.id = id;
        this.maxValue = maxValue;
        this.description = description;
        this.missionIsFinished = false;
        this.actualValue = 0;
    }

    public double getActualValue() {
        return actualValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public Component getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public void setActualValue(double actualValue) {
        if (this.missionIsFinished) return;

        this.actualValue = actualValue;
        System.out.println("Nouvelle valeur: " + this.actualValue + " / " + this.maxValue);

        if (actualValue >= maxValue) {
            finishAscentMission();
        }
    }

    public void finishAscentMission() {
        System.out.println("Quête terminée");
        this.actualValue = 0;
        this.missionIsFinished = true;
    }

    public boolean isMissionIsFinished() {
        return missionIsFinished;
    }
}