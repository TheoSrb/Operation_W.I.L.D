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

    public static class Kangaroo {
        /** Durée max de la Tornade de Poings (maintien clic droit) : 15 s. */
        public static final int WHIRLWIND_MAX_DURATION_TICKS = 300;
        /** Cooldown appliqué après usage : 35 s. */
        public static final int WHIRLWIND_COOLDOWN_TICKS = 700;
        /** Seuil de déclenchement du cooldown : il faut avoir tourné ≥ 3 s consécutives. */
        public static final int WHIRLWIND_COOLDOWN_THRESHOLD_TICKS = 60;
        /** Avant 0,5 s : aucun dégât. */
        public static final int WHIRLWIND_DAMAGE_START_TICKS = 10;
        /** Pic de dégâts atteint à 3 s (vitesse d'anim et montant des coups). */
        public static final int WHIRLWIND_DAMAGE_PEAK_TICKS = 60;
        /** Cadence des coups : un toutes les 10 ticks (0,5 s), calée sur les i-frames vanilla. */
        public static final int WHIRLWIND_DAMAGE_INTERVAL_TICKS = 10;
        /** Rayon (en blocs) devant le kangourou où les poignets touchent. */
        public static final double WHIRLWIND_RADIUS = 3.0;
        /** Cône frontal : cos de l'angle max au « devant ». 0.3 ≈ ±72° (zone avant, pas les côtés/dos). */
        public static final double WHIRLWIND_FRONT_DOT = 0.3;
        /** Dégâts par coup au démarrage (0,5 s). */
        public static final float WHIRLWIND_DAMAGE_MIN = 1.0f;
        /** Dégâts par coup au pic (3 s) — plafond pour ne pas être trop cheat. */
        public static final float WHIRLWIND_DAMAGE_MAX = 3.0f;
        /** Coût en énergie vitale (0 = gratuit, équilibré par le cooldown). */
        public static final float WHIRLWIND_ENERGY = 0f;
        /** Maintien minimal (ms) du clic avant que la tornade ne démarre : empêche le spam-clic. */
        public static final long WHIRLWIND_HOLD_TO_START_MS = 300L;
    }

    public static class Boa {
        public static final int VENOM_FANGS_MIN_DURATION_TICKS = 600;
        public static final int VENOM_FANGS_MAX_DURATION_TICKS = 1200;
        public static final int VENOM_FANGS_COOLDOWN_TICKS = 1800;
        public static final float VENOM_FANGS_ENERGY = 0f;

        public static final float  THERMAL_MAX_HP       = 150f;
        public static final double THERMAL_RANGE        = 24.0;
        public static final double THERMAL_ENGAGE_RANGE = 5.0;
        public static final double THERMAL_AIM_RADIUS   = 0.65;
        public static final float  THERMAL_REF_SIZE      = 1.5f;
        public static final float  THERMAL_SCALE_MIN     = 0.6f;
        public static final float  THERMAL_SCALE_MAX     = 1.75f;
        public static final float  THERMAL_HEART_MULT   = 1.5f;

        public static final int CONSTRICT_DURATION_TICKS = 200;
        public static final int CONSTRICT_COOLDOWN_TICKS = 400;
        public static final double CONSTRICT_ENGAGE_RANGE = 3.0;
        public static final float CONSTRICT_MAX_TARGET_WIDTH = 3f;
        public static final float CONSTRICT_MAX_TARGET_HEIGHT = 3f;
        public static final int CONSTRICT_DAMAGE_INTERVAL = 10;
        public static final float CONSTRICT_BASE_DAMAGE = 0.5f;
        public static final float CONSTRICT_MAX_DAMAGE = 3.0f;
        public static final double CONSTRICT_MOVE_SPEED = 0.2;
        public static final int CONSTRICT_APPROACH_TICKS = 20;
        public static final double CONSTRICT_HEAD_RAISE_RATIO = 0.5;
        public static final int CONSTRICT_GRAB_MAX_TIMEOUT = 160;
        public static final int CONSTRICT_GRAB_START_TIMEOUT = 60;
        public static final int CONSTRICT_STRUGGLE_REDUCTION = 12;

        public static final int    CONSTRICT_ULT_KILLS_REQUIRED = 5;
        public static final int    CONSTRICT_ULT_COOLDOWN_TICKS = 1200;
        public static final long   CONSTRICT_ULT_TARGETING_MS   = 10_000L;
        public static final double CONSTRICT_ULT_RANGE          = 5.0;
        public static final float  CONSTRICT_ULT_ENERGY         = 100f;
    }
}