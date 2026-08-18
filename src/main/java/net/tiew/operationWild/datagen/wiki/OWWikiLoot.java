package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.tiew.operationWild.datagen.OWEntityLootTableProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OWWikiLoot {

    private OWWikiLoot() {}

    public static Map<ResourceLocation, JsonObject> collect(HolderLookup.Provider registries) {
        Map<ResourceLocation, JsonObject> tables = new LinkedHashMap<>();
        DynamicOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        try {
            new OWEntityLootTableProvider(registries).generate((key, builder) -> {
                try {
                    LootTable table = builder.setParamSet(LootContextParamSets.ENTITY).build();
                    LootTable.DIRECT_CODEC.encodeStart(ops, table)
                            .resultOrPartial(error -> {})
                            .ifPresent(json -> tables.put(key.location(), json.getAsJsonObject()));
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return tables;
    }

    public static JsonArray summarize(JsonObject table) {
        JsonArray drops = new JsonArray();
        if (table == null || !table.has("pools")) return drops;
        for (JsonElement poolElement : table.getAsJsonArray("pools")) {
            if (!poolElement.isJsonObject()) continue;
            JsonObject pool = poolElement.getAsJsonObject();
            JsonObject inherited = new JsonObject();
            applyConditions(inherited, pool.getAsJsonArray("conditions"));
            applyFunctions(inherited, pool.getAsJsonArray("functions"));
            if (pool.has("rolls")) inherited.add("rolls", pool.get("rolls"));
            if (pool.has("entries")) {
                for (JsonElement entry : pool.getAsJsonArray("entries")) {
                    collectEntry(entry, inherited, drops);
                }
            }
        }
        return drops;
    }

    private static void collectEntry(JsonElement element, JsonObject inherited, JsonArray drops) {
        if (!element.isJsonObject()) return;
        JsonObject entry = element.getAsJsonObject();

        if (entry.has("children")) {
            for (JsonElement child : entry.getAsJsonArray("children")) collectEntry(child, inherited, drops);
            return;
        }

        String type = entry.has("type") ? entry.get("type").getAsString() : "";
        if (!type.endsWith("item")) return;

        JsonObject drop = inherited.deepCopy();
        drop.addProperty("item", entry.has("name") ? entry.get("name").getAsString() : "?");
        applyConditions(drop, entry.getAsJsonArray("conditions"));
        applyFunctions(drop, entry.getAsJsonArray("functions"));
        drops.add(drop);
    }

    private static void applyConditions(JsonObject target, JsonArray conditions) {
        if (conditions == null) return;
        for (JsonElement element : conditions) {
            if (!element.isJsonObject()) continue;
            JsonObject condition = element.getAsJsonObject();
            String type = condition.has("condition") ? condition.get("condition").getAsString() : "";
            if (type.endsWith("random_chance") && condition.has("chance")) {
                target.addProperty("chance", condition.get("chance").getAsFloat());
            } else if (type.endsWith("killed_by_player")) {
                target.addProperty("requires_player_kill", true);
            } else if (type.endsWith("entity_properties") || type.endsWith("damage_source_properties")) {
                target.addProperty("conditional", true);
            }
        }
    }

    private static void applyFunctions(JsonObject target, JsonArray functions) {
        if (functions == null) return;
        for (JsonElement element : functions) {
            if (!element.isJsonObject()) continue;
            JsonObject function = element.getAsJsonObject();
            String type = function.has("function") ? function.get("function").getAsString() : "";
            if (type.endsWith("set_count") && function.has("count")) {
                JsonElement count = function.get("count");
                if (count.isJsonObject()) {
                    JsonObject range = count.getAsJsonObject();
                    if (range.has("min")) target.add("min", range.get("min"));
                    if (range.has("max")) target.add("max", range.get("max"));
                } else {
                    target.add("min", count);
                    target.add("max", count);
                }
            } else if (type.endsWith("looting_enchant") || type.endsWith("enchanted_count_increase")) {
                target.addProperty("looting_bonus", true);
            } else if (type.endsWith("furnace_smelt")) {
                target.addProperty("smelts_when_burning", true);
            }
        }
    }
}
