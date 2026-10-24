package net.tiew.operationWild.entity.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.tiew.operationWild.entity.OWEntity;
import org.joml.Vector3f;

public abstract class OWComboModel<T extends OWEntity> extends HierarchicalModel<T> {

    public static final int COMBO_COUNT = 3;

    private static final Vector3f WEIGHTED_VECTOR = new Vector3f();

    protected abstract AnimationDefinition comboAnimation(int index);

    protected float comboSpeed(int index) {
        return 1.0f;
    }

    protected final void animateCombos(OWEntity entity, float ageInTicks, float speedMultiplier) {
        animateCombo(entity, 1, entity.attack1Combo, ageInTicks, speedMultiplier);
        animateCombo(entity, 2, entity.attack2Combo, ageInTicks, speedMultiplier);
        animateCombo(entity, 3, entity.attack3Combo, ageInTicks, speedMultiplier);
    }

    protected final void animateCombos(OWEntity entity, float ageInTicks) {
        animateCombos(entity, ageInTicks, 1.0f);
    }

    private void animateCombo(OWEntity entity, int index, AnimationState state, float ageInTicks, float speedMultiplier) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return;
        if (!entity.isCombo(index) && !state.isStarted()) return;
        this.animate(state, animation, ageInTicks,
                comboSpeed(index) * speedMultiplier * entity.comboSpeedMultiplier);
    }

    public final float comboDurationTicks(OWEntity entity, int index, float speedMultiplier) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return 0f;
        float speed = comboSpeed(index) * speedMultiplier * entity.comboSpeedMultiplier;
        return speed <= 0f ? 0f : animation.lengthInSeconds() * 20f / speed;
    }

    protected static float swimWeight(OWEntity entity, float ageInTicks) {
        float t = Mth.clamp(entity.swimPose(Mth.clamp(ageInTicks - entity.tickCount, 0f, 1f)), 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    protected final void animateSwim(OWEntity entity, AnimationDefinition swim, float ageInTicks, float weight) {
        if (weight <= 0.001f) return;
        float pt = Mth.clamp(ageInTicks - entity.tickCount, 0f, 1f);
        KeyframeAnimations.animate(this, swim, (long) entity.swimTime(pt), weight, WEIGHTED_VECTOR);
    }

    protected final void animateWeighted(AnimationState state, AnimationDefinition animation, float ageInTicks, float speed, float weight) {
        state.updateTime(ageInTicks, speed);
        if (weight <= 0.001f) return;
        state.ifStarted(s -> KeyframeAnimations.animate(this, animation, s.getAccumulatedTime(), weight, WEIGHTED_VECTOR));
    }

    protected final void animateWalkWeighted(AnimationDefinition animation, float limbSwing, float limbSwingAmount,
                                             float maxAnimationSpeed, float animationScaleFactor, float weight) {
        float amount = Math.min(limbSwingAmount * animationScaleFactor, 1.0f) * weight;
        if (amount <= 0.001f) return;
        KeyframeAnimations.animate(this, animation, (long) (limbSwing * 50.0f * maxAnimationSpeed), amount, WEIGHTED_VECTOR);
    }
}
