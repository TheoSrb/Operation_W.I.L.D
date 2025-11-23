package net.tiew.operationWild.entity.taming;

import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.behavior.lion.LionBehaviorHandler;

/**
 * This class primarily manages the taming process for the Lion.
 * It manages the taming method from start to finish.
 */

public class TamingLion {

    private LionEntity kodiak;
    private LionBehaviorHandler kodiakManagement;

    public TamingLion(LionEntity lion, LionBehaviorHandler lionBehaviorHandler) {
        this.kodiak = lion;
        this.kodiakManagement = lionBehaviorHandler;
    }

    public void tick() {
        handleTamingSystem();
    }

    private void handleTamingSystem() {

    }
}
