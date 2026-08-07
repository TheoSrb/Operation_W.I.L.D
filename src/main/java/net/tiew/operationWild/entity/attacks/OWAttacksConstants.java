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

    public static class Elephant {
        // ── Coup d'Épaule (attaque secondaire instantanée) ──────────────────────
        public static final int SHOULDER_BASH_COOLDOWN_TICKS = 100;
        public static final float SHOULDER_BASH_ENERGY = 60f;
        /** Poussée latérale imprimée à l'éléphant, appliquée dans le localEffect du cavalier. */
        public static final double SHOULDER_BASH_SIDE_POWER = 1.35;
        /** Petit sursaut vertical : sans lui la masse colle au sol et l'écart ne se voit pas. */
        public static final double SHOULDER_BASH_LIFT = 0.22;
        /** Demi-largeur de la boîte de frappe, prise du côté vers lequel l'éléphant se déporte. */
        public static final double SHOULDER_BASH_RADIUS = 4.0;
        public static final float SHOULDER_BASH_DAMAGE = 11f;
        public static final float SHOULDER_BASH_KNOCKBACK = 2.6f;
        /** Le geste dure ce que dure le déport ; passé ce délai l'éléphant reprend la main. */
        public static final int SHOULDER_BASH_DURATION_TICKS = 12;

        // ── Tremblement de Terre (ultime) ───────────────────────────────────────
        public static final int EARTHQUAKE_KILLS_REQUIRED = 5;
        public static final float EARTHQUAKE_ENERGY = 100f;
        public static final int EARTHQUAKE_COOLDOWN_TICKS = 1200;
        /**
         * Levée des pattes avant l'impact. L'animation {@code super_attack} dure 4,16 s et frappe
         * le sol à 3,0 s : le tick d'impact est donc calé là, pas à la fin du geste.
         */
        public static final int EARTHQUAKE_WINDUP_TICKS = 62;
        /** Durée totale du geste (4,16 s), au-delà de laquelle l'éléphant redevient mobile. */
        public static final int EARTHQUAKE_TOTAL_TICKS = 84;
        public static final long EARTHQUAKE_DURATION_MS = EARTHQUAKE_TOTAL_TICKS * 50L;
        /**
         * Le séisme n'est pas une onde qui s'éloigne, c'est une zone qui tremble.
         *
         * <p>Il a d'abord été un front de fracture partant du centre — spectaculaire une fois, mais
         * qui laissait une couronne nette et ne se passait plus rien après. Il bat maintenant : une
         * pulsation par seconde pendant quinze secondes, chacune disloquant seize blocs pris au
         * hasard dans le rayon et faisant sursauter tout ce qui s'y trouve. Le sol se ruine par
         * plaques et la menace dure, au lieu de balayer une fois et de s'éteindre.</p>
         */
        public static final int EARTHQUAKE_DURATION_TICKS = 300;
        public static final int EARTHQUAKE_PULSE_INTERVAL = 20;
        public static final double EARTHQUAKE_RADIUS = 24.0;
        public static final int EARTHQUAKE_BLOCKS_PER_PULSE = 16;
        public static final float EARTHQUAKE_PULSE_DAMAGE = 2f;
        /** Sursaut imprimé à chaque pulsation : on trébuche, on n'est pas projeté. */
        public static final double EARTHQUAKE_PULSE_HOP = 0.25;
        /** Portée de la secousse de caméra, plus large que le cratère : on la sent avant de la voir. */
        public static final double EARTHQUAKE_SHAKE_RADIUS = 40.0;
        /**
         * Amplitude de la secousse de caméra, et part qui subsiste entre deux pulsations.
         *
         * <p>Elle court sur toute la durée du séisme et n'épargne personne, cavalier compris : le
         * sol tremble sous tout le monde. Mais elle <b>bat</b> au lieu de vibrer à plat — pleine à
         * chaque effondrement, retombée au tiers juste avant le suivant. Une secousse constante
         * pendant quinze secondes se lit comme un bug d'affichage ; une secousse qui pulse se lit
         * comme un sol qui cède par à-coups.</p>
         */
        public static final float EARTHQUAKE_SHAKE_INTENSITY = 1.6f;
        public static final float EARTHQUAKE_SHAKE_FLOOR = 0.33f;

        // ── Onde de Choc (troisième frappe du combo) ────────────────────────────
        /** Avancée par tick, en blocs. Trente blocs parcourus en huit secondes et demie. */
        public static final double SHOCKWAVE_SPEED = 0.18;
        public static final double SHOCKWAVE_LENGTH = 30.0;
        /** Demi-largeur du couloir : quatre blocs de large en tout. */
        public static final double SHOCKWAVE_HALF_WIDTH = 2.0;
        /**
         * L'onde bouscule, elle ne tue pas : deux points, comme une pulsation de séisme. Sa valeur
         * est de désorganiser un groupe, pas de nettoyer un couloir de trente blocs d'un coup de
         * clic gauche.
         */
        public static final float SHOCKWAVE_DAMAGE = 2f;
        /**
         * L'envol : bas mais long. Environ un bloc de hauteur pour cinq et demi de recul, à
         * contresens de l'onde — la victime est balayée hors du couloir plus qu'elle n'est soulevée,
         * et le front poursuit sa route sans elle.
         *
         * <p>La hauteur se lit au carré de cette valeur : 0,62 donnait encore près de trois blocs,
         * ce qui expliquait qu'un premier abaissement ne se voie pas. À 0,34 on retombe sous le
         * saut du joueur. Le recul, lui, est linéaire une fois divisé par la friction de l'air —
         * d'où une portée qui suit proprement la valeur.</p>
         */
        public static final double SHOCKWAVE_LAUNCH = 0.34;
        public static final double SHOCKWAVE_PUSHBACK = 0.50;
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
        /** Rayon de recherche des cibles pendant la phase de désignation de l'ultime. */
        public static final double PRIMAL_DIVE_TARGET_RANGE = 10.0;
    }

    public static class Orca {
        public static final int TIDAL_RUSH_COOLDOWN_TICKS = 400;
        public static final float TIDAL_RUSH_SPEED = 3.5f;
        public static final float TIDAL_RUSH_ENERGY = 100f;
        /** Damage multiplier applied to the Tidal Rush when the orca is wild:
         *  the dash then acts mainly as a mobility boost rather than a heavy hit.
         *  The tamed version keeps full damage (multiplier 1.0). */
        public static final float TIDAL_RUSH_WILD_DAMAGE_MULTIPLIER = 0.3f;

        public static final int BIG_MOUTH_KILLS_REQUIRED = 5;
        public static final int BIG_MOUTH_COOLDOWN_TICKS = 1200;
        /**
         * Duree du transport dans la gueule : c'est elle que la carte du HUD fait descendre, et
         * elle doit rester le miroir exact de {@code OrcaEntity.MOUTH_HOLD_TICKS}.
         */
        public static final long BIG_MOUTH_DURATION_MS = 50_000L;
        public static final float BIG_MOUTH_ENERGY = 100f;
    }

    public static class Kangaroo {
        public static final int WHIRLWIND_MAX_DURATION_TICKS = 300;
        public static final int WHIRLWIND_COOLDOWN_TICKS = 700;
        public static final int WHIRLWIND_COOLDOWN_THRESHOLD_TICKS = 60;
        public static final int WHIRLWIND_DAMAGE_START_TICKS = 10;
        public static final int WHIRLWIND_DAMAGE_PEAK_TICKS = 60;
        public static final int WHIRLWIND_DAMAGE_INTERVAL_TICKS = 10;
        public static final double WHIRLWIND_RADIUS = 3.0;
        public static final double WHIRLWIND_FRONT_DOT = 0.3;
        public static final float WHIRLWIND_DAMAGE_MIN = 1.0f;
        public static final float WHIRLWIND_DAMAGE_MAX = 3.0f;
        public static final float WHIRLWIND_ENERGY = 0f;
        public static final long WHIRLWIND_HOLD_TO_START_MS = 120L;

        // ── Pilon Tellurique (ultime) ──────────────────────────────────────────
        public static final int    TELLURIC_STOMP_KILLS_REQUIRED   = 5;
        /** Ancrage au sol avant le bond : le kangourou s'écrase sur ses pattes et frappe le sol. */
        public static final int    TELLURIC_STOMP_WINDUP_TICKS     = 7;
        /** Réception après l'impact (client) : amorti des pattes puis retour au repos. */
        public static final int    TELLURIC_STOMP_OUTRO_TICKS      = 10;
        public static final int    TELLURIC_STOMP_COOLDOWN_TICKS   = 500;   // 25 s
        public static final float  TELLURIC_STOMP_ENERGY           = 100f;
        /** Vitesse verticale initiale du bond (blocs/tick) : gros BOOM au départ, amorti vers l'apogée. */
        public static final double TELLURIC_STOMP_LEAP_POWER       = 2.0;
        /** Élan horizontal du bond (blocs/tick, vers le yaw du rider) → trajectoire en arc. */
        public static final double TELLURIC_STOMP_LEAP_FORWARD     = 0.55;
        /** Durée de la montée du bond initial avant la suspension. */
        public static final int    TELLURIC_STOMP_LEAP_TICKS       = 11;
        /** Suspension en l'air avant le plongeon (inclut la décélération). */
        public static final int    TELLURIC_STOMP_HOVER_TICKS      = 5;
        /** Facteur d'amortissement de la vélocité par tick pendant la suspension (proche de 1 = plus doux). */
        public static final float  TELLURIC_STOMP_HOVER_DAMPING    = 0.5f;
        /** Vitesse verticale du plongeon (blocs/tick). */
        public static final double TELLURIC_STOMP_DIVE_SPEED       = 2.5;
        /** Durée de montée en puissance du plongeon (départ doux puis pleine vitesse). */
        public static final int    TELLURIC_STOMP_DIVE_RAMP_TICKS  = 3;
        /** Angle minimal du plongeon sous l'horizontale (degrés) : on suit le yaw du regard mais le
         *  pitch est borné entre cette valeur et 90° (verticale) → jamais vers le haut. */
        public static final float  TELLURIC_STOMP_MIN_DIVE_PITCH   = 45f;
        /** Sécurité : durée max du plongeon avant impact forcé. */
        public static final int    TELLURIC_STOMP_MAX_DIVE_TICKS   = 60;
        public static final double TELLURIC_STOMP_RADIUS           = 5.0;
        /** Multiplicateur de getDamage() au centre de l'impact (distance 0). */
        public static final float  TELLURIC_STOMP_DAMAGE_CENTER_MULT = 1.5f;
        /** Multiplicateur de getDamage() au bord de l'impact (distance = rayon). */
        public static final float  TELLURIC_STOMP_DAMAGE_EDGE_MULT   = 0.5f;
        /** Durée de Lenteur I appliquée aux ennemis touchés. */
        public static final int    TELLURIC_STOMP_SLOWNESS_TICKS   = 200;   // 10 s
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