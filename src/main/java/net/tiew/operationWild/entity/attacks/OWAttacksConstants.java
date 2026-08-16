package net.tiew.operationWild.entity.attacks;

public class OWAttacksConstants {

    public static class Tiger {
        /**
         * Recharge commune aux deux cartes secondaires : le Bond Bestial et le Rugissement Primal.
         *
         * <p>Une valeur unique, et surtout un seul compteur pour les deux. Chacune avait la sienne —
         * vingt secondes pour le bond, soixante pour le cri — et il suffisait d'un cran de molette
         * entre les deux pour n'en subir aucune : on bondissait, on retournait la carte, on
         * rugissait, et le tigre repartait comme si de rien n'était.</p>
         */
        public static final int SECONDARY_COOLDOWN_TICKS = 800;
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

        public static final long PRIMAL_ROAR_CHARGE_MS = 2000L;
        public static final int PRIMAL_ROAR_CHARGE_TICKS = (int) (PRIMAL_ROAR_CHARGE_MS / 50L);
        public static final float PRIMAL_ROAR_ENERGY = 70f;
        public static final double PRIMAL_ROAR_RADIUS = 15.0;
        public static final int PRIMAL_ROAR_FEAR_TICKS = 100;
        public static final int PRIMAL_ROAR_SLOW_TICKS = 200;

        /**
         * Images sautées de l'animation de rugissement au moment du déclenchement.
         *
         * <p>La charge laisse le tigre ramassé, dans la pose que ROAR atteint au bout de 0,4 s. Faire
         * repartir le rugissement de zéro le remettrait debout le temps d'un battement avant de le
         * recroqueviller à nouveau ; on entre donc dans l'animation là où la charge s'est arrêtée.</p>
         */
        public static final int PRIMAL_ROAR_ANIM_SKIP_TICKS = 8;
    }

    public static class Elephant {
        /**
         * Recharge commune aux deux cartes secondaires : le Coup d'Épaule et le Jet de Trompe.
         *
         * <p>Elle n'est armée que par le Coup d'Épaule — le jet, lui, se paie sur la réserve de la
         * trompe et non sur le temps. Mais elle les bloque tous les deux : sans cela, retourner la
         * carte après une charge rendait à l'éléphant l'usage immédiat de sa trompe, et le retour à
         * la charge n'attendait plus que la fin d'un compteur qu'on avait cessé de subir.</p>
         */
        public static final int SECONDARY_COOLDOWN_TICKS = 300;

        // ── Coup d'Épaule (attaque secondaire instantanée) ──────────────────────
        public static final float SHOULDER_BASH_ENERGY = 60f;

        /**
         * Le geste se joue en trois temps, et c'est ce découpage qui le rend lisible : la bête se
         * ramasse du côté opposé, se jette sur l'épaule, puis se rattrape.
         *
         * <p>L'amorce sert aussi de tampon réseau. La ruée n'est plus une impulsion unique posée par
         * le client du cavalier mais une vitesse <b>entretenue</b> des deux côtés pendant toute sa
         * durée ; il faut donc que l'état soit arrivé avant qu'elle ne commence, et un quart de
         * seconde d'anticipation y suffit largement.</p>
         */
        public static final int SHOULDER_BASH_WINDUP_TICKS = 4;
        public static final int SHOULDER_BASH_DASH_TICKS = 8;
        public static final int SHOULDER_BASH_RECOVER_TICKS = 7;
        public static final int SHOULDER_BASH_DURATION_TICKS =
                SHOULDER_BASH_WINDUP_TICKS + SHOULDER_BASH_DASH_TICKS + SHOULDER_BASH_RECOVER_TICKS;

