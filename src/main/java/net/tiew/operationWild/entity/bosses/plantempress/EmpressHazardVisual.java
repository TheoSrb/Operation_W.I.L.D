package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.world.level.block.state.BlockState;

public interface EmpressHazardVisual {

    BlockState visualBlock();

    float visualWidth(float partialTick);

    float visualHeight(float partialTick);

    default float visualSpin(float partialTick) {
        return 0f;
    }

    default float visualRing() {
        return 0f;
    }

    default int visualCopies() {
        return 1;
    }
}
