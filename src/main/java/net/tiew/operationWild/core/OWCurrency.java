package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.OWCoinsSyncPacket;

/**
 * Monnaie "Pièces Sauvages" (Wild Coins) — un porte-monnaie <b>par joueur</b> (partagé entre tous ses pets),
 * stocké dans les données persistantes du joueur. Gagné en montant des entités (et à terme boss/quêtes),
 * dépensé pour acheter des skins.
 * <p>Côté serveur uniquement pour les mutations ; le client en a une copie via {@code OWCoinsSyncPacket}
 * (voir {@link net.tiew.operationWild.networking.ClientCoinData}).</p>
 */
public final class OWCurrency {
    private OWCurrency() {}

    public static final String WILD_COINS_KEY = "ow_wild_coins";

    public static int getWildCoins(Player player) {
        return player.getPersistentData().getInt(WILD_COINS_KEY);
    }

    public static void setWildCoins(Player player, int amount) {
        player.getPersistentData().putInt(WILD_COINS_KEY, Math.max(0, amount));
    }

    public static void addWildCoins(Player player, int amount) {
        setWildCoins(player, getWildCoins(player) + amount);
    }

    public static boolean spendWildCoins(Player player, int amount) {
        if (amount <= 0) return true;
        if (getWildCoins(player) < amount) return false;
        setWildCoins(player, getWildCoins(player) - amount);
        return true;
    }

    /** Octroie des pièces et notifie le client (avec feedback de gain). */
    public static void grantWildCoins(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        addWildCoins(player, amount);
        OWNetworkHandler.sendToClient(new OWCoinsSyncPacket(getWildCoins(player), amount), player);
    }

    /** Resynchronise le total vers le client, sans feedback de gain. */
    public static void syncWildCoins(ServerPlayer player) {
        OWNetworkHandler.sendToClient(new OWCoinsSyncPacket(getWildCoins(player), 0), player);
    }
}