        /**
         * Vitesse latérale maintenue pendant la ruée. Elle est réécrite à chaque tick, donc le
         * frottement n'entre pas en jeu : dix ticks à 0,26 déportent l'éléphant de deux blocs et
         * demi, l'écart demandé. Une impulsion unique, elle, s'éteignait en un demi-bloc.
         */
        public static final double SHOULDER_BASH_DASH_SPEED = 0.42;
        /** Petit sursaut vertical au départ : sans lui la masse colle au sol et l'écart ne se voit pas. */
        public static final double SHOULDER_BASH_LIFT = 0.26;
        /** Demi-largeur de la boîte de frappe, prise du côté vers lequel l'éléphant se déporte. */
        public static final double SHOULDER_BASH_RADIUS = 5.0;
        /**
         * Part des dégâts de l'éléphant infligée par le coup d'épaule.
         *
         * <p>Proportionnel et non plus forfaitaire : un chiffre fixe ignorait le niveau et les
         * statistiques de la bête, si bien qu'une jeune recrue frappait aussi fort qu'une monture
         * de fin de partie. La moitié des dégâts, c'est plus d'une frappe de combo — qui n'en vaut
         * qu'un tiers — sans approcher la démesure d'un ultime.</p>
         */
        public static final float SHOULDER_BASH_DAMAGE_RATIO = 0.5f;
        public static final float SHOULDER_BASH_KNOCKBACK = 3.4f;
        public static final double SHOULDER_BASH_KNOCKBACK_LIFT = 0.62;
        /**
         * Bornes du dosage par gabarit, plus resserrées que celles du jet d'eau.
         *
         * <p>Un coup d'épaule reste un choc physique : il doit ébranler même une grosse bête, là où
         * de l'eau se contente de la mouiller. L'écart entre une poule et un pachyderme y est donc
         * de un à six, contre un à quinze pour le jet.</p>
         */
        public static final double SHOULDER_BASH_PUSH_MIN_FACTOR = 0.25;
        public static final double SHOULDER_BASH_PUSH_MAX_FACTOR = 1.5;

