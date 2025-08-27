package net.tiew.operationWild.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class OccupiedTerritoryEffect extends MobEffect {
    public OccupiedTerritoryEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }
}
