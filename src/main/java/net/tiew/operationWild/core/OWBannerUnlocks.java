package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.SyncBannerUnlocksPacket;
import net.tiew.operationWild.team.OWTeamBannerShape;

/**
 * Formes de bannière débloquées par le joueur (celles à prix). Stocké en bitmask dans les données
 * persistantes du joueur. Les formes gratuites ({@link OWTeamBannerShape#isPurchasable()} == false)
 * sont toujours considérées comme débloquées.
 */
public final class OWBannerUnlocks {
    public static final String KEY = "ow_banner_unlocks";

    private OWBannerUnlocks() {}

    public static int getMask(Player p) { return p.getPersistentData().getInt(KEY); }

    public static void setMask(Player p, int mask) { p.getPersistentData().putInt(KEY, mask); }

    public static boolean isUnlocked(Player p, OWTeamBannerShape s) {
        return !s.isPurchasable() || (getMask(p) & (1 << s.getId())) != 0;
    }

    public static void unlock(Player p, OWTeamBannerShape s) {
        setMask(p, getMask(p) | (1 << s.getId()));
    }

    public static void sync(ServerPlayer p) {
        OWNetworkHandler.sendToClient(new SyncBannerUnlocksPacket(getMask(p)), p);
    }
}
