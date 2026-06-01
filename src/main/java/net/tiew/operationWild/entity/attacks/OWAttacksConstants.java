package net.tiew.operationWild.entity.attacks;

public class OWAttacksConstants {

    public static class Tiger {
        public static final int JUMP_ATTACK_COOLDOWN_TICKS = 400;
        public static final float JUMP_POWER = 1.75f;
        public static final float JUMP_ENERGY = 100f;

        public static final int SHADOW_STRIKE_KILLS_REQUIRED = 5;
        public static final int SHADOW_STRIKE_DURATION_TICKS = 200;
        public static final long SHADOW_STRIKE_DURATION_MS = SHADOW_STRIKE_DURATION_TICKS * 50L;
        public static final int SHADOW_STRIKE_COOLDOWN_TICKS = 1200;
        public static final double SHADOW_STRIKE_DAMAGE_BONUS = 0.25;
        public static final float SHADOW_STRIKE_SPEED_FACTOR = 1.15f;
        public static final float SHADOW_STRIKE_ENERGY = 100f;
        public static final float PREDATOR_THRESHOLD = 0.30f;
        public static final float PREDATOR_RADIUS = 32f;
    }

    public static class Kodiak {
        public static final int PAW_SLAM_COOLDOWN_TICKS = 800;
        public static final float PAW_SLAM_ENERGY = 100f;

        public static final int NAP_KILLS_REQUIRED = 5;
        public static final int NAP_DURATION_TICKS = 500;
        public static final long NAP_DURATION_MS = NAP_DURATION_TICKS * 50L;
        public static final int NAP_COOLDOWN_TICKS = 1200;
        public static final float NAP_ENERGY = 100f;
        public static final float BUTCHER_INSTINCT_MULTIPLIER = 1.5f;
    }

    public static class Crocodile {
        public static final int MOUTH_SLAM_COOLDOWN_TICKS = 600;
        public static final float MOUTH_SLAM_ENERGY = 100f;

        public static final int PRIMAL_DIVE_KILLS_REQUIRED = 5;
        public static final int PRIMAL_DIVE_COOLDOWN_TICKS = 1200;
        public static final long PRIMAL_DIVE_TARGETING_MS = 10_000L;
        public static final float PRIMAL_DIVE_ENERGY = 100f;
    }

    public static class Orca {
        public static final int TIDAL_RUSH_COOLDOWN_TICKS = 400;
        public static final float TIDAL_RUSH_SPEED = 3.5f;
        public static final float TIDAL_RUSH_ENERGY = 100f;

        public static final int ORCA_CALL_KILLS_REQUIRED = 5;
        public static final int ORCA_CALL_COOLDOWN_TICKS = 1200;
        public static final long ORCA_CALL_DURATION_MS = 3_000L;
        public static final float ORCA_CALL_ENERGY = 100f;
    }

    public static class Boa {
        // Toggle "Crochets Venimeux" : armé d'un clic droit, consommé au prochain coup de combo.
        public static final int   VENOM_FANGS_MIN_DURATION_TICKS = 600;   // 30 s
        public static final int   VENOM_FANGS_MAX_DURATION_TICKS = 1200;  // 60 s
        public static final int   VENOM_FANGS_COOLDOWN_TICKS      = 1800;  // 1 min 30 s
        public static final float VENOM_FANGS_ENERGY              = 0f;    // pas de coût d'énergie : équilibré par le cooldown

        // Passif "Embuscade Silencieuse" : le Boa apprivoisé devient muet (idle + pas)
        // tant qu'au moins une menace est détectée dans ce rayon.
        public static final double SILENT_AMBUSH_RADIUS         = 16.0;
        public static final int    SILENT_AMBUSH_CHECK_INTERVAL = 10;     // ticks entre deux scans (0,5 s)
    }
}