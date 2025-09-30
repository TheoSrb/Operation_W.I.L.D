package net.tiew.operationWild.entity.AI;

import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class AIKodiak {

    private KodiakEntity kodiak;
    public AIKodiakManagement aiKodiakManagement;
    private KodiakState kodiakState = KodiakState.IDLE;
    private final int UPDATE_STATE_COOLDOWN = 20;

    public enum KodiakState {
        IDLE,
        NAPPING,
        ROLLING,
        GOING_TO_CHEST,
        GOING_TO_FOOD_ITEM,
        GOING_TO_CAMPFIRE,
        GOING_TO_BEE_NEST,
    }

    public AIKodiak(KodiakEntity kodiak) {
        this.kodiak = kodiak;
        this.aiKodiakManagement = new AIKodiakManagement(kodiak, this);
    }

    public void tick() {
        if (kodiak.tickCount % this.UPDATE_STATE_COOLDOWN == 0) {
            updateState();
        }
    }

    private void updateState() {
        System.out.println("Changement d'état...");
        if (getKodiakState() == KodiakState.IDLE) {

        }
    }

    public KodiakState getKodiakState() {
        return kodiakState;
    }

    public void setKodiakState(KodiakState kodiakState) {
        this.kodiakState = kodiakState;
    }

}
