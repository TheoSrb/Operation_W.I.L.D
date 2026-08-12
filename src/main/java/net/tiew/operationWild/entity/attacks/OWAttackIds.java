package net.tiew.operationWild.entity.attacks;

import java.util.Set;

public final class OWAttackIds {

    public static final int ATTACK_JUMP = 1;
    public static final int SHADOW_STRIKE = 2;
    public static final int MOUTH_SLAM = 3;
    public static final int PRIMAL_DIVE = 4;
    public static final int PAW_SLAM = 5;
    public static final int NAP_ULTIMATE = 6;
    public static final int TIDAL_RUSH = 7;
    public static final int BIG_MOUTH = 8;
    public static final int VENOM_FANGS = 9;
    public static final int CONSTRICT_ULTIMATE = 10;
    public static final int WHIRLWIND_FISTS = 11;
    public static final int TELLURIC_STOMP = 12;
    public static final int SHOULDER_BASH = 13;
    public static final int EARTHQUAKE = 14;
    public static final int WATER_SPRAY = 15;
    public static final int PRIMAL_ROAR = 16;
    public static final int HEAL_ORB = 17;
    public static final int LIFE_AURA = 18;

    private static final Set<Integer> ULTIMATES = Set.of(
            SHADOW_STRIKE,
            PRIMAL_DIVE,
            NAP_ULTIMATE,
            BIG_MOUTH,
            CONSTRICT_ULTIMATE,
            TELLURIC_STOMP,
            EARTHQUAKE,
            LIFE_AURA
    );

    public static boolean isUltimate(int id) {
        return ULTIMATES.contains(id);
    }

    private OWAttackIds() {
    }
}
