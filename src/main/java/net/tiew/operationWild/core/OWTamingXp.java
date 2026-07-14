package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.OWTamingXpSyncPacket;

/**
 * Expérience d'Apprivoisement — une cagnotte <b>par joueur</b> (partagée entre tous ses pets),
 * stockée dans les données persistantes du joueur (conservée à la mort / au changement de dimension
 * via {@link net.tiew.operationWild.event.ServerEvents#onPlayerClone}). Gagnée en apprivoisant et via
 * les quêtes une fois le pet au niveau max, dépensée sur la Piste Sauvage.
 * <p>Côté serveur uniquement pour les mutations ; le client en a une copie via {@link OWTamingXpSyncPacket}
 * (voir {@link net.tiew.operationWild.networking.ClientTamingData}).</p>
 */
public final class OWTamingXp {
    private OWTamingXp() {}

    public static final String KEY = "ow_taming_xp";

    public static double getTamingXp(Player player) {
        return player.getPersistentData().getDouble(KEY);
    }

    public static void setTamingXp(Player player, double amount) {
        player.getPersistentData().putDouble(KEY, Math.max(0, amount));
    }

    public static void addTamingXp(Player player, double amount) {
        setTamingXp(player, getTamingXp(player) + amount);
    }

    /** Débite {@code amount} si la cagnotte est suffisante. Retourne {@code false} (sans débiter) sinon. */
    public static boolean spendTamingXp(Player player, double amount) {
        if (amount <= 0) return true;
        if (getTamingXp(player) < amount) return false;
        setTamingXp(player, getTamingXp(player) - amount);
        return true;
    }

    /** Ajoute de l'Expérience d'Apprivoisement et resynchronise le client. */
    public static void grantTamingXp(ServerPlayer player, double amount) {
        if (amount <= 0) return;
        addTamingXp(player, amount);
        syncTamingXp(player);
    }

    /** Resynchronise le total vers le client. */
    public static void syncTamingXp(ServerPlayer player) {
        OWNetworkHandler.sendToClient(new OWTamingXpSyncPacket(getTamingXp(player)), player);
    }
}
