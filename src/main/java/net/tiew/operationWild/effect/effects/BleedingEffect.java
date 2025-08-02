package net.tiew.operationWild.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {
    public BleedingEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), 1.0F);
        return true;
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int $$2 = 300 >> amplifier;
        if ($$2 > 0) {
            return duration % $$2 == 0;
        } else {
            return true;
        }
    }
}
