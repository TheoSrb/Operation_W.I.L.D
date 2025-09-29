package net.tiew.operationWild.entity.AI;

import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class KodiakAI {

    private KodiakEntity kodiak;
    private KodiakState kodiakState = KodiakState.IDLE;
    private final int UPDATE_STATE_COOLDOWN = 100;

    public enum KodiakState {
        IDLE,
        NAPPING,
        ROLLING,
        GOING_TO_CHEST,
        GOING_TO_FOOD_ITEM,
        GOING_TO_CAMPFIRE,
        GOING_TO_BEE_NEST,
    }

    public KodiakAI(KodiakEntity kodiak) {
        this.kodiak = kodiak;
    }

    public void tick() {
        if (kodiak.tickCount % this.UPDATE_STATE_COOLDOWN == 0) {
            updateState();
        }
    }

    private void updateState() {
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
