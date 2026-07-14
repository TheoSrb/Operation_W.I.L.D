package net.tiew.operationWild.entity.attacks;

import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.piste.OWPisteGraph;
import net.tiew.operationWild.entity.piste.OWPisteGraphs;
import net.tiew.operationWild.entity.piste.OWPisteNode;

/**
 * Passifs débloquables sur la Piste Sauvage (paliers {@link OWPisteNode.Type#ATTACK}).
 * <p>Chaque passif a un id qui correspond à celui déclaré dans
 * {@link net.tiew.operationWild.entity.piste.OWPisteAttacks} (nom / description / image).
 * Un individu possède un passif dès qu'il a débloqué un palier de passif dont l'option choisie
 * pointe sur cet id ({@link #has}).</p>
 */
public final class OWPistePassives {

    private OWPistePassives() {}

    /** Récolte de viande accrue sur les proies tuées (même effet que l'Instinct du Boucher du Kodiak, séparé). */
    public static final int BUTCHER = 0;
    /** Désarmement de la cible après un coup réussi (comme le tigre à l'état sauvage). */
    public static final int DISARM = 1;

    /** Multiplicateur de viande crue lâchée par les proies (séparé de la valeur innée du Kodiak). */
    public static final float BUTCHER_MULTIPLIER = 1.5f;
    /** Probabilité de désarmement : 1 chance sur {@value} (identique à l'état sauvage du tigre). */
    public static final int DISARM_CHANCE = 7;

    /** DEBUG temporaire : trace la résolution des passifs dans le log. */
    public static boolean DEBUG = false;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("OWPistePassives");

    /** {@code true} si {@code entity} a débloqué le passif {@code passiveId} via sa Piste. */
    public static boolean has(OWEntity entity, int passiveId) {
        if (entity == null) return false;
        OWPisteGraph graph = OWPisteGraphs.forEntity(entity);
        if (graph == null) {
            if (DEBUG) LOG.info("[PISTE] has({}): pas de graphe pour {}", passiveId, entity.getClass().getSimpleName());
            return false;
        }
        boolean result = false;
        StringBuilder dbg = DEBUG ? new StringBuilder() : null;
        for (int nodeId : entity.getPisteUnlockedNodes()) {
            OWPisteNode node = graph.get(nodeId);
            if (node == null) continue;
            if (node.getType() != OWPisteNode.Type.ATTACK) continue;
            int chosen = entity.getPisteChoice(nodeId);
            if (dbg != null) dbg.append(" [node=").append(nodeId).append(" attackIds=").append(node.getAttackIds())
                    .append(" chosen=").append(chosen).append("]");
            if (chosen >= 0 && chosen < node.getAttackIds().size()
                    && node.getAttackIds().get(chosen) == passiveId) {
                result = true;
            }
        }
        if (DEBUG) LOG.info("[PISTE] has(passiveId={}) sur {} → {} | unlocked={} choices='{}' ATTACK:{}",
                passiveId, entity.getClass().getSimpleName(), result,
                entity.getPisteUnlockedNodes(), entity.getPisteChoicesRaw(), dbg);
        return result;
    }
}
