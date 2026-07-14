package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWCurrency;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.piste.OWPisteGraph;
import net.tiew.operationWild.entity.piste.OWPisteGraphs;
import net.tiew.operationWild.entity.piste.OWPisteNode;
import net.tiew.operationWild.entity.piste.OWPisteRules;

/**
 * Demande d'avancement du pion vers un nœud de la Piste Sauvage. Envoyé par le client,
 * <b>entièrement validé côté serveur</b> (adjacence, coût en Expérience d'Apprivoisement, palier de niveau,
 * porte de complétion). {@code choice} = option retenue sur un palier à choix (-1 sinon).
 * Cible : l'entité chevauchée par le joueur.
 */
public record OWPisteAdvancePacket(int nodeId, int choice) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWPisteAdvancePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "piste_advance"));

    public static final StreamCodec<FriendlyByteBuf, OWPisteAdvancePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OWPisteAdvancePacket::nodeId,
                    ByteBufCodecs.VAR_INT, OWPisteAdvancePacket::choice,
                    OWPisteAdvancePacket::new
            );

    @Override
    public CustomPacketPayload.Type<OWPisteAdvancePacket> type() {
        return TYPE;
    }

    public static void handle(OWPisteAdvancePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity vehicle = player.getRootVehicle();
            if (!(vehicle instanceof OWEntity entity)) return;

            OWPisteGraph graph = OWPisteGraphs.forEntity(entity);
            if (graph == null) return;

            int targetId = packet.nodeId();
            OWPisteNode target = graph.get(targetId);
            if (target == null) return;

            // Déjà débloqué : simple repositionnement du pion le long d'un couloir déjà ouvert (gratuit).
            if (entity.isPisteNodeUnlocked(targetId)) {
                if (adjacentToUnlocked(graph, entity, targetId)) {
                    entity.setPisteCurrentNode(targetId);
                }
                return;
            }

            // Accessible ? (adjacence + porte de complétion pour la récompense finale)
            if (!OWPisteRules.reachable(entity, graph, target)) return;

            // Palier de niveau requis.
            if (entity.getLevel() < target.getRequiredLevel()) return;

            // Palier à choix (XP/Pièces ou attaques) : une option valide est obligatoire (définitif).
            boolean isAttack = target.isAttackChoice();
            if (target.hasChoice() && (packet.choice() < 0 || packet.choice() >= target.getOptions().size())) return;
            if (isAttack && (packet.choice() < 0 || packet.choice() >= target.getAttackIds().size())) return;

            // Coût en Expérience d'Apprivoisement (cagnotte par joueur, serveur-autoritaire — voir OWTamingXp).
            // Débite si assez, sinon refus. La resynchro vers le client est faite par spendTamingXp/syncTamingXp.
            if (!net.tiew.operationWild.core.OWTamingXp.spendTamingXp(player, target.getCost())) return;
            net.tiew.operationWild.core.OWTamingXp.syncTamingXp(player);

            // --- Validation OK : on applique ---
            entity.addPisteUnlockedNode(targetId);
            entity.setPisteCurrentNode(targetId);

            if (target.hasChoice()) {
                entity.setPisteChoice(targetId, packet.choice());                  // définitif, aucun retour
                OWPisteNode.Option chosen = target.getOptions().get(packet.choice());
                applyReward(entity, player, chosen.type(), chosen.amount());
            } else if (isAttack) {
                entity.setPisteChoice(targetId, packet.choice());                  // passif choisi, définitif
                if (net.tiew.operationWild.entity.attacks.OWPistePassives.DEBUG) {
                    org.slf4j.LoggerFactory.getLogger("OWPistePassives").info(
                            "[PISTE] passif choisi : node={} choice={} attackIds={} → choices='{}'",
                            targetId, packet.choice(), target.getAttackIds(), entity.getPisteChoicesRaw());
                }
            } else {
                applyReward(entity, player, target.getType(), target.getRewardAmount());
            }
            entity.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        });
    }

    /** Adjacence brute (sert au repositionnement gratuit du pion sur un nœud déjà débloqué). */
    private static boolean adjacentToUnlocked(OWPisteGraph graph, OWEntity entity, int targetId) {
        for (int unlocked : entity.getPisteUnlockedNodes()) {
            if (graph.isAdjacent(unlocked, targetId)) return true;
        }
        return false;
    }

    private static void applyReward(OWEntity entity, ServerPlayer player, OWPisteNode.Type type, int amount) {
        switch (type) {
            case XP -> {
                entity.gainLevelXp(amount);
                // Animation d'obtention (« +N »), comme la récompense d'une quête quotidienne.
                net.tiew.operationWild.networking.OWNetworkHandler.sendToClient(
                        new net.tiew.operationWild.networking.packets.to_client.OWXpGainPacket(amount, false), player);
            }
            // Les pièces déclenchent déjà l'animation via OWCurrency.grantWildCoins → OWCoinsSyncPacket.
            case COINS -> OWCurrency.grantWildCoins(player, amount);
            default -> {}
        }
    }
}
