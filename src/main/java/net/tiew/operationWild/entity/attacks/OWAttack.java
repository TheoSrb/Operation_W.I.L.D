package net.tiew.operationWild.entity.attacks;

import net.minecraft.client.KeyMapping;
import net.tiew.operationWild.entity.OWEntity;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class OWAttack {

    private int id;
    private KeyMapping key;
    private float energyRequired;
    private Consumer<OWEntity> action;
    private int cooldownTicks;

    // null = toujours déverrouillée ; défini via withUnlockCondition() pour les ultimes
    private Predicate<OWEntity> unlockCondition = null;
    // null = renvoie 0 ou 1 selon isUnlocked ; sinon progression [0..1] pour l'overlay
    private Function<OWEntity, Float> unlockProgressFn = null;
    // 0 = pas d'animation de drain ; sinon durée en ms de la phase active de l'ultime
    private long ultimateDurationMs = 0L;

    public OWAttack(int id, KeyMapping key, float energyRequired, Consumer<OWEntity> action, int cooldownTicks) {
        this.id = id;
        this.key = key;
        this.energyRequired = energyRequired;
        this.action = action;
        this.cooldownTicks = cooldownTicks;
    }

    public OWAttack(int id, KeyMapping key, float energyRequired, Consumer<OWEntity> action) {
        this(id, key, energyRequired, action, 0);
    }

    /**
     * Définit la condition de déverrouillage de cette attaque (marque l'attaque comme "ultime").
     * Retourne this pour le chaînage.
     */
    public OWAttack withUnlockCondition(Predicate<OWEntity> condition) {
        this.unlockCondition = condition;
        return this;
    }

    /** Retourne true si l'attaque est disponible pour cette entité (toujours vrai si pas de condition). */
    public boolean isUnlocked(OWEntity entity) {
        return unlockCondition == null || unlockCondition.test(entity);
    }

    /** Retourne true si c'est une attaque ultime (avec condition de déverrouillage). */
    public boolean isUltimate() {
        return unlockCondition != null;
    }

    /**
     * Définit la fonction de progression de déverrouillage [0..1] utilisée par l'overlay
     * pour remplir la carte progressivement (ex : kills / killsRequired).
     * Retourne this pour le chaînage.
     */
    public OWAttack withUnlockProgress(Function<OWEntity, Float> progressFn) {
        this.unlockProgressFn = progressFn;
        return this;
    }

    /**
     * Progression de déverrouillage [0..1] pour l'overlay.
     * Si aucune fonction définie : 0 si verrouillé, 1 si déverrouillé.
     */
    public float getUnlockProgress(OWEntity entity) {
        if (unlockProgressFn == null) return isUnlocked(entity) ? 1f : 0f;
        return Math.min(1f, Math.max(0f, unlockProgressFn.apply(entity)));
    }

    /**
     * Durée en millisecondes de la phase active de l'ultime (pour l'animation de drain).
     * 0 = pas d'animation de drain.
     */
    public OWAttack withUltimateDuration(long durationMs) {
        this.ultimateDurationMs = durationMs;
        return this;
    }

    public long getUltimateDurationMs() {
        return ultimateDurationMs;
    }

    public void trigger(OWEntity entity) {
        action.accept(entity);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public KeyMapping getKey() {
        return key;
    }

    public void setKey(KeyMapping key) {
        this.key = key;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public float getEnergyRequired() {
        return energyRequired;
    }

    public void setEnergyRequired(float energyRequired) {
        this.energyRequired = energyRequired;
    }
}
