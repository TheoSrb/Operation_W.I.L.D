package net.tiew.operationWild.entity.platform;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IOWPlatformCarrier {

    @Nullable
    OWPlatform getPlatform();

    boolean canBuildOnPlatform(Player player);

    float platformScale();

    default double platformPoseDrop() {
        return 0.0;
    }

    default boolean platformCollidable() {
        return true;
    }

    void onPlatformChanged();

    void flushPlatform();

    void receivePlatformBlocks(OWPlatformBlocks blocks);
}
