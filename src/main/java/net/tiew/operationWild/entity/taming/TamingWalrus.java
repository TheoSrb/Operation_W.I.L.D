package net.tiew.operationWild.entity.taming;

import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.behavior.WalrusBehaviorHandler;

public class TamingWalrus {

    private WalrusEntity walrus;
    private WalrusBehaviorHandler walrusManagement;

    public TamingWalrus(WalrusEntity kodiak, WalrusBehaviorHandler kodiakManagement) {
        this.walrus = kodiak;
        this.walrusManagement = kodiakManagement;
    }

    public void tick() {
        handleTamingSystem();
    }

    private void handleTamingSystem() {

    }
}
