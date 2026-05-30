package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Copie exacte d'IHurtableMultipart (Alex's Mobs).
 * Fournit les deux helpers geometriques utilises par la chaine :
 *   - calcOffsetVec : vecteur d'offset oriente (yaw + pitch)
 *   - limitAngle    : rapprochement borne d'un angle vers une cible (anti-saut)
 */
public interface IHurtableMultipart {

    void onAttackedFromServer(LivingEntity parent, float damage, DamageSource damageSource);

    default Vec3 calcOffsetVec(float offset, float xRot, float yRot) {
        // Copie EXACTE d'Alex : new Vec3(0, 0, offset).xRot(...).yRot(...)
        return new Vec3(0, 0, offset)
                .xRot(xRot * Mth.DEG_TO_RAD)
                .yRot(-yRot * Mth.DEG_TO_RAD);
    }

    /**
     * Rapproche actualRotation de intendedRotation d'au plus maxIncrement degres.
     * Copie exacte de la logique d'Alex (limitAngle).
     */
    default float limitAngle(float actualRotation, float intendedRotation, float maxIncrement) {
        float f = Mth.wrapDegrees(intendedRotation - actualRotation);
        if (f > maxIncrement) {
            f = maxIncrement;
        }
        if (f < -maxIncrement) {
            f = -maxIncrement;
        }
        float f1 = actualRotation + f;
        return f1;
    }
}