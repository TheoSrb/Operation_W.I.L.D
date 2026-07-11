package net.tiew.operationWild.entity.piste;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class OWPisteMaze {

    private static final class Spec {
        final OWPisteNode.Type type;
        final int amount, cost, level;

        Spec(OWPisteNode.Type type, int amount, int cost, int level) {
            this.type = type;
            this.amount = amount;
            this.cost = cost;
            this.level = level;
        }
    }

    private final int spacing;
    private String art = "";
    private final Map<Character, Spec> specs = new HashMap<>();
    private final Set<Character> finals = new HashSet<>();
    private final Map<Character, OWPisteNode.Type[]> choiceTypes = new HashMap<>();
    private final Map<Character, int[]> choiceAmounts = new HashMap<>();
    private final Map<Character, int[]> attackPairs = new HashMap<>();

    private OWPisteMaze(int spacing) {
        this.spacing = spacing;
    }

    public static OWPisteMaze create(int spacing) {
        return new OWPisteMaze(spacing);
    }

    public OWPisteMaze map(String ascii) {
        this.art = ascii;
        return this;
    }

    public OWPisteMaze step(char symbol, int cost, int level) {
        specs.put(symbol, new Spec(OWPisteNode.Type.STEP, 0, cost, level));
        return this;
    }

    public OWPisteMaze xp(char symbol, int amount, int cost, int level) {
        specs.put(symbol, new Spec(OWPisteNode.Type.XP, amount, cost, level));
        return this;
    }

    public OWPisteMaze coins(char symbol, int amount, int cost, int level) {
        specs.put(symbol, new Spec(OWPisteNode.Type.COINS, amount, cost, level));
        return this;
    }

    public OWPisteMaze choice(char symbol, OWPisteNode.Type typeA, int amountA,
                              OWPisteNode.Type typeB, int amountB, int cost, int level) {
        specs.put(symbol, new Spec(typeA, amountA, cost, level));
        choiceTypes.put(symbol, new OWPisteNode.Type[]{typeA, typeB});
        choiceAmounts.put(symbol, new int[]{amountA, amountB});
        return this;
    }

    public OWPisteMaze requireAll(char symbol) {
        finals.add(symbol);
        return this;
    }

    /**
     * Déclare un symbole comme palier d'<b>attaque</b> : choix définitif entre deux attaques
     * (voir {@link OWPisteAttacks}), présenté dans une fenêtre dédiée avec image + description.
     */
    public OWPisteMaze attack(char symbol, int attackA, int attackB, int cost, int level) {
        specs.put(symbol, new Spec(OWPisteNode.Type.ATTACK, 0, cost, level));
        attackPairs.put(symbol, new int[]{attackA, attackB});
        return this;
    }

    /**
     * @deprecated Le système de « carrefour exclusif » (choisir un chemin en exclut un autre à vie)
     * a été retiré. Conservé en no-op pour compatibilité : un fork ne verrouille plus rien. Pour un
     * choix, utiliser plutôt {@link #choice(char, OWPisteNode.Type, int, OWPisteNode.Type, int, int, int)}.
     */
    @Deprecated
    public OWPisteMaze fork(char symbol) { return this; }

    private static boolean isNode(char c) {
        return c != ' ' && c != '.' && c != '-' && c != '|';
    }

    private Spec specFor(char symbol) {
        Spec s = specs.get(symbol);
        if (s != null) return s;
        if (symbol == 'S') return new Spec(OWPisteNode.Type.START, 0, 0, 0);
        return new Spec(OWPisteNode.Type.STEP, 0, 1, 0);
    }

    public OWPisteGraph build() {
        String[] rawLines = art.replace("\r", "").split("\n", -1);
        List<String> lines = new ArrayList<>();
        for (String l : rawLines) lines.add(l);
        while (!lines.isEmpty() && lines.get(0).trim().isEmpty()) lines.remove(0);
        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) lines.remove(lines.size() - 1);

        int rows = lines.size();

        Map<Long, Integer> cellToNode = new LinkedHashMap<>();
        List<int[]> nodeRC = new ArrayList<>();
        List<Character> nodeChar = new ArrayList<>();
        int startNode = -1;

        for (int r = 0; r < rows; r++) {
            String line = lines.get(r);
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                if (!isNode(ch)) continue;
                int idx = nodeRC.size();
                cellToNode.put(key(r, c), idx);
                nodeRC.add(new int[]{r, c});
                nodeChar.add(ch);
                if (ch == 'S') {
                    if (startNode != -1) throw new IllegalStateException("Piste : plusieurs départs 'S'");
                    startNode = idx;
                }
            }
        }
        if (startNode == -1) throw new IllegalStateException("Piste : aucun départ 'S'");

        Set<Long> undirected = new HashSet<>();
        List<int[]> edges = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            String line = lines.get(r);
            int prevCol = -1;
            for (int c = 0; c < line.length(); c++) {
                if (!isNode(line.charAt(c))) continue;
                if (prevCol != -1 && c - prevCol >= 2 && allEqual(line, prevCol + 1, c, '-')) {
                    addEdge(undirected, edges, cellToNode.get(key(r, prevCol)), cellToNode.get(key(r, c)));
                }
                prevCol = c;
            }
        }
        int maxCols = 0;
        for (String l : lines) maxCols = Math.max(maxCols, l.length());
        for (int c = 0; c < maxCols; c++) {
            int prevRow = -1;
            for (int r = 0; r < rows; r++) {
                char ch = charAt(lines, r, c);
                if (!isNode(ch)) continue;
                if (prevRow != -1 && r - prevRow >= 2 && allVertical(lines, prevRow + 1, r, c)) {
                    addEdge(undirected, edges, cellToNode.get(key(prevRow, c)), cellToNode.get(key(r, c)));
                }
                prevRow = r;
            }
        }

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < nodeRC.size(); i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        int[] dist = new int[nodeRC.size()];
        java.util.Arrays.fill(dist, Integer.MAX_VALUE);
        int[] id = new int[nodeRC.size()];
        java.util.Arrays.fill(id, -1);
        Queue<Integer> queue = new ArrayDeque<>();
        dist[startNode] = 0;
        id[startNode] = 0;
        queue.add(startNode);
        int nextId = 1;
        while (!queue.isEmpty()) {
            int n = queue.poll();
            for (int m : adj.get(n)) {
                if (dist[m] == Integer.MAX_VALUE) {
                    dist[m] = dist[n] + 1;
                    id[m] = nextId++;
                    queue.add(m);
                }
            }
        }
        for (int i = 0; i < nodeRC.size(); i++)
            if (id[i] == -1) {
                id[i] = nextId++;
                dist[i] = Integer.MAX_VALUE;
            }

        java.util.Random rng = new java.util.Random(art.hashCode());
        int[] nodeGapRange = {spacing - 8, spacing * 2 - 12};
        int gapMin = nodeGapRange[0], gapMax = nodeGapRange[1];
        int halfMin = Math.max(1, gapMin / 2);
        int halfSpan = Math.max(1, (gapMax - gapMin) / 2);
        int[] colX = new int[maxCols + 2];
        for (int c = 1; c < colX.length; c++) colX[c] = colX[c - 1] + halfMin + rng.nextInt(halfSpan + 1);
        int[] rowY = new int[rows + 2];
        for (int r = 1; r < rowY.length; r++) rowY[r] = rowY[r - 1] + halfMin + rng.nextInt(halfSpan + 1);

        final int JITTER = 0;

        int[] startRC = nodeRC.get(startNode);
        OWPisteGraph.Builder builder = OWPisteGraph.builder(
                colX[startRC[1]] + rng.nextInt(2 * JITTER + 1) - JITTER,
                rowY[startRC[0]] + rng.nextInt(2 * JITTER + 1) - JITTER);

        for (int i = 0; i < nodeRC.size(); i++) {
            if (i == startNode) continue;
            Spec spec = specFor(nodeChar.get(i));
            int[] rc = nodeRC.get(i);
            int jx = rng.nextInt(2 * JITTER + 1) - JITTER;
            int jy = rng.nextInt(2 * JITTER + 1) - JITTER;
            builder.node(id[i], colX[rc[1]] + jx, rowY[rc[0]] + jy, spec.type, spec.amount, spec.cost, spec.level, false);
        }

        for (int[] e : edges) {
            int a = e[0], b = e[1];
            boolean aParent = dist[a] < dist[b] || (dist[a] == dist[b] && id[a] < id[b]);
            int from = aParent ? a : b, to = aParent ? b : a;
            builder.edge(id[from], id[to]);
        }

        for (int i = 0; i < nodeRC.size(); i++) {
            if (i == startNode) continue;
            char ch = nodeChar.get(i);
            OWPisteNode.Type[] ct = choiceTypes.get(ch);
            if (ct != null) {
                int[] ca = choiceAmounts.get(ch);
                builder.choice(id[i], ct[0], ca[0], ct[1], ca[1]);
            }
            int[] ap = attackPairs.get(ch);
            if (ap != null) builder.attack(id[i], ap[0], ap[1]);
            if (finals.contains(ch)) builder.requireAll(id[i]);
        }

        return builder.build();
    }

    private static long key(int row, int col) {
        return (long) row * 100000L + col;
    }

    private static char charAt(List<String> lines, int r, int c) {
        if (r < 0 || r >= lines.size()) return ' ';
        String l = lines.get(r);
        return c < l.length() ? l.charAt(c) : ' ';
    }

    private static boolean allEqual(String line, int from, int to, char expected) {
        for (int c = from; c < to; c++) if (line.charAt(c) != expected) return false;
        return true;
    }

    private static boolean allVertical(List<String> lines, int fromRow, int toRow, int col) {
        for (int r = fromRow; r < toRow; r++) if (charAt(lines, r, col) != '|') return false;
        return true;
    }

    private static void addEdge(Set<Long> seen, List<int[]> edges, Integer a, Integer b) {
        if (a == null || b == null || a.equals(b)) return;
        long k = (long) Math.min(a, b) * 100000L + Math.max(a, b);
        if (seen.add(k)) edges.add(new int[]{a, b});
    }
}