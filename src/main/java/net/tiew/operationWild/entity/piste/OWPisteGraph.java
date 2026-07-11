package net.tiew.operationWild.entity.piste;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le tracé complet d'une Piste Sauvage (le « labyrinthe ») pour une espèce donnée.
 * Défini en dur via le {@link Builder} (choix validé : graphe codé en Java).
 *
 * <p>Le graphe est immuable une fois construit : les mutations (nœuds débloqués / verrouillés,
 * position courante) vivent uniquement sur l'entité, pas ici.</p>
 */
public class OWPisteGraph {

    private final Map<Integer, OWPisteNode> nodes;
    private final int startId;

    private OWPisteGraph(Map<Integer, OWPisteNode> nodes, int startId) {
        this.nodes = nodes;
        this.startId = startId;
    }

    public int getStartId() { return startId; }

    public OWPisteNode get(int id) { return nodes.get(id); }

    public boolean contains(int id) { return nodes.containsKey(id); }

    public Collection<OWPisteNode> nodes() { return nodes.values(); }

    /** {@code true} si une arête orientée relie {@code fromId} → {@code toId}. */
    public boolean isAdjacent(int fromId, int toId) {
        OWPisteNode from = nodes.get(fromId);
        return from != null && from.getChildren().contains(toId);
    }

    /** Renvoie les parents directs de {@code childId} (nœuds ayant une arête vers lui). */
    public List<OWPisteNode> parentsOf(int childId) {
        List<OWPisteNode> parents = new ArrayList<>();
        for (OWPisteNode n : nodes.values()) {
            if (n.getChildren().contains(childId)) parents.add(n);
        }
        return parents;
    }

    public static Builder builder(int startX, int startY) {
        return new Builder(startX, startY);
    }

    /**
     * Constructeur fluide. Exemple :
     * <pre>
     *   OWPisteGraph.builder(0, 0)          // crée le nœud 0 (START) au centre
     *       .xp(1, 40, 0, 1, 0)             // id=1, x=40,y=0, +XP, coût 1, niveau 0
     *       .edge(0, 1)
     *       .build();
     * </pre>
     * Les coordonnées sont exprimées dans un repère de « canvas » arbitraire ;
     * l'écran les recentre autour du nœud courant.
     */
    public static class Builder {
        private final Map<Integer, OWPisteNode> nodes = new LinkedHashMap<>();
        private final int startId = 0;

        private Builder(int startX, int startY) {
            nodes.put(startId, new OWPisteNode(startId, startX, startY, OWPisteNode.Type.START, 0, 0, 0, false, false));
        }

        /** Palier neutre : coûte des Empreintes pour cheminer, sans récompense. */
        public Builder step(int id, int x, int y, int cost, int requiredLevel) {
            return node(id, x, y, OWPisteNode.Type.STEP, 0, cost, requiredLevel, false);
        }

        public Builder xp(int id, int x, int y, int amount, int cost, int requiredLevel) {
            return node(id, x, y, OWPisteNode.Type.XP, amount, cost, requiredLevel, false);
        }

        public Builder coins(int id, int x, int y, int amount, int cost, int requiredLevel) {
            return node(id, x, y, OWPisteNode.Type.COINS, amount, cost, requiredLevel, false);
        }

        /** Marque un nœud comme récompense finale (accessible seulement une fois tout le reste exploré). */
        public Builder requireAll(int id) {
            OWPisteNode old = nodes.get(id);
            if (old == null) throw new IllegalArgumentException("requireAll() sur un nœud inexistant: " + id);
            OWPisteNode rebuilt = new OWPisteNode(id, old.getX(), old.getY(), old.getType(),
                    old.getRewardAmount(), old.getCost(), old.getRequiredLevel(), false, true);
            for (int c : old.getChildren()) rebuilt.addChild(c);
            for (OWPisteNode.Option o : old.getOptions()) rebuilt.addOption(o.type(), o.amount());
            nodes.put(id, rebuilt);
            return this;
        }

        /**
         * Fait de ce nœud un palier à choix définitif entre deux récompenses : à l'unlock, le joueur
         * choisit l'une des deux (aucun retour en arrière). Ne verrouille aucun chemin.
         */
        public Builder choice(int id, OWPisteNode.Type typeA, int amountA, OWPisteNode.Type typeB, int amountB) {
            OWPisteNode n = nodes.get(id);
            if (n == null) throw new IllegalArgumentException("choice() sur un nœud inexistant: " + id);
            n.addOption(typeA, amountA);
            n.addOption(typeB, amountB);
            return this;
        }

        /** Renseigne les deux attaques d'un palier ATTACK (choix définitif à l'unlock). */
        public Builder attack(int id, int attackA, int attackB) {
            OWPisteNode n = nodes.get(id);
            if (n == null) throw new IllegalArgumentException("attack() sur un nœud inexistant: " + id);
            n.addAttack(attackA);
            n.addAttack(attackB);
            return this;
        }

        public Builder node(int id, int x, int y, OWPisteNode.Type type, int amount, int cost, int requiredLevel, boolean fork) {
            if (id == startId) throw new IllegalArgumentException("id 0 réservé au nœud START");
            nodes.put(id, new OWPisteNode(id, x, y, type, amount, cost, requiredLevel, fork, false));
            return this;
        }

        public Builder edge(int fromId, int toId) {
            OWPisteNode from = nodes.get(fromId);
            if (from == null) throw new IllegalArgumentException("edge() départ inexistant: " + fromId);
            if (!nodes.containsKey(toId)) throw new IllegalArgumentException("edge() arrivée inexistante: " + toId);
            from.addChild(toId);
            return this;
        }

        public OWPisteGraph build() {
            return new OWPisteGraph(new LinkedHashMap<>(nodes), startId);
        }
    }
}
