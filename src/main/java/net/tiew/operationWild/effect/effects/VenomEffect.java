package net.tiew.operationWild.effect.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VenomEffect extends MobEffect {

    public VenomEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    /** Seuil de PV sous lequel le Venin cesse d'infliger des dégâts. */
    private static final float MIN_HEALTH = 4.0f;

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        float hp = entity.getHealth();
        if (hp <= MIN_HEALTH) {
            return true; // déjà au/sous le seuil : le Venin ne touche plus
        }

        // Dégâts bornés pour laisser au moins MIN_HEALTH PV, et JAMAIS un coup létal.
        float dmg = Math.min(1.0F, hp - MIN_HEALTH);
        if (dmg > 0.0F && dmg < hp) {
            entity.hurt(entity.damageSources().magic(), dmg);
        }

        // Garde-fou dur : si pour une raison quelconque ce tick de Venin a fait passer les PV
        // sous le seuil (modificateurs, ordre des dégâts...), on les ramène au seuil. Le Venin
        // ne peut donc jamais finir une entité.
        if (entity.isAlive() && entity.getHealth() < MIN_HEALTH) {
            entity.setHealth(MIN_HEALTH);
        }
        return true;
    }

    @Override
    public void onMobHurt(LivingEntity living, int i, DamageSource damageSource, float v) {
        super.onMobHurt(living, i, damageSource, v);
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int $$2 = 200 >> amplifier;
        if ($$2 > 0) {
            return duration % $$2 == 0;
        } else {
            return true;
        }
    }
}
