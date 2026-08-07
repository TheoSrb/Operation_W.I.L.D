package net.tiew.operationWild.entity.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.AnimationState;
import net.tiew.operationWild.entity.OWEntity;

public abstract class OWComboModel<T extends OWEntity> extends HierarchicalModel<T> {

    public static final int COMBO_COUNT = 3;

    protected abstract AnimationDefinition comboAnimation(int index);

    protected float comboSpeed(int index) {
        return 1.0f;
    }

    protected final void animateCombos(OWEntity entity, float ageInTicks) {
        animateCombo(entity, 1, entity.attack1Combo, ageInTicks);
        animateCombo(entity, 2, entity.attack2Combo, ageInTicks);
        animateCombo(entity, 3, entity.attack3Combo, ageInTicks);
    }

    private void animateCombo(OWEntity entity, int index, AnimationState state, float ageInTicks) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return;
        if (!entity.isCombo(index) && !state.isStarted()) return;
        this.animate(state, animation, ageInTicks, comboSpeed(index) * OWEntity.comboSpeedMultiplier);
    }

    public final float comboDurationTicks(int index) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return 0f;
        float speed = comboSpeed(index) * OWEntity.comboSpeedMultiplier;
        return speed <= 0f ? 0f : animation.lengthInSeconds() * 20f / speed;
    }
}
