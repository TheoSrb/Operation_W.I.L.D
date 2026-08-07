package net.tiew.operationWild.entity.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.AnimationState;
import net.tiew.operationWild.entity.OWEntity;

public abstract class OWComboModel<T extends OWEntity> extends HierarchicalModel<T> {

    public static final int COMBO_COUNT = 3;

    protected abstract AnimationDefinition comboAnimation(int index);

    /**
     * Rapport d'allure <b>entre les trois frappes</b> : la dernière plus vive que la première, par
     * exemple. Ce n'est pas la molette d'ensemble — celle-là se passe à {@link #animateCombos}.
     */
    protected float comboSpeed(int index) {
        return 1.0f;
    }

    /**
     * Joue les trois frappes du combo.
     *
     * @param speedMultiplier allure générale du combo pour ce modèle, appliquée aux trois frappes à
     *                        la fois. Deux réglages distincts valent mieux qu'un : {@link
     *                        #comboSpeed(int)} décide du <i>rythme interne</i> de l'enchaînement,
     *                        celui-ci de la vitesse à laquelle l'espèce frappe. Sans cette
     *                        séparation, « c'est trop lent » obligeait à retoucher trois nombres
     *                        <i>et</i> à recalculer leur rapport, et le rythme se perdait au passage.
     */
    protected final void animateCombos(OWEntity entity, float ageInTicks, float speedMultiplier) {
        animateCombo(entity, 1, entity.attack1Combo, ageInTicks, speedMultiplier);
        animateCombo(entity, 2, entity.attack2Combo, ageInTicks, speedMultiplier);
        animateCombo(entity, 3, entity.attack3Combo, ageInTicks, speedMultiplier);
    }

    /** Allure nominale, pour un modèle qui n'a rien à moduler. */
    protected final void animateCombos(OWEntity entity, float ageInTicks) {
        animateCombos(entity, ageInTicks, 1.0f);
    }

    private void animateCombo(OWEntity entity, int index, AnimationState state, float ageInTicks, float speedMultiplier) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return;
        if (!entity.isCombo(index) && !state.isStarted()) return;
        this.animate(state, animation, ageInTicks,
                comboSpeed(index) * speedMultiplier * OWEntity.comboSpeedMultiplier);
    }

    /**
     * Durée réelle d'une frappe, en ticks — de quoi caler les minuteurs de {@code
     * setupComboAnimations} sans les relever à la main.
     *
     * <p>Le facteur d'ensemble se passe explicitement plutôt que d'être relu quelque part : sans
     * cela, cette méthode annoncerait une durée que l'animation ne respecte plus dès qu'un modèle
     * module son allure.</p>
     */
    public final float comboDurationTicks(int index, float speedMultiplier) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return 0f;
        float speed = comboSpeed(index) * speedMultiplier * OWEntity.comboSpeedMultiplier;
        return speed <= 0f ? 0f : animation.lengthInSeconds() * 20f / speed;
    }
}
