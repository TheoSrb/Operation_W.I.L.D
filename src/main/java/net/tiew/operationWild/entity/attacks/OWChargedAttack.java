package net.tiew.operationWild.entity.attacks;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OWChargedAttack extends OWAttack {

    @FunctionalInterface
    public interface LocalEffect {
        void apply(OWEntity entity, float chargeFactor, Vec3 chargeDirection);
    }

    private final long minChargeMs;
    private final long maxChargeMs;
    private final Consumer<OWEntity> chargeStartAction;
    private final Consumer<OWEntity> chargeCancelAction;
    private final BiConsumer<OWEntity, Float> chargeReleaseAction;
    private final LocalEffect localEffect;
    private final boolean showGhost;
    private final boolean hasCameraEffect;

    // Prédicat optionnel — vérifié côté client avant de démarrer la charge.
    // Ex : entity -> !entity.isInWater() pour bloquer la charge en eau.
    private Predicate<OWEntity> canUsePredicate = null;

    public OWChargedAttack(
            int id,
            KeyMapping key,
            float energyRequired,
            int cooldownTicks,
            long minChargeMs,
            long maxChargeMs,
            Consumer<OWEntity> chargeStartAction,
            Consumer<OWEntity> chargeCancelAction,
            BiConsumer<OWEntity, Float> chargeReleaseAction,
            LocalEffect localEffect,
            boolean showGhost,
            boolean hasCameraEffect
    ) {
        super(id, key, energyRequired, e -> {}, cooldownTicks);
        this.minChargeMs = minChargeMs;
        this.maxChargeMs = maxChargeMs;
        this.chargeStartAction = chargeStartAction;
        this.chargeCancelAction = chargeCancelAction;
        this.chargeReleaseAction = chargeReleaseAction;
        this.localEffect = localEffect;
        this.showGhost = showGhost;
        this.hasCameraEffect = hasCameraEffect;
    }

    public void onChargeStart(OWEntity entity) {
        if (chargeStartAction != null) chargeStartAction.accept(entity);
    }

    public void onChargeCancel(OWEntity entity) {
        if (chargeCancelAction != null) chargeCancelAction.accept(entity);
    }

    public void onChargeRelease(OWEntity entity, float chargeFactor) {
        if (chargeReleaseAction != null) chargeReleaseAction.accept(entity, chargeFactor);
    }

    public void applyLocalEffect(OWEntity entity, float chargeFactor, Vec3 chargeDirection) {
        if (entity.getVitalEnergy() > entity.getVitalEnergyCapacity() - getEnergyRequired()) return;
        if (localEffect != null) localEffect.apply(entity, chargeFactor, chargeDirection);
    }

    public OWChargedAttack withCanUsePredicate(Predicate<OWEntity> predicate) {
        this.canUsePredicate = predicate;
        return this;
    }

    /** Retourne true si la charge peut démarrer pour cette entité (vérifié côté client). */
    public boolean canUse(OWEntity entity) {
        return canUsePredicate == null || canUsePredicate.test(entity);
    }

    public long getMinChargeMs()     { return minChargeMs; }
    public long getMaxChargeMs()     { return maxChargeMs; }
    public boolean isShowGhost()     { return showGhost; }
    public boolean hasCameraEffect() { return hasCameraEffect; }
}
