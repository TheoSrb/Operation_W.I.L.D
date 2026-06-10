package net.tiew.operationWild.networking;

/**
 * Cache client de la monnaie "Pièces Sauvages" du joueur local, alimenté par {@code OWCoinsSyncPacket}.
 * Lu par les écrans (inventaire de l'entité, skins) pour l'affichage et la vérification d'achat.
 */
public final class ClientCoinData {
    private ClientCoinData() {}

    public static int wildCoins = 0;
}
