package net.tiew.operationWild.entity.behavior;

import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.WalrusEntity;

/**
 * This class only manages methods and functions that are useful and necessary for the proper functioning of Walrus's artificial intelligence.
 * It is a complementary class to the latter.
 */

public class WalrusBehaviorHandler {

    private WalrusEntity walrus;

    public WalrusBehaviorHandler(WalrusEntity walrus) {
        this.walrus = walrus;
    }

    public boolean canScratch() {
        return walrus.getTarget() == null && !walrus.isNapping() && !walrus.isMoving();
    }
}
