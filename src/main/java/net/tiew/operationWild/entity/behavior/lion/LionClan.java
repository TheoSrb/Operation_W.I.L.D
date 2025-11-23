package net.tiew.operationWild.entity.behavior.lion;

import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;

import java.util.ArrayList;
import java.util.List;

public class LionClan {

    private int clanID;
    private LionEntity alpha;
    private List<LionEntity> lionesses = new ArrayList<>();

    public static final int MAX_LIONESS_IN_CLAN = 6;

    public LionClan(int clanID, LionEntity alpha) {
        this.clanID = clanID;
        this.alpha = alpha;
        this.lionesses.clear();
    }

    public void excludeLionessFromClan(LionEntity lioness) {
        if (this.lionesses != null) {
            this.lionesses.remove(lioness);
        }
    }

    public void addLionessToClan(LionEntity lioness) {
        if (!lioness.isFemale() || this.lionesses.size() >= MAX_LIONESS_IN_CLAN || lioness.isTame()) {
            return;
        }

        if (!this.lionesses.contains(lioness)) {
            lioness.level().broadcastEntityEvent(lioness, (byte) 8);
            this.lionesses.add(lioness);
        }
    }

    public void switchAlpha(LionEntity newAlpha) {
        this.setAlpha(newAlpha);
    }

    public int generateNewClanColor() {
        int red = (int) OWUtils.generateRandomInterval(0, 255);
        int green = (int) OWUtils.generateRandomInterval(0, 255);
        int blue = (int) OWUtils.generateRandomInterval(0, 255);
        int color = (red << 16) | (green << 8) | blue;

        return color;
    }

    public int getClanID() {
        return clanID;
    }

    public LionEntity getAlpha() {
        return alpha;
    }

    public void setAlpha(LionEntity alpha) {
        this.alpha = alpha;
    }

    public List<LionEntity> getLionesses() {
        return lionesses;
    }

    public void setLionesses(List<LionEntity> lionesses) {
        this.lionesses = lionesses;
    }
}
