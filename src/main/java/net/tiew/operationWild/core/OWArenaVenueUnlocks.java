package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.SyncArenaVenueUnlocksPacket;

/**
 * Décors d'arène débloqués par le joueur, en bitmask dans ses données persistantes. Calqué sur
 * {@link OWBannerUnlocks} : les décors gratuits sont toujours considérés comme débloqués.
 */
public final class OWArenaVenueUnlocks {
    public static final String KEY = "ow_arena_venue_unlocks";

    private OWArenaVenueUnlocks() {}

    public static int getMask(Player p) { return p.getPersistentData().getInt(KEY); }

    public static void setMask(Player p, int mask) { p.getPersistentData().putInt(KEY, mask); }

    public static boolean isUnlocked(Player p, OWArenaVenue v) {
        return !v.isPurchasable() || (getMask(p) & (1 << v.getId())) != 0;
    }

    public static void unlock(Player p, OWArenaVenue v) {
        setMask(p, getMask(p) | (1 << v.getId()));
    }

    public static void sync(ServerPlayer p) {
        OWNetworkHandler.sendToClient(new SyncArenaVenueUnlocksPacket(getMask(p)), p);
    }
}
