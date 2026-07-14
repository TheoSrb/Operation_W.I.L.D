package net.tiew.operationWild.networking;

/**
 * Cache client de l'Expérience d'Apprivoisement du joueur local, alimenté par
 * {@code OWTamingXpSyncPacket}. Lu par les écrans (Piste Sauvage) pour l'affichage et
 * la vérification des coûts. La source de vérité reste serveur (voir {@link net.tiew.operationWild.core.OWTamingXp}).
 */
public final class ClientTamingData {
    private ClientTamingData() {}

    public static double tamingXp = 0;
}
