package net.tiew.operationWild.entity.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.AnimationState;
import net.tiew.operationWild.entity.OWEntity;

/**
 * Classe mère des modèles d'entités du mod qui enchaînent des coups de combo.
 *
 * <p>Elle porte le rendu des trois coups, autrefois recopié à l'identique dans chaque modèle — six
 * exemplaires d'une même dizaine de lignes, qui avaient fini par diverger : l'un empilait ses
 * animations, l'autre sortait au premier coup trouvé, un troisième oubliait le repli du dernier
 * coup. Un défaut corrigé quelque part ne l'était nulle part ailleurs. Il n'y en a plus qu'un
 * exemplaire, et toute espèce ajoutée par la suite en hérite sans écrire une ligne.</p>
 *
 * <h3>Le principe de l'enchaînement</h3>
 *
 * <p>Les trois coups sont <b>empilés</b>, sans sortie anticipée : les transformations d'une
 * animation s'ajoutent à la pose courante, donc la fin du coup précédent et le début du suivant se
 * mélangent d'eux-mêmes le temps de leur recouvrement. C'est ce chevauchement qui donne un
 * enchaînement continu plutôt qu'une succession de poses tranchées.</p>
 *
 * <p>Un coup continue d'être joué tant que son {@link AnimationState} vit, même si la chaîne est
 * passée au suivant — c'est le rôle du repli sur {@code isStarted()}. Sa durée de vie est réglée
 * par l'entité (cf. {@code setupComboAnimation}), et doit couvrir le geste entier plus une marge
 * pendant laquelle il tient sa pose finale.</p>
 */
public abstract class OWComboModel<T extends OWEntity> extends HierarchicalModel<T> {

    /** Nombre de coups d'une chaîne. */
    public static final int COMBO_COUNT = 3;

    /**
     * Animation du coup {@code index} (1 à {@value #COMBO_COUNT}), ou {@code null} si l'espèce n'en
     * possède pas à ce rang.
     */
    protected abstract AnimationDefinition comboAnimation(int index);

    /**
     * Vitesse de lecture propre au coup, <b>hors</b> multiplicateur global de combo, qui est appliqué
     * par-dessus. Sert à donner du caractère à la chaîne — un dernier coup plus sec que le premier.
     */
    protected float comboSpeed(int index) {
        return 1.0f;
    }

    /**
     * Joue les coups de combo de cette créature. À appeler une fois depuis {@code setupAnim}, avant
     * les animations de déplacement, qui doivent rester dessous.
     */
    protected final void animateCombos(OWEntity entity, float ageInTicks) {
        animateCombo(entity, 1, entity.attack1Combo, ageInTicks);
        animateCombo(entity, 2, entity.attack2Combo, ageInTicks);
        animateCombo(entity, 3, entity.attack3Combo, ageInTicks);
    }

    private void animateCombo(OWEntity entity, int index, AnimationState state, float ageInTicks) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return;
        // Le coup en cours, ou celui dont le geste n'est pas encore achevé.
        if (!entity.isCombo(index) && !state.isStarted()) return;
        this.animate(state, animation, ageInTicks, comboSpeed(index) * OWEntity.comboSpeedMultiplier);
    }

    /**
     * Durée réelle d'un coup, en ticks, vitesse de lecture comprise. Sert à vérifier qu'un minuteur
     * de combo couvre bien l'animation : en dessous, le geste est tranché avant sa dernière image et
     * l'enchaînement saccade.
     */
    public final float comboDurationTicks(int index) {
        AnimationDefinition animation = comboAnimation(index);
        if (animation == null) return 0f;
        float speed = comboSpeed(index) * OWEntity.comboSpeedMultiplier;
        return speed <= 0f ? 0f : animation.lengthInSeconds() * 20f / speed;
    }
}
