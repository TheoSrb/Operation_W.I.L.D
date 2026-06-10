package net.tiew.operationWild.core;

import net.minecraft.server.level.ServerPlayer;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.OWIndicationPacket;

/**
 * API d'affichage d'indications / didacticiels à l'écran d'un joueur.
 * <p>
 * Une indication est un encart centré, traduit, avec fondu d'entrée/sortie, fond stylé et son ;
 * son texte se met automatiquement sur plusieurs lignes selon la largeur. Idéal pour guider le
 * joueur (ex : première fois qu'il monte une créature, rappel de touches, etc.).
 * </p>
 *
 * <h3>Exemple d'utilisation (côté serveur)</h3>
 * <pre>{@code
 * // Quand le joueur monte une entité pour la première fois (logique serveur) :
 * if (rider instanceof ServerPlayer sp) {
 *     OWIndication.show(sp, "indication.ow.first_mount");           // durée par défaut
 *     // ou avec une durée custom (en ticks) :
 *     OWIndication.show(sp, "indication.ow.first_mount", 160);      // 8 secondes
 * }
 * }</pre>
 *
 * Le texte est défini dans les fichiers de langue (clé de traduction), donc traduit dans toutes les
 * langues. Les retours à la ligne explicites ({@code \n}) y sont supportés, et le texte est en plus
 * découpé automatiquement s'il dépasse la largeur de l'encart.
 */
public final class OWIndication {

    private OWIndication() {}

    /** Durée d'affichage par défaut (en ticks) : 6 secondes. */
    public static final int DEFAULT_DURATION_TICKS = 120;

    /** Affiche une indication au joueur pour la durée par défaut. */
    public static void show(ServerPlayer player, String translationKey) {
        show(player, translationKey, DEFAULT_DURATION_TICKS);
    }

    /** Affiche une indication au joueur pour la durée donnée (en ticks). */
    public static void show(ServerPlayer player, String translationKey, int durationTicks) {
        if (player == null || translationKey == null || translationKey.isEmpty()) return;
        OWNetworkHandler.sendToClient(new OWIndicationPacket(translationKey, durationTicks), player);
    }
}