        // ── Jet de Trompe (attaque secondaire alternative, maintenue) ───────────
        public static final float WATER_SPRAY_ENERGY = 0f;
        /** Contenance de la trompe, en unités de réserve. */
        public static final int WATER_SPRAY_CAPACITY = 300;
        /** Aspiration par tick au contact de l'eau : sept secondes et demie pour faire le plein. */
        public static final int WATER_SPRAY_FILL_PER_TICK = 2;
        /** Distance à laquelle la trompe atteint l'eau : il faut vraiment être au bord. */
        public static final double WATER_SPRAY_FILL_REACH = 2.5;
        /**
         * Consommation du jet : une unité tous les deux ticks, soit trente secondes de douche à
         * pleine trompe. De quoi figer une douzaine de blocs de lave par plein.
         */
        public static final int WATER_SPRAY_COST_INTERVAL = 2;
        /**
         * Fuite naturelle. Elle aussi mise à l'échelle : une trompe pleine se vide en cinq minutes,
         * comme avant l'agrandissement de la réserve.
         */
        public static final int WATER_SPRAY_LEAK_INTERVAL = 20;
        /**
         * Longueur maximale de la trajectoire, mesurée le long de la direction de tir.
         *
         * <p>C'est un plafond, pas une portée : l'arc s'arrête au premier bloc plein rencontré. Tiré
         * à plat, le jet touche le sol bien avant ; tiré vers le haut, il déroule toute sa parabole
         * et porte loin. C'est le geste de visée qui décide de l'allonge, comme avec une lance.</p>
         */
        public static final double WATER_SPRAY_RANGE = 30.0;
        /**
         * Demi-largeur du faisceau à pleine portée. Resserrée : le jet est une lance, pas une
         * douche. Il se resserre encore en approchant de la trompe, et l'émission de particules
         * reprend exactement ce profil pour que ce qu'on voit arrosé soit bien ce qui est touché.
         */
        public static final double WATER_SPRAY_RADIUS = 3.2;
        /**
         * Rayon d'extinction autour de l'arc, en blocs.
         *
         * <p>Plus généreux que le faisceau près de la trompe : viser un brasier au bloc près relève
         * de l'exercice de tir, alors qu'arroser un incendie devrait rester facile. Il reste
         * toutefois en deçà du panache sur la fin de course, pour qu'un feu s'éteigne parce qu'on
         * l'a arrosé et non parce qu'on est passé à côté.</p>
         */
        public static final double WATER_SPRAY_DOUSE_RADIUS = 1.9;
        /**
         * Affaissement du jet : la moitié de la gravité, la vitesse initiale valant un bloc par tick.
         *
         * <p>Le jet est un <b>projectile</b>. Il part droit dans l'axe de la trompe — donc vers le
         * haut si l'on vise haut — monte, culmine, puis retombe. Une valeur trop forte donnait un
         * filet qui piquait du nez dès la sortie, sans jamais s'élever : la parabole existait mais
         * son sommet était collé à la trompe.</p>
         *
         * <p>À 40° de visée, le sommet est atteint vers douze blocs et culmine à près de quatre
         * blocs au-dessus de la trompe, avant de redescendre. C'est cette courbe-là qui porte le
         * panache comme la zone touchée.</p>
         */
        public static final double WATER_SPRAY_DROOP = 0.0275;
        /**
         * Relevé de la trompe au-dessus de la ligne de visée, en degrés.
         *
         * <p>Une trompe qui pointe pile là où l'on regarde pend trop bas : elle donne l'impression
         * d'une bête qui traîne son nez par terre. Elle se tient donc un peu au-dessus.</p>
         *
         * <p>Le relevé s'applique <b>aussi</b> à la direction du jet, et c'est indispensable : les
         * gouttes partent selon l'axe réel de la trompe, si bien qu'un relevé purement décoratif
         * ferait diverger le panache de la zone réellement arrosée. Effet de bord bienvenu — même
         * en visant à plat, le jet est lobé et décrit un arc au lieu de tomber devant les pattes.</p>
         */
        public static final float WATER_SPRAY_LIFT_DEGREES = 11f;
        /** Vitesse de sortie des gouttes, en blocs par tick : vingt blocs franchis en une seconde. */
        public static final double WATER_SPRAY_LAUNCH_SPEED = 1.5;
        /**
         * Gravité de la goutte, au format attendu par {@code Particle} (qui retranche
         * {@code 0.04 × gravity} à la vitesse verticale à chaque tick).
         *
         * <p>Déduite de l'affaissement et de la vitesse de sortie plutôt que réglée à la main : la
         * parabole parcourue par les gouttes est ainsi <b>exactement</b> celle sur laquelle le
         * serveur calcule les dégâts, et un ajustement de l'une suit forcément l'autre.</p>
         */
        public static final float WATER_SPRAY_PARTICLE_GRAVITY =
                (float) (2.0 * WATER_SPRAY_DROOP * WATER_SPRAY_LAUNCH_SPEED * WATER_SPRAY_LAUNCH_SPEED / 0.04);
        /** Poussée par tick sur ce que le jet balaie : on bouscule, on ne projette pas. */
        public static final double WATER_SPRAY_PUSH = 0.42;
        /**
         * Encombrement de référence pour la poussée du jet : celui d'un joueur.
         *
         * <p>Un jet d'eau n'a aucune raison de déplacer une bête d'une tonne comme il chasse une
         * poule. La poussée se pondère donc par le volume de la cible, rapporté à ce gabarit-là.</p>
         */
        public static final double WATER_SPRAY_PUSH_REFERENCE_VOLUME = 0.65;
        /**
         * Bornes de cette pondération.
         *
         * <p>Le rapport brut des volumes va de un à plusieurs centaines entre une poule et un
         * éléphant : appliqué tel quel, il enverrait la volaille en orbite et rendrait les grosses
         * bêtes parfaitement inamovibles. On en prend la racine carrée, puis on borne.</p>
         */
        public static final double WATER_SPRAY_PUSH_MIN_FACTOR = 0.12;
        public static final double WATER_SPRAY_PUSH_MAX_FACTOR = 1.8;
        /**
         * Dégâts par salve aux créatures que l'eau brûle — blaze, enderman, golem de neige,
         * strider. Modestes mais réguliers : c'est l'arrosage qui tue, pas le premier jet.
         */
        public static final float WATER_SPRAY_SENSITIVE_DAMAGE = 1.5f;
        /** Intervalle entre deux salves de dégâts : trois par seconde, pas vingt. */
        public static final int WATER_SPRAY_DAMAGE_INTERVAL = 6;
        /**
         * Ticks de jet <b>continu</b> sur le même bloc de lave avant qu'il ne fige. Deux secteurs
         * et demi par bloc : nettoyer une coulée demande de s'y tenir, et vide la trompe.
         */
        public static final int WATER_SPRAY_LAVA_TICKS = 50;
        /** Points échantillonnés le long de l'arc : un tous les trois quarts de bloc environ. */
        public static final int WATER_SPRAY_BLOCK_STEPS = 40;

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
         * L'onde ne frappe pas, elle <b>charrie</b> : chaque impact projette la victime vers l'avant,
         * dans le sens de la marche du front. Elle retombe devant lui, il la rattrape, la relance —
         * et la promène ainsi sur toute la longueur du couloir en la punissant à chaque bond.
         *
         * <p>Le dosage se règle tout seul, et c'est ce qui rend le charriage lisible. Un bond porte
         * à cinq blocs et demi pour une vingtaine de ticks de vol, pendant que le front n'avance que
         * de trois et demi : la victime atterrit donc <b>devant</b> lui et doit attendre qu'il la
         * rejoigne. Sur trente blocs de couloir, cela fait cinq ou six reprises — pas trente.</p>
         *
         * <p>La hauteur se lit au carré de sa valeur : 0,62 donnait encore près de trois blocs, ce
         * qui expliquait qu'un premier abaissement ne se voie pas. À 0,34 on retombe sous le saut du
         * joueur. La portée, elle, est linéaire une fois divisée par la friction de l'air.</p>
         */
        public static final double SHOCKWAVE_LAUNCH = 0.34;
        public static final double SHOCKWAVE_CARRY = 0.50;
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

