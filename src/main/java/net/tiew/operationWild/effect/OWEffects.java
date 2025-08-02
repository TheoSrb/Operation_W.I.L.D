package net.tiew.operationWild.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.effects.*;

public class OWEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, OperationWild.MOD_ID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    public static final DeferredHolder<MobEffect, MobEffect> CAMOUFLAGE_EFFECT = MOB_EFFECTS.register("camouflage", () -> new CamouflageEffect(MobEffectCategory.NEUTRAL, 0xe38d5e));
    public static final DeferredHolder<MobEffect, MobEffect> VENOM_EFFECT = MOB_EFFECTS.register("venom", () -> new VenomEffect(MobEffectCategory.HARMFUL, 0x94de6b));
    public static final DeferredHolder<MobEffect, MobEffect> FEAR_EFFECT = MOB_EFFECTS.register("fear", () -> new FearEffect(MobEffectCategory.HARMFUL, 0x373737));
    public static final DeferredHolder<MobEffect, MobEffect> WATER_PRESSURE_EFFECT = MOB_EFFECTS.register("water_pressure", () -> new WaterPressureEffect(MobEffectCategory.HARMFUL, 0x56b8ff));
    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING_EFFECT = MOB_EFFECTS.register("bleeding", () -> new BleedingEffect(MobEffectCategory.HARMFUL, 0xa7392d));
}