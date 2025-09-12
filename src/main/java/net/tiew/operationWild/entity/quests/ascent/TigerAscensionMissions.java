package net.tiew.operationWild.entity.quests.ascent;

import net.minecraft.network.chat.Component;

public enum TigerAscensionMissions {
    MISSION_1(0, Component.translatable("ascent.mission.tiger.1", 20), 20);
    /*MISSION_2(1),
    MISSION_3(2),
    MISSION_4(3),
    MISSION_5(4),
    MISSION_6(5),
    MISSION_7(6),
    MISSION_8(7),
    MISSION_9(8),
    MISSION_10(9),
    MISSION_11(10),
    MISSION_12(11);*/

    private final AscentMission mission;

    TigerAscensionMissions(int id, Component description, double maxValue) {
        this.mission = new AscentMission(id, description, maxValue);
    }

    public AscentMission getMission() {
        return mission;
    }

    public double getActualValue() {
        return mission.getActualValue();
    }

    public double getMaxValue() {
        return mission.getMaxValue();
    }

    public Component getDescription() {
        return mission.getDescription();
    }

    public int getId() {
        return mission.getId();
    }

    public void setActualValue(double actualValue) {
        mission.setActualValue(actualValue);
    }

    public void finishAscentMission() {
        mission.finishAscentMission();
    }

    public boolean isMissionIsFinished() {
        return mission.isMissionIsFinished();
    }

    public void setMissionIsFinished(boolean missionIsFinished) {
        mission.missionIsFinished = missionIsFinished;
    }
}