    public static class RedPanda {
        public static final int HEAL_SNACK_COOLDOWN_TICKS = 100;
        /**
         * Coût d'une orbe, sur une réserve de 300.
         *
         * <p>Dimensionné contre la récupération, pas dans l'absolu : le panda regagne 7 points par
         * seconde, soit 35 sur les cinq secondes de recharge. À 70, chaque jet creuse donc de 35 —
         * une poignée de soins d'affilée passe sans peine, mais un soutien continu vide la réserve
         * en une quarantaine de secondes et impose une pause. C'est le débit soutenu qui est borné,
         * pas la réaction d'urgence.</p>
         */
        public static final float HEAL_SNACK_ENERGY = 70f;
        public static final double HEAL_SNACK_RANGE = 36.0;
        public static final double HEAL_SNACK_AIM_TOLERANCE = 1.5;
        /**
         * Distance en deçà de laquelle rien n'est désignable.
         *
         * <p>La visée part des yeux du porteur, et le panda comme le porteur lui-même occupent cet
         * espace : sans plancher, l'orbe se verrouillait systématiquement sur ce qui était collé à
         * la caméra au lieu de la bête qu'on regardait.</p>
         */
        public static final double HEAL_SNACK_MIN_AIM_DISTANCE = 2.0;
        /**
         * Parts de la vie maximale rendues par un en-cas, en deux temps.
         *
         * <p>La premiere bouchee compte tout de suite, le reste vient pendant la mastication et la
         * digestion qui suit.</p>
         */
        public static final float HEAL_SNACK_INSTANT_RATIO = 0.05f;
        public static final float HEAL_SNACK_OVER_TIME_RATIO = 0.14f;
        /** Duree de la mastication. Purement visuelle : la digestion, elle, court bien plus longtemps. */
        public static final int HEAL_SNACK_FEED_TICKS = 45;
        public static final int HEAL_SNACK_OVER_TIME_TICKS = 200;
        public static final int HEAL_SNACK_OVER_TIME_INTERVAL = 10;
        public static final double HEAL_SNACK_SPEED = 0.85;
        public static final int HEAL_SNACK_MAX_LIFETIME = 200;
        public static final int HEAL_SNACK_THROW_TICKS = 26;
        public static final int HEAL_SNACK_RELEASE_TICKS = 15;

