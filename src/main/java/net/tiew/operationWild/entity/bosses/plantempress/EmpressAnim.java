package net.tiew.operationWild.entity.bosses.plantempress;

public enum EmpressAnim {
    IDLE("MISC_IDLE"),
    IDLE_RAGE("MISC_IDLE_RAGE"),
    SPAWN("SPAWN"),
    DEATH("DEATH"),
    HEAD_2_DEAD("HEAD_2_DEAD"),
    HEAD_3_DEAD("HEAD_3_DEAD"),
    HEAD_2_DEAD_LOOP("HEAD_2_DEAD_LOOP"),
    HEAD_3_DEAD_LOOP("HEAD_3_DEAD_LOOP"),
    PAIN_ROAR_2("PAIN_ROAR_HEAD_2"),
    PAIN_ROAR_3("PAIN_ROAR_HEAD_3"),
    HURT_1("HURT_HEAD_1"),
    HURT_2("HURT_HEAD_2"),
    HURT_3("HURT_HEAD_3"),
    STRETCH("IDLE_STRETCH"),
    EARTHQUAKE("ATTACK_EARTHQUAKE"),
    PLUNGE("ATTACK_PLUNGING_BITE"),
    ROOT_WHIP("ATTACK_ROOT_WHIP"),
    TRIPLE_BITE("ATTACK_TRIPLE_BITE"),
    SPORE_SPIT("ATTACK_SPORE_SPIT"),
    LUNGE_2("ATTACK_LUNGE_HEAD_2"),
    LUNGE_3("ATTACK_LUNGE_HEAD_3"),
    STAGGER_1("STAGGER_HEAD_1"),
    STAGGER_2("STAGGER_HEAD_2"),
    STAGGER_3("STAGGER_HEAD_3"),
    VINE_CAST("ATTACK_VINE_CAST"),
    CARCASS_2("ATTACK_CARCASS_SLAM_2"),
    CARCASS_3("ATTACK_CARCASS_SLAM_3"),
    DESPERATION("DESPERATION_BLOOM"),
    BITE_1("ATTACK_BITE_HEAD_1"),
    BITE_2("ATTACK_BITE_HEAD_2"),
    BITE_3("ATTACK_BITE_HEAD_3"),
    CHEW_1("ATTACK_CHEW_HEAD_1"),
    CHEW_2("ATTACK_CHEW_HEAD_2"),
    CHEW_3("ATTACK_CHEW_HEAD_3"),
    STEMS_DIG("STEMS_DIG"),
    STEMS_LASH("STEMS_LASH");

    private static final EmpressAnim[] VALUES = values();

    public final String key;

    EmpressAnim(String key) {
        this.key = key;
    }

    public static EmpressAnim byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : null;
    }

    public static EmpressAnim lunge(int chain) {
        return chain == 1 ? LUNGE_2 : LUNGE_3;
    }

    public static EmpressAnim bite(int chain) {
        return chain == 0 ? BITE_1 : chain == 1 ? BITE_2 : BITE_3;
    }

    public static EmpressAnim chew(int chain) {
        return chain == 0 ? CHEW_1 : chain == 1 ? CHEW_2 : CHEW_3;
    }

    public static EmpressAnim stagger(int chain) {
        return chain == 0 ? STAGGER_1 : chain == 1 ? STAGGER_2 : STAGGER_3;
    }

    public static EmpressAnim hurt(int chain) {
        return chain == 0 ? HURT_1 : chain == 1 ? HURT_2 : HURT_3;
    }

    public static EmpressAnim carcass(int chain) {
        return chain == 1 ? CARCASS_2 : CARCASS_3;
    }

    public static EmpressAnim painRoar(int chain) {
        return chain == 1 ? PAIN_ROAR_2 : PAIN_ROAR_3;
    }

    public static EmpressAnim headDead(int chain) {
        return chain == 1 ? HEAD_2_DEAD : HEAD_3_DEAD;
    }

    public static EmpressAnim headDeadLoop(int chain) {
        return chain == 1 ? HEAD_2_DEAD_LOOP : HEAD_3_DEAD_LOOP;
    }
}
