package net.tiew.operationWild.enchantment;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.enchantment.custom.*;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.item.OWItems;

public class OWEnchantments {
    public static final ResourceKey<Enchantment> PUTRID = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "putrid"));
    public static final ResourceKey<Enchantment> VAMPIRISM = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "vampirism"));
    public static final ResourceKey<Enchantment> LIGHTNESS = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lightness"));
    public static final ResourceKey<Enchantment> SLIDING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sliding"));
    public static final ResourceKey<Enchantment> FAWNS_PROTECTION = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "fawns_protection"));
    public static final ResourceKey<Enchantment> VENOM_PROTECTION = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "venom_protection"));
    public static final ResourceKey<Enchantment> REPTILIAN_CALAMITY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "reptilian_calamity"));
    public static final ResourceKey<Enchantment> PLANT_PROVIDENCE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "plant_providence"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);

        register(context, PUTRID, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(OWTags.Items.ACCEPT_PUTRID),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 11),
                        Enchantment.dynamicCost(21, 11),
                        1,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new PutridEnchantment()));

        register(context, VAMPIRISM, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        7,
                        3,
                        Enchantment.dynamicCost(1, 11),
                        Enchantment.dynamicCost(21, 11),
                        1,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new VampirismEnchantment()));

        register(context, LIGHTNESS, Enchantment.enchantment(Enchantment.definition(
                        HolderSet.direct(items.getOrThrow(OWItems.PRIMITIVE_SPEAR.getKey())),
                        5,
                        2,
                        Enchantment.dynamicCost(1, 11),
                        Enchantment.dynamicCost(21, 11),
                        1,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new LightnessEnchantment()));

        register(context, SLIDING,
                Enchantment.enchantment(Enchantment.definition(
                                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                6,
                                2,
                                Enchantment.dynamicCost(8, 10),
                                Enchantment.dynamicCost(20, 10),
                                1,
                                EquipmentSlotGroup.ARMOR))
                        .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE)));

        register(context, FAWNS_PROTECTION,
                Enchantment.enchantment(Enchantment.definition(
                                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                5,
                                4,
                                Enchantment.dynamicCost(10, 8),
                                Enchantment.dynamicCost(18, 8),
                                2,
                                EquipmentSlotGroup.ARMOR
                        ))
                        .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
                        .withEffect(
                                EnchantmentEffectComponents.DAMAGE_PROTECTION,
                                new AddValue(LevelBasedValue.perLevel(0.47f)),
                                DamageSourceCondition.hasDamageSource(
                                        DamageSourcePredicate.Builder.damageType()
                                                .direct(EntityPredicate.Builder.entity()
                                                        .of(OWTags.Entities.FAWNS)
                                                )
                                )
                        )
        );

        register(context, VENOM_PROTECTION,
                Enchantment.enchantment(Enchantment.definition(
                                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                3,
                                4,
                                Enchantment.dynamicCost(10, 8),
                                Enchantment.dynamicCost(18, 8),
                                2,
                                EquipmentSlotGroup.ARMOR
                        ))
                        .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                        .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                                EnchantmentTarget.VICTIM, new VenomProtectionEnchantment()));

        register(context, REPTILIAN_CALAMITY,
                Enchantment.enchantment(Enchantment.definition(
                                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                5,
                                5,
                                Enchantment.dynamicCost(5, 8),
                                Enchantment.dynamicCost(25, 8),
                                2,
                                new EquipmentSlotGroup[]{EquipmentSlotGroup.MAINHAND}))
                        .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                        .withEffect(EnchantmentEffectComponents.DAMAGE,
                                new AddValue(LevelBasedValue.perLevel(0.75F)),
                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity().entityType(
                                                EntityTypePredicate.of(
                                                        OWTags.Entities.REPTILES)))));


        register(context, PLANT_PROVIDENCE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(OWTags.Items.ACCEPT_EFFECTIVE_WEEDING),
                        4,
                        2,
                        Enchantment.dynamicCost(1, 11),
                        Enchantment.dynamicCost(21, 11),
                        1,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new PlantProvidenceEnchantment()));



    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}