        /**
         * L'aura prend les deux tiers de la réserve : on ne la déclenche pas au passage. Épuisé, on
         * n'y a pas droit du tout, et juste après il ne reste de quoi lancer qu'une seule orbe.
         */
        public static final float FEAST_ENERGY = 210f;
        public static final int FEAST_COOLDOWN_TICKS = 1200;
        public static final int FEAST_DURATION_TICKS = 100;
        public static final long FEAST_DURATION_MS = FEAST_DURATION_TICKS * 50L;
        public static final double FEAST_RADIUS = 8.0;
        /**
         * Nombre de parts du tas, et duree au-dela de laquelle il se gate.
         *
         * <p>Une part part a chaque pulsation OU QUELQU'UN MANGE : un tas devant lequel personne ne
         * vient ne s'epuise pas. Le minuteur de peremption est la pour qu'il finisse quand meme par
         * disparaitre, faute de quoi un festin oublie resterait la jusqu'a la fin du monde.</p>
         */
        public static final int FEAST_PORTIONS = FEAST_DURATION_TICKS / 20;
        /**
         * Peremption du tas, en parts de sa propre duree.
         *
         * <p>Une fois et demie : un festin que personne ne vient manger restait sur place une
         * demi-minute, si bien que l'ultime paraissait interminable alors que son minuteur etait
         * depuis longtemps ecoule. La duree variant desormais avec la surcharge, le plafond se
         * calcule sur celle du tas et non sur une constante.</p>
         */
        public static final int FEAST_LIFETIME_FACTOR_NUM = 3;
        public static final int FEAST_LIFETIME_FACTOR_DEN = 2;
        /** Distance devant le panda ou le tas est pose. */
        public static final double FEAST_TOSS_DISTANCE = 1.6;
        /**
         * Repartition du festin : nombre de petits tas, et part du rayon laissee libre au centre.
         *
         * <p>Un tas unique sur un seul bloc ne disait rien de la portee et ne ressemblait pas a un
         * festin. La nourriture couvre desormais toute la nappe, avec un creux au milieu — c'est ce
         * vide central qui laisse la place aux convives et fait lire la chose comme une tablee.</p>
         */
        /**
         * Densite de la nappe, en emplacements par bloc carre, et plafond de securite.
         *
         * <p>Un nombre fixe ne tenait pas : la Grande Tablee elargit le rayon de trente pour cent,
         * soit deux tiers de surface en plus, et la meme quantite de nourriture s'y serait diluee au
         * point de rendre le bonus moins fourni que le festin ordinaire. Compter en densite le fait
         * suivre tout seul, ici comme pour toute portee future.</p>
         */
        public static final double FEAST_SPOT_DENSITY = 0.95;
        public static final int FEAST_SPOTS_MIN = 40;
        public static final int FEAST_SPOTS_MAX = 340;
        /**
         * Arrivee de la nourriture : duree de vol d'une piece, et retard du bord sur le centre.
         *
         * <p>Le tas surgissait d'un bloc a l'image ou les pattes s'ouvraient. Les pieces partent
         * desormais du panda, grandissent en vol et se posent de proche en proche, si bien que la
         * nappe se deroule vers l'exterieur au lieu d'apparaitre.</p>
         */
        public static final float FEAST_ARRIVAL_TICKS = 9f;
        public static final float FEAST_ARRIVAL_SPREAD = 8f;
        public static final double FEAST_SPREAD_INNER = 0.18;
        public static final double FEAST_SPREAD_OUTER = 0.94;
        /** Couronne de miettes qui marque la limite : cadence et nombre de points. */
        public static final int FEAST_EDGE_INTERVAL = 6;
        public static final int FEAST_EDGE_POINTS = 30;
        /**
         * Bruits de nappe : cadence, et pas angulaire d'un bruit au suivant.
         *
         * <p>Le point d'emission fait le tour du bord par bonds irreguliers, ce qui donne a entendre
         * l'etendue du festin. Un son cale sur le centre n'aurait rien dit de sa portee.</p>
         */
        public static final int FEAST_EDGE_SOUND_INTERVAL = 13;
        /**
         * Vagues de miettes qui partent du centre et gagnent le bord.
         *
         * <p>Deux fronts sont toujours en vol, l'un a mi-chemin quand l'autre nait : une seule onde
         * a la fois laissait de longs silences visuels entre deux passages. Elles rasent le sol et
         * n'emploient que la particule de nourriture — c'est une nappe qu'on deroule, pas un sort.</p>
         */
        /**
         * Ronde de miettes autour de chaque convive : cadence d'emission, points par tour, vitesse
         * angulaire, et cadence de recensement des betes concernees.
         */
        public static final int FEAST_SWIRL_INTERVAL = 1;
        public static final int FEAST_SWIRL_POINTS = 4;
        public static final double FEAST_SWIRL_SPEED = 0.26;
        public static final int FEAST_DINER_SCAN_INTERVAL = 5;

