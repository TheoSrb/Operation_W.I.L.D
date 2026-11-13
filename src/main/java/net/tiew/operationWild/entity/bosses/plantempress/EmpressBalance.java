package net.tiew.operationWild.entity.bosses.plantempress;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EmpressBalance {

    public static final float DAMAGE_BITE_SIDE = 14f;
    public static final float DAMAGE_BITE_MAIN = 18f;
    public static final float DAMAGE_SLAM = 16f;
    public static final float DAMAGE_PLUNGE = 18f;
    public static final float DAMAGE_TRIPLE = 12f;
    public static final float DAMAGE_WHIP = 10f;
    public static final float DAMAGE_QUAKE_SLAM = 14f;
    public static final float DAMAGE_QUAKE_WAVE = 12f;
    public static final float DAMAGE_SPORE = 6f;
    public static final float DAMAGE_SPROUT = 3f;
    public static final float DAMAGE_CHEW = 3f;
    public static final float DAMAGE_ROOT_SPIKE = 9f;
    public static final float DAMAGE_GUARD_THORNS = 8f;
    public static final float DAMAGE_CARCASS = 14f;
    public static final float DAMAGE_POLLEN = 10f;
    public static final float DAMAGE_BURROW_ERUPTION = 12f;
    public static final float DAMAGE_SHOCKWAVE = 4f;
    public static final float DAMAGE_PUDDLE = 2f;
    public static final float DAMAGE_WALL = 3f;

    public static final float MULTIPLIER_STAGGERED = 1.5f;
    public static final float MULTIPLIER_PLANTED = 1.25f;
    public static final float MULTIPLIER_COUNTER = 1.25f;
    public static final float MULTIPLIER_VENOM = 1.5f;
    public static final float SAP_GAIN_FACTOR = 2.2f;
    public static final float SAP_MELEE_FACTOR = 1.5f;
    public static final float SAP_CRITICAL_FACTOR = 1.35f;
    public static final float SAP_COUNTER_BONUS = 0.35f;
    public static final float SAP_PLANTED_FACTOR = 1.2f;
    public static final float HEALTH_PER_EXTRA_PLAYER = 0.5f;
    public static final float AGITATION_START = 0.7f;
    public static final float AGITATION_FULL = 0.4f;
    public static final float FRENZY_START = 0.36f;
    public static final float FRENZY_FULL = 0.14f;
    public static final float REFLEX_ONE = 0.55f;
    public static final float REFLEX_TWO = 0.36f;
    public static final float REFLEX_THREE = 0.2f;
    public static final float DESPERATION_THRESHOLD = 0.25f;

    public record Move(String id, String head, int[] weights, int cooldown, float damage, String telegraph,
                       boolean blockable, boolean counterable) {}

    public static final Map<String, Move> MOVES = new LinkedHashMap<>();

    private static void move(String id, String head, int[] weights, int cooldown, float damage, String telegraph, boolean blockable, boolean counterable) {
        MOVES.put(id, new Move(id, head, weights, cooldown, damage, telegraph, blockable, counterable));
    }

    static {
        move("bite", "side", new int[]{26, 26, 26}, 70, DAMAGE_BITE_SIDE, "red", true, true);
        move("slam", "side", new int[]{11, 11, 11}, 130, DAMAGE_SLAM, "red", true, true);
        move("scissors", "both_sides", new int[]{12, 0, 0}, 170, DAMAGE_SLAM, "red", true, true);
        move("whip", "body", new int[]{14, 11, 11}, 170, DAMAGE_WHIP, "brown", false, false);
        move("spit", "main", new int[]{9, 7, 5}, 170, DAMAGE_SPORE, "green", false, true);
        move("seeds", "main", new int[]{0, 9, 9}, 190, DAMAGE_SPROUT, "green", true, true);
        move("snare", "main", new int[]{0, 14, 14}, 170, DAMAGE_CHEW, "vine", false, false);
        move("bite_main", "main", new int[]{0, 0, 22}, 60, DAMAGE_BITE_MAIN, "red", true, true);
        move("triple", "body", new int[]{0, 12, 12}, 150, DAMAGE_TRIPLE, "red", true, true);
        move("plunge", "body", new int[]{0, 0, 16}, 90, DAMAGE_PLUNGE, "red", true, true);
        move("quake", "body", new int[]{0, 0, 12}, 200, DAMAGE_QUAKE_WAVE, "brown", false, true);
        move("rain", "none", new int[]{0, 0, 8}, 240, DAMAGE_SPORE, "green", false, false);
        move("eruption", "none", new int[]{0, 0, 14}, 120, DAMAGE_ROOT_SPIKE, "brown", false, false);
        move("carcass", "dead_side", new int[]{0, 0, 14}, 100, DAMAGE_CARCASS, "red", false, false);
        move("burrow", "body", new int[]{9, 9, 9}, 520, DAMAGE_BURROW_ERUPTION, "brown", false, false);
    }

    private EmpressBalance() {
    }

    private static Move of(String candidate) {
        String id = candidate;
        if (id.equals("bite0")) id = "bite_main";
        while (!id.isEmpty() && Character.isDigit(id.charAt(id.length() - 1))) id = id.substring(0, id.length() - 1);
        Move move = MOVES.get(id);
        if (move == null) throw new IllegalArgumentException("Unknown Plant Empress move " + candidate);
        return move;
    }

    public static int weight(String candidate, int stage) {
        int[] weights = of(candidate).weights();
        return weights[Math.max(0, Math.min(weights.length - 1, stage - 1))];
    }

    public static int cooldown(String candidate) {
        return of(candidate).cooldown();
    }
}
