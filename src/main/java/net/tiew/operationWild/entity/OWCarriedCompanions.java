package net.tiew.operationWild.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class OWCarriedCompanions {

    private OWCarriedCompanions() {
    }

    public static @Nullable OWEntity getCarried(@Nullable Player player) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof IOWCarriedCompanion && passenger instanceof OWEntity owEntity) return owEntity;
        }
        return null;
    }

    public static @Nullable OWEntity resolveControlledEntity(@Nullable Player player) {
        if (player == null) return null;
        if (player.getRootVehicle() instanceof OWEntity mount) return mount;
        return getCarried(player);
    }

    public static boolean isCarried(@Nullable Entity entity) {
        return entity instanceof IOWCarriedCompanion companion && companion.isCarried();
    }
}