        public static final int FEAST_WAVE_INTERVAL = 14;
        public static final int FEAST_WAVE_COUNT = 2;
        public static final int FEAST_WAVE_TRAVEL_TICKS = FEAST_WAVE_INTERVAL * FEAST_WAVE_COUNT;
        public static final double FEAST_EDGE_SOUND_STEP = 2.399963;
        /**
         * Soin par pulsation, et la pulsation tombe chaque seconde : trois points de vie par seconde.
         *
         * <p>Forfaitaire et non proportionnel, contrairement a l'en-cas — le festin sert tout un
         * cercle pendant plusieurs secondes, l'indexer sur la vie maximale de chacun aurait remis une
         * grosse bete d'aplomb a elle seule. Il echappe pour la meme raison a la Puissance de Soin,
         * dont la moindre multiplication pesait ici sur tout le monde a la fois.</p>
         */
        public static final float FEAST_HEAL_PER_PULSE = 3f;
        public static final int FEAST_PULSE_INTERVAL = 20;
        /**
         * En-cas a servir pour armer le festin.
         *
         * <p>Les autres ultimes s'arment sur cinq victimes. Un soigneur n'en fait aucune : le sien
         * s'arme donc sur cinq betes NOURRIES, ce qui demande le meme engagement dans le meme sens.
         * La condition n'est plus une formalite toujours vraie, et la recharge redescend a la minute
         * commune au reste du mod.</p>
         */
        public static final int FEAST_SNACKS_REQUIRED = 5;
        /**
         * Surcharge : chaque en-cas servi APRES l'armement rallonge le festin d'une seconde.
         *
         * <p>Le compteur ne s'arrete donc plus a cinq. Continuer a soigner entre deux ultimes n'est
         * plus du gaspillage mais une mise de fonds — le festin passe de cinq a quinze secondes selon
         * ce qu'on a nourri avant lui.</p>
         */
        public static final int FEAST_OVERCHARGE_MAX = 10;
        public static final int FEAST_OVERCHARGE_TICKS_EACH = 20;
        /** Duree du geste de dispersion, et image a laquelle la nourriture quitte les pattes. */
        public static final int FEAST_CAST_TICKS = 30;
        public static final int FEAST_CAST_RELEASE_TICKS = 17;

        /** Rayon de la bille, en blocs. */
        public static final float HEAL_SNACK_VISUAL_RADIUS = 0.11f;

        /**
         * Gravité de l'orbe, en blocs par tick². Le quart de celle d'un objet lancé : la bille est
         * légère et plane, elle décrit un arc franc sans plonger comme une pierre.
         */
        public static final double HEAL_SNACK_GRAVITY = 0.014;
        /** Vitesse horizontale de croisière : c'est elle qui fixe la durée de vol. */
        public static final double HEAL_SNACK_FLIGHT_SPEED = 0.75;
        /** Bornes de la durée de vol, en ticks — un jet de trois blocs garde un arc lisible. */
        public static final int HEAL_SNACK_MIN_FLIGHT_TICKS = 9;
        public static final int HEAL_SNACK_MAX_FLIGHT_TICKS = 70;

        // ── Sens Vital (passif, actif seulement quand le panda roux est porté) ──
        public static final double VITAL_SENSE_RADIUS = 32.0;
        /** Hauteur du panneau au-dessus du crâne de la cible. */
        public static final double VITAL_SENSE_BAR_HEIGHT = 0.90;

        public static final float VITAL_SENSE_SIGN_SCALE = 0.021f;
        /**
         * Grossissement par bloc de distance.
         *
         * <p>Un panneau de taille fixe dans le monde rétrécit avec l'éloignement et disparaît bien
         * avant la portée du passif. Il grandit donc avec la distance, ce qui lui garde à peu près la
         * même taille à l'écran — c'est une alerte, pas un objet posé dans le décor.</p>
         */
        public static final float VITAL_SENSE_SIGN_DISTANCE_GAIN = 0.055f;

        /** Seuil d'alerte : en dessous, et en dessous seulement, le panneau apparaît. */
        public static final float VITAL_SENSE_WARNING_RATIO = 0.35f;
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