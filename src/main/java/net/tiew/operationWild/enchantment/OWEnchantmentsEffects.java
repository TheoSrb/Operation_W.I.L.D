package net.tiew.operationWild.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.enchantment.custom.*;

public class OWEnchantmentsEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_ENCHANTMENT =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, OperationWild.MOD_ID);

    public static void register(IEventBus eventBus) {
        ENTITY_ENCHANTMENT.register(eventBus);
    }

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<PutridEnchantment>> PUTRID =
            ENTITY_ENCHANTMENT.register("putrid", () -> PutridEnchantment.CODEC);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<VampirismEnchantment>> VAMPIRISM =
            ENTITY_ENCHANTMENT.register("vampirism", () -> VampirismEnchantment.CODEC);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<LightnessEnchantment>> LIGHTNESS =
            ENTITY_ENCHANTMENT.register("lightness", () -> LightnessEnchantment.CODEC);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<VenomProtectionEnchantment>> VENOM_PROTECTION =
            ENTITY_ENCHANTMENT.register("venom_protection", () -> VenomProtectionEnchantment.CODEC);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<PlantProvidenceEnchantment>> PLANT_PROVIDENCE =
            ENTITY_ENCHANTMENT.register("plant_providence", () -> PlantProvidenceEnchantment.CODEC);
}