package net.tiew.operationWild.client;

import net.tiew.operationWild.core.OWArenaVenue;

/** Miroir client des décors d'arène débloqués (bitmask), alimenté par {@code SyncArenaVenueUnlocksPacket}. */
public final class OWClientArenaVenueUnlocks {
    public static int mask = 0;

    private OWClientArenaVenueUnlocks() {}

    public static boolean isUnlocked(OWArenaVenue v) {
        return !v.isPurchasable() || (mask & (1 << v.getId())) != 0;
    }
}
