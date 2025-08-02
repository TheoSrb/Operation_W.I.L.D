package net.tiew.operationWild.effect.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VenomEffect extends MobEffect {

    public VenomEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), 1.0F);
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
