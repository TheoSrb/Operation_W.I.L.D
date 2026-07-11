package net.tiew.operationWild.entity.piste;

import net.tiew.operationWild.entity.OWEntity;

/**
 * Règles d'accessibilité de la Piste Sauvage, <b>partagées</b> par le serveur (validation dans le
 * packet) et le client (affichage). Centralisées ici pour garantir que les deux côtés calculent
 * exactement la même chose.
 */
public final class OWPisteRules {

    private OWPisteRules() {}

    /**
     * Accessibilité « structurelle » : le nœud n'est ni débloqué ni verrouillé à vie, et il est
     * adjacent à au moins un nœud déjà débloqué. (Ne tient pas compte du coût / niveau.)
     */
    public static boolean structurallyReachable(OWEntity entity, OWPisteGraph graph, int nodeId) {
        if (entity == null || graph == null) return false;
        if (entity.isPisteNodeUnlocked(nodeId) || entity.isPisteNodeLocked(nodeId)) return false;
        for (int unlocked : entity.getPisteUnlockedNodes()) {
            if (graph.isAdjacent(unlocked, nodeId)) return true;
        }
        return false;
    }

    /**
     * Porte de complétion : ouverte lorsqu'aucun nœud <i>non final</i> n'est plus explorable. Autrement
     * dit, tout ce qui pouvait être débloqué l'a été (les branches rendues inatteignables par un choix
     * exclusif ne sont plus accessibles, donc ne bloquent pas). Tant qu'un palier reste accessible, la
     * récompense finale demeure verrouillée.
     */
    public static boolean finalGateOpen(OWEntity entity, OWPisteGraph graph) {
        if (graph == null) return false;
        for (OWPisteNode n : graph.nodes()) {
            if (n.isRequiresAll()) continue;                       // les nœuds finaux ne se bloquent pas entre eux
            if (structurallyReachable(entity, graph, n.getId())) return false;
        }
        return true;
    }

    /**
     * Accessibilité effective : structurelle, plus la porte de complétion pour une récompense finale
     * ({@link OWPisteNode#isRequiresAll()}). Ne tient pas compte du coût / niveau (gérés à l'unlock).
     */
    public static boolean reachable(OWEntity entity, OWPisteGraph graph, OWPisteNode node) {
        if (!structurallyReachable(entity, graph, node.getId())) return false;
        return !node.isRequiresAll() || finalGateOpen(entity, graph);
    }

    /**
     * Vrai si ce nœud est une récompense finale, structurellement atteignable, mais encore bloquée
     * par la porte de complétion (utile pour l'affichage : « explore d'abord tout le reste »).
     */
    public static boolean isBlockedByGate(OWEntity entity, OWPisteGraph graph, OWPisteNode node) {
        return node.isRequiresAll()
                && structurallyReachable(entity, graph, node.getId())
                && !finalGateOpen(entity, graph);
    }
}
