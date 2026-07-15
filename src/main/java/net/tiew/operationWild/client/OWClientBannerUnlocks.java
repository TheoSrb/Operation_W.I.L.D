package net.tiew.operationWild.client;

import net.tiew.operationWild.team.OWTeamBannerShape;

/** Miroir client des formes de bannière débloquées (bitmask), alimenté par {@code SyncBannerUnlocksPacket}. */
public final class OWClientBannerUnlocks {
    public static int mask = 0;

    private OWClientBannerUnlocks() {}

    public static boolean isUnlocked(OWTeamBannerShape s) {
        return !s.isPurchasable() || (mask & (1 << s.getId())) != 0;
    }
}
