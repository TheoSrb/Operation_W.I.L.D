package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWSaddleRecipe;
import net.tiew.operationWild.core.OWSaddleRecipes;
import net.tiew.operationWild.worldgen.OWBiomeModifiers;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

public final class OWWikiCatalogSection {

    private OWWikiCatalogSection() {}

    public static JsonArray items(OWWikiLang lang) {
        JsonArray items = new JsonArray();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(OperationWild.MOD_ID)) continue;
            items.add(item(item, id, lang));
        }
        return items;
    }

    private static JsonObject item(Item item, ResourceLocation id, OWWikiLang lang) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id.toString());
        entry.addProperty("path", id.getPath());
        entry.addProperty("translation_key", item.getDescriptionId());
        entry.addProperty("class", item.getClass().getSimpleName());
        JsonObject names = lang.translations(item.getDescriptionId());
        if (names != null) entry.add("names", names);

        try {
            ItemStack stack = new ItemStack(item);
            entry.addProperty("max_stack_size", stack.getMaxStackSize());
            entry.addProperty("max_damage", stack.getMaxDamage());
            entry.addProperty("rarity", stack.getRarity().name());
            entry.addProperty("fire_resistant", stack.has(DataComponents.FIRE_RESISTANT));

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                JsonObject nutrition = new JsonObject();
                nutrition.addProperty("nutrition", food.nutrition());
                nutrition.addProperty("saturation", food.saturation());
                nutrition.addProperty("can_always_eat", food.canAlwaysEat());
                nutrition.addProperty("eat_seconds", food.eatSeconds());
                entry.add("food", nutrition);
            }
        } catch (Throwable ignored) {
        }

        if (item instanceof ArmorItem armor) {
            JsonObject armorEntry = new JsonObject();
            armorEntry.addProperty("slot", armor.getEquipmentSlot().getName());
            try {
                armorEntry.addProperty("material", armor.getMaterial().getRegisteredName());
            } catch (Throwable ignored) {
            }
            entry.add("armor", armorEntry);
        }

        if (item instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            JsonObject tool = new JsonObject();
            tool.addProperty("uses", tier.getUses());
            tool.addProperty("speed", tier.getSpeed());
            tool.addProperty("attack_damage_bonus", tier.getAttackDamageBonus());
            tool.addProperty("enchantment_value", tier.getEnchantmentValue());
            entry.add("tool", tool);
        }

        return entry;
    }

    public static JsonArray blocks(OWWikiLang lang) {
        JsonArray blocks = new JsonArray();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals(OperationWild.MOD_ID)) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("id", id.toString());
            entry.addProperty("translation_key", block.getDescriptionId());
            entry.addProperty("class", block.getClass().getSimpleName());
            JsonObject names = lang.translations(block.getDescriptionId());
            if (names != null) entry.add("names", names);
            try {
                entry.addProperty("explosion_resistance", block.getExplosionResistance());
                entry.addProperty("friction", block.getFriction());
            } catch (Throwable ignored) {
            }
            blocks.add(entry);
        }
        return blocks;
    }

    public static JsonArray saddles(OWWikiLang lang) {
        JsonArray saddles = new JsonArray();
        for (OWSaddleRecipe recipe : OWSaddleRecipes.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", recipe.id());
            entry.addProperty("entity_key", recipe.entityKey());
            entry.addProperty("result", BuiltInRegistries.ITEM.getKey(recipe.result().get()).toString());
            entry.addProperty("taming_threshold", recipe.tamingThreshold());
            entry.addProperty("accent_color", String.format(Locale.ROOT, "#%06X", recipe.accentColor() & 0xFFFFFF));
            JsonObject names = lang.translations(recipe.entityKey());
            if (names != null) entry.add("entity_names", names);
            JsonArray needs = new JsonArray();
            for (OWSaddleRecipe.Need need : recipe.needs()) {
                JsonObject value = new JsonObject();
                value.addProperty("count", need.count());
                JsonArray options = new JsonArray();
                for (ItemStack stack : need.ingredient().getItems()) {
                    options.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                }
                value.add("items", options);
                if (need.labelKey() != null) value.addProperty("label_key", need.labelKey());
                if (need.colorSlot() != OWSaddleRecipe.NO_COLOR_SLOT) value.addProperty("color_slot", need.colorSlot());
                needs.add(value);
            }
            entry.add("needs", needs);
            saddles.add(entry);
        }
        return saddles;
    }

    public static JsonObject worldgen() {
        JsonObject worldgen = new JsonObject();

        JsonArray biomes = new JsonArray();
        for (Field field : OWBiomes.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !ResourceKey.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                ResourceKey<?> key = (ResourceKey<?>) field.get(null);
                JsonObject entry = new JsonObject();
                entry.addProperty("constant", field.getName());
                entry.addProperty("id", key.location().toString());
                entry.addProperty("registry", key.registry().toString());
                biomes.add(entry);
            } catch (Throwable ignored) {
            }
        }
        worldgen.add("biomes", biomes);

        JsonArray spawns = new JsonArray();
        for (OWBiomeModifiers.SpawnRule rule : OWBiomeModifiers.SPAWN_RULES) {
            JsonObject entry = new JsonObject();
            entry.addProperty("biome_modifier", rule.modifier().location().toString());
            entry.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(rule.entity().get()).toString());
            entry.addProperty("weight", rule.weight());
            entry.addProperty("min_count", rule.minCount());
            entry.addProperty("max_count", rule.maxCount());
            JsonArray targets = new JsonArray();
            rule.biomes().forEach(biome -> targets.add(biome.location().toString()));
            entry.add("biomes", targets);
            spawns.add(entry);
        }
        worldgen.add("spawns", spawns);
        return worldgen;
    }

    public static JsonArray advancements(OWWikiLang lang) {
        JsonArray advancements = new JsonArray();
        for (Field field : OWAdvancements.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !ResourceLocation.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                ResourceLocation id = (ResourceLocation) field.get(null);
                JsonObject entry = new JsonObject();
                entry.addProperty("constant", field.getName());
                entry.addProperty("id", id.toString());
                JsonObject title = lang.translations("ow.advancements." + id.getPath() + ".title");
                if (title != null) entry.add("title", title);
                JsonObject description = lang.translations("ow.advancements." + id.getPath() + ".description");
                if (description != null) entry.add("description", description);
                advancements.add(entry);
            } catch (Throwable ignored) {
            }
        }
        return advancements;
    }
}
