package net.tiew.operationWild.client;

import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.core.OWArena;

import java.util.ArrayList;
import java.util.List;

/**
 * Boîte aux lettres client du dernier butin de coffre d'arène reçu du serveur
 * ({@code ArenaChestRewardPacket}). L'écran d'arène la consomme pour jouer l'animation d'ouverture.
 *
 * <p>Le paquet peut arriver avant ou après le début de l'animation ; l'écran interroge donc
 * {@link #poll()} à chaque frame plutôt que de dépendre de l'ordre d'arrivée.</p>
 */
public final class OWClientArenaReward {

    /** Butin d'un coffre : palier, Pièces Sauvages et items. */
    public record Reward(OWArena.Chest chest, int coins, List<ItemStack> items) {}

    private static Reward pending = null;

    private OWClientArenaReward() {}

    /** Appelé à la réception du paquet serveur. */
    public static void deliver(OWArena.Chest chest, int coins, List<ItemStack> items) {
        pending = new Reward(chest, coins, new ArrayList<>(items));
    }

    /** Récupère et efface le butin en attente, ou {@code null} s'il n'y en a pas. */
    public static Reward poll() {
        Reward r = pending;
        pending = null;
        return r;
    }

    /** Efface un butin non consommé (fermeture d'écran, changement de tribu…). */
    public static void clear() {
        pending = null;
    }
}
