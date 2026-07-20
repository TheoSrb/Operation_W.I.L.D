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
    public record Reward(OWArena.Chest chest, int coins, List<ItemStack> items, List<Boolean> rare) {
        /** Vrai si le lot d'indice {@code i} est une trouvaille remarquable pour ce coffre. */
        public boolean isRare(int i) { return i >= 0 && i < rare.size() && rare.get(i); }
    }

    private static Reward pending = null;

    private OWClientArenaReward() {}

    /** Appelé à la réception du paquet serveur. */
    public static void deliver(OWArena.Chest chest, int coins, List<ItemStack> items, List<Boolean> rare) {
        // Les trouvailles remarquables passent en DERNIER, l'ordre d'origine étant conservé de part
        // et d'autre. La révélation garde ainsi son crescendo : les lots ordinaires défilent, puis
        // le rare arrive en bouquet final au lieu de se perdre au milieu de la grille.
        List<ItemStack> sortedItems = new ArrayList<>(items.size());
        List<Boolean> sortedRare = new ArrayList<>(rare.size());
        for (int pass = 0; pass < 2; pass++) {
            boolean wantRare = pass == 1;
            for (int i = 0; i < items.size(); i++) {
                boolean isRare = i < rare.size() && rare.get(i);
                if (isRare != wantRare) continue;
                sortedItems.add(items.get(i));
                sortedRare.add(isRare);
            }
        }
        pending = new Reward(chest, coins, sortedItems, sortedRare);
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
