package net.tiew.operationWild.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OWCarriedCompanions {

    private static final Map<UUID, Integer> SELECTED_SLOT = new ConcurrentHashMap<>();

    private OWCarriedCompanions() {
    }

    public static int selectedSlot(Player player) {
        return SELECTED_SLOT.getOrDefault(player.getUUID(), 0);
    }

    public static void selectSlot(Player player, int slot) {
        SELECTED_SLOT.put(player.getUUID(), slot);
    }

    public static @Nullable OWEntity getCarried(@Nullable Player player) {
        if (player == null) return null;
        OWEntity fallback = null;
        int selected = selectedSlot(player);
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof IOWCarriedCompanion companion && passenger instanceof OWEntity owEntity) {
                if (companion.carrySlot() == selected) return owEntity;
                if (fallback == null) fallback = owEntity;
            }
        }
        return fallback;
    }

    public static @Nullable OWEntity getCarried(@Nullable Player player, int slot) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof IOWCarriedCompanion companion && passenger instanceof OWEntity owEntity
                    && companion.carrySlot() == slot) return owEntity;
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
