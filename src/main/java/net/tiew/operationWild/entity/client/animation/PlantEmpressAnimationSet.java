package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressAnim;

public final class PlantEmpressAnimationSet {

    private PlantEmpressAnimationSet() {
    }

    public static AnimationDefinition get(EmpressAnim anim) {
        return switch (anim) {
            case IDLE -> PlantEmpressAnimations.MISC_IDLE;
            case IDLE_RAGE -> PlantEmpressAnimations.MISC_IDLE_RAGE;
            case SPAWN -> PlantEmpressAnimations.SPAWN;
            case DEATH -> PlantEmpressAnimations.DEATH;
            case HEAD_2_DEAD -> PlantEmpressAnimations.HEAD_2_DEAD;
            case HEAD_3_DEAD -> PlantEmpressAnimations.HEAD_3_DEAD;
            case HEAD_2_DEAD_LOOP -> PlantEmpressAnimations.HEAD_2_DEAD_LOOP;
            case HEAD_3_DEAD_LOOP -> PlantEmpressAnimations.HEAD_3_DEAD_LOOP;
            case PAIN_ROAR_2 -> PlantEmpressAnimations.PAIN_ROAR_HEAD_2;
            case PAIN_ROAR_3 -> PlantEmpressAnimations.PAIN_ROAR_HEAD_3;
            case HURT_1 -> PlantEmpressAnimations.HURT_HEAD_1;
            case HURT_2 -> PlantEmpressAnimations.HURT_HEAD_2;
            case HURT_3 -> PlantEmpressAnimations.HURT_HEAD_3;
            case STRETCH -> PlantEmpressAnimations.IDLE_STRETCH;
            case EARTHQUAKE -> PlantEmpressAttackAnimations.ATTACK_EARTHQUAKE;
            case PLUNGE -> PlantEmpressAttackAnimations.ATTACK_PLUNGING_BITE;
            case ROOT_WHIP -> PlantEmpressAttackAnimations.ATTACK_ROOT_WHIP;
            case TRIPLE_BITE -> PlantEmpressAttackAnimations.ATTACK_TRIPLE_BITE;
            case SPORE_SPIT -> PlantEmpressAttackAnimations.ATTACK_SPORE_SPIT;
            case LUNGE_2 -> PlantEmpressAttackAnimations.ATTACK_LUNGE_HEAD_2;
            case LUNGE_3 -> PlantEmpressAttackAnimations.ATTACK_LUNGE_HEAD_3;
            case STAGGER_1 -> PlantEmpressAttackAnimations.STAGGER_HEAD_1;
            case STAGGER_2 -> PlantEmpressAttackAnimations.STAGGER_HEAD_2;
            case STAGGER_3 -> PlantEmpressAttackAnimations.STAGGER_HEAD_3;
            case VINE_CAST -> PlantEmpressAttackAnimations.ATTACK_VINE_CAST;
            case CARCASS_2 -> PlantEmpressAttackAnimations.ATTACK_CARCASS_SLAM_2;
            case CARCASS_3 -> PlantEmpressAttackAnimations.ATTACK_CARCASS_SLAM_3;
            case DESPERATION -> PlantEmpressAttackAnimations.DESPERATION_BLOOM;
            case BITE_1 -> PlantEmpressAttackAnimations.ATTACK_BITE_HEAD_1;
            case BITE_2 -> PlantEmpressAttackAnimations.ATTACK_BITE_HEAD_2;
            case BITE_3 -> PlantEmpressAttackAnimations.ATTACK_BITE_HEAD_3;
            case CHEW_1 -> PlantEmpressAttackAnimations.ATTACK_CHEW_HEAD_1;
            case CHEW_2 -> PlantEmpressAttackAnimations.ATTACK_CHEW_HEAD_2;
            case CHEW_3 -> PlantEmpressAttackAnimations.ATTACK_CHEW_HEAD_3;
            case STEMS_DIG -> PlantEmpressAttackAnimations.STEMS_DIG;
            case STEMS_LASH -> PlantEmpressAttackAnimations.STEMS_LASH;
        };
    }
}
