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
        if (hp > MIN_HEALTH) {
            // On borne les dégâts pour ne JAMAIS descendre sous MIN_HEALTH PV → le Venin
            // affaiblit mais ne peut pas tuer (ni amener trop bas).
            float dmg = Math.min(1.0F, hp - MIN_HEALTH);
            entity.hurt(entity.damageSources().magic(), dmg);
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
