package net.tiew.operationWild.entity.breakout;

public final class OWBreakLevel {

    public static final int NONE = -1;
    public static final int ANY = 0;
    public static final int SOFT = 1;
    public static final int WOOD = 2;
    public static final int STONE = 3;
    public static final int METAL = 4;
    public static final int OBSIDIAN = 5;

    private static final float[] ADVANTAGE = {1.0f, 1.8f, 2.8f, 4.5f, 6.5f, 9.0f};

    private OWBreakLevel() {
    }

    public static boolean canBreak(int level, int tier) {
        if (level == NONE || tier == OWBlockToughness.UNBREAKABLE || tier <= OWBlockToughness.PASSABLE) return false;
        return level == ANY || tier <= level;
    }

    public static int strength(int level) {
        return level == ANY ? OBSIDIAN + 1 : level;
    }

    public static float advantage(int level, int tier) {
        int margin = strength(level) - tier;
        if (margin < 0) return 0f;
        return ADVANTAGE[Math.min(margin, ADVANTAGE.length - 1)];
    }

    public static String name(int level) {
        return switch (level) {
            case ANY -> "any";
            case SOFT -> "soft";
            case WOOD -> "wood";
            case STONE -> "stone";
            case METAL -> "metal";
            case OBSIDIAN -> "obsidian";
            default -> "none";
        };
    }
}
