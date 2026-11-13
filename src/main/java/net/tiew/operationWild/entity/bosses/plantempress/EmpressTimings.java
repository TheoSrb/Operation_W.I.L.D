package net.tiew.operationWild.entity.bosses.plantempress;

public final class EmpressTimings {

    public static final int SPAWN_TICKS = 300;
    public static final int SPAWN_BURST = 150;
    public static final int SPAWN_ROAR = 226;
    public static final int SPAWN_BAR_SHOW = 180;
    public static final int SPAWN_BAR_FILL_START = 200;
    public static final int SPAWN_BAR_FILL_END = 280;

    public static final int AWAKEN_TICKS = 80;
    public static final int AWAKEN_ROAR = 36;
    public static final int AWAKEN_BAR_FILL_START = 20;
    public static final int AWAKEN_BAR_FILL_END = 70;

    public static final int HEAD_TRANSITION_TICKS = 60;
    public static final int RAGE_TRANSITION_TICKS = 90;
    public static final int TRANSITION_SHOCKWAVE = 10;
    public static final int FINAL_DEATH_TICKS = 1050;
    public static final int DISSOLVE_START = 125;
    public static final int DISSOLVE_END = 1025;

    public static final int BAR_FADE_END = 150;


    public static final int LUNGE_LOCK = 12;
    public static final int LUNGE_IMPACT = 18;
    public static final int LUNGE_PLANT_END = 48;
    public static final int LUNGE_COUNTER_START = 6;
    public static final int LUNGE_MISS_HOLD_AT = 26;
    public static final int LUNGE_MISS_HOLD = 10;

    public static final int PLUNGE_LOCK = 19;
    public static final int PLUNGE_IMPACT = 25;
    public static final int PLUNGE_PLANT_END = 38;
    public static final int PLUNGE_COUNTER_START = 12;

    public static final int TRIPLE_LOCK = 12;
    public static final int[] TRIPLE_IMPACT = {47, 20, 32};

    public static final int WHIP_WAVE_START = 26;
    public static final int WHIP_WAVE_END = 38;
    public static final float WHIP_RADIUS_START = 2.5f;
    public static final float WHIP_RADIUS_END = 12.5f;

    public static final int QUAKE_SLAM = 46;
    public static final int QUAKE_WAVE_GAP = 10;
    public static final int QUAKE_COUNTER_START = 30;

    public static final int SPIT_RELEASE = 38;
    public static final int SPIT_COUNTER_START = 28;
    public static final int CAST_RELEASE = 16;
    public static final int CARCASS_IMPACT = 24;

    public static final int STAGGER_DOWN_START = 8;
    public static final int STAGGER_DOWN_END = 74;
    public static final int STAGGER_IMMUNITY = 200;

    public static final int DESPERATION_TICKS = 160;

    public static final int COUNTER_WINDOW = 6;
    public static final int HEAD_TELL = 10;

    public static final int BITE_IMPACT_SIDE = 15;
    public static final int BITE_IMPACT_MAIN = 17;
    public static final int CHEW_GRAB = 12;
    public static final int[] CHEW_BITES = {18, 26, 34, 42};
    public static final int CHEW_RELEASE = 52;

    public static final int BURROW_DOWN = 30;
    public static final int BURROW_TRAVEL = 40;
    public static final int BURROW_HIDE = 28;
    public static final int BURROW_EMERGE = 18;
    public static final int BURROW_UNFURL = 40;
    public static final int BURROW_UP = BURROW_EMERGE + BURROW_UNFURL;
    public static final int BURROW_BURST = 30 + 40 + BURROW_EMERGE;
    public static final int BURROW_TICKS = BURROW_DOWN + BURROW_TRAVEL + BURROW_UP;

    public static int biteImpact(int chain) {
        return chain == 0 ? BITE_IMPACT_MAIN : BITE_IMPACT_SIDE;
    }

    private EmpressTimings() {
    }

    public static int counterStart(EmpressAnim anim, int chain) {
        if (anim == null) return -1;
        return switch (anim) {
            case LUNGE_2, LUNGE_3 -> LUNGE_COUNTER_START;
            case BITE_1 -> BITE_IMPACT_MAIN - 8;
            case BITE_2, BITE_3 -> BITE_IMPACT_SIDE - 8;
            case PLUNGE -> chain == 0 ? PLUNGE_COUNTER_START : -1;
            case TRIPLE_BITE -> TRIPLE_IMPACT[chain] - 8;
            case EARTHQUAKE -> chain == 0 ? QUAKE_COUNTER_START : -1;
            case SPORE_SPIT -> chain == 0 ? SPIT_COUNTER_START : -1;
            default -> -1;
        };
    }

    public static boolean inCounterWindow(EmpressAnim anim, int chain, float dt) {
        int start = counterStart(anim, chain);
        return start >= 0 && dt >= start && dt < start + COUNTER_WINDOW;
    }
}
